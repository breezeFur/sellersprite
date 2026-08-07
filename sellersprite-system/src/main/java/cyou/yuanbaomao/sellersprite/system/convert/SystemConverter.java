package cyou.yuanbaomao.sellersprite.system.convert;

import cyou.yuanbaomao.sellersprite.db.entity.Dept;
import cyou.yuanbaomao.dict.mybatis.entity.DictDataEntity;
import cyou.yuanbaomao.dict.mybatis.entity.DictTypeEntity;
import cyou.yuanbaomao.sellersprite.db.entity.Role;
import cyou.yuanbaomao.sellersprite.db.entity.SysApi;
import cyou.yuanbaomao.sellersprite.db.entity.SysFunction;
import cyou.yuanbaomao.sellersprite.db.entity.User;
import cyou.yuanbaomao.sellersprite.system.dept.model.vo.DeptVo;
import cyou.yuanbaomao.sellersprite.system.dict.model.vo.DictItemVo;
import cyou.yuanbaomao.sellersprite.system.dict.model.vo.DictTypeVo;
import cyou.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import cyou.yuanbaomao.sellersprite.system.permission.model.vo.SysApiVo;
import cyou.yuanbaomao.sellersprite.system.permission.model.vo.SysFunctionVo;
import cyou.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
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
        vo.setComponentPath(entity.getComponentPath());
        vo.setPermissionCode(entity.getPermissionCode());
        vo.setIcon(entity.getIcon());
        vo.setVisible(entity.getVisible());
        vo.setCacheable(entity.getCacheable());
        vo.setExternalLink(entity.getExternalLink());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
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
        vo.setModuleName(entity.getModuleName());
        vo.setOperationName(entity.getOperationName());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    public static DictTypeVo toDictTypeVo(DictTypeEntity entity, List<DictItemVo> items) {
        DictTypeVo vo = new DictTypeVo();
        vo.setDictType(entity.getDictType());
        vo.setDictName(entity.getDictTypeName());
        vo.setSystemBuiltin(entity.getSystemBuiltin());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        vo.setItems(items);
        return vo;
    }

    public static DictItemVo toDictItemVo(DictDataEntity entity) {
        DictItemVo vo = new DictItemVo();
        vo.setDictDataId(entity.getDictDataId());
        vo.setDictType(entity.getDictType());
        vo.setDictValue(entity.getDictValue());
        vo.setDictLabel(entity.getDictLabel());
        vo.setDictName(entity.getDictName());
        vo.setColor(entity.getColor());
        vo.setDefaultFlag(entity.getDefaultFlag());
        vo.setSortOrder(entity.getSortOrder());
        vo.setSystemBuiltin(entity.getSystemBuiltin());
        vo.setStatus(entity.getStatus());
        return vo;
    }
}
