import { createPinia } from 'pinia'
import { createApp } from 'vue'

import App from '@/app/App.vue'
import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import { createPermissionDirective } from '@/features/permission/directives/permission'
import { usePermissionStore } from '@/features/permission/stores/usePermissionStore'
import router from '@/router'
import { installRouterGuards } from '@/router/guards'
import { configureHttpAuth } from '@/shared/api/http'
import '@/styles/tokens.css'
import '@/styles/base.css'
import '@/styles/element-plus.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)

const authStore = useAuthStore(pinia)
const permissionStore = usePermissionStore(pinia)
configureHttpAuth({
  getAccessToken: () => authStore.accessToken,
  refreshSession: () => authStore.refreshAccessToken(),
  onUnauthorized: () => {
    const redirect = router.currentRoute.value.fullPath
    authStore.expireSession()
    permissionStore.resetRoutes(router)
    if (router.currentRoute.value.name !== 'login') {
      void router.replace({ name: 'login', query: { redirect } })
    }
  },
})
installRouterGuards(router, pinia)
app.directive('permission', createPermissionDirective(pinia))
app.use(router)

app.mount('#app')
