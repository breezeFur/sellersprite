<script setup lang="ts">
import {
  CopyDocument,
  Download,
  Refresh,
  Search,
  UploadFilled,
  WarningFilled,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref, watch } from 'vue'

import { ApiError } from '@/shared/api/ApiError'
import type { PageResult } from '@/shared/api/types'
import StatePanel from '@/shared/components/StatePanel.vue'
import { getEnabledDictionary } from '@/features/system/api/dictionaryApi'
import type { DictionaryType } from '@/features/system/model/system'

import { adaptSellerSpritePage } from '../api/sellerSpritePagination'
import { executeSellerSpriteOperation } from '../api/sellerspriteApi'
import {
  getGuidedSellerSpriteOperation,
  guidedSellerSpriteGroups,
  guidedSellerSpriteOperations,
  type GuidedSortValue,
  type GuidedSellerSpriteOperation,
  type GuidedSellerSpriteGroupId,
} from '../model/guidedOperations'
import { getSellerSpriteOperation } from '../model/operations'
import type {
  SellerSpriteExecutionResult,
  SellerSpriteOperation,
  SellerSpriteRequestPayload,
} from '../model/sellersprite'
import { exportSellerSpriteQueryResult } from '../utils/sellerSpriteExcelExport'

interface GuidedQueryError {
  code: string
  message: string
  trackId?: string
}

type DisplayRow = Record<string, unknown> & { __rowKey: string }

const props = withDefaults(defineProps<{
  initialGroup?: GuidedSellerSpriteGroupId
}>(), {
  initialGroup: 'product',
})

const firstConfig = guidedSellerSpriteOperations.find((operation) => operation.group === props.initialGroup)
  ?? guidedSellerSpriteOperations[0]
if (!firstConfig) {
  throw new Error('SellerSprite 引导操作目录不能为空')
}

const selectedGroup = ref(firstConfig.group)
const selectedConfigId = ref(firstConfig.id)
const formModel = ref<SellerSpriteRequestPayload>(createFormModel(firstConfig))
const selectedFiles = ref<Record<string, File | undefined>>({})
const validationErrors = ref<Record<string, string>>({})
const submitting = ref(false)
const exporting = ref(false)
const executionResult = ref<SellerSpriteExecutionResult | null>(null)
const pageResult = ref<PageResult<Record<string, unknown>> | null>(null)
const pageMetadata = ref<Record<string, unknown>>({})
const queryError = ref<GuidedQueryError | null>(null)
const currentPage = ref(1)
const pageSize = ref(firstConfig.pagination.defaultSize)
const lastSubmittedFilters = ref<SellerSpriteRequestPayload | null>(null)
const appendixDictionaries = ref<Record<string, DictionaryType>>({})

const selectedConfig = computed<GuidedSellerSpriteOperation>(() => (
  getGuidedSellerSpriteOperation(selectedConfigId.value) ?? firstConfig
))

const selectedOperation = computed<SellerSpriteOperation>(() => {
  const operation = getSellerSpriteOperation(selectedConfig.value.id)
  if (!operation) {
    throw new Error(`未找到 SellerSprite 操作：${selectedConfig.value.id}`)
  }
  return operation
})

const visibleConfigs = computed(() => (
  guidedSellerSpriteOperations.filter((item) => item.group === selectedGroup.value)
))

const rawResultSource = computed(() => {
  if (!executionResult.value) return ''
  try {
    return JSON.stringify(executionResult.value.data, null, 2)
  } catch {
    return String(executionResult.value.data)
  }
})

const resultRows = computed<Record<string, unknown>[]>(() => {
  if (pageResult.value) return pageResult.value.records
  const data = executionResult.value?.data
  if (Array.isArray(data)) return data.map(toDisplayRecord)
  if (isRecord(data)) return [data]
  if (data === undefined || data === null || data === '') return []
  return [{ value: data }]
})

const displayRows = computed<DisplayRow[]>(() => (
  resultRows.value.map((row, index) => ({
    ...row,
    __rowKey: createRowKey(row, index),
  }))
))

const canExportResult = computed(() => executionResult.value !== null && resultRows.value.length > 0)

const tableColumns = computed(() => {
  const keys = new Set<string>()
  for (const row of pageResult.value?.records.slice(0, 20) ?? []) {
    Object.keys(row).forEach((key) => keys.add(key))
  }
  const preferred = [
    'asin', 'keyword', 'title', 'brand', 'price', 'sales', 'revenue', 'searches',
    'purchases', 'rating', 'ratings', 'office', 'brandName', 'status', 'author', 'star',
  ]
  const documented = selectedConfig.value.responseFields
    .filter((field) => !field.field.startsWith('└'))
    .map((field) => field.field)
  return [
    ...preferred.filter((key) => keys.delete(key)),
    ...documented.filter((key) => keys.delete(key)),
    ...keys,
  ]
})

