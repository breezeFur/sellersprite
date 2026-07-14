import type { Pinia } from 'pinia'
import type { RouteLocationNormalized, Router } from 'vue-router'

import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import { routeComponentWhitelist } from '@/features/permission/router/componentWhitelist'
import type { RouteComponentWhitelist } from '@/features/permission/router/dynamicRoutes'
import { usePermissionStore } from '@/features/permission/stores/usePermissionStore'

export function installRouterGuards(
  router: Router,
  pinia: Pinia,
  whitelist: RouteComponentWhitelist = routeComponentWhitelist,
) {
  router.beforeEach(async (to) => {
    const authStore = useAuthStore(pinia)
    const permissionStore = usePermissionStore(pinia)

    if (authStore.status === 'idle') {
      await authStore.restore()
    }

    let registeredNow = false
    if (authStore.isAuthenticated && !permissionStore.routesRegistered) {
      permissionStore.registerRoutes(router, authStore.menuTree, whitelist, authStore.isSuperAdmin)
      registeredNow = true
    }

    if (to.meta.public) {
      if (to.name === 'login' && authStore.isAuthenticated) {
        return permissionStore.firstAccessiblePath ?? noAccessibleRoute()
      }
      return true
    }

    if (!authStore.isAuthenticated) {
      return {
        name: 'login',
        query: { redirect: safeReturnPath(to) },
      }
    }

    if (registeredNow) {
      return to.fullPath
    }

    const permissionCode = to.meta.permissionCode
    if (permissionCode && !authStore.permissionCodes.has(permissionCode)) {
      return { name: 'forbidden' }
    }

    return true
  })
}

function safeReturnPath(to: RouteLocationNormalized) {
  const fullPath = to.fullPath
  return fullPath.startsWith('/') && !fullPath.startsWith('//') && !fullPath.includes('\\') ? fullPath : '/'
}

function noAccessibleRoute() {
  return {
    name: 'forbidden',
    query: { reason: 'no-accessible-route' },
  }
}
