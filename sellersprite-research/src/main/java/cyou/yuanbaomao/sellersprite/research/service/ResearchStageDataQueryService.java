package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchJobDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceCatalog;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchEvidencePageVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchEvidenceTableSummaryVo;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

/** 查询已持久化证据表；大表通过 REST 分页返回，不进入 SSE 事件载荷。 */
@Service
@RequiredArgsConstructor
public class ResearchStageDataQueryService {

    private static final long MAX_PAGE_SIZE = 200L;

    private final MarketResearchJobDao jobDao;
    private final ResearchDatasetService datasetService;

    @Transactional(readOnly = true)
    public List<ResearchEvidenceTableSummaryVo> listEvidence(
            String jobId, EvidenceStage stage) {
        requireOwnedJob(jobId);
        Map<String, MarketResearchDataset> datasets = evidenceDatasets(jobId);
        return ResearchEvidenceCatalog.definitions(stage).stream()
                .map(definition -> {
                    MarketResearchDataset dataset = datasets.get(definition.datasetCode());
                    return ResearchEvidenceTableSummaryVo.builder()
                            .datasetCode(definition.datasetCode())
                            .sheetName(definition.sheetName())
                            .stageCode(stage.name())
                            .rowCount(dataset == null ? 0 : dataset.getRecordCount())
                            .columns(definition.columns())
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ResearchEvidencePageVo pageEvidence(
            String jobId, String datasetCode, long current, long size) {
        requireOwnedJob(jobId);
        ResearchEvidenceCatalog.Definition definition;
        try {
            definition = ResearchEvidenceCatalog.requireByDatasetCode(datasetCode);
        } catch (IllegalArgumentException exception) {
            throw new BizException(ResultCode.RESOURCE_NOT_FOUND);
        }
        MarketResearchDataset dataset = evidenceDatasets(jobId).get(datasetCode);
        if (dataset == null) {
            throw new BizException(ResultCode.RESOURCE_NOT_FOUND);
        }
        JsonNode items = datasetService.readPayload(dataset).path("items");
        List<JsonNode> allRecords = new ArrayList<>();
        if (items.isArray()) {
            items.forEach(allRecords::add);
        }
        long safeCurrent = Math.max(1L, current);
        long safeSize = Math.max(1L, Math.min(size, MAX_PAGE_SIZE));
        int fromIndex = (int) Math.min(allRecords.size(), (safeCurrent - 1L) * safeSize);
        int toIndex = (int) Math.min(allRecords.size(), fromIndex + safeSize);
        return ResearchEvidencePageVo.builder()
                .datasetCode(datasetCode)
                .sheetName(definition.sheetName())
                .stageCode(definition.stage().name())
                .columns(definition.columns())
                .records(List.copyOf(allRecords.subList(fromIndex, toIndex)))
                .current(safeCurrent)
                .size(safeSize)
                .total((long) allRecords.size())
                .build();
    }

    private Map<String, MarketResearchDataset> evidenceDatasets(String jobId) {
        Map<String, MarketResearchDataset> result = new LinkedHashMap<>();
        for (MarketResearchDataset dataset : datasetService.listByJobId(jobId)) {
            if (ResearchEvidenceCatalog.DEFINITIONS.stream()
                    .anyMatch(definition -> definition.datasetCode().equals(dataset.getDatasetCode()))) {
                result.putIfAbsent(dataset.getDatasetCode(), dataset);
            }
        }
        return result;
    }

    private void requireOwnedJob(String jobId) {
        String userId = RequestContextHolder.get()
                .map(context -> context.getUserId())
                .filter(value -> value != null && !value.isBlank())
                .orElseThrow(() -> new BizException(ResultCode.UNAUTHORIZED));
        jobDao.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new BizException(ResultCode.MARKET_RESEARCH_JOB_NOT_FOUND));
    }
}
