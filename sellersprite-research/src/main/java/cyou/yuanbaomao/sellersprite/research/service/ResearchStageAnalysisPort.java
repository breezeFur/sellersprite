package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;

/** 由阶段子图同步触发逐表分析，research 模块不感知具体 AI 实现。 */
public interface ResearchStageAnalysisPort {

    void runStage(String jobId, String parentExecutionToken, ResearchStageCode stage);
}
