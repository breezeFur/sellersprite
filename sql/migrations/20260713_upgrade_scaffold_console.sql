USE `sellersprite_service`;

-- 执行顺序：先执行 20260710_add_sellersprite_web_console.sql，再执行本迁移，
-- 最后按需执行 20260713_add_sellersprite_workbench.sql。
-- 本迁移只收敛字段类型、补齐脚手架控制台功能和角色功能授权，不创建或更新用户，也不修改密码。
-- 所有 MODIFY COLUMN 都收敛到同一目标定义，菜单和授权使用 UPSERT，可安全重复执行。

-- 兼容旧库的 JSON/TEXT 字段，并允许 Spring AI JDBC Memory 持久化 TOOL 消息。
ALTER TABLE `operation_log`
  MODIFY COLUMN `request_params` longtext DEFAULT NULL COMMENT '请求参数长文本',
  MODIFY COLUMN `response_payload` longtext DEFAULT NULL COMMENT '响应结果长文本';

ALTER TABLE `SPRING_AI_CHAT_MEMORY`
  MODIFY COLUMN `conversation_id` varchar(36) NOT NULL COMMENT '会话ID',
  MODIFY COLUMN `content` longtext NOT NULL COMMENT '消息内容长文本',
  MODIFY COLUMN `type` enum('USER', 'ASSISTANT', 'SYSTEM', 'TOOL') NOT NULL COMMENT '消息类型：USER用户 ASSISTANT助手 SYSTEM系统 TOOL工具',
  MODIFY COLUMN `timestamp` timestamp NOT NULL COMMENT '消息创建时间',
  MODIFY COLUMN `sequence_id` bigint NOT NULL COMMENT '会话内消息顺序ID';

ALTER TABLE `ai_conversation`
  MODIFY COLUMN `system_prompt` longtext NOT NULL COMMENT '会话系统提示词';

ALTER TABLE `ai_conversation_message`
  MODIFY COLUMN `metadata` longtext DEFAULT NULL COMMENT '消息扩展元数据长文本';

ALTER TABLE `ai_prompt_record`
  MODIFY COLUMN `request_messages` longtext NOT NULL COMMENT '实际送模完整消息长文本',
  MODIFY COLUMN `response_metadata` longtext DEFAULT NULL COMMENT '模型响应元数据长文本';

START TRANSACTION;

SET @console_now_ms = CAST(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000 AS UNSIGNED);

INSERT INTO `sys_function` (
  `sys_function_id`, `parent_id`, `function_code`, `function_name`, `function_type`,
  `route_path`, `component_path`, `permission_code`, `icon`, `visible`, `cacheable`,
  `external_link`, `sort_order`, `status`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
) VALUES
  ('019f4d77-0000-7000-8000-000000000010', '0', 'dashboard', '首页概览', 'MENU',
   '/dashboard', 'dashboard/overview', 'dashboard:view', 'House', 1, 0,
   NULL, 10, 1, @console_now_ms, @console_now_ms, '', '', 0, '系统初始化首页菜单'),
  ('019f4d77-0000-7000-8000-000000000011', '0', 'ai.chat', 'AI 对话', 'MENU',
   '/ai/chat', 'ai/chat', 'ai:chat:view', 'ChatLineRound', 1, 1,
   NULL, 20, 1, @console_now_ms, @console_now_ms, '', '', 0, '系统初始化 AI 对话菜单'),
  ('019f4d77-0000-7000-8000-000000000020', '0', 'system', '系统管理', 'DIR',
   NULL, NULL, NULL, 'SetUp', 1, 0,
   NULL, 30, 1, @console_now_ms, @console_now_ms, '', '', 0, '系统初始化系统管理目录'),
  ('019f4d77-0000-7000-8000-000000000021', '019f4d77-0000-7000-8000-000000000020', 'system.user', '用户管理', 'MENU',
   '/system/users', 'system/users', 'system:user:view', 'User', 1, 0,
   NULL, 10, 1, @console_now_ms, @console_now_ms, '', '', 0, '系统初始化用户菜单'),
  ('019f4d77-0000-7000-8000-000000000022', '019f4d77-0000-7000-8000-000000000020', 'system.dept', '部门管理', 'MENU',
   '/system/departments', 'system/departments', 'system:dept:view', 'OfficeBuilding', 1, 0,
   NULL, 20, 1, @console_now_ms, @console_now_ms, '', '', 0, '系统初始化部门菜单'),
  ('019f4d77-0000-7000-8000-000000000023', '019f4d77-0000-7000-8000-000000000020', 'system.role', '角色管理', 'MENU',
   '/system/roles', 'system/roles', 'system:role:view', 'UserFilled', 1, 0,
   NULL, 30, 1, @console_now_ms, @console_now_ms, '', '', 0, '系统初始化角色菜单'),
  ('019f4d77-0000-7000-8000-000000000024', '019f4d77-0000-7000-8000-000000000020', 'system.dict', '字典管理', 'MENU',
   '/system/dictionaries', 'system/dictionaries', 'system:dict:view', 'Collection', 1, 0,
   NULL, 40, 1, @console_now_ms, @console_now_ms, '', '', 0, '系统初始化字典菜单'),
  ('019f4d77-0000-7000-8000-000000000025', '019f4d77-0000-7000-8000-000000000020', 'system.function', '功能菜单', 'MENU',
   '/system/functions', 'system/functions', 'system:function:view', 'Menu', 1, 0,
   NULL, 50, 1, @console_now_ms, @console_now_ms, '', '', 0, '系统初始化功能菜单'),
  ('019f4d77-0000-7000-8000-000000000026', '019f4d77-0000-7000-8000-000000000020', 'system.api', '接口资源', 'MENU',
   '/system/apis', 'system/apis', 'system:api:view', 'Connection', 1, 0,
   NULL, 60, 1, @console_now_ms, @console_now_ms, '', '', 0, '系统初始化接口资源菜单'),
  ('019f4d77-0000-7000-8000-000000000030', '0', 'ops', '运维管理', 'DIR',
   NULL, NULL, NULL, 'DataAnalysis', 1, 0,
   NULL, 40, 1, @console_now_ms, @console_now_ms, '', '', 0, '系统初始化运维管理目录'),
  ('019f4d77-0000-7000-8000-000000000031', '019f4d77-0000-7000-8000-000000000030', 'ops.cache', '缓存管理', 'MENU',
   '/ops/cache', 'ops/cache', 'ops:cache:view', 'Key', 1, 0,
   NULL, 10, 1, @console_now_ms, @console_now_ms, '', '', 0, '系统初始化缓存菜单'),
  ('019f4d77-0000-7000-8000-000000000032', '019f4d77-0000-7000-8000-000000000030', 'ops.logs', '日志查询', 'MENU',
   '/ops/logs', 'ops/logs', 'ops:logs:view', 'Document', 1, 0,
   NULL, 20, 1, @console_now_ms, @console_now_ms, '', '', 0, '系统初始化日志菜单')
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
  `updated_at` = @console_now_ms,
  `updated_by` = '',
  `deleted` = 0,
  `remark` = VALUES(`remark`);

