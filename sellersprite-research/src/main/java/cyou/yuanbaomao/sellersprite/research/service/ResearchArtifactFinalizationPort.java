package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.sellersprite.research.enums.ResearchSelectionDecision;

/** 在父 Graph 终态节点统一生成并发布本次流程应有的文件。 */
public interface ResearchArtifactFinalizationPort {

    void finalizeArtifacts(
            String jobId,
            String parentExecutionToken,
            ResearchSelectionDecision decision);
}
