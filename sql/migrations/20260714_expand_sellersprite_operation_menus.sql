USE `sellersprite_service`;

-- 将单一工作台菜单扩展为业务域目录；保留原工作台路径作为“全部接口”。
START TRANSACTION;

SET @sellersprite_menu_now_ms = CAST(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000 AS UNSIGNED);
SET @sellersprite_menu_operator = (
  SELECT `user_id`
  FROM `user`
  WHERE `username` = 'admin' AND `deleted` = 0
  ORDER BY `created_at`
  LIMIT 1
);
SET @sellersprite_menu_root_id = (
  SELECT `sys_function_id`
  FROM `sys_function`
  WHERE `function_code` = 'sellersprite.workbench' AND `deleted` = 0
  ORDER BY `created_at`
  LIMIT 1
);

UPDATE `sys_function`
SET `function_name` = '卖家精灵',
    `function_type` = 'DIR',
    `route_path` = NULL,
    `component_path` = NULL,
    `permission_code` = NULL,
    `icon` = 'DataBoard',
    `visible` = 1,
    `cacheable` = 0,
    `updated_at` = @sellersprite_menu_now_ms,
    `updated_by` = COALESCE(@sellersprite_menu_operator, ''),
    `deleted` = 0,
    `remark` = 'SellerSprite 业务域菜单目录'
WHERE `sys_function_id` = @sellersprite_menu_root_id;

