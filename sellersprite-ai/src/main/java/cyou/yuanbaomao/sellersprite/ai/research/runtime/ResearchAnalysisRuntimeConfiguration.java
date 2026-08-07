package cyou.yuanbaomao.sellersprite.ai.research.runtime;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@EnableScheduling
@Configuration(proxyBeanMethods = false)
public class ResearchAnalysisRuntimeConfiguration {

    public static final String EXECUTOR_BEAN_NAME = "researchAnalysisTaskExecutor";
    public static final String HEARTBEAT_SCHEDULER_BEAN_NAME = "researchAnalysisHeartbeatScheduler";

    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 4;
    private static final int QUEUE_CAPACITY = 100;
    private static final int HEARTBEAT_POOL_SIZE = 2;

    @Bean(name = EXECUTOR_BEAN_NAME)
    Executor researchAnalysisTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("market-research-analysis-");
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @Bean(name = HEARTBEAT_SCHEDULER_BEAN_NAME)
    ThreadPoolTaskScheduler researchAnalysisHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("market-research-analysis-heartbeat-");
        scheduler.setPoolSize(HEARTBEAT_POOL_SIZE);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        scheduler.initialize();
        return scheduler;
    }
}
