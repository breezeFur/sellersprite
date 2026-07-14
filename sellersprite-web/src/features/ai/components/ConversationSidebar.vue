<script setup lang="ts">
import { Delete, EditPen, Plus, Search } from '@element-plus/icons-vue'

import type { AiConversationSummary } from '../model/ai'

defineProps<{
  conversations: AiConversationSummary[]
  selectedId: string | null
  searchTerm: string
  loading: boolean
  busy: boolean
}>()

const emit = defineEmits<{
  new: []
  select: [conversationId: string]
  rename: [conversation: AiConversationSummary]
  delete: [conversation: AiConversationSummary]
  search: []
  'update:searchTerm': [value: string]
}>()

function formatTime(timestamp: number) {
  if (!timestamp) {
    return '--'
  }
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(timestamp))
}
</script>

<template>
  <aside
    class="conversation-sidebar"
    aria-label="AI 会话"
  >
    <div class="conversation-sidebar__header">
      <h2>会话</h2>
      <button
        class="conversation-sidebar__new"
        type="button"
        title="新建对话"
        aria-label="新建对话"
        :disabled="busy"
        @click="emit('new')"
      >
        <Plus aria-hidden="true" />
      </button>
    </div>

    <ElInput
      :model-value="searchTerm"
      class="conversation-sidebar__search"
      clearable
      placeholder="搜索会话"
      aria-label="搜索会话"
      @update:model-value="emit('update:searchTerm', String($event))"
      @keyup.enter="emit('search')"
      @clear="emit('search')"
    >
      <template #suffix>
        <button
          class="conversation-sidebar__search-button"
          type="button"
          title="查询会话"
          aria-label="查询会话"
          @click="emit('search')"
        >
          <Search aria-hidden="true" />
        </button>
      </template>
    </ElInput>

    <div
      v-if="loading"
      class="conversation-sidebar__loading"
      aria-busy="true"
    >
      正在加载
    </div>
    <div
      v-else-if="conversations.length === 0"
      class="conversation-sidebar__empty"
    >
      暂无会话
    </div>
    <ul
      v-else
      class="conversation-sidebar__list"
    >
      <li
        v-for="conversation in conversations"
        :key="conversation.conversationId"
        class="conversation-item"
        :class="{ 'is-active': selectedId === conversation.conversationId }"
      >
        <button
          class="conversation-item__select"
          type="button"
          :disabled="busy"
          @click="emit('select', conversation.conversationId)"
        >
          <strong>{{ conversation.title || '未命名会话' }}</strong>
          <span>{{ formatTime(conversation.lastMessageAt || conversation.updatedAt) }}</span>
        </button>
        <div class="conversation-item__actions">
          <button
            type="button"
            title="重命名会话"
            aria-label="重命名会话"
            :disabled="busy"
            @click="emit('rename', conversation)"
          >
            <EditPen aria-hidden="true" />
          </button>
          <button
            type="button"
            title="删除会话"
            aria-label="删除会话"
            :disabled="busy"
            @click="emit('delete', conversation)"
          >
            <Delete aria-hidden="true" />
          </button>
        </div>
      </li>
    </ul>
  </aside>
</template>

<style scoped>
.conversation-sidebar {
  display: grid;
  min-width: 0;
  min-height: 0;
  padding: var(--space-4);
  grid-template-rows: 36px auto minmax(0, 1fr);
  gap: var(--space-3);
  background: var(--color-surface-muted);
}

.conversation-sidebar__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.conversation-sidebar__header h2 {
  margin: 0;
  color: var(--color-text);
  font-size: var(--font-size-lg);
  font-weight: 650;
}

.conversation-sidebar__new,
.conversation-sidebar__search-button,
.conversation-item__actions button {
  display: grid;
  width: 30px;
  height: 30px;
  padding: 7px;
  place-items: center;
  color: var(--color-text-secondary);
  background: transparent;
  border: 0;
  border-radius: var(--radius-md);
  cursor: pointer;
}

.conversation-sidebar__new:hover:not(:disabled),
.conversation-sidebar__search-button:hover,
.conversation-item__actions button:hover:not(:disabled) {
  color: var(--color-brand-700);
  background: var(--color-brand-100);
}

.conversation-sidebar__new:disabled,
.conversation-item__actions button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.conversation-sidebar__new svg,
.conversation-sidebar__search-button svg,
.conversation-item__actions svg {
  width: 16px;
  height: 16px;
}

.conversation-sidebar__search-button {
  width: 26px;
  height: 26px;
  padding: 5px;
}

.conversation-sidebar__loading,
.conversation-sidebar__empty {
  display: grid;
  min-height: 120px;
  place-items: center;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.conversation-sidebar__list {
  min-height: 0;
  margin: 0 -4px;
  padding: 0 4px;
  list-style: none;
  overflow: auto;
}

.conversation-item {
  display: grid;
  min-width: 0;
  margin-bottom: var(--space-1);
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  border: 1px solid transparent;
  border-radius: var(--radius-md);
}

.conversation-item:hover {
  background: var(--color-surface);
  border-color: var(--color-border);
}

.conversation-item.is-active {
  background: var(--color-brand-50);
  border-color: var(--color-brand-100);
}

.conversation-item__select {
  display: flex;
  min-width: 0;
  padding: var(--space-3);
  align-items: flex-start;
  flex-direction: column;
  color: inherit;
  background: transparent;
  border: 0;
  cursor: pointer;
  text-align: left;
}

.conversation-item__select:disabled {
  cursor: not-allowed;
}

.conversation-item__select strong {
  width: 100%;
  overflow: hidden;
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: 550;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conversation-item__select span {
  margin-top: 3px;
  color: var(--color-text-muted);
  font-size: 10px;
  font-variant-numeric: tabular-nums;
}

.conversation-item__actions {
  display: flex;
  padding-right: var(--space-1);
  opacity: 0;
}

.conversation-item:hover .conversation-item__actions,
.conversation-item:focus-within .conversation-item__actions,
.conversation-item.is-active .conversation-item__actions {
  opacity: 1;
}
</style>
