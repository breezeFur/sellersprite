import { apiClient } from '@/shared/api/http'

import type { DashboardOverview } from '../model/dashboard'

export function getOverview() {
  return apiClient.request<DashboardOverview>({
    method: 'GET',
    url: '/dashboard/overview',
  })
}
