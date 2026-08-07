USE `sellersprite_service`;

-- 开发阶段破坏性切换：旧任务与 checkpoint 不进入新采集参数契约。
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
  ADD COLUMN `collection_config` longtext NOT NULL
    COMMENT '采集子图强类型参数JSON快照'
    AFTER `seed_asins`;

ALTER TABLE `market_research_node_execution`
  ADD COLUMN `graph_code` varchar(32) NOT NULL
    COMMENT '所属子图编码：collection evidence report'
    AFTER `job_id`;

ALTER TABLE `market_research_analysis_run`
  DROP COLUMN `auto_start`;

ALTER TABLE `market_research_artifact`
  MODIFY COLUMN `artifact_type` varchar(32) NOT NULL
    COMMENT '产物类型：RAW_DATA_WORKBOOK EVIDENCE_WORKBOOK AI_ANALYSIS_REPORT';

DELETE role_binding
FROM `role_api` AS role_binding
JOIN `sys_api` AS api ON api.`sys_api_id` = role_binding.`sys_api_id`
WHERE api.`http_method` = 'GET'
  AND api.`path_pattern` = '/api/market-research/jobs/{jobId}/download';

DELETE function_binding
FROM `function_api` AS function_binding
JOIN `sys_api` AS api ON api.`sys_api_id` = function_binding.`sys_api_id`
WHERE api.`http_method` = 'GET'
  AND api.`path_pattern` = '/api/market-research/jobs/{jobId}/download';

DELETE FROM `sys_api`
WHERE `http_method` = 'GET'
  AND `path_pattern` = '/api/market-research/jobs/{jobId}/download';
