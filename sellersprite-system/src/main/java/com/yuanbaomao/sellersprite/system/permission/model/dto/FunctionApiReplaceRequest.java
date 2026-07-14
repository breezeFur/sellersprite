package com.yuanbaomao.sellersprite.system.permission.model.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class FunctionApiReplaceRequest {

    @NotNull(message = "接口ID集合不能为空")
    private List<String> apiIds;
}
