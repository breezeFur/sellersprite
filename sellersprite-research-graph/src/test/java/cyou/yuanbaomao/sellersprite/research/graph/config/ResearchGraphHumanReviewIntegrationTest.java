package cyou.yuanbaomao.sellersprite.research.graph.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import cyou.yuanbaomao.sellersprite.research.graph.service.ResearchWorkflowServiceImpl;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@ExtendWith(MockitoExtension.class)
class ResearchGraphHumanReviewIntegrationTest {

    private static final String WORKFLOW_VERSION = ResearchConstants.WORKFLOW_VERSION;
    private static final String EXECUTION_OWNER = "review-worker";
    private static final String INITIAL_TOKEN = "initial-token";
    private static final String RESUME_TOKEN = "resume-token";

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
    void shouldResumeEnterBranchAfterGraphRecreationWithoutRepeatingScreening() throws Exception {
        String jobId = "human-review-enter";
        DriverManagerDataSource dataSource = dataSource(jobId);
        CompiledGraph graph = graph(dataSource);
        RunnableConfig threadConfig = threadConfig(jobId);
        ResearchExecutionLease initialLease = lease(jobId, INITIAL_TOKEN, 1);

        NodeOutput paused = graph.stream(executionState(initialLease), threadConfig).blockLast();

        assertThat(paused).isNotNull();
        assertThat(paused.isEND()).isFalse();
        assertThat(graph.lastStateOf(threadConfig)).get()
                .extracting(snapshot -> snapshot.next())
                .isEqualTo(ResearchGraphConfiguration.PRODUCT_SELECTION_GATE_NODE);
        verify(stepService).execute(jobId, INITIAL_TOKEN, ResearchPhase.COLLECT_PRODUCTS);
        verify(stageAnalysisPort).runStage(jobId, INITIAL_TOKEN, ResearchStageCode.SCREENING);
        verify(stepService, never()).execute(anyString(), anyString(), eq(ResearchPhase.COLLECT_REVIEWS));

        when(stageInputService.findSelection(jobId)).thenReturn(Optional.of(
                new ResearchProductSelection(ResearchSelectionDecision.ENTER, List.of("B000000001"))));
        ResearchExecutionLease resumedLease = lease(jobId, RESUME_TOKEN, 1);
        CompiledGraph restartedGraph = graph(dataSource, false);
        RunnableConfig resumeConfig = restartedGraph.updateState(
                restartedGraph.lastStateOf(threadConfig).orElseThrow().config(),
                executionLeaseState(resumedLease));

        NodeOutput completed = restartedGraph.stream(Map.of(), resumeConfig).blockLast();

        assertThat(completed).isNotNull();
        assertThat(completed.isEND()).isTrue();
        assertThat(completed.state()
                .value(ResearchWorkflowNodes.STATE_WORKFLOW_OUTCOME, String.class))
                .contains(ResearchWorkflowNodes.OUTCOME_SUCCEEDED);
        verify(stepService).execute(jobId, RESUME_TOKEN, ResearchPhase.COLLECT_REVIEWS);
        verify(stageAnalysisPort).runStage(jobId, RESUME_TOKEN, ResearchStageCode.DEEP_DIVE);
        verify(stageAnalysisPort).runStage(jobId, RESUME_TOKEN, ResearchStageCode.FINAL_ANALYSIS);
        verify(stepService, never()).execute(
                anyString(), anyString(), eq(ResearchPhase.RUN_INITIAL_ANALYSIS));
        verify(artifactFinalizationPort).finalizeArtifacts(
                jobId, RESUME_TOKEN, ResearchSelectionDecision.ENTER);
        verify(stepService).execute(jobId, INITIAL_TOKEN, ResearchPhase.PREPARE_US_EVIDENCE);
        verify(stepService, never()).execute(jobId, RESUME_TOKEN, ResearchPhase.COLLECT_PRODUCTS);
    }

    @Test
    void shouldGenerateBackendTopologyFromActualThreeStageParentGraph() throws Exception {
        CompiledGraph graph = graph("human-review-topology");

        var topology = new ResearchWorkflowServiceImpl(graph).topology();

        assertThat(topology.getContent()).contains(
                ResearchGraphConfiguration.SCREENING_GRAPH_NODE,
                ResearchGraphConfiguration.PRODUCT_SELECTION_GATE_NODE,
                ResearchGraphConfiguration.DEEP_DIVE_GRAPH_NODE,
                ResearchGraphConfiguration.FINAL_ANALYSIS_GRAPH_NODE,
                ResearchGraphConfiguration.FINALIZE_ARTIFACTS_NODE);
        assertThat(topology.getSteps())
                .extracting(step -> List.of(step.getCode(), step.getNodeCode(), step.getLabel()))
                .containsExactly(
                        List.of("SCREENING", "screeningGraph", "阶段一：市场初筛"),
                        List.of("PRODUCT_SELECTION", "productSelectionGate", "商品选择"),
                        List.of("DEEP_DIVE", "deepDiveGraph", "阶段二：商品深挖"),
                        List.of("FINAL_ANALYSIS", "finalAnalysisGraph", "阶段三：最终分析"),
                        List.of("ARTIFACT_FINALIZATION", "finalizeArtifacts", "生成并发布产物"));
    }

