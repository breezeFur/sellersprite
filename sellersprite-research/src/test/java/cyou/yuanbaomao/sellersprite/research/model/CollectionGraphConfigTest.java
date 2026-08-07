package cyou.yuanbaomao.sellersprite.research.model;

import static org.assertj.core.api.Assertions.assertThat;

import cyou.yuanbaomao.sellersprite.api.common.enums.SellerSpriteMarketplace;
import cyou.yuanbaomao.sellersprite.research.model.dto.ResearchJobCreateRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class CollectionGraphConfigTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldAcceptDefaultCollectionConfig() {
        ResearchJobCreateRequest request = validRequest();

        assertThat(validator.validate(request)).isEmpty();
        assertThat(request.getCollectionConfig()
                        .getCollectProducts()
                        .getPagination()
                        .getPageSize())
                .isEqualTo(100);
        assertThat(request.getCollectionConfig()
                        .getCollectMarketSalesTrend()
                .getMonthCount())
                .isEqualTo(12);
        assertThat(request.getCollectionConfig()
                .getCollectReviews()
                        .getPagination()
                        .getTargetCountPerAsin())
                .isEqualTo(20);
    }

    @Test
    void shouldRejectOnlyExplicitCollectionBoundaries() {
        ResearchJobCreateRequest request = validRequest();
        request.getCollectionConfig()
                .getCollectReviews()
                .getPagination()
                .setPageSize(11);
        request.getCollectionConfig()
                .getCollectReviews()
                .getPagination()
                .setTargetCountPerAsin(21);
        request.getCollectionConfig().getCollectMarketSalesTrend().setMonthCount(0);
        request.getCollectionConfig().getCollectKeywordDemandTrend().setTopN(0);

        assertThat(validator.validate(request))
                .hasSize(3)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains(
                        "collectionConfig.collectReviews.pagination.pageSize",
                        "collectionConfig.collectReviews.pagination.targetCountPerAsin",
                        "collectionConfig.collectMarketSalesTrend.monthCount");
    }

    @Test
    void shouldRequireCollectionConfig() {
        ResearchJobCreateRequest request = validRequest();
        request.setCollectionConfig(null);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("collectionConfig");
    }

    private ResearchJobCreateRequest validRequest() {
        ResearchJobCreateRequest request = new ResearchJobCreateRequest();
        request.setReportName("美国站收纳盒调研");
        request.setMarketplace(SellerSpriteMarketplace.US);
        request.setNodeIdPath("1055398:1063306");
        request.setMonth("2026-07");
        request.setCollectionConfig(new CollectionGraphConfig());
        return request;
    }
}
