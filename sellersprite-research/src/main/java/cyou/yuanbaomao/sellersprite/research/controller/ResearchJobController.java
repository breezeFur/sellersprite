package cyou.yuanbaomao.sellersprite.research.controller;

import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDownload;
import cyou.yuanbaomao.sellersprite.research.model.dto.ResearchJobCreateRequest;
import cyou.yuanbaomao.sellersprite.research.model.dto.ResearchJobPageRequest;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobCreatedVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobDetailVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobHistoryVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchNodeExecutionVo;
import cyou.yuanbaomao.sellersprite.research.service.ResearchJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Tag(name = "市场调研报告")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/market-research/jobs")
public class ResearchJobController {

    private final ResearchJobService researchJobService;

    @Operation(summary = "创建市场调研任务")
    @PostMapping
    public Result<ResearchJobCreatedVo> create(@Valid @RequestBody ResearchJobCreateRequest request) {
        return Result.success(researchJobService.create(request));
    }

    @Operation(summary = "分页查询我的全部历史报告")
    @GetMapping
    public Result<PageResult<ResearchJobHistoryVo>> page(@Valid ResearchJobPageRequest request) {
        return Result.success(researchJobService.page(request));
    }

    @Operation(summary = "查询市场调研任务进度")
    @GetMapping("/{jobId}")
    public Result<ResearchJobDetailVo> detail(@PathVariable String jobId) {
        return Result.success(researchJobService.detail(jobId));
    }

    @Operation(summary = "查询市场调研节点执行轨迹")
    @GetMapping("/{jobId}/nodes")
    public Result<List<ResearchNodeExecutionVo>> nodes(@PathVariable String jobId) {
        return Result.success(researchJobService.nodes(jobId));
    }

    @Operation(summary = "取消市场调研任务")
    @PostMapping("/{jobId}/cancel")
    public Result<Void> cancel(@PathVariable String jobId) {
        researchJobService.cancel(jobId);
        return Result.success();
    }

    @Operation(summary = "重试失败的市场调研任务")
    @PostMapping("/{jobId}/retry")
    public Result<Void> retry(@PathVariable String jobId) {
        researchJobService.retry(jobId);
        return Result.success();
    }

    @Operation(summary = "下载市场调研任务的指定鉴权附件")
    @GetMapping("/{jobId}/artifacts/{artifactId}/download")
    public ResponseEntity<StreamingResponseBody> downloadArtifact(
            @PathVariable String jobId, @PathVariable String artifactId) {
        return downloadResponse(researchJobService.downloadArtifact(jobId, artifactId));
    }

    private ResponseEntity<StreamingResponseBody> downloadResponse(ResearchDownload download) {
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(download.fileName(), StandardCharsets.UTF_8)
                .build();
        StreamingResponseBody body = outputStream -> {
            try (InputStream inputStream = download.resource().getInputStream()) {
                inputStream.transferTo(outputStream);
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.mediaType()))
                .contentLength(download.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(body);
    }
}
