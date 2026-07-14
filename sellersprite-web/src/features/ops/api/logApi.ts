import { apiClient } from '@/shared/api/http'
import type { PageResult } from '@/shared/api/types'

import type {
  AiPromptLog,
  AiPromptLogQuery,
  LoginLog,
  LoginLogQuery,
  OperationLog,
  OperationLogQuery,
} from '../model/log'

export function pageLoginLogs(params: LoginLogQuery) {
  return apiClient.request<PageResult<LoginLog>>({ method: 'GET', url: '/logs/login', params })
}

export function getLoginLog(loginLogId: string) {
  return apiClient.request<LoginLog>({ method: 'GET', url: `/logs/login/${loginLogId}` })
}

export function pageOperationLogs(params: OperationLogQuery) {
  return apiClient.request<PageResult<OperationLog>>({ method: 'GET', url: '/logs/operation', params })
}

export function getOperationLog(operationLogId: string) {
  return apiClient.request<OperationLog>({ method: 'GET', url: `/logs/operation/${operationLogId}` })
}

export function pageAiPromptLogs(params: AiPromptLogQuery) {
  return apiClient.request<PageResult<AiPromptLog>>({ method: 'GET', url: '/logs/ai-prompts', params })
}

export function getAiPromptLog(promptRecordId: string) {
  return apiClient.request<AiPromptLog>({ method: 'GET', url: `/logs/ai-prompts/${promptRecordId}` })
}
