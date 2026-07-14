import { apiClient } from '@/shared/api/http'
import type { PageResult } from '@/shared/api/types'

import type { SystemUser, UserCreatePayload, UserUpdatePayload } from '../model/system'

export function pageUsers(params: { current: number; size: number; username?: string; status?: number }) {
  return apiClient.request<PageResult<SystemUser>>({ method: 'GET', url: '/users', params })
}

export function getUser(userId: string) {
  return apiClient.request<SystemUser>({ method: 'GET', url: `/users/${userId}` })
}

export function createUser(payload: UserCreatePayload) {
  return apiClient.request<SystemUser>({ method: 'POST', url: '/users', data: payload })
}

export function updateUser(userId: string, payload: UserUpdatePayload) {
  return apiClient.request<SystemUser>({ method: 'PUT', url: `/users/${userId}`, data: payload })
}

export function updateUserStatus(userId: string, status: number) {
  return apiClient.request<void>({ method: 'PUT', url: `/users/${userId}/status`, data: { status } })
}

export function replaceUserRoles(userId: string, roleIds: string[]) {
  return apiClient.request<void>({ method: 'PUT', url: `/users/${userId}/roles`, data: { roleIds } })
}

export function resetUserPassword(userId: string, password: string) {
  return apiClient.request<void>({ method: 'PUT', url: `/users/${userId}/password`, data: { password } })
}

export function deleteUser(userId: string) {
  return apiClient.request<void>({ method: 'DELETE', url: `/users/${userId}` })
}
