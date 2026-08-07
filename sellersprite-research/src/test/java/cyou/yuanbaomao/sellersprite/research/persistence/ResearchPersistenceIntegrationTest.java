package cyou.yuanbaomao.sellersprite.research.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchDatasetDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchEventDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchNodeExecutionDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchStageInputDao;
import cyou.yuanbaomao.sellersprite.db.dao.impl.MarketResearchArtifactDaoImpl;
import cyou.yuanbaomao.sellersprite.db.dao.impl.MarketResearchDatasetDaoImpl;
import cyou.yuanbaomao.sellersprite.db.dao.impl.MarketResearchEventDaoImpl;
import cyou.yuanbaomao.sellersprite.db.dao.impl.MarketResearchJobDaoImpl;
import cyou.yuanbaomao.sellersprite.db.dao.impl.MarketResearchNodeExecutionDaoImpl;
import cyou.yuanbaomao.sellersprite.db.dao.impl.MarketResearchStageInputDaoImpl;
import cyou.yuanbaomao.sellersprite.db.entity.BaseAudit;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchEvent;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchNodeExecution;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchStageInput;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(
        classes = ResearchPersistenceIntegrationTest.TestApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:research-persistence;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.sql.init.mode=always",
                "spring.sql.init.schema-locations=classpath:research-test-schema.sql"
        })
class ResearchPersistenceIntegrationTest {

    private static final String JOB_ID = "00000000-0000-7000-8000-000000000001";
    private static final String USER_ID = "00000000-0000-7000-8000-000000000002";
    private static final String TOKEN_A = "execution-token-a";
    private static final String TOKEN_B = "execution-token-b";
    private static final String TOKEN_C = "execution-token-c";
    private static final String COLLECTION_CONFIG =
            "{\"collectReviews\":{\"pagination\":{\"targetCountPerAsin\":100}}}";
    private static final long NOW = 1_720_000_000_000L;

    @Autowired
    private MarketResearchJobDao jobDao;

    @Autowired
    private MarketResearchNodeExecutionDao nodeExecutionDao;

    @Autowired
    private MarketResearchDatasetDao datasetDao;

    @Autowired
    private MarketResearchArtifactDao artifactDao;

    @Autowired
    private MarketResearchEventDao eventDao;

    @Autowired
    private MarketResearchStageInputDao stageInputDao;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanBusinessTables() {
        jdbcTemplate.update("DELETE FROM market_research_event");
        jdbcTemplate.update("DELETE FROM market_research_event_stream_lock");
        jdbcTemplate.update("DELETE FROM market_research_artifact");
        jdbcTemplate.update("DELETE FROM market_research_stage_input");
        jdbcTemplate.update("DELETE FROM market_research_dataset");
        jdbcTemplate.update("DELETE FROM market_research_node_execution");
        jdbcTemplate.update("DELETE FROM market_research_job");
    }

    @Test
    void shouldPersistAndQueryFourBusinessTables() {
        assertThat(jobDao.save(job())).isTrue();
        assertThat(jobDao.findByIdAndUserId(JOB_ID, USER_ID))
                .get()
                .extracting(MarketResearchJob::getCollectionConfig)
                .isEqualTo(COLLECTION_CONFIG);

        MarketResearchNodeExecution execution = nodeExecution();
        assertThat(nodeExecutionDao.save(execution)).isTrue();
        assertThat(nodeExecutionDao.nextNodeAttempt(JOB_ID, "validate", 1)).isEqualTo(2);
        assertThat(nodeExecutionDao.listByJobId(JOB_ID))
                .extracting(MarketResearchNodeExecution::getExecutionId)
                .containsExactly(execution.getExecutionId());

        MarketResearchDataset first = dataset("dataset-1", "products", "request-a");
        MarketResearchDataset second = dataset("dataset-2", "keywords", "request-b");
        assertThat(datasetDao.saveBatch(List.of(first, second))).isTrue();
        assertThat(datasetDao.findByIdempotencyKey(
                        JOB_ID, "collectMarketAndProducts", "PRODUCT_RESEARCH", "products", "request-a"))
                .isPresent();
        assertThat(datasetDao.listByJobId(JOB_ID))
                .extracting(MarketResearchDataset::getDatasetId)
                .containsExactly("dataset-1", "dataset-2");

        MarketResearchArtifact artifact = artifact();
        assertThat(artifactDao.save(artifact)).isTrue();
        assertThat(artifactDao.findByJobIdAndType(
                JOB_ID, ResearchConstants.ARTIFACT_TYPE_STAGE1_EVIDENCE_WORKBOOK)).isPresent();
        artifact.setSha256("b".repeat(64));
        artifact.setFileSize(1024L);
        artifact.setArtifactStatus("PUBLISHED");
        artifact.setPublishedAt(NOW);
        assertThat(artifactDao.updateById(artifact)).isTrue();
        assertThat(artifactDao.findAvailableByJobIdAndType(
                JOB_ID, ResearchConstants.ARTIFACT_TYPE_STAGE1_EVIDENCE_WORKBOOK))
                .get()
                .extracting(MarketResearchArtifact::getFileName)
                .isEqualTo("evidence.xlsx");
    }

