import { apiClient } from '@/shared/api/http'

import type { DepartmentNode, DepartmentPayload } from '../model/system'

export function getDepartmentTree() {
  return apiClient.request<DepartmentNode[]>({ method: 'GET', url: '/depts/tree' })
}

export function getDepartment(deptId: string) {
  return apiClient.request<DepartmentNode>({ method: 'GET', url: `/depts/${deptId}` })
}

export function createDepartment(payload: DepartmentPayload) {
  return apiClient.request<DepartmentNode>({ method: 'POST', url: '/depts', data: payload })
}

export function updateDepartment(deptId: string, payload: DepartmentPayload) {
  return apiClient.request<DepartmentNode>({ method: 'PUT', url: `/depts/${deptId}`, data: payload })
}

export function updateDepartmentStatus(deptId: string, status: number) {
  return apiClient.request<void>({ method: 'PUT', url: `/depts/${deptId}/status`, data: { status } })
}

export function deleteDepartment(deptId: string) {
  return apiClient.request<void>({ method: 'DELETE', url: `/depts/${deptId}` })
}
