package cyou.yuanbaomao.sellersprite.research.service;

/**
 * 市场调研 Excel 生成与发布服务。
 */
public interface ResearchReportService {

    void renderRawDraft(String jobId);

    void validateAndPublishRaw(String jobId);

    void renderEvidenceDraft(String jobId);

    void validateAndPublishEvidence(String jobId);
}
