<script setup lang="ts">
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'

import { ApiError } from '@/shared/api/ApiError'
import StatePanel from '@/shared/components/StatePanel.vue'

import {
  createApiResource,
  deleteApiResource,
  loadBackendApiCatalog,
  pageApiResources,
  syncMenuApiBindings,
  updateApiResource,
  updateApiResourceStatus,
} from '../api/apiResourceApi'
import type { ApiResourcePayload, SystemApiResource } from '../model/system'

const records = ref<SystemApiResource[]>([])
const loading = ref(true)
const loadError = ref('')
const current = ref(1)
const size = ref(20)
const total = ref(0)
const filters = reactive({ keyword: '', apiType: '', httpMethod: '', status: null as number | null })
const editorOpen = ref(false)
const editorMode = ref<'create' | 'edit'>('create')
const editingId = ref<string | null>(null)
const submitting = ref(false)
const catalogSyncing = ref(false)
const bindingSyncing = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<ApiResourcePayload>(emptyForm())
const editorTitle = computed(() => editorMode.value === 'create' ? '新增接口资源' : '编辑接口资源')
const rules: FormRules<ApiResourcePayload> = {
  apiCode: [{ required: true, message: '请输入接口编码', trigger: 'blur' }],
  apiName: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
  apiType: [{ required: true, message: '请选择接口类型', trigger: 'change' }],
  httpMethod: [{ required: true, message: '请选择 HTTP 方法', trigger: 'change' }],
  pathPattern: [{ required: true, message: '请输入接口路径模式', trigger: 'blur' }],
}

function emptyForm(): ApiResourcePayload {
  return { apiCode: '', apiName: '', apiType: 'PERMISSION', httpMethod: 'GET', pathPattern: '', permissionCode: '', moduleName: '', operationName: '', remark: '' }
}

async function loadPage() {
  loading.value = true
  loadError.value = ''
  try {
    const page = await pageApiResources({ current: current.value, size: size.value, keyword: filters.keyword.trim() || undefined, apiType: filters.apiType || undefined, httpMethod: filters.httpMethod || undefined, status: filters.status ?? undefined })
    records.value = page.records
    current.value = page.current
    size.value = page.size
    total.value = page.total
  } catch (error) { loadError.value = messageOf(error, '接口资源加载失败') }
  finally { loading.value = false }
}

function search() { current.value = 1; void loadPage() }
function resetFilters() { Object.assign(filters, { keyword: '', apiType: '', httpMethod: '', status: null }); search() }
function openCreate() { editorMode.value = 'create'; editingId.value = null; Object.assign(form, emptyForm()); editorOpen.value = true }
function openEdit(api: SystemApiResource) {
  editorMode.value = 'edit'; editingId.value = api.sysApiId
  Object.assign(form, { apiCode: api.apiCode, apiName: api.apiName, apiType: api.apiType as ApiResourcePayload['apiType'], httpMethod: api.httpMethod, pathPattern: api.pathPattern, permissionCode: api.permissionCode ?? '', moduleName: api.moduleName ?? '', operationName: api.operationName ?? '', remark: '' })
  editorOpen.value = true
}

async function submit() {
  if (!(await formRef.value?.validate()) || submitting.value) return
  if (form.apiType === 'PERMISSION' && !form.permissionCode.trim()) { ElMessage.warning('权限接口必须填写权限码'); return }
  submitting.value = true
  try {
    const payload = { ...form, httpMethod: form.httpMethod.toUpperCase(), pathPattern: form.pathPattern.trim() }
    if (editorMode.value === 'create') await createApiResource(payload)
    else if (editingId.value) await updateApiResource(editingId.value, payload)
    ElMessage.success(editorMode.value === 'create' ? '接口资源已创建' : '接口资源已更新')
    editorOpen.value = false
    await loadPage()
  } catch (error) { ElMessage.error(messageOf(error, '接口资源保存失败')) }
  finally { submitting.value = false }
}

async function toggleStatus(api: SystemApiResource) {
  try {
    await updateApiResourceStatus(api.sysApiId, api.status === 1 ? 0 : 1)
    ElMessage.success(api.status === 1 ? '接口已停用' : '接口已启用')
    await loadPage()
  } catch (error) { ElMessage.error(messageOf(error, '状态更新失败')) }
}

async function remove(api: SystemApiResource) {
  try {
    await ElMessageBox.confirm(`确认删除接口“${api.apiName}”？`, '删除接口资源', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    await deleteApiResource(api.sysApiId)
    ElMessage.success('接口资源已删除')
    await loadPage()
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error, '接口资源删除失败')) }
}

