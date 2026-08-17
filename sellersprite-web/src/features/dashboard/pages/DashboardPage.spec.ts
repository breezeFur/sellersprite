import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import * as dashboardApi from '../api/dashboardApi'
import DashboardPage from './DashboardPage.vue'

vi.mock('../api/dashboardApi', () => ({
  getOverview: vi.fn(),
}))

const overview = {
  userCount: 12,
  enabledRoleCount: 3,
  departmentCount: 4,
  dictTypeCount: 5,
  todayAiConversationCount: 6,
  todayFailedOperationCount: 1,
  trends: [
    {
      date: '2026-07-11',
      loginCount: 8,
      aiConversationCount: 6,
      aiCallCount: 10,
    },
  ],
  recentActivities: [
    {
      type: 'LOGIN',
      title: '用户登录',
      description: 'admin',
      success: 1,
      occurredAt: 1_783_702_800_000,
      traceId: 'trace-login',
    },
  ],
}

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.mocked(dashboardApi.getOverview).mockReset()
  })

  it('renders real metrics, trend values and recent activities', async () => {
    vi.mocked(dashboardApi.getOverview).mockResolvedValue(overview)

    const wrapper = mount(DashboardPage)
    await flushPromises()

    expect(wrapper.get('[aria-label="核心指标"]').text()).toContain('12')
    expect(wrapper.get('[aria-label="核心指标"]').text()).toContain('今日 AI 会话')
    expect(wrapper.get('[aria-label="近七日活跃度"]').text()).toContain('8')
    expect(wrapper.get('[aria-label="近七日活跃度"]').text()).toContain('10')
    expect(wrapper.get('[aria-label="最近活动"]').text()).toContain('用户登录')
    expect(wrapper.get('[aria-label="最近活动"]').text()).toContain('admin')
  })

  it('renders independent empty states for sparse dashboard data', async () => {
    vi.mocked(dashboardApi.getOverview).mockResolvedValue({
      ...overview,
      trends: [],
      recentActivities: [],
    })

    const wrapper = mount(DashboardPage)
    await flushPromises()

    expect(wrapper.text()).toContain('近七日暂无活跃数据')
    expect(wrapper.text()).toContain('暂无最近活动')
  })

  it('shows a retryable error state when loading fails', async () => {
    vi.mocked(dashboardApi.getOverview)
      .mockRejectedValueOnce(new Error('network'))
      .mockResolvedValueOnce(overview)

    const wrapper = mount(DashboardPage)
    await flushPromises()

    expect(wrapper.text()).toContain('首页数据加载失败')
    await wrapper.get('button').trigger('click')
    await flushPromises()

    expect(dashboardApi.getOverview).toHaveBeenCalledTimes(2)
    expect(wrapper.get('[aria-label="核心指标"]').text()).toContain('12')
  })
})
