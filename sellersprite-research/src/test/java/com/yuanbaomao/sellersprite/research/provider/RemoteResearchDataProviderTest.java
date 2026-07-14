package com.yuanbaomao.sellersprite.research.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.yuanbaomao.sellersprite.api.account.model.vo.VisitsVo;
import com.yuanbaomao.sellersprite.api.account.service.AccountService;
import com.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import com.yuanbaomao.sellersprite.api.common.model.vo.ProductSummaryVo;
import com.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchRequest;
import com.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchItemVo;
import com.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchVo;
import com.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import com.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest;
import com.yuanbaomao.sellersprite.api.product.model.vo.ProductResearchVo;
import com.yuanbaomao.sellersprite.api.product.service.ProductService;
import com.yuanbaomao.sellersprite.api.review.model.dto.ReviewListRequest;
import com.yuanbaomao.sellersprite.api.review.model.vo.ReviewListItemVo;
import com.yuanbaomao.sellersprite.api.review.model.vo.ReviewListVo;
import com.yuanbaomao.sellersprite.api.review.service.ReviewService;
import com.yuanbaomao.sellersprite.research.model.ResearchDataset;
import com.yuanbaomao.sellersprite.research.model.ResearchInput;

import tools.jackson.databind.ObjectMapper;

class RemoteResearchDataProviderTest {

    private AccountService accountService;
    private ProductService productService;
    private KeywordService keywordService;
    private ReviewService reviewService;
    private RemoteResearchDataProvider provider;
    private ResearchInput input;

    @BeforeEach
    void setUp() {
        accountService = mock(AccountService.class);
        productService = mock(ProductService.class);
        keywordService = mock(KeywordService.class);
        reviewService = mock(ReviewService.class);
        provider = new RemoteResearchDataProvider(
                new ObjectMapper(), accountService, productService, keywordService, reviewService);
        input = ResearchInput.builder()
                .jobId("job-remote-001")
                .marketplace("us")
                .keyword("facial cleansing device")
                .seedAsins(List.of(" B0TEST0001 ", "B0TEST0001", "B0TEST0002"))
                .build();
    }

    @Test
    void shouldCheckRemoteQuotaEndpoint() {
        tools.jackson.databind.node.ObjectNode details = new ObjectMapper().createObjectNode();
        details.put("status", "available");
        when(accountService.getVisits()).thenReturn(new VisitsVo(details));

        List<ResearchDataset> datasets = provider.checkQuota(input);

        verify(accountService).getVisits();
        assertThat(datasets).singleElement().satisfies(dataset -> {
            assertThat(dataset.getDatasetCode()).isEqualTo("quota.visits");
            assertThat(dataset.getOperation()).isEqualTo("ACCOUNT_VISITS");
            assertThat(dataset.getRecordCount()).isEqualTo(1);
            assertThat(dataset.getPayload().at("/details/status").asText()).isEqualTo("available");
        });
    }

    @Test
    void shouldDelegateProductCollectionWithTypedRequest() {
        ProductResearchVo response = new ProductResearchVo();
        response.setItems(List.of(new ProductSummaryVo(), new ProductSummaryVo()));
        when(productService.researchProducts(any(ProductResearchRequest.class))).thenReturn(response);

        List<ResearchDataset> datasets = provider.collectMarketAndProducts(input);

        ArgumentCaptor<ProductResearchRequest> requestCaptor = ArgumentCaptor.forClass(ProductResearchRequest.class);
        verify(productService).researchProducts(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getMarketplace()).isEqualTo(SellerSpriteMarketplace.US);
        assertThat(requestCaptor.getValue().getKeyword()).isEqualTo("facial cleansing device");
        assertThat(requestCaptor.getValue().getSize()).isEqualTo(50);
        assertThat(datasets).singleElement().satisfies(dataset -> {
            assertThat(dataset.getDatasetCode()).isEqualTo("products");
            assertThat(dataset.getOperation()).isEqualTo("PRODUCT_RESEARCH");
            assertThat(dataset.getRecordCount()).isEqualTo(2);
            assertThat(dataset.getPayload().get("items").size()).isEqualTo(2);
        });
    }

    @Test
    void shouldDelegateKeywordCollectionWithTypedRequest() {
        KeywordResearchVo response = new KeywordResearchVo();
        response.setItems(List.of(new KeywordResearchItemVo()));
        when(keywordService.researchKeywords(any(KeywordResearchRequest.class))).thenReturn(response);

        List<ResearchDataset> datasets = provider.collectKeywords(input);

        ArgumentCaptor<KeywordResearchRequest> requestCaptor = ArgumentCaptor.forClass(KeywordResearchRequest.class);
        verify(keywordService).researchKeywords(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getMarketplace()).isEqualTo(SellerSpriteMarketplace.US);
        assertThat(requestCaptor.getValue().getKeywords()).isEqualTo("facial cleansing device");
        assertThat(requestCaptor.getValue().getSize()).isEqualTo(15);
        assertThat(datasets).singleElement().satisfies(dataset -> {
            assertThat(dataset.getOperation()).isEqualTo("KEYWORD_RESEARCH");
            assertThat(dataset.getRecordCount()).isEqualTo(1);
        });
    }

    @Test
    void shouldDelegateReviewCollectionOncePerDistinctAsin() {
        ReviewListVo response = new ReviewListVo();
        response.setItems(List.of(new ReviewListItemVo()));
        when(reviewService.listReviews(any(ReviewListRequest.class))).thenReturn(response);

        List<ResearchDataset> datasets = provider.collectReviews(input);

        ArgumentCaptor<ReviewListRequest> requestCaptor = ArgumentCaptor.forClass(ReviewListRequest.class);
        verify(reviewService, org.mockito.Mockito.times(2)).listReviews(requestCaptor.capture());
        assertThat(requestCaptor.getAllValues())
                .extracting(ReviewListRequest::getAsin)
                .containsExactly("B0TEST0001", "B0TEST0002");
        assertThat(requestCaptor.getAllValues())
                .allSatisfy(request -> {
                    assertThat(request.getMarketplace()).isEqualTo(SellerSpriteMarketplace.US);
                    assertThat(request.getSize()).isEqualTo(10);
                });
        assertThat(datasets)
                .extracting(ResearchDataset::getDatasetCode)
                .containsExactly("reviews.B0TEST0001", "reviews.B0TEST0002");
    }

    @Test
    void shouldRejectNullRemoteResponse() {
        when(keywordService.researchKeywords(any(KeywordResearchRequest.class))).thenReturn(null);

        assertThatThrownBy(() -> provider.collectKeywords(input))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("KEYWORD_RESEARCH");
    }
}
