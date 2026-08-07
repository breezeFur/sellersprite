USE `sellersprite_service`;

-- 为已部署的 market-research-v3 Graph 租约增加 fencing token。
SET @research_job_token_column_missing = (
  SELECT COUNT(*) = 0
  FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'market_research_job'
    AND `COLUMN_NAME` = 'execution_token'
);
SET @research_job_ddl = IF(
  @research_job_token_column_missing,
  'ALTER TABLE `market_research_job` ADD COLUMN `execution_token` char(36) DEFAULT NULL COMMENT ''当前任务领取令牌，租约转移时更新'' AFTER `execution_owner`',
  'SELECT 1'
);
PREPARE research_job_statement FROM @research_job_ddl;
EXECUTE research_job_statement;
DEALLOCATE PREPARE research_job_statement;

SET @research_job_token_index_missing = (
  SELECT COUNT(*) = 0
  FROM `information_schema`.`STATISTICS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'market_research_job'
    AND `INDEX_NAME` = 'idx_market_research_job_token_lease'
);
SET @research_job_ddl = IF(
  @research_job_token_index_missing,
  'ALTER TABLE `market_research_job` ADD KEY `idx_market_research_job_token_lease` (`execution_token`, `lease_until`)',
  'SELECT 1'
);
PREPARE research_job_statement FROM @research_job_ddl;
EXECUTE research_job_statement;
DEALLOCATE PREPARE research_job_statement;

CREATE TABLE IF NOT EXISTS `market_research_analysis_run` (
  `analysis_run_id` char(36) NOT NULL COMMENT '分析运行ID，UUIDv7',
  `job_id` char(36) NOT NULL COMMENT '市场调研任务ID',
  `user_id` char(36) NOT NULL COMMENT '分析运行归属用户ID',
  `conversation_id` char(36) DEFAULT NULL COMMENT '关联AI会话ID',
  `parent_run_id` char(36) DEFAULT NULL COMMENT '重试或追问来源分析运行ID',
  `run_type` varchar(32) NOT NULL COMMENT '分析类型：INITIAL RETRY FOLLOW_UP',
  `analysis_goal` longtext NOT NULL COMMENT '本次分析目标或后续问题',
  `auto_start` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '数据完成后是否自动排队：1是 0否',
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

-- 兼容本迁移早期版本已创建 analysis_run、但尚未包含自动启动开关的环境。
SET @analysis_auto_start_column_missing = (
  SELECT COUNT(*) = 0
  FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'market_research_analysis_run'
    AND `COLUMN_NAME` = 'auto_start'
);
SET @analysis_run_ddl = IF(
  @analysis_auto_start_column_missing,
  'ALTER TABLE `market_research_analysis_run` ADD COLUMN `auto_start` tinyint unsigned NOT NULL DEFAULT 1 COMMENT ''数据完成后是否自动排队：1是 0否'' AFTER `analysis_goal`',
  'SELECT 1'
);
PREPARE analysis_run_statement FROM @analysis_run_ddl;
EXECUTE analysis_run_statement;
DEALLOCATE PREPARE analysis_run_statement;

-- 旧环境已存在 artifact 表，下面逐项判断后再补列、索引和外键，脚本可重复执行。
SET @analysis_run_column_missing = (
  SELECT COUNT(*) = 0
  FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'market_research_artifact'
    AND `COLUMN_NAME` = 'analysis_run_id'
);
SET @artifact_ddl = IF(
  @analysis_run_column_missing,
  'ALTER TABLE `market_research_artifact` ADD COLUMN `analysis_run_id` char(36) DEFAULT NULL COMMENT ''可选关联分析运行ID，Excel证据产物为空'' AFTER `job_id`',
  'SELECT 1'
);
PREPARE artifact_statement FROM @artifact_ddl;
EXECUTE artifact_statement;
DEALLOCATE PREPARE artifact_statement;

SET @artifact_scope_column_missing = (
  SELECT COUNT(*) = 0
  FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'market_research_artifact'
    AND `COLUMN_NAME` = 'artifact_scope_id'
);
SET @artifact_ddl = IF(
  @artifact_scope_column_missing,
  'ALTER TABLE `market_research_artifact` ADD COLUMN `artifact_scope_id` char(36) DEFAULT NULL COMMENT ''产物唯一作用域，数据报告使用任务ID，AI报告使用分析运行ID'' AFTER `analysis_run_id`',
  'SELECT 1'
);
PREPARE artifact_statement FROM @artifact_ddl;
EXECUTE artifact_statement;
DEALLOCATE PREPARE artifact_statement;

UPDATE `market_research_artifact`
SET `artifact_scope_id` = COALESCE(`analysis_run_id`, `job_id`)
WHERE `artifact_scope_id` IS NULL OR `artifact_scope_id` = '';

SET @artifact_scope_column_nullable = (
  SELECT COUNT(*) > 0
  FROM `information_schema`.`COLUMNS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'market_research_artifact'
    AND `COLUMN_NAME` = 'artifact_scope_id'
    AND `IS_NULLABLE` = 'YES'
);
SET @artifact_ddl = IF(
  @artifact_scope_column_nullable,
  'ALTER TABLE `market_research_artifact` MODIFY COLUMN `artifact_scope_id` char(36) NOT NULL COMMENT ''产物唯一作用域，数据报告使用任务ID，AI报告使用分析运行ID''',
  'SELECT 1'
);
PREPARE artifact_statement FROM @artifact_ddl;
EXECUTE artifact_statement;
DEALLOCATE PREPARE artifact_statement;

