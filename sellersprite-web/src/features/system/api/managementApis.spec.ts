import { beforeEach, describe, expect, it, vi } from 'vitest'

import { apiClient } from '@/shared/api/http'

import { loadBackendApiCatalog, pageApiResources, syncMenuApiBindings } from './apiResourceApi'
import { menuApiBindings } from '@/features/permission/config/menuApiBindings'
import { pageDictTypes } from './dictionaryApi'
import { replaceFunctionApis } from './functionApi'

vi.mock('@/shared/api/http', () => ({
  apiClient: { request: vi.fn() },
}))

describe('system management APIs', () => {
  beforeEach(() => vi.clearAllMocks())

  it('uses the dictionary compatibility prefix', () => {
    pageDictTypes({ current: 1, size: 20, dictType: 'user' })

    expect(apiClient.request).toHaveBeenCalledWith({
      method: 'GET',
      url: '/system/dicts/types',
      params: { current: 1, size: 20, dictType: 'user' },
    })
  })

  it('replaces function API bindings with the typed request body', () => {
    replaceFunctionApis('function-1', ['api-1'])

    expect(apiClient.request).toHaveBeenCalledWith({
      method: 'PUT',
      url: '/permissions/functions/function-1/apis',
      data: { apiIds: ['api-1'] },
    })
  })

  it('pages API resources through the permission contract', () => {
    pageApiResources({ current: 1, size: 20, keyword: 'user' })

    expect(apiClient.request).toHaveBeenCalledWith({
      method: 'GET',
      url: '/permissions/apis',
      params: { current: 1, size: 20, keyword: 'user' },
    })
  })

  it('loads the backend catalog and synchronizes the typed menu binding manifest', () => {
    loadBackendApiCatalog()
    syncMenuApiBindings()

    expect(apiClient.request).toHaveBeenNthCalledWith(1, {
      method: 'POST',
      url: '/permissions/apis/catalog/sync',
    })
    expect(apiClient.request).toHaveBeenNthCalledWith(2, {
      method: 'PUT',
      url: '/permissions/functions/api-bindings/sync',
      data: { bindings: menuApiBindings },
    })
  })
})
