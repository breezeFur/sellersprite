package cyou.yuanbaomao.sellersprite.research.service;

/** 由报告子图同步触发初次分析，research 模块不感知具体 AI 实现。 */
public interface ResearchAnalysisStagePort {

    void runInitial(String jobId, String parentExecutionToken);
}
