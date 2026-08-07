// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.trademark.model.vo;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

/**
 * 全球商标库-统计响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Slf4j
@Data
@Schema(description = "全球商标库-统计响应模型")
public class TrademarkStatsVo {

    /** 全球商标库-统计响应参数：知识产权局；[{"key":"US","count":2}] */
    @Schema(description = "全球商标库-统计响应参数：知识产权局；[{\"key\":\"US\",\"count\":2}]")
    private List<OfficeVo> office;

    /** 全球商标库-统计响应参数：品牌名；格式同office */
    @Schema(description = "全球商标库-统计响应参数：品牌名；格式同office")
    private List<BrandNameVo> brandName;

    /** 全球商标库-统计响应参数：状态；格式同office */
    @Schema(description = "全球商标库-统计响应参数：状态；格式同office")
    private List<StatusVo> status;

    /** 全球商标库-统计响应参数：申请人；格式同office */
    @Schema(description = "全球商标库-统计响应参数：申请人；格式同office")
    private List<ApplicantVo> applicant;

    /** 全球商标库-统计响应参数：尼斯分类；格式同office */
    @Schema(description = "全球商标库-统计响应参数：尼斯分类；格式同office")
    private List<NiceClassVo> niceClass;

    /** 全球商标库-统计响应参数：申请年份；格式同office */
    @Schema(description = "全球商标库-统计响应参数：申请年份；格式同office")
    private List<ApplicationYearVo> applicationYear;

    /** 全球商标库-统计响应参数：过期年份；格式同office */
    @Schema(description = "全球商标库-统计响应参数：过期年份；格式同office")
    private List<ExpiryYearVo> expiryYear;

    /** 官方响应中未建模字段的原始值。 */
    @Schema(description = "官方响应未建模字段", hidden = true)
    private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

    @JsonAnySetter
    public void putAdditionalProperty(String name, JsonNode value) {
        log.warn("SellerSprite 响应包含未建模字段 modelType={}, fieldName={}, fieldValue={}",
                getClass().getName(), name, value);
        additionalProperties.put(name, value);
    }

    @JsonAnyGetter
    public Map<String, JsonNode> getAdditionalProperties() {
        return additionalProperties;
    }

    @Slf4j

    @Data
    @Schema(description = "全球商标库-统计响应参数：知识产权局；[{\"key\":\"US\",\"count\":2}]")
    public static class OfficeVo {

        /** 全球商标库-统计响应参数：值；US */
        @Schema(description = "全球商标库-统计响应参数：值；US")
        private String key;

        /** 全球商标库-统计响应参数：数量；2 */
        @Schema(description = "全球商标库-统计响应参数：数量；2")
        private Integer count;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            log.warn("SellerSprite 响应包含未建模字段 modelType={}, fieldName={}, fieldValue={}",
                    getClass().getName(), name, value);
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

    @Slf4j

    @Data
    @Schema(description = "全球商标库-统计响应参数：品牌名；格式同office")
    public static class BrandNameVo {

        /** 全球商标库-统计响应参数：值；ADVENTURE CLUB */
        @Schema(description = "全球商标库-统计响应参数：值；ADVENTURE CLUB")
        private String key;

        /** 全球商标库-统计响应参数：数量；4 */
        @Schema(description = "全球商标库-统计响应参数：数量；4")
        private Integer count;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            log.warn("SellerSprite 响应包含未建模字段 modelType={}, fieldName={}, fieldValue={}",
                    getClass().getName(), name, value);
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

    @Slf4j

    @Data
    @Schema(description = "全球商标库-统计响应参数：状态；格式同office")
    public static class StatusVo {

        /** 全球商标库-统计响应参数：值；Registered */
        @Schema(description = "全球商标库-统计响应参数：值；Registered")
        private String key;

        /** 全球商标库-统计响应参数：数量；12 */
        @Schema(description = "全球商标库-统计响应参数：数量；12")
        private Integer count;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            log.warn("SellerSprite 响应包含未建模字段 modelType={}, fieldName={}, fieldValue={}",
                    getClass().getName(), name, value);
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

    @Slf4j

    @Data
    @Schema(description = "全球商标库-统计响应参数：申请人；格式同office")
    public static class ApplicantVo {

        /** 全球商标库-统计响应参数：值；ANKER INC */
        @Schema(description = "全球商标库-统计响应参数：值；ANKER INC")
        private String key;

        /** 全球商标库-统计响应参数：数量；4 */
        @Schema(description = "全球商标库-统计响应参数：数量；4")
        private Integer count;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            log.warn("SellerSprite 响应包含未建模字段 modelType={}, fieldName={}, fieldValue={}",
                    getClass().getName(), name, value);
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

    @Slf4j

    @Data
    @Schema(description = "全球商标库-统计响应参数：尼斯分类；格式同office")
    public static class NiceClassVo {

        /** 全球商标库-统计响应参数：值；5 */
        @Schema(description = "全球商标库-统计响应参数：值；5")
        private String key;

        /** 全球商标库-统计响应参数：数量；2 */
        @Schema(description = "全球商标库-统计响应参数：数量；2")
        private Integer count;

        /** 全球商标库-统计响应参数：分类名称；医药用品 */
        @Schema(description = "全球商标库-统计响应参数：分类名称；医药用品")
        private String label;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            log.warn("SellerSprite 响应包含未建模字段 modelType={}, fieldName={}, fieldValue={}",
                    getClass().getName(), name, value);
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

    @Slf4j

    @Data
    @Schema(description = "全球商标库-统计响应参数：申请年份；格式同office")
    public static class ApplicationYearVo {

        /** 全球商标库-统计响应参数：值；1985 */
        @Schema(description = "全球商标库-统计响应参数：值；1985")
        private String key;

        /** 全球商标库-统计响应参数：数量；5 */
        @Schema(description = "全球商标库-统计响应参数：数量；5")
        private Integer count;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            log.warn("SellerSprite 响应包含未建模字段 modelType={}, fieldName={}, fieldValue={}",
                    getClass().getName(), name, value);
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

    @Slf4j

    @Data
    @Schema(description = "全球商标库-统计响应参数：过期年份；格式同office")
    public static class ExpiryYearVo {

        /** 全球商标库-统计响应参数：值；2026 */
        @Schema(description = "全球商标库-统计响应参数：值；2026")
        private String key;

        /** 全球商标库-统计响应参数：数量；2 */
        @Schema(description = "全球商标库-统计响应参数：数量；2")
        private Integer count;

        /** 官方响应中未建模字段的原始值。 */
        @Schema(description = "官方响应未建模字段", hidden = true)
        private final Map<String, JsonNode> additionalProperties = new LinkedHashMap<>();

        @JsonAnySetter
        public void putAdditionalProperty(String name, JsonNode value) {
            log.warn("SellerSprite 响应包含未建模字段 modelType={}, fieldName={}, fieldValue={}",
                    getClass().getName(), name, value);
            additionalProperties.put(name, value);
        }

        @JsonAnyGetter
        public Map<String, JsonNode> getAdditionalProperties() {
            return additionalProperties;
        }

    }

}
