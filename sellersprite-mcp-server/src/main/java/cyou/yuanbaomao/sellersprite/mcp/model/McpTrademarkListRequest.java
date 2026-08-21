package cyou.yuanbaomao.sellersprite.mcp.model;

import cyou.yuanbaomao.sellersprite.api.trademark.model.dto.TrademarkListRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

/**
 * MCP 全球商标列表查询请求，不包含浏览器 MultipartFile 字段。
 */
@Data
@Schema(description = "MCP 全球商标列表查询请求")
public class McpTrademarkListRequest {

    @Schema(description = "数据范围，例如 US")
    private List<String> office;

    @NotBlank
    @Schema(description = "查询文本")
    private String text;

    @Schema(description = "图片 Base64，可选")
    private String imageBase64;

    @Schema(description = "品牌名过滤")
    private List<String> brandName;

    @Schema(description = "商标状态过滤")
    private List<String> status;

    @Schema(description = "申请人过滤")
    private List<String> applicant;

    @Schema(description = "尼斯分类过滤")
    private List<String> niceClass;

    @Schema(description = "申请年份过滤")
    private List<String> applicationYear;

    @Schema(description = "过期年份过滤")
    private List<String> expiryYear;

    @Schema(description = "排序字段")
    private String orderField;

    @Schema(description = "是否降序")
    private Boolean orderDesc;

    @Min(1)
    @Schema(description = "页码")
    private Integer page;

    @Min(1)
    @Max(100)
    @Schema(description = "每页条数，最大 100")
    private Integer size;

    public TrademarkListRequest toApiRequest() {
        TrademarkListRequest request = new TrademarkListRequest();
        request.setOffice(office);
        request.setText(text);
        request.setImageBase64(imageBase64);
        request.setBrandName(brandName);
        request.setStatus(status);
        request.setApplicant(applicant);
        request.setNiceClass(niceClass);
        request.setApplicationYear(applicationYear);
        request.setExpiryYear(expiryYear);
        request.setOrderField(orderField);
        request.setOrderDesc(orderDesc);
        request.setPage(page);
        request.setSize(size);
        return request;
    }
}
