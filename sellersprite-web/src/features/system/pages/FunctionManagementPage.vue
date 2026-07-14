<script setup lang="ts">
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'

import { ApiError } from '@/shared/api/ApiError'
import StatePanel from '@/shared/components/StatePanel.vue'

import {
  createFunction,
  deleteFunction,
  getFunctionApis,
  getFunctionTree,
  replaceFunctionApis,
  updateFunction,
  updateFunctionStatus,
} from '../api/functionApi'
import { pageApiOptions } from '../api/permissionOptionApi'
import type { FunctionPayload, SystemApiResource, SystemFunction } from '../model/system'

interface FunctionRow extends SystemFunction { depth: number }

const ROOT_PARENT_ID = '0'
const tree = ref<SystemFunction[]>([])
const loading = ref(true)
const loadError = ref('')
const editorOpen = ref(false)
const editorMode = ref<'create' | 'edit'>('create')
const editingId = ref<string | null>(null)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<FunctionPayload>(emptyForm())
const bindingOpen = ref(false)
const bindingFunction = ref<SystemFunction | null>(null)
const bindingApiIds = ref<string[]>([])
const apiOptions = ref<SystemApiResource[]>([])
const bindingLoading = ref(false)
const bindingSaving = ref(false)
const rows = computed(() => flatten(tree.value))
const editorTitle = computed(() => editorMode.value === 'create' ? '新增功能' : '编辑功能')
const rules: FormRules<FunctionPayload> = {
  parentId: [{ required: true, message: '请选择父功能', trigger: 'change' }],
  functionCode: [{ required: true, message: '请输入功能编码', trigger: 'blur' }],
  functionName: [{ required: true, message: '请输入功能名称', trigger: 'blur' }],
  functionType: [{ required: true, message: '请选择功能类型', trigger: 'change' }],
}

function emptyForm(): FunctionPayload {
  return { parentId: ROOT_PARENT_ID, functionCode: '', functionName: '', functionType: 'MENU', routePath: '', componentPath: '', permissionCode: '', sortOrder: 0, icon: '', visible: 1, cacheable: 1, externalLink: '', remark: '' }
}

function flatten(nodes: SystemFunction[], depth = 0): FunctionRow[] {
  return nodes.flatMap((node) => [{ ...node, depth }, ...flatten(node.children ?? [], depth + 1)])
}

function typeLabel(type: string) { return ({ DIR: '目录', MENU: '菜单', BUTTON: '按钮' } as Record<string, string>)[type] ?? type }

async function loadTree() {
  loading.value = true
  loadError.value = ''
  try { tree.value = await getFunctionTree() }
  catch (error) { loadError.value = messageOf(error, '功能树加载失败') }
  finally { loading.value = false }
}

async function loadApis() {
  try { apiOptions.value = (await pageApiOptions()).records }
  catch (error) { ElMessage.error(messageOf(error, '接口选项加载失败')) }
}

function openCreate(parentId = ROOT_PARENT_ID) {
  editorMode.value = 'create'
  editingId.value = null
  Object.assign(form, emptyForm(), { parentId })
  editorOpen.value = true
}

function openEdit(node: SystemFunction) {
  editorMode.value = 'edit'
  editingId.value = node.sysFunctionId
  Object.assign(form, {
    parentId: node.parentId,
    functionCode: node.functionCode,
    functionName: node.functionName,
    functionType: node.functionType as FunctionPayload['functionType'],
    routePath: node.routePath ?? '',
    componentPath: node.componentPath ?? '',
    permissionCode: node.permissionCode ?? '',
    sortOrder: node.sortOrder,
    icon: node.icon ?? '',
    visible: node.visible,
    cacheable: node.cacheable,
    externalLink: node.externalLink ?? '',
    remark: '',
  })
  editorOpen.value = true
}

async function submit() {
  if (!(await formRef.value?.validate()) || submitting.value) return
  submitting.value = true
  try {
    if (editorMode.value === 'create') await createFunction({ ...form })
    else if (editingId.value) await updateFunction(editingId.value, { ...form })
    ElMessage.success(editorMode.value === 'create' ? '功能已创建' : '功能已更新')
    editorOpen.value = false
    await loadTree()
  } catch (error) { ElMessage.error(messageOf(error, '功能保存失败')) }
  finally { submitting.value = false }
}

async function toggleStatus(node: SystemFunction) {
  try {
    await updateFunctionStatus(node.sysFunctionId, node.status === 1 ? 0 : 1)
    ElMessage.success(node.status === 1 ? '功能已停用' : '功能已启用')
    await loadTree()
  } catch (error) { ElMessage.error(messageOf(error, '状态更新失败')) }
}

async function remove(node: SystemFunction) {
  try {
    await ElMessageBox.confirm(`确认删除功能“${node.functionName}”？`, '删除功能', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    await deleteFunction(node.sysFunctionId)
    ElMessage.success('功能已删除')
    await loadTree()
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error, '功能删除失败')) }
}

