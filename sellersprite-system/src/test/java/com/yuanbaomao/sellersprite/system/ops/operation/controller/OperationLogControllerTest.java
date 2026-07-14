package com.yuanbaomao.sellersprite.system.ops.operation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.system.ops.operation.model.dto.OperationLogPageRequest;
import com.yuanbaomao.sellersprite.system.ops.operation.model.vo.OperationLogVo;
import com.yuanbaomao.sellersprite.system.ops.operation.service.OperationLogService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class OperationLogControllerTest {

    @Test
    void shouldExposeOperationLogPageAndDetail() throws Exception {
        OperationLogService service = mock(OperationLogService.class);
        OperationLogVo log = new OperationLogVo();
        log.setOperationLogId("operation-1");
        log.setOperationName("更新用户");
        when(service.page(any(OperationLogPageRequest.class)))
                .thenReturn(PageResult.of(1, 20, 1, List.of(log)));
        when(service.detail("operation-1")).thenReturn(log);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new OperationLogController(service)).build();

        mockMvc.perform(get("/api/logs/operation").param("operationType", "UPDATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].operationLogId").value("operation-1"));
        mockMvc.perform(get("/api/logs/operation/operation-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.operationName").value("更新用户"));
    }
}
