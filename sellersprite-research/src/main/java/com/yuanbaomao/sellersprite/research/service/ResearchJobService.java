package com.yuanbaomao.sellersprite.research.service;

import com.yuanbaomao.sellersprite.research.model.ResearchDownload;
import com.yuanbaomao.sellersprite.research.model.dto.ResearchJobCreateRequest;
import com.yuanbaomao.sellersprite.research.model.vo.ResearchJobCreatedVo;
import com.yuanbaomao.sellersprite.research.model.vo.ResearchJobDetailVo;

public interface ResearchJobService {

    ResearchJobCreatedVo create(ResearchJobCreateRequest request);

    ResearchJobDetailVo detail(String jobId);

    ResearchDownload download(String jobId);
}
