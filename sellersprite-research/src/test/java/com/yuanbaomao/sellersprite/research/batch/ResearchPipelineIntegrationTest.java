package com.yuanbaomao.sellersprite.research.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.yuanbaomao.base.context.RequestContext;
import com.yuanbaomao.base.context.RequestContextHolder;
import com.yuanbaomao.base.id.IdGenerator;
import com.yuanbaomao.sellersprite.db.dao.MarketResearchArtifactDao;
import com.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import com.yuanbaomao.sellersprite.db.dao.MarketResearchSnapshotDao;
import com.yuanbaomao.sellersprite.db.dao.impl.MarketResearchArtifactDaoImpl;
import com.yuanbaomao.sellersprite.db.dao.impl.MarketResearchJobDaoImpl;
import com.yuanbaomao.sellersprite.db.dao.impl.MarketResearchSnapshotDaoImpl;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchArtifact;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import com.yuanbaomao.sellersprite.research.model.dto.ResearchJobCreateRequest;
import com.yuanbaomao.sellersprite.research.model.vo.ResearchJobCreatedVo;
import com.yuanbaomao.sellersprite.research.service.ResearchJobService;
import com.yuanbaomao.sellersprite.research.storage.ReportStorage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(
        classes = ResearchPipelineIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:research-pipeline;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.sql.init.mode=always",
                "spring.sql.init.schema-locations=classpath:research-test-schema.sql",
                "spring.batch.jdbc.initialize-schema=always",
                "spring.batch.job.enabled=false",
                "spring.data.redis.repositories.enabled=false",
                "mybatis-plus.global-config.banner=false",
                "sellersprite.research.source-mode=MOCK",
                "sellersprite.research.output-directory=target/research-pipeline-output"
        })
class ResearchPipelineIntegrationTest {

    private static final Duration MAX_WAIT = Duration.ofSeconds(30);
    private static final String USER_ID = "00000000-0000-7000-8000-000000000009";

    @Autowired
    private ResearchJobService researchJobService;

    @Autowired
    private MarketResearchJobDao jobDao;

    @Autowired
    private MarketResearchSnapshotDao snapshotDao;

    @Autowired
    private MarketResearchArtifactDao artifactDao;

    @Autowired
    private ReportStorage reportStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void clearContext() {
        RequestContextHolder.clear();
    }

    @Test
    void shouldCreateRunAndPublishMockWorkbookEndToEnd() throws Exception {
        RequestContextHolder.set(RequestContext.builder().userId(USER_ID).username("research-user").build());
        ResearchJobCreateRequest request = new ResearchJobCreateRequest();
        request.setReportName("美容仪Mock市场调研");
        request.setKeyword("facial cleansing device");
        request.setSeedAsins(List.of("B0DB5VT4QJ"));

        ResearchJobCreatedVo created = researchJobService.create(request);
        MarketResearchJob completed = waitForCompletion(created.getJobId());

        assertThat(completed.getJobStatus()).isEqualTo("SUCCEEDED");
        assertThat(completed.getProgress()).isEqualTo(100);
        assertThat(snapshotDao.listByJobId(created.getJobId()))
                .extracting(snapshot -> snapshot.getBusinessKey())
                .containsExactly("quota.visits", "market.overview", "products", "keywords", "reviews.fixture");
        MarketResearchArtifact artifact = artifactDao.findAvailableByJobId(created.getJobId()).orElseThrow();
        Path report = reportStorage.resolve(artifact.getStorageKey());
        assertThat(report).isRegularFile();
        assertThat(Files.size(report)).isEqualTo(artifact.getFileSize());
        try (XSSFWorkbook workbook = new XSSFWorkbook(Files.newInputStream(report))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(ResearchConstants.REPORT_SHEETS.size());
            assertThat(containsText(workbook.getSheet("US"), "B0DB5VT4QJ")).isTrue();
            assertThat(workbook.getSheet("VOC").getRow(0).getCell(0).toString()).contains("未启用AI");
            assertThat(containsText(workbook.getSheet("原始_配额"), "mock-available")).isTrue();
            assertThat(containsText(workbook.getSheet("原始_市场商品"), "item.units")).isTrue();
            assertThat(containsText(workbook.getSheet("原始_关键词"), "monthlySearches")).isTrue();
            assertThat(containsText(workbook.getSheet("原始_评论"), "verified")).isTrue();
        }
        Integer stepCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM BATCH_STEP_EXECUTION WHERE JOB_EXECUTION_ID = ?",
                Integer.class,
                completed.getBatchJobExecutionId());
        assertThat(stepCount).isEqualTo(8);
    }

    private MarketResearchJob waitForCompletion(String jobId) throws InterruptedException {
        Instant deadline = Instant.now().plus(MAX_WAIT);
        MarketResearchJob job = jobDao.getById(jobId);
        while (job != null && ("QUEUED".equals(job.getJobStatus()) || "RUNNING".equals(job.getJobStatus()))
                && Instant.now().isBefore(deadline)) {
            Thread.sleep(50);
            job = jobDao.getById(jobId);
        }
        return job;
    }

    private boolean containsText(org.apache.poi.ss.usermodel.Sheet sheet, String expected) {
        for (org.apache.poi.ss.usermodel.Row row : sheet) {
            for (org.apache.poi.ss.usermodel.Cell cell : row) {
                if (cell.toString().contains(expected)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(
            basePackages = "com.yuanbaomao.sellersprite.research",
            excludeFilters = @ComponentScan.Filter(
                    type = FilterType.ANNOTATION,
                    classes = SpringBootConfiguration.class))
    @MapperScan("com.yuanbaomao.sellersprite.db.mapper")
    @Import({
            MarketResearchJobDaoImpl.class,
            MarketResearchSnapshotDaoImpl.class,
            MarketResearchArtifactDaoImpl.class
    })
    static class TestApplication {

        @Bean
        @Primary
        IdGenerator researchTestIdGenerator() {
            return () -> UUID.randomUUID().toString();
        }
    }
}
