package com.yuanbaomao.sellersprite.research.model.dto;

import com.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "创建市场调研任务请求")
public class ResearchJobCreateRequest {

    @NotBlank
    @Size(max = 128)
    @Schema(description = "报告名称", example = "美容仪美国站市场调研")
    private String reportName;

    @NotBlank
    @Size(max = 256)
    @Schema(description = "核心关键词", example = "facial cleansing device")
    private String keyword;

    @Size(max = ResearchConstants.MAX_SEED_ASINS)
    @Schema(description = "可选种子ASIN，最多20个")
    private List<@Pattern(regexp = "^[A-Za-z0-9]{10}$", message = "ASIN必须是10位字母或数字") String> seedAsins;
}