const metadataSummary = computed(() => {
  const metadata = pageMetadata.value
  return [
    metadata.took !== undefined ? `上游 ${metadata.took} ms` : '',
    metadata.pages !== undefined ? `共 ${metadata.pages} 页` : '',
    metadata.hasNextPage !== undefined ? (metadata.hasNextPage ? '还有下一页' : '已到末页') : '',
  ].filter(Boolean).join(' · ')
})

onMounted(() => {
  void loadAppendixDictionaries()
})

watch(() => props.initialGroup, (group) => {
  if (group !== selectedGroup.value) selectGroup(group)
})

async function loadAppendixDictionaries() {
  const dictTypes = [...new Set(guidedSellerSpriteOperations.flatMap((operation) =>
    operation.fields.flatMap((field) => [
      field.dictType,
      ...Object.values(field.dictTypesByMarketplace ?? {}),
    ]).filter((dictType): dictType is string => Boolean(dictType)),
  ))]
  const entries = await Promise.all(dictTypes.map(async (dictType) => {
    try {
      return [dictType, await getEnabledDictionary(dictType)] as const
    } catch {
      return null
    }
  }))
  appendixDictionaries.value = Object.fromEntries(entries.filter((entry) => entry !== null))
}

function fieldOptions(field: GuidedSellerSpriteOperation['fields'][number]) {
  const dictType = field.dictTypesByMarketplace
    ? field.dictTypesByMarketplace[String(formModel.value.marketplace ?? '')]
    : field.dictType
  if (!dictType) return field.options ?? []
  const dictionary = appendixDictionaries.value[dictType]
  if (!dictionary) return field.options ?? []
  return dictionary.items.map((item) => ({ label: item.dictName, value: item.dictLabel }))
}

function selectGroup(groupId: GuidedSellerSpriteGroupId) {
  selectedGroup.value = groupId
  const first = guidedSellerSpriteOperations.find((item) => item.group === groupId)
  if (first) selectOperation(first)
}

function selectOperation(config: GuidedSellerSpriteOperation) {
  selectedConfigId.value = config.id
  formModel.value = createFormModel(config)
  selectedFiles.value = {}
  validationErrors.value = {}
  currentPage.value = 1
  pageSize.value = config.pagination.defaultSize
  lastSubmittedFilters.value = null
  resetResult()
}

function resetForm() {
  formModel.value = createFormModel(selectedConfig.value)
  selectedFiles.value = {}
  validationErrors.value = {}
  currentPage.value = 1
  pageSize.value = selectedConfig.value.pagination.defaultSize
  lastSubmittedFilters.value = null
  resetResult()
}

function updateFile(fieldName: string, event: Event) {
  const input = event.target as HTMLInputElement
  selectedFiles.value = {
    ...selectedFiles.value,
    [fieldName]: input.files?.[0],
  }
}

function setFieldValue(fieldKey: string, value: unknown) {
  if (fieldKey === 'marketplace' && formModel.value.marketplace !== value) {
    for (const field of selectedConfig.value.fields.filter((item) => item.dictTypesByMarketplace)) {
      formModel.value[field.key] = []
    }
  }
  formModel.value[fieldKey] = value
  if (validationErrors.value[fieldKey]) {
    validationErrors.value = { ...validationErrors.value, [fieldKey]: '' }
  }
}

function selectFieldValue(fieldKey: string) {
  const value = formModel.value[fieldKey]
  return typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean'
    ? value
    : undefined
}

function arrayFieldValue(fieldKey: string): Array<string | number> {
  const value = formModel.value[fieldKey]
  return Array.isArray(value)
    ? value.filter((item): item is string | number => typeof item === 'string' || typeof item === 'number')
    : []
}

function stringFieldValue(fieldKey: string) {
  const value = formModel.value[fieldKey]
  return typeof value === 'string' ? value : ''
}

function numberFieldValue(fieldKey: string) {
  const value = formModel.value[fieldKey]
  return typeof value === 'number' ? value : undefined
}

function booleanFieldValue(fieldKey: string) {
  return formModel.value[fieldKey] === true
}

function sortFieldValue(fieldKey: string) {
  const value = formModel.value[fieldKey]
  return isSortValue(value) ? value.field : ''
}

