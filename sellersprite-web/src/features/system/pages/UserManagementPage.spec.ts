import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import { createPermissionDirective } from '@/features/permission/directives/permission'

import * as optionApi from '../api/systemOptionApi'
import * as userApi from '../api/userApi'
import UserManagementPage from './UserManagementPage.vue'

vi.mock('../api/userApi', () => ({
  pageUsers: vi.fn(),
  getUser: vi.fn(),
  createUser: vi.fn(),
  updateUser: vi.fn(),
  updateUserStatus: vi.fn(),
  replaceUserRoles: vi.fn(),
  resetUserPassword: vi.fn(),
  deleteUser: vi.fn(),
}))

vi.mock('../api/systemOptionApi', () => ({
  listEnabledRoles: vi.fn(),
  getDepartmentTree: vi.fn(),
}))

const admin = {
  userId: 'user-admin',
  username: 'admin',
  nickname: '管理员',
  realName: '系统管理员',
  avatarUrl: '',
  mobile: '13800000000',
  email: 'admin@example.com',
  primaryDeptId: 'dept-root',
  status: 1,
  roleIds: ['role-admin'],
}

function setup() {
  const pinia = createPinia()
  setActivePinia(pinia)
  const authStore = useAuthStore()
  authStore.status = 'authenticated'
  authStore.accessToken = 'token'
  authStore.roles = [{ roleId: 'role-admin', roleCode: 'admin', roleName: '系统管理员' }]
  authStore.user = { ...admin, primaryDeptId: 'dept-root' }
  return mount(UserManagementPage, {
    global: {
      plugins: [pinia],
      directives: { permission: createPermissionDirective(pinia) },
      stubs: { Teleport: true },
    },
  })
}

describe('UserManagementPage', () => {
  beforeEach(() => {
    vi.mocked(userApi.pageUsers).mockReset().mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      records: [admin],
    })
    vi.mocked(optionApi.listEnabledRoles).mockReset().mockResolvedValue([
      { roleId: 'role-admin', roleCode: 'admin', roleName: '系统管理员', roleType: 'SYSTEM', sortOrder: 0, status: 1 },
    ])
    vi.mocked(optionApi.getDepartmentTree).mockReset().mockResolvedValue([
      { deptId: 'dept-root', parentId: '0', deptCode: 'ROOT', deptName: '根部门', deptPath: '/dept-root/', sortOrder: 0, status: 1, children: [] },
    ])
    vi.mocked(userApi.updateUserStatus).mockReset().mockResolvedValue(undefined)
  })

  it('renders users with resolved department and role names', async () => {
    const wrapper = setup()
    await flushPromises()

    expect(wrapper.text()).toContain('admin')
    expect(wrapper.text()).toContain('系统管理员')
    expect(wrapper.text()).toContain('根部门')
    expect(wrapper.text()).toContain('共 1 条')
  })

  it('updates a non-current user status and reloads the page', async () => {
    vi.mocked(userApi.pageUsers).mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      records: [{ ...admin, userId: 'user-other', username: 'operator' }],
    })
    const wrapper = setup()
    await flushPromises()

    await wrapper.get('[aria-label="停用用户 operator"]').trigger('click')
    await flushPromises()

    expect(userApi.updateUserStatus).toHaveBeenCalledWith('user-other', 0)
    expect(userApi.pageUsers).toHaveBeenCalledTimes(2)
  })
})
