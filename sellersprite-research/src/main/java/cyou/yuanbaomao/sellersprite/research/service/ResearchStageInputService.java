package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchDatasetDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchStageInputDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchStageInput;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchJobStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchSelectionDecision;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchStageCode;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchWaitingInputType;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventCommand;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventScope;
import cyou.yuanbaomao.sellersprite.research.event.ResearchEventTypes;
import cyou.yuanbaomao.sellersprite.research.event.ResearchJobCreatedEvent;
import cyou.yuanbaomao.sellersprite.research.model.ResearchProductSelection;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.model.dto.ResearchProductSelectionRequest;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchProductCandidateVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchProductSelectionVo;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** 持久化商品选择关卡输入，并在事务提交后唤醒父 Graph。 */
@Service
@RequiredArgsConstructor
public class ResearchStageInputService {

    private static final String EVIDENCE_PRODUCTS_DATASET_CODE = "evidence.products";
    private static final String PRODUCT_CANDIDATES_DATASET_CODE = "selection.productCandidates";
    private static final String PRODUCT_CANDIDATES_OPERATION = "PREPARE_PRODUCT_CANDIDATES";
    private static final int PRODUCT_CANDIDATE_LIMIT = 20;

    private final MarketResearchJobDao jobDao;
    private final MarketResearchStageInputDao stageInputDao;
    private final MarketResearchDatasetDao datasetDao;
    private final ResearchDatasetService datasetService;
    private final ResearchInputService inputService;
    private final ResearchSseEventPublisher eventPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ResearchProductSelectionVo getForCurrentUser(String jobId) {
        MarketResearchJob job = requireOwnedJob(jobId);
        return toVo(job, findSelectionEntity(jobId).orElse(null));
    }

    @Transactional(readOnly = true)
    public Optional<ResearchProductSelection> findSelection(String jobId) {
        return findSelectionEntity(jobId).map(this::readSelection);
    }

