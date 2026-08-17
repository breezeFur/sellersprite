import { beforeEach, describe, expect, it, vi } from 'vitest'

import { apiClient } from '@/shared/api/http'

import {
  getAiPromptLog,
  getLoginLog,
  getOperationLog,
  pageAiPromptLogs,
  pageLoginLogs,
  pageOperationLogs,
} from './logApi'

vi.mock('@/shared/api/http', () => ({ apiClient: { request: vi.fn() } }))

describe('logApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('uses server-side paging contracts for all three log types', () => {
    pageLoginLogs({ current: 1, size: 20, username: 'admin', success: 0 })
    pageOperationLogs({ current: 2, size: 20, moduleName: 'PermissionController', traceId: 'trace-1' })
    pageAiPromptLogs({ current: 3, size: 20, model: 'gpt-5', status: 'FAILED' })

    expect(apiClient.request).toHaveBeenNthCalledWith(1, {
      method: 'GET', url: '/logs/login', params: { current: 1, size: 20, username: 'admin', success: 0 },
    })
    expect(apiClient.request).toHaveBeenNthCalledWith(2, {
      method: 'GET', url: '/logs/operation', params: { current: 2, size: 20, moduleName: 'PermissionController', traceId: 'trace-1' },
    })
    expect(apiClient.request).toHaveBeenNthCalledWith(3, {
      method: 'GET', url: '/logs/ai-prompts', params: { current: 3, size: 20, model: 'gpt-5', status: 'FAILED' },
    })
  })

  it('uses dedicated detail endpoints', () => {
    getLoginLog('login-1')
    getOperationLog('operation-1')
    getAiPromptLog('prompt-1')

    expect(apiClient.request).toHaveBeenNthCalledWith(1, { method: 'GET', url: '/logs/login/login-1' })
    expect(apiClient.request).toHaveBeenNthCalledWith(2, { method: 'GET', url: '/logs/operation/operation-1' })
    expect(apiClient.request).toHaveBeenNthCalledWith(3, { method: 'GET', url: '/logs/ai-prompts/prompt-1' })
  })
})
