package cyou.yuanbaomao.sellersprite.research.constants;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class ResearchConstantsTest {

    @Test
    void shouldExposeOnlyStageSpecificStableArtifactValues() {
        Set<String> artifactTypes = Set.of(
                ResearchConstants.ARTIFACT_TYPE_STAGE1_RAW_WORKBOOK,
                ResearchConstants.ARTIFACT_TYPE_STAGE1_EVIDENCE_WORKBOOK,
                ResearchConstants.ARTIFACT_TYPE_STAGE1_CONCLUSION_REPORT,
                ResearchConstants.ARTIFACT_TYPE_STAGE2_RAW_WORKBOOK,
                ResearchConstants.ARTIFACT_TYPE_STAGE2_EVIDENCE_WORKBOOK,
                ResearchConstants.ARTIFACT_TYPE_STAGE2_CONCLUSION_REPORT,
                ResearchConstants.ARTIFACT_TYPE_AI_ANALYSIS_REPORT);

        assertThat(artifactTypes).containsExactlyInAnyOrder(
                "STAGE1_RAW_WORKBOOK",
                "STAGE1_EVIDENCE_WORKBOOK",
                "STAGE1_CONCLUSION_REPORT",
                "STAGE2_RAW_WORKBOOK",
                "STAGE2_EVIDENCE_WORKBOOK",
                "STAGE2_CONCLUSION_REPORT",
                "AI_ANALYSIS_REPORT");
        assertThat(artifactTypes).doesNotContain("RAW_DATA_WORKBOOK", "EVIDENCE_WORKBOOK");
    }
}
