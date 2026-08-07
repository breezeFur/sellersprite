package cyou.yuanbaomao.sellersprite.ai.research.curation.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.model.ResearchRawDataAccessScope;
import cyou.yuanbaomao.sellersprite.research.service.ResearchRawDataInspectionService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResearchRawDataToolsTest {

    @Mock
    private ResearchRawDataInspectionService inspectionService;

    @Test
    void shouldResolveJobAndStageFromServerSideRunContext() {
        AmazonSelectionToolContext toolContext = new AmazonSelectionToolContext();
        ResearchRawDataTools tools = new ResearchRawDataTools(toolContext, inspectionService);
        toolContext.start(
                "run-1",
                "conversation-1",
                new ProductWorkbook(),
                new ResearchRawDataAccessScope("job-1", ResearchStageCode.DEEP_DIVE));
        when(inspectionService.queryFields(
                "job-1",
                ResearchStageCode.DEEP_DIVE,
                "reviews.B000TEST",
                List.of("items[].content", "items[].star"),
                5))
                .thenReturn("bounded-result");

        String result = tools.queryResearchRawDatasetFields(
                "run-1",
                "reviews.B000TEST",
                "items[].content，items[].star",
                5);

        assertThat(result).isEqualTo("bounded-result");
        verify(inspectionService).queryFields(
                "job-1",
                ResearchStageCode.DEEP_DIVE,
                "reviews.B000TEST",
                List.of("items[].content", "items[].star"),
                5);
    }

    @Test
    void shouldRejectRunWithoutRawDataScope() {
        AmazonSelectionToolContext toolContext = new AmazonSelectionToolContext();
        ResearchRawDataTools tools = new ResearchRawDataTools(toolContext, inspectionService);
        toolContext.start("run-1", new ProductWorkbook());

        assertThatThrownBy(() -> tools.inspectResearchRawDatasetCatalog("run-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("未绑定原始数据访问范围");
    }
}
