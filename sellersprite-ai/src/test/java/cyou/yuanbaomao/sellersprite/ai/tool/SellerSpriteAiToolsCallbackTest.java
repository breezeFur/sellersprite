package cyou.yuanbaomao.sellersprite.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.api.account.service.AccountService;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinDetailVo;
import cyou.yuanbaomao.sellersprite.api.asin.service.AsinService;
import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import cyou.yuanbaomao.sellersprite.api.market.service.MarketService;
import cyou.yuanbaomao.sellersprite.api.product.service.ProductService;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;

@ExtendWith(MockitoExtension.class)
class SellerSpriteAiToolsCallbackTest {

    private static final String ASIN = "B08GHW4TBS";

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

    private ToolCallback[] callbacks;

    @BeforeEach
    void setUp() {
        SellerSpriteAiTools tools = new SellerSpriteAiTools(
                accountService, asinService, productService, keywordService, marketService);
        callbacks = MethodToolCallbackProvider.builder()
                .toolObjects(tools)
                .build()
                .getToolCallbacks();
    }

    @Test
    void shouldDiscoverFiveAnnotatedToolsWithStableNames() {
        assertThat(callbacks)
                .hasSize(5)
                .extracting(callback -> callback.getToolDefinition().name())
                .containsExactlyInAnyOrder(
                        SellerSpriteAiTools.ACCOUNT_VISITS_TOOL_NAME,
                        SellerSpriteAiTools.ASIN_DETAIL_TOOL_NAME,
                        SellerSpriteAiTools.PRODUCT_RESEARCH_TOOL_NAME,
                        SellerSpriteAiTools.KEYWORD_RESEARCH_TOOL_NAME,
                        SellerSpriteAiTools.MARKET_RESEARCH_TOOL_NAME);
        assertThat(callbacks)
                .allSatisfy(callback -> assertThat(callback.getToolDefinition().description()).isNotBlank());
    }

    @Test
    void shouldGenerateStronglyTypedAsinInputSchema() {
        String inputSchema = callback(SellerSpriteAiTools.ASIN_DETAIL_TOOL_NAME)
                .getToolDefinition()
                .inputSchema();

        assertThat(inputSchema)
                .contains("request", "marketplace", "asin")
                .doesNotContain("url", "header", "secretKey", "operation");
    }

    @Test
    void shouldInvokeAnnotatedToolFromJsonAndSerializeDomainResult() {
        AsinDetailVo expected = new AsinDetailVo();
        expected.setAsin(ASIN);
        when(asinService.getAsinDetail(SellerSpriteMarketplace.US, ASIN)).thenReturn(expected);

        String result = callback(SellerSpriteAiTools.ASIN_DETAIL_TOOL_NAME)
                .call("{\"request\":{\"marketplace\":\"US\",\"asin\":\"" + ASIN + "\"}}");

        verify(asinService).getAsinDetail(SellerSpriteMarketplace.US, ASIN);
        assertThat(result).contains("\"asin\":\"" + ASIN + "\"");
    }

    private ToolCallback callback(String name) {
        return Arrays.stream(callbacks)
                .filter(callback -> name.equals(callback.getToolDefinition().name()))
                .findFirst()
                .orElseThrow();
    }
}
