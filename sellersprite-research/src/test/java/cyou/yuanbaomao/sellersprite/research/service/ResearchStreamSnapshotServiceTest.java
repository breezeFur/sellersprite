package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchNodeExecutionDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchNodeExecution;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ResearchStreamSnapshotServiceTest {

    @Test
    void shouldBuildAuthoritativeJobAndNodeSnapshot() {
        MarketResearchJobDao jobDao = mock(MarketResearchJobDao.class);
        MarketResearchArtifactDao artifactDao = mock(MarketResearchArtifactDao.class);
        MarketResearchAnalysisRunDao analysisRunDao = mock(MarketResearchAnalysisRunDao.class);
        MarketResearchNodeExecutionDao nodeExecutionDao = mock(MarketResearchNodeExecutionDao.class);
        ResearchStreamSnapshotService service = new ResearchStreamSnapshotService(
                jobDao,
                artifactDao,
                analysisRunDao,
                nodeExecutionDao,
                new ResearchInputService(new ObjectMapper()));
        MarketResearchJob job = job();
        MarketResearchArtifact artifact = new MarketResearchArtifact();
        artifact.setArtifactId("artifact-1");
        artifact.setArtifactType(ResearchConstants.ARTIFACT_TYPE_EVIDENCE_WORKBOOK);
        artifact.setFileName("市场调研.xlsx");
        MarketResearchAnalysisRun analysis = new MarketResearchAnalysisRun();
        analysis.setAnalysisRunId("run-1");
        analysis.setConversationId("conversation-1");
        analysis.setRunStatus("RUNNING");
        analysis.setCurrentPhase("sheet");
        analysis.setProgress(46);
        analysis.setAnalysisGoal("判断进入机会");
        MarketResearchNodeExecution node = new MarketResearchNodeExecution();
        node.setExecutionId("execution-1");
        node.setGraphCode("collection");
        node.setNodeCode("collection.validate");
        node.setNodeName("校验任务参数");
        node.setExecutionStatus("SUCCEEDED");
        when(jobDao.findByIdAndUserId("job-1", "user-1")).thenReturn(Optional.of(job));
        when(artifactDao.listAvailableByJobIds(List.of("job-1"))).thenReturn(List.of(artifact));
        when(analysisRunDao.findLatestByJobIdAndUserId("job-1", "user-1"))
                .thenReturn(Optional.of(analysis));
        when(nodeExecutionDao.listByJobId("job-1")).thenReturn(List.of(node));

        var snapshot = service.requireOwnedSnapshot("job-1", "user-1");

        assertThat(snapshot.getJob().getStatus()).isEqualTo("RUNNING");
        assertThat(snapshot.getJob().getCurrentNodeName()).isEqualTo("校验任务参数");
        assertThat(snapshot.getJob().getSeedAsins()).containsExactly("B000000001", "B000000002");
        assertThat(snapshot.getJob().getCollectionConfig().getCollectMarketSalesTrend().getMonthCount())
                .isEqualTo(12);
        assertThat(snapshot.getJob().getAnalysisRunId()).isEqualTo("run-1");
        assertThat(snapshot.getJob().getAnalysisProgress()).isEqualTo(46);
        assertThat(snapshot.getJob().getArtifacts()).singleElement()
                .extracting(item -> item.getArtifactId())
                .isEqualTo("artifact-1");
        assertThat(snapshot.getNodes()).singleElement()
                .satisfies(nodeVo -> {
                    assertThat(nodeVo.getExecutionId()).isEqualTo("execution-1");
                    assertThat(nodeVo.getGraphCode()).isEqualTo("collection");
                });
    }

    private MarketResearchJob job() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId("job-1");
        job.setUserId("user-1");
        job.setReportName("市场调研");
        job.setMarketplace("US");
        job.setNodeIdPath("1:2");
        job.setResearchMonth("2026-07");
        job.setSeedAsins("[\"B000000001\",\"B000000002\"]");
        job.setCollectionConfig("{\"collectMarketSalesTrend\":{\"monthCount\":12}}");
        job.setDataSourceMode("REMOTE");
        job.setWorkflowVersion(ResearchConstants.WORKFLOW_VERSION);
        job.setJobStatus("RUNNING");
        job.setCurrentNode("collection.validate");
        job.setProgress(10);
        job.setAttemptCount(1);
        job.setMaxAttempts(3);
        return job;
    }
}
