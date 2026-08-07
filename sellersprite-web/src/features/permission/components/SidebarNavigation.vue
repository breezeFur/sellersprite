<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

import type { AuthMenu } from '@/features/auth/model/auth'
import { useLayoutStore } from '@/layouts/useLayoutStore'

import SidebarMenuItem from './SidebarMenuItem.vue'

const props = defineProps<{
  items: AuthMenu[]
}>()

const route = useRoute()
const layoutStore = useLayoutStore()
const visibleItems = computed(() => props.items.filter((item) => item.type !== 'BUTTON'))
</script>

<template>
  <ElMenu
    class="sidebar-navigation"
    :default-active="route.path"
    :collapse="layoutStore.navigationCollapsed"
    :collapse-transition="false"
    router
    @select="layoutStore.closeMobileSidebar"
  >
    <SidebarMenuItem
      v-for="item in visibleItems"
      :key="item.functionId"
      :item="item"
    />
  </ElMenu>
</template>

<style scoped>
.sidebar-navigation {
  --el-menu-bg-color: transparent;
  --el-menu-text-color: var(--color-sidebar-text);
  --el-menu-hover-bg-color: var(--color-sidebar-raised);
  --el-menu-active-color: #ffffff;
  width: 100%;
  border-right: 0;
}

.sidebar-navigation:not(.el-menu--collapse) {
  width: 100%;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  height: 42px;
  margin: 2px 0;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  line-height: 42px;
}

:deep(.el-menu-item.is-active) {
  background: var(--color-brand-600);
}

:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  width: 18px;
  font-size: 18px;
}
</style>
