import { flushPromises } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { afterEach, describe, expect, it, vi } from 'vitest'

import type { AuthMenu } from '@/features/auth/model/auth'
import type { RouteComponentWhitelist } from '../router/dynamicRoutes'

import { usePermissionStore } from './usePermissionStore'

const dashboardMenu: AuthMenu = {
  functionId: 'menu-dashboard',
  parentId: '0',
  name: '首页概览',
  type: 'MENU',
  routePath: '/dashboard',
  componentPath: 'dashboard/overview',
  icon: 'House',
  cacheable: 1,
  permissionCode: 'dashboard:view',
  sortOrder: 0,
  children: [],
}

const usersMenu: AuthMenu = {
  ...dashboardMenu,
  functionId: 'menu-users',
  name: '用户管理',
  routePath: '/system/users',
  componentPath: 'system/users',
  permissionCode: 'system:user:view',
}

afterEach(() => {
  vi.useRealTimers()
})

describe('usePermissionStore', () => {
  it('preloads remaining accessible components after the first dynamic navigation', async () => {
    vi.useFakeTimers()
    const dashboardLoader = vi.fn(() => Promise.resolve({ default: { template: '<p>首页</p>' } }))
    const usersLoader = vi.fn(() => Promise.resolve({ default: { template: '<p>用户</p>' } }))
    const whitelist: RouteComponentWhitelist = {
      'dashboard/overview': dashboardLoader,
      'system/users': usersLoader,
    }
    const router = createRouter({ history: createMemoryHistory(), routes: [] })
    const store = usePermissionStore(createPinia())

    store.registerRoutes(router, [dashboardMenu, usersMenu], whitelist)
    await router.push('/dashboard')
    expect(usersLoader).not.toHaveBeenCalled()

    await vi.runAllTimersAsync()
    await flushPromises()

    expect(store.routesRegistered).toBe(true)
    expect(usersLoader).toHaveBeenCalledTimes(1)
  })

  it('cancels a pending preload when dynamic routes are reset', async () => {
    vi.useFakeTimers()
    const dashboardLoader = vi.fn(() => Promise.resolve({ default: { template: '<p>首页</p>' } }))
    const usersLoader = vi.fn(() => Promise.resolve({ default: { template: '<p>用户</p>' } }))
    const whitelist: RouteComponentWhitelist = {
      'dashboard/overview': dashboardLoader,
      'system/users': usersLoader,
    }
    const router = createRouter({ history: createMemoryHistory(), routes: [] })
    const store = usePermissionStore(createPinia())

    store.registerRoutes(router, [dashboardMenu, usersMenu], whitelist)
    await router.push('/dashboard')
    store.resetRoutes(router)
    await vi.runAllTimersAsync()
    await flushPromises()

    expect(usersLoader).not.toHaveBeenCalled()
  })
})
