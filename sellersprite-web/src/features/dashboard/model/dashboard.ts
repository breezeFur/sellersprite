export interface DashboardTrendPoint {
  date: string
  loginCount: number
  aiConversationCount: number
  aiCallCount: number
}

export interface DashboardActivity {
  type: 'LOGIN' | 'OPERATION' | string
  title: string
  description: string
  success: number
  occurredAt: number
  trackId: string
}

export interface DashboardOverview {
  userCount: number
  enabledRoleCount: number
  departmentCount: number
  dictTypeCount: number
  todayAiConversationCount: number
  todayFailedOperationCount: number
  trends: DashboardTrendPoint[]
  recentActivities: DashboardActivity[]
}
