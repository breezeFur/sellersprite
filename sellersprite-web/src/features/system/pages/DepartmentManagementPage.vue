<script setup lang="ts">
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'

import { ApiError } from '@/shared/api/ApiError'
import StatePanel from '@/shared/components/StatePanel.vue'

import {
  createDepartment,
  deleteDepartment,
  getDepartment,
  getDepartmentTree,
  updateDepartment,
  updateDepartmentStatus,
} from '../api/departmentApi'
import DepartmentTreeNode from '../components/DepartmentTreeNode.vue'
import type { DepartmentNode, DepartmentPayload } from '../model/system'

const ROOT_PARENT_ID = '0'
const tree = ref<DepartmentNode[]>([])
const loading = ref(true)
const loadError = ref('')
const editorOpen = ref(false)
const editorMode = ref<'create' | 'edit'>('create')
const editingId = ref<string | null>(null)
const editorSubmitting = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<DepartmentPayload>(emptyForm())
const rules: FormRules<DepartmentPayload> = {
  parentId: [{ required: true, message: '请选择父部门', trigger: 'change' }],
  deptCode: [{ required: true, message: '请输入部门编码', trigger: 'blur' }],
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
}

const editorTitle = computed(() => editorMode.value === 'create' ? '新增部门' : '编辑部门')
const departmentOptions = computed(() => [
  { value: ROOT_PARENT_ID, label: '根节点', children: [] },
  ...mapOptions(tree.value),
])

async function loadTree() {
  loading.value = true
  loadError.value = ''
  try {
    tree.value = await getDepartmentTree()
  } catch (error) {
    loadError.value = messageOf(error, '部门树加载失败')
  } finally {
    loading.value = false
  }
}

function openRootCreate() {
  openCreate(ROOT_PARENT_ID)
}

function openChildCreate(parent: DepartmentNode) {
  openCreate(parent.deptId)
}

function openCreate(parentId: string) {
  editorMode.value = 'create'
  editingId.value = null
  Object.assign(form, emptyForm(), { parentId })
  editorOpen.value = true
}

async function openEdit(node: DepartmentNode) {
  editorMode.value = 'edit'
  editingId.value = node.deptId
  editorOpen.value = true
  try {
    const detail = await getDepartment(node.deptId)
    Object.assign(form, {
      parentId: detail.parentId,
      deptCode: detail.deptCode,
      deptName: detail.deptName,
      leaderUserId: null,
      sortOrder: detail.sortOrder,
      remark: '',
    })
  } catch (error) {
    editorOpen.value = false
    ElMessage.error(messageOf(error, '部门详情加载失败'))
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
    const payload = normalizedPayload()
    if (editorMode.value === 'create') {
      await createDepartment(payload)
      ElMessage.success('部门已创建')
    } else if (editingId.value) {
      await updateDepartment(editingId.value, payload)
      ElMessage.success('部门已更新')
    }
    editorOpen.value = false
    await loadTree()
  } catch (error) {
    ElMessage.error(messageOf(error, '部门保存失败'))
  } finally {
    editorSubmitting.value = false
  }
}

async function toggleStatus(node: DepartmentNode) {
  try {
    await updateDepartmentStatus(node.deptId, node.status === 1 ? 0 : 1)
    ElMessage.success(node.status === 1 ? '部门已停用' : '部门已启用')
    await loadTree()
  } catch (error) {
    ElMessage.error(messageOf(error, '部门状态更新失败'))
  }
}

async function removeNode(node: DepartmentNode) {
  try {
    await ElMessageBox.confirm(`确认删除部门“${node.deptName}”？`, '删除部门', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteDepartment(node.deptId)
    ElMessage.success('部门已删除')
    await loadTree()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(messageOf(error, '部门删除失败'))
    }
  }
}

function normalizedPayload(): DepartmentPayload {
  return {
    parentId: form.parentId,
    deptCode: form.deptCode.trim(),
    deptName: form.deptName.trim(),
    leaderUserId: form.leaderUserId?.trim() || null,
    sortOrder: Number(form.sortOrder) || 0,
    remark: form.remark.trim(),
  }
}

function emptyForm(): DepartmentPayload {
  return {
    parentId: ROOT_PARENT_ID,
    deptCode: '',
    deptName: '',
    leaderUserId: null,
    sortOrder: 0,
    remark: '',
  }
}

function mapOptions(nodes: DepartmentNode[]): Array<{ value: string; label: string; children: unknown[] }> {
  return nodes.map((node) => ({
    value: node.deptId,
    label: node.deptName,
    children: mapOptions(node.children),
  }))
}

function messageOf(error: unknown, fallback: string) {
  return error instanceof ApiError ? error.message : fallback
}

onMounted(loadTree)
</script>

<template>
  <section
    class="department-management"
    aria-label="部门管理"
  >
    <header class="department-management__toolbar">
      <div>
        <h2>部门树</h2>
        <p>共 {{ tree.length }} 个根部门</p>
      </div>
      <ElButton
        v-permission="'system:dept:create'"
        type="primary"
        @click="openRootCreate"
      >
        新增根部门
      </ElButton>
    </header>

    <StatePanel
      v-if="loading"
      status="loading"
      title="正在加载部门树"
    />
    <StatePanel
      v-else-if="loadError"
      status="error"
      title="部门树加载失败"
      :description="loadError"
      action-label="重新加载"
      @action="loadTree"
    />
    <StatePanel
      v-else-if="tree.length === 0"
      status="empty"
      title="暂无部门"
    />
    <div
      v-else
      class="department-management__tree-wrap"
    >
      <ul class="department-management__tree">
        <DepartmentTreeNode
          v-for="node in tree"
          :key="node.deptId"
          :node="node"
          @add="openChildCreate"
          @edit="openEdit"
          @status="toggleStatus"
          @delete="removeNode"
        />
      </ul>
    </div>

    <ElDrawer
      v-model="editorOpen"
      :title="editorTitle"
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
          label="父部门"
          prop="parentId"
        >
          <ElTreeSelect
            v-model="form.parentId"
            :data="departmentOptions"
            check-strictly
            default-expand-all
          />
        </ElFormItem>
        <ElFormItem
          label="部门编码"
          prop="deptCode"
        >
          <ElInput
            v-model="form.deptCode"
            maxlength="64"
          />
        </ElFormItem>
        <ElFormItem
          label="部门名称"
          prop="deptName"
        >
          <ElInput
            v-model="form.deptName"
            maxlength="128"
          />
        </ElFormItem>
        <ElFormItem label="负责人用户 ID">
          <ElInput v-model="form.leaderUserId" />
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
  </section>
</template>

<style scoped>
.department-management {
  display: grid;
  height: calc(100vh - var(--header-height) - var(--content-gutter) * 2);
  min-height: 600px;
  grid-template-rows: auto minmax(0, 1fr);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.department-management__toolbar {
  display: flex;
  min-height: 72px;
  padding: var(--space-4);
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  border-bottom: 1px solid var(--color-border);
}

.department-management__toolbar h2,
.department-management__toolbar p {
  margin: 0;
}

.department-management__toolbar h2 {
  color: var(--color-text);
  font-size: var(--font-size-lg);
}

.department-management__toolbar p {
  margin-top: 2px;
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
}

.department-management__tree-wrap {
  min-width: 0;
  min-height: 0;
  overflow: auto;
}

.department-management__tree {
  min-width: 720px;
  margin: 0;
  padding: 0;
}

@media (max-width: 768px) {
  :global(.department-management + .el-overlay .el-drawer) {
    max-width: 92vw;
  }
}
</style>
