package cyou.yuanbaomao.sellersprite.api.tool.validation;

import org.springframework.util.StringUtils;

import cyou.yuanbaomao.sellersprite.api.tool.model.dto.OcrRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * OCR 图像来源条件校验器。
 */
public class OcrSourceValidator implements ConstraintValidator<ValidOcrSource, OcrRequest> {

    private static final int REMOTE_URL_TYPE = 0;
    private static final int BASE64_TYPE = 1;
    private static final int FILE_TYPE = 2;

    @Override
    public boolean isValid(OcrRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getType() == null) {
            return true;
        }
        return switch (request.getType()) {
            case REMOTE_URL_TYPE -> StringUtils.hasText(request.getUrl());
            case BASE64_TYPE -> StringUtils.hasText(request.getBase64());
            case FILE_TYPE -> request.getImage() != null && !request.getImage().isEmpty();
            default -> false;
        };
    }
}
