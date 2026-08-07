CREATE DATABASE IF NOT EXISTS `sellersprite_service`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `sellersprite_service`;

CREATE TABLE IF NOT EXISTS `user` (
  `user_id` char(36) NOT NULL COMMENT '用户ID，UUIDv7',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
  `nickname` varchar(64) NOT NULL DEFAULT '' COMMENT '昵称',
  `real_name` varchar(64) NOT NULL DEFAULT '' COMMENT '真实姓名',
  `avatar_url` varchar(512) NOT NULL DEFAULT '' COMMENT '头像地址',
  `mobile` varchar(32) DEFAULT NULL COMMENT '手机号',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `gender` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '性别：0未知 1男 2女',
  `primary_dept_id` char(36) DEFAULT NULL COMMENT '主部门ID',
  `status` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `last_login_at` bigint unsigned DEFAULT NULL COMMENT '最后登录时间，Unix毫秒',
  `password_updated_at` bigint unsigned DEFAULT NULL COMMENT '密码更新时间，Unix毫秒',
  `permission_version` bigint unsigned NOT NULL DEFAULT 0 COMMENT '权限版本，授权变更时递增',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_username` (`username`, `deleted`),
  UNIQUE KEY `uk_user_mobile` (`mobile`, `deleted`),
  UNIQUE KEY `uk_user_email` (`email`, `deleted`),
  KEY `idx_user_primary_dept_id` (`primary_dept_id`),
  KEY `idx_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `dept` (
  `dept_id` char(36) NOT NULL COMMENT '部门ID，UUIDv7',
  `parent_id` char(36) NOT NULL DEFAULT '0' COMMENT '父部门ID，0表示根节点',
  `dept_code` varchar(64) NOT NULL COMMENT '部门编码',
  `dept_name` varchar(128) NOT NULL COMMENT '部门名称',
  `dept_path` varchar(1024) NOT NULL DEFAULT '/' COMMENT '部门路径',
  `leader_user_id` char(36) DEFAULT NULL COMMENT '负责人用户ID',
  `phone` varchar(32) NOT NULL DEFAULT '' COMMENT '联系电话',
  `email` varchar(128) NOT NULL DEFAULT '' COMMENT '邮箱',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`dept_id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`, `deleted`),
  KEY `idx_dept_parent_id` (`parent_id`),
  KEY `idx_dept_leader_user_id` (`leader_user_id`),
  KEY `idx_dept_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

CREATE TABLE IF NOT EXISTS `role` (
  `role_id` char(36) NOT NULL COMMENT '角色ID，UUIDv7',
  `role_code` varchar(64) NOT NULL COMMENT '角色编码',
  `role_name` varchar(128) NOT NULL COMMENT '角色名称',
  `role_type` varchar(32) NOT NULL DEFAULT 'BUSINESS' COMMENT '角色类型：SYSTEM系统 BUSINESS业务 CUSTOM自定义',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`, `deleted`),
  KEY `idx_role_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `user_role` (
  `user_role_id` char(36) NOT NULL COMMENT '用户角色ID，UUIDv7',
  `user_id` char(36) NOT NULL COMMENT '用户ID',
  `role_id` char(36) NOT NULL COMMENT '角色ID',
  `dept_id` char(36) NOT NULL COMMENT '角色所属部门ID',
  `primary_role` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否主角色：1是 0否',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`user_role_id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`, `dept_id`, `deleted`),
  KEY `idx_user_role_user_id` (`user_id`),
  KEY `idx_user_role_role_id` (`role_id`),
  KEY `idx_user_role_dept_id` (`dept_id`),
  KEY `idx_user_role_primary` (`user_id`, `dept_id`, `primary_role`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色表';

CREATE TABLE IF NOT EXISTS `sys_function` (
  `sys_function_id` char(36) NOT NULL COMMENT '系统功能ID，UUIDv7',
  `parent_id` char(36) NOT NULL DEFAULT '0' COMMENT '父功能ID，0表示根节点',
  `function_code` varchar(128) NOT NULL COMMENT '功能编码',
  `function_name` varchar(128) NOT NULL COMMENT '功能名称',
  `function_type` varchar(16) NOT NULL COMMENT '功能类型：DIR目录 MENU菜单 BUTTON按钮',
  `route_path` varchar(255) DEFAULT NULL COMMENT '前端路由路径',
  `component_path` varchar(255) DEFAULT NULL COMMENT '前端组件路径',
  `permission_code` varchar(128) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(128) NOT NULL DEFAULT '' COMMENT '图标',
  `visible` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否可见：1是 0否',
  `cacheable` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否缓存：1是 0否',
  `external_link` varchar(512) DEFAULT NULL COMMENT '外链地址',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`sys_function_id`),
  UNIQUE KEY `uk_sys_function_code` (`function_code`, `deleted`),
  KEY `idx_sys_function_parent_id` (`parent_id`),
  KEY `idx_sys_function_permission_code` (`permission_code`),
  KEY `idx_sys_function_type_status_sort` (`function_type`, `status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统功能表';

