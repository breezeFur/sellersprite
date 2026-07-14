package com.yuanbaomao.sellersprite.framework.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.dao.UserTokenDao;
import com.yuanbaomao.sellersprite.db.entity.User;
import com.yuanbaomao.sellersprite.db.entity.UserToken;
import com.yuanbaomao.base.constants.HttpHeaderConstants;
import com.yuanbaomao.base.context.RequestContext;
import com.yuanbaomao.base.context.RequestContextHolder;
import com.yuanbaomao.base.exception.BizException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class TokenAuthInterceptorTest {

    private static final String ACCESS_TOKEN = "access-token";
    private static final String USER_ID = "user-001";
    private static final String USERNAME = "admin";

    @Mock
    private UserTokenDao userTokenDao;

    @Mock
    private UserDao userDao;

    @Mock
    private ApiResourceMatcher apiResourceMatcher;

    @AfterEach
    void tearDown() {
        RequestContextHolder.clear();
    }

    @Test
    void shouldRejectProtectedRequestWithoutBearerToken() {
        TokenAuthInterceptor interceptor = new TokenAuthInterceptor(new TokenHasher(), userTokenDao, userDao,
                apiResourceMatcher);

        assertThatThrownBy(() -> interceptor.preHandle(new MockHttpServletRequest("GET", "/api/users"),
                new MockHttpServletResponse(), new Object()))
                .isInstanceOf(BizException.class)
                .hasMessage(ResultCode.UNAUTHORIZED.getMessage())
                .extracting("code")
                .isEqualTo(ResultCode.UNAUTHORIZED.getCode());
    }

    @Test
    void shouldSetCurrentUserWhenBearerTokenIsValid() throws Exception {
        TokenHasher tokenHasher = new TokenHasher();
        String accessTokenHash = tokenHasher.sha256(ACCESS_TOKEN);
        UserToken userToken = new UserToken();
        userToken.setUserId(USER_ID);
        User user = new User();
        user.setUserId(USER_ID);
        user.setUsername(USERNAME);
        user.setStatus(1);
        when(userTokenDao.findValidByAccessTokenHash(accessTokenHash)).thenReturn(Optional.of(userToken));
        when(userDao.getById(USER_ID)).thenReturn(user);
        TokenAuthInterceptor interceptor = new TokenAuthInterceptor(tokenHasher, userTokenDao, userDao,
                apiResourceMatcher);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        request.addHeader(HttpHeaderConstants.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(result).isTrue();
        Optional<RequestContext> context = RequestContextHolder.get();
        assertThat(context).isPresent();
        assertThat(context.get().getUserId()).isEqualTo(USER_ID);
        assertThat(context.get().getUsername()).isEqualTo(USERNAME);
    }

    @Test
    void shouldRejectDisabledUserEvenWhenBearerTokenIsValid() {
        TokenHasher tokenHasher = new TokenHasher();
        String accessTokenHash = tokenHasher.sha256(ACCESS_TOKEN);
        UserToken userToken = new UserToken();
        userToken.setUserId(USER_ID);
        User user = new User();
        user.setUserId(USER_ID);
        user.setUsername(USERNAME);
        user.setStatus(0);
        when(userTokenDao.findValidByAccessTokenHash(accessTokenHash)).thenReturn(Optional.of(userToken));
        when(userDao.getById(USER_ID)).thenReturn(user);
        TokenAuthInterceptor interceptor = new TokenAuthInterceptor(tokenHasher, userTokenDao, userDao,
                apiResourceMatcher);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        request.addHeader(HttpHeaderConstants.AUTHORIZATION, "Bearer " + ACCESS_TOKEN);

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(BizException.class)
                .hasMessage(ResultCode.UNAUTHORIZED.getMessage())
                .extracting("code")
                .isEqualTo(ResultCode.UNAUTHORIZED.getCode());
    }

    @Test
    void shouldSkipPublicApiPath() throws Exception {
        TokenAuthInterceptor interceptor = new TokenAuthInterceptor(new TokenHasher(), userTokenDao, userDao,
                apiResourceMatcher);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        when(apiResourceMatcher.isStaticPublic(request)).thenReturn(true);

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(result).isTrue();
        verify(userTokenDao, never()).findValidByAccessTokenHash(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldRequireAuthenticationForRegisteredPublicApiPath() {
        TokenAuthInterceptor interceptor = new TokenAuthInterceptor(new TokenHasher(), userTokenDao, userDao,
                apiResourceMatcher);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/public/ping");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(BizException.class)
                .hasMessage(ResultCode.UNAUTHORIZED.getMessage());
        verify(userTokenDao, never()).findValidByAccessTokenHash(org.mockito.ArgumentMatchers.anyString());
    }
}
