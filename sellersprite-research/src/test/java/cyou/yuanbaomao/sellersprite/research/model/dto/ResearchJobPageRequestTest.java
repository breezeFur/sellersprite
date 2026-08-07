package cyou.yuanbaomao.sellersprite.research.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class ResearchJobPageRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldUseHistoryPageDefaults() {
        ResearchJobPageRequest request = new ResearchJobPageRequest();

        assertThat(request.getCurrent()).isEqualTo(1L);
        assertThat(request.getSize()).isEqualTo(20L);
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void shouldRejectOversizedPageAndInvalidMonth() {
        ResearchJobPageRequest request = new ResearchJobPageRequest();
        request.setSize(101L);
        request.setMonth("2026-13");

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("size", "month");
    }
}
