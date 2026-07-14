<script setup lang="ts">
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { nextTick, onMounted, reactive, ref } from 'vue'

import { ApiError } from '@/shared/api/ApiError'
import StatePanel from '@/shared/components/StatePanel.vue'

import { getFunctionTree, pageApiOptions } from '../api/permissionOptionApi'
import {
  createRole,
  deleteRole,
  getRole,
  getRolePermissions,
  pageRoles,
  replaceRolePermissions,
  updateRole,
  updateRoleStatus,
} from '../api/roleApi'
import type {
  RoleEffectiveApi,
  RolePayload,
  SystemApiResource,
  SystemFunction,
  SystemRole,
} from '../model/system'
import { grantSourceLabel } from '../utils/rolePresentation'

interface FunctionTreeExpose {
  setCheckedKeys(keys: string[]): void
  getCheckedKeys(): unknown[]
}

const records = ref<SystemRole[]>([])
const loading = ref(true)
const loadError = ref('')
const total = ref(0)
const current = ref(1)
const size = ref(20)
const filters = reactive<{ roleName: string; status: number | null }>({ roleName: '', status: null })
const editorOpen = ref(false)
const editorMode = ref<'create' | 'edit'>('create')
const editingId = ref<string | null>(null)
const editorSubmitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<RolePayload>(emptyRoleForm())
const permissionOpen = ref(false)
const permissionLoading = ref(false)
const permissionSaving = ref(false)
const permissionRole = ref<SystemRole | null>(null)
const functionTree = ref<SystemFunction[]>([])
const apiOptions = ref<SystemApiResource[]>([])
const selectedExtraApiIds = ref<string[]>([])
const effectiveApis = ref<RoleEffectiveApi[]>([])
const functionTreeRef = ref<FunctionTreeExpose>()
const functionTreeProps = { label: 'functionName', children: 'children' }
const rules: FormRules<RolePayload> = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
}

async function loadPage() {
  loading.value = true
  loadError.value = ''
  try {
    const page = await pageRoles({
      current: current.value,
      size: size.value,
      roleName: filters.roleName.trim() || undefined,
      status: filters.status ?? undefined,
    })
    records.value = page.records
    total.value = page.total
    current.value = page.current
    size.value = page.size
  } catch (error) {
    loadError.value = messageOf(error, '角色列表加载失败')
  } finally {
    loading.value = false
  }
}

async function loadPermissionOptions() {
  try {
    const [functions, apis] = await Promise.all([getFunctionTree(), pageApiOptions()])
    functionTree.value = functions
    apiOptions.value = apis.records
  } catch (error) {
    ElMessage.error(messageOf(error, '权限选项加载失败'))
  }
}

function search() {
  current.value = 1
  void loadPage()
}

function resetFilters() {
  filters.roleName = ''
  filters.status = null
  search()
}

function openCreate() {
  editorMode.value = 'create'
  editingId.value = null
  Object.assign(form, emptyRoleForm())
  editorOpen.value = true
}

async function openEdit(role: SystemRole) {
  editorMode.value = 'edit'
  editingId.value = role.roleId
  editorOpen.value = true
  try {
    const detail = await getRole(role.roleId)
    Object.assign(form, {
      roleCode: detail.roleCode,
      roleName: detail.roleName,
      roleType: detail.roleType || 'BUSINESS',
      sortOrder: detail.sortOrder || 0,
      remark: '',
    })
  } catch (error) {
    editorOpen.value = false
    ElMessage.error(messageOf(error, '角色详情加载失败'))
  }
}

async function submitEditor() {
  if (!formRef.value || editorSubmitting.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }
  editorSubmitting.value = true
  try {
    const payload = normalizedRolePayload()
    if (editorMode.value === 'create') {
      await createRole(payload)
      ElMessage.success('角色已创建')
    } else if (editingId.value) {
      await updateRole(editingId.value, payload)
      ElMessage.success('角色已更新')
    }
    editorOpen.value = false
    await loadPage()
  } catch (error) {
    ElMessage.error(messageOf(error, '角色保存失败'))
  } finally {
    editorSubmitting.value = false
  }
}

async function toggleStatus(role: SystemRole) {
  if (role.roleCode === 'admin') {
    ElMessage.warning('系统管理员角色不能停用')
    return
  }
  try {
    await updateRoleStatus(role.roleId, role.status === 1 ? 0 : 1)
    ElMessage.success(role.status === 1 ? '角色已停用' : '角色已启用')
    await loadPage()
  } catch (error) {
    ElMessage.error(messageOf(error, '角色状态更新失败'))
  }
}

