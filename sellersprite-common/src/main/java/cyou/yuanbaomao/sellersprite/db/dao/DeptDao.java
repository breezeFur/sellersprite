package cyou.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import cyou.yuanbaomao.sellersprite.db.entity.Dept;
import java.util.List;

public interface DeptDao extends IService<Dept> {

    List<Dept> listByParentId(String parentId);

    List<Dept> listAll();

    List<Dept> listDescendantsByPathPrefix(String pathPrefix, String excludedDeptId);

    boolean existsByDeptCode(String deptCode);

    boolean existsByDeptCodeExcludingDeptId(String deptCode, String deptId);

    boolean existsByParentId(String parentId);
}
