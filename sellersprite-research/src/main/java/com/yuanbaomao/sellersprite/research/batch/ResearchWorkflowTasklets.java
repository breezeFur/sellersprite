package com.yuanbaomao.sellersprite.research.batch;

import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchSnapshot;
import com.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import com.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import com.yuanbaomao.sellersprite.research.model.ResearchDataset;
import com.yuanbaomao.sellersprite.research.model.ResearchInput;
import com.yuanbaomao.sellersprite.research.model.ResearchSourceMode;
import com.yuanbaomao.sellersprite.research.provider.ResearchDataProvider;
import com.yuanbaomao.sellersprite.research.service.ResearchInputService;
import com.yuanbaomao.sellersprite.research.service.ResearchJobStateService;
import com.yuanbaomao.sellersprite.research.service.ResearchReportService;
import com.yuanbaomao.sellersprite.research.service.ResearchSnapshotService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

/**
 * 固定八步市场调研工作流的无状态 Tasklet 集合。
 */
@Component
@RequiredArgsConstructor
public class ResearchWorkflowTasklets {

    private final ResearchJobStateService jobStateService;
    private final ResearchInputService inputService;
    private final ResearchSnapshotService snapshotService;
    private final ResearchDataProvider dataProvider;
    private final ResearchReportService reportService;
    private final ObjectMapper objectMapper;

    public RepeatStatus validate(StepContribution contribution, ChunkContext chunkContext) {
        String jobId = jobId(contribution);
        runPhase(jobId, ResearchPhase.VALIDATE, () -> validateJob(jobStateService.requireJob(jobId)));
        return RepeatStatus.FINISHED;
    }

    public RepeatStatus checkQuota(StepContribution contribution, ChunkContext chunkContext) {
        String jobId = jobId(contribution);
        runPhase(jobId, ResearchPhase.CHECK_QUOTA, () -> collect(
                jobId,
                ResearchPhase.CHECK_QUOTA,
                dataProvider::checkQuota,
                false));
        return RepeatStatus.FINISHED;
    }

    public RepeatStatus collectMarketAndProducts(
            StepContribution contribution, ChunkContext chunkContext) {
        String jobId = jobId(contribution);
        runPhase(jobId, ResearchPhase.COLLECT_MARKET_AND_PRODUCTS, () -> collect(
                jobId,
                ResearchPhase.COLLECT_MARKET_AND_PRODUCTS,
                dataProvider::collectMarketAndProducts,
                false));
        return RepeatStatus.FINISHED;
    }

    public RepeatStatus collectKeywords(StepContribution contribution, ChunkContext chunkContext) {
        String jobId = jobId(contribution);
        runPhase(jobId, ResearchPhase.COLLECT_KEYWORDS, () -> collect(
                jobId,
                ResearchPhase.COLLECT_KEYWORDS,
                dataProvider::collectKeywords,
                false));
        return RepeatStatus.FINISHED;
    }

    public RepeatStatus collectReviews(StepContribution contribution, ChunkContext chunkContext) {
        String jobId = jobId(contribution);
        runPhase(jobId, ResearchPhase.COLLECT_REVIEWS, () -> collect(
                jobId,
                ResearchPhase.COLLECT_REVIEWS,
                dataProvider::collectReviews,
                true));
        return RepeatStatus.FINISHED;
    }

    public RepeatStatus prepareData(StepContribution contribution, ChunkContext chunkContext) {
        String jobId = jobId(contribution);
        runPhase(jobId, ResearchPhase.PREPARE_DATA, () -> {
            List<MarketResearchSnapshot> snapshots = snapshotService.listByJobId(jobId);
            requirePhase(snapshots, ResearchPhase.CHECK_QUOTA);
            requirePhase(snapshots, ResearchPhase.COLLECT_MARKET_AND_PRODUCTS);
            requirePhase(snapshots, ResearchPhase.COLLECT_KEYWORDS);
            requirePhase(snapshots, ResearchPhase.COLLECT_REVIEWS);
        });
        return RepeatStatus.FINISHED;
    }

    public RepeatStatus renderExcel(StepContribution contribution, ChunkContext chunkContext) {
        String jobId = jobId(contribution);
        runPhase(jobId, ResearchPhase.RENDER_EXCEL, () -> reportService.renderDraft(jobId));
        return RepeatStatus.FINISHED;
    }

    public RepeatStatus validateAndPublish(StepContribution contribution, ChunkContext chunkContext) {
        String jobId = jobId(contribution);
        runPhase(jobId, ResearchPhase.VALIDATE_AND_PUBLISH,
                () -> reportService.validateAndPublish(jobId));
        return RepeatStatus.FINISHED;
    }

    private void collect(
            String jobId,
            ResearchPhase phase,
            DatasetCollector collector,
            boolean allowEmpty) {
        if (snapshotService.hasPhaseSnapshot(jobId, phase)) {
            return;
        }
        MarketResearchJob job = jobStateService.requireJob(jobId);
        requireMatchingSourceMode(job);
        ResearchInput input = inputService.from(job);
        List<ResearchDataset> datasets = collector.collect(input);
        if (datasets == null || datasets.isEmpty()) {
            if (!allowEmpty) {
                throw new IllegalStateException(phase.getDisplayName() + "未返回任何数据集");
            }
            datasets = List.of(new ResearchDataset(
                    "reviews.empty", "REVIEW_LIST", objectMapper.createArrayNode(), 0));
        }
        snapshotService.saveDatasets(job, phase, input, datasets);
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
        if (!ResearchConstants.MARKETPLACE_US.equals(job.getMarketplace())) {
            throw new IllegalArgumentException("第一版仅支持美国站US");
        }
        if (!StringUtils.hasText(job.getKeyword())) {
            throw new IllegalArgumentException("核心关键词不能为空");
        }
        ResearchInput input = inputService.from(job);
        if (input.getSeedAsins().size() > ResearchConstants.MAX_SEED_ASINS) {
            throw new IllegalArgumentException("种子ASIN不能超过" + ResearchConstants.MAX_SEED_ASINS + "个");
        }
    }

    private void requirePhase(List<MarketResearchSnapshot> snapshots, ResearchPhase phase) {
        boolean present = snapshots.stream().anyMatch(snapshot -> phase.name().equals(snapshot.getPhase()));
        if (!present) {
            throw new IllegalStateException("缺少工作流阶段快照: " + phase.name());
        }
    }

    private void runPhase(String jobId, ResearchPhase phase, Runnable action) {
        jobStateService.enter(jobId, phase);
        action.run();
        jobStateService.advance(jobId, phase);
    }

    private String jobId(StepContribution contribution) {
        String jobId = contribution.getStepExecution()
                .getJobExecution()
                .getJobParameters()
                .getString(ResearchConstants.JOB_ID_PARAMETER);
        if (!StringUtils.hasText(jobId)) {
            throw new IllegalStateException("Spring Batch参数缺少jobId");
        }
        return jobId;
    }

    @FunctionalInterface
    private interface DatasetCollector {
        List<ResearchDataset> collect(ResearchInput input);
    }
}
