<script setup lang="ts">
import {
  ChatDotRound,
  Promotion,
  RefreshRight,
  VideoPause,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { computed, nextTick, onMounted, ref, watch } from 'vue'

import SafeMarkdown from '@/features/ai/components/SafeMarkdown.vue'
import { ApiError } from '@/shared/api/ApiError'
import { useAutoFollowScroll } from '@/shared/composables/useAutoFollowScroll'

import {
  cancelResearchAnalysis,
  continueResearchAnalysis,
  downloadResearchArtifact,
  listResearchAnalysisRuns,
  retryResearchAnalysis,
  sendResearchMessage,
} from '../api/researchStreamApi'
import {
  isResearchOfficialEvent,
  isResearchProcessEvent,
  isResearchUserEvent,
  isResearchWorkspaceReportEvent,
} from '../model/researchEventPresentation'
import type {
  ResearchAnalysisRun,
  ResearchAnalysisRunStatus,
  ResearchJobStatus,
} from '../model/research'
import type { ResearchReportDownload, ResearchStreamRecord } from '../model/researchStream'
import { useResearchAgentStore } from '../stores/useResearchAgentStore'
import ResearchAnalysisProcess from './ResearchAnalysisProcess.vue'
import ResearchOfficialEventList from './ResearchOfficialEventList.vue'

interface AnalysisEventGroup {
  key: string
  processEvents: ResearchStreamRecord[]
  officialEvents: ResearchStreamRecord[]
}

interface FollowUpTurn {
  run: ResearchAnalysisRun
  analysisRunId: string
  question: string
  answer: string
  status: ResearchAnalysisRunStatus
  errorMessage: string
  streaming: boolean
  retryable: boolean
}

type ResearchConversationSection = 'all' | 'report' | 'process'

const props = withDefaults(defineProps<{
  jobId: string
  jobStatus: ResearchJobStatus
  focusMode?: boolean
  section?: ResearchConversationSection
  workspace?: boolean
  activeEventId?: string
}>(), {
  focusMode: false,
  section: 'all',
  workspace: false,
  activeEventId: '',
})

const emit = defineEmits<{
  resume: []
  followUpRunsChange: [analysisRunIds: string[]]
}>()

const store = useResearchAgentStore()
const {
  activeAnalysisRunId,
  analysisRetryable,
  analysisRunning,
  analysisState,
  connecting,
  connectionError,
  events,
  hasActivity,
  historyLoading,
  lastSequence,
  reconnecting,
  streaming,
  workflowTerminal,
} = storeToRefs(store)

const question = ref('')
const commandLoading = ref(false)
const downloadingArtifactId = ref('')
const analysisRuns = ref<ResearchAnalysisRun[]>([])
const optimisticAnalysisRuns = ref<ResearchAnalysisRun[]>([])
const analysisRunsLoading = ref(false)
const analysisRunsError = ref('')
const timelineElement = ref<HTMLElement>()
let analysisRunsRequestVersion = 0
let refreshedTerminalSignal = ''
const {
  handleScroll: handleTimelineScroll,
  isAutoFollowing: isTimelineAutoFollowing,
  resetAutoFollow: resetTimelineAutoFollow,
  setScrollContainer: setTimelineContainer,
  scrollToBottom: scrollTimelineToEnd,
} = useAutoFollowScroll()

const globalProcessEvents = computed(() => events.value.filter((event) => (
  event.scope !== 'analysis'
  && !event.analysisRunId
  && isResearchProcessEvent(event)
)))
const globalOfficialEvents = computed(() => events.value.filter((event) => (
  !event.analysisRunId
  && isResearchOfficialEvent(event)
)))
const mergedAnalysisRuns = computed(() => {
  const runs = new Map<string, ResearchAnalysisRun>()
  optimisticAnalysisRuns.value.forEach((run) => runs.set(run.analysisRunId, run))
  analysisRuns.value.forEach((run) => {
    runs.set(run.analysisRunId, { ...runs.get(run.analysisRunId), ...run })
  })
  return [...runs.values()]
})
const visibleAnalysisRuns = computed(() => {
  const runIds = new Set(mergedAnalysisRuns.value.map((run) => run.analysisRunId))
  const supersededRunIds = new Set(mergedAnalysisRuns.value
    .filter((run) => run.runType === 'FOLLOW_UP' && run.parentRunId && runIds.has(run.parentRunId))
    .map((run) => run.parentRunId!))
  return mergedAnalysisRuns.value.filter((run) => !supersededRunIds.has(run.analysisRunId))
})
const followUpRunIdSet = computed(() => {
  const runIds = new Set(mergedAnalysisRuns.value
    .filter((run) => run.runType === 'FOLLOW_UP')
    .map((run) => run.analysisRunId))
  events.value.forEach((event) => {
    if (event.analysisRunId && eventRunType(event) === 'FOLLOW_UP') {
      runIds.add(event.analysisRunId)
    }
  })
  return runIds
})
const analysisGroups = computed<AnalysisEventGroup[]>(() => {
  const groups = new Map<string, AnalysisEventGroup>()
  events.value.forEach((event) => {
    if (event.scope !== 'analysis' && !event.analysisRunId) return
    const key = event.analysisRunId || event.conversationId || 'initial-analysis'
    const group = groups.get(key) ?? {
      key,
      processEvents: [],
      officialEvents: [],
    }
    if (isResearchUserEvent(event)) return
    if (isResearchOfficialEvent(event)) group.officialEvents.push(event)
    else if (isResearchProcessEvent(event)) group.processEvents.push(event)
    groups.set(key, group)
  })
  return [...groups.values()]
})
function isWorkspaceProcessOfficialEvent(event: ResearchStreamRecord) {
  return !isResearchWorkspaceReportEvent(event)
}
const initialAnalysisGroups = computed(() => analysisGroups.value.filter((group) => (
  !followUpRunIdSet.value.has(group.key)
)))
const hasReportActivity = computed(() => (
  globalOfficialEvents.value.some(isResearchWorkspaceReportEvent)
  || initialAnalysisGroups.value.some((group) => (
    group.officialEvents.some(isResearchWorkspaceReportEvent)
  ))
))
const hasProcessActivity = computed(() => (
  globalProcessEvents.value.length > 0
  || globalOfficialEvents.value.some(isWorkspaceProcessOfficialEvent)
  || analysisGroups.value.some((group) => (
    group.processEvents.length > 0
    || group.officialEvents.some(isWorkspaceProcessOfficialEvent)
  ))
))
const followUpTurns = computed<FollowUpTurn[]>(() => visibleAnalysisRuns.value
  .filter((run) => run.runType === 'FOLLOW_UP')
  .sort(compareAnalysisRuns)
  .map(toFollowUpTurn))
const followUpAnalysisRunning = computed(() => followUpTurns.value.some((turn) => (
  ['WAITING_RESEARCH', 'QUEUED', 'RUNNING', 'RETRY_WAIT'].includes(turn.status)
)))
const analysisBusy = computed(() => analysisRunning.value || followUpAnalysisRunning.value)
const canSendMessage = computed(() => (
  props.jobStatus === 'SUCCEEDED'
  && !analysisBusy.value
  && !commandLoading.value
  && Boolean(question.value.trim())
))
const connectionLabel = computed(() => {
  if (historyLoading.value) return '正在恢复历史事件'
  if (reconnecting.value) return `连接中断，正在从 #${lastSequence.value} 恢复`
  if (connecting.value) return '正在建立事件流'
  if (workflowTerminal.value || analysisState.value === 'SUCCEEDED') {
    return '本轮工作流已结束，可继续追问'
  }
  if (streaming.value) return '事件流已连接'
  if (props.jobStatus !== 'SUCCEEDED') return '等待数据 Graph 产出证据'
  return '等待分析事件'
})
const statusType = computed<'info' | 'warning' | 'success' | 'danger'>(() => {
  if (connectionError.value) return 'danger'
  if (analysisState.value === 'FAILED' || workflowTerminal.value?.eventType === 'workflow_failed') {
    return 'danger'
  }
  if (workflowTerminal.value?.eventType === 'workflow_cancelled') return 'info'
  if (workflowTerminal.value || analysisState.value === 'SUCCEEDED') return 'success'
  if (streaming.value || connecting.value || reconnecting.value) return 'warning'
  return 'info'
})
const composerPlaceholder = computed(() => {
  if (props.jobStatus === 'WAITING_INPUT') return '请先在商品选择关卡确认阶段二商品'
  if (props.jobStatus === 'ABANDONED') return '该市场已放弃，阶段一分析记录保持只读'
  if (props.jobStatus !== 'SUCCEEDED') return '证据采集完成后，可以基于本报告继续提问'
  if (analysisBusy.value) return '当前分析仍在执行，请等待本轮完成'
  return '继续追问，例如：只看退货风险和差评原因'
})
const timelineContentSignal = computed(() => [
  streaming.value,
  analysisBusy.value,
  workflowTerminal.value?.id || '',
  ...followUpTurns.value.map((turn) => [
    turn.analysisRunId,
    turn.status,
    turn.answer.length,
    turn.errorMessage,
  ].join(':')),
  ...events.value.map((event) => [
    event.id,
    event.sequenceNo ?? '',
    event.message.length,
    typeof event.data === 'string' ? event.data.length : '',
  ].join(':')),
].join('|'))
const followUpRunIdsSignal = computed(() => [...followUpRunIdSet.value].sort().join('|'))
const terminalFollowUpSignal = computed(() => events.value
  .filter((event) => (
    Boolean(event.analysisRunId)
    && followUpRunIdSet.value.has(event.analysisRunId!)
    && ['done', 'error', 'analysis_cancelled'].includes(event.eventType)
  ))
  .map((event) => `${event.id}:${event.sequenceNo ?? ''}:${event.eventType}`)
  .join('|'))

function compareAnalysisRuns(left: ResearchAnalysisRun, right: ResearchAnalysisRun) {
  const createdAtDifference = (left.createdAt ?? 0) - (right.createdAt ?? 0)
  return createdAtDifference || left.analysisRunId.localeCompare(right.analysisRunId)
}

function toFollowUpTurn(run: ResearchAnalysisRun): FollowUpTurn {
  const runEvents = events.value.filter((event) => event.analysisRunId === run.analysisRunId)
  let status = run.status
  if (!isTerminalAnalysisStatus(status)) {
    runEvents.forEach((event) => {
      if (event.eventType === 'done') status = analysisStatusFromEvent(event, status)
      else if (event.eventType === 'error') status = 'FAILED'
      else if (event.eventType === 'analysis_cancelled') status = 'CANCELLED'
      else if (!isTerminalAnalysisStatus(status)) {
        if (event.eventType === 'analysis_queued') status = 'QUEUED'
        else if (event.scope === 'analysis' && !isResearchUserEvent(event)) status = 'RUNNING'
      }
    })
  }
  const summaries = runEvents.filter((event) => event.eventType === 'summary')
  const persistedSummary = summaries.filter((event) => !event.streaming).at(-1)
  const streamingSummary = summaries.filter((event) => event.streaming).at(-1)
  const finalSummary = run.finalSummary?.trim() || ''
  const answer = finalSummary
    || persistedSummary?.message.trim()
    || streamingSummary?.message.trim()
    || ''
  const eventError = runEvents.filter((event) => event.eventType === 'error').at(-1)
  return {
    run,
    analysisRunId: run.analysisRunId,
    question: run.analysisGoal?.trim() || '追问内容暂不可用',
    answer,
    status,
    errorMessage: run.errorMessage?.trim() || eventError?.message.trim() || '',
    streaming: !finalSummary && !persistedSummary && Boolean(streamingSummary),
    retryable: Boolean(run.retryable) || status === 'FAILED',
  }
}

function isTerminalAnalysisStatus(status: ResearchAnalysisRunStatus) {
  return ['SUCCEEDED', 'FAILED', 'CANCELLED'].includes(status)
}

function analysisStatusFromEvent(
  event: ResearchStreamRecord,
  fallback: ResearchAnalysisRunStatus,
) {
  if (!isRecord(event.data)) return fallback
  const value = event.data.analysisStatus ?? event.data.status
  if (typeof value !== 'string') return fallback
  const normalized = value.toUpperCase()
  return [
    'WAITING_RESEARCH',
    'QUEUED',
    'RUNNING',
    'RETRY_WAIT',
    'SUCCEEDED',
    'FAILED',
    'CANCELLED',
  ].includes(normalized)
    ? normalized as ResearchAnalysisRunStatus
    : fallback
}

function eventRunType(event: ResearchStreamRecord) {
  if (!isRecord(event.data) || typeof event.data.runType !== 'string') return ''
  return event.data.runType.toUpperCase()
}

function followUpStatusLabel(status: ResearchAnalysisRunStatus) {
  return {
    WAITING_RESEARCH: '等待证据',
    QUEUED: '等待回答',
    RUNNING: '正在回答',
    RETRY_WAIT: '等待重试',
    SUCCEEDED: '已回答',
    FAILED: '回答失败',
    CANCELLED: '已取消',
  }[status]
}

function followUpStatusType(
  status: ResearchAnalysisRunStatus,
): 'success' | 'danger' | 'info' | 'warning' {
  if (status === 'SUCCEEDED') return 'success'
  if (status === 'FAILED') return 'danger'
  if (status === 'CANCELLED') return 'info'
  return 'warning'
}

function formatFollowUpTime(timestamp: number | null) {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleString('zh-CN', {
    hour12: false,
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}

async function loadResearchAnalysisHistory(jobId = props.jobId, silent = false) {
  const requestVersion = ++analysisRunsRequestVersion
  if (!silent) analysisRunsLoading.value = true
  analysisRunsError.value = ''
  try {
    const runs = await listResearchAnalysisRuns(jobId)
    if (requestVersion !== analysisRunsRequestVersion || jobId !== props.jobId) return
    analysisRuns.value = runs
    const persistedRunIds = new Set(runs.map((run) => run.analysisRunId))
    optimisticAnalysisRuns.value = optimisticAnalysisRuns.value.filter((run) => (
      !persistedRunIds.has(run.analysisRunId)
    ))
  } catch (error) {
    if (requestVersion !== analysisRunsRequestVersion || jobId !== props.jobId) return
    analysisRunsError.value = errorMessage(error, '追问历史加载失败')
  } finally {
    if (requestVersion === analysisRunsRequestVersion) analysisRunsLoading.value = false
  }
}

function upsertOptimisticRun(run: ResearchAnalysisRun) {
  const index = optimisticAnalysisRuns.value.findIndex((candidate) => (
    candidate.analysisRunId === run.analysisRunId
  ))
  if (index < 0) optimisticAnalysisRuns.value.push(run)
  else optimisticAnalysisRuns.value[index] = { ...optimisticAnalysisRuns.value[index], ...run }
}

function containsActiveEvent(groupEvents: ResearchStreamRecord[]) {
  return groupEvents.some((event) => event.id === props.activeEventId)
}

function setTimelineElement(element: unknown) {
  timelineElement.value = element instanceof HTMLElement ? element : undefined
  setTimelineContainer(element)
}

function findTimelineEvent(eventId: string) {
  return [...(timelineElement.value?.querySelectorAll<HTMLElement>('[data-event-id]') ?? [])]
    .find((element) => element.dataset.eventId === eventId)
}

async function scrollTimelineToActivity(force = false) {
  if (props.section !== 'process' || !props.activeEventId) {
    await scrollTimelineToEnd(force)
    return
  }
  if (!force && !isTimelineAutoFollowing.value) return

  await nextTick()
  await nextTick()
  const eventElement = findTimelineEvent(props.activeEventId)
  if (!eventElement) {
    await scrollTimelineToEnd(force)
    return
  }
  eventElement.scrollIntoView?.({ block: 'nearest' })
  resetTimelineAutoFollow()
}

watch(
  [timelineContentSignal, () => props.section, () => props.activeEventId],
  ([, section], [, previousSection]) => {
    void scrollTimelineToActivity(section !== previousSection)
  },
  { flush: 'post' },
)

watch(() => props.jobId, (jobId) => {
  analysisRunsRequestVersion += 1
  analysisRuns.value = []
  optimisticAnalysisRuns.value = []
  analysisRunsError.value = ''
  refreshedTerminalSignal = ''
  void loadResearchAnalysisHistory(jobId)
  void scrollTimelineToActivity(true)
}, { flush: 'post', immediate: true })

watch(followUpRunIdsSignal, () => {
  emit('followUpRunsChange', [...followUpRunIdSet.value])
}, { immediate: true })

watch(terminalFollowUpSignal, (signal) => {
  if (!signal || signal === refreshedTerminalSignal) return
  refreshedTerminalSignal = signal
  void loadResearchAnalysisHistory(props.jobId, true)
})

onMounted(() => {
  void scrollTimelineToActivity(true)
})

async function submitQuestion() {
  const content = question.value.trim()
  if (!canSendMessage.value || !content) return
  commandLoading.value = true
  try {
    const result = await sendResearchMessage(props.jobId, content)
    upsertOptimisticRun({
      ...result,
      analysisGoal: result.analysisGoal?.trim() || content,
    })
    store.prepareForNewAnalysis(result.analysisRunId)
    question.value = ''
    await scrollTimelineToEnd(true)
    emit('resume')
  } catch (error) {
    ElMessage.error(errorMessage(error, '追问提交失败'))
  } finally {
    commandLoading.value = false
  }
}

async function continueAnalysis() {
  if (!activeAnalysisRunId.value || commandLoading.value) return
  commandLoading.value = true
  try {
    const result = await continueResearchAnalysis(activeAnalysisRunId.value)
    if (result.runType === 'FOLLOW_UP') upsertOptimisticRun(result)
    store.prepareForNewAnalysis(result.analysisRunId)
    emit('resume')
    ElMessage.success('已继续分析，继续使用当前任务证据')
  } catch (error) {
    ElMessage.error(errorMessage(error, '继续分析失败'))
  } finally {
    commandLoading.value = false
  }
}

async function retryFollowUp(analysisRunId: string) {
  if (commandLoading.value) return
  commandLoading.value = true
  try {
    const result = await retryResearchAnalysis(analysisRunId)
    if (result.runType === 'FOLLOW_UP') upsertOptimisticRun(result)
    store.prepareForNewAnalysis(result.analysisRunId)
    await scrollTimelineToEnd(true)
    emit('resume')
  } catch (error) {
    ElMessage.error(errorMessage(error, '追问重试失败'))
  } finally {
    commandLoading.value = false
  }
}

async function cancelAnalysis() {
  if (!activeAnalysisRunId.value || commandLoading.value) return
  commandLoading.value = true
  try {
    await cancelResearchAnalysis(activeAnalysisRunId.value)
    ElMessage.success('分析取消请求已提交')
  } catch (error) {
    ElMessage.error(errorMessage(error, '分析取消失败'))
  } finally {
    commandLoading.value = false
  }
}

async function downloadArtifact(report: ResearchReportDownload) {
  if (downloadingArtifactId.value) return
  downloadingArtifactId.value = report.artifactId
  try {
    const content = await downloadResearchArtifact(props.jobId, report)
    const objectUrl = URL.createObjectURL(content)
    const link = document.createElement('a')
    link.href = objectUrl
    link.download = report.fileName || `market-research-analysis-${report.artifactId}.md`
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.setTimeout(() => URL.revokeObjectURL(objectUrl), 1_000)
  } catch (error) {
    ElMessage.error(errorMessage(error, '分析报告下载失败'))
  } finally {
    downloadingArtifactId.value = ''
  }
}

function requestReconnect() {
  emit('resume')
}

function errorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError || error instanceof Error) return error.message || fallback
  return fallback
}

</script>

<template>
  <section
    class="research-agent"
    :class="{
      'research-agent--focus': props.focusMode,
      'research-agent--workspace': props.workspace,
    }"
    aria-label="AI 市场调研对话"
    data-testid="research-conversation-panel"
  >
    <header
      v-if="!props.workspace"
      class="research-agent__header"
    >
      <div>
        <span class="research-agent__eyebrow">
          <ElIcon><ChatDotRound /></ElIcon>
          Curation Agent
        </span>
        <h3>AI 市场调研对话</h3>
        <p>{{ connectionLabel }}</p>
      </div>
      <div class="research-agent__actions">
        <ElTag
          :type="statusType"
          size="small"
          effect="light"
        >
          {{ analysisState }} · #{{ lastSequence }}
        </ElTag>
        <ElButton
          v-if="connectionError || reconnecting"
          :icon="RefreshRight"
          size="small"
          :loading="connecting"
          data-testid="reconnect-research-stream"
          @click="requestReconnect"
        >
          立即恢复
        </ElButton>
        <ElButton
          v-if="analysisRunning && activeAnalysisRunId"
          :icon="VideoPause"
          size="small"
          :loading="commandLoading"
          data-testid="cancel-research-analysis"
          @click="cancelAnalysis"
        >
          取消分析
        </ElButton>
        <ElButton
          v-else-if="analysisState === 'FAILED' && analysisRetryable && activeAnalysisRunId"
          :icon="RefreshRight"
          type="primary"
          plain
          size="small"
          :loading="commandLoading"
           data-testid="continue-research-analysis"
           @click="continueAnalysis"
         >
          继续分析
        </ElButton>
      </div>
    </header>

    <header
      v-else
      class="research-agent__statusbar"
      data-testid="research-workspace-stream-status"
    >
      <div>
        <span
          class="research-agent__status-dot"
          :class="`research-agent__status-dot--${statusType}`"
          aria-hidden="true"
        />
        <p>{{ connectionLabel }}</p>
        <ElTag
          :type="statusType"
          size="small"
          effect="plain"
        >
          {{ analysisState }} · #{{ lastSequence }}
        </ElTag>
      </div>
      <div class="research-agent__actions">
        <ElButton
          v-if="connectionError || reconnecting"
          :icon="RefreshRight"
          size="small"
          :loading="connecting"
          data-testid="reconnect-research-stream"
          @click="requestReconnect"
        >
          立即恢复
        </ElButton>
        <ElButton
          v-if="analysisRunning && activeAnalysisRunId"
          :icon="VideoPause"
          size="small"
          :loading="commandLoading"
          data-testid="cancel-research-analysis"
          @click="cancelAnalysis"
        >
          取消分析
        </ElButton>
        <ElButton
          v-else-if="analysisState === 'FAILED' && analysisRetryable && activeAnalysisRunId"
          :icon="RefreshRight"
          type="primary"
          plain
          size="small"
          :loading="commandLoading"
           data-testid="continue-research-analysis"
           @click="continueAnalysis"
         >
          继续分析
        </ElButton>
      </div>
    </header>

    <ElAlert
      v-if="connectionError"
      class="research-agent__alert"
      type="warning"
      :title="connectionError"
      :closable="false"
      show-icon
    />

    <div
      :ref="setTimelineElement"
      class="research-agent__timeline"
      :class="`research-agent__timeline--${props.section}`"
      @scroll.passive="handleTimelineScroll"
    >
      <ElEmpty
        v-if="props.section === 'all' && !hasActivity && followUpTurns.length === 0"
        :image-size="56"
        description="任务事件会在这里实时出现"
      />

      <template v-if="props.section === 'all'">
        <ResearchAnalysisProcess
          :events="globalProcessEvents"
          :expanded="globalOfficialEvents.length === 0 || containsActiveEvent(globalProcessEvents)"
          :active-event-id="activeEventId"
        />

        <section
          v-for="group in analysisGroups"
          :key="group.key"
          class="analysis-turn"
        >
          <ResearchAnalysisProcess
            :events="group.processEvents"
            :expanded="group.officialEvents.length === 0 || containsActiveEvent(group.processEvents)"
            :active-event-id="activeEventId"
          />
          <ResearchOfficialEventList
            v-if="!followUpRunIdSet.has(group.key)"
            :events="group.officialEvents.filter(isResearchWorkspaceReportEvent)"
            :downloading-artifact-id="downloadingArtifactId"
            :active-event-id="activeEventId"
            @download="downloadArtifact"
          />
        </section>

        <ResearchOfficialEventList
          :events="globalOfficialEvents.filter(isResearchWorkspaceReportEvent)"
          :downloading-artifact-id="downloadingArtifactId"
          :active-event-id="activeEventId"
          @download="downloadArtifact"
        />
      </template>

      <template v-else-if="props.section === 'report'">
        <ElEmpty
          v-if="!hasReportActivity"
          :image-size="56"
          description="正式报告生成后会显示在这里"
        />
        <section
          v-for="group in initialAnalysisGroups"
          :key="group.key"
          class="report-section"
        >
          <ResearchOfficialEventList
            :events="group.officialEvents.filter(isResearchWorkspaceReportEvent)"
            :downloading-artifact-id="downloadingArtifactId"
            :active-event-id="activeEventId"
            @download="downloadArtifact"
          />
        </section>
        <ResearchOfficialEventList
          :events="globalOfficialEvents.filter(isResearchWorkspaceReportEvent)"
          :downloading-artifact-id="downloadingArtifactId"
          :active-event-id="activeEventId"
          @download="downloadArtifact"
        />
      </template>

      <template v-else>
        <ElEmpty
          v-if="!hasProcessActivity"
          :image-size="56"
          description="执行过程事件会在这里实时出现"
        />
        <ResearchAnalysisProcess
          :events="globalProcessEvents"
          :expanded="containsActiveEvent(globalProcessEvents)"
          :active-event-id="activeEventId"
        />
        <ResearchOfficialEventList
          :events="globalOfficialEvents.filter(isWorkspaceProcessOfficialEvent)"
          :downloading-artifact-id="downloadingArtifactId"
          :active-event-id="activeEventId"
          @download="downloadArtifact"
        />
        <section
          v-for="group in analysisGroups"
          :key="group.key"
          class="process-section"
        >
          <ResearchAnalysisProcess
            :events="group.processEvents"
            :expanded="containsActiveEvent(group.processEvents)"
            :active-event-id="activeEventId"
          />
          <ResearchOfficialEventList
            :events="group.officialEvents.filter(isWorkspaceProcessOfficialEvent)"
            :downloading-artifact-id="downloadingArtifactId"
            :active-event-id="activeEventId"
            @download="downloadArtifact"
          />
        </section>
      </template>

      <section
        v-if="props.section === 'all' || props.section === 'report'"
        class="report-follow-up"
        data-testid="research-report-follow-up"
        aria-labelledby="research-report-follow-up-title"
      >
        <header class="report-follow-up__header">
          <div>
            <h3 id="research-report-follow-up-title">
              报告问答
            </h3>
            <span v-if="followUpTurns.length > 0">{{ followUpTurns.length }} 次追问</span>
          </div>
          <ElButton
            v-if="analysisRunsError"
            link
            type="primary"
            :icon="RefreshRight"
            @click="loadResearchAnalysisHistory()"
          >
            重新加载
          </ElButton>
        </header>

        <ElAlert
          v-if="analysisRunsError"
          type="warning"
          :title="analysisRunsError"
          :closable="false"
          show-icon
        />
        <ElSkeleton
          v-else-if="analysisRunsLoading && followUpTurns.length === 0"
          :rows="2"
          animated
        />
        <p
          v-else-if="followUpTurns.length === 0"
          class="report-follow-up__empty"
        >
          暂无追问
        </p>

        <article
          v-for="turn in followUpTurns"
          :key="turn.analysisRunId"
          class="follow-up-turn"
          :data-follow-up-run-id="turn.analysisRunId"
          data-testid="research-follow-up-turn"
        >
          <div class="follow-up-turn__question">
            <span aria-hidden="true">我</span>
            <div>
              <p>{{ turn.question }}</p>
              <time v-if="formatFollowUpTime(turn.run.createdAt)">
                {{ formatFollowUpTime(turn.run.createdAt) }}
              </time>
            </div>
          </div>
          <div class="follow-up-turn__answer">
            <span aria-hidden="true">AI</span>
            <div>
              <header>
                <strong>简短回答</strong>
                <ElTag
                  :type="followUpStatusType(turn.status)"
                  size="small"
                  effect="plain"
                >
                  {{ followUpStatusLabel(turn.status) }}
                </ElTag>
              </header>
              <SafeMarkdown
                v-if="turn.answer"
                class="follow-up-turn__content"
                :class="{ 'is-streaming': turn.streaming }"
                :content="turn.answer"
              />
              <p
                v-else-if="['WAITING_RESEARCH', 'QUEUED', 'RUNNING', 'RETRY_WAIT'].includes(turn.status)"
                class="follow-up-turn__pending"
                data-testid="research-follow-up-pending"
              >
                <span aria-hidden="true" />
                正在回答
              </p>
              <ElAlert
                v-if="turn.status === 'FAILED'"
                class="follow-up-turn__error"
                type="error"
                :title="turn.errorMessage || '本次追问回答失败'"
                :closable="false"
                show-icon
              />
              <p
                v-else-if="turn.status === 'CANCELLED' && !turn.answer"
                class="follow-up-turn__cancelled"
              >
                本次回答已取消
              </p>
              <ElButton
                v-if="turn.retryable && turn.status === 'FAILED'"
                class="follow-up-turn__retry"
                type="primary"
                plain
                size="small"
                :icon="RefreshRight"
                :loading="commandLoading"
                :data-testid="`retry-research-follow-up-${turn.analysisRunId}`"
                @click="retryFollowUp(turn.analysisRunId)"
              >
                重试回答
              </ElButton>
            </div>
          </div>
        </article>
      </section>

      <p
        v-if="streaming && !workflowTerminal && analysisRunning"
        class="research-agent__streaming"
        data-testid="research-streaming-status"
      >
        <span aria-hidden="true" />
        正在接收 Graph 与 Agent 事件
      </p>
    </div>

    <form
      v-if="props.section === 'all' || props.section === 'report'"
      class="research-agent__composer"
      data-testid="research-follow-up-composer"
      @submit.prevent="submitQuestion"
    >
      <ElInput
        v-model="question"
        type="textarea"
        :rows="2"
        maxlength="1000"
        show-word-limit
        resize="none"
        aria-label="继续追问"
        :disabled="props.jobStatus !== 'SUCCEEDED' || analysisBusy"
        :placeholder="composerPlaceholder"
        @keydown.ctrl.enter="submitQuestion"
        @keydown.meta.enter="submitQuestion"
      />
      <div class="research-agent__composer-footer">
        <ElButton
          type="primary"
          :icon="Promotion"
          :loading="commandLoading"
          :disabled="!canSendMessage"
          data-testid="send-research-follow-up"
          native-type="submit"
        >
          发送追问
        </ElButton>
      </div>
    </form>
  </section>
