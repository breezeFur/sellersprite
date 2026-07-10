package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.Dept;
import java.util.List;

public interface DeptDao extends IService<Dept> {

    List<Dept> listByParentId(String parentId);

    boolean existsByDeptCode(String deptCode);
}
