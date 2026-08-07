package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchEventDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchEvent;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventScope;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchEventEnvelope;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.ObjectMapper;

/**
 * 持久化市场调研用户可见事件。
 *
 * <p>SSE 订阅端只读取本服务已经提交的记录，因此天然满足“先落库、后发送”。</p>
 */
@Service
@RequiredArgsConstructor
public class ResearchSseEventPublisher {

    private static final String EMPTY_PAYLOAD = "{}";

    private final MarketResearchEventDao eventDao;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;
    private final ResearchSseEmitterHub emitterHub;

    @Transactional(rollbackFor = Exception.class)
    public ResearchEventEnvelope publish(ResearchEventCommand command) {
        requireText(command.getJobId(), "jobId");
        requireText(command.getEventType(), "eventType");
        if (command.getScope() == null) {
            throw new IllegalArgumentException("事件scope不能为空");
        }

        MarketResearchEvent event = new MarketResearchEvent();
        event.setEventId(idGenerator.nextId());
        event.setJobId(command.getJobId());
        event.setConversationId(normalize(command.getConversationId()));
        event.setAnalysisRunId(normalize(command.getAnalysisRunId()));
        event.setScope(command.getScope().name());
        event.setEventType(command.getEventType());
        event.setPhase(normalize(command.getPhase()));
        event.setSheetName(normalize(command.getSheetName()));
        event.setNodeCode(normalize(command.getNodeCode()));
        event.setMessage(command.getMessage() == null ? "" : command.getMessage());
        event.setPayload(writePayload(command.getPayload()));
        event.setTerminal(command.isTerminal() ? 1 : 0);
        ResearchEventEnvelope envelope = toEnvelope(eventDao.saveEvent(event));
        broadcastAfterCommit(emitterHub.prepareBroadcast(envelope));
        return envelope;
    }

    public ResearchEventEnvelope toEnvelope(MarketResearchEvent event) {
        return ResearchEventEnvelope.builder()
                .sequenceNo(event.getSequenceNo())
                .eventId(event.getEventId())
                .jobId(event.getJobId())
                .conversationId(event.getConversationId())
                .analysisRunId(event.getAnalysisRunId())
                .scope(ResearchEventScope.valueOf(event.getScope()))
                .eventType(event.getEventType())
                .phase(event.getPhase())
                .sheetName(event.getSheetName())
                .nodeCode(event.getNodeCode())
                .message(event.getMessage())
                .payload(readPayload(event.getPayload()))
                .terminal(Integer.valueOf(1).equals(event.getTerminal()))
                .createdAt(event.getCreatedAt())
                .build();
    }

    private String writePayload(Object payload) {
        if (payload == null) {
            return EMPTY_PAYLOAD;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception exception) {
            throw new IllegalArgumentException("序列化市场调研事件payload失败", exception);
        }
    }

    private Object readPayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(payload, Object.class);
        } catch (Exception exception) {
            return Map.of("raw", payload, "parseError", true);
        }
    }

    private void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("事件" + name + "不能为空");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void broadcastAfterCommit(ResearchSseEmitterHub.BroadcastTicket ticket) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            ticket.commit();
            return;
        }
        try {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    ticket.commit();
                }

                @Override
                public void afterCompletion(int status) {
                    if (status != TransactionSynchronization.STATUS_COMMITTED) {
                        ticket.cancel();
                    }
                }
            });
        } catch (RuntimeException exception) {
            ticket.cancel();
            throw exception;
        }
    }
}
