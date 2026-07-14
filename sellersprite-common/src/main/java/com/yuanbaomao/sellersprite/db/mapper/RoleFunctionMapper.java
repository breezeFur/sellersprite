package com.yuanbaomao.sellersprite.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuanbaomao.sellersprite.db.entity.RoleFunction;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface RoleFunctionMapper extends BaseMapper<RoleFunction> {

    @Delete("DELETE FROM role_function WHERE role_id = #{roleId}")
    int deletePhysicallyByRoleId(@Param("roleId") String roleId);
}
