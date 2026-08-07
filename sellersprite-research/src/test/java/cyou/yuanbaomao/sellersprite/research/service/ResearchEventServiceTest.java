package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchEventDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchEvent;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventScope;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchEventEnvelope;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class ResearchEventServiceTest {

    private static final String JOB_ID = "job-event-001";

    @Mock
    private MarketResearchEventDao eventDao;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private ResearchSseEmitterHub emitterHub;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        RequestContextHolder.clear();
    }

    @Test
    void shouldPersistEventBeforeReturningReplayEnvelope() {
        ResearchSseEmitterHub.BroadcastTicket ticket = org.mockito.Mockito.mock(
                ResearchSseEmitterHub.BroadcastTicket.class);
        when(idGenerator.nextId()).thenReturn("event-001");
        when(eventDao.saveEvent(any(MarketResearchEvent.class))).thenAnswer(invocation -> {
            MarketResearchEvent event = invocation.getArgument(0);
            event.setSequenceNo(17L);
            event.setCreatedAt(1_722_000_000_000L);
            return event;
        });
        when(emitterHub.prepareBroadcast(any(ResearchEventEnvelope.class))).thenReturn(ticket);
        ResearchSseEventPublisher publisher =
                new ResearchSseEventPublisher(eventDao, idGenerator, new ObjectMapper(), emitterHub);

        ResearchEventEnvelope envelope = publisher.publish(ResearchEventCommand.builder()
                .jobId(JOB_ID)
                .analysisRunId(" run-001 ")
                .scope(ResearchEventScope.ANALYSIS)
                .eventType("summary_delta")
                .phase("summary")
                .message("结")
                .payload(Map.of("data", "结", "stepIndex", 7))
                .build());

        ArgumentCaptor<MarketResearchEvent> eventCaptor =
                ArgumentCaptor.forClass(MarketResearchEvent.class);
        verify(eventDao).saveEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getPayload())
                .contains("\"data\":\"结\"")
                .contains("\"stepIndex\":7");
        assertThat(envelope.getSequenceNo()).isEqualTo(17L);
        assertThat(envelope.getAnalysisRunId()).isEqualTo("run-001");
        assertThat(envelope.getPayload()).isEqualTo(Map.of("data", "结", "stepIndex", 7));
        assertThat(envelope.isTerminal()).isFalse();
        verify(emitterHub).prepareBroadcast(envelope);
        verify(ticket).commit();
    }

    @Test
    void shouldBroadcastOnlyAfterTransactionCommit() {
        ResearchSseEmitterHub.BroadcastTicket ticket = org.mockito.Mockito.mock(
                ResearchSseEmitterHub.BroadcastTicket.class);
        when(idGenerator.nextId()).thenReturn("event-commit-001");
        when(eventDao.saveEvent(any(MarketResearchEvent.class))).thenAnswer(invocation -> {
            MarketResearchEvent event = invocation.getArgument(0);
            event.setSequenceNo(18L);
            event.setCreatedAt(1_722_000_000_001L);
            return event;
        });
        when(emitterHub.prepareBroadcast(any(ResearchEventEnvelope.class))).thenReturn(ticket);
        ResearchSseEventPublisher publisher =
                new ResearchSseEventPublisher(eventDao, idGenerator, new ObjectMapper(), emitterHub);
        TransactionSynchronizationManager.initSynchronization();

        ResearchEventEnvelope envelope = publisher.publish(ResearchEventCommand.builder()
                .jobId(JOB_ID)
                .scope(ResearchEventScope.RESEARCH)
                .eventType("research_node_started")
                .message("开始执行")
                .build());

        verify(ticket, never()).commit();
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);
        verify(ticket).commit();
    }
}
