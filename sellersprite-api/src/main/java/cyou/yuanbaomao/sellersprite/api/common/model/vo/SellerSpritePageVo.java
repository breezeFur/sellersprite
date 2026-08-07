package cyou.yuanbaomao.sellersprite.api.common.model.vo;

import java.util.List;

import cyou.yuanbaomao.sellersprite.api.common.model.dto.SortOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * SellerSprite 标准分页数据。
 *
 * @param <T> 当前页明细类型
 */
@Data
@Schema(description = "SellerSprite 标准分页数据")
public class SellerSpritePageVo<T> {

    @Schema(description = "访客标识，密钥调用通常为空")
    private String guestId;

    @Schema(description = "总页数")
    private Integer pages;

    @Schema(description = "当前页码，从 1 开始")
    private Integer page;

    @Schema(description = "每页条数")
    private Integer size;

    @Schema(description = "命中总条数")
    private Long total;

    @Schema(description = "SellerSprite 查询耗时，单位毫秒")
    private Long took;

    @Schema(description = "结果关联地址，通常为空")
    private String url;

    @Schema(description = "实际采用的排序条件")
    private SortOrder order;

    @Schema(description = "当前页明细")
    private List<T> items;

    @Schema(description = "终止游标，普通分页通常为空")
    private String terminal;

    @Schema(description = "是否还有下一页")
    private Boolean hasNextPage;

    @Schema(description = "是否按访客额度计次")
    private Boolean guestVisited;
}
