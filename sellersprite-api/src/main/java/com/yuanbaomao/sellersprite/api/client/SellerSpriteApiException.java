package com.yuanbaomao.sellersprite.api.client;

import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.sellersprite.common.result.ResultCode;

import lombok.Getter;

/**
 * SellerSprite 外部调用异常，保留可诊断的上游错误码和请求 ID。
 */
@Getter
public class SellerSpriteApiException extends BizException {

    private final String providerCode;
    private final String requestId;

    public SellerSpriteApiException(ResultCode resultCode, String providerCode, String requestId, Throwable cause) {
        this(resultCode, resultCode.getMessage(), providerCode, requestId, cause);
    }

    public SellerSpriteApiException(ResultCode resultCode, String message, String providerCode, String requestId,
            Throwable cause) {
        super(resultCode, message);
        this.providerCode = providerCode;
        this.requestId = requestId;
        if (cause != null) {
            initCause(cause);
        }
    }
}
