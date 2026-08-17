[CmdletBinding()]
param(
    [switch]$KeepLocalRepository
)

$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$runRoot = Join-Path ([IO.Path]::GetTempPath()) ('sellersprite-vendored-' + [Guid]::NewGuid().ToString('N'))
$localRepository = Join-Path $runRoot 'repository'
$settings = Join-Path $projectRoot '.mvn\isolated-settings.xml'
$maven = Join-Path $projectRoot 'mvnw.cmd'
$checksumManifest = Join-Path $projectRoot 'maven-repository\SHA256SUMS'
$artifacts = @(
    'yuanbaomao-base',
    'yuanbaomao-web-starter',
    'yuanbaomao-mybatis-starter',
    'yuanbaomao-cache-starter',
    'yuanbaomao-log-starter',
    'yuanbaomao-dict-starter'
)
$artifactVersions = @{
    'yuanbaomao-base' = '0.2.0'
    'yuanbaomao-web-starter' = '0.2.0'
    'yuanbaomao-mybatis-starter' = '0.2.0'
    'yuanbaomao-cache-starter' = '0.2.0'
    'yuanbaomao-log-starter' = '0.2.0'
    'yuanbaomao-dict-starter' = '0.2.0'
}

function Remove-RunRoot {
    $tempRoot = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
    $resolvedRunRoot = [IO.Path]::GetFullPath($runRoot)
    $runName = [IO.Path]::GetFileName($resolvedRunRoot)
    if (-not $resolvedRunRoot.StartsWith($tempRoot, [StringComparison]::OrdinalIgnoreCase) -or
        -not $runName.StartsWith('sellersprite-vendored-', [StringComparison]::Ordinal)) {
        throw "Refusing to delete a directory outside the verified boundary: $resolvedRunRoot"
    }
    $longRunRoot = '\\?\' + $resolvedRunRoot

    for ($attempt = 1; $attempt -le 5; $attempt++) {
        if (-not [IO.Directory]::Exists($longRunRoot)) {
            return
        }
        try {
            [IO.Directory]::Delete($longRunRoot, $true)
            return
        } catch [IO.DirectoryNotFoundException] {
            return
        } catch {
            if ($attempt -eq 5) {
                throw
            }
            Start-Sleep -Milliseconds 200
        }
    }
}

if (-not (Test-Path -LiteralPath $maven -PathType Leaf)) {
    throw "Maven Wrapper is missing: $maven"
}
if (-not (Test-Path -LiteralPath $settings -PathType Leaf)) {
    throw "Isolated Maven settings are missing: $settings"
}

foreach ($line in Get-Content -LiteralPath $checksumManifest -Encoding UTF8) {
    if ($line -notmatch '^([0-9a-f]{64})  (.+)$') {
        throw "Cannot parse SHA-256 manifest line: $line"
    }
    $expectedHash = $Matches[1]
    $relativePath = $Matches[2].Replace('/', [IO.Path]::DirectorySeparatorChar)
    $artifactPath = Join-Path $projectRoot "maven-repository\$relativePath"
    $actualHash = (Get-FileHash -LiteralPath $artifactPath -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actualHash -ne $expectedHash) {
        throw "Bundled artifact SHA-256 mismatch: $artifactPath"
    }
}

[IO.Directory]::CreateDirectory($localRepository) | Out-Null
$succeeded = $false

try {
    Push-Location $projectRoot
    try {
        & $maven -B -ntp -U -s $settings -gs $settings "-Dmaven.repo.local=$localRepository" clean verify
        if ($LASTEXITCODE -ne 0) {
            throw "Isolated Maven build failed with exit code $LASTEXITCODE"
        }
    } finally {
        Pop-Location
    }

    foreach ($artifact in $artifacts) {
        $artifactVersion = $artifactVersions[$artifact]
        $artifactDirectory = Join-Path $localRepository "cyou\yuanbaomao\$artifact\$artifactVersion"
        $trackingFile = Join-Path $artifactDirectory '_remote.repositories'
        if (-not (Test-Path -LiteralPath $trackingFile -PathType Leaf)) {
            throw "Maven repository source tracking is missing: $trackingFile"
        }
        $sourceInfo = Get-Content -LiteralPath $trackingFile -Raw -Encoding UTF8
        foreach ($extension in @('jar', 'pom')) {
            $fileName = "$artifact-$artifactVersion.$extension"
            if (-not (Test-Path -LiteralPath (Join-Path $artifactDirectory $fileName) -PathType Leaf)) {
                throw "Artifact is missing from the isolated local repository: $artifact/$fileName"
            }
            if ($sourceInfo -notmatch [regex]::Escape("$fileName>project-local=")) {
                throw "Artifact was not resolved from project-local: $artifact/$fileName"
            }
        }
    }

    $serverJars = @(Get-ChildItem -LiteralPath (Join-Path $projectRoot 'sellersprite-server\target') -Filter 'sellersprite-server-*.jar' -File)
    if ($serverJars.Count -ne 1) {
        throw "Expected one Spring Boot JAR, found $($serverJars.Count)"
    }

    $jarCommand = if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\jar.exe'))) {
        Join-Path $env:JAVA_HOME 'bin\jar.exe'
    } else {
        (Get-Command jar.exe -ErrorAction Stop).Source
    }
    $entries = @(& $jarCommand tf $serverJars[0].FullName)
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot read Spring Boot JAR: $($serverJars[0].FullName)"
    }

    $expectedLibraries = @($artifacts | ForEach-Object { "BOOT-INF/lib/$_-$($artifactVersions[$_]).jar" } | Sort-Object)
    $actualLibraries = @($entries | Where-Object { $_ -like 'BOOT-INF/lib/yuanbaomao-*.jar' } | Sort-Object)
    $libraryDifference = @(Compare-Object -ReferenceObject $expectedLibraries -DifferenceObject $actualLibraries)
    if ($libraryDifference.Count -ne 0) {
        throw "Unexpected Yuanbaomao libraries in Spring Boot JAR: $($libraryDifference | Out-String)"
    }
    if ($entries | Where-Object { $_ -match '(^|/)maven-repository/' }) {
        throw 'maven-repository was unexpectedly embedded in the Spring Boot JAR'
    }
    if ($entries | Where-Object { $_ -like '*yuanbaomao-login-starter*' }) {
        throw 'The unused login Starter was unexpectedly embedded in the Spring Boot JAR'
    }

    $succeeded = $true
    [pscustomobject]@{
        Build = 'PASS'
        PrivateArtifactsFromProjectLocal = $artifacts.Count
        BootJar = $serverJars[0].FullName
        EmbeddedPrivateLibraries = $actualLibraries.Count
        RepositoryDirectoryEmbedded = $false
        LoginStarterEmbedded = $false
    }
} finally {
    if ($succeeded -and -not $KeepLocalRepository) {
        Remove-RunRoot
    } elseif (Test-Path -LiteralPath $runRoot) {
        Write-Host "Isolated Maven local repository retained at: $runRoot"
    }
}
