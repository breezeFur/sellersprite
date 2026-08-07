// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.trademark.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 全球商标库-统计请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "全球商标库-统计请求模型")
public class TrademarkStatsRequest {

    /** 全球商标库-统计请求参数：数据范围，见上一个接口；["US"] */
    @NotEmpty
    @Schema(description = "全球商标库-统计请求参数：数据范围，见上一个接口；[\"US\"]")
    private List<String> office;

    /** 全球商标库-统计请求参数：查询文本；CHINESE */
    @NotBlank
    @Schema(description = "全球商标库-统计请求参数：查询文本；CHINESE")
    private String text;

    /** 全球商标库-统计请求参数：base64字符串 */
    @Schema(description = "全球商标库-统计请求参数：base64字符串")
    private String imageBase64;

    /** 全球商标库-统计请求参数：上传的文件；C:\fakepath\人像.jpeg */
    @Schema(description = "全球商标库-统计请求参数：上传的文件；C:\\fakepath\\人像.jpeg")
    private MultipartFile imageFile;

}
