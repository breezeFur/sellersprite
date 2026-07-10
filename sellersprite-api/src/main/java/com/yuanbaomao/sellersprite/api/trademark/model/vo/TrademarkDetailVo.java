// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.trademark.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import tools.jackson.databind.JsonNode;

/**
 * 全球商标库-详情响应模型。
 *
 * <p>字段来源：SellerSprite 官方接口文档表格。</p>
 */
@Data
@Schema(description = "全球商标库-详情响应模型")
public class TrademarkDetailVo {

    /** 全球商标库-详情响应参数：id；US502022097612203 */
    @Schema(description = "全球商标库-详情响应参数：id；US502022097612203")
    private String id;

    /** 全球商标库-详情响应参数：申请人；ANKER INC */
    @Schema(description = "全球商标库-详情响应参数：申请人；ANKER INC")
    private List<String> applicant;

    /** 全球商标库-详情响应参数：申请人国家；US */
    @Schema(description = "全球商标库-详情响应参数：申请人国家；US")
    private Integer applicantCountryCode;

    /** 全球商标库-详情响应参数：申请人详情；格式同office,结构见下表 */
    @Schema(description = "全球商标库-详情响应参数：申请人详情；格式同office,结构见下表")
    private List<String> applicants;

    /** 全球商标库-详情响应参数：申请日期；2022-09-29 */
    @Schema(description = "全球商标库-详情响应参数：申请日期；2022-09-29")
    private String applicationDate;

    /** 全球商标库-详情响应参数：申请语言；en */
    @Schema(description = "全球商标库-详情响应参数：申请语言；en")
    private String applicationLanguageCode;

    /** 全球商标库-详情响应参数：申请编号；97612203 */
    @Schema(description = "全球商标库-详情响应参数：申请编号；97612203")
    private String applicationNumber;

    /** 全球商标库-详情响应参数：注册号；4590785 */
    @Schema(description = "全球商标库-详情响应参数：注册号；4590785")
    private String registrationNumber;

    /** 全球商标库-详情响应参数：申请参考号 */
    @Schema(description = "全球商标库-详情响应参数：申请参考号")
    private List<String> applicationRefNumber;

    /** 全球商标库-详情响应参数：品牌名；[ "1ST AID"] */
    @Schema(description = "全球商标库-详情响应参数：品牌名；[ \"1ST AID\"]")
    private List<String> brandName;

    /** 全球商标库-详情响应参数：数据集；ustm */
    @Schema(description = "全球商标库-详情响应参数：数据集；ustm")
    private String collection;

    /** 全球商标库-详情响应参数：指定国家；["US"] */
    @Schema(description = "全球商标库-详情响应参数：指定国家；[\"US\"]")
    private List<String> designatedCountries;

    /** 全球商标库-详情响应参数：指定国家；["US"] */
    @Schema(description = "全球商标库-详情响应参数：指定国家；[\"US\"]")
    private List<String> designation;

    /** 全球商标库-详情响应参数：申请地点 */
    @Schema(description = "全球商标库-详情响应参数：申请地点")
    private String filingPlace;

    /** 全球商标库-详情响应参数：商标类别；["Individual"] */
    @Schema(description = "全球商标库-详情响应参数：商标类别；[\"Individual\"]")
    private List<String> kind;

    /** 全球商标库-详情响应参数：logo */
    @Schema(description = "全球商标库-详情响应参数：logo")
    private List<LogosVo> logos;

    /** 全球商标库-详情响应参数：商标种类；Combined */
    @Schema(description = "全球商标库-详情响应参数：商标种类；Combined")
    private String markFeature;

    /** 全球商标库-详情响应参数：尼斯分类；[5] */
    @Schema(description = "全球商标库-详情响应参数：尼斯分类；[5]")
    private List<String> niceClass;

    /** 全球商标库-详情响应参数：知识产权局；US */
    @Schema(description = "全球商标库-详情响应参数：知识产权局；US")
    private String office;

    /** 全球商标库-详情响应参数：状态；Pending */
    @Schema(description = "全球商标库-详情响应参数：状态；Pending")
    private String status;

    /** 全球商标库-详情响应参数：状态更新日期；2023-05-02 */
    @Schema(description = "全球商标库-详情响应参数：状态更新日期；2023-05-02")
    private String statusDate;

    /** 全球商标库-详情响应参数：类型；TRADEMARK */
    @Schema(description = "全球商标库-详情响应参数：类型；TRADEMARK")
    private String type;

