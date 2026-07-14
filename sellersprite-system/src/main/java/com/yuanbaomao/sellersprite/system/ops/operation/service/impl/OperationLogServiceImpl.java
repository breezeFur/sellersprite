package com.yuanbaomao.sellersprite.system.ops.operation.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.log.mybatis.entity.OperationLogEntity;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.OperationLogQueryDao;
import com.yuanbaomao.sellersprite.system.ops.operation.convert.OperationLogConverter;
import com.yuanbaomao.sellersprite.system.ops.operation.model.dto.OperationLogPageRequest;
import com.yuanbaomao.sellersprite.system.ops.operation.model.vo.OperationLogVo;
import com.yuanbaomao.sellersprite.system.ops.operation.service.OperationLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogQueryDao operationLogQueryDao;

    @Override
    public PageResult<OperationLogVo> page(OperationLogPageRequest request) {
        if (request.getStartTime() != null && request.getEndTime() != null
                && request.getStartTime() > request.getEndTime()) {
            throw new BizException(ResultCode.PARAM_INVALID, "开始时间不能晚于结束时间");
        }
        Page<OperationLogEntity> page = operationLogQueryDao.page(
                request.getUserId(), request.getUsername(), request.getModuleName(), request.getOperationType(),
                request.getSuccess(), request.getTrackId(), request.getStartTime(), request.getEndTime(),
                request.getCurrent(), request.getSize());
        List<OperationLogVo> records = page.getRecords().stream().map(OperationLogConverter::toVo).toList();
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    public OperationLogVo detail(String operationLogId) {
        OperationLogEntity log = operationLogQueryDao.findById(operationLogId)
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));
        return OperationLogConverter.toVo(log);
    }
}
