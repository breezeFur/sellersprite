package cyou.yuanbaomao.sellersprite.system.dept.service.impl;

import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.DeptDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserRoleDao;
import cyou.yuanbaomao.sellersprite.db.entity.Dept;
import cyou.yuanbaomao.base.constants.SystemConstants;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import cyou.yuanbaomao.sellersprite.system.convert.SystemConverter;
import cyou.yuanbaomao.sellersprite.system.dept.model.dto.DeptCreateRequest;
import cyou.yuanbaomao.sellersprite.system.dept.model.dto.DeptUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dept.model.vo.DeptVo;
import cyou.yuanbaomao.sellersprite.system.dept.service.DeptService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptDao deptDao;
    private final UserDao userDao;
    private final UserRoleDao userRoleDao;

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

    @Override
    public List<DeptVo> tree() {
        Map<String, DeptVo> nodes = new LinkedHashMap<>();
        for (Dept department : deptDao.listAll()) {
            DeptVo node = SystemConverter.toDeptVo(department);
            nodes.put(node.getDeptId(), node);
        }
        for (DeptVo node : nodes.values()) {
            DeptVo parent = nodes.get(node.getParentId());
            if (parent != null) {
                parent.getChildren().add(node);
            }
        }
        return nodes.values().stream()
                .filter(node -> !nodes.containsKey(node.getParentId()))
                .toList();
    }

    @Override
    public DeptVo detail(String deptId) {
        return SystemConverter.toDeptVo(requireDepartment(deptId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeptVo update(String deptId, DeptUpdateRequest request) {
        Dept department = requireDepartment(deptId);
        if (deptDao.existsByDeptCodeExcludingDeptId(request.getDeptCode(), deptId)) {
            throw new BizException(ResultCode.DEPT_CODE_ALREADY_EXISTS);
        }
        String newPath = resolveUpdatedPath(department, request.getParentId());
        String oldPath = department.getDeptPath();
        department.setParentId(request.getParentId());
        department.setDeptCode(request.getDeptCode());
        department.setDeptName(request.getDeptName());
        department.setDeptPath(newPath);
        department.setLeaderUserId(request.getLeaderUserId());
        department.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        department.setRemark(defaultString(request.getRemark()));
        deptDao.updateById(department);
        rewriteDescendantPaths(deptId, oldPath, newPath);
        return SystemConverter.toDeptVo(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String deptId, Integer status) {
        if (!Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(status)
                && !Integer.valueOf(SystemBusinessConstants.STATUS_DISABLED).equals(status)) {
            throw new BizException(ResultCode.PARAM_INVALID);
        }
        Dept department = requireDepartment(deptId);
        department.setStatus(status);
        deptDao.updateById(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String deptId) {
        requireDepartment(deptId);
        if (deptDao.existsByParentId(deptId)) {
            throw new BizException(ResultCode.RESOURCE_CONFLICT, "部门仍有子部门，无法删除");
        }
        if (userDao.existsByPrimaryDeptId(deptId)) {
            throw new BizException(ResultCode.RESOURCE_CONFLICT, "部门仍被用户引用，无法删除");
        }
        if (userRoleDao.existsByDeptId(deptId)) {
            throw new BizException(ResultCode.RESOURCE_CONFLICT, "部门仍被用户角色引用，无法删除");
        }
        deptDao.removeById(deptId);
    }

    private String resolveUpdatedPath(Dept department, String parentId) {
        if (department.getDeptId().equals(parentId)) {
            throw new BizException(ResultCode.RESOURCE_CONFLICT, "部门父子关系形成循环，无法保存");
        }
        if (SystemConstants.ROOT_PARENT_ID.equals(parentId)) {
            return "/" + department.getDeptId() + "/";
        }
        Dept parent = requireDepartment(parentId);
        String selfPathSegment = "/" + department.getDeptId() + "/";
        if (parent.getDeptPath() != null && parent.getDeptPath().contains(selfPathSegment)) {
            throw new BizException(ResultCode.RESOURCE_CONFLICT, "部门父子关系形成循环，无法保存");
        }
        return appendPath(parent.getDeptPath(), department.getDeptId());
    }

    private void rewriteDescendantPaths(String deptId, String oldPath, String newPath) {
        if (Objects.equals(oldPath, newPath)) {
            return;
        }
        List<Dept> descendants = deptDao.listDescendantsByPathPrefix(oldPath, deptId);
        for (Dept descendant : descendants) {
            descendant.setDeptPath(newPath + descendant.getDeptPath().substring(oldPath.length()));
        }
        if (!descendants.isEmpty()) {
            deptDao.updateBatchById(descendants);
        }
    }

    private Dept requireDepartment(String deptId) {
        Dept department = deptDao.getById(deptId);
        if (department == null) {
            throw new BizException(ResultCode.DEPT_NOT_FOUND);
        }
        return department;
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
        return appendPath(prefix, deptId);
    }

    private String appendPath(String prefix, String deptId) {
        return prefix.endsWith("/") ? prefix + deptId + "/" : prefix + "/" + deptId + "/";
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
