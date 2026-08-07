// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.client;

import java.util.List;

/**
 * SellerSprite 九域 Service 与 Controller 契约索引。
 */
public final class GeneratedSellerSpriteEndpointIndex {

    private static final List<Class<?>> SERVICE_TYPES = List.of(
            cyou.yuanbaomao.sellersprite.api.account.service.AccountService.class,
            cyou.yuanbaomao.sellersprite.api.product.service.ProductService.class,
            cyou.yuanbaomao.sellersprite.api.asin.service.AsinService.class,
            cyou.yuanbaomao.sellersprite.api.keyword.service.KeywordService.class,
            cyou.yuanbaomao.sellersprite.api.traffic.service.TrafficService.class,
            cyou.yuanbaomao.sellersprite.api.market.service.MarketService.class,
            cyou.yuanbaomao.sellersprite.api.review.service.ReviewService.class,
            cyou.yuanbaomao.sellersprite.api.trademark.service.TrademarkService.class,
            cyou.yuanbaomao.sellersprite.api.tool.service.ToolService.class);

    private static final List<Class<?>> CONTROLLER_TYPES = List.of(
            cyou.yuanbaomao.sellersprite.api.account.controller.AccountController.class,
            cyou.yuanbaomao.sellersprite.api.product.controller.ProductController.class,
            cyou.yuanbaomao.sellersprite.api.asin.controller.AsinController.class,
            cyou.yuanbaomao.sellersprite.api.keyword.controller.KeywordController.class,
            cyou.yuanbaomao.sellersprite.api.traffic.controller.TrafficController.class,
            cyou.yuanbaomao.sellersprite.api.market.controller.MarketController.class,
            cyou.yuanbaomao.sellersprite.api.review.controller.ReviewController.class,
            cyou.yuanbaomao.sellersprite.api.trademark.controller.TrademarkController.class,
            cyou.yuanbaomao.sellersprite.api.tool.controller.ToolController.class);

    private GeneratedSellerSpriteEndpointIndex() {
    }

    public static List<Class<?>> getServiceTypes() {
        return SERVICE_TYPES;
    }

    public static List<Class<?>> getControllerTypes() {
        return CONTROLLER_TYPES;
    }

    public static int getOperationCount() {
        return 45;
    }
}
