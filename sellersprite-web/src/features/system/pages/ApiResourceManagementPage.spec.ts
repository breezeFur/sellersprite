import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import { ElMessageBox } from 'element-plus'

import * as apiResourceApi from '../api/apiResourceApi'
import ApiResourceManagementPage from './ApiResourceManagementPage.vue'

vi.mock('../api/apiResourceApi', () => ({
  pageApiResources: vi.fn(),
  createApiResource: vi.fn(),
  updateApiResource: vi.fn(),
  updateApiResourceStatus: vi.fn(),
  deleteApiResource: vi.fn(),
  loadBackendApiCatalog: vi.fn(),
  syncMenuApiBindings: vi.fn(),
}))

describe('ApiResourceManagementPage', () => {
  it('renders method, path and permission code', async () => {
    vi.mocked(apiResourceApi.pageApiResources).mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      records: [{ sysApiId: 'api-1', apiCode: 'user.page', apiName: '用户分页', apiType: 'PERMISSION', httpMethod: 'GET', pathPattern: '/api/users', permissionCode: 'system:user:view', moduleName: 'system', operationName: '查询用户', status: 1 }],
    })
    const wrapper = mount(ApiResourceManagementPage, { global: { stubs: { Teleport: true } } })
    await flushPromises()

    expect(wrapper.text()).toContain('用户分页')
    expect(wrapper.text()).toContain('GET')
    expect(wrapper.text()).toContain('/api/users')
    expect(wrapper.text()).toContain('system:user:view')
  })

  it('loads the backend catalog and then synchronizes menu bindings', async () => {
    vi.spyOn(ElMessageBox, 'confirm').mockResolvedValue('confirm' as never)
    vi.mocked(apiResourceApi.pageApiResources).mockResolvedValue({ current: 1, size: 20, total: 0, records: [] })
    vi.mocked(apiResourceApi.loadBackendApiCatalog).mockResolvedValue({ scanned: 80, created: 80, updated: 0, unchanged: 0 })
    vi.mocked(apiResourceApi.syncMenuApiBindings).mockResolvedValue({ functionCount: 10, bindingCount: 60, publicApiCount: 4, permissionApiCount: 56 })
    const wrapper = mount(ApiResourceManagementPage, { global: { stubs: { Teleport: true } } })
    await flushPromises()

    await wrapper.get('[data-testid="load-api-catalog"]').trigger('click')
    await flushPromises()
    await wrapper.get('[data-testid="sync-menu-bindings"]').trigger('click')
    await flushPromises()

    expect(apiResourceApi.loadBackendApiCatalog).toHaveBeenCalledOnce()
    expect(apiResourceApi.syncMenuApiBindings).toHaveBeenCalledOnce()
  })
})
