package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.RoleApi;
import java.util.Collection;
import java.util.List;

public interface RoleApiDao extends IService<RoleApi> {

    List<RoleApi> listByRoleIds(Collection<String> roleIds);

    boolean existsByApiId(String apiId);

    void replaceByRoleId(String roleId, Collection<RoleApi> roleApis);
}
