package cyou.yuanbaomao.sellersprite.system.permission.service.impl;

import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.FunctionApiDao;
import cyou.yuanbaomao.sellersprite.db.dao.RoleApiDao;
import cyou.yuanbaomao.sellersprite.db.dao.RoleDao;
import cyou.yuanbaomao.sellersprite.db.dao.RoleFunctionDao;
import cyou.yuanbaomao.sellersprite.db.dao.SysApiDao;
import cyou.yuanbaomao.sellersprite.db.dao.SysFunctionDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserRoleDao;
import cyou.yuanbaomao.sellersprite.db.entity.FunctionApi;
import cyou.yuanbaomao.sellersprite.db.entity.RoleApi;
import cyou.yuanbaomao.sellersprite.db.entity.RoleFunction;
import cyou.yuanbaomao.sellersprite.db.entity.SysApi;
import cyou.yuanbaomao.sellersprite.db.entity.SysFunction;
import cyou.yuanbaomao.sellersprite.db.entity.UserRole;
import cyou.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import cyou.yuanbaomao.sellersprite.system.permission.enums.RoleApiGrantSource;
import cyou.yuanbaomao.sellersprite.system.permission.service.RolePermissionService;
import cyou.yuanbaomao.sellersprite.system.role.model.vo.RoleEffectiveApiVo;
import cyou.yuanbaomao.sellersprite.system.role.model.vo.RolePermissionVo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RoleDao roleDao;
    private final SysFunctionDao sysFunctionDao;
    private final SysApiDao sysApiDao;
    private final RoleFunctionDao roleFunctionDao;
    private final FunctionApiDao functionApiDao;
    private final RoleApiDao roleApiDao;
    private final UserRoleDao userRoleDao;
    private final UserDao userDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceRolePermissions(String roleId, Collection<String> functionIds,
            Collection<String> extraApiIds) {
        if (roleDao.getById(roleId) == null) {
            throw new BizException(ResultCode.ROLE_NOT_FOUND);
        }
        Set<String> normalizedFunctionIds = normalizeIds(functionIds);
        Set<String> normalizedExtraApiIds = normalizeIds(extraApiIds);
        validateEnabledFunctions(normalizedFunctionIds);
        validateEnabledApis(normalizedExtraApiIds);

        roleFunctionDao.replaceByRoleId(roleId, normalizedFunctionIds);
        Set<String> derivedApiIds = functionApiDao.listByFunctionIds(normalizedFunctionIds).stream()
                .map(FunctionApi::getSysApiId)
                .collect(java.util.stream.Collectors.toSet());
        roleApiDao.replaceByRoleId(roleId, mergeApiSources(roleId, derivedApiIds, normalizedExtraApiIds));
        incrementPermissionVersions(Set.of(roleId));
    }

    @Override
    public RolePermissionVo getRolePermissions(String roleId) {
        if (roleDao.getById(roleId) == null) {
            throw new BizException(ResultCode.ROLE_NOT_FOUND);
        }
        List<String> functionIds = roleFunctionDao.listByRoleIds(Set.of(roleId)).stream()
                .map(RoleFunction::getSysFunctionId)
                .distinct()
                .sorted()
                .toList();
        List<RoleApi> roleApis = roleApiDao.listByRoleIds(Set.of(roleId));
        List<String> extraApiIds = roleApis.stream()
                .filter(roleApi -> RoleApiGrantSource.EXTRA.getCode().equals(roleApi.getGrantSource())
                        || RoleApiGrantSource.BOTH.getCode().equals(roleApi.getGrantSource()))
                .map(RoleApi::getSysApiId)
                .distinct()
                .sorted()
                .toList();
        Set<String> apiIds = roleApis.stream()
                .map(RoleApi::getSysApiId)
                .collect(java.util.stream.Collectors.toSet());
        List<SysApi> grantedApis = apiIds.isEmpty() ? List.of() : sysApiDao.listByIds(apiIds);
        Map<String, SysApi> apiById = grantedApis.stream()
                .filter(api -> Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(api.getStatus()))
                .collect(java.util.stream.Collectors.toMap(SysApi::getSysApiId, api -> api));
        List<RoleEffectiveApiVo> effectiveApis = roleApis.stream()
                .filter(roleApi -> apiById.containsKey(roleApi.getSysApiId()))
                .map(roleApi -> toEffectiveApi(apiById.get(roleApi.getSysApiId()), roleApi.getGrantSource()))
                .sorted(java.util.Comparator.comparing(RoleEffectiveApiVo::getApiCode,
                        java.util.Comparator.nullsLast(String::compareTo)))
                .toList();
        RolePermissionVo result = new RolePermissionVo();
        result.setRoleId(roleId);
        result.setFunctionIds(functionIds);
        result.setExtraApiIds(extraApiIds);
        result.setEffectiveApis(effectiveApis);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recalculateRolesForFunction(String functionId) {
        List<RoleFunction> affectedBindings = roleFunctionDao.listByFunctionIds(Set.of(functionId));
        Set<String> roleIds = affectedBindings.stream()
                .map(RoleFunction::getRoleId)
                .collect(java.util.stream.Collectors.toSet());
        if (roleIds.isEmpty()) {
            return;
        }

        List<RoleFunction> allRoleFunctions = roleFunctionDao.listByRoleIds(roleIds);
        Set<String> allFunctionIds = allRoleFunctions.stream()
                .map(RoleFunction::getSysFunctionId)
                .collect(java.util.stream.Collectors.toSet());
        Map<String, Set<String>> apiIdsByFunction = groupApiIdsByFunction(
                functionApiDao.listByFunctionIds(allFunctionIds));
        Map<String, Set<String>> functionIdsByRole = groupFunctionIdsByRole(allRoleFunctions);
        Map<String, Set<String>> extraApiIdsByRole = groupExtraApiIdsByRole(roleApiDao.listByRoleIds(roleIds));

        for (String roleId : new TreeSet<>(roleIds)) {
            Set<String> derivedApiIds = new HashSet<>();
            for (String roleFunctionId : functionIdsByRole.getOrDefault(roleId, Set.of())) {
                derivedApiIds.addAll(apiIdsByFunction.getOrDefault(roleFunctionId, Set.of()));
            }
            roleApiDao.replaceByRoleId(roleId, mergeApiSources(roleId, derivedApiIds,
                    extraApiIdsByRole.getOrDefault(roleId, Set.of())));
        }
        incrementPermissionVersions(roleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearRolePermissions(String roleId) {
        roleFunctionDao.replaceByRoleId(roleId, Set.of());
        roleApiDao.replaceByRoleId(roleId, List.of());
    }

    private RoleEffectiveApiVo toEffectiveApi(SysApi api, String grantSource) {
        RoleEffectiveApiVo result = new RoleEffectiveApiVo();
        result.setSysApiId(api.getSysApiId());
        result.setApiCode(api.getApiCode());
        result.setApiName(api.getApiName());
        result.setHttpMethod(api.getHttpMethod());
        result.setPathPattern(api.getPathPattern());
        result.setPermissionCode(api.getPermissionCode());
        result.setGrantSource(grantSource);
        return result;
    }

    private void incrementPermissionVersions(Set<String> roleIds) {
        Set<String> userIds = userRoleDao.listByRoleIds(roleIds).stream()
                .map(UserRole::getUserId)
                .collect(java.util.stream.Collectors.toSet());
        if (!userIds.isEmpty()) {
            userDao.incrementPermissionVersion(userIds);
        }
    }

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

    private void validateEnabledFunctions(Set<String> functionIds) {
        if (functionIds.isEmpty()) {
            return;
        }
        Set<String> validIds = sysFunctionDao.listByIds(functionIds).stream()
                .filter(function -> Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED)
                        .equals(function.getStatus()))
                .map(SysFunction::getSysFunctionId)
                .collect(java.util.stream.Collectors.toSet());
        if (!validIds.equals(functionIds)) {
            throw new BizException(ResultCode.FUNCTION_NOT_FOUND);
        }
    }

    private void validateEnabledApis(Set<String> apiIds) {
        if (apiIds.isEmpty()) {
            return;
        }
        Set<String> validIds = sysApiDao.listByIds(apiIds).stream()
                .filter(api -> Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(api.getStatus()))
                .map(SysApi::getSysApiId)
                .collect(java.util.stream.Collectors.toSet());
        if (!validIds.equals(apiIds)) {
            throw new BizException(ResultCode.API_NOT_FOUND);
        }
    }

    private List<RoleApi> mergeApiSources(String roleId, Set<String> derivedApiIds, Set<String> extraApiIds) {
        Set<String> allApiIds = new TreeSet<>(derivedApiIds);
        allApiIds.addAll(extraApiIds);
        List<RoleApi> grants = new ArrayList<>(allApiIds.size());
        for (String apiId : allApiIds) {
            RoleApi grant = new RoleApi();
            grant.setRoleId(roleId);
            grant.setSysApiId(apiId);
            grant.setGrantSource(resolveSource(derivedApiIds.contains(apiId), extraApiIds.contains(apiId)));
            grant.setRemark("");
            grants.add(grant);
        }
        return grants;
    }

    private String resolveSource(boolean derived, boolean extra) {
        if (derived && extra) {
            return RoleApiGrantSource.BOTH.getCode();
        }
        return derived ? RoleApiGrantSource.FUNCTION.getCode() : RoleApiGrantSource.EXTRA.getCode();
    }

    private Map<String, Set<String>> groupApiIdsByFunction(List<FunctionApi> bindings) {
        Map<String, Set<String>> result = new HashMap<>();
        for (FunctionApi binding : bindings) {
            result.computeIfAbsent(binding.getSysFunctionId(), ignored -> new HashSet<>())
                    .add(binding.getSysApiId());
        }
        return result;
    }

    private Map<String, Set<String>> groupFunctionIdsByRole(List<RoleFunction> bindings) {
        Map<String, Set<String>> result = new HashMap<>();
        for (RoleFunction binding : bindings) {
            result.computeIfAbsent(binding.getRoleId(), ignored -> new HashSet<>())
                    .add(binding.getSysFunctionId());
        }
        return result;
    }

    private Map<String, Set<String>> groupExtraApiIdsByRole(List<RoleApi> grants) {
        Map<String, Set<String>> result = new HashMap<>();
        for (RoleApi grant : grants) {
            if (isExtraSource(grant.getGrantSource())) {
                result.computeIfAbsent(grant.getRoleId(), ignored -> new HashSet<>())
                        .add(grant.getSysApiId());
            }
        }
        return result;
    }

    private boolean isExtraSource(String source) {
        return source == null || RoleApiGrantSource.EXTRA.getCode().equals(source)
                || RoleApiGrantSource.BOTH.getCode().equals(source);
    }
}
