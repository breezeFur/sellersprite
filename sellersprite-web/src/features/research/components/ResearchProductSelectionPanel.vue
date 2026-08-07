<script setup lang="ts">
import { Check, Close } from '@element-plus/icons-vue'
import { ElMessage, type TableInstance } from 'element-plus'
import { computed, nextTick, ref, watch } from 'vue'

import { ApiError } from '@/shared/api/ApiError'

import {
  getResearchProductSelection,
  submitResearchProductSelection,
} from '../api/researchApi'
import type {
  ResearchJobStatus,
  ResearchProductCandidate,
  ResearchProductSelection,
  ResearchProductSelectionDecision,
} from '../model/research'

const props = defineProps<{
  jobId: string
  jobStatus: ResearchJobStatus
  draftAsins?: string[]
}>()

const emit = defineEmits<{
  submitted: [decision: ResearchProductSelectionDecision]
  'update:draftAsins': [asins: string[]]
}>()

const tableRef = ref<TableInstance>()
const selection = ref<ResearchProductSelection | null>(null)
const selectedAsins = ref<string[]>([])
const loading = ref(false)
const submittingDecision = ref<ResearchProductSelectionDecision | ''>('')
const loadError = ref('')
let requestVersion = 0

const canDecide = computed(() => (
  props.jobStatus === 'WAITING_INPUT'
  && selection.value?.status === 'PENDING'
  && !submittingDecision.value
))
const selectedCount = computed(() => selectedAsins.value.length)
const previewImageUrls = computed(() => (
  selection.value?.candidates
    .map((candidate) => validImageUrl(candidate.imageUrl))
    .filter((url, index, urls) => Boolean(url) && urls.indexOf(url) === index)
  ?? []
))

watch(
  () => props.jobId,
  () => void loadSelection(),
  { immediate: true },
)

async function loadSelection() {
  const version = ++requestVersion
  selection.value = null
  selectedAsins.value = []
  loadError.value = ''
  if (!props.jobId) return

  loading.value = true
  try {
    const result = await getResearchProductSelection(props.jobId)
    if (version !== requestVersion) return
    selection.value = result
    const candidateAsins = new Set(result.candidates.map((candidate) => candidate.asin))
    const restoredDraft = result.status === 'PENDING'
      ? props.draftAsins?.filter((asin) => candidateAsins.has(asin))
      : undefined
    setSelectedAsins(restoredDraft ?? result.selectedAsins)
    await restoreSelection()
  } catch (error) {
    if (version === requestVersion) {
      loadError.value = errorMessage(error, '候选商品加载失败')
    }
  } finally {
    if (version === requestVersion) loading.value = false
  }
}

function updateSelectedProducts(rows: ResearchProductCandidate[]) {
  setSelectedAsins(rows.map((row) => row.asin))
}

function setSelectedAsins(asins: string[]) {
  selectedAsins.value = [...asins]
  emit('update:draftAsins', [...asins])
}

async function restoreSelection() {
  await nextTick()
  const selected = new Set(selectedAsins.value)
  selection.value?.candidates.forEach((candidate) => {
    if (selected.has(candidate.asin)) tableRef.value?.toggleRowSelection(candidate, true)
  })
}

async function submitDecision(decision: ResearchProductSelectionDecision) {
  if (!canDecide.value) return
  const asins = decision === 'ENTER' ? [...selectedAsins.value] : []
  if (decision === 'ENTER' && asins.length === 0) {
    ElMessage.warning('请至少选择一个商品')
    return
  }

  submittingDecision.value = decision
  loadError.value = ''
  try {
    const result = await submitResearchProductSelection(props.jobId, {
      decision,
      selectedAsins: asins,
    })
    if (result) {
      selection.value = result
      setSelectedAsins(result.selectedAsins)
    } else if (selection.value) {
      selection.value = {
        ...selection.value,
        status: decision === 'ABANDON' ? 'ABANDONED' : 'SUBMITTED',
        selectedAsins: asins,
      }
      setSelectedAsins(asins)
    }
    emit('submitted', decision)
    ElMessage.success(decision === 'ENTER' ? '商品选择已提交' : '已放弃该市场')
  } catch (error) {
    loadError.value = errorMessage(error, '商品选择提交失败')
  } finally {
    submittingDecision.value = ''
  }
}

function rowSelectable() {
  return canDecide.value
}

function validImageUrl(value: string | null | undefined) {
  const url = value?.trim() ?? ''
  return /^https?:\/\//i.test(url) ? url : ''
}

function previewImageIndex(value: string | null | undefined) {
  return Math.max(0, previewImageUrls.value.indexOf(validImageUrl(value)))
}

function formatNumber(value: number | string | null, digits = 0) {
  const numericValue = typeof value === 'string' ? Number(value) : value
  if (numericValue === null || !Number.isFinite(numericValue)) return '--'
  return numericValue.toLocaleString('zh-CN', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits,
  })
}

function errorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError || error instanceof Error) return error.message || fallback
  return fallback
}
</script>

