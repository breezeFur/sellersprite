package cyou.yuanbaomao.sellersprite.ai.research.curation.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

class ResearchStageAgentRouterTest {

    @Test
    void shouldRouteEachBusinessStageToDedicatedAgent() {
        ObjectProvider<ScreeningAnalysisAgent> screeningProvider = provider();
        ObjectProvider<DeepDiveAnalysisAgent> deepDiveProvider = provider();
        ObjectProvider<FinalDecisionAgent> finalProvider = provider();
        ObjectProvider<FollowUpAnswerAgent> followUpProvider = provider();
        ScreeningAnalysisAgent screeningAgent = mock(ScreeningAnalysisAgent.class);
        DeepDiveAnalysisAgent deepDiveAgent = mock(DeepDiveAnalysisAgent.class);
        FinalDecisionAgent finalAgent = mock(FinalDecisionAgent.class);
        FollowUpAnswerAgent followUpAgent = mock(FollowUpAnswerAgent.class);
        when(screeningProvider.getObject()).thenReturn(screeningAgent);
        when(deepDiveProvider.getObject()).thenReturn(deepDiveAgent);
        when(finalProvider.getObject()).thenReturn(finalAgent);
        when(followUpProvider.getObject()).thenReturn(followUpAgent);
        ResearchStageAgentRouter router = new ResearchStageAgentRouter(
                screeningProvider, deepDiveProvider, finalProvider, followUpProvider);

        assertThat(router.getAgent(ResearchAnalysisRunType.SCREENING)).isSameAs(screeningAgent);
        assertThat(router.getAgent(ResearchAnalysisRunType.DEEP_DIVE)).isSameAs(deepDiveAgent);
        assertThat(router.getAgent(ResearchAnalysisRunType.FINAL_ANALYSIS)).isSameAs(finalAgent);
        assertThat(router.getAgent(ResearchAnalysisRunType.INITIAL)).isSameAs(finalAgent);
        assertThat(router.getAgent(ResearchAnalysisRunType.RETRY)).isSameAs(finalAgent);
        assertThat(router.getAgent(ResearchAnalysisRunType.FOLLOW_UP)).isSameAs(followUpAgent);
        assertThat(router.usesFinalAnalysisPath(ResearchAnalysisRunType.SCREENING)).isFalse();
        assertThat(router.usesFinalAnalysisPath(ResearchAnalysisRunType.FINAL_ANALYSIS)).isTrue();
        assertThat(router.usesFinalAnalysisPath(ResearchAnalysisRunType.FOLLOW_UP)).isTrue();
    }

    @SuppressWarnings("unchecked")
    private <T> ObjectProvider<T> provider() {
        return mock(ObjectProvider.class);
    }
}
