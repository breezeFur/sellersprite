import { officialSellerSpriteOperationContracts } from './officialOperationContracts.generated'
import { sellerSpriteDomains, sellerSpriteOperations } from './operations'
import type { SellerSpriteDomainId, SellerSpriteOperation } from './sellersprite'

export type GuidedFieldControl =
  | 'text'
  | 'number'
  | 'select'
  | 'multi-select'
  | 'tags'
  | 'month'
  | 'date'
  | 'period-date'
  | 'switch'
  | 'sort'
  | 'json'

export type GuidedFieldOptionValue = string | number | boolean
export interface GuidedSortValue {
  field: string
  desc: boolean
}

export type GuidedFieldValue =
  | GuidedFieldOptionValue
  | readonly string[]
  | readonly number[]
  | GuidedSortValue

export interface GuidedFieldOption {
  label: string
  value: GuidedFieldOptionValue
}

export interface GuidedField {
  key: string
  label: string
  control: GuidedFieldControl
  defaultValue: GuidedFieldValue
  options?: readonly GuidedFieldOption[]
  dictType?: string
  dictTypesByMarketplace?: Readonly<Record<string, string>>
  joinWithComma?: boolean
  required?: boolean
  placeholder?: string
  valueFormat?: 'YYYYMM' | 'YYYYMMDD'
  maxItems?: number
}

export type GuidedSellerSpriteGroupId = SellerSpriteDomainId

export interface GuidedSellerSpriteGroup {
  id: GuidedSellerSpriteGroupId
  label: string
}

export interface GuidedSellerSpritePagination {
  defaultSize: number
  pageSizes: number[]
}

export interface GuidedSellerSpriteOperation {
  id: string
  group: GuidedSellerSpriteGroupId
  fields: readonly GuidedField[]
  pagination: GuidedSellerSpritePagination
  responseShape: 'page' | 'list' | 'object'
  responseFields: readonly OfficialFieldContract[]
}

interface OfficialFieldContract {
  field: string
  type: string
  required: boolean
  name: string
  description: string
}

type CuratedGuidedOperation = Omit<GuidedSellerSpriteOperation, 'responseShape' | 'responseFields'>

export const guidedSellerSpriteGroups: readonly GuidedSellerSpriteGroup[] = sellerSpriteDomains

export const sellerSpriteMarketplaceOptions = [
  { label: '美国站', value: 'MARKET_US' },
  { label: '日本站', value: 'MARKET_JP' },
  { label: '英国站', value: 'MARKET_UK' },
  { label: '德国站', value: 'MARKET_DE' },
  { label: '法国站', value: 'MARKET_FR' },
  { label: '意大利站', value: 'MARKET_IT' },
  { label: '西班牙站', value: 'MARKET_ES' },
  { label: '加拿大站', value: 'MARKET_CA' },
  { label: '印度站', value: 'MARKET_IN' },
] as const satisfies readonly GuidedFieldOption[]

const matchTypeOptions = [
  { label: '词组匹配', value: 1 },
  { label: '模糊匹配', value: 2 },
  { label: '精准匹配', value: 3 },
] as const satisfies readonly GuidedFieldOption[]

const queryTypeOptions = [
  { label: '所有变体', value: 0 },
  { label: '畅销变体', value: 1 },
  { label: '当前变体', value: 2 },
] as const satisfies readonly GuidedFieldOption[]

const searchModelOptions = [
  { label: '热门市场', value: 1 },
  { label: '异动市场', value: 2 },
  { label: '持续增长市场', value: 3 },
  { label: '快速飙升市场', value: 4 },
  { label: '潜力市场', value: 5 },
  { label: '长尾市场', value: 6 },
] as const satisfies readonly GuidedFieldOption[]

const reverseTypeOptions = [
  { label: '按周', value: 'W' },
  { label: '按月', value: 'M' },
] as const satisfies readonly GuidedFieldOption[]

