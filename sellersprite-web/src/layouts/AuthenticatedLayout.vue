<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import SidebarNavigation from '@/features/permission/components/SidebarNavigation.vue'

import AppShell from './AppShell.vue'
import CachedRouterView from './CachedRouterView.vue'

const route = useRoute()
const authStore = useAuthStore()
const pageTitle = computed(() => String(route.meta.title ?? ''))
const userName = computed(() => authStore.user?.nickname || authStore.user?.username || '当前用户')
</script>

<template>
  <AppShell
    :page-title="pageTitle"
    :user-name="userName"
  >
    <template #navigation>
      <SidebarNavigation :items="authStore.menuTree" />
    </template>

    <CachedRouterView />
  </AppShell>
</template>
