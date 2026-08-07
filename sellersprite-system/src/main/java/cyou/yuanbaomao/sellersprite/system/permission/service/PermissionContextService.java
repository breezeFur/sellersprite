package cyou.yuanbaomao.sellersprite.system.permission.service;

import cyou.yuanbaomao.sellersprite.system.permission.model.vo.UserPermissionContextVo;

public interface PermissionContextService {

    UserPermissionContextVo getByUserId(String userId);
}
