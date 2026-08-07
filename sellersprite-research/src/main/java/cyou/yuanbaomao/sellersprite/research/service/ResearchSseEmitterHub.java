package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchEventDao;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchEventEnvelope;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchStreamFrameVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchStreamStateVo;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 保存当前进程中的SSE连接，并在事件事务提交后直接推送。
 *
 * <p>数据库只用于连接建立时的断点回放；实时阶段不查询事件表。</p>
 */
@Slf4j
@Service
public class ResearchSseEmitterHub {

    private static final String FRAME_TYPE_SNAPSHOT = "snapshot";
    private static final String FRAME_TYPE_EVENTS = "events";

    private static final Set<String> STATE_REFRESH_EVENT_TYPES = Set.of(
            ResearchEventTypes.WORKFLOW_STARTED,
            ResearchEventTypes.RESEARCH_RETRY_SCHEDULED,
            ResearchEventTypes.RESEARCH_NODE_STARTED,
            ResearchEventTypes.RESEARCH_NODE_PROGRESS,
            ResearchEventTypes.RESEARCH_NODE_COMPLETED,
            ResearchEventTypes.RESEARCH_NODE_FAILED,
            ResearchEventTypes.RESEARCH_NODE_CANCELLED,
            ResearchEventTypes.RESEARCH_COMPLETED,
            ResearchEventTypes.WORKBOOK_READY,
            ResearchEventTypes.ANALYSIS_WAITING_RESEARCH,
            ResearchEventTypes.ANALYSIS_QUEUED,
            ResearchEventTypes.ANALYSIS_CANCEL_REQUESTED,
            ResearchEventTypes.PLAN,
            ResearchEventTypes.WORKBOOK,
            ResearchEventTypes.SHEET_PREPARE,
            ResearchEventTypes.SHEET,
            ResearchEventTypes.SHEET_FOCUS,
            ResearchEventTypes.SHEET_THINK,
            ResearchEventTypes.SUMMARY_PREPARE,
            ResearchEventTypes.SUMMARY,
            ResearchEventTypes.REPORT,
            ResearchEventTypes.DOWNLOAD,
            ResearchEventTypes.DONE,
            ResearchEventTypes.ERROR,
            ResearchEventTypes.WORKFLOW_COMPLETED,
            ResearchEventTypes.WORKFLOW_FAILED,
            ResearchEventTypes.WORKFLOW_CANCELLED,
            ResearchEventTypes.STAGE_COMPLETED,
            ResearchEventTypes.PRODUCT_SELECTION_REQUIRED,
            ResearchEventTypes.PRODUCT_SELECTION_SUBMITTED,
            ResearchEventTypes.MARKET_ABANDONED);

    private final Map<String, Set<Subscription>> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, CommitQueue> commitQueues = new ConcurrentHashMap<>();
    private final ResearchProperties properties;
    private final ResearchStreamSnapshotService snapshotService;
    private final ResearchSseFrameSender frameSender;

    public ResearchSseEmitterHub(
            ResearchProperties properties,
            ResearchStreamSnapshotService snapshotService,
            ResearchSseFrameSender frameSender) {
        this.properties = properties;
        this.snapshotService = snapshotService;
        this.frameSender = frameSender;
    }

    public Subscription subscribe(String jobId, long afterSequence) {
        return subscribe(jobId, afterSequence,
                new SseEmitter(properties.getEventStream().getTimeoutMs()));
    }

    Subscription subscribe(String jobId, long afterSequence, SseEmitter emitter) {
        Subscription subscription = new Subscription(
                jobId,
                Math.max(0L, afterSequence),
                MarketResearchEventDao.normalizeReplayLimit(
                        properties.getEventStream().getReplayBatchSize()),
                Math.max(0L, properties.getEventStream().getLiveBatchWindowMs()),
                Math.max(1, properties.getEventStream().getOutboundQueueCapacity()),
                emitter);
        subscriptions.computeIfAbsent(jobId, ignored -> ConcurrentHashMap.newKeySet())
                .add(subscription);
        emitter.onCompletion(subscription::close);
        emitter.onTimeout(subscription::close);
        emitter.onError(ignored -> subscription.close());
        return subscription;
    }

    public void broadcast(ResearchEventEnvelope event) {
        Set<Subscription> active = subscriptions.get(event.getJobId());
        if (active == null || active.isEmpty()) {
            return;
        }
        active.forEach(subscription -> subscription.accept(event));
    }

    /**
     * 在事件事务仍持有任务级流锁时登记通知顺序，提交回调只负责确认该槽位。
     */
    public BroadcastTicket prepareBroadcast(ResearchEventEnvelope event) {
        PendingBroadcast[] pending = new PendingBroadcast[1];
        commitQueues.compute(event.getJobId(), (jobId, existing) -> {
            CommitQueue queue = existing == null ? new CommitQueue(jobId) : existing;
            synchronized (queue) {
                pending[0] = queue.add(event);
            }
            return queue;
        });
        return new BroadcastTicket() {
            @Override
            public void commit() {
                resolveBroadcast(pending[0], PendingState.COMMITTED);
            }

            @Override
            public void cancel() {
                resolveBroadcast(pending[0], PendingState.CANCELLED);
            }
        };
    }

