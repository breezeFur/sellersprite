package cyou.yuanbaomao.sellersprite.research.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchEventDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.dao.impl.MarketResearchAnalysisRunDaoImpl;
import cyou.yuanbaomao.sellersprite.db.dao.impl.MarketResearchArtifactDaoImpl;
import cyou.yuanbaomao.sellersprite.db.dao.impl.MarketResearchEventDaoImpl;
import cyou.yuanbaomao.sellersprite.db.dao.impl.MarketResearchJobDaoImpl;
import cyou.yuanbaomao.sellersprite.db.entity.BaseAudit;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchEvent;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(
        classes = MarketResearchAnalysisPersistenceIntegrationTest.TestApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:research-analysis-persistence;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.sql.init.mode=always",
                "spring.sql.init.schema-locations=classpath:research-test-schema.sql"
        })
class MarketResearchAnalysisPersistenceIntegrationTest {

    private static final String JOB_ID = "00000000-0000-7000-8000-000000000101";
    private static final String USER_ID = "00000000-0000-7000-8000-000000000102";
    private static final String RUN_ID = "00000000-0000-7000-8000-000000000103";
    private static final String RETRY_RUN_ID = "00000000-0000-7000-8000-000000000104";
    private static final String CANCEL_RUN_ID = "00000000-0000-7000-8000-000000000105";
    private static final String RUNNING_CANCEL_ID = "00000000-0000-7000-8000-000000000106";
    private static final String TOKEN_A = "00000000-0000-7000-8000-000000000201";
    private static final String TOKEN_B = "00000000-0000-7000-8000-000000000202";
    private static final long NOW = 1_722_000_000_000L;

    @Autowired
    private MarketResearchJobDao jobDao;

    @Autowired
    private MarketResearchAnalysisRunDao analysisRunDao;

    @Autowired
    private MarketResearchEventDao eventDao;

    @Autowired
    private MarketResearchArtifactDao artifactDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void cleanBusinessTables() {
        jdbcTemplate.update("DELETE FROM market_research_event");
        jdbcTemplate.update("DELETE FROM market_research_artifact");
        jdbcTemplate.update("DELETE FROM market_research_analysis_run");
        jdbcTemplate.update("DELETE FROM market_research_dataset");
        jdbcTemplate.update("DELETE FROM market_research_node_execution");
        jdbcTemplate.update("DELETE FROM market_research_job");
        jdbcTemplate.update("DELETE FROM ai_conversation");
    }