const conversionTypeOptions = [
  { label: '转化优质词', value: 'REVERSE_ASIN_CONVERSION_TYPE_EXCELLENT' },
  { label: '转化平稳词', value: 'REVERSE_ASIN_CONVERSION_TYPE_STABLE' },
  { label: '转化流失词', value: 'REVERSE_ASIN_CONVERSION_TYPE_LOST' },
  { label: '无效曝光词', value: 'REVERSE_ASIN_CONVERSION_TYPE_INVALID' },
] as const satisfies readonly GuidedFieldOption[]

const relationOptions = [
  { label: '看了又看', value: 'RELATED_PRODUCT_ASSOCIATION_TYPE_VAV' },
] as const satisfies readonly GuidedFieldOption[]

const reviewStarOptions = [
  { label: '一星', value: '1' },
  { label: '二星', value: '2' },
  { label: '三星', value: '3' },
  { label: '四星', value: '4' },
  { label: '五星', value: '5' },
] as const satisfies readonly GuidedFieldOption[]

const reviewTypeOptions = [
  { label: '图片评论', value: '1' },
  { label: '视频评论', value: '2' },
  { label: 'VP 评论', value: '3' },
  { label: 'Vine 评论', value: '4' },
] as const satisfies readonly GuidedFieldOption[]

const trademarkOfficeOptions = [
  { label: '美国', value: 'US' },
  { label: '中国', value: 'CN' },
  { label: '欧盟', value: 'EU' },
  { label: '英国', value: 'UK' },
  { label: '日本', value: 'JP' },
  { label: '德国', value: 'DE' },
  { label: '法国', value: 'FR' },
  { label: '意大利', value: 'IT' },
  { label: '西班牙', value: 'ES' },
  { label: '加拿大', value: 'CA' },
  { label: '印度', value: 'IN' },
  { label: '澳大利亚', value: 'AU' },
] as const satisfies readonly GuidedFieldOption[]

const marketplaceField = (): GuidedField => ({
  key: 'marketplace',
  label: '站点',
  control: 'select',
  defaultValue: 'MARKET_US',
  options: sellerSpriteMarketplaceOptions,
  dictType: 'MARKET',
  required: true,
  placeholder: '选择 Amazon 站点',
})

