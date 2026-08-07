USE `sellersprite_service`;

-- 破坏性重建：旧市场调研任务、快照、产物和Graph checkpoint均不迁移。
DROP TABLE IF EXISTS `GRAPH_CHECKPOINT`;
DROP TABLE IF EXISTS `GRAPH_THREAD`;
DROP TABLE IF EXISTS `market_research_node_execution`;
DROP TABLE IF EXISTS `market_research_dataset`;
DROP TABLE IF EXISTS `market_research_artifact`;
DROP TABLE IF EXISTS `market_research_snapshot`;
DROP TABLE IF EXISTS `market_research_job`;

CREATE TABLE `market_research_job` (
  `job_id` char(36) NOT NULL COMMENT '市场调研任务ID，UUIDv7',
  `user_id` char(36) NOT NULL COMMENT '任务归属用户ID',
  `report_name` varchar(128) NOT NULL COMMENT '报告名称',
  `marketplace` varchar(16) NOT NULL DEFAULT 'US' COMMENT 'Amazon站点，第一版固定US',
  `keyword` varchar(256) NOT NULL COMMENT '核心调研关键词',
  `seed_asins` longtext DEFAULT NULL COMMENT '可选种子ASIN数组JSON，最多20个',
  `template_code` varchar(64) NOT NULL DEFAULT 'market-research-v1' COMMENT 'Excel模板代码',
  `data_source_mode` varchar(16) NOT NULL DEFAULT 'MOCK' COMMENT '数据源模式：MOCK REMOTE',
  `workflow_version` varchar(64) NOT NULL COMMENT '固定Java Graph工作流版本',
  `job_status` varchar(32) NOT NULL DEFAULT 'QUEUED' COMMENT '任务状态：QUEUED RUNNING RETRY_WAIT SUCCEEDED FAILED CANCELLED',
  `current_node` varchar(64) NOT NULL DEFAULT 'validate' COMMENT '当前Graph节点编码',
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

CREATE TABLE `market_research_node_execution` (
  `execution_id` char(36) NOT NULL COMMENT '节点执行记录ID，UUIDv7',
  `job_id` char(36) NOT NULL COMMENT '市场调研任务ID',
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

CREATE TABLE `market_research_dataset` (
  `dataset_id` char(36) NOT NULL COMMENT '不可变数据集ID，UUIDv7',
  `job_id` char(36) NOT NULL COMMENT '市场调研任务ID',
  `node_code` varchar(64) NOT NULL COMMENT '产生数据集的Graph节点编码',
  `operation` varchar(64) NOT NULL COMMENT '外部采集操作编码',
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

CREATE TABLE `market_research_artifact` (
  `artifact_id` char(36) NOT NULL COMMENT '报告产物ID，UUIDv7',
  `job_id` char(36) NOT NULL COMMENT '市场调研任务ID',
  `workflow_version` varchar(64) NOT NULL COMMENT '生成产物的工作流版本',
  `artifact_type` varchar(32) NOT NULL DEFAULT 'EXCEL_REPORT' COMMENT '产物类型',
  `file_name` varchar(255) NOT NULL COMMENT '下载文件名',
  `storage_key` varchar(1024) NOT NULL COMMENT '受控存储键，不是公开URL',
  `media_type` varchar(128) NOT NULL COMMENT '文件媒体类型',
  `file_size` bigint unsigned NOT NULL DEFAULT 0 COMMENT '文件大小，字节',
  `sha256` char(64) DEFAULT NULL COMMENT '文件SHA-256十六进制摘要',
  `artifact_status` varchar(32) NOT NULL DEFAULT 'GENERATING' COMMENT '产物状态：GENERATING PUBLISHED FAILED',
  `published_at` bigint unsigned DEFAULT NULL COMMENT '发布时间，Unix毫秒',
  `created_at` bigint unsigned NOT NULL COMMENT '创建时间，Unix毫秒',
  `updated_at` bigint unsigned NOT NULL COMMENT '更新时间，Unix毫秒',
  `created_by` char(36) NOT NULL DEFAULT '' COMMENT '创建人ID',
  `updated_by` char(36) NOT NULL DEFAULT '' COMMENT '更新人ID',
  `deleted` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1删除',
  `remark` varchar(512) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`artifact_id`),
  UNIQUE KEY `uk_market_research_artifact_job_type` (`job_id`, `artifact_type`, `deleted`),
  KEY `idx_market_research_artifact_status` (`artifact_status`, `updated_at`),
  KEY `idx_market_research_artifact_sha256` (`sha256`),
  CONSTRAINT `fk_market_research_artifact_job` FOREIGN KEY (`job_id`)
    REFERENCES `market_research_job` (`job_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='市场调研产物表';

-- Spring AI Alibaba Graph 2.0.0-M1.1 MysqlSaver 官方表结构。
CREATE TABLE `GRAPH_THREAD` (
  `thread_id` varchar(36) NOT NULL,
  `thread_name` varchar(255) DEFAULT NULL COMMENT '市场调研使用jobId',
  `is_released` boolean NOT NULL DEFAULT false,
  PRIMARY KEY (`thread_id`),
  UNIQUE KEY `IDX_GRAPH_THREAD_NAME_RELEASED` (`thread_name`, `is_released`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Graph执行线程表';

CREATE TABLE `GRAPH_CHECKPOINT` (
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

-- 接口目录同步后可安全重跑，绑定新增的节点、取消、重试与拓扑接口。
START TRANSACTION;
SET @research_runtime_now_ms = CAST(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000 AS UNSIGNED);
SET @research_runtime_user_id = COALESCE((
  SELECT `user_id` FROM `user`
  WHERE `username` = 'admin' AND `deleted` = 0
  ORDER BY `created_at` LIMIT 1
), '');

INSERT INTO `function_api` (
  `function_api_id`, `sys_function_id`, `sys_api_id`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
)
SELECT
  CONCAT('019f53a0-0000-7000-8005-', SUBSTRING(MD5(`api`.`sys_api_id`), 1, 12)),
  `function`.`sys_function_id`, `api`.`sys_api_id`,
  @research_runtime_now_ms, @research_runtime_now_ms,
  @research_runtime_user_id, @research_runtime_user_id, 0,
  '系统绑定市场调研Graph运行时接口'
FROM `sys_function` AS `function`
JOIN `sys_api` AS `api`
  ON `api`.`path_pattern` LIKE '/api/market-research/%'
 AND `api`.`status` = 1
 AND `api`.`deleted` = 0
WHERE `function`.`function_code` = 'research.market-report'
  AND `function`.`status` = 1
  AND `function`.`deleted` = 0
ON DUPLICATE KEY UPDATE
  `updated_at` = @research_runtime_now_ms,
  `updated_by` = @research_runtime_user_id,
  `deleted` = 0,
  `remark` = '系统绑定市场调研Graph运行时接口';
COMMIT;
