package cyou.yuanbaomao.sellersprite.system.dept.service;

import cyou.yuanbaomao.sellersprite.system.dept.model.dto.DeptCreateRequest;
import cyou.yuanbaomao.sellersprite.system.dept.model.dto.DeptUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.dept.model.vo.DeptVo;
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
