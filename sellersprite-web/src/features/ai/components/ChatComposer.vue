<script setup lang="ts">
import { Promotion, VideoPause } from '@element-plus/icons-vue'

const props = defineProps<{
  modelValue: string
  busy: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  send: []
  stop: []
}>()

function handleKeydown(event: Event | KeyboardEvent) {
  if (event instanceof KeyboardEvent
    && (event.ctrlKey || event.metaKey)
    && event.key === 'Enter'
    && !props.busy) {
    event.preventDefault()
    emit('send')
  }
}
</script>

<template>
  <div class="chat-composer">
    <ElInput
      :model-value="modelValue"
      type="textarea"
      :autosize="{ minRows: 2, maxRows: 6 }"
      maxlength="10000"
      show-word-limit
      resize="none"
      placeholder="输入消息"
      aria-label="消息内容"
      :disabled="busy"
      @update:model-value="emit('update:modelValue', String($event))"
      @keydown="handleKeydown"
    />
    <button
      v-if="busy"
      class="chat-composer__button chat-composer__button--stop"
      type="button"
      @click="emit('stop')"
    >
      <VideoPause aria-hidden="true" />
      <span>停止</span>
    </button>
    <button
      v-else
      class="chat-composer__button chat-composer__button--send"
      type="button"
      :disabled="!modelValue.trim()"
      @click="emit('send')"
    >
      <Promotion aria-hidden="true" />
      <span>发送</span>
    </button>
  </div>
</template>

<style scoped>
.chat-composer {
  display: grid;
  width: min(100%, 900px);
  margin: 0 auto;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: var(--space-3);
}

.chat-composer__button {
  display: inline-flex;
  min-width: 82px;
  height: 40px;
  padding: 0 var(--space-4);
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  color: #ffffff;
  border: 0;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-weight: 600;
  cursor: pointer;
}

.chat-composer__button svg {
  width: 16px;
  height: 16px;
}

.chat-composer__button--send {
  background: var(--color-brand-600);
}

.chat-composer__button--send:hover:not(:disabled) {
  background: var(--color-brand-700);
}

.chat-composer__button--send:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.chat-composer__button--stop {
  background: var(--color-danger);
}

@media (max-width: 768px) {
  .chat-composer {
    grid-template-columns: 1fr;
  }

  .chat-composer__button {
    width: 100%;
  }
}
</style>
