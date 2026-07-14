package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    @Override
    public boolean existsByRoleCodeExcludingRoleId(String roleCode, String roleId) {
        return lambdaQuery()
                .eq(Role::getRoleCode, roleCode)
                .ne(Role::getRoleId, roleId)
                .exists();
    }

    @Override
    public Page<Role> pageRoles(String roleName, Integer status, long current, long size) {
        return lambdaQuery()
                .like(roleName != null && !roleName.isBlank(), Role::getRoleName, roleName)
                .eq(status != null, Role::getStatus, status)
                .orderByAsc(Role::getSortOrder)
                .orderByAsc(Role::getRoleId)
                .page(Page.of(current, size));
    }

    @Override
    public long countByStatus(Integer status) {
        return lambdaQuery().eq(Role::getStatus, status).count();
    }
}
