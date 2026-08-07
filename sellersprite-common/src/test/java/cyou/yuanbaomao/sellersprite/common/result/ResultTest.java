package cyou.yuanbaomao.sellersprite.common.result;

import static org.assertj.core.api.Assertions.assertThat;

import cyou.yuanbaomao.base.result.Result;
import org.junit.jupiter.api.Test;

class ResultTest {

    @Test
    void shouldUseDefaultSuccessCode() {
        Result<String> result = Result.success("ok");

        assertThat(result.getCode()).isEqualTo("00000");
        assertThat(result.getMessage()).isEqualTo("操作成功");
        assertThat(result.getData()).isEqualTo("ok");
    }

    @Test
    void shouldUseDefaultFailureCode() {
        Result<Void> result = Result.fail();

        assertThat(result.getCode()).isEqualTo("-1");
        assertThat(result.getMessage()).isEqualTo("系统异常，请稍后重试");
        assertThat(result.getData()).isNull();
    }

    @Test
    void shouldWrapResultCodeWhenFailed() {
        Result<Void> result = Result.fail(ResultCode.USER_NOT_FOUND);

        assertThat(result.getCode()).isEqualTo("D404");
        assertThat(result.getMessage()).isEqualTo("用户不存在");
        assertThat(result.getData()).isNull();
    }

    @Test
    void shouldUseCustomFailureCodeAndMessage() {
        Result<Void> result = Result.fail("CUSTOM_001", "自定义错误");

        assertThat(result.getCode()).isEqualTo("CUSTOM_001");
        assertThat(result.getMessage()).isEqualTo("自定义错误");
        assertThat(result.getData()).isNull();
    }

    @Test
    void shouldExposeStableConsoleErrorCodes() {
        assertThat(ResultCode.RESOURCE_CONFLICT.getCode()).isEqualTo("D409");
        assertThat(ResultCode.SESSION_EXPIRED.getCode()).isEqualTo("A401");
        assertThat(ResultCode.REFRESH_TOKEN_REUSED.getCode()).isEqualTo("A401");
        assertThat(ResultCode.CURRENT_USER_OPERATION_FORBIDDEN.getCode()).isEqualTo("A403");
        assertThat(ResultCode.AI_MESSAGE_NOT_RETRYABLE.getCode()).isEqualTo("B409");
    }
}
