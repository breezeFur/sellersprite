package com.yuanbaomao.sellersprite.system.permission.service;

import com.yuanbaomao.sellersprite.system.permission.model.vo.UserPermissionContextVo;

public interface PermissionContextService {

    UserPermissionContextVo getByUserId(String userId);
}