INSERT INTO `sys_function` (
  `sys_function_id`, `parent_id`, `function_code`, `function_name`, `function_type`,
  `route_path`, `component_path`, `permission_code`, `icon`, `visible`, `cacheable`,
  `external_link`, `sort_order`, `status`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
) VALUES
  ('019f5f00-0000-7000-8000-000000000001', @sellersprite_menu_root_id, 'sellersprite.all', '全部接口', 'MENU', '/sellersprite/workbench', 'sellersprite/workbench', 'sellersprite:all:view', 'Grid', 1, 0, NULL, 10, 1, @sellersprite_menu_now_ms, @sellersprite_menu_now_ms, COALESCE(@sellersprite_menu_operator, ''), COALESCE(@sellersprite_menu_operator, ''), 0, 'SellerSprite 全部 45 个接口'),
  ('019f5f00-0000-7000-8000-000000000002', @sellersprite_menu_root_id, 'sellersprite.account', '账户次数', 'MENU', '/sellersprite/account', 'sellersprite/workbench', 'sellersprite:account:view', 'Wallet', 1, 0, NULL, 20, 1, @sellersprite_menu_now_ms, @sellersprite_menu_now_ms, COALESCE(@sellersprite_menu_operator, ''), COALESCE(@sellersprite_menu_operator, ''), 0, 'SellerSprite 账户接口'),
  ('019f5f00-0000-7000-8000-000000000003', @sellersprite_menu_root_id, 'sellersprite.product', '产品分析', 'MENU', '/sellersprite/product', 'sellersprite/workbench', 'sellersprite:product:view', 'Goods', 1, 0, NULL, 30, 1, @sellersprite_menu_now_ms, @sellersprite_menu_now_ms, COALESCE(@sellersprite_menu_operator, ''), COALESCE(@sellersprite_menu_operator, ''), 0, 'SellerSprite 产品分析接口'),
  ('019f5f00-0000-7000-8000-000000000004', @sellersprite_menu_root_id, 'sellersprite.asin', 'ASIN 分析', 'MENU', '/sellersprite/asin', 'sellersprite/workbench', 'sellersprite:asin:view', 'Box', 1, 0, NULL, 40, 1, @sellersprite_menu_now_ms, @sellersprite_menu_now_ms, COALESCE(@sellersprite_menu_operator, ''), COALESCE(@sellersprite_menu_operator, ''), 0, 'SellerSprite ASIN 分析接口'),
  ('019f5f00-0000-7000-8000-000000000005', @sellersprite_menu_root_id, 'sellersprite.keyword', '关键词研究', 'MENU', '/sellersprite/keyword', 'sellersprite/workbench', 'sellersprite:keyword:view', 'Search', 1, 0, NULL, 50, 1, @sellersprite_menu_now_ms, @sellersprite_menu_now_ms, COALESCE(@sellersprite_menu_operator, ''), COALESCE(@sellersprite_menu_operator, ''), 0, 'SellerSprite 关键词研究接口'),
  ('019f5f00-0000-7000-8000-000000000006', @sellersprite_menu_root_id, 'sellersprite.traffic', '流量分析', 'MENU', '/sellersprite/traffic', 'sellersprite/workbench', 'sellersprite:traffic:view', 'Connection', 1, 0, NULL, 60, 1, @sellersprite_menu_now_ms, @sellersprite_menu_now_ms, COALESCE(@sellersprite_menu_operator, ''), COALESCE(@sellersprite_menu_operator, ''), 0, 'SellerSprite 流量分析接口'),
  ('019f5f00-0000-7000-8000-000000000007', @sellersprite_menu_root_id, 'sellersprite.market', '市场分析', 'MENU', '/sellersprite/market', 'sellersprite/workbench', 'sellersprite:market:view', 'TrendCharts', 1, 0, NULL, 70, 1, @sellersprite_menu_now_ms, @sellersprite_menu_now_ms, COALESCE(@sellersprite_menu_operator, ''), COALESCE(@sellersprite_menu_operator, ''), 0, 'SellerSprite 市场分析接口'),
  ('019f5f00-0000-7000-8000-000000000008', @sellersprite_menu_root_id, 'sellersprite.review', '评论分析', 'MENU', '/sellersprite/review', 'sellersprite/workbench', 'sellersprite:review:view', 'ChatLineSquare', 1, 0, NULL, 80, 1, @sellersprite_menu_now_ms, @sellersprite_menu_now_ms, COALESCE(@sellersprite_menu_operator, ''), COALESCE(@sellersprite_menu_operator, ''), 0, 'SellerSprite 评论分析接口'),
  ('019f5f00-0000-7000-8000-000000000009', @sellersprite_menu_root_id, 'sellersprite.trademark', '全球商标', 'MENU', '/sellersprite/trademark', 'sellersprite/workbench', 'sellersprite:trademark:view', 'CollectionTag', 1, 0, NULL, 90, 1, @sellersprite_menu_now_ms, @sellersprite_menu_now_ms, COALESCE(@sellersprite_menu_operator, ''), COALESCE(@sellersprite_menu_operator, ''), 0, 'SellerSprite 全球商标接口'),
  ('019f5f00-0000-7000-8000-000000000010', @sellersprite_menu_root_id, 'sellersprite.tool', '数据工具', 'MENU', '/sellersprite/tool', 'sellersprite/workbench', 'sellersprite:tool:view', 'Tools', 1, 0, NULL, 100, 1, @sellersprite_menu_now_ms, @sellersprite_menu_now_ms, COALESCE(@sellersprite_menu_operator, ''), COALESCE(@sellersprite_menu_operator, ''), 0, 'SellerSprite 数据工具接口')
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
  `sort_order` = VALUES(`sort_order`),
  `status` = VALUES(`status`),
  `updated_at` = @sellersprite_menu_now_ms,
  `updated_by` = COALESCE(@sellersprite_menu_operator, ''),
  `deleted` = 0,
  `remark` = VALUES(`remark`);

-- 原来拥有工作台权限的角色自动获得全部业务域子菜单。
INSERT INTO `role_function` (
  `role_function_id`, `role_id`, `sys_function_id`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
)
SELECT
  CONCAT('019f5f00-0000-7001-8000-', SUBSTRING(MD5(CONCAT(`grant`.`role_id`, `child`.`sys_function_id`)), 1, 12)),
  `grant`.`role_id`, `child`.`sys_function_id`, @sellersprite_menu_now_ms, @sellersprite_menu_now_ms,
  COALESCE(@sellersprite_menu_operator, ''), COALESCE(@sellersprite_menu_operator, ''), 0,
  '从原 SellerSprite 工作台权限迁移到业务域菜单'
