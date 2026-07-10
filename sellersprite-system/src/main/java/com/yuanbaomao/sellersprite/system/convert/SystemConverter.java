package com.yuanbaomao.sellersprite.system.convert;

import com.yuanbaomao.sellersprite.db.entity.Dept;
import com.yuanbaomao.dict.mybatis.entity.DictItemEntity;
import com.yuanbaomao.dict.mybatis.entity.DictTypeEntity;
import com.yuanbaomao.sellersprite.db.entity.Role;
import com.yuanbaomao.sellersprite.db.entity.SysApi;
import com.yuanbaomao.sellersprite.db.entity.SysFunction;
import com.yuanbaomao.sellersprite.db.entity.User;
import com.yuanbaomao.sellersprite.system.dept.model.vo.DeptVo;
import com.yuanbaomao.sellersprite.system.dict.model.vo.DictItemVo;
import com.yuanbaomao.sellersprite.system.dict.model.vo.DictTypeVo;
import com.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.SysApiVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.SysFunctionVo;
import com.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import java.util.List;

public final class SystemConverter {

    private SystemConverter() {
    }

    public static UserDetailVo toUserDetailVo(User entity) {
        if (entity == null) {
            return null;
        }
        UserDetailVo vo = new UserDetailVo();
        vo.setUserId(entity.getUserId());
        vo.setUsername(entity.getUsername());
        vo.setNickname(entity.getNickname());
        vo.setRealName(entity.getRealName());
        vo.setAvatarUrl(entity.getAvatarUrl());
        vo.setMobile(entity.getMobile());
        vo.setEmail(entity.getEmail());
        vo.setPrimaryDeptId(entity.getPrimaryDeptId());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    public static DeptVo toDeptVo(Dept entity) {
        DeptVo vo = new DeptVo();
        vo.setDeptId(entity.getDeptId());
        vo.setParentId(entity.getParentId());
        vo.setDeptCode(entity.getDeptCode());
        vo.setDeptName(entity.getDeptName());
        vo.setDeptPath(entity.getDeptPath());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    public static RoleVo toRoleVo(Role entity) {
        RoleVo vo = new RoleVo();
        vo.setRoleId(entity.getRoleId());
        vo.setRoleCode(entity.getRoleCode());
        vo.setRoleName(entity.getRoleName());
        vo.setRoleType(entity.getRoleType());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    public static SysFunctionVo toSysFunctionVo(SysFunction entity) {
        SysFunctionVo vo = new SysFunctionVo();
        vo.setSysFunctionId(entity.getSysFunctionId());
        vo.setParentId(entity.getParentId());
        vo.setFunctionCode(entity.getFunctionCode());
        vo.setFunctionName(entity.getFunctionName());
        vo.setFunctionType(entity.getFunctionType());
        vo.setRoutePath(entity.getRoutePath());
        vo.setPermissionCode(entity.getPermissionCode());
        vo.setSortOrder(entity.getSortOrder());
        return vo;
    }

    public static SysApiVo toSysApiVo(SysApi entity) {
        SysApiVo vo = new SysApiVo();
        vo.setSysApiId(entity.getSysApiId());
        vo.setApiCode(entity.getApiCode());
        vo.setApiName(entity.getApiName());
        vo.setApiType(entity.getApiType());
        vo.setHttpMethod(entity.getHttpMethod());
        vo.setPathPattern(entity.getPathPattern());
        vo.setPermissionCode(entity.getPermissionCode());
        return vo;
    }

    public static DictTypeVo toDictTypeVo(DictTypeEntity entity, List<DictItemVo> items) {
        DictTypeVo vo = new DictTypeVo();
        vo.setDictTypeId(entity.getDictTypeId());
        vo.setDictCode(entity.getDictCode());
        vo.setDictName(entity.getDictName());
        vo.setItems(items);
        return vo;
    }

    public static DictItemVo toDictItemVo(DictItemEntity entity) {
        DictItemVo vo = new DictItemVo();
        vo.setDictItemId(entity.getDictItemId());
        vo.setItemLabel(entity.getItemLabel());
        vo.setItemValue(entity.getItemValue());
        vo.setColor(entity.getColor());
        vo.setDefaultItem(entity.getDefaultItem());
        vo.setSortOrder(entity.getSortOrder());
        return vo;
    }
}
