import { apiClient } from '@/shared/api/http'
import type { PageResult } from '@/shared/api/types'
import { menuApiBindings } from '@/features/permission/config/menuApiBindings'

import type { ApiCatalogSyncResult, ApiResourcePayload, MenuApiBindingSyncResult, SystemApiResource } from '../model/system'

interface ApiResourceQuery {
  current: number
  size: number
  keyword?: string
  apiType?: string
  httpMethod?: string
  moduleName?: string
  status?: number
}

export function pageApiResources(params: ApiResourceQuery) {
  return apiClient.request<PageResult<SystemApiResource>>({ method: 'GET', url: '/permissions/apis', params })
}

export function createApiResource(data: ApiResourcePayload) {
  return apiClient.request<SystemApiResource>({ method: 'POST', url: '/permissions/apis', data })
}

export function updateApiResource(apiId: string, data: ApiResourcePayload) {
  return apiClient.request<SystemApiResource>({ method: 'PUT', url: `/permissions/apis/${apiId}`, data })
}

export function updateApiResourceStatus(apiId: string, status: number) {
  return apiClient.request<void>({ method: 'PUT', url: `/permissions/apis/${apiId}/status`, data: { status } })
}

export function deleteApiResource(apiId: string) {
  return apiClient.request<void>({ method: 'DELETE', url: `/permissions/apis/${apiId}` })
}

export function loadBackendApiCatalog() {
  return apiClient.request<ApiCatalogSyncResult>({ method: 'POST', url: '/permissions/apis/catalog/sync' })
}

export function syncMenuApiBindings() {
  return apiClient.request<MenuApiBindingSyncResult>({
    method: 'PUT',
    url: '/permissions/functions/api-bindings/sync',
    data: { bindings: menuApiBindings },
  })
}