function sortDescValue(fieldKey: string) {
  const value = formModel.value[fieldKey]
  return isSortValue(value) ? value.desc : true
}

function setSortFieldValue(fieldKey: string, field: unknown) {
  const current = formModel.value[fieldKey]
  setFieldValue(fieldKey, {
    field: typeof field === 'string' ? field : '',
    desc: isSortValue(current) ? current.desc : true,
  })
}

function setSortDescValue(fieldKey: string, desc: unknown) {
  const current = formModel.value[fieldKey]
  setFieldValue(fieldKey, {
    field: isSortValue(current) ? current.field : '',
    desc: desc === true,
  })
}

async function submitNewQuery() {
  let filters: SellerSpriteRequestPayload
  try {
    filters = normalizeStructuredFields(cleanPayload(formModel.value))
  } catch {
    return
  }
  if (!validateFilters(filters)) return
  currentPage.value = 1
  lastSubmittedFilters.value = clonePayload(filters)
  await executeQuery(filters, 1, pageSize.value)
}

async function changeCurrentPage(page: number) {
  if (!lastSubmittedFilters.value || submitting.value) return
  await executeQuery(lastSubmittedFilters.value, page, pageSize.value)
}

async function changePageSize(size: number) {
  pageSize.value = size
  if (!lastSubmittedFilters.value || submitting.value) return
  await executeQuery(lastSubmittedFilters.value, 1, size)
}

async function executeQuery(filters: SellerSpriteRequestPayload, page: number, size: number) {
  if (submitting.value) return

  const payload: SellerSpriteRequestPayload = clonePayload(filters)
  if (selectedConfig.value.responseShape === 'page') {
    payload.page = page
    payload.size = size
  }
  submitting.value = true
  queryError.value = null
  executionResult.value = null
  pageResult.value = null
  pageMetadata.value = {}
  try {
    const result = await executeSellerSpriteOperation(
      selectedOperation.value,
      payload,
      selectedFiles.value,
    )
    const adapted = adaptSellerSpritePage(result.data, payload)
    executionResult.value = result
    if (adapted) {
      pageResult.value = adapted.page
      pageMetadata.value = adapted.metadata
      currentPage.value = adapted.page.current
      pageSize.value = adapted.page.size
    }
  } catch (error) {
    queryError.value = normalizeQueryError(error)
  } finally {
    submitting.value = false
  }
}

function validateFilters(payload: SellerSpriteRequestPayload) {
  const errors: Record<string, string> = {}
  for (const field of selectedConfig.value.fields) {
    if (field.required && isEmptyValue(payload[field.key])) {
      errors[field.key] = `请选择或填写${field.label}`
    }
  }
  validationErrors.value = errors
  return Object.keys(errors).length === 0
}

function resetResult() {
  executionResult.value = null
  pageResult.value = null
  pageMetadata.value = {}
  queryError.value = null
}

async function copyRawResult() {
  if (!rawResultSource.value) return
  if (!navigator.clipboard) {
    ElMessage.warning('当前浏览器不支持剪贴板写入')
    return
  }
  try {
    await navigator.clipboard.writeText(rawResultSource.value)
    ElMessage.success('原始响应已复制')
  } catch {
    ElMessage.error('复制失败，请手动选择响应内容')
  }
}

async function exportCurrentResult() {
  if (!canExportResult.value || exporting.value) return

  const operation = selectedOperation.value
  const rows = resultRows.value
  const columns = tableColumns.value.map((key) => ({ key, label: columnLabel(key) }))
  exporting.value = true
  try {
    const exported = await exportSellerSpriteQueryResult({
      operationId: operation.id,
      operationName: operation.name,
      data: rows,
      columns,
    })
    ElMessage.success(`已导出本次查询结果（${exported.rowCount} 条）`)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '导出失败，请稍后重试')
  } finally {
    exporting.value = false
  }
}

function createFormModel(config: GuidedSellerSpriteOperation): SellerSpriteRequestPayload {
  return Object.fromEntries(config.fields.map((field) => [
    field.key,
    cloneValue(field.defaultValue),
  ]))
}

function cleanPayload(payload: SellerSpriteRequestPayload): SellerSpriteRequestPayload {
  return Object.fromEntries(Object.entries(payload).filter(([, value]) => !isEmptyValue(value)))
}

