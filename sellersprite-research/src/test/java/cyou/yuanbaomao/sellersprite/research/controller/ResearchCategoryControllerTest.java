package cyou.yuanbaomao.sellersprite.research.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductNodeVo;
import cyou.yuanbaomao.sellersprite.research.service.ResearchCategoryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.http.MediaType;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchCategoryCandidateVo;

class ResearchCategoryControllerTest {

    @Test
    void shouldExposeCachedProductNodeQuery() throws Exception {
        ResearchCategoryService service = mock(ResearchCategoryService.class);
        ProductNodeVo node = new ProductNodeVo();
        node.setNodeIdPath("3760911:11062741");
        node.setNodeLabelPath("Beauty & Personal Care:Tools & Accessories");
        when(service.listProductNodes(eq(SellerSpriteMarketplace.US), isNull(), eq("facial"), eq("202607")))
                .thenReturn(List.of(node));
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ResearchCategoryController(service))
                .build();

        mockMvc.perform(get("/api/market-research/categories")
                        .param("marketplace", "US")
                        .param("month", "202607")
                        .param("keyword", "facial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nodeIdPath").value("3760911:11062741"))
                .andExpect(jsonPath("$.data[0].nodeLabelPath")
                        .value("Beauty & Personal Care:Tools & Accessories"));
    }

    @Test
    void shouldExposeResolveByAsinsEndpoint() throws Exception {
        ResearchCategoryService service = mock(ResearchCategoryService.class);
        ResearchCategoryCandidateVo candidate = ResearchCategoryCandidateVo.builder()
                .nodeIdPath("1055398:1063252:1063280")
                .nodeId("1063280")
                .nodeLabelPath("Home & Kitchen:Bedding:Blankets & Throws")
                .nodeLabel("Blankets & Throws")
                .displayName("Blankets & Throws")
                .matchedCount(2)
                .matchedAsins(List.of("B08GHW4TBS", "B08GHW4TBC"))
                .matchedRatio(100.0)
                .build();

        when(service.resolveCategoriesByAsins(any())).thenReturn(List.of(candidate));
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ResearchCategoryController(service))
                .build();

        mockMvc.perform(post("/api/market-research/categories/resolve-by-asins")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "marketplace": "US",
                                  "month": "2026-07",
                                  "asins": ["B08GHW4TBS", "B08GHW4TBC"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nodeIdPath").value("1055398:1063252:1063280"))
                .andExpect(jsonPath("$.data[0].nodeLabel").value("Blankets & Throws"))
                .andExpect(jsonPath("$.data[0].matchedCount").value(2));
    }
}
