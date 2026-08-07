import type { RouteComponentWhitelist } from './dynamicRoutes'

export const routeComponentWhitelist: RouteComponentWhitelist = {
  'dashboard/overview': () => import('@/features/dashboard/pages/DashboardPage.vue'),
  'ai/chat': () => import('@/features/ai/pages/AiChatPage.vue'),
  'sellersprite/workbench': () => import('@/features/sellersprite/pages/SellerSpriteWorkbenchPage.vue'),
  'research/market-report': () => import('@/features/research/pages/MarketResearchPage.vue'),
  'research/report-history': () => import('@/features/research/pages/ResearchHistoryPage.vue'),
  'system/users': () => import('@/features/system/pages/UserManagementPage.vue'),
  'system/departments': () => import('@/features/system/pages/DepartmentManagementPage.vue'),
  'system/roles': () => import('@/features/system/pages/RoleManagementPage.vue'),
  'system/dictionaries': () => import('@/features/system/pages/DictionaryManagementPage.vue'),
  'system/functions': () => import('@/features/system/pages/FunctionManagementPage.vue'),
  'system/apis': () => import('@/features/system/pages/ApiResourceManagementPage.vue'),
  'ops/cache': () => import('@/features/ops/pages/CacheManagementPage.vue'),
  'ops/logs': () => import('@/features/ops/pages/LogQueryPage.vue'),
}
