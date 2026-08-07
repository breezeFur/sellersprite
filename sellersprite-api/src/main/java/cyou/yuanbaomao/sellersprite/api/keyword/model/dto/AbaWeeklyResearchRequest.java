// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.keyword.model.dto;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.api.common.model.dto.SortOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * ABA 数据选品-按周请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "ABA 数据选品-按周请求模型")
public class AbaWeeklyResearchRequest {

    /** ABA 数据选品-按周请求参数：市场；见表 1.2 */
    @NotNull
    @Schema(description = "ABA 数据选品-按周请求参数：市场；见表 1.2")
    private SellerSpriteMarketplace marketplace;

    /** ABA 数据选品-按周请求参数：为空时，查最新周；20230610，限定为周六的日期） */
    @Schema(description = "ABA 数据选品-按周请求参数：为空时，查最新周；20230610，限定为周六的日期）")
    private String date;

    /** ABA 数据选品-按周请求参数：类目列表；["automotive","baby-products"] */
    @Schema(description = "ABA 数据选品-按周请求参数：类目列表；[\"automotive\",\"baby-products\"]")
    private List<String> departments;

    /** ABA 数据选品-按周请求参数：排除关键词；portable */
    @Schema(description = "ABA 数据选品-按周请求参数：排除关键词；portable")
    private String excludeKeywords;

    /** ABA 数据选品-按周请求参数：包含关键词 */
    @Schema(description = "ABA 数据选品-按周请求参数：包含关键词")
    private String includeKeywords;

    /** ABA 数据选品-按周请求参数：是否精确匹配 */
    @Schema(description = "ABA 数据选品-按周请求参数：是否精确匹配")
    private Boolean exactFlag;

    /** ABA 数据选品-按周请求参数：搜索增长量 */
    @Schema(description = "ABA 数据选品-按周请求参数：搜索增长量")
    private Integer rankGrowthValue;

    /** ABA 数据选品-按周请求参数：搜索增长率 */
    @Schema(description = "ABA 数据选品-按周请求参数：搜索增长率")
    private BigDecimal rankGrowthRate;

    /** ABA 数据选品-按周请求参数：最小排名增长率 */
    @Schema(description = "ABA 数据选品-按周请求参数：最小排名增长率")
    private BigDecimal minRankGrowthRate;

    /** ABA 数据选品-按周请求参数：最大排名增长率 */
    @Schema(description = "ABA 数据选品-按周请求参数：最大排名增长率")
    private BigDecimal maxRankGrowthRate;

    /** ABA 数据选品-按周请求参数：最小排名 */
    @Schema(description = "ABA 数据选品-按周请求参数：最小排名")
    private Integer minSearchRank;

    /** ABA 数据选品-按周请求参数：最大排名 */
    @Schema(description = "ABA 数据选品-按周请求参数：最大排名")
    private Integer maxSearchRank;

    /** ABA 数据选品-按周请求参数：最小搜索量 */
    @Schema(description = "ABA 数据选品-按周请求参数：最小搜索量")
    private Integer minSearches;

    /** ABA 数据选品-按周请求参数：最大搜索量 */
    @Schema(description = "ABA 数据选品-按周请求参数：最大搜索量")
    private Integer maxSearches;

    /** ABA 数据选品-按周请求参数：最小点击集中度 */
    @Schema(description = "ABA 数据选品-按周请求参数：最小点击集中度")
    private BigDecimal minMonopolyClickRate;

    /** ABA 数据选品-按周请求参数：最大点击集中度 */
    @Schema(description = "ABA 数据选品-按周请求参数：最大点击集中度")
    private BigDecimal maxMonopolyClickRate;

    /** ABA 数据选品-按周请求参数：最小转化占比 */
    @Schema(description = "ABA 数据选品-按周请求参数：最小转化占比")
    private BigDecimal minConversionRate;

    /** ABA 数据选品-按周请求参数：最大转化占比 */
    @Schema(description = "ABA 数据选品-按周请求参数：最大转化占比")
    private BigDecimal maxConversionRate;

    /** ABA 数据选品-按周请求参数：最小单词数 */
    @Schema(description = "ABA 数据选品-按周请求参数：最小单词数")
    private Integer minWordCount;

    /** ABA 数据选品-按周请求参数：最大单词数 */
    @Schema(description = "ABA 数据选品-按周请求参数：最大单词数")
    private Integer maxWordCount;

    /** ABA 数据选品-按周请求参数：最小SPR */
    @Schema(description = "ABA 数据选品-按周请求参数：最小SPR")
    private Integer minSPR;

    /** ABA 数据选品-按周请求参数：最大SPR */
    @Schema(description = "ABA 数据选品-按周请求参数：最大SPR")
    private Integer maxSPR;

    /** ABA 数据选品-按周请求参数：最小标题密度 */
    @Schema(description = "ABA 数据选品-按周请求参数：最小标题密度")
    private Integer minTitleDensity;

    /** ABA 数据选品-按周请求参数：最大标题密度 */
    @Schema(description = "ABA 数据选品-按周请求参数：最大标题密度")
    private Integer maxTitleDensity;

    /** ABA 数据选品-按周请求参数：最小点击量；1 */
    @Schema(description = "ABA 数据选品-按周请求参数：最小点击量；1")
    private Integer minClicks;

    /** ABA 数据选品-按周请求参数：最大点击量；10000 */
    @Schema(description = "ABA 数据选品-按周请求参数：最大点击量；10000")
    private Integer maxClicks;

    /** ABA 数据选品-按周请求参数：最小展示量；10000 */
    @Schema(description = "ABA 数据选品-按周请求参数：最小展示量；10000")
    private Integer minImpressions;

    /** ABA 数据选品-按周请求参数：最大展示量；20000 */
    @Schema(description = "ABA 数据选品-按周请求参数：最大展示量；20000")
    private Integer maxImpressions;

    /** ABA 数据选品-按周请求参数：搜索模式：1：热门市场2：异动市场3：持续增长市场4：快速飙升市场5：潜力市场6：长尾市场；1 */
    @Schema(description = "ABA 数据选品-按周请求参数：搜索模式：1：热门市场2：异动市场3：持续增长市场4：快速飙升市场5：潜力市场6：长尾市场；1")
    private Integer searchModel;

    /** ABA 数据选品-按周请求参数：页码，从 1 开始；默认：1 */
    @Min(value = 1, message = "page 不能小于 1")
    @Schema(description = "ABA 数据选品-按周请求参数：页码，从 1 开始；默认：1")
    private Integer page = 1;

    /** ABA 数据选品-按周请求参数：每页条数，最大40；默认：40 */
    @Min(value = 1, message = "size 不能小于 1")
    @Max(value = 40, message = "size 不能大于 40")
    @Schema(description = "ABA 数据选品-按周请求参数：每页条数，最大40；默认：40")
    private Integer size = 40;

    /** ABA 数据选品-按周请求参数：排序 */
    @Schema(description = "ABA 数据选品-按周请求参数：排序")
    private SortOrder order;

}
