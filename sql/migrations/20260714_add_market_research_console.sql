USE `sellersprite_service`;

-- 本迁移补充市场调研报告菜单、管理员授权和三个市场调研任务接口绑定。
START TRANSACTION;

SET @research_console_now_ms = CAST(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000 AS UNSIGNED);
SET @research_console_admin_user_id = (
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
  '019f4d77-0000-7000-8000-000000000013', '0', 'research.market-report', '市场调研报告', 'MENU',
  '/research/market-report', 'research/market-report', 'research:market-report:view', 'DataAnalysis', 1, 0,
  NULL, 27, 1, @research_console_now_ms, @research_console_now_ms,
  COALESCE(@research_console_admin_user_id, ''), COALESCE(@research_console_admin_user_id, ''), 0,
  '系统初始化市场调研报告菜单'
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
  `updated_at` = @research_console_now_ms,
  `updated_by` = COALESCE(@research_console_admin_user_id, ''),
  `deleted` = 0,
  `remark` = VALUES(`remark`);

SET @research_console_function_id = (
  SELECT `sys_function_id`
  FROM `sys_function`
  WHERE `function_code` = 'research.market-report' AND `deleted` = 0
  ORDER BY `created_at`
  LIMIT 1
);

-- role_code=admin 是当前权限拦截器识别的超级管理员角色。
INSERT INTO `role_function` (
  `role_function_id`, `role_id`, `sys_function_id`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
)
SELECT
  '019f4d77-0000-7000-8000-000000000113', `role`.`role_id`, @research_console_function_id,
  @research_console_now_ms, @research_console_now_ms,
  COALESCE(@research_console_admin_user_id, ''), COALESCE(@research_console_admin_user_id, ''), 0,
  '系统初始化管理员市场调研报告授权'
FROM `role` AS `role`
WHERE `role`.`role_code` = 'admin'
  AND `role`.`status` = 1
  AND `role`.`deleted` = 0
  AND @research_console_function_id IS NOT NULL
ON DUPLICATE KEY UPDATE
  `role_id` = VALUES(`role_id`),
  `sys_function_id` = VALUES(`sys_function_id`),
  `updated_at` = @research_console_now_ms,
  `updated_by` = COALESCE(@research_console_admin_user_id, ''),
  `deleted` = 0,
  `remark` = VALUES(`remark`);

-- 接口目录通常在应用启动后同步；目录尚无目标接口时本语句写入 0 行且不会失败。
-- 完成接口目录同步后可安全重跑本迁移，补齐市场调研报告菜单的三个接口绑定。
INSERT INTO `function_api` (
  `function_api_id`, `sys_function_id`, `sys_api_id`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
)
SELECT
  CONCAT('019f4d77-0000-7000-8003-', SUBSTRING(MD5(`api`.`sys_api_id`), 1, 12)),
  @research_console_function_id, `api`.`sys_api_id`, @research_console_now_ms, @research_console_now_ms,
  COALESCE(@research_console_admin_user_id, ''), COALESCE(@research_console_admin_user_id, ''), 0,
  '系统初始化市场调研报告接口绑定'
FROM `sys_api` AS `api`
WHERE (
    (`api`.`http_method` = 'POST' AND `api`.`path_pattern` = '/api/market-research/jobs')
    OR (`api`.`http_method` = 'GET' AND `api`.`path_pattern` = '/api/market-research/jobs/{jobId}')
    OR (`api`.`http_method` = 'GET' AND `api`.`path_pattern` = '/api/market-research/jobs/{jobId}/download')
  )
  AND `api`.`status` = 1
  AND `api`.`deleted` = 0
  AND @research_console_function_id IS NOT NULL
ON DUPLICATE KEY UPDATE
  `sys_function_id` = VALUES(`sys_function_id`),
  `sys_api_id` = VALUES(`sys_api_id`),
  `updated_at` = @research_console_now_ms,
  `updated_by` = COALESCE(@research_console_admin_user_id, ''),
  `deleted` = 0,
  `remark` = VALUES(`remark`);

COMMIT;
