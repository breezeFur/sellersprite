import { ApiError } from '@/shared/api/ApiError'
import { apiClient, type ApiClient, type RawApiClient } from '@/shared/api/http'
import { fetchSse } from '@/shared/sse/postSse'

import type {
  ResearchAnalysisRun,
  ResearchJobDetail,
  ResearchNodeExecution,
} from '../model/research'
import type {
  ResearchReportDownload,
  ResearchStreamFrame,
  ResearchStreamEvent,
} from '../model/researchStream'

const RESEARCH_JOBS_PATH = '/market-research/jobs'
const ANALYSIS_RUNS_PATH = '/market-research/analysis-runs'

export interface ResearchStreamOptions {
  signal: AbortSignal
  getAccessToken: () => string | null
  refreshAccessToken: () => Promise<string>
  onOpen?: () => void
  onFrame: (frame: ResearchStreamFrame) => void
}

export function streamResearchEvents(
  jobId: string,
  afterSequence: number,
  options: ResearchStreamOptions,
) {
  const encodedJobId = encodeURIComponent(jobId)
  const query = new URLSearchParams({ afterSequence: `${afterSequence}` })
  let firstFrameReceived = false
  return fetchSse({
    url: `/api${RESEARCH_JOBS_PATH}/${encodedJobId}/stream?${query.toString()}`,
    method: 'GET',
    signal: options.signal,
    lastEventId: afterSequence,
    getAccessToken: options.getAccessToken,
    refreshAccessToken: options.refreshAccessToken,
    onOpen: options.onOpen,
    onEvent(message) {
      if (message.event === 'heartbeat') return
      try {
        if ((!firstFrameReceived && message.event !== 'snapshot')
          || (firstFrameReceived && message.event === 'snapshot')) {
          throw invalidFrame('事件流首帧必须是唯一的snapshot帧')
        }
        const frame = parseFrame(JSON.parse(message.data), message.event, jobId)
        firstFrameReceived = true
        options.onFrame(frame)
      } catch (error) {
        if (error instanceof ApiError) throw error
        throw new ApiError('SSE_INVALID_FRAME', '市场调研事件帧格式不正确', { cause: error })
      }
    },
  })
}

export function sendResearchMessage(
  jobId: string,
  content: string,
  client: ApiClient = apiClient,
) {
  return client.request<ResearchAnalysisRun>({
    method: 'POST',
    url: `${RESEARCH_JOBS_PATH}/${encodeURIComponent(jobId)}/messages`,
    data: { content },
  })
}

export function listResearchAnalysisRuns(
  jobId: string,
  client: ApiClient = apiClient,
) {
  return client.request<ResearchAnalysisRun[]>({
    method: 'GET',
    url: `${RESEARCH_JOBS_PATH}/${encodeURIComponent(jobId)}/analyses`,
  })
}

export function retryResearchAnalysis(
    analysisRunId: string,
    client: ApiClient = apiClient,
) {
  return client.request<ResearchAnalysisRun>({
    method: 'POST',
    url: `${ANALYSIS_RUNS_PATH}/${encodeURIComponent(analysisRunId)}/retry`,
    })
}

export function continueResearchAnalysis(
  analysisRunId: string,
  client: ApiClient = apiClient,
) {
  return client.request<ResearchAnalysisRun>({
    method: 'POST',
    url: `${ANALYSIS_RUNS_PATH}/${encodeURIComponent(analysisRunId)}/continue`,
  })
}

export function cancelResearchAnalysis(
  analysisRunId: string,
  client: ApiClient = apiClient,
) {
  return client.request<void>({
    method: 'POST',
    url: `${ANALYSIS_RUNS_PATH}/${encodeURIComponent(analysisRunId)}/cancel`,
  })
}

export function downloadResearchArtifact(
  jobId: string,
  artifact: Pick<ResearchReportDownload, 'artifactId'>,
  client: RawApiClient = apiClient,
) {
  return client.requestRaw<Blob>({
    method: 'GET',
    url: `${RESEARCH_JOBS_PATH}/${encodeURIComponent(jobId)}/artifacts/${encodeURIComponent(artifact.artifactId)}/download`,
    responseType: 'blob',
    timeout: 0,
  })
}

function parseFrame(value: unknown, sseEventName: string, expectedJobId: string): ResearchStreamFrame {
  if (!isRecord(value) || (sseEventName !== 'snapshot' && sseEventName !== 'events')) {
    throw invalidFrame('SSE事件名称必须是snapshot或events')
  }
  if (value.frameType !== sseEventName) {
    throw invalidFrame('SSE事件名称与frameType不一致')
  }
  if (value.jobId !== expectedJobId) {
    throw invalidFrame('SSE事件帧任务标识不匹配')
  }

  const afterSequence = nonNegativeInteger(value.afterSequence)
  const lastSequence = nonNegativeInteger(value.lastSequence)
  if (afterSequence === null || lastSequence === null || lastSequence < afterSequence) {
    throw invalidFrame('SSE事件帧序号不正确')
  }
  if (typeof value.replayComplete !== 'boolean' || !Array.isArray(value.events)) {
    throw invalidFrame('SSE事件帧缺少回放状态或事件数组')
  }

  const events = value.events.map((event) => parseEvent(event, expectedJobId))
  const base = {
    jobId: expectedJobId,
    afterSequence,
    lastSequence,
    replayComplete: value.replayComplete,
    events,
  }

  if (value.frameType === 'snapshot') {
    if (!isRecord(value.job) || value.job.jobId !== expectedJobId || !Array.isArray(value.nodes)) {
      throw invalidFrame('snapshot帧必须包含当前任务和节点状态')
    }
    return {
      ...base,
      frameType: 'snapshot',
      job: value.job as unknown as ResearchJobDetail,
      nodes: value.nodes as ResearchNodeExecution[],
    }
  }

  if (value.job !== undefined && (!isRecord(value.job) || value.job.jobId !== expectedJobId)) {
    throw invalidFrame('events帧任务状态不正确')
  }
  if (value.nodes !== undefined && !Array.isArray(value.nodes)) {
    throw invalidFrame('events帧节点状态不正确')
  }
  return {
    ...base,
    frameType: 'events',
    ...(value.job === undefined ? {} : { job: value.job as unknown as ResearchJobDetail }),
    ...(value.nodes === undefined ? {} : { nodes: value.nodes as ResearchNodeExecution[] }),
  }
}

function parseEvent(value: unknown, expectedJobId: string): ResearchStreamEvent {
  if (!isRecord(value)) throw invalidFrame('事件数组包含非法事件')
  const sequenceNo = positiveInteger(value.sequenceNo)
  if (
    sequenceNo === null
    || value.jobId !== expectedJobId
    || typeof value.eventType !== 'string'
    || !value.eventType
    || typeof value.message !== 'string'
  ) {
    throw invalidFrame('事件数组包含缺失必要字段的事件')
  }
  return {
    ...value,
    sequenceNo,
    jobId: expectedJobId,
    eventType: value.eventType,
    scope: typeof value.scope === 'string' ? value.scope : 'research',
    message: value.message,
  } as ResearchStreamEvent
}

function positiveInteger(value: unknown) {
  return Number.isSafeInteger(value) && Number(value) > 0 ? Number(value) : null
}

function nonNegativeInteger(value: unknown) {
  return Number.isSafeInteger(value) && Number(value) >= 0 ? Number(value) : null
}

function invalidFrame(message: string) {
  return new ApiError('SSE_INVALID_FRAME', message)
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}
