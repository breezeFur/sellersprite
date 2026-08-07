package cyou.yuanbaomao.sellersprite.research.service;

import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.sellersprite.research.model.ResearchDownload;
import cyou.yuanbaomao.sellersprite.research.model.dto.ResearchJobCreateRequest;
import cyou.yuanbaomao.sellersprite.research.model.dto.ResearchJobPageRequest;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobCreatedVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobDetailVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchJobHistoryVo;
import cyou.yuanbaomao.sellersprite.research.model.vo.ResearchNodeExecutionVo;
import java.util.List;

public interface ResearchJobService {

    ResearchJobCreatedVo create(ResearchJobCreateRequest request);

    PageResult<ResearchJobHistoryVo> page(ResearchJobPageRequest request);

    ResearchJobDetailVo detail(String jobId);

    List<ResearchNodeExecutionVo> nodes(String jobId);

    void cancel(String jobId);

    void retry(String jobId);

    ResearchDownload downloadArtifact(String jobId, String artifactId);
}
