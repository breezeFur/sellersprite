package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.RoleFunctionDao;
import com.yuanbaomao.sellersprite.db.entity.RoleFunction;
import com.yuanbaomao.sellersprite.db.mapper.RoleFunctionMapper;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class RoleFunctionDaoImpl extends ServiceImpl<RoleFunctionMapper, RoleFunction>
        implements RoleFunctionDao {

    @Override
    public List<RoleFunction> listByRoleIds(Collection<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(RoleFunction::getRoleId, roleIds).list();
    }
}