async function openBinding(node: SystemFunction) {
  bindingFunction.value = node
  bindingOpen.value = true
  bindingLoading.value = true
  try { bindingApiIds.value = await getFunctionApis(node.sysFunctionId) }
  catch (error) { ElMessage.error(messageOf(error, '功能接口绑定加载失败')) }
  finally { bindingLoading.value = false }
}

async function saveBinding() {
  if (!bindingFunction.value || bindingSaving.value) return
  bindingSaving.value = true
  try {
    await replaceFunctionApis(bindingFunction.value.sysFunctionId, [...bindingApiIds.value])
    ElMessage.success('功能接口绑定已更新')
    bindingOpen.value = false
  } catch (error) { ElMessage.error(messageOf(error, '功能接口绑定保存失败')) }
  finally { bindingSaving.value = false }
}

function messageOf(error: unknown, fallback: string) { return error instanceof ApiError ? error.message : fallback }
onMounted(() => void Promise.all([loadTree(), loadApis()]))
</script>

<template>
  <section
    class="function-page"
    aria-label="功能菜单管理"
  >
    <header class="page-header">
      <div><h2>功能菜单树</h2><p>维护目录、菜单、按钮及其接口绑定</p></div><ElButton
        type="primary"
        @click="openCreate()"
      >
        新增根功能
      </ElButton>
    </header>
    <StatePanel
      v-if="loading && rows.length === 0"
      status="loading"
      title="正在加载功能树"
    />
    <StatePanel
      v-else-if="loadError && rows.length === 0"
      status="error"
      title="功能树加载失败"
      :description="loadError"
      action-label="重新加载"
      @action="loadTree"
    />
    <div
      v-else
      class="table-wrap"
    >
      <table>
        <thead><tr><th>功能名称</th><th>类型</th><th>路由 / 组件</th><th>权限码</th><th>排序</th><th>状态</th><th>操作</th></tr></thead><tbody>
          <tr
            v-for="node in rows"
            :key="node.sysFunctionId"
          >
            <td>
              <div
                class="function-name"
                :style="{ paddingLeft: `${node.depth * 22}px` }"
              >
                <span
                  v-if="node.depth"
                  class="branch"
                >└</span><strong>{{ node.functionName }}</strong><code>{{ node.functionCode }}</code>
              </div>
            </td><td><span :class="['type-badge', `is-${node.functionType.toLowerCase()}`]">{{ typeLabel(node.functionType) }}</span></td><td><div>{{ node.routePath || '--' }}</div><code>{{ node.componentPath || '--' }}</code></td><td><code>{{ node.permissionCode || '--' }}</code></td><td>{{ node.sortOrder }}</td><td :class="node.status === 1 ? 'enabled' : 'disabled'">
              {{ node.status === 1 ? '启用' : '停用' }}
            </td><td class="actions">
              <button
                type="button"
                @click="openCreate(node.sysFunctionId)"
              >
                新增下级
              </button><button
                type="button"
                @click="openBinding(node)"
              >
                绑定接口
              </button><button
                type="button"
                @click="openEdit(node)"
              >
                编辑
              </button><button
                type="button"
                @click="toggleStatus(node)"
              >
                {{ node.status === 1 ? '停用' : '启用' }}
              </button><button
                type="button"
                class="danger"
                @click="remove(node)"
              >
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table><StatePanel
        v-if="!loading && rows.length === 0"
        status="empty"
        title="暂无功能"
        description="点击新增根功能开始维护"
      />
    </div>

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
            label="父功能"
            prop="parentId"
          >
            <select
              v-model="form.parentId"
              aria-label="父功能"
            >
              <option :value="ROOT_PARENT_ID">
                根节点
              </option><option
                v-for="node in rows"
                :key="node.sysFunctionId"
                :value="node.sysFunctionId"
                :disabled="node.sysFunctionId === editingId"
              >
                {{ '  '.repeat(node.depth) }}{{ node.functionName }}
              </option>
            </select>
          </ElFormItem><ElFormItem
            label="功能类型"
            prop="functionType"
          >
            <select
              v-model="form.functionType"
              aria-label="功能类型"
            >
              <option value="DIR">
                目录
              </option><option value="MENU">
                菜单
              </option><option value="BUTTON">
                按钮
              </option>
            </select>
          </ElFormItem><ElFormItem
            label="功能编码"
            prop="functionCode"
          >
            <ElInput v-model="form.functionCode" />
          </ElFormItem><ElFormItem
            label="功能名称"
            prop="functionName"
          >
            <ElInput v-model="form.functionName" />
          </ElFormItem><ElFormItem label="路由路径">
            <ElInput
              v-model="form.routePath"
              placeholder="/system/users"
            />
          </ElFormItem><ElFormItem label="组件白名单路径">
            <ElInput
              v-model="form.componentPath"
              placeholder="system/users"
            />
          </ElFormItem><ElFormItem label="权限码">
            <ElInput
              v-model="form.permissionCode"
              placeholder="system:user:view"
            />
          </ElFormItem><ElFormItem label="图标">
            <ElInput v-model="form.icon" />
          </ElFormItem><ElFormItem label="排序值">
            <ElInputNumber
              v-model="form.sortOrder"
              :min="0"
            />
          </ElFormItem><ElFormItem label="外链地址">
            <ElInput v-model="form.externalLink" />
          </ElFormItem><ElFormItem label="菜单可见">
            <ElSwitch
              v-model="form.visible"
              :active-value="1"
              :inactive-value="0"
            />
          </ElFormItem><ElFormItem label="页面保活">
            <div class="switch-field">
              <ElSwitch
                v-model="form.cacheable"
                :active-value="1"
                :inactive-value="0"
              /><span>切换菜单时保留筛选、分页和页面状态</span>
            </div>
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
    <ElDrawer
      v-model="bindingOpen"
      :title="`绑定接口 · ${bindingFunction?.functionName ?? ''}`"
      size="min(640px, 100%)"
    >
      <StatePanel
        v-if="bindingLoading"
        status="loading"
        title="正在加载接口绑定"
      /><div
        v-else
        class="binding-panel"
      >
        <div class="binding-summary">
          <p>可多选该功能必需的接口；保存后所有拥有此功能的角色会自动重算接口授权。</p>
          <button
            type="button"
            @click="bindingApiIds = []"
          >
            清空选择
          </button>
        </div><select
          v-model="bindingApiIds"
          aria-label="功能接口"
          multiple
        >
          <option
            v-for="api in apiOptions"
            :key="api.sysApiId"
            :value="api.sysApiId"
          >
            {{ api.httpMethod }} {{ api.pathPattern }} · {{ api.apiName }}
          </option>
        </select><StatePanel
          v-if="apiOptions.length === 0"
          status="empty"
          title="暂无启用接口资源"
          description="请先在接口资源页创建接口"
        />
      </div><template #footer>
        <ElButton @click="bindingOpen = false">
          取消
        </ElButton><ElButton
          type="primary"
          :loading="bindingSaving"
          @click="saveBinding"
        >
          保存绑定
        </ElButton>
      </template>
    </ElDrawer>
  </section>
