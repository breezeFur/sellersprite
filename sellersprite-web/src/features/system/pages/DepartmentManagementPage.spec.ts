import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import { createPermissionDirective } from '@/features/permission/directives/permission'

import * as departmentApi from '../api/departmentApi'
import DepartmentManagementPage from './DepartmentManagementPage.vue'

vi.mock('../api/departmentApi', () => ({
  getDepartmentTree: vi.fn(),
  getDepartment: vi.fn(),
  createDepartment: vi.fn(),
  updateDepartment: vi.fn(),
  updateDepartmentStatus: vi.fn(),
  deleteDepartment: vi.fn(),
}))

describe('DepartmentManagementPage', () => {
  beforeEach(() => {
    vi.mocked(departmentApi.getDepartmentTree).mockReset().mockResolvedValue([
      {
        deptId: 'root',
        parentId: '0',
        deptCode: 'ROOT',
        deptName: '根部门',
        deptPath: '/root/',
        sortOrder: 0,
        status: 1,
        children: [
          {
            deptId: 'child',
            parentId: 'root',
            deptCode: 'OPS',
            deptName: '运维部',
            deptPath: '/root/child/',
            sortOrder: 10,
            status: 1,
            children: [],
          },
        ],
      },
    ])
  })

  it('renders nested departments with status and sort order', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    useAuthStore().roles = [{ roleId: 'admin', roleCode: 'admin', roleName: '系统管理员' }]
    const wrapper = mount(DepartmentManagementPage, {
      global: {
        plugins: [pinia],
        directives: { permission: createPermissionDirective(pinia) },
        stubs: { Teleport: true },
      },
    })
    await flushPromises()

    expect(wrapper.text()).toContain('根部门')
    expect(wrapper.text()).toContain('运维部')
    expect(wrapper.text()).toContain('排序 10')
  })
})
