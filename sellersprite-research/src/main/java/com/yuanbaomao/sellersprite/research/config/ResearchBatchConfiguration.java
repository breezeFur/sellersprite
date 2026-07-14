package com.yuanbaomao.sellersprite.research.config;

import com.yuanbaomao.sellersprite.research.batch.ResearchJobExecutionListener;
import com.yuanbaomao.sellersprite.research.batch.ResearchWorkflowTasklets;
import com.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.boot.batch.autoconfigure.BatchTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 市场调研固定 Spring Batch 工作流。
 */
@Configuration(proxyBeanMethods = false)
public class ResearchBatchConfiguration {

    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 4;
    private static final int QUEUE_CAPACITY = 100;

    @Bean
    @BatchTaskExecutor
    ThreadPoolTaskExecutor researchBatchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("market-research-");
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }

    @Bean
    Step validateResearchStep(JobRepository jobRepository, ResearchWorkflowTasklets tasklets) {
        return new StepBuilder("validateResearchStep", jobRepository)
                .tasklet(tasklets::validate)
                .build();
    }

    @Bean
    Step checkResearchQuotaStep(JobRepository jobRepository, ResearchWorkflowTasklets tasklets) {
        return new StepBuilder("checkResearchQuotaStep", jobRepository)
                .tasklet(tasklets::checkQuota)
                .build();
    }

    @Bean
    Step collectMarketAndProductsStep(JobRepository jobRepository, ResearchWorkflowTasklets tasklets) {
        return new StepBuilder("collectMarketAndProductsStep", jobRepository)
                .tasklet(tasklets::collectMarketAndProducts)
                .build();
    }

    @Bean
    Step collectKeywordsStep(JobRepository jobRepository, ResearchWorkflowTasklets tasklets) {
        return new StepBuilder("collectKeywordsStep", jobRepository)
                .tasklet(tasklets::collectKeywords)
                .build();
    }

    @Bean
    Step collectReviewsStep(JobRepository jobRepository, ResearchWorkflowTasklets tasklets) {
        return new StepBuilder("collectReviewsStep", jobRepository)
                .tasklet(tasklets::collectReviews)
                .build();
    }

    @Bean
    Step prepareResearchDataStep(JobRepository jobRepository, ResearchWorkflowTasklets tasklets) {
        return new StepBuilder("prepareResearchDataStep", jobRepository)
                .tasklet(tasklets::prepareData)
                .build();
    }

    @Bean
    Step renderResearchExcelStep(JobRepository jobRepository, ResearchWorkflowTasklets tasklets) {
        return new StepBuilder("renderResearchExcelStep", jobRepository)
                .tasklet(tasklets::renderExcel)
                .build();
    }

    @Bean
    Step validateAndPublishResearchStep(JobRepository jobRepository, ResearchWorkflowTasklets tasklets) {
        return new StepBuilder("validateAndPublishResearchStep", jobRepository)
                .tasklet(tasklets::validateAndPublish)
                .build();
    }

    @Bean(name = ResearchConstants.JOB_NAME)
    Job marketResearchJob(
            JobRepository jobRepository,
            ResearchJobExecutionListener listener,
            Step validateResearchStep,
            Step checkResearchQuotaStep,
            Step collectMarketAndProductsStep,
            Step collectKeywordsStep,
            Step collectReviewsStep,
            Step prepareResearchDataStep,
            Step renderResearchExcelStep,
            Step validateAndPublishResearchStep) {
        return new JobBuilder(ResearchConstants.JOB_NAME, jobRepository)
                .listener(listener)
                .start(validateResearchStep)
                .next(checkResearchQuotaStep)
                .next(collectMarketAndProductsStep)
                .next(collectKeywordsStep)
                .next(collectReviewsStep)
                .next(prepareResearchDataStep)
                .next(renderResearchExcelStep)
                .next(validateAndPublishResearchStep)
                .build();
    }
}
