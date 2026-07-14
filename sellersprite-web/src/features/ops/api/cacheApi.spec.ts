import { beforeEach, describe, expect, it, vi } from 'vitest'

import { apiClient } from '@/shared/api/http'

import { clearCachePrefix, deleteCacheKey, getCacheKeys, getCacheValue } from './cacheApi'

vi.mock('@/shared/api/http', () => ({ apiClient: { request: vi.fn() } }))

describe('cacheApi', () => {
  beforeEach(() => vi.clearAllMocks())

  it('uses only the five Cache Starter endpoints', () => {
    getCacheKeys()
    getCacheValue('alpha')
    deleteCacheKey('alpha')
    clearCachePrefix()

    expect(apiClient.request).toHaveBeenNthCalledWith(1, { method: 'GET', url: '/cache/keys' })
    expect(apiClient.request).toHaveBeenNthCalledWith(2, { method: 'GET', url: '/cache/value', params: { key: 'alpha' } })
    expect(apiClient.request).toHaveBeenNthCalledWith(3, { method: 'DELETE', url: '/cache/key', params: { key: 'alpha' } })
    expect(apiClient.request).toHaveBeenNthCalledWith(4, { method: 'DELETE', url: '/cache' })
  })
})
