package com.yuanbaomao.sellersprite.research.event;

/**
 * 任务主表提交后触发 Batch 的领域事件。
 */
public record ResearchJobCreatedEvent(String jobId) {
}
