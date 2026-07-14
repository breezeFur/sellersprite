package com.yuanbaomao.sellersprite.research.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.yuanbaomao.sellersprite.research.model.ResearchSourceMode;

import lombok.Data;

/**
 * 市场调研工作流配置。
 */
@Data
@ConfigurationProperties(prefix = ResearchProperties.PREFIX)
public class ResearchProperties {

    /** 市场调研配置前缀。 */
    public static final String PREFIX = "sellersprite.research";

    /** 默认使用本地 Mock，确保开发和自动化测试不依赖远端网络。 */
    private ResearchSourceMode sourceMode = ResearchSourceMode.MOCK;

    /** 应用启动时是否补偿尚未绑定 BatchExecution 的排队任务。 */
    private boolean recoveryEnabled = true;

    /** 版本化 Mock fixture 的 Spring Resource 地址。 */
    private String mockFixtureLocation = "classpath:research/mock/v1/market-research.json";

    /** 第一版市场调研 Excel 模板的 Spring Resource 地址。 */
    private String templateLocation = "classpath:research/templates/market-research-v1.xlsx";

    /** 生成报告的本地输出目录。 */
    private String outputDirectory = "./data/market-research";
}