function normalizeStructuredFields(payload: SellerSpriteRequestPayload): SellerSpriteRequestPayload {
  const normalized = clonePayload(payload)
  for (const field of selectedConfig.value.fields.filter((item) => item.joinWithComma)) {
    const value = normalized[field.key]
    if (Array.isArray(value)) {
      normalized[field.key] = value.join(',')
    }
  }
  for (const field of selectedConfig.value.fields.filter((item) => item.control === 'json')) {
    const value = normalized[field.key]
    if (typeof value !== 'string' || value.trim() === '') continue
    try {
      normalized[field.key] = JSON.parse(value) as unknown
    } catch {
      validationErrors.value = {
        ...validationErrors.value,
        [field.key]: `${field.label}必须是有效 JSON`,
      }
      throw new Error(`${field.label}必须是有效 JSON`)
    }
  }
  return normalized
}

function clonePayload(payload: SellerSpriteRequestPayload): SellerSpriteRequestPayload {
  return Object.fromEntries(Object.entries(payload).map(([key, value]) => [key, cloneValue(value)]))
}

function cloneValue(value: unknown): unknown {
  if (Array.isArray(value)) return value.map(cloneValue)
  if (value !== null && typeof value === 'object') return { ...value }
  return value
}

function isEmptyValue(value: unknown) {
  return value === undefined
    || value === null
    || (typeof value === 'string' && value.trim() === '')
    || (Array.isArray(value) && value.length === 0)
}

function normalizeQueryError(error: unknown): GuidedQueryError {
  if (error instanceof ApiError) {
    return { code: error.code, message: error.message, trackId: error.trackId }
  }
  if (error instanceof Error) {
    return { code: 'REQUEST_FAILED', message: error.message || '请求失败，请稍后重试。' }
  }
  return { code: 'REQUEST_FAILED', message: '请求失败，请稍后重试。' }
}

function createRowKey(row: Record<string, unknown>, index: number) {
  const identity = row.asin ?? row.keyword ?? row.id ?? row.brandId ?? row.title ?? 'row'
  return `${String(identity)}-${currentPage.value}-${index}`
}

function toDisplayRecord(value: unknown): Record<string, unknown> {
  return isRecord(value) ? value : { value }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === 'object' && !Array.isArray(value)
}

function isSortValue(value: unknown): value is GuidedSortValue {
  return isRecord(value) && typeof value.field === 'string' && typeof value.desc === 'boolean'
}

function formatCell(value: unknown) {
  if (value === undefined || value === null || value === '') return '--'
  if (typeof value === 'boolean') return value ? '是' : '否'
  if (typeof value === 'object') {
    try {
      return JSON.stringify(value)
    } catch {
      return String(value)
    }
  }
  return String(value)
}

function columnLabel(key: string) {
  const labels: Record<string, string> = {
    asin: 'ASIN',
    keyword: '关键词',
    title: '标题',
    brand: '品牌',
    brandUrl: '品牌链接',
    imageUrl: '商品主图',
    parent: '父体 ASIN',
    nodeLabelPath: '类目路径',
    nodeIdPath: '类目 ID 路径',
    nodeId: '类目 ID',
    symbol: '商品标识',
    bsrId: 'BSR 类目',
    bsr: 'BSR 排名',
    bsrCr: 'BSR 增长率',
    bsrCv: 'BSR 增长数',
    amzUnit: '子体月销量',
    amzUnitDate: '子体销量更新时间',
    amzSales: '子体月销售额',
    units: '父体月销量',
    unitsGr: '月销量增长率',
    price: '价格',
    averagePrice: '平均价格',
    primePrice: 'Prime 价格',
    profit: '毛利率',
    fba: 'FBA 运费',
    sales: '销量',
    revenue: '销售额',
    searches: '搜索量',
    purchases: '购买量',
    rating: '评分',
    ratings: '评分数',
    ratingsRate: '留评率',
    ratingsCv: '月新增评分数',
    ratingDelta: '近 30 天新增评论',
    availableDate: '上架时间',
    fulfillment: '配送方式',
    variations: '变体数',
    sellers: '卖家数',
    sellerId: 'Buy Box 卖家 ID',
    sellerName: 'Buy Box 卖家',
    sellerNation: '卖家国籍',
    lqs: 'Listing 质量分',
    weight: '商品重量',
    dimension: '商品尺寸',
    dimensionsType: '商品尺寸类型',
    pkgDimensions: '包装尺寸',
    pkgDimensionType: '包装尺寸类型',
    pkgWeight: '包装重量',
    sku: 'SKU 属性',
    deliveryPrice: '卖家运费',
    badge: '商品徽章',
    subcategories: '子类目排名',
    office: '注册局',
    brandName: '商标名',
    status: '状态',
    author: '评论者',
    star: '星级',
  }
  const documented = selectedConfig.value.responseFields.find((field) => field.field === key)
  return labels[key] ?? documented?.name ?? key
}
</script>

