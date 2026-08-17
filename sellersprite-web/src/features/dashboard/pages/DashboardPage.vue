<script setup lang="ts">
import {
  ChatLineRound,
  Collection,
  OfficeBuilding,
  Refresh,
  User,
  UserFilled,
  WarningFilled,
} from '@element-plus/icons-vue'
import { computed, onMounted, ref, type Component } from 'vue'

import { ApiError } from '@/shared/api/ApiError'
import StatePanel from '@/shared/components/StatePanel.vue'

import { getOverview } from '../api/dashboardApi'
import type { DashboardActivity, DashboardOverview, DashboardTrendPoint } from '../model/dashboard'

interface MetricItem {
  label: string
  value: number
  icon: Component
  tone: 'blue' | 'green' | 'cyan' | 'amber' | 'violet' | 'red'
}

const overview = ref<DashboardOverview | null>(null)
const loading = ref(true)
const errorMessage = ref('')
const refreshedAt = ref<number | null>(null)

const metrics = computed<MetricItem[]>(() => {
  const data = overview.value
  if (!data) {
    return []
  }
  return [
    { label: '用户总数', value: data.userCount, icon: User, tone: 'blue' },
    { label: '启用角色', value: data.enabledRoleCount, icon: UserFilled, tone: 'green' },
    { label: '部门数量', value: data.departmentCount, icon: OfficeBuilding, tone: 'cyan' },
    { label: '字典类型', value: data.dictTypeCount, icon: Collection, tone: 'amber' },
    { label: '今日 AI 会话', value: data.todayAiConversationCount, icon: ChatLineRound, tone: 'violet' },
    { label: '今日失败操作', value: data.todayFailedOperationCount, icon: WarningFilled, tone: 'red' },
  ]
})

const trendMax = computed(() => {
  const values = (overview.value?.trends ?? []).flatMap((point) => [
    point.loginCount,
    point.aiConversationCount,
    point.aiCallCount,
  ])
  return Math.max(1, ...values)
})

async function loadOverview() {
  loading.value = true
  errorMessage.value = ''
  try {
    overview.value = await getOverview()
    refreshedAt.value = Date.now()
  } catch (error) {
    errorMessage.value = error instanceof ApiError ? error.message : '请检查网络连接后重试。'
  } finally {
    loading.value = false
  }
}

function barHeight(value: number) {
  return `${Math.max(0, Math.min(100, (value / trendMax.value) * 100))}%`
}

function formatTrendDate(date: string) {
  const parts = date.split('-')
  return parts.length === 3 ? `${Number(parts[1])}/${Number(parts[2])}` : date
}

function formatTime(timestamp: number) {
  if (!Number.isFinite(timestamp)) {
    return '--'
  }
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(timestamp))
}

function formatRefreshedAt(timestamp: number | null) {
  if (!timestamp) {
    return ''
  }
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  }).format(new Date(timestamp))
}

function activityTypeLabel(activity: DashboardActivity) {
  return activity.type === 'LOGIN' ? '登录' : '操作'
}

function activityDescription(activity: DashboardActivity) {
  return activity.description || '未提供模块信息'
}

function seriesTitle(point: DashboardTrendPoint, label: string, value: number) {
  return `${point.date} ${label} ${value}`
}

onMounted(loadOverview)
</script>

