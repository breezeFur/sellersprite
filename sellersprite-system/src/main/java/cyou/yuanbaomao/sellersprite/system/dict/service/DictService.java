package cyou.yuanbaomao.sellersprite.system.dict.service;

import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictItemCreateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictItemPageRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictItemUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictTypeCreateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictTypePageRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictTypeUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.vo.DictItemVo;
import cyou.yuanbaomao.sellersprite.system.dict.model.vo.DictTypeVo;

public interface DictService {

    DictTypeVo createType(DictTypeCreateRequest request);

    PageResult<DictTypeVo> pageTypes(DictTypePageRequest request);

    DictTypeVo detailType(String dictTypeId);

    DictTypeVo updateType(String dictTypeId, DictTypeUpdateRequest request);

    void updateTypeStatus(String dictTypeId, Integer status);

    void deleteType(String dictTypeId);

    DictItemVo createItem(DictItemCreateRequest request);

    PageResult<DictItemVo> pageItems(String dictTypeId, DictItemPageRequest request);

    DictItemVo detailItem(String dictItemId);

    DictItemVo updateItem(String dictItemId, DictItemUpdateRequest request);

    void updateItemStatus(String dictItemId, Integer status);

    void deleteItem(String dictItemId);

    DictTypeVo detailByCode(String dictCode);
}
