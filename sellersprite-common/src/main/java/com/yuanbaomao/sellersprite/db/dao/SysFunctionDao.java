package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.SysFunction;
import java.util.List;

public interface SysFunctionDao extends IService<SysFunction> {

    List<SysFunction> listEnabled();

    List<SysFunction> listAll();

    boolean existsByFunctionCode(String functionCode);

    boolean existsByFunctionCodeExcludingId(String functionCode, String functionId);

    boolean existsByPermissionCodeExcludingId(String permissionCode, String functionId);

    boolean existsByParentId(String parentId);
}
