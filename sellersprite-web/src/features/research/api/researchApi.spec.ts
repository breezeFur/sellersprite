import { describe, expect, it, vi } from 'vitest'

import type { ApiRequestConfig, RawApiClient } from '@/shared/api/http'

import type { ResearchJobCreateRequest, ResearchJobDetail } from '../model/research'
import { createResearchJob, downloadResearchReport, getResearchJob } from './researchApi'

function createClient() {
  const request = vi.fn<(config: ApiRequestConfig) => Promise<unknown>>()
  const requestRaw = vi.fn<(config: ApiRequestConfig) => Promise<unknown>>()
  return {
    client: { request, requestRaw } as unknown as RawApiClient,
    request,
    requestRaw,
  }
}

describe('researchApi', () => {
  it('creates a market research job with the typed request body', async () => {
    const { client, request } = createClient()
    const payload: ResearchJobCreateRequest = {
      reportName: '美容仪美国站市场调研',
      keyword: 'facial cleansing device',
      seedAsins: ['B07Z82895W'],
    }
    request.mockResolvedValue({ jobId: 'job-1', status: 'QUEUED', dataSourceMode: 'MOCK' })

    await createResearchJob(payload, client)

    expect(request).toHaveBeenCalledWith({
      method: 'POST',
      url: '/market-research/jobs',
      data: payload,
    })
  })

  it('loads one owned job with a safely encoded id', async () => {
    const { client, request } = createClient()
    request.mockResolvedValue({ jobId: 'job/1' } as ResearchJobDetail)

    await getResearchJob('job/1', client)

    expect(request).toHaveBeenCalledWith({
      method: 'GET',
      url: '/market-research/jobs/job%2F1',
    })
  })

  it('downloads the published workbook through the authenticated raw client', async () => {
    const { client, requestRaw } = createClient()
    const workbook = new Blob(['xlsx'])
    requestRaw.mockResolvedValue(workbook)

    await expect(downloadResearchReport('job-1', client)).resolves.toBe(workbook)
    expect(requestRaw).toHaveBeenCalledWith({
      method: 'GET',
      url: '/market-research/jobs/job-1/download',
      responseType: 'blob',
    })
  })
})
