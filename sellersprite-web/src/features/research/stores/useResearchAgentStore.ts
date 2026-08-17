import { defineStore } from 'pinia'

import {
  isResearchOfficialEvent,
  isResearchProcessEvent,
  isResearchUserEvent,
  isWorkflowTerminalEvent,
  type ResearchWorkspaceLiveSection,
} from '../model/researchEventPresentation'
import type { ResearchJobDetail, ResearchNodeExecution } from '../model/research'
import type {
  ResearchEventScope,
  ResearchReportChart,
  ResearchStreamFrame,
  ResearchStreamEvent,
  ResearchStreamRecord,
} from '../model/researchStream'

const STREAMING_SUMMARY_ID_PREFIX = 'streaming-summary-'
const STREAMING_SHEET_THINK_ID_PREFIX = 'streaming-sheet-think-'

type AnalysisState = 'IDLE' | 'QUEUED' | 'RUNNING' | 'SUCCEEDED' | 'FAILED' | 'CANCELLED'

interface ResearchAgentState {
  jobId: string
  job: ResearchJobDetail | null
  nodes: ResearchNodeExecution[]
  events: ResearchStreamRecord[]
  reportCharts: Record<string, ResearchReportChart>
  seenSequences: Record<string, true>
  lastSequence: number
  replayComplete: boolean
  historyLoading: boolean
  connecting: boolean
  streaming: boolean
  reconnecting: boolean
  connectionError: string
  workflowTerminal: ResearchStreamRecord | null
  activeAnalysisRunId: string
  analysisState: AnalysisState
  analysisRetryable: boolean
  workspaceDismissedJobId: string
  workspaceDismissedRunId: string
  workspaceFollowPaused: boolean
  workspaceFollowContext: string
  workspaceSuggestedSection: ResearchWorkspaceLiveSection | ''
  workspaceActiveEventId: string
  workspaceActiveSequence: number
  workspaceEvidenceStage: 'SCREENING' | 'DEEP_DIVE'
}

