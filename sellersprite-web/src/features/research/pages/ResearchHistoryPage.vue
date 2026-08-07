<script setup lang="ts">
import {
  DocumentAdd,
  Download,
  RefreshRight,
  Search,
  View,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

import { ApiError } from '@/shared/api/ApiError'
import StatePanel from '@/shared/components/StatePanel.vue'

import { pageResearchJobs } from '../api/researchApi'
import { downloadResearchArtifact } from '../api/researchStreamApi'
import {
  researchArtifactGraph,
  researchArtifactLabels,
  researchArtifactTypes,
  researchGraphCodes,
  researchGraphLabels,
  researchJobStatuses,
  sellerSpriteMarketplaces,
  type ResearchArtifactSummary,
  type ResearchGraphCode,
  type ResearchJobHistory,
  type ResearchJobHistoryQuery,
  type ResearchJobStatus,
  type SellerSpriteMarketplace,
} from '../model/research'

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface HistoryFilterForm {
  keyword: string
  status: ResearchJobStatus | ''
  marketplace: SellerSpriteMarketplace | ''
  month: string
}

const jobStatusPresentation: Record<ResearchJobStatus, { label: string; type: TagType }> = {
  QUEUED: { label: '排队中', type: 'info' },
  RUNNING: { label: '采集中', type: 'primary' },
  RETRY_WAIT: { label: '等待重试', type: 'warning' },
  WAITING_INPUT: { label: '等待商品选择', type: 'warning' },
  SUCCEEDED: { label: '已完成', type: 'success' },
  ABANDONED: { label: '已放弃', type: 'info' },
  FAILED: { label: '失败', type: 'danger' },
  CANCELLED: { label: '已取消', type: 'info' },
}

const analysisPhasePresentation: Record<string, string> = {
  waiting_research: '等待市场数据',
  queued: '等待执行',
  plan: '制定分析计划',
  observe: '读取工作簿',
  think: '分析工作表',
  context: '整理上下文',
  summary: '生成总结',
  report: '发布报告',
  retry_wait: '等待重试',
  completed: '分析完成',
  cancelled: '已取消',
  failed: '分析失败',
  error: '分析失败',
}

const emptyFilters = (): HistoryFilterForm => ({
  keyword: '',
  status: '',
  marketplace: '',
  month: '',
})

const router = useRouter()
const filters = reactive<HistoryFilterForm>(emptyFilters())
const appliedFilters = reactive<HistoryFilterForm>(emptyFilters())
const records = ref<ResearchJobHistory[]>([])
const current = ref(1)
const size = ref(20)
const total = ref(0)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))
const loading = ref(false)
const loadError = ref('')
const downloadingArtifactId = ref('')
let loadSequence = 0

async function loadPage(
  nextCurrent = current.value,
  nextSize = size.value,
  clearRecords = false,
) {
  const sequence = ++loadSequence
  current.value = nextCurrent
  size.value = nextSize
  if (clearRecords) {
    records.value = []
    total.value = 0
  }
  loading.value = true
  loadError.value = ''
  try {
    const page = await pageResearchJobs(toQuery(nextCurrent, nextSize))
    if (sequence !== loadSequence) return
    records.value = page.records
    current.value = page.current
    size.value = page.size
    total.value = page.total
  } catch (error) {
    if (sequence !== loadSequence) return
    loadError.value = errorMessage(error, '历史报告加载失败')
  } finally {
    if (sequence === loadSequence) loading.value = false
  }
}

function toQuery(nextCurrent: number, nextSize: number): ResearchJobHistoryQuery {
  const keyword = appliedFilters.keyword.trim()
  return {
    current: nextCurrent,
    size: nextSize,
    ...(keyword ? { keyword } : {}),
    ...(appliedFilters.status ? { status: appliedFilters.status } : {}),
    ...(appliedFilters.marketplace ? { marketplace: appliedFilters.marketplace } : {}),
    ...(appliedFilters.month ? { month: appliedFilters.month } : {}),
  }
}

function searchReports() {
  Object.assign(appliedFilters, filters)
  void loadPage(1, size.value, true)
}

function resetFilters() {
  Object.assign(filters, emptyFilters())
  Object.assign(appliedFilters, emptyFilters())
  void loadPage(1, size.value, true)
}