    @Test
    void shouldFenceAnalysisRunLifecycleWithExecutionToken() {
        assertThat(jobDao.save(job())).isTrue();
        MarketResearchAnalysisRun run = analysisRun(RUN_ID, "INITIAL", "WAITING_RESEARCH");
        assertThat(analysisRunDao.save(run)).isTrue();

        assertThat(analysisRunDao.findByIdAndUserId(RUN_ID, USER_ID)).isPresent();
        assertThat(analysisRunDao.listByJobIdAndUserId(JOB_ID, USER_ID))
                .extracting(MarketResearchAnalysisRun::getAnalysisRunId)
                .containsExactly(RUN_ID);
        assertThat(analysisRunDao.listDispatchCandidates(NOW, 20)).isEmpty();
        assertThat(analysisRunDao.tryStartInitial(
                        RUN_ID, "parent-graph", TOKEN_A, NOW, NOW + 60_000L))
                .isTrue();
        assertThat(analysisRunDao.heartbeat(
                        RUN_ID, "parent-graph", TOKEN_B, NOW + 1_000L, NOW + 61_000L))
                .isFalse();
        assertThat(analysisRunDao.heartbeat(
                        RUN_ID, "parent-graph", TOKEN_A, NOW + 1_000L, NOW + 61_000L))
                .isTrue();
        assertThat(analysisRunDao.updateProgress(
                        RUN_ID, "parent-graph", TOKEN_A, "sheet_analysis", 45))
                .isTrue();
        assertThat(analysisRunDao.incrementCounters(RUN_ID, "parent-graph", TOKEN_A, 2, 5))
                .isTrue();
        assertThat(analysisRunDao.saveFinalSummary(
                        RUN_ID, "parent-graph", TOKEN_B, "stale saved summary"))
                .isFalse();
        assertThat(analysisRunDao.saveFinalSummary(
                        RUN_ID, "parent-graph", TOKEN_A, "durable saved summary"))
                .isTrue();

        MarketResearchAnalysisRun expired = analysisRunDao.getById(RUN_ID);
        expired.setLeaseUntil(NOW - 1L);
        assertThat(analysisRunDao.updateById(expired)).isTrue();
        assertThat(analysisRunDao.tryStartInitial(
                        RUN_ID, "worker-b", TOKEN_B, NOW, NOW + 60_000L))
                .isTrue();
        assertThat(analysisRunDao.getById(RUN_ID).getFinalSummary())
                .isEqualTo("durable saved summary");
        assertThat(analysisRunDao.markSucceeded(
                        RUN_ID, "worker-a", TOKEN_A, "stale summary", NOW + 2_000L))
                .isFalse();
        assertThat(analysisRunDao.markSucceeded(
                        RUN_ID, "worker-b", TOKEN_B, "final summary", NOW + 2_000L))
                .isTrue();
        assertThat(analysisRunDao.getById(RUN_ID))
                .returns("SUCCEEDED", MarketResearchAnalysisRun::getRunStatus)
                .returns("final summary", MarketResearchAnalysisRun::getFinalSummary)
                .returns(2, MarketResearchAnalysisRun::getAttemptCount)
                .returns(2, MarketResearchAnalysisRun::getModelCallCount)
                .returns(5, MarketResearchAnalysisRun::getEventCount)
                .returns(null, MarketResearchAnalysisRun::getExecutionToken);

        MarketResearchAnalysisRun retry = analysisRun(RETRY_RUN_ID, "RETRY", "QUEUED");
        retry.setParentRunId(RUN_ID);
        retry.setCreatedAt(NOW + 10L);
        assertThat(analysisRunDao.save(retry)).isTrue();
        assertThat(analysisRunDao.tryClaim(
                        RETRY_RUN_ID, "worker-c", TOKEN_A, NOW, NOW + 60_000L))
                .isTrue();
        assertThat(analysisRunDao.markRetryWait(
                        RETRY_RUN_ID,
                        "worker-c",
                        TOKEN_A,
                        NOW + 5_000L,
                        "MODEL_TEMPORARY",
                        "temporary"))
                .isTrue();
        assertThat(analysisRunDao.tryClaim(
                        RETRY_RUN_ID, "worker-d", TOKEN_B, NOW + 5_000L, NOW + 65_000L))
                .isTrue();
        assertThat(analysisRunDao.markFailed(
                        RETRY_RUN_ID,
                        "worker-d",
                        TOKEN_A,
                        NOW + 6_000L,
                        "MODEL_FAILED",
                        "failed"))
                .isFalse();
        assertThat(analysisRunDao.markFailed(
                        RETRY_RUN_ID,
                        "worker-d",
                        TOKEN_B,
                        NOW + 6_000L,
                        "MODEL_FAILED",
                        "failed"))
                .isTrue();
        assertThat(analysisRunDao.findLatestByJobIdAndUserId(JOB_ID, USER_ID))
                .get()
                .extracting(MarketResearchAnalysisRun::getAnalysisRunId)
                .isEqualTo(RETRY_RUN_ID);

        assertThat(analysisRunDao.save(
                        analysisRun(CANCEL_RUN_ID, "FOLLOW_UP", "WAITING_RESEARCH")))
                .isTrue();
        assertThat(analysisRunDao.cancelPending(CANCEL_RUN_ID, USER_ID, NOW + 7_000L)).isTrue();
        assertThat(analysisRunDao.getById(CANCEL_RUN_ID))
                .returns("CANCELLED", MarketResearchAnalysisRun::getRunStatus);

        assertThat(analysisRunDao.save(
                        analysisRun(RUNNING_CANCEL_ID, "FOLLOW_UP", "QUEUED")))
                .isTrue();
        assertThat(analysisRunDao.tryClaim(
                        RUNNING_CANCEL_ID, "worker-e", TOKEN_A, NOW, NOW + 60_000L))
                .isTrue();
        assertThat(analysisRunDao.requestRunningCancel(
                        RUNNING_CANCEL_ID, USER_ID, NOW + 8_000L))
                .isTrue();
        assertThat(analysisRunDao.markCancelled(
                        RUNNING_CANCEL_ID, "worker-e", TOKEN_B, NOW + 9_000L))
                .isFalse();
        assertThat(analysisRunDao.markCancelled(
                        RUNNING_CANCEL_ID, "worker-e", TOKEN_A, NOW + 9_000L))
                .isTrue();
    }