SET @artifact_job_index_missing = (
  SELECT COUNT(*) = 0
  FROM `information_schema`.`STATISTICS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'market_research_artifact'
    AND `INDEX_NAME` = 'idx_market_research_artifact_job'
);
SET @artifact_ddl = IF(
  @artifact_job_index_missing,
  'ALTER TABLE `market_research_artifact` ADD KEY `idx_market_research_artifact_job` (`job_id`)',
  'SELECT 1'
);
PREPARE artifact_statement FROM @artifact_ddl;
EXECUTE artifact_statement;
DEALLOCATE PREPARE artifact_statement;

SET @legacy_artifact_unique_exists = (
  SELECT COUNT(*) > 0
  FROM `information_schema`.`STATISTICS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'market_research_artifact'
    AND `INDEX_NAME` = 'uk_market_research_artifact_job_type'
);
SET @artifact_ddl = IF(
  @legacy_artifact_unique_exists,
  'ALTER TABLE `market_research_artifact` DROP INDEX `uk_market_research_artifact_job_type`',
  'SELECT 1'
);
PREPARE artifact_statement FROM @artifact_ddl;
EXECUTE artifact_statement;
DEALLOCATE PREPARE artifact_statement;

SET @artifact_scope_unique_missing = (
  SELECT COUNT(*) = 0
  FROM `information_schema`.`STATISTICS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'market_research_artifact'
    AND `INDEX_NAME` = 'uk_market_research_artifact_scope'
);
SET @artifact_ddl = IF(
  @artifact_scope_unique_missing,
  'ALTER TABLE `market_research_artifact` ADD UNIQUE KEY `uk_market_research_artifact_scope` (`job_id`, `artifact_type`, `artifact_scope_id`, `deleted`)',
  'SELECT 1'
);
PREPARE artifact_statement FROM @artifact_ddl;
EXECUTE artifact_statement;
DEALLOCATE PREPARE artifact_statement;

SET @artifact_analysis_index_missing = (
  SELECT COUNT(*) = 0
  FROM `information_schema`.`STATISTICS`
  WHERE `TABLE_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'market_research_artifact'
    AND `INDEX_NAME` = 'idx_market_research_artifact_analysis_run'
);
SET @artifact_ddl = IF(
  @artifact_analysis_index_missing,
  'ALTER TABLE `market_research_artifact` ADD KEY `idx_market_research_artifact_analysis_run` (`analysis_run_id`)',
  'SELECT 1'
);
PREPARE artifact_statement FROM @artifact_ddl;
EXECUTE artifact_statement;
DEALLOCATE PREPARE artifact_statement;

SET @artifact_analysis_fk_missing = (
  SELECT COUNT(*) = 0
  FROM `information_schema`.`REFERENTIAL_CONSTRAINTS`
  WHERE `CONSTRAINT_SCHEMA` = DATABASE()
    AND `TABLE_NAME` = 'market_research_artifact'
    AND `CONSTRAINT_NAME` = 'fk_market_research_artifact_analysis_run'
);
SET @artifact_ddl = IF(
  @artifact_analysis_fk_missing,
  'ALTER TABLE `market_research_artifact` ADD CONSTRAINT `fk_market_research_artifact_analysis_run` FOREIGN KEY (`analysis_run_id`) REFERENCES `market_research_analysis_run` (`analysis_run_id`) ON DELETE CASCADE',
  'SELECT 1'
);
PREPARE artifact_statement FROM @artifact_ddl;
EXECUTE artifact_statement;
DEALLOCATE PREPARE artifact_statement;

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
