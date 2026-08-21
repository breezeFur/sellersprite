<script setup lang="ts">
import {
  Check,
  Close,
  DocumentAdd,
  Download,
  FullScreen,
  RefreshRight,
  Search,
  Share,
} from '@element-plus/icons-vue'
import {
  ElMessage,
  type CascaderNode,
  type CascaderOption,
  type CascaderProps,
  type FormInstance,
  type FormRules,
} from 'element-plus'
import { storeToRefs } from 'pinia'
import { computed, onActivated, onBeforeUnmount, onDeactivated, onMounted, reactive, ref, toRaw, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import { useLayoutStore } from '@/layouts/useLayoutStore'
import { ApiError } from '@/shared/api/ApiError'
import StatePanel from '@/shared/components/StatePanel.vue'

import {
  cancelResearchJob,
  createResearchJob,
  getResearchCategoryNodes,
  getResearchWorkflowTopology,
  resolveResearchCategoriesByAsins,
  retryResearchJob,
} from '../api/researchApi'
import { downloadResearchArtifact, streamResearchEvents } from '../api/researchStreamApi'
import ResearchEvidencePanel from '../components/ResearchEvidencePanel.vue'
import ResearchConversationPanel from '../components/ResearchConversationPanel.vue'
import ResearchProductSelectionPanel from '../components/ResearchProductSelectionPanel.vue'
import ResearchReportWorkspace from '../components/ResearchReportWorkspace.vue'
import ResearchTaskInputSummary from '../components/ResearchTaskInputSummary.vue'
import ResearchWorkflowDiagram from '../components/ResearchWorkflowDiagram.vue'
import {
  createDefaultCollectionGraphConfig,
  defaultResearchWorkflowSteps,
  researchArtifactLabels,
  researchArtifactTypes,
  researchWorkflowPhaseCount,
  resolveResearchWorkflowStepCode,
  type CollectionGraphConfig,
  type ResearchArtifactSummary,
  type ResearchCategoryCandidate,
  type ResearchCategoryNode,
  type ResearchJobCreateRequest,
  type ResearchJobStatus,
  type ResearchNodeExecution,
  type ResearchWorkflowStep,
  type ResearchWorkflowStepCode,
  type ResearchWorkflowTopology,
  type SellerSpriteMarketplace,
} from '../model/research'
import {
  researchWorkspaceIntent,
  type ResearchWorkspaceLiveSection,
} from '../model/researchEventPresentation'
import type { ResearchStreamFrame, ResearchStreamRecord } from '../model/researchStream'
import { useResearchAgentStore } from '../stores/useResearchAgentStore'

interface ResearchFormModel {
  reportName: string
  marketplace: SellerSpriteMarketplace
  month: string
  nodeIdPath: string
  keyword: string
  analysisGoal: string
  collectionConfig: CollectionGraphConfig
}

interface PageError {
  code?: string
  message: string
  traceId?: string
}

interface CategorySearchTreeNode {
  label: string
  value: string
  products: number | null
  children: CategorySearchTreeNode[]
}

type ResearchWorkspaceSection = ResearchWorkspaceLiveSection | 'selection' | 'artifacts'
type ResearchAgentWorkspaceSection = 'report' | 'process'

const WORKFLOW_VERSION = 'market-research-v6-cache-insights'
const HORIZONTAL_STEP_WIDTH_PX = 154
const VERTICAL_STEP_HEIGHT_PX = 48
const PRODUCT_SELECTION_EVENT_TYPES = new Set([
  'product_selection_required',
  'product_selection_submitted',
  'market_abandoned',
])
const DEEP_DIVE_VISIBLE_STAGES = new Set([
  'DEEP_DIVE',
  'FINAL_ANALYSIS',
  'ARTIFACT_FINALIZATION',
])

const marketplaceOptions: Array<{
  value: SellerSpriteMarketplace
  label: string
}> = [
  { value: 'US', label: '美国站 US' },
  { value: 'JP', label: '日本站 JP' },
  { value: 'UK', label: '英国站 UK' },
  { value: 'DE', label: '德国站 DE' },
  { value: 'FR', label: '法国站 FR' },
  { value: 'IT', label: '意大利站 IT' },
  { value: 'ES', label: '西班牙站 ES' },
  { value: 'CA', label: '加拿大站 CA' },
  { value: 'IN', label: '印度站 IN' },
]

const statusPresentation: Record<ResearchJobStatus, {
  label: string
  type: 'info' | 'warning' | 'success' | 'danger'
}> = {
  QUEUED: { label: '排队中', type: 'info' },
  RUNNING: { label: '执行中', type: 'warning' },
  RETRY_WAIT: { label: '等待重试', type: 'warning' },
  WAITING_INPUT: { label: '等待商品选择', type: 'warning' },
  SUCCEEDED: { label: '已完成', type: 'success' },
  ABANDONED: { label: '已放弃', type: 'info' },
  FAILED: { label: '执行失败', type: 'danger' },
  CANCELLED: { label: '已取消', type: 'info' },
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const layoutStore = useLayoutStore()
const researchAgentStore = useResearchAgentStore()
const {
  activeAnalysisRunId,
  analysisState,
  connecting,
  connectionError,
  events: researchEvents,
  historyLoading,
  job: currentJob,
  lastSequence,
  nodes: nodeExecutions,
  reconnecting,
  workflowTerminal,
  workspaceActiveEventId,
  workspaceSuggestedSection,
} = storeToRefs(researchAgentStore)
const workspaceStreamStatusLabel = computed(() => {
  if (historyLoading.value) return '正在恢复历史事件'
  if (reconnecting.value) return `连接中断，正在从 #${lastSequence.value} 恢复`
  if (connecting.value) return '正在建立事件流'
  if (workflowTerminal.value || analysisState.value === 'SUCCEEDED') {
    return '本轮工作流已结束，可继续追问'
  }
  if (analysisState.value === 'FAILED') return '本轮分析失败，可重新执行或继续分析'
  if (currentJob.value?.status !== 'SUCCEEDED') return '等待数据 Graph 产出证据'
  return '等待分析事件'
})
const workspaceStreamStatusType = computed<'info' | 'warning' | 'success' | 'danger'>(() => {
  if (connectionError.value || analysisState.value === 'FAILED') return 'danger'
  if (workflowTerminal.value || analysisState.value === 'SUCCEEDED') return 'success'
  if (historyLoading.value || reconnecting.value || connecting.value) return 'warning'
  return 'info'
})
const currentJobId = computed(() => researchAgentStore.jobId)
const productSelectionDraftAsins = ref<string[] | undefined>(undefined)
const activeEvidenceStage = computed<'SCREENING' | 'DEEP_DIVE'>({
  get: () => researchAgentStore.workspaceEvidenceStage,
  set: (stage) => researchAgentStore.setWorkspaceEvidenceStage(stage),
})
const autoWorkspaceSuppressedJobId = ref('')
const autoWorkspaceSuppressedRunId = ref('')
const lastObservedAnalysisRunId = ref('')
const followUpAnalysisRunIds = ref<string[]>([])
const workspaceExitLocked = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<ResearchFormModel>({
  reportName: '',
  marketplace: 'US',
  month: previousBusinessMonth(),
  nodeIdPath: '',
  keyword: '',
  analysisGoal: '',
  collectionConfig: createDefaultCollectionGraphConfig(),
})
const formRules: FormRules<ResearchFormModel> = {
  reportName: [
    { required: true, whitespace: true, message: '请输入报告名称', trigger: 'blur' },
    { max: 128, message: '报告名称不能超过 128 个字符', trigger: 'blur' },
  ],
  marketplace: [
    { required: true, message: '请选择市场', trigger: 'change' },
  ],
  month: [
    { required: true, message: '请选择月份', trigger: 'change' },
    { pattern: /^\d{4}-(0[1-9]|1[0-2])$/, message: '月份必须为 yyyy-MM 格式', trigger: 'change' },
  ],
  nodeIdPath: [
    { required: true, message: '请选择类目', trigger: 'change' },
  ],
  keyword: [
    { max: 256, message: '核心关键词不能超过 256 个字符', trigger: 'blur' },
  ],
  analysisGoal: [
    { max: 1000, message: '分析目标不能超过 1000 个字符', trigger: 'blur' },
  ],
}

const creating = ref(false)
const categoryPendingRequests = ref(0)
const categoryKeyword = ref('')
const categorySubmittedKeyword = ref('')
const categorySearchActive = ref(false)
const categorySearchLoading = ref(false)
const categorySearchOptions = ref<CascaderOption[]>([])
const categoryLoading = computed(() => (
  categoryPendingRequests.value > 0 || categorySearchLoading.value
))
const categoryTreeVersion = ref(0)
const categoryError = ref<PageError | null>(null)
const categoryLazyCascaderProps: CascaderProps = {
  checkStrictly: true,
  emitPath: false,
  lazy: true,
  lazyLoad: loadCategoryChildren,
}
const categorySearchCascaderProps: CascaderProps = {
  checkStrictly: true,
  emitPath: false,
}
const categoryCascaderProps = computed<CascaderProps>(() => (
  categorySearchActive.value ? categorySearchCascaderProps : categoryLazyCascaderProps
))
const categoryCascaderOptions = computed(() => (
  categorySearchActive.value ? categorySearchOptions.value : []
))
const categoryCascaderPlaceholder = computed(() => (
  categorySearchActive.value ? '选择搜索结果' : '逐级选择类目'
))
const expandedCollectionNodes = ref(['collectMarketSalesTrend', 'collectReviews'])
const expandedExecutionGraphs = ref<ResearchWorkflowStepCode[]>([
  'SCREENING',
  'DEEP_DIVE',
  'FINAL_ANALYSIS',
])
const downloadingArtifactId = ref('')
const actionLoading = ref(false)
const jobLoading = ref(false)
const createError = ref<PageError | null>(null)
const loadError = ref<PageError | null>(null)
const downloadError = ref<PageError | null>(null)
const actionError = ref<PageError | null>(null)
const workflowVisible = ref(false)
const workflowLoading = ref(false)
const workflowTopology = ref<ResearchWorkflowTopology | null>(null)
const workflowError = ref<PageError | null>(null)

let streamController: AbortController | null = null
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
let reconnectAttempt = 0
let streamVersion = 0
let disposed = false
let autoWorkspaceTimer: ReturnType<typeof setTimeout> | null = null
let productSelectionGateRevealPending = false
let authoritativeSelectionStateJobId = ''

const currentStatus = computed(() => (
  currentJob.value ? statusPresentation[currentJob.value.status] : null
))
const workflowSteps = computed<ResearchWorkflowStep[]>(() => (
  workflowTopology.value?.steps?.length
    ? workflowTopology.value.steps
    : defaultResearchWorkflowSteps
))
const horizontalStepsWidth = computed(() => (
  `${workflowSteps.value.length * HORIZONTAL_STEP_WIDTH_PX}px`
))
const verticalStepsHeight = computed(() => (
  `${workflowSteps.value.length * VERTICAL_STEP_HEIGHT_PX}px`
))
const activeWorkflowStep = computed(() => {
  const job = currentJob.value
  if (!job) return 0
  if (job.status === 'SUCCEEDED') return workflowSteps.value.length
  if (job.status === 'ABANDONED') return workflowStepIndex('PRODUCT_SELECTION')
  const currentNodeStep = workflowSteps.value.findIndex((step) => step.nodeCode === job.currentNode)
  if (currentNodeStep >= 0) return currentNodeStep
  if (job.status === 'WAITING_INPUT' || job.waitingInputType === 'PRODUCT_SELECTION') {
    return workflowStepIndex('PRODUCT_SELECTION')
  }
  if (job.currentStage === 'ARTIFACT_FINALIZATION') {
    return workflowStepIndex('ARTIFACT_FINALIZATION')
  }
  if (job.currentStage === 'PRODUCT_SELECTION') return workflowStepIndex('PRODUCT_SELECTION')
  if (job.currentStage === 'FINAL_ANALYSIS') return workflowStepIndex('FINAL_ANALYSIS')
  if (job.currentStage === 'DEEP_DIVE') return workflowStepIndex('DEEP_DIVE')
  return workflowStepIndex('SCREENING')
})
const normalizedProgress = computed(() => {
  const job = currentJob.value
  if (!job) return 0
  if (activeWorkflowStep.value >= workflowSteps.value.length) return 100
  const step = workflowSteps.value[activeWorkflowStep.value]
  const code = toWorkflowStepCode(step?.code)
  const localProgress = code ? workflowStepProgress(code) : 0
  return Math.round(
    Math.min(1, Math.max(0, (activeWorkflowStep.value + localProgress)
      / Math.max(1, workflowSteps.value.length))) * 100,
  )
})
const nodeGroups = computed(() => workflowSteps.value
  .filter((step) => ['SCREENING', 'DEEP_DIVE', 'FINAL_ANALYSIS'].includes(step.code))
  .map((step) => ({
    stepCode: step.code as ResearchWorkflowStepCode,
    label: step.label,
    nodes: nodeExecutions.value.filter((node) => (
      resolveNodeWorkflowStepCode(node) === step.code
    )),
  })))
const currentNodeName = computed(() => {
  const job = currentJob.value
  if (!job) return ''
  const runningNode = nodeExecutions.value.find((node) => node.status === 'RUNNING')
  if (runningNode?.nodeName) return runningNode.nodeName
  const directStep = workflowSteps.value.find((step) => step.nodeCode === job.currentNode)
  if (directStep) return directStep.label
  if (analysisState.value === 'RUNNING') {
    const stepCode = job.currentStage === 'FINAL_ANALYSIS'
      ? 'FINAL_ANALYSIS'
      : job.currentStage === 'DEEP_DIVE' ? 'DEEP_DIVE' : 'SCREENING'
    return `${workflowStepLabel(stepCode)} · AI 分析`
  }
  if (job.currentNodeName) return job.currentNodeName
  if (job.status === 'QUEUED') return '等待执行'
  if (job.status === 'RETRY_WAIT') return '等待自动重试'
  return '状态更新中'
})
const publishedArtifacts = computed(() => {
  const artifacts = new Map<string, ResearchArtifactSummary>()
  currentJob.value?.artifacts?.forEach((artifact) => artifacts.set(artifact.artifactId, artifact))
  researchEvents.value.forEach((event) => {
    const artifact = readArtifact(event.data)
    if (artifact) artifacts.set(artifact.artifactId, artifact)
  })
  return researchArtifactTypes.flatMap((artifactType) => (
    [...artifacts.values()].filter((artifact) => artifact.artifactType === artifactType)
  ))
})
const productSelectionPending = computed(() => (
  currentJob.value?.status === 'WAITING_INPUT'
  || currentJob.value?.waitingInputType === 'PRODUCT_SELECTION'
))
const selectionReviewVisible = computed(() => (
  currentJob.value?.status === 'WAITING_INPUT'
  || currentJob.value?.waitingInputType === 'PRODUCT_SELECTION'
  || researchEvents.value.some((event) => PRODUCT_SELECTION_EVENT_TYPES.has(event.eventType))
))
const deepDiveEvidenceVisible = computed(() => {
  const job = currentJob.value
  if (!job || job.status === 'ABANDONED') return false
  if (job.status === 'SUCCEEDED' || DEEP_DIVE_VISIBLE_STAGES.has(String(job.currentStage))) return true
  return researchEvents.value.some((event) => event.stageCode === 'DEEP_DIVE')
})
const hasActiveJob = computed(() => (
  currentJob.value !== null && !isTerminalStatus(currentJob.value.status)
))
const submissionLocked = computed(() => (
  creating.value
  || categoryLoading.value
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
const selectedContextLabel = computed(() => (
  currentJob.value
    ? `${currentJob.value.marketplace} · ${currentJob.value.month} · ${currentJob.value.workflowVersion}`
    : `${form.marketplace} · ${form.month} · ${WORKFLOW_VERSION}`
))
const hasAnalysisConversation = computed(() => (
  !['IDLE', 'QUEUED'].includes(analysisState.value)
  || researchEvents.value.some((event) => (
    (event.scope === 'analysis' || Boolean(event.analysisRunId))
    && ![
      'analysis_waiting_research',
      'analysis_queued',
      'user_message',
      'follow_up_requested',
    ].includes(event.eventType)
  ))
))
const workspaceMode = computed(() => (
  Boolean(readRouteJobId())
  && readRouteQueryValue('view') === 'conversation'
))
const workspaceSection = computed<ResearchWorkspaceSection>(() => {
  const section = readRouteQueryValue('section')
  if (['report', 'evidence', 'selection', 'process', 'artifacts'].includes(section)) {
    return section as ResearchWorkspaceSection
  }
  return 'report'
})
const workspaceShowsAgentPanel = computed(() => (
  ['report', 'process'].includes(workspaceSection.value)
))
const workspaceAgentSection = computed<ResearchAgentWorkspaceSection>(() => (
  workspaceShowsAgentPanel.value
    ? workspaceSection.value as ResearchAgentWorkspaceSection
    : 'report'
))
const evidenceStageOptions = computed(() => [
  { label: '阶段一市场判断', value: 'SCREENING' },
  ...(deepDiveEvidenceVisible.value
    ? [{ label: '阶段二深度验证', value: 'DEEP_DIVE' }]
    : []),
])

async function loadCategoryChildren(
  node: CascaderNode,
  resolve: (nodes?: CascaderOption[]) => void,
  reject: () => void,
) {
  const requestVersion = categoryTreeVersion.value
  const nodeIdPath = node.root ? '' : String(node.value ?? '').trim()
  categoryPendingRequests.value += 1
  categoryError.value = null
  try {
    const nodes = await getResearchCategoryNodes({
      marketplace: form.marketplace,
      month: form.month,
      ...(nodeIdPath ? { nodeIdPath } : {}),
    })
    if (requestVersion !== categoryTreeVersion.value) {
      resolve([])
      return
    }
    resolve(nodes
      .filter((categoryNode) => Boolean(categoryNode.nodeIdPath?.trim()))
      .map(toCategoryOption))
  } catch (error) {
    if (requestVersion === categoryTreeVersion.value) {
      categoryError.value = normalizeError(error, '类目查询失败')
    }
    reject()
  } finally {
    categoryPendingRequests.value = Math.max(0, categoryPendingRequests.value - 1)
  }
}

async function searchCategoryOptions() {
  const keyword = categoryKeyword.value.trim()
  if (!keyword) {
    resetCategorySearch()
    return
  }

  categoryKeyword.value = keyword
  categorySubmittedKeyword.value = keyword
  form.nodeIdPath = ''
  categoryError.value = null
  categorySearchActive.value = true
  categorySearchOptions.value = []
  categoryTreeVersion.value += 1
  const requestVersion = categoryTreeVersion.value
  categorySearchLoading.value = true

  try {
    const nodes = await getResearchCategoryNodes({
      marketplace: form.marketplace,
      month: form.month,
      keyword,
    })
    if (
      requestVersion !== categoryTreeVersion.value
      || categorySubmittedKeyword.value !== keyword
    ) return
    categorySearchOptions.value = toCategorySearchOptions(nodes)
  } catch (error) {
    if (
      requestVersion === categoryTreeVersion.value
      && categorySubmittedKeyword.value === keyword
    ) {
      categoryError.value = normalizeError(error, '类目搜索失败')
    }
  } finally {
    if (
      requestVersion === categoryTreeVersion.value
      && categorySubmittedKeyword.value === keyword
    ) {
      categorySearchLoading.value = false
    }
  }
}

function resetCategorySearch() {
  categoryKeyword.value = ''
  if (!categorySearchActive.value) return
  invalidateCategorySearch()
}

function invalidateCategorySearch() {
  categorySubmittedKeyword.value = ''
  categorySearchActive.value = false
  categorySearchLoading.value = false
  categorySearchOptions.value = []
  form.nodeIdPath = ''
  categoryError.value = null
  categoryTreeVersion.value += 1
}

const categoryAsin = ref('')
const resolvingCategory = ref(false)

async function handleResolveCategoryByAsin() {
  const asin = categoryAsin.value.trim().toUpperCase()
  if (!asin) {
    ElMessage.warning('请输入要反查的单个 ASIN')
    return
  }
  if (!/^[A-Z0-9]{10}$/.test(asin)) {
    ElMessage.warning('ASIN 必须是 10 位字母或数字')
    return
  }

  resolvingCategory.value = true
  try {
    const candidates = await resolveResearchCategoriesByAsins({
      marketplace: form.marketplace,
      asins: [asin],
      month: form.month,
    })
    if (!candidates || candidates.length === 0) {
      ElMessage.info(`未识别到 ASIN [${asin}] 的所属类目，请检查 ASIN 或手动选择类目`)
      return
    }
    const candidate = candidates[0]
    applyCategoryCandidate(candidate)
    ElMessage.success(`已自动填入类目：${candidate.displayName}`)
  } catch (error) {
    ElMessage.error(errorMessage(error, '反查类目失败'))
  } finally {
    resolvingCategory.value = false
  }
}

function applyCategoryCandidate(candidate: ResearchCategoryCandidate) {
  form.nodeIdPath = candidate.nodeIdPath
  const node: ResearchCategoryNode = {
    nodeIdPath: candidate.nodeIdPath,
    nodeLabelPath: candidate.nodeLabelPath ?? null,
    products: null,
    nodeLabelLocale: candidate.nodeLabelLocale ?? null,
    nodeLabelPathLocale: null,
    displayName: candidate.displayName,
    nodeId: candidate.nodeId,
    nodeLabel: candidate.nodeLabel,
  }
  categorySearchActive.value = true
  categorySearchOptions.value = toCategorySearchOptions([node])
}

async function submitResearchJob() {
  if (submissionLocked.value) return

  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const analysisGoal = form.analysisGoal.trim()
  const collectionConfig = structuredClone(toRaw(form.collectionConfig))
  const request: ResearchJobCreateRequest = {
    reportName: form.reportName.trim(),
    marketplace: form.marketplace,
    nodeIdPath: form.nodeIdPath,
    month: form.month,
    ...(form.keyword.trim() ? { keyword: form.keyword.trim() } : {}),
    ...(analysisGoal ? { analysisGoal } : {}),
    collectionConfig,
  }

  createError.value = null
  loadError.value = null
  downloadError.value = null
  actionError.value = null
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

  await router.replace({ query: { ...route.query, jobId } })
  if (currentJobId.value !== jobId) switchRouteJob(jobId)
  ElMessage.success('市场调研任务已创建')
}

async function cancelCurrentJob() {
  const job = currentJob.value
  if (!job?.cancellable || actionLoading.value) return
  actionLoading.value = true
  actionError.value = null
  try {
    await cancelResearchJob(job.jobId)
    ElMessage.success('取消请求已提交')
    resumeStream()
  } catch (error) {
    actionError.value = normalizeError(error, '市场调研任务取消失败')
  } finally {
    actionLoading.value = false
  }
}

async function retryCurrentJob() {
  const job = currentJob.value
  if (!job?.retryable || actionLoading.value) return
  actionLoading.value = true
  actionError.value = null
  try {
    await retryResearchJob(job.jobId)
    ElMessage.success('市场调研任务已重新排队')
    resumeStream()
  } catch (error) {
    actionError.value = normalizeError(error, '市场调研任务重试失败')
  } finally {
    actionLoading.value = false
  }
}

async function downloadArtifact(artifact: ResearchArtifactSummary) {
  const job = currentJob.value
  if (!job || downloadingArtifactId.value) return

  downloadingArtifactId.value = artifact.artifactId
  downloadError.value = null
  try {
    const content = await downloadResearchArtifact(job.jobId, artifact)
    const objectUrl = URL.createObjectURL(content)
    const link = document.createElement('a')
    link.href = objectUrl
    link.download = artifact.fileName || `market-research-${artifact.artifactId}`
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1_000)
    ElMessage.success('调研产物已开始下载')
  } catch (error) {
    downloadError.value = normalizeError(error, '调研产物下载失败')
  } finally {
    downloadingArtifactId.value = ''
  }
}

async function toggleWorkflowTopology() {
  workflowVisible.value = !workflowVisible.value
  if (workflowVisible.value && !workflowTopology.value && !workflowLoading.value) {
    await loadWorkflowTopology()
  }
}

function startNewResearchTask() {
  clearAutoWorkspaceTimer()
  researchAgentStore.clearWorkspaceDismissal()
  researchAgentStore.resumeWorkspaceFollow()
  layoutStore.setSidebarCollapsed(false)
  void router.push({ path: '/research/market-report' })
}

async function loadWorkflowTopology() {
  workflowLoading.value = true
  workflowError.value = null
  try {
    workflowTopology.value = await getResearchWorkflowTopology()
  } catch (error) {
    workflowError.value = normalizeError(error, '市场调研工作流加载失败')
  } finally {
    workflowLoading.value = false
  }
}

function startStream(jobId: string, version = streamVersion, reconnecting = false) {
  if (
    disposed
    || !jobId
    || version !== streamVersion
    || jobId !== currentJobId.value
    || streamController
  ) return

  clearReconnectTimer()
  const controller = new AbortController()
  streamController = controller
  researchAgentStore.markConnecting(reconnecting)
  const connectionAfterSequence = lastSequence.value

  void streamResearchEvents(jobId, connectionAfterSequence, {
    signal: controller.signal,
    getAccessToken: () => authStore.accessToken,
    refreshAccessToken: () => authStore.refreshAccessToken(),
    onOpen: () => {
      if (!disposed && version === streamVersion && streamController === controller) {
        researchAgentStore.markConnected()
      }
    },
    onFrame: (frame) => {
      if (
        disposed
        || version !== streamVersion
        || streamController !== controller
        || frame.jobId !== currentJobId.value
      ) return
      const previousLastSequence = lastSequence.value
      const replayWasComplete = researchAgentStore.replayComplete
      const firstAuthoritativeJobFrame = authoritativeSelectionStateJobId !== frame.jobId
      const productSelectionWasPending = productSelectionPending.value
      researchAgentStore.applyFrame(frame)
      authoritativeSelectionStateJobId = frame.jobId
      reconnectAttempt = 0
      jobLoading.value = false
      loadError.value = null
      if (
        productSelectionPending.value
        && (firstAuthoritativeJobFrame || !productSelectionWasPending)
      ) {
        showProductSelectionGate()
      }
      handleWorkspaceFrame(
        frame,
        previousLastSequence,
        replayWasComplete,
        connectionAfterSequence,
      )
    },
  }).then(() => {
    if (!controller.signal.aborted && version === streamVersion) {
      scheduleReconnect(version, '事件流已关闭，正在恢复连接')
    }
  }).catch((error: unknown) => {
    if (!controller.signal.aborted && version === streamVersion) {
      scheduleReconnect(version, errorMessage(error, '事件流连接中断，正在恢复'))
    }
  }).finally(() => {
    if (streamController === controller) streamController = null
  })
}

function handleWorkspaceFrame(
  frame: ResearchStreamFrame,
  previousLastSequence: number,
  replayWasComplete: boolean,
  connectionAfterSequence: number,
) {
  if (!frame.replayComplete) return
  if (!replayWasComplete) {
    const latestReplayRecord = researchEvents.value
      .filter((event) => (
        event.sequenceNo !== undefined && event.sequenceNo > connectionAfterSequence
      ))
      .sort((left, right) => (left.sequenceNo ?? 0) - (right.sequenceNo ?? 0))
      .at(-1)
    if (connectionAfterSequence === 0) {
      syncInitialWorkspace(latestReplayRecord ?? researchEvents.value.at(-1))
    } else if (latestReplayRecord) {
      followWorkspaceEvent(latestReplayRecord)
    }
    return
  }
  const newSequences = new Set(frame.events
    .filter((event) => event.sequenceNo > previousLastSequence)
    .map((event) => event.sequenceNo))
  const liveRecords = researchEvents.value
    .filter((event) => event.sequenceNo !== undefined && newSequences.has(event.sequenceNo))
    .sort((left, right) => (left.sequenceNo ?? 0) - (right.sequenceNo ?? 0))
  const canFollowFrame = frame.frameType === 'events'
    ? replayWasComplete || frame.afterSequence > 0
    : frame.afterSequence > 0

  if (canFollowFrame && liveRecords.length > 0) {
    followWorkspaceEvent(liveRecords.at(-1)!)
    return
  }
}

function followWorkspaceEvent(event: ResearchStreamRecord) {
  const followUpContext = Boolean(event.analysisRunId) && (
    followUpAnalysisRunIds.value.includes(event.analysisRunId!)
    || researchEvents.value.some((candidate) => (
      candidate.analysisRunId === event.analysisRunId
      && researchEventRunType(candidate) === 'FOLLOW_UP'
    ))
  )
  const intent = researchWorkspaceIntent(event, followUpContext)
  const context = event.analysisRunId
    ? `${event.jobId}:analysis:${event.analysisRunId}`
    : `${event.jobId}:attempt:${currentJob.value?.attemptCount ?? 0}`

  if (intent.mode === 'task') {
    researchAgentStore.workspaceActiveEventId = event.id
    researchAgentStore.workspaceActiveSequence = Math.max(
      researchAgentStore.workspaceActiveSequence,
      event.sequenceNo ?? 0,
    )
    showProductSelectionGate()
    return
  }
  if (!intent.section) {
    researchAgentStore.workspaceActiveEventId = event.id
    researchAgentStore.workspaceActiveSequence = Math.max(
      researchAgentStore.workspaceActiveSequence,
      event.sequenceNo ?? 0,
    )
    return
  }

  const sectionChanged = Boolean(researchAgentStore.workspaceSuggestedSection)
    && researchAgentStore.workspaceSuggestedSection !== intent.section
  researchAgentStore.noteWorkspaceLiveEvent(
    intent.section,
    event.id,
    event.sequenceNo ?? 0,
    context,
    intent.evidenceStage,
  )
  if (researchAgentStore.workspaceFollowPaused && !intent.force) {
    if (!sectionChanged) return
    researchAgentStore.resumeWorkspaceFollow()
  }
  if (!workspaceMode.value && !shouldAutoEnterWorkspace()) return
  if (workspaceMode.value && workspaceSection.value === intent.section) return
  navigateToWorkspace(intent.section, true, false)
}

function researchEventRunType(event: ResearchStreamRecord) {
  if (!isRecord(event.data) || typeof event.data.runType !== 'string') return ''
  return event.data.runType.toUpperCase()
}

function updateFollowUpAnalysisRunIds(analysisRunIds: string[]) {
  followUpAnalysisRunIds.value = [...new Set(analysisRunIds)]
}

function syncInitialWorkspace(latestEvent?: ResearchStreamRecord) {
  if (workspaceMode.value || !shouldAutoEnterWorkspace()) return
  const status = currentJob.value?.status
  const section: ResearchWorkspaceLiveSection = status === 'SUCCEEDED'
    ? 'report'
    : 'process'
  if (latestEvent) {
    const context = latestEvent.analysisRunId
      ? `${latestEvent.jobId}:analysis:${latestEvent.analysisRunId}`
      : `${latestEvent.jobId}:attempt:${currentJob.value?.attemptCount ?? 0}`
    researchAgentStore.noteWorkspaceLiveEvent(
      section,
      latestEvent.id,
      latestEvent.sequenceNo ?? 0,
      context,
    )
  }
  scheduleAutoWorkspace(section)
}

function showProductSelectionGate() {
  if (!productSelectionPending.value || productSelectionGateRevealPending) return
  clearAutoWorkspaceTimer()
  researchAgentStore.resumeWorkspaceFollow()
  if (workspaceMode.value && workspaceSection.value === 'selection') return
  productSelectionGateRevealPending = true
  const navigation = navigateToWorkspace('selection', true, false)
  if (!navigation) {
    productSelectionGateRevealPending = false
    return
  }
  void navigation.then(
    () => { productSelectionGateRevealPending = false },
    () => { productSelectionGateRevealPending = false },
  )
}

function handleProductSelectionSubmitted() {
  researchAgentStore.resumeWorkspaceFollow()
  navigateToWorkspace('process', true, false)
  resumeStream()
}

function scheduleReconnect(version: number, message: string) {
  if (disposed || version !== streamVersion || !currentJobId.value) return
  researchAgentStore.markDisconnected(`${message}（从 #${lastSequence.value} 继续）`)
  jobLoading.value = false
  if (!currentJob.value) loadError.value = { message }
  const delay = Math.min(1_000 * 2 ** reconnectAttempt, 10_000)
  reconnectAttempt += 1
  clearReconnectTimer()
  reconnectTimer = window.setTimeout(() => startStream(currentJobId.value, version, true), delay)
}

function reconnectNow() {
  const jobId = currentJobId.value
  if (!jobId) return
  reconnectAttempt = 0
  clearReconnectTimer()
  streamController?.abort()
  streamController = null
  jobLoading.value = true
  loadError.value = null
  researchAgentStore.beginHistoryLoad()
  startStream(jobId, streamVersion, true)
}

function resumeStream() {
  if (!currentJobId.value || streamController) return
  reconnectAttempt = 0
  clearReconnectTimer()
  startStream(currentJobId.value, streamVersion, true)
}

function stopConnection() {
  clearReconnectTimer()
  streamController?.abort()
  streamController = null
  researchAgentStore.stopStreaming()
}

function clearReconnectTimer() {
  if (reconnectTimer === null) return
  window.clearTimeout(reconnectTimer)
  reconnectTimer = null
}

function previousBusinessMonth() {
  const now = new Date()
  const previousMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1)
  return `${previousMonth.getFullYear()}-${String(previousMonth.getMonth() + 1).padStart(2, '0')}`
}

function composeCategoryNodeName(node: ResearchCategoryNode) {
  if (node.displayName?.trim()) {
    return node.displayName.trim()
  }
  const en = node.nodeLabel?.trim() || lastCategoryPathSegment(node.nodeLabelPath)
  const zh = node.nodeLabelLocale?.trim() || lastCategoryPathSegment(node.nodeLabelPathLocale)
  if (en && zh) {
    return `${en} (${zh})`
  }
  return en || zh || node.nodeIdPath
}

function categoryLabel(node: ResearchCategoryNode) {
  const name = composeCategoryNodeName(node)
  return node.products === null ? name : `${name}（${node.products}）`
}

function lastCategoryPathSegment(path: string | null | undefined) {
  return path?.split(':').at(-1)?.trim() || ''
}

function toCategoryOption(node: ResearchCategoryNode): CascaderOption {
  return {
    label: categoryLabel(node),
    value: node.nodeIdPath.trim(),
  }
}

function toCategorySearchOptions(nodes: ResearchCategoryNode[]): CascaderOption[] {
  const roots: CategorySearchTreeNode[] = []
  const optionByPath = new Map<string, CategorySearchTreeNode>()

  nodes.forEach((node) => {
    const nodeIdSegments = splitCategoryPath(node.nodeIdPath)
    if (nodeIdSegments.length === 0) return
    const labelSegments = categoryPathLabels(node, nodeIdSegments)
    let siblings = roots

    nodeIdSegments.forEach((nodeId, index) => {
      const value = nodeIdSegments.slice(0, index + 1).join(':')
      let option = optionByPath.get(value)
      if (!option) {
        option = {
          label: labelSegments[index] || nodeId,
          value,
          products: null,
          children: [],
        }
        optionByPath.set(value, option)
        siblings.push(option)
      }
      if (index === nodeIdSegments.length - 1) {
        option.label = labelSegments[index] || nodeId
        option.products = node.products
      }
      siblings = option.children
    })
  })

  return roots.map(toSearchCascaderOption)
}

function categoryPathLabels(node: ResearchCategoryNode, nodeIdSegments: string[]) {
  const enLabels = splitCategoryPath(node.nodeLabelPath)
  const zhLabels = splitCategoryPath(node.nodeLabelPathLocale)

  return nodeIdSegments.map((nodeId, index) => {
    const en = enLabels[index]?.trim()
    const zh = zhLabels[index]?.trim()
      || (index === nodeIdSegments.length - 1 ? node.nodeLabelLocale?.trim() : undefined)
    if (en && zh) return `${en} (${zh})`
    if (en) return en
    if (zh) return zh
    return nodeId
  })
}

function splitCategoryPath(path: string | null) {
  return path?.split(':').map((segment) => segment.trim()).filter(Boolean) ?? []
}

function toSearchCascaderOption(node: CategorySearchTreeNode): CascaderOption {
  const children = node.children.map(toSearchCascaderOption)
  return {
    label: node.products === null ? node.label : `${node.label}（${node.products}）`,
    value: node.value,
    ...(children.length > 0 ? { children } : { leaf: true }),
  }
}

function isTerminalStatus(status: ResearchJobStatus) {
  return ['SUCCEEDED', 'ABANDONED', 'FAILED', 'CANCELLED'].includes(status)
}

function toWorkflowStepCode(code?: string | null): ResearchWorkflowStepCode | null {
  return defaultResearchWorkflowSteps.some((step) => step.code === code)
    ? code as ResearchWorkflowStepCode
    : null
}

function workflowStepIndex(code: ResearchWorkflowStepCode) {
  const index = workflowSteps.value.findIndex((step) => step.code === code)
  return index >= 0 ? index : 0
}

function workflowStepLabel(code: ResearchWorkflowStepCode) {
  return workflowSteps.value.find((step) => step.code === code)?.label
    || defaultResearchWorkflowSteps.find((step) => step.code === code)?.label
    || code
}

function currentStageLabel(stage?: string | null) {
  if (currentJob.value?.status === 'SUCCEEDED') {
    return workflowStepLabel('ARTIFACT_FINALIZATION')
  }
  if (currentJob.value?.status === 'WAITING_INPUT') return workflowStepLabel('PRODUCT_SELECTION')
  const code = toWorkflowStepCode(stage)
  return code ? workflowStepLabel(code) : stage || '--'
}

function resolveNodeWorkflowStepCode(node: ResearchNodeExecution) {
  const resolved = resolveResearchWorkflowStepCode(node.nodeCode)
  if (resolved) return resolved
  return node.graphCode === 'report' ? 'FINAL_ANALYSIS' : null
}

function workflowStepProgress(code: ResearchWorkflowStepCode) {
  const job = currentJob.value
  if (!job) return 0
  if (code === 'PRODUCT_SELECTION') return job.status === 'ABANDONED' ? 1 : 0.5
  if (code === 'ARTIFACT_FINALIZATION') {
    return Math.min(0.95, (job.artifacts?.length ?? 0) / researchArtifactTypes.length)
  }
  if (code === 'FINAL_ANALYSIS') {
    if (analysisState.value === 'SUCCEEDED') return 0.95
    return Math.min(0.95, Math.max(0.05, (job.analysisProgress ?? 0) / 100))
  }

  const totalPhases = researchWorkflowPhaseCount(code)
  const stageNodes = nodeExecutions.value.filter((node) => resolveNodeWorkflowStepCode(node) === code)
  const finishedPhases = new Set(stageNodes
    .filter((node) => node.status === 'SUCCEEDED')
    .map((node) => node.nodeCode))
  const runningShare = stageNodes.some((node) => node.status === 'RUNNING') ? 0.5 : 0
  const dataShare = totalPhases > 0
    ? Math.min(1, (finishedPhases.size + runningShare) / totalPhases)
    : 0
  const fallbackShare = stageNodes.length === 0
    ? Math.min(0.85, Math.max(0, (job.progress ?? 0) / 100) * 0.9)
    : 0
  const dataProgress = Math.max(fallbackShare, dataShare * 0.9)
  const stageMatches = job.currentStage === code
  const analysisProgress = stageMatches && analysisState.value === 'RUNNING'
    ? 0.9 + Math.min(0.09, Math.max(0, (job.analysisProgress ?? 0) / 100) * 0.09)
    : 0
  return Math.min(0.99, Math.max(dataProgress, analysisProgress))
}

function readRouteJobId() {
  return readRouteQueryValue('jobId').trim()
}

function readRouteQueryValue(key: string) {
  const value = route.query[key]
  const jobId = Array.isArray(value) ? value[0] : value
  return typeof jobId === 'string' ? jobId.trim() : ''
}

function switchRouteJob(jobId: string) {
  if (jobId === currentJobId.value && streamController) return

  stopConnection()
  streamVersion += 1
  reconnectAttempt = 0
  researchAgentStore.startJob(jobId)
  loadError.value = null
  downloadError.value = null
  actionError.value = null
  jobLoading.value = Boolean(jobId)

  if (jobId) {
    if (!workflowTopology.value && !workflowLoading.value) void loadWorkflowTopology()
    researchAgentStore.beginHistoryLoad()
    startStream(jobId, streamVersion)
  }
}

function formatDateTime(timestamp: number | null) {
  if (!timestamp) return '--'
  return new Date(timestamp).toLocaleString('zh-CN', { hour12: false })
}

function formatDuration(durationMs: number | null) {
  if (durationMs === null) return '--'
  if (durationMs < 1_000) return `${durationMs} ms`
  return `${(durationMs / 1_000).toFixed(1)} s`
}

function artifactLabel(artifact: ResearchArtifactSummary) {
  return researchArtifactLabels[
    artifact.artifactType as keyof typeof researchArtifactLabels
  ] ?? artifact.artifactType
}

function artifactsOfType(artifactType: string) {
  return publishedArtifacts.value.filter((artifact) => artifact.artifactType === artifactType)
}

function readArtifact(data: unknown): ResearchArtifactSummary | null {
  if (!isRecord(data)) return null
  if (
    typeof data.artifactId !== 'string'
    || typeof data.artifactType !== 'string'
    || typeof data.fileName !== 'string'
  ) return null
  return {
    artifactId: data.artifactId,
    analysisRunId: typeof data.analysisRunId === 'string' ? data.analysisRunId : null,
    artifactType: data.artifactType,
    fileName: data.fileName,
    mediaType: typeof data.mediaType === 'string' ? data.mediaType : 'application/octet-stream',
    fileSize: typeof data.fileSize === 'number' ? data.fileSize : null,
    createdAt: typeof data.createdAt === 'number' ? data.createdAt : Date.now(),
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

function formatError(error: PageError) {
  return [error.code, error.message, error.traceId ? `追踪号 ${error.traceId}` : '']
    .filter(Boolean)
    .join(' · ')
}

function normalizeError(error: unknown, fallback: string): PageError {
  if (error instanceof ApiError) {
    return { code: error.code, message: error.message, traceId: error.traceId }
  }
  if (error instanceof Error) return { message: error.message || fallback }
  return { message: fallback }
}

function errorMessage(error: unknown, fallback: string) {
  return normalizeError(error, fallback).message
}

function navigateToWorkspace(
  section: ResearchWorkspaceSection = 'report',
  replace = false,
  manual = true,
) {
  const jobId = currentJobId.value || readRouteJobId()
  if (!jobId) return null
  if (manual) {
    researchAgentStore.clearWorkspaceDismissal()
    autoWorkspaceSuppressedJobId.value = ''
    autoWorkspaceSuppressedRunId.value = ''
    workspaceExitLocked.value = false
  }
  const location = {
    query: {
      ...route.query,
      jobId,
      view: 'conversation',
      section,
    },
  }
  const navigation = replace ? router.replace(location) : router.push(location)
  layoutStore.setSidebarCollapsed(true)
  return navigation
}

function selectWorkspaceSection(section: ResearchWorkspaceSection) {
  if (section === workspaceSuggestedSection.value) researchAgentStore.resumeWorkspaceFollow()
  else researchAgentStore.pauseWorkspaceFollow()
  if (section === workspaceSection.value && readRouteQueryValue('section') === section) return
  navigateToWorkspace(section, true)
}

function exitWorkspaceMode() {
  clearAutoWorkspaceTimer()
  researchAgentStore.pauseWorkspaceFollow()
  autoWorkspaceSuppressedJobId.value = currentJobId.value
  autoWorkspaceSuppressedRunId.value = activeAnalysisRunId.value || lastObservedAnalysisRunId.value
  workspaceExitLocked.value = true
  if (currentJobId.value) {
    researchAgentStore.dismissWorkspace(
      currentJobId.value,
      autoWorkspaceSuppressedRunId.value,
    )
  }
  const query = { ...route.query }
  delete query.view
  delete query.section
  void router.push({ query })
}

function clearAutoWorkspaceTimer() {
  if (autoWorkspaceTimer === null) return
  window.clearTimeout(autoWorkspaceTimer)
  autoWorkspaceTimer = null
}

function scheduleAutoWorkspace(section: ResearchWorkspaceLiveSection) {
  clearAutoWorkspaceTimer()
  autoWorkspaceTimer = window.setTimeout(() => {
    autoWorkspaceTimer = null
    if (disposed || workspaceMode.value || !shouldAutoEnterWorkspace()) return
    navigateToWorkspace(section, true, false)
  }, 0)
}

function shouldAutoEnterWorkspace() {
  const status = currentJob.value?.status
  if (!currentJobId.value || !status || status === 'WAITING_INPUT') {
    return false
  }
  if (
    workspaceExitLocked.value
    && autoWorkspaceSuppressedJobId.value === currentJobId.value
  ) {
    const newAnalysisRunStarted = analysisState.value === 'RUNNING'
      && Boolean(activeAnalysisRunId.value)
      && activeAnalysisRunId.value !== autoWorkspaceSuppressedRunId.value
    if (!newAnalysisRunStarted) return false
    workspaceExitLocked.value = false
    researchAgentStore.clearWorkspaceDismissal()
  }
  return true
}

watch(categoryKeyword, (keyword) => {
  if (!categorySearchActive.value) return
  if (keyword.trim() !== categorySubmittedKeyword.value) invalidateCategorySearch()
})
watch([() => form.marketplace, () => form.month], () => {
  categoryKeyword.value = ''
  invalidateCategorySearch()
})
watch(workspaceMode, (enabled) => {
  if (enabled) layoutStore.setSidebarCollapsed(true)
  layoutStore.setWorkspaceFocusMode(enabled)
}, { immediate: true })

function syncWorkspaceFocusMode() {
  layoutStore.setWorkspaceFocusMode(workspaceMode.value)
}

onMounted(syncWorkspaceFocusMode)
onActivated(syncWorkspaceFocusMode)
onDeactivated(() => layoutStore.setWorkspaceFocusMode(false))
watch(deepDiveEvidenceVisible, (visible) => {
  if (!visible) activeEvidenceStage.value = 'SCREENING'
})
watch(activeAnalysisRunId, (analysisRunId) => {
  if (analysisRunId) lastObservedAnalysisRunId.value = analysisRunId
}, { immediate: true })
watch(currentJobId, (jobId, previousJobId) => {
  if (jobId !== previousJobId) {
    productSelectionDraftAsins.value = undefined
    followUpAnalysisRunIds.value = []
  }
  if (!jobId) return
  if (researchAgentStore.workspaceDismissedJobId === jobId) {
    autoWorkspaceSuppressedJobId.value = jobId
    autoWorkspaceSuppressedRunId.value = researchAgentStore.workspaceDismissedRunId
    workspaceExitLocked.value = true
  } else if (jobId !== previousJobId) {
    autoWorkspaceSuppressedJobId.value = ''
    autoWorkspaceSuppressedRunId.value = ''
    workspaceExitLocked.value = false
  }
}, { immediate: true })
watch(readRouteJobId, (jobId, previousJobId) => {
  if (previousJobId !== undefined && jobId !== previousJobId) {
    autoWorkspaceSuppressedJobId.value = ''
    autoWorkspaceSuppressedRunId.value = ''
    lastObservedAnalysisRunId.value = ''
    workspaceExitLocked.value = false
  }
  switchRouteJob(jobId)
}, { immediate: true })

onBeforeUnmount(() => {
  layoutStore.setWorkspaceFocusMode(false)
  disposed = true
  clearAutoWorkspaceTimer()
  streamVersion += 1
  stopConnection()
})
</script>

<template>
  <section
    class="research-page"
    :class="{ 'research-page--workspace': workspaceMode }"
    aria-label="市场调研报告"
  >
    <header
      v-if="!workspaceMode"
      class="page-header"
    >
      <div>
        <h1>市场调研报告</h1>
        <p>分阶段采集与分析市场数据，在商品关卡确认后继续深挖。</p>
      </div>
      <div class="page-header__actions">
        <ElButton
          v-if="currentJobId"
          type="primary"
          :icon="DocumentAdd"
          data-testid="research-create-new-job"
          @click="startNewResearchTask"
        >
          新建任务
        </ElButton>
        <ElTag
          type="info"
          effect="plain"
        >
          {{ selectedContextLabel }}
        </ElTag>
        <ElButton
          :icon="Share"
          data-testid="toggle-research-workflow"
          @click="toggleWorkflowTopology"
        >
          {{ workflowVisible ? '收起工作流' : '查看工作流' }}
        </ElButton>
      </div>
    </header>

    <section
      v-if="!workspaceMode && workflowVisible"
      class="topology-panel"
      aria-label="市场调研工作流拓扑"
    >
      <header class="topology-panel__header">
        <div>
          <h2>{{ workflowTopology?.title || '市场调研工作流' }}</h2>
          <ElTag
            size="small"
            effect="plain"
          >
            {{ workflowTopology?.type || 'MERMAID' }}
          </ElTag>
        </div>
        <ElTooltip content="收起工作流拓扑">
          <ElButton
            :icon="Close"
            circle
            aria-label="收起工作流拓扑"
            @click="workflowVisible = false"
          />
        </ElTooltip>
      </header>

      <StatePanel
        v-if="workflowLoading"
        status="loading"
        title="正在读取工作流拓扑"
      />
      <StatePanel
        v-else-if="workflowError"
        status="error"
        title="工作流拓扑加载失败"
        :description="formatError(workflowError)"
        action-label="重新加载"
        @action="loadWorkflowTopology"
      />
      <ResearchWorkflowDiagram
        v-else-if="workflowTopology"
        :source="workflowTopology.content"
        :steps="workflowSteps"
      />
    </section>

    <div
      class="research-layout"
      :class="{
        'research-layout--workspace': workspaceMode,
        'research-layout--create': !currentJobId,
      }"
    >
      <section
        v-if="!workspaceMode"
        class="form-panel"
        :class="{ 'form-panel--create': !currentJobId }"
        :aria-label="currentJob ? '本次市场调研任务参数' : '创建市场调研任务'"
      >
        <ResearchTaskInputSummary
          v-if="currentJob"
          :job="currentJob"
        />

        <template v-else>
          <div class="panel-heading">
            <h2>任务参数</h2>
            <span>市场 · 类目 · 月份</span>
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
            <div class="selection-grid">
              <ElFormItem
                label="市场"
                prop="marketplace"
              >
                <ElSelect
                  v-model="form.marketplace"
                  aria-label="市场"
                  placeholder="选择市场"
                >
                  <ElOption
                    v-for="option in marketplaceOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </ElSelect>
              </ElFormItem>
              <ElFormItem
                label="月份"
                prop="month"
              >
                <ElDatePicker
                  v-model="form.month"
                  aria-label="月份"
                  type="month"
                  format="YYYY-MM"
                  value-format="YYYY-MM"
                  placeholder="选择月份"
                  :editable="false"
                />
              </ElFormItem>
            </div>
            <ElFormItem
              label="类目"
              required
            >
              <div class="category-picker">
                <div class="category-picker__tools">
                  <ElInput
                    v-model="categoryKeyword"
                    aria-label="类目搜索关键词"
                    clearable
                    data-testid="research-category-search-input"
                    maxlength="128"
                    placeholder="输入类目名称或节点 ID"
                    @clear="resetCategorySearch"
                    @keyup.enter="searchCategoryOptions"
                  >
                    <template #append>
                      <ElButton
                        aria-label="搜索类目"
                        data-testid="search-research-categories"
                        :disabled="!categoryKeyword.trim()"
                        :icon="Search"
                        :loading="categorySearchLoading"
                        @click="searchCategoryOptions"
                      />
                    </template>
                  </ElInput>
                  <ElInput
                    v-model="categoryAsin"
                    aria-label="ASIN 反查类目"
                    clearable
                    data-testid="research-category-asin-input"
                    maxlength="10"
                    placeholder="输入单个 ASIN 反查类目（如 B08GHW4TBS）"
                    @keyup.enter="handleResolveCategoryByAsin"
                  >
                    <template #append>
                      <ElButton
                        aria-label="从 ASIN 反查类目"
                        data-testid="resolve-category-by-asin"
                        :disabled="!categoryAsin.trim()"
                        :loading="resolvingCategory"
                        @click="handleResolveCategoryByAsin"
                      >
                        反查类目
                      </ElButton>
                    </template>
                  </ElInput>
                </div>
                <ElFormItem
                  class="category-picker__selection"
                  prop="nodeIdPath"
                >
                  <ElCascader
                    :key="categoryTreeVersion"
                    v-model="form.nodeIdPath"
                    aria-label="类目"
                    clearable
                    data-testid="research-category-cascader"
                    filterable
                    :options="categoryCascaderOptions"
                    :props="categoryCascaderProps"
                    :placeholder="categoryCascaderPlaceholder"
                    separator=" / "
                  />
                  <ElAlert
                    v-if="categoryError"
                    type="error"
                    :title="formatError(categoryError)"
                    :closable="false"
                    show-icon
                  />
                </ElFormItem>
              </div>
            </ElFormItem>
            <ElFormItem
              label="核心关键词（可选）"
              prop="keyword"
            >
              <ElInput
                v-model="form.keyword"
                aria-label="核心关键词"
                maxlength="256"
                placeholder="例如：facial cleansing device（可不填）"
                show-word-limit
              />
            </ElFormItem>
            <ElFormItem label="采集节点参数">
              <ElCollapse
                v-model="expandedCollectionNodes"
                class="collection-config"
              >
                <ElCollapseItem
                  title="采集商品池"
                  name="collectProducts"
                >
                  <div class="collection-config__grid">
                    <label>
                      <span>商品目标数</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectProducts.pagination.targetCount"
                        aria-label="商品目标数"
                        :min="1"
                        controls-position="right"
                      />
                    </label>
                    <label>
                      <span>每页商品数</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectProducts.pagination.pageSize"
                        aria-label="每页商品数"
                        :min="1"
                        :max="100"
                        controls-position="right"
                      />
                    </label>
                    <label>
                      <span>补充 ASIN 数</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectProducts.enrichmentAsinLimit"
                        aria-label="补充 ASIN 数"
                        :min="0"
                        :max="20"
                        controls-position="right"
                      />
                    </label>
                    <label>
                      <span>最低月销量</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectProducts.productResearch.minUnits"
                        aria-label="商品最低月销量"
                        :min="0"
                        controls-position="right"
                      />
                    </label>
                  </div>
                </ElCollapseItem>
                <ElCollapseItem
                  title="采集市场销售趋势"
                  name="collectMarketSalesTrend"
                >
                  <div class="collection-config__grid">
                    <label>
                      <span>趋势月数</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectMarketSalesTrend.monthCount"
                        aria-label="销售趋势月数"
                        :min="1"
                        :max="120"
                        controls-position="right"
                      />
                    </label>
                  </div>
                </ElCollapseItem>
                <ElCollapseItem
                  title="采集关键词需求趋势"
                  name="collectKeywordDemandTrend"
                >
                  <div class="collection-config__grid">
                    <label>
                      <span>样本数</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectKeywordDemandTrend.topN"
                        aria-label="关键词需求样本数"
                        :min="1"
                        controls-position="right"
                      />
                    </label>
                  </div>
                </ElCollapseItem>
                <ElCollapseItem
                  title="采集细分市场机会"
                  name="collectSegmentOpportunity"
                >
                  <div class="collection-config__grid">
                    <label>
                      <span>市场目标数</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectSegmentOpportunity.pagination.targetCount"
                        aria-label="细分市场目标数"
                        :min="1"
                        controls-position="right"
                      />
                    </label>
                    <label>
                      <span>每页市场数</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectSegmentOpportunity.pagination.pageSize"
                        aria-label="每页细分市场数"
                        :min="1"
                        :max="200"
                        controls-position="right"
                      />
                    </label>
                    <label>
                      <span>分布 Top N</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectSegmentOpportunity.distribution.topN"
                        aria-label="细分市场分布 Top N"
                        :min="1"
                        controls-position="right"
                      />
                    </label>
                    <label>
                      <span>新品月数</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectSegmentOpportunity.distribution.newProduct"
                        aria-label="细分市场新品月数"
                        :min="0"
                        controls-position="right"
                      />
                    </label>
                  </div>
                </ElCollapseItem>
                <ElCollapseItem
                  title="采集评论"
                  name="collectReviews"
                >
                  <div class="collection-config__grid">
                    <label>
                      <span>每个 ASIN 评论数</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectReviews.pagination.targetCountPerAsin"
                        aria-label="每个 ASIN 评论数"
                        :min="1"
                        :max="20"
                        controls-position="right"
                      />
                    </label>
                    <label>
                      <span>每页评论数</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectReviews.pagination.pageSize"
                        aria-label="每页评论数"
                        :min="1"
                        :max="10"
                        controls-position="right"
                      />
                    </label>
                    <fieldset class="collection-config__choices">
                      <legend>评论星级</legend>
                      <ElCheckboxGroup v-model="form.collectionConfig.collectReviews.starList">
                        <ElCheckbox
                          v-for="star in ['1', '2', '3', '4', '5']"
                          :key="star"
                          :value="star"
                        >
                          {{ star }} 星
                        </ElCheckbox>
                      </ElCheckboxGroup>
                    </fieldset>
                    <fieldset class="collection-config__choices">
                      <legend>评论类型</legend>
                      <ElCheckboxGroup v-model="form.collectionConfig.collectReviews.typeList">
                        <ElCheckbox value="1">
                          图片
                        </ElCheckbox>
                        <ElCheckbox value="2">
                          视频
                        </ElCheckbox>
                        <ElCheckbox value="3">
                          VP
                        </ElCheckbox>
                        <ElCheckbox value="4">
                          Vine
                        </ElCheckbox>
                      </ElCheckboxGroup>
                    </fieldset>
                  </div>
                </ElCollapseItem>
                <ElCollapseItem
                  title="采集关键词情报"
                  name="collectKeywordIntelligence"
                >
                  <div class="collection-config__grid">
                    <label>
                      <span>流量词 ASIN 数</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectKeywordIntelligence.trafficAsinLimit"
                        aria-label="流量词 ASIN 数"
                        :min="0"
                        :max="20"
                        controls-position="right"
                      />
                    </label>
                    <label>
                      <span>关键词选品每页数</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectKeywordIntelligence.keywordResearch.size"
                        aria-label="关键词选品每页数"
                        :min="1"
                        :max="15"
                        controls-position="right"
                      />
                    </label>
                    <label>
                      <span>关键词挖掘每页数</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectKeywordIntelligence.keywordMiner.size"
                        aria-label="关键词挖掘每页数"
                        :min="1"
                        :max="100"
                        controls-position="right"
                      />
                    </label>
                    <label>
                      <span>流量词每页数</span>
                      <ElInputNumber
                        v-model="form.collectionConfig.collectKeywordIntelligence.trafficKeyword.size"
                        aria-label="流量词每页数"
                        :min="1"
                        :max="100"
                        controls-position="right"
                      />
                    </label>
                  </div>
                </ElCollapseItem>
              </ElCollapse>
            </ElFormItem>
            <ElFormItem
              label="Agent 分析目标（可选）"
              prop="analysisGoal"
            >
              <ElInput
                v-model="form.analysisGoal"
                aria-label="Agent 分析目标"
                type="textarea"
                :rows="3"
                maxlength="1000"
                show-word-limit
                resize="vertical"
                placeholder="例如：重点判断进入机会、退货风险和差评原因"
              />
            </ElFormItem>

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
        </template>
      </section>

      <section
        v-if="currentJobId"
        class="workflow-panel"
        :class="{ 'workflow-panel--workspace': workspaceMode }"
        aria-label="市场调研任务进度"
      >
        <StatePanel
          v-if="jobLoading && !currentJob"
          status="loading"
          title="正在读取任务状态"
        />
        <StatePanel
          v-else-if="loadError && !currentJob"
          status="error"
          title="任务状态加载失败"
          :description="formatError(loadError)"
          action-label="重新加载"
          @action="reconnectNow"
        />

        <template v-else-if="currentJob">
          <ResearchReportWorkspace
            v-if="workspaceMode"
            :active-section="workspaceSection"
            :title="currentJob.reportName"
            :job-id="currentJob.jobId"
            :status-label="currentStatus?.label || currentJob.status"
            :status-type="currentStatus?.type || 'info'"
            :status-detail="currentNodeName"
            :stream-status-label="workspaceStreamStatusLabel"
            :stream-status-type="workspaceStreamStatusType"
            :stream-status-sequence="lastSequence"
            :selection-available="selectionReviewVisible"
            @select="selectWorkspaceSection"
            @back="exitWorkspaceMode"
          >
            <template #actions>
              <ElButton
                type="primary"
                :icon="DocumentAdd"
                data-testid="research-create-new-job"
                @click="startNewResearchTask"
              >
                新建任务
              </ElButton>
              <ElButton
                v-if="currentJob.cancellable"
                :icon="Close"
                :loading="actionLoading"
                data-testid="cancel-research-job"
                @click="cancelCurrentJob"
              >
                取消任务
              </ElButton>
              <ElButton
                v-if="currentJob.retryable"
                type="primary"
                :icon="RefreshRight"
                :loading="actionLoading"
                data-testid="retry-research-job"
                @click="retryCurrentJob"
              >
                重新执行
              </ElButton>
              <ElTooltip content="重新连接事件流并同步状态">
                <ElButton
                  :icon="RefreshRight"
                  circle
                  :loading="jobLoading"
                  aria-label="刷新任务状态"
                  @click="reconnectNow"
                />
              </ElTooltip>
            </template>

            <template #context>
              <ElSegmented
                v-if="workspaceSection === 'evidence' && evidenceStageOptions.length > 1"
                v-model="activeEvidenceStage"
                :options="evidenceStageOptions"
                size="small"
                aria-label="证据阶段"
              />
            </template>

            <ResearchConversationPanel
              v-if="workspaceShowsAgentPanel"
              :job-id="currentJob.jobId"
              :job-status="currentJob.status"
              :section="workspaceAgentSection"
              :active-event-id="workspaceActiveEventId"
              workspace
              @resume="resumeStream"
              @follow-up-runs-change="updateFollowUpAnalysisRunIds"
              @open-product-selection="navigateToWorkspace('selection')"
            />

            <section
              v-else-if="workspaceSection === 'evidence'"
              class="workspace-pane workspace-pane--evidence"
              data-testid="research-workspace-evidence"
              aria-label="报告证据数据"
            >
              <header class="workspace-pane__header">
                <div>
                  <h2>证据数据</h2>
                  <p>核对报告引用的市场、商品与消费者证据。</p>
                </div>
              </header>
              <ResearchEvidencePanel
                :key="`${currentJob.jobId}-${activeEvidenceStage}`"
                :job-id="currentJob.jobId"
                :stage-code="activeEvidenceStage"
              />
            </section>

            <section
              v-else-if="workspaceSection === 'selection'"
              class="workspace-pane workspace-pane--selection"
              data-testid="research-workspace-product-selection"
              aria-label="商品选择"
            >
              <header class="workspace-pane__header">
                <div>
                  <h2>商品选择</h2>
                  <p>阶段一筛选结果与阶段二采集范围。</p>
                </div>
                <ElTag
                  type="warning"
                  effect="light"
                >
                  人工关卡
                </ElTag>
              </header>
              <ResearchProductSelectionPanel
                :job-id="currentJob.jobId"
                :job-status="currentJob.status"
                :draft-asins="productSelectionDraftAsins"
                @update:draft-asins="productSelectionDraftAsins = $event"
                @submitted="handleProductSelectionSubmitted"
              />
            </section>

            <section
              v-else
              class="workspace-pane workspace-pane--artifacts"
              data-testid="research-workspace-artifacts"
              aria-label="报告已发布文件"
            >
              <header class="workspace-pane__header">
                <div>
                  <h2>已发布文件</h2>
                  <p>下载这次任务产生的报告、证据和阶段产物。</p>
                </div>
                <ElTag
                  type="info"
                  effect="plain"
                >
                  {{ publishedArtifacts.length }} 个文件
                </ElTag>
              </header>
              <ElAlert
                v-if="downloadError"
                class="job-alert"
                type="error"
                :title="formatError(downloadError)"
                :closable="false"
                show-icon
              />
              <section
                class="artifact-list artifact-list--workspace"
                aria-label="已发布产物"
              >
                <div
                  v-for="artifactType in researchArtifactTypes"
                  :key="artifactType"
                  class="artifact-list__row"
                >
                  <span>{{ researchArtifactLabels[artifactType] }}</span>
                  <template
                    v-for="artifact in artifactsOfType(artifactType)"
                    :key="artifact.artifactId"
                  >
                    <ElButton
                      link
                      type="primary"
                      :icon="Download"
                      :loading="downloadingArtifactId === artifact.artifactId"
                      :data-testid="`download-research-artifact-${artifactType}`"
                      @click="downloadArtifact(artifact)"
                    >
                      {{ artifact.fileName || artifactLabel(artifact) }}
                    </ElButton>
                  </template>
                  <small
                    v-if="artifactsOfType(artifactType).length === 0"
                  >
                    未发布
                  </small>
                </div>
              </section>
            </section>
          </ResearchReportWorkspace>

          <template v-else>
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
                  <span>{{ currentNodeName }}</span>
                </div>
                <h2>{{ currentJob.reportName }}</h2>
                <code>{{ currentJob.jobId }}</code>
              </div>
              <div class="job-actions">
                <ElButton
                  v-if="productSelectionPending"
                  type="warning"
                  :icon="Check"
                  data-testid="open-research-product-selection"
                  @click="navigateToWorkspace('selection')"
                >
                  选择商品
                </ElButton>
                <ElButton
                  v-if="hasAnalysisConversation"
                  :icon="FullScreen"
                  data-testid="enter-research-focus"
                  @click="navigateToWorkspace('report')"
                >
                  打开报告工作台
                </ElButton>
                <ElButton
                  v-if="currentJob.cancellable"
                  :loading="actionLoading"
                  data-testid="cancel-research-job"
                  @click="cancelCurrentJob"
                >
                  取消任务
                </ElButton>
                <ElButton
                  v-if="currentJob.retryable"
                  type="primary"
                  :loading="actionLoading"
                  data-testid="retry-research-job"
                  @click="retryCurrentJob"
                >
                  重新执行
                </ElButton>
                <ElTooltip content="重新连接事件流并同步状态">
                  <ElButton
                    :icon="RefreshRight"
                    circle
                    :loading="jobLoading"
                    aria-label="刷新任务状态"
                    @click="reconnectNow"
                  />
                </ElTooltip>
              </div>
            </div>

            <ElAlert
              v-if="loadError"
              class="job-alert"
              type="error"
              :title="formatError(loadError)"
              :closable="false"
              show-icon
            />
            <ElAlert
              v-if="actionError"
              class="job-alert"
              type="error"
              :title="formatError(actionError)"
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
              <ElDescriptionsItem label="月份">
                {{ currentJob.month }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="类目路径">
                {{ currentJob.nodeIdPath }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="数据源">
                {{ currentJob.dataSourceMode }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="工作流版本">
                {{ currentJob.workflowVersion }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="当前阶段">
                {{ currentStageLabel(currentJob.currentStage) }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="等待输入">
                {{ currentJob.waitingInputType || '--' }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="执行次数">
                {{ currentJob.attemptCount }} / {{ currentJob.maxAttempts }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="核心关键词">
                {{ currentJob.keyword || '未填写' }}
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
              <ElDescriptionsItem label="下次调度">
                {{ formatDateTime(currentJob.nextRunAt) }}
              </ElDescriptionsItem>
              <ElDescriptionsItem label="最近心跳">
                {{ formatDateTime(currentJob.heartbeatAt) }}
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
                :style="{ minWidth: horizontalStepsWidth }"
                :active="activeWorkflowStep"
                finish-status="success"
                :process-status="currentJob.status === 'FAILED' ? 'error' : 'process'"
                align-center
              >
                <ElStep
                  v-for="step in workflowSteps"
                  :key="step.code"
                  :title="step.label"
                />
              </ElSteps>
            </div>

            <ElSteps
              class="research-steps research-steps--vertical"
              :style="{ height: verticalStepsHeight }"
              :active="activeWorkflowStep"
              finish-status="success"
              :process-status="currentJob.status === 'FAILED' ? 'error' : 'process'"
              direction="vertical"
              :space="48"
            >
              <ElStep
                v-for="step in workflowSteps"
                :key="step.code"
                :title="step.label"
              />
            </ElSteps>

            <section
              class="node-history"
              aria-label="节点执行轨迹"
            >
              <div class="node-history__heading">
                <h3>节点执行轨迹</h3>
                <span>{{ nodeExecutions.length }} 条记录</span>
              </div>
              <ElCollapse
                v-if="nodeExecutions.length > 0"
                v-model="expandedExecutionGraphs"
                class="graph-executions"
              >
                <ElCollapseItem
                  v-for="group in nodeGroups"
                  :key="group.stepCode"
                  :name="group.stepCode"
                >
                  <template #title>
                    <strong>{{ group.label }}</strong>
                    <span>{{ group.nodes.length }} 个节点</span>
                  </template>
                  <ElTable
                    v-if="group.nodes.length > 0"
                    :data="group.nodes"
                    size="small"
                    table-layout="fixed"
                  >
                    <ElTableColumn
                      prop="nodeName"
                      label="节点"
                      min-width="150"
                    />
                    <ElTableColumn
                      label="尝试"
                      width="88"
                    >
                      <template #default="scope">
                        {{ scope.row.jobAttempt }}.{{ scope.row.nodeAttempt }}
                      </template>
                    </ElTableColumn>
                    <ElTableColumn
                      prop="status"
                      label="状态"
                      width="108"
                    />
                    <ElTableColumn
                      label="耗时"
                      width="92"
                    >
                      <template #default="scope">
                        {{ formatDuration(scope.row.durationMs) }}
                      </template>
                    </ElTableColumn>
                    <ElTableColumn
                      label="开始时间"
                      min-width="168"
                    >
                      <template #default="scope">
                        {{ formatDateTime(scope.row.startedAt) }}
                      </template>
                    </ElTableColumn>
                  </ElTable>
                  <p
                    v-else
                    class="node-history__empty"
                  >
                    尚未执行该阶段节点。
                  </p>
                </ElCollapseItem>
              </ElCollapse>
              <p
                v-else
                class="node-history__empty"
              >
                任务尚未开始执行节点。
              </p>
            </section>
          </template>

          <ResearchConversationPanel
            v-if="!workspaceMode"
            :job-id="currentJob.jobId"
            :job-status="currentJob.status"
            :active-event-id="workspaceActiveEventId"
            @resume="resumeStream"
            @follow-up-runs-change="updateFollowUpAnalysisRunIds"
          />

          <section
            v-if="!workspaceMode && deepDiveEvidenceVisible"
            class="selection-review"
            data-testid="research-deep-dive-evidence"
            aria-label="阶段二证据"
          >
            <header class="selection-review__header">
              <div>
                <h3>阶段二深度验证</h3>
                <span>查看所选商品的评价、VOC 和 Keywords 三张证据表。</span>
              </div>
              <ElTag
                type="success"
                effect="light"
              >
                阶段二
              </ElTag>
            </header>
            <ResearchEvidencePanel
              :job-id="currentJob.jobId"
              stage-code="DEEP_DIVE"
            />
          </section>

          <template v-if="!workspaceMode">
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
              v-else-if="currentJob.status === 'RETRY_WAIT'"
              class="job-result"
              type="warning"
              title="任务等待自动重试"
              :description="`下次调度：${formatDateTime(currentJob.nextRunAt)}`"
              :closable="false"
              show-icon
            />
            <ElAlert
              v-else-if="currentJob.status === 'WAITING_INPUT'"
              class="job-result"
              type="warning"
              title="阶段一已完成，等待商品选择"
              description="证据和 AI 初步结论已持久化。选择商品进入阶段二，或放弃该市场。"
              :closable="false"
              show-icon
            />
            <ElAlert
              v-else-if="currentJob.status === 'ABANDONED'"
              class="job-result"
              type="info"
              title="已放弃该市场"
              description="这是正常业务终态，阶段一数据与分析结论仍然保留。"
              :closable="false"
              show-icon
            />
            <ElAlert
              v-else-if="currentJob.status === 'CANCELLED'"
              class="job-result"
              type="info"
              title="任务已取消"
              description="已停止后续节点调度，已完成的数据集和节点审计记录会保留。"
              :closable="false"
              show-icon
            />
            <ElAlert
              v-else-if="currentJob.cancelRequestedAt"
              class="job-result"
              type="warning"
              title="正在等待安全节点边界取消"
              :description="`取消请求时间：${formatDateTime(currentJob.cancelRequestedAt)}`"
              :closable="false"
              show-icon
            />
            <ElAlert
              v-else-if="currentJob.status === 'SUCCEEDED'"
              class="job-result"
              type="success"
              title="报告已生成"
              description="采集、证据和 AI 报告产物已发布。"
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

            <section
              class="artifact-list"
              aria-label="已发布产物"
            >
              <h3>已发布产物</h3>
              <div
                v-for="artifactType in researchArtifactTypes"
                :key="artifactType"
                class="artifact-list__row"
              >
                <span>{{ researchArtifactLabels[artifactType] }}</span>
                <template
                  v-for="artifact in artifactsOfType(artifactType)"
                  :key="artifact.artifactId"
                >
                  <ElButton
                    link
                    type="primary"
                    :icon="Download"
                    :loading="downloadingArtifactId === artifact.artifactId"
                    :data-testid="`download-research-artifact-${artifactType}`"
                    @click="downloadArtifact(artifact)"
                  >
                    {{ artifact.fileName || artifactLabel(artifact) }}
                  </ElButton>
                </template>
                <small
                  v-if="artifactsOfType(artifactType).length === 0"
                >
                  未发布
                </small>
              </div>
            </section>
          </template>
        </template>
      </section>
    </div>
  </section>
</template>

<style scoped>
.research-page{display:flex;min-height:calc(100dvh - 104px);flex-direction:column;overflow:hidden;background:var(--color-surface);border:1px solid var(--color-border);border-radius:var(--radius-lg)}.page-header{display:flex;align-items:center;justify-content:space-between;gap:16px;padding:18px 20px;border-bottom:1px solid var(--color-border)}.page-header__actions{display:flex;align-items:center;gap:10px}.topology-panel{display:grid;gap:16px;padding:18px 20px;border-bottom:1px solid var(--color-border);background:var(--color-surface)}.topology-panel__header{display:flex;align-items:center;justify-content:space-between;gap:16px}.topology-panel__header>div{display:flex;align-items:center;gap:10px}h1,h2,h3{margin:0;color:var(--color-text);letter-spacing:0}h1{font-size:20px}h2{font-size:16px}h3{font-size:14px}.page-header p{margin:5px 0 0;color:var(--color-text-secondary);font-size:12px}.research-layout{display:grid;grid-template-columns:minmax(320px,400px) minmax(0,1fr);min-height:0;flex:1}.form-panel{padding:20px;border-right:1px solid var(--color-border);background:var(--color-surface-muted)}.panel-heading{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:20px}.panel-heading span{color:var(--color-text-secondary);font-size:12px}.form-help{display:block;margin-top:6px;color:var(--color-text-secondary);font-size:12px;line-height:1.5}.selection-grid{display:grid;grid-template-columns:1fr 1fr;gap:10px}.selection-grid :deep(.el-select),.selection-grid :deep(.el-date-editor){width:100%}.category-picker{display:grid;width:100%;gap:10px}.category-picker :deep(.el-cascader){width:100%}.form-alert,.job-alert{margin:0 0 14px}.create-button{width:100%}.workflow-panel{min-width:0;padding:20px}.job-heading{display:flex;align-items:flex-start;justify-content:space-between;gap:16px;padding-bottom:16px;border-bottom:1px solid var(--color-border)}.job-heading__status,.job-actions{display:flex;align-items:center;gap:10px}.job-heading__status{margin-bottom:9px;color:var(--color-text-secondary);font-size:12px}.job-actions{flex-wrap:wrap;justify-content:flex-end}.job-heading code{display:block;margin-top:7px;color:var(--color-text-muted);font:12px/1.5 var(--font-mono);overflow-wrap:anywhere}.job-summary{margin-top:16px}.progress-block{display:grid;gap:10px;margin-top:22px}.progress-block>div{display:flex;align-items:center;justify-content:space-between;gap:12px}.progress-block span{color:var(--color-text-secondary);font:13px var(--font-mono)}.steps-scroll{margin-top:24px;padding:4px 0 10px;overflow-x:auto;overflow-y:hidden}.research-steps{width:100%;min-width:0}.research-steps--vertical{display:none;margin-top:24px}.node-history{display:grid;gap:12px;margin-top:20px}.node-history__heading{display:flex;align-items:center;justify-content:space-between;gap:12px}.node-history__heading span,.node-history__empty{color:var(--color-text-secondary);font-size:12px}.node-history__empty{margin:0;padding:16px;border:1px dashed var(--color-border);text-align:center}.job-result{margin-top:18px}@media(max-width:1180px){.research-layout{grid-template-columns:1fr}.form-panel{border-right:0;border-bottom:1px solid var(--color-border)}}@media(max-width:768px){.steps-scroll{display:none}.research-steps--vertical{display:flex}}@media(max-width:640px){.research-page{min-height:calc(100dvh - 88px)}.page-header,.job-heading{align-items:flex-start;flex-direction:column}.page-header{padding:16px}.page-header__actions,.job-actions{width:100%;justify-content:space-between}.topology-panel,.form-panel,.workflow-panel{padding:16px}.selection-grid{grid-template-columns:1fr}.job-summary :deep(.el-descriptions__body) .el-descriptions__table{display:block}.job-summary :deep(.el-descriptions__cell){display:block;width:100%}}
</style>

<style scoped>
.category-picker__selection {
  margin-bottom: 0;
}

.category-picker__selection :deep(.el-form-item__content) {
  display: grid;
  width: 100%;
  gap: 10px;
}

.collection-config {
  width: 100%;
  border-top: 1px solid var(--color-border);
}

.collection-config__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.collection-config__grid > label {
  display: grid;
  min-width: 0;
  gap: 6px;
}

.collection-config__grid > label > span,
.collection-config__choices legend {
  color: var(--color-text-secondary);
  font-size: 12px;
}

.collection-config__grid :deep(.el-input-number) {
  width: 100%;
}

.collection-config__choices {
  display: grid;
  min-width: 0;
  margin: 0;
  padding: 0;
  grid-column: 1 / -1;
  gap: 6px;
  border: 0;
}

.collection-config__choices :deep(.el-checkbox) {
  margin-right: 12px;
}

.graph-executions :deep(.el-collapse-item__title) {
  gap: 10px;
}

.graph-executions :deep(.el-collapse-item__title span) {
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 400;
}

.artifact-list {
  display: grid;
  margin-top: 20px;
  padding-top: 16px;
  gap: 8px;
  border-top: 1px solid var(--color-border);
}

.selection-review {
  display: grid;
  margin-top: 20px;
  padding: 18px 0;
  gap: 24px;
  border-top: 1px solid var(--color-border);
  border-bottom: 1px solid var(--color-border);
}

.selection-review__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.selection-review__header > div {
  display: grid;
  gap: 4px;
}

.selection-review__header h3 {
  margin: 0;
  color: var(--color-text);
  font-size: 15px;
  letter-spacing: 0;
}

.selection-review__header span {
  color: var(--color-text-secondary);
  font-size: 12px;
}

.artifact-list__row {
  display: grid;
  min-height: 32px;
  grid-template-columns: 150px minmax(0, 1fr);
  align-items: center;
  gap: 8px 16px;
}

.artifact-list__row > span,
.artifact-list__row > small {
  color: var(--color-text-secondary);
  font-size: 12px;
}

.artifact-list__row :deep(.el-button) {
  width: fit-content;
  max-width: 100%;
  margin: 0;
  overflow: hidden;
}

.artifact-list__row :deep(.el-button > span) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.research-page--workspace {
  height: 100dvh;
  min-height: 100dvh;
  min-width: 0;
  border: 0;
  border-radius: 0;
}

.research-layout--workspace {
  display: flex;
  min-width: 0;
  min-height: 0;
}

.workflow-panel--workspace {
  display: flex;
  min-width: 0;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  padding: 0;
}

.workspace-pane {
  display: flex;
  position: relative;
  min-width: 0;
  min-height: 0;
  height: 100%;
  flex: 1;
  flex-direction: column;
  gap: 20px;
  padding: 20px 24px 28px;
  overflow-x: hidden;
  overflow-y: auto;
}

.workspace-pane__header {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--color-border);
}

.workspace-pane__header > div {
  min-width: 0;
}

.workspace-pane__header h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 16px;
}

.workspace-pane__header p {
  margin: 5px 0 0;
  color: var(--color-text-secondary);
  font-size: 12px;
}

@media (hover: hover) and (pointer: fine) {
  .workspace-pane {
    padding-top: 10px;
  }

  .workspace-pane__header {
    position: absolute;
    inset: 0 0 auto;
    z-index: 2;
    padding: 0 0 14px;
    background: var(--color-surface-muted);
    transform: translateY(calc(-100% + 8px));
    transition: padding var(--motion-fast) ease, transform var(--motion-fast) ease;
  }

  .workspace-pane__header:hover,
  .workspace-pane__header:focus-within {
    padding-top: 14px;
    transform: translateY(0);
  }
}

.workspace-pane--evidence :deep(.evidence-panel),
.workspace-pane--evidence :deep(.el-table) {
  min-width: 0;
  max-width: 100%;
}

.workspace-pane--artifacts {
  width: 100%;
  max-width: 1040px;
  margin: 0 auto;
}

.workspace-pane--selection {
  width: 100%;
  max-width: 1280px;
  margin: 0 auto;
}

.artifact-list--workspace {
  margin-top: 0;
  padding-top: 0;
  border-top: 0;
}

.artifact-list--workspace .artifact-list__row {
  min-width: 0;
  min-height: 48px;
  padding: 8px 0;
  border-bottom: 1px solid var(--color-border);
}

.category-picker__tools {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.research-layout--create {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 32px 20px 48px;
  overflow-y: auto;
  background: var(--color-surface-muted, #f8fafc);
}

.form-panel--create {
  width: 100%;
  max-width: 860px;
  margin: 0 auto;
  padding: 32px 36px;
  background: var(--color-surface, #ffffff);
  border: 1px solid var(--color-border, #e2e8f0);
  border-radius: var(--radius-lg, 12px);
  box-shadow: 0 4px 16px -2px rgba(0, 0, 0, 0.05), 0 2px 6px -1px rgba(0, 0, 0, 0.03);
}

.form-panel--create .panel-heading {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border);
}

.form-panel--create .panel-heading h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
}

.form-panel--create .panel-heading span {
  font-size: 13px;
  color: var(--color-text-secondary, #64748b);
}

.form-panel--create .create-button {
  margin-top: 16px;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 8px;
}

@media (max-width: 640px) {
  .category-picker__tools {
    grid-template-columns: 1fr;
  }

  .research-layout--create {
    padding: 16px 12px 24px;
  }

  .form-panel--create {
    padding: 20px 16px;
    border-radius: var(--radius-md, 8px);
  }

  .collection-config__grid,
  .artifact-list__row {
    grid-template-columns: 1fr;
  }

  .selection-review__header {
    align-items: flex-start;
    flex-direction: column;
  }

  .research-page--workspace {
    height: 100dvh;
    min-height: 100dvh;
  }

  .workspace-pane {
    padding: 14px 12px 20px;
  }

  .workspace-pane__header {
    position: static;
    align-items: flex-start;
    flex-direction: column;
    padding-bottom: 14px;
    transform: none;
  }

  .workspace-pane__header :deep(.el-segmented) {
    max-width: 100%;
    overflow-x: auto;
  }
}
</style>