function changePage(nextCurrent: number) {
  void loadPage(nextCurrent, size.value, true)
}

function changeSize(nextSize: number) {
  void loadPage(1, nextSize, true)
}

function selectPageSize(event: Event) {
  const nextSize = Number((event.target as HTMLSelectElement).value)
  if ([10, 20, 50, 100].includes(nextSize)) changeSize(nextSize)
}

function openReport(row: ResearchJobHistory) {
  void router.push({
    path: '/research/market-report',
    query: { jobId: row.jobId, view: 'conversation', section: 'report' },
  })
}

function createResearchJob() {
  void router.push({ path: '/research/market-report' })
}

async function downloadArtifact(row: ResearchJobHistory, artifact: ResearchArtifactSummary) {
  if (downloadingArtifactId.value) return
  downloadingArtifactId.value = artifact.artifactId
  try {
    const content = await downloadResearchArtifact(row.jobId, artifact)
    const objectUrl = URL.createObjectURL(content)
    const link = document.createElement('a')
    link.href = objectUrl
    link.download = artifact.fileName || `market-research-${artifact.artifactId}`
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1_000)
  } catch (error) {
    ElMessage.error(errorMessage(error, '报告文件下载失败'))
  } finally {
    downloadingArtifactId.value = ''
  }
}

function jobStatus(status: ResearchJobStatus) {
  return jobStatusPresentation[status]
}

function analysisPhase(phase: string | null) {
  if (!phase) return '--'
  return analysisPhasePresentation[phase.toLowerCase()] ?? phase
}

function graphStatus(row: ResearchJobHistory, graphCode: ResearchGraphCode) {
  if (artifactsForGraph(row, graphCode).length > 0) {
    return { label: '已完成', type: 'success' as TagType }
  }
  const graphIndex = researchGraphCodes.indexOf(graphCode)
  const firstPendingIndex = researchGraphCodes.findIndex((code) => (
    artifactsForGraph(row, code).length === 0
  ))
  if (graphIndex !== firstPendingIndex) return { label: '未开始', type: 'info' as TagType }
  return jobStatus(row.status)
}

function graphDetail(row: ResearchJobHistory, graphCode: ResearchGraphCode) {
  if (graphCode !== 'report') return ''
  const phase = analysisPhase(row.analysisPhase)
  if (phase === '--') return ''
  const progress = row.analysisProgress
  return progress === null || progress === undefined
    ? phase
    : `${phase} · ${normalizedProgress(progress)}%`
}

function artifactsForGraph(row: ResearchJobHistory, graphCode: ResearchGraphCode) {
  return row.artifacts.filter((artifact) => (
    researchArtifactGraph[artifact.artifactType as keyof typeof researchArtifactGraph] === graphCode
  ))
}

function artifactsOfType(row: ResearchJobHistory, artifactType: string) {
  return row.artifacts.filter((artifact) => artifact.artifactType === artifactType)
}

function normalizedProgress(progress: number) {
  return Math.max(0, Math.min(100, Number.isFinite(progress) ? progress : 0))
}

function artifactType(artifact: ResearchArtifactSummary) {
  return researchArtifactLabels[
    artifact.artifactType as keyof typeof researchArtifactLabels
  ] ?? artifact.artifactType
}

