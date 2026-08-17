package cyou.yuanbaomao.sellersprite.system.ops.ai.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.AiPromptRecordDao;
import cyou.yuanbaomao.sellersprite.db.entity.AiPromptRecord;
import cyou.yuanbaomao.sellersprite.system.ops.ai.convert.AiPromptLogConverter;
import cyou.yuanbaomao.sellersprite.system.ops.ai.model.dto.AiPromptLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.ai.model.vo.AiPromptLogVo;
import cyou.yuanbaomao.sellersprite.system.ops.ai.service.AiPromptLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPromptLogServiceImpl implements AiPromptLogService {

    private final AiPromptRecordDao promptRecordDao;

    @Override
    public YPage<AiPromptLogVo> page(YPage<AiPromptLogVo> page, AiPromptLogPageRequest request) {
        if (request.getStartTime() != null && request.getEndTime() != null
                && request.getStartTime() > request.getEndTime()) {
            throw new BizException(ResultCode.PARAM_INVALID, "开始时间不能晚于结束时间");
        }
        Page<AiPromptRecord> entityPage = promptRecordDao.page(
                request.getUserId(), request.getConversationId(), request.getProvider(), request.getModel(),
                request.getStatus(), request.getStartTime(), request.getEndTime(),
                page.getCurrent(), page.getSize());
        List<AiPromptLogVo> records = entityPage.getRecords().stream()
                .map(AiPromptLogConverter::toSummaryVo)
                .toList();
        page.setTotal(entityPage.getTotal());
        page.setRecords(records);
        return page;
    }

    @Override
    public AiPromptLogVo detail(String promptRecordId) {
        AiPromptRecord record = promptRecordDao.findById(promptRecordId)
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));
        return AiPromptLogConverter.toDetailVo(record);
    }
}
