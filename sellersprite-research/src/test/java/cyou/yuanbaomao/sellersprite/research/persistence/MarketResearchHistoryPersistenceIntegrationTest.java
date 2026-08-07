package cyou.yuanbaomao.sellersprite.research.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.dao.impl.MarketResearchAnalysisRunDaoImpl;
import cyou.yuanbaomao.sellersprite.db.dao.impl.MarketResearchArtifactDaoImpl;
import cyou.yuanbaomao.sellersprite.db.dao.impl.MarketResearchJobDaoImpl;
import cyou.yuanbaomao.sellersprite.db.entity.BaseAudit;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(
        classes = MarketResearchHistoryPersistenceIntegrationTest.TestApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:research-history;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.sql.init.mode=always",
                "spring.sql.init.schema-locations=classpath:research-test-schema.sql"
        })
class MarketResearchHistoryPersistenceIntegrationTest {

    private static final String USER_ID = "history-user-001";
    private static final String OTHER_USER_ID = "history-user-002";
    private static final String JOB_ID_ONE = "history-job-001";
    private static final String JOB_ID_TWO = "history-job-002";
    private static final long CREATED_AT = 1_722_470_400_000L;

    @Autowired
    private MarketResearchJobDao jobDao;

    @Autowired
    private MarketResearchAnalysisRunDao analysisRunDao;

    @Autowired
    private MarketResearchArtifactDao artifactDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanBusinessTables() {
        jdbcTemplate.update("DELETE FROM market_research_artifact");
        jdbcTemplate.update("DELETE FROM market_research_analysis_run");
        jdbcTemplate.update("DELETE FROM market_research_job");
    }

    @Test
    void shouldScopeFilterAndStablyPageOwnedHistory() {
        assertThat(jobDao.saveBatch(List.of(
                        job(JOB_ID_ONE, USER_ID, "facial device market", "beauty", "SUCCEEDED"),
                        job(JOB_ID_TWO, USER_ID, "Second report", "facial device", "SUCCEEDED"),
                        job("history-job-003", USER_ID, "Facial failed", "facial", "FAILED"),
                        job("history-job-004", OTHER_USER_ID, "Facial other user", "facial", "SUCCEEDED"))))
                .isTrue();

        Page<MarketResearchJob> firstPage = jobDao.pageByUserId(
                USER_ID, "facial", "SUCCEEDED", "US", "2026-07", 1L, 1L);
        Page<MarketResearchJob> secondPage = jobDao.pageByUserId(
                USER_ID, "facial", "SUCCEEDED", "US", "2026-07", 2L, 1L);

        assertThat(firstPage.getTotal()).isEqualTo(2L);
        assertThat(firstPage.getRecords())
                .extracting(MarketResearchJob::getJobId)
                .containsExactly(JOB_ID_TWO);
        assertThat(secondPage.getRecords())
                .extracting(MarketResearchJob::getJobId)
                .containsExactly(JOB_ID_ONE);
    }

    @Test
    void shouldTreatLikeWildcardsAsLiteralSearchText() {
        assertThat(jobDao.saveBatch(List.of(
                        job(JOB_ID_ONE, USER_ID, "Market with 20% share", "percent", "SUCCEEDED"),
                        job(JOB_ID_TWO, USER_ID, "Market with 200 share", "number", "SUCCEEDED"),
                        job("history-job-003", USER_ID, "Literal_under score", "underscore", "SUCCEEDED"),
                        job("history-job-004", USER_ID, "LiteralXunder score", "letter", "SUCCEEDED"))))
                .isTrue();

        Page<MarketResearchJob> percentPage = jobDao.pageByUserId(
                USER_ID, "20%", null, null, null, 1L, 20L);
        Page<MarketResearchJob> underscorePage = jobDao.pageByUserId(
                USER_ID, "_under", null, null, null, 1L, 20L);

        assertThat(percentPage.getRecords())
                .extracting(MarketResearchJob::getJobId)
                .containsExactly(JOB_ID_ONE);
        assertThat(underscorePage.getRecords())
                .extracting(MarketResearchJob::getJobId)
                .containsExactly("history-job-003");
    }

