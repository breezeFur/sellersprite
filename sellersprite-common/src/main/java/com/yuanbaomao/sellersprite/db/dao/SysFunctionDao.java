package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.SysFunction;
import java.util.List;

public interface SysFunctionDao extends IService<SysFunction> {

    List<SysFunction> listEnabled();

    boolean existsByFunctionCode(String functionCode);
}