</template>

<style scoped>
.research-agent {
  margin-top: 20px;
  overflow: hidden;
  background: var(--color-surface-muted);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-lg);
}

.research-agent__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.research-agent__header h3 {
  margin-top: 5px;
}

.research-agent__header p {
  margin: 4px 0 0;
  color: var(--color-text-secondary);
  font-size: 11px;
}

.research-agent__eyebrow {
  display: flex;
  align-items: center;
  gap: 5px;
  color: var(--color-brand-700);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0;
  text-transform: uppercase;
}

.research-agent__actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.research-agent__alert {
  width: auto;
  margin: 14px 16px 0;
}

.research-agent__timeline {
  min-width: 0;
  min-height: 120px;
  max-height: 680px;
  padding: 0 16px 16px;
  overflow: auto;
}

.analysis-turn {
  min-width: 0;
  margin-top: 14px;
  padding-top: 2px;
  border-top: 1px dashed var(--color-border-strong);
}

.research-agent__streaming {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 14px 2px 0;
  color: var(--color-text-secondary);
  font-size: 11px;
}

.research-agent__streaming span,
.follow-up-turn__pending > span {
  width: 7px;
  height: 7px;
  background: var(--color-brand-500);
  border-radius: 50%;
  animation: stream-pulse 1.2s ease-in-out infinite;
}