<template>
  <section
    class="guided-workbench"
    aria-label="SellerSprite 引导查询"
  >
    <aside
      class="guided-catalog"
      aria-label="引导查询分类"
    >
      <nav class="guided-groups">
        <button
          v-for="group in guidedSellerSpriteGroups"
          :key="group.id"
          type="button"
          :data-guided-group-id="group.id"
          :class="{ active: selectedGroup === group.id }"
          @click="selectGroup(group.id)"
        >
          <span>{{ group.label }}</span>
          <small>{{ guidedSellerSpriteOperations.filter((item) => item.group === group.id).length }}</small>
        </button>
      </nav>

      <div class="guided-operations">
        <button
          v-for="config in visibleConfigs"
          :key="config.id"
          type="button"
          :class="{ active: selectedConfigId === config.id }"
          :data-guided-operation-id="config.id"
          @click="selectOperation(config)"
        >
          <ElIcon><Search /></ElIcon>
          <span>
            <strong>{{ getSellerSpriteOperation(config.id)?.name }}</strong>
            <small>{{ getSellerSpriteOperation(config.id)?.description }}</small>
          </span>
        </button>
      </div>
    </aside>

    <section
      class="guided-form-panel"
      aria-label="引导查询条件"
    >
      <header class="guided-panel-heading">
        <div>
          <span>{{ selectedOperation.id }}</span>
          <h2>{{ selectedOperation.name }}</h2>
          <p>{{ selectedOperation.description }}</p>
        </div>
        <ElTooltip content="恢复默认查询条件">
          <ElButton
            :icon="Refresh"
            circle
            aria-label="恢复引导查询条件"
            @click="resetForm"
          />
        </ElTooltip>
      </header>

      <ElForm
        class="guided-form"
        label-position="top"
        :model="formModel"
        @submit.prevent="submitNewQuery"
      >
        <ElFormItem
          v-for="field in selectedConfig.fields"
          :key="field.key"
          :label="field.label"
          :required="field.required"
          :data-guided-field="field.key"
        >
          <ElSelect
            v-if="field.control === 'select'"
            :model-value="selectFieldValue(field.key)"
            :placeholder="field.placeholder ?? `请选择${field.label}`"
            clearable
            filterable
            @update:model-value="setFieldValue(field.key, $event)"
          >
            <ElOption
              v-for="option in fieldOptions(field)"
              :key="String(option.value)"
              :label="option.label"
              :value="option.value"
            />
          </ElSelect>

          <ElSelect
            v-else-if="field.control === 'multi-select' || field.control === 'tags'"
            :model-value="arrayFieldValue(field.key)"
            :placeholder="field.placeholder ?? `请选择${field.label}`"
            :allow-create="field.control === 'tags'"
            :reserve-keyword="false"
            clearable
            filterable
            multiple
            @update:model-value="setFieldValue(field.key, $event)"
          >
            <ElOption
              v-for="option in fieldOptions(field)"
              :key="String(option.value)"
              :label="option.label"
              :value="option.value"
            />
          </ElSelect>

          <ElDatePicker
            v-else-if="field.control === 'month'"
            :model-value="stringFieldValue(field.key)"
            type="month"
            format="YYYY-MM"
            :value-format="field.valueFormat ?? 'YYYYMM'"
            :placeholder="field.placeholder ?? `请选择${field.label}`"
            clearable
            @update:model-value="setFieldValue(field.key, $event)"
          />

          <ElDatePicker
            v-else-if="field.control === 'date'"
            :model-value="stringFieldValue(field.key)"
            type="date"
            format="YYYY-MM-DD"
            :value-format="field.valueFormat ?? 'YYYYMMDD'"
            :placeholder="field.placeholder ?? `请选择${field.label}`"
            clearable
            @update:model-value="setFieldValue(field.key, $event)"
          />

          <ElDatePicker
            v-else-if="field.control === 'period-date'"
            :model-value="stringFieldValue(field.key)"
            :type="formModel.reverseType === 'M' ? 'month' : 'date'"
            :format="formModel.reverseType === 'M' ? 'YYYY-MM' : 'YYYY-MM-DD'"
            :value-format="formModel.reverseType === 'M' ? 'YYYYMM' : 'YYYYMMDD'"
            :placeholder="field.placeholder ?? `请选择${field.label}`"
            clearable
            @update:model-value="setFieldValue(field.key, $event)"
          />

          <ElInputNumber
            v-else-if="field.control === 'number'"
            :model-value="numberFieldValue(field.key)"
            controls-position="right"
            @update:model-value="setFieldValue(field.key, $event)"
          />

          <ElSwitch
            v-else-if="field.control === 'switch'"
            :model-value="booleanFieldValue(field.key)"
            @update:model-value="setFieldValue(field.key, $event)"
          />

          <div
            v-else-if="field.control === 'sort'"
            class="guided-sort-control"
          >
            <ElSelect
              :model-value="sortFieldValue(field.key)"
              :placeholder="field.placeholder ?? '请选择排序字段'"
              clearable
              filterable
              @update:model-value="setSortFieldValue(field.key, $event)"
            >
              <ElOption
                v-for="option in fieldOptions(field)"
                :key="String(option.value)"
                :label="option.label"
                :value="option.value"
              />
            </ElSelect>
            <label>
              <span>降序</span>
              <ElSwitch
                :model-value="sortDescValue(field.key)"
                aria-label="排序方式：降序"
                @update:model-value="setSortDescValue(field.key, $event)"
              />
            </label>
          </div>

          <ElInput
            v-else-if="field.control === 'json'"
            :model-value="stringFieldValue(field.key)"
            type="textarea"
            :autosize="{ minRows: 3, maxRows: 8 }"
            :placeholder="field.placeholder ?? `请输入${field.label} JSON`"
            @update:model-value="setFieldValue(field.key, $event)"
          />

          <ElInput
            v-else
            :model-value="stringFieldValue(field.key)"
            :placeholder="field.placeholder ?? `请输入${field.label}`"
            clearable
            @update:model-value="setFieldValue(field.key, $event)"
          />
          <p
            v-if="validationErrors[field.key]"
            class="guided-field-error"
            role="alert"
          >
            {{ validationErrors[field.key] }}
          </p>
        </ElFormItem>

        <div
          v-if="selectedOperation.fileFields?.length"
          class="guided-files"
        >
          <label
            v-for="field in selectedOperation.fileFields"
            :key="field.name"
          >
            <input
              type="file"
              :accept="field.accept"
              :aria-label="field.label"
              @change="updateFile(field.name, $event)"
            >
            <ElIcon><UploadFilled /></ElIcon>
            <span>
              <strong>{{ field.label }}</strong>
              <small>{{ selectedFiles[field.name]?.name ?? '未选择文件' }}</small>
            </span>
          </label>
        </div>
      </ElForm>

      <footer class="guided-form-actions">
        <span><code>/api{{ selectedOperation.path }}</code></span>
        <ElButton
          type="primary"
          :loading="submitting"
          :disabled="submitting"
          aria-label="执行引导查询"
          @click="submitNewQuery"
        >
          查询
        </ElButton>
      </footer>
    </section>

    <section
      class="guided-results"
      aria-label="引导查询结果"
    >
      <header class="guided-result-heading">
        <div>
          <h2>查询结果</h2>
          <p v-if="pageResult">
            共 {{ pageResult.total }} 条 · 第 {{ pageResult.current }} 页
            <template v-if="metadataSummary">
              · {{ metadataSummary }}
            </template>
          </p>
          <p v-else-if="executionResult">
            返回 {{ displayRows.length }} 条记录
          </p>
          <p v-else>
            完整字段表格
          </p>
        </div>
        <div class="guided-result-actions">
          <ElTooltip content="复制原始响应">
            <ElButton
              :icon="CopyDocument"
              circle
              :disabled="!rawResultSource"
              aria-label="复制引导查询原始响应"
              @click="copyRawResult"
            />
          </ElTooltip>
          <ElTooltip :content="pageResult ? '导出本次查询结果（当前页）' : '导出本次查询结果'">
            <ElButton
              :icon="Download"
              circle
              :loading="exporting"
              :disabled="!canExportResult || exporting"
              aria-label="导出引导查询结果"
              @click="exportCurrentResult"
            />
          </ElTooltip>
        </div>
      </header>

      <StatePanel
        v-if="submitting"
        class="guided-state"
        status="loading"
        title="正在查询"
        :description="selectedOperation.name"
      />

      <div
        v-else-if="queryError"
        class="guided-error"
        role="alert"
      >
        <ElIcon><WarningFilled /></ElIcon>
        <div>
          <span>{{ queryError.code }}</span>
          <h3>{{ queryError.message }}</h3>
          <p v-if="queryError.trackId">
            trackId: <code>{{ queryError.trackId }}</code>
          </p>
        </div>
      </div>

      <StatePanel
        v-else-if="executionResult && displayRows.length === 0"
        class="guided-state"
        status="empty"
        title="没有匹配数据"
        description="可调整查询条件后重试"
      />

      <div
        v-else-if="displayRows.length > 0"
        class="guided-table-wrap"
      >
        <ElTable
          :data="displayRows"
          row-key="__rowKey"
          stripe
          height="100%"
        >
          <ElTableColumn
            type="index"
            label="#"
            width="56"
            fixed="left"
          />
          <ElTableColumn
            v-for="column in tableColumns"
            :key="column"
            :prop="column"
            :label="columnLabel(column)"
            min-width="138"
            show-overflow-tooltip
          >
            <template #default="scope">
              {{ formatCell(scope.row[column]) }}
            </template>
          </ElTableColumn>
        </ElTable>
      </div>

      <div
        v-else
        class="guided-idle"
      >
        <ElIcon><Search /></ElIcon>
        <h3>等待查询</h3>
        <p>选择业务查询并提交筛选条件</p>
      </div>

      <footer
        v-if="pageResult && selectedConfig.responseShape === 'page'"
        class="guided-pagination"
      >
        <span>每页 {{ pageResult.size }} 条</span>
        <ElPagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="pageResult.current"
          :page-size="pageResult.size"
          :page-sizes="selectedConfig.pagination.pageSizes"
          :total="pageResult.total"
          :disabled="submitting"
          @current-change="changeCurrentPage"
          @size-change="changePageSize"
        />
      </footer>

      <details
        v-if="rawResultSource"
        class="guided-raw"
      >
        <summary>原始响应</summary>
        <pre>{{ rawResultSource }}</pre>
      </details>
    </section>
  </section>