    /** 全球商标库-详情响应参数：上诉信息 */
    @Schema(description = "全球商标库-详情响应参数：上诉信息")
    private List<AppealsVo> appeals;

    /** 全球商标库-详情响应参数：通信地址 */
    @Schema(description = "全球商标库-详情响应参数：通信地址")
    private JsonNode correspondence;

    /** 全球商标库-详情响应参数：事件 */
    @Schema(description = "全球商标库-详情响应参数：事件")
    private List<EventsVo> events;

    /** 全球商标库-详情响应参数：过期时间 */
    @Schema(description = "全球商标库-详情响应参数：过期时间")
    private String expiryDate;

    /** 全球商标库-详情响应参数：扩展信息 */
    @Schema(description = "全球商标库-详情响应参数：扩展信息")
    private String extra;

    /** 全球商标库-详情响应参数：品牌状态 */
    @Schema(description = "全球商标库-详情响应参数：品牌状态")
    private String gbdStatus;

    /** 全球商标库-详情响应参数：商品分类信息 */
    @Schema(description = "全球商标库-详情响应参数：商品分类信息")
    private GoodsServicesClassificationVo goodsServicesClassification;

    /** 全球商标库-详情响应参数：商品未分类信息 */
    @Schema(description = "全球商标库-详情响应参数：商品未分类信息")
    private JsonNode goodsServicesUnclassified;

    /** 全球商标库-详情响应参数：商标描述细节 */
    @Schema(description = "全球商标库-详情响应参数：商标描述细节")
    private List<MarkDescriptionDetailsVo> markDescriptionDetails;

    /** 全球商标库-详情响应参数：商标免责声明 */
    @Schema(description = "全球商标库-详情响应参数：商标免责声明")
    private List<MarkDisclaimerDetailsVo> markDisclaimerDetails;

    /** 全球商标库-详情响应参数：商标图形分类 */
    @Schema(description = "全球商标库-详情响应参数：商标图形分类")
    private List<JsonNode> markImageDetails;

    /** 全球商标库-详情响应参数：国际商品分类信息 */
    @Schema(description = "全球商标库-详情响应参数：国际商品分类信息")
    private NationalGoodsServicesClassificationVo nationalGoodsServicesClassification;

    /** 全球商标库-详情响应参数：办公状态 */
    @Schema(description = "全球商标库-详情响应参数：办公状态")
    private String officeStatus;

    /** 全球商标库-详情响应参数：优先事项 */
    @Schema(description = "全球商标库-详情响应参数：优先事项")
    private List<PrioritiesVo> priorities;

    /** 全球商标库-详情响应参数：发表日期 */
    @Schema(description = "全球商标库-详情响应参数：发表日期")
    private String publicationDate;

    /** 全球商标库-详情响应参数：发表详情 */
    @Schema(description = "全球商标库-详情响应参数：发表详情")
    private List<PublicationsVo> publications;

    /** 全球商标库-详情响应参数：审核意见 */
    @Schema(description = "全球商标库-详情响应参数：审核意见")
    private List<QcVo> qc;

    /** 全球商标库-详情响应参数：参考信息 */
    @Schema(description = "全球商标库-详情响应参数：参考信息")
    private ReferenceVo reference;

    /** 全球商标库-详情响应参数：参考办公室 */
    @Schema(description = "全球商标库-详情响应参数：参考办公室")
    private String refOffice;

    /** 全球商标库-详情响应参数：注册日期 */
    @Schema(description = "全球商标库-详情响应参数：注册日期")
    private String registrationDate;

    /** 全球商标库-详情响应参数：注册国家 */
    @Schema(description = "全球商标库-详情响应参数：注册国家")
    private String registrationOfficeCode;

    /** 全球商标库-详情响应参数：注册参考号 */
    @Schema(description = "全球商标库-详情响应参数：注册参考号")
    private List<String> registrationRefNumber;

    /** 全球商标库-详情响应参数：代表信息 */
    @Schema(description = "全球商标库-详情响应参数：代表信息")
    private List<String> representatives;

    /** 全球商标库-详情响应参数：第二语言 */
    @Schema(description = "全球商标库-详情响应参数：第二语言")
    private String secondLanguageCode;

    /** 全球商标库-详情响应参数：id */
    @Schema(description = "全球商标库-详情响应参数：id")
    private String st13;

