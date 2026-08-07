package cyou.yuanbaomao.sellersprite.system.permission.service;

import cyou.yuanbaomao.base.result.PageResult;
import cyou.yuanbaomao.sellersprite.system.permission.model.dto.SysApiCreateRequest;
import cyou.yuanbaomao.sellersprite.system.permission.model.dto.SysApiPageRequest;
import cyou.yuanbaomao.sellersprite.system.permission.model.dto.SysApiUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.permission.model.dto.SysFunctionCreateRequest;
import cyou.yuanbaomao.sellersprite.system.permission.model.dto.SysFunctionUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.permission.model.vo.SysApiVo;
import cyou.yuanbaomao.sellersprite.system.permission.model.vo.SysFunctionVo;
import java.util.List;
import java.util.Collection;

public interface PermissionService {

    SysFunctionVo createFunction(SysFunctionCreateRequest request);

    SysApiVo createApi(SysApiCreateRequest request);

    List<SysFunctionVo> listFunctions();

    List<SysFunctionVo> functionTree();

    SysFunctionVo functionDetail(String functionId);

    SysFunctionVo updateFunction(String functionId, SysFunctionUpdateRequest request);

    void updateFunctionStatus(String functionId, Integer status);

    List<String> getFunctionApiIds(String functionId);

    List<SysApiVo> listPublicApis();

    PageResult<SysApiVo> pageApis(SysApiPageRequest request);

    SysApiVo apiDetail(String apiId);

    SysApiVo updateApi(String apiId, SysApiUpdateRequest request);

    void updateApiStatus(String apiId, Integer status);

    void replaceFunctionApis(String functionId, Collection<String> apiIds);

    void deleteFunction(String functionId);

    void deleteApi(String apiId);
}
