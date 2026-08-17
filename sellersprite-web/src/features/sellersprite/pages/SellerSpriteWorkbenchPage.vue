<script setup lang="ts">
import {
  Check,
  CopyDocument,
  Document,
  Download,
  Refresh,
  Search,
  UploadFilled,
  WarningFilled,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import { ApiError } from '@/shared/api/ApiError'
import StatePanel from '@/shared/components/StatePanel.vue'

import SellerSpriteGuidedWorkbench from '../components/SellerSpriteGuidedWorkbench.vue'
import {
  executeSellerSpriteOperation,
  parseSellerSpriteRequest,
  SellerSpriteRequestJsonError,
} from '../api/sellerspriteApi'
import { sellerSpriteDomains, sellerSpriteOperations } from '../model/operations'
import type {
  SellerSpriteDomainId,
  SellerSpriteExecutionResult,
  SellerSpriteOperation,
} from '../model/sellersprite'
import {
  exportSellerSpriteQueryResult,
  normalizeExportRecords,
} from '../utils/sellerSpriteExcelExport'

interface WorkbenchError {
  code: string
  message: string
  traceId?: string
}

type WorkbenchMode = 'guided' | 'debug'

const route = useRoute()
const routeDomain = computed(() => resolveRouteDomain(route.path))
const initialOperation = sellerSpriteOperations.find((operation) => operation.domain === routeDomain.value)
  ?? sellerSpriteOperations[0]
const workbenchMode = ref<WorkbenchMode>('guided')
const workbenchModes = [
  { value: 'guided' as const, label: '引导查询' },
  { value: 'debug' as const, label: 'API 调试' },
]
const selectedDomain = ref<SellerSpriteDomainId>(routeDomain.value)
const selectedOperationId = ref<string>(initialOperation.id)
const catalogKeyword = ref('')
const requestSource = ref(formatExample(initialOperation))
const selectedFiles = ref<Record<string, File | undefined>>({})
const validationError = ref('')
const submitting = ref(false)
const exporting = ref(false)
const result = ref<SellerSpriteExecutionResult | null>(null)
const executionError = ref<WorkbenchError | null>(null)

const selectedOperation = computed<SellerSpriteOperation>(() => (
  sellerSpriteOperations.find((operation) => operation.id === selectedOperationId.value)
  ?? sellerSpriteOperations[0]
))

const visibleOperations = computed(() => {
  const keyword = catalogKeyword.value.trim().toLowerCase()
  return sellerSpriteOperations.filter((operation) => {
    if (operation.domain !== selectedDomain.value) return false
    if (!keyword) return true
    return [operation.name, operation.description, operation.id, operation.path]
      .some((value) => value.toLowerCase().includes(keyword))
  })
})

const resultSource = computed(() => {
  if (!result.value) return ''
  try {
    return JSON.stringify(result.value.data, null, 2)
  } catch {
    return String(result.value.data)
  }
})

const resultIsEmpty = computed(() => isEmptyResult(result.value?.data))
const canExportResult = computed(() => (
  result.value !== null && normalizeExportRecords(result.value.data).length > 0
))
const transportLabel = computed(() => ({
  query: 'Query 参数',
  json: 'JSON Body',
  multipart: 'Multipart',
})[selectedOperation.value.transport])

watch(routeDomain, (domain) => {
  if (domain !== selectedDomain.value) selectDomain(domain)
})

function selectDomain(domain: SellerSpriteDomainId) {
  selectedDomain.value = domain
  const firstOperation = sellerSpriteOperations.find((operation) => operation.domain === domain)
  if (firstOperation) selectOperation(firstOperation)
}

function resolveRouteDomain(path: string): SellerSpriteDomainId {
  const candidate = path.split('/').filter(Boolean).at(-1)
  return sellerSpriteDomains.some((domain) => domain.id === candidate)
    ? candidate as SellerSpriteDomainId
    : 'product'
}

function selectOperation(operation: SellerSpriteOperation) {
  selectedOperationId.value = operation.id
  requestSource.value = formatExample(operation)
  selectedFiles.value = {}
  validationError.value = ''
  result.value = null
  executionError.value = null
}

function resetRequest() {
  requestSource.value = formatExample(selectedOperation.value)
  selectedFiles.value = {}
  validationError.value = ''
}

function updateFile(fieldName: string, event: Event) {
  const input = event.target as HTMLInputElement
  selectedFiles.value = {
    ...selectedFiles.value,
    [fieldName]: input.files?.[0],
  }
}

async function submitRequest() {
  if (submitting.value) return

  validationError.value = ''
  executionError.value = null
  let payload
  try {
    payload = parseSellerSpriteRequest(requestSource.value)
  } catch (error) {
    validationError.value = error instanceof SellerSpriteRequestJsonError
      ? error.message
      : '请求内容解析失败。'
    return
  }

  submitting.value = true
  result.value = null
  try {
    result.value = await executeSellerSpriteOperation(
      selectedOperation.value,
      payload,
      selectedFiles.value,
    )
  } catch (error) {
    executionError.value = normalizeExecutionError(error)
  } finally {
    submitting.value = false
  }
}

async function copyResult() {
  if (!resultSource.value) return
  if (!navigator.clipboard) {
    ElMessage.warning('当前浏览器不支持剪贴板写入')
    return
  }
  try {
    await navigator.clipboard.writeText(resultSource.value)
    ElMessage.success('响应已复制')
  } catch {
    ElMessage.error('复制失败，请手动选择响应内容')
  }
}

async function exportCurrentResult() {
  if (!result.value || !canExportResult.value || exporting.value) return

  const operation = selectedOperation.value
  const data = result.value.data
  exporting.value = true
  try {
    const exported = await exportSellerSpriteQueryResult({
      operationId: operation.id,
      operationName: operation.name,
      data,
    })
    ElMessage.success(`已导出本次查询结果（${exported.rowCount} 条）`)
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '导出失败，请稍后重试')
  } finally {
    exporting.value = false
  }
}

