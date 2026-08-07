package cyou.yuanbaomao.sellersprite.ai.research.runtime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunType;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.model.ResearchAnalysisLease;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchAnalysisStateService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultResearchAnalysisStageAdapterTest {

    private static final String JOB_ID = "job-1";
    private static final String PARENT_EXECUTION_TOKEN = "parent-token";

    @Mock
    private ResearchAnalysisService analysisService;

    @Mock
    private ResearchAnalysisStateService stateService;

    @Mock
    private ResearchAnalysisExecutor executor;

    private DefaultResearchAnalysisStageAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DefaultResearchAnalysisStageAdapter(
                analysisService, stateService, executor);
    }

    @Test
    void shouldEnsureClaimAndSynchronouslyExecuteScreeningStage() {
        ResearchAnalysisLease lease = lease(ResearchStageCode.SCREENING);
        when(analysisService.ensureStageRun(JOB_ID, ResearchStageCode.SCREENING))
                .thenReturn(new MarketResearchAnalysisRun());
        when(stateService.tryStartStage(
                JOB_ID, PARENT_EXECUTION_TOKEN, ResearchStageCode.SCREENING))
                .thenReturn(Optional.of(lease));
        when(stateService.isStageSucceeded(JOB_ID, ResearchStageCode.SCREENING))
                .thenReturn(true);

        adapter.runStage(JOB_ID, PARENT_EXECUTION_TOKEN, ResearchStageCode.SCREENING);

        InOrder order = inOrder(analysisService, stateService, executor);
        order.verify(analysisService).ensureStageRun(JOB_ID, ResearchStageCode.SCREENING);
        order.verify(stateService).tryStartStage(
                JOB_ID, PARENT_EXECUTION_TOKEN, ResearchStageCode.SCREENING);
        order.verify(executor).executeSynchronously(lease);
        order.verify(stateService).isStageSucceeded(JOB_ID, ResearchStageCode.SCREENING);
    }

    @Test
    void shouldTreatLegacyInitialPortAsFinalAnalysisStage() {
        ResearchAnalysisLease lease = lease(ResearchStageCode.FINAL_ANALYSIS);
        when(analysisService.ensureStageRun(JOB_ID, ResearchStageCode.FINAL_ANALYSIS))
                .thenReturn(new MarketResearchAnalysisRun());
        when(stateService.tryStartStage(
                JOB_ID, PARENT_EXECUTION_TOKEN, ResearchStageCode.FINAL_ANALYSIS))
                .thenReturn(Optional.of(lease));
        when(stateService.isStageSucceeded(JOB_ID, ResearchStageCode.FINAL_ANALYSIS))
                .thenReturn(true);

        adapter.runInitial(JOB_ID, PARENT_EXECUTION_TOKEN);

        verify(analysisService).ensureStageRun(JOB_ID, ResearchStageCode.FINAL_ANALYSIS);
        verify(stateService).tryStartStage(
                JOB_ID, PARENT_EXECUTION_TOKEN, ResearchStageCode.FINAL_ANALYSIS);
        verify(executor).executeSynchronously(lease);
    }

    @Test
    void shouldReturnWithoutExecutingWhenStageAlreadySucceeded() {
        when(analysisService.ensureStageRun(JOB_ID, ResearchStageCode.DEEP_DIVE))
                .thenReturn(new MarketResearchAnalysisRun());
        when(stateService.tryStartStage(
                JOB_ID, PARENT_EXECUTION_TOKEN, ResearchStageCode.DEEP_DIVE))
                .thenReturn(Optional.empty());
        when(stateService.isStageSucceeded(JOB_ID, ResearchStageCode.DEEP_DIVE))
                .thenReturn(true);

        adapter.runStage(JOB_ID, PARENT_EXECUTION_TOKEN, ResearchStageCode.DEEP_DIVE);

        verify(executor, never()).executeSynchronously(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldFailParentGraphWhenStageDoesNotReachSucceededState() {
        ResearchAnalysisLease lease = lease(ResearchStageCode.DEEP_DIVE);
        when(analysisService.ensureStageRun(JOB_ID, ResearchStageCode.DEEP_DIVE))
                .thenReturn(new MarketResearchAnalysisRun());
        when(stateService.tryStartStage(
                JOB_ID, PARENT_EXECUTION_TOKEN, ResearchStageCode.DEEP_DIVE))
                .thenReturn(Optional.of(lease));
        when(stateService.isStageSucceeded(JOB_ID, ResearchStageCode.DEEP_DIVE))
                .thenReturn(false);

        assertThatThrownBy(() -> adapter.runStage(
                JOB_ID, PARENT_EXECUTION_TOKEN, ResearchStageCode.DEEP_DIVE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("阶段分析未成功完成")
                .hasMessageContaining(ResearchStageCode.DEEP_DIVE.name());
    }

    @Test
    void shouldKeepDataGraphSuccessfulWhenStageAnalysisFails() {
        ResearchAnalysisLease lease = lease(ResearchStageCode.SCREENING);
        when(analysisService.ensureStageRun(JOB_ID, ResearchStageCode.SCREENING))
                .thenReturn(new MarketResearchAnalysisRun());
        when(stateService.tryStartStage(
                JOB_ID, PARENT_EXECUTION_TOKEN, ResearchStageCode.SCREENING))
                .thenReturn(Optional.of(lease));
        when(stateService.isStageFailed(JOB_ID, ResearchStageCode.SCREENING))
                .thenReturn(true);
        doThrow(new IllegalStateException("阶段分析失败"))
                .when(executor).executeSynchronously(lease);

        adapter.runStage(JOB_ID, PARENT_EXECUTION_TOKEN, ResearchStageCode.SCREENING);

        verify(stateService).isStageFailed(JOB_ID, ResearchStageCode.SCREENING);
    }

    private ResearchAnalysisLease lease(ResearchStageCode stage) {
        return new ResearchAnalysisLease(
                "run-" + stage.name(),
                JOB_ID,
                "user-1",
                "conversation-1",
                ResearchAnalysisRunType.valueOf(stage.name()).name(),
                "分析目标",
                "research-graph:" + JOB_ID,
                PARENT_EXECUTION_TOKEN,
                1,
                3);
    }
}
