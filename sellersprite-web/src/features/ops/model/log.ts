export interface LoginLog {
  loginLogId: string
  userId: string
  username: string
  loginType: string
  success: number
  errorCode: string
  failureReason: string
  loginIp: string
  loginLocation: string
  userAgent: string
  deviceName: string
  clientType: string
  trackId: string
  createdAt: number
}

export interface OperationLog {
  operationLogId: string
  userId: string
  username: string
  moduleName: string
  operationName: string
  operationType: string
  httpMethod: string
  requestUri: string
  requestParams: string
  responsePayload: string
  responseStatus: number
  success: number
  errorMessage: string
  clientIp: string
  userAgent: string
  costMs: number
  trackId: string
  createdAt: number
}

export interface AiPromptLog {
  promptRecordId: string
  conversationId: string
  userId: string
  provider: string
  model: string
  requestMessages: string
  promptSummary: string
  promptTruncated: number
  responseContent: string
  responseMetadata: string
  promptTokens: number
  completionTokens: number
  totalTokens: number
  finishReason: string
  status: string
  errorType: string
  errorMessage: string
  costMs: number
  trackId: string
  createdAt: number
}

export interface LoginLogQuery {
  current: number
  size: number
  userId?: string
  username?: string
  success?: number
  loginIp?: string
  startTime?: number
  endTime?: number
}

export interface OperationLogQuery {
  current: number
  size: number
  userId?: string
  username?: string
  moduleName?: string
  operationType?: string
  success?: number
  trackId?: string
  startTime?: number
  endTime?: number
}

export interface AiPromptLogQuery {
  current: number
  size: number
  userId?: string
  conversationId?: string
  provider?: string
  model?: string
  status?: string
  startTime?: number
  endTime?: number
}
