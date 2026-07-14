<script setup lang="ts">
import { ArrowDown, ArrowRight } from '@element-plus/icons-vue'
import { ref } from 'vue'

import type { DepartmentNode } from '../model/system'

defineOptions({ name: 'DepartmentTreeNode' })

defineProps<{
  node: DepartmentNode
}>()

const emit = defineEmits<{
  add: [node: DepartmentNode]
  edit: [node: DepartmentNode]
  status: [node: DepartmentNode]
  delete: [node: DepartmentNode]
}>()

const expanded = ref(true)
</script>

<template>
  <li class="department-node">
    <div class="department-node__row">
      <button
        v-if="node.children.length"
        class="department-node__expand"
        type="button"
        :aria-label="expanded ? `收起${node.deptName}` : `展开${node.deptName}`"
        @click="expanded = !expanded"
      >
        <ArrowDown v-if="expanded" />
        <ArrowRight v-else />
      </button>
      <span
        v-else
        class="department-node__spacer"
      />
      <div class="department-node__identity">
        <strong>{{ node.deptName }}</strong>
        <span>{{ node.deptCode }}</span>
      </div>
      <span class="department-node__sort">排序 {{ node.sortOrder }}</span>
      <span
        class="department-node__status"
        :class="node.status === 1 ? 'is-enabled' : 'is-disabled'"
      >
        {{ node.status === 1 ? '启用' : '停用' }}
      </span>
      <div class="department-node__actions">
        <button
          v-permission="'system:dept:create'"
          type="button"
          @click="emit('add', node)"
        >
          新增下级
        </button>
        <button
          v-permission="'system:dept:update'"
          type="button"
          @click="emit('edit', node)"
        >
          编辑
        </button>
        <button
          v-permission="'system:dept:status'"
          type="button"
          @click="emit('status', node)"
        >
          {{ node.status === 1 ? '停用' : '启用' }}
        </button>
        <button
          v-permission="'system:dept:delete'"
          class="is-danger"
          type="button"
          @click="emit('delete', node)"
        >
          删除
        </button>
      </div>
    </div>
    <ul
      v-if="expanded && node.children.length"
      class="department-node__children"
    >
      <DepartmentTreeNode
        v-for="child in node.children"
        :key="child.deptId"
        :node="child"
        @add="emit('add', $event)"
        @edit="emit('edit', $event)"
        @status="emit('status', $event)"
        @delete="emit('delete', $event)"
      />
    </ul>
  </li>
</template>

<style scoped>
.department-node {
  min-width: 720px;
  list-style: none;
}

.department-node__row {
  display: grid;
  min-height: 48px;
  padding: 0 var(--space-3);
  grid-template-columns: 28px minmax(180px, 1fr) 90px 70px auto;
  align-items: center;
  gap: var(--space-2);
  border-bottom: 1px solid var(--color-border);
}

.department-node__row:hover {
  background: var(--color-brand-50);
}

.department-node__expand {
  display: grid;
  width: 26px;
  height: 26px;
  padding: 6px;
  place-items: center;
  color: var(--color-text-secondary);
  background: transparent;
  border: 0;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.department-node__expand svg {
  width: 14px;
  height: 14px;
}

.department-node__spacer {
  width: 26px;
}

.department-node__identity {
  min-width: 0;
}

.department-node__identity strong,
.department-node__identity span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.department-node__identity strong {
  color: var(--color-text);
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.department-node__identity span,
.department-node__sort {
  color: var(--color-text-muted);
  font-size: 10px;
}

.department-node__status {
  font-size: var(--font-size-xs);
}

.department-node__status.is-enabled {
  color: var(--color-success);
}

.department-node__status.is-disabled {
  color: var(--color-text-muted);
}

.department-node__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
}

.department-node__actions button {
  padding: 3px 5px;
  color: var(--color-brand-700);
  background: transparent;
  border: 0;
  font-size: var(--font-size-xs);
  cursor: pointer;
}

.department-node__actions button.is-danger {
  color: var(--color-danger);
}

.department-node__children {
  margin: 0 0 0 var(--space-5);
  padding: 0;
  border-left: 1px solid var(--color-border-strong);
}
</style>
