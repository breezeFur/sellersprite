package cyou.yuanbaomao.sellersprite.api.tool.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * 校验 OCR type 与图像来源字段的一致性。
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OcrSourceValidator.class)
public @interface ValidOcrSource {

    String message() default "OCR type 必须为 0、1、2，并提供对应的 url、base64 或 image";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
