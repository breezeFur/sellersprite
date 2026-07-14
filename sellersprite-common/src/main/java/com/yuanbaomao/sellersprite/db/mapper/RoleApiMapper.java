package com.yuanbaomao.sellersprite.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuanbaomao.sellersprite.db.entity.RoleApi;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface RoleApiMapper extends BaseMapper<RoleApi> {

    @Delete("DELETE FROM role_api WHERE role_id = #{roleId}")
    int deletePhysicallyByRoleId(@Param("roleId") String roleId);
}
