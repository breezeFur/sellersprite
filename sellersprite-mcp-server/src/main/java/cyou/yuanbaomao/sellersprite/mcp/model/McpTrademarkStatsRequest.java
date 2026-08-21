package cyou.yuanbaomao.sellersprite.mcp.model;

import cyou.yuanbaomao.sellersprite.api.trademark.model.dto.TrademarkStatsRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

/**
 * MCP 全球商标统计请求，不包含浏览器 MultipartFile 字段。
 */
@Data
@Schema(description = "MCP 全球商标统计请求")
public class McpTrademarkStatsRequest {

    @NotEmpty
    @Schema(description = "数据范围，例如 US")
    private List<String> office;

    @NotBlank
    @Schema(description = "查询文本")
    private String text;

    @Schema(description = "图片 Base64，可选")
    private String imageBase64;

    public TrademarkStatsRequest toApiRequest() {
        TrademarkStatsRequest request = new TrademarkStatsRequest();
        request.setOffice(office);
        request.setText(text);
        request.setImageBase64(imageBase64);
        return request;
    }
}
