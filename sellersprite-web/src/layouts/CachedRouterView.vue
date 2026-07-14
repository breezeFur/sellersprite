<script setup lang="ts">
import type { RouteLocationNormalizedLoaded } from 'vue-router'

const MAX_CACHED_PAGE_COUNT = 12

function stableRouteKey(route: RouteLocationNormalizedLoaded) {
  return route.name ?? route.path
}
</script>

<template>
  <RouterView v-slot="{ Component: routeComponent, route: currentRoute }">
    <KeepAlive :max="MAX_CACHED_PAGE_COUNT">
      <Component
        :is="routeComponent"
        v-if="currentRoute.meta.cacheable"
        :key="stableRouteKey(currentRoute)"
      />
    </KeepAlive>
    <Component
      :is="routeComponent"
      v-if="!currentRoute.meta.cacheable"
      :key="currentRoute.fullPath"
    />
  </RouterView>
</template>
