package cyou.yuanbaomao.sellersprite.api.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.AbaMonthlyResearchRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.AbaWeeklyResearchRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordOrderRequest;
import cyou.yuanbaomao.sellersprite.api.keyword.model.dto.KeywordResearchRequest;
import cyou.yuanbaomao.sellersprite.api.product.model.dto.CompetitorLookupRequest;
import cyou.yuanbaomao.sellersprite.api.review.model.dto.ReviewListRequest;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class SellerSpriteRequestDefaultTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldUseOnlyDefaultsExplicitlyDeclaredByOfficialDocumentation() {
        assertThat(new CompetitorLookupRequest().getSize()).isEqualTo(50);
        assertThat(new AbaWeeklyResearchRequest().getSize()).isEqualTo(40);
        assertThat(new AbaMonthlyResearchRequest().getSize()).isEqualTo(15);
        assertThat(new KeywordOrderRequest().getSize()).isEqualTo(50);
        assertThat(new ReviewListRequest().getSize()).isEqualTo(5);

        assertThat(new KeywordResearchRequest().getSize()).isEqualTo(15);
    }

    @Test
    void shouldEnforceDocumentedPageAndCollectionLimits() {
        CompetitorLookupRequest competitor = new CompetitorLookupRequest();
        competitor.setAsins(Collections.nCopies(41, "B0TESTASIN"));
        assertThat(validator.validate(competitor))
                .anyMatch(error -> error.getPropertyPath().toString().equals("asins"));

        KeywordOrderRequest keywordOrder = new KeywordOrderRequest();
        keywordOrder.setAsins(Collections.nCopies(21, "B0TESTASIN"));
        assertThat(validator.validate(keywordOrder))
                .anyMatch(error -> error.getPropertyPath().toString().equals("asins"));

        ReviewListRequest review = new ReviewListRequest();
        review.setSize(11);
        assertThat(validator.validate(review))
                .anyMatch(error -> error.getPropertyPath().toString().equals("size"));
    }
}
