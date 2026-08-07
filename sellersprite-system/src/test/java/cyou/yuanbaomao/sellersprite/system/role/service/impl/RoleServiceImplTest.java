package cyou.yuanbaomao.sellersprite.system.role.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.DeptDao;
import cyou.yuanbaomao.sellersprite.db.dao.RoleDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserRoleDao;
import cyou.yuanbaomao.sellersprite.db.entity.Role;
import cyou.yuanbaomao.sellersprite.db.entity.User;
import cyou.yuanbaomao.sellersprite.db.entity.Dept;
import cyou.yuanbaomao.sellersprite.db.entity.UserRole;
import cyou.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import cyou.yuanbaomao.sellersprite.system.role.model.dto.RolePageRequest;
import cyou.yuanbaomao.sellersprite.system.role.model.dto.RoleUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.role.model.dto.UserRoleBindRequest;
import cyou.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import cyou.yuanbaomao.sellersprite.system.permission.service.RolePermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleDao roleDao;
    @Mock
    private UserDao userDao;
    @Mock
    private DeptDao deptDao;
    @Mock
    private UserRoleDao userRoleDao;
    @Mock
    private RolePermissionService rolePermissionService;

    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleServiceImpl(roleDao, userDao, deptDao, userRoleDao, rolePermissionService);
    }

    @Test
    void shouldRejectDeletingRoleAssignedToUser() {
        when(roleDao.getById("role-1")).thenReturn(role());
        when(userRoleDao.existsByRoleId("role-1")).thenReturn(true);

        assertThatThrownBy(() -> roleService.delete("role-1"))
                .isInstanceOfSatisfying(BizException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ResultCode.RESOURCE_CONFLICT);
                    assertThat(exception.getMessage()).contains("用户");
                });
        verify(roleDao, never()).removeById(org.mockito.ArgumentMatchers.anyString());
        verify(rolePermissionService, never()).clearRolePermissions(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldClearPermissionsWhenDeletingUnassignedRole() {
        when(roleDao.getById("role-1")).thenReturn(role());

        roleService.delete("role-1");

        verify(rolePermissionService).clearRolePermissions("role-1");
        verify(roleDao).removeById("role-1");
    }

    @Test
    void shouldIncrementPermissionVersionWhenBindingRoleToUser() {
        when(userDao.getById("user-1")).thenReturn(new User());
        when(roleDao.getById("role-1")).thenReturn(role());
        Dept department = new Dept();
        department.setStatus(1);
        when(deptDao.getById("dept-1")).thenReturn(department);
        UserRoleBindRequest request = new UserRoleBindRequest();
        request.setUserId("user-1");
        request.setRoleId("role-1");
        request.setDeptId("dept-1");

        roleService.bindUserRole(request);

        verify(userRoleDao).clearPrimaryRoleByUserId("user-1");
        verify(userDao).incrementPermissionVersion(Set.of("user-1"));
    }

    @Test
    void shouldIncrementAssignedUsersPermissionVersionWhenDisablingRole() {
        Role role = role();
        UserRole binding = new UserRole();
        binding.setUserId("user-1");
        when(roleDao.getById("role-1")).thenReturn(role);
        when(userRoleDao.listByRoleIds(Set.of("role-1"))).thenReturn(List.of(binding));

        roleService.updateStatus("role-1", SystemBusinessConstants.STATUS_DISABLED);

        assertThat(role.getStatus()).isZero();
        verify(roleDao).updateById(role);
        verify(userDao).incrementPermissionVersion(Set.of("user-1"));
    }

    @Test
    void shouldPageAndUpdateRoles() {
        Role role = role();
        role.setRoleCode("operator");
        role.setRoleName("操作员");
        Page<Role> page = new Page<>(2, 10, 1);
        page.setRecords(List.of(role));
        when(roleDao.pageRoles("操作", 1, 2, 10)).thenReturn(page);
        RolePageRequest pageRequest = new RolePageRequest();
        pageRequest.setCurrent(2L);
        pageRequest.setSize(10L);
        pageRequest.setRoleName("操作");
        pageRequest.setStatus(1);

        PageResult<RoleVo> pageResult = roleService.page(pageRequest);

        assertThat(pageResult.getRecords()).extracting(RoleVo::getRoleCode).containsExactly("operator");

        when(roleDao.getById("role-1")).thenReturn(role);
        RoleUpdateRequest updateRequest = new RoleUpdateRequest();
        updateRequest.setRoleCode("auditor");
        updateRequest.setRoleName("审计员");
        RoleVo updated = roleService.update("role-1", updateRequest);

        assertThat(updated.getRoleCode()).isEqualTo("auditor");
        assertThat(updated.getRoleName()).isEqualTo("审计员");
        verify(roleDao).updateById(role);
    }

    @Test
    void shouldRejectDuplicateUserRoleBinding() {
        User user = new User();
        Role role = role();
        Dept department = new Dept();
        department.setStatus(1);
        UserRoleBindRequest request = new UserRoleBindRequest();
        request.setUserId("user-1");
        request.setRoleId("role-1");
        request.setDeptId("dept-1");
        when(userDao.getById("user-1")).thenReturn(user);
        when(roleDao.getById("role-1")).thenReturn(role);
        when(deptDao.getById("dept-1")).thenReturn(department);
        when(userRoleDao.findByUserIdAndRoleId("user-1", "role-1")).thenReturn(Optional.of(new UserRole()));

        assertThatThrownBy(() -> roleService.bindUserRole(request))
                .isInstanceOfSatisfying(BizException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ResultCode.RESOURCE_CONFLICT));
        verify(userRoleDao, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldUnbindUserAndPromoteRemainingRoleWhenPrimaryRoleRemoved() {
        UserRole removed = new UserRole();
        removed.setUserRoleId("binding-1");
        removed.setUserId("user-1");
        removed.setRoleId("role-1");
        removed.setPrimaryRole(1);
        UserRole remaining = new UserRole();
        remaining.setUserRoleId("binding-2");
        remaining.setUserId("user-1");
        remaining.setRoleId("role-2");
        remaining.setPrimaryRole(0);
        when(roleDao.getById("role-1")).thenReturn(role());
        when(userDao.getById("user-1")).thenReturn(new User());
        when(userRoleDao.findByUserIdAndRoleId("user-1", "role-1")).thenReturn(Optional.of(removed));
        when(userRoleDao.listByUserId("user-1")).thenReturn(List.of(remaining));

        roleService.unbindUser("role-1", "user-1");

        verify(userRoleDao).removeByUserIdAndRoleId("user-1", "role-1");
        assertThat(remaining.getPrimaryRole()).isEqualTo(1);
        verify(userRoleDao).updateById(remaining);
        verify(userDao).incrementPermissionVersion(Set.of("user-1"));
    }

    private Role role() {
        Role role = new Role();
        role.setRoleId("role-1");
        role.setStatus(1);
        return role;
    }
}
