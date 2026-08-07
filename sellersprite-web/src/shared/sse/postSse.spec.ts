import { afterEach, describe, expect, it, vi } from 'vitest'

import { fetchSse, postSseJson } from './postSse'

function sseResponse(content: string) {
  return new Response(content, {
    status: 200,
    headers: { 'Content-Type': 'text/event-stream;charset=UTF-8' },
  })
}

describe('postSseJson', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('sends JSON with bearer credentials and forwards parsed events', async () => {
    const fetchMock = vi.fn().mockResolvedValue(sseResponse('event: delta\ndata: {"content":"ok"}\n\n'))
    vi.stubGlobal('fetch', fetchMock)
    const onEvent = vi.fn()

    await postSseJson({
      url: '/api/ai/chat/stream',
      body: { prompt: 'hello' },
      getAccessToken: () => 'access-token',
      refreshAccessToken: vi.fn(),
      onEvent,
    })

    const [, request] = fetchMock.mock.calls[0]
    expect(request.credentials).toBe('include')
    expect(new Headers(request.headers).get('Authorization')).toBe('Bearer access-token')
    expect(onEvent).toHaveBeenCalledWith({ event: 'delta', data: '{"content":"ok"}' })
  })

  it('refreshes once after 401 and replays with the new token', async () => {
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(new Response(null, { status: 401 }))
      .mockResolvedValueOnce(sseResponse('event: done\ndata: {"chat":{}}\n\n'))
    vi.stubGlobal('fetch', fetchMock)
    const refreshAccessToken = vi.fn().mockResolvedValue('new-token')

    await postSseJson({
      url: '/api/ai/chat/stream',
      body: { prompt: 'hello' },
      getAccessToken: () => 'old-token',
      refreshAccessToken,
      onEvent: vi.fn(),
    })

    expect(refreshAccessToken).toHaveBeenCalledTimes(1)
    expect(fetchMock).toHaveBeenCalledTimes(2)
    expect(new Headers(fetchMock.mock.calls[1][1].headers).get('Authorization')).toBe('Bearer new-token')
  })

  it('refreshes before opening the stream when no access token is available', async () => {
    const fetchMock = vi.fn().mockResolvedValue(sseResponse('event: done\ndata: {}\n\n'))
    vi.stubGlobal('fetch', fetchMock)
    const refreshAccessToken = vi.fn().mockResolvedValue('restored-token')

    await fetchSse({
      url: '/api/market-research/jobs/job-1/stream',
      getAccessToken: () => null,
      refreshAccessToken,
      onEvent: vi.fn(),
    })

    expect(refreshAccessToken).toHaveBeenCalledTimes(1)
    expect(fetchMock).toHaveBeenCalledTimes(1)
    expect(new Headers(fetchMock.mock.calls[0][1].headers).get('Authorization'))
      .toBe('Bearer restored-token')
  })

  it('opens an authenticated GET stream with Last-Event-ID and no JSON content type', async () => {
    const fetchMock = vi.fn().mockResolvedValue(sseResponse('id: 9\nevent: done\ndata: {}\n\n'))
    vi.stubGlobal('fetch', fetchMock)
    const onOpen = vi.fn()

    await fetchSse({
      url: '/api/market-research/jobs/job-1/stream?afterSequence=9',
      lastEventId: 9,
      getAccessToken: () => 'access-token',
      refreshAccessToken: vi.fn(),
      onOpen,
      onEvent: vi.fn(),
    })

    const [, request] = fetchMock.mock.calls[0]
    const headers = new Headers(request.headers)
    expect(request.method).toBe('GET')
    expect(headers.get('Last-Event-ID')).toBe('9')
    expect(headers.get('Content-Type')).toBeNull()
    expect(onOpen).toHaveBeenCalledTimes(1)
  })
})
