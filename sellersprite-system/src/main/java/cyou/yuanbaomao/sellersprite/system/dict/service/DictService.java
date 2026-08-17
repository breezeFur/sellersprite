package cyou.yuanbaomao.sellersprite.system.dict.service;

import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictItemCreateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictItemUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictTypeCreateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictTypeUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.vo.DictItemVo;
import cyou.yuanbaomao.sellersprite.system.dict.model.vo.DictTypeVo;

public interface DictService {

    DictTypeVo createType(DictTypeCreateRequest request);

    YPage<DictTypeVo> pageTypes(YPage<DictTypeVo> page, String dictType, String dictName, Integer status);

    DictTypeVo detailType(String dictTypeId);

    DictTypeVo updateType(String dictTypeId, DictTypeUpdateRequest request);

    void updateTypeStatus(String dictTypeId, Integer status);

    void deleteType(String dictTypeId);

    DictItemVo createItem(DictItemCreateRequest request);

    YPage<DictItemVo> pageItems(String dictType, YPage<DictItemVo> page,
            String dictLabel, String dictName, String dictValue, Integer status);

    DictItemVo detailItem(String dictItemId);

    DictItemVo updateItem(String dictItemId, DictItemUpdateRequest request);

    void updateItemStatus(String dictItemId, Integer status);

    void deleteItem(String dictItemId);

    DictTypeVo detailByCode(String dictCode);
}
