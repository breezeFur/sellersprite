// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.traffic.service;

import com.yuanbaomao.sellersprite.api.traffic.model.dto.RelatedTrafficRequest;
import com.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficKeywordRequest;
import com.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficKeywordStatRequest;
import com.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficListingStatRequest;
import com.yuanbaomao.sellersprite.api.traffic.model.dto.TrafficSourceRequest;
import com.yuanbaomao.sellersprite.api.traffic.model.vo.RelatedTrafficVo;
import com.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordStatVo;
import com.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficKeywordVo;
import com.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficListingStatVo;
import com.yuanbaomao.sellersprite.api.traffic.model.vo.TrafficSourceVo;

/**
 * SellerSprite 流量分析接口封装。
 */
public interface TrafficService {

    /**
     * 关键词反查(流量词列表)。
     *
     * <p>调用 SellerSprite 官方 POST /v1/traffic/keyword，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 关键词反查(流量词列表)的强类型请求参数
     * @return 关键词反查(流量词列表)的强类型响应数据
     */
    TrafficKeywordVo reverseKeywords(TrafficKeywordRequest request);

    /**
     * 关联流量列表。
     *
     * <p>调用 SellerSprite 官方 POST /v1/traffic/listing/page，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 关联流量列表的强类型请求参数
     * @return 关联流量列表的强类型响应数据
     */
    RelatedTrafficVo listRelatedTraffic(RelatedTrafficRequest request);

    /**
     * 流量词统计。
     *
     * <p>调用 SellerSprite 官方 GET /v1/traffic/keyword/stat/{marketplace}/{asin}，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 流量词统计的强类型请求参数
     * @return 流量词统计的强类型响应数据
     */
    TrafficKeywordStatVo getKeywordStats(TrafficKeywordStatRequest request);

    /**
     * 关联流量统计。
     *
     * <p>调用 SellerSprite 官方 GET /v1/traffic/listing/stat/{marketplace}/{asin}，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 关联流量统计的强类型请求参数
     * @return 关联流量统计的强类型响应数据
     */
    TrafficListingStatVo getListingStats(TrafficListingStatRequest request);

    /**
     * 查流量来源(关键词流向)。
     *
     * <p>调用 SellerSprite 官方 POST /v1/traffic/source，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 查流量来源(关键词流向)的强类型请求参数
     * @return 查流量来源(关键词流向)的强类型响应数据
     */
    TrafficSourceVo getTrafficSources(TrafficSourceRequest request);

}
