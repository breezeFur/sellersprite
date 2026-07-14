import { apiClient } from '@/shared/api/http'
import type { PageResult } from '@/shared/api/types'

import type {
  DictionaryItem,
  DictionaryItemPayload,
  DictionaryType,
  DictionaryTypePayload,
} from '../model/system'

interface DictTypeQuery {
  current: number
  size: number
  dictType?: string
  dictName?: string
  status?: number
}

interface DictItemQuery {
  current: number
  size: number
  dictLabel?: string
  dictName?: string
  dictValue?: string
  status?: number
}

export function pageDictTypes(params: DictTypeQuery) {
  return apiClient.request<PageResult<DictionaryType>>({ method: 'GET', url: '/system/dicts/types', params })
}

export function getEnabledDictionary(dictType: string) {
  return apiClient.request<DictionaryType>({ method: 'GET', url: `/system/dicts/${dictType}` })
}

export function createDictType(data: DictionaryTypePayload) {
  return apiClient.request<DictionaryType>({ method: 'POST', url: '/system/dicts/types', data })
}

export function updateDictType(dictType: string, data: Omit<DictionaryTypePayload, 'systemBuiltin' | 'dictType'>) {
  return apiClient.request<DictionaryType>({ method: 'PUT', url: `/system/dicts/types/${dictType}`, data })
}

export function updateDictTypeStatus(dictType: string, status: number) {
  return apiClient.request<void>({ method: 'PUT', url: `/system/dicts/types/${dictType}/status`, data: { status } })
}

export function deleteDictType(dictType: string) {
  return apiClient.request<void>({ method: 'DELETE', url: `/system/dicts/types/${dictType}` })
}

export function pageDictItems(dictType: string, params: DictItemQuery) {
  return apiClient.request<PageResult<DictionaryItem>>({ method: 'GET', url: `/system/dicts/types/${dictType}/items`, params })
}

export function createDictItem(data: DictionaryItemPayload) {
  return apiClient.request<DictionaryItem>({ method: 'POST', url: '/system/dicts/items', data })
}

export function updateDictItem(dictDataId: string, data: Omit<DictionaryItemPayload, 'dictType'>) {
  return apiClient.request<DictionaryItem>({ method: 'PUT', url: `/system/dicts/items/${dictDataId}`, data })
}

export function updateDictItemStatus(dictDataId: string, status: number) {
  return apiClient.request<void>({ method: 'PUT', url: `/system/dicts/items/${dictDataId}/status`, data: { status } })
}

export function deleteDictItem(dictDataId: string) {
  return apiClient.request<void>({ method: 'DELETE', url: `/system/dicts/items/${dictDataId}` })
}
