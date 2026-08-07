package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchNodeExecution;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceService;
import cyou.yuanbaomao.sellersprite.research.exception.ResearchJobCancelledException;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.model.ResearchSourceMode;
import cyou.yuanbaomao.sellersprite.research.provider.ResearchDataProvider;
import cyou.yuanbaomao.sellersprite.research.support.ResearchMonthUtils;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

/** 执行与 Graph 框架无关的确定性市场调研业务节点。 */
@Service
@RequiredArgsConstructor
public class ResearchWorkflowStepService {

    private static final String COLLECTION_NODE_PREFIX = "collection.";

    private final ResearchJobStateService jobStateService;
    private final ResearchNodeExecutionService nodeExecutionService;
    private final ResearchInputService inputService;
    private final ResearchStageInputService stageInputService;
    private final ResearchDatasetService datasetService;
    private final ResearchDataProvider dataProvider;
    private final ResearchReportService reportService;
    private final ResearchAnalysisStagePort analysisStagePort;
    private final ResearchEvidenceService evidenceService;
    private final ObjectMapper objectMapper;

    public void execute(String jobId, String executionToken, ResearchPhase phase) {
        jobStateService.assertExecutable(jobId, executionToken);
        MarketResearchJob job = jobStateService.requireJob(jobId);
        MarketResearchNodeExecution execution = nodeExecutionService.begin(job, phase);
        try {
            jobStateService.enter(jobId, executionToken, phase);
            executeAction(job, executionToken, phase);
            jobStateService.assertExecutable(jobId, executionToken);
            jobStateService.advance(jobId, executionToken, phase);
            nodeExecutionService.markSucceeded(execution);
        } catch (ResearchJobCancelledException exception) {
            nodeExecutionService.markCancelled(execution, exception);
            throw exception;
        } catch (RuntimeException exception) {
            nodeExecutionService.markFailed(execution, exception);
            throw exception;
        }
    }

    private void executeAction(
            MarketResearchJob job, String executionToken, ResearchPhase phase) {
        switch (phase) {
            case VALIDATE -> validateJob(job);
            case CHECK_QUOTA -> collect(job, phase, dataProvider::checkQuota, false);
            case COLLECT_PRODUCTS -> collect(job, phase, dataProvider::collectProducts, false);
            case COLLECT_MARKET_SALES_TREND ->
                    collect(job, phase, dataProvider::collectMarketSalesTrend, false);
            case COLLECT_KEYWORD_DEMAND_TREND ->
                    collect(job, phase, dataProvider::collectKeywordDemandTrend, false);
            case COLLECT_SEGMENT_OPPORTUNITY ->
                    collect(job, phase, dataProvider::collectSegmentOpportunity, false);
            case COLLECT_REVIEWS -> collectWithProductSeeds(job, phase, dataProvider::collectReviews, true);
            case COLLECT_ASIN_INTELLIGENCE -> collectWithProductSeeds(
                    job, phase, dataProvider::collectAsinIntelligence, false);
            case COLLECT_KEYWORD_INTELLIGENCE -> collectWithProductSeeds(
                    job, phase, dataProvider::collectKeywordIntelligence, false);
            case VALIDATE_RAW_DATA -> validateRawData(job);
            case RENDER_RAW_WORKBOOK -> reportService.renderRawDraft(job.getJobId());
            case PUBLISH_RAW_WORKBOOK -> reportService.validateAndPublishRaw(job.getJobId());
            case PREPARE_US_EVIDENCE,
                    PREPARE_SALES_TREND_EVIDENCE,
                    PREPARE_DEMAND_TREND_EVIDENCE,
                    PREPARE_SEGMENT_MARKET_EVIDENCE,
                    PREPARE_SEGMENT_RETURN_EVIDENCE,
                    PREPARE_BRAND_EVIDENCE,
                    PREPARE_CONCENTRATION_EVIDENCE,
                    PREPARE_REVIEW_EVIDENCE,
                    PREPARE_VOC_EVIDENCE,
                    PREPARE_KEYWORD_EVIDENCE,
                    PREPARE_ASIN_SALES_TREND_EVIDENCE,
                    PREPARE_ASIN_OPERATION_TREND_EVIDENCE -> prepareEvidence(job, phase);
            case VALIDATE_EVIDENCE -> evidenceService.validate(job.getJobId());
            case RENDER_EVIDENCE_WORKBOOK -> reportService.renderEvidenceDraft(job.getJobId());
            case PUBLISH_EVIDENCE_WORKBOOK ->
                    reportService.validateAndPublishEvidence(job.getJobId());
            case RUN_INITIAL_ANALYSIS ->
                    analysisStagePort.runInitial(job.getJobId(), executionToken);
        }
    }

