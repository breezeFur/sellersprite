USE `sellersprite_service`;

-- 历史任务无法可靠推断类目和月份，因此新增列保持可空；新任务由应用层强制写入完整上下文。
ALTER TABLE `market_research_job`
  ADD COLUMN `node_id_path` varchar(1024) DEFAULT NULL COMMENT 'SellerSprite类目节点路径' AFTER `marketplace`,
  ADD COLUMN `research_month` char(7) DEFAULT NULL COMMENT '调研月份，yyyy-MM格式' AFTER `node_id_path`,
  MODIFY COLUMN `keyword` varchar(256) DEFAULT NULL COMMENT '可选核心调研关键词';
