-- 0.2 链路标识命名迁移：必须在部署 0.2 应用前执行。
-- 该迁移仅重命名列和索引，不修改已有链路标识数据。
USE `sellersprite_service`;

ALTER TABLE login_log
    RENAME COLUMN track_id TO trace_id,
    RENAME INDEX idx_login_log_track_id TO idx_login_log_trace_id;

ALTER TABLE operation_log
    RENAME COLUMN track_id TO trace_id,
    RENAME INDEX idx_operation_log_track_id TO idx_operation_log_trace_id;

ALTER TABLE ai_prompt_record
    RENAME COLUMN track_id TO trace_id,
    RENAME INDEX idx_ai_prompt_track_id TO idx_ai_prompt_trace_id;
