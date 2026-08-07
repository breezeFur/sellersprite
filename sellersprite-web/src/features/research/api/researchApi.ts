import { apiClient, type ApiClient } from '@/shared/api/http'
import type { PageResult } from '@/shared/api/types'

import type {
  ResearchCategoryNode,
  ResearchCategoryNodeQuery,
  ResearchEvidencePage,
  ResearchEvidenceTableSummary,
  ResearchJobCreateRequest,
  ResearchJobCreated,
  ResearchJobDetail,
  ResearchJobHistory,
  ResearchJobHistoryQuery,
  ResearchNodeExecution,
  ResearchProductSelection,
  ResearchProductSelectionRequest,
  ResearchStageCode,
  ResearchWorkflowTopology,
} from '../model/research'

const RESEARCH_JOBS_PATH = '/market-research/jobs'
const RESEARCH_WORKFLOW_PATH = '/market-research/workflow'
const RESEARCH_CATEGORIES_PATH = '/market-research/categories'

export function getResearchCategoryNodes(
  query: ResearchCategoryNodeQuery,
  client: ApiClient = apiClient,
) {
  const nodeIdPath = query.nodeIdPath?.trim()
  const keyword = query.keyword?.trim()
  return client.request<ResearchCategoryNode[]>({
    method: 'GET',
    url: RESEARCH_CATEGORIES_PATH,
    params: {
      marketplace: query.marketplace,
      month: toSellerSpriteMonth(query.month),
      ...(nodeIdPath ? { nodeIdPath } : {}),
      ...(keyword ? { keyword } : {}),
    },
  })
}

export function createResearchJob(
  data: ResearchJobCreateRequest,
  client: ApiClient = apiClient,
) {
  return client.request<ResearchJobCreated>({
    method: 'POST',
    url: RESEARCH_JOBS_PATH,
    data,
  })
}

export function pageResearchJobs(
  query: ResearchJobHistoryQuery,
  client: ApiClient = apiClient,
) {
  const keyword = query.keyword?.trim()
  return client.request<PageResult<ResearchJobHistory>>({
    method: 'GET',
    url: RESEARCH_JOBS_PATH,
    params: {
      current: query.current,
      size: query.size,
      ...(keyword ? { keyword } : {}),
      ...(query.status ? { status: query.status } : {}),
      ...(query.marketplace ? { marketplace: query.marketplace } : {}),
      ...(query.month ? { month: query.month } : {}),
    },
  })
}

export function getResearchJob(jobId: string, client: ApiClient = apiClient) {
  return client.request<ResearchJobDetail>({
    method: 'GET',
    url: `${RESEARCH_JOBS_PATH}/${encodeURIComponent(jobId)}`,
  })
}

export function getResearchJobNodes(jobId: string, client: ApiClient = apiClient) {
  return client.request<ResearchNodeExecution[]>({
    method: 'GET',
    url: `${RESEARCH_JOBS_PATH}/${encodeURIComponent(jobId)}/nodes`,
  })
}

export function listResearchEvidenceTables(
  jobId: string,
  stageCode: ResearchStageCode | string,
  client: ApiClient = apiClient,
) {
  return client.request<ResearchEvidenceTableSummary[]>({
    method: 'GET',
    url: `${RESEARCH_JOBS_PATH}/${encodeURIComponent(jobId)}/evidence`,
    params: { stageCode },
  })
}

export function getResearchEvidencePage(
  jobId: string,
  datasetCode: string,
  current: number,
  size: number,
  client: ApiClient = apiClient,
) {
  return client.request<ResearchEvidencePage>({
    method: 'GET',
    url: `${RESEARCH_JOBS_PATH}/${encodeURIComponent(jobId)}`
      + `/evidence/${encodeURIComponent(datasetCode)}`,
    params: { current, size },
  })
}

export function getResearchProductSelection(
  jobId: string,
  client: ApiClient = apiClient,
) {
  return client.request<ResearchProductSelection>({
    method: 'GET',
    url: `${RESEARCH_JOBS_PATH}/${encodeURIComponent(jobId)}/product-selection`,
  })
}

export function submitResearchProductSelection(
  jobId: string,
  data: ResearchProductSelectionRequest,
  client: ApiClient = apiClient,
) {
  return client.request<ResearchProductSelection | undefined>({
    method: 'POST',
    url: `${RESEARCH_JOBS_PATH}/${encodeURIComponent(jobId)}/product-selection`,
    data,
  })
}

export function cancelResearchJob(jobId: string, client: ApiClient = apiClient) {
  return client.request<void>({
    method: 'POST',
    url: `${RESEARCH_JOBS_PATH}/${encodeURIComponent(jobId)}/cancel`,
  })
}

export function retryResearchJob(jobId: string, client: ApiClient = apiClient) {
  return client.request<void>({
    method: 'POST',
    url: `${RESEARCH_JOBS_PATH}/${encodeURIComponent(jobId)}/retry`,
  })
}

export function getResearchWorkflowTopology(client: ApiClient = apiClient) {
  return client.request<ResearchWorkflowTopology>({
    method: 'GET',
    url: RESEARCH_WORKFLOW_PATH,
  })
}

export function toSellerSpriteMonth(month: string) {
  if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(month)) {
    throw new Error('月份必须为 yyyy-MM 格式')
  }
  return month.replace('-', '')
}
