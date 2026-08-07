import type { ResearchStreamRecord } from './researchStream'

const OFFICIAL_EVENT_TYPES = new Set([
  'summary',
  'report',
  'download',
  'error',
  'product_selection_required',
  'product_selection_submitted',
  'stage_completed',
  'market_abandoned',
  'workflow_completed',
  'workflow_failed',
  'workflow_cancelled',
])
const HIDDEN_EVENT_TYPES = new Set(['summary_delta', 'sheet_think_delta'])
const USER_EVENT_TYPES = new Set(['user_message', 'follow_up_requested'])

export type ResearchWorkspaceLiveSection = 'report' | 'evidence' | 'process'

export interface ResearchWorkspaceEventIntent {
  mode: 'workspace' | 'task'
  section?: ResearchWorkspaceLiveSection
  evidenceStage?: 'SCREENING' | 'DEEP_DIVE'
  force?: boolean
}

export const RESEARCH_EVENT_TYPE_LABELS: Record<string, string> = {
  research_queued: '调研已排队',
  research_started: '调研开始',
  research_node_started: '节点开始',
  research_node_progress: '节点进度',
  research_node_completed: '节点完成',
  research_node_failed: '节点失败',
  research_retry_scheduled: '调研等待重试',
  research_retry_wait: '等待重试',
  workbook_ready: '证据工作簿就绪',
  analysis_waiting_research: '分析等待证据',
  analysis_queued: '分析已排队',
  analysis_cancel_requested: '分析取消中',
  product_selection_required: '等待商品选择',
  product_selection_submitted: '商品选择已提交',
  stage_completed: '阶段已完成',
  market_abandoned: '已放弃该市场',
  plan: '执行计划',
  workbook: '工作簿结构',
  sheet_prepare: 'Sheet 准备',
  sheet: 'Sheet 分析',
  sheet_focus: 'Sheet 聚焦',
  sheet_think: '模型判断',
  context_compress_start: '上下文整理',
  context_compress_done: '上下文已整理',
  context_compress_failed: '上下文整理失败',
  think: 'Agent 思考',
  tool: '工具执行',
  summary_prepare: '总结准备',
  summary: '分析结论',
  report: '生成分析报告',
  download: '分析报告附件',
  done: '本轮分析完成',
  error: '本轮分析失败',
  user_message: '我的问题',
  follow_up_requested: '继续追问',
  workflow_completed: '完整流程已完成',
  workflow_failed: '完整流程失败',
  workflow_cancelled: '完整流程已取消',
}

export const RESEARCH_PHASE_LABELS: Record<string, string> = {
  collect: '采集',
  prepare: '整理',
  publish: '发布',
  plan: '计划',
  observe: '观察',
  context: '上下文',
  think: '思考',
  act: '执行',
  summary: '总结',
  report: '报告',
  review: '人工审核',
  done: '完成',
  error: '错误',
}

export function researchEventTypeLabel(eventType: string) {
  return RESEARCH_EVENT_TYPE_LABELS[eventType] ?? eventType
}

export function researchPhaseLabel(phase?: string) {
  return phase ? RESEARCH_PHASE_LABELS[phase] ?? phase : ''
}

export function isResearchOfficialEvent(
  event: Pick<ResearchStreamRecord, 'eventType' | 'streaming'>,
) {
  if (event.eventType === 'summary' && event.streaming) return false
  return !isResearchHiddenEvent(event) && OFFICIAL_EVENT_TYPES.has(event.eventType)
}

export function isResearchUserEvent(event: Pick<ResearchStreamRecord, 'eventType'>) {
  return USER_EVENT_TYPES.has(event.eventType)
}

export function isResearchProcessEvent(event: Pick<ResearchStreamRecord, 'eventType'>) {
  return !isResearchHiddenEvent(event)
    && !isResearchOfficialEvent(event)
    && !isResearchUserEvent(event)
}

export function isResearchHiddenEvent(event: Pick<ResearchStreamRecord, 'eventType'>) {
  return HIDDEN_EVENT_TYPES.has(event.eventType)
}

