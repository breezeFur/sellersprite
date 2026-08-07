package cyou.yuanbaomao.sellersprite.research.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDownload;
import cyou.yuanbaomao.sellersprite.research.service.ResearchJobService;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ResearchJobControllerTest {

    private static final String JOB_ID = "job-download-001";
    private static final String ARTIFACT_ID = "artifact-download-001";

    @Mock
    private ResearchJobService researchJobService;

    @TempDir
    private Path temporaryDirectory;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ResearchJobController(researchJobService))
                .build();
    }

    @Test
    void shouldStreamArtifactWithoutMutatingSourceFile() throws Exception {
        byte[] expected = "market-research-artifact".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Path source = temporaryDirectory.resolve("artifact.xlsx");
        Files.write(source, expected);
        when(researchJobService.downloadArtifact(JOB_ID, ARTIFACT_ID))
                .thenReturn(new ResearchDownload(
                        new FileSystemResource(source),
                        "artifact.xlsx",
                        ResearchConstants.EXCEL_MEDIA_TYPE,
                        expected.length));

        MvcResult streaming = mockMvc.perform(get(
                        "/api/market-research/jobs/{jobId}/artifacts/{artifactId}/download",
                        JOB_ID,
                        ARTIFACT_ID))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(streaming))
                .andExpect(status().isOk())
                .andExpect(content().bytes(expected));
        assertThat(Files.readAllBytes(source)).isEqualTo(expected);
    }
}
