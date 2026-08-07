package cyou.yuanbaomao.sellersprite.ai.research.runtime;

import cyou.yuanbaomao.sellersprite.research.model.ResearchAnalysisLease;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisStagePort;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisStateService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchStageAnalysisPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 把父 Graph 的阶段分析节点适配到现有 Curation 执行链。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultResearchAnalysisStageAdapter
        implements ResearchAnalysisStagePort, ResearchStageAnalysisPort {

    private final ResearchAnalysisService analysisService;
    private final ResearchAnalysisStateService stateService;
    private final ResearchAnalysisExecutor executor;

    @Override
    public void runInitial(String jobId, String parentExecutionToken) {
        runStage(jobId, parentExecutionToken, ResearchStageCode.FINAL_ANALYSIS);
    }

    @Override
    public void runStage(
            String jobId, String parentExecutionToken, ResearchStageCode stage) {
        analysisService.ensureStageRun(jobId, stage);
        Optional<ResearchAnalysisLease> lease = stateService.tryStartStage(
                jobId, parentExecutionToken, stage);
        if (lease.isEmpty()) {
            if (stateService.isStageSucceeded(jobId, stage)) {
                return;
            }
            throw new IllegalStateException("阶段分析无法由父Graph启动: " + jobId + "/" + stage);
        }
        try {
            executor.executeSynchronously(lease.orElseThrow());
        } catch (RuntimeException exception) {
            if (!stateService.isStageFailed(jobId, stage)) {
                throw exception;
            }
            log.warn("阶段AI分析失败，保留失败运行并继续完成数据Graph，jobId={}，stage={}",
                    jobId, stage, exception);
            return;
        }
        if (!stateService.isStageSucceeded(jobId, stage)) {
            if (stateService.isStageFailed(jobId, stage)) {
                log.warn("阶段AI分析未成功，保留失败运行并继续完成数据Graph，jobId={}，stage={}",
                        jobId, stage);
                return;
            }
            throw new IllegalStateException("阶段分析未成功完成: " + jobId + "/" + stage);
        }
    }
}