    /** 全球商标库-详情响应参数：终止日期 */
    @Schema(description = "全球商标库-详情响应参数：终止日期")
    private String terminationDate;

    /** 全球商标库-详情响应参数：文字商标说明 */
    @Schema(description = "全球商标库-详情响应参数：文字商标说明")
    private WordMarkSpecificationVo wordMarkSpecification;

    @Data
    @Schema(description = "全球商标库-详情响应参数：logo")
    public static class LogosVo {

        /** 全球商标库-详情响应参数：logo */
        @Schema(description = "全球商标库-详情响应参数：logo")
        private String logo;

        /** 全球商标库-详情响应参数：logo url */
        @Schema(description = "全球商标库-详情响应参数：logo url")
        private String logoUrl;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：上诉信息")
    public static class AppealsVo {

        /** 全球商标库-详情响应参数：日期 */
        @Schema(description = "全球商标库-详情响应参数：日期")
        private String date;

        /** 全球商标库-详情响应参数：分类 */
        @Schema(description = "全球商标库-详情响应参数：分类")
        private String kind;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：事件")
    public static class EventsVo {

        /** 全球商标库-详情响应参数：日期 */
        @Schema(description = "全球商标库-详情响应参数：日期")
        private String date;

        /** 全球商标库-详情响应参数：产权局分类 */
        @Schema(description = "全球商标库-详情响应参数：产权局分类")
        private String officeKind;

        /** 全球商标库-详情响应参数：品牌分析 */
        @Schema(description = "全球商标库-详情响应参数：品牌分析")
        private String gbdKind;

        /** 全球商标库-详情响应参数：文档 */
        @Schema(description = "全球商标库-详情响应参数：文档")
        private String doc;

        /** 全球商标库-详情响应参数：国家 */
        @Schema(description = "全球商标库-详情响应参数：国家")
        private String country;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：商品分类信息")
    public static class GoodsServicesClassificationVo {

        /** 全球商标库-详情响应参数：类型 */
        @Schema(description = "全球商标库-详情响应参数：类型")
        private String kind;

        /** 全球商标库-详情响应参数：版本 */
        @Schema(description = "全球商标库-详情响应参数：版本")
        private String version;

        /** 全球商标库-详情响应参数：详情 */
        @Schema(description = "全球商标库-详情响应参数：详情")
        private String classification;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：商标描述细节")
    public static class MarkDescriptionDetailsVo {

        /** 全球商标库-详情响应参数：描述；ANKER INC */
        @Schema(description = "全球商标库-详情响应参数：描述；ANKER INC")
        private String text;

        /** 全球商标库-详情响应参数：语言；en */
        @Schema(description = "全球商标库-详情响应参数：语言；en")
        private String languageCode;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：商标免责声明")
    public static class MarkDisclaimerDetailsVo {

        /** 全球商标库-详情响应参数：描述；ANKER INC */
        @Schema(description = "全球商标库-详情响应参数：描述；ANKER INC")
        private String text;

        /** 全球商标库-详情响应参数：语言；en */
        @Schema(description = "全球商标库-详情响应参数：语言；en")
        private String languageCode;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：国际商品分类信息")
    public static class NationalGoodsServicesClassificationVo {

        /** 全球商标库-详情响应参数：类型 */
        @Schema(description = "全球商标库-详情响应参数：类型")
        private String kind;

        /** 全球商标库-详情响应参数：版本 */
        @Schema(description = "全球商标库-详情响应参数：版本")
        private String version;

        /** 全球商标库-详情响应参数：详情 */
        @Schema(description = "全球商标库-详情响应参数：详情")
        private String classification;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：优先事项")
    public static class PrioritiesVo {

        /** 全球商标库-详情响应参数：级别 */
        @Schema(description = "全球商标库-详情响应参数：级别")
        private String severity;

        /** 全球商标库-详情响应参数：code码 */
        @Schema(description = "全球商标库-详情响应参数：code码")
        private String code;

        /** 全球商标库-详情响应参数：字段 */
        @Schema(description = "全球商标库-详情响应参数：字段")
        private String field;

        /** 全球商标库-详情响应参数：类型 */
        @Schema(description = "全球商标库-详情响应参数：类型")
        private String type;

        /** 全球商标库-详情响应参数：说明 */
        @Schema(description = "全球商标库-详情响应参数：说明")
        private String message;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：发表详情")
    public static class PublicationsVo {

        /** 全球商标库-详情响应参数：日期 */
        @Schema(description = "全球商标库-详情响应参数：日期")
        private String date;

