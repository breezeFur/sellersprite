<script setup lang="ts">
import { ArrowLeft, House } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

import { usePermissionStore } from '@/features/permission/stores/usePermissionStore'
import StatePanel from '@/shared/components/StatePanel.vue'

const router = useRouter()
const permissionStore = usePermissionStore()

function goHome() {
  void router.push(permissionStore.firstAccessiblePath ?? '/login')
}
</script>

<template>
  <main class="status-page">
    <StatePanel
      status="empty"
      title="页面不存在"
      description="页面可能已被移动或删除。"
    >
      <template #actions>
        <div class="status-page__actions">
          <ElButton
            :icon="ArrowLeft"
            @click="router.back()"
          >
            返回
          </ElButton>
          <ElButton
            type="primary"
            :icon="House"
            @click="goHome"
          >
            首页
          </ElButton>
        </div>
      </template>
    </StatePanel>
  </main>
</template>

<style scoped>
.status-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  background: var(--color-page);
}

.status-page__actions {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-5);
}
</style>