-- 兼容已有库中使用非种子 ID 创建的 system/ops 目录，按稳定功能编码修正子菜单父关系。
SET @console_system_function_id = (
  SELECT `sys_function_id`
  FROM `sys_function`
  WHERE `function_code` = 'system' AND `status` = 1 AND `deleted` = 0
  ORDER BY `created_at`
  LIMIT 1
);
SET @console_ops_function_id = (
  SELECT `sys_function_id`
  FROM `sys_function`
  WHERE `function_code` = 'ops' AND `status` = 1 AND `deleted` = 0
  ORDER BY `created_at`
  LIMIT 1
);

UPDATE `sys_function`
SET `parent_id` = @console_system_function_id,
    `updated_at` = @console_now_ms,
    `updated_by` = ''
WHERE `function_code` IN (
    'system.user', 'system.dept', 'system.role',
    'system.dict', 'system.function', 'system.api'
  )
  AND `deleted` = 0
  AND @console_system_function_id IS NOT NULL;

UPDATE `sys_function`
SET `parent_id` = @console_ops_function_id,
    `updated_at` = @console_now_ms,
    `updated_by` = ''
WHERE `function_code` IN ('ops.cache', 'ops.logs')
  AND `deleted` = 0
  AND @console_ops_function_id IS NOT NULL;

-- role_code=admin 是当前权限拦截器识别的 SUPER 管理员；同时兼容已有 role_code=SUPER 的部署。
-- 若两类启用角色都不存在，本语句写入 0 行且不会失败；角色创建后可安全重跑本迁移补齐授权。
INSERT INTO `role_function` (
  `role_function_id`, `role_id`, `sys_function_id`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
)
SELECT
  CONCAT('019f4d77-0000-7000-8002-',
         SUBSTRING(MD5(CONCAT(`role`.`role_id`, ':', `function`.`sys_function_id`)), 1, 12)),
  `role`.`role_id`, `function`.`sys_function_id`, @console_now_ms, @console_now_ms,
  '', '', 0, '系统初始化控制台 SUPER 管理员功能授权'
FROM `role` AS `role`
JOIN `sys_function` AS `function`
  ON `function`.`function_code` IN (
    'dashboard', 'ai.chat', 'system', 'system.user', 'system.dept', 'system.role',
    'system.dict', 'system.function', 'system.api', 'ops', 'ops.cache', 'ops.logs'
  )
 AND `function`.`status` = 1
 AND `function`.`deleted` = 0
WHERE `role`.`role_code` IN ('admin', 'SUPER')
  AND `role`.`status` = 1
  AND `role`.`deleted` = 0
ON DUPLICATE KEY UPDATE
  `role_id` = VALUES(`role_id`),
  `sys_function_id` = VALUES(`sys_function_id`),
  `updated_at` = @console_now_ms,
  `updated_by` = '',
  `deleted` = 0,
  `remark` = VALUES(`remark`);

COMMIT;
