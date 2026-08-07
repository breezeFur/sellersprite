USE `sellersprite_service`;

-- 开发阶段破坏性升级：旧任务、事件和 Graph checkpoint 不兼容 v5 人工关卡。
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `market_research_event`;
TRUNCATE TABLE `market_research_event_stream_lock`;
TRUNCATE TABLE `market_research_artifact`;
TRUNCATE TABLE `market_research_analysis_run`;
TRUNCATE TABLE `market_research_dataset`;
TRUNCATE TABLE `market_research_node_execution`;
TRUNCATE TABLE `market_research_job`;
DELETE cp
FROM `GRAPH_CHECKPOINT` AS cp
JOIN `GRAPH_THREAD` AS gt ON gt.`thread_id` = cp.`thread_id`
WHERE gt.`thread_name` LIKE 'market-research-%';
DELETE FROM `GRAPH_THREAD`
WHERE `thread_name` LIKE 'market-research-%';
SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE `market_research_job`
  ADD COLUMN `current_stage` varchar(32) NOT NULL DEFAULT 'SCREENING'
    COMMENT '当前业务阶段：SCREENING DEEP_DIVE FINAL_ANALYSIS'
    AFTER `current_node`,
  ADD COLUMN `waiting_input_type` varchar(32) DEFAULT NULL
    COMMENT '当前等待的人工输入类型'
    AFTER `current_stage`;

CREATE TABLE `market_research_stage_input` (
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
