package cyou.yuanbaomao.sellersprite.research.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

/** Handles normal client disconnects without attempting a second SSE response. */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = ResearchEventController.class)
public class ResearchSseExceptionHandler {

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleDisconnectedClient(AsyncRequestNotUsableException exception) {
        log.debug("Market research SSE client disconnected: {}", exception.getMessage());
    }
}
