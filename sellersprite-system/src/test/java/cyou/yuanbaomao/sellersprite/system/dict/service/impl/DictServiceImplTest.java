package cyou.yuanbaomao.sellersprite.system.dict.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.dict.core.DictTemplate;
import cyou.yuanbaomao.dict.mybatis.entity.DictDataEntity;
import cyou.yuanbaomao.dict.mybatis.entity.DictTypeEntity;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.system.dict.model.dto.DictItemCreateRequest;
import cyou.yuanbaomao.sellersprite.system.dict.model.vo.DictItemVo;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DictServiceImplTest {

    @Mock
    private DictTemplate dictTemplate;

    private DictServiceImpl dictService;

    @BeforeEach
    void setUp() {
        dictService = new DictServiceImpl(dictTemplate);
    }

    @Test
    void shouldDelegateDictionaryCreationToTemplateWithFourFieldContract() {
        when(dictTemplate.createData(any())).thenAnswer(invocation -> invocation.getArgument(0));
        DictItemCreateRequest request = new DictItemCreateRequest();
        request.setDictType("MARKET");
        request.setDictLabel("MARKET_US");
        request.setDictName("美国站");
        request.setDictValue("US");

        DictItemVo result = dictService.createItem(request);

        ArgumentCaptor<DictDataEntity> captor = ArgumentCaptor.forClass(DictDataEntity.class);
        verify(dictTemplate).createData(captor.capture());
        assertThat(captor.getValue())
                .extracting(DictDataEntity::getDictType, DictDataEntity::getDictLabel,
                        DictDataEntity::getDictName, DictDataEntity::getDictValue)
                .containsExactly("MARKET", "MARKET_US", "美国站", "US");
        assertThat(result.getDictLabel()).isEqualTo("MARKET_US");
    }

    @Test
    void shouldReadEnabledDataThroughTemplate() {
        DictTypeEntity type = new DictTypeEntity();
        type.setDictType("MARKET");
        type.setDictTypeName("市场");
        DictDataEntity data = new DictDataEntity();
        data.setDictType("MARKET");
        data.setDictLabel("MARKET_US");
        data.setDictName("美国站");
        data.setDictValue("US");
        when(dictTemplate.getType("MARKET")).thenReturn(type);
        when(dictTemplate.listEnabledData("MARKET")).thenReturn(List.of(data));

        assertThat(dictService.detailByCode("MARKET").getItems())
                .singleElement()
                .extracting(DictItemVo::getDictLabel, DictItemVo::getDictValue)
                .containsExactly("MARKET_US", "US");
    }

    @Test
    void shouldRejectDeletingBuiltinData() {
        DictDataEntity data = new DictDataEntity();
        data.setDictDataId("data-1");
        data.setSystemBuiltin(1);
        when(dictTemplate.getData("data-1")).thenReturn(data);

        assertThatThrownBy(() -> dictService.deleteItem("data-1"))
                .isInstanceOfSatisfying(BizException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ResultCode.RESOURCE_CONFLICT));
        verify(dictTemplate, never()).deleteData(any());
    }
}