const curatedGuidedSellerSpriteOperations: readonly CuratedGuidedOperation[] = [
  {
    id: 'PRODUCT_COMPETITOR_LOOKUP',
    group: 'product',
    fields: [
      marketplaceField(),
      { key: 'month', label: '月份', control: 'month', defaultValue: '', valueFormat: 'YYYYMM', placeholder: '默认最近 30 天' },
      { key: 'keyword', label: '关键词', control: 'text', defaultValue: '', placeholder: '例如 wireless earbuds' },
      { key: 'asins', label: 'ASIN', control: 'tags', defaultValue: [], maxItems: 20, placeholder: '输入后回车，可添加多个' },
      { key: 'brand', label: '品牌', control: 'text', defaultValue: '', placeholder: '输入品牌名' },
      { key: 'sellerName', label: '卖家', control: 'text', defaultValue: '', placeholder: '输入卖家名' },
      { key: 'matchType', label: '匹配方式', control: 'select', defaultValue: 2, options: matchTypeOptions },
    ],
    pagination: { defaultSize: 50, pageSizes: [20, 50, 100] },
  },
  {
    id: 'PRODUCT_RESEARCH',
    group: 'product',
    fields: [
      marketplaceField(),
      { key: 'month', label: '月份', control: 'month', defaultValue: '', valueFormat: 'YYYYMM', placeholder: '默认最近 30 天' },
      { key: 'keyword', label: '关键词', control: 'text', defaultValue: '', placeholder: '输入产品关键词' },
      { key: 'nodeIdPaths', label: '类目路径', control: 'tags', defaultValue: [], placeholder: '输入类目路径后回车' },
      { key: 'nodeIdPathEqual', label: '精确类目', control: 'switch', defaultValue: false },
      { key: 'minPrice', label: '最低价格', control: 'number', defaultValue: '', placeholder: '不限' },
      { key: 'maxPrice', label: '最高价格', control: 'number', defaultValue: '', placeholder: '不限' },
      { key: 'minUnits', label: '最低月销量', control: 'number', defaultValue: '', placeholder: '不限' },
      { key: 'maxUnits', label: '最高月销量', control: 'number', defaultValue: '', placeholder: '不限' },
    ],
    pagination: { defaultSize: 50, pageSizes: [20, 50, 100] },
  },
  {
    id: 'KEYWORD_RESEARCH',
    group: 'keyword',
    fields: [
      marketplaceField(),
      { key: 'month', label: '月份', control: 'month', defaultValue: '', valueFormat: 'YYYYMM', placeholder: '支持近 24 个月' },
      { key: 'departments', label: '类目编码', control: 'tags', defaultValue: [], placeholder: '输入类目编码后回车' },
      { key: 'keywords', label: '关键词', control: 'text', defaultValue: '', placeholder: '输入选品关键词' },
      { key: 'excludeKeywords', label: '排除词', control: 'text', defaultValue: '', placeholder: '输入要排除的关键词' },
      { key: 'minSearches', label: '最低月搜索量', control: 'number', defaultValue: '', placeholder: '不限' },
      { key: 'maxSearches', label: '最高月搜索量', control: 'number', defaultValue: '', placeholder: '不限' },
      { key: 'withYearlyGrowth', label: '新细分市场', control: 'switch', defaultValue: false },
    ],
    pagination: { defaultSize: 15, pageSizes: [5, 10, 15] },
  },
  {
    id: 'KEYWORD_MINER',
    group: 'keyword',
    fields: [
      marketplaceField(),
      { key: 'historyDate', label: '历史月份', control: 'month', defaultValue: '', valueFormat: 'YYYYMM', placeholder: '默认最近 30 天' },
      { key: 'keyword', label: '种子词', control: 'text', defaultValue: '', required: true, placeholder: '输入要挖掘的关键词' },
      { key: 'keywordList', label: '批量关键词', control: 'tags', defaultValue: [], placeholder: '输入后回车，可添加多个' },
      { key: 'minSearch', label: '最低搜索量', control: 'number', defaultValue: '', placeholder: '不限' },
      { key: 'maxSearch', label: '最高搜索量', control: 'number', defaultValue: '', placeholder: '不限' },
    ],
    pagination: { defaultSize: 50, pageSizes: [20, 50, 100] },
  },
  {
    id: 'KEYWORD_TRAFFIC_EXTEND',
    group: 'keyword',
    fields: [
      marketplaceField(),
      { key: 'historyDate', label: '历史月份', control: 'month', defaultValue: '', valueFormat: 'YYYYMM', placeholder: '默认最近 30 天' },
      { key: 'asinList', label: 'ASIN', control: 'tags', defaultValue: [], required: true, maxItems: 20, placeholder: '输入后回车，最多 20 个' },
      { key: 'queryType', label: '查询范围', control: 'select', defaultValue: 2, options: queryTypeOptions },
      { key: 'includeKeywords', label: '包含词', control: 'tags', defaultValue: [], placeholder: '输入后回车' },
      { key: 'excludeKeywords', label: '排除词', control: 'tags', defaultValue: [], placeholder: '输入后回车' },
      { key: 'amazonChoice', label: 'Amazon 推荐词', control: 'switch', defaultValue: false },
    ],
    pagination: { defaultSize: 50, pageSizes: [10, 20, 50] },
  },
  {
    id: 'ABA_RESEARCH_WEEKLY',
    group: 'keyword',
    fields: [
      marketplaceField(),
      { key: 'date', label: '周日期', control: 'date', defaultValue: '', valueFormat: 'YYYYMMDD', placeholder: '选择周六，留空查最新周' },
      { key: 'departments', label: '类目编码', control: 'tags', defaultValue: [], placeholder: '输入类目编码后回车' },
      { key: 'includeKeywords', label: '包含词', control: 'text', defaultValue: '', placeholder: '输入包含词' },
      { key: 'excludeKeywords', label: '排除词', control: 'text', defaultValue: '', placeholder: '输入排除词' },
      { key: 'exactFlag', label: '精确匹配', control: 'switch', defaultValue: false },
      { key: 'searchModel', label: '搜索模式', control: 'select', defaultValue: '', options: searchModelOptions, placeholder: '不限' },
    ],
    pagination: { defaultSize: 40, pageSizes: [10, 20, 40] },
  },
  {
    id: 'ABA_RESEARCH_MONTHLY',
    group: 'keyword',
    fields: [
      marketplaceField(),
      { key: 'date', label: '月份', control: 'month', defaultValue: '', valueFormat: 'YYYYMM', placeholder: '留空查最近 30 天' },
      { key: 'departments', label: '类目编码', control: 'tags', defaultValue: [], placeholder: '输入类目编码后回车' },
      { key: 'includeKeywords', label: '包含词', control: 'text', defaultValue: '', placeholder: '输入包含词' },
      { key: 'excludeKeywords', label: '排除词', control: 'text', defaultValue: '', placeholder: '输入排除词' },
      { key: 'exactFlag', label: '精确匹配', control: 'switch', defaultValue: false },
      { key: 'searchModel', label: '搜索模式', control: 'select', defaultValue: '', options: searchModelOptions, placeholder: '不限' },
    ],
    pagination: { defaultSize: 15, pageSizes: [5, 10, 15] },
  },
  {
    id: 'KEYWORD_ORDER',
    group: 'keyword',
    fields: [
      marketplaceField(),
      { key: 'asins', label: 'ASIN', control: 'tags', defaultValue: [], required: true, maxItems: 20, placeholder: '输入后回车，最多 20 个' },
      { key: 'reverseType', label: '反查周期', control: 'select', defaultValue: 'W', options: reverseTypeOptions, required: true },
      { key: 'date', label: '查询日期', control: 'period-date', defaultValue: '', placeholder: '按反查周期选择日期' },
      { key: 'conversionType', label: '转化类型', control: 'multi-select', defaultValue: [], options: conversionTypeOptions, dictType: 'REVERSE_ASIN_CONVERSION_TYPE', placeholder: '不限' },
      { key: 'variation', label: '变体范围', control: 'multi-select', defaultValue: [], options: [
        { label: '不查询变体', value: 'Y' },
        { label: '查询变体', value: 'N' },
      ] },
    ],
    pagination: { defaultSize: 50, pageSizes: [50] },
  },
  {
    id: 'TRAFFIC_KEYWORD',
    group: 'traffic',
    fields: [
      marketplaceField(),
      { key: 'asin', label: 'ASIN', control: 'text', defaultValue: '', required: true, placeholder: '例如 B07Z82895W' },
      { key: 'keyword', label: '关键词', control: 'text', defaultValue: '', placeholder: '可选关键词筛选' },
      { key: 'month', label: '历史月份', control: 'month', defaultValue: '', valueFormat: 'YYYYMM', placeholder: '默认最近 30 天' },
      { key: 'badges', label: '流量词类型', control: 'tags', defaultValue: [], placeholder: '输入类型后回车' },
      { key: 'trafficKeywordTypes', label: '流量占比类型', control: 'multi-select', defaultValue: [], dictType: 'REVERSE_ASIN_SHARE_TYPE', placeholder: '请选择类型' },
      { key: 'conversionKeywordTypes', label: '流量转化类型', control: 'multi-select', defaultValue: [], dictType: 'REVERSE_ASIN_CONVERSION_TYPE', placeholder: '请选择类型' },
    ],
    pagination: { defaultSize: 50, pageSizes: [20, 50, 100] },
  },
  {
    id: 'TRAFFIC_LISTING_PAGE',
    group: 'traffic',
    fields: [
      marketplaceField(),
      { key: 'asinList', label: 'ASIN', control: 'tags', defaultValue: [], required: true, placeholder: '输入后回车，可添加多个' },
      { key: 'relations', label: '关联类型', control: 'multi-select', defaultValue: ['RELATED_PRODUCT_ASSOCIATION_TYPE_VAV'], options: relationOptions, dictType: 'RELATED_PRODUCT_ASSOCIATION_TYPE', required: true },
      { key: 'variations', label: '查询变体', control: 'switch', defaultValue: false },
    ],
    pagination: { defaultSize: 50, pageSizes: [20, 50, 100] },
  },
  {
    id: 'TRAFFIC_SOURCE',
    group: 'traffic',
    fields: [
      marketplaceField(),
      { key: 'q', label: 'ASIN 或关键词', control: 'text', defaultValue: '', required: true, placeholder: '输入 ASIN 或关键词' },
      { key: 'month', label: '月份', control: 'month', defaultValue: '', valueFormat: 'YYYYMM', required: true, placeholder: '选择查询月份' },
    ],
    pagination: { defaultSize: 50, pageSizes: [20, 50, 100] },
  },
  {
    id: 'MARKET_RESEARCH',
    group: 'market',
    fields: [
      marketplaceField(),
      { key: 'month', label: '月份', control: 'month', defaultValue: '', valueFormat: 'YYYYMM', placeholder: '默认最近 30 天' },
      { key: 'nodeIdPath', label: '类目路径', control: 'text', defaultValue: '', placeholder: '例如 172282:281407' },
      { key: 'departmentKeyword', label: '类目关键词', control: 'text', defaultValue: '', placeholder: '输入类目关键词' },
      { key: 'minAvgUnits', label: '最低月均销量', control: 'number', defaultValue: '', placeholder: '不限' },
      { key: 'maxAvgUnits', label: '最高月均销量', control: 'number', defaultValue: '', placeholder: '不限' },
      { key: 'minAvgRevenue', label: '最低月均销售额', control: 'number', defaultValue: '', placeholder: '不限' },
      { key: 'maxAvgRevenue', label: '最高月均销售额', control: 'number', defaultValue: '', placeholder: '不限' },
    ],
    pagination: { defaultSize: 50, pageSizes: [20, 50, 100, 200] },
  },
  {
    id: 'REVIEW_LIST',
    group: 'review',
    fields: [
      marketplaceField(),
      { key: 'asin', label: 'ASIN', control: 'text', defaultValue: '', required: true, placeholder: '例如 B07Z82895W' },
      { key: 'starList', label: '评论星级', control: 'multi-select', defaultValue: [], options: reviewStarOptions, placeholder: '全部星级' },
      { key: 'typeList', label: '评论类型', control: 'multi-select', defaultValue: [], options: reviewTypeOptions, placeholder: '全部类型' },
    ],
    pagination: { defaultSize: 5, pageSizes: [5, 10] },
  },
  {
    id: 'GLOBAL_BRAND_LIST',
    group: 'trademark',
    fields: [
      { key: 'office', label: '注册局', control: 'multi-select', defaultValue: ['US'], options: trademarkOfficeOptions, placeholder: '选择注册局' },
      { key: 'text', label: '查询文本', control: 'text', defaultValue: '', required: true, placeholder: '输入商标或品牌文本' },
      { key: 'brandName', label: '品牌名', control: 'tags', defaultValue: [], placeholder: '输入后回车，可添加多个' },
      { key: 'status', label: '状态', control: 'tags', defaultValue: [], placeholder: '例如 Registered' },
      { key: 'applicant', label: '申请人', control: 'tags', defaultValue: [], placeholder: '输入后回车，可添加多个' },
      { key: 'niceClass', label: '尼斯分类', control: 'tags', defaultValue: [], placeholder: '输入分类后回车' },
      { key: 'orderField', label: '排序字段', control: 'select', defaultValue: '', options: [
        { label: '相关度', value: '' },
        { label: '申请日期', value: 'applicationDate' },
      ] },
      { key: 'orderDesc', label: '降序', control: 'switch', defaultValue: true },
    ],
    pagination: { defaultSize: 20, pageSizes: [20, 50, 100] },
  },
]

