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
        List<ResearchEvidenceCatalog.Definition> definitions =
                ResearchEvidenceCatalog.definitions(stage);
        Map<String, MarketResearchDataset> datasets = evidenceDatasetMetadata(jobId, definitions);
        return definitions.stream()
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
        MarketResearchDataset dataset = datasetService
                .findPayloadByJobIdAndDatasetCode(jobId, datasetCode)
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));
        JsonNode items = datasetService.readPayload(dataset).path("items");
        int total = items.isArray() ? items.size() : 0;
        long safeCurrent = Math.max(1L, current);
        long safeSize = Math.max(1L, Math.min(size, MAX_PAGE_SIZE));
        long pageIndex = safeCurrent - 1L;
        long offset = pageIndex > Long.MAX_VALUE / safeSize
                ? Long.MAX_VALUE
                : pageIndex * safeSize;
        int fromIndex = (int) Math.min(total, offset);
        int toIndex = (int) Math.min(total, fromIndex + safeSize);
        List<JsonNode> records = new ArrayList<>(toIndex - fromIndex);
        for (int index = fromIndex; index < toIndex; index++) {
            records.add(items.get(index));
        }
        return ResearchEvidencePageVo.builder()
                .datasetCode(datasetCode)
                .sheetName(definition.sheetName())
                .stageCode(definition.stage().name())
                .columns(definition.columns())
                .records(List.copyOf(records))
                .current(safeCurrent)
                .size(safeSize)
                .total((long) total)
                .build();
    }

    private Map<String, MarketResearchDataset> evidenceDatasetMetadata(
            String jobId, List<ResearchEvidenceCatalog.Definition> definitions) {
        List<String> datasetCodes = definitions.stream()
                .map(ResearchEvidenceCatalog.Definition::datasetCode)
                .toList();
        Map<String, MarketResearchDataset> result = new LinkedHashMap<>();
        for (MarketResearchDataset dataset :
                datasetService.listMetadataByJobIdAndDatasetCodes(jobId, datasetCodes)) {
            result.putIfAbsent(dataset.getDatasetCode(), dataset);
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
