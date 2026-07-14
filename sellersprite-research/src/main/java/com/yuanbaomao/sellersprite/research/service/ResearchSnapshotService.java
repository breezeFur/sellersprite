package com.yuanbaomao.sellersprite.research.service;

import com.yuanbaomao.base.id.IdGenerator;
import com.yuanbaomao.sellersprite.db.dao.MarketResearchSnapshotDao;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchJob;
import com.yuanbaomao.sellersprite.db.entity.MarketResearchSnapshot;
import com.yuanbaomao.sellersprite.research.enums.ResearchPhase;
import com.yuanbaomao.sellersprite.research.model.ResearchDataset;
import com.yuanbaomao.sellersprite.research.model.ResearchInput;
import com.yuanbaomao.sellersprite.research.support.ResearchHashUtils;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * 持久化可重启复用的数据采集快照。
 */
@Service
@RequiredArgsConstructor
public class ResearchSnapshotService {

    private final MarketResearchSnapshotDao snapshotDao;
    private final IdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public boolean hasPhaseSnapshot(String jobId, ResearchPhase phase) {
        List<MarketResearchSnapshot> phaseSnapshots = snapshotDao.listByJobId(jobId).stream()
                .filter(snapshot -> phase.name().equals(snapshot.getPhase()))
                .toList();
        return !phaseSnapshots.isEmpty() && phaseSnapshots.stream().allMatch(this::isIntact);
    }

    @Transactional(readOnly = true)
    public List<MarketResearchSnapshot> listByJobId(String jobId) {
        return snapshotDao.listByJobId(jobId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveDatasets(
            MarketResearchJob job,
            ResearchPhase phase,
            ResearchInput input,
            List<ResearchDataset> datasets) {
        for (ResearchDataset dataset : datasets) {
            saveDataset(job, phase, input, dataset);
        }
    }

    @Transactional(readOnly = true)
    public JsonNode readPayload(MarketResearchSnapshot snapshot) {
        if (!isIntact(snapshot)) {
            throw new IllegalStateException("市场调研快照SHA-256不匹配: " + snapshot.getSnapshotId());
        }
        try {
            return objectMapper.readTree(snapshot.getResponsePayload());
        } catch (Exception exception) {
            throw new IllegalStateException("市场调研快照JSON损坏: " + snapshot.getSnapshotId(), exception);
        }
    }

    private void saveDataset(
            MarketResearchJob job,
            ResearchPhase phase,
            ResearchInput input,
            ResearchDataset dataset) {
        String businessKey = dataset.getDatasetCode();
        try {
            String responsePayload = objectMapper.writeValueAsString(dataset.getPayload());
            Optional<MarketResearchSnapshot> existing = snapshotDao.findByIdempotencyKey(
                    job.getJobId(), phase.name(), dataset.getOperation(), businessKey);
            if (existing.filter(this::isIntact).isPresent()) {
                return;
            }
            MarketResearchSnapshot snapshot = existing.orElseGet(MarketResearchSnapshot::new);
            if (snapshot.getSnapshotId() == null) {
                snapshot.setSnapshotId(idGenerator.nextId());
            }
            snapshot.setJobId(job.getJobId());
            snapshot.setPhase(phase.name());
            snapshot.setOperation(dataset.getOperation());
            snapshot.setBusinessKey(businessKey);
            snapshot.setDataSourceMode(job.getDataSourceMode());
            snapshot.setRequestPayload(objectMapper.writeValueAsString(input));
            snapshot.setResponsePayload(responsePayload);
            snapshot.setRecordCount(dataset.getRecordCount());
            snapshot.setSha256(ResearchHashUtils.sha256(responsePayload));
            snapshot.setFetchedAt(System.currentTimeMillis());
            boolean persisted = existing.isPresent()
                    ? snapshotDao.updateById(snapshot)
                    : snapshotDao.save(snapshot);
            if (!persisted) {
                throw new IllegalStateException("保存市场调研快照失败: " + dataset.getDatasetCode());
            }
        } catch (Exception exception) {
            throw new IllegalStateException("序列化或保存市场调研快照失败: " + dataset.getDatasetCode(), exception);
        }
    }

    private boolean isIntact(MarketResearchSnapshot snapshot) {
        return snapshot != null
                && snapshot.getResponsePayload() != null
                && Objects.equals(
                        snapshot.getSha256(),
                        ResearchHashUtils.sha256(snapshot.getResponsePayload()));
    }
}
