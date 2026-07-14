import type { Component } from 'vue'
import type { RouteRecordRaw } from 'vue-router'

import type { AuthMenu } from '@/features/auth/model/auth'

export type RouteComponentLoader = () => Promise<{ default: Component }>
export type RouteComponentWhitelist = Record<string, RouteComponentLoader>

export interface DynamicRouteResult {
  routes: RouteRecordRaw[]
  firstAccessiblePath: string | null
}

export class RouteComponentNotAllowedError extends Error {
  constructor(componentPath: string) {
    super(`动态组件路径未登记：${componentPath}`)
    this.name = 'RouteComponentNotAllowedError'
  }
}

export function buildDynamicRoutes(
  menuTree: AuthMenu[],
  whitelist: RouteComponentWhitelist,
): DynamicRouteResult {
  const routes: RouteRecordRaw[] = []

  function visit(items: AuthMenu[]) {
    for (const item of items) {
      if (item.type !== 'BUTTON' && item.routePath && item.componentPath) {
        const componentPath = item.componentPath.trim()
        const component = whitelist[componentPath]
        if (!component) {
          throw new RouteComponentNotAllowedError(componentPath)
        }
        const path = normalizeChildRoutePath(item.routePath)
        routes.push({
          path,
          name: `business-${item.functionId}`,
          component,
          meta: {
            title: item.name,
            functionId: item.functionId,
            permissionCode: item.permissionCode || undefined,
            cacheable: item.cacheable === 1,
            dynamic: true,
          },
        })
      }
      if (item.children.length > 0) {
        visit(item.children)
      }
    }
  }

  visit(menuTree)
  return {
    routes,
    firstAccessiblePath: routes[0] ? `/${String(routes[0].path)}` : null,
  }
}

export function normalizeFullRoutePath(routePath: string) {
  return `/${normalizeChildRoutePath(routePath)}`
}

function normalizeChildRoutePath(routePath: string) {
  const trimmed = routePath.trim()
  if (
    !trimmed ||
    trimmed.includes('://') ||
    trimmed.includes('..') ||
    trimmed.includes('\\') ||
    trimmed.includes('?') ||
    trimmed.includes('#')
  ) {
    throw new Error(`动态路由路径不合法：${routePath}`)
  }
  const normalized = trimmed.replace(/^\/+|\/+$/g, '').replace(/\/{2,}/g, '/')
  if (!normalized) {
    throw new Error('动态路由不能覆盖应用根路径')
  }
  return normalized
}
