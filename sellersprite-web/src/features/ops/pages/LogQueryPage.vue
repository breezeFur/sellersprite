<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'

import { ApiError } from '@/shared/api/ApiError'
import StatePanel from '@/shared/components/StatePanel.vue'

import {
  getAiPromptLog,
  getLoginLog,
  getOperationLog,
  pageAiPromptLogs,
  pageLoginLogs,
  pageOperationLogs,
} from '../api/logApi'
import type { AiPromptLog, LoginLog, OperationLog } from '../model/log'

type LogTab = 'login' | 'operation' | 'ai'

interface PageState<T> {
  records: T[]
  current: number
  size: number
  total: number
  loading: boolean
  error: string
}

const activeTab = ref<LogTab>('login')
const loginState = reactive<PageState<LoginLog>>(emptyState())
const operationState = reactive<PageState<OperationLog>>(emptyState())
const aiState = reactive<PageState<AiPromptLog>>(emptyState())
const loginFilters = reactive({ username: '', success: null as number | null, loginIp: '', startTime: '', endTime: '' })
const operationFilters = reactive({ username: '', moduleName: '', operationType: '', success: null as number | null, trackId: '', startTime: '', endTime: '' })
const aiFilters = reactive({ conversationId: '', provider: '', model: '', status: '', startTime: '', endTime: '' })
const detailOpen = ref(false)
const detailLoading = ref(false)
const detailError = ref('')
const detailKind = ref<LogTab>('login')
const detailRecord = ref<LoginLog | OperationLog | AiPromptLog | null>(null)

const activeState = computed(() => {
  if (activeTab.value === 'login') return loginState
  if (activeTab.value === 'operation') return operationState
  return aiState
})

const currentStartTime = computed({
  get: () => activeTab.value === 'login' ? loginFilters.startTime : activeTab.value === 'operation' ? operationFilters.startTime : aiFilters.startTime,
  set: (value: string) => {
    if (activeTab.value === 'login') loginFilters.startTime = value
    else if (activeTab.value === 'operation') operationFilters.startTime = value
    else aiFilters.startTime = value
  },
})

const currentEndTime = computed({
  get: () => activeTab.value === 'login' ? loginFilters.endTime : activeTab.value === 'operation' ? operationFilters.endTime : aiFilters.endTime,
  set: (value: string) => {
    if (activeTab.value === 'login') loginFilters.endTime = value
    else if (activeTab.value === 'operation') operationFilters.endTime = value
    else aiFilters.endTime = value
  },
})

const detailTitle = computed(() => {
  if (detailKind.value === 'login') return '登录日志详情'
  if (detailKind.value === 'operation') return '操作日志详情'
  return 'AI Prompt 日志详情'
})

const detailFields = computed(() => {
  const record = detailRecord.value
  if (!record) return []
  if (detailKind.value === 'login') {
    const value = record as LoginLog
    return [
      ['用户', value.username || '--'], ['登录类型', value.loginType || '--'], ['结果', resultText(value.success)],
      ['错误码', value.errorCode || '--'], ['失败原因', value.failureReason || '--'], ['登录 IP', value.loginIp || '--'],
      ['位置', value.loginLocation || '--'], ['设备', value.deviceName || '--'], ['客户端', value.clientType || '--'],
      ['Track ID', value.trackId || '--'], ['发生时间', formatTime(value.createdAt)],
    ]
  }
  if (detailKind.value === 'operation') {
    const value = record as OperationLog
    return [
      ['操作人', value.username || '--'], ['模块', value.moduleName || '--'], ['操作', value.operationName || '--'],
      ['类型', value.operationType || '--'], ['请求', `${value.httpMethod || '--'} ${value.requestUri || '--'}`],
      ['结果', resultText(value.success)], ['响应状态', String(value.responseStatus ?? '--')], ['耗时', `${value.costMs ?? 0} ms`],
      ['错误信息', value.errorMessage || '--'], ['客户端 IP', value.clientIp || '--'], ['Track ID', value.trackId || '--'],
      ['发生时间', formatTime(value.createdAt)],
    ]
  }
  const value = record as AiPromptLog
  return [
    ['用户 ID', value.userId || '--'], ['会话 ID', value.conversationId || '--'], ['Provider', value.provider || '--'],
    ['模型', value.model || '--'], ['状态', value.status || '--'], ['结束原因', value.finishReason || '--'],
    ['Token', `${value.promptTokens ?? 0} + ${value.completionTokens ?? 0} = ${value.totalTokens ?? 0}`],
    ['耗时', `${value.costMs ?? 0} ms`], ['错误类型', value.errorType || '--'], ['错误信息', value.errorMessage || '--'],
    ['Track ID', value.trackId || '--'], ['发生时间', formatTime(value.createdAt)],
  ]
})

