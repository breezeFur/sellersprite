package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.RoleDao;
import com.yuanbaomao.sellersprite.db.entity.Role;
import com.yuanbaomao.sellersprite.db.mapper.RoleMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RoleDaoImpl extends ServiceImpl<RoleMapper, Role> implements RoleDao {

    @Override
    public Optional<Role> findByRoleCode(String roleCode) {
        return Optional.ofNullable(lambdaQuery().eq(Role::getRoleCode, roleCode).one());
    }

    @Override
    public boolean existsByRoleCode(String roleCode) {
        return lambdaQuery().eq(Role::getRoleCode, roleCode).exists();
    }
}
