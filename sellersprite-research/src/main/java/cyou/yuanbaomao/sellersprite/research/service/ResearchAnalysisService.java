package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.AiConversationDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchAnalysisRunDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.AiConversation;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchAnalysisRun;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.config.ResearchProperties;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchAnalysisRunType;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchJobStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventScope;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.model.dto.ResearchAnalysisMessageRequest;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchAnalysisRunVo;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 创建并管理同一市场调研任务下的初次分析、重试和后续追问。 */
@Service
@RequiredArgsConstructor
public class ResearchAnalysisService {

    private static final String DEFAULT_ANALYSIS_GOAL =
            "基于本次市场调研的全部证据，给出机会、风险、竞争格局与下一步行动建议";
    private static final String CONVERSATION_SYSTEM_PROMPT =
            "你是市场调研分析助手。只依据当前任务的持久化 evidence 回答，不编造不存在的数据。";
    private static final String CONVERSATION_STATUS_ACTIVE = "ACTIVE";
    private static final String SCREENING_ANALYSIS_INSTRUCTION =
            "阶段一仅分析7张市场初筛证据表，逐表给出简短结论，并形成帮助用户选择商品的阶段总结。";
    private static final String DEEP_DIVE_ANALYSIS_INSTRUCTION =
            "阶段二分析评价、VOC、Keywords、ASIN销售趋势、ASIN运营趋势五张表。"
                    + "结合销量规模、价格、BSR、评分数、评分和卖家数变化判断所选商品的增长稳定性与竞争状态。"
                    + "Keywords用于评估宣传获客成本、竞争和投放难度；PPC竞价只是参考，"
                    + "缺少曝光、点击、转化和实际花费时不得推算预算、ACOS或ROI。";
    private static final String FINAL_ANALYSIS_INSTRUCTION =
            "阶段三基于完整12组证据形成最终市场进入结论、主要机会、核心风险和下一步行动建议。";

    private final MarketResearchJobDao jobDao;
    private final MarketResearchAnalysisRunDao analysisRunDao;
    private final AiConversationDao conversationDao;
    private final IdGenerator idGenerator;
    private final ResearchProperties properties;
    private final ResearchSseEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public ResearchAnalysisRunVo createInitial(MarketResearchJob job, String analysisGoal) {
        long now = System.currentTimeMillis();
        AiConversation conversation = new AiConversation();
        conversation.setConversationId(idGenerator.nextId());
        conversation.setUserId(job.getUserId());
        conversation.setTitle(job.getReportName() + " · 市场调研分析");
        conversation.setProvider("");
        conversation.setModel("");
        conversation.setSystemPrompt(CONVERSATION_SYSTEM_PROMPT);
        conversation.setMessageCount(0);
        conversation.setLastMessageAt(now);
        conversation.setStatus(CONVERSATION_STATUS_ACTIVE);
        if (!conversationDao.save(conversation)) {
            throw new IllegalStateException("创建市场调研AI会话失败");
        }

        MarketResearchAnalysisRun run = newRun(
                job,
                conversation.getConversationId(),
                null,
                ResearchAnalysisRunType.SCREENING,
                stageGoal(ResearchStageCode.SCREENING, analysisGoal),
                ResearchAnalysisRunStatus.WAITING_RESEARCH,
                now);
        if (!analysisRunDao.save(run)) {
            throw new IllegalStateException("创建市场调研初次分析运行失败");
        }
        return toVo(run);
    }