const detailBlocks = computed(() => {
  const record = detailRecord.value
  if (!record) return []
  if (detailKind.value === 'operation') {
    const value = record as OperationLog
    return [['请求参数', prettyText(value.requestParams)], ['响应内容', prettyText(value.responsePayload)], ['User-Agent', value.userAgent || '--']]
  }
  if (detailKind.value === 'ai') {
    const value = record as AiPromptLog
    return [
      ['Prompt 摘要', value.promptSummary || '--'], ['请求消息（已脱敏）', prettyText(value.requestMessages)],
      ['响应内容（已脱敏）', prettyText(value.responseContent)], ['响应元数据', prettyText(value.responseMetadata)],
    ]
  }
  return [['User-Agent', (record as LoginLog).userAgent || '--']]
})

function emptyState<T>(): PageState<T> {
  return { records: [], current: 1, size: 20, total: 0, loading: false, error: '' }
}

function optional(value: string) {
  return value.trim() || undefined
}

function timestamp(value: string) {
  if (!value) return undefined
  const parsed = new Date(value).getTime()
  return Number.isFinite(parsed) ? parsed : undefined
}

async function loadLogin() {
  loginState.loading = true
  loginState.error = ''
  try {
    const page = await pageLoginLogs({
      current: loginState.current, size: loginState.size, username: optional(loginFilters.username),
      success: loginFilters.success ?? undefined, loginIp: optional(loginFilters.loginIp),
      startTime: timestamp(loginFilters.startTime), endTime: timestamp(loginFilters.endTime),
    })
    Object.assign(loginState, page)
  } catch (error) { loginState.error = messageOf(error, '登录日志加载失败') }
  finally { loginState.loading = false }
}

async function loadOperation() {
  operationState.loading = true
  operationState.error = ''
  try {
    const page = await pageOperationLogs({
      current: operationState.current, size: operationState.size, username: optional(operationFilters.username),
      moduleName: optional(operationFilters.moduleName), operationType: optional(operationFilters.operationType),
      success: operationFilters.success ?? undefined, trackId: optional(operationFilters.trackId),
      startTime: timestamp(operationFilters.startTime), endTime: timestamp(operationFilters.endTime),
    })
    Object.assign(operationState, page)
  } catch (error) { operationState.error = messageOf(error, '操作日志加载失败') }
  finally { operationState.loading = false }
}

async function loadAi() {
  aiState.loading = true
  aiState.error = ''
  try {
    const page = await pageAiPromptLogs({
      current: aiState.current, size: aiState.size, conversationId: optional(aiFilters.conversationId),
      provider: optional(aiFilters.provider), model: optional(aiFilters.model), status: optional(aiFilters.status),
      startTime: timestamp(aiFilters.startTime), endTime: timestamp(aiFilters.endTime),
    })
    Object.assign(aiState, page)
  } catch (error) { aiState.error = messageOf(error, 'AI Prompt 日志加载失败') }
  finally { aiState.loading = false }
}

function loadActive() {
  if (activeTab.value === 'login') return loadLogin()
  if (activeTab.value === 'operation') return loadOperation()
  return loadAi()
}

function selectTab(tab: LogTab) {
  if (activeTab.value === tab) return
  activeTab.value = tab
  void loadActive()
}

function search() {
  activeState.value.current = 1
  void loadActive()
}

function resetFilters() {
  if (activeTab.value === 'login') Object.assign(loginFilters, { username: '', success: null, loginIp: '', startTime: '', endTime: '' })
  else if (activeTab.value === 'operation') Object.assign(operationFilters, { username: '', moduleName: '', operationType: '', success: null, trackId: '', startTime: '', endTime: '' })
  else Object.assign(aiFilters, { conversationId: '', provider: '', model: '', status: '', startTime: '', endTime: '' })
  search()
}

