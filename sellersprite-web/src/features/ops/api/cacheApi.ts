import { apiClient } from '@/shared/api/http'

export interface CacheValueResult {
  key: string
  exists: boolean
  value: unknown
}

export function getCacheKeys() {
  return apiClient.request<string[]>({ method: 'GET', url: '/cache/keys' })
}

export function getCacheValue(key: string) {
  return apiClient.request<CacheValueResult>({ method: 'GET', url: '/cache/value', params: { key } })
}

export function checkCacheKey(key: string) {
  return apiClient.request<boolean>({ method: 'GET', url: '/cache/exists', params: { key } })
}

export function deleteCacheKey(key: string) {
  return apiClient.request<void>({ method: 'DELETE', url: '/cache/key', params: { key } })
}

export function clearCachePrefix() {
  return apiClient.request<void>({ method: 'DELETE', url: '/cache' })
}
