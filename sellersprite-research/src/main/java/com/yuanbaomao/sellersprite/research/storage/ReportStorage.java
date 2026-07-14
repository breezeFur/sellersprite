package com.yuanbaomao.sellersprite.research.storage;

import java.io.IOException;
import java.nio.file.Path;
import org.springframework.core.io.Resource;

/**
 * 市场调研报告文件存储边界。
 */
public interface ReportStorage {

    Path createDraftPath(String jobId, String artifactId) throws IOException;

    String storageKey(Path path);

    Path resolve(String storageKey);

    String publish(String draftStorageKey) throws IOException;

    Resource load(String storageKey);
}
