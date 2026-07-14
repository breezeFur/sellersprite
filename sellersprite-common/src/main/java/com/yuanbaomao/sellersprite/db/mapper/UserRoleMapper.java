package com.yuanbaomao.sellersprite.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuanbaomao.sellersprite.db.entity.UserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface UserRoleMapper extends BaseMapper<UserRole> {

    @Delete("DELETE FROM user_role WHERE user_id = #{userId}")
    int deletePhysicallyByUserId(@Param("userId") String userId);

    @Delete("DELETE FROM user_role WHERE user_id = #{userId} AND role_id = #{roleId}")
    int deletePhysicallyByUserIdAndRoleId(@Param("userId") String userId, @Param("roleId") String roleId);
}
