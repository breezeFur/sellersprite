package cyou.yuanbaomao.sellersprite.research.graph.runtime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.model.ResearchExecutionLease;
import cyou.yuanbaomao.sellersprite.research.service.ResearchJobStateService;
import cyou.yuanbaomao.sellersprite.research.service.ResearchMarketTrendCacheWarmup;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResearchGraphDispatcherTest {

    private static final String EXECUTION_TOKEN = "execution-token-test";

    @Mock
    private ResearchJobStateService jobStateService;

    @Mock
    private ResearchGraphExecutor graphExecutor;

    @Mock
    private ResearchMarketTrendCacheWarmup marketTrendCacheWarmup;

    @Test
    void shouldSubmitOnlyJobsClaimedByThisDispatcher() {
        ResearchExecutionLease lease = new ResearchExecutionLease(
                "job-1", ResearchConstants.WORKFLOW_VERSION, "worker", EXECUTION_TOKEN, 1);
        when(jobStateService.listDispatchCandidates()).thenReturn(List.of("job-1", "job-2"));
        when(marketTrendCacheWarmup.isReadyForDispatch()).thenReturn(true);
        when(jobStateService.tryClaim(org.mockito.ArgumentMatchers.eq("job-1"), anyString()))
                .thenReturn(Optional.of(lease));
        when(jobStateService.tryClaim(org.mockito.ArgumentMatchers.eq("job-2"), anyString()))
                .thenReturn(Optional.empty());

        new ResearchGraphDispatcher(jobStateService, graphExecutor, marketTrendCacheWarmup).poll();

        verify(graphExecutor).submit(lease);
        verify(jobStateService, never()).handleExecutionFailure(
                org.mockito.ArgumentMatchers.eq(lease), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldMoveClaimedJobToFailureHandlingWhenExecutorRejectsSubmission() {
        ResearchExecutionLease lease = new ResearchExecutionLease(
                "job-1", ResearchConstants.WORKFLOW_VERSION, "worker", EXECUTION_TOKEN, 1);
        RuntimeException rejection = new IllegalStateException("queue full");
        when(jobStateService.tryClaim(org.mockito.ArgumentMatchers.eq("job-1"), anyString()))
                .thenReturn(Optional.of(lease));
        when(marketTrendCacheWarmup.isReadyForDispatch()).thenReturn(true);
        org.mockito.Mockito.doThrow(rejection).when(graphExecutor).submit(lease);

        new ResearchGraphDispatcher(jobStateService, graphExecutor, marketTrendCacheWarmup)
                .dispatchNow("job-1");

        verify(jobStateService).handleExecutionFailure(lease, rejection);
    }

    @Test
    void shouldNotClaimJobsBeforeMarketTrendWarmupCompletes() {
        when(marketTrendCacheWarmup.isReadyForDispatch()).thenReturn(false);

        new ResearchGraphDispatcher(jobStateService, graphExecutor, marketTrendCacheWarmup).poll();

        verify(jobStateService, never()).listDispatchCandidates();
        verify(graphExecutor, never()).submit(org.mockito.ArgumentMatchers.any());
    }
}
