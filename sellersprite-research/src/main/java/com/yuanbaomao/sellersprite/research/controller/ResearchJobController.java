package com.yuanbaomao.sellersprite.research.controller;

import com.yuanbaomao.base.result.Result;
import com.yuanbaomao.sellersprite.research.model.ResearchDownload;
import com.yuanbaomao.sellersprite.research.model.dto.ResearchJobCreateRequest;
import com.yuanbaomao.sellersprite.research.model.vo.ResearchJobCreatedVo;
import com.yuanbaomao.sellersprite.research.model.vo.ResearchJobDetailVo;
import com.yuanbaomao.sellersprite.research.service.ResearchJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
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

    @Operation(summary = "查询市场调研任务进度")
    @GetMapping("/{jobId}")
    public Result<ResearchJobDetailVo> detail(@PathVariable String jobId) {
        return Result.success(researchJobService.detail(jobId));
    }

    @Operation(summary = "下载已完成的市场调研Excel")
    @GetMapping("/{jobId}/download")
    public ResponseEntity<Resource> download(@PathVariable String jobId) {
        ResearchDownload download = researchJobService.download(jobId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(download.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.mediaType()))
                .contentLength(download.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(download.resource());
    }
}