    @Test
    void shouldAtomicallyClaimTakeOverExpiredLeaseAndRetryFailedJob() {
        assertThat(jobDao.save(job())).isTrue();
        assertThat(jobDao.listDispatchCandidates(NOW, 20))
                .extracting(MarketResearchJob::getJobId)
                .containsExactly(JOB_ID);

        assertThat(jobDao.tryClaim(JOB_ID, "worker-a", TOKEN_A, NOW, NOW + 60_000L)).isTrue();
        assertThat(jobDao.tryClaim(JOB_ID, "worker-b", TOKEN_B, NOW, NOW + 60_000L)).isFalse();
        assertThat(jobDao.heartbeat(JOB_ID, "worker-b", TOKEN_B, NOW + 1_000L, NOW + 61_000L))
                .isFalse();
        assertThat(jobDao.heartbeat(JOB_ID, "worker-a", TOKEN_A, NOW + 1_000L, NOW + 61_000L))
                .isTrue();
        assertThat(jobDao.getById(JOB_ID))
                .returns("RUNNING", MarketResearchJob::getJobStatus)
                .returns(1, MarketResearchJob::getAttemptCount)
                .returns("worker-a", MarketResearchJob::getExecutionOwner)
                .returns(TOKEN_A, MarketResearchJob::getExecutionToken);

        MarketResearchJob expired = jobDao.getById(JOB_ID);
        expired.setLeaseUntil(NOW - 1L);
        assertThat(jobDao.updateById(expired)).isTrue();
        assertThat(jobDao.listDispatchCandidates(NOW, 20))
                .extracting(MarketResearchJob::getJobId)
                .containsExactly(JOB_ID);
        assertThat(jobDao.tryClaim(JOB_ID, "worker-b", TOKEN_B, NOW, NOW + 60_000L)).isTrue();
        assertThat(jobDao.getById(JOB_ID))
                .returns(2, MarketResearchJob::getAttemptCount)
                .returns("worker-b", MarketResearchJob::getExecutionOwner)
                .returns(TOKEN_B, MarketResearchJob::getExecutionToken);

        assertThat(jobDao.markRetryWait(
                        JOB_ID, "worker-b", TOKEN_B, NOW + 5_000L, "R500", "temporary failure"))
                .isTrue();
        assertThat(jobDao.tryClaim(JOB_ID, "worker-c", TOKEN_C, NOW + 4_999L, NOW + 64_999L))
                .isFalse();
        assertThat(jobDao.tryClaim(JOB_ID, "worker-c", TOKEN_C, NOW + 5_000L, NOW + 65_000L))
                .isTrue();
        assertThat(jobDao.markFailed(
                        JOB_ID, "worker-c", TOKEN_C, NOW + 6_000L, "R500", "failed"))
                .isTrue();
        assertThat(jobDao.retryFailed(JOB_ID, USER_ID, NOW + 7_000L)).isTrue();
        assertThat(jobDao.getById(JOB_ID))
                .returns("QUEUED", MarketResearchJob::getJobStatus)
                .returns(0, MarketResearchJob::getAttemptCount)
                .returns(null, MarketResearchJob::getExecutionOwner)
                .returns("", MarketResearchJob::getErrorCode);
    }

