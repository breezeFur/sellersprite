package com.yuanbaomao.sellersprite.system.permission.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.system.permission.model.dto.SysApiPageRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.SysApiUpdateRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.SysFunctionUpdateRequest;
import com.yuanbaomao.sellersprite.system.permission.model.vo.SysApiVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.SysFunctionVo;
import com.yuanbaomao.sellersprite.system.permission.service.PermissionService;
import com.yuanbaomao.sellersprite.system.permission.service.ApiCatalogService;
import com.yuanbaomao.sellersprite.system.permission.model.vo.ApiCatalogSyncResultVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.MenuApiBindingSyncResultVo;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PermissionControllerTest {

    @Test
    void shouldExposeFunctionTreeAndApiResourceLifecycleEndpoints() throws Exception {
        PermissionService permissionService = mock(PermissionService.class);
        ApiCatalogService apiCatalogService = mock(ApiCatalogService.class);
        PermissionController controller = new PermissionController(permissionService, apiCatalogService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SysFunctionVo function = new SysFunctionVo();
        function.setSysFunctionId("function-1");
        function.setFunctionName("用户管理");
        SysApiVo api = new SysApiVo();
        api.setSysApiId("api-1");
        api.setApiName("用户分页");
        when(permissionService.functionTree()).thenReturn(List.of(function));
        when(permissionService.functionDetail("function-1")).thenReturn(function);
        when(permissionService.updateFunction(eq("function-1"), any(SysFunctionUpdateRequest.class)))
                .thenReturn(function);
        when(permissionService.getFunctionApiIds("function-1")).thenReturn(List.of("api-1"));
        when(permissionService.pageApis(any(SysApiPageRequest.class)))
                .thenReturn(PageResult.of(1, 20, 1, List.of(api)));
        when(permissionService.apiDetail("api-1")).thenReturn(api);
        when(permissionService.updateApi(eq("api-1"), any(SysApiUpdateRequest.class))).thenReturn(api);

        mockMvc.perform(get("/api/permissions/functions/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sysFunctionId").value("function-1"));
        mockMvc.perform(get("/api/permissions/functions/function-1"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/permissions/functions/function-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentId\":\"0\",\"functionCode\":\"users\","
                                + "\"functionName\":\"用户管理\",\"functionType\":\"DIR\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/permissions/functions/function-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/permissions/functions/function-1/apis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("api-1"));
        mockMvc.perform(put("/api/permissions/functions/function-1/apis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"apiIds\":[\"api-1\"]}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/permissions/apis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].sysApiId").value("api-1"));
        mockMvc.perform(get("/api/permissions/apis/api-1"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/permissions/apis/api-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"apiCode\":\"user.page\",\"apiName\":\"用户分页\","
                                + "\"apiType\":\"PERMISSION\",\"httpMethod\":\"GET\","
                                + "\"pathPattern\":\"/api/users\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/permissions/apis/api-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/permissions/apis/api-1"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/permissions/functions/function-1"))
                .andExpect(status().isOk());

        verify(permissionService).updateFunctionStatus("function-1", 0);
        verify(permissionService).replaceFunctionApis("function-1", List.of("api-1"));
        verify(permissionService).updateApiStatus("api-1", 0);
        verify(permissionService).deleteApi("api-1");
        verify(permissionService).deleteFunction("function-1");
    }

    @Test
    void shouldExposeCatalogAndMenuBindingSyncEndpoints() throws Exception {
        PermissionService permissionService = mock(PermissionService.class);
        ApiCatalogService apiCatalogService = mock(ApiCatalogService.class);
        PermissionController controller = new PermissionController(permissionService, apiCatalogService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        ApiCatalogSyncResultVo catalog = new ApiCatalogSyncResultVo();
        catalog.setScanned(12);
        catalog.setCreated(10);
        MenuApiBindingSyncResultVo bindings = new MenuApiBindingSyncResultVo();
        bindings.setFunctionCount(2);
        bindings.setBindingCount(3);
        when(apiCatalogService.syncCatalog()).thenReturn(catalog);
        when(apiCatalogService.syncMenuBindings(any())).thenReturn(bindings);

        mockMvc.perform(post("/api/permissions/apis/catalog/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.created").value(10));
        mockMvc.perform(put("/api/permissions/functions/api-bindings/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bindings\":[{\"functionCode\":\"system.user\",\"apis\":["
                                + "{\"httpMethod\":\"GET\",\"pathPattern\":\"/api/users\"}]}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bindingCount").value(3));
    }
}
