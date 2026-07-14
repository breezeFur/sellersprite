package com.yuanbaomao.sellersprite.research.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
class ResearchSnapshotServiceTest {

    private static final String JOB_ID = "job-snapshot-001";
    private static final String SNAPSHOT_ID = "snapshot-001";

    @Mock
    private MarketResearchSnapshotDao snapshotDao;

    @Mock
    private IdGenerator idGenerator;

    private ObjectMapper objectMapper;
    private ResearchSnapshotService snapshotService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        snapshotService = new ResearchSnapshotService(snapshotDao, idGenerator, objectMapper);
    }

    @Test
    void shouldPersistDeterministicPayloadHashAndRequestContext() throws Exception {
        MarketResearchJob job = job();
        ResearchInput input = input();
        ObjectNode payload = objectMapper.createObjectNode();
        payload.putArray("items").addObject().put("asin", "B0TEST0001");
        ResearchDataset dataset = new ResearchDataset(
                "products", "PRODUCT_RESEARCH", payload, 1);

        when(snapshotDao.findByIdempotencyKey(
                        JOB_ID,
                        ResearchPhase.COLLECT_MARKET_AND_PRODUCTS.name(),
                        "PRODUCT_RESEARCH",
                        "products"))
                .thenReturn(Optional.empty());
        when(idGenerator.nextId()).thenReturn(SNAPSHOT_ID);
        when(snapshotDao.save(org.mockito.ArgumentMatchers.any(MarketResearchSnapshot.class)))
                .thenReturn(true);

        snapshotService.saveDatasets(
                job,
                ResearchPhase.COLLECT_MARKET_AND_PRODUCTS,
                input,
                List.of(dataset));

        ArgumentCaptor<MarketResearchSnapshot> captor =
                ArgumentCaptor.forClass(MarketResearchSnapshot.class);
        verify(snapshotDao).save(captor.capture());
        MarketResearchSnapshot saved = captor.getValue();
        String expectedPayload = objectMapper.writeValueAsString(payload);
        assertThat(saved.getSnapshotId()).isEqualTo(SNAPSHOT_ID);
        assertThat(saved.getJobId()).isEqualTo(JOB_ID);
        assertThat(saved.getBusinessKey()).isEqualTo("products");
        assertThat(saved.getOperation()).isEqualTo("PRODUCT_RESEARCH");
        assertThat(saved.getDataSourceMode()).isEqualTo("MOCK");
        assertThat(saved.getResponsePayload()).isEqualTo(expectedPayload);
        assertThat(saved.getRequestPayload()).isEqualTo(objectMapper.writeValueAsString(input));
        assertThat(saved.getSha256()).isEqualTo(ResearchHashUtils.sha256(expectedPayload));
        assertThat(saved.getRecordCount()).isOne();
        assertThat(saved.getFetchedAt()).isPositive();
    }

    @Test
    void shouldSkipExistingDatasetByStableIdempotencyKey() {
        MarketResearchJob job = job();
        ResearchDataset dataset = new ResearchDataset(
                "products",
                "PRODUCT_RESEARCH",
                objectMapper.createObjectNode(),
                0);
        MarketResearchSnapshot existing = new MarketResearchSnapshot();
        existing.setResponsePayload("{}");
        existing.setSha256(ResearchHashUtils.sha256("{}"));
        when(snapshotDao.findByIdempotencyKey(
                        JOB_ID,
                        ResearchPhase.COLLECT_MARKET_AND_PRODUCTS.name(),
                        "PRODUCT_RESEARCH",
                        "products"))
                .thenReturn(Optional.of(existing));

        snapshotService.saveDatasets(
                job,
                ResearchPhase.COLLECT_MARKET_AND_PRODUCTS,
                input(),
                List.of(dataset));

        verify(snapshotDao, never()).save(org.mockito.ArgumentMatchers.any());
        verify(snapshotDao, never()).updateById(org.mockito.ArgumentMatchers.any());
        verify(idGenerator, never()).nextId();
    }

    @Test
    void shouldRejectTamperedSnapshotAndNotReuseItsPhase() {
        MarketResearchSnapshot snapshot = new MarketResearchSnapshot();
        snapshot.setSnapshotId(SNAPSHOT_ID);
        snapshot.setPhase(ResearchPhase.COLLECT_KEYWORDS.name());
        snapshot.setResponsePayload("{\"items\":[]}");
        snapshot.setSha256(ResearchHashUtils.sha256("{\"items\":[1]}"));
        when(snapshotDao.listByJobId(JOB_ID)).thenReturn(List.of(snapshot));

        assertThat(snapshotService.hasPhaseSnapshot(JOB_ID, ResearchPhase.COLLECT_KEYWORDS))
                .isFalse();
        assertThatThrownBy(() -> snapshotService.readPayload(snapshot))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SHA-256不匹配");
    }

    @Test
    void shouldRepairCorruptedSnapshotWithFreshDataset() {
        MarketResearchJob job = job();
        ObjectNode freshPayload = objectMapper.createObjectNode();
        freshPayload.putArray("items");
        ResearchDataset dataset = new ResearchDataset(
                "products", "PRODUCT_RESEARCH", freshPayload, 0);
        MarketResearchSnapshot corrupted = new MarketResearchSnapshot();
        corrupted.setSnapshotId(SNAPSHOT_ID);
        corrupted.setResponsePayload("broken");
        corrupted.setSha256("0".repeat(64));
        when(snapshotDao.findByIdempotencyKey(
                        JOB_ID,
                        ResearchPhase.COLLECT_MARKET_AND_PRODUCTS.name(),
                        "PRODUCT_RESEARCH",
                        "products"))
                .thenReturn(Optional.of(corrupted));
        when(snapshotDao.updateById(corrupted)).thenReturn(true);

        snapshotService.saveDatasets(
                job,
                ResearchPhase.COLLECT_MARKET_AND_PRODUCTS,
                input(),
                List.of(dataset));

        assertThat(corrupted.getResponsePayload()).isEqualTo("{\"items\":[]}");
        assertThat(corrupted.getSha256())
                .isEqualTo(ResearchHashUtils.sha256(corrupted.getResponsePayload()));
        verify(snapshotDao).updateById(corrupted);
        verify(snapshotDao, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private MarketResearchJob job() {
        MarketResearchJob job = new MarketResearchJob();
        job.setJobId(JOB_ID);
        job.setDataSourceMode("MOCK");
        return job;
    }

    private ResearchInput input() {
        return ResearchInput.builder()
                .jobId(JOB_ID)
                .marketplace("US")
                .keyword("facial cleansing device")
                .seedAsins(List.of("B0TEST0001"))
                .build();
    }
}
