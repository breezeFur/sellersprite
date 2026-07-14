package com.yuanbaomao.sellersprite.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.sellersprite.api.account.model.vo.VisitsVo;
import com.yuanbaomao.sellersprite.api.account.service.AccountService;
import com.yuanbaomao.sellersprite.api.asin.model.dto.AsinDetailRequest;
import com.yuanbaomao.sellersprite.api.asin.model.vo.AsinDetailVo;
import com.yuanbaomao.sellersprite.api.asin.service.AsinService;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteApiException;
import com.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchRequest;
import com.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchVo;
import com.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketResearchRequest;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketResearchVo;
import com.yuanbaomao.sellersprite.api.market.service.MarketService;
import com.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest;
import com.yuanbaomao.sellersprite.api.product.model.vo.ProductResearchVo;
import com.yuanbaomao.sellersprite.api.product.service.ProductService;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SellerSpriteAiToolsTest {

    @Mock
    private AccountService accountService;

    @Mock
    private AsinService asinService;

    @Mock
    private ProductService productService;

    @Mock
    private KeywordService keywordService;

    @Mock
    private MarketService marketService;

    private SellerSpriteAiTools tools;

    @BeforeEach
    void setUp() {
        tools = new SellerSpriteAiTools(accountService, asinService, productService, keywordService, marketService);
    }

    @Test
    void shouldDelegateAccountVisitsQuery() {
        VisitsVo expected = new VisitsVo();
        when(accountService.getVisits()).thenReturn(expected);

        VisitsVo result = tools.getAccountVisits();

        assertThat(result).isSameAs(expected);
        verify(accountService).getVisits();
    }

    @Test
    void shouldDelegateAsinDetailQuery() {
        AsinDetailRequest request = new AsinDetailRequest();
        AsinDetailVo expected = new AsinDetailVo();
        when(asinService.getAsinDetail(request)).thenReturn(expected);

        AsinDetailVo result = tools.getAsinDetail(request);

        assertThat(result).isSameAs(expected);
        verify(asinService).getAsinDetail(request);
    }

    @Test
    void shouldDelegateProductResearchQuery() {
        ProductResearchRequest request = new ProductResearchRequest();
        ProductResearchVo expected = new ProductResearchVo();
        when(productService.researchProducts(request)).thenReturn(expected);

        ProductResearchVo result = tools.researchProducts(request);

        assertThat(result).isSameAs(expected);
        verify(productService).researchProducts(request);
    }

    @Test
    void shouldDelegateKeywordResearchQuery() {
        KeywordResearchRequest request = new KeywordResearchRequest();
        KeywordResearchVo expected = new KeywordResearchVo();
        when(keywordService.researchKeywords(request)).thenReturn(expected);

        KeywordResearchVo result = tools.researchKeywords(request);

        assertThat(result).isSameAs(expected);
        verify(keywordService).researchKeywords(request);
    }

    @Test
    void shouldDelegateMarketResearchQuery() {
        MarketResearchRequest request = new MarketResearchRequest();
        MarketResearchVo expected = new MarketResearchVo();
        when(marketService.researchMarkets(request)).thenReturn(expected);

        MarketResearchVo result = tools.researchMarkets(request);

        assertThat(result).isSameAs(expected);
        verify(marketService).researchMarkets(request);
    }

    @Test
    void shouldPropagateSellerSpriteFailureWithoutFallbackResult() {
        AsinDetailRequest request = new AsinDetailRequest();
        SellerSpriteApiException expected = new SellerSpriteApiException(
                ResultCode.SELLERSPRITE_QUOTA_EXHAUSTED, null, "request-id", null);
        when(asinService.getAsinDetail(request)).thenThrow(expected);

        assertThatThrownBy(() -> tools.getAsinDetail(request)).isSameAs(expected);
        verify(asinService).getAsinDetail(request);
    }
}