export const useResearchAgentStore = defineStore('research-agent', {
  state: (): ResearchAgentState => emptyState(),
  getters: {
    officialEvents: (state) => state.events.filter(isResearchOfficialEvent),
    processEvents: (state) => state.events.filter(isResearchProcessEvent),
    userEvents: (state) => state.events.filter(isResearchUserEvent),
    hasActivity: (state) => state.events.length > 0
      || state.historyLoading
      || state.connecting
      || state.streaming,
    analysisRunning: (state) => ['QUEUED', 'RUNNING'].includes(state.analysisState),
    reportChartList: (state) => Object.values(state.reportCharts),
  },
  actions: {
    startJob(jobId: string) {
      if (this.jobId === jobId) return
      Object.assign(this, emptyState(), { jobId })
    },
    beginHistoryLoad() {
      this.historyLoading = true
      this.replayComplete = false
      this.connectionError = ''
    },
    applyFrame(frame: ResearchStreamFrame) {
      if (frame.jobId !== this.jobId) return

      if (frame.frameType === 'snapshot') {
        this.job = frame.job
        this.nodes = [...frame.nodes]
        if (frame.afterSequence === 0) {
          this.hydrate(frame.events)
        } else {
          frame.events
            .slice()
            .sort((left, right) => left.sequenceNo - right.sequenceNo)
            .forEach((event) => this.appendEvent(event))
        }
      } else {
        if (frame.job) this.job = frame.job
        if (frame.nodes) this.nodes = [...frame.nodes]
        frame.events
          .slice()
          .sort((left, right) => left.sequenceNo - right.sequenceNo)
          .forEach((event) => this.appendEvent(event))
      }

      this.lastSequence = Math.max(this.lastSequence, frame.lastSequence)
      this.replayComplete = frame.replayComplete
      this.historyLoading = !frame.replayComplete
      this.connectionError = ''
    },
    hydrate(events: ResearchStreamEvent[]) {
      this.events = []
      this.reportCharts = {}
      this.seenSequences = {}
      this.lastSequence = 0
      this.workflowTerminal = null
      this.activeAnalysisRunId = ''
      this.analysisState = 'IDLE'
      this.analysisRetryable = false
      events
        .filter((event) => event.jobId === this.jobId)
        .sort((left, right) => left.sequenceNo - right.sequenceNo)
        .forEach((event) => this.appendEvent(event))
      this.historyLoading = false
      this.replayComplete = true
    },
    failHistoryLoad(message: string) {
      this.historyLoading = false
      this.replayComplete = false
      this.connectionError = message
    },
    appendEvent(event: ResearchStreamEvent) {
      if (event.jobId !== this.jobId || this.seenSequences[`${event.sequenceNo}`]) return

      const record = recordFromEvent(event)
      this.seenSequences[`${event.sequenceNo}`] = true
      this.lastSequence = Math.max(this.lastSequence, event.sequenceNo)
      if (record.eventType === 'report_chart') this.rememberReportChart(record)

      if (record.eventType === 'summary_delta') {
        this.appendSummaryDelta(record)
      } else if (record.eventType === 'sheet_think_delta') {
        this.appendSheetThinkDelta(record)
      } else if (record.eventType === 'summary') {
        this.removeStreamingSummary(record)
        this.events.push(record)
      } else if (record.eventType === 'sheet_think' && this.finalizeStreamingSheetThink(record)) {
        // The final persisted event replaces its temporary delta aggregation in place.
      } else {
        this.events.push(record)
      }

      this.updateLifecycle(record)
    },
    prepareForNewAnalysis(analysisRunId = '') {
      this.workflowTerminal = null
      this.analysisState = 'QUEUED'
      this.analysisRetryable = false
      this.connectionError = ''
      if (analysisRunId) this.activeAnalysisRunId = analysisRunId
    },
    rememberReportChart(record: ResearchStreamRecord) {
      if (!isResearchReportChart(record.data)) return
      const key = `${record.analysisRunId || record.jobId}:${record.data.sectionCode}:${record.data.chartCode}`
      this.reportCharts[key] = record.data
    },
    dismissWorkspace(jobId: string, analysisRunId = '') {
      this.workspaceDismissedJobId = jobId
      this.workspaceDismissedRunId = analysisRunId
    },
    clearWorkspaceDismissal() {
      this.workspaceDismissedJobId = ''
      this.workspaceDismissedRunId = ''
    },
    noteWorkspaceLiveEvent(
      section: ResearchWorkspaceLiveSection,
      eventId: string,
      sequence: number,
      context: string,
      evidenceStage?: 'SCREENING' | 'DEEP_DIVE',
    ) {
      if (context && context !== this.workspaceFollowContext) {
        this.workspaceFollowPaused = false
        this.workspaceFollowContext = context
      }
      this.workspaceSuggestedSection = section
      this.workspaceActiveEventId = eventId
      this.workspaceActiveSequence = Math.max(this.workspaceActiveSequence, sequence)
      if (evidenceStage) this.workspaceEvidenceStage = evidenceStage
    },
    pauseWorkspaceFollow() {
      this.workspaceFollowPaused = true
    },
    resumeWorkspaceFollow() {
      this.workspaceFollowPaused = false
    },
    setWorkspaceEvidenceStage(stage: 'SCREENING' | 'DEEP_DIVE') {
      this.workspaceEvidenceStage = stage
    },
    markConnecting(reconnecting = false) {
      this.connecting = true
      this.reconnecting = reconnecting
      this.connectionError = ''
    },
    markConnected() {
      this.connecting = false
      this.reconnecting = false
      this.streaming = true
      this.connectionError = ''
    },
    markDisconnected(message = '') {
      this.connecting = false
      this.streaming = false
      this.reconnecting = Boolean(message) && !this.workflowTerminal
      this.connectionError = message
    },
    stopStreaming() {
      this.connecting = false
      this.streaming = false
      this.reconnecting = false
    },
    appendSummaryDelta(record: ResearchStreamRecord) {
      const key = analysisRecordKey(record)
      const id = `${STREAMING_SUMMARY_ID_PREFIX}${key}`
      const delta = deltaText(record)
      const existing = this.events.find((event) => event.id === id)
      if (existing) {
        existing.message += delta
        existing.data = existing.message
        existing.sequenceNo = record.sequenceNo
        existing.receivedAt = record.receivedAt
        return
      }
      this.events.push({
        ...record,
        id,
        eventType: 'summary',
        message: delta,
        data: delta,
        streaming: true,
      })
    },
    removeStreamingSummary(record: ResearchStreamRecord) {
      const id = `${STREAMING_SUMMARY_ID_PREFIX}${analysisRecordKey(record)}`
      this.events = this.events.filter((event) => event.id !== id)
    },
    appendSheetThinkDelta(record: ResearchStreamRecord) {
      const id = `${STREAMING_SHEET_THINK_ID_PREFIX}${analysisRecordKey(record)}-${record.sheetName || 'unknown'}`
      const delta = deltaText(record)
      const existing = this.events.find((event) => event.id === id)
      if (existing) {
        existing.data = `${typeof existing.data === 'string' ? existing.data : ''}${delta}`
        existing.sequenceNo = record.sequenceNo
        existing.receivedAt = record.receivedAt
        return
      }
      this.events.push({
        ...record,
        id,
        eventType: 'sheet_think',
        message: sheetThinkMessage(record.sheetName),
        data: delta,
        streaming: true,
      })
    },
    finalizeStreamingSheetThink(record: ResearchStreamRecord) {
      const id = `${STREAMING_SHEET_THINK_ID_PREFIX}${analysisRecordKey(record)}-${record.sheetName || 'unknown'}`
      const index = this.events.findIndex((event) => event.id === id)
      if (index < 0) return false
      this.events[index] = record
      return true
    },
    updateLifecycle(record: ResearchStreamRecord) {
      if (
        record.scope === 'analysis'
        && this.workflowTerminal?.sequenceNo !== undefined
        && record.sequenceNo !== undefined
        && record.sequenceNo > this.workflowTerminal.sequenceNo
      ) {
        this.workflowTerminal = null
      }
      if (record.analysisRunId) this.activeAnalysisRunId = record.analysisRunId
      if (record.scope === 'analysis') {
        if (record.eventType === 'done') {
          const status = analysisStatus(record.data)
          this.analysisState = status
          this.analysisRetryable = status === 'FAILED'
        } else if (record.eventType === 'stage_completed') {
          this.analysisState = 'SUCCEEDED'
          this.analysisRetryable = false
        } else if (record.eventType === 'error') {
          this.analysisState = 'FAILED'
          this.analysisRetryable = eventRetryable(record.data)
        } else if (record.eventType === 'analysis_cancelled') {
          this.analysisState = 'CANCELLED'
          this.analysisRetryable = true
        } else if (['analysis_waiting_research', 'analysis_queued'].includes(record.eventType)) {
          this.analysisState = 'QUEUED'
          this.analysisRetryable = false
        } else if (!isResearchUserEvent(record)) {
          this.analysisState = 'RUNNING'
        }
      }
      if (isWorkflowTerminalEvent(record)) {
        this.workflowTerminal = record
      }
    },
  },
})

