package com.yuanbaomao.sellersprite.research.service;

/**
 * 市场调研 Excel 生成与发布服务。
 */
public interface ResearchReportService {

    void renderDraft(String jobId);

    void validateAndPublish(String jobId);
}
