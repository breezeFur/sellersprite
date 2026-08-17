package cyou.yuanbaomao.sellersprite.research.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordMinerRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.KeepaTrendRequest;
import cyou.yuanbaomao.sellersprite.api.market.model.dto.MarketResearchRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest;
import cyou.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficKeywordRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/**
 * 采集子图参数快照。
 *
 * <p>外部请求 DTO 中的 marketplace、month、keyword、nodeIdPath、asin、page 和 size
 * 在实际调用前由任务根上下文与编排参数覆盖。</p>
 */
@Data
@Schema(description = "采集子图参数")
public class CollectionGraphConfig {

    private static final int DEFAULT_START_PAGE = 1;
    private static final int DEFAULT_PRODUCT_PAGE_SIZE = 100;
    private static final int DEFAULT_PRODUCT_TARGET_COUNT = 100;
    private static final int DEFAULT_MARKET_PAGE_SIZE = 50;
    private static final int DEFAULT_MARKET_TARGET_COUNT = 50;
    private static final int DEFAULT_REVIEW_PAGE_SIZE = 10;
    private static final int DEFAULT_REVIEW_TARGET_COUNT = 20;
    private static final int MAX_REVIEW_TARGET_COUNT = 20;
    private static final int DEFAULT_ENRICHMENT_ASIN_LIMIT = 5;
    private static final int DEFAULT_TRAFFIC_ASIN_LIMIT = 5;
    private static final int DEFAULT_MONTH_COUNT = 12;
    private static final int MAX_MONTH_COUNT = 120;
    private static final int DEFAULT_TOP_N = 100;
    private static final int DEFAULT_NEW_PRODUCT_MONTHS = 6;

    @Valid
    @NotNull
    @Schema(description = "商品池采集参数")
    private ProductCollectionConfig collectProducts = new ProductCollectionConfig();

    @Valid
    @NotNull
    @Schema(description = "市场销售趋势采集参数")
    private MarketSalesTrendCollectionConfig collectMarketSalesTrend =
            new MarketSalesTrendCollectionConfig();

    @Valid
    @NotNull
    @Schema(description = "关键词需求趋势采集参数")
    private KeywordDemandTrendCollectionConfig collectKeywordDemandTrend =
            new KeywordDemandTrendCollectionConfig();

    @Valid
    @NotNull
    @Schema(description = "细分市场机会采集参数")
    private SegmentOpportunityCollectionConfig collectSegmentOpportunity =
            new SegmentOpportunityCollectionConfig();

    @Valid
    @NotNull
    @Schema(description = "评论采集参数")
    private ReviewCollectionConfig collectReviews = new ReviewCollectionConfig();

    @Valid
    @NotNull
    @Schema(description = "关键词情报采集参数")
    private KeywordIntelligenceCollectionConfig collectKeywordIntelligence =
            new KeywordIntelligenceCollectionConfig();

    @Valid
    @NotNull
    @Schema(description = "人工选中ASIN趋势情报采集参数")
    private AsinIntelligenceCollectionConfig collectAsinIntelligence =
            new AsinIntelligenceCollectionConfig();

    @Data
    @Schema(description = "商品池采集参数")
    public static class ProductCollectionConfig {

        @NotNull
        @Schema(description = "选产品接口筛选参数；根上下文和分页字段由系统覆盖")
        private ProductResearchRequest productResearch = new ProductResearchRequest();

        @Valid
        @NotNull
        private ProductPagination pagination = new ProductPagination();

        @Schema(description = "补充采集ASIN数量", example = "5")
        @NotNull
        @Min(0)
        private Integer enrichmentAsinLimit = DEFAULT_ENRICHMENT_ASIN_LIMIT;
    }

    @Data
    @Schema(description = "市场销售趋势采集参数")
    public static class MarketSalesTrendCollectionConfig {

        @NotNull
        @Min(1)
        @Max(MAX_MONTH_COUNT)
        @Schema(description = "包含基准月的连续自然月数量", example = "12")
        private Integer monthCount = DEFAULT_MONTH_COUNT;
    }

