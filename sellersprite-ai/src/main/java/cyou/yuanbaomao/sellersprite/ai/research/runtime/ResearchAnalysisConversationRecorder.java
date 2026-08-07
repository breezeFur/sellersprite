package cyou.yuanbaomao.sellersprite.ai.research.runtime;

import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.ai.conversation.constants.AiConversationConstants;
import cyou.yuanbaomao.sellersprite.ai.conversation.enums.AiMessageRole;
import cyou.yuanbaomao.sellersprite.ai.conversation.enums.AiMessageStatus;
import cyou.yuanbaomao.sellersprite.ai.prompt.enums.AiPromptStatus;
import cyou.yuanbaomao.sellersprite.db.dao.AiConversationDao;
import cyou.yuanbaomao.sellersprite.db.dao.AiConversationMessageDao;
import cyou.yuanbaomao.sellersprite.db.dao.AiPromptRecordDao;
import cyou.yuanbaomao.sellersprite.db.entity.AiConversation;
import cyou.yuanbaomao.sellersprite.db.entity.AiConversationMessage;
import cyou.yuanbaomao.sellersprite.db.entity.AiPromptRecord;
import cyou.yuanbaomao.sellersprite.framework.security.SensitiveDataMasker;
import cyou.yuanbaomao.sellersprite.research.model.ResearchAnalysisLease;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/** 将每次分析的用户目标和最终回答复用现有 AI 会话表持久化。 */
@Service
@RequiredArgsConstructor
public class ResearchAnalysisConversationRecorder {

    private static final int MAX_PROMPT_SUMMARY_LENGTH = 2_048;
    private static final int MAX_ERROR_MESSAGE_LENGTH = 4_000;
    private static final String EMPTY_JSON_ARRAY = "[]";
    private static final String EMPTY_STRING = "";