const PRODUCT_FIELD_DICTIONARIES: Readonly<Record<string, string>> = {
  availableMonth: 'LISTING_DATE',
  marketplace: 'MARKET',
  sellerNation: 'SELLER_NATIONALITY',
  weightUnit: 'PRODUCT_WEIGHT_UNIT',
}

const PRODUCT_SIZE_DICTIONARIES_BY_MARKETPLACE: Readonly<Record<string, string>> = {
  MARKET_US: 'PRODUCT_SIZE_US',
  MARKET_JP: 'PRODUCT_SIZE_JP',
  MARKET_CA: 'PRODUCT_SIZE_CA',
  MARKET_UK: 'PRODUCT_SIZE_EU',
  MARKET_DE: 'PRODUCT_SIZE_EU',
  MARKET_FR: 'PRODUCT_SIZE_EU',
  MARKET_IT: 'PRODUCT_SIZE_EU',
  MARKET_ES: 'PRODUCT_SIZE_EU',
}

const SORT_DICTIONARIES_BY_OPERATION: Readonly<Record<string, string>> = {
  PRODUCT_COMPETITOR_LOOKUP: 'PRODUCT_SORT_FIELD',
  PRODUCT_RESEARCH: 'PRODUCT_SORT_FIELD',
  KEYWORD_RESEARCH: 'KEYWORD_RESEARCH_SORT_FIELD',
  KEYWORD_MINER: 'ABA_SORT_FIELD',
  KEYWORD_TRAFFIC_EXTEND: 'REVERSE_MULTIPLE_ASIN_SORT_FIELD',
  ABA_RESEARCH_WEEKLY: 'ABA_SORT_FIELD',
  ABA_RESEARCH_MONTHLY: 'ABA_SORT_FIELD',
  KEYWORD_ORDER: 'KEYWORD_EXPLORER_SORT_FIELD',
  TRAFFIC_KEYWORD: 'REVERSE_ASIN_SORT_FIELD',
  TRAFFIC_LISTING_PAGE: 'RELATED_PRODUCT_ASSOCIATION_TYPE',
  TRAFFIC_SOURCE: 'ABA_SORT_FIELD',
  MARKET_RESEARCH: 'PRODUCT_SORT_FIELD',
}

