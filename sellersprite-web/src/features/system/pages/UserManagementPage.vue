<script setup lang="ts">
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'

import { useAuthStore } from '@/features/auth/stores/useAuthStore'
import { ApiError } from '@/shared/api/ApiError'
import StatePanel from '@/shared/components/StatePanel.vue'

import { getDepartmentTree, listEnabledRoles } from '../api/systemOptionApi'
import {
  createUser,
  deleteUser,
  getUser,
  pageUsers,
  replaceUserRoles,
  resetUserPassword,
  updateUser,
  updateUserStatus,
} from '../api/userApi'
import type {
  DepartmentNode,
  SystemRole,
  SystemUser,
  UserCreatePayload,
  UserUpdatePayload,
} from '../model/system'

interface UserEditorForm {
  username: string
  password: string
  nickname: string
  realName: string
  mobile: string
  email: string
  primaryDeptId: string | null
  roleIds: string[]
}

interface DepartmentOption {
  value: string
  label: string
  disabled: boolean
  children: DepartmentOption[]
}

const authStore = useAuthStore()
const loading = ref(true)
const loadError = ref('')
const records = ref<SystemUser[]>([])
const total = ref(0)
const current = ref(1)
const size = ref(20)
const filters = reactive<{ username: string; status: number | null }>({
  username: '',
  status: null,
})
const roles = ref<SystemRole[]>([])
const departments = ref<DepartmentNode[]>([])
const editorOpen = ref(false)
const editorMode = ref<'create' | 'edit'>('create')
const editingUserId = ref<string | null>(null)
const editorSubmitting = ref(false)
const formRef = ref<FormInstance>()
const detailOpen = ref(false)
const detailUser = ref<SystemUser | null>(null)
const detailLoading = ref(false)
const form = reactive<UserEditorForm>(emptyForm())

const roleNameMap = computed(() => new Map(roles.value.map((role) => [role.roleId, role.roleName])))
const departmentNameMap = computed(() => {
  const result = new Map<string, string>()
  function visit(nodes: DepartmentNode[]) {
    for (const node of nodes) {
      result.set(node.deptId, node.deptName)
      visit(node.children)
    }
  }
  visit(departments.value)
  return result
})
const departmentOptions = computed(() => mapDepartmentOptions(departments.value))
const editorTitle = computed(() => editorMode.value === 'create' ? '新增用户' : '编辑用户')
const rules = computed<FormRules<UserEditorForm>>(() => ({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { max: 64, message: '用户名不能超过64个字符', trigger: 'blur' },
  ],
  password: editorMode.value === 'create'
    ? [
        { required: true, message: '请输入初始密码', trigger: 'blur' },
        { min: 6, max: 128, message: '密码长度必须在6到128个字符之间', trigger: 'blur' },
      ]
    : [],
  nickname: [{ max: 64, message: '昵称不能超过64个字符', trigger: 'blur' }],
  realName: [{ max: 64, message: '真实姓名不能超过64个字符', trigger: 'blur' }],
  mobile: [{ max: 32, message: '手机号不能超过32个字符', trigger: 'blur' }],
  email: [
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
    { max: 128, message: '邮箱不能超过128个字符', trigger: 'blur' },
  ],
}))

