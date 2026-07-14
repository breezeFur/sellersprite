import { apiClient } from '@/shared/api/http'

import type { AuthCredentials, AuthLoginResult, AuthSession } from '../model/auth'

export function login(credentials: AuthCredentials) {
  return apiClient.request<AuthLoginResult>({
    method: 'POST',
    url: '/auth/login',
    data: credentials,
    skipAuthHeader: true,
    skipAuthRefresh: true,
  })
}

export function refresh() {
  return apiClient.request<AuthLoginResult>({
    method: 'POST',
    url: '/auth/refresh',
    skipAuthHeader: true,
    skipAuthRefresh: true,
  })
}

export function logout() {
  return apiClient.request<void>({
    method: 'POST',
    url: '/auth/logout',
    skipAuthRefresh: true,
  })
}

export function session() {
  return apiClient.request<AuthSession>({
    method: 'GET',
    url: '/auth/session',
  })
}
