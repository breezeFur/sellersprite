package cyou.yuanbaomao.sellersprite.system.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cyou.yuanbaomao.sellersprite.system.user.model.dto.UserUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import cyou.yuanbaomao.sellersprite.system.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserControllerTest {

    @Test
    void shouldExposeUserLifecycleWriteEndpoints() throws Exception {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        UserDetailVo updated = new UserDetailVo();
        updated.setUserId("user-1");
        updated.setUsername("yuanbao-new");
        when(userService.update(eq("user-1"), any(UserUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"yuanbao-new\",\"email\":\"yuanbao@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("yuanbao-new"));
        mockMvc.perform(put("/api/users/user-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/users/user-1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleIds\":[\"role-1\",\"role-2\"]}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/users/user-1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"new-password\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/users/user-1"))
                .andExpect(status().isOk());

        verify(userService).updateStatus("user-1", 0);
        verify(userService).replaceRoles("user-1", java.util.List.of("role-1", "role-2"));
        verify(userService).resetPassword("user-1", "new-password");
        verify(userService).delete("user-1");
    }
}
