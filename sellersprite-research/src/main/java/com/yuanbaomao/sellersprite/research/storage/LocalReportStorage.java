package com.yuanbaomao.sellersprite.research.storage;

import com.yuanbaomao.sellersprite.research.config.ResearchProperties;
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

    private static final String DRAFT_SUFFIX = ".draft.xlsx";
    private static final String FINAL_SUFFIX = ".xlsx";

    private final Path root;

    public LocalReportStorage(ResearchProperties properties) {
        this.root = Path.of(properties.getOutputDirectory()).toAbsolutePath().normalize();
    }

    @Override
    public Path createDraftPath(String jobId, String artifactId) throws IOException {
        validateSegment(jobId, "jobId");
        validateSegment(artifactId, "artifactId");
        Path directory = checked(root.resolve(jobId));
        Files.createDirectories(directory);
        return checked(directory.resolve(artifactId + DRAFT_SUFFIX));
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
        if (fileName.endsWith(FINAL_SUFFIX) && !fileName.endsWith(DRAFT_SUFFIX)) {
            if (!Files.isRegularFile(draft)) {
                throw new IOException("已发布报告文件不存在: " + draftStorageKey);
            }
            return storageKey(draft);
        }
        if (!fileName.endsWith(DRAFT_SUFFIX)) {
            throw new IllegalArgumentException("不是合法的报告草稿文件: " + draftStorageKey);
        }
        String publishedName = fileName.substring(0, fileName.length() - DRAFT_SUFFIX.length())
                + FINAL_SUFFIX;
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
