package cyou.yuanbaomao.sellersprite.research.graph.runtime;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.state.StateSnapshot;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.exception.ResearchJobCancelledException;
import cyou.yuanbaomao.sellersprite.research.graph.config.ResearchGraphConfiguration;
import cyou.yuanbaomao.sellersprite.research.model.ResearchExecutionLease;
import cyou.yuanbaomao.sellersprite.research.service.ResearchJobStateService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchStageInputService;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

/** 执行已由数据库原子抢占的市场调研Graph。 */
@Slf4j
@Component
public class ResearchGraphExecutor {

    private final CompiledGraph graph;
    private final Executor taskExecutor;
    private final TaskScheduler heartbeatScheduler;
    private final ResearchJobStateService jobStateService;
    private final ResearchStageInputService stageInputService;
    private final ResearchProperties properties;

    public ResearchGraphExecutor(
            CompiledGraph marketResearchGraph,
            @Qualifier(ResearchGraphConfiguration.EXECUTOR_BEAN_NAME) Executor taskExecutor,
            @Qualifier(ResearchGraphConfiguration.HEARTBEAT_SCHEDULER_BEAN_NAME)
                    TaskScheduler heartbeatScheduler,
            ResearchJobStateService jobStateService,
            ResearchStageInputService stageInputService,
            ResearchProperties properties) {
        this.graph = marketResearchGraph;
        this.taskExecutor = taskExecutor;
        this.heartbeatScheduler = heartbeatScheduler;
        this.jobStateService = jobStateService;
        this.stageInputService = stageInputService;
        this.properties = properties;
    }

    public void submit(ResearchExecutionLease lease) {
        taskExecutor.execute(() -> execute(lease));
    }

    void execute(ResearchExecutionLease lease) {
        AtomicBoolean leaseLost = new AtomicBoolean(false);
        ScheduledFuture<?> heartbeat = heartbeatScheduler.scheduleAtFixedRate(
                () -> refreshLease(lease, leaseLost),
                Duration.ofMillis(properties.getHeartbeatIntervalMs()));
        try {
            RunnableConfig threadConfig = threadConfig(lease);
            Optional<StateSnapshot> checkpoint = graph.lastStateOf(threadConfig);
            if (isPendingSelectionInterruption(lease.jobId(), checkpoint)) {
                jobStateService.markWaitingInput(lease);
                log.info("恢复市场调研人工关卡等待状态，jobId={}", lease.jobId());
                return;
            }
            RunnableConfig runnableConfig = runnableConfig(lease, threadConfig, checkpoint);
            NodeOutput finalOutput = graph.stream(
                            executionState(lease),
                            runnableConfig)
                    .doOnNext(output -> {
                        if (leaseLost.get()) {
                            throw new IllegalStateException("市场调研任务执行租约已丢失");
                        }
                        log.debug("市场调研Graph节点完成，jobId={}，node={}",
                                lease.jobId(), output.node());
                    })
                    .blockLast();
            if (finalOutput == null) {
                throw new IllegalStateException("市场调研Graph未返回最终节点输出");
            }
            if (leaseLost.get()) {
                throw new IllegalStateException("市场调研任务执行租约已丢失");
            }
            completeExecution(lease, threadConfig, finalOutput);
        } catch (Exception exception) {
            if (findCause(exception, ResearchJobCancelledException.class) != null) {
                jobStateService.markCancelled(lease);
                log.info("市场调研Graph已协作式取消，jobId={}", lease.jobId());
            } else {
                jobStateService.handleExecutionFailure(lease, exception);
                log.error("市场调研Graph执行异常，jobId={}，attempt={}",
                        lease.jobId(), lease.attemptCount(), exception);
            }
        } finally {
            if (heartbeat != null) {
                heartbeat.cancel(false);
            }
        }
    }

    private void refreshLease(ResearchExecutionLease lease, AtomicBoolean leaseLost) {
        try {
            if (!jobStateService.heartbeat(lease)) {
                leaseLost.set(true);
                log.warn("市场调研Graph心跳更新失败，执行租约可能已丢失，jobId={}，owner={}",
                        lease.jobId(), lease.executionOwner());
            }
        } catch (RuntimeException exception) {
            leaseLost.set(true);
            log.error("市场调研Graph心跳异常，jobId={}，owner={}",
                    lease.jobId(), lease.executionOwner(), exception);
        }
    }

    private RunnableConfig threadConfig(ResearchExecutionLease lease) {
        return RunnableConfig.builder()
                .threadId(lease.workflowVersion() + ":" + lease.jobId())
                .build();
    }

    private RunnableConfig runnableConfig(
            ResearchExecutionLease lease,
            RunnableConfig threadConfig,
            Optional<StateSnapshot> checkpoint) throws Exception {
        if (checkpoint.isEmpty()) {
            return threadConfig;
        }
        log.info("从Graph checkpoint恢复市场调研任务，jobId={}，workflowVersion={}",
                lease.jobId(), lease.workflowVersion());
        return graph.updateState(checkpoint.orElseThrow().config(), executionLeaseState(lease));
    }

    private boolean isPendingSelectionInterruption(
            String jobId, Optional<StateSnapshot> checkpoint) {
        return checkpoint
                .filter(snapshot -> ResearchGraphConfiguration.PRODUCT_SELECTION_GATE_NODE
                        .equals(snapshot.next()))
                .isPresent()
                && stageInputService.findSelection(jobId).isEmpty();
    }

    private void completeExecution(
            ResearchExecutionLease lease,
            RunnableConfig threadConfig,
            NodeOutput finalOutput) {
        if (!finalOutput.isEND()) {
            String nextNode = graph.lastStateOf(threadConfig)
                    .map(StateSnapshot::next)
                    .orElse(null);
            if (ResearchGraphConfiguration.PRODUCT_SELECTION_GATE_NODE.equals(nextNode)) {
                jobStateService.markWaitingInput(lease);
                log.info("市场调研Graph等待商品选择，jobId={}，attempt={}",
                        lease.jobId(), lease.attemptCount());
                return;
            }
            throw new IllegalStateException("市场调研Graph提前结束，nextNode=" + nextNode);
        }

        String outcome = finalOutput.state()
                .value(ResearchWorkflowNodes.STATE_WORKFLOW_OUTCOME, String.class)
                .orElseThrow(() -> new IllegalStateException("市场调研Graph缺少终态结果"));
        if (ResearchWorkflowNodes.OUTCOME_ABANDONED.equals(outcome)) {
            jobStateService.markAbandoned(lease);
        } else if (ResearchWorkflowNodes.OUTCOME_SUCCEEDED.equals(outcome)) {
            jobStateService.markSucceeded(lease);
        } else {
            throw new IllegalStateException("未知市场调研Graph终态: " + outcome);
        }
        log.info("市场调研Graph执行完成，jobId={}，attempt={}，outcome={}",
                lease.jobId(), lease.attemptCount(), outcome);
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

    private <T extends Throwable> T findCause(Throwable cause, Class<T> type) {
        Throwable current = cause;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }
}
