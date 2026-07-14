package com.yuanbaomao.sellersprite.system.dict.service;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.system.dict.model.dto.DictItemCreateRequest;
import com.yuanbaomao.sellersprite.system.dict.model.dto.DictItemPageRequest;
import com.yuanbaomao.sellersprite.system.dict.model.dto.DictItemUpdateRequest;
import com.yuanbaomao.sellersprite.system.dict.model.dto.DictTypeCreateRequest;
import com.yuanbaomao.sellersprite.system.dict.model.dto.DictTypePageRequest;
import com.yuanbaomao.sellersprite.system.dict.model.dto.DictTypeUpdateRequest;
import com.yuanbaomao.sellersprite.system.dict.model.vo.DictItemVo;
import com.yuanbaomao.sellersprite.system.dict.model.vo.DictTypeVo;

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
