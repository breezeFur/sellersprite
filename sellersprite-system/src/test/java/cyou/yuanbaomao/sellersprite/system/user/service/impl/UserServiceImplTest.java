package cyou.yuanbaomao.sellersprite.system.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.base.context.RequestContext;
import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.UserDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserTokenDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserRoleDao;
import cyou.yuanbaomao.sellersprite.db.dao.RoleDao;
import cyou.yuanbaomao.sellersprite.db.dao.DeptDao;
import cyou.yuanbaomao.sellersprite.db.entity.User;
import cyou.yuanbaomao.sellersprite.db.entity.Role;
import cyou.yuanbaomao.sellersprite.db.entity.Dept;
import cyou.yuanbaomao.sellersprite.db.entity.UserRole;
import cyou.yuanbaomao.sellersprite.system.user.model.dto.UserUpdateRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;
    @Mock
    private UserTokenDao userTokenDao;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRoleDao userRoleDao;
    @Mock
    private RoleDao roleDao;
    @Mock
    private DeptDao deptDao;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userDao, passwordEncoder, userTokenDao, userRoleDao, roleDao, deptDao);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void shouldRevokeSessionsWhenUserIsDisabled() {
        User user = user();
        when(userDao.getById("user-1")).thenReturn(user);

        userService.updateStatus("user-1", 0);

        assertThat(user.getStatus()).isZero();
        verify(userDao).updateById(user);
        verify(userTokenDao).revokeByUserId(anyString(), any(Long.class), anyString());
    }

    @Test
    void shouldRevokeSessionsWhenPasswordIsReset() {
        User user = user();
        when(userDao.getById("user-1")).thenReturn(user);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        userService.resetPassword("user-1", "new-password");

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(user.getPasswordUpdatedAt()).isNotNull();
        verify(userTokenDao).revokeByUserId(anyString(), any(Long.class), anyString());
    }

    @Test
    void shouldRevokeSessionsBeforeDeletingAnotherUser() {
        User user = user();
        when(userDao.getById("user-1")).thenReturn(user);
        RequestContextHolder.set(RequestContext.builder().userId("operator-1").username("operator").build());

        userService.delete("user-1");

        verify(userTokenDao).revokeByUserId(anyString(), any(Long.class), anyString());
        verify(userRoleDao).replaceByUserId("user-1", null, Set.of());
        verify(userDao).removeById("user-1");
    }

    @Test
    void shouldRejectDeletingCurrentUser() {
        RequestContextHolder.set(RequestContext.builder().userId("user-1").username("yuanbao").build());

        assertThatThrownBy(() -> userService.delete("user-1"))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ResultCode.CURRENT_USER_OPERATION_FORBIDDEN));
        verify(userTokenDao, never()).revokeByUserId(anyString(), any(Long.class), anyString());
        verify(userDao, never()).removeById(anyString());
    }

    @Test
    void shouldUpdateUserProfileAfterValidatingDepartmentAndUsername() {
        User user = user();
        when(userDao.getById("user-1")).thenReturn(user);
        when(deptDao.getById("dept-1")).thenReturn(enabledDept());
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("yuanbao-new");
        request.setNickname("元宝");
        request.setRealName("元宝猫");
        request.setEmail("yuanbao@example.com");
        request.setPrimaryDeptId("dept-1");

        userService.update("user-1", request);

        assertThat(user.getUsername()).isEqualTo("yuanbao-new");
        assertThat(user.getPrimaryDeptId()).isEqualTo("dept-1");
        verify(userDao).updateById(user);
    }

    @Test
    void shouldKeepUserRolesWhenAnyRequestedRoleIsInvalid() {
        User user = user();
        user.setPrimaryDeptId("dept-1");
        when(userDao.getById("user-1")).thenReturn(user);
        when(deptDao.getById("dept-1")).thenReturn(enabledDept());
        when(roleDao.listByIds(Set.of("role-1", "role-missing"))).thenReturn(List.of(enabledRole("role-1")));

        assertThatThrownBy(() -> userService.replaceRoles("user-1", List.of("role-1", "role-missing")))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ResultCode.ROLE_NOT_FOUND));
        verify(userRoleDao, never()).replaceByUserId(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyCollection());
    }

    @Test
    void shouldReplaceRolesAndIncrementPermissionVersion() {
        User user = user();
        user.setPrimaryDeptId("dept-1");
        when(userDao.getById("user-1")).thenReturn(user);
        when(deptDao.getById("dept-1")).thenReturn(enabledDept());
        when(roleDao.listByIds(Set.of("role-1", "role-2")))
                .thenReturn(List.of(enabledRole("role-1"), enabledRole("role-2")));

        userService.replaceRoles("user-1", List.of("role-1", "role-2", "role-1"));

        verify(userRoleDao).replaceByUserId(org.mockito.ArgumentMatchers.eq("user-1"),
                org.mockito.ArgumentMatchers.eq("dept-1"),
                org.mockito.ArgumentMatchers.argThat(ids -> Set.copyOf(ids).equals(Set.of("role-1", "role-2"))));
        verify(userDao).incrementPermissionVersion(Set.of("user-1"));
    }

    @Test
    void shouldPageUsersThroughDaoBoundary() {
        Page<User> page = Page.of(2, 10, 11);
        page.setRecords(List.of(user()));
        when(userDao.pageUsers("yuan", 1, 2, 10)).thenReturn(page);
        UserRole userRole = new UserRole();
        userRole.setUserId("user-1");
        userRole.setRoleId("role-1");
        when(userRoleDao.listByUserId("user-1")).thenReturn(List.of(userRole));

        cyou.yuanbaomao.mybatis.result.YPage<cyou.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo> result =
                userService.page(cyou.yuanbaomao.mybatis.result.YPage.of(2L, 10L), "yuan", 1);

        assertThat(result.getCurrent()).isEqualTo(2L);
        assertThat(result.getSize()).isEqualTo(10L);
        assertThat(result.getTotal()).isEqualTo(11L);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().getFirst().getRoleIds()).containsExactly("role-1");
    }

    private User user() {
        User user = new User();
        user.setUserId("user-1");
        user.setUsername("yuanbao");
        user.setStatus(1);
        return user;
    }

    private Role enabledRole(String roleId) {
        Role role = new Role();
        role.setRoleId(roleId);
        role.setStatus(1);
        return role;
    }

    private Dept enabledDept() {
        Dept dept = new Dept();
        dept.setDeptId("dept-1");
        dept.setStatus(1);
        return dept;
    }
}
