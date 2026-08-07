package cyou.yuanbaomao.sellersprite.system.dashboard.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cyou.yuanbaomao.sellersprite.system.dashboard.model.vo.DashboardOverviewVo;
import cyou.yuanbaomao.sellersprite.system.dashboard.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DashboardControllerTest {

    @Test
    void shouldExposeDashboardOverview() throws Exception {
        DashboardService service = mock(DashboardService.class);
        DashboardOverviewVo overview = new DashboardOverviewVo();
        overview.setUserCount(12L);
        when(service.overview()).thenReturn(overview);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(service)).build();

        mockMvc.perform(get("/api/dashboard/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userCount").value(12));
    }
}
