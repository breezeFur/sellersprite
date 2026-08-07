package cyou.yuanbaomao.sellersprite.ai.tool;

import cyou.yuanbaomao.sellersprite.api.account.model.vo.VisitsVo;
import cyou.yuanbaomao.sellersprite.api.account.service.AccountService;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinDetailRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinDetailVo;
import cyou.yuanbaomao.sellersprite.api.asin.service.AsinService;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchVo;
import cyou.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketResearchRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.vo.MarketResearchVo;
import cyou.yuanbaomao.sellersprite.api.market.service.MarketService;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.vo.ProductResearchVo;
import cyou.yuanbaomao.sellersprite.api.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * 面向大模型的 SellerSprite 只读工具适配层。
 *
 * <p>工具仅委派给既有强类型业务 Service，不接受外部地址、认证头、密钥或任意操作名。</p>
 */
@Component
@Validated
@RequiredArgsConstructor
public class SellerSpriteAiTools {

    static final String ACCOUNT_VISITS_TOOL_NAME = "sellersprite_get_account_visits";
    static final String ASIN_DETAIL_TOOL_NAME = "sellersprite_get_asin_detail";
    static final String PRODUCT_RESEARCH_TOOL_NAME = "sellersprite_research_products";
    static final String KEYWORD_RESEARCH_TOOL_NAME = "sellersprite_research_keywords";
    static final String MARKET_RESEARCH_TOOL_NAME = "sellersprite_research_markets";

    private final AccountService accountService;
    private final AsinService asinService;
    private final ProductService productService;
    private final KeywordService keywordService;
    private final MarketService marketService;

    /**
     * 查询当前 SellerSprite 账户各业务模块的剩余调用次数。
     */
    @Tool(name = ACCOUNT_VISITS_TOOL_NAME,
            description = "查询当前 SellerSprite 账户各业务模块的可用调用次数；仅用于判断查询配额，不返回密钥或认证信息")
    public VisitsVo getAccountVisits() {
        return accountService.getVisits();
    }

    /**
     * 查询指定站点的 ASIN 商品详情。
     */
    @Tool(name = ASIN_DETAIL_TOOL_NAME,
            description = "查询指定 Amazon 站点的 ASIN 商品详情，包括价格、销量、排名、评分和类目等只读数据")
    public AsinDetailVo getAsinDetail(
            @Valid @ToolParam(description = "ASIN 详情查询条件，必须提供 marketplace 和 asin")
            AsinDetailRequest request) {
        return asinService.getAsinDetail(request);
    }

    /**
     * 按筛选条件查询 SellerSprite 选产品数据。
     */
    @Tool(name = PRODUCT_RESEARCH_TOOL_NAME,
            description = "按 Amazon 站点、类目和销量等条件查询 SellerSprite 选产品结果；适合分析候选商品和竞争情况")
    public ProductResearchVo researchProducts(
            @Valid @ToolParam(description = "选产品强类型筛选条件，必须提供 marketplace，分页和筛选字段遵循 SellerSprite 契约")
            ProductResearchRequest request) {
        return productService.researchProducts(request);
    }

    /**
     * 按筛选条件查询 SellerSprite 关键词选品数据。
     */
    @Tool(name = KEYWORD_RESEARCH_TOOL_NAME,
            description = "按 Amazon 站点、关键词搜索量、竞争度和转化等条件查询 SellerSprite 关键词选品结果")
    public KeywordResearchVo researchKeywords(
            @Valid @ToolParam(description = "关键词选品强类型筛选条件，必须提供 marketplace，分页和筛选字段遵循 SellerSprite 契约")
            KeywordResearchRequest request) {
        return keywordService.researchKeywords(request);
    }

    /**
     * 按筛选条件查询 SellerSprite 选市场数据。
     */
    @Tool(name = MARKET_RESEARCH_TOOL_NAME,
            description = "按 Amazon 站点、类目、销量、销售额和竞争度等条件查询 SellerSprite 选市场结果")
    public MarketResearchVo researchMarkets(
            @Valid @ToolParam(description = "选市场强类型筛选条件，必须提供 marketplace，分页和筛选字段遵循 SellerSprite 契约")
            MarketResearchRequest request) {
        return marketService.researchMarkets(request);
    }
}