function changePage(delta: number) {
  activeState.value.current += delta
  void loadActive()
}

async function openDetail(kind: LogTab, id: string) {
  detailKind.value = kind
  detailOpen.value = true
  detailLoading.value = true
  detailError.value = ''
  detailRecord.value = null
  try {
    if (kind === 'login') detailRecord.value = await getLoginLog(id)
    else if (kind === 'operation') detailRecord.value = await getOperationLog(id)
    else detailRecord.value = await getAiPromptLog(id)
  } catch (error) { detailError.value = messageOf(error, '日志详情加载失败') }
  finally { detailLoading.value = false }
}

function resultText(success: number) {
  return success === 1 ? '成功' : '失败'
}

function formatTime(value: number) {
  if (!value) return '--'
  return new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'medium', hour12: false }).format(new Date(value))
}

function prettyText(value: string) {
  if (!value) return '--'
  try { return JSON.stringify(JSON.parse(value), null, 2) }
  catch { return value }
}

function messageOf(error: unknown, fallback: string) {
  return error instanceof ApiError ? error.message : fallback
}

onMounted(() => void loadLogin())
</script>

<template>
  <section
    class="log-page"
    aria-label="日志查询"
  >
    <div
      class="log-tabs"
      role="tablist"
      aria-label="日志类型"
    >
      <button
        role="tab"
        :aria-selected="activeTab === 'login'"
        data-testid="tab-login"
        @click="selectTab('login')"
      >
        登录日志
      </button>
      <button
        role="tab"
        :aria-selected="activeTab === 'operation'"
        data-testid="tab-operation"
        @click="selectTab('operation')"
      >
        操作日志
      </button>
      <button
        role="tab"
        :aria-selected="activeTab === 'ai'"
        data-testid="tab-ai"
        @click="selectTab('ai')"
      >
        AI Prompt
      </button>
    </div>

    <div class="filters">
      <template v-if="activeTab === 'login'">
        <label>用户名<input
          v-model="loginFilters.username"
          placeholder="用户名"
        ></label>
        <label>结果<select v-model="loginFilters.success"><option :value="null">全部</option><option :value="1">成功</option><option :value="0">失败</option></select></label>
        <label>客户端 IP<input
          v-model="loginFilters.loginIp"
          placeholder="IP 关键字"
        ></label>
      </template>
      <template v-else-if="activeTab === 'operation'">
        <label>操作人<input
          v-model="operationFilters.username"
          placeholder="用户名"
        ></label>
        <label>模块<input
          v-model="operationFilters.moduleName"
          placeholder="模块名"
        ></label>
        <label>类型<input
          v-model="operationFilters.operationType"
          placeholder="CREATE / UPDATE"
        ></label>
        <label>结果<select v-model="operationFilters.success"><option :value="null">全部</option><option :value="1">成功</option><option :value="0">失败</option></select></label>
        <label>Track ID<input
          v-model="operationFilters.trackId"
          placeholder="精确追踪"
        ></label>
      </template>
      <template v-else>
        <label>会话 ID<input
          v-model="aiFilters.conversationId"
          placeholder="会话 ID"
        ></label>
        <label>Provider<input
          v-model="aiFilters.provider"
          placeholder="Provider"
        ></label>
        <label>模型<input
          v-model="aiFilters.model"
          placeholder="模型"
        ></label>
        <label>状态<select v-model="aiFilters.status"><option value="">全部</option><option value="SUCCESS">完成</option><option value="FAILED">失败</option><option value="CANCELLED">已取消</option></select></label>
      </template>
      <label>开始时间<input
        v-model="currentStartTime"
        type="datetime-local"
      ></label>
      <label>结束时间<input
        v-model="currentEndTime"
        type="datetime-local"
      ></label>
      <ElButton
        type="primary"
        @click="search"
      >
        查询
      </ElButton><ElButton @click="resetFilters">
        重置
      </ElButton>
    </div>

    <StatePanel
      v-if="activeState.loading && activeState.records.length === 0"
      status="loading"
      title="正在加载日志"
    />
    <StatePanel
      v-else-if="activeState.error && activeState.records.length === 0"
      status="error"
      title="日志加载失败"
      :description="activeState.error"
      action-label="重新加载"
      @action="loadActive"
    />
    <div
      v-else
      class="table-wrap"
    >
      <table v-if="activeTab === 'login'">
        <thead><tr><th>时间</th><th>用户 / 类型</th><th>结果</th><th>客户端</th><th>错误</th><th>Track ID</th><th>操作</th></tr></thead>
        <tbody>
          <tr
            v-for="log in loginState.records"
            :key="log.loginLogId"
          >
            <td>{{ formatTime(log.createdAt) }}</td><td><strong>{{ log.username || '--' }}</strong><small>{{ log.loginType }}</small></td><td><span :class="['result', log.success === 1 ? 'success' : 'failure']">{{ resultText(log.success) }}</span></td><td>{{ log.loginIp || '--' }}<small>{{ log.clientType || '--' }}</small></td><td>{{ log.errorCode || '--' }}<small>{{ log.failureReason || '' }}</small></td><td><code>{{ log.trackId || '--' }}</code></td><td>
              <button
                type="button"
                @click="openDetail('login', log.loginLogId)"
              >
                详情
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <table v-else-if="activeTab === 'operation'">
        <thead><tr><th>时间</th><th>操作人</th><th>模块 / 操作</th><th>请求</th><th>结果 / 耗时</th><th>Track ID</th><th>操作</th></tr></thead>
        <tbody>
          <tr
            v-for="log in operationState.records"
            :key="log.operationLogId"
          >
            <td>{{ formatTime(log.createdAt) }}</td><td>{{ log.username || '--' }}</td><td><strong>{{ log.operationName }}</strong><small>{{ log.moduleName }} · {{ log.operationType }}</small></td><td><span class="method">{{ log.httpMethod }}</span><code>{{ log.requestUri }}</code></td><td><span :class="['result', log.success === 1 ? 'success' : 'failure']">{{ resultText(log.success) }}</span><small>{{ log.costMs }} ms</small></td><td><code>{{ log.trackId || '--' }}</code></td><td>
              <button
                type="button"
                :data-testid="`operation-detail-${log.operationLogId}`"
                @click="openDetail('operation', log.operationLogId)"
              >
                详情
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <table v-else>
        <thead><tr><th>时间</th><th>会话 / 用户</th><th>Provider / 模型</th><th>状态</th><th>Token</th><th>耗时 / Track ID</th><th>操作</th></tr></thead>
        <tbody>
          <tr
            v-for="log in aiState.records"
            :key="log.promptRecordId"
          >
            <td>{{ formatTime(log.createdAt) }}</td><td><code>{{ log.conversationId || '--' }}</code><small>{{ log.userId || '--' }}</small></td><td>{{ log.provider || '--' }}<small>{{ log.model || '--' }}</small></td><td><span :class="['result', log.status === 'SUCCESS' ? 'success' : 'failure']">{{ log.status }}</span></td><td>{{ log.totalTokens ?? 0 }}<small>{{ log.promptTokens ?? 0 }} + {{ log.completionTokens ?? 0 }}</small></td><td>{{ log.costMs }} ms<small><code>{{ log.trackId || '--' }}</code></small></td><td>
              <button
                type="button"
                @click="openDetail('ai', log.promptRecordId)"
              >
                详情
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <StatePanel
        v-if="!activeState.loading && activeState.records.length === 0"
        status="empty"
        title="暂无日志"
        description="调整筛选条件后重试"
      />
    </div>

    <footer class="pager">
      <span>共 {{ activeState.total }} 条，第 {{ activeState.current }} 页</span><div>
        <button
          :disabled="activeState.current <= 1"
          @click="changePage(-1)"
        >
          上一页
        </button><button
          :disabled="activeState.current * activeState.size >= activeState.total"
          @click="changePage(1)"
        >
          下一页
        </button>
      </div>
    </footer>

    <ElDrawer
      v-model="detailOpen"
      :title="detailTitle"
      size="min(760px, 100%)"
    >
      <StatePanel
        v-if="detailLoading"
        status="loading"
        title="正在加载日志详情"
      />
      <StatePanel
        v-else-if="detailError"
        status="error"
        title="日志详情加载失败"
        :description="detailError"
      />
      <div
        v-else-if="detailRecord"
        class="detail-content"
      >
        <dl>
          <div
            v-for="field in detailFields"
            :key="field[0]"
          >
            <dt>{{ field[0] }}</dt><dd>{{ field[1] }}</dd>
          </div>
        </dl>
        <section
          v-for="block in detailBlocks"
          :key="block[0]"
          class="detail-block"
        >
          <h3>{{ block[0] }}</h3><pre>{{ block[1] }}</pre>
        </section>
      </div>
    </ElDrawer>
  </section>
