package cyou.yuanbaomao.sellersprite.db.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.log.mybatis.entity.OperationLogEntity;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface OperationLogQueryMapper {

    @Select("SELECT COUNT(*) FROM operation_log WHERE success = 0 AND created_at >= #{startTime} AND created_at < #{endTime}")
    long countFailedByCreatedAtRange(@Param("startTime") long startTime, @Param("endTime") long endTime);

    @Select("""
            SELECT operation_log_id, user_id, username, module_name, operation_name, operation_type,
                   http_method, request_uri, request_params, response_payload, response_status,
                   success, error_message, client_ip, user_agent, cost_ms, trace_id, created_at
            FROM operation_log
            ORDER BY created_at DESC
            LIMIT #{limit}
            """)
    List<OperationLogEntity> listRecent(@Param("limit") int limit);

    @Select("""
            <script>
            SELECT operation_log_id, user_id, username, module_name, operation_name, operation_type,
                   http_method, request_uri, request_params, response_payload, response_status,
                   success, error_message, client_ip, user_agent, cost_ms, trace_id, created_at
            FROM operation_log
            WHERE 1 = 1
            <if test="userId != null and userId != ''">AND user_id = #{userId}</if>
            <if test="username != null and username != ''">
                AND username LIKE CONCAT('%', #{username}, '%')
            </if>
            <if test="moduleName != null and moduleName != ''">
                AND module_name LIKE CONCAT('%', #{moduleName}, '%')
            </if>
            <if test="operationType != null and operationType != ''">
                AND operation_type = #{operationType}
            </if>
            <if test="success != null">AND success = #{success}</if>
            <if test="traceId != null and traceId != ''">AND trace_id = #{traceId}</if>
            <if test="startTime != null">AND created_at &gt;= #{startTime}</if>
            <if test="endTime != null">AND created_at &lt;= #{endTime}</if>
            ORDER BY created_at DESC
            </script>
            """)
    Page<OperationLogEntity> page(Page<OperationLogEntity> page,
                                  @Param("userId") String userId,
                                  @Param("username") String username,
                                  @Param("moduleName") String moduleName,
                                  @Param("operationType") String operationType,
                                  @Param("success") Integer success,
                                  @Param("traceId") String traceId,
                                  @Param("startTime") Long startTime,
                                  @Param("endTime") Long endTime);

    @Select("""
            SELECT operation_log_id, user_id, username, module_name, operation_name, operation_type,
                   http_method, request_uri, request_params, response_payload, response_status,
                   success, error_message, client_ip, user_agent, cost_ms, trace_id, created_at
            FROM operation_log
            WHERE operation_log_id = #{operationLogId}
            """)
    OperationLogEntity findById(@Param("operationLogId") String operationLogId);
}
