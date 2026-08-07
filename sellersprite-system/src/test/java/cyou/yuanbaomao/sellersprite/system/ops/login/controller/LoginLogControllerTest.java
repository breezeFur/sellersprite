package cyou.yuanbaomao.sellersprite.system.ops.login.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.sellersprite.system.ops.login.model.dto.LoginLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.login.model.vo.LoginLogVo;
import cyou.yuanbaomao.sellersprite.system.ops.login.service.LoginLogService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class LoginLogControllerTest {

    @Test
    void shouldExposeLoginLogPageAndDetail() throws Exception {
        LoginLogService service = mock(LoginLogService.class);
        LoginLogVo log = new LoginLogVo();
        log.setLoginLogId("login-1");
        log.setUsername("yuanbao");
        when(service.page(any(LoginLogPageRequest.class)))
                .thenReturn(PageResult.of(1, 20, 1, List.of(log)));
        when(service.detail("login-1")).thenReturn(log);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new LoginLogController(service)).build();

        mockMvc.perform(get("/api/logs/login").param("success", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].loginLogId").value("login-1"));
        mockMvc.perform(get("/api/logs/login/login-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("yuanbao"));
    }
}
