<script setup lang="ts">
import { RefreshRight } from '@element-plus/icons-vue'
import { computed, ref, watch } from 'vue'

import { ApiError } from '@/shared/api/ApiError'

import {
  getResearchEvidencePage,
  listResearchEvidenceTables,
} from '../api/researchApi'
import type {
  ResearchEvidencePage,
  ResearchEvidenceTableSummary,
  ResearchStageCode,
} from '../model/research'

const DEFAULT_PAGE_SIZE = 50

const props = withDefaults(defineProps<{
  jobId: string
  stageCode?: ResearchStageCode | string
}>(), {
  stageCode: 'SCREENING',
})

const tables = ref<ResearchEvidenceTableSummary[]>([])
const activeDatasetCode = ref('')
const evidencePage = ref<ResearchEvidencePage | null>(null)
const tableLoading = ref(false)
const pageLoading = ref(false)
const loadError = ref('')
let tableRequestVersion = 0
let pageRequestVersion = 0

const activeTable = computed(() => tables.value.find(
  (table) => table.datasetCode === activeDatasetCode.value,
) ?? null)
const columns = computed(() => evidencePage.value?.columns ?? activeTable.value?.columns ?? [])
const records = computed(() => evidencePage.value?.records ?? [])
const currentPage = computed(() => evidencePage.value?.current ?? 1)
const pageSize = computed(() => evidencePage.value?.size ?? DEFAULT_PAGE_SIZE)
const total = computed(() => evidencePage.value?.total ?? activeTable.value?.rowCount ?? 0)
const stageLabel = computed(() => {
  if (props.stageCode === 'SCREENING') return '阶段一证据数据'
  if (props.stageCode === 'DEEP_DIVE') return '阶段二证据数据'
  return '阶段证据数据'
})

watch(
  () => [props.jobId, props.stageCode] as const,
  () => void loadTables(),
  { immediate: true },
)

watch(activeDatasetCode, (datasetCode) => {
  if (datasetCode) void loadPage(1)
})

async function loadTables() {
  const version = ++tableRequestVersion
  pageRequestVersion += 1
  tables.value = []
  activeDatasetCode.value = ''
  evidencePage.value = null
  loadError.value = ''
  if (!props.jobId) return

  tableLoading.value = true
  try {
    const result = await listResearchEvidenceTables(props.jobId, props.stageCode)
    if (version !== tableRequestVersion) return
    tables.value = result
    activeDatasetCode.value = result[0]?.datasetCode ?? ''
  } catch (error) {
    if (version === tableRequestVersion) {
      loadError.value = errorMessage(error, '证据表加载失败')
    }
  } finally {
    if (version === tableRequestVersion) tableLoading.value = false
  }
}

async function loadPage(current: number) {
  const datasetCode = activeDatasetCode.value
  if (!props.jobId || !datasetCode) return

  const version = ++pageRequestVersion
  pageLoading.value = true
  loadError.value = ''
  try {
    const result = await getResearchEvidencePage(
      props.jobId,
      datasetCode,
      current,
      evidencePage.value?.size ?? DEFAULT_PAGE_SIZE,
    )
    if (version !== pageRequestVersion || datasetCode !== activeDatasetCode.value) return
    evidencePage.value = result
  } catch (error) {
    if (version === pageRequestVersion) {
      evidencePage.value = null
      loadError.value = errorMessage(error, '证据表数据加载失败')
    }
  } finally {
    if (version === pageRequestVersion) pageLoading.value = false
  }
}

function formatCell(value: unknown) {
  if (value === null || value === undefined || value === '') return '--'
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}

function imageUrl(column: string, value: unknown) {
  if (column !== '图片' || typeof value !== 'string') return ''
  return /^https?:\/\//i.test(value) ? value : ''
}

function columnWidth(column: string) {
  if (column === '图片') return 84
  if (['标题', '内容', '代表正向评价', '代表负向评价'].includes(column)) return 280
  if (['ASIN', '父体ASIN', '图片链接'].includes(column)) return 150
  return 132
}

function errorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError || error instanceof Error) return error.message || fallback
  return fallback
}
</script>

<template>
  <section
    class="evidence-panel"
    data-testid="research-evidence-panel"
    :aria-label="stageLabel"
  >
    <header class="evidence-panel__header">
      <div>
        <h3>{{ stageLabel }}</h3>
        <span>{{ tables.length }} 张表 · {{ total }} 条当前数据</span>
      </div>
      <ElButton
        :icon="RefreshRight"
        link
        type="primary"
        :loading="tableLoading"
        data-testid="refresh-research-evidence"
        @click="loadTables"
      >
        刷新
      </ElButton>
    </header>

    <ElAlert
      v-if="loadError"
      type="error"
      :title="loadError"
      :closable="false"
      show-icon
    />

    <ElSkeleton
      v-if="tableLoading && tables.length === 0"
      :rows="5"
      animated
    />

    <ElEmpty
      v-else-if="tables.length === 0 && !loadError"
      :image-size="52"
      description="阶段证据表尚未就绪"
    />

    <template v-else>
      <ElTabs
        v-model="activeDatasetCode"
        class="evidence-panel__tabs"
      >
        <ElTabPane
          v-for="table in tables"
          :key="table.datasetCode"
          :name="table.datasetCode"
          :label="`${table.sheetName} (${table.rowCount})`"
        />
      </ElTabs>

      <ElTable
        v-loading="pageLoading"
        :data="records"
        table-layout="auto"
        max-height="520"
        empty-text="当前证据表暂无数据"
        data-testid="research-evidence-table"
      >
        <ElTableColumn
          v-for="column in columns"
          :key="column"
          :label="column"
          :min-width="columnWidth(column)"
          show-overflow-tooltip
        >
          <template #default="scope">
            <ElImage
              v-if="imageUrl(column, scope.row[column])"
              class="evidence-panel__image"
              :src="imageUrl(column, scope.row[column])"
              fit="contain"
              lazy
              :preview-src-list="[imageUrl(column, scope.row[column])]"
              preview-teleported
            />
            <span v-else>{{ formatCell(scope.row[column]) }}</span>
          </template>
        </ElTableColumn>
      </ElTable>

      <ElPagination
        v-if="total > pageSize"
        class="evidence-panel__pagination"
        background
        layout="prev, pager, next, total"
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        @current-change="loadPage"
      />
    </template>
  </section>
</template>

<style scoped>
.evidence-panel{display:grid;min-width:0;gap:14px}.evidence-panel__header{display:flex;align-items:center;justify-content:space-between;gap:16px}.evidence-panel__header>div{display:grid;gap:4px}.evidence-panel__header h3{margin:0;color:var(--color-text);font-size:14px;letter-spacing:0}.evidence-panel__header span{color:var(--color-text-secondary);font-size:12px}.evidence-panel__tabs{min-width:0}.evidence-panel__tabs :deep(.el-tabs__header){margin-bottom:12px}.evidence-panel__image{width:56px;height:56px;border:1px solid var(--color-border);border-radius:var(--radius-sm);background:var(--color-surface-muted)}.evidence-panel__pagination{justify-content:flex-end}@media(max-width:640px){.evidence-panel__header{align-items:flex-start}.evidence-panel__pagination{justify-content:center;overflow-x:auto}}
</style>