    private final AiConversationDao conversationDao;
    private final AiConversationMessageDao messageDao;
    private final AiPromptRecordDao promptRecordDao;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Session start(ResearchAnalysisLease lease) {
        long startedAt = System.currentTimeMillis();
        AiPromptRecord promptRecord = promptRecordDao.getById(lease.analysisRunId());
        if (promptRecord == null) {
            promptRecord = new AiPromptRecord();
            promptRecord.setPromptRecordId(lease.analysisRunId());
            promptRecord.setConversationId(lease.conversationId());
            promptRecord.setUserId(lease.userId());
            promptRecord.setProvider(EMPTY_STRING);
            promptRecord.setModel(EMPTY_STRING);
        }
        String requestMessages = writeJson(Map.of(
                "jobId", lease.jobId(),
                "analysisRunId", lease.analysisRunId(),
                "runType", lease.runType(),
                "analysisGoal", lease.analysisGoal()));
        SensitiveDataMasker.MaskedText summary = SensitiveDataMasker.maskAndTruncate(
                requestMessages, MAX_PROMPT_SUMMARY_LENGTH);
        promptRecord.setRequestMessages(SensitiveDataMasker.mask(requestMessages));
        promptRecord.setPromptSummary(summary.content());
        promptRecord.setPromptTruncated(summary.truncated() ? 1 : 0);
        promptRecord.setResponseContent(EMPTY_STRING);
        promptRecord.setResponseMetadata(null);
        promptRecord.setFinishReason(EMPTY_STRING);
        promptRecord.setStatus(AiPromptStatus.PROCESSING.name());
        promptRecord.setErrorType(EMPTY_STRING);
        promptRecord.setErrorMessage(EMPTY_STRING);
        promptRecord.setCostMs(0L);
        promptRecord.setTrackId(EMPTY_STRING);
        if (!promptRecordDao.saveOrUpdate(promptRecord)) {
            throw new IllegalStateException("保存市场调研分析Prompt记录失败");
        }
        ensureMessage(lease, AiMessageRole.USER, lease.analysisGoal());
        return new Session(lease.analysisRunId(), startedAt);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void complete(Session session, ResearchAnalysisLease lease, String finalSummary) {
        AiPromptRecord record = new AiPromptRecord();
        record.setPromptRecordId(session.promptRecordId());
        record.setResponseContent(SensitiveDataMasker.mask(finalSummary));
        record.setStatus(AiPromptStatus.SUCCESS.name());
        record.setFinishReason("stop");
        record.setErrorType(EMPTY_STRING);
        record.setErrorMessage(EMPTY_STRING);
        record.setCostMs(Math.max(0L, System.currentTimeMillis() - session.startedAt()));
        if (!promptRecordDao.updateById(record)) {
            throw new IllegalStateException("完成市场调研分析Prompt记录失败");
        }
        ensureMessage(lease, AiMessageRole.ASSISTANT, finalSummary);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void fail(Session session, Throwable throwable) {
        AiPromptRecord record = new AiPromptRecord();
        record.setPromptRecordId(session.promptRecordId());
        record.setStatus(AiPromptStatus.FAILED.name());
        record.setErrorType(throwable == null ? EMPTY_STRING : throwable.getClass().getName());
        record.setErrorMessage(SensitiveDataMasker.mask(limit(
                throwable == null ? EMPTY_STRING : throwable.getMessage(), MAX_ERROR_MESSAGE_LENGTH)));
        record.setCostMs(Math.max(0L, System.currentTimeMillis() - session.startedAt()));
        promptRecordDao.updateById(record);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void cancel(Session session) {
        AiPromptRecord record = new AiPromptRecord();
        record.setPromptRecordId(session.promptRecordId());
        record.setStatus(AiPromptStatus.CANCELLED.name());
        record.setFinishReason("cancelled");
        record.setErrorType(EMPTY_STRING);
        record.setErrorMessage(EMPTY_STRING);
        record.setCostMs(Math.max(0L, System.currentTimeMillis() - session.startedAt()));
        if (!promptRecordDao.updateById(record)) {
            throw new IllegalStateException("取消市场调研分析Prompt记录失败");
        }
    }

    private void ensureMessage(
            ResearchAnalysisLease lease, AiMessageRole role, String content) {
        if (messageDao.findByPromptRecordId(
                lease.userId(), lease.conversationId(), lease.analysisRunId(), role.name()).isPresent()) {
            return;
        }
        AiConversation conversation = conversationDao.findByIdAndUserId(
                        lease.conversationId(), lease.userId())
                .orElseThrow(() -> new IllegalStateException("市场调研AI会话不存在"));
        int sequenceNo = conversation.getMessageCount() == null
                ? 1
                : conversation.getMessageCount() + 1;
        AiConversationMessage message = new AiConversationMessage();
        message.setMessageId(idGenerator.nextId());
        message.setConversationId(lease.conversationId());
        message.setUserId(lease.userId());
        message.setPromptRecordId(lease.analysisRunId());
        message.setSequenceNo(sequenceNo);
        message.setRole(role.name());
        message.setContent(content == null ? EMPTY_STRING : content);
        message.setContentType(AiConversationConstants.CONTENT_TYPE_TEXT);
        message.setMetadata(writeJson(Map.of(
                "jobId", lease.jobId(),
                "analysisRunId", lease.analysisRunId(),
                "runType", lease.runType())));
        message.setMessageStatus(AiMessageStatus.COMPLETED.name());
        message.setErrorCode(EMPTY_STRING);
        message.setErrorMessage(EMPTY_STRING);
        if (!messageDao.save(message)) {
            throw new IllegalStateException("保存市场调研AI会话消息失败");
        }
        conversation.setMessageCount(sequenceNo);
        conversation.setLastMessageAt(System.currentTimeMillis());
        if (!conversationDao.updateById(conversation)) {
            throw new IllegalStateException("更新市场调研AI会话失败");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new IllegalStateException("序列化市场调研AI会话元数据失败", exception);
        }
    }

    private String limit(String value, int maxLength) {
        String text = value == null ? EMPTY_STRING : value;
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    public record Session(String promptRecordId, long startedAt) {
    }
}
