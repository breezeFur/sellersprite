package cyou.yuanbaomao.sellersprite.research.graph.config;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;
import static com.alibaba.cloud.ai.graph.action.AsyncNodeAction.node_async;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.CreateOption;
import com.alibaba.cloud.ai.graph.checkpoint.savers.mysql.MysqlSaver;
import com.alibaba.cloud.ai.graph.state.strategy.AppendStrategy;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class NestedSubgraphCheckpointIntegrationTest {

    private static final String THREAD_ID = "nested-graph-thread";
    private static final String COLLECTION_GRAPH = "collectionGraph";
    private static final String EVIDENCE_GRAPH = "evidenceGraph";
    private static final String REPORT_GRAPH = "reportGraph";
    private static final String COLLECTION_FETCH = "collection.fetch";
    private static final String COLLECTION_PUBLISH = "collection.publish";
    private static final String EVIDENCE_PREPARE = "evidence.prepare";
    private static final String EVIDENCE_UNSTABLE = "evidence.unstable";
    private static final String REPORT_RENDER = "report.render";
    private static final String STATE_JOB_ID = "jobId";
    private static final String STATE_RAW_READY = "rawReady";
    private static final String STATE_EVIDENCE_READY = "evidenceReady";
    private static final String STATE_REPORT_READY = "reportReady";
    private static final String STATE_TRACE = "trace";

    @Test
    void shouldResumeInsideSecondOfThreeSubgraphsWithIsolatedCheckpointThreads() throws Exception {
        JdbcTemplate jdbcTemplate = checkpointJdbcTemplate();
        MysqlSaver saver = MysqlSaver.builder()
                .dataSource(jdbcTemplate.getDataSource())
                .createOption(CreateOption.CREATE_IF_NOT_EXISTS)
                .build();
        CompileConfig checkpointConfig = checkpointConfig(saver);
        ExecutionCounters counters = new ExecutionCounters();
        CompiledGraph graph = parentGraph(checkpointConfig, counters);
        RunnableConfig initialConfig = RunnableConfig.builder().threadId(THREAD_ID).build();
        Map<String, Object> input = Map.of(STATE_JOB_ID, "job-001");

        assertThatThrownBy(() -> graph.stream(input, initialConfig).blockLast())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("simulated evidence failure");

        assertThat(checkpointThreads(jdbcTemplate))
                .containsExactlyInAnyOrder(
                        THREAD_ID,
                        childThread(COLLECTION_GRAPH),
                        childThread(EVIDENCE_GRAPH));

        String parentCheckpointId = saver.get(initialConfig).orElseThrow().getId();
        // 通用 resume 标记会泄漏给后续子图；父 checkpointId 只恢复当前失败子图。
        RunnableConfig resumeConfig = RunnableConfig.builder(initialConfig)
                .checkPointId(parentCheckpointId)
                .build();
        NodeOutput output = graph.stream(input, resumeConfig).blockLast();

        assertThat(output).isNotNull();
        assertThat(output.state().value(STATE_RAW_READY, Boolean.class)).contains(true);
        assertThat(output.state().value(STATE_EVIDENCE_READY, Boolean.class)).contains(true);
        assertThat(output.state().value(STATE_REPORT_READY, Boolean.class)).contains(true);
        Object trace = output.state().value(STATE_TRACE).orElseThrow();
        assertThat(trace).isInstanceOf(List.class);
        assertThat(((List<?>) trace).containsAll(List.of(
                COLLECTION_FETCH,
                COLLECTION_PUBLISH,
                EVIDENCE_PREPARE,
                EVIDENCE_UNSTABLE,
                REPORT_RENDER))).isTrue();
        assertThat(counters.collectionFetch).hasValue(1);
        assertThat(counters.collectionPublish).hasValue(1);
        assertThat(counters.evidencePrepare).hasValue(1);
        assertThat(counters.evidenceUnstable).hasValue(2);
        assertThat(counters.reportRender).hasValue(1);
        assertThat(checkpointThreads(jdbcTemplate))
                .containsExactlyInAnyOrder(
                        THREAD_ID,
                        childThread(COLLECTION_GRAPH),
                        childThread(EVIDENCE_GRAPH),
                        childThread(REPORT_GRAPH));
    }

    private static CompiledGraph parentGraph(CompileConfig checkpointConfig, ExecutionCounters counters)
            throws Exception {
        CompiledGraph collectionGraph = collectionGraph(checkpointConfig, counters);
        CompiledGraph evidenceGraph = evidenceGraph(checkpointConfig, counters);
        CompiledGraph reportGraph = reportGraph(checkpointConfig, counters);
        StateGraph parent = stateGraph("nested-parent");
        parent.addNode(COLLECTION_GRAPH, collectionGraph);
        parent.addNode(EVIDENCE_GRAPH, evidenceGraph);
        parent.addNode(REPORT_GRAPH, reportGraph);
        parent.addEdge(START, COLLECTION_GRAPH);
        parent.addEdge(COLLECTION_GRAPH, EVIDENCE_GRAPH);
        parent.addEdge(EVIDENCE_GRAPH, REPORT_GRAPH);
        parent.addEdge(REPORT_GRAPH, END);
        return parent.compile(checkpointConfig);
    }

    private static CompiledGraph collectionGraph(CompileConfig checkpointConfig, ExecutionCounters counters)
            throws Exception {
        StateGraph graph = stateGraph("collection-subgraph");
        graph.addNode(COLLECTION_FETCH, node_async(state -> {
            requireValue(state, STATE_JOB_ID, "job-001");
            counters.collectionFetch.incrementAndGet();
            return Map.of(STATE_RAW_READY, true, STATE_TRACE, List.of(COLLECTION_FETCH));
        }));
        graph.addNode(COLLECTION_PUBLISH, node_async(state -> {
            requireValue(state, STATE_RAW_READY, true);
            counters.collectionPublish.incrementAndGet();
            return Map.of(STATE_TRACE, List.of(COLLECTION_PUBLISH));
        }));
        graph.addEdge(START, COLLECTION_FETCH);
        graph.addEdge(COLLECTION_FETCH, COLLECTION_PUBLISH);
        graph.addEdge(COLLECTION_PUBLISH, END);
        return graph.compile(checkpointConfig);
    }

    private static CompiledGraph evidenceGraph(CompileConfig checkpointConfig, ExecutionCounters counters)
            throws Exception {
        StateGraph graph = stateGraph("evidence-subgraph");
        graph.addNode(EVIDENCE_PREPARE, node_async(state -> {
            requireValue(state, STATE_RAW_READY, true);
            counters.evidencePrepare.incrementAndGet();
            return Map.of(STATE_EVIDENCE_READY, true, STATE_TRACE, List.of(EVIDENCE_PREPARE));
        }));
        graph.addNode(EVIDENCE_UNSTABLE, node_async(state -> {
            requireValue(state, STATE_EVIDENCE_READY, true);
            counters.evidenceUnstable.incrementAndGet();
            if (counters.failEvidenceOnce.getAndSet(false)) {
                throw new IllegalStateException("simulated evidence failure");
            }
            return Map.of(STATE_TRACE, List.of(EVIDENCE_UNSTABLE));
        }));
        graph.addEdge(START, EVIDENCE_PREPARE);
        graph.addEdge(EVIDENCE_PREPARE, EVIDENCE_UNSTABLE);
        graph.addEdge(EVIDENCE_UNSTABLE, END);
        return graph.compile(checkpointConfig);
    }

    private static CompiledGraph reportGraph(CompileConfig checkpointConfig, ExecutionCounters counters)
            throws Exception {
        StateGraph graph = stateGraph("report-subgraph");
        graph.addNode(REPORT_RENDER, node_async(state -> {
            requireValue(state, STATE_EVIDENCE_READY, true);
            counters.reportRender.incrementAndGet();
            return Map.of(STATE_REPORT_READY, true, STATE_TRACE, List.of(REPORT_RENDER));
        }));
        graph.addEdge(START, REPORT_RENDER);
        graph.addEdge(REPORT_RENDER, END);
        return graph.compile(checkpointConfig);
    }

    private static StateGraph stateGraph(String name) {
        return new StateGraph(name, () -> Map.of(
                STATE_JOB_ID, new ReplaceStrategy(),
                STATE_RAW_READY, new ReplaceStrategy(),
                STATE_EVIDENCE_READY, new ReplaceStrategy(),
                STATE_REPORT_READY, new ReplaceStrategy(),
                STATE_TRACE, new AppendStrategy()));
    }

    private static CompileConfig checkpointConfig(MysqlSaver saver) {
        return CompileConfig.builder()
                .saverConfig(SaverConfig.builder().register(saver).build())
                .releaseThread(false)
                .build();
    }

    private static JdbcTemplate checkpointJdbcTemplate() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:nested-subgraph-checkpoint;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("CREATE ALIAS IF NOT EXISTS JSON_EXTRACT FOR "
                + "'cyou.yuanbaomao.sellersprite.research.graph.config.MysqlJsonFunctions.jsonExtract'");
        jdbcTemplate.execute("CREATE ALIAS IF NOT EXISTS JSON_UNQUOTE FOR "
                + "'cyou.yuanbaomao.sellersprite.research.graph.config.MysqlJsonFunctions.jsonUnquote'");
        return jdbcTemplate;
    }

    private static List<String> checkpointThreads(JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.queryForList(
                "SELECT thread_name FROM GRAPH_THREAD WHERE is_released = FALSE",
                String.class);
    }

    private static String childThread(String graphNode) {
        return THREAD_ID + "_subgraph_" + graphNode;
    }

    private static void requireValue(OverAllState state, String key, Object expected) {
        Object actual = state.value(key).orElseThrow(() -> new IllegalStateException("missing state: " + key));
        if (!expected.equals(actual)) {
            throw new IllegalStateException("unexpected state: " + key);
        }
    }

    private static final class ExecutionCounters {

        private final AtomicInteger collectionFetch = new AtomicInteger();
        private final AtomicInteger collectionPublish = new AtomicInteger();
        private final AtomicInteger evidencePrepare = new AtomicInteger();
        private final AtomicInteger evidenceUnstable = new AtomicInteger();
        private final AtomicInteger reportRender = new AtomicInteger();
        private final AtomicBoolean failEvidenceOnce = new AtomicBoolean(true);
    }
}
