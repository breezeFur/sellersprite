package cyou.yuanbaomao.sellersprite.research.storage;

import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * 第一版报告的受控本地文件存储。
 */
@Component
public class LocalReportStorage implements ReportStorage {

    private final Path root;

    public LocalReportStorage(ResearchProperties properties) {
        this.root = Path.of(properties.getOutputDirectory()).toAbsolutePath().normalize();
    }

    @Override
    public Path createDraftPath(String jobId, String artifactId) throws IOException {
        return createDraftPath(jobId, artifactId, "xlsx");
    }

    @Override
    public Path createDraftPath(String jobId, String artifactId, String extension) throws IOException {
        validateSegment(jobId, "jobId");
        validateSegment(artifactId, "artifactId");
        if (extension == null || !extension.matches("[a-z0-9]+")) {
            throw new IllegalArgumentException("报告扩展名不合法");
        }
        Path directory = checked(root.resolve(jobId));
        Files.createDirectories(directory);
        return checked(directory.resolve(artifactId + ".draft." + extension));
    }

    @Override
    public String storageKey(Path path) {
        Path checkedPath = checked(path.toAbsolutePath().normalize());
        return root.relativize(checkedPath).toString().replace('\\', '/');
    }

    @Override
    public Path resolve(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("报告存储键不能为空");
        }
        return checked(root.resolve(storageKey).normalize());
    }

    @Override
    public String publish(String draftStorageKey) throws IOException {
        Path draft = resolve(draftStorageKey);
        String fileName = draft.getFileName().toString();
        int draftMarker = fileName.lastIndexOf(".draft.");
        if (draftMarker < 0) {
            if (!Files.isRegularFile(draft)) {
                throw new IOException("已发布报告文件不存在: " + draftStorageKey);
            }
            return storageKey(draft);
        }
        String publishedName = fileName.substring(0, draftMarker)
                + fileName.substring(draftMarker + ".draft".length());
        Path published = checked(draft.resolveSibling(publishedName));
        if (Files.isRegularFile(draft)) {
            try {
                Files.move(draft, published,
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(draft, published, StandardCopyOption.REPLACE_EXISTING);
            }
        } else if (!Files.isRegularFile(published)) {
            throw new IOException("报告草稿和已发布文件均不存在: " + draftStorageKey);
        }
        return storageKey(published);
    }

    @Override
    public Resource load(String storageKey) {
        return new FileSystemResource(resolve(storageKey));
    }

    private Path checked(Path candidate) {
        if (!candidate.startsWith(root)) {
            throw new IllegalArgumentException("报告路径超出受控目录");
        }
        return candidate;
    }

    private void validateSegment(String value, String name) {
        if (value == null || !value.matches("[A-Za-z0-9-]+")) {
            throw new IllegalArgumentException(name + "包含非法路径字符");
        }
    }
}
