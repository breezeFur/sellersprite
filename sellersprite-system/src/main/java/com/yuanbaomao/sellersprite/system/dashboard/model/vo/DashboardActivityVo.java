package com.yuanbaomao.sellersprite.system.dashboard.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "首页近期活动")
public class DashboardActivityVo {
    private String type;
    private String title;
    private String description;
    private Integer success;
    private Long occurredAt;
    private String trackId;
}