const DEFAULT_SORT_LABELS: Readonly<Record<string, string>> = {
  PRODUCT_COMPETITOR_LOOKUP: 'PRODUCT_SORT_FIELD_TOTAL_UNITS',
  PRODUCT_RESEARCH: 'PRODUCT_SORT_FIELD_TOTAL_UNITS',
  TRAFFIC_KEYWORD: 'REVERSE_ASIN_SORT_FIELD_RANK_POSITION',
}

const ASCENDING_BY_DEFAULT_OPERATIONS = new Set(['KEYWORD_ORDER', 'TRAFFIC_KEYWORD'])

const YES_NO_OPTIONS = [
  { label: '是', value: 'Y' },
  { label: '否', value: 'N' },
] as const satisfies readonly GuidedFieldOption[]

const PRODUCT_FLAG_FIELDS = new Set(['badgeAC', 'badgeBS', 'badgeNR', 'filterSub', 'variation'])
const OMITTED_FORM_FIELDS = new Set(['page', 'size'])

export const guidedSellerSpriteOperations: readonly GuidedSellerSpriteOperation[] = sellerSpriteOperations.map(
  (operation) => buildGuidedOperation(operation),
)

function buildGuidedOperation(operation: SellerSpriteOperation): GuidedSellerSpriteOperation {
  const contract = officialSellerSpriteOperationContracts.find((item) => item.operation === operation.id)
  const curated = curatedGuidedSellerSpriteOperations.find((item) => item.id === operation.id)
  const curatedFields = new Map(curated?.fields.map((field) => [field.key, field]) ?? [])
  const requestFields = contract?.requestFields
    .filter((field) => !field.field.startsWith('└') && !OMITTED_FORM_FIELDS.has(field.field))
    .filter((field) => !isFileField(field))
    .map((field) => curatedFields.get(field.field) ?? buildOfficialField(field, operation))
    ?? []

  return {
    id: operation.id,
    group: operation.domain,
    fields: requestFields,
    pagination: curated?.pagination ?? defaultPagination(contract?.responseShape === 'page'),
    responseShape: contract?.responseShape ?? 'object',
    responseFields: contract?.responseFields ?? [],
  }
}

