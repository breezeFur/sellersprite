package com.yuanbaomao.sellersprite.ai.context;

import com.yuanbaomao.base.context.RequestContext;
import com.yuanbaomao.base.context.RequestContextHolder;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import org.springframework.stereotype.Component;

@Component
public class AiCurrentUser {

    public String requireUserId() {
        return RequestContextHolder.get()
                .map(RequestContext::getUserId)
                .filter(userId -> !userId.isBlank())
                .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
    }
}
