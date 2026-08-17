package cyou.yuanbaomao.sellersprite.system.ops.operation.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.log.mybatis.entity.OperationLogEntity;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.OperationLogQueryDao;
import cyou.yuanbaomao.sellersprite.system.ops.operation.convert.OperationLogConverter;
import cyou.yuanbaomao.sellersprite.system.ops.operation.model.dto.OperationLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.operation.model.vo.OperationLogVo;
import cyou.yuanbaomao.sellersprite.system.ops.operation.service.OperationLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogQueryDao operationLogQueryDao;

    @Override
    public YPage<OperationLogVo> page(YPage<OperationLogVo> page, OperationLogPageRequest request) {
        if (request.getStartTime() != null && request.getEndTime() != null
                && request.getStartTime() > request.getEndTime()) {
            throw new BizException(ResultCode.PARAM_INVALID, "开始时间不能晚于结束时间");
        }
        Page<OperationLogEntity> entityPage = operationLogQueryDao.page(
                request.getUserId(), request.getUsername(), request.getModuleName(), request.getOperationType(),
                request.getSuccess(), request.getTraceId(), request.getStartTime(), request.getEndTime(),
                page.getCurrent(), page.getSize());
        List<OperationLogVo> records = entityPage.getRecords().stream().map(OperationLogConverter::toVo).toList();
        page.setTotal(entityPage.getTotal());
        page.setRecords(records);
        return page;
    }

    @Override
    public OperationLogVo detail(String operationLogId) {
        OperationLogEntity log = operationLogQueryDao.findById(operationLogId)
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));
        return OperationLogConverter.toVo(log);
    }
}