export function isResearchWorkspaceReportEvent(
  event: Pick<ResearchStreamRecord, 'eventType' | 'streaming'>,
) {
  if (event.eventType === 'summary') return !event.streaming
  return ['report', 'download'].includes(event.eventType)
}

export function researchWorkspaceIntent(
  event: ResearchStreamRecord,
  hasFollowUpContext = false,
): ResearchWorkspaceEventIntent {
  if (hasFollowUpContext) {
    return { mode: 'workspace' }
  }
  if (event.eventType === 'product_selection_required') {
    return { mode: 'task' }
  }
  if (event.eventType === 'workbook_ready') {
    return {
      mode: 'workspace',
      section: 'evidence',
      evidenceStage: event.stageCode === 'DEEP_DIVE' ? 'DEEP_DIVE' : 'SCREENING',
    }
  }
  if (isResearchUserEvent(event)) {
    return { mode: 'workspace' }
  }
  if (event.eventType === 'summary' || event.eventType === 'report') {
    if (event.streaming) return { mode: 'workspace', section: 'process' }
    return { mode: 'workspace', section: 'report' }
  }
  if (event.eventType === 'download') {
    return { mode: 'workspace', section: 'report', force: true }
  }
  if (event.eventType === 'done') {
    const status = eventStatus(event.data)
    if (['FAILED', 'CANCELLED'].includes(status)) {
      return { mode: 'workspace', section: 'process' }
    }
    return { mode: 'workspace', section: 'report' }
  }
  if (event.eventType === 'stage_completed' && event.stageCode === 'FINAL_ANALYSIS') {
    return { mode: 'workspace', section: 'report' }
  }
  if (event.eventType === 'market_abandoned') {
    return { mode: 'workspace', section: 'process', force: true }
  }
  if (['workflow_completed', 'research_completed'].includes(event.eventType)) {
    return { mode: 'workspace', section: 'report', force: true }
  }
  return { mode: 'workspace', section: 'process' }
}

export function researchEventTitle(event: ResearchStreamRecord) {
  if (event.sheetName) {
    return `${researchEventTypeLabel(event.eventType)} · ${event.sheetName}`
  }
  if (event.nodeCode && event.eventType.startsWith('research_node_')) {
    return `${researchEventTypeLabel(event.eventType)} · ${event.nodeCode}`
  }
  return researchEventTypeLabel(event.eventType)
}

export function researchEventMeta(event: ResearchStreamRecord) {
  return [
    researchPhaseLabel(event.phase),
    event.sequenceNo === undefined ? '' : `#${event.sequenceNo}`,
  ].filter(Boolean).join(' · ')
}

export function researchEventDetail(event: ResearchStreamRecord) {
  if (!['sheet', 'sheet_focus', 'sheet_think'].includes(event.eventType)) {
    return ''
  }
  const detail = stringifyDetailData(event.data)
  if (!detail || detail === event.message) {
    return ''
  }
  return detail.replace(/\r\n/g, '\n').trim()
}

export function researchEventDetailLabel(event: Pick<ResearchStreamRecord, 'eventType'>) {
  if (event.eventType === 'sheet_think') return '查看模型判断'
  if (event.eventType === 'sheet_focus') return '查看思考重点'
  return '查看原始观察'
}

export function isWorkflowTerminalEvent(event: Pick<ResearchStreamRecord, 'scope' | 'eventType'>) {
  return event.scope === 'workflow'
    && [
      'workflow_completed',
      'workflow_failed',
      'workflow_cancelled',
      'market_abandoned',
    ].includes(event.eventType)
}

function stringifyDetailData(data: unknown) {
  if (typeof data === 'string') return data.trim()
  if (!isRecord(data)) return ''
  const preferredValues = ['summary', 'observation', 'action']
    .map((key) => data[key])
    .filter((value): value is string => typeof value === 'string' && value.trim().length > 0)
    .map((value) => value.trim())
  if (preferredValues.length > 0) {
    return [...new Set(preferredValues)].join('\n')
  }
  return JSON.stringify(data, null, 2)
}

function eventStatus(data: unknown) {
  if (!isRecord(data)) return 'SUCCEEDED'
  const value = data.analysisStatus ?? data.status
  return typeof value === 'string' ? value.toUpperCase() : 'SUCCEEDED'
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}
