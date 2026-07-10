USE `sellersprite_service`;

ALTER TABLE `user`
  ADD COLUMN `permission_version` bigint unsigned NOT NULL DEFAULT 0 COMMENT '权限版本，授权变更时递增' AFTER `password_updated_at`;

ALTER TABLE `user_token`
  ADD COLUMN `session_family_id` char(36) DEFAULT NULL COMMENT '刷新会话链ID，UUIDv7' AFTER `refresh_token_hash`,
  ADD COLUMN `replaced_by_token_id` char(36) DEFAULT NULL COMMENT '轮换后的令牌ID' AFTER `session_family_id`,
  ADD COLUMN `refresh_expires_at` bigint unsigned DEFAULT NULL COMMENT '刷新令牌过期时间，Unix毫秒' AFTER `expires_at`,
  ADD COLUMN `last_used_at` bigint unsigned DEFAULT NULL COMMENT '最近刷新时间，Unix毫秒' AFTER `refresh_expires_at`,
  ADD KEY `idx_user_token_family_id` (`session_family_id`),
  ADD KEY `idx_user_token_replaced_by` (`replaced_by_token_id`),
  ADD KEY `idx_user_token_refresh_expires_at` (`refresh_expires_at`);

UPDATE `user_token`
SET `session_family_id` = `user_token_id`,
    `refresh_expires_at` = `expires_at`
WHERE `session_family_id` IS NULL OR `refresh_expires_at` IS NULL;

ALTER TABLE `user_token`
  MODIFY COLUMN `session_family_id` char(36) NOT NULL COMMENT '刷新会话链ID，UUIDv7',
  MODIFY COLUMN `refresh_expires_at` bigint unsigned NOT NULL COMMENT '刷新令牌过期时间，Unix毫秒';

CREATE TABLE `function_api` (
  `function_api_id` char(36) NOT NULL COMMENT '功能接口关联ID，UUIDv7',
  `sys_function_id` char(36) NOT NULL COMMENT '系统功能ID',
  `sys_api_id` char(36) NOT NULL COMMENT '系统接口ID',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`function_api_id`),
  UNIQUE KEY `uk_function_api` (`sys_function_id`, `sys_api_id`, `deleted`),
  KEY `idx_function_api_function_id` (`sys_function_id`),
  KEY `idx_function_api_api_id` (`sys_api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='功能接口关联表';

ALTER TABLE `login_log`
  ADD COLUMN `error_code` varchar(64) NOT NULL DEFAULT '' COMMENT '稳定错误码' AFTER `success`;

ALTER TABLE `operation_log`
  ADD COLUMN `resource_id` varchar(128) NOT NULL DEFAULT '' COMMENT '业务资源ID' AFTER `operation_type`,
  ADD COLUMN `error_code` varchar(64) NOT NULL DEFAULT '' COMMENT '稳定错误码' AFTER `success`,
  ADD KEY `idx_operation_log_operation_type` (`operation_type`);

ALTER TABLE `ai_conversation_message`
  ADD COLUMN `message_status` varchar(32) NOT NULL DEFAULT 'COMPLETED' COMMENT '消息状态：STREAMING COMPLETED CANCELLED FAILED' AFTER `metadata`,
  ADD COLUMN `error_code` varchar(64) NOT NULL DEFAULT '' COMMENT '稳定错误码' AFTER `message_status`,
  ADD COLUMN `error_message` varchar(512) NOT NULL DEFAULT '' COMMENT '安全错误摘要' AFTER `error_code`,
  ADD KEY `idx_ai_message_status` (`conversation_id`, `message_status`);

ALTER TABLE `ai_prompt_record`
  ADD COLUMN `prompt_summary` varchar(2048) NOT NULL DEFAULT '' COMMENT '截断脱敏后的Prompt摘要' AFTER `request_messages`,
  ADD COLUMN `prompt_truncated` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '摘要是否被截断：1是 0否' AFTER `prompt_summary`;
