import { describe, expect, it, vi } from 'vitest'

import type { ApiClient, ApiRequestConfig } from '@/shared/api/http'

import {
  createDefaultCollectionGraphConfig,
  type ResearchJobCreateRequest,
  type ResearchJobDetail,
} from '../model/research'
import {
  cancelResearchJob,
  createResearchJob,
  getResearchEvidencePage,
  getResearchCategoryNodes,
  getResearchJob,
  getResearchJobNodes,
  getResearchProductSelection,
  getResearchWorkflowTopology,
  listResearchEvidenceTables,
  pageResearchJobs,
  resolveResearchCategoriesByAsins,
  retryResearchJob,
  submitResearchProductSelection,
} from './researchApi'

function createClient() {
  const request = vi.fn<(config: ApiRequestConfig) => Promise<unknown>>()
  return {
    client: { request } as unknown as ApiClient,
    request,
  }
}

describe('researchApi', () => {
  it('creates a market research job with the typed request body', async () => {
    const { client, request } = createClient()
    const payload: ResearchJobCreateRequest = {
      reportName: '美容仪美国站市场调研',
      marketplace: 'US',
      nodeIdPath: '172282:281407',
      month: '2026-07',
      keyword: 'facial cleansing device',
      seedAsins: ['B07Z82895W'],
      collectionConfig: createDefaultCollectionGraphConfig(),
    }
    request.mockResolvedValue({ jobId: 'job-1', status: 'QUEUED', dataSourceMode: 'MOCK' })

    await createResearchJob(payload, client)

    expect(request).toHaveBeenCalledWith({
      method: 'POST',
      url: '/market-research/jobs',
      data: payload,
    })
  })

  it('queries child category nodes with the SellerSprite yyyyMM month', async () => {
    const { client, request } = createClient()
    request.mockResolvedValue([])

    await getResearchCategoryNodes({
      marketplace: 'JP',
      month: '2026-07',
      nodeIdPath: '  172282  ',
      keyword: '  dishwasher  ',
    }, client)

    expect(request).toHaveBeenCalledWith({
      method: 'GET',
      url: '/market-research/categories',
      params: {
        marketplace: 'JP',
        month: '202607',
        nodeIdPath: '172282',
        keyword: 'dishwasher',
      },
    })
  })

  it('omits empty category filters when querying root nodes', async () => {
    const { client, request } = createClient()
    request.mockResolvedValue([])

    await getResearchCategoryNodes({
      marketplace: 'US',
      month: '2026-07',
      nodeIdPath: '   ',
      keyword: '   ',
    }, client)

    expect(request).toHaveBeenCalledWith({
      method: 'GET',
      url: '/market-research/categories',
      params: {
        marketplace: 'US',
        month: '202607',
      },
    })
  })

  it('resolves categories by asins with normalized payload and month', async () => {
    const { client, request } = createClient()
    request.mockResolvedValue([])

    await resolveResearchCategoriesByAsins({
      marketplace: 'US',
      asins: ['B08GHW4TBS', 'B08GHW4TBC'],
      month: '2026-07',
    }, client)

    expect(request).toHaveBeenCalledWith({
      method: 'POST',
      url: '/market-research/categories/resolve-by-asins',
      data: {
        marketplace: 'US',
        asins: ['B08GHW4TBS', 'B08GHW4TBC'],
        month: '202607',
      },
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

  it('pages owned research jobs with trimmed search and explicit filters', async () => {
    const { client, request } = createClient()
    request.mockResolvedValue({ current: 2, size: 50, total: 0, records: [] })

    await pageResearchJobs({
      current: 2,
      size: 50,
      keyword: '  facial device  ',
      status: 'SUCCEEDED',
      marketplace: 'US',
      month: '2026-07',
    }, client)

    expect(request).toHaveBeenCalledWith({
      method: 'GET',
      url: '/market-research/jobs',
      params: {
        current: 2,
        size: 50,
        keyword: 'facial device',
        status: 'SUCCEEDED',
        marketplace: 'US',
        month: '2026-07',
      },
    })
  })

  it('loads node attempts and sends cancel and retry commands', async () => {
    const { client, request } = createClient()
    request.mockResolvedValue(undefined)

    await getResearchJobNodes('job/1', client)
    await cancelResearchJob('job/1', client)
    await retryResearchJob('job/1', client)

    expect(request).toHaveBeenNthCalledWith(1, {
      method: 'GET',
      url: '/market-research/jobs/job%2F1/nodes',
    })
    expect(request).toHaveBeenNthCalledWith(2, {
      method: 'POST',
      url: '/market-research/jobs/job%2F1/cancel',
    })
    expect(request).toHaveBeenNthCalledWith(3, {
      method: 'POST',
      url: '/market-research/jobs/job%2F1/retry',
    })
  })

  it('loads the compiled workflow topology', async () => {
    const { client, request } = createClient()
    request.mockResolvedValue({
      type: 'MERMAID',
      title: '市场调研工作流',
      content: 'flowchart TD',
    })

    await getResearchWorkflowTopology(client)

    expect(request).toHaveBeenCalledWith({
      method: 'GET',
      url: '/market-research/workflow',
    })
  })

  it('loads stage evidence metadata and one safely encoded evidence page', async () => {
    const { client, request } = createClient()
    request.mockResolvedValue([])

    await listResearchEvidenceTables('job/1', 'SCREENING', client)
    await getResearchEvidencePage('job/1', 'evidence.products/us', 2, 50, client)

    expect(request).toHaveBeenNthCalledWith(1, {
      method: 'GET',
      url: '/market-research/jobs/job%2F1/evidence',
      params: { stageCode: 'SCREENING' },
    })
    expect(request).toHaveBeenNthCalledWith(2, {
      method: 'GET',
      url: '/market-research/jobs/job%2F1/evidence/evidence.products%2Fus',
      params: { current: 2, size: 50 },
    })
  })

  it('loads and submits the product selection gate with a typed decision', async () => {
    const { client, request } = createClient()
    request.mockResolvedValue(undefined)

    await getResearchProductSelection('job/1', client)
    await submitResearchProductSelection('job/1', {
      decision: 'ENTER',
      selectedAsins: ['B012345678'],
    }, client)

    expect(request).toHaveBeenNthCalledWith(1, {
      method: 'GET',
      url: '/market-research/jobs/job%2F1/product-selection',
    })
    expect(request).toHaveBeenNthCalledWith(2, {
      method: 'POST',
      url: '/market-research/jobs/job%2F1/product-selection',
      data: {
        decision: 'ENTER',
        selectedAsins: ['B012345678'],
      },
    })
  })
})
