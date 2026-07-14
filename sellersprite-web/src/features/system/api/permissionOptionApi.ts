import { apiClient } from '@/shared/api/http'
import type { PageResult } from '@/shared/api/types'

import type { SystemApiResource, SystemFunction } from '../model/system'

const MAX_API_OPTION_PAGE_SIZE = 500

export function getFunctionTree() {
  return apiClient.request<SystemFunction[]>({ method: 'GET', url: '/permissions/functions/tree' })
}

export function pageApiOptions() {
  return apiClient.request<PageResult<SystemApiResource>>({
    method: 'GET',
    url: '/permissions/apis',
    params: { current: 1, size: MAX_API_OPTION_PAGE_SIZE, status: 1 },
  })
}