function buildOfficialField(field: OfficialFieldContract, operation: SellerSpriteOperation): GuidedField {
  const exampleValue = operation.example[field.field]
  const dictType = PRODUCT_FIELD_DICTIONARIES[field.field]
  const sortDictType = field.field === 'order' ? SORT_DICTIONARIES_BY_OPERATION[operation.id] : undefined
  const common = {
    key: field.field,
    label: field.name || field.field,
    required: field.required,
    placeholder: field.description || `请输入${field.name || field.field}`,
  }

  if (field.field === 'marketplace') return marketplaceField()
  if (sortDictType) {
    return {
      ...common,
      control: 'sort',
      defaultValue: {
        field: DEFAULT_SORT_LABELS[operation.id] ?? '',
        desc: !ASCENDING_BY_DEFAULT_OPERATIONS.has(operation.id),
      },
      dictType: sortDictType,
    }
  }
  if (operation.id === 'PRODUCT_RESEARCH' && field.field === 'dimensionType') {
    return {
      ...common,
      control: 'multi-select',
      defaultValue: [],
      dictTypesByMarketplace: PRODUCT_SIZE_DICTIONARIES_BY_MARKETPLACE,
      joinWithComma: true,
    }
  }
  if (operation.id === 'PRODUCT_RESEARCH' && field.field === 'sellerNation') {
    return {
      ...common,
      control: 'multi-select',
      defaultValue: [],
      dictType: 'SELLER_NATIONALITY',
      joinWithComma: true,
    }
  }
  if (PRODUCT_FLAG_FIELDS.has(field.field)) {
    return { ...common, control: 'select', defaultValue: stringDefault(exampleValue), options: YES_NO_OPTIONS }
  }
  if (dictType) {
    return {
      ...common,
      control: field.type.includes('List') ? 'multi-select' : 'select',
      defaultValue: field.type.includes('List') ? arrayDefault(exampleValue) : stringDefault(exampleValue),
      dictType,
    }
  }
  if (isMonthField(field)) {
    return { ...common, control: 'month', defaultValue: stringDefault(exampleValue), valueFormat: 'YYYYMM' }
  }
  if (isDateField(field)) {
    return { ...common, control: 'date', defaultValue: stringDefault(exampleValue), valueFormat: 'YYYYMMDD' }
  }
  if (field.type.includes('List') || field.type.includes('Array')) {
    return { ...common, control: 'tags', defaultValue: arrayDefault(exampleValue) }
  }
  if (field.type === 'Boolean' || field.type === 'boolean') {
    return { ...common, control: 'switch', defaultValue: exampleValue === true }
  }
  if (/Integer|Long|Float|Double|Decimal|Number/.test(field.type)) {
    return { ...common, control: 'number', defaultValue: numberDefault(exampleValue) }
  }
  if (field.type !== 'String') {
    return { ...common, control: 'json', defaultValue: jsonDefault(exampleValue) }
  }
  return { ...common, control: 'text', defaultValue: stringDefault(exampleValue) }
}

