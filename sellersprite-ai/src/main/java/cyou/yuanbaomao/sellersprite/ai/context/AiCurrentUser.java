package cyou.yuanbaomao.sellersprite.ai.context;

import cyou.yuanbaomao.base.context.RequestContext;
import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
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
