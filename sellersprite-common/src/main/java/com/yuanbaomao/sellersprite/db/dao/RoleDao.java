package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuanbaomao.sellersprite.db.entity.Role;
import java.util.Optional;

public interface RoleDao extends IService<Role> {

    Optional<Role> findByRoleCode(String roleCode);

    boolean existsByRoleCode(String roleCode);

    boolean existsByRoleCodeExcludingRoleId(String roleCode, String roleId);

    Page<Role> pageRoles(String roleName, Integer status, long current, long size);

    long countByStatus(Integer status);
}
