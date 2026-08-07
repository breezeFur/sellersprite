package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventScope;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchEventEnvelope;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobDetailVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchStreamFrameVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchStreamStateVo;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class ResearchSseEmitterHubTest {

    private static final String JOB_ID = "job-stream-001";

    @Test
    void shouldSendSnapshotThenBufferedAndLiveEventsWithoutClosingAtTerminal() throws Exception {
        ResearchProperties properties = new ResearchProperties();
        ResearchStreamSnapshotService snapshotService = mock(ResearchStreamSnapshotService.class);
        ResearchSseFrameSender frameSender = mock(ResearchSseFrameSender.class);
        ResearchSseEmitterHub hub = new ResearchSseEmitterHub(properties, snapshotService, frameSender);
        SseEmitter emitter = mock(SseEmitter.class);
        ResearchStreamStateVo state = state("RUNNING");
        ResearchStreamStateVo terminalState = state("SUCCEEDED");
        when(snapshotService.snapshot(JOB_ID)).thenReturn(terminalState);
        properties.getEventStream().setLiveBatchWindowMs(50L);
        ResearchSseEmitterHub.Subscription subscription = hub.subscribe(JOB_ID, 0L, emitter);
        ResearchEventEnvelope replayed = event(1L, ResearchEventTypes.RESEARCH_NODE_STARTED);
        ResearchEventEnvelope buffered = event(2L, ResearchEventTypes.SUMMARY_DELTA);

        subscription.sendReplay(List.of(), state);
        subscription.sendReplay(List.of(replayed), null);
        hub.broadcast(buffered);
        subscription.finishReplay(() -> state);
        hub.broadcast(event(3L, ResearchEventTypes.WORKFLOW_COMPLETED));
        hub.broadcast(event(4L, ResearchEventTypes.PLAN));

        ArgumentCaptor<ResearchStreamFrameVo> frames = ArgumentCaptor.forClass(ResearchStreamFrameVo.class);
        verify(frameSender, org.mockito.Mockito.timeout(1_000).times(4))
                .send(any(SseEmitter.class), frames.capture());
        assertThat(frames.getAllValues())
                .extracting(ResearchStreamFrameVo::getFrameType)
                .containsExactly("snapshot", "events", "events", "events");
        assertThat(frames.getAllValues().get(0).getEvents()).isEmpty();
        assertThat(frames.getAllValues().get(0).getJob()).isEqualTo(state.getJob());
        assertThat(frames.getAllValues().get(1).getEvents()).containsExactly(replayed);
        assertThat(frames.getAllValues().get(2).isReplayComplete()).isTrue();
        assertThat(frames.getAllValues().get(3).getJob()).isEqualTo(terminalState.getJob());
        assertThat(frames.getAllValues().get(3).getEvents())
                .extracting(ResearchEventEnvelope::getSequenceNo)
                .containsExactly(2L, 3L, 4L);
        assertThat(subscription.isClosed()).isFalse();
        subscription.fail(new IllegalStateException("test complete"));
    }

    @Test
    void shouldSendEmptyReadyFrameForJobWithoutMissedEvents() throws Exception {
        ResearchProperties properties = new ResearchProperties();
        ResearchSseFrameSender frameSender = mock(ResearchSseFrameSender.class);
        ResearchSseEmitterHub hub = new ResearchSseEmitterHub(
                properties,
                mock(ResearchStreamSnapshotService.class),
                frameSender);
        ResearchSseEmitterHub.Subscription subscription =
                hub.subscribe(JOB_ID, 7L, mock(SseEmitter.class));
        ResearchStreamStateVo state = state("SUCCEEDED");

        subscription.sendReplay(List.of(), state);
        subscription.finishReplay(() -> state);

        ArgumentCaptor<ResearchStreamFrameVo> frames = ArgumentCaptor.forClass(ResearchStreamFrameVo.class);
        verify(frameSender, times(2)).send(any(SseEmitter.class), frames.capture());
        assertThat(frames.getAllValues().get(0).getFrameType()).isEqualTo("snapshot");
        assertThat(frames.getAllValues().get(0).getLastSequence()).isEqualTo(7L);
        assertThat(frames.getAllValues().get(1).isReplayComplete()).isTrue();
        assertThat(frames.getAllValues().get(1).getEvents()).isEmpty();
        subscription.fail(new IllegalStateException("test complete"));
    }

    @Test
    void shouldKeepConcurrentTerminalEventOutOfStaleHandoffFrame() throws Exception {
        ResearchProperties properties = new ResearchProperties();
        properties.getEventStream().setLiveBatchWindowMs(0L);
        ResearchStreamSnapshotService snapshotService = mock(ResearchStreamSnapshotService.class);
        ResearchSseFrameSender frameSender = mock(ResearchSseFrameSender.class);
        ResearchSseEmitterHub hub = new ResearchSseEmitterHub(properties, snapshotService, frameSender);
        ResearchSseEmitterHub.Subscription subscription =
                hub.subscribe(JOB_ID, 0L, mock(SseEmitter.class));
        ResearchStreamStateVo runningState = state("RUNNING");
        ResearchStreamStateVo terminalState = state("SUCCEEDED");
        CountDownLatch snapshotStarted = new CountDownLatch(1);
        CountDownLatch releaseSnapshot = new CountDownLatch(1);
        when(snapshotService.snapshot(JOB_ID)).thenReturn(terminalState);
        subscription.sendReplay(List.of(), runningState);

        Thread handoff = Thread.ofVirtual().start(() -> subscription.finishReplay(() -> {
            snapshotStarted.countDown();
            await(releaseSnapshot);
            return runningState;
        }));
        assertThat(snapshotStarted.await(1, TimeUnit.SECONDS)).isTrue();
        Thread terminalBroadcast = Thread.ofVirtual().start(
                () -> hub.broadcast(event(1L, ResearchEventTypes.WORKFLOW_COMPLETED)));
        terminalBroadcast.join(1_000L);
        assertThat(terminalBroadcast.isAlive()).isFalse();

        releaseSnapshot.countDown();
        handoff.join(1_000L);
        terminalBroadcast.join(1_000L);

        ArgumentCaptor<ResearchStreamFrameVo> frames = ArgumentCaptor.forClass(ResearchStreamFrameVo.class);
        verify(frameSender, org.mockito.Mockito.timeout(1_000).times(3))
                .send(any(SseEmitter.class), frames.capture());
        assertThat(frames.getAllValues().get(1).getJob()).isEqualTo(runningState.getJob());
        assertThat(frames.getAllValues().get(1).getEvents()).isEmpty();
        assertThat(frames.getAllValues().get(2).getEvents())
                .extracting(ResearchEventEnvelope::getSequenceNo)
                .containsExactly(1L);
        assertThat(frames.getAllValues().get(2).getJob()).isEqualTo(terminalState.getJob());
        subscription.fail(new IllegalStateException("test complete"));
    }

    @Test
    void shouldNotBlockBroadcastWhenEmitterWriteIsSlow() throws Exception {
        ResearchProperties properties = new ResearchProperties();
        properties.getEventStream().setLiveBatchWindowMs(0L);
        ResearchSseFrameSender frameSender = mock(ResearchSseFrameSender.class);
        CountDownLatch writeStarted = new CountDownLatch(1);
        CountDownLatch releaseWrite = new CountDownLatch(1);
        doAnswer(invocation -> {
            ResearchStreamFrameVo frame = invocation.getArgument(1);
            if (!frame.getEvents().isEmpty()) {
                writeStarted.countDown();
                await(releaseWrite);
            }
            return null;
        }).when(frameSender).send(any(SseEmitter.class), any(ResearchStreamFrameVo.class));
        ResearchSseEmitterHub hub = new ResearchSseEmitterHub(
                properties,
                mock(ResearchStreamSnapshotService.class),
                frameSender);
        ResearchSseEmitterHub.Subscription subscription =
                hub.subscribe(JOB_ID, 0L, mock(SseEmitter.class));
        subscription.sendReplay(List.of(), state("RUNNING"));
        subscription.finishReplay(() -> state("RUNNING"));

        long startedAt = System.nanoTime();
        hub.broadcast(event(1L, ResearchEventTypes.SUMMARY_DELTA));
        assertThat(writeStarted.await(1, TimeUnit.SECONDS)).isTrue();
        startedAt = System.nanoTime();
        hub.broadcast(event(2L, ResearchEventTypes.SUMMARY_DELTA));
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

        assertThat(elapsedMs).isLessThan(100L);
        releaseWrite.countDown();
        subscription.fail(new IllegalStateException("test complete"));
    }

    @Test
    void shouldCloseConnectionWhenBoundedQueueOverflowsDuringReplay() {
        ResearchProperties properties = new ResearchProperties();
        properties.getEventStream().setOutboundQueueCapacity(1);
        SseEmitter emitter = mock(SseEmitter.class);
        ResearchSseEmitterHub hub = new ResearchSseEmitterHub(
                properties,
                mock(ResearchStreamSnapshotService.class),
                mock(ResearchSseFrameSender.class));
        ResearchSseEmitterHub.Subscription subscription = hub.subscribe(JOB_ID, 0L, emitter);

        hub.broadcast(event(1L, ResearchEventTypes.SUMMARY_DELTA));
        hub.broadcast(event(2L, ResearchEventTypes.SUMMARY_DELTA));

        assertThat(subscription.isClosed()).isTrue();
        verify(emitter, org.mockito.Mockito.timeout(1_000))
                .completeWithError(any(IllegalStateException.class));
    }

    @Test
    void shouldPreservePreparedOrderWhenCommitCallbacksArriveInReverse() throws Exception {
        ResearchProperties properties = new ResearchProperties();
        properties.getEventStream().setLiveBatchWindowMs(20L);
        ResearchSseFrameSender frameSender = mock(ResearchSseFrameSender.class);
        ResearchSseEmitterHub hub = new ResearchSseEmitterHub(
                properties,
                mock(ResearchStreamSnapshotService.class),
                frameSender);
        ResearchSseEmitterHub.Subscription subscription =
                hub.subscribe(JOB_ID, 0L, mock(SseEmitter.class));
        subscription.sendReplay(List.of(), state("RUNNING"));
        subscription.finishReplay(() -> state("RUNNING"));
        ResearchSseEmitterHub.BroadcastTicket first =
                hub.prepareBroadcast(event(1L, ResearchEventTypes.SUMMARY_DELTA));
        ResearchSseEmitterHub.BroadcastTicket second =
                hub.prepareBroadcast(event(2L, ResearchEventTypes.SUMMARY_DELTA));

        second.commit();
        verify(frameSender, times(2)).send(any(SseEmitter.class), any(ResearchStreamFrameVo.class));
        first.commit();

        ArgumentCaptor<ResearchStreamFrameVo> frames = ArgumentCaptor.forClass(ResearchStreamFrameVo.class);
        verify(frameSender, org.mockito.Mockito.timeout(1_000).times(3))
                .send(any(SseEmitter.class), frames.capture());
        assertThat(frames.getAllValues().get(2).getEvents())
                .extracting(ResearchEventEnvelope::getSequenceNo)
                .containsExactly(1L, 2L);
        subscription.fail(new IllegalStateException("test complete"));
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(1, TimeUnit.SECONDS)) {
                throw new IllegalStateException("test latch timeout");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private ResearchStreamStateVo state(String status) {
        return ResearchStreamStateVo.builder()
                .job(ResearchJobDetailVo.builder().jobId(JOB_ID).status(status).build())
                .nodes(List.of())
                .build();
    }

    private ResearchEventEnvelope event(long sequence, String eventType) {
        return ResearchEventEnvelope.builder()
                .sequenceNo(sequence)
                .eventId("event-" + sequence)
                .jobId(JOB_ID)
                .scope(ResearchEventScope.RESEARCH)
                .eventType(eventType)
                .message(eventType)
                .build();
    }
}
