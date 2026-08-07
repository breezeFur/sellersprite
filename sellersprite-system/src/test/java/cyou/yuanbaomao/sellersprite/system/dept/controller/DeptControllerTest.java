package cyou.yuanbaomao.sellersprite.system.dept.controller;

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

import cyou.yuanbaomao.sellersprite.system.dept.model.dto.DeptUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dept.model.vo.DeptVo;
import cyou.yuanbaomao.sellersprite.system.dept.service.DeptService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DeptControllerTest {

    @Test
    void shouldExposeDepartmentTreeAndLifecycleEndpoints() throws Exception {
        DeptService deptService = mock(DeptService.class);
        DeptController controller = new DeptController(deptService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        DeptVo department = new DeptVo();
        department.setDeptId("dept-1");
        department.setDeptName("研发部");
        when(deptService.tree()).thenReturn(List.of(department));
        when(deptService.detail("dept-1")).thenReturn(department);
        when(deptService.update(eq("dept-1"), any(DeptUpdateRequest.class))).thenReturn(department);

        mockMvc.perform(get("/api/depts/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].deptId").value("dept-1"));
        mockMvc.perform(get("/api/depts/dept-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deptName").value("研发部"));
        mockMvc.perform(put("/api/depts/dept-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentId\":\"0\",\"deptCode\":\"rd\",\"deptName\":\"研发部\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/depts/dept-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/depts/dept-1"))
                .andExpect(status().isOk());

        verify(deptService).updateStatus("dept-1", 0);
        verify(deptService).delete("dept-1");
    }
}