    @Data
    @JsonIgnoreProperties("newProduct")
    @Schema(description = "关键词需求趋势采集参数")
    public static class KeywordDemandTrendCollectionConfig {

        @Schema(description = "市场需求趋势样本数量", example = "100")
        private Integer topN = DEFAULT_TOP_N;
    }

    @Data
    @Schema(description = "细分市场机会采集参数")
    public static class SegmentOpportunityCollectionConfig {

        @NotNull
        @Schema(description = "选市场接口筛选参数；根上下文和分页字段由系统覆盖")
        private MarketResearchRequest marketResearch = new MarketResearchRequest();

        @Valid
        @NotNull
        private MarketPagination pagination = new MarketPagination();

        @Valid
        @NotNull
        private MarketDistributionConfig distribution = new MarketDistributionConfig();
    }

    @Data
    @Schema(description = "评论采集参数")
    public static class ReviewCollectionConfig {

        @Schema(description = "评论星级筛选")
        private List<String> starList = List.of();

        @Schema(description = "评论类型筛选")
        private List<String> typeList = List.of();

        @Valid
        @NotNull
        private ReviewPagination pagination = new ReviewPagination();
    }

    @Data
    @Schema(description = "关键词情报采集参数")
    public static class KeywordIntelligenceCollectionConfig {

        @NotNull
        @Schema(description = "关键词选品接口筛选；根上下文由系统覆盖")
        private KeywordResearchRequest keywordResearch = new KeywordResearchRequest();

        @NotNull
        @Schema(description = "关键词挖掘接口筛选；根上下文由系统覆盖")
        private KeywordMinerRequest keywordMiner = new KeywordMinerRequest();

        @NotNull
        @Schema(description = "流量词接口筛选；根上下文与ASIN由系统覆盖")
        private TrafficKeywordRequest trafficKeyword = new TrafficKeywordRequest();

        @Schema(description = "反查流量词的ASIN数量", example = "5")
        @NotNull
        @Min(0)
        private Integer trafficAsinLimit = DEFAULT_TRAFFIC_ASIN_LIMIT;
    }

    @Data
    @Schema(description = "人工选中ASIN趋势情报采集参数")
    public static class AsinIntelligenceCollectionConfig {

        @NotNull
        @Schema(description = "Keepa趋势筛选；marketplace与ASIN由人工选择覆盖")
        private KeepaTrendRequest keepaTrend = new KeepaTrendRequest();
    }

    @Data
    @Schema(description = "商品采集分页参数")
    public static class ProductPagination {

        @NotNull
        @Min(1)
        private Integer startPage = DEFAULT_START_PAGE;

        @NotNull
        @Min(1)
        @Max(100)
        private Integer pageSize = DEFAULT_PRODUCT_PAGE_SIZE;

        @NotNull
        @Min(1)
        private Integer targetCount = DEFAULT_PRODUCT_TARGET_COUNT;
    }

    @Data
    @Schema(description = "市场采集分页参数")
    public static class MarketPagination {

        @NotNull
        @Min(1)
        private Integer startPage = DEFAULT_START_PAGE;

        @NotNull
        @Min(1)
        @Max(200)
        private Integer pageSize = DEFAULT_MARKET_PAGE_SIZE;

        @NotNull
        @Min(1)
        private Integer targetCount = DEFAULT_MARKET_TARGET_COUNT;
    }

    @Data
    @Schema(description = "评论采集分页参数")
    public static class ReviewPagination {

        @NotNull
        @Min(1)
        private Integer startPage = DEFAULT_START_PAGE;

        @NotNull
        @Min(1)
        @Max(10)
        private Integer pageSize = DEFAULT_REVIEW_PAGE_SIZE;

        @NotNull
        @Min(1)
        @Max(MAX_REVIEW_TARGET_COUNT)
        private Integer targetCountPerAsin = DEFAULT_REVIEW_TARGET_COUNT;
    }

    @Data
    @Schema(description = "市场统计与分布公共参数")
    public static class MarketDistributionConfig {

        private Integer topN = DEFAULT_TOP_N;

        private Integer newProduct = DEFAULT_NEW_PRODUCT_MONTHS;

        private List<String> asins = List.of();
    }
}
