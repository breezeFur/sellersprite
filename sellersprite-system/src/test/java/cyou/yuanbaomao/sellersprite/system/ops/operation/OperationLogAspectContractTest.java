package cyou.yuanbaomao.sellersprite.system.ops.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import cyou.yuanbaomao.base.context.RequestContext;
import cyou.yuanbaomao.log.aspect.OperationLogAspect;
import cyou.yuanbaomao.log.autoconfigure.LogProperties;
import cyou.yuanbaomao.log.core.OperationLogRecord;
import cyou.yuanbaomao.log.core.OperationLogSanitizer;
import cyou.yuanbaomao.log.core.OperationLogSink;
import io.swagger.v3.oas.annotations.Operation;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletRequestAttributes;

class OperationLogAspectContractTest {

    @AfterEach
    void tearDown() {
        cyou.yuanbaomao.base.context.RequestContextHolder.clear();
        org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldRecordSuccessfulManagementWriteWithCostAndTraceId() throws Throwable {
        OperationLogSink sink = mock(OperationLogSink.class);
        OperationLogAspect aspect = aspect(sink);
        ProceedingJoinPoint joinPoint = joinPoint("saved");
        Operation operation = operation("系统管理", "新增用户");
        bindRequest("POST", "/api/users", "trace-1");

        Object result = aspect.aroundOperation(joinPoint, operation);

        assertThat(result).isEqualTo("saved");
        ArgumentCaptor<OperationLogRecord> captor = ArgumentCaptor.forClass(OperationLogRecord.class);
        verify(sink).save(captor.capture());
        OperationLogRecord record = captor.getValue();
        assertThat(record.getModuleName()).isEqualTo("系统管理");
        assertThat(record.getOperationName()).isEqualTo("新增用户");
        assertThat(record.getOperationType()).isEqualTo("CREATE");
        assertThat(record.isSuccess()).isTrue();
        assertThat(record.getDurationMillis()).isGreaterThanOrEqualTo(0L);
        assertThat(record.getTraceId()).isEqualTo("trace-1");
        assertThat(record.getRequestPayload()).contains("******").doesNotContain("secret");
    }

    @Test
    void shouldRecordFailedManagementWriteAndRethrow() throws Throwable {
        OperationLogSink sink = mock(OperationLogSink.class);
        OperationLogAspect aspect = aspect(sink);
        ProceedingJoinPoint joinPoint = joinPoint(null);
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("保存失败"));
        bindRequest("DELETE", "/api/users/user-1", "trace-2");

        assertThatThrownBy(() -> aspect.aroundOperation(
                joinPoint, operation("系统管理", "删除用户")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("保存失败");

        ArgumentCaptor<OperationLogRecord> captor = ArgumentCaptor.forClass(OperationLogRecord.class);
        verify(sink).save(captor.capture());
        OperationLogRecord record = captor.getValue();
        assertThat(record.getOperationType()).isEqualTo("DELETE");
        assertThat(record.isSuccess()).isFalse();
        assertThat(record.getResponseStatus()).isEqualTo(500);
        assertThat(record.getExceptionMessage()).isEqualTo("保存失败");
        assertThat(record.getTraceId()).isEqualTo("trace-2");
    }

    private OperationLogAspect aspect(OperationLogSink sink) {
        return new OperationLogAspect(sink, new OperationLogSanitizer(), new ObjectMapper(), new LogProperties());
    }

    private ProceedingJoinPoint joinPoint(Object result) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(joinPoint.proceed()).thenReturn(result);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringTypeName()).thenReturn("UserController");
        when(signature.getDeclaringType()).thenReturn(UserControllerMarker.class);
        when(signature.getName()).thenReturn("save");
        when(joinPoint.getArgs()).thenReturn(new Object[] {Map.of("password", "secret")});
        return joinPoint;
    }

    private Operation operation(String module, String summary) {
        Operation operation = mock(Operation.class);
        when(operation.tags()).thenReturn(new String[] {module});
        when(operation.summary()).thenReturn(summary);
        return operation;
    }

    private void bindRequest(String method, String uri, String traceId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(uri);
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "JUnit");
        org.springframework.web.context.request.RequestContextHolder
                .setRequestAttributes(new ServletRequestAttributes(request));
        cyou.yuanbaomao.base.context.RequestContextHolder.set(RequestContext.builder()
                .userId("user-1")
                .username("yuanbao")
                .traceId(traceId)
                .build());
    }

    private static final class UserControllerMarker {
    }
}
