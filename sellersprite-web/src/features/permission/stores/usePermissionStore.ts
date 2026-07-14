import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Router } from 'vue-router'

import type { AuthMenu } from '@/features/auth/model/auth'

import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import { routeComponentWhitelist } from '../router/componentWhitelist'
import { buildDynamicRoutes, type RouteComponentWhitelist } from '../router/dynamicRoutes'

const AUTHENTICATED_ROUTE_NAME = 'authenticated-root'
const SUPER_ADMIN_BOOTSTRAP_COMPONENT = 'dashboard/overview'
const SUPER_ADMIN_BOOTSTRAP_PATH = 'dashboard'

export const usePermissionStore = defineStore('permission', () => {
  const routesRegistered = ref(false)
  const firstAccessiblePath = ref<string | null>(null)

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
  }

  function resetRoutes(router: Router) {
    if (router.hasRoute(AUTHENTICATED_ROUTE_NAME)) {
      router.removeRoute(AUTHENTICATED_ROUTE_NAME)
    }
    routesRegistered.value = false
    firstAccessiblePath.value = null
  }

  return {
    routesRegistered,
    firstAccessiblePath,
    registerRoutes,
    resetRoutes,
  }
})