    @Test
    void shouldListOnlyActiveRemoteJobsForMarketTrendCacheWarmup() {
        MarketResearchJob job = job();
        job.setDataSourceMode("REMOTE");
        assertThat(jobDao.save(job)).isTrue();

        assertThat(jobDao.listMarketTrendCacheWarmupCandidates())
                .extracting(MarketResearchJob::getJobId)
                .containsExactly(JOB_ID);

        job.setJobStatus("WAITING_INPUT");
        assertThat(jobDao.updateById(job)).isTrue();
        assertThat(jobDao.listMarketTrendCacheWarmupCandidates()).isEmpty();
    }

    @Test
    void shouldPersistHumanInputAndRequeueWaitingJob() {
        assertThat(jobDao.save(job())).isTrue();
        assertThat(jobDao.tryClaim(JOB_ID, "worker-a", TOKEN_A, NOW, NOW + 60_000L)).isTrue();
        assertThat(jobDao.markWaitingInput(
                        JOB_ID,
                        "worker-a",
                        TOKEN_A,
                        "productSelectionGate",
                        "SCREENING",
                        "PRODUCT_SELECTION"))
                .isTrue();
        assertThat(jobDao.getById(JOB_ID))
                .returns("WAITING_INPUT", MarketResearchJob::getJobStatus)
                .returns("SCREENING", MarketResearchJob::getCurrentStage)
                .returns("PRODUCT_SELECTION", MarketResearchJob::getWaitingInputType)
                .returns(null, MarketResearchJob::getExecutionToken)
                .returns(null, MarketResearchJob::getLeaseUntil);

        MarketResearchStageInput input = stageInput();
        assertThat(stageInputDao.save(input)).isTrue();
        assertThat(stageInputDao.find(JOB_ID, "SCREENING", "PRODUCT_SELECTION"))
                .get()
                .returns("ENTER", MarketResearchStageInput::getDecision)
                .returns("{\"selectedAsins\":[\"B000000001\"]}",
                        MarketResearchStageInput::getInputPayload);

        assertThat(jobDao.requeueWaitingInput(
                        JOB_ID, USER_ID, "PRODUCT_SELECTION", NOW + 1_000L))
                .isTrue();
        assertThat(jobDao.getById(JOB_ID))
                .returns("QUEUED", MarketResearchJob::getJobStatus)
                .returns(null, MarketResearchJob::getWaitingInputType)
                .returns(NOW + 1_000L, MarketResearchJob::getNextRunAt);
    }

    @Test
    void shouldSerializeSameJobEventTransactionsBeforeAssigningSequence() throws Exception {
        assertThat(jobDao.save(job())).isTrue();
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        CountDownLatch firstInserted = new CountDownLatch(1);
        CountDownLatch releaseFirstCommit = new CountDownLatch(1);
        CountDownLatch secondStarted = new CountDownLatch(1);
        CountDownLatch secondSaved = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<MarketResearchEvent> first = executor.submit(() -> transactionTemplate.execute(status -> {
                MarketResearchEvent persisted = eventDao.saveEvent(event("event-a"));
                firstInserted.countDown();
                await(releaseFirstCommit);
                return persisted;
            }));
            assertThat(firstInserted.await(1, TimeUnit.SECONDS)).isTrue();
            Future<MarketResearchEvent> second = executor.submit(() -> transactionTemplate.execute(status -> {
                secondStarted.countDown();
                MarketResearchEvent persisted = eventDao.saveEvent(event("event-b"));
                secondSaved.countDown();
                return persisted;
            }));
            assertThat(secondStarted.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(secondSaved.await(150, TimeUnit.MILLISECONDS)).isFalse();

            releaseFirstCommit.countDown();
            MarketResearchEvent firstPersisted = first.get(2, TimeUnit.SECONDS);
            MarketResearchEvent secondPersisted = second.get(2, TimeUnit.SECONDS);

            assertThat(firstPersisted.getSequenceNo()).isLessThan(secondPersisted.getSequenceNo());
            assertThat(eventDao.listByJobIdAfterSequence(JOB_ID, 0L))
                    .extracting(MarketResearchEvent::getEventId)
                    .containsExactly("event-a", "event-b");
        }
    }