<template>
  <section
    class="product-selection"
    data-testid="research-product-selection"
    aria-label="Top20 商品选择"
  >
    <header class="product-selection__header">
      <div>
        <h3>Top20 商品选择</h3>
        <span>保留采集接口默认顺序，已选 {{ selectedCount }} 个</span>
      </div>
      <ElTag
        v-if="selection"
        :type="selection.status === 'PENDING' ? 'warning' : 'info'"
        effect="light"
      >
        {{ selection.status === 'PENDING' ? '等待选择' : '选择已锁定' }}
      </ElTag>
    </header>

    <ElAlert
      v-if="loadError"
      type="error"
      :title="loadError"
      :closable="false"
      show-icon
    />

    <ElSkeleton
      v-if="loading && !selection"
      :rows="6"
      animated
    />

    <ElEmpty
      v-else-if="!selection && !loadError"
      :image-size="52"
      description="候选商品尚未就绪"
    />

    <template v-else-if="selection">
      <ElTable
        ref="tableRef"
        :data="selection.candidates"
        row-key="asin"
        max-height="560"
        table-layout="fixed"
        empty-text="暂无候选商品"
        data-testid="research-product-candidate-table"
        @selection-change="updateSelectedProducts"
      >
        <ElTableColumn
          type="selection"
          width="48"
          :selectable="rowSelectable"
          reserve-selection
        />
        <ElTableColumn
          prop="rank"
          label="#"
          width="52"
        />
        <ElTableColumn
          label="商品"
          min-width="300"
        >
          <template #default="scope">
            <div class="product-selection__product">
              <ElImage
                v-if="validImageUrl(scope.row.imageUrl)"
                :src="validImageUrl(scope.row.imageUrl)"
                fit="contain"
                lazy
                :preview-src-list="previewImageUrls"
                :initial-index="previewImageIndex(scope.row.imageUrl)"
                preview-teleported
              />
              <div>
                <strong>{{ scope.row.title || scope.row.asin }}</strong>
                <code>{{ scope.row.asin }}</code>
                <span>{{ [scope.row.brand, scope.row.category].filter(Boolean).join(' · ') || '--' }}</span>
              </div>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn
          label="月销量"
          width="100"
          align="right"
        >
          <template #default="scope">
            {{ formatNumber(scope.row.units) }}
          </template>
        </ElTableColumn>
        <ElTableColumn
          label="月销售额"
          width="120"
          align="right"
        >
          <template #default="scope">
            ${{ formatNumber(scope.row.revenue) }}
          </template>
        </ElTableColumn>
        <ElTableColumn
          label="价格"
          width="92"
          align="right"
        >
          <template #default="scope">
            ${{ formatNumber(scope.row.price, 2) }}
          </template>
        </ElTableColumn>
        <ElTableColumn
          label="评分"
          width="108"
          align="right"
        >
          <template #default="scope">
            {{ formatNumber(scope.row.rating, 1) }} / {{ formatNumber(scope.row.ratings) }}
          </template>
        </ElTableColumn>
      </ElTable>

      <footer class="product-selection__actions">
        <span v-if="!canDecide">本次选择已经提交，候选与所选 ASIN 保持只读。</span>
        <span v-else>进入阶段二后，将只采集所选商品的评价、VOC 和 Keywords。</span>
        <div>
          <ElButton
            :icon="Close"
            :disabled="!canDecide"
            :loading="submittingDecision === 'ABANDON'"
            data-testid="abandon-research-market"
            @click="submitDecision('ABANDON')"
          >
            放弃该市场
          </ElButton>
          <ElButton
            type="primary"
            :icon="Check"
            :disabled="!canDecide || selectedCount === 0"
            :loading="submittingDecision === 'ENTER'"
            data-testid="submit-research-product-selection"
            @click="submitDecision('ENTER')"
          >
            进入阶段二（{{ selectedCount }}）
          </ElButton>
        </div>
      </footer>
    </template>
  </section>
</template>

<style scoped>
.product-selection{display:grid;min-width:0;gap:14px}.product-selection__header{display:flex;align-items:center;justify-content:space-between;gap:16px}.product-selection__header>div{display:grid;gap:4px}.product-selection__header h3{margin:0;color:var(--color-text);font-size:14px;letter-spacing:0}.product-selection__header span,.product-selection__actions>span{color:var(--color-text-secondary);font-size:12px}.product-selection__product{display:grid;min-width:0;grid-template-columns:56px minmax(0,1fr);align-items:center;gap:10px}.product-selection__product :deep(.el-image){width:56px;height:56px;border:1px solid var(--color-border);border-radius:var(--radius-sm);background:var(--color-surface-muted)}.product-selection__product>div{display:grid;min-width:0;gap:3px}.product-selection__product strong{overflow:hidden;color:var(--color-text);font-size:12px;text-overflow:ellipsis;white-space:nowrap}.product-selection__product code{color:var(--color-brand-700);font:11px var(--font-mono)}.product-selection__product span{overflow:hidden;color:var(--color-text-secondary);font-size:11px;text-overflow:ellipsis;white-space:nowrap}.product-selection__actions{display:flex;align-items:center;justify-content:space-between;gap:16px}.product-selection__actions>div{display:flex;flex:0 0 auto;gap:10px}@media(max-width:760px){.product-selection__header,.product-selection__actions{align-items:stretch;flex-direction:column}.product-selection__actions>div{display:grid;grid-template-columns:1fr 1fr}.product-selection__actions .el-button{width:100%;margin:0}}@media(max-width:520px){.product-selection__actions>div{grid-template-columns:1fr}}
</style>
