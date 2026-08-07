package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchEventDao;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchEventEnvelope;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchStreamStateVo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** 建立SseEmitter订阅并执行一次性断点回放。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResearchEventStreamService {

    private static final long FIRST_SEQUENCE = 0L;

    private final MarketResearchEventDao eventDao;
    private final ResearchSseEventPublisher eventPublisher;
    private final ResearchProperties properties;
    private final ResearchStreamSnapshotService snapshotService;
    private final ResearchSseEmitterHub emitterHub;

    public SseEmitter stream(String jobId, long afterSequence) {
        String userId = currentUserId();
        ResearchStreamStateVo initialState = snapshotService.requireOwnedSnapshot(jobId, userId);
        long cursor = normalizeSequence(afterSequence);
        ResearchSseEmitterHub.Subscription subscription = emitterHub.subscribe(jobId, cursor);
        Thread.ofVirtual()
                .name("market-research-sse-" + shortJobId(jobId))
                .start(() -> replay(jobId, userId, cursor, initialState, subscription));
        return subscription.emitter();
    }

    private void replay(
            String jobId,
            String userId,
            long afterSequence,
            ResearchStreamStateVo initialState,
            ResearchSseEmitterHub.Subscription subscription) {
        long cursor = afterSequence;
        int batchSize = MarketResearchEventDao.normalizeReplayLimit(
                properties.getEventStream().getReplayBatchSize());
        try {
            subscription.sendReplay(List.of(), initialState);
            while (!subscription.isClosed()) {
                List<ResearchEventEnvelope> events = eventDao
                        .listByJobIdAfterSequence(jobId, cursor, batchSize)
                        .stream()
                        .map(eventPublisher::toEnvelope)
                        .toList();
                if (!events.isEmpty()) {
                    subscription.sendReplay(events, null);
                }
                if (events.isEmpty()) {
                    break;
                }
                cursor = events.get(events.size() - 1).getSequenceNo();
                if (events.size() < batchSize) {
                    break;
                }
            }
            if (!subscription.isClosed()) {
                subscription.finishReplay(
                        () -> snapshotService.requireOwnedSnapshot(jobId, userId));
            }
        } catch (RuntimeException exception) {
            log.warn("市场调研SSE断点回放异常，jobId={}，afterSequence={}", jobId, cursor, exception);
            subscription.fail(exception);
        }
    }

    private String currentUserId() {
        return RequestContextHolder.get()
                .map(context -> context.getUserId())
                .filter(value -> value != null && !value.isBlank())
                .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
    }

    private long normalizeSequence(long sequence) {
        return Math.max(FIRST_SEQUENCE, sequence);
    }

    private String shortJobId(String jobId) {
        return jobId.length() <= 8 ? jobId : jobId.substring(0, 8);
    }
}
