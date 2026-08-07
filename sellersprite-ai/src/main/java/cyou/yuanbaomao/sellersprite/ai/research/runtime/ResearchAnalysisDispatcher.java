package cyou.yuanbaomao.sellersprite.ai.research.runtime;

import cyou.yuanbaomao.sellersprite.ai.research.curation.config.CurationAnalysisProperties;
import cyou.yuanbaomao.sellersprite.research.model.ResearchAnalysisLease;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisStateService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 从数据库领取已完成数据 Graph 的分析运行。 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = CurationAnalysisProperties.PREFIX,
        name = "enabled",
        havingValue = "true")
public class ResearchAnalysisDispatcher {

    private final String executionOwner = "analysis-" + UUID.randomUUID();

    private final ResearchAnalysisStateService stateService;
    private final ResearchAnalysisExecutor executor;
    private final CurationAnalysisProperties properties;

    @Scheduled(fixedDelayString = "${sellersprite.research.analysis.poll-interval-ms:2000}")
    public void poll() {
        if (!properties.isDispatcherEnabled()) {
            return;
        }
        for (String analysisRunId : stateService.listDispatchCandidates(
                properties.getDispatchBatchSize())) {
            dispatchNow(analysisRunId);
        }
    }

    public void dispatchNow(String analysisRunId) {
        stateService.tryClaim(
                        analysisRunId,
                        executionOwner,
                        properties.getLeaseDurationMs())
                .ifPresent(this::submit);
    }

    private void submit(ResearchAnalysisLease lease) {
        try {
            executor.submit(lease);
        } catch (RuntimeException exception) {
            log.error("提交市场调研分析失败，analysisRunId={}", lease.analysisRunId(), exception);
            stateService.handleFailure(
                    lease,
                    "ANALYSIS_SUBMIT_FAILED",
                    exception.getMessage(),
                    true);
        }
    }
}