async function loadCatalog() {
  try {
    await ElMessageBox.confirm('将扫描当前后端所有 /api 接口并幂等写入接口资源，是否继续？', '装载后端接口', { type: 'warning', confirmButtonText: '开始装载', cancelButtonText: '取消' })
    catalogSyncing.value = true
    const result = await loadBackendApiCatalog()
    ElMessage.success(`装载完成：扫描 ${result.scanned}，新增 ${result.created}，更新 ${result.updated}，未变化 ${result.unchanged}`)
    await loadPage()
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error, '后端接口装载失败')) }
  finally { catalogSyncing.value = false }
}

async function syncBindings() {
  try {
    await ElMessageBox.confirm('将按前端完整清单替换各菜单的接口绑定，并自动识别多菜单共享接口，是否继续？', '同步菜单绑定', { type: 'warning', confirmButtonText: '开始同步', cancelButtonText: '取消' })
    bindingSyncing.value = true
    const result = await syncMenuApiBindings()
    ElMessage.success(`同步完成：${result.functionCount} 个菜单，${result.bindingCount} 项绑定，${result.publicApiCount} 个共享接口`)
    await loadPage()
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error, '菜单接口绑定同步失败')) }
  finally { bindingSyncing.value = false }
}

function methodClass(method: string) { return `method-${method.toLowerCase()}` }
function messageOf(error: unknown, fallback: string) { return error instanceof ApiError ? error.message : fallback }
onMounted(() => void loadPage())
</script>

<template>
  <section
    class="api-page"
    aria-label="接口资源管理"
  >
    <div class="toolbar">
      <label>关键字<input
        v-model="filters.keyword"
        aria-label="接口关键字"
        placeholder="编码、名称或路径"
      ></label><label>类型<select
        v-model="filters.apiType"
        aria-label="接口类型筛选"
      ><option value="">全部类型</option><option value="PERMISSION">权限接口</option><option value="PUBLIC">共享接口（需登录）</option></select></label><label>方法<select
        v-model="filters.httpMethod"
        aria-label="HTTP 方法筛选"
      ><option value="">全部方法</option><option
        v-for="method in ['GET','POST','PUT','DELETE','PATCH']"
        :key="method"
        :value="method"
      >{{ method }}</option></select></label><label>状态<select
        v-model="filters.status"
        aria-label="接口状态筛选"
      ><option :value="null">全部状态</option><option :value="1">启用</option><option :value="0">停用</option></select></label><ElButton
        type="primary"
        @click="search"
      >
        查询
      </ElButton><ElButton @click="resetFilters">
        重置
      </ElButton><ElButton
        data-testid="load-api-catalog"
        :loading="catalogSyncing"
        @click="loadCatalog"
      >
        装载后端接口
      </ElButton><ElButton
        data-testid="sync-menu-bindings"
        :loading="bindingSyncing"
        @click="syncBindings"
      >
        同步菜单绑定
      </ElButton><ElButton
        class="create-button"
        type="primary"
        @click="openCreate"
      >
        新增接口
      </ElButton>
    </div>
    <StatePanel
      v-if="loading && records.length === 0"
      status="loading"
      title="正在加载接口资源"
    />
    <StatePanel
      v-else-if="loadError && records.length === 0"
      status="error"
      title="接口资源加载失败"
      :description="loadError"
      action-label="重新加载"
      @action="loadPage"
    />
    <div
      v-else
      class="table-wrap"
    >
      <table>
        <thead><tr><th>接口</th><th>类型</th><th>方法与路径</th><th>权限码</th><th>模块 / 操作</th><th>状态</th><th>操作</th></tr></thead><tbody>
          <tr
            v-for="api in records"
            :key="api.sysApiId"
          >
            <td><strong>{{ api.apiName }}</strong><code>{{ api.apiCode }}</code></td><td>{{ api.apiType === 'PUBLIC' ? '共享（需登录）' : '权限' }}</td><td><span :class="['method', methodClass(api.httpMethod)]">{{ api.httpMethod }}</span><code class="path">{{ api.pathPattern }}</code></td><td><code>{{ api.permissionCode || '--' }}</code></td><td><div>{{ api.moduleName || '--' }}</div><small>{{ api.operationName || '--' }}</small></td><td :class="api.status === 1 ? 'enabled' : 'disabled'">
              {{ api.status === 1 ? '启用' : '停用' }}
            </td><td class="actions">
              <button
                type="button"
                @click="openEdit(api)"
              >
                编辑
              </button><button
                type="button"
                @click="toggleStatus(api)"
              >
                {{ api.status === 1 ? '停用' : '启用' }}
              </button><button
                type="button"
                class="danger"
                @click="remove(api)"
              >
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table><StatePanel
        v-if="!loading && records.length === 0"
        status="empty"
        title="暂无接口资源"
        description="未登记接口会被权限拦截器默认拒绝，请按需创建"
      />
    </div>
    <footer class="pager">
      <span>共 {{ total }} 条，第 {{ current }} 页</span><div>
        <button
          :disabled="current <= 1"
          @click="current -= 1; loadPage()"
        >
          上一页
        </button><button
          :disabled="current * size >= total"
          @click="current += 1; loadPage()"
        >
          下一页
        </button>
      </div>
    </footer>

    <ElDrawer
      v-model="editorOpen"
      :title="editorTitle"
      size="min(560px, 100%)"
    >
      <ElForm
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <div class="form-grid">
          <ElFormItem
            label="接口编码"
            prop="apiCode"
          >
            <ElInput v-model="form.apiCode" />
          </ElFormItem><ElFormItem
            label="接口名称"
            prop="apiName"
          >
            <ElInput v-model="form.apiName" />
          </ElFormItem><ElFormItem
            label="接口类型"
            prop="apiType"
          >
            <select
              v-model="form.apiType"
              aria-label="接口类型"
            >
              <option value="PERMISSION">
                权限接口
              </option><option value="PUBLIC">
                共享接口（需登录）
              </option>
            </select>
          </ElFormItem><ElFormItem
            label="HTTP 方法"
            prop="httpMethod"
          >
            <select
              v-model="form.httpMethod"
              aria-label="HTTP 方法"
            >
              <option
                v-for="method in ['GET','POST','PUT','DELETE','PATCH']"
                :key="method"
                :value="method"
              >
                {{ method }}
              </option>
            </select>
          </ElFormItem><ElFormItem
            class="wide"
            label="路径模式"
            prop="pathPattern"
          >
            <ElInput
              v-model="form.pathPattern"
              placeholder="/api/users/{id}"
            />
          </ElFormItem><ElFormItem label="权限码">
            <ElInput
              v-model="form.permissionCode"
              :disabled="form.apiType === 'PUBLIC'"
              placeholder="system:user:view"
            />
          </ElFormItem><ElFormItem label="模块名称">
            <ElInput v-model="form.moduleName" />
          </ElFormItem><ElFormItem label="操作名称">
            <ElInput v-model="form.operationName" />
          </ElFormItem>
        </div><ElFormItem label="备注">
          <ElInput
            v-model="form.remark"
            type="textarea"
          />
        </ElFormItem>
      </ElForm><template #footer>
        <ElButton @click="editorOpen = false">
          取消
        </ElButton><ElButton
          type="primary"
          :loading="submitting"
          @click="submit"
        >
          保存
        </ElButton>
      </template>
    </ElDrawer>
  </section>
