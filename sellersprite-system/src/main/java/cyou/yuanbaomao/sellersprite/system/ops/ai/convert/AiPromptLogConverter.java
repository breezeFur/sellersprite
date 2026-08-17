package cyou.yuanbaomao.sellersprite.system.ops.ai.convert;

import cyou.yuanbaomao.sellersprite.db.entity.AiPromptRecord;
import cyou.yuanbaomao.sellersprite.framework.security.SensitiveDataMasker;
import cyou.yuanbaomao.sellersprite.system.ops.ai.model.vo.AiPromptLogVo;

public final class AiPromptLogConverter {

    private AiPromptLogConverter() {
    }

    public static AiPromptLogVo toSummaryVo(AiPromptRecord entity) {
        return toVo(entity, false);
    }

    public static AiPromptLogVo toDetailVo(AiPromptRecord entity) {
        return toVo(entity, true);
    }

    private static AiPromptLogVo toVo(AiPromptRecord entity, boolean includePayloads) {
        AiPromptLogVo vo = new AiPromptLogVo();
        vo.setPromptRecordId(entity.getPromptRecordId());
        vo.setConversationId(entity.getConversationId());
        vo.setUserId(entity.getUserId());
        vo.setProvider(entity.getProvider());
        vo.setModel(entity.getModel());
        if (includePayloads) {
            vo.setRequestMessages(SensitiveDataMasker.mask(entity.getRequestMessages()));
            vo.setResponseContent(SensitiveDataMasker.mask(entity.getResponseContent()));
            vo.setResponseMetadata(SensitiveDataMasker.mask(entity.getResponseMetadata()));
        }
        vo.setPromptSummary(SensitiveDataMasker.mask(entity.getPromptSummary()));
        vo.setPromptTruncated(entity.getPromptTruncated());
        vo.setPromptTokens(entity.getPromptTokens());
        vo.setCompletionTokens(entity.getCompletionTokens());
        vo.setTotalTokens(entity.getTotalTokens());
        vo.setFinishReason(entity.getFinishReason());
        vo.setStatus(entity.getStatus());
        vo.setErrorType(entity.getErrorType());
        vo.setErrorMessage(SensitiveDataMasker.mask(entity.getErrorMessage()));
        vo.setCostMs(entity.getCostMs());
        vo.setTraceId(entity.getTraceId());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}
