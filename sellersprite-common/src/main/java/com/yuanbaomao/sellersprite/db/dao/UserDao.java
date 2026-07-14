package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuanbaomao.sellersprite.db.entity.User;
import java.util.Collection;
import java.util.Optional;

public interface UserDao extends IService<User> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameExcludingUserId(String username, String userId);

    boolean incrementPermissionVersion(Collection<String> userIds);

    Page<User> pageUsers(String username, Integer status, long current, long size);

    Page<User> pageByRoleId(String roleId, String username, long current, long size);

    boolean existsByPrimaryDeptId(String deptId);
}
