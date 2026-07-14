import { apiClient } from '@/shared/api/http'
import type { PageResult } from '@/shared/api/types'

import type { AiConversationDetail, AiConversationSettings, AiConversationSummary } from '../model/ai'

export function pageConversations(params: { current: number; size: number; title?: string }) {
  return apiClient.request<PageResult<AiConversationSummary>>({
    method: 'GET',
    url: '/ai/conversations',
    params,
  })
}

export function getConversation(conversationId: string) {
  return apiClient.request<AiConversationDetail>({
    method: 'GET',
    url: `/ai/conversations/${conversationId}`,
  })
}

export function renameConversation(conversationId: string, title: string) {
  return apiClient.request<AiConversationSummary>({
    method: 'PUT',
    url: `/ai/conversations/${conversationId}`,
    data: { title },
  })
}

export function updateConversationSettings(conversationId: string, systemPrompt: string) {
  return apiClient.request<AiConversationSettings>({
    method: 'PUT',
    url: `/ai/conversations/${conversationId}/settings`,
    data: { systemPrompt },
  })
}

export function deleteConversation(conversationId: string) {
  return apiClient.request<void>({
    method: 'DELETE',
    url: `/ai/conversations/${conversationId}`,
  })
}
