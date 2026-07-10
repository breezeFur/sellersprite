// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.keyword.controller;

import com.yuanbaomao.base.result.Result;
import com.yuanbaomao.sellersprite.api.keyword.model.dto.AbaKeywordTrendRequest;
import com.yuanbaomao.sellersprite.api.keyword.model.dto.AbaMonthlyResearchRequest;
import com.yuanbaomao.sellersprite.api.keyword.model.dto.AbaWeeklyResearchRequest;
import com.yuanbaomao.sellersprite.api.keyword.model.dto.GoogleTrendRequest;
import com.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordMinerRequest;
import com.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordOrderRequest;
import com.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchRequest;
import com.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchTrendRequest;
import com.yuanbaomao.sellersprite.api.keyword.model.dto.TrafficKeywordExtendRequest;
import com.yuanbaomao.sellersprite.api.keyword.model.vo.AbaKeywordTrendVo;
import com.yuanbaomao.sellersprite.api.keyword.model.vo.AbaMonthlyResearchVo;
import com.yuanbaomao.sellersprite.api.keyword.model.vo.AbaWeeklyResearchVo;
import com.yuanbaomao.sellersprite.api.keyword.model.vo.GoogleTrendVo;
import com.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordMinerVo;
import com.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordOrderVo;
import com.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchTrendVo;
import com.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchVo;
import com.yuanbaomao.sellersprite.api.keyword.model.vo.TrafficKeywordExtendVo;
import com.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SellerSprite 关键词研究", description = "SellerSprite 关键词研究分类接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellersprite/keywords")
public class KeywordController {

    private final KeywordService keywordService;

    @Operation(summary = "关键词选品", description = "通过统一 SellerSpriteClient 调用 /v1/keyword-research")
    @PostMapping("/research")
    public Result<KeywordResearchVo> researchKeywords(@Valid @RequestBody KeywordResearchRequest request) {
        return Result.success(keywordService.researchKeywords(request));
    }

    @Operation(summary = "关键词选品-趋势数据", description = "通过统一 SellerSpriteClient 调用 /v1/keyword-research/trends")
    @PostMapping("/research/trends")
    public Result<List<KeywordResearchTrendVo>> getKeywordResearchTrends(@Valid @RequestBody KeywordResearchTrendRequest request) {
        return Result.success(keywordService.getKeywordResearchTrends(request));
    }

    @Operation(summary = "关键词挖掘", description = "通过统一 SellerSpriteClient 调用 /v1/keyword/miner")
    @PostMapping("/mine")
    public Result<KeywordMinerVo> mineKeywords(@Valid @RequestBody KeywordMinerRequest request) {
        return Result.success(keywordService.mineKeywords(request));
    }

    @Operation(summary = "拓展流量词", description = "通过统一 SellerSpriteClient 调用 /v1/traffic/extend")
    @PostMapping("/traffic/extend")
    public Result<TrafficKeywordExtendVo> extendTrafficKeywords(@Valid @RequestBody TrafficKeywordExtendRequest request) {
        return Result.success(keywordService.extendTrafficKeywords(request));
    }

    @Operation(summary = "ABA 数据选品-按周", description = "通过统一 SellerSpriteClient 调用 /v1/aba/research/weekly")
    @PostMapping("/aba/weekly")
    public Result<AbaWeeklyResearchVo> researchAbaWeekly(@Valid @RequestBody AbaWeeklyResearchRequest request) {
        return Result.success(keywordService.researchAbaWeekly(request));
    }

    @Operation(summary = "ABA 数据选品-按月", description = "通过统一 SellerSpriteClient 调用 /v1/aba/research/monthly")
    @PostMapping("/aba/monthly")
    public Result<AbaMonthlyResearchVo> researchAbaMonthly(@Valid @RequestBody AbaMonthlyResearchRequest request) {
        return Result.success(keywordService.researchAbaMonthly(request));
    }

    @Operation(summary = "ABA 数据选品-关键词趋势", description = "通过统一 SellerSpriteClient 调用 /v1/aba/research/trends")
    @PostMapping("/aba/trends")
    public Result<List<AbaKeywordTrendVo>> getAbaKeywordTrends(@Valid @RequestBody AbaKeywordTrendRequest request) {
        return Result.success(keywordService.getAbaKeywordTrends(request));
    }

    @Operation(summary = "谷歌趋势", description = "通过统一 SellerSpriteClient 调用 /v1/google/trends")
    @GetMapping("/google-trends")
    public Result<GoogleTrendVo> getGoogleTrends(@Valid @ModelAttribute GoogleTrendRequest request) {
        return Result.success(keywordService.getGoogleTrends(request));
    }

    @Operation(summary = "出单词反查", description = "通过统一 SellerSpriteClient 调用 /v1/keyword-order")
    @PostMapping("/order/reverse")
    public Result<KeywordOrderVo> reverseOrderKeywords(@Valid @RequestBody KeywordOrderRequest request) {
        return Result.success(keywordService.reverseOrderKeywords(request));
    }

}
