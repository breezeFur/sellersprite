package com.yuanbaomao.sellersprite.system.ops.operation.service;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.system.ops.operation.model.dto.OperationLogPageRequest;
import com.yuanbaomao.sellersprite.system.ops.operation.model.vo.OperationLogVo;

public interface OperationLogService {

    PageResult<OperationLogVo> page(OperationLogPageRequest request);

    OperationLogVo detail(String operationLogId);
}