.research-agent__composer {
  display: grid;
  gap: 10px;
  padding: 14px 16px;
  background: var(--color-surface);
  border-top: 1px solid var(--color-border);
}

.research-agent__composer-footer {
  display: flex;
  justify-content: flex-end;
}

@keyframes stream-pulse {
  0%,
  100% {
    opacity: 0.35;
    transform: scale(0.85);
  }

  50% {
    opacity: 1;
    transform: scale(1.15);
  }
}

@media (max-width: 720px) {
  .research-agent__header {
    align-items: stretch;
    flex-direction: column;
  }

  .research-agent__actions {
    justify-content: flex-start;
  }
}
</style>

<style scoped>
.research-agent--focus {
  display: flex;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  margin-top: 0;
}

.research-agent--focus .research-agent__timeline {
  min-height: 0;
  max-height: none;
  flex: 1;
}

.research-agent--workspace {
  display: flex;
  min-width: 0;
  min-height: 0;
  height: 100%;
  flex: 1;
  flex-direction: column;
  margin-top: 0;
  border: 0;
  border-radius: 0;
  background: var(--color-surface);
}

.research-agent__statusbar {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 20px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-surface-muted);
}

.research-agent__statusbar > div {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
}

.research-agent__statusbar p {
  margin: 0;
  overflow: hidden;
  color: var(--color-text-secondary);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.research-agent__status-dot {
  flex: 0 0 7px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--el-color-info);
}

