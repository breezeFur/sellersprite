package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.DeptDao;
import com.yuanbaomao.sellersprite.db.entity.Dept;
import com.yuanbaomao.sellersprite.db.mapper.DeptMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class DeptDaoImpl extends ServiceImpl<DeptMapper, Dept> implements DeptDao {

    @Override
    public List<Dept> listByParentId(String parentId) {
        return lambdaQuery()
                .eq(Dept::getParentId, parentId)
                .orderByAsc(Dept::getSortOrder)
                .list();
    }

    @Override
    public boolean existsByDeptCode(String deptCode) {
        return lambdaQuery().eq(Dept::getDeptCode, deptCode).exists();
    }
}
