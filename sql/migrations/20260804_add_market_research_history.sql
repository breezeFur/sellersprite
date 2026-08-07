USE `sellersprite_service`;

-- 为用户历史报告分页补充按用户和创建时间倒序扫描的复合索引。
SET @research_history_job_table_exists = (
  SELECT COUNT(*) > 0
  FROM `information_schema`.`TABLES`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'market_research_job'
);
SET @research_history_job_index_missing = (
  SELECT COUNT(*) = 0
  FROM `information_schema`.`STATISTICS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'market_research_job'
    AND `INDEX_NAME` = 'idx_market_research_job_user_created'
);
SET @research_history_job_ddl = IF(
  @research_history_job_table_exists AND @research_history_job_index_missing,
  'ALTER TABLE `market_research_job` ADD KEY `idx_market_research_job_user_created` (`user_id`, `created_at`)',
  'SELECT 1'
);
PREPARE research_history_job_statement FROM @research_history_job_ddl;
EXECUTE research_history_job_statement;
DEALLOCATE PREPARE research_history_job_statement;

-- 注册独立历史报告菜单，并为初始化管理员授予菜单与已同步的只读/下载接口。
START TRANSACTION;

SET @research_history_now_ms = CAST(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000 AS UNSIGNED);
SET @research_history_admin_user_id = (
  SELECT `user_id`
  FROM `user`
  WHERE `username` = 'admin' AND `deleted` = 0
  ORDER BY `created_at`
  LIMIT 1
);

INSERT INTO `sys_function` (
  `sys_function_id`, `parent_id`, `function_code`, `function_name`, `function_type`,
  `route_path`, `component_path`, `permission_code`, `icon`, `visible`, `cacheable`,
  `external_link`, `sort_order`, `status`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
) VALUES (
  '019f4d77-0000-7000-8000-000000000014', '0', 'research.report-history', '我的全部历史报告', 'MENU',
  '/research/report-history', 'research/report-history', 'research:report-history:view', 'Tickets', 1, 0,
  NULL, 28, 1, @research_history_now_ms, @research_history_now_ms,
  COALESCE(@research_history_admin_user_id, ''), COALESCE(@research_history_admin_user_id, ''), 0,
  '系统初始化历史报告菜单'
)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `function_name` = VALUES(`function_name`),
  `function_type` = VALUES(`function_type`),
  `route_path` = VALUES(`route_path`),
  `component_path` = VALUES(`component_path`),
  `permission_code` = VALUES(`permission_code`),
  `icon` = VALUES(`icon`),
  `visible` = VALUES(`visible`),
  `cacheable` = VALUES(`cacheable`),
  `external_link` = VALUES(`external_link`),
  `sort_order` = VALUES(`sort_order`),
  `status` = VALUES(`status`),
  `updated_at` = @research_history_now_ms,
  `updated_by` = COALESCE(@research_history_admin_user_id, ''),
  `deleted` = 0,
  `remark` = VALUES(`remark`);

SET @research_history_function_id = (
  SELECT `sys_function_id`
  FROM `sys_function`
  WHERE `function_code` = 'research.report-history' AND `deleted` = 0
  ORDER BY `created_at`
  LIMIT 1
);

INSERT INTO `role_function` (
  `role_function_id`, `role_id`, `sys_function_id`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
)
SELECT
  '019f4d77-0000-7000-8000-000000000114', `role`.`role_id`, @research_history_function_id,
  @research_history_now_ms, @research_history_now_ms,
  COALESCE(@research_history_admin_user_id, ''), COALESCE(@research_history_admin_user_id, ''), 0,
  '系统初始化管理员历史报告授权'
FROM `role` AS `role`
WHERE `role`.`role_code` = 'admin'
  AND `role`.`status` = 1
  AND `role`.`deleted` = 0
  AND @research_history_function_id IS NOT NULL
ON DUPLICATE KEY UPDATE
  `role_id` = VALUES(`role_id`),
  `sys_function_id` = VALUES(`sys_function_id`),
  `updated_at` = @research_history_now_ms,
  `updated_by` = COALESCE(@research_history_admin_user_id, ''),
  `deleted` = 0,
  `remark` = VALUES(`remark`);

-- 接口目录通常在应用启动后同步；目录尚无目标接口时本语句写入 0 行且不会失败。
-- 完成接口目录同步后可安全重跑本迁移，补齐历史报告分页与文件下载接口绑定。
INSERT INTO `function_api` (
  `function_api_id`, `sys_function_id`, `sys_api_id`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
)
SELECT
  CONCAT('019f4d77-0000-7000-8004-', SUBSTRING(MD5(`api`.`sys_api_id`), 1, 12)),
  @research_history_function_id, `api`.`sys_api_id`, @research_history_now_ms, @research_history_now_ms,
  COALESCE(@research_history_admin_user_id, ''), COALESCE(@research_history_admin_user_id, ''), 0,
  '系统初始化历史报告接口绑定'
FROM `sys_api` AS `api`
WHERE `api`.`http_method` = 'GET'
  AND `api`.`path_pattern` IN (
    '/api/market-research/jobs',
    '/api/market-research/jobs/{jobId}/download',
    '/api/market-research/jobs/{jobId}/artifacts/{artifactId}/download'
  )
  AND `api`.`status` = 1
  AND `api`.`deleted` = 0
  AND @research_history_function_id IS NOT NULL
ON DUPLICATE KEY UPDATE
  `sys_function_id` = VALUES(`sys_function_id`),
  `sys_api_id` = VALUES(`sys_api_id`),
  `updated_at` = @research_history_now_ms,
  `updated_by` = COALESCE(@research_history_admin_user_id, ''),
  `deleted` = 0,
  `remark` = VALUES(`remark`);

COMMIT;