async function loadPage() {
  loading.value = true
  loadError.value = ''
  try {
    const page = await pageUsers({
      current: current.value,
      size: size.value,
      username: filters.username.trim() || undefined,
      status: filters.status ?? undefined,
    })
    records.value = page.records
    total.value = page.total
    current.value = page.current
    size.value = page.size
  } catch (error) {
    loadError.value = messageOf(error, '用户列表加载失败')
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  try {
    const [roleOptions, departmentTree] = await Promise.all([
      listEnabledRoles(),
      getDepartmentTree(),
    ])
    roles.value = roleOptions
    departments.value = departmentTree
  } catch (error) {
    ElMessage.error(messageOf(error, '用户选项加载失败'))
  }
}

function search() {
  current.value = 1
  void loadPage()
}

function resetFilters() {
  filters.username = ''
  filters.status = null
  current.value = 1
  void loadPage()
}

function openCreate() {
  editorMode.value = 'create'
  editingUserId.value = null
  Object.assign(form, emptyForm())
  editorOpen.value = true
}

async function openEdit(user: SystemUser) {
  editorMode.value = 'edit'
  editingUserId.value = user.userId
  editorOpen.value = true
  try {
    const detail = await getUser(user.userId)
    assignForm(detail)
  } catch (error) {
    editorOpen.value = false
    ElMessage.error(messageOf(error, '用户详情加载失败'))
  }
}

async function openDetail(user: SystemUser) {
  detailOpen.value = true
  detailLoading.value = true
  detailUser.value = null
  try {
    detailUser.value = await getUser(user.userId)
  } catch (error) {
    ElMessage.error(messageOf(error, '用户详情加载失败'))
  } finally {
    detailLoading.value = false
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
    if (editorMode.value === 'create') {
      await createUser(createPayload())
      ElMessage.success('用户已创建')
    } else if (editingUserId.value) {
      await updateUser(editingUserId.value, updatePayload())
      await replaceUserRoles(editingUserId.value, [...form.roleIds])
      ElMessage.success('用户已更新')
    }
    editorOpen.value = false
    await loadPage()
  } catch (error) {
    ElMessage.error(messageOf(error, '用户保存失败'))
  } finally {
    editorSubmitting.value = false
  }
}

async function toggleStatus(user: SystemUser) {
  if (user.userId === authStore.user?.userId) {
    ElMessage.warning('不能修改当前登录用户状态')
    return
  }
  const nextStatus = user.status === 1 ? 0 : 1
  try {
    await updateUserStatus(user.userId, nextStatus)
    ElMessage.success(nextStatus === 1 ? '用户已启用' : '用户已停用')
    await loadPage()
  } catch (error) {
    ElMessage.error(messageOf(error, '用户状态更新失败'))
  }
}

async function resetPassword(user: SystemUser) {
  try {
    const result = await ElMessageBox.prompt(`为用户 ${user.username} 设置新密码`, '重置密码', {
      inputType: 'password',
      inputPattern: /^.{6,128}$/s,
      inputErrorMessage: '密码长度必须在6到128个字符之间',
      confirmButtonText: '重置',
      cancelButtonText: '取消',
    })
    await resetUserPassword(user.userId, result.value)
    ElMessage.success('密码已重置，现有会话已撤销')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(messageOf(error, '密码重置失败'))
    }
  }
}

