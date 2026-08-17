package cyou.yuanbaomao.sellersprite.framework.web;

import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.base.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class BizExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException exception) {
        if (exception.getCause() == null) {
            log.warn("业务异常 code={}, message={}", exception.getCode(), exception.getMessage());
        } else {
            log.error("业务异常包含原始异常 code={}, message={}", exception.getCode(), exception.getMessage(), exception);
        }
        return Result.<Void>fail(exception.getCode(), exception.getMessage())
                .withTraceId(RequestContextHolder.currentTraceId());
    }
}
