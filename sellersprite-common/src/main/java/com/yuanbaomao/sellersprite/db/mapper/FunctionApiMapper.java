package com.yuanbaomao.sellersprite.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuanbaomao.sellersprite.db.entity.FunctionApi;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface FunctionApiMapper extends BaseMapper<FunctionApi> {

    @Delete("DELETE FROM function_api WHERE sys_function_id = #{functionId}")
    int deletePhysicallyByFunctionId(@Param("functionId") String functionId);
}
