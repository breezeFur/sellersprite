package com.yuanbaomao.sellersprite.system.permission.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "单个菜单接口清单")
public class MenuApiBindingItemRequest {

    @NotBlank
    @Schema(description = "菜单功能编码", example = "system.user")
    private String functionCode;

    @NotEmpty
    @Schema(description = "菜单实际使用的接口")
    private List<@Valid ApiEndpointRefRequest> apis;
}
