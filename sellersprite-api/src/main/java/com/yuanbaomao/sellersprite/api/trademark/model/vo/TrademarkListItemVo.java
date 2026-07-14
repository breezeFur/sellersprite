// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.trademark.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * 全球商标库-列表明细响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "全球商标库-列表明细响应模型")
public class TrademarkListItemVo {

    /** 全球商标库-列表明细响应参数：id；US502022097612203 */
    @Schema(description = "全球商标库-列表明细响应参数：id；US502022097612203")
    private String id;

    /** 全球商标库-列表明细响应参数：申请人；ANKER INC */
    @Schema(description = "全球商标库-列表明细响应参数：申请人；ANKER INC")
    private List<String> applicant;

    /** 全球商标库-列表明细响应参数：申请人国家；US */
    @Schema(description = "全球商标库-列表明细响应参数：申请人国家；US")
    private Integer applicantCountryCode;

    /** 全球商标库-列表明细响应参数：申请人详情；格式同office */
    @Schema(description = "全球商标库-列表明细响应参数：申请人详情；格式同office")
    private List<ApplicantsVo> applicants;

    /** 全球商标库-列表明细响应参数：申请日期；2022-09-29 */
    @Schema(description = "全球商标库-列表明细响应参数：申请日期；2022-09-29")
    private String applicationDate;

    /** 全球商标库-列表明细响应参数：申请语言；en */
    @Schema(description = "全球商标库-列表明细响应参数：申请语言；en")
    private String applicationLanguageCode;

    /** 全球商标库-列表明细响应参数：申请编号；97612203 */
    @Schema(description = "全球商标库-列表明细响应参数：申请编号；97612203")
    private String applicationNumber;

    /** 全球商标库-列表明细响应参数：注册号；4590785 */
    @Schema(description = "全球商标库-列表明细响应参数：注册号；4590785")
    private String registrationNumber;

    /** 全球商标库-列表明细响应参数：申请参考号 */
    @Schema(description = "全球商标库-列表明细响应参数：申请参考号")
    private List<String> applicationRefNumber;

    /** 全球商标库-列表明细响应参数：品牌名；[ "1ST AID"] */
    @Schema(description = "全球商标库-列表明细响应参数：品牌名；[ \"1ST AID\"]")
    private List<String> brandName;

    /** 全球商标库-列表明细响应参数：数据集；ustm */
    @Schema(description = "全球商标库-列表明细响应参数：数据集；ustm")
    private String collection;

    /** 全球商标库-列表明细响应参数：指定国家；["US"] */
    @Schema(description = "全球商标库-列表明细响应参数：指定国家；[\"US\"]")
    private List<String> designatedCountries;

    /** 全球商标库-列表明细响应参数：指定国家；["US"] */
    @Schema(description = "全球商标库-列表明细响应参数：指定国家；[\"US\"]")
    private List<String> designation;

    /** 全球商标库-列表明细响应参数：申请地点 */
    @Schema(description = "全球商标库-列表明细响应参数：申请地点")
    private String filingPlace;

    /** 全球商标库-列表明细响应参数：商标类别；["Individual"] */
    @Schema(description = "全球商标库-列表明细响应参数：商标类别；[\"Individual\"]")
    private List<String> kind;

    /** 全球商标库-列表明细响应参数：logo */
    @Schema(description = "全球商标库-列表明细响应参数：logo")
    private List<LogosVo> logos;

    /** 全球商标库-列表明细响应参数：商标种类；Combined */
    @Schema(description = "全球商标库-列表明细响应参数：商标种类；Combined")
    private String markFeature;

    /** 全球商标库-列表明细响应参数：尼斯分类；[5] */
    @Schema(description = "全球商标库-列表明细响应参数：尼斯分类；[5]")
    private List<String> niceClass;

    /** 全球商标库-列表明细响应参数：知识产权局；US */
    @Schema(description = "全球商标库-列表明细响应参数：知识产权局；US")
    private String office;

