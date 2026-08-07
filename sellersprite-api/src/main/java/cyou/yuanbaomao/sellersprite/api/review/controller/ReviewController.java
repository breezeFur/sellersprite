// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.review.controller;

import cyou.yuanbaomao.base.result.Result;
import cyou.yuanbaomao.sellersprite.api.review.model.dto.ReviewListRequest;
import cyou.yuanbaomao.sellersprite.api.review.model.vo.ReviewListVo;
import cyou.yuanbaomao.sellersprite.api.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "SellerSprite 评论分析", description = "SellerSprite 评论分析分类接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellersprite/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "查评论", description = "通过统一 SellerSpriteClient 调用 /v1/review")
    @PostMapping("/search")
    public Result<ReviewListVo> listReviews(@Valid @RequestBody ReviewListRequest request) {
        return Result.success(reviewService.listReviews(request));
    }

}
