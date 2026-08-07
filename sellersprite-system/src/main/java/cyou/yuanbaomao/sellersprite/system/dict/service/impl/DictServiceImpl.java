package cyou.yuanbaomao.sellersprite.system.dict.service.impl;

import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.dict.core.DictTemplate;
import cyou.yuanbaomao.dict.model.DictDataPageQuery;
import cyou.yuanbaomao.dict.model.DictTypePageQuery;
import cyou.yuanbaomao.dict.mybatis.entity.DictDataEntity;
import cyou.yuanbaomao.dict.mybatis.entity.DictTypeEntity;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import cyou.yuanbaomao.sellersprite.system.convert.SystemConverter;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictItemCreateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictItemPageRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictItemUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictTypeCreateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictTypePageRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictTypeUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.vo.DictItemVo;
import cyou.yuanbaomao.sellersprite.system.dict.model.vo.DictTypeVo;
import cyou.yuanbaomao.sellersprite.system.dict.service.DictService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final DictTemplate dictTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictTypeVo createType(DictTypeCreateRequest request) {
        DictTypeEntity entity = new DictTypeEntity();
        entity.setDictType(request.getDictType());
        entity.setDictTypeName(request.getDictName());
        entity.setSystemBuiltin(defaultNumber(request.getSystemBuiltin(), SystemBusinessConstants.NO));
        entity.setSortOrder(defaultNumber(request.getSortOrder(), 0));
        entity.setStatus(SystemBusinessConstants.STATUS_ENABLED);
        entity.setRemark("");
        return SystemConverter.toDictTypeVo(dictTemplate.createType(entity), List.of());
    }

    @Override
    public PageResult<DictTypeVo> pageTypes(DictTypePageRequest request) {
        DictTypePageQuery query = new DictTypePageQuery();
        query.setCurrent(request.getCurrent());
        query.setSize(request.getSize());
        query.setDictType(request.getDictType());
        query.setDictTypeName(request.getDictName());
        query.setStatus(request.getStatus());
        PageResult<DictTypeEntity> page = dictTemplate.pageTypes(query);
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords().stream()
                .map(entity -> SystemConverter.toDictTypeVo(entity, List.of()))
                .toList());
    }

    @Override
    public DictTypeVo detailType(String dictType) {
        return toTypeWithData(dictTemplate.getType(dictType));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictTypeVo updateType(String dictType, DictTypeUpdateRequest request) {
        DictTypeEntity entity = dictTemplate.getType(dictType);
        entity.setDictTypeName(request.getDictName());
        entity.setSortOrder(defaultNumber(request.getSortOrder(), 0));
        entity.setRemark(defaultString(request.getRemark()));
        dictTemplate.updateType(dictType, entity);
        return toTypeWithData(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTypeStatus(String dictType, Integer status) {
        validateStatus(status);
        DictTypeEntity entity = dictTemplate.getType(dictType);
        entity.setStatus(status);
        dictTemplate.updateType(dictType, entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteType(String dictType) {
        DictTypeEntity entity = dictTemplate.getType(dictType);
        if (Integer.valueOf(SystemBusinessConstants.YES).equals(entity.getSystemBuiltin())) {
            throw new BizException(ResultCode.RESOURCE_CONFLICT, "系统内置字典类型不能删除");
        }
        dictTemplate.deleteType(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictItemVo createItem(DictItemCreateRequest request) {
        DictDataEntity entity = new DictDataEntity();
        entity.setDictType(request.getDictType());
        entity.setDictValue(request.getDictValue());
        entity.setDictLabel(request.getDictLabel());
        entity.setDictName(request.getDictName());
        entity.setCssClass("");
        entity.setColor(defaultString(request.getColor()));
        entity.setSystemBuiltin(SystemBusinessConstants.NO);
        entity.setDefaultFlag(defaultNumber(request.getDefaultFlag(), SystemBusinessConstants.NO));
        entity.setSortOrder(defaultNumber(request.getSortOrder(), 0));
        entity.setStatus(SystemBusinessConstants.STATUS_ENABLED);
        entity.setRemark("");
        return SystemConverter.toDictItemVo(dictTemplate.createData(entity));
    }

    @Override
    public PageResult<DictItemVo> pageItems(String dictType, DictItemPageRequest request) {
        DictDataPageQuery query = new DictDataPageQuery();
        query.setCurrent(request.getCurrent());
        query.setSize(request.getSize());
        query.setDictType(dictType);
        query.setDictLabel(request.getDictLabel());
        query.setDictName(request.getDictName());
        query.setDictValue(request.getDictValue());
        query.setStatus(request.getStatus());
        PageResult<DictDataEntity> page = dictTemplate.pageData(query);
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords().stream()
                .map(SystemConverter::toDictItemVo)
                .toList());
    }

    @Override
    public DictItemVo detailItem(String dictDataId) {
        return SystemConverter.toDictItemVo(dictTemplate.getData(dictDataId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DictItemVo updateItem(String dictDataId, DictItemUpdateRequest request) {
        DictDataEntity entity = dictTemplate.getData(dictDataId);
        entity.setDictValue(request.getDictValue());
        entity.setDictLabel(request.getDictLabel());
        entity.setDictName(request.getDictName());
        entity.setColor(defaultString(request.getColor()));
        entity.setDefaultFlag(defaultNumber(request.getDefaultFlag(), SystemBusinessConstants.NO));
        entity.setSortOrder(defaultNumber(request.getSortOrder(), 0));
        entity.setRemark(defaultString(request.getRemark()));
        dictTemplate.updateData(entity);
        return SystemConverter.toDictItemVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateItemStatus(String dictDataId, Integer status) {
        validateStatus(status);
        DictDataEntity entity = dictTemplate.getData(dictDataId);
        entity.setStatus(status);
        dictTemplate.updateData(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteItem(String dictDataId) {
        DictDataEntity entity = dictTemplate.getData(dictDataId);
        if (Integer.valueOf(SystemBusinessConstants.YES).equals(entity.getSystemBuiltin())) {
            throw new BizException(ResultCode.RESOURCE_CONFLICT, "系统内置字典项不能删除");
        }
        dictTemplate.deleteData(dictDataId);
    }

    @Override
    public DictTypeVo detailByCode(String dictType) {
        return toTypeWithData(dictTemplate.getType(dictType));
    }

    private DictTypeVo toTypeWithData(DictTypeEntity entity) {
        List<DictItemVo> items = dictTemplate.listEnabledData(entity.getDictType()).stream()
                .map(SystemConverter::toDictItemVo)
                .toList();
        return SystemConverter.toDictTypeVo(entity, items);
    }

    private void validateStatus(Integer status) {
        if (!Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(status)
                && !Integer.valueOf(SystemBusinessConstants.STATUS_DISABLED).equals(status)) {
            throw new BizException(ResultCode.PARAM_INVALID);
        }
    }

    private int defaultNumber(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
