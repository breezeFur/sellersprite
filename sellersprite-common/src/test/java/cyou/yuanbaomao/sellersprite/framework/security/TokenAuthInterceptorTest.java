package cyou.yuanbaomao.sellersprite.framework.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.UserDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserTokenDao;
import cyou.yuanbaomao.sellersprite.db.entity.User;
import cyou.yuanbaomao.sellersprite.db.entity.UserToken;
import cyou.yuanbaomao.base.constants.HttpHeaderConstants;
import cyou.yuanbaomao.base.context.RequestContext;
import cyou.yuanbaomao.base.context.RequestContextHolder;
import cyou.yuanbaomao.base.exception.BizException;
import jakarta.servlet.DispatcherType;
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
    void shouldSkipMarketResearchStreamAsyncRedispatchAfterInitialAuthentication() throws Exception {
        TokenAuthInterceptor interceptor = new TokenAuthInterceptor(new TokenHasher(), userTokenDao, userDao,
                apiResourceMatcher);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/market-research/jobs/job-1/stream");
        request.setDispatcherType(DispatcherType.ASYNC);

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), new Object())).isTrue();
        verify(apiResourceMatcher, never()).isStaticPublic(request);
        verify(userTokenDao, never()).findValidByAccessTokenHash(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldRequireAuthenticationForUnrelatedAsyncRedispatch() {
        TokenAuthInterceptor interceptor = new TokenAuthInterceptor(new TokenHasher(), userTokenDao, userDao,
                apiResourceMatcher);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        request.setDispatcherType(DispatcherType.ASYNC);

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(BizException.class)
                .hasMessage(ResultCode.UNAUTHORIZED.getMessage());
        verify(apiResourceMatcher).isStaticPublic(request);
    }

    @Test
    void shouldRequireAuthenticationForInitialMarketResearchStreamRequest() {
        TokenAuthInterceptor interceptor = new TokenAuthInterceptor(new TokenHasher(), userTokenDao, userDao,
                apiResourceMatcher);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/market-research/jobs/job-1/stream");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(BizException.class)
                .hasMessage(ResultCode.UNAUTHORIZED.getMessage());
        verify(apiResourceMatcher).isStaticPublic(request);
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
