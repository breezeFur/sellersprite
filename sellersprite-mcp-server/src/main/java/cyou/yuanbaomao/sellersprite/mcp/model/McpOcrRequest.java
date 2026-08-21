package cyou.yuanbaomao.sellersprite.mcp.model;

import cyou.yuanbaomao.sellersprite.api.tool.model.dto.OcrRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * MCP 图片文字识别请求。
 *
 * <p>MCP 工具使用 JSON 传输，不支持直接传递 MultipartFile；图片文件应先转换为远程 URL 或 Base64。</p>
 */
@Data
@Schema(description = "MCP 图片文字识别请求")
public class McpOcrRequest {

    private static final int REMOTE_URL_TYPE = 0;
    private static final int BASE64_TYPE = 1;

    @NotNull
    @Schema(description = "图片来源类型：0 远程 URL，1 Base64")
    private Integer type;

    @NotBlank
    @Schema(description = "识别语言种类，例如 CHINESE 或 LATIN")
    private String fn;

    @Schema(description = "远程图片 URL；type=0 时必填")
    private String url;

    @Schema(description = "图片 Base64；type=1 时必填")
    private String base64;

    public OcrRequest toApiRequest() {
        if (type == null) {
            throw new IllegalArgumentException("MCP OCR 必须提供图片来源类型");
        }
        if (REMOTE_URL_TYPE == type && !StringUtils.hasText(url)) {
            throw new IllegalArgumentException("OCR 使用远程图片时必须提供 url");
        }
        if (BASE64_TYPE == type && !StringUtils.hasText(base64)) {
            throw new IllegalArgumentException("OCR 使用 Base64 图片时必须提供 base64");
        }
        if (type != REMOTE_URL_TYPE && type != BASE64_TYPE) {
            throw new IllegalArgumentException("MCP OCR 仅支持远程 URL 或 Base64 图片");
        }
        OcrRequest request = new OcrRequest();
        request.setType(type);
        request.setFn(fn);
        request.setUrl(url);
        request.setBase64(base64);
        return request;
    }
}
