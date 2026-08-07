package cyou.yuanbaomao.sellersprite.system.dict.controller;

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

import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictItemPageRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictItemUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictTypePageRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictTypeUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.vo.DictItemVo;
import cyou.yuanbaomao.sellersprite.system.dict.model.vo.DictTypeVo;
import cyou.yuanbaomao.sellersprite.system.dict.service.DictService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DictControllerTest {

    @Test
    void shouldExposeDictionaryTypeAndItemLifecycleEndpoints() throws Exception {
        DictService dictService = mock(DictService.class);
        DictController controller = new DictController(dictService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        DictTypeVo type = new DictTypeVo();
        type.setDictType("MARKET");
        type.setDictName("用户状态");
        DictItemVo item = new DictItemVo();
        item.setDictDataId("item-1");
        item.setDictLabel("MARKET_US");
        when(dictService.pageTypes(any(DictTypePageRequest.class)))
                .thenReturn(PageResult.of(1, 20, 1, List.of(type)));
        when(dictService.detailType("type-1")).thenReturn(type);
        when(dictService.updateType(eq("type-1"), any(DictTypeUpdateRequest.class))).thenReturn(type);
        when(dictService.pageItems(eq("type-1"), any(DictItemPageRequest.class)))
                .thenReturn(PageResult.of(1, 20, 1, List.of(item)));
        when(dictService.detailItem("item-1")).thenReturn(item);
        when(dictService.updateItem(eq("item-1"), any(DictItemUpdateRequest.class))).thenReturn(item);

        mockMvc.perform(get("/api/system/dicts/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].dictType").value("MARKET"));
        mockMvc.perform(get("/api/system/dicts/types/type-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dictName").value("用户状态"));
        mockMvc.perform(put("/api/system/dicts/types/type-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dictName\":\"用户状态\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/system/dicts/types/type-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/system/dicts/types/type-1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].dictDataId").value("item-1"));
        mockMvc.perform(get("/api/system/dicts/items/item-1"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/system/dicts/items/item-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dictLabel\":\"MARKET_US\",\"dictName\":\"美国站\",\"dictValue\":\"US\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(put("/api/system/dicts/items/item-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/system/dicts/items/item-1"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/system/dicts/types/type-1"))
                .andExpect(status().isOk());

        verify(dictService).updateTypeStatus("type-1", 0);
        verify(dictService).updateItemStatus("item-1", 0);
        verify(dictService).deleteItem("item-1");
        verify(dictService).deleteType("type-1");
    }
}
