package com.yuanbaomao.sellersprite.research.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 市场调研采集输入。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResearchInput {

    /** 调研任务标识。 */
    private String jobId;

    /** Amazon 市场编码，例如 US。 */
    private String marketplace;

    /** 本次调研的核心关键词。 */
    private String keyword;

    /** 用于评论采集的种子 ASIN，可为空。 */
    private List<String> seedAsins;
}