    @Test
    void shouldResumeAbandonBranchWithoutRunningDeepDiveOrFinalAnalysis() throws Exception {
        String jobId = "human-review-abandon";
        CompiledGraph graph = graph(jobId);
        RunnableConfig threadConfig = threadConfig(jobId);
        ResearchExecutionLease initialLease = lease(jobId, INITIAL_TOKEN, 1);
        graph.stream(executionState(initialLease), threadConfig).blockLast();
        when(stageInputService.findSelection(jobId)).thenReturn(Optional.of(
                new ResearchProductSelection(ResearchSelectionDecision.ABANDON, List.of())));
        ResearchExecutionLease resumedLease = lease(jobId, RESUME_TOKEN, 1);
        RunnableConfig resumeConfig = graph.updateState(
                graph.lastStateOf(threadConfig).orElseThrow().config(),
                executionLeaseState(resumedLease));

        NodeOutput completed = graph.stream(Map.of(), resumeConfig).blockLast();

        assertThat(completed).isNotNull();
        assertThat(completed.isEND()).isTrue();
        assertThat(completed.state()
                .value(ResearchWorkflowNodes.STATE_WORKFLOW_OUTCOME, String.class))
                .contains(ResearchWorkflowNodes.OUTCOME_ABANDONED);
        verify(artifactFinalizationPort).finalizeArtifacts(
                jobId, RESUME_TOKEN, ResearchSelectionDecision.ABANDON);
        verify(stepService, never()).execute(anyString(), anyString(), eq(ResearchPhase.COLLECT_REVIEWS));
        verify(stepService, never()).execute(anyString(), anyString(), eq(ResearchPhase.RUN_INITIAL_ANALYSIS));
        verify(stageAnalysisPort, never()).runStage(
                anyString(), anyString(), eq(ResearchStageCode.DEEP_DIVE));
        verify(stageAnalysisPort, never()).runStage(
                anyString(), anyString(), eq(ResearchStageCode.FINAL_ANALYSIS));
        verify(jobStateService, never()).moveToStage(
                any(ResearchExecutionLease.class), eq(ResearchStageCode.DEEP_DIVE));
    }

    private CompiledGraph graph(String databaseName) throws Exception {
        return graph(dataSource(databaseName));
    }

    private DriverManagerDataSource dataSource(String databaseName) {
        return new DriverManagerDataSource(
                "jdbc:h2:mem:" + databaseName + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
    }

    private CompiledGraph graph(DriverManagerDataSource dataSource) throws Exception {
        return graph(dataSource, true);
    }

    private CompiledGraph graph(
            DriverManagerDataSource dataSource, boolean initializeCheckpointSchema) throws Exception {
        ResearchGraphConfiguration configuration = new ResearchGraphConfiguration();
        ResearchProperties properties = new ResearchProperties();
        properties.setCheckpointInitializeSchema(initializeCheckpointSchema);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE ALIAS IF NOT EXISTS JSON_EXTRACT FOR "
                + "'cyou.yuanbaomao.sellersprite.research.graph.config.MysqlJsonFunctions.jsonExtract'");
        jdbcTemplate.execute("CREATE ALIAS IF NOT EXISTS JSON_UNQUOTE FOR "
                + "'cyou.yuanbaomao.sellersprite.research.graph.config.MysqlJsonFunctions.jsonUnquote'");
        MysqlSaver saver = configuration.marketResearchCheckpointSaver(dataSource, properties);
        return configuration.marketResearchGraph(new ResearchWorkflowNodes(
                stepService,
                stageInputService,
                stageAnalysisPort,
                artifactFinalizationPort,
                jobStateService), saver);
    }

    private RunnableConfig threadConfig(String jobId) {
        return RunnableConfig.builder().threadId(WORKFLOW_VERSION + ":" + jobId).build();
    }

    private ResearchExecutionLease lease(String jobId, String token, int attempt) {
        return new ResearchExecutionLease(jobId, WORKFLOW_VERSION, EXECUTION_OWNER, token, attempt);
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
