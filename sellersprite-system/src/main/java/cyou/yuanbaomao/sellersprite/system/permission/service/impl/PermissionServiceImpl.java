package cyou.yuanbaomao.sellersprite.system.permission.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.SysApiDao;
import cyou.yuanbaomao.sellersprite.db.dao.FunctionApiDao;
import cyou.yuanbaomao.sellersprite.db.dao.RoleApiDao;
import cyou.yuanbaomao.sellersprite.db.dao.RoleFunctionDao;
import cyou.yuanbaomao.sellersprite.db.dao.SysFunctionDao;
import cyou.yuanbaomao.sellersprite.db.entity.SysApi;
import cyou.yuanbaomao.sellersprite.db.entity.SysFunction;
import cyou.yuanbaomao.sellersprite.db.entity.FunctionApi;
import cyou.yuanbaomao.base.constants.SystemConstants;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import cyou.yuanbaomao.sellersprite.system.convert.SystemConverter;
import cyou.yuanbaomao.sellersprite.framework.security.ApiResourceMatcher;
import cyou.yuanbaomao.sellersprite.system.permission.enums.ApiType;
import cyou.yuanbaomao.sellersprite.system.permission.enums.FunctionType;
import cyou.yuanbaomao.sellersprite.system.permission.model.dto.SysApiCreateRequest;
import cyou.yuanbaomao.sellersprite.system.permission.model.dto.SysApiPageRequest;
import cyou.yuanbaomao.sellersprite.system.permission.model.dto.SysApiUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.permission.model.dto.SysFunctionCreateRequest;
import cyou.yuanbaomao.sellersprite.system.permission.model.dto.SysFunctionUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.permission.model.vo.SysApiVo;
import cyou.yuanbaomao.sellersprite.system.permission.model.vo.SysFunctionVo;
import cyou.yuanbaomao.sellersprite.system.permission.service.PermissionService;
import cyou.yuanbaomao.sellersprite.system.permission.service.RolePermissionService;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final SysFunctionDao sysFunctionDao;
    private final SysApiDao sysApiDao;
    private final FunctionApiDao functionApiDao;
    private final RolePermissionService rolePermissionService;
    private final RoleApiDao roleApiDao;
    private final RoleFunctionDao roleFunctionDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysFunctionVo createFunction(SysFunctionCreateRequest request) {
        validateFunction(request, null);
        if (sysFunctionDao.existsByFunctionCodeExcludingId(request.getFunctionCode(), null)) {
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
        entity.setIcon(defaultString(request.getIcon()));
        entity.setVisible(request.getVisible() == null ? SystemBusinessConstants.YES : request.getVisible());
        entity.setCacheable(request.getCacheable() == null ? SystemBusinessConstants.NO : request.getCacheable());
        entity.setExternalLink(request.getExternalLink());
        entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        entity.setStatus(SystemBusinessConstants.STATUS_ENABLED);
        entity.setRemark(defaultString(request.getRemark()));
        sysFunctionDao.save(entity);
        return SystemConverter.toSysFunctionVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysApiVo createApi(SysApiCreateRequest request) {
        ApiIdentity identity = validateApiIdentity(request, null);
        SysApi entity = new SysApi();
        entity.setApiCode(request.getApiCode());
        entity.setApiName(request.getApiName());
        entity.setApiType(request.getApiType());
        entity.setHttpMethod(identity.method());
        entity.setPathPattern(identity.path());
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
    public List<SysFunctionVo> functionTree() {
        Map<String, SysFunctionVo> nodes = new LinkedHashMap<>();
        sysFunctionDao.listAll().forEach(entity -> nodes.put(entity.getSysFunctionId(), SystemConverter.toSysFunctionVo(entity)));
        nodes.values().forEach(node -> { SysFunctionVo parent = nodes.get(node.getParentId()); if (parent != null) parent.getChildren().add(node); });
        return nodes.values().stream().filter(node -> !nodes.containsKey(node.getParentId())).toList();
    }

    @Override
    public SysFunctionVo functionDetail(String functionId) {
        return SystemConverter.toSysFunctionVo(requireFunction(functionId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysFunctionVo updateFunction(String functionId, SysFunctionUpdateRequest request) {
        SysFunction entity = requireFunction(functionId);
        validateFunction(request, functionId);
        if (sysFunctionDao.existsByFunctionCodeExcludingId(request.getFunctionCode(), functionId)) {
            throw new BizException(ResultCode.FUNCTION_CODE_ALREADY_EXISTS);
        }
        applyFunction(entity, request);
        sysFunctionDao.updateById(entity);
        return SystemConverter.toSysFunctionVo(entity);
    }

    @Override
    public void updateFunctionStatus(String functionId, Integer status) {
        validateStatus(status); SysFunction entity = requireFunction(functionId); entity.setStatus(status); sysFunctionDao.updateById(entity);
    }

    @Override
    public List<String> getFunctionApiIds(String functionId) {
        requireFunction(functionId);
        return functionApiDao.listByFunctionIds(Set.of(functionId)).stream().map(FunctionApi::getSysApiId).distinct().sorted().toList();
    }

    @Override
    public List<SysApiVo> listPublicApis() {
        return sysApiDao.listPublicApis().stream().map(SystemConverter::toSysApiVo).toList();
    }

    @Override
    public YPage<SysApiVo> pageApis(YPage<SysApiVo> page, SysApiPageRequest request) {
        String method = request.getHttpMethod() == null ? null : request.getHttpMethod().toUpperCase(Locale.ROOT);
        Page<SysApi> entityPage = sysApiDao.pageApis(request.getKeyword(), request.getApiType(), method,
                request.getModuleName(), request.getStatus(), page.getCurrent(), page.getSize());
        page.setTotal(entityPage.getTotal());
        page.setRecords(entityPage.getRecords().stream().map(SystemConverter::toSysApiVo).toList());
        return page;
    }

    @Override
    public SysApiVo apiDetail(String apiId) { return SystemConverter.toSysApiVo(requireApi(apiId)); }

    @Override
    public SysApiVo updateApi(String apiId, SysApiUpdateRequest request) {
        SysApi entity = requireApi(apiId); ApiIdentity identity = validateApiIdentity(request, apiId);
        entity.setApiCode(request.getApiCode()); entity.setApiName(request.getApiName()); entity.setApiType(request.getApiType().toUpperCase(Locale.ROOT));
        entity.setHttpMethod(identity.method()); entity.setPathPattern(identity.path()); entity.setPermissionCode(request.getPermissionCode());
        entity.setModuleName(defaultString(request.getModuleName())); entity.setOperationName(defaultString(request.getOperationName())); entity.setRemark(defaultString(request.getRemark()));
        sysApiDao.updateById(entity); return SystemConverter.toSysApiVo(entity);
    }

    @Override
    public void updateApiStatus(String apiId, Integer status) {
        validateStatus(status); SysApi entity = requireApi(apiId); entity.setStatus(status); sysApiDao.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceFunctionApis(String functionId, Collection<String> apiIds) {
        if (sysFunctionDao.getById(functionId) == null) {
            throw new BizException(ResultCode.FUNCTION_NOT_FOUND);
        }
        Set<String> normalizedApiIds = normalizeIds(apiIds);
        if (!normalizedApiIds.isEmpty()) {
            Set<String> validApiIds = sysApiDao.listByIds(normalizedApiIds).stream()
                    .filter(api -> Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(api.getStatus()))
                    .map(SysApi::getSysApiId)
                    .collect(java.util.stream.Collectors.toSet());
            if (!validApiIds.equals(normalizedApiIds)) {
                throw new BizException(ResultCode.API_NOT_FOUND);
            }
        }
        functionApiDao.replaceByFunctionId(functionId, normalizedApiIds);
        rolePermissionService.recalculateRolesForFunction(functionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFunction(String functionId) {
        if (sysFunctionDao.getById(functionId) == null) {
            throw new BizException(ResultCode.FUNCTION_NOT_FOUND);
        }
        if (sysFunctionDao.existsByParentId(functionId)) {
            throw new BizException(ResultCode.RESOURCE_CONFLICT, "功能仍有子功能，无法删除");
        }
        if (roleFunctionDao.existsByFunctionId(functionId)) {
            throw new BizException(ResultCode.RESOURCE_CONFLICT, "功能仍被角色引用，无法删除");
        }
        functionApiDao.replaceByFunctionId(functionId, Set.of());
        sysFunctionDao.removeById(functionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApi(String apiId) {
        if (sysApiDao.getById(apiId) == null) {
            throw new BizException(ResultCode.API_NOT_FOUND);
        }
        if (functionApiDao.existsByApiId(apiId)) {
            throw new BizException(ResultCode.RESOURCE_CONFLICT, "接口资源仍被功能引用，无法删除");
        }
        if (roleApiDao.existsByApiId(apiId)) {
            throw new BizException(ResultCode.RESOURCE_CONFLICT, "接口资源仍被角色引用，无法删除");
        }
        sysApiDao.removeById(apiId);
    }

    private void validateFunction(SysFunctionCreateRequest request, String currentId) {
        FunctionType type;
        try { type = FunctionType.valueOf(request.getFunctionType().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) { throw new BizException(ResultCode.PARAM_INVALID, "功能类型不合法"); }
        if (request.getPermissionCode() != null && !request.getPermissionCode().isBlank()
                && sysFunctionDao.existsByPermissionCodeExcludingId(request.getPermissionCode(), currentId)) {
            throw new BizException(ResultCode.RESOURCE_CONFLICT, "功能权限码已存在");
        }
        if (SystemConstants.ROOT_PARENT_ID.equals(request.getParentId())) {
            if (type != FunctionType.DIR) throw new BizException(ResultCode.PARAM_INVALID, "根节点下只能创建目录");
        } else {
            SysFunction parent = requireFunction(request.getParentId());
            SysFunction cursor = parent;
            Set<String> visited = new java.util.HashSet<>();
            while (cursor != null && visited.add(cursor.getSysFunctionId())) {
                if (currentId != null && currentId.equals(cursor.getSysFunctionId())) throw new BizException(ResultCode.RESOURCE_CONFLICT, "功能父子关系形成循环");
                if (SystemConstants.ROOT_PARENT_ID.equals(cursor.getParentId())) break;
                cursor = sysFunctionDao.getById(cursor.getParentId());
            }
            boolean valid = (type == FunctionType.DIR && FunctionType.DIR.getCode().equals(parent.getFunctionType()))
                    || (type == FunctionType.MENU && FunctionType.DIR.getCode().equals(parent.getFunctionType()))
                    || (type == FunctionType.BUTTON && FunctionType.MENU.getCode().equals(parent.getFunctionType()));
            if (!valid) throw new BizException(ResultCode.PARAM_INVALID, "功能节点父子类型不合法");
        }
        if (type == FunctionType.MENU && (isBlank(request.getRoutePath()) || isBlank(request.getComponentPath()))) throw new BizException(ResultCode.PARAM_INVALID, "菜单路由和组件路径不能为空");
        if (type == FunctionType.BUTTON && isBlank(request.getPermissionCode())) throw new BizException(ResultCode.PARAM_INVALID, "按钮权限码不能为空");
    }

    private void applyFunction(SysFunction entity, SysFunctionCreateRequest request) {
        entity.setParentId(request.getParentId()); entity.setFunctionCode(request.getFunctionCode()); entity.setFunctionName(request.getFunctionName());
        entity.setFunctionType(request.getFunctionType().toUpperCase(Locale.ROOT)); entity.setRoutePath(request.getRoutePath()); entity.setComponentPath(request.getComponentPath());
        entity.setPermissionCode(request.getPermissionCode()); entity.setIcon(defaultString(request.getIcon()));
        entity.setVisible(request.getVisible() == null ? SystemBusinessConstants.YES : request.getVisible());
        entity.setCacheable(request.getCacheable() == null ? SystemBusinessConstants.NO : request.getCacheable()); entity.setExternalLink(request.getExternalLink());
        entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder()); entity.setRemark(defaultString(request.getRemark()));
    }

    private ApiIdentity validateApiIdentity(SysApiCreateRequest request, String apiId) {
        try { ApiType.valueOf(request.getApiType().toUpperCase(Locale.ROOT)); org.springframework.http.HttpMethod.valueOf(request.getHttpMethod().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) { throw new BizException(ResultCode.PARAM_INVALID, "接口类型或HTTP方法不合法"); }
        String method = request.getHttpMethod().toUpperCase(Locale.ROOT); String path = ApiResourceMatcher.normalizePattern(request.getPathPattern());
        if (!path.startsWith("/api")) throw new BizException(ResultCode.PARAM_INVALID, "接口路径必须以/api开头");
        if (sysApiDao.existsByApiCodeExcludingId(request.getApiCode(), apiId)) throw new BizException(ResultCode.API_CODE_ALREADY_EXISTS);
        if (sysApiDao.existsByHttpMethodAndPathPattern(method, path, apiId)) throw new BizException(ResultCode.API_METHOD_PATH_ALREADY_EXISTS);
        return new ApiIdentity(method, path);
    }

    private SysFunction requireFunction(String id) { SysFunction value = sysFunctionDao.getById(id); if (value == null) throw new BizException(ResultCode.FUNCTION_NOT_FOUND); return value; }
    private SysApi requireApi(String id) { SysApi value = sysApiDao.getById(id); if (value == null) throw new BizException(ResultCode.API_NOT_FOUND); return value; }
    private void validateStatus(Integer value) { if (!Integer.valueOf(0).equals(value) && !Integer.valueOf(1).equals(value)) throw new BizException(ResultCode.PARAM_INVALID); }
    private boolean isBlank(String value) { return value == null || value.isBlank(); }
    private record ApiIdentity(String method, String path) { }

    private Set<String> normalizeIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String id : ids) {
            if (id == null || id.isBlank()) {
                throw new BizException(ResultCode.PARAM_INVALID);
            }
            normalized.add(id);
        }
        return normalized;
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
