export const researchJobStatuses = ['QUEUED', 'RUNNING', 'SUCCEEDED', 'FAILED'] as const

export type ResearchJobStatus = (typeof researchJobStatuses)[number]

export const researchPhaseCodes = [
  'VALIDATE',
  'CHECK_QUOTA',
  'COLLECT_MARKET_AND_PRODUCTS',
  'COLLECT_KEYWORDS',
  'COLLECT_REVIEWS',
  'PREPARE_DATA',
  'RENDER_EXCEL',
  'VALIDATE_AND_PUBLISH',
] as const

export type ResearchPhaseCode = (typeof researchPhaseCodes)[number]

export interface ResearchJobCreateRequest {
  reportName: string
  keyword: string
  seedAsins?: string[]
}

export interface ResearchJobCreated {
  jobId: string
  status: ResearchJobStatus
  dataSourceMode: string
}

export interface ResearchJobDetail {
  jobId: string
  reportName: string
  marketplace: string
  keyword: string
  dataSourceMode: string
  status: ResearchJobStatus
  currentPhase: ResearchPhaseCode | string | null
  currentPhaseName: string | null
  progress: number
  batchJobExecutionId: number | null
  errorCode: string | null
  errorMessage: string | null
  startedAt: number | null
  finishedAt: number | null
  createdAt: number
  downloadable: boolean
  fileName: string | null
}
