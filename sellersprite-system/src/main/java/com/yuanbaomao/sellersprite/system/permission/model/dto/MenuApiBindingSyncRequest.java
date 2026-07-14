package com.yuanbaomao.sellersprite.system.permission.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "完整菜单接口绑定同步请求")
public class MenuApiBindingSyncRequest {

    @NotEmpty
    @Size(max = 200)
    @Schema(description = "所有业务菜单的接口清单")
    private List<@Valid MenuApiBindingItemRequest> bindings;
}
