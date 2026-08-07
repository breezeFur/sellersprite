package cyou.yuanbaomao.sellersprite.research.model;

import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;

/** Agent 原始数据工具的服务端授权范围。 */
public record ResearchRawDataAccessScope(String jobId, ResearchStageCode stageCode) {

    public ResearchRawDataAccessScope {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("jobId 不能为空");
        }
        if (stageCode == null) {
            throw new IllegalArgumentException("stageCode 不能为空");
        }
    }
}
