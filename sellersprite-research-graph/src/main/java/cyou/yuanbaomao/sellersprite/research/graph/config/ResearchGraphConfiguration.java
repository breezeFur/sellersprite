package cyou.yuanbaomao.sellersprite.research.graph.config;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncEdgeAction.edge_async;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.CreateOption;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchSelectionDecision;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.graph.runtime.ResearchWorkflowNodes;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/** 市场调研Graph、Checkpoint与运行线程配置。 */
@EnableScheduling
@Configuration(proxyBeanMethods = false)
public class ResearchGraphConfiguration {

    public static final String EXECUTOR_BEAN_NAME = "researchGraphTaskExecutor";
    public static final String HEARTBEAT_SCHEDULER_BEAN_NAME = "researchGraphHeartbeatScheduler";
    public static final String SCREENING_GRAPH_NODE = "screeningGraph";
    public static final String PRODUCT_SELECTION_GATE_NODE = "productSelectionGate";
    public static final String DEEP_DIVE_GRAPH_NODE = "deepDiveGraph";
    public static final String FINAL_ANALYSIS_GRAPH_NODE = "finalAnalysisGraph";
    public static final String FINALIZE_ARTIFACTS_NODE = "finalizeArtifacts";

    public static final String SCREENING_ANALYSIS_NODE = "screening.analysis";
    public static final String DEEP_DIVE_ANALYSIS_NODE = "deepDive.analysis";
    public static final String FINAL_ANALYSIS_NODE = "finalAnalysis.analysis";

    private static final List<ResearchPhase> SCREENING_PHASES = List.of(
            ResearchPhase.VALIDATE,
            ResearchPhase.CHECK_QUOTA,
            ResearchPhase.COLLECT_PRODUCTS,
            ResearchPhase.COLLECT_MARKET_SALES_TREND,
            ResearchPhase.COLLECT_KEYWORD_DEMAND_TREND,
            ResearchPhase.COLLECT_SEGMENT_OPPORTUNITY,
            ResearchPhase.PREPARE_US_EVIDENCE,
            ResearchPhase.PREPARE_SALES_TREND_EVIDENCE,
            ResearchPhase.PREPARE_DEMAND_TREND_EVIDENCE,
            ResearchPhase.PREPARE_SEGMENT_MARKET_EVIDENCE,
            ResearchPhase.PREPARE_SEGMENT_RETURN_EVIDENCE,
            ResearchPhase.PREPARE_BRAND_EVIDENCE,
            ResearchPhase.PREPARE_CONCENTRATION_EVIDENCE);

    private static final List<ResearchPhase> DEEP_DIVE_PHASES = List.of(
            ResearchPhase.COLLECT_ASIN_INTELLIGENCE,
            ResearchPhase.COLLECT_REVIEWS,
            ResearchPhase.COLLECT_KEYWORD_INTELLIGENCE,
            ResearchPhase.PREPARE_REVIEW_EVIDENCE,
            ResearchPhase.PREPARE_VOC_EVIDENCE,
            ResearchPhase.PREPARE_KEYWORD_EVIDENCE,
            ResearchPhase.PREPARE_ASIN_SALES_TREND_EVIDENCE,
            ResearchPhase.PREPARE_ASIN_OPERATION_TREND_EVIDENCE);

    private static final List<ResearchPhase> FINAL_ANALYSIS_PHASES = List.of();

    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 4;
    private static final int QUEUE_CAPACITY = 100;
    private static final int HEARTBEAT_POOL_SIZE = 2;

    @Bean(name = EXECUTOR_BEAN_NAME)
    Executor researchGraphTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("market-research-graph-");
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @Bean(name = HEARTBEAT_SCHEDULER_BEAN_NAME)
    ThreadPoolTaskScheduler researchGraphHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("market-research-heartbeat-");
        scheduler.setPoolSize(HEARTBEAT_POOL_SIZE);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    MysqlSaver marketResearchCheckpointSaver(DataSource dataSource, ResearchProperties properties) {
        CreateOption createOption = properties.isCheckpointInitializeSchema()
                ? CreateOption.CREATE_IF_NOT_EXISTS
                : CreateOption.CREATE_NONE;
        return MysqlSaver.builder()
                .dataSource(dataSource)
                .createOption(createOption)
                .build();
    }

