package com.yuanbaomao.sellersprite.research.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.yuanbaomao.sellersprite.research.config.ResearchProperties;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalReportStorageTest {

    @TempDir
    private Path temporaryDirectory;

    @Test
    void shouldResumePublishWhenDraftWasAlreadyMoved() throws Exception {
        ResearchProperties properties = new ResearchProperties();
        properties.setOutputDirectory(temporaryDirectory.toString());
        LocalReportStorage storage = new LocalReportStorage(properties);
        Path draft = storage.createDraftPath("job-001", "artifact-001");
        Files.writeString(draft, "report");
        String draftKey = storage.storageKey(draft);

        String publishedKey = storage.publish(draftKey);

        assertThat(Files.exists(draft)).isFalse();
        assertThat(Files.readString(storage.resolve(publishedKey))).isEqualTo("report");
        assertThat(storage.publish(draftKey)).isEqualTo(publishedKey);
        assertThat(storage.publish(publishedKey)).isEqualTo(publishedKey);
    }
}
