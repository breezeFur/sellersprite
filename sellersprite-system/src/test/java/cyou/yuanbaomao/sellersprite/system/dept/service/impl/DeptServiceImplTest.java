package cyou.yuanbaomao.sellersprite.system.dept.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.DeptDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserDao;
import cyou.yuanbaomao.sellersprite.db.dao.UserRoleDao;
import cyou.yuanbaomao.sellersprite.db.entity.Dept;
import cyou.yuanbaomao.sellersprite.system.dept.model.dto.DeptUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dept.model.vo.DeptVo;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeptServiceImplTest {

    @Mock
    private DeptDao deptDao;
    @Mock
    private UserDao userDao;
    @Mock
    private UserRoleDao userRoleDao;

    private DeptServiceImpl deptService;

    @BeforeEach
    void setUp() {
        deptService = new DeptServiceImpl(deptDao, userDao, userRoleDao);
    }

    @Test
    void shouldRejectMovingDepartmentBelowItsDescendant() {
        Dept department = dept("dept-a", "0", "/dept-a/", 1);
        Dept descendant = dept("dept-child", "dept-a", "/dept-a/dept-child/", 1);
        when(deptDao.getById("dept-a")).thenReturn(department);
        when(deptDao.getById("dept-child")).thenReturn(descendant);

        assertThatThrownBy(() -> deptService.update("dept-a", updateRequest("dept-child")))
                .isInstanceOfSatisfying(BizException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ResultCode.RESOURCE_CONFLICT);
                    assertThat(exception.getMessage()).contains("循环");
                });
        verify(deptDao, never()).updateById(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldRewriteDescendantPathsWhenMovingDepartment() {
        Dept department = dept("dept-a", "0", "/dept-a/", 1);
        Dept newParent = dept("dept-b", "0", "/dept-b/", 1);
        Dept child = dept("dept-child", "dept-a", "/dept-a/dept-child/", 2);
        when(deptDao.getById("dept-a")).thenReturn(department);
        when(deptDao.getById("dept-b")).thenReturn(newParent);
        when(deptDao.listDescendantsByPathPrefix("/dept-a/", "dept-a")).thenReturn(List.of(child));

        DeptVo result = deptService.update("dept-a", updateRequest("dept-b"));

        assertThat(result.getDeptPath()).isEqualTo("/dept-b/dept-a/");
        assertThat(child.getDeptPath()).isEqualTo("/dept-b/dept-a/dept-child/");
        verify(deptDao).updateBatchById(List.of(child));
    }

    @Test
    void shouldRejectDeletingDepartmentReferencedByUser() {
        when(deptDao.getById("dept-a")).thenReturn(dept("dept-a", "0", "/dept-a/", 1));
        when(userDao.existsByPrimaryDeptId("dept-a")).thenReturn(true);

        assertThatThrownBy(() -> deptService.delete("dept-a"))
                .isInstanceOfSatisfying(BizException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ResultCode.RESOURCE_CONFLICT);
                    assertThat(exception.getMessage()).contains("用户");
                });
        verify(deptDao, never()).removeById(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldRejectDeletingDepartmentWithChildren() {
        when(deptDao.getById("dept-a")).thenReturn(dept("dept-a", "0", "/dept-a/", 1));
        when(deptDao.existsByParentId("dept-a")).thenReturn(true);

        assertThatThrownBy(() -> deptService.delete("dept-a"))
                .isInstanceOfSatisfying(BizException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ResultCode.RESOURCE_CONFLICT);
                    assertThat(exception.getMessage()).contains("子部门");
                });
        verify(deptDao, never()).removeById(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldRejectDeletingDepartmentReferencedByUserRole() {
        when(deptDao.getById("dept-a")).thenReturn(dept("dept-a", "0", "/dept-a/", 1));
        when(userRoleDao.existsByDeptId("dept-a")).thenReturn(true);

        assertThatThrownBy(() -> deptService.delete("dept-a"))
                .isInstanceOfSatisfying(BizException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ResultCode.RESOURCE_CONFLICT);
                    assertThat(exception.getMessage()).contains("用户角色");
                });
        verify(deptDao, never()).removeById(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void shouldBuildStableDepartmentTree() {
        when(deptDao.listAll()).thenReturn(List.of(
                dept("dept-root", "0", "/dept-root/", 1),
                dept("dept-child", "dept-root", "/dept-root/dept-child/", 2)));

        List<DeptVo> tree = deptService.tree();

        assertThat(tree).hasSize(1);
        assertThat(tree.getFirst().getDeptId()).isEqualTo("dept-root");
        assertThat(tree.getFirst().getChildren()).extracting("deptId").containsExactly("dept-child");
    }

    private DeptUpdateRequest updateRequest(String parentId) {
        DeptUpdateRequest request = new DeptUpdateRequest();
        request.setParentId(parentId);
        request.setDeptCode("dept-a");
        request.setDeptName("部门A");
        request.setSortOrder(1);
        return request;
    }

    private Dept dept(String deptId, String parentId, String path, int sortOrder) {
        Dept dept = new Dept();
        dept.setDeptId(deptId);
        dept.setParentId(parentId);
        dept.setDeptCode(deptId);
        dept.setDeptName(deptId);
        dept.setDeptPath(path);
        dept.setSortOrder(sortOrder);
        dept.setStatus(1);
        return dept;
    }
}