</template>

<style scoped>
.function-page{min-height:calc(100vh - 104px);overflow:hidden;background:#fff;border:1px solid var(--color-border);border-radius:10px}.page-header{display:flex;align-items:center;justify-content:space-between;padding:16px;border-bottom:1px solid var(--color-border)}h2{margin:0;font-size:16px}p{margin:5px 0 0;color:var(--color-text-secondary);font-size:12px}.table-wrap{overflow:auto}table{width:100%;min-width:1160px;border-collapse:collapse}th,td{padding:12px 14px;border-bottom:1px solid var(--color-border);text-align:left;font-size:13px}th{background:#f8fafc}.function-name{display:grid;grid-template-columns:auto 1fr;gap:2px 8px;align-items:center}.function-name code{grid-column:2;color:var(--color-text-secondary)}.branch{color:#94a3b8}.type-badge{display:inline-flex;padding:2px 8px;border-radius:999px;background:#e2e8f0}.type-badge.is-dir{color:#475569;background:#e2e8f0}.type-badge.is-menu{color:#1d4ed8;background:#dbeafe}.type-badge.is-button{color:#7c3aed;background:#ede9fe}.enabled{color:var(--color-success)}.disabled{color:var(--color-text-secondary)}.actions{display:flex;min-width:270px;gap:10px;white-space:nowrap}.actions button{padding:0;color:var(--color-primary);background:transparent;border:0;cursor:pointer}.actions .danger{color:var(--color-danger)}.form-grid{display:grid;grid-template-columns:1fr 1fr;gap:0 16px}.form-grid select{width:100%;height:32px;padding:0 10px;border:1px solid var(--color-border);border-radius:4px;background:#fff}.binding-summary{display:flex;align-items:center;justify-content:space-between;gap:16px}.binding-summary button{padding:0;color:var(--color-primary);white-space:nowrap;background:transparent;border:0;cursor:pointer}.binding-panel>select{width:100%;min-height:360px;margin-top:16px;padding:8px;border:1px solid var(--color-border);border-radius:6px}.binding-panel option{padding:8px}@media(max-width:768px){.page-header{align-items:flex-start}.form-grid{grid-template-columns:1fr}.function-page{min-height:calc(100vh - 88px)}}
.function-page {
  --color-primary: var(--color-brand-600);
  --color-text-primary: var(--color-text);
}
.switch-field{display:flex;align-items:center;gap:8px}.switch-field span{color:var(--color-text-secondary);font-size:12px}
</style>
