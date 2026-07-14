import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

import * as cacheApi from '../api/cacheApi'
import CacheManagementPage from './CacheManagementPage.vue'

vi.mock('../api/cacheApi', () => ({
  getCacheKeys: vi.fn(),
  getCacheValue: vi.fn(),
  checkCacheKey: vi.fn(),
  deleteCacheKey: vi.fn(),
  clearCachePrefix: vi.fn(),
}))

describe('CacheManagementPage', () => {
  it('filters the starter key list on the client', async () => {
    vi.mocked(cacheApi.getCacheKeys).mockResolvedValue(['alpha', 'beta'])
    const wrapper = mount(CacheManagementPage, { global: { stubs: { Teleport: true } } })
    await flushPromises()

    expect(wrapper.text()).toContain('alpha')
    expect(wrapper.text()).toContain('beta')
    await wrapper.get('[aria-label="缓存键筛选"]').setValue('alp')

    expect(wrapper.text()).toContain('alpha')
    expect(wrapper.text()).not.toContain('beta')
    expect(wrapper.text()).not.toContain('TTL')
  })
})
