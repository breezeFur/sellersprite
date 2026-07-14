import { apiClient, type ApiClient, type RawApiClient } from '@/shared/api/http'

import type {
  ResearchJobCreateRequest,
  ResearchJobCreated,
  ResearchJobDetail,
} from '../model/research'

const RESEARCH_JOBS_PATH = '/market-research/jobs'

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

export function getResearchJob(jobId: string, client: ApiClient = apiClient) {
  return client.request<ResearchJobDetail>({
    method: 'GET',
    url: `${RESEARCH_JOBS_PATH}/${encodeURIComponent(jobId)}`,
  })
}

export function downloadResearchReport(jobId: string, client: RawApiClient = apiClient) {
  return client.requestRaw<Blob>({
    method: 'GET',
    url: `${RESEARCH_JOBS_PATH}/${encodeURIComponent(jobId)}/download`,
    responseType: 'blob',
  })
}
