import { apiClient } from '@/shared/api/http'
import type { PageResult } from '@/shared/api/types'

import type { RolePayload, RolePermission, SystemRole } from '../model/system'

export function pageRoles(params: { current: number; size: number; roleName?: string; status?: number }) {
  return apiClient.request<PageResult<SystemRole>>({ method: 'GET', url: '/roles', params })
}

export function getRole(roleId: string) {
  return apiClient.request<SystemRole>({ method: 'GET', url: `/roles/${roleId}` })
}

export function createRole(payload: RolePayload) {
  return apiClient.request<SystemRole>({ method: 'POST', url: '/roles', data: payload })
}

export function updateRole(roleId: string, payload: RolePayload) {
  return apiClient.request<SystemRole>({ method: 'PUT', url: `/roles/${roleId}`, data: payload })
}

export function updateRoleStatus(roleId: string, status: number) {
  return apiClient.request<void>({ method: 'PUT', url: `/roles/${roleId}/status`, data: { status } })
}

export function deleteRole(roleId: string) {
  return apiClient.request<void>({ method: 'DELETE', url: `/roles/${roleId}` })
}

export function getRolePermissions(roleId: string) {
  return apiClient.request<RolePermission>({ method: 'GET', url: `/roles/${roleId}/permissions` })
}

export function replaceRolePermissions(roleId: string, functionIds: string[], extraApiIds: string[]) {
  return apiClient.request<RolePermission>({
    method: 'PUT',
    url: `/roles/${roleId}/permissions`,
    data: { functionIds, extraApiIds },
  })
}
