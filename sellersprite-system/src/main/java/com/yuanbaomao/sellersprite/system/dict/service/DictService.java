package com.yuanbaomao.sellersprite.system.dict.service;

import com.yuanbaomao.sellersprite.system.dict.model.dto.DictItemCreateRequest;
import com.yuanbaomao.sellersprite.system.dict.model.dto.DictTypeCreateRequest;
import com.yuanbaomao.sellersprite.system.dict.model.vo.DictItemVo;
import com.yuanbaomao.sellersprite.system.dict.model.vo.DictTypeVo;

public interface DictService {

    DictTypeVo createType(DictTypeCreateRequest request);

    DictItemVo createItem(DictItemCreateRequest request);

    DictTypeVo detailByCode(String dictCode);
}
