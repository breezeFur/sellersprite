import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Router } from 'vue-router'

import type { AuthMenu } from '@/features/auth/model/auth'

import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import { routeComponentWhitelist } from '../router/componentWhitelist'
import {
  buildDynamicRoutes,
  preloadAccessibleRouteComponents,
  type RouteComponentWhitelist,
} from '../router/dynamicRoutes'

const AUTHENTICATED_ROUTE_NAME = 'authenticated-root'
const SUPER_ADMIN_BOOTSTRAP_COMPONENT = 'dashboard/overview'
const SUPER_ADMIN_BOOTSTRAP_PATH = 'dashboard'
const ROUTE_COMPONENT_PRELOAD_TIMEOUT_MS = 1_500

export const usePermissionStore = defineStore('permission', () => {
  const routesRegistered = ref(false)
  const firstAccessiblePath = ref<string | null>(null)
  let cancelPendingPreload: (() => void) | null = null
  let removePreloadNavigationHook: (() => void) | null = null

  function registerRoutes(
    router: Router,
    menuTree: AuthMenu[],
    whitelist: RouteComponentWhitelist = routeComponentWhitelist,
    isSuperAdmin = false,
  ) {
    resetRoutes(router)
    const dynamicRoutes = buildDynamicRoutes(menuTree, whitelist)
    const routes = [...dynamicRoutes.routes]
    let firstPath = dynamicRoutes.firstAccessiblePath

    if (!firstPath && isSuperAdmin) {
      const dashboardComponent = whitelist[SUPER_ADMIN_BOOTSTRAP_COMPONENT]
      if (dashboardComponent) {
        routes.push({
          path: SUPER_ADMIN_BOOTSTRAP_PATH,
          name: 'super-admin-bootstrap-dashboard',
          component: dashboardComponent,
          meta: {
            title: '首页概览',
            dynamic: true,
            bootstrap: true,
            cacheable: true,
          },
        })
        firstPath = `/${SUPER_ADMIN_BOOTSTRAP_PATH}`
      }
    }

    router.addRoute({
      path: '/',
      name: AUTHENTICATED_ROUTE_NAME,
      component: AuthenticatedLayout,
      redirect: firstPath ?? '/403?reason=no-accessible-route',
      children: routes,
      meta: { requiresAuth: true },
    })
    firstAccessiblePath.value = firstPath
    routesRegistered.value = true
    schedulePreloadAfterFirstDynamicNavigation(router, menuTree, whitelist)
  }

  function resetRoutes(router: Router) {
    cancelPreloadScheduling()
    if (router.hasRoute(AUTHENTICATED_ROUTE_NAME)) {
      router.removeRoute(AUTHENTICATED_ROUTE_NAME)
    }
    routesRegistered.value = false
    firstAccessiblePath.value = null
  }

  function schedulePreloadAfterFirstDynamicNavigation(
    router: Router,
    menuTree: AuthMenu[],
    whitelist: RouteComponentWhitelist,
  ) {
    removePreloadNavigationHook = router.afterEach((to) => {
      if (!to.meta.dynamic) return

      removePreloadNavigationHook?.()
      removePreloadNavigationHook = null
      cancelPendingPreload = scheduleWhenBrowserIsIdle(() => {
        cancelPendingPreload = null
        void preloadAccessibleRouteComponents(menuTree, whitelist)
      })
    })
  }

  function cancelPreloadScheduling() {
    removePreloadNavigationHook?.()
    removePreloadNavigationHook = null
    cancelPendingPreload?.()
    cancelPendingPreload = null
  }

  return {
    routesRegistered,
    firstAccessiblePath,
    registerRoutes,
    resetRoutes,
  }
})

function scheduleWhenBrowserIsIdle(callback: () => void) {
  if (typeof window === 'undefined') {
    callback()
    return () => undefined
  }
  if (typeof window.requestIdleCallback === 'function') {
    const requestId = window.requestIdleCallback(callback, {
      timeout: ROUTE_COMPONENT_PRELOAD_TIMEOUT_MS,
    })
    return () => window.cancelIdleCallback(requestId)
  }

  const timeoutId = window.setTimeout(callback, 0)
  return () => window.clearTimeout(timeoutId)
}
