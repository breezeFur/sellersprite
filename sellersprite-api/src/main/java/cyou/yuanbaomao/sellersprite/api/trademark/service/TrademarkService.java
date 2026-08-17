// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.trademark.service;

import cyou.yuanbaomao.sellersprite.api.trademark.model.dto.TrademarkListRequest;
import cyou.yuanbaomao.sellersprite.api.trademark.model.dto.TrademarkStatsRequest;
import cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkDetailVo;
import cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkListVo;
import cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkRangeVo;
import cyou.yuanbaomao.sellersprite.api.trademark.model.vo.TrademarkStatsVo;
import java.util.List;

/**
 * SellerSprite 全球商标接口封装。
 */
public interface TrademarkService {

    /**
     * 全球商标库-数据范围。
     *
     * <p>调用 SellerSprite 官方 GET /v1/global/brand/range，认证、超时和错误转换由统一 Client 处理。</p>
     * @return 全球商标库-数据范围的强类型响应数据
     */
    List<TrademarkRangeVo> getBrandRange();

    /**
     * 全球商标库-详情。
     *
     * <p>调用 SellerSprite 官方 GET /v1/global/brand/detail，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 全球商标库-详情的强类型请求参数
     * @return 全球商标库-详情的强类型响应数据
     */
    TrademarkDetailVo getBrandDetail(String office, String brandId);

    /**
     * 全球商标库-列表。
     *
     * <p>调用 SellerSprite 官方 POST /v1/global/brand/list，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 全球商标库-列表的强类型请求参数
     * @return 全球商标库-列表的强类型响应数据
     */
    TrademarkListVo listBrands(TrademarkListRequest request);

    /**
     * 全球商标库-统计。
     *
     * <p>调用 SellerSprite 官方 POST /v1/global/brand/stats，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 全球商标库-统计的强类型请求参数
     * @return 全球商标库-统计的强类型响应数据
     */
    TrademarkStatsVo getBrandStats(TrademarkStatsRequest request);

}