<template>
  <section
    class="dashboard"
    aria-label="首页概览"
  >
    <StatePanel
      v-if="loading && !overview"
      status="loading"
      title="正在加载首页数据"
      description="正在汇总最新运行指标。"
    />

    <StatePanel
      v-else-if="errorMessage && !overview"
      status="error"
      title="首页数据加载失败"
      :description="errorMessage"
      action-label="重新加载"
      @action="loadOverview"
    />

    <template v-else-if="overview">
      <div class="dashboard__toolbar">
        <p class="dashboard__updated-at">
          数据更新于 {{ formatRefreshedAt(refreshedAt) }}
        </p>
        <button
          class="dashboard__refresh"
          type="button"
          title="刷新首页数据"
          aria-label="刷新首页数据"
          :disabled="loading"
          @click="loadOverview"
        >
          <Refresh aria-hidden="true" />
        </button>
      </div>

      <section
        class="dashboard__metrics"
        aria-label="核心指标"
      >
        <article
          v-for="metric in metrics"
          :key="metric.label"
          class="metric"
        >
          <span
            class="metric__icon"
            :class="`metric__icon--${metric.tone}`"
            aria-hidden="true"
          >
            <Component :is="metric.icon" />
          </span>
          <div class="metric__content">
            <span class="metric__label">{{ metric.label }}</span>
            <strong class="metric__value">{{ metric.value.toLocaleString('zh-CN') }}</strong>
          </div>
        </article>
      </section>

      <div class="dashboard__workspace">
        <section
          class="dashboard__trend"
          aria-label="近七日活跃度"
        >
          <header class="dashboard__section-header">
            <div>
              <h2>近七日活跃度</h2>
              <p>仅展示产生真实数据的日期</p>
            </div>
            <div
              class="trend-legend"
              aria-label="趋势图例"
            >
              <span><i class="trend-legend__dot trend-legend__dot--login" />登录</span>
              <span><i class="trend-legend__dot trend-legend__dot--conversation" />AI 会话</span>
              <span><i class="trend-legend__dot trend-legend__dot--call" />AI 调用</span>
            </div>
          </header>

          <div
            v-if="overview.trends.length"
            class="trend-chart"
          >
            <div
              v-for="point in overview.trends"
              :key="point.date"
              class="trend-chart__group"
            >
              <div class="trend-chart__bars">
                <div
                  class="trend-chart__bar trend-chart__bar--login"
                  :style="{ height: barHeight(point.loginCount) }"
                  :title="seriesTitle(point, '登录', point.loginCount)"
                >
                  <span>{{ point.loginCount }}</span>
                </div>
                <div
                  class="trend-chart__bar trend-chart__bar--conversation"
                  :style="{ height: barHeight(point.aiConversationCount) }"
                  :title="seriesTitle(point, 'AI 会话', point.aiConversationCount)"
                >
                  <span>{{ point.aiConversationCount }}</span>
                </div>
                <div
                  class="trend-chart__bar trend-chart__bar--call"
                  :style="{ height: barHeight(point.aiCallCount) }"
                  :title="seriesTitle(point, 'AI 调用', point.aiCallCount)"
                >
                  <span>{{ point.aiCallCount }}</span>
                </div>
              </div>
              <span class="trend-chart__date">{{ formatTrendDate(point.date) }}</span>
            </div>
          </div>
          <StatePanel
            v-else
            class="dashboard__inline-state"
            status="empty"
            title="近七日暂无活跃数据"
            description="产生登录或 AI 调用后将在这里显示。"
          />
        </section>

        <section
          class="dashboard__activity"
          aria-label="最近活动"
        >
          <header class="dashboard__section-header">
            <div>
              <h2>最近活动</h2>
              <p>登录与管理操作合并展示</p>
            </div>
          </header>

          <ul
            v-if="overview.recentActivities.length"
            class="activity-list"
          >
            <li
              v-for="activity in overview.recentActivities"
              :key="`${activity.type}-${activity.traceId}-${activity.occurredAt}`"
              class="activity-list__item"
            >
              <span
                class="activity-list__status"
                :class="activity.success === 1 ? 'is-success' : 'is-failed'"
                :title="activity.success === 1 ? '成功' : '失败'"
              />
              <div class="activity-list__body">
                <div class="activity-list__title-row">
                  <strong>{{ activity.title }}</strong>
                  <span>{{ activityTypeLabel(activity) }}</span>
                </div>
                <p>{{ activityDescription(activity) }}</p>
                <code v-if="activity.traceId">{{ activity.traceId }}</code>
              </div>
              <time :datetime="new Date(activity.occurredAt).toISOString()">
                {{ formatTime(activity.occurredAt) }}
              </time>
            </li>
          </ul>
          <StatePanel
            v-else
            class="dashboard__inline-state"
            status="empty"
            title="暂无最近活动"
            description="登录和管理操作产生后将在这里显示。"
          />
        </section>
      </div>
    </template>
  </section>
