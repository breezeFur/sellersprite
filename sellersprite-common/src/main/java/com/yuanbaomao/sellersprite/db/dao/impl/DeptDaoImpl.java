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
    public List<Dept> listAll() {
        return lambdaQuery()
                .orderByAsc(Dept::getSortOrder)
                .orderByAsc(Dept::getDeptId)
                .list();
    }

    @Override
    public List<Dept> listDescendantsByPathPrefix(String pathPrefix, String excludedDeptId) {
        return lambdaQuery()
                .likeRight(Dept::getDeptPath, pathPrefix)
                .ne(Dept::getDeptId, excludedDeptId)
                .orderByAsc(Dept::getDeptPath)
                .list();
    }

    @Override
    public boolean existsByDeptCode(String deptCode) {
        return lambdaQuery().eq(Dept::getDeptCode, deptCode).exists();
    }

    @Override
    public boolean existsByDeptCodeExcludingDeptId(String deptCode, String deptId) {
        return lambdaQuery()
                .eq(Dept::getDeptCode, deptCode)
                .ne(Dept::getDeptId, deptId)
                .exists();
    }

    @Override
    public boolean existsByParentId(String parentId) {
        return lambdaQuery().eq(Dept::getParentId, parentId).exists();
    }
}
