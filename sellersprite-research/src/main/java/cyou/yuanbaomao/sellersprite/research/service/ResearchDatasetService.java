package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.db.dao.MarketResearchDatasetDao;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchDataset;
import cyou.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import cyou.yuanbaomao.sellersprite.research.constants.ResearchConstants;
import cyou.yuanbaomao.sellersprite.research.evidence.EvidenceStage;
import cyou.yuanbaomao.sellersprite.research.evidence.ResearchEvidenceCatalog;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchDatasetValidationStatus;
import cyou.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDataset;
import cyou.yuanbaomao.sellersprite.research.model.ResearchInput;
import cyou.yuanbaomao.sellersprite.research.support.ResearchHashUtils;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** 持久化可跨checkpoint恢复复用的不可变市场调研数据集。 */
@Service
@RequiredArgsConstructor
public class ResearchDatasetService {

    private static final String VALIDATION_SUMMARY = "SHA-256完整性校验通过";

    private final MarketResearchDatasetDao datasetDao;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public boolean hasValidNodeDatasets(String jobId, ResearchPhase phase) {
        List<MarketResearchDataset> nodeDatasets = datasetDao.listByJobId(jobId).stream()
                .filter(dataset -> phase.getNodeCode().equals(dataset.getNodeCode()))
                .toList();
        return !nodeDatasets.isEmpty() && nodeDatasets.stream().allMatch(this::isIntact);
    }

    @Transactional(readOnly = true)
    public List<MarketResearchDataset> listByJobId(String jobId) {
        return datasetDao.listByJobId(jobId);
    }

    @Transactional(readOnly = true)
    public List<ResearchDataset> readEvidenceDatasets(String jobId) {
        return readEvidenceDatasets(jobId, ResearchEvidenceCatalog.DEFINITIONS);
    }

    @Transactional(readOnly = true)
    public List<ResearchDataset> readEvidenceDatasets(String jobId, EvidenceStage stage) {
        return readEvidenceDatasets(jobId, ResearchEvidenceCatalog.definitions(stage));
    }

    private List<ResearchDataset> readEvidenceDatasets(
            String jobId, List<ResearchEvidenceCatalog.Definition> definitions) {
        List<String> nodeCodes = definitions.stream()
                .map(definition -> definition.phase().getNodeCode())
                .toList();
        List<String> datasetCodes = definitions.stream()
                .map(ResearchEvidenceCatalog.Definition::datasetCode)
                .toList();
        return datasetDao.listByJobIdAndNodeCodesAndDatasetCodes(
                        jobId, nodeCodes, datasetCodes)
                .stream()
                .map(dataset -> new ResearchDataset(
                        dataset.getDatasetCode(),
                        dataset.getOperation(),
                        readPayload(dataset),
                        dataset.getRecordCount()))
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveDatasets(
            MarketResearchJob job,
            ResearchPhase phase,
            ResearchInput input,
            List<ResearchDataset> datasets) {
        try {
            String requestPayload = objectMapper.writeValueAsString(input);
            String requestHash = ResearchHashUtils.sha256(requestPayload);
            for (ResearchDataset dataset : datasets) {
                saveDataset(job, phase, dataset, requestPayload, requestHash);
            }
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("序列化市场调研数据集请求失败", exception);
        }
    }

    @Transactional(readOnly = true)
    public JsonNode readPayload(MarketResearchDataset dataset) {
        if (!isIntact(dataset)) {
            throw new IllegalStateException("市场调研数据集SHA-256不匹配: " + dataset.getDatasetId());
        }
        try {
            return objectMapper.readTree(dataset.getSourcePayload());
        } catch (Exception exception) {
            throw new IllegalStateException("市场调研数据集JSON损坏: " + dataset.getDatasetId(), exception);
        }
    }

    private void saveDataset(
            MarketResearchJob job,
            ResearchPhase phase,
            ResearchDataset source,
            String requestPayload,
            String requestHash) {
        try {
            String sourcePayload = objectMapper.writeValueAsString(source.getPayload());
            Optional<MarketResearchDataset> existing = datasetDao.findByIdempotencyKey(
                    job.getJobId(),
                    phase.getNodeCode(),
                    source.getOperation(),
                    source.getDatasetCode(),
                    requestHash);
            if (existing.isPresent()) {
                if (isIntact(existing.get())) {
                    return;
                }
                throw new IllegalStateException("已有市场调研数据集完整性校验失败: "
                        + existing.get().getDatasetId());
            }

            MarketResearchDataset dataset = new MarketResearchDataset();
            dataset.setDatasetId(idGenerator.nextId());
            dataset.setJobId(job.getJobId());
            dataset.setNodeCode(phase.getNodeCode());
            dataset.setOperation(source.getOperation());
            dataset.setDatasetCode(source.getDatasetCode());
            dataset.setRequestHash(requestHash);
            dataset.setDataSourceMode(job.getDataSourceMode());
            dataset.setRequestPayload(requestPayload);
            dataset.setSourcePayload(sourcePayload);
            dataset.setNormalizedPayload(null);
            dataset.setRecordCount(source.getRecordCount());
            dataset.setSchemaVersion(ResearchConstants.DATASET_SCHEMA_VERSION);
            dataset.setValidationStatus(ResearchDatasetValidationStatus.VALID.name());
            dataset.setValidationSummary(VALIDATION_SUMMARY);
            dataset.setSha256(ResearchHashUtils.sha256(sourcePayload));
            dataset.setFetchedAt(System.currentTimeMillis());
            if (!datasetDao.save(dataset)) {
                throw new IllegalStateException("保存市场调研数据集失败: " + source.getDatasetCode());
            }
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("序列化或保存市场调研数据集失败: "
                    + source.getDatasetCode(), exception);
        }
    }

    private boolean isIntact(MarketResearchDataset dataset) {
        return dataset != null
                && ResearchDatasetValidationStatus.VALID.name().equals(dataset.getValidationStatus())
                && ResearchConstants.DATASET_SCHEMA_VERSION.equals(dataset.getSchemaVersion())
                && dataset.getSourcePayload() != null
                && Objects.equals(dataset.getSha256(), ResearchHashUtils.sha256(dataset.getSourcePayload()));
    }
}