.research-agent__status-dot--warning {
  background: var(--el-color-warning);
}

.research-agent__status-dot--success {
  background: var(--el-color-success);
}

.research-agent__status-dot--danger {
  background: var(--el-color-danger);
}

.research-agent--workspace .research-agent__alert {
  margin: 12px 20px 0;
}

.research-agent--workspace .research-agent__timeline {
  min-height: 0;
  max-height: none;
  flex: 1;
  padding: 4px 20px 24px;
  overflow-x: hidden;
  overflow-y: auto;
}

.research-agent--workspace .research-agent__timeline--report > :deep(.official-event-list),
.research-agent--workspace .report-section,
.research-agent--workspace .report-follow-up {
  width: min(100%, 980px);
  margin-right: auto;
  margin-left: auto;
}

.research-agent--workspace .research-agent__composer {
  flex: 0 0 auto;
  padding: 12px 20px;
}

.report-section,
.process-section {
  min-width: 0;
}

.report-follow-up {
  margin-top: 28px;
  padding: 22px 0 8px;
  border-top: 1px solid var(--color-border-strong);
}

.report-follow-up__header,
.report-follow-up__header > div,
.follow-up-turn__answer header {
  display: flex;
  align-items: center;
}

.report-follow-up__header {
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 6px;
}

