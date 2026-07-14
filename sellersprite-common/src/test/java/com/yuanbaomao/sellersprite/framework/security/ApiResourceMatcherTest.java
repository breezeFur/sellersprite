package com.yuanbaomao.sellersprite.framework.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yuanbaomao.sellersprite.db.dao.SysApiDao;
import com.yuanbaomao.sellersprite.db.entity.SysApi;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class ApiResourceMatcherTest {

    @Mock
    private SysApiDao sysApiDao;

    @Test
    void shouldNormalizeContextPathAndChooseMostSpecificTemplate() {
        SysApi wildcard = api("wildcard", "/api/users/**");
        SysApi detail = api("detail", "/api/users/{userId}");
        when(sysApiDao.listEnabledByHttpMethod("GET")).thenReturn(List.of(wildcard, detail));
        ApiResourceMatcher matcher = new ApiResourceMatcher(sysApiDao);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/console/api/users/user-1/");
        request.setContextPath("/console");
        request.setQueryString("enabled=true");

        assertThat(matcher.match(request)).contains(detail);
        assertThat(matcher.match(request)).contains(detail);
        verify(sysApiDao).listEnabledByHttpMethod("GET");
    }

    @Test
    void shouldNotMatchResourceRegisteredForAnotherMethod() {
        when(sysApiDao.listEnabledByHttpMethod("DELETE")).thenReturn(List.of());
        ApiResourceMatcher matcher = new ApiResourceMatcher(sysApiDao);

        assertThat(matcher.match(new MockHttpServletRequest("DELETE", "/api/users/user-1"))).isEmpty();
    }

    private SysApi api(String apiId, String pathPattern) {
        SysApi api = new SysApi();
        api.setSysApiId(apiId);
        api.setHttpMethod("GET");
        api.setPathPattern(pathPattern);
        api.setStatus(1);
        return api;
    }
}
