package com.yuanbaomao.sellersprite.system.role.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.system.role.model.dto.RolePageRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.RolePermissionReplaceRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.RoleUpdateRequest;
import com.yuanbaomao.sellersprite.system.role.model.dto.RoleUserPageRequest;
import com.yuanbaomao.sellersprite.system.role.model.vo.RolePermissionVo;
import com.yuanbaomao.sellersprite.system.role.model.vo.RoleVo;
import com.yuanbaomao.sellersprite.system.role.service.RoleService;
import com.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RoleControllerTest {

    @Test
    void shouldExposeRoleManagementAndPermissionEndpoints() throws Exception {
        RoleService roleService = mock(RoleService.class);
        RoleController controller = new RoleController(roleService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        RoleVo role = new RoleVo();
        role.setRoleId("role-1");
        role.setRoleName("管理员");
        RolePermissionVo permission = new RolePermissionVo();
        permission.setRoleId("role-1");
        when(roleService.page(any(RolePageRequest.class)))
                .thenReturn(PageResult.of(1, 20, 1, List.of(role)));
        when(roleService.detail("role-1")).thenReturn(role);
        when(roleService.update(eq("role-1"), any(RoleUpdateRequest.class))).thenReturn(role);
        when(roleService.listUsers(eq("role-1"), any(RoleUserPageRequest.class)))
                .thenReturn(PageResult.of(1, 20, 0, List.<UserDetailVo>of()));
        when(roleService.getPermissions("role-1")).thenReturn(permission);
        when(roleService.replacePermissions(eq("role-1"), any(RolePermissionReplaceRequest.class)))
                .thenReturn(permission);

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].roleId").value("role-1"));
        mockMvc.perform(get("/api/roles/role-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleName").value("管理员"));
        mockMvc.perform(put("/api/roles/role-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"admin\",\"roleName\":\"管理员\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/roles/role-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/roles/role-1/users"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/roles/role-1/users/user-1"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/roles/role-1/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleId").value("role-1"));
        mockMvc.perform(put("/api/roles/role-1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"functionIds\":[],\"extraApiIds\":[]}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/roles/role-1"))
                .andExpect(status().isOk());

        verify(roleService).updateStatus("role-1", 0);
        verify(roleService).unbindUser("role-1", "user-1");
        verify(roleService).delete("role-1");
    }
}
