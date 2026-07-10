package com.yuanbaomao.sellersprite.api.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.yuanbaomao.sellersprite.api.tool.model.dto.OcrRequest;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class OcrSourceValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRequireSourceMatchingOcrType() {
        OcrRequest request = baseRequest(0);
        assertThat(validator.validate(request)).anyMatch(error -> error.getMessage().contains("OCR type"));

        request.setUrl("https://example.com/image.png");
        assertThat(validator.validate(request)).isEmpty();

        request = baseRequest(1);
        request.setBase64("aW1hZ2U=");
        assertThat(validator.validate(request)).isEmpty();

        request = baseRequest(2);
        request.setImage(new MockMultipartFile("image", "image.png", "image/png", new byte[] {1}));
        assertThat(validator.validate(request)).isEmpty();

        assertThat(validator.validate(baseRequest(3)))
                .anyMatch(error -> error.getMessage().contains("OCR type"));
    }

    private OcrRequest baseRequest(int type) {
        OcrRequest request = new OcrRequest();
        request.setType(type);
        request.setFn("CHINESE");
        return request;
    }
}
