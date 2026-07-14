<script setup lang="ts">
import { computed } from 'vue'

import type { AuthMenu } from '@/features/auth/model/auth'

import { normalizeFullRoutePath } from '../router/dynamicRoutes'
import { resolveMenuIcon } from './menuIcons'

const props = defineProps<{
  item: AuthMenu
}>()

const visibleChildren = computed(() => props.item.children.filter((item) => item.type !== 'BUTTON'))
const routePath = computed(() => (props.item.routePath ? normalizeFullRoutePath(props.item.routePath) : ''))
</script>

<template>
  <ElSubMenu
    v-if="visibleChildren.length > 0"
    :index="item.functionId"
  >
    <template #title>
      <ElIcon><Component :is="resolveMenuIcon(item.icon)" /></ElIcon>
      <span>{{ item.name }}</span>
    </template>
    <SidebarMenuItem
      v-for="child in visibleChildren"
      :key="child.functionId"
      :item="child"
    />
  </ElSubMenu>
  <ElMenuItem
    v-else-if="routePath"
    :index="routePath"
  >
    <ElIcon><Component :is="resolveMenuIcon(item.icon)" /></ElIcon>
    <template #title>
      {{ item.name }}
    </template>
  </ElMenuItem>
</template>
