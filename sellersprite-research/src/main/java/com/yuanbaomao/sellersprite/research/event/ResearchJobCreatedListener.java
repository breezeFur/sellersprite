package com.yuanbaomao.sellersprite.research.event;

import com.yuanbaomao.sellersprite.research.batch.ResearchBatchLauncher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 主表事务提交后再启动异步 Batch，避免 Batch 读取不到任务。
 */
@Component
@RequiredArgsConstructor
public class ResearchJobCreatedListener {

    private final ResearchBatchLauncher batchLauncher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(ResearchJobCreatedEvent event) {
        batchLauncher.start(event.jobId());
    }
}