function emptyState(): ResearchAgentState {
  return {
    jobId: '',
    job: null,
    nodes: [],
    events: [],
    reportCharts: {},
    seenSequences: {},
    lastSequence: 0,
    replayComplete: false,
    historyLoading: false,
    connecting: false,
    streaming: false,
    reconnecting: false,
    connectionError: '',
    workflowTerminal: null,
    activeAnalysisRunId: '',
    analysisState: 'IDLE',
    analysisRetryable: false,
    workspaceDismissedJobId: '',
    workspaceDismissedRunId: '',
    workspaceFollowPaused: false,
    workspaceFollowContext: '',
    workspaceSuggestedSection: '',
    workspaceActiveEventId: '',
    workspaceActiveSequence: 0,
    workspaceEvidenceStage: 'SCREENING',
  }
}

function recordFromEvent(event: ResearchStreamEvent): ResearchStreamRecord {
  return {
    id: event.eventId || `event-${event.sequenceNo}`,
    sequenceNo: event.sequenceNo,
    jobId: event.jobId,
    conversationId: event.conversationId || undefined,
    analysisRunId: event.analysisRunId || undefined,
    scope: normalizeScope(event.scope),
    eventType: event.eventType,
    phase: event.phase || undefined,
    sheetName: event.sheetName || undefined,
    stageCode: event.stageCode || payloadText(event.payload, 'stageCode'),
    datasetCode: event.datasetCode || payloadText(event.payload, 'datasetCode'),
    graphCode: event.graphCode || undefined,
    nodeCode: event.nodeCode || undefined,
    message: event.message,
    data: normalizedEventData(event),
    terminal: event.terminal ?? false,
    receivedAt: timeLabel(event.createdAt),
  }
}

function normalizeScope(scope: ResearchStreamEvent['scope']): ResearchEventScope {
  const normalized = scope.toLowerCase()
  return ['research', 'analysis', 'artifact', 'workflow'].includes(normalized)
    ? normalized as ResearchEventScope
    : 'research'
}

function normalizedEventData(event: ResearchStreamEvent) {
  if (event.data !== undefined) return event.data
  if (isRecord(event.payload) && Object.hasOwn(event.payload, 'data')) {
    return event.payload.data
  }
  return event.payload
}

function payloadText(payload: unknown, key: string) {
  if (!isRecord(payload)) return undefined
  const value = payload[key]
  return typeof value === 'string' && value.trim() ? value.trim() : undefined
}

function analysisRecordKey(record: ResearchStreamRecord) {
  const runKey = record.analysisRunId || record.conversationId || record.jobId
  return `${runKey}:${record.stageCode || 'global'}`
}

function deltaText(record: ResearchStreamRecord) {
  return typeof record.data === 'string' ? record.data : record.message
}

function sheetThinkMessage(sheetName?: string) {
  return sheetName
    ? `模型正在判断 Sheet「${sheetName}」的市场价值。`
    : '模型正在判断当前 Sheet 的市场价值。'
}

function eventRetryable(data: unknown) {
  return !isRecord(data) || data.retryable !== false
}

function isResearchReportChart(value: unknown): value is ResearchReportChart {
  if (!isRecord(value)) return false
  return typeof value.chartCode === 'string'
    && typeof value.sectionCode === 'string'
    && typeof value.sectionTitle === 'string'
    && ['LINE', 'BAR'].includes(String(value.type))
    && typeof value.title === 'string'
    && Array.isArray(value.categories)
    && Array.isArray(value.series)
}

function analysisStatus(data: unknown): AnalysisState {
  if (!isRecord(data) || typeof data.analysisStatus !== 'string') return 'SUCCEEDED'
  const status = data.analysisStatus.toUpperCase()
  return ['SUCCEEDED', 'FAILED', 'CANCELLED'].includes(status)
    ? status as AnalysisState
    : 'SUCCEEDED'
}

function timeLabel(value?: number | string | null) {
  const timestamp = typeof value === 'number' ? value : value ? Date.parse(value) : Date.now()
  const date = Number.isFinite(timestamp) ? new Date(timestamp) : new Date()
  return date.toLocaleTimeString('zh-CN', {
    hour12: false,
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}
