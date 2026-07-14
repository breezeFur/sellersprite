import { createPinia, setActivePinia } from 'pinia'
import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it } from 'vitest'

import { useAuthStore } from '@/features/auth/stores/useAuthStore'

import { createPermissionDirective, hasPermission } from './permission'

describe('permission directive', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('supports all and any permission checks', () => {
    const granted = new Set(['system:user:view', 'system:user:create'])

    expect(hasPermission(granted, ['system:user:view', 'system:user:create'])).toBe(true)
    expect(hasPermission(granted, ['system:user:delete', 'system:user:view'], 'any')).toBe(true)
    expect(hasPermission(granted, ['system:user:delete'])).toBe(false)
  })

  it('hides an action when its permission code is absent', () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    useAuthStore(pinia).permissionCodes = new Set(['system:user:view'])
    const wrapper = mount(
      { template: '<button v-permission="\'system:user:create\'">新增用户</button>' },
      {
        global: {
          plugins: [pinia],
          directives: { permission: createPermissionDirective(pinia) },
        },
      },
    )

    expect(wrapper.get('button').attributes('hidden')).toBeDefined()
  })

  it('keeps actions visible for the super admin role', () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    const authStore = useAuthStore(pinia)
    authStore.roles = [{ roleId: 'role-admin', roleCode: 'admin', roleName: '系统管理员' }]
    authStore.permissionCodes = new Set()
    const wrapper = mount(
      { template: '<button v-permission="\'system:user:create\'">新增用户</button>' },
      {
        global: {
          plugins: [pinia],
          directives: { permission: createPermissionDirective(pinia) },
        },
      },
    )

    expect(wrapper.get('button').attributes('hidden')).toBeUndefined()
  })
})