    private MarketResearchJob job() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setUserId(USER_ID);
        job.setReportName("持久化测试");
        job.setMarketplace("US");
        job.setNodeIdPath("172282:281407");
        job.setResearchMonth("2026-07");
        job.setKeyword("facial device");
        job.setSeedAsins("[]");
        job.setCollectionConfig(COLLECTION_CONFIG);
        job.setTemplateCode("market-research-v1");
        job.setDataSourceMode("MOCK");
        job.setWorkflowVersion("market-research-graph-v1");
        job.setJobStatus("QUEUED");
        job.setCurrentNode("validate");
        job.setProgress(0);
        job.setAttemptCount(0);
        job.setMaxAttempts(4);
        job.setNextRunAt(NOW);
        job.setErrorCode("");
        job.setErrorMessage("");
        audit(job);
        return job;
    }

    private MarketResearchNodeExecution nodeExecution() {
        MarketResearchNodeExecution execution = new MarketResearchNodeExecution();
        execution.setExecutionId("execution-1");
        execution.setJobId(JOB_ID);
        execution.setGraphCode("collection");
        execution.setNodeCode("validate");
        execution.setNodeName("校验输入");
        execution.setJobAttempt(1);
        execution.setNodeAttempt(1);
        execution.setExecutionStatus("SUCCEEDED");
        execution.setStartedAt(NOW);
        execution.setFinishedAt(NOW + 10L);
        execution.setDurationMs(10L);
        execution.setErrorCode("");
        execution.setErrorMessage("");
        audit(execution);
        return execution;
    }

    private MarketResearchDataset dataset(String id, String datasetCode, String requestHash) {
        MarketResearchDataset dataset = new MarketResearchDataset();
        dataset.setDatasetId(id);
        dataset.setJobId(JOB_ID);
        dataset.setNodeCode("collectMarketAndProducts");
        dataset.setOperation("PRODUCT_RESEARCH");
        dataset.setDatasetCode(datasetCode);
        dataset.setRequestHash(requestHash);
        dataset.setDataSourceMode("MOCK");
        dataset.setRequestPayload("{}");
        dataset.setSourcePayload("{}");
        dataset.setRecordCount(1);
        dataset.setSchemaVersion("v1");
        dataset.setValidationStatus("VALID");
        dataset.setValidationSummary("ok");
        dataset.setSha256("a".repeat(64));
        dataset.setFetchedAt(NOW);
        audit(dataset);
        return dataset;
    }

    private MarketResearchArtifact artifact() {
        MarketResearchArtifact artifact = new MarketResearchArtifact();
        artifact.setArtifactId("00000000-0000-7000-8000-000000000003");
        artifact.setJobId(JOB_ID);
        artifact.setArtifactScopeId(JOB_ID);
        artifact.setWorkflowVersion("market-research-graph-v1");
        artifact.setArtifactType(ResearchConstants.ARTIFACT_TYPE_STAGE1_EVIDENCE_WORKBOOK);
        artifact.setFileName("evidence.xlsx");
        artifact.setStorageKey("test/evidence.xlsx");
        artifact.setMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        artifact.setArtifactStatus("GENERATING");
        audit(artifact);
        return artifact;
    }

    private MarketResearchEvent event(String eventId) {
        MarketResearchEvent event = new MarketResearchEvent();
        event.setEventId(eventId);
        event.setJobId(JOB_ID);
        event.setScope("RESEARCH");
        event.setEventType("research_node_progress");
        event.setMessage(eventId);
        event.setPayload("{}");
        event.setTerminal(0);
        audit(event);
        return event;
    }

    private MarketResearchStageInput stageInput() {
        MarketResearchStageInput input = new MarketResearchStageInput();
        input.setInputId("00000000-0000-7000-8000-000000000004");
        input.setJobId(JOB_ID);
        input.setStageCode("SCREENING");
        input.setInputType("PRODUCT_SELECTION");
        input.setDecision("ENTER");
        input.setInputPayload("{\"selectedAsins\":[\"B000000001\"]}");
        input.setSubmittedBy(USER_ID);
        input.setSubmittedAt(NOW);
        audit(input);
        return input;
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(2, TimeUnit.SECONDS)) {
                throw new IllegalStateException("test latch timeout");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
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
            MarketResearchNodeExecutionDaoImpl.class,
            MarketResearchDatasetDaoImpl.class,
            MarketResearchEventDaoImpl.class,
            MarketResearchArtifactDaoImpl.class,
            MarketResearchStageInputDaoImpl.class
    })
    static class TestApplication {
    }
}
