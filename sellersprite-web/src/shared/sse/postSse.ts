import { ApiError } from '@/shared/api/ApiError'
import type { ApiResult } from '@/shared/api/types'

import { readSseStream, type SseMessage } from './sseParser'

interface PostSseJsonOptions {
  url: string
  body?: unknown
  signal?: AbortSignal
  getAccessToken: () => string | null
  refreshAccessToken: () => Promise<string>
  onEvent: (message: SseMessage) => void
}

const UNAUTHORIZED_CODE = 'A401'

export async function postSseJson(options: PostSseJsonOptions) {
  let token = options.getAccessToken()

  for (let attempt = 0; attempt < 2; attempt += 1) {
    const response = await fetch(options.url, {
      method: 'POST',
      credentials: 'include',
      headers: requestHeaders(token),
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
      signal: options.signal,
    })

    if (response.status === 401 && attempt === 0) {
      token = await options.refreshAccessToken()
      continue
    }

    const contentType = response.headers.get('Content-Type')?.toLowerCase() ?? ''
    if (!response.ok || !contentType.includes('text/event-stream')) {
      const error = await responseError(response)
      if (error.code === UNAUTHORIZED_CODE && attempt === 0) {
        token = await options.refreshAccessToken()
        continue
      }
      throw error
    }

    if (!response.body) {
      throw new ApiError('SSE_EMPTY_BODY', '流式响应内容为空', { status: response.status })
    }

    await readSseStream(response.body, options.onEvent)
    return
  }

  throw new ApiError(UNAUTHORIZED_CODE, '会话已过期')
}

function requestHeaders(token: string | null) {
  const headers = new Headers({
    Accept: 'text/event-stream',
    'Content-Type': 'application/json',
  })
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  return headers
}

async function responseError(response: Response) {
  const fallback = new ApiError(
    response.status === 403 ? 'A403' : 'SSE_HTTP_ERROR',
    response.status === 403 ? '没有执行此操作的权限' : '流式请求失败，请稍后重试',
    { status: response.status },
  )
  try {
    const result = await response.json() as ApiResult<unknown>
    if (typeof result.code === 'string' && typeof result.message === 'string') {
      return new ApiError(result.code, result.message, {
        status: response.status,
        trackId: result.trackId,
      })
    }
  } catch {
    return fallback
  }
  return fallback
}
