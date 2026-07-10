// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.trademark.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 全球商标库-列表请求模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "全球商标库-列表请求模型")
public class TrademarkListRequest {

    /** 全球商标库-列表请求参数：数据范围，见上一个接口；["US"] */
    @Schema(description = "全球商标库-列表请求参数：数据范围，见上一个接口；[\"US\"]")
    private List<String> office;

    /** 全球商标库-列表请求参数：查询文本；CHINESE */
    @NotBlank
    @Schema(description = "全球商标库-列表请求参数：查询文本；CHINESE")
    private String text;

    /** 全球商标库-列表请求参数：base64字符串 */
    @Schema(description = "全球商标库-列表请求参数：base64字符串")
    private String imageBase64;

    /** 全球商标库-列表请求参数：上传的文件；C:\fakepath\人像.jpeg */
    @Schema(description = "全球商标库-列表请求参数：上传的文件；C:\\fakepath\\人像.jpeg")
    private MultipartFile imageFile;

    /** 全球商标库-列表请求参数：品牌名，字段参数见统计接口；["ADVENTURE CLUB"] */
    @Schema(description = "全球商标库-列表请求参数：品牌名，字段参数见统计接口；[\"ADVENTURE CLUB\"]")
    private List<String> brandName;

    /** 全球商标库-列表请求参数：状态，字段参数见统计接口；["Registered"] */
    @Schema(description = "全球商标库-列表请求参数：状态，字段参数见统计接口；[\"Registered\"]")
    private List<String> status;

    /** 全球商标库-列表请求参数：申请人，字段参数见统计接口；["ANKER INC"] */
    @Schema(description = "全球商标库-列表请求参数：申请人，字段参数见统计接口；[\"ANKER INC\"]")
    private List<String> applicant;

    /** 全球商标库-列表请求参数：尼斯分类，字段参数见统计接口；[5] */
    @Schema(description = "全球商标库-列表请求参数：尼斯分类，字段参数见统计接口；[5]")
    private List<String> niceClass;

    /** 全球商标库-列表请求参数：申请年份，字段参数见统计接口；["1985"] */
    @Schema(description = "全球商标库-列表请求参数：申请年份，字段参数见统计接口；[\"1985\"]")
    private List<String> applicationYear;

    /** 全球商标库-列表请求参数：过期年份，字段参数见统计接口；["2026"] */
    @Schema(description = "全球商标库-列表请求参数：过期年份，字段参数见统计接口；[\"2026\"]")
    private List<String> expiryYear;

    /** 全球商标库-列表请求参数：排序字段，默认相关度，applicationDate申请日期 */
    @JsonProperty("order.field")
    @Schema(description = "全球商标库-列表请求参数：排序字段，默认相关度，applicationDate申请日期")
    private String orderField;

    /** 全球商标库-列表请求参数：true降序，false升序，默认true */
    @JsonProperty("order.desc")
    @Schema(description = "全球商标库-列表请求参数：true降序，false升序，默认true")
    private Boolean orderDesc;

    /** 全球商标库-列表请求参数：页码；1 */
    @Min(value = 1, message = "page 不能小于 1")
    @Schema(description = "全球商标库-列表请求参数：页码；1")
    private Integer page;

    /** 全球商标库-列表请求参数：每页条数，最大100；20 */
    @Min(value = 1, message = "size 不能小于 1")
    @Max(value = 100, message = "size 不能大于 100")
    @Schema(description = "全球商标库-列表请求参数：每页条数，最大100；20")
    private Integer size;

}
