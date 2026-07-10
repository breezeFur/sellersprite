// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.market.service;

import com.yuanbaomao.sellersprite.api.market.model.dto.MarketBrandConcentrationRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketDemandTrendRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketEbcDistributionRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketGoodsConcentrationRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketPriceDistributionRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketRatingDistributionRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketRatingsDistributionRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketResearchRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketSellerConcentrationRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketSellerLocationRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketSellerTypeRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketShelfTimeRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketShelfTrendRequest;
import com.yuanbaomao.sellersprite.api.market.model.dto.MarketStatisticsRequest;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketBrandConcentrationVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketDemandTrendVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketEbcDistributionVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketGoodsConcentrationVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketPriceDistributionVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketRatingDistributionVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketRatingsDistributionVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketResearchVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerConcentrationVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerLocationVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketSellerTypeVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketShelfTimeVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketShelfTrendVo;
import com.yuanbaomao.sellersprite.api.market.model.vo.MarketStatisticsVo;
import java.util.List;

/**
 * SellerSprite 市场分析接口封装。
 */
public interface MarketService {

    /**
     * 选市场列表。
     *
     * <p>调用 SellerSprite 官方 POST /v1/market/research，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选市场列表的强类型请求参数
     * @return 选市场列表的强类型响应数据
     */
    MarketResearchVo researchMarkets(MarketResearchRequest request);

    /**
     * 选市场-统计。
     *
     * <p>调用 SellerSprite 官方 POST /v1/market/statistics，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选市场-统计的强类型请求参数
     * @return 选市场-统计的强类型响应数据
     */
    MarketStatisticsVo getMarketStatistics(MarketStatisticsRequest request);

    /**
     * 选市场-商品集中度。
     *
     * <p>调用 SellerSprite 官方 POST /v1/market/goods，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选市场-商品集中度的强类型请求参数
     * @return 选市场-商品集中度的强类型响应数据
     */
    List<MarketGoodsConcentrationVo> getGoodsConcentration(MarketGoodsConcentrationRequest request);

    /**
     * 选市场-品牌集中度。
     *
     * <p>调用 SellerSprite 官方 POST /v1/market/brand，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选市场-品牌集中度的强类型请求参数
     * @return 选市场-品牌集中度的强类型响应数据
     */
    List<MarketBrandConcentrationVo> getBrandConcentration(MarketBrandConcentrationRequest request);

    /**
     * 选市场-卖家所属地分布。
     *
     * <p>调用 SellerSprite 官方 POST /v1/market/seller/location，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选市场-卖家所属地分布的强类型请求参数
     * @return 选市场-卖家所属地分布的强类型响应数据
     */
    List<MarketSellerLocationVo> getSellerLocationDistribution(MarketSellerLocationRequest request);

    /**
     * 选市场-卖家集中度。
     *
     * <p>调用 SellerSprite 官方 POST /v1/market/seller，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选市场-卖家集中度的强类型请求参数
     * @return 选市场-卖家集中度的强类型响应数据
     */
    List<MarketSellerConcentrationVo> getSellerConcentration(MarketSellerConcentrationRequest request);

    /**
     * 选市场-卖家类型分布。
     *
     * <p>调用 SellerSprite 官方 POST /v1/market/seller/type，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选市场-卖家类型分布的强类型请求参数
     * @return 选市场-卖家类型分布的强类型响应数据
     */
    List<MarketSellerTypeVo> getSellerTypeDistribution(MarketSellerTypeRequest request);

    /**
     * 选市场-商品需求趋势。
     *
     * <p>调用 SellerSprite 官方 POST /v1/market/performance，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选市场-商品需求趋势的强类型请求参数
     * @return 选市场-商品需求趋势的强类型响应数据
     */
    MarketDemandTrendVo getDemandTrend(MarketDemandTrendRequest request);

    /**
     * 选市场-上架时间分布。
     *
     * <p>调用 SellerSprite 官方 POST /v1/market/shelf/time，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选市场-上架时间分布的强类型请求参数
     * @return 选市场-上架时间分布的强类型响应数据
     */
    List<MarketShelfTimeVo> getShelfTimeDistribution(MarketShelfTimeRequest request);

    /**
     * 选市场-上架趋势分布。
     *
     * <p>调用 SellerSprite 官方 POST /v1/market/shelf/trend，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选市场-上架趋势分布的强类型请求参数
     * @return 选市场-上架趋势分布的强类型响应数据
     */
    List<MarketShelfTrendVo> getShelfTrendDistribution(MarketShelfTrendRequest request);

    /**
     * 选市场-评分数分布。
     *
     * <p>调用 SellerSprite 官方 POST /v1/market/ratings，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选市场-评分数分布的强类型请求参数
     * @return 选市场-评分数分布的强类型响应数据
     */
    List<MarketRatingsDistributionVo> getRatingsDistribution(MarketRatingsDistributionRequest request);

    /**
     * 选市场-评分值分布。
     *
     * <p>调用 SellerSprite 官方 POST /v1/market/rating，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选市场-评分值分布的强类型请求参数
     * @return 选市场-评分值分布的强类型响应数据
     */
    List<MarketRatingDistributionVo> getRatingDistribution(MarketRatingDistributionRequest request);

    /**
     * 选市场-价格分布。
     *
     * <p>调用 SellerSprite 官方 POST /v1/market/price，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选市场-价格分布的强类型请求参数
     * @return 选市场-价格分布的强类型响应数据
     */
    List<MarketPriceDistributionVo> getPriceDistribution(MarketPriceDistributionRequest request);

    /**
     * 选市场-A+视频分布。
     *
     * <p>调用 SellerSprite 官方 POST /v1/market/ebc，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 选市场-A+视频分布的强类型请求参数
     * @return 选市场-A+视频分布的强类型响应数据
     */
    List<MarketEbcDistributionVo> getEbcDistribution(MarketEbcDistributionRequest request);

}
