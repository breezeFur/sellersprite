package cyou.yuanbaomao.sellersprite.system.ops.operation.service;

import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.sellersprite.system.ops.operation.model.dto.OperationLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.operation.model.vo.OperationLogVo;

public interface OperationLogService {

    PageResult<OperationLogVo> page(OperationLogPageRequest request);

    OperationLogVo detail(String operationLogId);
}
