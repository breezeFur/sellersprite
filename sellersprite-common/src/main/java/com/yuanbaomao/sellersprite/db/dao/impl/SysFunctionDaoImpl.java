package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.SysFunctionDao;
import com.yuanbaomao.sellersprite.db.entity.SysFunction;
import com.yuanbaomao.sellersprite.db.mapper.SysFunctionMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class SysFunctionDaoImpl extends ServiceImpl<SysFunctionMapper, SysFunction> implements SysFunctionDao {

    private static final int ENABLED_STATUS = 1;

    @Override
    public List<SysFunction> listEnabled() {
        return lambdaQuery()
                .eq(SysFunction::getStatus, ENABLED_STATUS)
                .orderByAsc(SysFunction::getSortOrder)
                .list();
    }

    @Override
    public boolean existsByFunctionCode(String functionCode) {
        return lambdaQuery().eq(SysFunction::getFunctionCode, functionCode).exists();
    }
}
