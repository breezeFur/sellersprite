import type { Component } from 'vue'
import { describe, expect, it, vi } from 'vitest'

import type { AuthMenu } from '@/features/auth/model/auth'

import {
  RouteComponentNotAllowedError,
  buildDynamicRoutes,
  preloadAccessibleRouteComponents,
  type RouteComponentWhitelist,
} from './dynamicRoutes'

const pageLoader = () => Promise.resolve({ default: { template: '<p>页面</p>' } as Component })
const whitelist: RouteComponentWhitelist = {
  'dashboard/overview': pageLoader,
  'system/users': pageLoader,
}

function menu(overrides: Partial<AuthMenu>): AuthMenu {
  return {
    functionId: 'menu-1',
    parentId: '0',
    name: '菜单',
    type: 'MENU',
    routePath: '/dashboard',
    componentPath: 'dashboard/overview',
    icon: 'House',
    cacheable: 0,
    permissionCode: 'dashboard:view',
    sortOrder: 0,
    children: [],
    ...overrides,
  }
}

describe('buildDynamicRoutes', () => {
  it('flattens directory menus, ignores buttons and returns the first accessible path', () => {
    const menus = [
      menu({
        functionId: 'dir-system',
        name: '系统管理',
        type: 'DIR',
        routePath: null,
        componentPath: null,
        children: [
          menu({
            functionId: 'menu-users',
            name: '用户管理',
            routePath: '/system/users',
            componentPath: 'system/users',
          }),
          menu({ functionId: 'button-create', type: 'BUTTON', routePath: null, componentPath: null }),
        ],
      }),
      menu({ functionId: 'menu-dashboard' }),
    ]

    const result = buildDynamicRoutes(menus, whitelist)

    expect(result.firstAccessiblePath).toBe('/system/users')
    expect(result.routes).toHaveLength(2)
    expect(result.routes.map((route) => route.path)).toEqual(['system/users', 'dashboard'])
    expect(result.routes[0]?.meta).toMatchObject({
      title: '用户管理',
      permissionCode: 'dashboard:view',
      cacheable: false,
    })
  })

  it('rejects component paths that are not in the local whitelist', () => {
    expect(() =>
      buildDynamicRoutes([menu({ componentPath: '../../remote/Page' })], whitelist),
    ).toThrow(RouteComponentNotAllowedError)
  })

  it('returns no home fallback when there are no route-capable menus', () => {
    const result = buildDynamicRoutes(
      [menu({ type: 'BUTTON', routePath: null, componentPath: null })],
      whitelist,
    )

    expect(result.routes).toEqual([])
    expect(result.firstAccessiblePath).toBeNull()
  })

  it('preloads accessible component loaders once and isolates individual failures', async () => {
    const sharedLoader = vi.fn(pageLoader)
    const failedLoader = vi.fn(() => Promise.reject(new Error('chunk unavailable')))
    const unauthorizedLoader = vi.fn(pageLoader)
    const preloadWhitelist: RouteComponentWhitelist = {
      'dashboard/overview': sharedLoader,
      'system/users': failedLoader,
      'ops/logs': unauthorizedLoader,
    }
    const menuTree = [
      menu({ functionId: 'menu-dashboard', componentPath: 'dashboard/overview' }),
      menu({
        functionId: 'menu-dashboard-copy',
        routePath: '/dashboard-copy',
        componentPath: 'dashboard/overview',
      }),
      menu({
        functionId: 'menu-users',
        routePath: '/system/users',
        componentPath: 'system/users',
      }),
    ]

    await expect(preloadAccessibleRouteComponents(menuTree, preloadWhitelist)).resolves.toBeUndefined()

    expect(sharedLoader).toHaveBeenCalledTimes(1)
    expect(failedLoader).toHaveBeenCalledTimes(1)
    expect(unauthorizedLoader).not.toHaveBeenCalled()
  })
})