async function removeUser(user: SystemUser) {
  if (user.userId === authStore.user?.userId) {
    ElMessage.warning('不能删除当前登录用户')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除用户“${user.username}”？`, '删除用户', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteUser(user.userId)
    ElMessage.success('用户已删除')
    if (records.value.length === 1 && current.value > 1) {
      current.value -= 1
    }
    await loadPage()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(messageOf(error, '用户删除失败'))
    }
  }
}

function roleNames(user: SystemUser) {
  const names = user.roleIds.map((roleId) => roleNameMap.value.get(roleId) || roleId)
  return names.length ? names.join('、') : '--'
}

function departmentName(user: SystemUser) {
  return user.primaryDeptId ? departmentNameMap.value.get(user.primaryDeptId) || user.primaryDeptId : '--'
}

function assignForm(user: SystemUser) {
  Object.assign(form, {
    username: user.username,
    password: '',
    nickname: user.nickname || '',
    realName: user.realName || '',
    mobile: user.mobile || '',
    email: user.email || '',
    primaryDeptId: user.primaryDeptId,
    roleIds: [...user.roleIds],
  })
}

function createPayload(): UserCreatePayload {
  return {
    username: form.username.trim(),
    password: form.password,
    nickname: form.nickname.trim(),
    realName: form.realName.trim(),
    mobile: form.mobile.trim(),
    email: form.email.trim(),
    primaryDeptId: form.primaryDeptId,
    roleIds: [...form.roleIds],
  }
}

function updatePayload(): UserUpdatePayload {
  return {
    username: form.username.trim(),
    nickname: form.nickname.trim(),
    realName: form.realName.trim(),
    mobile: form.mobile.trim(),
    email: form.email.trim(),
    primaryDeptId: form.primaryDeptId,
  }
}

function emptyForm(): UserEditorForm {
  return {
    username: '',
    password: '',
    nickname: '',
    realName: '',
    mobile: '',
    email: '',
    primaryDeptId: null,
    roleIds: [],
  }
}

function mapDepartmentOptions(nodes: DepartmentNode[]): DepartmentOption[] {
  return nodes.map((node) => ({
    value: node.deptId,
    label: node.deptName,
    disabled: node.status !== 1,
    children: mapDepartmentOptions(node.children),
  }))
}

function messageOf(error: unknown, fallback: string) {
  return error instanceof ApiError ? error.message : fallback
}

onMounted(() => {
  void Promise.all([loadOptions(), loadPage()])
})
</script>

<template>
  <section
    class="user-management"
    aria-label="用户管理"
  >
    <div class="user-management__toolbar">
      <ElForm
        class="user-management__filters"
        :model="filters"
        inline
        @submit.prevent="search"
      >
        <ElFormItem label="用户名">
          <ElInput
            v-model="filters.username"
            clearable
            placeholder="输入用户名"
            @keyup.enter="search"
          />
        </ElFormItem>
        <ElFormItem label="状态">
          <select
            v-model="filters.status"
            class="user-management__native-select"
            aria-label="用户状态"
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
        v-permission="'system:user:create'"
        type="primary"
        @click="openCreate"
      >
        新增用户
      </ElButton>
    </div>

    <StatePanel
      v-if="loadError && records.length === 0"
      status="error"
      title="用户列表加载失败"
      :description="loadError"
      action-label="重新加载"
      @action="loadPage"
    />

    <div
      v-else
      class="user-management__table-wrap"
    >
      <div
        v-if="loading"
        class="user-management__table-loading"
        aria-busy="true"
      >
        正在加载用户
      </div>
      <table v-else-if="records.length">
        <thead>
          <tr>
            <th scope="col">
              用户名
            </th>
            <th scope="col">
              姓名
            </th>
            <th scope="col">
              联系方式
            </th>
            <th scope="col">
              主部门
            </th>
            <th scope="col">
              角色
            </th>
            <th scope="col">
              状态
            </th>
            <th scope="col">
              操作
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="row in records"
            :key="row.userId"
          >
            <td>
              <button
                class="user-management__username"
                type="button"
                @click="openDetail(row)"
              >
                {{ row.username }}
              </button>
            </td>
            <td>
              <div>{{ row.nickname || '--' }}</div>
              <small>{{ row.realName || '--' }}</small>
            </td>
            <td>
              <div>{{ row.mobile || '--' }}</div>
              <small>{{ row.email || '--' }}</small>
            </td>
            <td>
              {{ departmentName(row) }}
            </td>
            <td class="user-management__roles">
              {{ roleNames(row) }}
            </td>
            <td>
              <button
                v-permission="'system:user:status'"
                class="user-status"
                :class="row.status === 1 ? 'is-enabled' : 'is-disabled'"
                type="button"
                :disabled="row.userId === authStore.user?.userId"
                :aria-label="`${row.status === 1 ? '停用' : '启用'}用户 ${row.username}`"
                @click="toggleStatus(row)"
              >
                <span aria-hidden="true" />
                {{ row.status === 1 ? '启用' : '停用' }}
              </button>
            </td>
            <td class="user-management__actions">
              <ElButton
                link
                type="primary"
                @click="openDetail(row)"
              >
                详情
              </ElButton>
              <ElButton
                v-permission="'system:user:update'"
                link
                type="primary"
                @click="openEdit(row)"
              >
                编辑
              </ElButton>
              <ElButton
                v-permission="'system:user:password'"
                link
                type="warning"
                @click="resetPassword(row)"
              >
                重置密码
              </ElButton>
              <ElButton
                v-permission="'system:user:delete'"
                link
                type="danger"
                :disabled="row.userId === authStore.user?.userId"
                @click="removeUser(row)"
              >
                删除
              </ElButton>
            </td>
          </tr>
        </tbody>
      </table>
      <ElEmpty
        v-else
        description="暂无用户数据"
      />
    </div>

    <div class="user-management__pagination">
      <span>共 {{ total }} 条</span>
      <div class="user-management__pager-controls">
        <select
          v-model.number="size"
          class="user-management__native-select"
          aria-label="每页条数"
          @change="search"
        >
          <option :value="10">
            10 条/页
          </option>
          <option :value="20">
            20 条/页
          </option>
          <option :value="50">
            50 条/页
          </option>
          <option :value="100">
            100 条/页
          </option>
        </select>
        <button
          type="button"
          :disabled="current <= 1"
          @click="current -= 1; loadPage()"
        >
          上一页
        </button>
        <span>第 {{ current }} 页</span>
        <button
          type="button"
          :disabled="current * size >= total"
          @click="current += 1; loadPage()"
        >
          下一页
        </button>
      </div>
    </div>

    <ElDrawer
      v-model="editorOpen"
      :title="editorTitle"
      size="520px"
      destroy-on-close
    >
      <ElForm
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <div class="user-editor__grid">
          <ElFormItem
            label="用户名"
            prop="username"
          >
            <ElInput
              v-model="form.username"
              maxlength="64"
            />
          </ElFormItem>
          <ElFormItem
            v-if="editorMode === 'create'"
            label="初始密码"
            prop="password"
          >
            <ElInput
              v-model="form.password"
              type="password"
              show-password
              maxlength="128"
            />
          </ElFormItem>
          <ElFormItem
            label="昵称"
            prop="nickname"
          >
            <ElInput
              v-model="form.nickname"
              maxlength="64"
            />
          </ElFormItem>
          <ElFormItem
            label="真实姓名"
            prop="realName"
          >
            <ElInput
              v-model="form.realName"
              maxlength="64"
            />
          </ElFormItem>
          <ElFormItem
            label="手机号"
            prop="mobile"
          >
            <ElInput
              v-model="form.mobile"
              maxlength="32"
            />
          </ElFormItem>
          <ElFormItem
            label="邮箱"
            prop="email"
          >
            <ElInput
              v-model="form.email"
              maxlength="128"
            />
          </ElFormItem>
        </div>
        <ElFormItem label="主部门">
          <ElTreeSelect
            v-model="form.primaryDeptId"
            :data="departmentOptions"
            clearable
            check-strictly
            default-expand-all
            placeholder="选择主部门"
          />
        </ElFormItem>
        <ElFormItem label="角色">
          <ElSelect
            v-model="form.roleIds"
            multiple
            clearable
            placeholder="选择角色"
          >
            <ElOption
              v-for="role in roles"
              :key="role.roleId"
              :label="role.roleName"
              :value="role.roleId"
            />
          </ElSelect>
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
      v-model="detailOpen"
      title="用户详情"
      size="460px"
    >
      <StatePanel
        v-if="detailLoading"
        status="loading"
        title="正在加载用户详情"
      />
      <ElDescriptions
        v-else-if="detailUser"
        :column="1"
        border
      >
        <ElDescriptionsItem label="用户名">
          {{ detailUser.username }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="昵称">
          {{ detailUser.nickname || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="真实姓名">
          {{ detailUser.realName || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="手机号">
          {{ detailUser.mobile || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="邮箱">
          {{ detailUser.email || '--' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="主部门">
          {{ departmentName(detailUser) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="角色">
          {{ roleNames(detailUser) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          {{ detailUser.status === 1 ? '启用' : '停用' }}
        </ElDescriptionsItem>
      </ElDescriptions>
    </ElDrawer>
  </section>
</template>

<style scoped>
.user-management {
  display: grid;
  height: calc(100vh - var(--header-height) - var(--content-gutter) * 2);
  min-height: 600px;
  grid-template-rows: auto minmax(0, 1fr) auto;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.user-management__toolbar {
  display: flex;
  min-width: 0;
  padding: var(--space-4);
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  border-bottom: 1px solid var(--color-border);
}

.user-management__filters {
  display: flex;
  flex: 1;
  flex-wrap: wrap;
}

.user-management__filters :deep(.el-form-item) {
  margin-bottom: 0;
}

.user-management__filters :deep(.el-input) {
  width: 200px;
}

.user-management__table-wrap {
  position: relative;
  min-width: 0;
  min-height: 0;
  overflow: auto;
}

.user-management__table-wrap table {
  width: 100%;
  min-width: 1120px;
  border-collapse: collapse;
}

.user-management__table-wrap th,
.user-management__table-wrap td {
  padding: 11px var(--space-3);
  color: var(--color-text-secondary);
  border-bottom: 1px solid var(--color-border);
  font-size: var(--font-size-sm);
  text-align: left;
  vertical-align: middle;
}

.user-management__table-wrap th {
  position: sticky;
  top: 0;
  z-index: 1;
  color: var(--color-text);
  background: var(--color-surface-muted);
  font-size: var(--font-size-xs);
  font-weight: 600;
}

.user-management__table-wrap tbody tr:hover {
  background: var(--color-brand-50);
}

.user-management__table-loading {
  display: grid;
  min-height: 220px;
  place-items: center;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.user-management__table-wrap small {
  display: block;
  margin-top: 2px;
  color: var(--color-text-muted);
  font-size: 10px;
}

.user-management__roles {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-management__actions {
  min-width: 250px;
  white-space: nowrap;
}

.user-management__native-select {
  height: 32px;
  min-width: 140px;
  padding: 0 30px 0 var(--space-3);
  color: var(--color-text);
  background: var(--color-surface);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font: inherit;
}

.user-management__username {
  padding: 0;
  color: var(--color-brand-700);
  background: transparent;
  border: 0;
  font: inherit;
  cursor: pointer;
}

.user-management__username:hover {
  text-decoration: underline;
  text-underline-offset: 2px;
}

.user-status {
  display: inline-flex;
  height: 26px;
  padding: 0;
  align-items: center;
  gap: 6px;
  color: var(--color-text-secondary);
  background: transparent;
  border: 0;
  font-size: var(--font-size-xs);
  cursor: pointer;
}

.user-status:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.user-status span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.user-status.is-enabled span {
  background: var(--color-success);
}

.user-status.is-disabled span {
  background: var(--color-text-muted);
}

.user-management__pagination {
  display: flex;
  min-height: 54px;
  padding: 0 var(--space-4);
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  color: var(--color-text-muted);
  border-top: 1px solid var(--color-border);
  font-size: var(--font-size-xs);
}

.user-management__pager-controls {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.user-management__pager-controls button {
  height: 30px;
  padding: 0 var(--space-3);
  color: var(--color-text-secondary);
  background: var(--color-surface);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  cursor: pointer;
}

.user-management__pager-controls button:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.user-editor__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 var(--space-4);
}

@media (max-width: 900px) {
  .user-management__toolbar {
    flex-direction: column;
  }

  .user-management__toolbar > .el-button {
    align-self: flex-end;
  }
}

@media (max-width: 768px) {
  .user-management {
    min-height: 680px;
  }

  .user-management__filters {
    display: grid;
    width: 100%;
    grid-template-columns: 1fr;
  }

  .user-management__filters :deep(.el-form-item),
  .user-management__filters :deep(.el-input),
  .user-management__native-select {
    width: 100%;
  }

  .user-management__pagination {
    align-items: flex-start;
    flex-direction: column;
    padding-block: var(--space-3);
  }

  .user-management__pager-controls {
    max-width: 100%;
    flex-wrap: wrap;
    overflow-x: auto;
  }

  .user-editor__grid {
    grid-template-columns: 1fr;
  }

  :global(.el-drawer) {
    max-width: 92vw;
  }
}
</style>
