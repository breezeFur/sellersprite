import type { SellerSpriteDomain, SellerSpriteOperation } from './sellersprite'

const SAMPLE_ASIN = 'B07Z82895W'
const SAMPLE_MONTH = '202507'
const SAMPLE_NODE_PATH = '1064954:1069242:1069784:1069820:1069838:1069828'

export const sellerSpriteDomains = [
  { id: 'account', label: '账户次数' },
  { id: 'product', label: '产品分析' },
  { id: 'asin', label: 'ASIN 分析' },
  { id: 'keyword', label: '关键词研究' },
  { id: 'traffic', label: '流量分析' },
  { id: 'market', label: '市场分析' },
  { id: 'review', label: '评论分析' },
  { id: 'trademark', label: '全球商标' },
  { id: 'tool', label: '数据工具' },
] as const satisfies readonly SellerSpriteDomain[]

export const sellerSpriteOperations = [
  {
    id: 'ACCOUNT_VISITS', domain: 'account', name: '查询可用次数',
    description: '查询当前月份各模块可用调用次数。',
    method: 'GET', path: '/sellersprite/account/visits', transport: 'query', example: {},
  },
  {
    id: 'PRODUCT_COMPETITOR_LOOKUP', domain: 'product', name: '查竞品',
    description: '按市场、关键词或类目筛选竞品。',
    method: 'POST', path: '/sellersprite/products/competitors', transport: 'json',
    example: { marketplace: 'US', month: SAMPLE_MONTH, keyword: 'wireless earbuds', page: 1, size: 20 },
  },
  {
    id: 'PRODUCT_RESEARCH', domain: 'product', name: '选产品',
    description: '按市场和产品指标查询选品结果。',
    method: 'POST', path: '/sellersprite/products/research', transport: 'json',
    example: { marketplace: 'US', month: SAMPLE_MONTH, page: 1, size: 20 },
  },
  {
    id: 'PRODUCT_NODE', domain: 'product', name: '查产品类目',
    description: '查询指定市场的产品类目节点。',
    method: 'GET', path: '/sellersprite/products/nodes', transport: 'query',
    example: { marketplace: 'US' },
  },
  {
    id: 'ASIN_DETAIL', domain: 'asin', name: 'ASIN 详情',
    description: '查询指定市场和 ASIN 的商品详情。',
    method: 'GET', path: '/sellersprite/asins/detail', transport: 'query',
    example: { marketplace: 'US', asin: SAMPLE_ASIN },
  },
  {
    id: 'ASIN_COUPON_TREND', domain: 'asin', name: 'ASIN 优惠趋势',
    description: '查询指定 ASIN 的优惠变化趋势。',
    method: 'GET', path: '/sellersprite/asins/coupon-trend', transport: 'query',
    example: { marketplace: 'US', asin: SAMPLE_ASIN },
  },
  {
    id: 'ASIN_WITH_COUPON_TREND', domain: 'asin', name: 'ASIN 详情及优惠趋势',
    description: '一次查询商品详情与优惠趋势。',
    method: 'GET', path: '/sellersprite/asins/with-coupon-trend', transport: 'query',
    example: { marketplace: 'US', asin: SAMPLE_ASIN },
  },
  {
    id: 'ASIN_SALES_TREND', domain: 'asin', name: 'ASIN 销量趋势',
    description: '查询指定 ASIN 的销量趋势。',
    method: 'GET', path: '/sellersprite/asins/sales-trend', transport: 'query',
    example: { marketplace: 'US', asin: SAMPLE_ASIN },
  },
  {
    id: 'ASIN_SALES_PREDICTION', domain: 'asin', name: 'ASIN 销量预测',
    description: '根据 ASIN 预测商品销量。',
    method: 'GET', path: '/sellersprite/asins/sales-prediction', transport: 'query',
    example: { marketplace: 'US', asin: SAMPLE_ASIN },
  },
  {
    id: 'BSR_SALES_PREDICTION', domain: 'asin', name: 'BSR 销量预测',
    description: '根据类目 BSR 预测商品销量。',
    method: 'GET', path: '/sellersprite/asins/bsr-sales-prediction', transport: 'query',
    example: { marketplace: 'US', bsr: 1024, categoryId: '11260432011' },
  },
  {
    id: 'ASIN_KEEPA_TREND', domain: 'asin', name: 'Keepa 商品趋势',
    description: '查询指定 ASIN 的 Keepa 趋势数据。',
    method: 'GET', path: '/sellersprite/asins/keepa', transport: 'query',
    example: { marketplace: 'US', asin: SAMPLE_ASIN, dailyLatest: true },
  },
  {
    id: 'KEYWORD_RESEARCH', domain: 'keyword', name: '关键词选品',
    description: '按市场和关键词指标查询选品词。',
    method: 'POST', path: '/sellersprite/keywords/research', transport: 'json',
    example: { marketplace: 'US', keyword: 'wireless earbuds', page: 1, size: 20 },
  },
  {
    id: 'KEYWORD_RESEARCH_TRENDS', domain: 'keyword', name: '关键词选品趋势',
    description: '查询关键词的选品趋势。',
    method: 'POST', path: '/sellersprite/keywords/research/trends', transport: 'json',
    example: { marketplace: 'US', keyword: 'wireless earbuds' },
  },
  {
    id: 'KEYWORD_MINER', domain: 'keyword', name: '关键词挖掘',
    description: '围绕种子词挖掘关联关键词。',
    method: 'POST', path: '/sellersprite/keywords/mine', transport: 'json',
    example: { marketplace: 'US', keyword: 'wireless earbuds', page: 1, size: 20 },
  },
  {
    id: 'KEYWORD_TRAFFIC_EXTEND', domain: 'keyword', name: '拓展流量词',
    description: '根据一组 ASIN 拓展流量关键词。',
    method: 'POST', path: '/sellersprite/keywords/traffic/extend', transport: 'json',
    example: { marketplace: 'US', asinList: [SAMPLE_ASIN], page: 1, size: 20 },
  },
  {
    id: 'ABA_RESEARCH_WEEKLY', domain: 'keyword', name: 'ABA 数据选品（周）',
    description: '查询按周汇总的 ABA 选品数据。',
    method: 'POST', path: '/sellersprite/keywords/aba/weekly', transport: 'json',
    example: { marketplace: 'US', date: '20250705', page: 1, size: 20 },
  },
  {
    id: 'ABA_RESEARCH_MONTHLY', domain: 'keyword', name: 'ABA 数据选品（月）',
    description: '查询按月汇总的 ABA 选品数据。',
    method: 'POST', path: '/sellersprite/keywords/aba/monthly', transport: 'json',
    example: { marketplace: 'US', date: SAMPLE_MONTH, page: 1, size: 15 },
  },
  {
    id: 'ABA_RESEARCH_TRENDS', domain: 'keyword', name: 'ABA 关键词趋势',
    description: '查询 ABA 关键词排名趋势。',
    method: 'POST', path: '/sellersprite/keywords/aba/trends', transport: 'json',
    example: { marketplace: 'US', keyword: 'wireless earbuds' },
  },
  {
    id: 'GOOGLE_TRENDS', domain: 'keyword', name: '谷歌趋势',
    description: '查询关键词的 Google Trends 数据。',
    method: 'GET', path: '/sellersprite/keywords/google-trends', transport: 'query',
    example: { marketplace: 'US', keyword: 'wireless earbuds' },
  },
  {
    id: 'KEYWORD_ORDER', domain: 'keyword', name: '出单词反查',
    description: '根据 ASIN 反查产生订单的关键词。',
    method: 'POST', path: '/sellersprite/keywords/order/reverse', transport: 'json',
    example: { marketplace: 'US', asins: [SAMPLE_ASIN], reverseType: 'W' },
  },
  {
    id: 'TRAFFIC_KEYWORD', domain: 'traffic', name: '关键词反查',
    description: '查询指定 ASIN 的流量关键词。',
    method: 'POST', path: '/sellersprite/traffic/keywords/reverse', transport: 'json',
    example: { marketplace: 'US', asin: SAMPLE_ASIN, page: 1, size: 20 },
  },
  {
    id: 'TRAFFIC_LISTING_PAGE', domain: 'traffic', name: '关联流量列表',
    description: '查询 ASIN 的关联流量商品列表。',
    method: 'POST', path: '/sellersprite/traffic/related', transport: 'json',
    example: { marketplace: 'US', asinList: [SAMPLE_ASIN], relations: ['vav'], page: 1, size: 20 },
  },
  {
    id: 'TRAFFIC_KEYWORD_STAT', domain: 'traffic', name: '流量词统计',
    description: '汇总指定 ASIN 的流量词统计。',
    method: 'GET', path: '/sellersprite/traffic/keywords/stats', transport: 'query',
    example: { marketplace: 'US', asin: SAMPLE_ASIN, month: SAMPLE_MONTH },
  },
  {
    id: 'TRAFFIC_LISTING_STAT', domain: 'traffic', name: '关联流量统计',
    description: '汇总指定 ASIN 的关联流量统计。',
    method: 'GET', path: '/sellersprite/traffic/listings/stats', transport: 'query',
    example: { marketplace: 'US', asin: SAMPLE_ASIN },
  },
  {
    id: 'TRAFFIC_SOURCE', domain: 'traffic', name: '查流量来源',
    description: '查询关键词或 ASIN 的流量来源。',
    method: 'POST', path: '/sellersprite/traffic/sources', transport: 'json',
    example: { marketplace: 'US', q: SAMPLE_ASIN, month: '202503', page: 1, size: 20 },
  },
  {
    id: 'MARKET_RESEARCH', domain: 'market', name: '选市场列表',
    description: '按市场指标筛选候选类目。',
    method: 'POST', path: '/sellersprite/markets/research', transport: 'json',
    example: { marketplace: 'US', month: SAMPLE_MONTH, page: 1, size: 20 },
  },
  {
    id: 'MARKET_STATISTICS', domain: 'market', name: '选市场统计',
    description: '查询指定类目的市场总体统计。',
    method: 'POST', path: '/sellersprite/markets/statistics', transport: 'json',
    example: { marketplace: 'US', nodeIdPath: SAMPLE_NODE_PATH },
  },
  {
    id: 'MARKET_GOODS', domain: 'market', name: '商品集中度',
    description: '查询指定类目的商品集中度。',
    method: 'POST', path: '/sellersprite/markets/goods', transport: 'json',
    example: { marketplace: 'US', nodeIdPath: SAMPLE_NODE_PATH },
  },
  {
    id: 'MARKET_BRAND', domain: 'market', name: '品牌集中度',
    description: '查询指定类目的品牌集中度。',
    method: 'POST', path: '/sellersprite/markets/brands', transport: 'json',
    example: { marketplace: 'US', nodeIdPath: SAMPLE_NODE_PATH },
  },
  {
    id: 'MARKET_SELLER_LOCATION', domain: 'market', name: '卖家所属地分布',
    description: '查询指定类目的卖家所属地分布。',
    method: 'POST', path: '/sellersprite/markets/sellers/locations', transport: 'json',
    example: { marketplace: 'US', nodeIdPath: SAMPLE_NODE_PATH },
  },
  {
    id: 'MARKET_SELLER', domain: 'market', name: '卖家集中度',
    description: '查询指定类目的卖家集中度。',
    method: 'POST', path: '/sellersprite/markets/sellers', transport: 'json',
    example: { marketplace: 'US', nodeIdPath: SAMPLE_NODE_PATH },
  },
  {
    id: 'MARKET_SELLER_TYPE', domain: 'market', name: '卖家类型分布',
    description: '查询指定类目的卖家类型分布。',
    method: 'POST', path: '/sellersprite/markets/sellers/types', transport: 'json',
    example: { marketplace: 'US', nodeIdPath: SAMPLE_NODE_PATH },
  },
  {
    id: 'MARKET_PERFORMANCE', domain: 'market', name: '商品需求趋势',
    description: '查询指定类目的商品需求趋势。',
    method: 'POST', path: '/sellersprite/markets/demand-trend', transport: 'json',
    example: { marketplace: 'US', nodeIdPath: SAMPLE_NODE_PATH },
  },
  {
    id: 'MARKET_SHELF_TIME', domain: 'market', name: '上架时间分布',
    description: '查询指定类目的上架时间分布。',
    method: 'POST', path: '/sellersprite/markets/shelf-times', transport: 'json',
    example: { marketplace: 'US', nodeIdPath: SAMPLE_NODE_PATH },
  },
  {
    id: 'MARKET_SHELF_TREND', domain: 'market', name: '上架趋势分布',
    description: '查询指定类目的上架趋势分布。',
    method: 'POST', path: '/sellersprite/markets/shelf-trends', transport: 'json',
    example: { marketplace: 'US', nodeIdPath: SAMPLE_NODE_PATH },
  },
  {
    id: 'MARKET_RATINGS', domain: 'market', name: '评分数分布',
    description: '查询指定类目的评分数量分布。',
    method: 'POST', path: '/sellersprite/markets/ratings', transport: 'json',
    example: { marketplace: 'US', nodeIdPath: SAMPLE_NODE_PATH },
  },
  {
    id: 'MARKET_RATING', domain: 'market', name: '评分值分布',
    description: '查询指定类目的评分值分布。',
    method: 'POST', path: '/sellersprite/markets/rating', transport: 'json',
    example: { marketplace: 'US', nodeIdPath: SAMPLE_NODE_PATH },
  },
  {
    id: 'MARKET_PRICE', domain: 'market', name: '价格分布',
    description: '查询指定类目的价格分布。',
    method: 'POST', path: '/sellersprite/markets/prices', transport: 'json',
    example: { marketplace: 'US', nodeIdPath: SAMPLE_NODE_PATH },
  },
  {
    id: 'MARKET_EBC', domain: 'market', name: 'A+ 视频分布',
    description: '查询指定类目的 A+ 与视频分布。',
    method: 'POST', path: '/sellersprite/markets/ebc', transport: 'json',
    example: { marketplace: 'US', nodeIdPath: SAMPLE_NODE_PATH },
  },
  {
    id: 'REVIEW_LIST', domain: 'review', name: '查评论',
    description: '分页查询指定 ASIN 的评论。',
    method: 'POST', path: '/sellersprite/reviews/search', transport: 'json',
    example: { marketplace: 'US', asin: SAMPLE_ASIN, page: 1, size: 5 },
  },
  {
    id: 'GLOBAL_BRAND_RANGE', domain: 'trademark', name: '全球商标数据范围',
    description: '查询全球商标库支持的数据范围。',
    method: 'GET', path: '/sellersprite/trademarks/range', transport: 'query', example: {},
  },
  {
    id: 'GLOBAL_BRAND_DETAIL', domain: 'trademark', name: '全球商标详情',
    description: '按注册局和商标 ID 查询详情。',
    method: 'GET', path: '/sellersprite/trademarks/detail', transport: 'query',
    example: { office: 'US', brandId: 'US502022097612203' },
  },
  {
    id: 'GLOBAL_BRAND_LIST', domain: 'trademark', name: '全球商标列表',
    description: '按文本、图片和筛选条件查询商标。',
    method: 'POST', path: '/sellersprite/trademarks/search', transport: 'multipart',
    example: { office: ['US'], text: 'CHINESE', page: 1, size: 20 },
    fileFields: [{ name: 'imageFile', label: '商标图片', accept: 'image/*' }],
  },
  {
    id: 'GLOBAL_BRAND_STATS', domain: 'trademark', name: '全球商标统计',
    description: '按文本或图片统计全球商标。',
    method: 'POST', path: '/sellersprite/trademarks/stats', transport: 'multipart',
    example: { office: ['US'], text: 'CHINESE' },
    fileFields: [{ name: 'imageFile', label: '商标图片', accept: 'image/*' }],
  },
  {
    id: 'OCR', domain: 'tool', name: '图片文字识别',
    description: '通过远程地址、Base64 或图片文件识别文字。',
    method: 'POST', path: '/sellersprite/tools/ocr', transport: 'multipart',
    example: { type: 0, fn: 'CHINESE', url: 'https://o.sellersprite.com/docs/202310/sellersprite-2023101210394300742.jpg' },
    fileFields: [{ name: 'image', label: '待识别图片', accept: 'image/*' }],
  },
] as const satisfies readonly SellerSpriteOperation[]

export function getSellerSpriteOperation(operationId: string) {
  return sellerSpriteOperations.find((operation) => operation.id === operationId)
}
