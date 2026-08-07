package cyou.yuanbaomao.sellersprite.research.graph.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchSelectionDecision;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.graph.runtime.ResearchWorkflowNodes;
import cyou.yuanbaomao.sellersprite.research.model.ResearchExecutionLease;
import cyou.yuanbaomao.sellersprite.research.model.ResearchProductSelection;
import cyou.yuanbaomao.sellersprite.research.service.ResearchArtifactFinalizationPort;
import cyou.yuanbaomao.sellersprite.research.service.ResearchJobStateService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchStageAnalysisPort;
import cyou.yuanbaomao.sellersprite.research.service.ResearchStageInputService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchWorkflowStepService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@ExtendWith(MockitoExtension.class)
class ResearchGraphCheckpointRecoveryTest {

    private static final String JOB_ID = "job-checkpoint-001";
    private static final String OWNER = "checkpoint-worker";
    private static final String INITIAL_TOKEN = "initial-token";
    private static final String DEEP_DIVE_TOKEN = "deep-dive-token";
    private static final String RECOVERY_TOKEN = "recovery-token";

    @Mock
    private ResearchWorkflowStepService stepService;

    @Mock
    private ResearchStageInputService stageInputService;

    @Mock
    private ResearchStageAnalysisPort stageAnalysisPort;

    @Mock
    private ResearchArtifactFinalizationPort artifactFinalizationPort;

    @Mock
    private ResearchJobStateService jobStateService;

    @Test
    void shouldPatchNewTokenAndResumeFailedDeepDiveWithoutRepeatingScreening() throws Exception {
        ResearchGraphConfiguration configuration = new ResearchGraphConfiguration();
        MysqlSaver saver = checkpointSaver(configuration);
        CompiledGraph graph = configuration.marketResearchGraph(new ResearchWorkflowNodes(
                stepService,
                stageInputService,
                stageAnalysisPort,
                artifactFinalizationPort,
                jobStateService), saver);
        RunnableConfig threadConfig = RunnableConfig.builder()
                .threadId(ResearchConstants.WORKFLOW_VERSION + ":" + JOB_ID)
                .build();
        ResearchExecutionLease initialLease = lease(INITIAL_TOKEN, 1);
        graph.stream(executionState(initialLease), threadConfig).blockLast();
        when(stageInputService.findSelection(JOB_ID)).thenReturn(Optional.of(
                new ResearchProductSelection(ResearchSelectionDecision.ENTER, List.of("B000000001"))));
        AtomicBoolean failReviewsOnce = new AtomicBoolean(true);
        doAnswer(invocation -> {
                    ResearchPhase phase = invocation.getArgument(2);
                    if (phase == ResearchPhase.COLLECT_REVIEWS && failReviewsOnce.getAndSet(false)) {
                        throw new IllegalStateException("simulated deep-dive failure");
                    }
                    return null;
                })
                .when(stepService)
                .execute(org.mockito.ArgumentMatchers.eq(JOB_ID),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(ResearchPhase.class));

        ResearchExecutionLease deepDiveLease = lease(DEEP_DIVE_TOKEN, 1);
        RunnableConfig deepDiveConfig = graph.updateState(
                graph.lastStateOf(threadConfig).orElseThrow().config(),
                executionLeaseState(deepDiveLease));
        assertThatThrownBy(() -> graph.stream(Map.of(), deepDiveConfig).blockLast())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("simulated deep-dive failure");

        ResearchExecutionLease recoveryLease = lease(RECOVERY_TOKEN, 2);
        RunnableConfig recoveryConfig = graph.updateState(
                graph.lastStateOf(threadConfig).orElseThrow().config(),
                executionLeaseState(recoveryLease));
        NodeOutput completed = graph.stream(Map.of(), recoveryConfig).blockLast();

        assertThat(completed).isNotNull();
        assertThat(completed.isEND()).isTrue();
        verify(stepService, times(1)).execute(JOB_ID, INITIAL_TOKEN, ResearchPhase.COLLECT_PRODUCTS);
        verify(stepService, never()).execute(JOB_ID, RECOVERY_TOKEN, ResearchPhase.COLLECT_PRODUCTS);
        verify(stepService, times(1)).execute(JOB_ID, DEEP_DIVE_TOKEN, ResearchPhase.COLLECT_REVIEWS);
        verify(stepService, times(1)).execute(JOB_ID, RECOVERY_TOKEN, ResearchPhase.COLLECT_REVIEWS);
        verify(stageAnalysisPort).runStage(
                JOB_ID, RECOVERY_TOKEN, ResearchStageCode.FINAL_ANALYSIS);
        verify(stepService, never()).execute(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq(ResearchPhase.RUN_INITIAL_ANALYSIS));
        verify(stageAnalysisPort, times(1))
                .runStage(JOB_ID, INITIAL_TOKEN, ResearchStageCode.SCREENING);
        verify(artifactFinalizationPort).finalizeArtifacts(
                JOB_ID, RECOVERY_TOKEN, ResearchSelectionDecision.ENTER);
    }

    private MysqlSaver checkpointSaver(ResearchGraphConfiguration configuration) {
        ResearchProperties properties = new ResearchProperties();
        properties.setCheckpointInitializeSchema(true);
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:research-checkpoint-v5;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE ALIAS IF NOT EXISTS JSON_EXTRACT FOR "
                + "'cyou.yuanbaomao.sellersprite.research.graph.config.MysqlJsonFunctions.jsonExtract'");
        jdbcTemplate.execute("CREATE ALIAS IF NOT EXISTS JSON_UNQUOTE FOR "
                + "'cyou.yuanbaomao.sellersprite.research.graph.config.MysqlJsonFunctions.jsonUnquote'");
        return configuration.marketResearchCheckpointSaver(dataSource, properties);
    }

    private ResearchExecutionLease lease(String token, int attempt) {
        return new ResearchExecutionLease(
                JOB_ID, ResearchConstants.WORKFLOW_VERSION, OWNER, token, attempt);
    }

    private Map<String, Object> executionState(ResearchExecutionLease lease) {
        return Map.of(
                ResearchWorkflowNodes.STATE_JOB_ID, lease.jobId(),
                ResearchWorkflowNodes.STATE_WORKFLOW_VERSION, lease.workflowVersion(),
                ResearchWorkflowNodes.STATE_EXECUTION_OWNER, lease.executionOwner(),
                ResearchWorkflowNodes.STATE_EXECUTION_TOKEN, lease.executionToken(),
                ResearchWorkflowNodes.STATE_ATTEMPT_COUNT, lease.attemptCount());
    }

    private Map<String, Object> executionLeaseState(ResearchExecutionLease lease) {
        return Map.of(
                ResearchWorkflowNodes.STATE_EXECUTION_OWNER, lease.executionOwner(),
                ResearchWorkflowNodes.STATE_EXECUTION_TOKEN, lease.executionToken(),
                ResearchWorkflowNodes.STATE_ATTEMPT_COUNT, lease.attemptCount());
    }
}
