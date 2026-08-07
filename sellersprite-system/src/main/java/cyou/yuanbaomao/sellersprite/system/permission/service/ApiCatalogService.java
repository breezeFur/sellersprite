package cyou.yuanbaomao.sellersprite.system.permission.service;

import cyou.yuanbaomao.sellersprite.system.permission.model.dto.MenuApiBindingSyncRequest;
import cyou.yuanbaomao.sellersprite.system.permission.model.vo.ApiCatalogSyncResultVo;
import cyou.yuanbaomao.sellersprite.system.permission.model.vo.MenuApiBindingSyncResultVo;

public interface ApiCatalogService {

    ApiCatalogSyncResultVo syncCatalog();

    MenuApiBindingSyncResultVo syncMenuBindings(MenuApiBindingSyncRequest request);
}