    private void validateRawData(MarketResearchJob job) {
        List<MarketResearchDataset> datasets = datasetService.listByJobId(job.getJobId()).stream()
                .filter(dataset -> dataset.getNodeCode() != null
                        && dataset.getNodeCode().startsWith(COLLECTION_NODE_PREFIX))
                .toList();
        if (datasets.isEmpty()) {
            throw new IllegalStateException("采集子图没有生成原始数据集");
        }
        datasets.forEach(datasetService::readPayload);
    }

    private void collect(
            MarketResearchJob job,
            ResearchPhase phase,
            DatasetCollector collector,
            boolean allowEmpty) {
        if (datasetService.hasValidNodeDatasets(job.getJobId(), phase)) {
            return;
        }
        requireMatchingSourceMode(job);
        ResearchInput input = inputService.from(job);
        saveCollected(job, phase, input, collector.collect(input), allowEmpty);
    }

    private void collectWithProductSeeds(
            MarketResearchJob job,
            ResearchPhase phase,
            DatasetCollector collector,
            boolean allowEmpty) {
        if (datasetService.hasValidNodeDatasets(job.getJobId(), phase)) {
            return;
        }
        requireMatchingSourceMode(job);
        ResearchInput input = inputService.from(job);
        List<String> selectedAsins = stageInputService.findSelection(job.getJobId())
                .filter(selection -> selection.decision()
                        == cyou.yuanbaomao.sellersprite.research.enums.ResearchSelectionDecision.ENTER)
                .map(cyou.yuanbaomao.sellersprite.research.model.ResearchProductSelection::selectedAsins)
                .orElseThrow(() -> new IllegalStateException(
                        "阶段二采集缺少用户商品选择: " + job.getJobId()));
        input = inputService.withSeedAsins(input, selectedAsins);
        saveCollected(job, phase, input, collector.collect(input), allowEmpty);
    }

    private void saveCollected(
            MarketResearchJob job,
            ResearchPhase phase,
            ResearchInput input,
            List<ResearchDataset> datasets,
            boolean allowEmpty) {
        if (datasets == null || datasets.isEmpty()) {
            if (!allowEmpty) {
                throw new IllegalStateException(phase.getDisplayName() + "未返回任何数据集");
            }
            datasets = List.of(new ResearchDataset(
                    "reviews.empty", "REVIEW_LIST", objectMapper.createArrayNode(), 0));
        }
        datasetService.saveDatasets(job, phase, input, datasets);
    }

    private void prepareEvidence(MarketResearchJob job, ResearchPhase phase) {
        if (datasetService.hasValidNodeDatasets(job.getJobId(), phase)) {
            return;
        }
        ResearchInput input = inputService.from(job);
        ResearchDataset evidence = evidenceService.prepare(job, phase);
        datasetService.saveDatasets(job, phase, input, List.of(evidence));
    }

    private void requireMatchingSourceMode(MarketResearchJob job) {
        ResearchSourceMode currentMode = dataProvider.sourceMode();
        if (currentMode == null || !currentMode.name().equals(job.getDataSourceMode())) {
            throw new IllegalStateException("任务数据源模式为" + job.getDataSourceMode()
                    + "，当前服务数据源模式为" + (currentMode == null ? "UNKNOWN" : currentMode.name())
                    + "，请使用任务原数据源配置后重启");
        }
    }

    private void validateJob(MarketResearchJob job) {
        if (!StringUtils.hasText(job.getReportName())) {
            throw new IllegalArgumentException("报告名称不能为空");
        }
        if (SellerSpriteMarketplace.fromTransportValue(job.getMarketplace()) == null) {
            throw new IllegalArgumentException("市场不能为空");
        }
        if (!StringUtils.hasText(job.getNodeIdPath())
                || !Pattern.matches(ResearchConstants.NODE_ID_PATH_PATTERN, job.getNodeIdPath().trim())) {
            throw new IllegalArgumentException("类目节点路径格式不正确");
        }
        ResearchMonthUtils.normalize(job.getResearchMonth());
        if (inputService.from(job).getSeedAsins().size() > ResearchConstants.MAX_SEED_ASINS) {
            throw new IllegalArgumentException("种子ASIN不能超过" + ResearchConstants.MAX_SEED_ASINS + "个");
        }
    }

    @FunctionalInterface
    private interface DatasetCollector {
        List<ResearchDataset> collect(ResearchInput input);
    }
}