</template>

<style scoped>
.guided-workbench{display:grid;min-height:0;flex:1;grid-template-columns:220px 360px minmax(0,1fr);background:#fff}.guided-catalog,.guided-form-panel,.guided-results{min-width:0;min-height:0}.guided-catalog{display:flex;flex-direction:column;background:#f8fafc;border-right:1px solid var(--color-border)}.guided-groups{display:grid;gap:4px;padding:12px}.guided-groups button{display:flex;min-height:34px;align-items:center;justify-content:space-between;padding:0 10px;color:var(--color-text-secondary);background:transparent;border:1px solid transparent;border-radius:5px;cursor:pointer;font-size:12px;text-align:left}.guided-groups button:hover{background:#fff;border-color:var(--color-border)}.guided-groups button.active{color:var(--color-brand-700);background:#fff;border-color:var(--color-brand-200);font-weight:600}.guided-groups small{color:var(--color-text-muted);font-size:10px}.guided-operations{min-height:0;padding:8px;overflow:auto;border-top:1px solid var(--color-border)}.guided-operations>button{display:grid;width:100%;min-height:56px;align-items:start;grid-template-columns:18px minmax(0,1fr);gap:8px;margin-bottom:4px;padding:9px;color:var(--color-text);background:transparent;border:0;border-radius:5px;cursor:pointer;text-align:left}.guided-operations>button:hover{background:#eef2f7}.guided-operations>button.active{background:#e8f0fc;box-shadow:inset 3px 0 var(--color-brand-600)}.guided-operations .el-icon{margin-top:2px;color:var(--color-text-muted)}.guided-operations span{display:flex;min-width:0;flex-direction:column}.guided-operations strong{font-size:12px;line-height:1.35}.guided-operations small{display:-webkit-box;margin-top:4px;overflow:hidden;color:var(--color-text-muted);font-size:10px;line-height:1.4;-webkit-box-orient:vertical;-webkit-line-clamp:2}.guided-form-panel{display:flex;flex-direction:column;border-right:1px solid var(--color-border)}.guided-panel-heading,.guided-result-heading{display:flex;min-height:88px;align-items:flex-start;justify-content:space-between;gap:12px;padding:16px 18px;border-bottom:1px solid var(--color-border)}.guided-panel-heading span{display:block;overflow:hidden;color:var(--color-text-muted);font:10px/1.4 var(--font-mono);text-overflow:ellipsis;white-space:nowrap}.guided-panel-heading h2,.guided-result-heading h2{margin:4px 0 0;font-size:16px}.guided-panel-heading p,.guided-result-heading p{margin:4px 0 0;color:var(--color-text-secondary);font-size:11px;line-height:1.45}.guided-form{min-height:0;flex:1;padding:16px 18px;overflow:auto}.guided-form :deep(.el-form-item){margin-bottom:16px}.guided-form :deep(.el-form-item__label){padding-bottom:6px;color:var(--color-text-secondary);font-size:12px;font-weight:600;line-height:1.4}.guided-form :deep(.el-select),.guided-form :deep(.el-date-editor),.guided-form :deep(.el-input-number){width:100%}.guided-files{display:grid;gap:8px;margin-top:4px}.guided-files label{display:flex;min-height:54px;align-items:center;gap:10px;padding:9px 12px;background:#f8fafc;border:1px dashed #aab8cb;border-radius:6px;cursor:pointer}.guided-files input{position:absolute;width:1px;height:1px;overflow:hidden;clip:rect(0,0,0,0)}.guided-files .el-icon{width:20px;height:20px;flex:0 0 auto;color:var(--color-brand-600)}.guided-files span{display:flex;min-width:0;flex-direction:column}.guided-files strong{font-size:12px}.guided-files small{margin-top:3px;overflow:hidden;color:var(--color-text-muted);font-size:11px;text-overflow:ellipsis;white-space:nowrap}.guided-form-actions{display:flex;min-height:62px;align-items:center;justify-content:space-between;gap:12px;padding:12px 18px;border-top:1px solid var(--color-border)}.guided-form-actions span{min-width:0;overflow:hidden;color:var(--color-text-muted);font-size:10px;text-overflow:ellipsis;white-space:nowrap}.guided-form-actions code{font-family:var(--font-mono)}.guided-results{display:flex;min-height:0;flex-direction:column;background:#fbfcfe}.guided-result-heading{min-height:88px}.guided-result-heading h2{margin-top:0}.guided-state{min-height:0;flex:1}.guided-error{display:flex;max-width:560px;align-items:flex-start;gap:12px;margin:32px 20px;padding:16px;color:#8a2c2c;background:#fff6f5;border:1px solid #efc5c1;border-radius:7px}.guided-error>.el-icon{width:24px;height:24px;flex:0 0 auto}.guided-error span{font:600 11px/1.4 var(--font-mono)}.guided-error h3{margin:5px 0 0;font-size:14px;line-height:1.5}.guided-error p{margin:8px 0 0;color:#a14b43;font-size:11px}.guided-table-wrap{min-height:280px;flex:1;padding:12px 12px 0}.guided-pagination{display:flex;min-height:62px;align-items:center;justify-content:space-between;gap:16px;padding:10px 16px;background:#fff;border-top:1px solid var(--color-border)}.guided-pagination>span{color:var(--color-text-muted);font-size:11px;white-space:nowrap}.guided-raw{max-height:220px;background:#111827;border-top:1px solid #26334a;color:#dbeafe}.guided-raw summary{padding:10px 16px;cursor:pointer;font-size:11px}.guided-raw pre{max-height:170px;margin:0;padding:0 16px 14px;overflow:auto;font:11px/1.55 var(--font-mono);white-space:pre-wrap;overflow-wrap:anywhere}.guided-idle,.guided-unpaged{display:flex;min-height:320px;flex:1;align-items:center;justify-content:center;flex-direction:column;color:var(--color-text-muted);text-align:center}.guided-idle>.el-icon,.guided-unpaged>.el-icon{width:34px;height:34px}.guided-idle h3,.guided-unpaged h3{margin:12px 0 0;color:var(--color-text-secondary);font-size:14px}.guided-idle p,.guided-unpaged p{margin:5px 0 0;font-size:11px}@media(max-width:1280px){.guided-workbench{grid-template-columns:200px 340px minmax(0,1fr)}}@media(max-width:1080px){.guided-workbench{grid-template-columns:210px minmax(360px,1fr)}.guided-results{min-height:560px;grid-column:1/-1;border-top:1px solid var(--color-border)}.guided-form-panel{border-right:0}}@media(max-width:760px){.guided-workbench{display:flex;flex-direction:column}.guided-catalog{max-height:360px;border-right:0;border-bottom:1px solid var(--color-border)}.guided-groups{grid-template-columns:repeat(2,minmax(0,1fr))}.guided-form-panel{min-height:620px}.guided-results{min-height:560px}.guided-pagination{align-items:flex-start;flex-direction:column}.guided-pagination :deep(.el-pagination){max-width:100%;justify-content:flex-start;overflow-x:auto}.guided-table-wrap{min-height:360px}}
.guided-result-actions{display:flex;align-items:center;gap:8px}.guided-sort-control{display:grid;width:100%;grid-template-columns:minmax(0,1fr) auto;gap:12px}.guided-sort-control>label{display:flex;min-width:72px;align-items:center;justify-content:flex-end;gap:8px;color:var(--color-text-secondary);font-size:12px;white-space:nowrap}.guided-field-error{width:100%;margin:5px 0 0;color:var(--color-danger);font-size:11px;line-height:1.4}
</style>
