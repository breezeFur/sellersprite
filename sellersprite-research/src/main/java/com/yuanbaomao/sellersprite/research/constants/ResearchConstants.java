package com.yuanbaomao.sellersprite.research.constants;

import java.util.List;
import java.util.stream.Stream;

/**
 * 市场调研模块稳定常量。
 */
public final class ResearchConstants {

    public static final String JOB_NAME = "marketResearchJob";
    public static final String JOB_ID_PARAMETER = "jobId";
    public static final String MARKETPLACE_US = "US";
    public static final String TEMPLATE_CODE = "market-research-v1";
    public static final String EXCEL_MEDIA_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String ERROR_CODE_EXECUTION_FAILED = "MR_EXECUTION_FAILED";
    public static final String ERROR_CODE_VALIDATION_FAILED = "MR_VALIDATION_FAILED";
    public static final String ERROR_CODE_ARTIFACT_INVALID = "MR_ARTIFACT_INVALID";
    public static final int MAX_SEED_ASINS = 20;
    public static final List<String> TEMPLATE_SHEETS = List.of(
            "市场调研总结",
            "US",
            "行业销售趋势",
            "行业需求及趋势",
            "细分市场现状",
            "细分市场退货率",
            "竞品品牌",
            "商品集中度",
            "评价",
            "VOC",
            "keywords");
    public static final List<String> RAW_DATA_SHEETS = List.of(
            "原始数据索引",
            "原始_配额",
            "原始_市场商品",
            "原始_关键词",
            "原始_评论");
    public static final List<String> REPORT_SHEETS = Stream.concat(
            TEMPLATE_SHEETS.stream(), RAW_DATA_SHEETS.stream()).toList();
    public static final List<String> RAW_INDEX_HEADERS = List.of(
            "_snapshot.snapshotId",
            "_snapshot.jobId",
            "_snapshot.phase",
            "_snapshot.operation",
            "_snapshot.businessKey",
            "_snapshot.sourceMode",
            "_snapshot.recordCount",
            "_snapshot.fetchedAt",
            "_snapshot.fetchedAtEpochMs",
            "_snapshot.sha256");
    public static final List<String> RAW_RECORD_HEADERS = List.of(
            "_snapshot.phase",
            "_snapshot.operation",
            "_snapshot.businessKey",
            "_snapshot.sourceMode",
            "_snapshot.recordCount",
            "_snapshot.fetchedAt",
            "_snapshot.fetchedAtEpochMs",
            "_recordIndex");

    private ResearchConstants() {
    }
}