function defaultPagination(paginated: boolean): GuidedSellerSpritePagination {
  return paginated
    ? { defaultSize: 50, pageSizes: [20, 50, 100] }
    : { defaultSize: 50, pageSizes: [50] }
}

function isFileField(field: OfficialFieldContract) {
  return /MultipartFile|File/.test(field.type)
}

function isMonthField(field: OfficialFieldContract) {
  return field.field === 'month'
    || field.field === 'historyDate'
    || (field.field === 'date' && /yyyyMM(?!dd)/i.test(field.description))
}

function isDateField(field: OfficialFieldContract) {
  return field.field === 'date' && /yyyyMMdd/i.test(field.description)
}

function stringDefault(value: unknown) {
  return typeof value === 'string' ? value : ''
}

function numberDefault(value: unknown) {
  return typeof value === 'number' ? value : ''
}

function arrayDefault(value: unknown): readonly string[] | readonly number[] {
  if (!Array.isArray(value)) return []
  if (value.every((item) => typeof item === 'number')) return value as number[]
  return value.filter((item): item is string => typeof item === 'string')
}

function jsonDefault(value: unknown) {
  if (value === undefined || value === null) return ''
  return JSON.stringify(value, null, 2)
}

export function getGuidedSellerSpriteOperation(operationId: string) {
  return guidedSellerSpriteOperations.find((operation) => operation.id === operationId)
}
