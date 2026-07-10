package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.AiPromptRecordDao;
import com.yuanbaomao.sellersprite.db.entity.AiPromptRecord;
import com.yuanbaomao.sellersprite.db.mapper.AiPromptRecordMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AiPromptRecordDaoImpl extends ServiceImpl<AiPromptRecordMapper, AiPromptRecord>
        implements AiPromptRecordDao {
}
