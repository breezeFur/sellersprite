import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

import { useAuthStore } from '@/features/auth/stores/useAuthStore'

import * as conversationApi from '../api/aiConversationApi'
import * as streamApi from '../api/aiStreamApi'
import type { AiStreamEvent } from '../model/ai'
import AiChatPage from './AiChatPage.vue'

vi.mock('../api/aiConversationApi', () => ({
  pageConversations: vi.fn(),
  getConversation: vi.fn(),
  renameConversation: vi.fn(),
  updateConversationSettings: vi.fn(),
  deleteConversation: vi.fn(),
}))

vi.mock('../api/aiStreamApi', () => ({
  streamChat: vi.fn(),
  retryChat: vi.fn(),
}))

function setup() {
  const pinia = createPinia()
  setActivePinia(pinia)
  const authStore = useAuthStore()
  authStore.status = 'authenticated'
  authStore.accessToken = 'access-token'
  return mount(AiChatPage, {
    global: { plugins: [pinia] },
  })
}

describe('AiChatPage', () => {
  beforeEach(() => {
    vi.mocked(conversationApi.pageConversations).mockReset().mockResolvedValue({
      current: 1,
      size: 30,
      total: 0,
      records: [],
    })
    vi.mocked(conversationApi.getConversation).mockReset()
    vi.mocked(streamApi.streamChat).mockReset()
    vi.mocked(streamApi.retryChat).mockReset()
  })

  it('streams a new conversation into the message view', async () => {
    vi.mocked(streamApi.streamChat).mockImplementation(async (_request, options) => {
      options.onEvent({
        event: 'conversation',
        data: { conversationId: 'conversation-1', userMessageId: 'user-message-1' },
      })
      options.onEvent({ event: 'delta', data: { content: '你好，' } })
      options.onEvent({ event: 'delta', data: { content: '**管理员**' } })
      options.onEvent({
        event: 'done',
        data: {
          chat: {
            conversationId: 'conversation-1',
            messageId: 'assistant-message-1',
            promptRecordId: 'prompt-1',
            content: '你好，**管理员**',
            provider: 'openai',
            model: 'test-model',
            createdAt: 1,
            promptTokens: 2,
            completionTokens: 3,
            totalTokens: 5,
            finishReason: 'stop',
          },
        },
      })
    })

    const wrapper = setup()
    await flushPromises()
    expect(wrapper.text()).toContain('开始新对话')

    await wrapper.get('textarea[aria-label="消息内容"]').setValue('你好')
    await wrapper.get('.chat-composer__button--send').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('你好')
    expect(wrapper.find('.safe-markdown strong').text()).toBe('管理员')
    expect(streamApi.streamChat).toHaveBeenCalledTimes(1)
  })

  it('opens an existing conversation at the bottom after its messages render', async () => {
    const conversation = {
      conversationId: 'conversation-1',
      title: '已有会话',
      provider: 'openai',
      model: 'test-model',
      messageCount: 1,
      lastMessageAt: 2,
      status: 'ACTIVE',
      createdAt: 1,
      updatedAt: 2,
    }
    vi.mocked(conversationApi.pageConversations).mockResolvedValue({
      current: 1,
      size: 30,
      total: 1,
      records: [conversation],
    })
    vi.mocked(conversationApi.getConversation).mockResolvedValue({
      conversation,
      settings: { provider: 'openai', model: 'test-model', systemPrompt: '' },
      messages: [
        {
          messageId: 'assistant-message-1',
          promptRecordId: 'prompt-1',
          sequenceNo: 1,
          role: 'ASSISTANT',
          content: '已有的长回复',
          contentType: 'TEXT',
          metadata: '{}',
          messageStatus: 'COMPLETED',
          errorCode: '',
          errorMessage: '',
          createdAt: 2,
        },
      ],
    })

    const wrapper = setup()
    const messages = wrapper.get('.ai-chat__messages').element as HTMLElement
    Object.defineProperty(messages, 'scrollHeight', {
      configurable: true,
      get: () => messages.querySelector('article') ? 500 : 100,
    })
    Object.defineProperty(messages, 'clientHeight', { configurable: true, value: 100 })
    await flushPromises()
    await nextTick()

    expect(wrapper.find('.ai-chat__messages article').exists()).toBe(true)
    expect(messages.scrollTop).toBe(500)
  })

  it('auto-follows a streaming reply unless the user scrolls away from the bottom', async () => {
    const stream = {} as {
      emit: (event: AiStreamEvent) => void
      finish: () => void
    }
    vi.mocked(streamApi.streamChat).mockImplementation((_request, options) => {
      stream.emit = options.onEvent
      return new Promise<void>((resolve) => {
        stream.finish = resolve
      })
    })

    const wrapper = setup()
    await flushPromises()
    const messages = wrapper.get('.ai-chat__messages').element as HTMLElement
    let scrollHeight = 500
    Object.defineProperty(messages, 'scrollHeight', {
      configurable: true,
      get: () => scrollHeight,
    })
    Object.defineProperty(messages, 'clientHeight', { configurable: true, value: 100 })

    await wrapper.get('textarea[aria-label="消息内容"]').setValue('开始')
    await wrapper.get('.chat-composer__button--send').trigger('click')
    await nextTick()
    await nextTick()
    expect(messages.scrollTop).toBe(500)
    expect(stream.emit).toBeTypeOf('function')

    messages.scrollTop = 120
    await wrapper.get('.ai-chat__messages').trigger('scroll')
    scrollHeight = 600
    stream.emit({ event: 'delta', data: { content: '第一段' } })
    await nextTick()
    await nextTick()
    expect(messages.scrollTop).toBe(120)

    messages.scrollTop = 500
    await wrapper.get('.ai-chat__messages').trigger('scroll')
    scrollHeight = 700
    stream.emit({ event: 'delta', data: { content: '第二段' } })
    await nextTick()
    await nextTick()
    expect(messages.scrollTop).toBe(700)

    stream.emit({
      event: 'done',
      data: {
        chat: {
          conversationId: 'conversation-1',
          messageId: 'assistant-message-1',
          promptRecordId: 'prompt-1',
          content: '第一段第二段',
          provider: 'openai',
          model: 'test-model',
          createdAt: 1,
          promptTokens: 2,
          completionTokens: 3,
          totalTokens: 5,
          finishReason: 'stop',
        },
      },
    })
    stream.finish()
    await flushPromises()
  })

  it('marks a partial reply as cancelled after the user stops streaming', async () => {
    vi.mocked(streamApi.streamChat).mockImplementation((_request, options) => {
      options.onEvent({
        event: 'conversation',
        data: { conversationId: 'conversation-1', userMessageId: 'user-message-1' },
      })
      options.onEvent({ event: 'delta', data: { content: '部分回复' } })
      return new Promise<void>((_resolve, reject) => {
        options.signal.addEventListener('abort', () => reject(new DOMException('aborted', 'AbortError')))
      })
    })

    const wrapper = setup()
    await flushPromises()
    await wrapper.get('textarea[aria-label="消息内容"]').setValue('开始')
    await wrapper.get('.chat-composer__button--send').trigger('click')
    await flushPromises()
    await wrapper.get('.chat-composer__button--stop').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('部分回复')
    expect(wrapper.text()).toContain('已停止')
  })

  it('retries the last cancelled assistant message', async () => {
    const conversation = {
      conversationId: 'conversation-1',
      title: '已有会话',
      provider: 'openai',
      model: 'test-model',
      messageCount: 2,
      lastMessageAt: 2,
      status: 'ACTIVE',
      createdAt: 1,
      updatedAt: 2,
    }
    vi.mocked(conversationApi.pageConversations).mockResolvedValue({
      current: 1,
      size: 30,
      total: 1,
      records: [conversation],
    })
    vi.mocked(conversationApi.getConversation).mockResolvedValue({
      conversation,
      settings: { provider: 'openai', model: 'test-model', systemPrompt: '' },
      messages: [
        {
          messageId: 'assistant-cancelled',
          promptRecordId: 'prompt-old',
          sequenceNo: 2,
          role: 'ASSISTANT',
          content: '部分内容',
          contentType: 'TEXT',
          metadata: '{}',
          messageStatus: 'CANCELLED',
          errorCode: 'CANCELLED',
          errorMessage: '',
          createdAt: 2,
        },
      ],
    })
    vi.mocked(streamApi.retryChat).mockImplementation(async (_conversationId, _messageId, options) => {
      options.onEvent({
        event: 'done',
        data: {
          chat: {
            conversationId: 'conversation-1',
            messageId: 'assistant-retried',
            promptRecordId: 'prompt-new',
            content: '重试成功',
            provider: 'openai',
            model: 'test-model',
            createdAt: 3,
            promptTokens: 1,
            completionTokens: 1,
            totalTokens: 2,
            finishReason: 'stop',
          },
        },
      })
    })

    const wrapper = setup()
    await flushPromises()
    await wrapper.get('[aria-label="重试回复"]').trigger('click')
    await flushPromises()

    expect(streamApi.retryChat).toHaveBeenCalledWith(
      'conversation-1',
      'assistant-cancelled',
      expect.objectContaining({ onEvent: expect.any(Function) }),
    )
    expect(wrapper.text()).toContain('重试成功')
  })
})
