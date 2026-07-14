<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRouter } from 'vue-router'

import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import GlobalErrorBoundary from '@/shared/components/GlobalErrorBoundary.vue'
import StartupScreen from '@/shared/components/StartupScreen.vue'

const router = useRouter()
const authStore = useAuthStore()
const booting = computed(() => authStore.status === 'idle' || authStore.status === 'restoring')

function retryCurrentRoute() {
  router.go(0)
}
</script>

<template>
  <StartupScreen v-if="booting" />
  <GlobalErrorBoundary
    v-else
    @retry="retryCurrentRoute"
  >
    <RouterView />
  </GlobalErrorBoundary>
</template>