</template>

<style scoped>
.log-page{--color-primary:var(--color-brand-600);display:flex;min-height:calc(100vh - 104px);flex-direction:column;overflow:hidden;background:#fff;border:1px solid var(--color-border);border-radius:10px}.log-tabs{display:flex;gap:24px;padding:0 18px;border-bottom:1px solid var(--color-border)}.log-tabs button{position:relative;padding:15px 2px 13px;color:var(--color-text-secondary);font-weight:650;background:transparent;border:0;cursor:pointer}.log-tabs button[aria-selected="true"]{color:var(--color-brand-600)}.log-tabs button[aria-selected="true"]::after{position:absolute;right:0;bottom:-1px;left:0;height:2px;background:var(--color-brand-600);content:""}.filters{display:flex;align-items:flex-end;gap:10px;padding:14px 16px;border-bottom:1px solid var(--color-border);background:#fbfcfe}.filters label{display:grid;gap:5px;color:var(--color-text-secondary);font-size:12px}.filters input,.filters select{width:150px;height:34px;padding:0 9px;border:1px solid var(--color-border);border-radius:6px;background:#fff}.filters input[type="datetime-local"]{width:184px}.table-wrap{overflow:auto}table{width:100%;min-width:1120px;border-collapse:collapse}th,td{padding:11px 13px;border-bottom:1px solid var(--color-border);text-align:left;vertical-align:top;font-size:13px}th{position:sticky;top:0;background:#f7f9fc;color:var(--color-text-secondary);font-weight:650}td strong,td small{display:block}td small{margin-top:4px;color:var(--color-text-secondary)}td code{font-size:12px;overflow-wrap:anywhere}.method{display:inline-flex;margin-right:7px;padding:1px 6px;border-radius:4px;color:#1d4ed8;background:#dbeafe;font-size:11px;font-weight:700}.result{display:inline-flex;padding:2px 7px;border-radius:999px;font-size:12px}.result.success{color:#166534;background:#dcfce7}.result.failure{color:#b91c1c;background:#fee2e2}td button,.pager button{padding:0;color:var(--color-primary);background:transparent;border:0;cursor:pointer}.pager{display:flex;justify-content:space-between;margin-top:auto;padding:12px 16px;border-top:1px solid var(--color-border);color:var(--color-text-secondary);font-size:12px}.pager div{display:flex;gap:16px}.pager button:disabled{cursor:not-allowed;opacity:.4}.detail-content dl{display:grid;grid-template-columns:1fr 1fr;margin:0;border-top:1px solid var(--color-border);border-left:1px solid var(--color-border)}.detail-content dl div{display:grid;grid-template-columns:110px 1fr;border-right:1px solid var(--color-border);border-bottom:1px solid var(--color-border)}.detail-content dt,.detail-content dd{margin:0;padding:9px 10px;overflow-wrap:anywhere}.detail-content dt{color:var(--color-text-secondary);background:#f7f9fc;font-size:12px}.detail-content dd{font-size:13px}.detail-block{margin-top:18px}.detail-block h3{margin:0 0 7px;font-size:13px}.detail-block pre{max-height:300px;margin:0;padding:12px;overflow:auto;border:1px solid var(--color-border);border-radius:7px;background:#0f172a;color:#dbeafe;font:12px/1.65 ui-monospace,SFMono-Regular,Consolas,monospace;white-space:pre-wrap;overflow-wrap:anywhere}@media(max-width:1100px){.filters{flex-wrap:wrap}.filters input,.filters select{width:140px}}@media(max-width:700px){.log-tabs{gap:14px}.filters label{min-width:calc(50% - 6px)}.filters input,.filters select,.filters input[type="datetime-local"]{width:100%}.detail-content dl{grid-template-columns:1fr}}
</style>
