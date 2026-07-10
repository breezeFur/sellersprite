package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.SysApiDao;
import com.yuanbaomao.sellersprite.db.entity.SysApi;
import com.yuanbaomao.sellersprite.db.mapper.SysApiMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class SysApiDaoImpl extends ServiceImpl<SysApiMapper, SysApi> implements SysApiDao {

    private static final int ENABLED_STATUS = 1;
    private static final String PUBLIC_API_TYPE = "PUBLIC";

    @Override
    public List<SysApi> listPublicApis() {
        return lambdaQuery()
                .eq(SysApi::getApiType, PUBLIC_API_TYPE)
                .eq(SysApi::getStatus, ENABLED_STATUS)
                .list();
    }

    @Override
    public boolean existsByApiCode(String apiCode) {
        return lambdaQuery().eq(SysApi::getApiCode, apiCode).exists();
    }
}