async function removeRole(role: SystemRole) {
  if (role.roleCode === 'admin') {
    ElMessage.warning('系统管理员角色不能删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除角色“${role.roleName}”？`, '删除角色', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteRole(role.roleId)
    ElMessage.success('角色已删除')
    await loadPage()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(messageOf(error, '角色删除失败'))
    }
  }
}

async function openPermissions(role: SystemRole) {
  permissionRole.value = role
  permissionOpen.value = true
  permissionLoading.value = true
  let checkedFunctionIds: string[] | null = null
  try {
    const permission = await getRolePermissions(role.roleId)
    selectedExtraApiIds.value = [...permission.extraApiIds]
    effectiveApis.value = permission.effectiveApis
    checkedFunctionIds = permission.functionIds
  } catch (error) {
    ElMessage.error(messageOf(error, '角色权限加载失败'))
  } finally {
    permissionLoading.value = false
  }
  if (checkedFunctionIds) {
    await nextTick()
    functionTreeRef.value?.setCheckedKeys(checkedFunctionIds)
  }
}

async function savePermissions() {
  if (!permissionRole.value || permissionSaving.value) {
    return
  }
  permissionSaving.value = true
  try {
    const functionIds = functionTreeRef.value?.getCheckedKeys().map(String) ?? []
    const permission = await replaceRolePermissions(
      permissionRole.value.roleId,
      functionIds,
      [...selectedExtraApiIds.value],
    )
    effectiveApis.value = permission.effectiveApis
    ElMessage.success('角色权限已更新')
  } catch (error) {
    ElMessage.error(messageOf(error, '角色权限保存失败'))
  } finally {
    permissionSaving.value = false
  }
}

function normalizedRolePayload(): RolePayload {
  return {
    roleCode: form.roleCode.trim(),
    roleName: form.roleName.trim(),
    roleType: form.roleType.trim() || 'BUSINESS',
    sortOrder: Number(form.sortOrder) || 0,
    remark: form.remark.trim(),
  }
}

function emptyRoleForm(): RolePayload {
  return { roleCode: '', roleName: '', roleType: 'BUSINESS', sortOrder: 0, remark: '' }
}

function grantSourceClass(source: string) {
  return `is-${source.toLowerCase()}`
}

function messageOf(error: unknown, fallback: string) {
  return error instanceof ApiError ? error.message : fallback
}

onMounted(() => {
  void Promise.all([loadPermissionOptions(), loadPage()])
})
</script>

