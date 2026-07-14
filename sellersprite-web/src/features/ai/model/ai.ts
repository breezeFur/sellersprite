export interface AiConversationSummary {
  conversationId: string
  title: string
  provider: string
  model: string
  messageCount: number
  lastMessageAt: number
  status: string
  createdAt: number
  updatedAt: number
}

export interface AiConversationMessage {
  messageId: string
  promptRecordId: string | null
  sequenceNo: number
  role: 'SYSTEM' | 'USER' | 'ASSISTANT' | 'TOOL' | string
  content: string
  contentType: string
  metadata: string
  messageStatus: 'STREAMING' | 'COMPLETED' | 'CANCELLED' | 'FAILED' | string
  errorCode: string
  errorMessage: string
  createdAt: number
  retryable?: boolean
}

export interface AiConversationSettings {
  provider: string
  model: string
  systemPrompt: string | null
}

export interface AiConversationDetail {
  conversation: AiConversationSummary
  messages: AiConversationMessage[]
  settings: AiConversationSettings
}

export interface AiChatRequest {
  conversationId?: string
  prompt: string
  systemPrompt?: string
}

export interface AiChatResult {
  conversationId: string
  messageId: string
  promptRecordId: string
  content: string
  provider: string
  model: string
  createdAt: number
  promptTokens: number | null
  completionTokens: number | null
  totalTokens: number | null
  finishReason: string | null
}

export type AiStreamEvent =
  | { event: 'conversation'; data: { conversationId: string; userMessageId: string } }
  | { event: 'delta'; data: { content: string } }
  | { event: 'done'; data: { chat: AiChatResult } }
  | { event: 'error'; data: { code: string; message: string; trackId: string; retryable: boolean } }
