package com.yuanbaomao.sellersprite.system.permission.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "后端接口目录装载结果")
public class ApiCatalogSyncResultVo {

    @Schema(description = "扫描到的明确方法接口数")
    private int scanned;

    @Schema(description = "新增接口数")
    private int created;

    @Schema(description = "刷新元数据接口数")
    private int updated;

    @Schema(description = "未变化接口数")
    private int unchanged;
}
