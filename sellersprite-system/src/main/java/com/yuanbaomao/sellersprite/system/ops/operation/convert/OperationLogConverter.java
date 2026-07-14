package com.yuanbaomao.sellersprite.system.ops.operation.convert;

import com.yuanbaomao.log.mybatis.entity.OperationLogEntity;
import com.yuanbaomao.sellersprite.framework.security.SensitiveDataMasker;
import com.yuanbaomao.sellersprite.system.ops.operation.model.vo.OperationLogVo;

public final class OperationLogConverter {

    private OperationLogConverter() {
    }

    public static OperationLogVo toVo(OperationLogEntity entity) {
        OperationLogVo vo = new OperationLogVo();
        vo.setOperationLogId(entity.getOperationLogId());
        vo.setUserId(entity.getUserId());
        vo.setUsername(entity.getUsername());
        vo.setModuleName(entity.getModuleName());
        vo.setOperationName(entity.getOperationName());
        vo.setOperationType(entity.getOperationType());
        vo.setHttpMethod(entity.getHttpMethod());
        vo.setRequestUri(entity.getRequestUri());
        vo.setRequestParams(SensitiveDataMasker.mask(entity.getRequestParams()));
        vo.setResponsePayload(SensitiveDataMasker.mask(entity.getResponsePayload()));
        vo.setResponseStatus(entity.getResponseStatus());
        vo.setSuccess(entity.getSuccess());
        vo.setErrorMessage(SensitiveDataMasker.mask(entity.getErrorMessage()));
        vo.setClientIp(entity.getClientIp());
        vo.setUserAgent(SensitiveDataMasker.mask(entity.getUserAgent()));
        vo.setCostMs(entity.getCostMs());
        vo.setTrackId(entity.getTrackId());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
