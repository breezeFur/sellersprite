-- 为已有数据库补充 SellerSprite 官方接口参数字典；可重复执行。
USE `sellersprite_service`;

START TRANSACTION;

SET @seed_now_ms = CAST(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000 AS UNSIGNED);
SET @seed_user_id = COALESCE((
  SELECT `user_id` FROM `user`
  WHERE `username` = 'admin' AND `deleted` = 0
  LIMIT 1
), '');

-- 初始化 SellerSprite 官方接口参数字典。dict_label 是前后端稳定传输标识，dict_value 是上游接口值。
INSERT INTO `dict_type` (
  `dict_type`, `dict_type_name`, `system_builtin`, `sort_order`, `status`,
  `created_at`, `updated_at`, `created_by`, `updated_by`, `remark`
) VALUES
  ('MARKET', '市场站点', 1, 10, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('LISTING_DATE', '上架时间', 1, 20, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('PRODUCT_SIZE_US', '美国站商品尺寸', 1, 30, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('PRODUCT_SIZE_JP', '日本站商品尺寸', 1, 40, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('PRODUCT_SIZE_CA', '加拿大站商品尺寸', 1, 50, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('PRODUCT_SIZE_EU', '欧洲站商品尺寸', 1, 60, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('SELLER_NATIONALITY', '卖家国家/地区', 1, 70, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('PRODUCT_SORT_FIELD', '商品排序字段', 1, 80, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('MARKET_PERIOD', '市场周期', 1, 90, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('KEYWORD_RESEARCH_SORT_FIELD', '关键词选品排序字段', 1, 100, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('KEYWORD_RESEARCH_TREND_SORT_FIELD', '关键词选品趋势排序字段', 1, 110, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('REVERSE_ASIN_EXPOSURE_POSITION', '反查曝光位置', 1, 120, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('REVERSE_ASIN_SHARE_TYPE', '反查流量占比类型', 1, 130, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('REVERSE_ASIN_CONVERSION_TYPE', '反查转化类型', 1, 140, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('RELATED_PRODUCT_ASSOCIATION_TYPE', '关联商品类型', 1, 150, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('REVERSE_ASIN_SORT_FIELD', '反查关键词排序字段', 1, 160, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('ABA_SORT_FIELD', 'ABA/关键词流向排序字段', 1, 170, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('REVERSE_MULTIPLE_ASIN_SORT_FIELD', '多ASIN拓展排序字段', 1, 180, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('KEYWORD_EXPLORER_SORT_FIELD', '出单词反查排序字段', 1, 190, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'),
  ('PRODUCT_WEIGHT_UNIT', '商品重量单位', 1, 200, 1, @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典')
ON DUPLICATE KEY UPDATE
  `dict_type_name` = VALUES(`dict_type_name`),
  `system_builtin` = 1,
  `sort_order` = VALUES(`sort_order`),
  `status` = 1,
  `updated_at` = @seed_now_ms,
  `updated_by` = @seed_user_id,
  `remark` = 'SellerSprite 官方接口参数字典';

INSERT INTO `dict_data` (
  `dict_data_id`, `dict_type`, `dict_value`, `dict_label`, `dict_name`,
  `css_class`, `color`, `system_builtin`, `default_flag`, `sort_order`, `status`,
  `created_at`, `updated_at`, `created_by`, `updated_by`, `remark`
)
SELECT
  CONCAT('019f4d77-0000-7000-8002-', SUBSTRING(MD5(CONCAT('sellersprite.dict.', `seed`.`dict_label`)), 1, 12)),
  `seed`.`dict_type`, `seed`.`dict_value`, `seed`.`dict_label`, `seed`.`dict_name`,
  '', '', 1, `seed`.`default_flag`, `seed`.`sort_order`, 1,
  @seed_now_ms, @seed_now_ms, @seed_user_id, @seed_user_id, 'SellerSprite 官方接口参数字典'
FROM (
  SELECT 'MARKET' AS `dict_type`, 'US' AS `dict_value`, 'MARKET_US' AS `dict_label`, '美国站' AS `dict_name`, 10 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'MARKET' AS `dict_type`, 'JP' AS `dict_value`, 'MARKET_JP' AS `dict_label`, '日本站' AS `dict_name`, 20 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET' AS `dict_type`, 'UK' AS `dict_value`, 'MARKET_UK' AS `dict_label`, '英国站' AS `dict_name`, 30 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET' AS `dict_type`, 'DE' AS `dict_value`, 'MARKET_DE' AS `dict_label`, '德国站' AS `dict_name`, 40 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET' AS `dict_type`, 'FR' AS `dict_value`, 'MARKET_FR' AS `dict_label`, '法国站' AS `dict_name`, 50 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET' AS `dict_type`, 'IT' AS `dict_value`, 'MARKET_IT' AS `dict_label`, '意大利站' AS `dict_name`, 60 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET' AS `dict_type`, 'ES' AS `dict_value`, 'MARKET_ES' AS `dict_label`, '西班牙站' AS `dict_name`, 70 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET' AS `dict_type`, 'CA' AS `dict_value`, 'MARKET_CA' AS `dict_label`, '加拿大站' AS `dict_name`, 80 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET' AS `dict_type`, 'IN' AS `dict_value`, 'MARKET_IN' AS `dict_label`, '印度站' AS `dict_name`, 90 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'LISTING_DATE' AS `dict_type`, NULL AS `dict_value`, 'LISTING_DATE_NULL' AS `dict_label`, '不限' AS `dict_name`, 100 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'LISTING_DATE' AS `dict_type`, '1' AS `dict_value`, 'LISTING_DATE_1' AS `dict_label`, '近1个月' AS `dict_name`, 110 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'LISTING_DATE' AS `dict_type`, '3' AS `dict_value`, 'LISTING_DATE_3' AS `dict_label`, '近3个月' AS `dict_name`, 120 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'LISTING_DATE' AS `dict_type`, '6' AS `dict_value`, 'LISTING_DATE_6' AS `dict_label`, '近6个月' AS `dict_name`, 130 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'LISTING_DATE' AS `dict_type`, '12' AS `dict_value`, 'LISTING_DATE_12' AS `dict_label`, '近1年' AS `dict_name`, 140 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'LISTING_DATE' AS `dict_type`, '24' AS `dict_value`, 'LISTING_DATE_24' AS `dict_label`, '近2年' AS `dict_name`, 150 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_US' AS `dict_type`, 'ST/SS' AS `dict_value`, 'PRODUCT_SIZE_US_ST_SS' AS `dict_label`, '小号标准尺寸' AS `dict_name`, 160 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_US' AS `dict_type`, 'LS' AS `dict_value`, 'PRODUCT_SIZE_US_LS' AS `dict_label`, '大号标准尺寸' AS `dict_name`, 170 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_US' AS `dict_type`, 'SO' AS `dict_value`, 'PRODUCT_SIZE_US_SO' AS `dict_label`, '小号超大尺寸' AS `dict_name`, 180 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_US' AS `dict_type`, 'MO' AS `dict_value`, 'PRODUCT_SIZE_US_MO' AS `dict_label`, '中号超大尺寸' AS `dict_name`, 190 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_US' AS `dict_type`, 'LO/LB' AS `dict_value`, 'PRODUCT_SIZE_US_LO_LB' AS `dict_label`, '大号超大尺寸' AS `dict_name`, 200 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_US' AS `dict_type`, 'SP' AS `dict_value`, 'PRODUCT_SIZE_US_SP' AS `dict_label`, '特殊超大尺寸' AS `dict_name`, 210 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_US' AS `dict_type`, 'O' AS `dict_value`, 'PRODUCT_SIZE_US_O' AS `dict_label`, '其他尺寸' AS `dict_name`, 220 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_US' AS `dict_type`, 'ELO' AS `dict_value`, 'PRODUCT_SIZE_US_ELO' AS `dict_label`, '超大尺寸' AS `dict_name`, 230 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_US' AS `dict_type`, 'EL5O' AS `dict_value`, 'PRODUCT_SIZE_US_EL5O' AS `dict_label`, '超大尺寸5磅' AS `dict_name`, 240 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_US' AS `dict_type`, 'EL7O' AS `dict_value`, 'PRODUCT_SIZE_US_EL7O' AS `dict_label`, '超大尺寸7磅' AS `dict_name`, 250 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_US' AS `dict_type`, 'EL15O' AS `dict_value`, 'PRODUCT_SIZE_US_EL15O' AS `dict_label`, '超大尺寸15磅' AS `dict_name`, 260 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_JP' AS `dict_type`, 'SM' AS `dict_value`, 'PRODUCT_SIZE_JP_SM' AS `dict_label`, '小号' AS `dict_name`, 270 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_JP' AS `dict_type`, 'ST' AS `dict_value`, 'PRODUCT_SIZE_JP_ST' AS `dict_label`, '标准' AS `dict_name`, 280 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_JP' AS `dict_type`, 'OV' AS `dict_value`, 'PRODUCT_SIZE_JP_OV' AS `dict_label`, '超大' AS `dict_name`, 290 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_JP' AS `dict_type`, 'SS' AS `dict_value`, 'PRODUCT_SIZE_JP_SS' AS `dict_label`, '特大' AS `dict_name`, 300 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_JP' AS `dict_type`, 'O' AS `dict_value`, 'PRODUCT_SIZE_JP_O' AS `dict_label`, '其他' AS `dict_name`, 310 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_CA' AS `dict_type`, 'EN' AS `dict_value`, 'PRODUCT_SIZE_CA_EN' AS `dict_label`, '信封' AS `dict_name`, 320 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_CA' AS `dict_type`, 'ST' AS `dict_value`, 'PRODUCT_SIZE_CA_ST' AS `dict_label`, '标准' AS `dict_name`, 330 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_CA' AS `dict_type`, 'OS' AS `dict_value`, 'PRODUCT_SIZE_CA_OS' AS `dict_label`, '超大' AS `dict_name`, 340 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_CA' AS `dict_type`, 'O' AS `dict_value`, 'PRODUCT_SIZE_CA_O' AS `dict_label`, '其他' AS `dict_name`, 350 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_EU' AS `dict_type`, 'SL' AS `dict_value`, 'PRODUCT_SIZE_EU_SL' AS `dict_label`, '小号信封' AS `dict_name`, 360 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_EU' AS `dict_type`, 'NL' AS `dict_value`, 'PRODUCT_SIZE_EU_NL' AS `dict_label`, '标准信封' AS `dict_name`, 370 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_EU' AS `dict_type`, 'LL' AS `dict_value`, 'PRODUCT_SIZE_EU_LL' AS `dict_label`, '大号信封' AS `dict_name`, 380 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_EU' AS `dict_type`, 'SD' AS `dict_value`, 'PRODUCT_SIZE_EU_SD' AS `dict_label`, '标准包裹' AS `dict_name`, 390 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_EU' AS `dict_type`, 'SB' AS `dict_value`, 'PRODUCT_SIZE_EU_SB' AS `dict_label`, '小号超大' AS `dict_name`, 400 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_EU' AS `dict_type`, 'NB' AS `dict_value`, 'PRODUCT_SIZE_EU_NB' AS `dict_label`, '标准超大' AS `dict_name`, 410 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_EU' AS `dict_type`, 'LB' AS `dict_value`, 'PRODUCT_SIZE_EU_LB' AS `dict_label`, '大号超大' AS `dict_name`, 420 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SIZE_EU' AS `dict_type`, 'O' AS `dict_value`, 'PRODUCT_SIZE_EU_O' AS `dict_label`, '其他' AS `dict_name`, 430 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'SELLER_NATIONALITY' AS `dict_type`, 'CN' AS `dict_value`, 'SELLER_NATIONALITY_CN' AS `dict_label`, '中国' AS `dict_name`, 440 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'SELLER_NATIONALITY' AS `dict_type`, 'HK' AS `dict_value`, 'SELLER_NATIONALITY_HK' AS `dict_label`, '中国香港' AS `dict_name`, 450 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'SELLER_NATIONALITY' AS `dict_type`, 'US' AS `dict_value`, 'SELLER_NATIONALITY_US' AS `dict_label`, '美国' AS `dict_name`, 460 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'SELLER_NATIONALITY' AS `dict_type`, 'JP' AS `dict_value`, 'SELLER_NATIONALITY_JP' AS `dict_label`, '日本' AS `dict_name`, 470 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'SELLER_NATIONALITY' AS `dict_type`, 'DE' AS `dict_value`, 'SELLER_NATIONALITY_DE' AS `dict_label`, '德国' AS `dict_name`, 480 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'SELLER_NATIONALITY' AS `dict_type`, 'FR' AS `dict_value`, 'SELLER_NATIONALITY_FR' AS `dict_label`, '法国' AS `dict_name`, 490 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'SELLER_NATIONALITY' AS `dict_type`, 'UK' AS `dict_value`, 'SELLER_NATIONALITY_UK' AS `dict_label`, '英国' AS `dict_name`, 500 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'SELLER_NATIONALITY' AS `dict_type`, 'IT' AS `dict_value`, 'SELLER_NATIONALITY_IT' AS `dict_label`, '意大利' AS `dict_name`, 510 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'SELLER_NATIONALITY' AS `dict_type`, 'ES' AS `dict_value`, 'SELLER_NATIONALITY_ES' AS `dict_label`, '西班牙' AS `dict_name`, 520 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'SELLER_NATIONALITY' AS `dict_type`, 'CA' AS `dict_value`, 'SELLER_NATIONALITY_CA' AS `dict_label`, '加拿大' AS `dict_name`, 530 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'SELLER_NATIONALITY' AS `dict_type`, 'IN' AS `dict_value`, 'SELLER_NATIONALITY_IN' AS `dict_label`, '印度' AS `dict_name`, 540 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'total_units' AS `dict_value`, 'PRODUCT_SORT_FIELD_TOTAL_UNITS' AS `dict_label`, '商品总销量' AS `dict_name`, 550 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'total_amount' AS `dict_value`, 'PRODUCT_SORT_FIELD_TOTAL_AMOUNT' AS `dict_label`, '商品总销售额' AS `dict_name`, 560 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'bsr_rank' AS `dict_value`, 'PRODUCT_SORT_FIELD_BSR_RANK' AS `dict_label`, 'BSR排名' AS `dict_name`, 570 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'price' AS `dict_value`, 'PRODUCT_SORT_FIELD_PRICE' AS `dict_label`, '价格' AS `dict_name`, 580 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'rating' AS `dict_value`, 'PRODUCT_SORT_FIELD_RATING' AS `dict_label`, '评分' AS `dict_name`, 590 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'reviews' AS `dict_value`, 'PRODUCT_SORT_FIELD_REVIEWS' AS `dict_label`, '评分数' AS `dict_name`, 600 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'profit' AS `dict_value`, 'PRODUCT_SORT_FIELD_PROFIT' AS `dict_label`, '利润' AS `dict_name`, 610 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'reviews_rate' AS `dict_value`, 'PRODUCT_SORT_FIELD_REVIEWS_RATE' AS `dict_label`, '评分率' AS `dict_name`, 620 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'available_date' AS `dict_value`, 'PRODUCT_SORT_FIELD_AVAILABLE_DATE' AS `dict_label`, '上架时间' AS `dict_name`, 630 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'questions' AS `dict_value`, 'PRODUCT_SORT_FIELD_QUESTIONS' AS `dict_label`, '问答数' AS `dict_name`, 640 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'total_units_growth' AS `dict_value`, 'PRODUCT_SORT_FIELD_TOTAL_UNITS_GROWTH' AS `dict_label`, '销量增长' AS `dict_name`, 650 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'total_amount_growth' AS `dict_value`, 'PRODUCT_SORT_FIELD_TOTAL_AMOUNT_GROWTH' AS `dict_label`, '销售额增长' AS `dict_name`, 660 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'reviews_increasement' AS `dict_value`, 'PRODUCT_SORT_FIELD_REVIEWS_INCREASEMENT' AS `dict_label`, '评分数增长' AS `dict_name`, 670 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'bsr_rank_cv' AS `dict_value`, 'PRODUCT_SORT_FIELD_BSR_RANK_CV' AS `dict_label`, 'BSR增长数' AS `dict_name`, 680 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'bsr_rank_cr' AS `dict_value`, 'PRODUCT_SORT_FIELD_BSR_RANK_CR' AS `dict_label`, 'BSR增长率' AS `dict_name`, 690 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_SORT_FIELD' AS `dict_type`, 'amz_unit' AS `dict_value`, 'PRODUCT_SORT_FIELD_AMZ_UNIT' AS `dict_label`, '子体销量' AS `dict_name`, 700 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'N' AS `dict_value`, 'MARKET_PERIOD_N' AS `dict_label`, '普通周期' AS `dict_name`, 710 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'S1' AS `dict_value`, 'MARKET_PERIOD_S1' AS `dict_label`, '1月' AS `dict_name`, 720 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'S2' AS `dict_value`, 'MARKET_PERIOD_S2' AS `dict_label`, '2月' AS `dict_name`, 730 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'S3' AS `dict_value`, 'MARKET_PERIOD_S3' AS `dict_label`, '3月' AS `dict_name`, 740 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'S4' AS `dict_value`, 'MARKET_PERIOD_S4' AS `dict_label`, '4月' AS `dict_name`, 750 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'S5' AS `dict_value`, 'MARKET_PERIOD_S5' AS `dict_label`, '5月' AS `dict_name`, 760 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'S6' AS `dict_value`, 'MARKET_PERIOD_S6' AS `dict_label`, '6月' AS `dict_name`, 770 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'S7' AS `dict_value`, 'MARKET_PERIOD_S7' AS `dict_label`, '7月' AS `dict_name`, 780 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'S8' AS `dict_value`, 'MARKET_PERIOD_S8' AS `dict_label`, '8月' AS `dict_name`, 790 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'S9' AS `dict_value`, 'MARKET_PERIOD_S9' AS `dict_label`, '9月' AS `dict_name`, 800 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'S10' AS `dict_value`, 'MARKET_PERIOD_S10' AS `dict_label`, '10月' AS `dict_name`, 810 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'S11' AS `dict_value`, 'MARKET_PERIOD_S11' AS `dict_label`, '11月' AS `dict_name`, 820 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'S12' AS `dict_value`, 'MARKET_PERIOD_S12' AS `dict_label`, '12月' AS `dict_name`, 830 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'I' AS `dict_value`, 'MARKET_PERIOD_I' AS `dict_label`, '持续增长市场' AS `dict_name`, 840 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'MARKET_PERIOD' AS `dict_type`, 'D' AS `dict_value`, 'MARKET_PERIOD_D' AS `dict_label`, '持续衰退市场' AS `dict_name`, 850 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_RESEARCH_SORT_FIELD' AS `dict_type`, 'searches' AS `dict_value`, 'KEYWORD_RESEARCH_SORT_FIELD_SEARCHES' AS `dict_label`, '搜索量' AS `dict_name`, 860 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_RESEARCH_SORT_FIELD' AS `dict_type`, 'keywordsIsHide' AS `dict_value`, 'KEYWORD_RESEARCH_SORT_FIELD_KEYWORDS_IS_HIDE' AS `dict_label`, '隐藏关键词数' AS `dict_name`, 870 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_RESEARCH_SORT_FIELD' AS `dict_type`, 'searches_growth' AS `dict_value`, 'KEYWORD_RESEARCH_SORT_FIELD_SEARCHES_GROWTH' AS `dict_label`, '搜索量增长' AS `dict_name`, 880 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_RESEARCH_SORT_FIELD' AS `dict_type`, 'yearly_growth_rate' AS `dict_value`, 'KEYWORD_RESEARCH_SORT_FIELD_YEARLY_GROWTH_RATE' AS `dict_label`, '年增长率' AS `dict_name`, 890 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_RESEARCH_SORT_FIELD' AS `dict_type`, 'growth_rate_trend_min' AS `dict_value`, 'KEYWORD_RESEARCH_SORT_FIELD_GROWTH_RATE_TREND_MIN' AS `dict_label`, '最低增长趋势' AS `dict_name`, 900 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_RESEARCH_SORT_FIELD' AS `dict_type`, 'monopoly_click_rate' AS `dict_value`, 'KEYWORD_RESEARCH_SORT_FIELD_MONOPOLY_CLICK_RATE' AS `dict_label`, '点击垄断率' AS `dict_name`, 910 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_RESEARCH_SORT_FIELD' AS `dict_type`, 'goods_value' AS `dict_value`, 'KEYWORD_RESEARCH_SORT_FIELD_GOODS_VALUE' AS `dict_label`, '商品价值' AS `dict_name`, 920 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_RESEARCH_TREND_SORT_FIELD' AS `dict_type`, 'searchfrequencyrank' AS `dict_value`, 'KEYWORD_RESEARCH_TREND_SORT_FIELD_SEARCHFREQUENCYRANK' AS `dict_label`, '搜索频率排名' AS `dict_name`, 930 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_RESEARCH_TREND_SORT_FIELD' AS `dict_type`, 'n1RankGrowthValue' AS `dict_value`, 'KEYWORD_RESEARCH_TREND_SORT_FIELD_N1_RANK_GROWTH_VALUE' AS `dict_label`, 'N1排名增长量' AS `dict_name`, 940 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_RESEARCH_TREND_SORT_FIELD' AS `dict_type`, 'n1RankGrowthRate' AS `dict_value`, 'KEYWORD_RESEARCH_TREND_SORT_FIELD_N1_RANK_GROWTH_RATE' AS `dict_label`, 'N1排名增长率' AS `dict_name`, 950 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_RESEARCH_TREND_SORT_FIELD' AS `dict_type`, 'monopolyClickRate' AS `dict_value`, 'KEYWORD_RESEARCH_TREND_SORT_FIELD_MONOPOLY_CLICK_RATE' AS `dict_label`, '点击垄断率' AS `dict_name`, 960 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_EXPOSURE_POSITION' AS `dict_type`, 'naturalSearching' AS `dict_value`, 'REVERSE_ASIN_EXPOSURE_POSITION_NATURAL_SEARCHING' AS `dict_label`, '自然搜索' AS `dict_name`, 970 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_EXPOSURE_POSITION' AS `dict_type`, 'amazonChoice' AS `dict_value`, 'REVERSE_ASIN_EXPOSURE_POSITION_AMAZON_CHOICE' AS `dict_label`, 'Amazon推荐' AS `dict_name`, 980 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_EXPOSURE_POSITION' AS `dict_type`, 'editorialRecommendations' AS `dict_value`, 'REVERSE_ASIN_EXPOSURE_POSITION_EDITORIAL_RECOMMENDATIONS' AS `dict_label`, '编辑推荐' AS `dict_name`, 990 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_EXPOSURE_POSITION' AS `dict_type`, 'fourStar' AS `dict_value`, 'REVERSE_ASIN_EXPOSURE_POSITION_FOUR_STAR' AS `dict_label`, '四星推荐' AS `dict_name`, 1000 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_EXPOSURE_POSITION' AS `dict_type`, 'highlyRated' AS `dict_value`, 'REVERSE_ASIN_EXPOSURE_POSITION_HIGHLY_RATED' AS `dict_label`, '高评分' AS `dict_name`, 10 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_EXPOSURE_POSITION' AS `dict_type`, 'sponsorBrand' AS `dict_value`, 'REVERSE_ASIN_EXPOSURE_POSITION_SPONSOR_BRAND' AS `dict_label`, '品牌广告' AS `dict_name`, 20 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_EXPOSURE_POSITION' AS `dict_type`, 'sponsorVideo' AS `dict_value`, 'REVERSE_ASIN_EXPOSURE_POSITION_SPONSOR_VIDEO' AS `dict_label`, '视频广告' AS `dict_name`, 30 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_EXPOSURE_POSITION' AS `dict_type`, 'ads' AS `dict_value`, 'REVERSE_ASIN_EXPOSURE_POSITION_ADS' AS `dict_label`, '广告位' AS `dict_name`, 40 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SHARE_TYPE' AS `dict_type`, 'primary' AS `dict_value`, 'REVERSE_ASIN_SHARE_TYPE_PRIMARY' AS `dict_label`, '主流量' AS `dict_name`, 50 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SHARE_TYPE' AS `dict_type`, 'precise' AS `dict_value`, 'REVERSE_ASIN_SHARE_TYPE_PRECISE' AS `dict_label`, '精准词' AS `dict_name`, 60 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SHARE_TYPE' AS `dict_type`, 'preciseLongTail' AS `dict_value`, 'REVERSE_ASIN_SHARE_TYPE_PRECISE_LONG_TAIL' AS `dict_label`, '精准长尾词' AS `dict_name`, 70 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_CONVERSION_TYPE' AS `dict_type`, 'excellent' AS `dict_value`, 'REVERSE_ASIN_CONVERSION_TYPE_EXCELLENT' AS `dict_label`, '转化优质词' AS `dict_name`, 80 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_CONVERSION_TYPE' AS `dict_type`, 'stable' AS `dict_value`, 'REVERSE_ASIN_CONVERSION_TYPE_STABLE' AS `dict_label`, '转化平稳词' AS `dict_name`, 90 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_CONVERSION_TYPE' AS `dict_type`, 'lost' AS `dict_value`, 'REVERSE_ASIN_CONVERSION_TYPE_LOST' AS `dict_label`, '转化流失词' AS `dict_name`, 100 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_CONVERSION_TYPE' AS `dict_type`, 'invalid' AS `dict_value`, 'REVERSE_ASIN_CONVERSION_TYPE_INVALID' AS `dict_label`, '无效曝光词' AS `dict_name`, 110 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'RELATED_PRODUCT_ASSOCIATION_TYPE' AS `dict_type`, 'mib' AS `dict_value`, 'RELATED_PRODUCT_ASSOCIATION_TYPE_MIB' AS `dict_label`, '更多购买选择' AS `dict_name`, 120 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'RELATED_PRODUCT_ASSOCIATION_TYPE' AS `dict_type`, 'fbt' AS `dict_value`, 'RELATED_PRODUCT_ASSOCIATION_TYPE_FBT' AS `dict_label`, '经常一起购买' AS `dict_name`, 130 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'RELATED_PRODUCT_ASSOCIATION_TYPE' AS `dict_type`, 'csi' AS `dict_value`, 'RELATED_PRODUCT_ASSOCIATION_TYPE_CSI' AS `dict_label`, '相似商品' AS `dict_name`, 140 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'RELATED_PRODUCT_ASSOCIATION_TYPE' AS `dict_type`, 'cob' AS `dict_value`, 'RELATED_PRODUCT_ASSOCIATION_TYPE_COB' AS `dict_label`, '同品牌商品' AS `dict_name`, 150 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'RELATED_PRODUCT_ASSOCIATION_TYPE' AS `dict_type`, 'mie' AS `dict_value`, 'RELATED_PRODUCT_ASSOCIATION_TYPE_MIE' AS `dict_label`, '更多关联商品' AS `dict_name`, 160 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'RELATED_PRODUCT_ASSOCIATION_TYPE' AS `dict_type`, 'bab' AS `dict_value`, 'RELATED_PRODUCT_ASSOCIATION_TYPE_BAB' AS `dict_label`, '购买此商品的用户也购买' AS `dict_name`, 170 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'RELATED_PRODUCT_ASSOCIATION_TYPE' AS `dict_type`, 'vav' AS `dict_value`, 'RELATED_PRODUCT_ASSOCIATION_TYPE_VAV' AS `dict_label`, '浏览此商品的用户也浏览' AS `dict_name`, 180 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'RELATED_PRODUCT_ASSOCIATION_TYPE' AS `dict_type`, 'avp' AS `dict_value`, 'RELATED_PRODUCT_ASSOCIATION_TYPE_AVP' AS `dict_label`, '也浏览过的商品' AS `dict_name`, 190 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'RELATED_PRODUCT_ASSOCIATION_TYPE' AS `dict_type`, 'bav' AS `dict_value`, 'RELATED_PRODUCT_ASSOCIATION_TYPE_BAV' AS `dict_label`, '购买后浏览' AS `dict_name`, 200 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'RELATED_PRODUCT_ASSOCIATION_TYPE' AS `dict_type`, 'asf' AS `dict_value`, 'RELATED_PRODUCT_ASSOCIATION_TYPE_ASF' AS `dict_label`, '也搜索过' AS `dict_name`, 210 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'RELATED_PRODUCT_ASSOCIATION_TYPE' AS `dict_type`, 'cpf' AS `dict_value`, 'RELATED_PRODUCT_ASSOCIATION_TYPE_CPF' AS `dict_label`, '气候友好商品' AS `dict_name`, 220 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'RELATED_PRODUCT_ASSOCIATION_TYPE' AS `dict_type`, 'bca' AS `dict_value`, 'RELATED_PRODUCT_ASSOCIATION_TYPE_BCA' AS `dict_label`, '品牌与类目关联' AS `dict_name`, 230 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'RELATED_PRODUCT_ASSOCIATION_TYPE' AS `dict_type`, 'fsa' AS `dict_value`, 'RELATED_PRODUCT_ASSOCIATION_TYPE_FSA' AS `dict_label`, '四星以上' AS `dict_name`, 240 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'RELATED_PRODUCT_ASSOCIATION_TYPE' AS `dict_type`, 'sp' AS `dict_value`, 'RELATED_PRODUCT_ASSOCIATION_TYPE_SP' AS `dict_label`, '赞助商品' AS `dict_name`, 250 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SORT_FIELD' AS `dict_type`, 'rankPosition' AS `dict_value`, 'REVERSE_ASIN_SORT_FIELD_RANK_POSITION' AS `dict_label`, '自然排名' AS `dict_name`, 260 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SORT_FIELD' AS `dict_type`, 'adPosition' AS `dict_value`, 'REVERSE_ASIN_SORT_FIELD_AD_POSITION' AS `dict_label`, '广告排名' AS `dict_name`, 270 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SORT_FIELD' AS `dict_type`, 'createdTime' AS `dict_value`, 'REVERSE_ASIN_SORT_FIELD_CREATED_TIME' AS `dict_label`, '上架时间' AS `dict_name`, 280 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SORT_FIELD' AS `dict_type`, 'searchesRank' AS `dict_value`, 'REVERSE_ASIN_SORT_FIELD_SEARCHES_RANK' AS `dict_label`, '搜索排名' AS `dict_name`, 290 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SORT_FIELD' AS `dict_type`, 'searches' AS `dict_value`, 'REVERSE_ASIN_SORT_FIELD_SEARCHES' AS `dict_label`, '搜索量' AS `dict_name`, 300 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SORT_FIELD' AS `dict_type`, 'purchases' AS `dict_value`, 'REVERSE_ASIN_SORT_FIELD_PURCHASES' AS `dict_label`, '购买量' AS `dict_name`, 310 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SORT_FIELD' AS `dict_type`, 'purchaseRate' AS `dict_value`, 'REVERSE_ASIN_SORT_FIELD_PURCHASE_RATE' AS `dict_label`, '购买率' AS `dict_name`, 320 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SORT_FIELD' AS `dict_type`, 'products' AS `dict_value`, 'REVERSE_ASIN_SORT_FIELD_PRODUCTS' AS `dict_label`, '商品数' AS `dict_name`, 330 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SORT_FIELD' AS `dict_type`, 'supplyDemandRatio' AS `dict_value`, 'REVERSE_ASIN_SORT_FIELD_SUPPLY_DEMAND_RATIO' AS `dict_label`, '供需比' AS `dict_name`, 340 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SORT_FIELD' AS `dict_type`, 'latest1daysAds' AS `dict_value`, 'REVERSE_ASIN_SORT_FIELD_LATEST_1DAYS_ADS' AS `dict_label`, '近1天广告数' AS `dict_name`, 350 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SORT_FIELD' AS `dict_type`, 'bid' AS `dict_value`, 'REVERSE_ASIN_SORT_FIELD_BID' AS `dict_label`, '竞价' AS `dict_name`, 360 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_ASIN_SORT_FIELD' AS `dict_type`, 'trafficPercentage' AS `dict_value`, 'REVERSE_ASIN_SORT_FIELD_TRAFFIC_PERCENTAGE' AS `dict_label`, '流量占比' AS `dict_name`, 370 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'ABA_SORT_FIELD' AS `dict_type`, 'searchfrequencyrank' AS `dict_value`, 'ABA_SORT_FIELD_SEARCHFREQUENCYRANK' AS `dict_label`, '搜索频率排名' AS `dict_name`, 380 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'ABA_SORT_FIELD' AS `dict_type`, 'n1RankGrowthValue' AS `dict_value`, 'ABA_SORT_FIELD_N1_RANK_GROWTH_VALUE' AS `dict_label`, 'N1排名增长量' AS `dict_name`, 390 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'ABA_SORT_FIELD' AS `dict_type`, 'n1RankGrowthRate' AS `dict_value`, 'ABA_SORT_FIELD_N1_RANK_GROWTH_RATE' AS `dict_label`, 'N1排名增长率' AS `dict_name`, 400 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'ABA_SORT_FIELD' AS `dict_type`, 'monopolyClickRate' AS `dict_value`, 'ABA_SORT_FIELD_MONOPOLY_CLICK_RATE' AS `dict_label`, '点击垄断率' AS `dict_name`, 410 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'ABA_SORT_FIELD' AS `dict_type`, 'keyword' AS `dict_value`, 'ABA_SORT_FIELD_KEYWORD' AS `dict_label`, '关键词' AS `dict_name`, 420 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'ABA_SORT_FIELD' AS `dict_type`, 'nk' AS `dict_value`, 'ABA_SORT_FIELD_NK' AS `dict_label`, '自然关键词数' AS `dict_name`, 430 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'ABA_SORT_FIELD' AS `dict_type`, 'ac' AS `dict_value`, 'ABA_SORT_FIELD_AC' AS `dict_label`, 'Amazon推荐词数' AS `dict_name`, 440 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'ABA_SORT_FIELD' AS `dict_type`, 'er' AS `dict_value`, 'ABA_SORT_FIELD_ER' AS `dict_label`, '编辑推荐词数' AS `dict_name`, 450 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'ABA_SORT_FIELD' AS `dict_type`, 'fs' AS `dict_value`, 'ABA_SORT_FIELD_FS' AS `dict_label`, '四星词数' AS `dict_name`, 460 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'ABA_SORT_FIELD' AS `dict_type`, 'hr' AS `dict_value`, 'ABA_SORT_FIELD_HR' AS `dict_label`, '高评分词数' AS `dict_name`, 470 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'ABA_SORT_FIELD' AS `dict_type`, 'spb' AS `dict_value`, 'ABA_SORT_FIELD_SPB' AS `dict_label`, '品牌广告词数' AS `dict_name`, 480 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'ABA_SORT_FIELD' AS `dict_type`, 'spv' AS `dict_value`, 'ABA_SORT_FIELD_SPV' AS `dict_label`, '视频广告词数' AS `dict_name`, 490 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'ABA_SORT_FIELD' AS `dict_type`, 'ads' AS `dict_value`, 'ABA_SORT_FIELD_ADS' AS `dict_label`, '广告词数' AS `dict_name`, 500 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'ABA_SORT_FIELD' AS `dict_type`, 'updatedTime' AS `dict_value`, 'ABA_SORT_FIELD_UPDATED_TIME' AS `dict_label`, '更新时间' AS `dict_name`, 510 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_MULTIPLE_ASIN_SORT_FIELD' AS `dict_type`, 'trafficPercentage' AS `dict_value`, 'REVERSE_MULTIPLE_ASIN_SORT_FIELD_TRAFFIC_PERCENTAGE' AS `dict_label`, '流量占比' AS `dict_name`, 520 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_MULTIPLE_ASIN_SORT_FIELD' AS `dict_type`, 'relationAsin' AS `dict_value`, 'REVERSE_MULTIPLE_ASIN_SORT_FIELD_RELATION_ASIN' AS `dict_label`, '关联ASIN' AS `dict_name`, 530 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_MULTIPLE_ASIN_SORT_FIELD' AS `dict_type`, 'searchesRank' AS `dict_value`, 'REVERSE_MULTIPLE_ASIN_SORT_FIELD_SEARCHES_RANK' AS `dict_label`, '搜索排名' AS `dict_name`, 540 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_MULTIPLE_ASIN_SORT_FIELD' AS `dict_type`, 'searches' AS `dict_value`, 'REVERSE_MULTIPLE_ASIN_SORT_FIELD_SEARCHES' AS `dict_label`, '搜索量' AS `dict_name`, 550 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_MULTIPLE_ASIN_SORT_FIELD' AS `dict_type`, 'purchases' AS `dict_value`, 'REVERSE_MULTIPLE_ASIN_SORT_FIELD_PURCHASES' AS `dict_label`, '购买量' AS `dict_name`, 560 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_MULTIPLE_ASIN_SORT_FIELD' AS `dict_type`, 'purchaseRate' AS `dict_value`, 'REVERSE_MULTIPLE_ASIN_SORT_FIELD_PURCHASE_RATE' AS `dict_label`, '购买率' AS `dict_name`, 570 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_MULTIPLE_ASIN_SORT_FIELD' AS `dict_type`, 'spr' AS `dict_value`, 'REVERSE_MULTIPLE_ASIN_SORT_FIELD_SPR' AS `dict_label`, 'SPR' AS `dict_name`, 580 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_MULTIPLE_ASIN_SORT_FIELD' AS `dict_type`, 'titleDensity' AS `dict_value`, 'REVERSE_MULTIPLE_ASIN_SORT_FIELD_TITLE_DENSITY' AS `dict_label`, '标题密度' AS `dict_name`, 590 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_MULTIPLE_ASIN_SORT_FIELD' AS `dict_type`, 'products' AS `dict_value`, 'REVERSE_MULTIPLE_ASIN_SORT_FIELD_PRODUCTS' AS `dict_label`, '商品数' AS `dict_name`, 600 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_MULTIPLE_ASIN_SORT_FIELD' AS `dict_type`, 'supplyDemandRatio' AS `dict_value`, 'REVERSE_MULTIPLE_ASIN_SORT_FIELD_SUPPLY_DEMAND_RATIO' AS `dict_label`, '供需比' AS `dict_name`, 610 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_MULTIPLE_ASIN_SORT_FIELD' AS `dict_type`, 'adProduct' AS `dict_value`, 'REVERSE_MULTIPLE_ASIN_SORT_FIELD_AD_PRODUCT' AS `dict_label`, '广告商品数' AS `dict_name`, 620 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_MULTIPLE_ASIN_SORT_FIELD' AS `dict_type`, 'monopolyClickRate' AS `dict_value`, 'REVERSE_MULTIPLE_ASIN_SORT_FIELD_MONOPOLY_CLICK_RATE' AS `dict_label`, '点击垄断率' AS `dict_name`, 630 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'REVERSE_MULTIPLE_ASIN_SORT_FIELD' AS `dict_type`, 'bid' AS `dict_value`, 'REVERSE_MULTIPLE_ASIN_SORT_FIELD_BID' AS `dict_label`, '竞价' AS `dict_name`, 640 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_EXPLORER_SORT_FIELD' AS `dict_type`, 'searchRank' AS `dict_value`, 'KEYWORD_EXPLORER_SORT_FIELD_SEARCH_RANK' AS `dict_label`, '搜索排名' AS `dict_name`, 650 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_EXPLORER_SORT_FIELD' AS `dict_type`, 'searches' AS `dict_value`, 'KEYWORD_EXPLORER_SORT_FIELD_SEARCHES' AS `dict_label`, '搜索量' AS `dict_name`, 660 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_EXPLORER_SORT_FIELD' AS `dict_type`, 'monopolyClickRate' AS `dict_value`, 'KEYWORD_EXPLORER_SORT_FIELD_MONOPOLY_CLICK_RATE' AS `dict_label`, '点击垄断率' AS `dict_name`, 670 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_EXPLORER_SORT_FIELD' AS `dict_type`, 'cvsShareRate' AS `dict_value`, 'KEYWORD_EXPLORER_SORT_FIELD_CVS_SHARE_RATE' AS `dict_label`, '转化共享率' AS `dict_name`, 680 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_EXPLORER_SORT_FIELD' AS `dict_type`, 'searchRankGv' AS `dict_value`, 'KEYWORD_EXPLORER_SORT_FIELD_SEARCH_RANK_GV' AS `dict_label`, '搜索排名月变化量' AS `dict_name`, 690 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_EXPLORER_SORT_FIELD' AS `dict_type`, 'searchRankGr' AS `dict_value`, 'KEYWORD_EXPLORER_SORT_FIELD_SEARCH_RANK_GR' AS `dict_label`, '搜索排名月变化率' AS `dict_name`, 700 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_EXPLORER_SORT_FIELD' AS `dict_type`, 'top3ClickingRate' AS `dict_value`, 'KEYWORD_EXPLORER_SORT_FIELD_TOP3_CLICKING_RATE' AS `dict_label`, '前三点击率' AS `dict_name`, 710 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_EXPLORER_SORT_FIELD' AS `dict_type`, 'top3ConversionRate' AS `dict_value`, 'KEYWORD_EXPLORER_SORT_FIELD_TOP3_CONVERSION_RATE' AS `dict_label`, '前三转化率' AS `dict_name`, 720 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'KEYWORD_EXPLORER_SORT_FIELD' AS `dict_type`, 'keyword' AS `dict_value`, 'KEYWORD_EXPLORER_SORT_FIELD_KEYWORD' AS `dict_label`, '关键词' AS `dict_name`, 730 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_WEIGHT_UNIT' AS `dict_type`, 'g' AS `dict_value`, 'PRODUCT_WEIGHT_UNIT_G' AS `dict_label`, '克' AS `dict_name`, 740 AS `sort_order`, 1 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_WEIGHT_UNIT' AS `dict_type`, 'kg' AS `dict_value`, 'PRODUCT_WEIGHT_UNIT_KG' AS `dict_label`, '千克' AS `dict_name`, 750 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_WEIGHT_UNIT' AS `dict_type`, 'lb' AS `dict_value`, 'PRODUCT_WEIGHT_UNIT_LB' AS `dict_label`, '磅' AS `dict_name`, 760 AS `sort_order`, 0 AS `default_flag`
  UNION ALL
  SELECT 'PRODUCT_WEIGHT_UNIT' AS `dict_type`, 'oz' AS `dict_value`, 'PRODUCT_WEIGHT_UNIT_OZ' AS `dict_label`, '盎司' AS `dict_name`, 770 AS `sort_order`, 0 AS `default_flag`
) AS `seed`
ON DUPLICATE KEY UPDATE
  `dict_type` = VALUES(`dict_type`),
  `dict_value` = VALUES(`dict_value`),
  `dict_name` = VALUES(`dict_name`),
  `system_builtin` = 1,
  `default_flag` = VALUES(`default_flag`),
  `sort_order` = VALUES(`sort_order`),
  `status` = 1,
  `updated_at` = @seed_now_ms,
  `updated_by` = @seed_user_id,
  `remark` = 'SellerSprite 官方接口参数字典';

COMMIT;
