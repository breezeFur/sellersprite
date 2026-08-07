package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.base.context.RequestContext;
import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchEventDao;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobDetailVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchStreamStateVo;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class ResearchEventStreamServiceTest {

    private static final String USER_ID = "user-stream-001";
    private static final String JOB_ID = "job-stream-001";

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void shouldReplayOnceThenWaitForDirectEmitterPushWithoutPollingEventTable() throws Exception {
        ResearchProperties properties = new ResearchProperties();
        MarketResearchEventDao eventDao = mock(MarketResearchEventDao.class);
        ResearchSseEventPublisher eventPublisher = mock(ResearchSseEventPublisher.class);
        ResearchStreamSnapshotService snapshotService = mock(ResearchStreamSnapshotService.class);
        ResearchSseFrameSender frameSender = mock(ResearchSseFrameSender.class);
        ResearchSseEmitterHub hub = new ResearchSseEmitterHub(properties, snapshotService, frameSender);
        ResearchEventStreamService streamService = new ResearchEventStreamService(
                eventDao, eventPublisher, properties, snapshotService, hub);
        ResearchStreamStateVo state = ResearchStreamStateVo.builder()
                .job(ResearchJobDetailVo.builder().jobId(JOB_ID).status("RUNNING").build())
                .nodes(List.of())
                .build();
        setUserContext();
        when(snapshotService.requireOwnedSnapshot(JOB_ID, USER_ID)).thenReturn(state);
        when(eventDao.listByJobIdAfterSequence(JOB_ID, 0L, 500)).thenReturn(List.of());

        SseEmitter emitter = streamService.stream(JOB_ID, 0L);

        verify(frameSender, timeout(1_000).atLeast(2)).send(any(SseEmitter.class), any());
        verify(eventDao, after(300).times(1)).listByJobIdAfterSequence(JOB_ID, 0L, 500);
        emitter.complete();
    }

    @Test
    void shouldClampConfiguredReplayBatchToDaoMaximum() {
        ResearchProperties properties = new ResearchProperties();
        properties.getEventStream().setReplayBatchSize(10_000);
        MarketResearchEventDao eventDao = mock(MarketResearchEventDao.class);
        ResearchStreamSnapshotService snapshotService = mock(ResearchStreamSnapshotService.class);
        ResearchSseEmitterHub hub = mock(ResearchSseEmitterHub.class);
        ResearchSseEmitterHub.Subscription subscription = mock(ResearchSseEmitterHub.Subscription.class);
        ResearchEventStreamService streamService = new ResearchEventStreamService(
                eventDao,
                mock(ResearchSseEventPublisher.class),
                properties,
                snapshotService,
                hub);
        ResearchStreamStateVo state = ResearchStreamStateVo.builder()
                .job(ResearchJobDetailVo.builder().jobId(JOB_ID).status("RUNNING").build())
                .nodes(List.of())
                .build();
        setUserContext();
        when(snapshotService.requireOwnedSnapshot(JOB_ID, USER_ID)).thenReturn(state);
        when(hub.subscribe(JOB_ID, 0L)).thenReturn(subscription);
        when(subscription.emitter()).thenReturn(mock(SseEmitter.class));
        when(subscription.isClosed()).thenReturn(false);
        when(eventDao.listByJobIdAfterSequence(JOB_ID, 0L, 5_000)).thenReturn(List.of());

        streamService.stream(JOB_ID, 0L);

        verify(eventDao, timeout(1_000)).listByJobIdAfterSequence(JOB_ID, 0L, 5_000);
    }

    @Test
    void shouldRejectStreamBeforeReadingEventsWhenRequestHasNoUser() {
        ResearchProperties properties = new ResearchProperties();
        MarketResearchEventDao eventDao = mock(MarketResearchEventDao.class);
        ResearchSseEmitterHub hub = mock(ResearchSseEmitterHub.class);
        ResearchEventStreamService streamService = new ResearchEventStreamService(
                eventDao,
                mock(ResearchSseEventPublisher.class),
                properties,
                mock(ResearchStreamSnapshotService.class),
                hub);

        assertThatThrownBy(() -> streamService.stream(JOB_ID, 0L))
                .isInstanceOf(BizException.class);
        verify(eventDao, never()).listByJobIdAfterSequence(any(), anyLong(), anyInt());
        verify(hub, never()).subscribe(any(), anyLong());
    }

    private void setUserContext() {
        RequestContextHolder.set(RequestContext.builder()
                .userId(USER_ID)
                .username("stream-user")
                .build());
    }
}
