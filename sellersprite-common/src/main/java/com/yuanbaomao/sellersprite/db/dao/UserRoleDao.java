package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.UserRole;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRoleDao extends IService<UserRole> {

    List<UserRole> listByUserId(String userId);

    List<UserRole> listByRoleIds(Collection<String> roleIds);

    boolean existsByRoleId(String roleId);

    boolean existsByDeptId(String deptId);

    Optional<UserRole> findByUserIdAndRoleId(String userId, String roleId);

    void removeByUserIdAndRoleId(String userId, String roleId);

    void clearPrimaryRoleByUserId(String userId);

    void replaceByUserId(String userId, String deptId, Collection<String> roleIds);
}