    /** 在进入人工中断前固化默认顺序Top20，后续页面刷新不再依赖外部响应。 */
    @Transactional(rollbackFor = Exception.class)
    public void prepareProductCandidates(String jobId) {
        List<MarketResearchDataset> datasets = datasetDao.listByJobId(jobId);
        if (datasets.stream()
                .anyMatch(dataset -> PRODUCT_CANDIDATES_DATASET_CODE.equals(
                        dataset.getDatasetCode()))) {
            return;
        }
        MarketResearchJob job = jobDao.getById(jobId);
        if (job == null) {
            throw new IllegalStateException("市场调研任务不存在: " + jobId);
        }
        MarketResearchDataset products = datasets.stream()
                .filter(dataset -> EVIDENCE_PRODUCTS_DATASET_CODE.equals(dataset.getDatasetCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("阶段一缺少商品证据表: " + jobId));
        JsonNode sourceItems = datasetService.readPayload(products).path("items");
        ObjectNode payload = objectMapper.createObjectNode();
        ArrayNode items = payload.putArray("items");
        if (sourceItems.isArray()) {
            for (JsonNode row : sourceItems) {
                if (items.size() >= PRODUCT_CANDIDATE_LIMIT) {
                    break;
                }
                String asin = text(row, "ASIN");
                if (asin.isBlank()) {
                    continue;
                }
                ObjectNode candidate = items.addObject();
                candidate.put("rank", items.size());
                candidate.put("asin", asin.toUpperCase(Locale.ROOT));
                candidate.put("imageUrl", text(row, "图片链接"));
                candidate.put("title", text(row, "标题"));
                candidate.put("brand", text(row, "品牌"));
                candidate.put("category", text(row, "类目"));
                candidate.put("units", text(row, "月销量"));
                candidate.put("revenue", text(row, "月销售额($)"));
                candidate.put("price", text(row, "价格($)"));
                candidate.put("rating", text(row, "评分"));
                candidate.put("ratings", text(row, "评分数"));
            }
        }
        datasetService.saveDatasets(
                job,
                ResearchPhase.PREPARE_US_EVIDENCE,
                inputService.from(job),
                List.of(new ResearchDataset(
                        PRODUCT_CANDIDATES_DATASET_CODE,
                        PRODUCT_CANDIDATES_OPERATION,
                        payload,
                        items.size())));
    }

    @Transactional(rollbackFor = Exception.class)
    public ResearchProductSelectionVo submitForCurrentUser(
            String jobId, ResearchProductSelectionRequest request) {
        MarketResearchJob job = requireOwnedJob(jobId);
        ResearchProductSelection selection = normalize(request);
        Optional<MarketResearchStageInput> existing = findSelectionEntity(jobId);
        if (existing.isPresent()) {
            if (!selection.equals(readSelection(existing.get()))) {
                throw new BizException(ResultCode.MARKET_RESEARCH_INPUT_NOT_ACCEPTABLE);
            }
            return toVo(job, existing.get());
        }
        requireWaitingForSelection(job);
        validateCandidates(jobId, selection);

        long now = System.currentTimeMillis();
        MarketResearchStageInput input = new MarketResearchStageInput();
        input.setInputId(idGenerator.nextId());
        input.setJobId(jobId);
        input.setStageCode(ResearchStageCode.SCREENING.name());
        input.setInputType(ResearchWaitingInputType.PRODUCT_SELECTION.name());
        input.setDecision(selection.decision().name());
        input.setInputPayload(writeSelection(selection));
        input.setSubmittedBy(job.getUserId());
        input.setSubmittedAt(now);
        if (!stageInputDao.save(input)
                || !jobDao.requeueWaitingInput(
                        jobId,
                        job.getUserId(),
                        ResearchWaitingInputType.PRODUCT_SELECTION.name(),
                        now)) {
            throw new BizException(ResultCode.MARKET_RESEARCH_INPUT_NOT_ACCEPTABLE);
        }

        eventPublisher.publish(ResearchEventCommand.builder()
                .jobId(jobId)
                .scope(ResearchEventScope.WORKFLOW)
                .eventType(ResearchEventTypes.PRODUCT_SELECTION_SUBMITTED)
                .phase(ResearchStageCode.SCREENING.name())
                .nodeCode("productSelectionGate")
                .message(selection.decision() == ResearchSelectionDecision.ENTER
                        ? "已提交商品选择，市场调研将继续执行"
                        : "已放弃进入该市场，正在生成阶段一文件")
                .payload(Map.of(
                        "stageCode", ResearchStageCode.SCREENING.name(),
                        "decision", selection.decision().name(),
                        "selectedAsins", selection.selectedAsins(),
                        "selectedCount", selection.selectedAsins().size()))
                .build());
        applicationEventPublisher.publishEvent(new ResearchJobCreatedEvent(jobId));
        return toVo(job, input);
    }

    private ResearchProductSelection normalize(ResearchProductSelectionRequest request) {
        if (request == null || request.getDecision() == null) {
            throw new BizException(ResultCode.PARAM_INVALID);
        }
        List<String> selectedAsins = request.getSelectedAsins() == null
                ? List.of()
                : List.copyOf(new LinkedHashSet<>(request.getSelectedAsins().stream()
                        .filter(value -> value != null && !value.isBlank())
                        .map(value -> value.trim().toUpperCase(Locale.ROOT))
                        .toList()));
        if (request.getDecision() == ResearchSelectionDecision.ENTER
                && (selectedAsins.isEmpty() || selectedAsins.size() > PRODUCT_CANDIDATE_LIMIT)) {
            throw new BizException(ResultCode.PARAM_INVALID, "请选择1到20个商品");
        }
        if (request.getDecision() == ResearchSelectionDecision.ABANDON && !selectedAsins.isEmpty()) {
            throw new BizException(ResultCode.PARAM_INVALID, "放弃市场时不能提交商品");
        }
        return new ResearchProductSelection(request.getDecision(), selectedAsins);
    }

    private void validateCandidates(String jobId, ResearchProductSelection selection) {
        if (selection.decision() == ResearchSelectionDecision.ABANDON) {
            return;
        }
        Set<String> candidates = productCandidates(jobId).stream()
                .map(ResearchProductCandidateVo::getAsin)
                .filter(value -> value != null && !value.isBlank())
                .collect(java.util.stream.Collectors.toSet());
        if (!candidates.containsAll(selection.selectedAsins())) {
            throw new BizException(ResultCode.PARAM_INVALID, "只能选择阶段一Top20商品");
        }
    }

    private ResearchProductSelectionVo toVo(
            MarketResearchJob job, MarketResearchStageInput input) {
        ResearchProductSelection selection = input == null ? null : readSelection(input);
        String status = selection == null
                ? "PENDING"
                : selection.decision() == ResearchSelectionDecision.ABANDON
                        ? "ABANDONED"
                        : "SUBMITTED";
        return ResearchProductSelectionVo.builder()
                .stageCode(ResearchStageCode.SCREENING.name())
                .status(status)
                .candidates(productCandidates(job.getJobId()))
                .selectedAsins(selection == null ? List.of() : selection.selectedAsins())
                .submittedAt(input == null ? null : input.getSubmittedAt())
                .build();
    }

    private List<ResearchProductCandidateVo> productCandidates(String jobId) {
        Optional<MarketResearchDataset> products = datasetDao.listByJobId(jobId).stream()
                .filter(dataset -> PRODUCT_CANDIDATES_DATASET_CODE.equals(dataset.getDatasetCode()))
                .findFirst();
        if (products.isEmpty()) {
            return List.of();
        }
        JsonNode items = datasetService.readPayload(products.orElseThrow()).path("items");
        if (!items.isArray()) {
            return List.of();
        }
        java.util.ArrayList<ResearchProductCandidateVo> candidates = new java.util.ArrayList<>();
        for (JsonNode row : items) {
            if (candidates.size() >= PRODUCT_CANDIDATE_LIMIT) {
                break;
            }
            String asin = text(row, "asin");
            if (asin.isBlank()) {
                continue;
            }
            candidates.add(ResearchProductCandidateVo.builder()
                    .rank(candidates.size() + 1)
                    .asin(asin.toUpperCase(Locale.ROOT))
                    .imageUrl(text(row, "imageUrl"))
                    .title(text(row, "title"))
                    .brand(text(row, "brand"))
                    .category(text(row, "category"))
                    .units(text(row, "units"))
                    .revenue(text(row, "revenue"))
                    .price(text(row, "price"))
                    .rating(text(row, "rating"))
                    .ratings(text(row, "ratings"))
                    .build());
        }
        return List.copyOf(candidates);
    }

    private Optional<MarketResearchStageInput> findSelectionEntity(String jobId) {
        return stageInputDao.find(
                jobId,
                ResearchStageCode.SCREENING.name(),
                ResearchWaitingInputType.PRODUCT_SELECTION.name());
    }

    private ResearchProductSelection readSelection(MarketResearchStageInput input) {
        try {
            JsonNode payload = objectMapper.readTree(input.getInputPayload());
            java.util.ArrayList<String> selectedAsins = new java.util.ArrayList<>();
            JsonNode values = payload.path("selectedAsins");
            if (values.isArray()) {
                values.forEach(value -> selectedAsins.add(value.asText()));
            }
            return new ResearchProductSelection(
                    ResearchSelectionDecision.valueOf(input.getDecision()),
                    List.copyOf(selectedAsins));
        } catch (RuntimeException exception) {
            throw new IllegalStateException("商品选择关卡输入格式错误: " + input.getInputId(), exception);
        }
    }

    private String writeSelection(ResearchProductSelection selection) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "decision", selection.decision().name(),
                    "selectedAsins", selection.selectedAsins()));
        } catch (Exception exception) {
            throw new IllegalStateException("序列化商品选择关卡输入失败", exception);
        }
    }

    private void requireWaitingForSelection(MarketResearchJob job) {
        if (!ResearchJobStatus.WAITING_INPUT.name().equals(job.getJobStatus())
                || !ResearchWaitingInputType.PRODUCT_SELECTION.name()
                        .equals(job.getWaitingInputType())) {
            throw new BizException(ResultCode.MARKET_RESEARCH_INPUT_NOT_ACCEPTABLE);
        }
    }

    private MarketResearchJob requireOwnedJob(String jobId) {
        return jobDao.findByIdAndUserId(jobId, currentUserId())
                .orElseThrow(() -> new BizException(ResultCode.MARKET_RESEARCH_JOB_NOT_FOUND));
    }

    private String currentUserId() {
        return RequestContextHolder.get()
                .map(context -> context.getUserId())
                .filter(userId -> userId != null && !userId.isBlank())
                .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
    }

    private String text(JsonNode row, String field) {
        JsonNode value = row.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText();
    }
}
