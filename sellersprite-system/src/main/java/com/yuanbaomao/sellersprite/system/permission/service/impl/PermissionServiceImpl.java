package com.yuanbaomao.sellersprite.system.permission.service.impl;

import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.SysApiDao;
import com.yuanbaomao.sellersprite.db.dao.SysFunctionDao;
import com.yuanbaomao.sellersprite.db.entity.SysApi;
import com.yuanbaomao.sellersprite.db.entity.SysFunction;
import com.yuanbaomao.base.constants.SystemConstants;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import com.yuanbaomao.sellersprite.system.convert.SystemConverter;
import com.yuanbaomao.sellersprite.system.permission.model.dto.SysApiCreateRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.SysFunctionCreateRequest;
import com.yuanbaomao.sellersprite.system.permission.model.vo.SysApiVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.SysFunctionVo;
import com.yuanbaomao.sellersprite.system.permission.service.PermissionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final SysFunctionDao sysFunctionDao;
    private final SysApiDao sysApiDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysFunctionVo createFunction(SysFunctionCreateRequest request) {
        if (sysFunctionDao.existsByFunctionCode(request.getFunctionCode())) {
            throw new BizException(ResultCode.FUNCTION_CODE_ALREADY_EXISTS);
        }
        if (!SystemConstants.ROOT_PARENT_ID.equals(request.getParentId())
                && sysFunctionDao.getById(request.getParentId()) == null) {
            throw new BizException(ResultCode.FUNCTION_NOT_FOUND, "父功能不存在");
        }
        SysFunction entity = new SysFunction();
        entity.setParentId(request.getParentId());
        entity.setFunctionCode(request.getFunctionCode());
        entity.setFunctionName(request.getFunctionName());
        entity.setFunctionType(request.getFunctionType());
        entity.setRoutePath(request.getRoutePath());
        entity.setComponentPath(request.getComponentPath());
        entity.setPermissionCode(request.getPermissionCode());
        entity.setIcon("");
        entity.setVisible(SystemBusinessConstants.YES);
        entity.setCacheable(SystemBusinessConstants.NO);
        entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        entity.setStatus(SystemBusinessConstants.STATUS_ENABLED);
        entity.setRemark("");
        sysFunctionDao.save(entity);
        return SystemConverter.toSysFunctionVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysApiVo createApi(SysApiCreateRequest request) {
        if (sysApiDao.existsByApiCode(request.getApiCode())) {
            throw new BizException(ResultCode.API_PERMISSION_CODE_ALREADY_EXISTS);
        }
        SysApi entity = new SysApi();
        entity.setApiCode(request.getApiCode());
        entity.setApiName(request.getApiName());
        entity.setApiType(request.getApiType());
        entity.setHttpMethod(request.getHttpMethod().toUpperCase());
        entity.setPathPattern(request.getPathPattern());
        entity.setPermissionCode(request.getPermissionCode());
        entity.setModuleName(defaultString(request.getModuleName()));
        entity.setOperationName(defaultString(request.getOperationName()));
        entity.setStatus(SystemBusinessConstants.STATUS_ENABLED);
        entity.setRemark("");
        sysApiDao.save(entity);
        return SystemConverter.toSysApiVo(entity);
    }

    @Override
    public List<SysFunctionVo> listFunctions() {
        return sysFunctionDao.listEnabled().stream().map(SystemConverter::toSysFunctionVo).toList();
    }

    @Override
    public List<SysApiVo> listPublicApis() {
        return sysApiDao.listPublicApis().stream().map(SystemConverter::toSysApiVo).toList();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