    @Scheduled(fixedDelayString = "${sellersprite.research.event-stream.heartbeat-interval-ms:15000}")
    void heartbeat() {
        subscriptions.values().forEach(active -> active.forEach(Subscription::heartbeat));
    }

    private ResearchStreamStateVo snapshotSafely(String jobId) {
        try {
            return snapshotService.snapshot(jobId);
        } catch (RuntimeException exception) {
            log.warn("市场调研SSE状态快照刷新失败，仍继续推送业务事件，jobId={}", jobId, exception);
            return null;
        }
    }

    private void remove(Subscription subscription) {
        subscriptions.computeIfPresent(subscription.jobId, (jobId, active) -> {
            active.remove(subscription);
            return active.isEmpty() ? null : active;
        });
    }

    private void resolveBroadcast(PendingBroadcast pending, PendingState state) {
        CommitQueue queue = pending.queue;
        synchronized (queue) {
            if (pending.state != PendingState.PENDING) {
                return;
            }
            pending.state = state;
            while (!queue.entries.isEmpty()
                    && queue.entries.peekFirst().state != PendingState.PENDING) {
                PendingBroadcast ready = queue.entries.removeFirst();
                if (ready.state == PendingState.COMMITTED) {
                    broadcast(ready.event);
                }
            }
        }
        commitQueues.compute(pending.event.getJobId(), (jobId, current) -> {
            if (current != queue) {
                return current;
            }
            synchronized (queue) {
                return queue.entries.isEmpty() ? null : queue;
            }
        });
    }

    public interface BroadcastTicket {

        void commit();

        void cancel();
    }

    private enum PendingState {
        PENDING,
        COMMITTED,
        CANCELLED
    }

    private static final class CommitQueue {

        private final String jobId;
        private final Deque<PendingBroadcast> entries = new ArrayDeque<>();

        private CommitQueue(String jobId) {
            this.jobId = jobId;
        }

        private PendingBroadcast add(ResearchEventEnvelope event) {
            if (!jobId.equals(event.getJobId())) {
                throw new IllegalArgumentException("事件任务与提交队列不匹配");
            }
            PendingBroadcast pending = new PendingBroadcast(this, event);
            entries.addLast(pending);
            return pending;
        }
    }

    private static final class PendingBroadcast {

        private final CommitQueue queue;
        private final ResearchEventEnvelope event;
        private PendingState state = PendingState.PENDING;

        private PendingBroadcast(CommitQueue queue, ResearchEventEnvelope event) {
            this.queue = queue;
            this.event = event;
        }
    }

    public final class Subscription {

        private final String jobId;
        private final int batchSize;
        private final long liveBatchWindowMs;
        private final SseEmitter emitter;
        private final BlockingQueue<ResearchEventEnvelope> outboundEvents;
        private final Set<Long> queuedSequences = new HashSet<>();
        private final Object ingressMonitor = new Object();
        private final Thread writerThread;
        private final AtomicBoolean closed = new AtomicBoolean();
        private volatile long cursor;
        private boolean snapshotSent;
        private boolean replaying = true;

        private Subscription(
                String jobId,
                long afterSequence,
                int batchSize,
                long liveBatchWindowMs,
                int outboundQueueCapacity,
                SseEmitter emitter) {
            this.jobId = jobId;
            this.cursor = afterSequence;
            this.batchSize = batchSize;
            this.liveBatchWindowMs = liveBatchWindowMs;
            this.emitter = emitter;
            this.outboundEvents = new ArrayBlockingQueue<>(outboundQueueCapacity);
            this.writerThread = Thread.ofVirtual()
                    .name("market-research-sse-writer-" + shortJobId(jobId))
                    .unstarted(this::writeLiveEvents);
            this.writerThread.start();
        }

        public SseEmitter emitter() {
            return emitter;
        }

        public boolean isClosed() {
            return closed.get();
        }

        public synchronized void sendReplay(
                List<ResearchEventEnvelope> events,
                ResearchStreamStateVo initialState) {
            if (closed.get()) {
                return;
            }
            List<ResearchEventEnvelope> unsent = normalizedUnsent(events);
            String frameType = snapshotSent ? FRAME_TYPE_EVENTS : FRAME_TYPE_SNAPSHOT;
            ResearchStreamStateVo state = snapshotSent ? null : initialState;
            sendFrame(frameType, unsent, state, false);
            snapshotSent = true;
        }

        public synchronized void finishReplay(Supplier<ResearchStreamStateVo> stateSupplier) {
            if (closed.get()) {
                return;
            }
            ResearchStreamStateVo currentState = stateSupplier.get();
            if (!snapshotSent) {
                sendFrame(FRAME_TYPE_SNAPSHOT, List.of(), currentState, false);
                snapshotSent = true;
            }
            replaying = false;
            sendFrame(FRAME_TYPE_EVENTS, List.of(), currentState, true);
            notifyAll();
        }

        public void fail(Throwable exception) {
            if (!markClosed()) {
                return;
            }
            completeWithError(exception);
        }

