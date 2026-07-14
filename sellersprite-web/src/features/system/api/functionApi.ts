import { apiClient } from '@/shared/api/http'

import type { FunctionPayload, SystemFunction } from '../model/system'

export function getFunctionTree() {
  return apiClient.request<SystemFunction[]>({ method: 'GET', url: '/permissions/functions/tree' })
}

export function createFunction(data: FunctionPayload) {
  return apiClient.request<SystemFunction>({ method: 'POST', url: '/permissions/functions', data })
}

export function updateFunction(functionId: string, data: FunctionPayload) {
  return apiClient.request<SystemFunction>({ method: 'PUT', url: `/permissions/functions/${functionId}`, data })
}

export function updateFunctionStatus(functionId: string, status: number) {
  return apiClient.request<void>({ method: 'PUT', url: `/permissions/functions/${functionId}/status`, data: { status } })
}

export function deleteFunction(functionId: string) {
  return apiClient.request<void>({ method: 'DELETE', url: `/permissions/functions/${functionId}` })
}

export function getFunctionApis(functionId: string) {
  return apiClient.request<string[]>({ method: 'GET', url: `/permissions/functions/${functionId}/apis` })
}

export function replaceFunctionApis(functionId: string, apiIds: string[]) {
  return apiClient.request<void>({ method: 'PUT', url: `/permissions/functions/${functionId}/apis`, data: { apiIds } })
}
