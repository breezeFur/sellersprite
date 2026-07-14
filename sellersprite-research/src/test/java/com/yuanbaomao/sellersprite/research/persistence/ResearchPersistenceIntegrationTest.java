package com.yuanbaomao.sellersprite.research.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import com.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import com.yuanbaomao.sellersprite.db.dao.MarketResearchSnapshotDao;
import com.yuanbaomao.sellersprite.db.dao.impl.MarketResearchArtifactDaoImpl;
import com.yuanbaomao.sellersprite.db.dao.impl.MarketResearchJobDaoImpl;
import com.yuanbaomao.sellersprite.db.dao.impl.MarketResearchSnapshotDaoImpl;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchSnapshot;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(
        classes = ResearchPersistenceIntegrationTest.TestApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:research-persistence;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.sql.init.mode=always",
                "spring.sql.init.schema-locations=classpath:research-test-schema.sql",
                "spring.batch.jdbc.initialize-schema=always",
                "spring.batch.job.enabled=false"
        })
class ResearchPersistenceIntegrationTest {

    private static final String JOB_ID = "00000000-0000-7000-8000-000000000001";
    private static final String USER_ID = "00000000-0000-7000-8000-000000000002";

    @Autowired
    private MarketResearchJobDao jobDao;

    @Autowired
    private MarketResearchSnapshotDao snapshotDao;

    @Autowired
    private MarketResearchArtifactDao artifactDao;

    @Test
    void shouldPersistAndQueryThreeBusinessTables() {
        MarketResearchJob job = job();
        assertThat(jobDao.save(job)).isTrue();
        assertThat(jobDao.findByIdAndUserId(JOB_ID, USER_ID)).isPresent();
        assertThat(jobDao.listQueuedWithoutBatchExecution(100))
                .extracting(MarketResearchJob::getJobId)
                .containsExactly(JOB_ID);

        MarketResearchSnapshot first = snapshot("snapshot-1", "products");
        MarketResearchSnapshot second = snapshot("snapshot-2", "keywords");
        assertThat(snapshotDao.saveBatch(List.of(first, second))).isTrue();
        assertThat(snapshotDao.findByIdempotencyKey(
                JOB_ID, "COLLECT_MARKET_AND_PRODUCTS", "PRODUCT_RESEARCH", "products"))
                .isPresent();
        assertThat(snapshotDao.listByJobId(JOB_ID))
                .extracting(MarketResearchSnapshot::getSnapshotId)
                .containsExactly("snapshot-1", "snapshot-2");

        MarketResearchArtifact artifact = artifact();
        assertThat(artifactDao.save(artifact)).isTrue();
        assertThat(artifactDao.getById(artifact.getArtifactId()).getSha256()).isNull();
        artifact.setSha256("b".repeat(64));
        artifact.setArtifactStatus("PUBLISHED");
        artifact.setPublishedAt(1L);
        assertThat(artifactDao.updateById(artifact)).isTrue();
        assertThat(artifactDao.findAvailableByJobId(JOB_ID))
                .get()
                .extracting(MarketResearchArtifact::getFileName)
                .isEqualTo("report.xlsx");
    }

    private MarketResearchJob job() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setUserId(USER_ID);
        job.setReportName("持久化测试");
        job.setMarketplace("US");
        job.setKeyword("facial device");
        job.setSeedAsins("[]");
        job.setTemplateCode("market-research-v1");
        job.setDataSourceMode("MOCK");
        job.setJobStatus("QUEUED");
        job.setCurrentPhase("VALIDATE");
        job.setProgress(0);
        job.setErrorCode("");
        job.setErrorMessage("");
        audit(job);
        return job;
    }

    private MarketResearchSnapshot snapshot(String id, String businessKey) {
        MarketResearchSnapshot snapshot = new MarketResearchSnapshot();
        snapshot.setSnapshotId(id);
        snapshot.setJobId(JOB_ID);
        snapshot.setPhase("COLLECT_MARKET_AND_PRODUCTS");
        snapshot.setOperation("PRODUCT_RESEARCH");
        snapshot.setBusinessKey(businessKey);
        snapshot.setDataSourceMode("MOCK");
        snapshot.setRequestPayload("{}");
        snapshot.setResponsePayload("{}");
        snapshot.setRecordCount(1);
        snapshot.setSha256("a".repeat(64));
        snapshot.setFetchedAt(1L);
        audit(snapshot);
        return snapshot;
    }

    private MarketResearchArtifact artifact() {
        MarketResearchArtifact artifact = new MarketResearchArtifact();
        artifact.setArtifactId("00000000-0000-7000-8000-000000000003");
        artifact.setJobId(JOB_ID);
        artifact.setFileName("report.xlsx");
        artifact.setStorageKey("test/report.xlsx");
        artifact.setMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        artifact.setFileSize(0L);
        artifact.setArtifactStatus("GENERATING");
        audit(artifact);
        return artifact;
    }

    private void audit(com.yuanbaomao.sellersprite.db.entity.BaseAudit value) {
        value.setCreatedAt(1L);
        value.setUpdatedAt(1L);
        value.setCreatedBy(USER_ID);
        value.setUpdatedBy(USER_ID);
        value.setDeleted(0);
        value.setRemark("");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @MapperScan("com.yuanbaomao.sellersprite.db.mapper")
    @Import({
            MarketResearchJobDaoImpl.class,
            MarketResearchSnapshotDaoImpl.class,
            MarketResearchArtifactDaoImpl.class
    })
    static class TestApplication {
    }
}
