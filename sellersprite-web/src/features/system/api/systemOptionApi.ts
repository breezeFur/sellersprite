import { apiClient } from '@/shared/api/http'

import type { DepartmentNode, SystemRole } from '../model/system'

export function listEnabledRoles() {
  return apiClient.request<SystemRole[]>({ method: 'GET', url: '/roles/enabled' })
}

export function getDepartmentTree() {
  return apiClient.request<DepartmentNode[]>({ method: 'GET', url: '/depts/tree' })
}
