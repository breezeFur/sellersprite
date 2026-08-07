package cyou.yuanbaomao.sellersprite.research.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ResearchPhaseTest {

    @Test
    void shouldDefineThreeNamespacedSubgraphsInOrder() {
        List<ResearchPhase> phases = List.of(ResearchPhase.values());

        assertThat(phases).containsExactly(
                ResearchPhase.VALIDATE,
                ResearchPhase.CHECK_QUOTA,
                ResearchPhase.COLLECT_PRODUCTS,
                ResearchPhase.COLLECT_MARKET_SALES_TREND,
                ResearchPhase.COLLECT_KEYWORD_DEMAND_TREND,
                ResearchPhase.COLLECT_SEGMENT_OPPORTUNITY,
                ResearchPhase.COLLECT_REVIEWS,
                ResearchPhase.COLLECT_ASIN_INTELLIGENCE,
                ResearchPhase.COLLECT_KEYWORD_INTELLIGENCE,
                ResearchPhase.VALIDATE_RAW_DATA,
                ResearchPhase.RENDER_RAW_WORKBOOK,
                ResearchPhase.PUBLISH_RAW_WORKBOOK,
                ResearchPhase.PREPARE_US_EVIDENCE,
                ResearchPhase.PREPARE_SALES_TREND_EVIDENCE,
                ResearchPhase.PREPARE_DEMAND_TREND_EVIDENCE,
                ResearchPhase.PREPARE_SEGMENT_MARKET_EVIDENCE,
                ResearchPhase.PREPARE_SEGMENT_RETURN_EVIDENCE,
                ResearchPhase.PREPARE_BRAND_EVIDENCE,
                ResearchPhase.PREPARE_CONCENTRATION_EVIDENCE,
                ResearchPhase.PREPARE_REVIEW_EVIDENCE,
                ResearchPhase.PREPARE_VOC_EVIDENCE,
                ResearchPhase.PREPARE_KEYWORD_EVIDENCE,
                ResearchPhase.PREPARE_ASIN_SALES_TREND_EVIDENCE,
                ResearchPhase.PREPARE_ASIN_OPERATION_TREND_EVIDENCE,
                ResearchPhase.VALIDATE_EVIDENCE,
                ResearchPhase.RENDER_EVIDENCE_WORKBOOK,
                ResearchPhase.PUBLISH_EVIDENCE_WORKBOOK,
                ResearchPhase.RUN_INITIAL_ANALYSIS);
        assertThat(phases).hasSize(28);
        assertThat(ResearchPhase.phases(ResearchGraphCode.COLLECTION)).hasSize(12);
        assertThat(ResearchPhase.phases(ResearchGraphCode.EVIDENCE)).hasSize(15);
        assertThat(ResearchPhase.phases(ResearchGraphCode.REPORT))
                .containsExactly(ResearchPhase.RUN_INITIAL_ANALYSIS);
    }

    @Test
    void shouldAdvanceProgressStrictlyAndContinueFromPreviousPhase() {
        List<ResearchPhase> phases = List.of(ResearchPhase.values());

        assertThat(phases.getFirst().getStartProgress()).isZero();
        assertThat(phases.getLast().getProgress()).isEqualTo(100);
        for (int index = 1; index < phases.size(); index++) {
            assertThat(phases.get(index).getProgress())
                    .isGreaterThan(phases.get(index - 1).getProgress());
            assertThat(phases.get(index).getStartProgress())
                    .isEqualTo(phases.get(index - 1).getProgress());
        }
    }

    @Test
    void shouldNotContainLegacyAiAnalysisOrSummaryPhases() {
        assertThat(ResearchPhase.values())
                .extracting(Enum::name)
                .doesNotContain("ANALYZE_DIMENSIONS", "SUMMARIZE_ANALYSIS")
                .noneMatch(name -> name.contains("ANALYZE") || name.contains("SUMMARIZE"));
        assertThat(ResearchPhase.values())
                .extracting(ResearchPhase::getNodeCode)
                .noneMatch(nodeCode -> nodeCode.toLowerCase(java.util.Locale.ROOT).contains("analyz")
                        || nodeCode.toLowerCase(java.util.Locale.ROOT).contains("summar"));
    }
}
