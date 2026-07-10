package com.yuanbaomao.sellersprite.api.client;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.yuanbaomao.sellersprite.api.account.controller.AccountController;
import com.yuanbaomao.sellersprite.api.account.service.AccountService;
import com.yuanbaomao.sellersprite.api.asin.controller.AsinController;
import com.yuanbaomao.sellersprite.api.asin.model.dto.AsinDetailRequest;
import com.yuanbaomao.sellersprite.api.asin.service.AsinService;
import com.yuanbaomao.sellersprite.api.keyword.controller.KeywordController;
import com.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchRequest;
import com.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import com.yuanbaomao.sellersprite.api.market.controller.MarketController;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketStatisticsRequest;
import com.yuanbaomao.sellersprite.api.market.service.MarketService;
import com.yuanbaomao.sellersprite.api.product.controller.ProductController;
import com.yuanbaomao.sellersprite.api.product.model.dto.CompetitorLookupRequest;
import com.yuanbaomao.sellersprite.api.product.service.ProductService;
import com.yuanbaomao.sellersprite.api.review.controller.ReviewController;
import com.yuanbaomao.sellersprite.api.review.model.dto.ReviewListRequest;
import com.yuanbaomao.sellersprite.api.review.service.ReviewService;
import com.yuanbaomao.sellersprite.api.tool.controller.ToolController;
import com.yuanbaomao.sellersprite.api.tool.model.dto.OcrRequest;
import com.yuanbaomao.sellersprite.api.tool.service.ToolService;
import com.yuanbaomao.sellersprite.api.trademark.controller.TrademarkController;
import com.yuanbaomao.sellersprite.api.trademark.service.TrademarkService;
import com.yuanbaomao.sellersprite.api.traffic.controller.TrafficController;
import com.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficKeywordRequest;
import com.yuanbaomao.sellersprite.api.traffic.service.TrafficService;

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
                        .content("{\"marketplace\":\"US\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        verify(service).lookupCompetitors(any(CompetitorLookupRequest.class));
    }

    @Test
    void shouldRouteAsinQueryRequest() throws Exception {
        AsinService service = mock(AsinService.class);

        mvc(new AsinController(service)).perform(get("/api/sellersprite/asins/detail")
                        .queryParam("marketplace", "US")
                        .queryParam("asin", "B0TESTASIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("00000"));

        verify(service).getAsinDetail(any(AsinDetailRequest.class));
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
        return MockMvcBuilders.standaloneSetup(controller).build();
    }
}
