package com.yuanbaomao.sellersprite.system.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.dao.UserRoleDao;
import com.yuanbaomao.sellersprite.db.dao.UserTokenDao;
import com.yuanbaomao.sellersprite.db.dao.RoleDao;
import com.yuanbaomao.sellersprite.db.dao.DeptDao;
import com.yuanbaomao.sellersprite.db.entity.User;
import com.yuanbaomao.sellersprite.db.entity.Role;
import com.yuanbaomao.sellersprite.db.entity.Dept;
import com.yuanbaomao.sellersprite.db.entity.UserRole;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.base.context.RequestContextHolder;
import com.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import com.yuanbaomao.sellersprite.system.convert.SystemConverter;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserCreateRequest;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserPageRequest;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserUpdateRequest;
import com.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import com.yuanbaomao.sellersprite.system.user.service.UserService;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final UserTokenDao userTokenDao;
    private final UserRoleDao userRoleDao;
    private final RoleDao roleDao;
    private final DeptDao deptDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDetailVo create(UserCreateRequest request) {
        if (userDao.existsByUsername(request.getUsername())) {
            throw new BizException(ResultCode.USERNAME_ALREADY_EXISTS);
        }
        validateDepartment(request.getPrimaryDeptId());
        User entity = new User();
        entity.setUsername(request.getUsername());
        entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        entity.setNickname(defaultString(request.getNickname()));
        entity.setRealName(defaultString(request.getRealName()));
        entity.setAvatarUrl("");
        entity.setMobile(request.getMobile());
        entity.setEmail(request.getEmail());
        entity.setGender(0);
        entity.setPrimaryDeptId(request.getPrimaryDeptId());
        entity.setStatus(SystemBusinessConstants.STATUS_ENABLED);
        entity.setPasswordUpdatedAt(System.currentTimeMillis());
        entity.setRemark("");
        userDao.save(entity);
        replaceRolesInternal(entity, request.getRoleIds());
        return toDetailVo(entity);
    }

    @Override
    public UserDetailVo detail(String userId) {
        User entity = userDao.getById(userId);
        if (entity == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        return toDetailVo(entity);
    }

    @Override
    public PageResult<UserDetailVo> page(UserPageRequest request) {
        Page<User> page = userDao.pageUsers(request.getUsername(), request.getStatus(), request.getCurrent(),
                request.getSize());
        List<UserDetailVo> records = page.getRecords().stream()
                .map(this::toDetailVo)
                .toList();
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDetailVo update(String userId, UserUpdateRequest request) {
        User user = requireUser(userId);
        if (userDao.existsByUsernameExcludingUserId(request.getUsername(), userId)) {
            throw new BizException(ResultCode.USERNAME_ALREADY_EXISTS);
        }
        validateDepartment(request.getPrimaryDeptId());
        List<String> existingRoleIds = userRoleDao.listByUserId(userId).stream()
                .map(UserRole::getRoleId)
                .toList();
        boolean departmentChanged = !java.util.Objects.equals(user.getPrimaryDeptId(), request.getPrimaryDeptId());
        user.setUsername(request.getUsername());
        user.setNickname(defaultString(request.getNickname()));
        user.setRealName(defaultString(request.getRealName()));
        user.setMobile(request.getMobile());
        user.setEmail(request.getEmail());
        user.setPrimaryDeptId(request.getPrimaryDeptId());
        userDao.updateById(user);
        if (departmentChanged && !existingRoleIds.isEmpty()) {
            replaceRolesInternal(user, existingRoleIds);
        }
        return toDetailVo(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String userId, Integer status) {
        if (!Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(status)
                && !Integer.valueOf(SystemBusinessConstants.STATUS_DISABLED).equals(status)) {
            throw new BizException(ResultCode.PARAM_INVALID);
        }
        User user = requireUser(userId);
        user.setStatus(status);
        userDao.updateById(user);
        if (Integer.valueOf(SystemBusinessConstants.STATUS_DISABLED).equals(status)) {
            revokeUserSessions(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String userId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new BizException(ResultCode.PARAM_INVALID);
        }
        User user = requireUser(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordUpdatedAt(System.currentTimeMillis());
        userDao.updateById(user);
        revokeUserSessions(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceRoles(String userId, Collection<String> roleIds) {
        replaceRolesInternal(requireUser(userId), roleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String userId) {
        String currentUserId = RequestContextHolder.get().map(context -> context.getUserId()).orElse(null);
        if (userId != null && userId.equals(currentUserId)) {
            throw new BizException(ResultCode.CURRENT_USER_OPERATION_FORBIDDEN);
        }
        requireUser(userId);
        revokeUserSessions(userId);
        userRoleDao.replaceByUserId(userId, null, Set.of());
        userDao.removeById(userId);
    }

    private void replaceRolesInternal(User user, Collection<String> roleIds) {
        Set<String> normalizedRoleIds = normalizeIds(roleIds);
        if (!normalizedRoleIds.isEmpty()) {
            if (user.getPrimaryDeptId() == null || user.getPrimaryDeptId().isBlank()) {
                throw new BizException(ResultCode.PARAM_INVALID, "用户未设置主部门，无法分配角色");
            }
            validateDepartment(user.getPrimaryDeptId());
            Set<String> enabledRoleIds = roleDao.listByIds(normalizedRoleIds).stream()
                    .filter(role -> Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(role.getStatus()))
                    .map(Role::getRoleId)
                    .collect(java.util.stream.Collectors.toSet());
            if (!enabledRoleIds.equals(normalizedRoleIds)) {
                throw new BizException(ResultCode.ROLE_NOT_FOUND);
            }
        }
        userRoleDao.replaceByUserId(user.getUserId(), user.getPrimaryDeptId(), normalizedRoleIds);
        userDao.incrementPermissionVersion(Set.of(user.getUserId()));
    }

    private Set<String> normalizeIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String id : ids) {
            if (id == null || id.isBlank()) {
                throw new BizException(ResultCode.PARAM_INVALID);
            }
            normalized.add(id);
        }
        return normalized;
    }

    private void validateDepartment(String departmentId) {
        if (departmentId == null) {
            return;
        }
        if (departmentId.isBlank()) {
            throw new BizException(ResultCode.PARAM_INVALID);
        }
        Dept department = deptDao.getById(departmentId);
        if (department == null
                || !Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(department.getStatus())) {
            throw new BizException(ResultCode.DEPT_NOT_FOUND);
        }
    }

    private UserDetailVo toDetailVo(User user) {
        UserDetailVo detail = SystemConverter.toUserDetailVo(user);
        detail.setRoleIds(userRoleDao.listByUserId(user.getUserId()).stream()
                .map(UserRole::getRoleId)
                .distinct()
                .toList());
        return detail;
    }

    private User requireUser(String userId) {
        User user = userDao.getById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void revokeUserSessions(String userId) {
        userTokenDao.revokeByUserId(userId, System.currentTimeMillis(),
                SystemBusinessConstants.TOKEN_REVOKE_REASON_USER_SECURITY_CHANGED);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
