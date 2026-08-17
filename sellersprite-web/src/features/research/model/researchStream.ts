import type {
  ResearchGraphCode,
  ResearchJobDetail,
  ResearchNodeExecution,
  ResearchStageCode,
} from './research'

export const researchEventScopes = ['research', 'analysis', 'artifact', 'workflow'] as const

export type ResearchEventScope = (typeof researchEventScopes)[number]

export interface ResearchStreamEvent {
  eventId?: string
  sequenceNo: number
  jobId: string
  conversationId?: string | null
  analysisRunId?: string | null
  scope: ResearchEventScope | Uppercase<ResearchEventScope>
  eventType: string
  phase?: string | null
  sheetName?: string | null
  stageCode?: ResearchStageCode | string | null
  datasetCode?: string | null
  graphCode?: ResearchGraphCode | string | null
  nodeCode?: string | null
  message: string
  data?: unknown
  payload?: unknown
  terminal?: boolean
  createdAt?: number | string | null
}

interface ResearchStreamFrameBase {
  frameType: 'snapshot' | 'events'
  jobId: string
  afterSequence: number
  lastSequence: number
  replayComplete: boolean
  events: ResearchStreamEvent[]
}

export interface ResearchStreamSnapshotFrame extends ResearchStreamFrameBase {
  frameType: 'snapshot'
  job: ResearchJobDetail
  nodes: ResearchNodeExecution[]
}

export interface ResearchStreamEventsFrame extends ResearchStreamFrameBase {
  frameType: 'events'
  job?: ResearchJobDetail
  nodes?: ResearchNodeExecution[]
}

export type ResearchStreamFrame = ResearchStreamSnapshotFrame | ResearchStreamEventsFrame

export interface ResearchStreamRecord {
  id: string
  sequenceNo?: number
  jobId: string
  conversationId?: string
  analysisRunId?: string
  scope: ResearchEventScope
  eventType: string
  phase?: string
  sheetName?: string
  stageCode?: ResearchStageCode | string
  datasetCode?: string
  graphCode?: ResearchGraphCode | string
  nodeCode?: string
  message: string
  data?: unknown
  terminal: boolean
  receivedAt: string
  streaming?: boolean
}

export interface ResearchMessageRequest {
  content: string
}

export interface ResearchReportDownload {
  artifactId: string
  fileName: string
  mediaType?: string
  analysisRunId?: string
  downloadUrl?: string
}

export type ResearchReportChartType = 'LINE' | 'BAR'

export interface ResearchReportChartSeries {
  name: string
  values: number[]
}

export interface ResearchReportChart {
  chartCode: string
  sectionCode: string
  sectionTitle: string
  type: ResearchReportChartType
  title: string
  categories: string[]
  series: ResearchReportChartSeries[]
  unit?: string
  methodology?: string
}
