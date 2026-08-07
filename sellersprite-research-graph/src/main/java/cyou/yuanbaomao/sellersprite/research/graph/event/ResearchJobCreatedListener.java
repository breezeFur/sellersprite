package cyou.yuanbaomao.sellersprite.research.graph.event;

import cyou.yuanbaomao.sellersprite.research.event.ResearchJobCreatedEvent;
import cyou.yuanbaomao.sellersprite.research.graph.runtime.ResearchGraphDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** 事务提交后低延迟唤醒Dispatcher；数据库轮询仍负责最终调度。 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "sellersprite.research",
        name = "dispatcher-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ResearchJobCreatedListener {

    private final ResearchGraphDispatcher dispatcher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(ResearchJobCreatedEvent event) {
        dispatcher.dispatchNow(event.jobId());
    }
}