</template>

<style scoped>
.dashboard {
  min-width: 0;
}

.dashboard__toolbar {
  display: flex;
  min-height: 32px;
  margin-bottom: var(--space-3);
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-2);
}

.dashboard__updated-at {
  margin: 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
}

.dashboard__refresh {
  display: grid;
  width: 30px;
  height: 30px;
  padding: 7px;
  place-items: center;
  color: var(--color-text-secondary);
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
}

.dashboard__refresh:hover:not(:disabled) {
  color: var(--color-brand-600);
  background: var(--color-brand-50);
  border-color: var(--color-brand-100);
}

.dashboard__refresh:disabled {
  cursor: wait;
  opacity: 0.5;
}

.dashboard__refresh svg {
  width: 16px;
  height: 16px;
}

.dashboard__metrics {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: var(--space-3);
  margin-bottom: var(--space-5);
}

.metric {
  display: flex;
  min-width: 0;
  min-height: 92px;
  padding: var(--space-4);
  align-items: center;
  gap: var(--space-3);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
}

.metric__icon {
  display: grid;
  flex: 0 0 38px;
  width: 38px;
  height: 38px;
  padding: 9px;
  place-items: center;
  border-radius: var(--radius-md);
}

.metric__icon svg {
  width: 20px;
  height: 20px;
}

.metric__icon--blue {
  color: #1d4ed8;
  background: #dbeafe;
}

.metric__icon--green {
  color: #15803d;
  background: #dcfce7;
}

.metric__icon--cyan {
  color: #0e7490;
  background: #cffafe;
}

.metric__icon--amber {
  color: #b45309;
  background: #fef3c7;
}

.metric__icon--violet {
  color: #6d28d9;
  background: #ede9fe;
}

.metric__icon--red {
  color: #b91c1c;
  background: #fee2e2;
}

.metric__content {
  display: flex;
  min-width: 0;
  flex-direction: column;
}