        private void accept(ResearchEventEnvelope event) {
            if (closed.get() || event.getSequenceNo() <= cursor) {
                return;
            }
            boolean overflow = false;
            synchronized (ingressMonitor) {
                if (closed.get() || event.getSequenceNo() <= cursor
                        || !queuedSequences.add(event.getSequenceNo())) {
                    return;
                }
                if (!outboundEvents.offer(event)) {
                    queuedSequences.remove(event.getSequenceNo());
                    overflow = true;
                }
            }
            if (overflow && markClosed()) {
                Thread.ofVirtual().start(() -> completeWithError(
                        new IllegalStateException("市场调研SSE待发送事件超过连接容量，请断线重放")));
            }
        }

        private synchronized void heartbeat() {
            if (closed.get()) {
                return;
            }
            try {
                frameSender.heartbeat(emitter);
            } catch (IOException | IllegalStateException exception) {
                closeAfterSendFailure(exception);
            }
        }

        private List<ResearchEventEnvelope> normalizedUnsent(List<ResearchEventEnvelope> events) {
            return events.stream()
                    .filter(event -> event != null && event.getSequenceNo() > cursor)
                    .sorted(Comparator.comparingLong(ResearchEventEnvelope::getSequenceNo))
                    .toList();
        }

        private void writeLiveEvents() {
            try {
                if (!awaitReplayCompletion()) {
                    return;
                }
                while (!Thread.currentThread().isInterrupted()) {
                    ResearchEventEnvelope first = outboundEvents.take();
                    List<ResearchEventEnvelope> batch = collectBatch(first);
                    sendLiveBatch(batch);
                    if (isClosed()) {
                        return;
                    }
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException exception) {
                log.warn("市场调研SSE异步写线程异常，jobId={}", jobId, exception);
                fail(exception);
            }
        }

        private synchronized boolean awaitReplayCompletion() throws InterruptedException {
            while (replaying && !closed.get()) {
                wait();
            }
            return !closed.get();
        }

        private List<ResearchEventEnvelope> collectBatch(ResearchEventEnvelope first)
                throws InterruptedException {
            List<ResearchEventEnvelope> batch = new ArrayList<>(batchSize);
            batch.add(first);
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(liveBatchWindowMs);
            while (batch.size() < batchSize) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0L) {
                    break;
                }
                ResearchEventEnvelope next = outboundEvents.poll(remaining, TimeUnit.NANOSECONDS);
                if (next == null) {
                    break;
                }
                batch.add(next);
            }
            return batch;
        }

        private void sendLiveBatch(List<ResearchEventEnvelope> batch) {
            boolean refreshState = batch.stream()
                    .anyMatch(event -> STATE_REFRESH_EVENT_TYPES.contains(event.getEventType()));
            ResearchStreamStateVo state = refreshState ? snapshotSafely(jobId) : null;
            synchronized (this) {
                try {
                    if (!closed.get()) {
                        List<ResearchEventEnvelope> unsent = normalizedUnsent(batch);
                        if (!unsent.isEmpty()) {
                            sendFrame(FRAME_TYPE_EVENTS, unsent, state, true);
                        }
                    }
                } finally {
                    synchronized (ingressMonitor) {
                        batch.forEach(event -> queuedSequences.remove(event.getSequenceNo()));
                    }
                }
            }
        }

        private void sendFrame(
                String frameType,
                List<ResearchEventEnvelope> events,
                ResearchStreamStateVo state,
                boolean replayComplete) {
            long afterSequence = cursor;
            long lastSequence = events.isEmpty()
                    ? cursor
                    : events.get(events.size() - 1).getSequenceNo();
            ResearchStreamFrameVo frame = ResearchStreamFrameVo.builder()
                    .frameType(frameType)
                    .jobId(jobId)
                    .afterSequence(afterSequence)
                    .lastSequence(lastSequence)
                    .replayComplete(replayComplete)
                    .job(state == null ? null : state.getJob())
                    .nodes(state == null ? null : state.getNodes())
                    .events(List.copyOf(events))
                    .build();
            try {
                frameSender.send(emitter, frame);
                cursor = lastSequence;
            } catch (IOException | IllegalStateException exception) {
                closeAfterSendFailure(exception);
            }
        }

        private void closeAfterSendFailure(Exception exception) {
            if (!markClosed()) {
                return;
            }
            log.debug("市场调研SSE连接已关闭，jobId={}，afterSequence={}，reason={}",
                    jobId, cursor, exception.getMessage());
        }

        private void close() {
            markClosed();
        }

        private boolean markClosed() {
            if (!closed.compareAndSet(false, true)) {
                return false;
            }
            remove(this);
            writerThread.interrupt();
            return true;
        }

        private void completeWithError(Throwable exception) {
            try {
                emitter.completeWithError(exception);
            } catch (IllegalStateException ignored) {
                // 容器可能已经完成连接。
            }
        }
    }

    private static String shortJobId(String jobId) {
        return jobId.length() <= 8 ? jobId : jobId.substring(0, 8);
    }
}
