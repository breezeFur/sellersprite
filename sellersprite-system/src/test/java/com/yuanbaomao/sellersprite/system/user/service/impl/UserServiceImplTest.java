package com.yuanbaomao.sellersprite.system.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.base.context.RequestContext;
import com.yuanbaomao.base.context.RequestContextHolder;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.dao.UserTokenDao;
import com.yuanbaomao.sellersprite.db.entity.User;
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

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userDao, passwordEncoder, userTokenDao);
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

    private User user() {
        User user = new User();
        user.setUserId("user-1");
        user.setUsername("yuanbao");
        user.setStatus(1);
        return user;
    }
}
