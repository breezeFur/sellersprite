package com.yuanbaomao.sellersprite.system.permission.service.impl;

import com.yuanbaomao.base.constants.SystemConstants;
import com.yuanbaomao.sellersprite.db.dao.RoleApiDao;
import com.yuanbaomao.sellersprite.db.dao.RoleDao;
import com.yuanbaomao.sellersprite.db.dao.RoleFunctionDao;
import com.yuanbaomao.sellersprite.db.dao.SysApiDao;
import com.yuanbaomao.sellersprite.db.dao.SysFunctionDao;
import com.yuanbaomao.sellersprite.db.dao.UserRoleDao;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.entity.Role;
import com.yuanbaomao.sellersprite.db.entity.RoleApi;
import com.yuanbaomao.sellersprite.db.entity.RoleFunction;
import com.yuanbaomao.sellersprite.db.entity.SysApi;
import com.yuanbaomao.sellersprite.db.entity.SysFunction;
import com.yuanbaomao.sellersprite.db.entity.UserRole;
import com.yuanbaomao.sellersprite.db.entity.User;
import com.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import com.yuanbaomao.sellersprite.system.convert.SystemConverter;
import com.yuanbaomao.sellersprite.system.permission.enums.FunctionType;
import com.yuanbaomao.sellersprite.system.permission.model.vo.EffectiveApiPermissionVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.PermissionApiSourceVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.PermissionMenuVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.UserPermissionContextVo;
import com.yuanbaomao.sellersprite.system.permission.service.PermissionContextService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionContextServiceImpl implements PermissionContextService {

    private final UserDao userDao;
    private final UserRoleDao userRoleDao;
    private final RoleDao roleDao;
    private final RoleFunctionDao roleFunctionDao;
    private final SysFunctionDao sysFunctionDao;
    private final RoleApiDao roleApiDao;
    private final SysApiDao sysApiDao;

    @Override
    public UserPermissionContextVo getByUserId(String userId) {
        UserPermissionContextVo context = new UserPermissionContextVo();
        User user = userDao.getById(userId);
        if (user == null || !Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(user.getStatus())) {
            return context;
        }
        Set<String> assignedRoleIds = userRoleDao.listByUserId(userId).stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toSet());
        if (assignedRoleIds.isEmpty()) {
            return context;
        }

        List<Role> enabledRoles = roleDao.listByIds(assignedRoleIds).stream()
                .filter(role -> Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(role.getStatus()))
                .sorted(Comparator.comparing(Role::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(Role::getRoleId))
                .toList();
        context.setRoles(enabledRoles.stream().map(SystemConverter::toRoleVo).toList());
        Set<String> roleIds = enabledRoles.stream().map(Role::getRoleId).collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return context;
        }

        aggregateFunctions(context, roleIds);
        aggregateApis(context, enabledRoles, roleIds);
        return context;
    }

    private void aggregateFunctions(UserPermissionContextVo context, Set<String> roleIds) {
        Set<String> grantedFunctionIds = roleFunctionDao.listByRoleIds(roleIds).stream()
                .map(RoleFunction::getSysFunctionId)
                .collect(Collectors.toSet());
        if (grantedFunctionIds.isEmpty()) {
            return;
        }
        List<SysFunction> enabledFunctions = sysFunctionDao.listEnabled();
        Map<String, SysFunction> functionById = enabledFunctions.stream()
                .collect(Collectors.toMap(SysFunction::getSysFunctionId, Function.identity()));
        Set<String> effectiveGrantedIds = grantedFunctionIds.stream()
                .filter(functionById::containsKey)
                .collect(Collectors.toSet());

        LinkedHashSet<String> permissionCodes = effectiveGrantedIds.stream()
                .map(functionById::get)
                .map(SysFunction::getPermissionCode)
                .filter(code -> code != null && !code.isBlank())
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        context.setPermissionCodes(permissionCodes);

        Set<String> menuFunctionIds = includeEnabledAncestors(effectiveGrantedIds, functionById);
        List<SysFunction> menuFunctions = enabledFunctions.stream()
                .filter(function -> menuFunctionIds.contains(function.getSysFunctionId()))
                .filter(function -> !FunctionType.BUTTON.getCode().equals(function.getFunctionType()))
                .filter(function -> Integer.valueOf(SystemBusinessConstants.YES).equals(function.getVisible()))
                .toList();
        context.setMenuTree(buildMenuTree(menuFunctions));
    }

    private Set<String> includeEnabledAncestors(Set<String> grantedFunctionIds,
            Map<String, SysFunction> functionById) {
        Set<String> result = new HashSet<>(grantedFunctionIds);
        for (String functionId : grantedFunctionIds) {
            Set<String> visited = new HashSet<>();
            SysFunction current = functionById.get(functionId);
            while (current != null && current.getParentId() != null
                    && !SystemConstants.ROOT_PARENT_ID.equals(current.getParentId())
                    && visited.add(current.getSysFunctionId())) {
                SysFunction parent = functionById.get(current.getParentId());
                if (parent == null) {
                    break;
                }
                result.add(parent.getSysFunctionId());
                current = parent;
            }
        }
        return result;
    }

    private List<PermissionMenuVo> buildMenuTree(List<SysFunction> functions) {
        Comparator<SysFunction> comparator = Comparator
                .comparing(SysFunction::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(SysFunction::getSysFunctionId);
        Map<String, PermissionMenuVo> menuById = functions.stream()
                .sorted(comparator)
                .collect(Collectors.toMap(SysFunction::getSysFunctionId, this::toMenu,
                        (left, right) -> left, LinkedHashMap::new));
        List<PermissionMenuVo> roots = new ArrayList<>();
        for (PermissionMenuVo menu : menuById.values()) {
            PermissionMenuVo parent = menuById.get(menu.getParentId());
            if (parent == null) {
                roots.add(menu);
            } else {
                parent.getChildren().add(menu);
            }
        }
        return roots;
    }

    private PermissionMenuVo toMenu(SysFunction function) {
        PermissionMenuVo menu = new PermissionMenuVo();
        menu.setFunctionId(function.getSysFunctionId());
        menu.setParentId(function.getParentId());
        menu.setName(function.getFunctionName());
        menu.setType(function.getFunctionType());
        menu.setRoutePath(function.getRoutePath());
        menu.setComponentPath(function.getComponentPath());
        menu.setIcon(function.getIcon());
        menu.setCacheable(function.getCacheable());
        menu.setPermissionCode(function.getPermissionCode());
        menu.setSortOrder(function.getSortOrder());
        return menu;
    }

    private void aggregateApis(UserPermissionContextVo context, List<Role> roles, Set<String> roleIds) {
        List<RoleApi> grants = roleApiDao.listByRoleIds(roleIds);
        Set<String> apiIds = grants.stream().map(RoleApi::getSysApiId).collect(Collectors.toSet());
        if (apiIds.isEmpty()) {
            return;
        }
        Map<String, Role> roleById = roles.stream()
                .collect(Collectors.toMap(Role::getRoleId, Function.identity()));
        Map<String, SysApi> enabledApiById = sysApiDao.listByIds(apiIds).stream()
                .filter(api -> Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(api.getStatus()))
                .collect(Collectors.toMap(SysApi::getSysApiId, Function.identity()));
        Map<String, List<RoleApi>> grantsByApiId = grants.stream()
                .filter(grant -> enabledApiById.containsKey(grant.getSysApiId()))
                .filter(grant -> roleById.containsKey(grant.getRoleId()))
                .collect(Collectors.groupingBy(RoleApi::getSysApiId, HashMap::new, Collectors.toList()));

        List<EffectiveApiPermissionVo> effectiveApis = enabledApiById.values().stream()
                .filter(api -> grantsByApiId.containsKey(api.getSysApiId()))
                .sorted(Comparator.comparing(SysApi::getHttpMethod).thenComparing(SysApi::getPathPattern)
                        .thenComparing(SysApi::getSysApiId))
                .map(api -> toEffectiveApi(api, grantsByApiId.get(api.getSysApiId()), roleById))
                .toList();
        context.setEffectiveApis(effectiveApis);
    }

    private EffectiveApiPermissionVo toEffectiveApi(SysApi api, List<RoleApi> grants,
            Map<String, Role> roleById) {
        EffectiveApiPermissionVo permission = new EffectiveApiPermissionVo();
        permission.setSysApiId(api.getSysApiId());
        permission.setApiCode(api.getApiCode());
        permission.setHttpMethod(api.getHttpMethod());
        permission.setPathPattern(api.getPathPattern());
        permission.setPermissionCode(api.getPermissionCode());
        List<PermissionApiSourceVo> sources = grants.stream()
                .sorted(Comparator.comparing(grant -> roleById.get(grant.getRoleId()).getRoleCode()))
                .map(grant -> toSource(grant, roleById.get(grant.getRoleId())))
                .toList();
        permission.setSources(sources);
        return permission;
    }

    private PermissionApiSourceVo toSource(RoleApi grant, Role role) {
        PermissionApiSourceVo source = new PermissionApiSourceVo();
        source.setRoleId(role.getRoleId());
        source.setRoleCode(role.getRoleCode());
        source.setGrantSource(grant.getGrantSource());
        return source;
    }
}
