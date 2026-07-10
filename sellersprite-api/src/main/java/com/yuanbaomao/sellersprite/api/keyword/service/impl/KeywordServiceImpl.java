// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.keyword.service.impl;

import com.yuanbaomao.sellersprite.api.client.SellerSpriteClient;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteRequestEncoder;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteResponse;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

/**
 * SellerSprite 关键词研究接口实现，所有请求统一委派给 SellerSpriteClient。
 */
@Service
@RequiredArgsConstructor
public class KeywordServiceImpl implements KeywordService {

    private final SellerSpriteClient client;

    @Override
    public KeywordResearchVo researchKeywords(KeywordResearchRequest request) {
        return client.post(SellerSpriteOperation.KEYWORD_RESEARCH, request,
                new ParameterizedTypeReference<SellerSpriteResponse<KeywordResearchVo>>() {
                });
    }

    @Override
    public List<KeywordResearchTrendVo> getKeywordResearchTrends(KeywordResearchTrendRequest request) {
        return client.post(SellerSpriteOperation.KEYWORD_RESEARCH_TRENDS, request,
                new ParameterizedTypeReference<SellerSpriteResponse<List<KeywordResearchTrendVo>>>() {
                });
    }

    @Override
    public KeywordMinerVo mineKeywords(KeywordMinerRequest request) {
        return client.post(SellerSpriteOperation.KEYWORD_MINER, request,
                new ParameterizedTypeReference<SellerSpriteResponse<KeywordMinerVo>>() {
                });
    }

    @Override
    public TrafficKeywordExtendVo extendTrafficKeywords(TrafficKeywordExtendRequest request) {
        return client.post(SellerSpriteOperation.KEYWORD_TRAFFIC_EXTEND, request,
                new ParameterizedTypeReference<SellerSpriteResponse<TrafficKeywordExtendVo>>() {
                });
    }

    @Override
    public AbaWeeklyResearchVo researchAbaWeekly(AbaWeeklyResearchRequest request) {
        return client.post(SellerSpriteOperation.ABA_RESEARCH_WEEKLY, request,
                new ParameterizedTypeReference<SellerSpriteResponse<AbaWeeklyResearchVo>>() {
                });
    }

    @Override
    public AbaMonthlyResearchVo researchAbaMonthly(AbaMonthlyResearchRequest request) {
        return client.post(SellerSpriteOperation.ABA_RESEARCH_MONTHLY, request,
                new ParameterizedTypeReference<SellerSpriteResponse<AbaMonthlyResearchVo>>() {
                });
    }

    @Override
    public List<AbaKeywordTrendVo> getAbaKeywordTrends(AbaKeywordTrendRequest request) {
        return client.post(SellerSpriteOperation.ABA_RESEARCH_TRENDS, request,
                new ParameterizedTypeReference<SellerSpriteResponse<List<AbaKeywordTrendVo>>>() {
                });
    }

    @Override
    public GoogleTrendVo getGoogleTrends(GoogleTrendRequest request) {
        return client.get(SellerSpriteOperation.GOOGLE_TRENDS,
                Map.of(), SellerSpriteRequestEncoder.toQuery(request, Set.of()),
                new ParameterizedTypeReference<SellerSpriteResponse<GoogleTrendVo>>() {
                });
    }

    @Override
    public KeywordOrderVo reverseOrderKeywords(KeywordOrderRequest request) {
        return client.post(SellerSpriteOperation.KEYWORD_ORDER, request,
                new ParameterizedTypeReference<SellerSpriteResponse<KeywordOrderVo>>() {
                });
    }

}