.report-follow-up__header > div {
  gap: 10px;
}

.report-follow-up__header h3 {
  margin: 0;
  color: var(--color-text);
  font-size: 15px;
  letter-spacing: 0;
}

.report-follow-up__header span,
.report-follow-up__empty,
.follow-up-turn time,
.follow-up-turn__cancelled {
  color: var(--color-text-muted);
  font-size: 11px;
}

.report-follow-up__empty {
  margin: 18px 0 4px;
}

.follow-up-turn {
  display: grid;
  gap: 14px;
  padding: 18px 0;
  border-bottom: 1px solid var(--color-border);
}

.follow-up-turn__question,
.follow-up-turn__answer {
  display: grid;
  min-width: 0;
  grid-template-columns: 30px minmax(0, 1fr);
  align-items: flex-start;
  gap: 10px;
}

.follow-up-turn__question > span,
.follow-up-turn__answer > span {
  display: grid;
  width: 30px;
  height: 30px;
  place-items: center;
  color: white;
  background: var(--color-brand-600);
  border-radius: 50%;
  font-size: 10px;
  font-weight: 650;
}

.follow-up-turn__answer > span {
  color: var(--color-brand-700);
  background: var(--color-brand-50);
  border: 1px solid var(--color-brand-200);
}

.follow-up-turn__question > div,
.follow-up-turn__answer > div {
  min-width: 0;
}

