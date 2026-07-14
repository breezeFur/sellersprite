package com.yuanbaomao.sellersprite.system.ops.operation.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.log.mybatis.entity.OperationLogEntity;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.OperationLogQueryDao;
import com.yuanbaomao.sellersprite.system.ops.operation.model.dto.OperationLogPageRequest;
import com.yuanbaomao.sellersprite.system.ops.operation.model.vo.OperationLogVo;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OperationLogServiceImplTest {

    @Mock
    private OperationLogQueryDao operationLogQueryDao;

    @InjectMocks
    private OperationLogServiceImpl operationLogService;

    @Test
    void shouldPageByOperatorModuleActionResultTrackAndTime() {
        OperationLogPageRequest request = new OperationLogPageRequest();
        request.setUserId("user-1");
        request.setUsername("yuanbao");
        request.setModuleName("系统管理");
        request.setOperationType("UPDATE");
        request.setSuccess(0);
        request.setTrackId("track-1");
        request.setStartTime(100L);
        request.setEndTime(200L);
        request.setCurrent(2L);
        request.setSize(10L);
        Page<OperationLogEntity> page = Page.of(2, 10, 1);
        page.setRecords(List.of(operationLog()));
        when(operationLogQueryDao.page("user-1", "yuanbao", "系统管理", "UPDATE", 0,
                "track-1", 100L, 200L, 2L, 10L)).thenReturn(page);

        com.yuanbaomao.base.result.PageResult<OperationLogVo> result = operationLogService.page(request);

        assertThat(result.getRecords()).extracting(OperationLogVo::getOperationLogId)
                .containsExactly("operation-1");
        verify(operationLogQueryDao).page("user-1", "yuanbao", "系统管理", "UPDATE", 0,
                "track-1", 100L, 200L, 2L, 10L);
    }

    @Test
    void shouldReturnDetailOrNotFound() {
        when(operationLogQueryDao.findById("operation-1")).thenReturn(Optional.of(operationLog()));
        when(operationLogQueryDao.findById("missing")).thenReturn(Optional.empty());

        OperationLogVo detail = operationLogService.detail("operation-1");
        assertThat(detail.getCostMs()).isEqualTo(12L);
        assertThat(detail.getRequestParams()).contains("[REDACTED]").doesNotContain("secret");
        assertThat(detail.getResponsePayload()).contains("[REDACTED]").doesNotContain("secret");
        assertThatThrownBy(() -> operationLogService.detail("missing"))
                .isInstanceOfSatisfying(BizException.class,
                        exception -> assertThat(exception.getCode())
                                .isEqualTo(ResultCode.RESOURCE_NOT_FOUND.getCode()));
    }

    private OperationLogEntity operationLog() {
        OperationLogEntity log = new OperationLogEntity();
        log.setOperationLogId("operation-1");
        log.setUserId("user-1");
        log.setUsername("yuanbao");
        log.setModuleName("系统管理");
        log.setOperationName("更新用户");
        log.setOperationType("UPDATE");
        log.setHttpMethod("PUT");
        log.setRequestUri("/api/users/user-1");
        log.setRequestParams("{\"password\":\"secret\"}");
        log.setResponsePayload(" token=secret ");
        log.setSuccess(0);
        log.setCostMs(12L);
        log.setTrackId("track-1");
        log.setCreatedAt(150L);
        return log;
    }
}
