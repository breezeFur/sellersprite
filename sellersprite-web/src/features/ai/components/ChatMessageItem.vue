<script setup lang="ts">
import { ChatDotRound, CopyDocument, RefreshRight, UserFilled } from '@element-plus/icons-vue'

import type { AiConversationMessage } from '../model/ai'
import SafeMarkdown from './SafeMarkdown.vue'

const props = defineProps<{
  message: AiConversationMessage
}>()

const emit = defineEmits<{
  copy: [content: string]
  retry: [message: AiConversationMessage]
}>()

function formatTime(timestamp: number) {
  if (!timestamp) {
    return ''
  }
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(timestamp))
}

function statusLabel(status: string) {
  const labels: Record<string, string> = {
    STREAMING: '生成中',
    CANCELLED: '已停止',
    FAILED: '生成失败',
  }
  return labels[status] ?? ''
}

function canRetry() {
  return props.message.role === 'ASSISTANT'
    && ['FAILED', 'CANCELLED'].includes(props.message.messageStatus)
}
</script>

<template>
  <article
    class="chat-message"
    :class="message.role === 'USER' ? 'chat-message--user' : 'chat-message--assistant'"
  >
    <span
      class="chat-message__avatar"
      aria-hidden="true"
    >
      <UserFilled v-if="message.role === 'USER'" />
      <ChatDotRound v-else />
    </span>
    <div class="chat-message__body">
      <header class="chat-message__header">
        <strong>{{ message.role === 'USER' ? '你' : 'AI 助手' }}</strong>
        <time v-if="message.createdAt">{{ formatTime(message.createdAt) }}</time>
        <span
          v-if="statusLabel(message.messageStatus)"
          class="chat-message__status"
          :class="`is-${message.messageStatus.toLowerCase()}`"
        >
          {{ statusLabel(message.messageStatus) }}
        </span>
      </header>

      <p
        v-if="message.role === 'USER'"
        class="chat-message__user-content"
      >
        {{ message.content }}
      </p>
      <SafeMarkdown
        v-else-if="message.content"
        :content="message.content"
      />
      <span
        v-else-if="message.messageStatus === 'STREAMING'"
        class="chat-message__typing"
        aria-label="正在生成回复"
      />

      <p
        v-if="message.errorMessage"
        class="chat-message__error"
      >
        {{ message.errorMessage }}
        <code v-if="message.errorCode">{{ message.errorCode }}</code>
      </p>

      <div
        v-if="message.role !== 'USER'"
        class="chat-message__actions"
      >
        <button
          v-if="message.content"
          type="button"
          title="复制回复"
          aria-label="复制回复"
          @click="emit('copy', message.content)"
        >
          <CopyDocument aria-hidden="true" />
        </button>
        <button
          v-if="canRetry()"
          type="button"
          title="重试回复"
          aria-label="重试回复"
          @click="emit('retry', message)"
        >
          <RefreshRight aria-hidden="true" />
        </button>
      </div>
    </div>
  </article>
</template>

<style scoped>
.chat-message {
  display: grid;
  width: min(100%, 880px);
  margin: 0 auto var(--space-5);
  grid-template-columns: 34px minmax(0, 1fr);
  align-items: start;
  gap: var(--space-3);
}

.chat-message__avatar {
  display: grid;
  width: 34px;
  height: 34px;
  padding: 8px;
  place-items: center;
  color: var(--color-brand-700);
  background: var(--color-brand-100);
  border-radius: var(--radius-md);
}

.chat-message--user .chat-message__avatar {
  color: #334155;
  background: #e2e8f0;
}

.chat-message__avatar svg {
  width: 18px;
  height: 18px;
}

.chat-message__body {
  position: relative;
  min-width: 0;
  padding: var(--space-3) var(--space-4);
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
}

.chat-message--user .chat-message__body {
  background: var(--color-brand-50);
  border-color: var(--color-brand-100);
}

.chat-message__header {
  display: flex;
  min-height: 22px;
  margin-bottom: var(--space-2);
  align-items: center;
  gap: var(--space-2);
}

.chat-message__header strong {
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: 650;
}

.chat-message__header time {
  color: var(--color-text-muted);
  font-size: 10px;
  font-variant-numeric: tabular-nums;
}

.chat-message__status {
  color: var(--color-text-muted);
  font-size: 10px;
}

.chat-message__status.is-failed {
  color: var(--color-danger);
}

.chat-message__status.is-cancelled {
  color: var(--color-warning);
}

.chat-message__user-content {
  margin: 0;
  line-height: 1.72;
  overflow-wrap: anywhere;
  white-space: pre-wrap;
}

.chat-message__error {
  margin: var(--space-3) 0 0;
  padding-top: var(--space-3);
  color: var(--color-danger);
  border-top: 1px solid #fecaca;
  font-size: var(--font-size-xs);
}

.chat-message__error code {
  margin-left: var(--space-2);
  font-family: var(--font-mono);
}

.chat-message__typing {
  display: block;
  width: 28px;
  height: 10px;
  background: radial-gradient(circle, var(--color-brand-500) 2px, transparent 3px) 0 50% / 10px 10px repeat-x;
  animation: chat-message-pulse 900ms ease-in-out infinite alternate;
}

.chat-message__actions {
  display: flex;
  min-height: 28px;
  margin-top: var(--space-2);
  gap: var(--space-1);
}

.chat-message__actions button {
  display: grid;
  width: 28px;
  height: 28px;
  padding: 6px;
  place-items: center;
  color: var(--color-text-muted);
  background: transparent;
  border: 0;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.chat-message__actions button:hover {
  color: var(--color-brand-700);
  background: var(--color-brand-50);
}

.chat-message__actions svg {
  width: 15px;
  height: 15px;
}

@keyframes chat-message-pulse {
  from {
    opacity: 0.45;
  }

  to {
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .chat-message {
    grid-template-columns: 28px minmax(0, 1fr);
    gap: var(--space-2);
  }

  .chat-message__avatar {
    width: 28px;
    height: 28px;
    padding: 6px;
  }

  .chat-message__body {
    padding: var(--space-3);
  }
}
</style>
