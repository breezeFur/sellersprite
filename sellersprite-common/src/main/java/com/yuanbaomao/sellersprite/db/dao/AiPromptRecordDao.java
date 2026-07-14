package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.AiPromptRecord;
import java.util.Optional;

public interface AiPromptRecordDao extends IService<AiPromptRecord> {

    long countByCreatedAtRange(long startTime, long endTime);

    Page<AiPromptRecord> page(String userId, String conversationId, String provider, String model,
                              String status, Long startTime, Long endTime, long current, long size);

    Optional<AiPromptRecord> findById(String promptRecordId);
}
