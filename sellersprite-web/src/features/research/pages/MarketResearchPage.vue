<script setup lang="ts">
import { DocumentAdd, Download, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { computed, onBeforeUnmount, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { ApiError } from '@/shared/api/ApiError'
import StatePanel from '@/shared/components/StatePanel.vue'

import {
  createResearchJob,
  downloadResearchReport,
  getResearchJob,
} from '../api/researchApi'
import {
  researchPhaseCodes,
  type ResearchJobCreateRequest,
  type ResearchJobDetail,
  type ResearchJobStatus,
  type ResearchPhaseCode,
} from '../model/research'

interface ResearchFormModel {
  reportName: string
  keyword: string
  seedAsinsText: string
}

interface PageError {
  code?: string
  message: string
  trackId?: string
}

const MARKETPLACE = 'US'
const TEMPLATE_CODE = 'market-research-v1'
const MAX_SEED_ASINS = 20
const POLL_INTERVAL_MS = 2_000

const phaseLabels: Record<ResearchPhaseCode, string> = {
  VALIDATE: '校验任务参数',
  CHECK_QUOTA: '检查数据源配置',
  COLLECT_MARKET_AND_PRODUCTS: '采集市场与商品',
  COLLECT_KEYWORDS: '采集关键词',
  COLLECT_REVIEWS: '采集评论',
  PREPARE_DATA: '整理报告数据',
  RENDER_EXCEL: '生成 Excel',
  VALIDATE_AND_PUBLISH: '校验并发布',
}

const statusPresentation: Record<ResearchJobStatus, {
  label: string
  type: 'info' | 'warning' | 'success' | 'danger'
}> = {
  QUEUED: { label: '排队中', type: 'info' },
  RUNNING: { label: '执行中', type: 'warning' },
  SUCCEEDED: { label: '已完成', type: 'success' },
  FAILED: { label: '执行失败', type: 'danger' },
}

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const form = reactive<ResearchFormModel>({
  reportName: '',
  keyword: '',
  seedAsinsText: '',
})
const formRules: FormRules<ResearchFormModel> = {
  reportName: [
    { required: true, whitespace: true, message: '请输入报告名称', trigger: 'blur' },
    { max: 128, message: '报告名称不能超过 128 个字符', trigger: 'blur' },
  ],
  keyword: [
    { required: true, whitespace: true, message: '请输入核心关键词', trigger: 'blur' },
    { max: 256, message: '核心关键词不能超过 256 个字符', trigger: 'blur' },
  ],
  seedAsinsText: [{
    trigger: ['blur', 'change'],
    validator: (_rule, value, callback) => {
      const asins = parseSeedAsins(String(value ?? ''))
      if (asins.length > MAX_SEED_ASINS) {
        callback(new Error(`种子 ASIN 最多填写 ${MAX_SEED_ASINS} 个`))
        return
      }
      const invalidAsin = asins.find((asin) => !/^[A-Za-z0-9]{10}$/.test(asin))
      if (invalidAsin) {
        callback(new Error(`ASIN ${invalidAsin} 必须是 10 位字母或数字`))
        return
      }
      callback()
    },
  }],
}

const creating = ref(false)
const downloading = ref(false)
const jobLoading = ref(false)
const currentJobId = ref('')
const currentJob = ref<ResearchJobDetail | null>(null)
const createError = ref<PageError | null>(null)
const loadError = ref<PageError | null>(null)
const downloadError = ref<PageError | null>(null)

let pollTimer: ReturnType<typeof setTimeout> | null = null
let requestVersion = 0
let disposed = false

const currentStatus = computed(() => (
  currentJob.value ? statusPresentation[currentJob.value.status] : null
))
const normalizedProgress = computed(() => Math.min(100, Math.max(0, currentJob.value?.progress ?? 0)))
const activePhase = computed(() => {
  if (!currentJob.value) return 0
  if (currentJob.value.status === 'SUCCEEDED') return researchPhaseCodes.length
  const index = researchPhaseCodes.indexOf(currentJob.value.currentPhase as ResearchPhaseCode)
  return index < 0 ? 0 : index
})
const currentPhaseName = computed(() => {
  const job = currentJob.value
  if (!job) return ''
  if (job.currentPhaseName) return job.currentPhaseName
  if (researchPhaseCodes.includes(job.currentPhase as ResearchPhaseCode)) {
    return phaseLabels[job.currentPhase as ResearchPhaseCode]
  }
  return job.status === 'QUEUED' ? '等待执行' : '状态更新中'
})
const hasActiveJob = computed(() => (
  currentJob.value !== null && !isTerminalStatus(currentJob.value.status)
))
const submissionLocked = computed(() => (
  creating.value
  || jobLoading.value
  || hasActiveJob.value
  || (Boolean(currentJobId.value) && currentJob.value === null && loadError.value === null)
))
const submitButtonText = computed(() => {
  if (creating.value) return '正在创建'
  if (jobLoading.value && !currentJob.value) return '任务加载中'
  if (hasActiveJob.value) return '任务执行中'
  return '创建调研任务'
})
const failureDescription = computed(() => {
  if (!currentJob.value || currentJob.value.status !== 'FAILED') return ''
  return [currentJob.value.errorCode, currentJob.value.errorMessage]
    .filter(Boolean)
    .join('：') || '任务执行失败，请刷新状态或重新创建任务。'
})

async function submitResearchJob() {
  if (submissionLocked.value) return

  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const seedAsins = parseSeedAsins(form.seedAsinsText).map((asin) => asin.toUpperCase())
  const request: ResearchJobCreateRequest = {
    reportName: form.reportName.trim(),
    keyword: form.keyword.trim(),
    ...(seedAsins.length > 0 ? { seedAsins } : {}),
  }

  clearPolling()
  createError.value = null
  loadError.value = null
  downloadError.value = null
  creating.value = true

  let jobId = ''
  try {
    const created = await createResearchJob(request)
    jobId = created.jobId
  } catch (error) {
    createError.value = normalizeError(error, '市场调研任务创建失败')
  } finally {
    creating.value = false
  }

  if (!jobId || disposed) return

  currentJobId.value = jobId
  currentJob.value = null
  await router.replace({ query: { ...route.query, jobId } })
  ElMessage.success('市场调研任务已创建')
  await loadJob(jobId)
}

async function loadJob(jobId: string) {
  clearPolling()
  const version = ++requestVersion
  jobLoading.value = true
  loadError.value = null

  try {
    const detail = await getResearchJob(jobId)
    if (disposed || version !== requestVersion || currentJobId.value !== jobId) return
    currentJob.value = detail
    if (!isTerminalStatus(detail.status)) schedulePolling(jobId)
  } catch (error) {
    if (disposed || version !== requestVersion || currentJobId.value !== jobId) return
    loadError.value = normalizeError(error, '市场调研任务状态加载失败')
  } finally {
    if (!disposed && version === requestVersion) jobLoading.value = false
  }
}

async function downloadReport() {
  const job = currentJob.value
  if (!job?.downloadable || downloading.value) return

  downloading.value = true
  downloadError.value = null
  try {
    const report = await downloadResearchReport(job.jobId)
    const objectUrl = URL.createObjectURL(report)
    const link = document.createElement('a')
    link.href = objectUrl
    link.download = job.fileName?.trim() || `market-research-${job.jobId}.xlsx`
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1_000)
    ElMessage.success('市场调研报告已开始下载')
  } catch (error) {
    downloadError.value = normalizeError(error, '市场调研报告下载失败')
  } finally {
    downloading.value = false
  }
}

function schedulePolling(jobId: string) {
  if (disposed) return
  clearPolling()
  pollTimer = window.setTimeout(() => void loadJob(jobId), POLL_INTERVAL_MS)
}

function clearPolling() {
  if (pollTimer === null) return
  window.clearTimeout(pollTimer)
  pollTimer = null
}

function parseSeedAsins(source: string) {
  return source.split(/[\s,，;；]+/).map((item) => item.trim()).filter(Boolean)
}

function isTerminalStatus(status: ResearchJobStatus) {
  return status === 'SUCCEEDED' || status === 'FAILED'
}

function readRouteJobId() {
  const value = route.query.jobId
  const jobId = Array.isArray(value) ? value[0] : value
  return typeof jobId === 'string' ? jobId.trim() : ''
}

function switchRouteJob(jobId: string) {
  if (jobId === currentJobId.value) return

  clearPolling()
  requestVersion += 1
  currentJobId.value = jobId
  currentJob.value = null
  loadError.value = null
  downloadError.value = null
  jobLoading.value = false

  if (jobId) void loadJob(jobId)
}

function formatDateTime(timestamp: number | null) {
  if (!timestamp) return '--'
  return new Date(timestamp).toLocaleString('zh-CN', { hour12: false })
}

function formatError(error: PageError) {
  return [error.code, error.message, error.trackId ? `追踪号 ${error.trackId}` : '']
    .filter(Boolean)
    .join(' · ')
}

function normalizeError(error: unknown, fallback: string): PageError {
  if (error instanceof ApiError) {
    return { code: error.code, message: error.message, trackId: error.trackId }
  }
  if (error instanceof Error) return { message: error.message || fallback }
  return { message: fallback }
}

watch(readRouteJobId, switchRouteJob, { immediate: true })

onBeforeUnmount(() => {
  disposed = true
  requestVersion += 1
  clearPolling()
})
</script>

<template>
  <section
    class="research-page"
    aria-label="市场调研报告"
  >
    <header class="page-header">
      <div>
        <h1>市场调研报告</h1>
        <p>创建固定八阶段任务，跟踪执行进度并下载 Excel 报告。</p>
      </div>
      <ElTag
        type="info"
        effect="plain"
      >
        {{ MARKETPLACE }} · {{ TEMPLATE_CODE }}
      </ElTag>
    </header>

    <div class="research-layout">
      <section
        class="form-panel"
        aria-label="创建市场调研任务"
      >
        <div class="panel-heading">
          <h2>任务参数</h2>
          <span>固定美国站模板</span>
        </div>

        <ElForm
          ref="formRef"
          :model="form"
          :rules="formRules"
          label-position="top"
          status-icon
        >
          <ElFormItem
            label="报告名称"
            prop="reportName"
          >
            <ElInput
              v-model="form.reportName"
              aria-label="报告名称"
              maxlength="128"
              placeholder="例如：美容仪美国站市场调研"
              show-word-limit
            />
          </ElFormItem>
          <ElFormItem
            label="核心关键词"
            prop="keyword"
          >
            <ElInput
              v-model="form.keyword"
              aria-label="核心关键词"
              maxlength="256"
              placeholder="例如：facial cleansing device"
              show-word-limit
            />
          </ElFormItem>
          <ElFormItem
            label="种子 ASIN（可选）"
            prop="seedAsinsText"
          >
            <ElInput
              v-model="form.seedAsinsText"
              aria-label="种子 ASIN"
              type="textarea"
              :rows="5"
              resize="vertical"
              placeholder="每行一个，或使用逗号分隔"
            />
            <span class="form-help">最多 20 个，每个 ASIN 为 10 位字母或数字。</span>
          </ElFormItem>

          <div class="fixed-fields">
            <div>
              <span>站点</span>
              <strong>{{ MARKETPLACE }}</strong>
            </div>
            <div>
              <span>报告模板</span>
              <strong>{{ TEMPLATE_CODE }}</strong>
            </div>
          </div>

          <ElAlert
            v-if="createError"
            class="form-alert"
            type="error"
            :title="formatError(createError)"
            :closable="false"
            show-icon
          />

          <ElButton
            class="create-button"
            type="primary"
            :icon="DocumentAdd"
            :loading="creating"
            :disabled="submissionLocked"
            data-testid="create-research-job"
            @click="submitResearchJob"
          >
            {{ submitButtonText }}
          </ElButton>
        </ElForm>
      </section>

      <section
        class="workflow-panel"
        aria-label="市场调研任务进度"
      >
        <StatePanel
          v-if="!currentJobId"
          status="empty"
          title="尚未创建调研任务"
          description="填写任务参数后，可在此查看八阶段执行进度。"
        />
        <StatePanel
          v-else-if="jobLoading && !currentJob"
          status="loading"
          title="正在读取任务状态"
        />
        <StatePanel
          v-else-if="loadError && !currentJob"
          status="error"
          title="任务状态加载失败"
          :description="formatError(loadError)"
          action-label="重新加载"
          @action="loadJob(currentJobId)"
        />

        <template v-else-if="currentJob">
          <div class="job-heading">
            <div>
              <div class="job-heading__status">
                <ElTag
                  v-if="currentStatus"
                  :type="currentStatus.type"
                  effect="light"
                  data-testid="research-job-status"
                >
                  {{ currentStatus.label }}
                </ElTag>
                <span>{{ currentPhaseName }}</span>
              </div>
              <h2>{{ currentJob.reportName }}</h2>
              <code>{{ currentJob.jobId }}</code>
            </div>
            <ElTooltip content="立即刷新任务状态">
              <ElButton
                :icon="RefreshRight"
                circle
                :loading="jobLoading"
                aria-label="刷新任务状态"
                @click="loadJob(currentJob.jobId)"
              />
            </ElTooltip>
          </div>

          <ElAlert
            v-if="loadError"
            class="job-alert"
            type="error"
            :title="formatError(loadError)"
            :closable="false"
            show-icon
          />

          <ElDescriptions
            class="job-summary"
            :column="2"
            border
          >
            <ElDescriptionsItem label="站点">
              {{ currentJob.marketplace }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="数据源">
              {{ currentJob.dataSourceMode }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="核心关键词">
              {{ currentJob.keyword }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="创建时间">
              {{ formatDateTime(currentJob.createdAt) }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="开始时间">
              {{ formatDateTime(currentJob.startedAt) }}
            </ElDescriptionsItem>
            <ElDescriptionsItem label="完成时间">
              {{ formatDateTime(currentJob.finishedAt) }}
            </ElDescriptionsItem>
          </ElDescriptions>

          <div class="progress-block">
            <div>
              <h3>执行进度</h3>
              <span>{{ normalizedProgress }}%</span>
            </div>
            <ElProgress
              :percentage="normalizedProgress"
              :show-text="false"
              :stroke-width="10"
              :status="currentJob.status === 'FAILED'
                ? 'exception'
                : currentJob.status === 'SUCCEEDED' ? 'success' : undefined"
            />
          </div>

          <div class="steps-scroll">
            <ElSteps
              class="research-steps research-steps--horizontal"
              :active="activePhase"
              finish-status="success"
              :process-status="currentJob.status === 'FAILED' ? 'error' : 'process'"
              align-center
            >
              <ElStep
                v-for="phase in researchPhaseCodes"
                :key="phase"
                :title="phaseLabels[phase]"
              />
            </ElSteps>
          </div>

          <ElSteps
            class="research-steps research-steps--vertical"
            :active="activePhase"
            finish-status="success"
            :process-status="currentJob.status === 'FAILED' ? 'error' : 'process'"
            direction="vertical"
            :space="48"
          >
            <ElStep
              v-for="phase in researchPhaseCodes"
              :key="phase"
              :title="phaseLabels[phase]"
            />
          </ElSteps>

          <ElAlert
            v-if="currentJob.status === 'FAILED'"
            class="job-result"
            type="error"
            title="市场调研任务执行失败"
            :description="failureDescription"
            :closable="false"
            show-icon
          />
          <ElAlert
            v-else-if="currentJob.status === 'SUCCEEDED'"
            class="job-result"
            type="success"
            title="报告已生成"
            :description="currentJob.fileName || 'Excel 报告已通过校验，可以下载。'"
            :closable="false"
            show-icon
          />

          <ElAlert
            v-if="downloadError"
            class="job-alert"
            type="error"
            :title="formatError(downloadError)"
            :closable="false"
            show-icon
          />

          <div
            v-if="currentJob.status === 'SUCCEEDED'"
            class="download-row"
          >
            <div>
              <span>报告文件</span>
              <strong>{{ currentJob.fileName || 'Excel 报告' }}</strong>
            </div>
            <ElButton
              type="primary"
              :icon="Download"
              :loading="downloading"
              :disabled="!currentJob.downloadable"
              data-testid="download-research-report"
              @click="downloadReport"
            >
              下载报告
            </ElButton>
          </div>
        </template>
      </section>
    </div>
  </section>
</template>

<style scoped>
.research-page{display:flex;min-height:calc(100dvh - 104px);flex-direction:column;overflow:hidden;background:var(--color-surface);border:1px solid var(--color-border);border-radius:var(--radius-lg)}.page-header{display:flex;align-items:center;justify-content:space-between;gap:16px;padding:18px 20px;border-bottom:1px solid var(--color-border)}h1,h2,h3{margin:0;color:var(--color-text);letter-spacing:0}h1{font-size:20px}h2{font-size:16px}h3{font-size:14px}.page-header p{margin:5px 0 0;color:var(--color-text-secondary);font-size:12px}.research-layout{display:grid;grid-template-columns:minmax(300px,380px) minmax(0,1fr);min-height:0;flex:1}.form-panel{padding:20px;border-right:1px solid var(--color-border);background:var(--color-surface-muted)}.panel-heading{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:20px}.panel-heading span{color:var(--color-text-secondary);font-size:12px}.form-help{display:block;margin-top:6px;color:var(--color-text-secondary);font-size:12px;line-height:1.5}.fixed-fields{display:grid;grid-template-columns:1fr 1fr;gap:10px;margin:4px 0 18px}.fixed-fields>div{display:grid;gap:5px;padding:11px 12px;border:1px solid var(--color-border);border-radius:var(--radius-md);background:var(--color-surface)}.fixed-fields span,.download-row span{color:var(--color-text-secondary);font-size:12px}.fixed-fields strong,.download-row strong{min-width:0;color:var(--color-text);font-size:13px;overflow-wrap:anywhere}.form-alert,.job-alert{margin:0 0 14px}.create-button{width:100%}.workflow-panel{min-width:0;padding:20px}.job-heading{display:flex;align-items:flex-start;justify-content:space-between;gap:16px;padding-bottom:16px;border-bottom:1px solid var(--color-border)}.job-heading__status{display:flex;align-items:center;gap:10px;margin-bottom:9px;color:var(--color-text-secondary);font-size:12px}.job-heading code{display:block;margin-top:7px;color:var(--color-text-muted);font:12px/1.5 var(--font-mono);overflow-wrap:anywhere}.job-summary{margin-top:16px}.progress-block{display:grid;gap:10px;margin-top:22px}.progress-block>div{display:flex;align-items:center;justify-content:space-between;gap:12px}.progress-block span{color:var(--color-text-secondary);font:13px var(--font-mono)}.steps-scroll{margin-top:24px;padding:4px 0 10px;overflow:hidden}.research-steps{width:100%;min-width:0}.research-steps--vertical{display:none;height:384px;margin-top:24px}.job-result{margin-top:18px}.download-row{display:flex;align-items:center;justify-content:space-between;gap:18px;margin-top:18px;padding:14px 16px;border:1px solid var(--color-border);border-radius:var(--radius-md);background:var(--color-surface-muted)}.download-row>div{display:grid;min-width:0;gap:4px}@media(max-width:1180px){.research-layout{grid-template-columns:1fr}.form-panel{border-right:0;border-bottom:1px solid var(--color-border)}}@media(max-width:768px){.steps-scroll{display:none}.research-steps--vertical{display:flex}}@media(max-width:640px){.research-page{min-height:calc(100dvh - 88px)}.page-header{align-items:flex-start;flex-direction:column;padding:16px}.form-panel,.workflow-panel{padding:16px}.fixed-fields{grid-template-columns:1fr}.job-summary :deep(.el-descriptions__body) .el-descriptions__table{display:block}.job-summary :deep(.el-descriptions__cell){display:block;width:100%}.download-row{align-items:stretch;flex-direction:column}.download-row .el-button{width:100%}}
</style>
