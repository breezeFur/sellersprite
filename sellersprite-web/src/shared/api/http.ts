import axios, {
  AxiosError,
  AxiosHeaders,
  type AxiosAdapter,
  type AxiosRequestConfig,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios'

import { ApiError } from './ApiError'
import type { ApiResult } from './types'

const SUCCESS_CODE = '00000'
const UNAUTHORIZED_CODE = 'A401'
const DEFAULT_TIMEOUT_MS = 15_000

export interface HttpAuthHooks {
  getAccessToken: () => string | null
  refreshSession: () => Promise<string>
  onUnauthorized: () => void
}

export type ApiRequestConfig = AxiosRequestConfig & {
  skipAuthHeader?: boolean
  skipAuthRefresh?: boolean
}

type RetryableRequestConfig = InternalAxiosRequestConfig & {
  authRetried?: boolean
  rawResponse?: true
  skipAuthHeader?: boolean
  skipAuthRefresh?: boolean
}

export interface ApiClient {
  request<T>(config: ApiRequestConfig): Promise<T>
}

export interface RawApiClient extends ApiClient {
  requestRaw<T>(config: ApiRequestConfig): Promise<T>
}

interface ApiClientOptions {
  adapter?: AxiosAdapter
  authHooks?: HttpAuthHooks
  baseURL?: string
}

const emptyAuthHooks: HttpAuthHooks = {
  getAccessToken: () => null,
  refreshSession: () => Promise.reject(new ApiError(UNAUTHORIZED_CODE, '会话已过期')),
  onUnauthorized: () => undefined,
}

let defaultAuthHooks = emptyAuthHooks

export function configureHttpAuth(hooks: HttpAuthHooks) {
  defaultAuthHooks = hooks
}

export function parseApiResult<T>(result: ApiResult<T>): T {
  if (result.code !== SUCCESS_CODE) {
    throw new ApiError(result.code, result.message, { trackId: result.trackId })
  }
  return result.data
}

export function createApiClient(options: ApiClientOptions = {}): RawApiClient {
  const authHooks = options.authHooks ?? emptyAuthHooks
  const instance = axios.create({
    baseURL: options.baseURL ?? '/api',
    timeout: DEFAULT_TIMEOUT_MS,
    withCredentials: true,
    adapter: options.adapter,
  })
  let refreshPromise: Promise<string> | null = null
  let unauthorizedNotified = false

  function notifyUnauthorizedOnce() {
    if (unauthorizedNotified) {
      return
    }
    unauthorizedNotified = true
    authHooks.onUnauthorized()
  }

  function refreshAccessToken() {
    if (refreshPromise) {
      return refreshPromise
    }
    unauthorizedNotified = false
    refreshPromise = authHooks
      .refreshSession()
      .then((accessToken) => {
        if (!accessToken) {
          throw new ApiError(UNAUTHORIZED_CODE, '会话已过期')
        }
        return accessToken
      })
      .catch((error: unknown) => {
        notifyUnauthorizedOnce()
        throw normalizeError(error)
      })
      .finally(() => {
        refreshPromise = null
      })
    return refreshPromise
  }

  async function replayAfterRefresh(
    config: RetryableRequestConfig,
    unauthorizedError: ApiError,
  ): Promise<AxiosResponse> {
    if (config.skipAuthRefresh) {
      throw unauthorizedError
    }
    if (config.authRetried) {
      notifyUnauthorizedOnce()
      throw unauthorizedError
    }

    const accessToken = await refreshAccessToken()
    config.authRetried = true
    config.headers = AxiosHeaders.from(config.headers)
    config.headers.set('Authorization', `Bearer ${accessToken}`)
    return instance.request(config)
  }

  instance.interceptors.request.use((config) => {
    const requestConfig = config as RetryableRequestConfig
    const accessToken = authHooks.getAccessToken()
    if (!requestConfig.skipAuthHeader && accessToken) {
      requestConfig.headers = AxiosHeaders.from(requestConfig.headers)
      requestConfig.headers.set('Authorization', `Bearer ${accessToken}`)
    }
    return requestConfig
  })

  instance.interceptors.response.use(
    (response) => {
      if ((response.config as RetryableRequestConfig).rawResponse) {
        unauthorizedNotified = false
        return response
      }
      const result = asApiResult(response.data)
      if (!result) {
        throw new ApiError('INVALID_RESPONSE', '服务端响应格式不正确', { status: response.status })
      }
      if (result.code === SUCCESS_CODE) {
        unauthorizedNotified = false
        return response
      }
      const apiError = new ApiError(result.code, result.message, {
        trackId: result.trackId,
        status: response.status,
      })
      if (result.code === UNAUTHORIZED_CODE) {
        return replayAfterRefresh(response.config as RetryableRequestConfig, apiError)
      }
      throw apiError
    },
    (error: unknown) => {
      if (error instanceof AxiosError && error.response?.status === 401 && error.config) {
        return replayAfterRefresh(
          error.config as RetryableRequestConfig,
          new ApiError(UNAUTHORIZED_CODE, '会话已过期', { status: 401, cause: error }),
        )
      }
      throw normalizeError(error)
    },
  )

  return {
    async request<T>(config: ApiRequestConfig) {
      const response = await instance.request<ApiResult<T>>(config)
      return parseApiResult(response.data)
    },
    async requestRaw<T>(config: ApiRequestConfig) {
      const rawConfig: ApiRequestConfig & { rawResponse: true } = {
        ...config,
        rawResponse: true,
      }
      const response = await instance.request<T>(rawConfig)
      const rawApiResult = await parseRawApiResult(response.data, responseContentType(response))
      if (rawApiResult) {
        if (rawApiResult.code !== SUCCESS_CODE) {
          throw new ApiError(rawApiResult.code, rawApiResult.message, {
            trackId: rawApiResult.trackId,
            status: response.status,
          })
        }
        throw new ApiError('INVALID_RESPONSE', '服务端未返回文件内容', {
          status: response.status,
        })
      }
      return response.data
    },
  }
}

function asApiResult(value: unknown): ApiResult<unknown> | null {
  if (!value || typeof value !== 'object') {
    return null
  }
  const candidate = value as Record<string, unknown>
  if (typeof candidate.code !== 'string' || typeof candidate.message !== 'string') {
    return null
  }
  return candidate as unknown as ApiResult<unknown>
}

async function parseRawApiResult(value: unknown, contentType: string) {
  if (!(value instanceof Blob)) {
    return asApiResult(value)
  }
  const responseType = value.type || contentType
  if (!responseType.toLowerCase().includes('json')) {
    return null
  }
  try {
    return asApiResult(JSON.parse(await value.text()))
  } catch {
    return null
  }
}

function responseContentType(response: AxiosResponse) {
  const headerValue = response.headers['content-type']
  return typeof headerValue === 'string' ? headerValue : ''
}

function normalizeError(error: unknown): ApiError {
  if (error instanceof ApiError) {
    return error
  }
  if (error instanceof AxiosError) {
    if (error.code === AxiosError.ERR_CANCELED) {
      return new ApiError('REQUEST_CANCELLED', '请求已取消', { cause: error })
    }
    if (!error.response) {
      return new ApiError('NETWORK_ERROR', '网络连接失败，请稍后重试', { cause: error })
    }
    return new ApiError('HTTP_ERROR', '服务暂时不可用，请稍后重试', {
      status: error.response.status,
      cause: error,
    })
  }
  return new ApiError('UNKNOWN_ERROR', '请求失败，请稍后重试', { cause: error })
}

const delegatedAuthHooks: HttpAuthHooks = {
  getAccessToken: () => defaultAuthHooks.getAccessToken(),
  refreshSession: () => defaultAuthHooks.refreshSession(),
  onUnauthorized: () => defaultAuthHooks.onUnauthorized(),
}

export const apiClient = createApiClient({ authHooks: delegatedAuthHooks })
