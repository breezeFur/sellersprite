package cyou.yuanbaomao.sellersprite.ai.research.curation.evidence;

import cyou.yuanbaomao.sellersprite.ai.research.curation.model.ProductWorkbook;
import java.util.List;

/**
 * 把持久化 evidence 快照按正式证据目录组装为 Curation 工作簿。
 */
public interface EvidenceWorkbookAssembler {

    ProductWorkbook assemble(String jobId, List<EvidenceDatasetPayload> datasets);
}