function formatFileSize(bytes: number | null) {
  if (bytes === null || !Number.isFinite(bytes) || bytes < 0) return '--'
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`
}

function formatTime(value: number | null) {
  if (!value) return '--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '--'
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date)
}

function errorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError || error instanceof Error) return error.message || fallback
  return fallback
}

onMounted(() => void loadPage())
</script>

<template>
  <section
    class="history-page"
    aria-label="我的全部历史报告"
  >
    <header class="history-page__header">
      <div>
        <h1>我的全部历史报告</h1>
        <p>共 {{ total }} 份市场调研报告</p>
      </div>
      <div class="history-page__header-actions">
        <ElButton
          type="primary"
          :icon="DocumentAdd"
          data-testid="history-create-job"
          @click="createResearchJob"
        >
          新建任务
        </ElButton>
        <ElButton
          :icon="RefreshRight"
          :loading="loading"
          data-testid="history-refresh"
          @click="loadPage()"
        >
          刷新
        </ElButton>
      </div>
    </header>

    <ElForm
      class="history-page__filters"
      :model="filters"
      inline
      data-testid="history-filter-form"
      @submit.prevent="searchReports"
    >
      <ElFormItem label="搜索">
        <ElInput
          v-model="filters.keyword"
          class="history-page__keyword"
          clearable
          placeholder="报告名称、关键词或任务 ID"
          data-testid="history-keyword"
        />
      </ElFormItem>
      <ElFormItem label="任务状态">
        <select
          v-model="filters.status"
          class="history-page__native-control"
          data-testid="history-status"
        >
          <option value="">
            全部状态
          </option>
          <option
            v-for="status in researchJobStatuses"
            :key="status"
            :value="status"
          >
            {{ jobStatus(status).label }}
          </option>
        </select>
      </ElFormItem>
      <ElFormItem label="站点">
        <select
          v-model="filters.marketplace"
          class="history-page__native-control"
          data-testid="history-marketplace"
        >
          <option value="">
            全部站点
          </option>
          <option
            v-for="marketplace in sellerSpriteMarketplaces"
            :key="marketplace"
            :value="marketplace"
          >
            {{ marketplace }}
          </option>
        </select>
      </ElFormItem>
      <ElFormItem label="数据月份">
        <input
          v-model="filters.month"
          type="month"
          class="history-page__native-control"
          data-testid="history-month"
        >
      </ElFormItem>
      <ElFormItem class="history-page__filter-actions">
        <ElButton
          type="primary"
          :icon="Search"
          native-type="submit"
          data-testid="history-search"
        >
          查询
        </ElButton>
        <ElButton
          data-testid="history-reset"
          @click="resetFilters"
        >
          重置
        </ElButton>
      </ElFormItem>
    </ElForm>

    <ElAlert
      v-if="loadError && records.length"
      class="history-page__alert"
      type="error"
      :title="loadError"
      :closable="false"
      show-icon
    />

    <StatePanel
      v-if="loading && records.length === 0"
      status="loading"
      title="正在加载历史报告"
    />
    <StatePanel
      v-else-if="loadError && records.length === 0"
      status="error"
      title="历史报告加载失败"
      :description="loadError"
      action-label="重新加载"
      @action="loadPage()"
    />
    <div
      v-else
      class="history-page__table-wrap"
      :aria-busy="loading"
    >
      <div
        v-if="loading"
        class="history-page__loading-bar"
      />
      <table v-if="records.length">
        <thead>
          <tr>
            <th scope="col">
              报告
            </th>
            <th scope="col">
              调研范围
            </th>
            <th scope="col">
              三段子图
            </th>
            <th scope="col">
              已发布文件
            </th>
            <th scope="col">
              时间
            </th>
            <th scope="col">
              操作
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="row in records"
            :key="row.jobId"
            :data-testid="`history-row-${row.jobId}`"
          >
            <td class="history-page__report">
              <strong>{{ row.reportName }}</strong>
              <span>{{ row.keyword || '未填写关键词' }}</span>
              <code>{{ row.jobId }}</code>
            </td>
            <td>
              <strong>{{ row.marketplace }} · {{ row.month }}</strong>
              <span class="history-page__node">{{ row.nodeIdPath }}</span>
            </td>
            <td class="history-page__graphs">
              <div
                v-for="graphCode in researchGraphCodes"
                :key="graphCode"
                :data-testid="`history-graph-${row.jobId}-${graphCode}`"
              >
                <span>{{ researchGraphLabels[graphCode] }}</span>
                <ElTag
                  :type="graphStatus(row, graphCode).type"
                  size="small"
                  effect="plain"
                >
                  {{ graphStatus(row, graphCode).label }}
                </ElTag>
                <small v-if="graphDetail(row, graphCode)">
                  {{ graphDetail(row, graphCode) }}
                </small>
              </div>
              <div class="history-page__progress">
                <span :style="{ width: `${normalizedProgress(row.progress)}%` }" />
              </div>
              <small>总进度 {{ normalizedProgress(row.progress) }}%</small>
            </td>
            <td class="history-page__artifacts">
              <div
                v-for="artifactTypeCode in researchArtifactTypes"
                :key="artifactTypeCode"
                :data-testid="`history-artifact-group-${row.jobId}-${artifactTypeCode}`"
              >
                <span>{{ researchArtifactLabels[artifactTypeCode] }}</span>
                <ElButton
                  v-for="artifact in artifactsOfType(row, artifactTypeCode)"
                  :key="artifact.artifactId"
                  link
                  type="primary"
                  :icon="Download"
                  :title="`${artifact.fileName} · ${formatFileSize(artifact.fileSize)}`"
                  :loading="downloadingArtifactId === artifact.artifactId"
                  :data-testid="`download-artifact-${artifact.artifactId}`"
                  @click="downloadArtifact(row, artifact)"
                >
                  <span>{{ artifact.fileName }}</span>
                  <small>{{ artifactType(artifact) }} · {{ formatFileSize(artifact.fileSize) }}</small>
                </ElButton>
                <small v-if="artifactsOfType(row, artifactTypeCode).length === 0">
                  未发布
                </small>
              </div>
            </td>
            <td class="history-page__time">
              <span>创建 {{ formatTime(row.createdAt) }}</span>
              <small>完成 {{ formatTime(row.finishedAt) }}</small>
            </td>
            <td>
              <ElButton
                link
                type="primary"
                :icon="View"
                :data-testid="`view-report-${row.jobId}`"
                @click="openReport(row)"
              >
                查看
              </ElButton>
            </td>
          </tr>
        </tbody>
      </table>
      <StatePanel
        v-else
        status="empty"
        title="暂无历史报告"
        description="调整搜索或筛选条件后重新查询"
      />
    </div>

    <footer class="history-page__pagination">
      <span>共 {{ total }} 份，第 {{ current }} / {{ totalPages }} 页</span>
      <div class="history-page__pager-controls">
        <select
          class="history-page__native-control"
          aria-label="每页条数"
          :value="size"
          :disabled="loading"
          @change="selectPageSize"
        >
          <option :value="10">
            10 条/页
          </option>
          <option :value="20">
            20 条/页
          </option>
          <option :value="50">
            50 条/页
          </option>
          <option :value="100">
            100 条/页
          </option>
        </select>
        <ElButton
          :disabled="loading || current <= 1"
          data-testid="history-previous-page"
          @click="changePage(current - 1)"
        >
          上一页
        </ElButton>
        <ElButton
          :disabled="loading || current >= totalPages"
          data-testid="history-next-page"
          @click="changePage(current + 1)"
        >
          下一页
        </ElButton>
      </div>
    </footer>
  </section>
</template>

<style scoped>
.history-page {
  display: flex;
  height: calc(100vh - var(--header-height) - var(--content-gutter) * 2);
  min-height: 620px;
  flex-direction: column;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.history-page__header {
  display: flex;
  min-width: 0;
  padding: 14px var(--space-4);
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  border-bottom: 1px solid var(--color-border);
}

.history-page__header h1 {
  margin: 0;
  color: var(--color-text);
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0;
}

.history-page__header p {
  margin: 3px 0 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
}

.history-page__header-actions {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  gap: var(--space-2);
}

.history-page__filters {
  display: flex;
  padding: 12px var(--space-4) 4px;
  align-items: flex-end;
  gap: 0 var(--space-2);
  background: var(--color-surface-muted);
  border-bottom: 1px solid var(--color-border);
}

.history-page__filters :deep(.el-form-item) {
  margin-right: var(--space-2);
  margin-bottom: 8px;
}

.history-page__filters :deep(.el-form-item__label) {
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
}

.history-page__keyword {
  width: 260px;
}

.history-page__native-control {
  width: 140px;
  height: 32px;
  padding: 0 10px;
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font: inherit;
}

.history-page__filter-actions {
  margin-left: auto;
}

.history-page__alert {
  border-radius: 0;
}

.history-page__table-wrap {
  position: relative;
  flex: 1;
  min-width: 0;
  min-height: 0;
  overflow: auto;
}

.history-page__loading-bar {
  position: sticky;
  top: 0;
  z-index: 3;
  height: 2px;
  background: var(--color-brand-500);
  animation: history-loading 1.2s ease-in-out infinite;
  transform-origin: left;
}

.history-page__table-wrap table {
  width: 100%;
  min-width: 1100px;
  border-collapse: collapse;
  table-layout: fixed;
}

.history-page__table-wrap th,
.history-page__table-wrap td {
  padding: 11px var(--space-3);
  color: var(--color-text-secondary);
  border-bottom: 1px solid var(--color-border);
  font-size: var(--font-size-sm);
  text-align: left;
  vertical-align: top;
}

.history-page__table-wrap th {
  position: sticky;
  top: 0;
  z-index: 2;
  color: var(--color-text);
  background: var(--color-surface-muted);
  font-size: var(--font-size-xs);
  font-weight: 600;
}

.history-page__table-wrap tbody tr:hover {
  background: var(--color-brand-50);
}

.history-page__table-wrap th:last-child,
.history-page__table-wrap td:last-child {
  position: sticky;
  right: 0;
  width: 72px;
  background: var(--color-surface);
  box-shadow: -6px 0 10px rgb(15 23 42 / 5%);
}

.history-page__table-wrap th:last-child {
  z-index: 3;
  background: var(--color-surface-muted);
}

.history-page__table-wrap tbody tr:hover td:last-child {
  background: var(--color-brand-50);
}

.history-page__table-wrap strong,
.history-page__table-wrap small,
.history-page__table-wrap code,
.history-page__table-wrap td > span {
  display: block;
}

.history-page__table-wrap small,
.history-page__table-wrap code,
.history-page__table-wrap td > span {
  margin-top: 4px;
  color: var(--color-text-muted);
  font-size: 11px;
}

.history-page__report {
  width: 220px;
}

.history-page__report strong {
  color: var(--color-text);
  overflow-wrap: anywhere;
}

.history-page__report code,
.history-page__node {
  max-width: 190px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-page__graphs {
  width: 210px;
}

.history-page__graphs > div:not(.history-page__progress) {
  display: grid;
  grid-template-columns:minmax(72px, 1fr) auto;
  margin-bottom: 6px;
  align-items: center;
  gap: 6px;
}

.history-page__graphs > div > span {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 12px;
}

.history-page__graphs > div > small {
  grid-column: 1 / -1;
  margin: 0;
}

.history-page__progress {
  width: 88px;
  height: 4px;
  margin-top: 9px;
  overflow: hidden;
  background: var(--color-border);
  border-radius: 2px;
}

.history-page__progress span {
  display: block;
  height: 100%;
  background: var(--color-brand-500);
}

.history-page__artifacts {
  width: 250px;
}

.history-page__artifacts > div {
  display: grid;
  margin-bottom: 8px;
  gap: 3px;
}

.history-page__artifacts > div > span {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 11px;
  font-weight: 600;
}

.history-page__artifacts :deep(.el-button) {
  display: flex;
  width: 100%;
  height: auto;
  margin: 0 0 6px;
  padding: 0;
  justify-content: flex-start;
  text-align: left;
}

.history-page__artifacts :deep(.el-button > span) {
  display: flex;
  width: 0;
  min-width: 0;
  flex: 1;
  align-items: flex-start;
  flex-direction: column;
  overflow: hidden;
}

.history-page__artifacts :deep(.el-button > span > span) {
  display: block;
  width: 100%;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-page__artifacts small {
  color: var(--color-text-muted);
  font-weight: 400;
}

.history-page__time {
  width: 170px;
}

.history-page__pagination {
  display: flex;
  min-height: 54px;
  padding: 0 var(--space-4);
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  color: var(--color-text-muted);
  border-top: 1px solid var(--color-border);
  font-size: var(--font-size-xs);
}

.history-page__pager-controls {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

@keyframes history-loading {
  0% { transform: scaleX(0.08); }
  50% { transform: scaleX(0.7); }
  100% { transform: scaleX(1); opacity: 0; }
}

@media (max-width: 1100px) {
  .history-page__filters {
    flex-wrap: wrap;
  }

  .history-page__filter-actions {
    margin-left: 0;
  }
}

@media (max-width: 720px) {
  .history-page {
    height: auto;
    min-height: calc(100vh - var(--header-height));
  }

  .history-page__header {
    align-items: flex-start;
    flex-direction: column;
  }

  .history-page__header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .history-page__filters {
    display: grid;
    grid-template-columns: 1fr;
  }

  .history-page__filters :deep(.el-form-item),
  .history-page__keyword,
  .history-page__native-control {
    width: 100%;
    margin-right: 0;
  }

  .history-page__pagination {
    min-width: 0;
    padding-block: var(--space-3);
    overflow-x: auto;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}
</style>
