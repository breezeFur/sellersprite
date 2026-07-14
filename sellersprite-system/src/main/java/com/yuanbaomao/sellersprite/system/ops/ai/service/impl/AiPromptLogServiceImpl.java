package com.yuanbaomao.sellersprite.system.ops.ai.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.AiPromptRecordDao;
import com.yuanbaomao.sellersprite.db.entity.AiPromptRecord;
import com.yuanbaomao.sellersprite.system.ops.ai.convert.AiPromptLogConverter;
import com.yuanbaomao.sellersprite.system.ops.ai.model.dto.AiPromptLogPageRequest;
import com.yuanbaomao.sellersprite.system.ops.ai.model.vo.AiPromptLogVo;
import com.yuanbaomao.sellersprite.system.ops.ai.service.AiPromptLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPromptLogServiceImpl implements AiPromptLogService {

    private final AiPromptRecordDao promptRecordDao;

    @Override
    public PageResult<AiPromptLogVo> page(AiPromptLogPageRequest request) {
        if (request.getStartTime() != null && request.getEndTime() != null
                && request.getStartTime() > request.getEndTime()) {
            throw new BizException(ResultCode.PARAM_INVALID, "开始时间不能晚于结束时间");
        }
        Page<AiPromptRecord> page = promptRecordDao.page(
                request.getUserId(), request.getConversationId(), request.getProvider(), request.getModel(),
                request.getStatus(), request.getStartTime(), request.getEndTime(),
                request.getCurrent(), request.getSize());
        List<AiPromptLogVo> records = page.getRecords().stream()
                .map(AiPromptLogConverter::toSummaryVo)
                .toList();
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    public AiPromptLogVo detail(String promptRecordId) {
        AiPromptRecord record = promptRecordDao.findById(promptRecordId)
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));
        return AiPromptLogConverter.toDetailVo(record);
    }
}
