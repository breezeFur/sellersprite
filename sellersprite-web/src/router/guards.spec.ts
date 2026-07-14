import { createPinia } from 'pinia'
import { createMemoryHistory, createRouter, type RouteRecordRaw } from 'vue-router'
import { describe, expect, it } from 'vitest'

import type { AuthMenu } from '@/features/auth/model/auth'
import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import type { RouteComponentWhitelist } from '@/features/permission/router/dynamicRoutes'
import { usePermissionStore } from '@/features/permission/stores/usePermissionStore'

import { installRouterGuards } from './guards'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: { template: '<p>登录</p>' }, meta: { public: true } },
  { path: '/403', name: 'forbidden', component: { template: '<p>无权限</p>' }, meta: { public: true } },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: { template: '<p>未找到</p>' } },
]

const dashboardMenu: AuthMenu = {
  functionId: 'menu-dashboard',
  parentId: '0',
  name: '首页概览',
  type: 'MENU',
  routePath: '/dashboard',
  componentPath: 'dashboard/overview',
  icon: 'House',
  cacheable: 0,
  permissionCode: 'dashboard:view',
  sortOrder: 0,
  children: [],
}

const whitelist: RouteComponentWhitelist = {
  'dashboard/overview': () => Promise.resolve({ default: { template: '<p>首页</p>' } }),
}

function setup() {
  const pinia = createPinia()
  const router = createRouter({ history: createMemoryHistory(), routes })
  installRouterGuards(router, pinia, whitelist)
  return { pinia, router, authStore: useAuthStore(pinia), permissionStore: usePermissionStore(pinia) }
}

describe('router guards', () => {
  it('redirects anonymous deep links to login with a safe return path', async () => {
    const { authStore, router } = setup()
    authStore.status = 'anonymous'

    await router.push('/dashboard?range=7d')

    expect(router.currentRoute.value.name).toBe('login')
    expect(router.currentRoute.value.query.redirect).toBe('/dashboard?range=7d')
  })

  it('registers dynamic routes and opens the first accessible home', async () => {
    const { authStore, permissionStore, router } = setup()
    authStore.status = 'authenticated'
    authStore.accessToken = 'token'
    authStore.menuTree = [dashboardMenu]
    authStore.permissionCodes = new Set(['dashboard:view'])

    await router.push('/login')

    expect(permissionStore.routesRegistered).toBe(true)
    expect(router.currentRoute.value.path).toBe('/dashboard')
  })

  it('shows 403 when an authenticated user has no accessible home', async () => {
    const { authStore, router } = setup()
    authStore.status = 'authenticated'
    authStore.accessToken = 'token'
    authStore.menuTree = []

    await router.push('/login')

    expect(router.currentRoute.value.name).toBe('forbidden')
    expect(router.currentRoute.value.query.reason).toBe('no-accessible-route')
  })

  it('registers a bootstrap dashboard for a super admin without seeded menus', async () => {
    const { authStore, permissionStore, router } = setup()
    authStore.status = 'authenticated'
    authStore.accessToken = 'token'
    authStore.roles = [
      {
        roleId: 'role-admin',
        roleCode: 'admin',
        roleName: '系统管理员',
      },
    ]
    authStore.menuTree = []

    await router.push('/login')

    expect(permissionStore.routesRegistered).toBe(true)
    expect(permissionStore.firstAccessiblePath).toBe('/dashboard')
    expect(router.currentRoute.value.path).toBe('/dashboard')
  })

  it('blocks a dynamic route when its permission code is absent', async () => {
    const { authStore, router } = setup()
    authStore.status = 'authenticated'
    authStore.accessToken = 'token'
    authStore.menuTree = [dashboardMenu]
    authStore.permissionCodes = new Set()

    await router.push('/dashboard')

    expect(router.currentRoute.value.name).toBe('forbidden')
  })
})