    @Test
    void shouldPersistReplayableEventsAndKeepArtifactUniquenessPerScope() {
        assertThat(jobDao.save(job())).isTrue();
        assertThat(analysisRunDao.save(analysisRun(RUN_ID, "INITIAL", "SUCCEEDED"))).isTrue();
        assertThat(analysisRunDao.save(analysisRun(RETRY_RUN_ID, "RETRY", "SUCCEEDED")))
                .isTrue();

        MarketResearchEvent first = event(
                "00000000-0000-7000-8000-000000000301", "plan", "开始分析");
        MarketResearchEvent second = event(
                "00000000-0000-7000-8000-000000000302", "sheet_think_delta", "增量");
        MarketResearchEvent savedFirst = eventDao.saveEvent(first);
        MarketResearchEvent savedSecond = eventDao.saveEvent(second);

        assertThat(savedFirst.getSequenceNo()).isPositive();
        assertThat(savedSecond.getSequenceNo()).isGreaterThan(savedFirst.getSequenceNo());
        assertThat(eventDao.listByJobIdAfterSequence(JOB_ID, savedFirst.getSequenceNo()))
                .extracting(MarketResearchEvent::getEventId)
                .containsExactly(second.getEventId());
        assertThat(eventDao.findLatestSequenceByJobId(JOB_ID))
                .isEqualTo(savedSecond.getSequenceNo());

        MarketResearchArtifact excel = artifact(
                "00000000-0000-7000-8000-000000000401", null, "EVIDENCE_WORKBOOK");
        assertThat(artifactDao.save(excel)).isTrue();
        assertThat(artifactDao.findByJobIdAndType(JOB_ID, "EVIDENCE_WORKBOOK"))
                .get()
                .extracting(MarketResearchArtifact::getArtifactId)
                .isEqualTo(excel.getArtifactId());
        assertThatThrownBy(() -> artifactDao.save(artifact(
                        "00000000-0000-7000-8000-000000000402", null, "EVIDENCE_WORKBOOK")))
                .isInstanceOf(DataAccessException.class);

        MarketResearchArtifact initialReport = artifact(
                "00000000-0000-7000-8000-000000000403", RUN_ID, "AI_ANALYSIS_REPORT");
        MarketResearchArtifact retryReport = artifact(
                "00000000-0000-7000-8000-000000000404", RETRY_RUN_ID, "AI_ANALYSIS_REPORT");
        assertThat(artifactDao.save(initialReport)).isTrue();
        assertThat(artifactDao.save(retryReport)).isTrue();
        assertThat(artifactDao.findByAnalysisRunIdAndType(RUN_ID, "AI_ANALYSIS_REPORT"))
                .get()
                .extracting(MarketResearchArtifact::getArtifactId)
                .isEqualTo(initialReport.getArtifactId());
        assertThatThrownBy(() -> artifactDao.save(artifact(
                        "00000000-0000-7000-8000-000000000405",
                        RUN_ID,
                        "AI_ANALYSIS_REPORT")))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void shouldExposeRequiredTablesIndexesAndForeignKeys() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            assertThat(tableNames(metadata))
                    .contains(
                            "MARKET_RESEARCH_ANALYSIS_RUN",
                            "MARKET_RESEARCH_EVENT",
                            "MARKET_RESEARCH_ARTIFACT");
            assertThat(indexNames(metadata, "MARKET_RESEARCH_ANALYSIS_RUN"))
                    .contains(
                            "IDX_MARKET_RESEARCH_ANALYSIS_DISPATCH",
                            "IDX_MARKET_RESEARCH_ANALYSIS_TOKEN_LEASE");
            Set<String> eventIndexes = indexNames(metadata, "MARKET_RESEARCH_EVENT");
            assertThat(eventIndexes).contains("IDX_MARKET_RESEARCH_EVENT_JOB_SEQUENCE");
            assertThat(eventIndexes)
                    .anyMatch(name -> name.startsWith("UK_MARKET_RESEARCH_EVENT_SEQUENCE"));
            Set<String> artifactIndexes = indexNames(metadata, "MARKET_RESEARCH_ARTIFACT");
            assertThat(artifactIndexes).contains("IDX_MARKET_RESEARCH_ARTIFACT_ANALYSIS_RUN");
            assertThat(artifactIndexes)
                    .anyMatch(name -> name.startsWith("UK_MARKET_RESEARCH_ARTIFACT_SCOPE"));
            assertThat(foreignKeyNames(metadata, "MARKET_RESEARCH_ANALYSIS_RUN"))
                    .contains("FK_MARKET_RESEARCH_ANALYSIS_JOB");
            assertThat(foreignKeyNames(metadata, "MARKET_RESEARCH_EVENT"))
                    .contains(
                            "FK_MARKET_RESEARCH_EVENT_JOB",
                            "FK_MARKET_RESEARCH_EVENT_ANALYSIS_RUN");
            assertThat(foreignKeyNames(metadata, "MARKET_RESEARCH_ARTIFACT"))
                    .contains(
                            "FK_MARKET_RESEARCH_ARTIFACT_JOB",
                            "FK_MARKET_RESEARCH_ARTIFACT_ANALYSIS_RUN");
        }
    }

    private Set<String> tableNames(DatabaseMetaData metadata) throws Exception {
        Set<String> names = new HashSet<>();
        try (ResultSet result = metadata.getTables(null, null, "%", new String[] {"TABLE"})) {
            while (result.next()) {
                names.add(result.getString("TABLE_NAME").toUpperCase());
            }
        }
        return names;
    }

    private Set<String> indexNames(DatabaseMetaData metadata, String tableName) throws Exception {
        Set<String> names = new HashSet<>();
        try (ResultSet result = metadata.getIndexInfo(null, null, tableName, false, false)) {
            while (result.next()) {
                String name = result.getString("INDEX_NAME");
                if (name != null) {
                    names.add(name.toUpperCase());
                }
            }
        }
        return names;
    }

    private Set<String> foreignKeyNames(DatabaseMetaData metadata, String tableName)
            throws Exception {
        Set<String> names = new HashSet<>();
        try (ResultSet result = metadata.getImportedKeys(null, null, tableName)) {
            while (result.next()) {
                names.add(result.getString("FK_NAME").toUpperCase());
            }
        }
        return names;
    }

    private MarketResearchJob job() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setUserId(USER_ID);
        job.setReportName("分析持久化测试");
        job.setMarketplace("US");
        job.setNodeIdPath("172282:281407");
        job.setResearchMonth("2026-07");
        job.setKeyword("facial device");
        job.setSeedAsins("[]");
        job.setCollectionConfig("{}");
        job.setTemplateCode("market-research-v1");
        job.setDataSourceMode("MOCK");
        job.setWorkflowVersion(ResearchConstants.WORKFLOW_VERSION);
        job.setJobStatus("SUCCEEDED");
        job.setCurrentNode("publishArtifact");
        job.setProgress(100);
        job.setAttemptCount(1);
        job.setMaxAttempts(4);
        job.setNextRunAt(NOW);
        job.setErrorCode("");
        job.setErrorMessage("");
        audit(job);
        return job;
    }

    private MarketResearchAnalysisRun analysisRun(String id, String type, String status) {
        MarketResearchAnalysisRun run = new MarketResearchAnalysisRun();
        run.setAnalysisRunId(id);
        run.setJobId(JOB_ID);
        run.setUserId(USER_ID);
        run.setRunType(type);
        run.setAnalysisGoal("给出市场机会与风险");
        run.setRunStatus(status);
        run.setCurrentPhase("WAITING_RESEARCH".equals(status) ? "waiting_research" : "queued");
        run.setProgress(0);
        run.setAttemptCount(0);
        run.setMaxAttempts(3);
        run.setNextRunAt(NOW);
        run.setModelCallCount(0);
        run.setEventCount(0);
        run.setErrorCode("");
        run.setErrorMessage("");
        audit(run);
        return run;
    }

    private MarketResearchEvent event(String id, String type, String message) {
        MarketResearchEvent event = new MarketResearchEvent();
        event.setEventId(id);
        event.setJobId(JOB_ID);
        event.setAnalysisRunId(RUN_ID);
        event.setScope("ANALYSIS");
        event.setEventType(type);
        event.setPhase("sheet_analysis");
        event.setMessage(message);
        event.setPayload("{}");
        event.setTerminal(0);
        audit(event);
        return event;
    }

    private MarketResearchArtifact artifact(String id, String analysisRunId, String type) {
        MarketResearchArtifact artifact = new MarketResearchArtifact();
        artifact.setArtifactId(id);
        artifact.setJobId(JOB_ID);
        artifact.setAnalysisRunId(analysisRunId);
        artifact.setArtifactScopeId(analysisRunId == null ? JOB_ID : analysisRunId);
        artifact.setWorkflowVersion(ResearchConstants.WORKFLOW_VERSION);
        artifact.setArtifactType(type);
        artifact.setFileName(id + ".md");
        artifact.setStorageKey("test/" + id + ".md");
        artifact.setMediaType("text/markdown");
        artifact.setFileSize(128L);
        artifact.setSha256("a".repeat(64));
        artifact.setArtifactStatus("PUBLISHED");
        artifact.setPublishedAt(NOW);
        audit(artifact);
        return artifact;
    }

    private void audit(BaseAudit value) {
        value.setCreatedAt(NOW);
        value.setUpdatedAt(NOW);
        value.setCreatedBy(USER_ID);
        value.setUpdatedBy(USER_ID);
        value.setDeleted(0);
        value.setRemark("");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @MapperScan("cyou.yuanbaomao.sellersprite.db.mapper")
    @Import({
            MarketResearchJobDaoImpl.class,
            MarketResearchAnalysisRunDaoImpl.class,
            MarketResearchEventDaoImpl.class,
            MarketResearchArtifactDaoImpl.class
    })
    static class TestApplication {
    }
}
