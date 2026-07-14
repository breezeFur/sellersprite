package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.FunctionApiDao;
import com.yuanbaomao.sellersprite.db.entity.FunctionApi;
import com.yuanbaomao.sellersprite.db.mapper.FunctionApiMapper;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class FunctionApiDaoImpl extends ServiceImpl<FunctionApiMapper, FunctionApi> implements FunctionApiDao {

    @Override
    public List<FunctionApi> listByFunctionIds(Collection<String> functionIds) {
        if (functionIds == null || functionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(FunctionApi::getSysFunctionId, functionIds).list();
    }

    @Override
    public boolean existsByApiId(String apiId) {
        return lambdaQuery().eq(FunctionApi::getSysApiId, apiId).exists();
    }

    @Override
    public void replaceByFunctionId(String functionId, Collection<String> apiIds) {
        baseMapper.deletePhysicallyByFunctionId(functionId);
        if (apiIds == null || apiIds.isEmpty()) {
            return;
        }
        List<FunctionApi> bindings = apiIds.stream().map(apiId -> binding(functionId, apiId)).toList();
        saveBatch(bindings);
    }

    private FunctionApi binding(String functionId, String apiId) {
        FunctionApi binding = new FunctionApi();
        binding.setSysFunctionId(functionId);
        binding.setSysApiId(apiId);
        binding.setRemark("");
        return binding;
    }
}
