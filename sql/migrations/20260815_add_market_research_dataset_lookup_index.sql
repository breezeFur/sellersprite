-- 为已有数据库补充市场调研数据集查询索引；可重复执行。
-- 该索引对应 schema.sql 中的 idx_market_research_dataset_job_code_created。
USE `sellersprite_service`;

SET @index_exists = (
  SELECT COUNT(*)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'market_research_dataset'
    AND index_name = 'idx_market_research_dataset_job_code_created'
);

SET @index_sql = IF(
  @index_exists = 0,
  'ALTER TABLE `market_research_dataset` ADD INDEX `idx_market_research_dataset_job_code_created` (`job_id`, `dataset_code`, `deleted`, `created_at`, `dataset_id`)',
  'SELECT 1'
);

PREPARE index_statement FROM @index_sql;
EXECUTE index_statement;
DEALLOCATE PREPARE index_statement;
