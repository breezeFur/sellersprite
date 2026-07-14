import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import { createPermissionDirective } from '@/features/permission/directives/permission'

import * as optionApi from '../api/permissionOptionApi'
import * as roleApi from '../api/roleApi'
import { grantSourceLabel } from '../utils/rolePresentation'
import RoleManagementPage from './RoleManagementPage.vue'

vi.mock('../api/roleApi', () => ({
  pageRoles: vi.fn(),
  getRole: vi.fn(),
  createRole: vi.fn(),
  updateRole: vi.fn(),
  updateRoleStatus: vi.fn(),
  deleteRole: vi.fn(),
  getRolePermissions: vi.fn(),
  replaceRolePermissions: vi.fn(),
}))

vi.mock('../api/permissionOptionApi', () => ({
  getFunctionTree: vi.fn(),
  pageApiOptions: vi.fn(),
}))

describe('RoleManagementPage', () => {
  beforeEach(() => {
    vi.mocked(roleApi.pageRoles).mockReset().mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      records: [
        { roleId: 'role-admin', roleCode: 'admin', roleName: '系统管理员', roleType: 'SYSTEM', sortOrder: 0, status: 1 },
      ],
    })
    vi.mocked(optionApi.getFunctionTree).mockReset().mockResolvedValue([])
    vi.mocked(optionApi.pageApiOptions).mockReset().mockResolvedValue({
      current: 1,
      size: 500,
      total: 0,
      records: [],
    })
    vi.mocked(roleApi.getRolePermissions).mockReset().mockResolvedValue({
      roleId: 'role-admin',
      functionIds: [],
      extraApiIds: [],
      effectiveApis: [],
    })
  })

  it('renders role rows and protects the system administrator', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    useAuthStore().roles = [{ roleId: 'role-admin', roleCode: 'admin', roleName: '系统管理员' }]
    const wrapper = mount(RoleManagementPage, {
      global: {
        plugins: [pinia],
        directives: { permission: createPermissionDirective(pinia) },
        stubs: { Teleport: true, ElSelect: true, ElOption: true },
      },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('系统管理员')
    expect(wrapper.get('[aria-label="停用角色 系统管理员"]').attributes('disabled')).toBeDefined()
  })

  it('translates effective API grant sources', () => {
    expect(grantSourceLabel('FUNCTION')).toBe('功能派生')
    expect(grantSourceLabel('EXTRA')).toBe('直接附加')
    expect(grantSourceLabel('BOTH')).toBe('双重来源')
  })

  it('checks existing function permissions after the tree is mounted', async () => {
    vi.mocked(optionApi.getFunctionTree).mockResolvedValue([
      {
        sysFunctionId: 'function-dashboard',
        parentId: '0',
        functionCode: 'dashboard',
        functionName: '首页概览',
        functionType: 'MENU',
        routePath: '/dashboard',
        componentPath: 'dashboard/overview',
        icon: 'House',
        visible: 1,
        cacheable: 0,
        externalLink: null,
        permissionCode: 'dashboard:view',
        sortOrder: 10,
        status: 1,
        children: [],
      },
    ])
    vi.mocked(roleApi.getRolePermissions).mockResolvedValue({
      roleId: 'role-admin',
      functionIds: ['function-dashboard'],
      extraApiIds: [],
      effectiveApis: [],
    })
    const pinia = createPinia()
    setActivePinia(pinia)
    const wrapper = mount(RoleManagementPage, {
      global: {
        plugins: [pinia],
        directives: { permission: createPermissionDirective(pinia) },
        stubs: { Teleport: true, ElSelect: true, ElOption: true },
      },
    })
    await flushPromises()

    const authorizeButton = wrapper.findAll('button').find((button) => button.text() === '授权')
    expect(authorizeButton).toBeDefined()
    await authorizeButton!.trigger('click')
    await flushPromises()

    expect(wrapper.find('.el-tree .el-checkbox').classes()).toContain('is-checked')
  })
})
