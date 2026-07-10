// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.keyword.service;

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
import java.util.List;

/**
 * SellerSprite 关键词研究接口封装。
 */
public interface KeywordService {

    /**
     * 关键词选品。
     *
     * <p>调用 SellerSprite 官方 POST /v1/keyword-research，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 关键词选品的强类型请求参数
     * @return 关键词选品的强类型响应数据
     */
    KeywordResearchVo researchKeywords(KeywordResearchRequest request);

    /**
     * 关键词选品-趋势数据。
     *
     * <p>调用 SellerSprite 官方 POST /v1/keyword-research/trends，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 关键词选品-趋势数据的强类型请求参数
     * @return 关键词选品-趋势数据的强类型响应数据
     */
    List<KeywordResearchTrendVo> getKeywordResearchTrends(KeywordResearchTrendRequest request);

    /**
     * 关键词挖掘。
     *
     * <p>调用 SellerSprite 官方 POST /v1/keyword/miner，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 关键词挖掘的强类型请求参数
     * @return 关键词挖掘的强类型响应数据
     */
    KeywordMinerVo mineKeywords(KeywordMinerRequest request);

    /**
     * 拓展流量词。
     *
     * <p>调用 SellerSprite 官方 POST /v1/traffic/extend，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 拓展流量词的强类型请求参数
     * @return 拓展流量词的强类型响应数据
     */
    TrafficKeywordExtendVo extendTrafficKeywords(TrafficKeywordExtendRequest request);

    /**
     * ABA 数据选品-按周。
     *
     * <p>调用 SellerSprite 官方 POST /v1/aba/research/weekly，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request ABA 数据选品-按周的强类型请求参数
     * @return ABA 数据选品-按周的强类型响应数据
     */
    AbaWeeklyResearchVo researchAbaWeekly(AbaWeeklyResearchRequest request);

    /**
     * ABA 数据选品-按月。
     *
     * <p>调用 SellerSprite 官方 POST /v1/aba/research/monthly，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request ABA 数据选品-按月的强类型请求参数
     * @return ABA 数据选品-按月的强类型响应数据
     */
    AbaMonthlyResearchVo researchAbaMonthly(AbaMonthlyResearchRequest request);

    /**
     * ABA 数据选品-关键词趋势。
     *
     * <p>调用 SellerSprite 官方 POST /v1/aba/research/trends，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request ABA 数据选品-关键词趋势的强类型请求参数
     * @return ABA 数据选品-关键词趋势的强类型响应数据
     */
    List<AbaKeywordTrendVo> getAbaKeywordTrends(AbaKeywordTrendRequest request);

    /**
     * 谷歌趋势。
     *
     * <p>调用 SellerSprite 官方 GET /v1/google/trends，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 谷歌趋势的强类型请求参数
     * @return 谷歌趋势的强类型响应数据
     */
    GoogleTrendVo getGoogleTrends(GoogleTrendRequest request);

    /**
     * 出单词反查。
     *
     * <p>调用 SellerSprite 官方 POST /v1/keyword-order，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 出单词反查的强类型请求参数
     * @return 出单词反查的强类型响应数据
     */
    KeywordOrderVo reverseOrderKeywords(KeywordOrderRequest request);

}
