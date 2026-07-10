// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.review.service;

import com.yuanbaomao.sellersprite.api.review.model.dto.ReviewListRequest;
import com.yuanbaomao.sellersprite.api.review.model.vo.ReviewListVo;

/**
 * SellerSprite 评论分析接口封装。
 */
public interface ReviewService {

    /**
     * 查评论。
     *
     * <p>调用 SellerSprite 官方 POST /v1/review，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 查评论的强类型请求参数
     * @return 查评论的强类型响应数据
     */
    ReviewListVo listReviews(ReviewListRequest request);

}