function formatExample(operation: SellerSpriteOperation) {
  return JSON.stringify(operation.example, null, 2)
}

function formatCompletedAt(timestamp: number) {
  return new Date(timestamp).toLocaleTimeString('zh-CN', { hour12: false })
}

function isEmptyResult(value: unknown) {
  if (value === null || value === undefined || value === '') return true
  if (Array.isArray(value)) return value.length === 0
  return typeof value === 'object' && Object.keys(value).length === 0
}

function normalizeExecutionError(error: unknown): WorkbenchError {
  if (error instanceof ApiError) {
    return { code: error.code, message: error.message, traceId: error.traceId }
  }
  if (error instanceof Error) {
    return { code: 'REQUEST_FAILED', message: error.message || '请求失败，请稍后重试。' }
  }
  return { code: 'REQUEST_FAILED', message: '请求失败，请稍后重试。' }
}
</script>

<template>
  <section
    class="workbench"
    aria-label="SellerSprite 工作台"
  >
    <header class="workbench__header">
      <div>
        <h1>SellerSprite 工作台</h1>
        <p v-if="workbenchMode === 'guided'">
          45 个官方接口 · 完整字段表单与结果表格
        </p>
        <p v-else>
          45 个固定代理操作 · 9 个业务域
        </p>
      </div>
      <div class="workbench__header-actions">
        <div
          class="workbench-mode"
          role="tablist"
          aria-label="SellerSprite 工作台模式"
        >
          <button
            v-for="mode in workbenchModes"
            :key="mode.value"
            type="button"
            role="tab"
            :aria-selected="workbenchMode === mode.value"
            :class="{ active: workbenchMode === mode.value }"
            @click="workbenchMode = mode.value"
          >
            {{ mode.label }}
          </button>
        </div>
        <div class="header-status">
          <span class="status-dot" />
          服务端安全代理
        </div>
      </div>
    </header>

    <SellerSpriteGuidedWorkbench
      v-if="workbenchMode === 'guided'"
      :initial-group="routeDomain"
    />

    <div
      v-else
      class="workbench__body"
      aria-label="SellerSprite API 调试台"
    >
      <aside
        class="catalog"
        aria-label="SellerSprite 操作目录"
      >
        <div class="catalog__search">
          <ElIcon><Search /></ElIcon>
          <input
            v-model="catalogKeyword"
            aria-label="搜索 SellerSprite 操作"
            placeholder="搜索名称、ID 或路径"
          >
        </div>

        <nav
          class="domain-list"
          aria-label="SellerSprite 业务域"
        >
          <button
            v-for="domain in sellerSpriteDomains"
            :key="domain.id"
            type="button"
            :class="{ active: selectedDomain === domain.id }"
            @click="selectDomain(domain.id)"
          >
            <span>{{ domain.label }}</span>
            <small>{{ sellerSpriteOperations.filter((item) => item.domain === domain.id).length }}</small>
          </button>
        </nav>

        <div class="operation-list">
          <button
            v-for="operation in visibleOperations"
            :key="operation.id"
            type="button"
            class="operation-item"
            :class="{ active: selectedOperationId === operation.id }"
            :data-operation-id="operation.id"
            @click="selectOperation(operation)"
          >
            <span
              class="operation-item__method"
              :class="operation.method.toLowerCase()"
            >
              {{ operation.method }}
            </span>
            <span class="operation-item__name">{{ operation.name }}</span>
          </button>
          <StatePanel
            v-if="visibleOperations.length === 0"
            class="catalog__empty"
            status="empty"
            title="没有匹配操作"
          />
        </div>
      </aside>

      <main class="request-panel">
        <div class="panel-heading">
          <div>
            <span class="eyebrow">{{ selectedOperation.id }}</span>
            <h2>{{ selectedOperation.name }}</h2>
            <p>{{ selectedOperation.description }}</p>
          </div>
          <ElTooltip content="恢复当前操作的请求示例">
            <ElButton
              :icon="Refresh"
              circle
              aria-label="恢复请求示例"
              @click="resetRequest"
            />
          </ElTooltip>
        </div>

        <dl class="request-meta">
          <div>
            <dt>方法</dt>
            <dd :class="`method-${selectedOperation.method.toLowerCase()}`">
              {{ selectedOperation.method }}
            </dd>
          </div>
          <div>
            <dt>内部路径</dt>
            <dd><code>/api{{ selectedOperation.path }}</code></dd>
          </div>
          <div>
            <dt>传输</dt>
            <dd>{{ transportLabel }}</dd>
          </div>
        </dl>

        <div class="editor-heading">
          <label for="sellersprite-request-editor">请求 JSON</label>
          <span>{{ requestSource.length }} 字符</span>
        </div>
        <textarea
          id="sellersprite-request-editor"
          v-model="requestSource"
          class="request-editor"
          aria-label="SellerSprite 请求 JSON"
          autocomplete="off"
          autocapitalize="off"
          spellcheck="false"
          @input="validationError = ''"
        />
        <p
          v-if="validationError"
          class="validation-error"
          role="alert"
        >
          <ElIcon><WarningFilled /></ElIcon>
          {{ validationError }}
        </p>

        <section
          v-if="selectedOperation.fileFields?.length"
          class="file-fields"
          aria-label="文件上传"
        >
          <label
            v-for="field in selectedOperation.fileFields"
            :key="field.name"
            class="file-field"
          >
            <input
              type="file"
              :accept="field.accept"
              :aria-label="field.label"
              @change="updateFile(field.name, $event)"
            >
            <ElIcon><UploadFilled /></ElIcon>
            <span>
              <strong>{{ field.label }}</strong>
              <small>{{ selectedFiles[field.name]?.name ?? '未选择文件' }}</small>
            </span>
          </label>
        </section>

        <footer class="request-actions">
          <span v-if="selectedOperation.id === 'ACCOUNT_VISITS'">次数查询请勿高频调用</span>
          <span v-else>请求由固定服务端代理发送</span>
          <ElButton
            type="primary"
            :loading="submitting"
            :disabled="submitting"
            aria-label="发送 SellerSprite 请求"
            @click="submitRequest"
          >
            发送请求
          </ElButton>
        </footer>
      </main>

      <section
        class="result-panel"
        aria-label="SellerSprite 响应"
      >
        <div class="panel-heading result-heading">
          <div>
            <span class="eyebrow">RESPONSE</span>
            <h2>响应结果</h2>
          </div>
          <div class="result-heading__actions">
            <ElTooltip content="复制格式化响应">
              <ElButton
                :icon="CopyDocument"
                circle
                :disabled="!resultSource"
                aria-label="复制响应"
                @click="copyResult"
              />
            </ElTooltip>
            <ElTooltip content="导出本次查询结果">
              <ElButton
                :icon="Download"
                circle
                :loading="exporting"
                :disabled="!canExportResult || exporting"
                aria-label="导出 API 调试结果"
                @click="exportCurrentResult"
              />
            </ElTooltip>
          </div>
        </div>

        <StatePanel
          v-if="submitting"
          status="loading"
          title="请求执行中"
          :description="selectedOperation.name"
        />

        <div
          v-else-if="executionError"
          class="execution-error"
          role="alert"
        >
          <ElIcon><WarningFilled /></ElIcon>
          <div>
            <span>{{ executionError.code }}</span>
            <h3>{{ executionError.message }}</h3>
            <p v-if="executionError.traceId">
              traceId: <code>{{ executionError.traceId }}</code>
            </p>
          </div>
        </div>

        <StatePanel
          v-else-if="result && resultIsEmpty"
          status="empty"
          title="请求成功，响应为空"
          description="服务端未返回可展示的数据"
        />

        <div
          v-else-if="result"
          class="result-success"
        >
          <div class="result-summary">
            <span><ElIcon><Check /></ElIcon> 调用成功</span>
            <dl>
              <div><dt>耗时</dt><dd>{{ result.durationMs }} ms</dd></div>
              <div><dt>完成时间</dt><dd>{{ formatCompletedAt(result.completedAt) }}</dd></div>
            </dl>
          </div>
          <pre tabindex="0">{{ resultSource }}</pre>
        </div>

        <div
          v-else
          class="result-idle"
        >
          <ElIcon><Document /></ElIcon>
          <h3>等待请求</h3>
          <p>选择操作并发送后在此查看响应</p>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.workbench{display:flex;min-height:calc(100vh - 104px);flex-direction:column;overflow:hidden;background:#fff;border:1px solid var(--color-border);border-radius:8px}.workbench__header{display:flex;min-height:66px;align-items:center;justify-content:space-between;gap:16px;padding:12px 18px;border-bottom:1px solid var(--color-border)}h1,h2,h3,p{margin:0}.workbench__header h1{font-size:18px;line-height:1.35}.workbench__header p,.panel-heading p{margin-top:4px;color:var(--color-text-secondary);font-size:12px}.workbench__header-actions{display:flex;align-items:center;gap:18px}.workbench-mode{display:flex;height:34px;padding:3px;background:#f1f4f8;border:1px solid var(--color-border);border-radius:6px}.workbench-mode button{min-width:84px;padding:0 12px;color:var(--color-text-secondary);background:transparent;border:0;border-radius:4px;cursor:pointer;font-size:12px}.workbench-mode button:hover{color:var(--color-text);background:#fff}.workbench-mode button.active{color:var(--color-brand-700);background:#fff;box-shadow:0 1px 2px rgb(15 23 42 / 12%);font-weight:600}.header-status{display:flex;align-items:center;gap:7px;color:var(--color-text-secondary);font-size:12px}.status-dot{width:8px;height:8px;background:var(--color-success);border-radius:50%}.workbench__body{display:grid;min-height:0;flex:1;grid-template-columns:248px minmax(360px,.9fr) minmax(380px,1.1fr)}.catalog,.request-panel,.result-panel{min-width:0;min-height:0}.catalog{display:flex;flex-direction:column;background:#f8fafc;border-right:1px solid var(--color-border)}.catalog__search{display:flex;height:38px;align-items:center;gap:8px;margin:12px;padding:0 10px;background:#fff;border:1px solid var(--color-border);border-radius:6px}.catalog__search .el-icon{color:var(--color-text-muted)}.catalog__search input{width:100%;min-width:0;color:var(--color-text);background:transparent;border:0;outline:0;font-size:12px}.domain-list{display:grid;grid-template-columns:1fr 1fr;gap:4px;padding:0 12px 10px}.domain-list button{display:flex;height:32px;align-items:center;justify-content:space-between;padding:0 9px;color:var(--color-text-secondary);background:transparent;border:1px solid transparent;border-radius:5px;cursor:pointer;font-size:12px}.domain-list button:hover{background:#fff;border-color:var(--color-border)}.domain-list button.active{color:var(--color-brand-700);background:#fff;border-color:var(--color-brand-200);font-weight:600}.domain-list small{color:var(--color-text-muted);font-size:10px}.operation-list{min-height:0;padding:4px 8px 12px;overflow:auto;border-top:1px solid var(--color-border)}.operation-item{display:grid;width:100%;min-height:40px;align-items:center;grid-template-columns:36px minmax(0,1fr);gap:8px;margin-top:4px;padding:5px 8px;color:var(--color-text);background:transparent;border:0;border-radius:5px;cursor:pointer;text-align:left}.operation-item:hover{background:#eef2f7}.operation-item.active{background:#e8f0fc;box-shadow:inset 3px 0 var(--color-brand-600)}.operation-item__method{font:600 9px/1 var(--font-mono)}.operation-item__method.get,.method-get{color:#16794b}.operation-item__method.post,.method-post{color:#98550d}.operation-item__name{min-width:0;overflow:hidden;font-size:12px;text-overflow:ellipsis;white-space:nowrap}.catalog__empty{min-height:180px}.request-panel,.result-panel{display:flex;flex-direction:column}.request-panel{border-right:1px solid var(--color-border)}.panel-heading{display:flex;min-height:88px;align-items:flex-start;justify-content:space-between;gap:12px;padding:16px 18px;border-bottom:1px solid var(--color-border)}.eyebrow{display:block;max-width:100%;overflow:hidden;color:var(--color-text-muted);font:10px/1.4 var(--font-mono);text-overflow:ellipsis;white-space:nowrap}.panel-heading h2{margin-top:4px;font-size:16px}.request-meta{display:grid;margin:0;padding:10px 18px;grid-template-columns:72px minmax(0,1fr) 100px;border-bottom:1px solid var(--color-border)}.request-meta>div{min-width:0}.request-meta dt{color:var(--color-text-muted);font-size:10px}.request-meta dd{min-width:0;margin:4px 0 0;font-size:12px;font-weight:600}.request-meta code{display:block;overflow:hidden;font:11px/1.4 var(--font-mono);text-overflow:ellipsis;white-space:nowrap}.editor-heading{display:flex;align-items:center;justify-content:space-between;padding:12px 18px 7px;color:var(--color-text-secondary);font-size:11px}.editor-heading label{color:var(--color-text);font-size:12px;font-weight:600}.request-editor{width:calc(100% - 36px);min-height:248px;margin:0 18px;padding:13px;resize:vertical;color:#dbeafe;background:#111827;border:1px solid #26334a;border-radius:6px;outline:0;font:12px/1.6 var(--font-mono);tab-size:2}.request-editor:focus{border-color:#5b8bd8}.validation-error{display:flex;align-items:flex-start;gap:6px;margin:8px 18px 0;color:var(--color-danger);font-size:11px;line-height:1.5}.validation-error .el-icon{flex:0 0 auto;margin-top:2px}.file-fields{display:grid;gap:8px;padding:12px 18px 0}.file-field{display:flex;min-height:54px;align-items:center;gap:10px;padding:9px 12px;background:#f8fafc;border:1px dashed #aab8cb;border-radius:6px;cursor:pointer}.file-field input{position:absolute;width:1px;height:1px;overflow:hidden;clip:rect(0,0,0,0)}.file-field .el-icon{width:20px;height:20px;color:var(--color-brand-600)}.file-field span{display:flex;min-width:0;flex-direction:column}.file-field strong{font-size:12px}.file-field small{margin-top:3px;overflow:hidden;color:var(--color-text-secondary);font-size:11px;text-overflow:ellipsis;white-space:nowrap}.request-actions{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-top:auto;padding:14px 18px;border-top:1px solid var(--color-border)}.request-actions>span{color:var(--color-text-muted);font-size:11px}.result-heading{min-height:88px}.result-heading__actions{display:flex;align-items:center;gap:8px}.result-panel{background:#fbfcfe}.result-panel>.state-panel{flex:1}.execution-error{display:flex;max-width:520px;align-items:flex-start;gap:12px;margin:32px 20px;padding:16px;color:#8a2c2c;background:#fff6f5;border:1px solid #efc5c1;border-radius:7px}.execution-error>.el-icon{width:24px;height:24px;flex:0 0 auto}.execution-error span{font:600 11px/1.4 var(--font-mono)}.execution-error h3{margin-top:5px;font-size:14px;line-height:1.5}.execution-error p{margin-top:8px;color:#a14b43;font-size:11px}.result-success{display:flex;min-height:0;flex:1;flex-direction:column}.result-summary{display:flex;align-items:center;justify-content:space-between;gap:14px;padding:10px 18px;background:#f1faf5;border-bottom:1px solid #cfe8da}.result-summary>span{display:flex;align-items:center;gap:6px;color:#16794b;font-size:12px;font-weight:600}.result-summary dl{display:flex;gap:20px;margin:0}.result-summary dl div{display:flex;gap:6px;font-size:10px}.result-summary dt{color:var(--color-text-muted)}.result-summary dd{margin:0;color:var(--color-text-secondary)}.result-success pre{min-height:0;flex:1;margin:0;padding:16px 18px;overflow:auto;color:#dbeafe;background:#111827;font:12px/1.65 var(--font-mono);white-space:pre-wrap;overflow-wrap:anywhere}.result-idle{display:flex;min-height:320px;flex:1;align-items:center;justify-content:center;flex-direction:column;color:var(--color-text-muted);text-align:center}.result-idle>.el-icon{width:34px;height:34px}.result-idle h3{margin-top:12px;color:var(--color-text-secondary);font-size:14px}.result-idle p{margin-top:5px;font-size:11px}@media(max-width:1180px){.workbench__body{grid-template-columns:220px minmax(350px,1fr)}.result-panel{min-height:480px;grid-column:1/-1;border-top:1px solid var(--color-border)}.request-panel{border-right:0}.catalog{grid-row:1}.result-success pre{min-height:360px}}@media(max-width:760px){.workbench{overflow:visible}.workbench__header{align-items:flex-start;flex-direction:column}.workbench__header-actions{width:100%;justify-content:space-between}.header-status{display:none}.workbench-mode{width:100%}.workbench-mode button{min-width:0;flex:1}.workbench__body{display:flex;flex-direction:column}.catalog{max-height:430px;border-right:0;border-bottom:1px solid var(--color-border)}.domain-list{grid-template-columns:repeat(3,1fr)}.request-panel{min-height:620px}.request-meta{grid-template-columns:64px minmax(0,1fr)}.request-meta>div:last-child{margin-top:8px;grid-column:1/-1}.result-panel{min-height:480px}.result-summary{align-items:flex-start;flex-direction:column}.request-actions{align-items:flex-end;flex-direction:column}.request-editor{min-height:280px}}
</style>
