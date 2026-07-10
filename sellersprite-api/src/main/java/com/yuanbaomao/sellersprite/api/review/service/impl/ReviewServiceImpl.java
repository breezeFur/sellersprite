// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.review.service.impl;

import com.yuanbaomao.sellersprite.api.client.SellerSpriteClient;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteRequestEncoder;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteResponse;
import com.yuanbaomao.sellersprite.api.review.model.dto.ReviewListRequest;
import com.yuanbaomao.sellersprite.api.review.model.vo.ReviewListVo;
import com.yuanbaomao.sellersprite.api.review.service.ReviewService;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

/**
 * SellerSprite 评论分析接口实现，所有请求统一委派给 SellerSpriteClient。
 */
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final SellerSpriteClient client;

    @Override
    public ReviewListVo listReviews(ReviewListRequest request) {
        return client.post(SellerSpriteOperation.REVIEW_LIST, request,
                new ParameterizedTypeReference<SellerSpriteResponse<ReviewListVo>>() {
                });
    }

}
