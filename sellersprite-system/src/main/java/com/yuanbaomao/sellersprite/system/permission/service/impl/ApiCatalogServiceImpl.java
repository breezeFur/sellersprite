package com.yuanbaomao.sellersprite.system.permission.service.impl;

import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.FunctionApiDao;
import com.yuanbaomao.sellersprite.db.dao.SysApiDao;
import com.yuanbaomao.sellersprite.db.dao.SysFunctionDao;
import com.yuanbaomao.sellersprite.db.entity.SysApi;
import com.yuanbaomao.sellersprite.db.entity.SysFunction;
import com.yuanbaomao.sellersprite.db.entity.FunctionApi;
import com.yuanbaomao.sellersprite.framework.security.ApiResourceMatcher;
import com.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import com.yuanbaomao.sellersprite.system.permission.enums.ApiType;
import com.yuanbaomao.sellersprite.system.permission.enums.FunctionType;
import com.yuanbaomao.sellersprite.system.permission.model.dto.ApiEndpointRefRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.MenuApiBindingItemRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.MenuApiBindingSyncRequest;
import com.yuanbaomao.sellersprite.system.permission.model.vo.ApiCatalogSyncResultVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.MenuApiBindingSyncResultVo;
import com.yuanbaomao.sellersprite.system.permission.service.ApiCatalogService;
import com.yuanbaomao.sellersprite.system.permission.service.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Service
public class ApiCatalogServiceImpl implements ApiCatalogService {

    private static final String API_PATH_PREFIX = "/api";
    private static final String AUTO_REMARK = "运行时接口目录自动装载";
    private static final int API_CODE_MAX_LENGTH = 128;
    private static final int IDENTITY_HASH_LENGTH = 12;

    private final RequestMappingHandlerMapping handlerMapping;
    private final SysApiDao sysApiDao;
    private final SysFunctionDao sysFunctionDao;
    private final FunctionApiDao functionApiDao;
    private final RolePermissionService rolePermissionService;

