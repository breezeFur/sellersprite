package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.SysApi;
import java.util.List;

public interface SysApiDao extends IService<SysApi> {

    List<SysApi> listPublicApis();

    boolean existsByApiCode(String apiCode);
}
