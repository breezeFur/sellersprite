// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.tool.model.dto;

import cyou.yuanbaomao.sellersprite.api.tool.validation.ValidOcrSource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 图片文字识别请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@ValidOcrSource
@Data
@Schema(description = "图片文字识别请求模型")
public class OcrRequest {

    /** 图片文字识别请求参数：0：远程图片；1：base64字符串；2：图片文件；2 */
    @NotNull
    @Schema(description = "图片文字识别请求参数：0：远程图片；1：base64字符串；2：图片文件；2")
    private Integer type;

    /** 图片文字识别请求参数：需要识别的语言种类 CHINESE:中文 LATIN:拉丁文；CHINESE */
    @NotBlank
    @Schema(description = "图片文字识别请求参数：需要识别的语言种类 CHINESE:中文 LATIN:拉丁文；CHINESE")
    private String fn;

    /** 图片文字识别请求参数：远程url；https://o.sellersprite.com/docs/202310/sellersprite-2023101210394300742.jpg */
    @Schema(description = "图片文字识别请求参数：远程url；https://o.sellersprite.com/docs/202310/sellersprite-2023101210394300742.jpg")
    private String url;

    /** 图片文字识别请求参数：base64字符串 */
    @Schema(description = "图片文字识别请求参数：base64字符串")
    private String base64;

    /** 图片文字识别请求参数：上传的文件；C:\fakepath\人像.jpeg */
    @Schema(description = "图片文字识别请求参数：上传的文件；C:\\fakepath\\人像.jpeg")
    private MultipartFile image;

}