    /** 全球商标库-列表明细响应参数：状态；Pending */
    @Schema(description = "全球商标库-列表明细响应参数：状态；Pending")
    private String status;

    /** 全球商标库-列表明细响应参数：状态更新日期；2023-05-02 */
    @Schema(description = "全球商标库-列表明细响应参数：状态更新日期；2023-05-02")
    private String statusDate;

    /** 全球商标库-列表明细响应参数：类型；TRADEMARK */
    @Schema(description = "全球商标库-列表明细响应参数：类型；TRADEMARK")
    private String type;

    /** 官方响应中未建模字段的原始值。 */
    @Schema(description = "官方响应未建模字段", hidden = true)
    private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

    @JsonAnySetter
    public void putAdditionalProperty(String name, JsonNode value) {
        additionalProperties.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, JsonNode> getAdditionalProperties() {
        return additionalProperties;
    }

    @Data
    @Schema(description = "全球商标库-列表明细响应参数：申请人详情；格式同office")
    public static class ApplicantsVo {

        /** 全球商标库-列表明细响应参数：类型；Legal Entity */
        @Schema(description = "全球商标库-列表明细响应参数：类型；Legal Entity")
        private String kind;

        /** 全球商标库-列表明细响应参数：标识；33744042 */
        @Schema(description = "全球商标库-列表明细响应参数：标识；33744042")
        private String identifier;

        /** 全球商标库-列表明细响应参数：国家编码；US */
        @Schema(description = "全球商标库-列表明细响应参数：国家编码；US")
        private String countryCode;

        /** 全球商标库-列表明细响应参数：联系方式 */
        @Schema(description = "全球商标库-列表明细响应参数：联系方式")
        private JsonNode contact;

        /** 全球商标库-列表明细响应参数：完整地址 */
        @Schema(description = "全球商标库-列表明细响应参数：完整地址")
        private List<ApplicantsFullAddressVo> fullAddress;

        /** 全球商标库-列表明细响应参数：完整名称 */
        @Schema(description = "全球商标库-列表明细响应参数：完整名称")
        private List<ApplicantsFullNameVo> fullName;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

    @Data
    @Schema(description = "全球商标库-列表明细响应参数：完整地址")
    public static class ApplicantsFullAddressVo {

        /** 全球商标库-列表明细响应参数：描述 */
        @Schema(description = "全球商标库-列表明细响应参数：描述")
        private String text;

        /** 全球商标库-列表明细响应参数：语言；en */
        @Schema(description = "全球商标库-列表明细响应参数：语言；en")
        private String languageCode;

        /** 全球商标库-列表明细响应参数：图片URL；https://o.sellersprite.com/w/brands/ustm/US502022097612203/ee45f.jpg */
        @Schema(description = "全球商标库-列表明细响应参数：图片URL；https://o.sellersprite.com/w/brands/ustm/US502022097612203/ee45f.jpg")
        private String imageUrl;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

    @Data
    @Schema(description = "全球商标库-列表明细响应参数：完整名称")
    public static class ApplicantsFullNameVo {

        /** 全球商标库-列表明细响应参数：描述；ANKER INC */
        @Schema(description = "全球商标库-列表明细响应参数：描述；ANKER INC")
        private String text;

        /** 全球商标库-列表明细响应参数：语言；en */
        @Schema(description = "全球商标库-列表明细响应参数：语言；en")
        private String languageCode;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

    @Data
    @Schema(description = "全球商标库-列表明细响应参数：logo")
    public static class LogosVo {

        /** 全球商标库-列表明细响应参数：logo */
        @Schema(description = "全球商标库-列表明细响应参数：logo")
        private String logo;

        /** 全球商标库-列表明细响应参数：logo url */
        @Schema(description = "全球商标库-列表明细响应参数：logo url")
        private String logoUrl;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

}
