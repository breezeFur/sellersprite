<script setup lang="ts">
import { Menu, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import { ApiError } from '@/shared/api/ApiError'
import StatePanel from '@/shared/components/StatePanel.vue'
import { useAutoFollowScroll } from '@/shared/composables/useAutoFollowScroll'

import {
  deleteConversation,
  getConversation,
  pageConversations,
  renameConversation,
  updateConversationSettings,
} from '../api/aiConversationApi'
import { retryChat, streamChat } from '../api/aiStreamApi'
import ChatComposer from '../components/ChatComposer.vue'
import ChatMessageItem from '../components/ChatMessageItem.vue'
import ConversationSidebar from '../components/ConversationSidebar.vue'
import type {
  AiConversationMessage,
  AiConversationSettings,
  AiConversationSummary,
  AiStreamEvent,
} from '../model/ai'

const authStore = useAuthStore()
const conversations = ref<AiConversationSummary[]>([])
const conversationsLoading = ref(false)
const conversationError = ref('')
const searchTerm = ref('')
const selectedId = ref<string | null>(null)
const currentConversation = ref<AiConversationSummary | null>(null)
const messages = ref<AiConversationMessage[]>([])
const settings = ref<AiConversationSettings>(emptySettings())
const detailLoading = ref(false)
const composerText = ref('')
const streaming = ref(false)
const settingsOpen = ref(false)
const mobileConversationsOpen = ref(false)
const {
  handleScroll: handleMessagesScroll,
  resetAutoFollow: resetMessagesAutoFollow,
  setScrollContainer: setMessagesContainer,
  scrollToBottom: scrollMessagesToEnd,
} = useAutoFollowScroll()
let activeController: AbortController | null = null
let activeAssistantId: string | null = null
let activeUserId: string | null = null
let temporarySequence = 0
let terminalReceived = false

const pageTitle = computed(() => currentConversation.value?.title || '新对话')
const modelLabel = computed(() => {
  const provider = settings.value.provider
  const model = settings.value.model
  return [provider, model].filter(Boolean).join(' / ') || '默认模型'
})

async function loadConversations(selectFirst = false) {
  conversationsLoading.value = true
  conversationError.value = ''
  try {
    const page = await pageConversations({
      current: 1,
      size: 30,
      title: searchTerm.value.trim() || undefined,
    })
    conversations.value = page.records
    if (selectFirst && !selectedId.value && page.records[0]) {
      await selectConversation(page.records[0].conversationId)
    }
  } catch (error) {
    conversationError.value = errorMessage(error, '会话列表加载失败')
  } finally {
    conversationsLoading.value = false
  }
}

async function selectConversation(conversationId: string) {
  if (streaming.value || selectedId.value === conversationId && currentConversation.value) {
    mobileConversationsOpen.value = false
    return
  }
  selectedId.value = conversationId
  detailLoading.value = true
  mobileConversationsOpen.value = false
  let loaded = false
  try {
    const detail = await getConversation(conversationId)
    currentConversation.value = detail.conversation
    messages.value = detail.messages
    settings.value = detail.settings
    loaded = true
  } catch (error) {
    ElMessage.error(errorMessage(error, '会话详情加载失败'))
  } finally {
    detailLoading.value = false
  }
  if (loaded) {
    await scrollMessagesToEnd(true)
  }
}

function startNewConversation() {
  if (streaming.value) {
    return
  }
  selectedId.value = null
  currentConversation.value = null
  messages.value = []
  settings.value = emptySettings()
  composerText.value = ''
  mobileConversationsOpen.value = false
  resetMessagesAutoFollow()
}

async function sendMessage() {
  const prompt = composerText.value.trim()
  if (!prompt || streaming.value) {
    return
  }

  const userMessage = temporaryMessage('USER', prompt)
  const assistantMessage = temporaryMessage('ASSISTANT', '', 'STREAMING')
  messages.value.push(userMessage, assistantMessage)
  activeUserId = userMessage.messageId
  activeAssistantId = assistantMessage.messageId
  composerText.value = ''
  await scrollMessagesToEnd(true)

  await executeStream((options) => streamChat({
    conversationId: selectedId.value || undefined,
    prompt,
    systemPrompt: selectedId.value ? undefined : settings.value.systemPrompt || undefined,
  }, options), prompt)
}

async function retryMessage(message: AiConversationMessage) {
  if (!selectedId.value || streaming.value) {
    return
  }
  const assistantMessage = temporaryMessage('ASSISTANT', '', 'STREAMING')
  messages.value.push(assistantMessage)
  activeUserId = null
  activeAssistantId = assistantMessage.messageId
  await scrollMessagesToEnd(true)
  const conversationId = selectedId.value
  await executeStream(
    (options) => retryChat(conversationId, message.messageId, options),
    currentConversation.value?.title || '重试回复',
  )
}

async function executeStream(
  invoke: (options: {
    signal: AbortSignal
    getAccessToken: () => string | null
    refreshAccessToken: () => Promise<string>
    onEvent: (event: AiStreamEvent) => void
  }) => Promise<void>,
  fallbackTitle: string,
) {
  const controller = new AbortController()
  activeController = controller
  terminalReceived = false
  streaming.value = true
  try {
    await invoke({
      signal: controller.signal,
      getAccessToken: () => authStore.accessToken,
      refreshAccessToken: () => authStore.refreshAccessToken(),
      onEvent: (event) => handleStreamEvent(event, fallbackTitle),
    })
    if (!terminalReceived) {
      markActiveAssistantFailed('SSE_INCOMPLETE', '流式响应未返回完成事件')
    }
  } catch (error) {
    if (controller.signal.aborted) {
      markActiveAssistantCancelled()
    } else {
      markActiveAssistantFailed('STREAM_REQUEST_FAILED', errorMessage(error, '流式请求失败'))
    }
  } finally {
    streaming.value = false
    activeController = null
    activeAssistantId = null
    activeUserId = null
    await loadConversations(false)
  }
}

function handleStreamEvent(event: AiStreamEvent, fallbackTitle: string) {
  if (event.event === 'conversation') {
    selectedId.value = event.data.conversationId
    const userMessage = messages.value.find((message) => message.messageId === activeUserId)
    if (userMessage) {
      userMessage.messageId = event.data.userMessageId
    }
    if (!currentConversation.value) {
      currentConversation.value = {
        conversationId: event.data.conversationId,
        title: fallbackTitle.slice(0, 128),
        provider: '',
        model: '',
        messageCount: messages.value.length,
        lastMessageAt: Date.now(),
        status: 'ACTIVE',
        createdAt: Date.now(),
        updatedAt: Date.now(),
      }
    }
    return
  }

  const assistant = activeAssistant()
  if (!assistant) {
    return
  }
  if (event.event === 'delta') {
    assistant.content += event.data.content
  } else if (event.event === 'done') {
    terminalReceived = true
    assistant.messageId = event.data.chat.messageId
    assistant.promptRecordId = event.data.chat.promptRecordId
    assistant.content = event.data.chat.content
    assistant.messageStatus = 'COMPLETED'
    assistant.createdAt = event.data.chat.createdAt
    assistant.metadata = JSON.stringify({
      promptTokens: event.data.chat.promptTokens,
      completionTokens: event.data.chat.completionTokens,
      totalTokens: event.data.chat.totalTokens,
      finishReason: event.data.chat.finishReason,
    })
    settings.value.provider = event.data.chat.provider
    settings.value.model = event.data.chat.model
  } else if (event.event === 'error') {
    terminalReceived = true
    assistant.messageStatus = 'FAILED'
    assistant.errorCode = event.data.code
    assistant.errorMessage = event.data.message
    assistant.retryable = event.data.retryable
  }
  void scrollMessagesToEnd()
}

function stopStream() {
  if (!activeController) {
    return
  }
  markActiveAssistantCancelled()
  activeController.abort()
}

function markActiveAssistantCancelled() {
  const assistant = activeAssistant()
  if (assistant && assistant.messageStatus === 'STREAMING') {
    assistant.messageStatus = 'CANCELLED'
    assistant.errorCode = 'CANCELLED'
    assistant.errorMessage = ''
    assistant.retryable = true
    void scrollMessagesToEnd()
  }
  terminalReceived = true
}

function markActiveAssistantFailed(code: string, message: string) {
  const assistant = activeAssistant()
  if (assistant && assistant.messageStatus === 'STREAMING') {
    assistant.messageStatus = 'FAILED'
    assistant.errorCode = code
    assistant.errorMessage = message
    assistant.retryable = true
    void scrollMessagesToEnd()
  }
  terminalReceived = true
}

function activeAssistant() {
  return messages.value.find((message) => message.messageId === activeAssistantId)
}

async function copyMessage(content: string) {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}

async function renameItem(conversation: AiConversationSummary) {
  try {
    const result = await ElMessageBox.prompt('输入新的会话名称', '重命名会话', {
      inputValue: conversation.title,
      inputPattern: /^.{1,128}$/s,
      inputErrorMessage: '会话名称长度应为 1-128 个字符',
      confirmButtonText: '保存',
      cancelButtonText: '取消',
    })
    const updated = await renameConversation(conversation.conversationId, result.value.trim())
    replaceConversation(updated)
    if (currentConversation.value?.conversationId === updated.conversationId) {
      currentConversation.value = updated
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(errorMessage(error, '重命名失败'))
    }
  }
}

async function deleteItem(conversation: AiConversationSummary) {
  try {
    await ElMessageBox.confirm(`确认删除“${conversation.title || '未命名会话'}”？`, '删除会话', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteConversation(conversation.conversationId)
    if (selectedId.value === conversation.conversationId) {
      startNewConversation()
    }
    await loadConversations(true)
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(errorMessage(error, '删除会话失败'))
    }
  }
}

async function saveSettings() {
  try {
    if (selectedId.value) {
      settings.value = await updateConversationSettings(
        selectedId.value,
        settings.value.systemPrompt?.trim() || '',
      )
    }
    settingsOpen.value = false
    ElMessage.success('设置已保存')
  } catch (error) {
    ElMessage.error(errorMessage(error, '设置保存失败'))
  }
}

function replaceConversation(updated: AiConversationSummary) {
  const index = conversations.value.findIndex((item) => item.conversationId === updated.conversationId)
  if (index >= 0) {
    conversations.value[index] = updated
  }
}

function temporaryMessage(
  role: 'USER' | 'ASSISTANT',
  content: string,
  status = 'COMPLETED',
): AiConversationMessage {
  temporarySequence += 1
  return {
    messageId: `temporary-${Date.now()}-${temporarySequence}`,
    promptRecordId: null,
    sequenceNo: messages.value.length + 1,
    role,
    content,
    contentType: 'TEXT',
    metadata: '{}',
    messageStatus: status,
    errorCode: '',
    errorMessage: '',
    createdAt: Date.now(),
  }
}

function emptySettings(): AiConversationSettings {
  return { provider: '', model: '', systemPrompt: '' }
}

function errorMessage(error: unknown, fallback: string) {
  return error instanceof ApiError ? error.message : fallback
}

onMounted(() => loadConversations(true))
onBeforeUnmount(() => activeController?.abort())
</script>

<template>
  <section
    class="ai-chat"
    aria-label="AI 对话"
  >
    <ConversationSidebar
      class="ai-chat__desktop-sidebar"
      :conversations="conversations"
      :selected-id="selectedId"
      :search-term="searchTerm"
      :loading="conversationsLoading"
      :busy="streaming"
      @update:search-term="searchTerm = $event"
      @search="loadConversations(false)"
      @new="startNewConversation"
      @select="selectConversation"
      @rename="renameItem"
      @delete="deleteItem"
    />

    <div class="ai-chat__main">
      <header class="ai-chat__header">
        <button
          class="ai-chat__header-button ai-chat__mobile-conversations"
          type="button"
          title="打开会话列表"
          aria-label="打开会话列表"
          @click="mobileConversationsOpen = true"
        >
          <Menu aria-hidden="true" />
        </button>
        <div class="ai-chat__title">
          <h2>{{ pageTitle }}</h2>
          <p>{{ modelLabel }}</p>
        </div>
        <button
          class="ai-chat__header-button"
          type="button"
          title="会话设置"
          aria-label="会话设置"
          :disabled="streaming"
          @click="settingsOpen = true"
        >
          <Setting aria-hidden="true" />
        </button>
      </header>

      <ElAlert
        v-if="conversationError"
        class="ai-chat__alert"
        type="error"
        :title="conversationError"
        show-icon
        closable
        @close="conversationError = ''"
      />

      <div
        :ref="setMessagesContainer"
        class="ai-chat__messages"
        aria-live="polite"
        @scroll.passive="handleMessagesScroll"
      >
        <StatePanel
          v-if="detailLoading"
          class="ai-chat__state"
          status="loading"
          title="正在加载会话"
        />
        <StatePanel
          v-else-if="messages.length === 0"
          class="ai-chat__state"
          status="empty"
          title="开始新对话"
        />
        <template v-else>
          <ChatMessageItem
            v-for="message in messages"
            :key="message.messageId"
            :message="message"
            @copy="copyMessage"
            @retry="retryMessage"
          />
        </template>
      </div>

      <footer class="ai-chat__composer">
        <ChatComposer
          v-model="composerText"
          :busy="streaming"
          @send="sendMessage"
          @stop="stopStream"
        />
      </footer>
    </div>

    <ElDrawer
      v-model="mobileConversationsOpen"
      class="ai-chat__conversation-drawer"
      title="AI 会话列表"
      direction="ltr"
      size="320px"
    >
      <ConversationSidebar
        :conversations="conversations"
        :selected-id="selectedId"
        :search-term="searchTerm"
        :loading="conversationsLoading"
        :busy="streaming"
        @update:search-term="searchTerm = $event"
        @search="loadConversations(false)"
        @new="startNewConversation"
        @select="selectConversation"
        @rename="renameItem"
        @delete="deleteItem"
      />
    </ElDrawer>

    <ElDrawer
      v-model="settingsOpen"
      title="会话设置"
      size="420px"
    >
      <ElForm label-position="top">
        <ElFormItem label="服务提供方">
          <ElInput
            :model-value="settings.provider || '创建会话后确定'"
            readonly
          />
        </ElFormItem>
        <ElFormItem label="模型">
          <ElInput
            :model-value="settings.model || '使用服务端默认模型'"
            readonly
          />
        </ElFormItem>
        <ElFormItem label="系统提示词">
          <ElInput
            v-model="settings.systemPrompt"
            type="textarea"
            :rows="8"
            maxlength="2000"
            show-word-limit
            placeholder="使用默认助手设定"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="settingsOpen = false">
          取消
        </ElButton>
        <ElButton
          type="primary"
          @click="saveSettings"
        >
          保存
        </ElButton>
      </template>
    </ElDrawer>
  </section>
</template>

<style scoped>
.ai-chat {
  display: grid;
  height: calc(100vh - var(--header-height) - var(--content-gutter) * 2);
  min-height: 580px;
  grid-template-columns: 280px minmax(0, 1fr);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.ai-chat__desktop-sidebar {
  border-right: 1px solid var(--color-border);
}

.ai-chat__main {
  display: grid;
  min-width: 0;
  min-height: 0;
  grid-template-rows: 60px auto minmax(0, 1fr) auto;
  grid-template-areas:
    "header"
    "alert"
    "messages"
    "composer";
}

.ai-chat__header {
  display: flex;
  min-width: 0;
  padding: 0 var(--space-5);
  align-items: center;
  gap: var(--space-3);
  border-bottom: 1px solid var(--color-border);
  grid-area: header;
}

.ai-chat__title {
  min-width: 0;
  margin-right: auto;
}

.ai-chat__title h2,
.ai-chat__title p {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ai-chat__title h2 {
  color: var(--color-text);
  font-size: var(--font-size-lg);
  font-weight: 650;
}

.ai-chat__title p {
  margin-top: 2px;
  color: var(--color-text-muted);
  font-size: 10px;
}

.ai-chat__header-button {
  display: grid;
  flex: 0 0 32px;
  width: 32px;
  height: 32px;
  padding: 7px;
  place-items: center;
  color: var(--color-text-secondary);
  background: transparent;
  border: 0;
  border-radius: var(--radius-md);
  cursor: pointer;
}

.ai-chat__header-button:hover:not(:disabled) {
  color: var(--color-brand-700);
  background: var(--color-brand-50);
}

.ai-chat__header-button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.ai-chat__header-button svg {
  width: 18px;
  height: 18px;
}

.ai-chat__mobile-conversations {
  display: none;
}

.ai-chat__alert {
  margin: var(--space-3) var(--space-5) 0;
  width: auto;
  grid-area: alert;
}

.ai-chat__messages {
  min-width: 0;
  min-height: 0;
  padding: var(--space-6) var(--space-5);
  background: var(--color-surface-muted);
  grid-area: messages;
  overflow: auto;
}

.ai-chat__state {
  min-height: 100%;
}

.ai-chat__composer {
  padding: var(--space-4) var(--space-5);
  background: var(--color-surface);
  border-top: 1px solid var(--color-border);
  grid-area: composer;
}

:global(.ai-chat__conversation-drawer .el-drawer__body) {
  padding: 0;
}

:global(.ai-chat__conversation-drawer .conversation-sidebar) {
  height: 100%;
}

@media (max-width: 1024px) {
  .ai-chat {
    grid-template-columns: 240px minmax(0, 1fr);
  }
}

@media (max-width: 900px) {
  .ai-chat {
    grid-template-columns: 1fr;
  }

  .ai-chat__desktop-sidebar {
    display: none;
  }

  .ai-chat__mobile-conversations {
    display: grid;
  }
}

@media (max-width: 768px) {
  .ai-chat {
    min-height: 620px;
  }

  .ai-chat__header {
    padding: 0 var(--space-3);
  }

  .ai-chat__messages {
    padding: var(--space-4) var(--space-3);
  }

  .ai-chat__composer {
    padding: var(--space-3);
  }

  :global(.ai-chat__conversation-drawer) {
    width: min(88vw, 320px) !important;
  }
}
</style>
