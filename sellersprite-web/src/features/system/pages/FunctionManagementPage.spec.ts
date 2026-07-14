import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

import * as functionApi from '../api/functionApi'
import * as optionApi from '../api/permissionOptionApi'
import FunctionManagementPage from './FunctionManagementPage.vue'

vi.mock('../api/functionApi', () => ({
  getFunctionTree: vi.fn(),
  createFunction: vi.fn(),
  updateFunction: vi.fn(),
  updateFunctionStatus: vi.fn(),
  deleteFunction: vi.fn(),
  getFunctionApis: vi.fn(),
  replaceFunctionApis: vi.fn(),
}))

vi.mock('../api/permissionOptionApi', () => ({ pageApiOptions: vi.fn().mockResolvedValue({ current: 1, size: 500, total: 0, records: [] }) }))

describe('FunctionManagementPage', () => {
  it('renders nested functions with their types', async () => {
    vi.mocked(functionApi.getFunctionTree).mockResolvedValue([{ sysFunctionId: 'root', parentId: '0', functionCode: 'system', functionName: '系统管理', functionType: 'DIR', routePath: null, componentPath: null, icon: 'SetUp', visible: 1, cacheable: 0, externalLink: null, permissionCode: null, sortOrder: 10, status: 1, children: [{ sysFunctionId: 'child', parentId: 'root', functionCode: 'system.user', functionName: '用户管理', functionType: 'MENU', routePath: '/system/users', componentPath: 'system/users', icon: 'User', visible: 1, cacheable: 0, externalLink: null, permissionCode: 'system:user:view', sortOrder: 10, status: 1, children: [] }] }])
    const wrapper = mount(FunctionManagementPage, { global: { stubs: { Teleport: true } } })
    await flushPromises()

    expect(wrapper.text()).toContain('系统管理')
    expect(wrapper.text()).toContain('用户管理')
    expect(wrapper.text()).toContain('菜单')
  })

  it('clears all function API bindings before saving', async () => {
    const node = { sysFunctionId: 'function-1', parentId: '0', functionCode: 'system.test', functionName: '测试功能', functionType: 'BUTTON', routePath: null, componentPath: null, icon: '', visible: 1, cacheable: 0, externalLink: null, permissionCode: 'system:test', sortOrder: 10, status: 1, children: [] }
    vi.mocked(functionApi.getFunctionTree).mockResolvedValue([node])
    vi.mocked(functionApi.getFunctionApis).mockResolvedValue(['api-1'])
    vi.mocked(functionApi.replaceFunctionApis).mockResolvedValue()
    vi.mocked(optionApi.pageApiOptions).mockResolvedValue({ current: 1, size: 500, total: 1, records: [{ sysApiId: 'api-1', apiCode: 'test.get', apiName: '测试接口', apiType: 'PERMISSION', httpMethod: 'GET', pathPattern: '/api/test', permissionCode: 'system:test', moduleName: 'system', operationName: '测试', status: 1 }] })
    const wrapper = mount(FunctionManagementPage, { global: { stubs: { Teleport: true } } })
    await flushPromises()

    const bindingButton = wrapper.findAll('button').find((button) => button.text() === '绑定接口')
    await bindingButton!.trigger('click')
    await flushPromises()
    const clearButton = wrapper.findAll('button').find((button) => button.text() === '清空选择')
    expect(clearButton).toBeDefined()
    await clearButton!.trigger('click')
    const saveButton = wrapper.findAll('button').find((button) => button.text() === '保存绑定')
    await saveButton!.trigger('click')
    await flushPromises()

    expect(functionApi.replaceFunctionApis).toHaveBeenCalledWith('function-1', [])
  })
})