</template>

<style scoped>
.api-page{display:flex;min-height:calc(100vh - 104px);flex-direction:column;overflow:hidden;background:#fff;border:1px solid var(--color-border);border-radius:10px}.toolbar{display:flex;align-items:flex-end;gap:10px;padding:16px;border-bottom:1px solid var(--color-border)}.toolbar label{display:grid;gap:5px;color:var(--color-text-secondary);font-size:12px}.toolbar input,.toolbar select,.form-grid select{height:34px;padding:0 10px;border:1px solid var(--color-border);border-radius:6px;background:#fff}.toolbar input{width:230px}.create-button{margin-left:auto}.table-wrap{overflow:auto}table{width:100%;min-width:1100px;border-collapse:collapse}th,td{padding:12px 14px;border-bottom:1px solid var(--color-border);text-align:left;font-size:13px}th{background:#f8fafc}td:first-child strong,td:first-child code{display:block}code,small{color:var(--color-text-secondary)}.method{display:inline-flex;min-width:54px;justify-content:center;margin-right:8px;padding:2px 7px;border-radius:4px;font-weight:700}.method-get{color:#166534;background:#dcfce7}.method-post{color:#1d4ed8;background:#dbeafe}.method-put,.method-patch{color:#92400e;background:#fef3c7}.method-delete{color:#b91c1c;background:#fee2e2}.path{color:var(--color-text-primary)}.enabled{color:var(--color-success)}.disabled{color:var(--color-text-secondary)}.actions{display:flex;gap:12px}.actions button,.pager button{padding:0;color:var(--color-primary);background:transparent;border:0;cursor:pointer}.actions .danger{color:var(--color-danger)}.pager{display:flex;justify-content:space-between;margin-top:auto;padding:12px 16px;border-top:1px solid var(--color-border);color:var(--color-text-secondary);font-size:12px}.pager div{display:flex;gap:16px}.pager button:disabled{cursor:not-allowed;opacity:.4}.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:0 16px}.form-grid select{width:100%;height:32px}.form-grid .wide{grid-column:1/-1}@media(max-width:900px){.toolbar{flex-wrap:wrap}.create-button{margin-left:0}.toolbar input{width:200px}}@media(max-width:640px){.toolbar label:first-child{width:100%}.toolbar input{width:100%}.form-grid{grid-template-columns:1fr}.form-grid .wide{grid-column:auto}}
.api-page {
  --color-primary: var(--color-brand-600);
  --color-text-primary: var(--color-text);
}
</style>