.follow-up-turn__question p {
  width: fit-content;
  max-width: min(100%, 720px);
  margin: 0 0 4px;
  padding: 9px 11px;
  color: var(--color-text);
  background: var(--color-brand-50);
  border: 1px solid var(--color-brand-200);
  border-radius: var(--radius-md);
  font-size: 13px;
  line-height: 1.65;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}

.follow-up-turn__answer header {
  min-height: 30px;
  justify-content: space-between;
  gap: 12px;
}

.follow-up-turn__answer header strong {
  color: var(--color-text);
  font-size: 13px;
}

.follow-up-turn__content {
  min-width: 0;
  margin-top: 6px;
  color: var(--color-text);
  font-size: 13px;
  line-height: 1.75;
  overflow-x: auto;
}

.follow-up-turn__content.is-streaming::after {
  display: inline-block;
  width: 6px;
  height: 14px;
  margin-left: 3px;
  background: var(--color-brand-500);
  content: '';
  vertical-align: -2px;
  animation: stream-pulse 1s ease-in-out infinite;
}

.follow-up-turn__pending {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0 0;
  color: var(--color-text-secondary);
  font-size: 12px;
}

.follow-up-turn__error {
  margin-top: 8px;
}

.follow-up-turn__retry {
  margin-top: 8px;
}

@media (max-width: 640px) {
  .research-agent__statusbar {
    align-items: flex-start;
    padding: 9px 12px;
  }

  .research-agent__statusbar > div:first-child {
    flex: 1;
    flex-wrap: wrap;
  }

  .research-agent__statusbar p {
    max-width: 100%;
    white-space: normal;
  }

  .research-agent__statusbar .research-agent__actions {
    flex: 0 0 auto;
  }

  .research-agent--workspace .research-agent__timeline {
    padding: 2px 12px 16px;
  }

  .research-agent--workspace .research-agent__composer {
    padding: 10px 12px;
  }
}
</style>
