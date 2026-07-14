import { ApiError } from '@/shared/api/ApiError'
import { postSseJson } from '@/shared/sse/postSse'

import type { AiChatRequest, AiStreamEvent } from '../model/ai'

interface AiStreamOptions {
  signal: AbortSignal
  getAccessToken: () => string | null
  refreshAccessToken: () => Promise<string>
  onEvent: (event: AiStreamEvent) => void
}

export function streamChat(request: AiChatRequest, options: AiStreamOptions) {
  return stream('/api/ai/chat/stream', request, options)
}

export function retryChat(conversationId: string, messageId: string, options: AiStreamOptions) {
  return stream(
    `/api/ai/conversations/${encodeURIComponent(conversationId)}/messages/${encodeURIComponent(messageId)}/retry`,
    undefined,
    options,
  )
}

function stream(url: string, body: unknown, options: AiStreamOptions) {
  return postSseJson({
    url,
    body,
    signal: options.signal,
    getAccessToken: options.getAccessToken,
    refreshAccessToken: options.refreshAccessToken,
    onEvent(message) {
      if (!['conversation', 'delta', 'done', 'error'].includes(message.event)) {
        return
      }
      try {
        options.onEvent({
          event: message.event,
          data: JSON.parse(message.data),
        } as AiStreamEvent)
      } catch (error) {
        throw new ApiError('SSE_INVALID_EVENT', '流式响应格式不正确', { cause: error })
      }
    },
  })
}
