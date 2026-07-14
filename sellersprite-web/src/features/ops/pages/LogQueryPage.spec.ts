import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

import * as logApi from '../api/logApi'
import LogQueryPage from './LogQueryPage.vue'

vi.mock('../api/logApi', () => ({
  pageLoginLogs: vi.fn(),
  getLoginLog: vi.fn(),
  pageOperationLogs: vi.fn(),
  getOperationLog: vi.fn(),
  pageAiPromptLogs: vi.fn(),
  getAiPromptLog: vi.fn(),
}))

describe('LogQueryPage', () => {
  it('loads tabs on demand and renders server-masked operation detail', async () => {
    vi.mocked(logApi.pageLoginLogs).mockResolvedValue({
      current: 1, size: 20, total: 1,
      records: [{ loginLogId: 'login-1', userId: 'user-1', username: 'admin', loginType: 'LOGIN', success: 1, errorCode: '', failureReason: '', loginIp: '127.0.0.1', loginLocation: '', userAgent: 'browser', deviceName: '', clientType: 'WEB', trackId: 'track-login', createdAt: 1 }],
    })
    vi.mocked(logApi.pageOperationLogs).mockResolvedValue({
      current: 1, size: 20, total: 1,
      records: [{ operationLogId: 'operation-1', userId: 'user-1', username: 'admin', moduleName: 'PermissionController', operationName: '同步菜单接口绑定', operationType: 'UPDATE', httpMethod: 'PUT', requestUri: '/api/permissions/functions/api-bindings/sync', requestParams: '', responsePayload: '', responseStatus: 200, success: 1, errorMessage: '', clientIp: '127.0.0.1', userAgent: 'browser', costMs: 12, trackId: 'track-operation', createdAt: 2 }],
    })
    vi.mocked(logApi.getOperationLog).mockResolvedValue({
      operationLogId: 'operation-1', userId: 'user-1', username: 'admin', moduleName: 'PermissionController', operationName: '同步菜单接口绑定', operationType: 'UPDATE', httpMethod: 'PUT', requestUri: '/api/permissions/functions/api-bindings/sync', requestParams: '{"authorization":"[REDACTED]"}', responsePayload: '{"code":"00000"}', responseStatus: 200, success: 1, errorMessage: '', clientIp: '127.0.0.1', userAgent: 'browser', costMs: 12, trackId: 'track-operation', createdAt: 2,
    })
    const wrapper = mount(LogQueryPage, { global: { stubs: { Teleport: true } } })
    await flushPromises()

    expect(wrapper.text()).toContain('admin')
    await wrapper.get('[data-testid="tab-operation"]').trigger('click')
    await flushPromises()
    expect(logApi.pageOperationLogs).toHaveBeenCalledOnce()
    expect(wrapper.text()).toContain('同步菜单接口绑定')

    await wrapper.get('[data-testid="operation-detail-operation-1"]').trigger('click')
    await flushPromises()
    expect(logApi.getOperationLog).toHaveBeenCalledWith('operation-1')
    expect(wrapper.text()).toContain('[REDACTED]')
  })

  it('treats the backend SUCCESS prompt status as successful', async () => {
    vi.mocked(logApi.pageLoginLogs).mockResolvedValue({ current: 1, size: 20, total: 0, records: [] })
    vi.mocked(logApi.pageAiPromptLogs).mockResolvedValue({
      current: 1, size: 20, total: 1,
      records: [{ promptRecordId: 'prompt-1', conversationId: 'conversation-1', userId: 'user-1', provider: 'openai', model: 'gpt-5.5', requestMessages: '', promptSummary: '', promptTruncated: 0, responseContent: '', responseMetadata: '', promptTokens: 10, completionTokens: 20, totalTokens: 30, finishReason: 'STOP', status: 'SUCCESS', errorType: '', errorMessage: '', costMs: 100, trackId: 'track-ai', createdAt: 3 }],
    })
    const wrapper = mount(LogQueryPage, { global: { stubs: { Teleport: true } } })
    await flushPromises()

    await wrapper.get('[data-testid="tab-ai"]').trigger('click')
    await flushPromises()

    expect(wrapper.get('.result.success').text()).toBe('SUCCESS')
    expect(wrapper.text()).toContain('完成')
  })
})
