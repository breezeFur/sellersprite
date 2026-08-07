import { afterEach, describe, expect, it, vi } from 'vitest'

import type { ApiRequestConfig, RawApiClient } from '@/shared/api/http'

import {
  cancelResearchAnalysis,
  continueResearchAnalysis,
  downloadResearchArtifact,
  listResearchAnalysisRuns,
  retryResearchAnalysis,
  sendResearchMessage,
  streamResearchEvents,
} from './researchStreamApi'

function createClient() {
  const request = vi.fn<(config: ApiRequestConfig) => Promise<unknown>>()
  const requestRaw = vi.fn<(config: ApiRequestConfig) => Promise<unknown>>()
  return {
    client: { request, requestRaw } as unknown as RawApiClient,
    request,
    requestRaw,
  }
}

describe('researchStreamApi', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('opens one resumable stream and parses snapshot followed by aggregate events', async () => {
    const snapshot = {
      frameType: 'snapshot',
      jobId: 'job/1',
      afterSequence: 18,
      lastSequence: 19,
      replayComplete: true,
      job: { jobId: 'job/1', status: 'RUNNING' },
      nodes: [{
        executionId: 'execution-1',
        graphCode: 'collection',
        nodeCode: 'collection.validate',
        status: 'SUCCEEDED',
      }],
      events: [event(19, 'job/1', 'research_node_completed')],
    }
    const events = {
      frameType: 'events',
      jobId: 'job/1',
      afterSequence: 19,
      lastSequence: 21,
      replayComplete: true,
      job: { jobId: 'job/1', status: 'SUCCEEDED' },
      nodes: [],
      events: [
        event(21, 'job/1', 'workflow_completed'),
        event(20, 'job/1', 'workbook_ready'),
      ],
    }
    const responseBody = [
      sseFrame('snapshot', 19, snapshot),
      sseFrame('events', 21, events),
    ].join('')
    const fetchMock = vi.fn().mockResolvedValue(new Response(responseBody, {
      status: 200,
      headers: { 'Content-Type': 'text/event-stream' },
    }))
    vi.stubGlobal('fetch', fetchMock)
    const onOpen = vi.fn()
    const onFrame = vi.fn()

    await streamResearchEvents('job/1', 18, {
      signal: new AbortController().signal,
      getAccessToken: () => 'token',
      refreshAccessToken: vi.fn(),
      onOpen,
      onFrame,
    })

    expect(fetchMock.mock.calls[0][0]).toBe('/api/market-research/jobs/job%2F1/stream?afterSequence=18')
    expect(new Headers(fetchMock.mock.calls[0][1].headers).get('Last-Event-ID')).toBe('18')
    expect(onOpen).toHaveBeenCalledTimes(1)
    expect(onFrame).toHaveBeenNthCalledWith(1, expect.objectContaining({
      frameType: 'snapshot',
      lastSequence: 19,
      events: [expect.objectContaining({ sequenceNo: 19 })],
    }))
    expect(onFrame).toHaveBeenNthCalledWith(2, expect.objectContaining({
      frameType: 'events',
      lastSequence: 21,
      events: [
        expect.objectContaining({ sequenceNo: 21 }),
        expect.objectContaining({ sequenceNo: 20 }),
      ],
    }))
  })

  it('rejects an events frame before the required snapshot', async () => {
    const responseBody = sseFrame('events', 1, {
      frameType: 'events',
      jobId: 'job-1',
      afterSequence: 0,
      lastSequence: 1,
      replayComplete: true,
      events: [event(1, 'job-1', 'research_started')],
    })
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(responseBody, {
      status: 200,
      headers: { 'Content-Type': 'text/event-stream' },
    })))

    await expect(streamResearchEvents('job-1', 0, {
      signal: new AbortController().signal,
      getAccessToken: () => 'token',
      refreshAccessToken: vi.fn(),
      onFrame: vi.fn(),
    })).rejects.toMatchObject({ code: 'SSE_INVALID_FRAME' })
  })

  it('rejects a snapshot whose job does not match the subscribed job', async () => {
    const responseBody = sseFrame('snapshot', 0, {
      frameType: 'snapshot',
      jobId: 'another-job',
      afterSequence: 0,
      lastSequence: 0,
      replayComplete: true,
      job: { jobId: 'another-job' },
      nodes: [],
      events: [],
    })
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(responseBody, {
      status: 200,
      headers: { 'Content-Type': 'text/event-stream' },
    })))

    await expect(streamResearchEvents('job-1', 0, {
      signal: new AbortController().signal,
      getAccessToken: () => 'token',
      refreshAccessToken: vi.fn(),
      onFrame: vi.fn(),
    })).rejects.toMatchObject({ code: 'SSE_INVALID_FRAME' })
  })

  it('loads analysis history and sends follow-up, continue, retry and cancel commands', async () => {
    const { client, request } = createClient()
    request.mockResolvedValue({ analysisRunId: 'run-2' })

    await listResearchAnalysisRuns('job/1', client)
    await sendResearchMessage('job/1', '只看退货原因', client)
    await continueResearchAnalysis('run/1', client)
    await retryResearchAnalysis('run/1', client)
    await cancelResearchAnalysis('run/1', client)

    expect(request).toHaveBeenNthCalledWith(1, {
      method: 'GET',
      url: '/market-research/jobs/job%2F1/analyses',
    })
    expect(request).toHaveBeenNthCalledWith(2, {
      method: 'POST',
      url: '/market-research/jobs/job%2F1/messages',
      data: { content: '只看退货原因' },
    })
    expect(request).toHaveBeenNthCalledWith(3, {
      method: 'POST',
      url: '/market-research/analysis-runs/run%2F1/continue',
    })
    expect(request).toHaveBeenNthCalledWith(4, {
      method: 'POST',
      url: '/market-research/analysis-runs/run%2F1/retry',
    })
    expect(request).toHaveBeenNthCalledWith(5, {
      method: 'POST',
      url: '/market-research/analysis-runs/run%2F1/cancel',
    })
  })

  it('downloads an analysis artifact through the authenticated raw client', async () => {
    const { client, requestRaw } = createClient()
    requestRaw.mockResolvedValue(new Blob(['markdown']))

    await downloadResearchArtifact('job/1', { artifactId: 'artifact/1' }, client)

    expect(requestRaw).toHaveBeenCalledWith({
      method: 'GET',
      url: '/market-research/jobs/job%2F1/artifacts/artifact%2F1/download',
      responseType: 'blob',
      timeout: 0,
    })
  })
})

function event(sequenceNo: number, jobId: string, eventType: string) {
  return {
    sequenceNo,
    eventId: `event-${sequenceNo}`,
    jobId,
    scope: eventType === 'workflow_completed' ? 'workflow' : 'research',
    eventType,
    message: eventType,
    terminal: eventType === 'workflow_completed',
  }
}

function sseFrame(eventName: 'snapshot' | 'events', id: number, data: object) {
  return `id: ${id}\nevent: ${eventName}\ndata: ${JSON.stringify(data)}\n\n`
}
