package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.RoleFunction;
import java.util.Collection;
import java.util.List;

public interface RoleFunctionDao extends IService<RoleFunction> {

    List<RoleFunction> listByRoleIds(Collection<String> roleIds);

    List<RoleFunction> listByFunctionIds(Collection<String> functionIds);

    boolean existsByFunctionId(String functionId);

    void replaceByRoleId(String roleId, Collection<String> functionIds);
}
