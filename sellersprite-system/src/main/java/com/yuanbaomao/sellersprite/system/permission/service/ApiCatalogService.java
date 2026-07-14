package com.yuanbaomao.sellersprite.system.permission.service;

import com.yuanbaomao.sellersprite.system.permission.model.dto.MenuApiBindingSyncRequest;
import com.yuanbaomao.sellersprite.system.permission.model.vo.ApiCatalogSyncResultVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.MenuApiBindingSyncResultVo;

public interface ApiCatalogService {

    ApiCatalogSyncResultVo syncCatalog();

    MenuApiBindingSyncResultVo syncMenuBindings(MenuApiBindingSyncRequest request);
}
