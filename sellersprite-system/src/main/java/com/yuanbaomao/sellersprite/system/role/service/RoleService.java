package com.yuanbaomao.sellersprite.system.role.service;

import com.yuanbaomao.sellersprite.system.role.model.dto.RoleCreateRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.UserRoleBindRequest;
import com.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import java.util.List;

public interface RoleService {

    RoleVo create(RoleCreateRequest request);

    void bindUserRole(UserRoleBindRequest request);

    List<RoleVo> listEnabled();
}
