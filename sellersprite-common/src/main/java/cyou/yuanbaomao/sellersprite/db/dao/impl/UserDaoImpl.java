package cyou.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.sellersprite.db.dao.UserDao;
import cyou.yuanbaomao.sellersprite.db.entity.User;
import cyou.yuanbaomao.sellersprite.db.mapper.UserMapper;
import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl extends ServiceImpl<UserMapper, User> implements UserDao {

    private static final String INCREMENT_PERMISSION_VERSION_SQL =
            "permission_version = permission_version + 1";

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(lambdaQuery().eq(User::getUsername, username).one());
    }

    @Override
    public boolean existsByUsername(String username) {
        return lambdaQuery().eq(User::getUsername, username).exists();
    }

    @Override
    public boolean existsByUsernameExcludingUserId(String username, String userId) {
        return lambdaQuery()
                .eq(User::getUsername, username)
                .ne(User::getUserId, userId)
                .exists();
    }

    @Override
    public boolean incrementPermissionVersion(Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }
        return lambdaUpdate()
                .in(User::getUserId, userIds)
                .setSql(INCREMENT_PERMISSION_VERSION_SQL)
                .update();
    }

    @Override
    public Page<User> pageUsers(String username, Integer status, long current, long size) {
        return lambdaQuery()
                .like(username != null && !username.isBlank(), User::getUsername, username)
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreatedAt)
                .page(Page.of(current, size));
    }

    @Override
    public Page<User> pageByRoleId(String roleId, String username, long current, long size) {
        return baseMapper.selectPageByRoleId(Page.of(current, size), roleId, username);
    }

    @Override
    public boolean existsByPrimaryDeptId(String deptId) {
        return lambdaQuery().eq(User::getPrimaryDeptId, deptId).exists();
    }
}
