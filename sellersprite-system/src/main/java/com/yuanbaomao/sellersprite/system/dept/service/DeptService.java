package com.yuanbaomao.sellersprite.system.dept.service;

import com.yuanbaomao.sellersprite.system.dept.model.dto.DeptCreateRequest;
import com.yuanbaomao.sellersprite.system.dept.model.vo.DeptVo;
import java.util.List;

public interface DeptService {

    DeptVo create(DeptCreateRequest request);

    List<DeptVo> listByParentId(String parentId);
}
