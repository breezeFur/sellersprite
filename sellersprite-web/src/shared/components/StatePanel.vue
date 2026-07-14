<script setup lang="ts">
import { CircleCloseFilled, Files, Loading, Lock } from '@element-plus/icons-vue'
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    status: 'loading' | 'empty' | 'error' | 'forbidden'
    title: string
    description?: string
    actionLabel?: string
  }>(),
  {
    description: '',
    actionLabel: '',
  },
)

const emit = defineEmits<{
  action: []
}>()

const icon = computed(() => {
  const icons = {
    loading: Loading,
    empty: Files,
    error: CircleCloseFilled,
    forbidden: Lock,
  }
  return icons[props.status]
})
</script>

<template>
  <section
    class="state-panel"
    :class="`state-panel--${status}`"
    :aria-busy="status === 'loading'"
    aria-live="polite"
  >
    <Component
      :is="icon"
      class="state-panel__icon"
      aria-hidden="true"
    />
    <h2 class="state-panel__title">
      {{ title }}
    </h2>
    <p
      v-if="description"
      class="state-panel__description"
    >
      {{ description }}
    </p>
    <button
      v-if="actionLabel"
      class="state-panel__action"
      type="button"
      @click="emit('action')"
    >
      {{ actionLabel }}
    </button>
    <slot name="actions" />
  </section>
</template>

<style scoped>
.state-panel {
  display: flex;
  min-height: 320px;
  padding: var(--space-8) var(--space-4);
  align-items: center;
  justify-content: center;
  flex-direction: column;
  color: var(--color-text-secondary);
  text-align: center;
}

.state-panel__icon {
  width: 38px;
  height: 38px;
  margin-bottom: var(--space-4);
  color: var(--color-text-muted);
}

.state-panel--loading .state-panel__icon {
  color: var(--color-brand-600);
  animation: state-panel-spin 900ms linear infinite;
}

.state-panel--error .state-panel__icon {
  color: var(--color-danger);
}

.state-panel--forbidden .state-panel__icon {
  color: var(--color-warning);
}

.state-panel__title {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-lg);
  font-weight: 650;
  line-height: var(--line-height-tight);
}

.state-panel__description {
  max-width: 480px;
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.state-panel__action {
  min-width: 88px;
  height: 34px;
  margin-top: var(--space-5);
  padding: 0 var(--space-4);
  color: #ffffff;
  background: var(--color-brand-600);
  border: 0;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-weight: 600;
  cursor: pointer;
}

.state-panel__action:hover {
  background: var(--color-brand-700);
}

@keyframes state-panel-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
