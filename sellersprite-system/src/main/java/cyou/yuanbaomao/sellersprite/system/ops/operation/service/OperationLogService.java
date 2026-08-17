package cyou.yuanbaomao.sellersprite.system.ops.operation.service;

import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.sellersprite.system.ops.operation.model.dto.OperationLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.operation.model.vo.OperationLogVo;

public interface OperationLogService {

    YPage<OperationLogVo> page(YPage<OperationLogVo> page, OperationLogPageRequest request);

    OperationLogVo detail(String operationLogId);
}
