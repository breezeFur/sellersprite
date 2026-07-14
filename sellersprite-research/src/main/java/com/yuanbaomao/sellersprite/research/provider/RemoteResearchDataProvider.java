package com.yuanbaomao.sellersprite.research.provider;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.util.StringUtils;

import com.yuanbaomao.sellersprite.api.account.model.vo.VisitsVo;
import com.yuanbaomao.sellersprite.api.account.service.AccountService;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import com.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import com.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchRequest;
import com.yuanbaomao.sellersprite.api.keyword.model.vo.KeywordResearchVo;
import com.yuanbaomao.sellersprite.api.keyword.service.KeywordService;
import com.yuanbaomao.sellersprite.api.product.model.dto.ProductResearchRequest;
import com.yuanbaomao.sellersprite.api.product.model.vo.ProductResearchVo;
import com.yuanbaomao.sellersprite.api.product.service.ProductService;
import com.yuanbaomao.sellersprite.api.review.model.dto.ReviewListRequest;
import com.yuanbaomao.sellersprite.api.review.model.vo.ReviewListVo;
import com.yuanbaomao.sellersprite.api.review.service.ReviewService;
import com.yuanbaomao.sellersprite.research.model.ResearchDataset;
import com.yuanbaomao.sellersprite.research.model.ResearchInput;
import com.yuanbaomao.sellersprite.research.model.ResearchSourceMode;

import tools.jackson.databind.ObjectMapper;

/**
 * 通过项目现有强类型 SellerSprite Service 采集远端数据的 Provider。
 */
public class RemoteResearchDataProvider implements ResearchDataProvider {

    private static final String QUOTA_DATASET_CODE = "quota.visits";
    private static final String PRODUCT_DATASET_CODE = "products";
    private static final String KEYWORD_DATASET_CODE = "keywords";
    private static final String REVIEW_DATASET_CODE_PREFIX = "reviews.";
    private static final int PRODUCT_PAGE_SIZE = 50;
    private static final int KEYWORD_PAGE_SIZE = 15;
    private static final int REVIEW_PAGE_SIZE = 10;

    private final ObjectMapper objectMapper;
    private final AccountService accountService;
    private final ProductService productService;
    private final KeywordService keywordService;
    private final ReviewService reviewService;

    public RemoteResearchDataProvider(
            ObjectMapper objectMapper,
            AccountService accountService,
            ProductService productService,
            KeywordService keywordService,
            ReviewService reviewService) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper 不能为空");
        this.accountService = Objects.requireNonNull(accountService, "accountService 不能为空");
        this.productService = Objects.requireNonNull(productService, "productService 不能为空");
        this.keywordService = Objects.requireNonNull(keywordService, "keywordService 不能为空");
        this.reviewService = Objects.requireNonNull(reviewService, "reviewService 不能为空");
    }

    @Override
    public ResearchSourceMode sourceMode() {
        return ResearchSourceMode.REMOTE;
    }

    @Override
    public List<ResearchDataset> checkQuota(ResearchInput input) {
        resolveMarketplace(input);
        VisitsVo visits = accountService.getVisits();
        if (visits == null || visits.getDetails() == null || visits.getDetails().isNull()) {
            throw new IllegalStateException("SellerSprite 配额接口返回空响应");
        }
        return List.of(toDataset(
                QUOTA_DATASET_CODE,
                SellerSpriteOperation.ACCOUNT_VISITS,
                visits,
                1));
    }

    @Override
    public List<ResearchDataset> collectMarketAndProducts(ResearchInput input) {
        SellerSpriteMarketplace marketplace = resolveMarketplace(input);
        ProductResearchRequest request = new ProductResearchRequest();
        request.setMarketplace(marketplace);
        request.setKeyword(input.getKeyword());
        request.setPage(1);
        request.setSize(PRODUCT_PAGE_SIZE);

        ProductResearchVo response = productService.researchProducts(request);
        return List.of(toDataset(
                PRODUCT_DATASET_CODE,
                SellerSpriteOperation.PRODUCT_RESEARCH,
                response,
                itemCount(response == null ? null : response.getItems())));
    }

    @Override
    public List<ResearchDataset> collectKeywords(ResearchInput input) {
        SellerSpriteMarketplace marketplace = resolveMarketplace(input);
        KeywordResearchRequest request = new KeywordResearchRequest();
        request.setMarketplace(marketplace);
        request.setKeywords(input.getKeyword());
        request.setPage(1);
        request.setSize(KEYWORD_PAGE_SIZE);

        KeywordResearchVo response = keywordService.researchKeywords(request);
        return List.of(toDataset(
                KEYWORD_DATASET_CODE,
                SellerSpriteOperation.KEYWORD_RESEARCH,
                response,
                itemCount(response == null ? null : response.getItems())));
    }

    @Override
    public List<ResearchDataset> collectReviews(ResearchInput input) {
        SellerSpriteMarketplace marketplace = resolveMarketplace(input);
        if (input.getSeedAsins() == null || input.getSeedAsins().isEmpty()) {
            return List.of();
        }
        return input.getSeedAsins().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .map(asin -> collectReviews(marketplace, asin))
                .toList();
    }

    private ResearchDataset collectReviews(SellerSpriteMarketplace marketplace, String asin) {
        ReviewListRequest request = new ReviewListRequest();
        request.setMarketplace(marketplace);
        request.setAsin(asin);
        request.setPage(1);
        request.setSize(REVIEW_PAGE_SIZE);

        ReviewListVo response = reviewService.listReviews(request);
        return toDataset(
                REVIEW_DATASET_CODE_PREFIX + asin,
                SellerSpriteOperation.REVIEW_LIST,
                response,
                itemCount(response == null ? null : response.getItems()));
    }

    private ResearchDataset toDataset(
            String datasetCode,
            SellerSpriteOperation operation,
            Object response,
            int recordCount) {
        if (response == null) {
            throw new IllegalStateException("SellerSprite 返回空响应: " + operation.name());
        }
        return new ResearchDataset(
                datasetCode,
                operation.name(),
                objectMapper.valueToTree(response),
                recordCount);
    }

    private int itemCount(List<?> items) {
        return items == null ? 0 : items.size();
    }

    private SellerSpriteMarketplace resolveMarketplace(ResearchInput input) {
        if (input == null
                || !StringUtils.hasText(input.getJobId())
                || !StringUtils.hasText(input.getMarketplace())
                || !StringUtils.hasText(input.getKeyword())) {
            throw new IllegalArgumentException("市场调研输入的 jobId、marketplace 和 keyword 不能为空");
        }
        try {
            return SellerSpriteMarketplace.valueOf(input.getMarketplace().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("不支持的 SellerSprite 市场编码: " + input.getMarketplace(), exception);
        }
    }
}