    /** 数据任务人工重试时保留旧记录，并为任务当前阶段准备新的分析运行。 */
    @Transactional(rollbackFor = Exception.class)
    public ResearchAnalysisRunVo prepareForResearchRetry(MarketResearchJob job) {
        MarketResearchAnalysisRun previous = requireLatest(job.getJobId(), job.getUserId());
        if (hasActiveRun(job.getJobId(), job.getUserId())) {
            throw new IllegalStateException("市场调研数据重试时仍存在活动分析运行");
        }
        ResearchStageCode stage = ResearchStageCode.valueOf(job.getCurrentStage());
        requirePreviousStageSucceeded(job, stage);
        MarketResearchAnalysisRun run = newRun(
                job,
                previous.getConversationId(),
                previous.getAnalysisRunId(),
                ResearchAnalysisRunType.valueOf(stage.name()),
                stageGoal(stage, previous.getAnalysisGoal()),
                ResearchAnalysisRunStatus.WAITING_RESEARCH,
                System.currentTimeMillis());
        if (!analysisRunDao.save(run)) {
            throw new IllegalStateException("创建数据重试后的市场调研分析运行失败");
        }
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(run.getJobId())
                .conversationId(run.getConversationId())
                .analysisRunId(run.getAnalysisRunId())
                .scope(ResearchEventScope.ANALYSIS)
                .eventType(ResearchEventTypes.ANALYSIS_WAITING_RESEARCH)
                .phase("waiting_research")
                .message("数据任务已重试，分析将在新证据与Excel就绪后启动")
                .payload(Map.of(
                        "analysisStatus", ResearchAnalysisRunStatus.WAITING_RESEARCH.name()))
                .build());
        return toVo(run);
    }

    /** 由父 Graph 在对应阶段创建或复用一条独立分析运行。 */
    @Transactional(rollbackFor = Exception.class)
    public MarketResearchAnalysisRun ensureStageRun(String jobId, ResearchStageCode stage) {
        MarketResearchJob job = jobDao.getById(jobId);
        if (job == null) {
            throw new IllegalStateException("市场调研任务不存在: " + jobId);
        }
        ResearchAnalysisRunType runType = ResearchAnalysisRunType.valueOf(stage.name());
        Optional<MarketResearchAnalysisRun> existing =
                analysisRunDao.findLatestByJobIdAndUserIdAndRunType(
                        jobId, job.getUserId(), runType.name());
        if (existing.isPresent()) {
            return existing.orElseThrow();
        }
        MarketResearchAnalysisRun previous = requireLatest(jobId, job.getUserId());
        requirePreviousStageSucceeded(job, stage);
        MarketResearchAnalysisRun run = newRun(
                job,
                previous.getConversationId(),
                previous.getAnalysisRunId(),
                runType,
                stageGoal(stage, previous.getAnalysisGoal()),
                ResearchAnalysisRunStatus.WAITING_RESEARCH,
                System.currentTimeMillis());
        if (!analysisRunDao.save(run)) {
            throw new IllegalStateException("创建市场调研阶段分析运行失败: " + stage);
        }
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(jobId)
                .conversationId(run.getConversationId())
                .analysisRunId(run.getAnalysisRunId())
                .scope(ResearchEventScope.ANALYSIS)
                .eventType(ResearchEventTypes.ANALYSIS_WAITING_RESEARCH)
                .phase(stage.name())
                .message("已准备" + stage.name() + "阶段分析运行")
                .payload(Map.of(
                        "stageCode", stage.name(),
                        "analysisStatus", ResearchAnalysisRunStatus.WAITING_RESEARCH.name()))
                .build());
        return run;
    }

    @Transactional(readOnly = true)
    public List<ResearchAnalysisRunVo> list(String jobId) {
        MarketResearchJob job = requireOwnedJob(jobId);
        return analysisRunDao.listByJobIdAndUserId(jobId, job.getUserId()).stream()
                .map(this::toVo)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResearchAnalysisRunVo latest(String jobId) {
        MarketResearchJob job = requireOwnedJob(jobId);
        return toVo(requireLatest(jobId, job.getUserId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResearchAnalysisRunVo retry(String jobId) {
        MarketResearchJob job = requireOwnedSucceededJob(jobId);
        MarketResearchAnalysisRun previous = requireLatest(jobId, job.getUserId());
        return createRetry(job, previous);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResearchAnalysisRunVo retryRun(String analysisRunId) {
        MarketResearchAnalysisRun previous = requireOwnedRun(analysisRunId);
        MarketResearchJob job = requireOwnedSucceededJob(previous.getJobId());
        MarketResearchAnalysisRun latest = requireLatest(job.getJobId(), job.getUserId());
        if (!latest.getAnalysisRunId().equals(previous.getAnalysisRunId())) {
            throw new BizException(ResultCode.MARKET_RESEARCH_ANALYSIS_NOT_RETRYABLE);
        }
        return createRetry(job, previous);
    }

    /** 继续失败的AI分析，复用已持久化证据和会话上下文，不重新采集远端数据。 */
    @Transactional(rollbackFor = Exception.class)
    public ResearchAnalysisRunVo continueRun(String analysisRunId) {
        return retryRun(analysisRunId);
    }

    private ResearchAnalysisRunVo createRetry(
            MarketResearchJob job, MarketResearchAnalysisRun previous) {
        if (!ResearchAnalysisRunStatus.FAILED.name().equals(previous.getRunStatus())) {
            throw new BizException(ResultCode.MARKET_RESEARCH_ANALYSIS_NOT_RETRYABLE);
        }
        ResearchAnalysisRunType retryRunType =
                ResearchAnalysisRunType.FOLLOW_UP.name().equals(previous.getRunType())
                        ? ResearchAnalysisRunType.FOLLOW_UP
                        : ResearchAnalysisRunType.RETRY;
        MarketResearchAnalysisRun run = newRun(
                job,
                previous.getConversationId(),
                previous.getAnalysisRunId(),
                retryRunType,
                previous.getAnalysisGoal(),
                ResearchAnalysisRunStatus.QUEUED,
                System.currentTimeMillis());
        if (!analysisRunDao.save(run)) {
            throw new IllegalStateException("创建市场调研分析重试失败");
        }
        publishQueued(run, "市场调研分析已重新排队，不会重复采集数据");
        return toVo(run);
    }

    @Transactional(rollbackFor = Exception.class)
    public ResearchAnalysisRunVo followUp(
            String jobId, ResearchAnalysisMessageRequest request) {
        MarketResearchJob job = requireOwnedSucceededJob(jobId);
        MarketResearchAnalysisRun previous = requireLatest(jobId, job.getUserId());
        if (hasActiveRun(jobId, job.getUserId())) {
            throw new BizException(ResultCode.MARKET_RESEARCH_ANALYSIS_NOT_STARTABLE);
        }
        MarketResearchAnalysisRun run = newRun(
                job,
                previous.getConversationId(),
                previous.getAnalysisRunId(),
                ResearchAnalysisRunType.FOLLOW_UP,
                request.getContent().trim(),
                ResearchAnalysisRunStatus.QUEUED,
                System.currentTimeMillis());
        if (!analysisRunDao.save(run)) {
            throw new IllegalStateException("创建市场调研后续分析失败");
        }
        publishQueued(run, "已收到后续问题，开始基于原任务证据继续分析");
        return toVo(run);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(String jobId) {
        MarketResearchJob job = requireOwnedJob(jobId);
        MarketResearchAnalysisRun run = requireLatest(jobId, job.getUserId());
        cancelRun(run);
    }

    private void cancelRun(MarketResearchAnalysisRun run) {
        long now = System.currentTimeMillis();
        if (analysisRunDao.cancelPending(run.getAnalysisRunId(), run.getUserId(), now)) {
            eventPublisher.publish(ResearchEventCommand.builder()
                    .jobId(run.getJobId())
                    .conversationId(run.getConversationId())
                    .analysisRunId(run.getAnalysisRunId())
                    .scope(ResearchEventScope.ANALYSIS)
                    .eventType(ResearchEventTypes.DONE)
                    .phase("cancelled")
                    .message("市场调研分析已取消")
                    .payload(Map.of("analysisStatus", ResearchAnalysisRunStatus.CANCELLED.name()))
                    .build());
            return;
        }
        if (analysisRunDao.requestRunningCancel(run.getAnalysisRunId(), run.getUserId(), now)) {
            eventPublisher.publish(ResearchEventCommand.builder()
                    .jobId(run.getJobId())
                    .conversationId(run.getConversationId())
                    .analysisRunId(run.getAnalysisRunId())
                    .scope(ResearchEventScope.ANALYSIS)
                    .eventType(ResearchEventTypes.ANALYSIS_CANCEL_REQUESTED)
                    .phase(run.getCurrentPhase())
                    .message("已请求取消当前分析，将在安全边界停止")
                    .payload(Map.of("requestedAt", now))
                    .build());
            return;
        }
        throw new BizException(ResultCode.MARKET_RESEARCH_ANALYSIS_NOT_CANCELLABLE);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelRun(String analysisRunId) {
        MarketResearchAnalysisRun run = requireOwnedRun(analysisRunId);
        cancelRun(run);
    }

    @Transactional(readOnly = true)
    public MarketResearchAnalysisRun findLatest(String jobId, String userId) {
        return analysisRunDao.findLatestByJobIdAndUserId(jobId, userId).orElse(null);
    }

    private MarketResearchAnalysisRun newRun(
            MarketResearchJob job,
            String conversationId,
            String parentRunId,
            ResearchAnalysisRunType runType,
            String analysisGoal,
            ResearchAnalysisRunStatus status,
            long now) {
        MarketResearchAnalysisRun run = new MarketResearchAnalysisRun();
        run.setAnalysisRunId(idGenerator.nextId());
        run.setJobId(job.getJobId());
        run.setUserId(job.getUserId());
        run.setConversationId(conversationId);
        run.setParentRunId(parentRunId);
        run.setRunType(runType.name());
        run.setAnalysisGoal(normalizeGoal(analysisGoal));
        run.setRunStatus(status.name());
        run.setCurrentPhase(status == ResearchAnalysisRunStatus.QUEUED
                ? "queued"
                : "waiting_research");
        run.setProgress(0);
        run.setAttemptCount(0);
        run.setMaxAttempts(properties.getMaxAttempts());
        run.setNextRunAt(now);
        run.setModelCallCount(0);
        run.setEventCount(0);
        run.setErrorCode("");
        run.setErrorMessage("");
        return run;
    }

    private void publishQueued(MarketResearchAnalysisRun run, String message) {
        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(run.getJobId())
                .conversationId(run.getConversationId())
                .analysisRunId(run.getAnalysisRunId())
                .scope(ResearchEventScope.ANALYSIS)
                .eventType(ResearchEventTypes.ANALYSIS_QUEUED)
                .phase("queued")
                .message(message)
                .payload(Map.of(
                        "runType", run.getRunType(),
                        "analysisStatus", ResearchAnalysisRunStatus.QUEUED.name()))
                .build());
    }

    private boolean hasActiveRun(String jobId, String userId) {
        return analysisRunDao.listByJobIdAndUserId(jobId, userId).stream()
                .map(MarketResearchAnalysisRun::getRunStatus)
                .anyMatch(status -> ResearchAnalysisRunStatus.WAITING_RESEARCH.name().equals(status)
                        || ResearchAnalysisRunStatus.QUEUED.name().equals(status)
                        || ResearchAnalysisRunStatus.RUNNING.name().equals(status)
                        || ResearchAnalysisRunStatus.RETRY_WAIT.name().equals(status));
    }

    private MarketResearchJob requireOwnedSucceededJob(String jobId) {
        MarketResearchJob job = requireOwnedJob(jobId);
        if (!ResearchJobStatus.SUCCEEDED.name().equals(job.getJobStatus())) {
            throw new BizException(ResultCode.MARKET_RESEARCH_ANALYSIS_NOT_STARTABLE);
        }
        return job;
    }

    private MarketResearchJob requireOwnedJob(String jobId) {
        String userId = currentUserId();
        return jobDao.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new BizException(ResultCode.MARKET_RESEARCH_JOB_NOT_FOUND));
    }

    private MarketResearchAnalysisRun requireLatest(String jobId, String userId) {
        return analysisRunDao.findLatestByJobIdAndUserId(jobId, userId)
                .orElseThrow(() -> new BizException(ResultCode.MARKET_RESEARCH_ANALYSIS_NOT_FOUND));
    }

    private MarketResearchAnalysisRun requireOwnedRun(String analysisRunId) {
        return analysisRunDao.findByIdAndUserId(analysisRunId, currentUserId())
                .orElseThrow(() -> new BizException(ResultCode.MARKET_RESEARCH_ANALYSIS_NOT_FOUND));
    }

    private String currentUserId() {
        return RequestContextHolder.get()
                .map(context -> context.getUserId())
                .filter(value -> value != null && !value.isBlank())
                .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
    }

    private String normalizeGoal(String goal) {
        return goal == null || goal.isBlank() ? DEFAULT_ANALYSIS_GOAL : goal.trim();
    }

    public ResearchAnalysisRunVo toVo(MarketResearchAnalysisRun run) {
        if (run == null) {
            return null;
        }
        ResearchAnalysisRunStatus status = ResearchAnalysisRunStatus.valueOf(run.getRunStatus());
        return ResearchAnalysisRunVo.builder()
                .analysisRunId(run.getAnalysisRunId())
                .jobId(run.getJobId())
                .conversationId(run.getConversationId())
                .parentRunId(run.getParentRunId())
                .runType(run.getRunType())
                .stageCode(stageCode(run.getRunType()))
                .analysisGoal(run.getAnalysisGoal())
                .status(run.getRunStatus())
                .currentPhase(run.getCurrentPhase())
                .progress(run.getProgress())
                .attemptCount(run.getAttemptCount())
                .maxAttempts(run.getMaxAttempts())
                .nextRunAt(run.getNextRunAt())
                .leaseUntil(run.getLeaseUntil())
                .heartbeatAt(run.getHeartbeatAt())
                .cancelRequestedAt(run.getCancelRequestedAt())
                .modelCallCount(run.getModelCallCount())
                .eventCount(run.getEventCount())
                .finalSummary(run.getFinalSummary())
                .errorCode(run.getErrorCode())
                .errorMessage(run.getErrorMessage())
                .startedAt(run.getStartedAt())
                .finishedAt(run.getFinishedAt())
                .createdAt(run.getCreatedAt())
                .cancellable(status == ResearchAnalysisRunStatus.WAITING_RESEARCH
                        || status == ResearchAnalysisRunStatus.QUEUED
                        || status == ResearchAnalysisRunStatus.RETRY_WAIT
                        || status == ResearchAnalysisRunStatus.RUNNING)
                .retryable(status == ResearchAnalysisRunStatus.FAILED)
                .build();
    }

    private void requirePreviousStageSucceeded(MarketResearchJob job, ResearchStageCode stage) {
        if (stage == ResearchStageCode.SCREENING) {
            return;
        }
        ResearchStageCode previousStage = stage == ResearchStageCode.DEEP_DIVE
                ? ResearchStageCode.SCREENING
                : ResearchStageCode.DEEP_DIVE;
        boolean succeeded = analysisRunDao.findLatestByJobIdAndUserIdAndRunType(
                        job.getJobId(), job.getUserId(), previousStage.name())
                .map(MarketResearchAnalysisRun::getRunStatus)
                .filter(ResearchAnalysisRunStatus.SUCCEEDED.name()::equals)
                .isPresent();
        if (!succeeded) {
            throw new IllegalStateException("前序阶段分析未完成: " + previousStage);
        }
    }

    private String stageGoal(ResearchStageCode stage, String goal) {
        String baseGoal = normalizeGoal(goal);
        String instruction = switch (stage) {
            case SCREENING -> SCREENING_ANALYSIS_INSTRUCTION;
            case DEEP_DIVE -> DEEP_DIVE_ANALYSIS_INSTRUCTION;
            case FINAL_ANALYSIS -> FINAL_ANALYSIS_INSTRUCTION;
        };
        return instruction + "\n用户分析目标：" + baseGoal;
    }

    private String stageCode(String runType) {
        try {
            return ResearchStageCode.valueOf(runType).name();
        } catch (IllegalArgumentException | NullPointerException ignored) {
            return ResearchStageCode.FINAL_ANALYSIS.name();
        }
    }
}
