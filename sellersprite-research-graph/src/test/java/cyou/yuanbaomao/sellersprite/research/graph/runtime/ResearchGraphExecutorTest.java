package cyou.yuanbaomao.sellersprite.research.graph.runtime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.exception.ResearchJobCancelledException;
import cyou.yuanbaomao.sellersprite.research.graph.config.ResearchGraphConfiguration;
import cyou.yuanbaomao.sellersprite.research.model.ResearchExecutionLease;
import cyou.yuanbaomao.sellersprite.research.service.ResearchJobStateService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchStageInputService;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class ResearchGraphExecutorTest {

    private static final String JOB_ID = "executor-job";
    private static final String OWNER = "executor-worker";
    private static final String TOKEN = "executor-token";

    @Mock
    private CompiledGraph graph;

    @Mock
    private TaskScheduler heartbeatScheduler;

    @Mock
    private ScheduledFuture<?> heartbeat;

    @Mock
    private ResearchJobStateService jobStateService;

    @Mock
    private ResearchStageInputService stageInputService;

    private ResearchGraphExecutor executor;
    private boolean runHeartbeatImmediately;

    @BeforeEach
    void setUp() {
        ResearchProperties properties = new ResearchProperties();
        properties.setHeartbeatIntervalMs(1000);
        Executor directExecutor = Runnable::run;
        executor = new ResearchGraphExecutor(
                graph,
                directExecutor,
                heartbeatScheduler,
                jobStateService,
                stageInputService,
                properties);
        doAnswer(invocation -> {
                    if (runHeartbeatImmediately) {
                        invocation.<Runnable>getArgument(0).run();
                    }
                    return heartbeat;
                })
                .when(heartbeatScheduler)
                .scheduleAtFixedRate(any(Runnable.class), any(Duration.class));
    }

    @Test
    void shouldMarkWaitingInsteadOfSucceededWhenGraphInterruptsBeforeSelection() {
        ResearchExecutionLease lease = lease(TOKEN, 1);
        NodeOutput pausedOutput = mock(NodeOutput.class);
        StateSnapshot pausedSnapshot = mock(StateSnapshot.class);
        when(pausedOutput.isEND()).thenReturn(false);
        when(pausedOutput.node()).thenReturn(ResearchGraphConfiguration.SCREENING_GRAPH_NODE);
        when(pausedSnapshot.next()).thenReturn(ResearchGraphConfiguration.PRODUCT_SELECTION_GATE_NODE);
        when(graph.lastStateOf(any(RunnableConfig.class)))
                .thenReturn(Optional.empty(), Optional.of(pausedSnapshot));
        when(graph.stream(anyMap(), any(RunnableConfig.class))).thenReturn(Flux.just(pausedOutput));

        executor.execute(lease);

        verify(jobStateService).markWaitingInput(lease);
        verify(jobStateService, never()).markSucceeded(lease);
        verify(jobStateService, never()).handleExecutionFailure(eq(lease), any());
        verify(heartbeat).cancel(false);
    }

    @Test
    void shouldRepairWaitingStateWithoutExecutingGateAfterCrashWindow() throws Exception {
        ResearchExecutionLease lease = lease(TOKEN, 1);
        StateSnapshot pausedSnapshot = mock(StateSnapshot.class);
        when(pausedSnapshot.next()).thenReturn(ResearchGraphConfiguration.PRODUCT_SELECTION_GATE_NODE);
        when(graph.lastStateOf(any(RunnableConfig.class))).thenReturn(Optional.of(pausedSnapshot));
        when(stageInputService.findSelection(JOB_ID)).thenReturn(Optional.empty());

        executor.execute(lease);

        verify(jobStateService).markWaitingInput(lease);
        verify(graph, never()).stream(anyMap(), any(RunnableConfig.class));
        verify(graph, never()).updateState(any(RunnableConfig.class), anyMap());
        verify(heartbeat).cancel(false);
    }

    @Test
    void shouldPatchNewLeaseIntoCheckpointBeforeSuccessfulResume() throws Exception {
        ResearchExecutionLease lease = lease("new-token", 2);
        StateSnapshot checkpoint = mock(StateSnapshot.class);
        RunnableConfig checkpointConfig = RunnableConfig.builder()
                .threadId(ResearchConstants.WORKFLOW_VERSION + ":" + JOB_ID)
                .checkPointId("checkpoint-1")
                .build();
        RunnableConfig resumedConfig = RunnableConfig.builder(checkpointConfig)
                .checkPointId("checkpoint-2")
                .build();
        when(checkpoint.next()).thenReturn(ResearchGraphConfiguration.DEEP_DIVE_GRAPH_NODE);
        when(checkpoint.config()).thenReturn(checkpointConfig);
        when(graph.lastStateOf(any(RunnableConfig.class))).thenReturn(Optional.of(checkpoint));
        when(graph.updateState(eq(checkpointConfig), argThat(state ->
                "new-token".equals(state.get(ResearchWorkflowNodes.STATE_EXECUTION_TOKEN))
                        && OWNER.equals(state.get(ResearchWorkflowNodes.STATE_EXECUTION_OWNER))
                        && Integer.valueOf(2).equals(
                                state.get(ResearchWorkflowNodes.STATE_ATTEMPT_COUNT)))))
                .thenReturn(resumedConfig);
        NodeOutput completed = terminalOutput(ResearchWorkflowNodes.OUTCOME_SUCCEEDED);
        when(graph.stream(anyMap(), eq(resumedConfig))).thenReturn(Flux.just(completed));

        executor.execute(lease);

        verify(jobStateService).markSucceeded(lease);
        verify(jobStateService, never()).markAbandoned(lease);
        verify(heartbeat).cancel(false);
    }

    @Test
    void shouldMarkAbandonedOnlyAfterGraphReachesEnd() {
        ResearchExecutionLease lease = lease(TOKEN, 1);
        when(graph.lastStateOf(any(RunnableConfig.class))).thenReturn(Optional.empty());
        NodeOutput completed = terminalOutput(ResearchWorkflowNodes.OUTCOME_ABANDONED);
        when(graph.stream(anyMap(), any(RunnableConfig.class))).thenReturn(Flux.just(completed));

        executor.execute(lease);

        verify(jobStateService).markAbandoned(lease);
        verify(jobStateService, never()).markSucceeded(lease);
        verify(heartbeat).cancel(false);
    }

    @Test
    void shouldFenceLostLeaseAndDelegateRetryHandling() {
        ResearchExecutionLease lease = lease(TOKEN, 1);
        runHeartbeatImmediately = true;
        when(jobStateService.heartbeat(lease)).thenReturn(false);
        when(graph.lastStateOf(any(RunnableConfig.class))).thenReturn(Optional.empty());
        NodeOutput output = mock(NodeOutput.class);
        when(graph.stream(anyMap(), any(RunnableConfig.class))).thenReturn(Flux.just(output));

        executor.execute(lease);

        verify(jobStateService).handleExecutionFailure(eq(lease), argThat(exception ->
                exception.getMessage().contains("执行租约已丢失")));
        verify(jobStateService, never()).markSucceeded(lease);
        verify(jobStateService, never()).markAbandoned(lease);
        verify(heartbeat).cancel(false);
    }

    @Test
    void shouldMarkCancelledWhenNodeSignalsCooperativeCancellation() {
        ResearchExecutionLease lease = lease(TOKEN, 1);
        when(graph.lastStateOf(any(RunnableConfig.class))).thenReturn(Optional.empty());
        when(graph.stream(anyMap(), any(RunnableConfig.class)))
                .thenReturn(Flux.error(new ResearchJobCancelledException(JOB_ID)));

        executor.execute(lease);

        verify(jobStateService).markCancelled(lease);
        verify(jobStateService, never()).handleExecutionFailure(eq(lease), any());
        verify(jobStateService, never()).markSucceeded(lease);
        verify(heartbeat).cancel(false);
    }

    private NodeOutput terminalOutput(String outcome) {
        NodeOutput output = mock(NodeOutput.class);
        OverAllState state = mock(OverAllState.class);
        when(output.isEND()).thenReturn(true);
        when(output.node()).thenReturn("__END__");
        when(output.state()).thenReturn(state);
        when(state.value(ResearchWorkflowNodes.STATE_WORKFLOW_OUTCOME, String.class))
                .thenReturn(Optional.of(outcome));
        return output;
    }

    private ResearchExecutionLease lease(String token, int attempt) {
        return new ResearchExecutionLease(
                JOB_ID,
                ResearchConstants.WORKFLOW_VERSION,
                OWNER,
                token,
                attempt);
    }
}