    public ApiCatalogServiceImpl(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
            SysApiDao sysApiDao,
            SysFunctionDao sysFunctionDao,
            FunctionApiDao functionApiDao,
            RolePermissionService rolePermissionService) {
        this.handlerMapping = handlerMapping;
        this.sysApiDao = sysApiDao;
        this.sysFunctionDao = sysFunctionDao;
        this.functionApiDao = functionApiDao;
        this.rolePermissionService = rolePermissionService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiCatalogSyncResultVo syncCatalog() {
        Map<EndpointIdentity, SysApi> existingApis = new LinkedHashMap<>();
        sysApiDao.list().forEach(api -> existingApis.put(identity(api.getHttpMethod(), api.getPathPattern()), api));
        List<DiscoveredEndpoint> discoveredEndpoints = discoverEndpoints();
        ApiCatalogSyncResultVo result = new ApiCatalogSyncResultVo();
        result.setScanned(discoveredEndpoints.size());

        for (DiscoveredEndpoint endpoint : discoveredEndpoints) {
            SysApi existing = existingApis.get(endpoint.identity());
            if (existing == null) {
                sysApiDao.save(toEntity(endpoint));
                result.setCreated(result.getCreated() + 1);
                continue;
            }
            if (refreshMetadata(existing, endpoint)) {
                sysApiDao.updateById(existing);
                result.setUpdated(result.getUpdated() + 1);
            } else {
                result.setUnchanged(result.getUnchanged() + 1);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuApiBindingSyncResultVo syncMenuBindings(MenuApiBindingSyncRequest request) {
        if (request == null || request.getBindings() == null || request.getBindings().isEmpty()) {
            throw new BizException(ResultCode.PARAM_INVALID, "菜单接口清单不能为空");
        }

        List<SysFunction> allFunctions = sysFunctionDao.listAll();
        Map<String, SysFunction> menusByCode = new LinkedHashMap<>();
        Map<String, SysFunction> menusById = new LinkedHashMap<>();
        allFunctions.stream().filter(this::isEnabledMenu).forEach(function -> {
            menusByCode.put(function.getFunctionCode(), function);
            menusById.put(function.getSysFunctionId(), function);
        });
        Map<EndpointIdentity, SysApi> apisByIdentity = new LinkedHashMap<>();
        Map<String, SysApi> apisById = new LinkedHashMap<>();
        sysApiDao.list().stream()
                .filter(api -> Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(api.getStatus()))
                .forEach(api -> {
                    apisByIdentity.put(identity(api.getHttpMethod(), api.getPathPattern()), api);
                    apisById.put(api.getSysApiId(), api);
                });

        Map<SysFunction, LinkedHashSet<SysApi>> resolvedBindings = resolveBindings(
                request.getBindings(), menusByCode, apisByIdentity);

        resolvedBindings.forEach((function, apis) -> functionApiDao.replaceByFunctionId(
                function.getSysFunctionId(), apis.stream().map(SysApi::getSysApiId).toList()));
        Map<String, Set<String>> menuUsage = collectMenuUsage(functionApiDao.list(), menusById, apisById);
        menuUsage.forEach((apiId, functionCodes) -> {
            SysApi api = apisById.get(apiId);
            api.setApiType(functionCodes.size() > 1 ? ApiType.PUBLIC.getCode() : ApiType.PERMISSION.getCode());
            sysApiDao.updateById(api);
        });
        resolvedBindings.keySet().forEach(function ->
                rolePermissionService.recalculateRolesForFunction(function.getSysFunctionId()));

        MenuApiBindingSyncResultVo result = new MenuApiBindingSyncResultVo();
        result.setFunctionCount(resolvedBindings.size());
        result.setBindingCount(resolvedBindings.values().stream().mapToInt(Set::size).sum());
        result.setPublicApiCount((int) menuUsage.values().stream().filter(codes -> codes.size() > 1).count());
        result.setPermissionApiCount(menuUsage.size() - result.getPublicApiCount());
        return result;
    }

    private List<DiscoveredEndpoint> discoverEndpoints() {
        Map<EndpointIdentity, DiscoveredEndpoint> endpoints = new LinkedHashMap<>();
        handlerMapping.getHandlerMethods().forEach((mapping, handlerMethod) ->
                addMappingEndpoints(endpoints, mapping, handlerMethod));
        return endpoints.values().stream()
                .sorted(Comparator.comparing((DiscoveredEndpoint value) -> value.identity().pathPattern())
                        .thenComparing(value -> value.identity().httpMethod()))
                .toList();
    }

    private void addMappingEndpoints(Map<EndpointIdentity, DiscoveredEndpoint> endpoints,
            RequestMappingInfo mapping, HandlerMethod handlerMethod) {
        Set<RequestMethod> methods = mapping.getMethodsCondition().getMethods();
        if (methods.isEmpty()) {
            return;
        }
        for (String rawPath : mapping.getPatternValues()) {
            String path = ApiResourceMatcher.normalizePattern(rawPath);
            if (!path.startsWith(API_PATH_PREFIX + "/") && !API_PATH_PREFIX.equals(path)) {
                continue;
            }
            for (RequestMethod requestMethod : methods) {
                EndpointIdentity identity = identity(requestMethod.name(), path);
                endpoints.putIfAbsent(identity, discoveredEndpoint(identity, handlerMethod));
            }
        }
    }

    private DiscoveredEndpoint discoveredEndpoint(EndpointIdentity identity, HandlerMethod handlerMethod) {
        Operation operation = handlerMethod.getMethodAnnotation(Operation.class);
        String operationName = handlerMethod.getMethod().getName();
        String apiName = operation != null && operation.summary() != null && !operation.summary().isBlank()
                ? operation.summary().trim()
                : handlerMethod.getBeanType().getSimpleName() + "." + operationName;
        return new DiscoveredEndpoint(identity, generateApiCode(identity), apiName,
                moduleName(identity.pathPattern()), operationName);
    }

    private SysApi toEntity(DiscoveredEndpoint endpoint) {
        SysApi entity = new SysApi();
        entity.setApiCode(endpoint.apiCode());
        entity.setApiName(endpoint.apiName());
        entity.setApiType(ApiType.PERMISSION.getCode());
        entity.setHttpMethod(endpoint.identity().httpMethod());
        entity.setPathPattern(endpoint.identity().pathPattern());
        entity.setPermissionCode("");
        entity.setModuleName(endpoint.moduleName());
        entity.setOperationName(endpoint.operationName());
        entity.setStatus(SystemBusinessConstants.STATUS_ENABLED);
        entity.setRemark(AUTO_REMARK);
        return entity;
    }

    private boolean refreshMetadata(SysApi entity, DiscoveredEndpoint endpoint) {
        boolean changed = !Objects.equals(entity.getApiName(), endpoint.apiName())
                || !Objects.equals(entity.getModuleName(), endpoint.moduleName())
                || !Objects.equals(entity.getOperationName(), endpoint.operationName());
        if (changed) {
            entity.setApiName(endpoint.apiName());
            entity.setModuleName(endpoint.moduleName());
            entity.setOperationName(endpoint.operationName());
        }
        return changed;
    }

    private Map<SysFunction, LinkedHashSet<SysApi>> resolveBindings(List<MenuApiBindingItemRequest> bindings,
            Map<String, SysFunction> menusByCode, Map<EndpointIdentity, SysApi> apisByIdentity) {
        Map<SysFunction, LinkedHashSet<SysApi>> resolved = new LinkedHashMap<>();
        for (MenuApiBindingItemRequest binding : bindings) {
            if (binding == null || binding.getFunctionCode() == null || binding.getFunctionCode().isBlank()
                    || binding.getApis() == null || binding.getApis().isEmpty()) {
                throw new BizException(ResultCode.PARAM_INVALID, "菜单接口清单格式不合法");
            }
            SysFunction menu = menusByCode.get(binding.getFunctionCode().trim());
            if (menu == null) {
                throw new BizException(ResultCode.FUNCTION_NOT_FOUND,
                        "菜单功能不存在或未启用：" + binding.getFunctionCode());
            }
            LinkedHashSet<SysApi> resolvedApis = resolved.computeIfAbsent(menu, ignored -> new LinkedHashSet<>());
            for (ApiEndpointRefRequest endpoint : binding.getApis()) {
                EndpointIdentity identity = requestIdentity(endpoint);
                SysApi api = apisByIdentity.get(identity);
                if (api == null) {
                    throw new BizException(ResultCode.API_NOT_FOUND,
                            "接口尚未装载：" + identity.httpMethod() + " " + identity.pathPattern());
                }
                resolvedApis.add(api);
            }
        }
        return resolved;
    }

    private Map<String, Set<String>> collectMenuUsage(List<FunctionApi> bindings,
            Map<String, SysFunction> menusById, Map<String, SysApi> apisById) {
        Map<String, Set<String>> usage = new LinkedHashMap<>();
        for (FunctionApi binding : bindings) {
            SysFunction menu = menusById.get(binding.getSysFunctionId());
            if (menu == null || !apisById.containsKey(binding.getSysApiId())) {
                continue;
            }
            usage.computeIfAbsent(binding.getSysApiId(), ignored -> new LinkedHashSet<>())
                    .add(menu.getFunctionCode());
        }
        return usage;
    }

    private EndpointIdentity requestIdentity(ApiEndpointRefRequest endpoint) {
        if (endpoint == null || endpoint.getHttpMethod() == null || endpoint.getPathPattern() == null) {
            throw new BizException(ResultCode.PARAM_INVALID, "接口方法和路径不能为空");
        }
        try {
            org.springframework.http.HttpMethod.valueOf(endpoint.getHttpMethod().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BizException(ResultCode.PARAM_INVALID, "HTTP 方法不合法");
        }
        EndpointIdentity identity = identity(endpoint.getHttpMethod(), endpoint.getPathPattern());
        if (!identity.pathPattern().startsWith(API_PATH_PREFIX)) {
            throw new BizException(ResultCode.PARAM_INVALID, "接口路径必须以/api开头");
        }
        return identity;
    }

    private EndpointIdentity identity(String method, String path) {
        return new EndpointIdentity(method.trim().toUpperCase(Locale.ROOT),
                ApiResourceMatcher.normalizePattern(path));
    }

    private boolean isEnabledMenu(SysFunction function) {
        return function != null
                && Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(function.getStatus())
                && FunctionType.MENU.getCode().equals(function.getFunctionType());
    }

    private String moduleName(String path) {
        String withoutPrefix = path.substring(API_PATH_PREFIX.length());
        String[] segments = withoutPrefix.split("/");
        for (String segment : segments) {
            if (!segment.isBlank()) {
                return segment.replaceAll("[{}]", "");
            }
        }
        return "root";
    }

    private String generateApiCode(EndpointIdentity identity) {
        String readablePath = identity.pathPattern().substring(API_PATH_PREFIX.length())
                .replaceAll("[{}]", "")
                .replaceAll("[^A-Za-z0-9]+", ".")
                .replaceAll("^\\.|\\.$", "")
                .toLowerCase(Locale.ROOT);
        String source = identity.httpMethod() + " " + identity.pathPattern();
        String hash = DigestUtils.md5DigestAsHex(source.getBytes(StandardCharsets.UTF_8))
                .substring(0, IDENTITY_HASH_LENGTH);
        String prefix = "auto." + identity.httpMethod().toLowerCase(Locale.ROOT) + "."
                + (readablePath.isBlank() ? "root" : readablePath) + ".";
        int maxPrefixLength = API_CODE_MAX_LENGTH - hash.length();
        if (prefix.length() > maxPrefixLength) {
            prefix = prefix.substring(0, maxPrefixLength);
        }
        return prefix + hash;
    }

    private record EndpointIdentity(String httpMethod, String pathPattern) {
    }

    private record DiscoveredEndpoint(EndpointIdentity identity, String apiCode, String apiName,
            String moduleName, String operationName) {
    }
}