.metric__label {
  overflow: hidden;
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.metric__value {
  margin-top: var(--space-1);
  color: var(--color-text);
  font-size: 24px;
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}

.dashboard__workspace {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(320px, 0.75fr);
  min-height: 420px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.dashboard__trend,
.dashboard__activity {
  min-width: 0;
  padding: var(--space-5);
}

.dashboard__activity {
  border-left: 1px solid var(--color-border);
}

.dashboard__section-header {
  display: flex;
  min-height: 52px;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
}

.dashboard__section-header h2,
.dashboard__section-header p {
  margin: 0;
}

.dashboard__section-header h2 {
  color: var(--color-text);
  font-size: var(--font-size-lg);
  font-weight: 650;
}

.dashboard__section-header p {
  margin-top: var(--space-1);
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
}

.trend-legend {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: var(--space-3);
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
}

.trend-legend span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  white-space: nowrap;
}

.trend-legend__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.trend-legend__dot--login,
.trend-chart__bar--login {
  background: #2563eb;
}

.trend-legend__dot--conversation,
.trend-chart__bar--conversation {
  background: #0891b2;
}

.trend-legend__dot--call,
.trend-chart__bar--call {
  background: #7c3aed;
}

.trend-chart {
  display: grid;
  height: 286px;
  padding: var(--space-5) var(--space-2) 0;
  grid-template-columns: repeat(7, minmax(42px, 1fr));
  align-items: end;
  gap: var(--space-3);
  border-bottom: 1px solid var(--color-border-strong);
  background-image: linear-gradient(to bottom, transparent calc(25% - 1px), var(--color-border) 25%, transparent calc(25% + 1px)),
    linear-gradient(to bottom, transparent calc(50% - 1px), var(--color-border) 50%, transparent calc(50% + 1px)),
    linear-gradient(to bottom, transparent calc(75% - 1px), var(--color-border) 75%, transparent calc(75% + 1px));
}

.trend-chart__group {
  display: grid;
  min-width: 0;
  height: 100%;
  grid-template-rows: minmax(0, 1fr) 30px;
  align-items: end;
}

.trend-chart__bars {
  display: flex;
  height: 100%;
  align-items: end;
  justify-content: center;
  gap: 4px;
}

.trend-chart__bar {
  position: relative;
  width: min(9px, 24%);
  min-height: 0;
  border-radius: 3px 3px 0 0;
  transition: opacity var(--motion-fast) ease;
}

.trend-chart__bar:hover {
  opacity: 0.72;
}

.trend-chart__bar span {
  position: absolute;
  right: 50%;
  bottom: calc(100% + 4px);
  color: var(--color-text-muted);
  font-size: 10px;
  font-variant-numeric: tabular-nums;
  transform: translateX(50%);
}

.trend-chart__date {
  align-self: end;
  padding-top: var(--space-2);
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
  text-align: center;
}

.activity-list {
  max-height: 334px;
  margin: var(--space-3) 0 0;
  padding: 0;
  list-style: none;
  overflow: auto;
}

.activity-list__item {
  display: grid;
  min-width: 0;
  padding: var(--space-3) 0;
  grid-template-columns: 10px minmax(0, 1fr) auto;
  align-items: start;
  gap: var(--space-3);
  border-bottom: 1px solid var(--color-border);
}

.activity-list__item:last-child {
  border-bottom: 0;
}

.activity-list__status {
  width: 8px;
  height: 8px;
  margin-top: 6px;
  border-radius: 50%;
}

.activity-list__status.is-success {
  background: var(--color-success);
}

.activity-list__status.is-failed {
  background: var(--color-danger);
}

.activity-list__body {
  min-width: 0;
}

.activity-list__title-row {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: var(--space-2);
}

.activity-list__title-row strong {
  overflow: hidden;
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-list__title-row span {
  flex: 0 0 auto;
  color: var(--color-text-muted);
  font-size: 10px;
}

.activity-list__body p {
  margin: 2px 0 0;
  overflow: hidden;
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-list__body code {
  display: block;
  margin-top: 3px;
  overflow: hidden;
  color: var(--color-text-muted);
  font-family: var(--font-mono);
  font-size: 10px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-list__item time {
  color: var(--color-text-muted);
  font-size: 10px;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.dashboard__inline-state {
  min-height: 286px;
}

@media (max-width: 1280px) {
  .dashboard__metrics {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1024px) {
  .dashboard__workspace {
    grid-template-columns: 1fr;
  }

  .dashboard__activity {
    border-top: 1px solid var(--color-border);
    border-left: 0;
  }

  .activity-list {
    max-height: none;
  }
}

@media (max-width: 768px) {
  .dashboard__toolbar {
    margin-bottom: var(--space-2);
  }

  .dashboard__metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: var(--space-2);
  }

  .metric {
    min-height: 84px;
    padding: var(--space-3);
  }

  .metric__icon {
    flex-basis: 34px;
    width: 34px;
    height: 34px;
    padding: 8px;
  }

  .metric__value {
    font-size: var(--font-size-xl);
  }

  .dashboard__trend,
  .dashboard__activity {
    padding: var(--space-4);
  }

  .dashboard__section-header {
    flex-direction: column;
  }

  .trend-legend {
    justify-content: flex-start;
  }

  .trend-chart {
    grid-template-columns: repeat(7, minmax(38px, 1fr));
    gap: var(--space-2);
    overflow-x: auto;
  }

  .activity-list__item {
    grid-template-columns: 10px minmax(0, 1fr);
  }

  .activity-list__item time {
    grid-column: 2;
  }
}
</style>