<template>
  <section
    class="role-management"
    aria-label="角色管理"
  >
    <header class="role-management__toolbar">
      <ElForm
        class="role-management__filters"
        :model="filters"
        inline
        @submit.prevent="search"
      >
        <ElFormItem label="角色名称">
          <ElInput
            v-model="filters.roleName"
            clearable
            placeholder="输入角色名称"
          />
        </ElFormItem>
        <ElFormItem label="状态">
          <select
            v-model="filters.status"
            class="role-management__select"
            aria-label="角色状态"
          >
            <option :value="null">
              全部状态
            </option>
            <option :value="1">
              启用
            </option>
            <option :value="0">
              停用
            </option>
          </select>
        </ElFormItem>
        <ElFormItem>
          <ElButton
            type="primary"
            native-type="submit"
          >
            查询
          </ElButton>
          <ElButton @click="resetFilters">
            重置
          </ElButton>
        </ElFormItem>
      </ElForm>
      <ElButton
        v-permission="'system:role:create'"
        type="primary"
        @click="openCreate"
      >
        新增角色
      </ElButton>
    </header>

    <StatePanel
      v-if="loadError && records.length === 0"
      status="error"
      title="角色列表加载失败"
      :description="loadError"
      action-label="重新加载"
      @action="loadPage"
    />
    <div
      v-else
      class="role-management__table-wrap"
    >
      <div
        v-if="loading"
        class="role-management__loading"
      >
        正在加载角色
      </div>
      <table v-else-if="records.length">
        <thead>
          <tr>
            <th>角色编码</th>
            <th>角色名称</th>
            <th>类型</th>
            <th>排序</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="role in records"
            :key="role.roleId"
          >
            <td><code>{{ role.roleCode }}</code></td>
            <td>{{ role.roleName }}</td>
            <td>{{ role.roleType || '--' }}</td>
            <td>{{ role.sortOrder }}</td>
            <td>
              <button
                v-permission="'system:role:status'"
                class="role-management__status"
                :class="role.status === 1 ? 'is-enabled' : 'is-disabled'"
                type="button"
                :disabled="role.roleCode === 'admin'"
                :aria-label="`${role.status === 1 ? '停用' : '启用'}角色 ${role.roleName}`"
                @click="toggleStatus(role)"
              >
                {{ role.status === 1 ? '启用' : '停用' }}
              </button>
            </td>
            <td class="role-management__actions">
              <ElButton
                v-permission="'system:role:permission'"
                link
                type="primary"
                @click="openPermissions(role)"
              >
                授权
              </ElButton>
              <ElButton
                v-permission="'system:role:update'"
                link
                type="primary"
                @click="openEdit(role)"
              >
                编辑
              </ElButton>
              <ElButton
                v-permission="'system:role:delete'"
                link
                type="danger"
                :disabled="role.roleCode === 'admin'"
                @click="removeRole(role)"
              >
                删除
              </ElButton>
            </td>
          </tr>
        </tbody>
      </table>
      <ElEmpty
        v-else
        description="暂无角色数据"
      />
    </div>

    <footer class="role-management__pagination">
      <span>共 {{ total }} 条，第 {{ current }} 页</span>
      <div>
        <button
          type="button"
          :disabled="current <= 1"
          @click="current -= 1; loadPage()"
        >
          上一页
        </button>
        <button
          type="button"
          :disabled="current * size >= total"
          @click="current += 1; loadPage()"
        >
          下一页
        </button>
      </div>
    </footer>

    <ElDrawer
      v-model="editorOpen"
      :title="editorMode === 'create' ? '新增角色' : '编辑角色'"
      size="480px"
      destroy-on-close
    >
      <ElForm
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <ElFormItem
          label="角色编码"
          prop="roleCode"
        >
          <ElInput
            v-model="form.roleCode"
            maxlength="64"
          />
        </ElFormItem>
        <ElFormItem
          label="角色名称"
          prop="roleName"
        >
          <ElInput
            v-model="form.roleName"
            maxlength="128"
          />
        </ElFormItem>
        <ElFormItem label="角色类型">
          <ElInput
            v-model="form.roleType"
            maxlength="32"
          />
        </ElFormItem>
        <ElFormItem label="排序值">
          <ElInputNumber
            v-model="form.sortOrder"
            :min="0"
            controls-position="right"
          />
        </ElFormItem>
        <ElFormItem label="备注">
          <ElInput
            v-model="form.remark"
            type="textarea"
            :rows="3"
            maxlength="512"
          />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="editorOpen = false">
          取消
        </ElButton>
        <ElButton
          type="primary"
          :loading="editorSubmitting"
          @click="submitEditor"
        >
          保存
        </ElButton>
      </template>
    </ElDrawer>

    <ElDrawer
      v-model="permissionOpen"
      :title="`角色授权 · ${permissionRole?.roleName || ''}`"
      size="760px"
    >
      <StatePanel
        v-if="permissionLoading"
        status="loading"
        title="正在加载角色权限"
      />
      <div
        v-else
        class="role-permission"
      >
        <section>
          <h3>功能权限</h3>
          <ElTree
            ref="functionTreeRef"
            :data="functionTree"
            :props="functionTreeProps"
            node-key="sysFunctionId"
            show-checkbox
            check-strictly
            default-expand-all
            empty-text="暂无功能"
          />
        </section>
        <section>
          <h3>额外接口</h3>
          <ElSelect
            v-model="selectedExtraApiIds"
            multiple
            filterable
            clearable
            placeholder="选择直接附加接口"
          >
            <ElOption
              v-for="api in apiOptions"
              :key="api.sysApiId"
              :label="`${api.httpMethod} ${api.pathPattern}`"
              :value="api.sysApiId"
            />
          </ElSelect>
          <p v-if="apiOptions.length === 0">
            暂无启用接口资源
          </p>
        </section>
        <section class="role-permission__preview">
          <h3>有效接口来源</h3>
          <table v-if="effectiveApis.length">
            <thead>
              <tr>
                <th>接口</th>
                <th>方法与路径</th>
                <th>来源</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="api in effectiveApis"
                :key="api.sysApiId"
              >
                <td>{{ api.apiName }}</td>
                <td><code>{{ api.httpMethod }} {{ api.pathPattern }}</code></td>
                <td>
                  <span
                    class="role-permission__source"
                    :class="grantSourceClass(api.grantSource)"
                  >{{ grantSourceLabel(api.grantSource) }}</span>
                </td>
              </tr>
            </tbody>
          </table>
          <ElEmpty
            v-else
            description="当前没有有效接口授权"
          />
        </section>
      </div>
      <template #footer>
        <ElButton @click="permissionOpen = false">
          关闭
        </ElButton>
        <ElButton
          type="primary"
          :loading="permissionSaving"
          @click="savePermissions"
        >
          保存授权
        </ElButton>
      </template>
    </ElDrawer>
  </section>