        /** 全球商标库-详情响应参数：标志 */
        @Schema(description = "全球商标库-详情响应参数：标志")
        private String identifier;

        /** 全球商标库-详情响应参数：内容 */
        @Schema(description = "全球商标库-详情响应参数：内容")
        private String section;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：审核意见")
    public static class QcVo {

        /** 全球商标库-详情响应参数：级别 */
        @Schema(description = "全球商标库-详情响应参数：级别")
        private String severity;

        /** 全球商标库-详情响应参数：code码 */
        @Schema(description = "全球商标库-详情响应参数：code码")
        private String code;

        /** 全球商标库-详情响应参数：字段 */
        @Schema(description = "全球商标库-详情响应参数：字段")
        private String field;

        /** 全球商标库-详情响应参数：类型 */
        @Schema(description = "全球商标库-详情响应参数：类型")
        private String type;

        /** 全球商标库-详情响应参数：说明 */
        @Schema(description = "全球商标库-详情响应参数：说明")
        private String message;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：参考信息")
    public static class ReferenceVo {

        /** 全球商标库-详情响应参数：机构code */
        @Schema(description = "全球商标库-详情响应参数：机构code")
        private String office;

        /** 全球商标库-详情响应参数：申请信息 */
        @Schema(description = "全球商标库-详情响应参数：申请信息")
        private ReferenceApplicationVo application;

        /** 全球商标库-详情响应参数：注册信息 */
        @Schema(description = "全球商标库-详情响应参数：注册信息")
        private ReferenceRegistrationVo registration;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：申请信息")
    public static class ReferenceApplicationVo {

        /** 全球商标库-详情响应参数：日期 */
        @Schema(description = "全球商标库-详情响应参数：日期")
        private String date;

        /** 全球商标库-详情响应参数：编号 */
        @Schema(description = "全球商标库-详情响应参数：编号")
        private String number;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：注册信息")
    public static class ReferenceRegistrationVo {

        /** 全球商标库-详情响应参数：日期 */
        @Schema(description = "全球商标库-详情响应参数：日期")
        private String date;

        /** 全球商标库-详情响应参数：编号 */
        @Schema(description = "全球商标库-详情响应参数：编号")
        private String number;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：文字商标说明")
    public static class WordMarkSpecificationVo {

        /** 全球商标库-详情响应参数：markTransliteration */
        @Schema(description = "全球商标库-详情响应参数：markTransliteration")
        private String markTransliteration;

        /** 全球商标库-详情响应参数：商标翻译 */
        @Schema(description = "全球商标库-详情响应参数：商标翻译")
        private WordMarkSpecificationMarkTranslationVo markTranslation;

        /** 全球商标库-详情响应参数：markVerbalElement */
        @Schema(description = "全球商标库-详情响应参数：markVerbalElement")
        private WordMarkSpecificationMarkVerbalElementVo markVerbalElement;

        /** 全球商标库-详情响应参数：markSignificantVerbalElement */
        @Schema(description = "全球商标库-详情响应参数：markSignificantVerbalElement")
        private WordMarkSpecificationMarkSignificantVerbalElementVo markSignificantVerbalElement;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：商标翻译")
    public static class WordMarkSpecificationMarkTranslationVo {

        /** 全球商标库-详情响应参数：内容 */
        @Schema(description = "全球商标库-详情响应参数：内容")
        private String text;

        /** 全球商标库-详情响应参数：语言类型 */
        @Schema(description = "全球商标库-详情响应参数：语言类型")
        private String languageCode;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：markVerbalElement")
    public static class WordMarkSpecificationMarkVerbalElementVo {

        /** 全球商标库-详情响应参数：内容 */
        @Schema(description = "全球商标库-详情响应参数：内容")
        private String text;

        /** 全球商标库-详情响应参数：语言类型 */
        @Schema(description = "全球商标库-详情响应参数：语言类型")
        private String languageCode;

    }

    @Data
    @Schema(description = "全球商标库-详情响应参数：markSignificantVerbalElement")
    public static class WordMarkSpecificationMarkSignificantVerbalElementVo {

        /** 全球商标库-详情响应参数：内容；SONICARE */
        @Schema(description = "全球商标库-详情响应参数：内容；SONICARE")
        private String text;

        /** 全球商标库-详情响应参数：语言类型；en */
        @Schema(description = "全球商标库-详情响应参数：语言类型；en")
        private String languageCode;

    }

}
