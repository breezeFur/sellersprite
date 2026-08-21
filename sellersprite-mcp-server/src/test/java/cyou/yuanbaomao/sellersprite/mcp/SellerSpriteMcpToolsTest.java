package cyou.yuanbaomao.sellersprite.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.api.account.service.AccountService;
import cyou.yuanbaomao.sellersprite.api.asin.service.AsinService;
import cyou.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import cyou.yuanbaomao.sellersprite.api.market.service.MarketService;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductResearchVo;
import cyou.yuanbaomao.sellersprite.api.product.service.ProductService;
import cyou.yuanbaomao.sellersprite.api.review.service.ReviewService;
import cyou.yuanbaomao.sellersprite.api.tool.service.ToolService;
import cyou.yuanbaomao.sellersprite.api.traffic.service.TrafficService;
import cyou.yuanbaomao.sellersprite.api.trademark.service.TrademarkService;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;

@ExtendWith(MockitoExtension.class)
class SellerSpriteMcpToolsTest {

    @Mock
    private AccountService accountService;

    @Mock
    private ProductService productService;

    @Mock
    private AsinService asinService;

    @Mock
    private KeywordService keywordService;

    @Mock
    private TrafficService trafficService;

    @Mock
    private MarketService marketService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private TrademarkService trademarkService;

    @Mock
    private ToolService toolService;

    private ToolCallback[] callbacks;
    private SellerSpriteMcpTools tools;

    @BeforeEach
    void setUp() {
        tools = new SellerSpriteMcpTools(
                accountService, productService, asinService, keywordService, trafficService,
                marketService, reviewService, trademarkService, toolService);
        callbacks = MethodToolCallbackProvider.builder()
                .toolObjects(tools)
                .build()
                .getToolCallbacks();
    }

    @Test
    void shouldDiscoverAllCurrentSellerSpriteOperations() {
        assertThat(callbacks)
                .hasSize(SellerSpriteMcpToolNames.all().size())
                .extracting(callback -> callback.getToolDefinition().name())
                .containsExactlyInAnyOrderElementsOf(SellerSpriteMcpToolNames.all());
        assertThat(callbacks)
                .allSatisfy(callback -> assertThat(callback.getToolDefinition().description()).isNotBlank());
    }

    @Test
    void shouldGenerateTypedProductResearchSchema() {
        String inputSchema = callback(SellerSpriteMcpToolNames.RESEARCH_PRODUCTS)
                .getToolDefinition()
                .inputSchema();

        assertThat(inputSchema)
                .contains("request", "marketplace", "page", "size")
                .doesNotContain("secretKey", "operation", "header");
    }

    @Test
    void shouldDelegateProductResearchTool() {
        ProductResearchRequest request = new ProductResearchRequest();
        ProductResearchVo expected = new ProductResearchVo();
        when(productService.researchProducts(request)).thenReturn(expected);

        ProductResearchVo result = tools.researchProducts(request);

        assertThat(result).isSameAs(expected);
        verify(productService).researchProducts(request);
    }

    @Test
    void shouldInvokeProductResearchFromMcpJson() {
        ProductResearchVo expected = new ProductResearchVo();
        when(productService.researchProducts(org.mockito.ArgumentMatchers.any(ProductResearchRequest.class)))
                .thenReturn(expected);

        String result = callback(SellerSpriteMcpToolNames.RESEARCH_PRODUCTS)
                .call("{\"request\":{\"marketplace\":\"US\",\"page\":1,\"size\":20}}");

        verify(productService).researchProducts(org.mockito.ArgumentMatchers.any(ProductResearchRequest.class));
        assertThat(result).isNotBlank();
    }

    private ToolCallback callback(String name) {
        return Arrays.stream(callbacks)
                .filter(callback -> name.equals(callback.getToolDefinition().name()))
                .findFirst()
                .orElseThrow();
    }
}
