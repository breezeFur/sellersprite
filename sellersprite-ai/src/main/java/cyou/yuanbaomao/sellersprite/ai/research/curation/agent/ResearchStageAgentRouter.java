package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/** 按分析运行类型选择职责独立的 prototype Agent。 */
@Component
@RequiredArgsConstructor
public class ResearchStageAgentRouter {

    private final ObjectProvider<ScreeningAnalysisAgent> screeningAgentProvider;
    private final ObjectProvider<DeepDiveAnalysisAgent> deepDiveAgentProvider;
    private final ObjectProvider<FinalDecisionAgent> finalDecisionAgentProvider;
    private final ObjectProvider<FollowUpAnswerAgent> followUpAnswerAgentProvider;

    public AmazonSelectionManus getAgent(ResearchAnalysisRunType runType) {
        return switch (runType) {
            case SCREENING -> screeningAgentProvider.getObject();
            case DEEP_DIVE -> deepDiveAgentProvider.getObject();
            case FINAL_ANALYSIS, INITIAL, RETRY -> finalDecisionAgentProvider.getObject();
            case FOLLOW_UP -> followUpAnswerAgentProvider.getObject();
        };
    }

    public boolean usesFinalAnalysisPath(ResearchAnalysisRunType runType) {
        return runType != ResearchAnalysisRunType.SCREENING
                && runType != ResearchAnalysisRunType.DEEP_DIVE;
    }
}
