import { AxiosHeaders, type AxiosAdapter, type InternalAxiosRequestConfig } from 'axios'
import { describe, expect, it, vi } from 'vitest'

import { ApiError } from './ApiError'
import { createApiClient, parseApiResult } from './http'
import type { ApiResult } from './types'

function response<T>(config: InternalAxiosRequestConfig, data: ApiResult<T>) {
  return Promise.resolve({
    data,
    status: 200,
    statusText: 'OK',
    headers: {},
    config,
  })
}

describe('parseApiResult', () => {
  it('returns data only for the unified success code', () => {
    expect(parseApiResult({ code: '00000', message: '操作成功', data: { id: '1' } })).toEqual({ id: '1' })
  })

  it('preserves business code, message and trackId', () => {
    expect(() =>
      parseApiResult({ code: 'D409', message: '资源仍被引用', data: null, trackId: 'track-1' }),
    ).toThrowError(
      expect.objectContaining({ code: 'D409', message: '资源仍被引用', trackId: 'track-1' }),
    )
  })
})

describe('createApiClient', () => {
  it('returns binary responses without applying the unified JSON envelope parser', async () => {
    const workbook = new Blob(['xlsx'], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })
    const adapter: AxiosAdapter = (config) => Promise.resolve({
      data: workbook,
      status: 200,
      statusText: 'OK',
      headers: {},
      config,
    })
    const client = createApiClient({ adapter })

    await expect(client.requestRaw<Blob>({
      url: '/market-research/jobs/job-1/download',
      responseType: 'blob',
    })).resolves.toBe(workbook)
  })

  it('preserves a business error returned as a JSON blob by a download endpoint', async () => {
    const errorBlob = new Blob([
      JSON.stringify({
        code: 'R409',
        message: '市场调研报告尚未生成',
        data: null,
        trackId: 'track-download-1',
      }),
    ], { type: 'application/json' })
    const adapter: AxiosAdapter = (config) => Promise.resolve({
      data: errorBlob,
      status: 200,
      statusText: 'OK',
      headers: { 'content-type': 'application/json' },
      config,
    })
    const client = createApiClient({ adapter })

    await expect(client.requestRaw<Blob>({
      url: '/market-research/jobs/job-1/download',
      responseType: 'blob',
    })).rejects.toMatchObject({
      code: 'R409',
      message: '市场调研报告尚未生成',
      trackId: 'track-download-1',
    })
  })

  it('uses one refresh for three concurrent A401 responses and replays each request once', async () => {
    let accessToken = 'expired-token'
    let refreshCount = 0
    const calls = new Map<string, number>()
    const adapter: AxiosAdapter = (config) => {
      const url = config.url ?? ''
      calls.set(url, (calls.get(url) ?? 0) + 1)
      const authorization = AxiosHeaders.from(config.headers).get('Authorization')
      if (authorization !== 'Bearer fresh-token') {
        return response(config, { code: 'A401', message: '会话已过期', data: null })
      }
      return response(config, { code: '00000', message: '操作成功', data: url })
    }
    const client = createApiClient({
      adapter,
      authHooks: {
        getAccessToken: () => accessToken,
        refreshSession: async () => {
          refreshCount += 1
          await Promise.resolve()
          accessToken = 'fresh-token'
          return accessToken
        },
        onUnauthorized: vi.fn(),
      },
    })

    await expect(
      Promise.all([client.request<string>({ url: '/one' }), client.request<string>({ url: '/two' }), client.request<string>({ url: '/three' })]),
    ).resolves.toEqual(['/one', '/two', '/three'])
    expect(refreshCount).toBe(1)
    expect([...calls.values()]).toEqual([2, 2, 2])
  })

  it('ends the session when refresh fails and rejects all waiting requests', async () => {
    const onUnauthorized = vi.fn()
    const adapter: AxiosAdapter = (config) =>
      response(config, { code: 'A401', message: '会话已过期', data: null })
    const client = createApiClient({
      adapter,
      authHooks: {
        getAccessToken: () => 'expired-token',
        refreshSession: () => Promise.reject(new ApiError('A401', '刷新失败')),
        onUnauthorized,
      },
    })

    const results = await Promise.allSettled([
      client.request({ url: '/one' }),
      client.request({ url: '/two' }),
    ])

    expect(results.every((result) => result.status === 'rejected')).toBe(true)
    expect(onUnauthorized).toHaveBeenCalledTimes(1)
  })

  it('never refreshes or replays the same request twice', async () => {
    const onUnauthorized = vi.fn()
    let requestCount = 0
    const adapter: AxiosAdapter = (config) => {
      requestCount += 1
      return response(config, { code: 'A401', message: '会话已过期', data: null })
    }
    const client = createApiClient({
      adapter,
      authHooks: {
        getAccessToken: () => 'expired-token',
        refreshSession: () => Promise.resolve('fresh-token'),
        onUnauthorized,
      },
    })

    await expect(client.request({ url: '/still-unauthorized' })).rejects.toMatchObject({ code: 'A401' })
    expect(requestCount).toBe(2)
    expect(onUnauthorized).toHaveBeenCalledTimes(1)
  })
})