FROM `role_function` AS `grant`
JOIN `sys_function` AS `child`
  ON `child`.`parent_id` = @sellersprite_menu_root_id
 AND `child`.`function_code` LIKE 'sellersprite.%'
 AND `child`.`deleted` = 0
WHERE `grant`.`sys_function_id` = @sellersprite_menu_root_id
  AND `grant`.`deleted` = 0
ON DUPLICATE KEY UPDATE
  `role_id` = VALUES(`role_id`),
  `sys_function_id` = VALUES(`sys_function_id`),
  `updated_at` = @sellersprite_menu_now_ms,
  `updated_by` = COALESCE(@sellersprite_menu_operator, ''),
  `deleted` = 0,
  `remark` = VALUES(`remark`);

-- 目录本身不再绑定业务接口，接口按业务域绑定到对应子菜单。
UPDATE `function_api`
SET `deleted` = 1,
    `updated_at` = @sellersprite_menu_now_ms,
    `updated_by` = COALESCE(@sellersprite_menu_operator, '')
WHERE `sys_function_id` = @sellersprite_menu_root_id
  AND `deleted` = 0;

INSERT INTO `function_api` (
  `function_api_id`, `sys_function_id`, `sys_api_id`, `created_at`, `updated_at`,
  `created_by`, `updated_by`, `deleted`, `remark`
)
SELECT
  CONCAT('019f5f00-0000-7002-8000-', SUBSTRING(MD5(CONCAT(`child`.`sys_function_id`, `api`.`sys_api_id`)), 1, 12)),
  `child`.`sys_function_id`, `api`.`sys_api_id`, @sellersprite_menu_now_ms, @sellersprite_menu_now_ms,
  COALESCE(@sellersprite_menu_operator, ''), COALESCE(@sellersprite_menu_operator, ''), 0,
  'SellerSprite 业务域菜单接口绑定'
FROM `sys_function` AS `child`
JOIN `sys_api` AS `api`
  ON `api`.`status` = 1
 AND `api`.`deleted` = 0
 AND (
   (`child`.`function_code` = 'sellersprite.all' AND `api`.`path_pattern` LIKE '/api/sellersprite/%')
   OR (`child`.`function_code` = 'sellersprite.account' AND `api`.`path_pattern` LIKE '/api/sellersprite/account/%')
   OR (`child`.`function_code` = 'sellersprite.product' AND `api`.`path_pattern` LIKE '/api/sellersprite/products/%')
   OR (`child`.`function_code` = 'sellersprite.asin' AND `api`.`path_pattern` LIKE '/api/sellersprite/asins/%')
   OR (`child`.`function_code` = 'sellersprite.keyword' AND `api`.`path_pattern` LIKE '/api/sellersprite/keywords/%')
   OR (`child`.`function_code` = 'sellersprite.traffic' AND `api`.`path_pattern` LIKE '/api/sellersprite/traffic/%')
   OR (`child`.`function_code` = 'sellersprite.market' AND `api`.`path_pattern` LIKE '/api/sellersprite/markets/%')
   OR (`child`.`function_code` = 'sellersprite.review' AND `api`.`path_pattern` LIKE '/api/sellersprite/reviews/%')
   OR (`child`.`function_code` = 'sellersprite.trademark' AND `api`.`path_pattern` LIKE '/api/sellersprite/trademarks/%')
   OR (`child`.`function_code` = 'sellersprite.tool' AND `api`.`path_pattern` LIKE '/api/sellersprite/tools/%')
 )
WHERE `child`.`parent_id` = @sellersprite_menu_root_id
  AND `child`.`deleted` = 0
ON DUPLICATE KEY UPDATE
  `updated_at` = @sellersprite_menu_now_ms,
  `updated_by` = COALESCE(@sellersprite_menu_operator, ''),
  `deleted` = 0,
  `remark` = VALUES(`remark`);

COMMIT;

SELECT `function_code`, `function_name`, `route_path`, `permission_code`
FROM `sys_function`
WHERE `parent_id` = @sellersprite_menu_root_id AND `deleted` = 0
ORDER BY `sort_order`, `function_code`;
