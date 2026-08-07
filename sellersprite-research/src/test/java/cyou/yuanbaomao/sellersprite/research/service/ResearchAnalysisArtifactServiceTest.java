package cyou.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunType;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.storage.ReportStorage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResearchAnalysisArtifactServiceTest {

    private static final String JOB_ID = "job-final-artifact";
    private static final String USER_ID = "user-final-artifact";
    private static final String RUN_ID = "run-final-artifact";
    private static final String CONVERSATION_ID = "conversation-final-artifact";
    private static final String ARTIFACT_ID = "artifact-final-markdown";
    private static final String MARKDOWN = "# Final market conclusion\n\nEnter this market carefully.";

    @Mock
    private MarketResearchAnalysisRunDao analysisRunDao;

    @Mock
    private MarketResearchArtifactDao artifactDao;

    @Mock
    private ReportStorage reportStorage;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private ResearchSseEventPublisher eventPublisher;

    @TempDir
    private Path temporaryDirectory;

    private ResearchAnalysisArtifactService service;

    @BeforeEach
    void setUp() {
        service = new ResearchAnalysisArtifactService(
                analysisRunDao, artifactDao, reportStorage, idGenerator, eventPublisher);
    }

    @Test
    void shouldPublishFinalMarkdownOnceAndReusePublishedFile() throws Exception {
        MarketResearchAnalysisRun run = finalRun(MARKDOWN);
        AtomicReference<MarketResearchArtifact> stored = new AtomicReference<>();
        Path draft = temporaryDirectory.resolve("final.md");
        when(analysisRunDao.findLatestByJobIdAndUserIdAndRunType(
                        JOB_ID, USER_ID, ResearchAnalysisRunType.FINAL_ANALYSIS.name()))
                .thenReturn(Optional.of(run));
        when(artifactDao.findByAnalysisRunIdAndType(
                        RUN_ID, ResearchConstants.ARTIFACT_TYPE_AI_ANALYSIS_REPORT))
                .thenAnswer(ignored -> Optional.ofNullable(stored.get()));
        when(idGenerator.nextId()).thenReturn(ARTIFACT_ID);
        when(reportStorage.createDraftPath(JOB_ID, ARTIFACT_ID, "md")).thenReturn(draft);
        when(reportStorage.storageKey(draft)).thenReturn(draft.toString());
        when(reportStorage.publish(draft.toString())).thenReturn(draft.toString());
        when(reportStorage.resolve(draft.toString())).thenReturn(draft);
        when(artifactDao.saveOrUpdate(any(MarketResearchArtifact.class))).thenAnswer(invocation -> {
            stored.set(invocation.getArgument(0));
            return true;
        });
        when(artifactDao.updateById(any(MarketResearchArtifact.class))).thenReturn(true);

        MarketResearchArtifact first = service.publishFinalMarkdown(JOB_ID, USER_ID);
        MarketResearchArtifact second = service.publishFinalMarkdown(JOB_ID, USER_ID);

        assertThat(second).isSameAs(first);
        assertThat(first.getArtifactStatus()).isEqualTo("PUBLISHED");
        assertThat(Files.readString(draft)).isEqualTo(MARKDOWN);
        verify(artifactDao, times(1)).saveOrUpdate(any(MarketResearchArtifact.class));
        verify(artifactDao, times(1)).updateById(any(MarketResearchArtifact.class));

        ArgumentCaptor<ResearchEventCommand> events =
                ArgumentCaptor.forClass(ResearchEventCommand.class);
        verify(eventPublisher, times(2)).publish(events.capture());
        assertThat(events.getAllValues())
                .extracting(ResearchEventCommand::getEventType)
                .containsExactly(ResearchEventTypes.REPORT, ResearchEventTypes.DOWNLOAD);
        assertThat(events.getAllValues())
                .allSatisfy(event -> assertThat(
                                ((Map<?, ?>) event.getPayload()).get("stageCode"))
                        .isEqualTo("FINAL_ANALYSIS"));
    }

    @Test
    void shouldRejectPublishingWhenFinalRunHasNoMarkdown() {
        when(analysisRunDao.findLatestByJobIdAndUserIdAndRunType(
                        JOB_ID, USER_ID, ResearchAnalysisRunType.FINAL_ANALYSIS.name()))
                .thenReturn(Optional.of(finalRun(" ")));

        assertThatThrownBy(() -> service.publishFinalMarkdown(JOB_ID, USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("AI分析报告内容不能为空");
    }

    private MarketResearchAnalysisRun finalRun(String markdown) {
        MarketResearchAnalysisRun run = new MarketResearchAnalysisRun();
        run.setAnalysisRunId(RUN_ID);
        run.setJobId(JOB_ID);
        run.setUserId(USER_ID);
        run.setConversationId(CONVERSATION_ID);
        run.setRunType(ResearchAnalysisRunType.FINAL_ANALYSIS.name());
        run.setRunStatus(ResearchAnalysisRunStatus.SUCCEEDED.name());
        run.setFinalSummary(markdown);
        return run;
    }
}
