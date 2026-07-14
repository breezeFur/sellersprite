package com.yuanbaomao.sellersprite.system.role.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.DeptDao;
import com.yuanbaomao.sellersprite.db.dao.RoleDao;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.dao.UserRoleDao;
import com.yuanbaomao.sellersprite.db.entity.Dept;
import com.yuanbaomao.sellersprite.db.entity.Role;
import com.yuanbaomao.sellersprite.db.entity.User;
import com.yuanbaomao.sellersprite.db.entity.UserRole;
import com.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import com.yuanbaomao.sellersprite.system.convert.SystemConverter;
import com.yuanbaomao.sellersprite.system.permission.service.RolePermissionService;
import com.yuanbaomao.sellersprite.system.role.model.dto.RoleCreateRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.RolePageRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.RolePermissionReplaceRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.RoleUpdateRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.RoleUserPageRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.UserRoleBindRequest;
import com.yuanbaomao.sellersprite.system.role.model.vo.RolePermissionVo;
import com.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import com.yuanbaomao.sellersprite.system.role.service.RoleService;
import com.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleDao roleDao;
    private final UserDao userDao;
    private final DeptDao deptDao;
    private final UserRoleDao userRoleDao;
    private final RolePermissionService rolePermissionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVo create(RoleCreateRequest request) {
        if (roleDao.existsByRoleCode(request.getRoleCode())) {
            throw new BizException(ResultCode.ROLE_CODE_ALREADY_EXISTS);
        }
        Role entity = new Role();
        entity.setRoleCode(request.getRoleCode());
        entity.setRoleName(request.getRoleName());
        entity.setRoleType(defaultIfBlank(request.getRoleType(), SystemBusinessConstants.DEFAULT_ROLE_TYPE));
        entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        entity.setStatus(SystemBusinessConstants.STATUS_ENABLED);
        entity.setRemark(defaultString(request.getRemark()));
        roleDao.save(entity);
        return SystemConverter.toRoleVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindUserRole(UserRoleBindRequest request) {
        User user = userDao.getById(request.getUserId());
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        Role role = roleDao.getById(request.getRoleId());
        if (role == null || !Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(role.getStatus())) {
            throw new BizException(ResultCode.ROLE_NOT_FOUND);
        }
        Dept department = deptDao.getById(request.getDeptId());
        if (department == null
                || !Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(department.getStatus())) {
            throw new BizException(ResultCode.DEPT_NOT_FOUND);
        }
        if (userRoleDao.findByUserIdAndRoleId(request.getUserId(), request.getRoleId()).isPresent()) {
            throw new BizException(ResultCode.RESOURCE_CONFLICT, "用户已绑定该角色");
        }
        List<UserRole> existingRoles = userRoleDao.listByUserId(request.getUserId());
        boolean primaryRole = Integer.valueOf(SystemBusinessConstants.YES).equals(request.getPrimaryRole())
                || existingRoles.isEmpty();
        if (primaryRole) {
            userRoleDao.clearPrimaryRoleByUserId(request.getUserId());
        }
        UserRole entity = new UserRole();
        entity.setUserId(request.getUserId());
        entity.setRoleId(request.getRoleId());
        entity.setDeptId(request.getDeptId());
        entity.setPrimaryRole(primaryRole ? SystemBusinessConstants.YES : SystemBusinessConstants.NO);
        entity.setRemark("");
        userRoleDao.save(entity);
        userDao.incrementPermissionVersion(java.util.Set.of(request.getUserId()));
    }

    @Override
    public List<RoleVo> listEnabled() {
        return roleDao.lambdaQuery()
                .eq(Role::getStatus, SystemBusinessConstants.STATUS_ENABLED)
                .orderByAsc(Role::getSortOrder)
                .list()
                .stream()
                .map(SystemConverter::toRoleVo)
                .toList();
    }

    @Override
    public PageResult<RoleVo> page(RolePageRequest request) {
        Page<Role> page = roleDao.pageRoles(request.getRoleName(), request.getStatus(), request.getCurrent(),
                request.getSize());
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords().stream()
                .map(SystemConverter::toRoleVo)
                .toList());
    }

    @Override
    public RoleVo detail(String roleId) {
        return SystemConverter.toRoleVo(requireRole(roleId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoleVo update(String roleId, RoleUpdateRequest request) {
        Role role = requireRole(roleId);
        if (roleDao.existsByRoleCodeExcludingRoleId(request.getRoleCode(), roleId)) {
            throw new BizException(ResultCode.ROLE_CODE_ALREADY_EXISTS);
        }
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setRoleType(defaultIfBlank(request.getRoleType(), SystemBusinessConstants.DEFAULT_ROLE_TYPE));
        role.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        role.setRemark(defaultString(request.getRemark()));
        roleDao.updateById(role);
        return SystemConverter.toRoleVo(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String roleId, Integer status) {
        if (!Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(status)
                && !Integer.valueOf(SystemBusinessConstants.STATUS_DISABLED).equals(status)) {
            throw new BizException(ResultCode.PARAM_INVALID);
        }
        Role role = requireRole(roleId);
        if (Integer.valueOf(role.getStatus()).equals(status)) {
            return;
        }
        role.setStatus(status);
        roleDao.updateById(role);
        incrementAssignedUserPermissionVersions(roleId);
    }

    @Override
    public PageResult<UserDetailVo> listUsers(String roleId, RoleUserPageRequest request) {
        requireRole(roleId);
        Page<User> page = userDao.pageByRoleId(roleId, request.getUsername(), request.getCurrent(), request.getSize());
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords().stream()
                .map(SystemConverter::toUserDetailVo)
                .toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindUser(String roleId, String userId) {
        requireRole(roleId);
        if (userDao.getById(userId) == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        UserRole binding = userRoleDao.findByUserIdAndRoleId(userId, roleId)
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND, "用户未绑定该角色"));
        userRoleDao.removeByUserIdAndRoleId(userId, roleId);
        if (Integer.valueOf(SystemBusinessConstants.YES).equals(binding.getPrimaryRole())) {
            promoteFirstRemainingRole(userId);
        }
        userDao.incrementPermissionVersion(Set.of(userId));
    }

    @Override
    public RolePermissionVo getPermissions(String roleId) {
        return rolePermissionService.getRolePermissions(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RolePermissionVo replacePermissions(String roleId, RolePermissionReplaceRequest request) {
        rolePermissionService.replaceRolePermissions(roleId, request.getFunctionIds(), request.getExtraApiIds());
        return rolePermissionService.getRolePermissions(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String roleId) {
        requireRole(roleId);
        if (userRoleDao.existsByRoleId(roleId)) {
            throw new BizException(ResultCode.RESOURCE_CONFLICT, "角色仍被用户引用，无法删除");
        }
        rolePermissionService.clearRolePermissions(roleId);
        roleDao.removeById(roleId);
    }

    private Role requireRole(String roleId) {
        Role role = roleDao.getById(roleId);
        if (role == null) {
            throw new BizException(ResultCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    private void promoteFirstRemainingRole(String userId) {
        List<UserRole> remainingRoles = userRoleDao.listByUserId(userId);
        if (remainingRoles.isEmpty()) {
            return;
        }
        UserRole primaryRole = remainingRoles.getFirst();
        primaryRole.setPrimaryRole(SystemBusinessConstants.YES);
        userRoleDao.updateById(primaryRole);
    }

    private void incrementAssignedUserPermissionVersions(String roleId) {
        Set<String> userIds = userRoleDao.listByRoleIds(Set.of(roleId)).stream()
                .map(UserRole::getUserId)
                .collect(java.util.stream.Collectors.toSet());
        if (!userIds.isEmpty()) {
            userDao.incrementPermissionVersion(userIds);
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
