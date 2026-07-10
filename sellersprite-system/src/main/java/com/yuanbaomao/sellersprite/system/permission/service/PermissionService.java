package com.yuanbaomao.sellersprite.system.permission.service;

import com.yuanbaomao.sellersprite.system.permission.model.dto.SysApiCreateRequest;
import com.yuanbaomao.sellersprite.system.permission.model.dto.SysFunctionCreateRequest;
import com.yuanbaomao.sellersprite.system.permission.model.vo.SysApiVo;
import com.yuanbaomao.sellersprite.system.permission.model.vo.SysFunctionVo;
import java.util.List;

public interface PermissionService {

    SysFunctionVo createFunction(SysFunctionCreateRequest request);

    SysApiVo createApi(SysApiCreateRequest request);

    List<SysFunctionVo> listFunctions();

    List<SysApiVo> listPublicApis();
}
