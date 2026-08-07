package cyou.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cyou.yuanbaomao.sellersprite.db.dao.UserRoleDao;
import cyou.yuanbaomao.sellersprite.db.entity.UserRole;
import cyou.yuanbaomao.sellersprite.db.mapper.UserRoleMapper;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRoleDaoImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleDao {

    @Override
    public List<UserRole> listByUserId(String userId) {
        return lambdaQuery()
                .eq(UserRole::getUserId, userId)
                .orderByDesc(UserRole::getPrimaryRole)
                .orderByAsc(UserRole::getCreatedAt)
                .orderByAsc(UserRole::getUserRoleId)
                .list();
    }

    @Override
    public List<UserRole> listByRoleIds(Collection<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(UserRole::getRoleId, roleIds).list();
    }

    @Override
    public boolean existsByRoleId(String roleId) {
        return lambdaQuery().eq(UserRole::getRoleId, roleId).exists();
    }

    @Override
    public boolean existsByDeptId(String deptId) {
        return lambdaQuery().eq(UserRole::getDeptId, deptId).exists();
    }

    @Override
    public Optional<UserRole> findByUserIdAndRoleId(String userId, String roleId) {
        return Optional.ofNullable(lambdaQuery()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getRoleId, roleId)
                .one());
    }

    @Override
    public void removeByUserIdAndRoleId(String userId, String roleId) {
        baseMapper.deletePhysicallyByUserIdAndRoleId(userId, roleId);
    }

    @Override
    public void clearPrimaryRoleByUserId(String userId) {
        lambdaUpdate()
                .eq(UserRole::getUserId, userId)
                .eq(UserRole::getPrimaryRole, 1)
                .set(UserRole::getPrimaryRole, 0)
                .update();
    }

    @Override
    public void replaceByUserId(String userId, String deptId, Collection<String> roleIds) {
        baseMapper.deletePhysicallyByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<UserRole> bindings = new ArrayList<>(roleIds.size());
        boolean primaryRole = true;
        for (String roleId : roleIds) {
            UserRole binding = new UserRole();
            binding.setUserId(userId);
            binding.setRoleId(roleId);
            binding.setDeptId(deptId);
            binding.setPrimaryRole(primaryRole ? 1 : 0);
            binding.setRemark("");
            bindings.add(binding);
            primaryRole = false;
        }
        saveBatch(bindings);
    }
}
