<script setup lang="ts">
import { onErrorCaptured, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import StatePanel from './StatePanel.vue'

const emit = defineEmits<{
  retry: []
}>()

const route = useRoute()
const failed = ref(false)

onErrorCaptured(() => {
  failed.value = true
  return false
})

watch(
  () => route.fullPath,
  () => {
    failed.value = false
  },
)
</script>

<template>
  <StatePanel
    v-if="failed"
    status="error"
    title="页面加载失败"
    description="当前页面未能正常显示，请重试。"
    action-label="重新载入"
    @action="emit('retry')"
  />
  <slot v-else />
</template>