    @Bean
    CompiledGraph marketResearchGraph(
            ResearchWorkflowNodes nodes, MysqlSaver checkpointSaver) throws Exception {
        CompileConfig subgraphCompileConfig = checkpointCompileConfig(checkpointSaver);
        CompiledGraph screeningGraph = compileStageSubgraph(
                SCREENING_GRAPH_NODE,
                ResearchStageCode.SCREENING,
                SCREENING_PHASES,
                nodes,
                true,
                subgraphCompileConfig);
        CompiledGraph deepDiveGraph = compileStageSubgraph(
                DEEP_DIVE_GRAPH_NODE,
                ResearchStageCode.DEEP_DIVE,
                DEEP_DIVE_PHASES,
                nodes,
                true,
                subgraphCompileConfig);
        CompiledGraph finalAnalysisGraph = compileStageSubgraph(
                FINAL_ANALYSIS_GRAPH_NODE,
                ResearchStageCode.FINAL_ANALYSIS,
                FINAL_ANALYSIS_PHASES,
                nodes,
                true,
                subgraphCompileConfig);

        StateGraph parentGraph = stateGraph("market-research");
        parentGraph.addNode(SCREENING_GRAPH_NODE, screeningGraph);
        parentGraph.addNode(PRODUCT_SELECTION_GATE_NODE, node_async(nodes::productSelectionGate));
        parentGraph.addNode(DEEP_DIVE_GRAPH_NODE, deepDiveGraph);
        parentGraph.addNode(FINAL_ANALYSIS_GRAPH_NODE, finalAnalysisGraph);
        parentGraph.addNode(FINALIZE_ARTIFACTS_NODE, node_async(nodes::finalizeArtifacts));
        parentGraph.addEdge(START, SCREENING_GRAPH_NODE);
        parentGraph.addEdge(SCREENING_GRAPH_NODE, PRODUCT_SELECTION_GATE_NODE);
        parentGraph.addConditionalEdges(
                PRODUCT_SELECTION_GATE_NODE,
                edge_async(nodes::selectionRoute),
                Map.of(
                        ResearchSelectionDecision.ENTER.name(), DEEP_DIVE_GRAPH_NODE,
                        ResearchSelectionDecision.ABANDON.name(), FINALIZE_ARTIFACTS_NODE));
        parentGraph.addEdge(DEEP_DIVE_GRAPH_NODE, FINAL_ANALYSIS_GRAPH_NODE);
        parentGraph.addEdge(FINAL_ANALYSIS_GRAPH_NODE, FINALIZE_ARTIFACTS_NODE);
        parentGraph.addEdge(FINALIZE_ARTIFACTS_NODE, END);

        CompileConfig parentCompileConfig = CompileConfig.builder(subgraphCompileConfig)
                .interruptBefore(PRODUCT_SELECTION_GATE_NODE)
                .build();
        return parentGraph.compile(parentCompileConfig);
    }

    private CompiledGraph compileStageSubgraph(
            String name,
            ResearchStageCode stage,
            List<ResearchPhase> phases,
            ResearchWorkflowNodes nodes,
            boolean analyzeStage,
            CompileConfig compileConfig) throws Exception {
        if (phases.isEmpty() && !analyzeStage) {
            throw new IllegalStateException("市场调研子图没有节点: " + name);
        }
        StateGraph graph = stateGraph(name);
        String enterStageNode = stage.name() + ".enter";
        graph.addNode(enterStageNode, node_async(state -> nodes.enterStage(state, stage)));
        String previousNode = enterStageNode;
        for (ResearchPhase phase : phases) {
            graph.addNode(
                    phase.getNodeCode(),
                    node_async(state -> nodes.execute(state, phase)));
            graph.addEdge(previousNode, phase.getNodeCode());
            previousNode = phase.getNodeCode();
        }
        graph.addEdge(START, enterStageNode);
        if (analyzeStage) {
            String analysisNode = analysisNode(stage);
            graph.addNode(analysisNode, node_async(state -> nodes.runStageAnalysis(state, stage)));
            graph.addEdge(previousNode, analysisNode);
            previousNode = analysisNode;
        }
        graph.addEdge(previousNode, END);
        return graph.compile(compileConfig);
    }

    private String analysisNode(ResearchStageCode stage) {
        return switch (stage) {
            case SCREENING -> SCREENING_ANALYSIS_NODE;
            case DEEP_DIVE -> DEEP_DIVE_ANALYSIS_NODE;
            case FINAL_ANALYSIS -> FINAL_ANALYSIS_NODE;
        };
    }

    private StateGraph stateGraph(String name) {
        return new StateGraph(name, () -> Map.of(
                ResearchWorkflowNodes.STATE_JOB_ID, new ReplaceStrategy(),
                ResearchWorkflowNodes.STATE_WORKFLOW_VERSION, new ReplaceStrategy(),
                ResearchWorkflowNodes.STATE_EXECUTION_OWNER, new ReplaceStrategy(),
                ResearchWorkflowNodes.STATE_EXECUTION_TOKEN, new ReplaceStrategy(),
                ResearchWorkflowNodes.STATE_ATTEMPT_COUNT, new ReplaceStrategy(),
                ResearchWorkflowNodes.STATE_LAST_GRAPH, new ReplaceStrategy(),
                ResearchWorkflowNodes.STATE_LAST_NODE, new ReplaceStrategy(),
                ResearchWorkflowNodes.STATE_SELECTION_DECISION, new ReplaceStrategy(),
                ResearchWorkflowNodes.STATE_WORKFLOW_OUTCOME, new ReplaceStrategy()));
    }

    private CompileConfig checkpointCompileConfig(MysqlSaver checkpointSaver) {
        return CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(checkpointSaver).build())
                .releaseThread(false)
                .build();
    }
}