    @Test
    void shouldBatchLoadOnlyOwnedAnalysisAndPublishedArtifacts() {
        assertThat(jobDao.saveBatch(List.of(
                        job(JOB_ID_ONE, USER_ID, "First", "one", "SUCCEEDED"),
                        job(JOB_ID_TWO, USER_ID, "Second", "two", "SUCCEEDED"))))
                .isTrue();
        assertThat(analysisRunDao.saveBatch(List.of(
                        analysisRun("analysis-old", JOB_ID_ONE, USER_ID, CREATED_AT),
                        analysisRun("analysis-latest", JOB_ID_ONE, USER_ID, CREATED_AT + 1L),
                        analysisRun("analysis-other-user", JOB_ID_ONE, OTHER_USER_ID, CREATED_AT + 2L))))
                .isTrue();
        assertThat(artifactDao.saveBatch(List.of(
                        artifact("artifact-published", JOB_ID_ONE, "PUBLISHED", CREATED_AT + 2L),
                        artifact("artifact-generating", JOB_ID_ONE, "GENERATING", CREATED_AT + 3L),
                        artifact("artifact-other-job", JOB_ID_TWO, "PUBLISHED", CREATED_AT + 4L))))
                .isTrue();

        List<MarketResearchAnalysisRun> analysisRuns = analysisRunDao.listByJobIdsAndUserId(
                List.of(JOB_ID_ONE), USER_ID);
        List<MarketResearchArtifact> artifacts = artifactDao.listAvailableByJobIds(
                List.of(JOB_ID_ONE));

        assertThat(analysisRuns)
                .extracting(MarketResearchAnalysisRun::getAnalysisRunId)
                .containsExactly("analysis-latest", "analysis-old");
        assertThat(artifacts)
                .extracting(MarketResearchArtifact::getArtifactId)
                .containsExactly("artifact-published");
    }

    private MarketResearchJob job(
            String jobId, String userId, String reportName, String keyword, String status) {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(jobId);
        job.setUserId(userId);
        job.setReportName(reportName);
        job.setMarketplace("US");
        job.setNodeIdPath("172282:281407");
        job.setResearchMonth("2026-07");
        job.setKeyword(keyword);
        job.setSeedAsins("[]");
        job.setCollectionConfig("{}");
        job.setTemplateCode("market-research-v1");
        job.setDataSourceMode("MOCK");
        job.setWorkflowVersion("market-research-graph-v1");
        job.setJobStatus(status);
        job.setCurrentNode("completed");
        job.setProgress(100);
        job.setAttemptCount(1);
        job.setMaxAttempts(3);
        job.setNextRunAt(CREATED_AT);
        job.setErrorCode("");
        job.setErrorMessage("");
        audit(job, userId, CREATED_AT);
        return job;
    }

    private MarketResearchAnalysisRun analysisRun(
            String analysisRunId, String jobId, String userId, long createdAt) {
        MarketResearchAnalysisRun analysisRun = new MarketResearchAnalysisRun();
        analysisRun.setAnalysisRunId(analysisRunId);
        analysisRun.setJobId(jobId);
        analysisRun.setUserId(userId);
        analysisRun.setRunType("INITIAL");
        analysisRun.setAnalysisGoal("分析市场机会");
        analysisRun.setRunStatus("SUCCEEDED");
        analysisRun.setCurrentPhase("completed");
        analysisRun.setProgress(100);
        analysisRun.setAttemptCount(1);
        analysisRun.setMaxAttempts(3);
        analysisRun.setNextRunAt(createdAt);
        analysisRun.setModelCallCount(1);
        analysisRun.setEventCount(1);
        analysisRun.setErrorCode("");
        analysisRun.setErrorMessage("");
        audit(analysisRun, userId, createdAt);
        return analysisRun;
    }

    private MarketResearchArtifact artifact(
            String artifactId, String jobId, String status, long createdAt) {
        MarketResearchArtifact artifact = new MarketResearchArtifact();
        artifact.setArtifactId(artifactId);
        artifact.setJobId(jobId);
        artifact.setArtifactScopeId(artifactId);
        artifact.setWorkflowVersion("market-research-graph-v1");
        artifact.setArtifactType("AI_REPORT");
        artifact.setFileName(artifactId + ".md");
        artifact.setStorageKey("history/" + artifactId + ".md");
        artifact.setMediaType("text/markdown");
        artifact.setFileSize(128L);
        artifact.setSha256("a".repeat(64));
        artifact.setArtifactStatus(status);
        artifact.setPublishedAt("PUBLISHED".equals(status) ? createdAt : null);
        audit(artifact, USER_ID, createdAt);
        return artifact;
    }

    private void audit(BaseAudit value, String userId, long createdAt) {
        value.setCreatedAt(createdAt);
        value.setUpdatedAt(createdAt);
        value.setCreatedBy(userId);
        value.setUpdatedBy(userId);
        value.setDeleted(0);
        value.setRemark("");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @MapperScan("cyou.yuanbaomao.sellersprite.db.mapper")
    @Import({
            MarketResearchJobDaoImpl.class,
            MarketResearchAnalysisRunDaoImpl.class,
            MarketResearchArtifactDaoImpl.class
    })
    static class TestApplication {
    }
}
