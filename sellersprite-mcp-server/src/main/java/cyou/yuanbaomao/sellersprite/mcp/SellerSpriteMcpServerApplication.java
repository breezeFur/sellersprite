package cyou.yuanbaomao.sellersprite.mcp;

import cyou.yuanbaomao.sellersprite.api.account.service.impl.AccountServiceImpl;
import cyou.yuanbaomao.sellersprite.api.asin.service.impl.AsinServiceImpl;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteApiConfig;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteDictionaryResolver;
import cyou.yuanbaomao.sellersprite.api.keyword.service.impl.KeywordServiceImpl;
import cyou.yuanbaomao.sellersprite.api.market.service.impl.MarketServiceImpl;
import cyou.yuanbaomao.sellersprite.api.product.service.impl.ProductServiceImpl;
import cyou.yuanbaomao.sellersprite.api.review.service.impl.ReviewServiceImpl;
import cyou.yuanbaomao.sellersprite.api.tool.service.impl.ToolServiceImpl;
import cyou.yuanbaomao.sellersprite.api.traffic.service.impl.TrafficServiceImpl;
import cyou.yuanbaomao.sellersprite.api.trademark.service.impl.TrademarkServiceImpl;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * SellerSprite API MCP 服务启动入口。
 *
 * <p>只导入 API Service 和 Client 配置，不扫描 REST Controller，避免独立 MCP 进程重复暴露管理台接口。</p>
 */
@SpringBootApplication
@Import({
        SellerSpriteApiConfig.class,
        SellerSpriteDictionaryResolver.class,
        AccountServiceImpl.class,
        ProductServiceImpl.class,
        AsinServiceImpl.class,
        KeywordServiceImpl.class,
        TrafficServiceImpl.class,
        MarketServiceImpl.class,
        ReviewServiceImpl.class,
        TrademarkServiceImpl.class,
        ToolServiceImpl.class
})
public class SellerSpriteMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SellerSpriteMcpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider sellerSpriteMcpToolCallbackProvider(SellerSpriteMcpTools tools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(tools)
                .build();
    }
}