</template>

<style scoped>
.role-management {
  display: grid;
  height: calc(100vh - var(--header-height) - var(--content-gutter) * 2);
  min-height: 600px;
  grid-template-rows: auto minmax(0, 1fr) auto;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.role-management__toolbar {
  display: flex;
  padding: var(--space-4);
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  border-bottom: 1px solid var(--color-border);
}

.role-management__filters {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
}

.role-management__filters :deep(.el-form-item) {
  margin-bottom: 0;
}

.role-management__select {
  height: 32px;
  min-width: 140px;
  padding: 0 var(--space-3);
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
}

.role-management__table-wrap {
  min-width: 0;
  min-height: 0;
  overflow: auto;
}

.role-management__table-wrap table,
.role-permission table {
  width: 100%;
  min-width: 760px;
  border-collapse: collapse;
}

.role-management__table-wrap th,
.role-management__table-wrap td,
.role-permission th,
.role-permission td {
  padding: 11px var(--space-3);
  color: var(--color-text-secondary);
  border-bottom: 1px solid var(--color-border);
  font-size: var(--font-size-sm);
  text-align: left;
}

.role-management__table-wrap th,
.role-permission th {
  color: var(--color-text);
  background: var(--color-surface-muted);
  font-size: var(--font-size-xs);
}

.role-management__table-wrap code,
.role-permission code {
  font-family: var(--font-mono);
  font-size: var(--font-size-xs);
}

.role-management__loading {
  display: grid;
  min-height: 220px;
  place-items: center;
  color: var(--color-text-muted);
}

.role-management__status {
  padding: 0;
  color: var(--color-text-secondary);
  background: transparent;
  border: 0;
  cursor: pointer;
}

.role-management__status.is-enabled {
  color: var(--color-success);
}

.role-management__status:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.role-management__actions {
  min-width: 180px;
  white-space: nowrap;
}

.role-management__pagination {
  display: flex;
  min-height: 54px;
  padding: 0 var(--space-4);
  align-items: center;
  justify-content: space-between;
  color: var(--color-text-muted);
  border-top: 1px solid var(--color-border);
  font-size: var(--font-size-xs);
}

.role-management__pagination button {
  height: 30px;
  margin-left: var(--space-2);
  padding: 0 var(--space-3);
  color: var(--color-text-secondary);
  background: var(--color-surface);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
}

.role-permission {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-6);
}

.role-permission section {
  min-width: 0;
}

.role-permission h3 {
  margin: 0 0 var(--space-3);
  color: var(--color-text);
  font-size: var(--font-size-sm);
}

.role-permission :deep(.el-select) {
  width: 100%;
}

.role-permission section > p {
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
}

.role-permission__preview {
  grid-column: 1 / -1;
  overflow: auto;
}

.role-permission__source {
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
}

.role-permission__source.is-function {
  color: var(--color-brand-700);
}

.role-permission__source.is-extra {
  color: var(--color-warning);
}

.role-permission__source.is-both {
  color: var(--color-success);
}

@media (max-width: 900px) {
  .role-management__toolbar {
    align-items: stretch;
    flex-direction: column;
  }

  .role-management__toolbar > .el-button {
    align-self: flex-end;
  }
}

@media (max-width: 768px) {
  .role-permission {
    grid-template-columns: 1fr;
  }

  .role-permission__preview {
    grid-column: auto;
  }

  :global(.role-management + .el-overlay .el-drawer) {
    max-width: 94vw;
  }
}
</style>
