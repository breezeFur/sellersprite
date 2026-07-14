<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

import StatePanel from '@/shared/components/StatePanel.vue'

const route = useRoute()
const title = computed(() => String(route.meta.title ?? '当前模块'))
</script>

<template>
  <section
    class="module-placeholder"
    :aria-label="title"
  >
    <StatePanel
      status="empty"
      :title="`${title}暂无数据`"
      description="当前没有可展示的数据。"
    />
  </section>
</template>

<style scoped>
.module-placeholder {
  min-height: calc(100vh - var(--header-height) - var(--content-gutter) * 2);
  padding: var(--space-8);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
}
</style>
