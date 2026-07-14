package com.yuanbaomao.sellersprite.system.dept.service;

import com.yuanbaomao.sellersprite.system.dept.model.dto.DeptCreateRequest;
import com.yuanbaomao.sellersprite.system.dept.model.dto.DeptUpdateRequest;
import com.yuanbaomao.sellersprite.system.dept.model.vo.DeptVo;
import java.util.List;

public interface DeptService {

    DeptVo create(DeptCreateRequest request);

    List<DeptVo> listByParentId(String parentId);

    List<DeptVo> tree();

    DeptVo detail(String deptId);

    DeptVo update(String deptId, DeptUpdateRequest request);

    void updateStatus(String deptId, Integer status);

    void delete(String deptId);
}
