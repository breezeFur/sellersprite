package cyou.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cyou.yuanbaomao.sellersprite.db.dao.RoleFunctionDao;
import cyou.yuanbaomao.sellersprite.db.entity.RoleFunction;
import cyou.yuanbaomao.sellersprite.db.mapper.RoleFunctionMapper;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class RoleFunctionDaoImpl extends ServiceImpl<RoleFunctionMapper, RoleFunction>
        implements RoleFunctionDao {

    @Override
    public List<RoleFunction> listByRoleIds(Collection<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(RoleFunction::getRoleId, roleIds).list();
    }

    @Override
    public List<RoleFunction> listByFunctionIds(Collection<String> functionIds) {
        if (functionIds == null || functionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(RoleFunction::getSysFunctionId, functionIds).list();
    }

    @Override
    public boolean existsByFunctionId(String functionId) {
        return lambdaQuery().eq(RoleFunction::getSysFunctionId, functionId).exists();
    }

    @Override
    public void replaceByRoleId(String roleId, Collection<String> functionIds) {
        baseMapper.deletePhysicallyByRoleId(roleId);
        if (functionIds == null || functionIds.isEmpty()) {
            return;
        }
        List<RoleFunction> bindings = functionIds.stream().map(functionId -> binding(roleId, functionId)).toList();
        saveBatch(bindings);
    }

    private RoleFunction binding(String roleId, String functionId) {
        RoleFunction binding = new RoleFunction();
        binding.setRoleId(roleId);
        binding.setSysFunctionId(functionId);
        binding.setRemark("");
        return binding;
    }
}
