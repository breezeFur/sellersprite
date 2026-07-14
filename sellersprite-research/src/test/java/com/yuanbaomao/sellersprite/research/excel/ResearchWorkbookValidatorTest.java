package com.yuanbaomao.sellersprite.research.excel;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.research.config.ResearchProperties;
import com.yuanbaomao.sellersprite.research.service.ResearchSnapshotService;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;

class ResearchWorkbookValidatorTest {

    @TempDir
    private Path temporaryDirectory;

    @Test
    void shouldRejectDamagedWorkbook() throws Exception {
        Path damaged = temporaryDirectory.resolve("damaged.xlsx");
        Files.writeString(damaged, "not-an-xlsx");

        assertThatThrownBy(() -> new ResearchWorkbookValidator().validate(damaged))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("文件过小");
    }

    @Test
    void shouldRejectWorkbookWithMissingTemplateSheet() throws Exception {
        Path invalid = temporaryDirectory.resolve("missing-sheet.xlsx");
        ClassPathResource template = new ClassPathResource("research/templates/market-research-v1.xlsx");
        try (InputStream inputStream = template.getInputStream();
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                java.io.OutputStream outputStream = Files.newOutputStream(invalid)) {
            workbook.removeSheetAt(workbook.getNumberOfSheets() - 1);
            appendEmptyRawSheets(workbook);
            workbook.write(outputStream);
        }

        assertThatThrownBy(() -> new ResearchWorkbookValidator().validate(invalid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("页签数量");
    }

    @Test
    void shouldRejectWorkbookWithMisalignedCriticalHeader() throws Exception {
        Path invalid = temporaryDirectory.resolve("wrong-header.xlsx");
        ClassPathResource template = new ClassPathResource("research/templates/market-research-v1.xlsx");
        try (InputStream inputStream = template.getInputStream();
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                java.io.OutputStream outputStream = Files.newOutputStream(invalid)) {
            workbook.getSheet("US").getRow(0).getCell(2).setCellValue("错误列");
            appendEmptyRawSheets(workbook);
            workbook.write(outputStream);
        }

        assertThatThrownBy(() -> new ResearchWorkbookValidator().validate(invalid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("US工作表表头C1错误");
    }

    @Test
    void shouldRejectMissingRuntimeTemplate() {
        ResearchProperties properties = new ResearchProperties();
        properties.setTemplateLocation("classpath:research/templates/not-found.xlsx");
        ResearchSnapshotService snapshots = org.mockito.Mockito.mock(ResearchSnapshotService.class);
        ResearchWorkbookRenderer renderer = new ResearchWorkbookRenderer(
                properties,
                new DefaultResourceLoader(),
                snapshots,
                new ResearchWorkbookValidator(),
                new ResearchRawWorkbookWriter());
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId("job-1");

        assertThatThrownBy(() -> renderer.render(job, temporaryDirectory.resolve("report.xlsx")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("模板不存在");
    }

    private void appendEmptyRawSheets(XSSFWorkbook workbook) {
        new ResearchRawWorkbookWriter().append(workbook, List.of(), snapshot -> null);
    }
}
