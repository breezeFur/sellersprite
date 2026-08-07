package cyou.yuanbaomao.sellersprite.research.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

class ResearchSseExceptionHandlerTest {

    @Test
    void shouldHandleDisconnectedClientWithoutWritingAnotherResponse() {
        ResearchSseExceptionHandler handler = new ResearchSseExceptionHandler();
        AsyncRequestNotUsableException exception =
                new AsyncRequestNotUsableException("client disconnected");

        assertThatCode(() -> handler.handleDisconnectedClient(exception)).doesNotThrowAnyException();

        Method method = new ExceptionHandlerMethodResolver(ResearchSseExceptionHandler.class)
                .resolveMethod(exception);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(Void.TYPE);
        assertThat(ResearchSseExceptionHandler.class.getAnnotation(Order.class).value())
                .isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }
}