CREATE TABLE IF NOT EXISTS `sys_api` (
  `sys_api_id` char(36) NOT NULL COMMENT '系统接口ID，UUIDv7',
  `api_code` varchar(128) NOT NULL COMMENT '接口编码',
  `api_name` varchar(128) NOT NULL COMMENT '接口名称',
  `api_type` varchar(16) NOT NULL COMMENT '接口类型：PUBLIC公开接口 PERMISSION权限接口',
  `http_method` varchar(16) NOT NULL COMMENT 'HTTP方法',
  `path_pattern` varchar(255) NOT NULL COMMENT '接口路径模式',
  `permission_code` varchar(128) DEFAULT NULL COMMENT '权限标识',
  `module_name` varchar(64) NOT NULL DEFAULT '' COMMENT '模块名称',
  `operation_name` varchar(64) NOT NULL DEFAULT '' COMMENT '操作名称',
  `status` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`sys_api_id`),
  UNIQUE KEY `uk_sys_api_code` (`api_code`, `deleted`),
  UNIQUE KEY `uk_sys_api_method_path` (`http_method`, `path_pattern`, `deleted`),
  KEY `idx_sys_api_type_status` (`api_type`, `status`),
  KEY `idx_sys_api_permission_code` (`permission_code`),
  KEY `idx_sys_api_module_name` (`module_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统接口表';

CREATE TABLE IF NOT EXISTS `role_function` (
  `role_function_id` char(36) NOT NULL COMMENT '角色功能ID，UUIDv7',
  `role_id` char(36) NOT NULL COMMENT '角色ID',
  `sys_function_id` char(36) NOT NULL COMMENT '系统功能ID',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`role_function_id`),
  UNIQUE KEY `uk_role_function` (`role_id`, `sys_function_id`, `deleted`),
  KEY `idx_role_function_role_id` (`role_id`),
  KEY `idx_role_function_sys_function_id` (`sys_function_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色功能表';

CREATE TABLE IF NOT EXISTS `function_api` (
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

CREATE TABLE IF NOT EXISTS `role_api` (
  `role_api_id` char(36) NOT NULL COMMENT '角色接口ID，UUIDv7',
  `role_id` char(36) NOT NULL COMMENT '角色ID',
  `sys_api_id` char(36) NOT NULL COMMENT '系统接口ID',
  `grant_source` varchar(16) NOT NULL DEFAULT 'EXTRA' COMMENT '授权来源：FUNCTION功能派生 EXTRA直接附加 BOTH双重来源',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`role_api_id`),
  UNIQUE KEY `uk_role_api` (`role_id`, `sys_api_id`, `deleted`),
  KEY `idx_role_api_role_id` (`role_id`),
  KEY `idx_role_api_sys_api_id` (`sys_api_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色接口表';

CREATE TABLE IF NOT EXISTS `dict_type` (
  `dict_type` varchar(64) NOT NULL COMMENT '字典类型，稳定业务主键',
  `dict_type_name` varchar(128) NOT NULL COMMENT '字典类型名称',
  `system_builtin` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否系统内置：1是 0否',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`dict_type`),
  KEY `idx_dict_type_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典类型表';

CREATE TABLE IF NOT EXISTS `dict_data` (
  `dict_data_id` char(36) NOT NULL COMMENT '字典数据ID，UUIDv7',
  `dict_type` varchar(64) NOT NULL COMMENT '字典类型，外键关联dict_type.dict_type',
  `dict_value` varchar(128) DEFAULT NULL COMMENT '系统内部使用的值，可为空',
  `dict_label` varchar(128) NOT NULL COMMENT '稳定传输标识，一般不修改，用于前后端传参',
  `dict_name` varchar(128) NOT NULL COMMENT '字典数据名称，一般用于前端展示',
  `css_class` varchar(64) NOT NULL DEFAULT '' COMMENT 'CSS类名',
  `color` varchar(32) NOT NULL DEFAULT '' COMMENT '展示颜色',
  `system_builtin` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否系统内置：1是 0否',
  `default_flag` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否默认数据：1是 0否',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `status` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`dict_data_id`),
  UNIQUE KEY `uk_dict_data_label` (`dict_label`),
  KEY `idx_dict_data_type_status_sort` (`dict_type`, `status`, `sort_order`),
  CONSTRAINT `fk_dict_data_type` FOREIGN KEY (`dict_type`) REFERENCES `dict_type` (`dict_type`)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='字典数据表';

CREATE TABLE IF NOT EXISTS `user_token` (
  `user_token_id` char(36) NOT NULL COMMENT '用户令牌ID，UUIDv7',
  `user_id` char(36) NOT NULL COMMENT '用户ID',
  `access_token_hash` char(64) NOT NULL COMMENT '访问令牌哈希，建议SHA-256',
  `refresh_token_hash` char(64) DEFAULT NULL COMMENT '刷新令牌哈希，建议SHA-256',
  `session_family_id` char(36) NOT NULL COMMENT '刷新会话链ID，UUIDv7',
  `replaced_by_token_id` char(36) DEFAULT NULL COMMENT '轮换后的令牌ID',
  `token_type` varchar(32) NOT NULL DEFAULT 'BEARER' COMMENT '令牌类型',
  `device_id` varchar(128) DEFAULT NULL COMMENT '设备ID',
  `device_name` varchar(128) NOT NULL DEFAULT '' COMMENT '设备名称',
  `client_type` varchar(32) NOT NULL DEFAULT 'WEB' COMMENT '客户端类型：WEB APP MINI_PROGRAM API',
  `login_ip` varchar(64) NOT NULL DEFAULT '' COMMENT '登录IP',
  `user_agent` varchar(512) NOT NULL DEFAULT '' COMMENT '用户代理',
  `issued_at` bigint unsigned NOT NULL COMMENT '签发时间，Unix毫秒',
  `expires_at` bigint unsigned NOT NULL COMMENT '过期时间，Unix毫秒',
  `refresh_expires_at` bigint unsigned NOT NULL COMMENT '刷新令牌过期时间，Unix毫秒',
  `last_used_at` bigint unsigned DEFAULT NULL COMMENT '最近刷新时间，Unix毫秒',
  `revoked_at` bigint unsigned DEFAULT NULL COMMENT '撤销时间，Unix毫秒',
  `revoke_reason` varchar(256) NOT NULL DEFAULT '' COMMENT '撤销原因',
  `status` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '状态：1有效 0失效',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`user_token_id`),
  UNIQUE KEY `uk_user_token_access_hash` (`access_token_hash`),
  UNIQUE KEY `uk_user_token_refresh_hash` (`refresh_token_hash`),
  KEY `idx_user_token_user_id` (`user_id`),
  KEY `idx_user_token_family_id` (`session_family_id`),
  KEY `idx_user_token_replaced_by` (`replaced_by_token_id`),
  KEY `idx_user_token_expires_at` (`expires_at`),
  KEY `idx_user_token_refresh_expires_at` (`refresh_expires_at`),
  KEY `idx_user_token_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户令牌表';

CREATE TABLE IF NOT EXISTS `login_log` (
  `login_log_id` char(36) NOT NULL COMMENT '登录日志ID，UUIDv7',
  `user_id` char(36) DEFAULT NULL COMMENT '用户ID',
  `username` varchar(64) NOT NULL DEFAULT '' COMMENT '用户名',
  `login_type` varchar(32) NOT NULL DEFAULT 'PASSWORD' COMMENT '登录方式：PASSWORD TOKEN SSO',
  `success` tinyint unsigned NOT NULL COMMENT '是否成功：1成功 0失败',
  `error_code` varchar(64) NOT NULL DEFAULT '' COMMENT '稳定错误码',
  `failure_reason` varchar(512) NOT NULL DEFAULT '' COMMENT '失败原因',
  `login_ip` varchar(64) NOT NULL DEFAULT '' COMMENT '登录IP',
  `login_location` varchar(128) NOT NULL DEFAULT '' COMMENT '登录地点',
  `user_agent` varchar(512) NOT NULL DEFAULT '' COMMENT '用户代理',
  `device_name` varchar(128) NOT NULL DEFAULT '' COMMENT '设备名称',
  `client_type` varchar(32) NOT NULL DEFAULT 'WEB' COMMENT '客户端类型',
  `track_id` varchar(64) NOT NULL DEFAULT '' COMMENT '链路追踪ID',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  PRIMARY KEY (`login_log_id`),
  KEY `idx_login_log_user_id` (`user_id`),
  KEY `idx_login_log_username` (`username`),
  KEY `idx_login_log_created_at` (`created_at`),
  KEY `idx_login_log_track_id` (`track_id`),
  KEY `idx_login_log_success` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='登录日志表';

CREATE TABLE IF NOT EXISTS `operation_log` (
  `operation_log_id` char(36) NOT NULL COMMENT '操作日志ID，UUIDv7',
  `user_id` char(36) DEFAULT NULL COMMENT '用户ID',
  `username` varchar(64) NOT NULL DEFAULT '' COMMENT '用户名',
  `module_name` varchar(64) NOT NULL DEFAULT '' COMMENT '模块名称',
  `operation_name` varchar(128) NOT NULL DEFAULT '' COMMENT '操作名称',
  `operation_type` varchar(32) NOT NULL DEFAULT '' COMMENT '操作类型：CREATE UPDATE DELETE QUERY EXPORT IMPORT',
  `resource_id` varchar(128) NOT NULL DEFAULT '' COMMENT '业务资源ID',
  `http_method` varchar(16) NOT NULL DEFAULT '' COMMENT 'HTTP方法',
  `request_uri` varchar(512) NOT NULL DEFAULT '' COMMENT '请求URI',
  `request_params` longtext DEFAULT NULL COMMENT '请求参数长文本',
  `response_payload` longtext DEFAULT NULL COMMENT '响应结果长文本',
  `response_status` int DEFAULT NULL COMMENT '响应状态码',
  `success` tinyint unsigned NOT NULL COMMENT '是否成功：1成功 0失败',
  `error_code` varchar(64) NOT NULL DEFAULT '' COMMENT '稳定错误码',
  `error_message` text DEFAULT NULL COMMENT '错误信息',
  `client_ip` varchar(64) NOT NULL DEFAULT '' COMMENT '客户端IP',
  `user_agent` varchar(512) NOT NULL DEFAULT '' COMMENT '用户代理',
  `cost_ms` bigint unsigned NOT NULL DEFAULT 0 COMMENT '耗时毫秒',
  `track_id` varchar(64) NOT NULL DEFAULT '' COMMENT '链路追踪ID',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  PRIMARY KEY (`operation_log_id`),
  KEY `idx_operation_log_user_id` (`user_id`),
  KEY `idx_operation_log_username` (`username`),
  KEY `idx_operation_log_created_at` (`created_at`),
  KEY `idx_operation_log_track_id` (`track_id`),
  KEY `idx_operation_log_module` (`module_name`),
  KEY `idx_operation_log_operation_type` (`operation_type`),
  KEY `idx_operation_log_success` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';

-- Spring AI 2.0 JDBC Memory 的运行时 SQL 固定使用该大写表名，不可改为小写。
CREATE TABLE IF NOT EXISTS `SPRING_AI_CHAT_MEMORY` (
  `conversation_id` varchar(36) NOT NULL COMMENT '会话ID',
  `content` longtext NOT NULL COMMENT '消息内容长文本',
  `type` enum('USER', 'ASSISTANT', 'SYSTEM', 'TOOL') NOT NULL COMMENT '消息类型：USER用户 ASSISTANT助手 SYSTEM系统 TOOL工具',
  `timestamp` timestamp NOT NULL COMMENT '消息创建时间',
  `sequence_id` bigint NOT NULL COMMENT '会话内消息顺序ID',
  KEY `SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX` (`conversation_id`, `timestamp`),
  KEY `SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_SEQUENCE_ID_IDX` (`conversation_id`, `sequence_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Spring AI模型上下文窗口表';

CREATE TABLE IF NOT EXISTS `ai_conversation` (
  `conversation_id` char(36) NOT NULL COMMENT '会话ID，UUIDv7',
  `user_id` char(36) NOT NULL COMMENT '会话归属用户ID',
  `title` varchar(128) NOT NULL DEFAULT '' COMMENT '会话标题',
  `provider` varchar(64) NOT NULL DEFAULT '' COMMENT '模型服务提供方',
  `model` varchar(128) NOT NULL DEFAULT '' COMMENT '模型名称',
  `system_prompt` longtext NOT NULL COMMENT '会话系统提示词',
  `message_count` int unsigned NOT NULL DEFAULT 0 COMMENT '前端可见消息数量',
  `last_message_at` bigint unsigned NOT NULL COMMENT '最后消息时间，Unix毫秒',
  `status` varchar(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE ARCHIVED',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`conversation_id`),
  KEY `idx_ai_conversation_user_updated` (`user_id`, `updated_at`),
  KEY `idx_ai_conversation_user_last_message` (`user_id`, `last_message_at`),
  KEY `idx_ai_conversation_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI会话主表';

CREATE TABLE IF NOT EXISTS `ai_conversation_message` (
  `message_id` char(36) NOT NULL COMMENT '消息ID，UUIDv7',
  `conversation_id` char(36) NOT NULL COMMENT '会话ID',
  `user_id` char(36) NOT NULL COMMENT '会话归属用户ID',
  `prompt_record_id` char(36) NOT NULL COMMENT '关联Prompt记录ID',
  `sequence_no` int unsigned NOT NULL COMMENT '会话内消息序号',
  `role` varchar(32) NOT NULL COMMENT '消息角色：SYSTEM USER ASSISTANT TOOL',
  `content` longtext NOT NULL COMMENT '消息内容',
  `content_type` varchar(32) NOT NULL DEFAULT 'TEXT' COMMENT '内容类型',
  `metadata` longtext DEFAULT NULL COMMENT '消息扩展元数据长文本',
  `message_status` varchar(32) NOT NULL DEFAULT 'COMPLETED' COMMENT '消息状态：STREAMING COMPLETED CANCELLED FAILED',
  `error_code` varchar(64) NOT NULL DEFAULT '' COMMENT '稳定错误码',
  `error_message` varchar(512) NOT NULL DEFAULT '' COMMENT '安全错误摘要',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  PRIMARY KEY (`message_id`),
  UNIQUE KEY `uk_ai_message_sequence` (`conversation_id`, `sequence_no`, `deleted`),
  KEY `idx_ai_message_user_conversation` (`user_id`, `conversation_id`),
  KEY `idx_ai_message_prompt_record` (`prompt_record_id`),
  KEY `idx_ai_message_status` (`conversation_id`, `message_status`),
  KEY `idx_ai_message_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI会话完整消息表';

CREATE TABLE IF NOT EXISTS `ai_prompt_record` (
  `prompt_record_id` char(36) NOT NULL COMMENT 'Prompt记录ID，UUIDv7',
  `conversation_id` char(36) NOT NULL COMMENT '会话ID',
  `user_id` char(36) NOT NULL COMMENT '调用用户ID',
  `provider` varchar(64) NOT NULL DEFAULT '' COMMENT '模型服务提供方',
  `model` varchar(128) NOT NULL DEFAULT '' COMMENT '模型名称',
  `request_messages` longtext NOT NULL COMMENT '实际送模完整消息长文本',
  `prompt_summary` varchar(2048) NOT NULL DEFAULT '' COMMENT '截断脱敏后的Prompt摘要',
  `prompt_truncated` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '摘要是否被截断：1是 0否',
  `response_content` longtext NOT NULL COMMENT '模型响应文本',
  `response_metadata` longtext DEFAULT NULL COMMENT '模型响应元数据长文本',
  `prompt_tokens` int unsigned DEFAULT NULL COMMENT '输入Token数',
  `completion_tokens` int unsigned DEFAULT NULL COMMENT '输出Token数',
  `total_tokens` int unsigned DEFAULT NULL COMMENT '总Token数',
  `finish_reason` varchar(64) NOT NULL DEFAULT '' COMMENT '模型结束原因',
  `status` varchar(32) NOT NULL DEFAULT 'PROCESSING' COMMENT '状态：PROCESSING SUCCESS FAILED',
  `error_type` varchar(256) NOT NULL DEFAULT '' COMMENT '错误类型',
  `error_message` text DEFAULT NULL COMMENT '错误信息',
  `cost_ms` bigint unsigned NOT NULL DEFAULT 0 COMMENT '调用耗时毫秒',
  `track_id` varchar(64) NOT NULL DEFAULT '' COMMENT '链路追踪ID',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`prompt_record_id`),
  KEY `idx_ai_prompt_conversation` (`conversation_id`, `created_at`),
  KEY `idx_ai_prompt_user` (`user_id`, `created_at`),
  KEY `idx_ai_prompt_track_id` (`track_id`),
  KEY `idx_ai_prompt_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI模型完整Prompt调用记录表';

-- 兼容已使用旧版脚本创建的数据库，确保长文本字段不再受结构化格式校验。
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

-- Spring AI Alibaba Graph 官方 MySQL checkpoint 表；工作流定义仍由 Java StateGraph 管理。
CREATE TABLE IF NOT EXISTS `GRAPH_THREAD` (
  `thread_id` varchar(36) NOT NULL,
  `thread_name` varchar(255) DEFAULT NULL COMMENT '业务任务ID，市场调研使用jobId',
  `is_released` boolean NOT NULL DEFAULT false,
  PRIMARY KEY (`thread_id`),
  UNIQUE KEY `IDX_GRAPH_THREAD_NAME_RELEASED` (`thread_name`, `is_released`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Graph执行线程表';

CREATE TABLE IF NOT EXISTS `GRAPH_CHECKPOINT` (
  `checkpoint_id` varchar(36) NOT NULL,
  `thread_id` varchar(36) NOT NULL,
  `node_id` varchar(255) DEFAULT NULL,
  `next_node_id` varchar(255) DEFAULT NULL,
  `state_data` json NOT NULL COMMENT 'Graph最小状态序列化数据',
  `saved_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`checkpoint_id`),
  KEY `IDX_GRAPH_CHECKPOINT_THREAD_SAVED` (`thread_id`, `saved_at`),
  CONSTRAINT `GRAPH_FK_THREAD` FOREIGN KEY (`thread_id`)
    REFERENCES `GRAPH_THREAD` (`thread_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Graph节点检查点表';

CREATE TABLE IF NOT EXISTS `market_research_job` (
  `job_id` char(36) NOT NULL COMMENT '市场调研任务ID，UUIDv7',
  `user_id` char(36) NOT NULL COMMENT '任务归属用户ID',
  `report_name` varchar(128) NOT NULL COMMENT '报告名称',
  `marketplace` varchar(16) NOT NULL COMMENT 'Amazon站点',
  `node_id_path` varchar(1024) NOT NULL COMMENT 'SellerSprite类目节点路径',
  `research_month` char(7) NOT NULL COMMENT '调研月份，yyyy-MM格式',
  `keyword` varchar(256) DEFAULT NULL COMMENT '可选核心调研关键词',
  `seed_asins` longtext DEFAULT NULL COMMENT '可选种子ASIN数组JSON，最多20个',
  `collection_config` longtext NOT NULL COMMENT '采集子图强类型参数JSON快照',
  `template_code` varchar(64) NOT NULL DEFAULT 'market-research-v1' COMMENT 'Excel模板代码',
  `data_source_mode` varchar(16) NOT NULL DEFAULT 'MOCK' COMMENT '数据源模式：MOCK REMOTE',
  `workflow_version` varchar(64) NOT NULL COMMENT '固定Java Graph工作流版本',
  `job_status` varchar(32) NOT NULL DEFAULT 'QUEUED' COMMENT '任务状态：QUEUED RUNNING WAITING_INPUT RETRY_WAIT SUCCEEDED ABANDONED FAILED CANCELLED',
  `current_node` varchar(64) NOT NULL DEFAULT 'validate' COMMENT '当前Graph节点编码',
  `current_stage` varchar(32) NOT NULL DEFAULT 'SCREENING' COMMENT '当前业务阶段：SCREENING DEEP_DIVE FINAL_ANALYSIS',
  `waiting_input_type` varchar(32) DEFAULT NULL COMMENT '当前等待的人工输入类型',
  `progress` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '任务进度，0到100',
  `attempt_count` int unsigned NOT NULL DEFAULT 0 COMMENT '任务已抢占执行次数',
  `max_attempts` int unsigned NOT NULL DEFAULT 3 COMMENT '最大自动执行次数',
  `next_run_at` bigint unsigned NOT NULL COMMENT '下次允许调度时间，Unix毫秒',
  `execution_owner` varchar(128) DEFAULT NULL COMMENT '当前执行实例标识',
  `execution_token` char(36) DEFAULT NULL COMMENT '当前任务领取令牌，租约转移时更新',
  `lease_until` bigint unsigned DEFAULT NULL COMMENT '执行租约截止时间，Unix毫秒',
  `heartbeat_at` bigint unsigned DEFAULT NULL COMMENT '最近心跳时间，Unix毫秒',
  `cancel_requested_at` bigint unsigned DEFAULT NULL COMMENT '取消请求时间，Unix毫秒',
  `error_code` varchar(64) NOT NULL DEFAULT '' COMMENT '稳定业务错误码',
  `error_message` varchar(512) NOT NULL DEFAULT '' COMMENT '可安全展示的错误摘要',
  `started_at` bigint unsigned DEFAULT NULL COMMENT '首次开始时间，Unix毫秒',
  `finished_at` bigint unsigned DEFAULT NULL COMMENT '终态完成时间，Unix毫秒',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`job_id`),
  KEY `idx_market_research_job_user_created` (`user_id`, `created_at`),
  KEY `idx_market_research_job_user_status` (`user_id`, `job_status`),
  KEY `idx_market_research_job_dispatch` (`job_status`, `next_run_at`, `lease_until`),
  KEY `idx_market_research_job_owner_lease` (`execution_owner`, `lease_until`),
  KEY `idx_market_research_job_token_lease` (`execution_token`, `lease_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='市场调研任务实例表';

CREATE TABLE IF NOT EXISTS `market_research_stage_input` (
  `input_id` char(36) NOT NULL COMMENT '阶段输入ID，UUIDv7',
  `job_id` char(36) NOT NULL COMMENT '市场调研任务ID',
  `stage_code` varchar(32) NOT NULL COMMENT '提交输入时所在业务阶段',
  `input_type` varchar(32) NOT NULL COMMENT '人工输入类型：PRODUCT_SELECTION',
  `decision` varchar(16) NOT NULL COMMENT '人工决定：ENTER ABANDON',
  `input_payload` longtext NOT NULL COMMENT '强类型人工输入JSON快照',
  `submitted_by` char(36) NOT NULL COMMENT '提交用户ID',
  `submitted_at` bigint unsigned NOT NULL COMMENT '提交时间，Unix毫秒',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`input_id`),
  UNIQUE KEY `uk_market_research_stage_input`
    (`job_id`, `stage_code`, `input_type`, `deleted`),
  CONSTRAINT `fk_market_research_stage_input_job` FOREIGN KEY (`job_id`)
    REFERENCES `market_research_job` (`job_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='市场调研人工阶段输入表';

CREATE TABLE IF NOT EXISTS `market_research_node_execution` (
  `execution_id` char(36) NOT NULL COMMENT '节点执行记录ID，UUIDv7',
  `job_id` char(36) NOT NULL COMMENT '市场调研任务ID',
  `graph_code` varchar(32) NOT NULL COMMENT '所属子图编码：collection evidence report',
  `node_code` varchar(64) NOT NULL COMMENT '固定Graph节点编码',
  `node_name` varchar(128) NOT NULL COMMENT '节点中文名称快照',
  `job_attempt` int unsigned NOT NULL COMMENT '所属任务执行次数',
  `node_attempt` int unsigned NOT NULL COMMENT '当前任务执行内节点尝试序号',
  `execution_status` varchar(32) NOT NULL COMMENT '节点状态：RUNNING SUCCEEDED FAILED CANCELLED',
  `started_at` bigint unsigned NOT NULL COMMENT '节点开始时间，Unix毫秒',
  `finished_at` bigint unsigned DEFAULT NULL COMMENT '节点结束时间，Unix毫秒',
  `duration_ms` bigint unsigned DEFAULT NULL COMMENT '节点耗时，毫秒',
  `error_code` varchar(64) NOT NULL DEFAULT '' COMMENT '稳定错误码',
  `error_message` varchar(512) NOT NULL DEFAULT '' COMMENT '可安全展示的错误摘要',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`execution_id`),
  UNIQUE KEY `uk_market_research_node_attempt`
    (`job_id`, `node_code`, `job_attempt`, `node_attempt`, `deleted`),
  KEY `idx_market_research_node_job_started`
    (`job_id`, `started_at`, `job_attempt`, `node_attempt`),
  KEY `idx_market_research_node_status` (`execution_status`, `updated_at`),
  CONSTRAINT `fk_market_research_node_job` FOREIGN KEY (`job_id`)
    REFERENCES `market_research_job` (`job_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='市场调研节点执行审计表';

CREATE TABLE IF NOT EXISTS `market_research_dataset` (
  `dataset_id` char(36) NOT NULL COMMENT '不可变数据集ID，UUIDv7',
  `job_id` char(36) NOT NULL COMMENT '市场调研任务ID',
  `node_code` varchar(64) NOT NULL COMMENT '产生数据集的Graph节点编码',
  `operation` varchar(64) NOT NULL COMMENT '采集操作编码',
  `dataset_code` varchar(128) NOT NULL COMMENT '数据集业务编码',
  `request_hash` char(64) NOT NULL COMMENT '幂等请求SHA-256摘要',
  `data_source_mode` varchar(16) NOT NULL COMMENT '数据源模式：MOCK REMOTE',
  `request_payload` longtext NOT NULL COMMENT '脱敏后的请求参数JSON',
  `source_payload` longtext NOT NULL COMMENT '外部接口原始响应JSON',
  `normalized_payload` longtext DEFAULT NULL COMMENT '可选标准化响应JSON',
  `record_count` int unsigned NOT NULL DEFAULT 0 COMMENT '顶层业务记录数',
  `schema_version` varchar(32) NOT NULL COMMENT '数据集结构版本',
  `validation_status` varchar(32) NOT NULL COMMENT '校验状态：VALID INVALID',
  `validation_summary` varchar(512) NOT NULL DEFAULT '' COMMENT '数据集校验摘要',
  `sha256` char(64) NOT NULL COMMENT '源响应SHA-256十六进制摘要',
  `fetched_at` bigint unsigned NOT NULL COMMENT '数据采集时间，Unix毫秒',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`dataset_id`),
  UNIQUE KEY `uk_market_research_dataset_idempotency`
    (`job_id`, `node_code`, `operation`, `dataset_code`, `request_hash`, `deleted`),
  KEY `idx_market_research_dataset_job_created` (`job_id`, `created_at`),
  KEY `idx_market_research_dataset_sha256` (`sha256`),
  CONSTRAINT `fk_market_research_dataset_job` FOREIGN KEY (`job_id`)
    REFERENCES `market_research_job` (`job_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='市场调研不可变数据集表';

CREATE TABLE IF NOT EXISTS `market_research_analysis_run` (
  `analysis_run_id` char(36) NOT NULL COMMENT '分析运行ID，UUIDv7',
  `job_id` char(36) NOT NULL COMMENT '市场调研任务ID',
  `user_id` char(36) NOT NULL COMMENT '分析运行归属用户ID',
  `conversation_id` char(36) DEFAULT NULL COMMENT '关联AI会话ID',
  `parent_run_id` char(36) DEFAULT NULL COMMENT '重试或追问来源分析运行ID',
  `run_type` varchar(32) NOT NULL COMMENT '分析类型：SCREENING DEEP_DIVE FINAL_ANALYSIS RETRY FOLLOW_UP',
  `analysis_goal` longtext NOT NULL COMMENT '本次分析目标或后续问题',
  `run_status` varchar(32) NOT NULL DEFAULT 'WAITING_RESEARCH' COMMENT '运行状态：WAITING_RESEARCH QUEUED RUNNING RETRY_WAIT SUCCEEDED FAILED CANCELLED',
  `current_phase` varchar(64) NOT NULL DEFAULT 'waiting_research' COMMENT '当前分析阶段编码',
  `progress` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '分析进度，0到100',
  `attempt_count` int unsigned NOT NULL DEFAULT 0 COMMENT '已抢占执行次数',
  `max_attempts` int unsigned NOT NULL DEFAULT 3 COMMENT '最大自动执行次数',
  `next_run_at` bigint unsigned NOT NULL COMMENT '下次允许调度时间，Unix毫秒',
  `execution_owner` varchar(128) DEFAULT NULL COMMENT '当前执行实例标识',
  `execution_token` char(36) DEFAULT NULL COMMENT '当前领取令牌，租约转移时更新',
  `lease_until` bigint unsigned DEFAULT NULL COMMENT '执行租约截止时间，Unix毫秒',
  `heartbeat_at` bigint unsigned DEFAULT NULL COMMENT '最近心跳时间，Unix毫秒',
  `cancel_requested_at` bigint unsigned DEFAULT NULL COMMENT '取消请求时间，Unix毫秒',
  `model_call_count` int unsigned NOT NULL DEFAULT 0 COMMENT '本次运行累计模型调用次数',
  `event_count` int unsigned NOT NULL DEFAULT 0 COMMENT '本次运行累计持久化事件数',
  `final_summary` longtext DEFAULT NULL COMMENT '最终分析摘要Markdown',
  `error_code` varchar(64) NOT NULL DEFAULT '' COMMENT '稳定业务错误码',
  `error_message` varchar(512) NOT NULL DEFAULT '' COMMENT '可安全展示的错误摘要',
  `started_at` bigint unsigned DEFAULT NULL COMMENT '首次开始时间，Unix毫秒',
  `finished_at` bigint unsigned DEFAULT NULL COMMENT '终态完成时间，Unix毫秒',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`analysis_run_id`),
  KEY `idx_market_research_analysis_user_job` (`user_id`, `job_id`, `created_at`),
  KEY `idx_market_research_analysis_job_status` (`job_id`, `run_status`, `created_at`),
  KEY `idx_market_research_analysis_dispatch` (`run_status`, `next_run_at`, `lease_until`),
  KEY `idx_market_research_analysis_token_lease` (`execution_token`, `lease_until`),
  KEY `idx_market_research_analysis_conversation` (`conversation_id`, `created_at`),
  KEY `idx_market_research_analysis_parent` (`parent_run_id`),
  CONSTRAINT `fk_market_research_analysis_job` FOREIGN KEY (`job_id`)
    REFERENCES `market_research_job` (`job_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_market_research_analysis_conversation` FOREIGN KEY (`conversation_id`)
    REFERENCES `ai_conversation` (`conversation_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_market_research_analysis_parent` FOREIGN KEY (`parent_run_id`)
    REFERENCES `market_research_analysis_run` (`analysis_run_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='市场调研AI分析运行表';

CREATE TABLE IF NOT EXISTS `market_research_artifact` (
  `artifact_id` char(36) NOT NULL COMMENT '报告产物ID，UUIDv7',
  `job_id` char(36) NOT NULL COMMENT '市场调研任务ID',
  `analysis_run_id` char(36) DEFAULT NULL COMMENT '可选关联分析运行ID，Excel证据产物为空',
  `artifact_scope_id` char(36) NOT NULL COMMENT '产物唯一作用域，数据报告使用任务ID，AI报告使用分析运行ID',
  `workflow_version` varchar(64) NOT NULL COMMENT '生成产物的工作流版本',
  `artifact_type` varchar(32) NOT NULL COMMENT '产物类型：RAW_DATA_WORKBOOK EVIDENCE_WORKBOOK AI_ANALYSIS_REPORT',
  `file_name` varchar(255) NOT NULL COMMENT '下载文件名',
  `storage_key` varchar(1024) NOT NULL COMMENT '受控存储键，不是公开URL',
  `media_type` varchar(128) NOT NULL DEFAULT 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' COMMENT '文件媒体类型',
  `file_size` bigint unsigned NOT NULL DEFAULT 0 COMMENT '文件大小，字节',
  `sha256` char(64) DEFAULT NULL COMMENT '文件SHA-256十六进制摘要，草稿生成前为空',
  `artifact_status` varchar(32) NOT NULL DEFAULT 'GENERATING' COMMENT '产物状态：GENERATING PUBLISHED FAILED',
  `published_at` bigint unsigned DEFAULT NULL COMMENT '发布时间，Unix毫秒',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`artifact_id`),
  UNIQUE KEY `uk_market_research_artifact_scope` (`job_id`, `artifact_type`, `artifact_scope_id`, `deleted`),
  KEY `idx_market_research_artifact_job` (`job_id`),
  KEY `idx_market_research_artifact_analysis_run` (`analysis_run_id`),
  KEY `idx_market_research_artifact_status` (`artifact_status`, `updated_at`),
  KEY `idx_market_research_artifact_sha256` (`sha256`),
  CONSTRAINT `fk_market_research_artifact_job` FOREIGN KEY (`job_id`)
    REFERENCES `market_research_job` (`job_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_market_research_artifact_analysis_run` FOREIGN KEY (`analysis_run_id`)
    REFERENCES `market_research_analysis_run` (`analysis_run_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='市场调研产物表';

CREATE TABLE IF NOT EXISTS `market_research_event_stream_lock` (
  `job_id` char(36) NOT NULL COMMENT '市场调研任务ID',
  PRIMARY KEY (`job_id`),
  CONSTRAINT `fk_market_research_event_stream_lock_job` FOREIGN KEY (`job_id`)
    REFERENCES `market_research_job` (`job_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='市场调研事件流事务串行锁';

CREATE TABLE IF NOT EXISTS `market_research_event` (
  `event_id` char(36) NOT NULL COMMENT '事件ID，UUIDv7',
  `sequence_no` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '全局单调递增SSE序号',
  `job_id` char(36) NOT NULL COMMENT '市场调研任务ID',
  `conversation_id` char(36) DEFAULT NULL COMMENT '可选关联AI会话ID',
  `analysis_run_id` char(36) DEFAULT NULL COMMENT '可选关联分析运行ID',
  `scope` varchar(32) NOT NULL COMMENT '事件作用域：RESEARCH ANALYSIS WORKFLOW',
  `event_type` varchar(64) NOT NULL COMMENT 'SSE事件类型',
  `phase` varchar(64) DEFAULT NULL COMMENT '业务阶段编码',
  `sheet_name` varchar(128) DEFAULT NULL COMMENT '可选证据Sheet名称',
  `node_code` varchar(64) DEFAULT NULL COMMENT '可选Graph节点编码',
  `message` longtext NOT NULL COMMENT '用户可读消息或模型增量',
  `payload` longtext NOT NULL COMMENT '事件结构化JSON载荷',
  `terminal` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否统一工作流终态：1是 0否',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`event_id`),
  UNIQUE KEY `uk_market_research_event_sequence` (`sequence_no`),
  KEY `idx_market_research_event_job_sequence` (`job_id`, `sequence_no`),
  KEY `idx_market_research_event_run_sequence` (`analysis_run_id`, `sequence_no`),
  KEY `idx_market_research_event_conversation_sequence` (`conversation_id`, `sequence_no`),
  KEY `idx_market_research_event_type_created` (`event_type`, `created_at`),
  CONSTRAINT `fk_market_research_event_job` FOREIGN KEY (`job_id`)
    REFERENCES `market_research_job` (`job_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_market_research_event_conversation` FOREIGN KEY (`conversation_id`)
    REFERENCES `ai_conversation` (`conversation_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_market_research_event_analysis_run` FOREIGN KEY (`analysis_run_id`)
    REFERENCES `market_research_analysis_run` (`analysis_run_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='市场调研可重放SSE事件表';

-- 初始化管理员和首批功能菜单。重复执行本脚本会将 admin 密码重置为 123456，请在首次登录后立即修改。
START TRANSACTION;

SET @seed_now_ms = CAST(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000 AS UNSIGNED);

INSERT INTO `dept` (
  `dept_id`, `parent_id`, `dept_code`, `dept_name`, `dept_path`, `leader_user_id`,
  `phone`, `email`, `sort_order`, `status`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
) VALUES (
  '019f4d77-0000-7000-8000-000000000001', '0', 'ROOT', '根部门',
  '/019f4d77-0000-7000-8000-000000000001/', NULL,
  '', '', 0, 1, @seed_now_ms, @seed_now_ms, '', '', 0, '系统初始化根部门'
)
ON DUPLICATE KEY UPDATE
  `dept_name` = '根部门',
  `status` = 1,
  `updated_at` = @seed_now_ms,
  `deleted` = 0,
  `remark` = '系统初始化根部门';

SELECT `dept_id` INTO @seed_dept_id
FROM `dept`
WHERE `dept_code` = 'ROOT' AND `deleted` = 0
LIMIT 1;

INSERT INTO `role` (
  `role_id`, `role_code`, `role_name`, `role_type`, `sort_order`, `status`,
  `created_at`, `updated_at`, `created_by`, `updated_by`, `deleted`, `remark`
) VALUES (
  '019f4d77-0000-7000-8000-000000000002', 'admin', '系统管理员', 'SYSTEM', 0, 1,
  @seed_now_ms, @seed_now_ms, '', '', 0, '系统初始化管理员角色'
)
ON DUPLICATE KEY UPDATE
  `role_name` = '系统管理员',
  `role_type` = 'SYSTEM',
  `status` = 1,
  `updated_at` = @seed_now_ms,
  `deleted` = 0,
  `remark` = '系统初始化管理员角色';

SELECT `role_id` INTO @seed_role_id
FROM `role`
WHERE `role_code` = 'admin' AND `deleted` = 0
LIMIT 1;

INSERT INTO `user` (
  `user_id`, `username`, `password_hash`, `nickname`, `real_name`, `avatar_url`,
  `mobile`, `email`, `gender`, `primary_dept_id`, `status`, `last_login_at`,
  `password_updated_at`, `permission_version`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
) VALUES (
  '019f4d77-0000-7000-8000-000000000003', 'admin',
  '$2b$10$zhW5OzJQR7EeoOqv0OIutOHzGPUajBgDqaPbNhNbukgQogKRMpNYK',
  '管理员', '系统管理员', '', NULL, NULL, 0, @seed_dept_id, 1, NULL,
  @seed_now_ms, 0, @seed_now_ms, @seed_now_ms, '', '', 0, '系统初始化管理员用户'
)
ON DUPLICATE KEY UPDATE
  `password_hash` = '$2b$10$zhW5OzJQR7EeoOqv0OIutOHzGPUajBgDqaPbNhNbukgQogKRMpNYK',
  `nickname` = '管理员',
  `real_name` = '系统管理员',
  `primary_dept_id` = @seed_dept_id,
  `status` = 1,
  `password_updated_at` = @seed_now_ms,
  `updated_at` = @seed_now_ms,
  `deleted` = 0,
  `remark` = '系统初始化管理员用户';

SELECT `user_id` INTO @seed_user_id
FROM `user`
WHERE `username` = 'admin' AND `deleted` = 0
LIMIT 1;

INSERT INTO `user_role` (
  `user_role_id`, `user_id`, `role_id`, `dept_id`, `primary_role`,
  `created_at`, `updated_at`, `created_by`, `updated_by`, `deleted`, `remark`
) VALUES (
  '019f4d77-0000-7000-8000-000000000004', @seed_user_id, @seed_role_id,
  @seed_dept_id, 1, @seed_now_ms, @seed_now_ms, '', '', 0, '系统初始化管理员角色绑定'
)
ON DUPLICATE KEY UPDATE
  `primary_role` = 1,
  `updated_at` = @seed_now_ms,
  `deleted` = 0,
  `remark` = '系统初始化管理员角色绑定';

INSERT INTO `sys_function` (
  `sys_function_id`, `parent_id`, `function_code`, `function_name`, `function_type`,
  `route_path`, `component_path`, `permission_code`, `icon`, `visible`, `cacheable`,
  `external_link`, `sort_order`, `status`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
) VALUES
  ('019f4d77-0000-7000-8000-000000000010', '0', 'dashboard', '首页概览', 'MENU',
   '/dashboard', 'dashboard/overview', 'dashboard:view', 'House', 1, 1,
   NULL, 10, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化首页菜单'),
  ('019f4d77-0000-7000-8000-000000000011', '0', 'ai.chat', 'AI 对话', 'MENU',
   '/ai/chat', 'ai/chat', 'ai:chat:view', 'ChatLineRound', 1, 1,
   NULL, 20, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化 AI 对话菜单'),
  ('019f4d77-0000-7000-8000-000000000012', '0', 'sellersprite.workbench', '卖家精灵工作台', 'MENU',
   '/sellersprite/workbench', 'sellersprite/workbench', 'sellersprite:workbench:view', 'DataBoard', 1, 0,
   NULL, 25, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化卖家精灵工作台菜单'),
  ('019f4d77-0000-7000-8000-000000000013', '0', 'research.market-report', '市场调研报告', 'MENU',
   '/research/market-report', 'research/market-report', 'research:market-report:view', 'DataAnalysis', 1, 0,
   NULL, 27, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化市场调研报告菜单'),
  ('019f4d77-0000-7000-8000-000000000014', '0', 'research.report-history', '我的全部历史报告', 'MENU',
   '/research/report-history', 'research/report-history', 'research:report-history:view', 'Tickets', 1, 0,
   NULL, 28, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化历史报告菜单'),
  ('019f4d77-0000-7000-8000-000000000020', '0', 'system', '系统管理', 'DIR',
   NULL, NULL, NULL, 'SetUp', 1, 0,
   NULL, 30, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化系统管理目录'),
  ('019f4d77-0000-7000-8000-000000000021', '019f4d77-0000-7000-8000-000000000020', 'system.user', '用户管理', 'MENU',
   '/system/users', 'system/users', 'system:user:view', 'User', 1, 1,
   NULL, 10, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化用户菜单'),
  ('019f4d77-0000-7000-8000-000000000022', '019f4d77-0000-7000-8000-000000000020', 'system.dept', '部门管理', 'MENU',
   '/system/departments', 'system/departments', 'system:dept:view', 'OfficeBuilding', 1, 1,
   NULL, 20, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化部门菜单'),
  ('019f4d77-0000-7000-8000-000000000023', '019f4d77-0000-7000-8000-000000000020', 'system.role', '角色管理', 'MENU',
   '/system/roles', 'system/roles', 'system:role:view', 'UserFilled', 1, 1,
   NULL, 30, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化角色菜单'),
  ('019f4d77-0000-7000-8000-000000000024', '019f4d77-0000-7000-8000-000000000020', 'system.dict', '字典管理', 'MENU',
   '/system/dictionaries', 'system/dictionaries', 'system:dict:view', 'Collection', 1, 1,
   NULL, 40, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化字典菜单'),
  ('019f4d77-0000-7000-8000-000000000025', '019f4d77-0000-7000-8000-000000000020', 'system.function', '功能菜单', 'MENU',
   '/system/functions', 'system/functions', 'system:function:view', 'Menu', 1, 1,
   NULL, 50, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化功能菜单'),
  ('019f4d77-0000-7000-8000-000000000026', '019f4d77-0000-7000-8000-000000000020', 'system.api', '接口资源', 'MENU',
   '/system/apis', 'system/apis', 'system:api:view', 'Connection', 1, 1,
   NULL, 60, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化接口资源菜单'),
  ('019f4d77-0000-7000-8000-000000000030', '0', 'ops', '运维管理', 'DIR',
   NULL, NULL, NULL, 'DataAnalysis', 1, 0,
   NULL, 40, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化运维管理目录'),
  ('019f4d77-0000-7000-8000-000000000031', '019f4d77-0000-7000-8000-000000000030', 'ops.cache', '缓存管理', 'MENU',
   '/ops/cache', 'ops/cache', 'ops:cache:view', 'Key', 1, 1,
   NULL, 10, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化缓存菜单'),
  ('019f4d77-0000-7000-8000-000000000032', '019f4d77-0000-7000-8000-000000000030', 'ops.logs', '日志查询', 'MENU',
   '/ops/logs', 'ops/logs', 'ops:logs:view', 'Document', 1, 1,
   NULL, 20, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 0, '系统初始化日志菜单')
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
  `updated_at` = @seed_now_ms,
  `updated_by` = @seed_user_id,
  `deleted` = 0,
  `remark` = VALUES(`remark`);

INSERT INTO `role_function` (
  `role_function_id`, `role_id`, `sys_function_id`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
)
SELECT
  CASE `function_code`
    WHEN 'dashboard' THEN '019f4d77-0000-7000-8000-000000000100'
    WHEN 'ai.chat' THEN '019f4d77-0000-7000-8000-000000000101'
    WHEN 'sellersprite.workbench' THEN '019f4d77-0000-7000-8000-000000000112'
    WHEN 'research.market-report' THEN '019f4d77-0000-7000-8000-000000000113'
    WHEN 'research.report-history' THEN '019f4d77-0000-7000-8000-000000000114'
    WHEN 'system' THEN '019f4d77-0000-7000-8000-000000000102'
    WHEN 'system.user' THEN '019f4d77-0000-7000-8000-000000000103'
    WHEN 'system.dept' THEN '019f4d77-0000-7000-8000-000000000104'
    WHEN 'system.role' THEN '019f4d77-0000-7000-8000-000000000105'
    WHEN 'system.dict' THEN '019f4d77-0000-7000-8000-000000000106'
    WHEN 'system.function' THEN '019f4d77-0000-7000-8000-000000000107'
    WHEN 'system.api' THEN '019f4d77-0000-7000-8000-000000000108'
    WHEN 'ops' THEN '019f4d77-0000-7000-8000-000000000109'
    WHEN 'ops.cache' THEN '019f4d77-0000-7000-8000-000000000110'
    WHEN 'ops.logs' THEN '019f4d77-0000-7000-8000-000000000111'
  END,
  @seed_role_id, `sys_function_id`, @seed_now_ms, @seed_now_ms,
  @seed_user_id, @seed_user_id, 0, '系统初始化管理员功能授权'
FROM `sys_function`
WHERE `function_code` IN (
  'dashboard', 'ai.chat', 'sellersprite.workbench', 'research.market-report', 'research.report-history',
  'system', 'system.user', 'system.dept', 'system.role',
  'system.dict', 'system.function', 'system.api', 'ops', 'ops.cache', 'ops.logs'
)
AND `deleted` = 0
ON DUPLICATE KEY UPDATE
  `role_id` = @seed_role_id,
  `sys_function_id` = VALUES(`sys_function_id`),
  `updated_at` = @seed_now_ms,
  `updated_by` = @seed_user_id,
  `deleted` = 0,
  `remark` = '系统初始化管理员功能授权';

-- 接口目录通常在应用启动后同步；目录尚无 SellerSprite 接口时本语句写入 0 行且不会失败。
-- 完成接口目录同步后可安全重跑本绑定语句，将全部启用的 /api/sellersprite/** 接口关联到工作台。
INSERT INTO `function_api` (
  `function_api_id`, `sys_function_id`, `sys_api_id`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
)
SELECT
  CONCAT('019f4d77-0000-7000-8001-', SUBSTRING(MD5(`api`.`sys_api_id`), 1, 12)),
  `function`.`sys_function_id`, `api`.`sys_api_id`, @seed_now_ms, @seed_now_ms,
  @seed_user_id, @seed_user_id, 0, '系统初始化卖家精灵工作台接口绑定'
FROM `sys_function` AS `function`
JOIN `sys_api` AS `api`
  ON `api`.`path_pattern` LIKE '/api/sellersprite/%'
 AND `api`.`status` = 1
 AND `api`.`deleted` = 0
WHERE `function`.`function_code` = 'sellersprite.workbench'
  AND `function`.`status` = 1
  AND `function`.`deleted` = 0
ON DUPLICATE KEY UPDATE
  `updated_at` = @seed_now_ms,
  `updated_by` = @seed_user_id,
  `deleted` = 0,
  `remark` = '系统初始化卖家精灵工作台接口绑定';

-- 接口目录通常在应用启动后同步；目录尚无目标接口时本语句写入 0 行且不会失败。
-- 完成接口目录同步后可安全重跑本绑定语句，补齐市场调研报告菜单的全部接口绑定。
INSERT INTO `function_api` (
  `function_api_id`, `sys_function_id`, `sys_api_id`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
)
SELECT
  CONCAT('019f4d77-0000-7000-8003-', SUBSTRING(MD5(`api`.`sys_api_id`), 1, 12)),
  `function`.`sys_function_id`, `api`.`sys_api_id`, @seed_now_ms, @seed_now_ms,
  @seed_user_id, @seed_user_id, 0, '系统初始化市场调研报告接口绑定'
FROM `sys_function` AS `function`
JOIN `sys_api` AS `api`
  ON `api`.`path_pattern` LIKE '/api/market-research/%'
 AND `api`.`status` = 1
 AND `api`.`deleted` = 0
WHERE `function`.`function_code` = 'research.market-report'
  AND `function`.`status` = 1
  AND `function`.`deleted` = 0
ON DUPLICATE KEY UPDATE
  `updated_at` = @seed_now_ms,
  `updated_by` = @seed_user_id,
  `deleted` = 0,
  `remark` = '系统初始化市场调研报告接口绑定';

-- 接口目录通常在应用启动后同步；目录尚无目标接口时本语句写入 0 行且不会失败。
-- 完成接口目录同步后可安全重跑本绑定语句，补齐历史报告分页与文件下载接口绑定。
INSERT INTO `function_api` (
  `function_api_id`, `sys_function_id`, `sys_api_id`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
)
SELECT
  CONCAT('019f4d77-0000-7000-8004-', SUBSTRING(MD5(`api`.`sys_api_id`), 1, 12)),
  `function`.`sys_function_id`, `api`.`sys_api_id`, @seed_now_ms, @seed_now_ms,
  @seed_user_id, @seed_user_id, 0, '系统初始化历史报告接口绑定'
FROM `sys_function` AS `function`
JOIN `sys_api` AS `api`
  ON `api`.`http_method` = 'GET'
 AND `api`.`path_pattern` IN (
   '/api/market-research/jobs',
   '/api/market-research/jobs/{jobId}/artifacts/{artifactId}/download'
 )
 AND `api`.`status` = 1
 AND `api`.`deleted` = 0
WHERE `function`.`function_code` = 'research.report-history'
  AND `function`.`status` = 1
  AND `function`.`deleted` = 0
ON DUPLICATE KEY UPDATE
  `updated_at` = @seed_now_ms,
  `updated_by` = @seed_user_id,
  `deleted` = 0,
  `remark` = '系统初始化历史报告接口绑定';

COMMIT;
