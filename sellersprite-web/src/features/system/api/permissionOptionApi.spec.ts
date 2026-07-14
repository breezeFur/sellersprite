import { beforeEach, describe, expect, it, vi } from 'vitest'

import { apiClient } from '@/shared/api/http'

import { pageApiOptions } from './permissionOptionApi'

vi.mock('@/shared/api/http', () => ({
  apiClient: {
    request: vi.fn(),
  },
}))

describe('permissionOptionApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('按后端允许的最大分页数量加载接口选项', () => {
    pageApiOptions()

    expect(apiClient.request).toHaveBeenCalledWith({
      method: 'GET',
      url: '/permissions/apis',
      params: { current: 1, size: 500, status: 1 },
    })
  })
})
