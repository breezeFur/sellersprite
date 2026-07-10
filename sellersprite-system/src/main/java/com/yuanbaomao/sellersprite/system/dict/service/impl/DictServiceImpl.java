package com.yuanbaomao.sellersprite.system.dict.service.impl;

import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.base.exception.BizException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yuanbaomao.dict.mybatis.dao.DictItemDao;
import com.yuanbaomao.dict.mybatis.dao.DictTypeDao;
import com.yuanbaomao.dict.mybatis.entity.DictItemEntity;
import com.yuanbaomao.dict.mybatis.entity.DictTypeEntity;
import com.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import com.yuanbaomao.sellersprite.system.convert.SystemConverter;
import com.yuanbaomao.sellersprite.system.dict.model.dto.DictItemCreateRequest;
import com.yuanbaomao.sellersprite.system.dict.model.dto.DictTypeCreateRequest;
import com.yuanbaomao.sellersprite.system.dict.model.vo.DictItemVo;
import com.yuanbaomao.sellersprite.system.dict.model.vo.DictTypeVo;
import com.yuanbaomao.sellersprite.system.dict.service.DictService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final DictTypeDao dictTypeDao;
    private final DictItemDao dictItemDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictTypeVo createType(DictTypeCreateRequest request) {
        if (dictTypeDao.count(Wrappers.<DictTypeEntity>lambdaQuery()
                .eq(DictTypeEntity::getDictCode, request.getDictCode())) > 0) {
            throw new BizException(ResultCode.DICT_TYPE_ALREADY_EXISTS);
        }
        DictTypeEntity entity = new DictTypeEntity();
        entity.setDictCode(request.getDictCode());
        entity.setDictName(request.getDictName());
        entity.setSystemBuiltin(request.getSystemBuiltin() == null ? SystemBusinessConstants.NO : request.getSystemBuiltin());
        entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        entity.setStatus(SystemBusinessConstants.STATUS_ENABLED);
        entity.setRemark("");
        dictTypeDao.save(entity);
        return SystemConverter.toDictTypeVo(entity, List.of());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictItemVo createItem(DictItemCreateRequest request) {
        if (dictTypeDao.getById(request.getDictTypeId()) == null) {
            throw new BizException(ResultCode.DICT_TYPE_NOT_FOUND);
        }
        DictItemEntity entity = new DictItemEntity();
        entity.setDictTypeId(request.getDictTypeId());
        entity.setItemLabel(request.getItemLabel());
        entity.setItemValue(request.getItemValue());
        entity.setCssClass("");
        entity.setColor(defaultString(request.getColor()));
        entity.setSystemBuiltin(SystemBusinessConstants.NO);
        entity.setDefaultItem(request.getDefaultItem() == null ? SystemBusinessConstants.NO : request.getDefaultItem());
        entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        entity.setStatus(SystemBusinessConstants.STATUS_ENABLED);
        entity.setRemark("");
        dictItemDao.save(entity);
        return SystemConverter.toDictItemVo(entity);
    }

    @Override
    public DictTypeVo detailByCode(String dictCode) {
        DictTypeEntity dictType = dictTypeDao.getOne(Wrappers.<DictTypeEntity>lambdaQuery()
                .eq(DictTypeEntity::getDictCode, dictCode), false);
        if (dictType == null) {
            throw new BizException(ResultCode.DICT_TYPE_NOT_FOUND);
        }
        List<DictItemVo> items = dictItemDao.list(Wrappers.<DictItemEntity>lambdaQuery()
                        .eq(DictItemEntity::getDictTypeId, dictType.getDictTypeId())
                        .orderByAsc(DictItemEntity::getSortOrder))
                .stream()
                .map(SystemConverter::toDictItemVo)
                .toList();
        return SystemConverter.toDictTypeVo(dictType, items);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
