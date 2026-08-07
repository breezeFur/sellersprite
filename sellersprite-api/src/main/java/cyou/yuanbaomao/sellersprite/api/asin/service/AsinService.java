// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.asin.service;

import cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinCouponTrendRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinDetailRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinSalesPredictionRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinSalesTrendRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.AsinWithCouponTrendRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.BsrSalesPredictionRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.dto.KeepaTrendRequest;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinCouponTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinDetailVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesPredictionVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinSalesTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinWithCouponTrendVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.BsrSalesPredictionVo;
import cyou.yuanbaomao.sellersprite.api.asin.model.vo.KeepaTrendVo;
import java.util.List;

/**
 * SellerSprite ASIN 分析接口封装。
 */
public interface AsinService {

    /**
     * ASIN 详情。
     *
     * <p>调用 SellerSprite 官方 GET /v1/asin/{marketplace}/{asin}，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request ASIN 详情的强类型请求参数
     * @return ASIN 详情的强类型响应数据
     */
    AsinDetailVo getAsinDetail(AsinDetailRequest request);

    /**
     * ASIN优惠趋势。
     *
     * <p>调用 SellerSprite 官方 GET /v1/asin/{marketplace}/{asin}/coupon-trend，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request ASIN优惠趋势的强类型请求参数
     * @return ASIN优惠趋势的强类型响应数据
     */
    List<AsinCouponTrendVo> getCouponTrend(AsinCouponTrendRequest request);

    /**
     * ASIN详情及优惠趋势。
     *
     * <p>调用 SellerSprite 官方 GET /v1/asin/{marketplace}/{asin}/with-coupon-trend，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request ASIN详情及优惠趋势的强类型请求参数
     * @return ASIN详情及优惠趋势的强类型响应数据
     */
    AsinWithCouponTrendVo getAsinWithCouponTrend(AsinWithCouponTrendRequest request);

    /**
     * ASIN 销量趋势。
     *
     * <p>调用 SellerSprite 官方 GET /v1/asin/{marketplace}/{asin}/sales-trend，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request ASIN 销量趋势的强类型请求参数
     * @return ASIN 销量趋势的强类型响应数据
     */
    AsinSalesTrendVo getSalesTrend(AsinSalesTrendRequest request);

    /**
     * ASIN 销量预测。
     *
     * <p>调用 SellerSprite 官方 GET /v1/sales/prediction/asin，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request ASIN 销量预测的强类型请求参数
     * @return ASIN 销量预测的强类型响应数据
     */
    AsinSalesPredictionVo predictAsinSales(AsinSalesPredictionRequest request);

    /**
     * BSR销量预测。
     *
     * <p>调用 SellerSprite 官方 GET /v1/sales/prediction/bsr，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request BSR销量预测的强类型请求参数
     * @return BSR销量预测的强类型响应数据
     */
    BsrSalesPredictionVo predictBsrSales(BsrSalesPredictionRequest request);

    /**
     * 商品趋势详情(keepa)。
     *
     * <p>调用 SellerSprite 官方 GET /v1/keepa/{marketplace}/{asin}，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 商品趋势详情(keepa)的强类型请求参数
     * @return 商品趋势详情(keepa)的强类型响应数据
     */
    KeepaTrendVo getKeepaTrend(KeepaTrendRequest request);

}
