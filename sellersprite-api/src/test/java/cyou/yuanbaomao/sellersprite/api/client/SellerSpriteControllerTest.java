package cyou.yuanbaomao.sellersprite.api.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cyou.yuanbaomao.sellersprite.api.account.controller.AccountController;
import cyou.yuanbaomao.sellersprite.api.account.service.AccountService;
import cyou.yuanbaomao.sellersprite.api.asin.controller.AsinController;
import cyou.yuanbaomao.sellersprite.api.asin.service.AsinService;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.keyword.controller.KeywordController;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import cyou.yuanbaomao.sellersprite.api.market.controller.MarketController;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketStatisticsRequest;
import cyou.yuanbaomao.sellersprite.api.market.service.MarketService;
import cyou.yuanbaomao.sellersprite.api.product.controller.ProductController;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.CompetitorLookupRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest;
import cyou.yuanbaomao.sellersprite.api.product.service.ProductService;
import cyou.yuanbaomao.sellersprite.api.review.controller.ReviewController;
import cyou.yuanbaomao.sellersprite.api.review.model.dto.ReviewListRequest;
import cyou.yuanbaomao.sellersprite.api.review.service.ReviewService;
import cyou.yuanbaomao.sellersprite.api.tool.controller.ToolController;
import cyou.yuanbaomao.sellersprite.api.tool.model.dto.OcrRequest;
import cyou.yuanbaomao.sellersprite.api.tool.service.ToolService;
import cyou.yuanbaomao.sellersprite.api.trademark.controller.TrademarkController;
import cyou.yuanbaomao.sellersprite.api.trademark.service.TrademarkService;
import cyou.yuanbaomao.sellersprite.api.traffic.controller.TrafficController;
import cyou.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficKeywordRequest;
import cyou.yuanbaomao.sellersprite.api.traffic.service.TrafficService;

class SellerSpriteControllerTest {

    @Test
    void shouldRouteAccountVisits() throws Exception {
        AccountService service = mock(AccountService.class);

        mvc(new AccountController(service)).perform(get("/api/sellersprite/account/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        verify(service).getVisits();
    }

    @Test
    void shouldRouteProductJsonRequest() throws Exception {
        ProductService service = mock(ProductService.class);

        mvc(new ProductController(service)).perform(post("/api/sellersprite/products/competitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marketplace\":\"MARKET_US\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        verify(service).lookupCompetitors(any(CompetitorLookupRequest.class));
    }

    @Test
    void shouldBindProductResearchDictionaryLabelsBeforeClientResolution() throws Exception {
        ProductService service = mock(ProductService.class);

        mvc(new ProductController(service)).perform(post("/api/sellersprite/products/research")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "marketplace": "MARKET_US",
                                  "availableMonth": "LISTING_DATE_3",
                                  "dimensionType": "PRODUCT_SIZE_US_ST_SS,PRODUCT_SIZE_US_LS",
                                  "order": {
                                    "field": "PRODUCT_SORT_FIELD_TOTAL_UNITS",
                                    "desc": true
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        ArgumentCaptor<ProductResearchRequest> requestCaptor = ArgumentCaptor.forClass(ProductResearchRequest.class);
        verify(service).researchProducts(requestCaptor.capture());
        ProductResearchRequest request = requestCaptor.getValue();
        assertThat(request.getAvailableMonth()).isEqualTo("LISTING_DATE_3");
        assertThat(request.getDimensionType()).isEqualTo("PRODUCT_SIZE_US_ST_SS,PRODUCT_SIZE_US_LS");
        assertThat(request.getOrder().getField()).isEqualTo("PRODUCT_SORT_FIELD_TOTAL_UNITS");
        assertThat(request.getOrder().getDesc()).isTrue();
    }

    @Test
    void shouldRouteAsinQueryRequest() throws Exception {
        AsinService service = mock(AsinService.class);

        mvc(new AsinController(service)).perform(get("/api/sellersprite/asins/detail")
                        .queryParam("marketplace", "MARKET_US")
                        .queryParam("asin", "B0TESTASIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        verify(service).getAsinDetail(SellerSpriteMarketplace.US, "B0TESTASIN");
    }

    @Test
    void shouldRouteKeywordJsonRequest() throws Exception {
        KeywordService service = mock(KeywordService.class);

        mvc(new KeywordController(service)).perform(post("/api/sellersprite/keywords/research")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marketplace\":\"US\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        verify(service).researchKeywords(any(KeywordResearchRequest.class));
    }

    @Test
    void shouldRouteTrafficJsonRequest() throws Exception {
        TrafficService service = mock(TrafficService.class);

        mvc(new TrafficController(service)).perform(post("/api/sellersprite/traffic/keywords/reverse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marketplace\":\"US\",\"asin\":\"B0TESTASIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        verify(service).reverseKeywords(any(TrafficKeywordRequest.class));
    }

    @Test
    void shouldRouteMarketJsonRequest() throws Exception {
        MarketService service = mock(MarketService.class);

        mvc(new MarketController(service)).perform(post("/api/sellersprite/markets/statistics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marketplace\":\"US\",\"nodeIdPath\":\"172282\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        verify(service).getMarketStatistics(any(MarketStatisticsRequest.class));
    }

    @Test
    void shouldRouteReviewJsonRequest() throws Exception {
        ReviewService service = mock(ReviewService.class);

        mvc(new ReviewController(service)).perform(post("/api/sellersprite/reviews/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"marketplace\":\"US\",\"asin\":\"B0TESTASIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        verify(service).listReviews(any(ReviewListRequest.class));
    }

    @Test
    void shouldRouteTrademarkGetRequest() throws Exception {
        TrademarkService service = mock(TrademarkService.class);

        mvc(new TrademarkController(service)).perform(get("/api/sellersprite/trademarks/range"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        verify(service).getBrandRange();
    }

    @Test
    void shouldRouteToolMultipartRequest() throws Exception {
        ToolService service = mock(ToolService.class);

        mvc(new ToolController(service)).perform(multipart("/api/sellersprite/tools/ocr")
                        .param("type", "0")
                        .param("fn", "CHINESE")
                        .param("url", "https://example.com/image.png"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        verify(service).recognizeImageText(any(OcrRequest.class));
    }

    @Test
    void shouldRejectOcrRequestWithoutMatchingImageSource() throws Exception {
        ToolService service = mock(ToolService.class);

        mvc(new ToolController(service)).perform(multipart("/api/sellersprite/tools/ocr")
                        .param("type", "0")
                        .param("fn", "CHINESE"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    private MockMvc mvc(Object controller) {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        new SellerSpriteApiConfig().addFormatters(conversionService);
        return MockMvcBuilders.standaloneSetup(controller)
                .setConversionService(conversionService)
                .build();
    }
}
