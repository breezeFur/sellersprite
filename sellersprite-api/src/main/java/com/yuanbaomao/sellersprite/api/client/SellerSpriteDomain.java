package com.yuanbaomao.sellersprite.api.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * SellerSprite Open API 业务分类。
 */
@Getter
@RequiredArgsConstructor
public enum SellerSpriteDomain {

    ACCOUNT("account", "账户次数"),
    PRODUCT("product", "产品研究"),
    ASIN("asin", "ASIN 分析"),
    KEYWORD("keyword", "关键词研究"),
    TRAFFIC("traffic", "流量分析"),
    MARKET("market", "市场分析"),
    REVIEW("review", "评论分析"),
    TRADEMARK("trademark", "全球商标"),
    TOOL("tool", "数据工具");

    /** 用于包、路由和检索的稳定分类编码。 */
    private final String code;

    /** 面向 OpenAPI 和日志的中文分类名称。 */
    private final String description;
}
