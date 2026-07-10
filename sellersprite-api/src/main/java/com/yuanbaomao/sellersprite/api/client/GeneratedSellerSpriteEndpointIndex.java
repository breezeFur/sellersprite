// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.client;

import java.util.List;

/**
 * SellerSprite 九域 Service 与 Controller 契约索引。
 */
public final class GeneratedSellerSpriteEndpointIndex {

    private static final List<Class<?>> SERVICE_TYPES = List.of(
            com.yuanbaomao.sellersprite.api.account.service.AccountService.class,
            com.yuanbaomao.sellersprite.api.product.service.ProductService.class,
            com.yuanbaomao.sellersprite.api.asin.service.AsinService.class,
            com.yuanbaomao.sellersprite.api.keyword.service.KeywordService.class,
            com.yuanbaomao.sellersprite.api.traffic.service.TrafficService.class,
            com.yuanbaomao.sellersprite.api.market.service.MarketService.class,
            com.yuanbaomao.sellersprite.api.review.service.ReviewService.class,
            com.yuanbaomao.sellersprite.api.trademark.service.TrademarkService.class,
            com.yuanbaomao.sellersprite.api.tool.service.ToolService.class);

    private static final List<Class<?>> CONTROLLER_TYPES = List.of(
            com.yuanbaomao.sellersprite.api.account.controller.AccountController.class,
            com.yuanbaomao.sellersprite.api.product.controller.ProductController.class,
            com.yuanbaomao.sellersprite.api.asin.controller.AsinController.class,
            com.yuanbaomao.sellersprite.api.keyword.controller.KeywordController.class,
            com.yuanbaomao.sellersprite.api.traffic.controller.TrafficController.class,
            com.yuanbaomao.sellersprite.api.market.controller.MarketController.class,
            com.yuanbaomao.sellersprite.api.review.controller.ReviewController.class,
            com.yuanbaomao.sellersprite.api.trademark.controller.TrademarkController.class,
            com.yuanbaomao.sellersprite.api.tool.controller.ToolController.class);

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
