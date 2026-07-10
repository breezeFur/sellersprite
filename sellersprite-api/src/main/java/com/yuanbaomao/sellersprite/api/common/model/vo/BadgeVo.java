package com.yuanbaomao.sellersprite.api.common.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Amazon 商品徽章标识。
 */
@Data
@Schema(description = "Amazon 商品徽章标识")
public class BadgeVo {

    @Schema(description = "Best Seller 标识或徽章文案")
    private String bestSeller;

    @Schema(description = "Amazon's Choice 标识，Y 表示有，N 表示无")
    private String amazonChoice;

    @Schema(description = "New Release 标识，Y 表示有，N 表示无")
    private String newRelease;

    @Schema(description = "是否包含 A+ 页面，Y 表示有，N 表示无")
    private String ebc;

    @Schema(description = "是否包含视频介绍，Y 表示有，N 表示无")
    private String video;
}
