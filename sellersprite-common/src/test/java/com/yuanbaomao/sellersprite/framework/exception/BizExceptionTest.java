package com.yuanbaomao.sellersprite.framework.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import org.junit.jupiter.api.Test;

class BizExceptionTest {

    @Test
    void shouldExposeResultCodeAndMessage() {
        BizException exception = new BizException(ResultCode.USER_NOT_FOUND);

        assertThat(exception.getErrorCode()).isEqualTo(ResultCode.USER_NOT_FOUND);
        assertThat(exception.getCode()).isEqualTo("D404");
        assertThat(exception.getMessage()).isEqualTo("用户不存在");
    }
}
