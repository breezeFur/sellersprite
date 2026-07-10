package com.yuanbaomao.sellersprite.system.role.service.impl;

import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.DeptDao;
import com.yuanbaomao.sellersprite.db.dao.RoleDao;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.dao.UserRoleDao;
import com.yuanbaomao.sellersprite.db.entity.Role;
import com.yuanbaomao.sellersprite.db.entity.UserRole;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import com.yuanbaomao.sellersprite.system.convert.SystemConverter;
import com.yuanbaomao.sellersprite.system.role.model.dto.RoleCreateRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.UserRoleBindRequest;
import com.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import com.yuanbaomao.sellersprite.system.role.service.RoleService;
import java.util.List;
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
        if (userDao.getById(request.getUserId()) == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        if (roleDao.getById(request.getRoleId()) == null) {
            throw new BizException(ResultCode.ROLE_NOT_FOUND);
        }
        if (deptDao.getById(request.getDeptId()) == null) {
            throw new BizException(ResultCode.DEPT_NOT_FOUND);
        }
        UserRole entity = new UserRole();
        entity.setUserId(request.getUserId());
        entity.setRoleId(request.getRoleId());
        entity.setDeptId(request.getDeptId());
        entity.setPrimaryRole(request.getPrimaryRole() == null ? SystemBusinessConstants.NO : request.getPrimaryRole());
        entity.setRemark("");
        userRoleDao.save(entity);
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

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
