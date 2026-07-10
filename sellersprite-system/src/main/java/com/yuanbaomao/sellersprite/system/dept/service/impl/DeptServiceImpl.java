package com.yuanbaomao.sellersprite.system.dept.service.impl;

import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.DeptDao;
import com.yuanbaomao.sellersprite.db.entity.Dept;
import com.yuanbaomao.base.constants.SystemConstants;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import com.yuanbaomao.sellersprite.system.convert.SystemConverter;
import com.yuanbaomao.sellersprite.system.dept.model.dto.DeptCreateRequest;
import com.yuanbaomao.sellersprite.system.dept.model.vo.DeptVo;
import com.yuanbaomao.sellersprite.system.dept.service.DeptService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptDao deptDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptVo create(DeptCreateRequest request) {
        if (deptDao.existsByDeptCode(request.getDeptCode())) {
            throw new BizException(ResultCode.DEPT_CODE_ALREADY_EXISTS);
        }
        Dept entity = new Dept();
        entity.setParentId(request.getParentId());
        entity.setDeptCode(request.getDeptCode());
        entity.setDeptName(request.getDeptName());
        entity.setDeptPath(buildDeptPath(request.getParentId()));
        entity.setLeaderUserId(request.getLeaderUserId());
        entity.setPhone("");
        entity.setEmail("");
        entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        entity.setStatus(SystemBusinessConstants.STATUS_ENABLED);
        entity.setRemark(defaultString(request.getRemark()));
        deptDao.save(entity);
        entity.setDeptPath(buildDeptPathWithSelf(request.getParentId(), entity.getDeptId()));
        deptDao.updateById(entity);
        return SystemConverter.toDeptVo(entity);
    }

    @Override
    public List<DeptVo> listByParentId(String parentId) {
        return deptDao.listByParentId(parentId).stream().map(SystemConverter::toDeptVo).toList();
    }

    private String buildDeptPath(String parentId) {
        if (SystemConstants.ROOT_PARENT_ID.equals(parentId)) {
            return "/";
        }
        Dept parent = deptDao.getById(parentId);
        if (parent == null) {
            throw new BizException(ResultCode.DEPT_NOT_FOUND, "父部门不存在");
        }
        return parent.getDeptPath();
    }

    private String buildDeptPathWithSelf(String parentId, String deptId) {
        String prefix = buildDeptPath(parentId);
        return prefix.endsWith("/") ? prefix + deptId + "/" : prefix + "/" + deptId + "/";
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
