package cyou.yuanbaomao.sellersprite.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cyou.yuanbaomao.sellersprite.db.entity.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("""
            SELECT u.*
            FROM `user` u
            INNER JOIN user_role ur ON ur.user_id = u.user_id AND ur.deleted = 0
            WHERE ur.role_id = #{roleId}
              AND u.deleted = 0
              AND (#{username} IS NULL OR #{username} = ''
                   OR u.username LIKE CONCAT('%', #{username}, '%'))
            ORDER BY u.created_at DESC, u.user_id ASC
            """)
    Page<User> selectPageByRoleId(Page<User> page, @Param("roleId") String roleId,
            @Param("username") String username);
}
