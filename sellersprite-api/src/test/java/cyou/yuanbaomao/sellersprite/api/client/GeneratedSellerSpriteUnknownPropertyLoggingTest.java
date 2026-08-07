package cyou.yuanbaomao.sellersprite.api.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinCouponTrendVo;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(OutputCaptureExtension.class)
class GeneratedSellerSpriteUnknownPropertyLoggingTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldWarnWithValueWhenResponseContainsUnknownProperty(CapturedOutput output)
            throws IOException {
        AsinCouponTrendVo response = objectMapper.readValue(
                "{\"unexpectedField\":\"hiddenValue\"}", AsinCouponTrendVo.class);

        assertThat(response.getAdditionalProperties()).containsKey("unexpectedField");
        assertThat(output.getOut() + output.getErr())
                .contains("WARN")
                .contains("modelType=cyou.yuanbaomao.sellersprite.api.asin.model.vo.AsinCouponTrendVo")
                .contains("fieldName=unexpectedField")
                .contains("fieldValue=\"hiddenValue\"");
    }
}
