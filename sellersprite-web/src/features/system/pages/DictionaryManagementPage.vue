<script setup lang="ts">
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'

import { ApiError } from '@/shared/api/ApiError'
import StatePanel from '@/shared/components/StatePanel.vue'

import {
  createDictItem,
  createDictType,
  deleteDictItem,
  deleteDictType,
  pageDictItems,
  pageDictTypes,
  updateDictItem,
  updateDictItemStatus,
  updateDictType,
  updateDictTypeStatus,
} from '../api/dictionaryApi'
import type {
  DictionaryItem,
  DictionaryItemPayload,
  DictionaryType,
  DictionaryTypePayload,
} from '../model/system'

const types = ref<DictionaryType[]>([])
const items = ref<DictionaryItem[]>([])
const selectedType = ref<DictionaryType | null>(null)
const typeLoading = ref(true)
const itemLoading = ref(false)
const loadError = ref('')
const typeCurrent = ref(1)
const typeTotal = ref(0)
const itemCurrent = ref(1)
const itemTotal = ref(0)
const pageSize = 20
const filters = reactive({ keyword: '', status: null as number | null })
const itemFilters = reactive({ keyword: '', status: null as number | null })

const typeEditorOpen = ref(false)
const typeEditorMode = ref<'create' | 'edit'>('create')
const editingTypeId = ref<string | null>(null)
const typeSubmitting = ref(false)
const typeFormRef = ref<FormInstance>()
const typeForm = reactive<DictionaryTypePayload>(emptyTypeForm())
const itemEditorOpen = ref(false)
const itemEditorMode = ref<'create' | 'edit'>('create')
const editingItemId = ref<string | null>(null)
const itemSubmitting = ref(false)
const itemFormRef = ref<FormInstance>()
const itemForm = reactive<DictionaryItemPayload>(emptyItemForm())

const typeTitle = computed(() => typeEditorMode.value === 'create' ? '新增字典类型' : '编辑字典类型')
const itemTitle = computed(() => itemEditorMode.value === 'create' ? '新增字典项' : '编辑字典项')
const typeRules: FormRules<DictionaryTypePayload> = {
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
}
const itemRules: FormRules<DictionaryItemPayload> = {
  dictLabel: [{ required: true, message: '请输入稳定标签', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入展示名称', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入远端参数值', trigger: 'blur' }],
}

function emptyTypeForm(): DictionaryTypePayload {
  return { dictType: '', dictName: '', systemBuiltin: 0, sortOrder: 0, remark: '' }
}

function emptyItemForm(): DictionaryItemPayload {
  return { dictType: '', dictLabel: '', dictName: '', dictValue: '', color: '', defaultFlag: 0, sortOrder: 0, remark: '' }
}

async function loadTypes(preserveSelection = true) {
  typeLoading.value = true
  loadError.value = ''
  try {
    const page = await pageDictTypes({
      current: typeCurrent.value,
      size: pageSize,
      dictType: filters.keyword.trim() || undefined,
      dictName: filters.keyword.trim() || undefined,
      status: filters.status ?? undefined,
    })
    types.value = page.records
    typeTotal.value = page.total
    typeCurrent.value = page.current
    const previousId = preserveSelection ? selectedType.value?.dictType : null
    selectedType.value = types.value.find((type) => type.dictType === previousId) ?? types.value[0] ?? null
    if (selectedType.value) await loadItems()
    else items.value = []
  } catch (error) {
    loadError.value = messageOf(error, '字典类型加载失败')
  } finally {
    typeLoading.value = false
  }
}

async function loadItems() {
  if (!selectedType.value) return
  itemLoading.value = true
  try {
    const page = await pageDictItems(selectedType.value.dictType, {
      current: itemCurrent.value,
      size: pageSize,
      dictLabel: itemFilters.keyword.trim() || undefined,
      dictName: itemFilters.keyword.trim() || undefined,
      dictValue: itemFilters.keyword.trim() || undefined,
      status: itemFilters.status ?? undefined,
    })
    items.value = page.records
    itemTotal.value = page.total
    itemCurrent.value = page.current
  } catch (error) {
    ElMessage.error(messageOf(error, '字典项加载失败'))
  } finally {
    itemLoading.value = false
  }
}

async function selectType(type: DictionaryType) {
  selectedType.value = type
  itemCurrent.value = 1
  itemFilters.keyword = ''
  itemFilters.status = null
  await loadItems()
}

function openTypeCreate() {
  typeEditorMode.value = 'create'
  editingTypeId.value = null
  Object.assign(typeForm, emptyTypeForm())
  typeEditorOpen.value = true
}

function openTypeEdit(type: DictionaryType) {
  typeEditorMode.value = 'edit'
  editingTypeId.value = type.dictType
  Object.assign(typeForm, { dictType: type.dictType, dictName: type.dictName, systemBuiltin: type.systemBuiltin, sortOrder: type.sortOrder, remark: '' })
  typeEditorOpen.value = true
}

async function submitType() {
  if (!(await typeFormRef.value?.validate()) || typeSubmitting.value) return
  typeSubmitting.value = true
  try {
    if (typeEditorMode.value === 'create') await createDictType({ ...typeForm })
    else if (editingTypeId.value) {
      const payload = {
        dictName: typeForm.dictName,
        sortOrder: typeForm.sortOrder,
        remark: typeForm.remark,
      }
      await updateDictType(editingTypeId.value, payload)
    }
    ElMessage.success(typeEditorMode.value === 'create' ? '字典类型已创建' : '字典类型已更新')
    typeEditorOpen.value = false
    await loadTypes()
  } catch (error) {
    ElMessage.error(messageOf(error, '字典类型保存失败'))
  } finally {
    typeSubmitting.value = false
  }
}

async function toggleTypeStatus(type: DictionaryType) {
  try {
    await updateDictTypeStatus(type.dictType, type.status === 1 ? 0 : 1)
    ElMessage.success(type.status === 1 ? '字典类型已停用' : '字典类型已启用')
    await loadTypes()
  } catch (error) { ElMessage.error(messageOf(error, '状态更新失败')) }
}

async function removeType(type: DictionaryType) {
  try {
    await ElMessageBox.confirm(`确认删除字典类型“${type.dictName}”？`, '删除字典类型', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    await deleteDictType(type.dictType)
    ElMessage.success('字典类型已删除')
    await loadTypes(false)
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error, '字典类型删除失败')) }
}

function openItemCreate() {
  if (!selectedType.value) return
  itemEditorMode.value = 'create'
  editingItemId.value = null
  Object.assign(itemForm, emptyItemForm(), { dictType: selectedType.value.dictType })
  itemEditorOpen.value = true
}

function openItemEdit(item: DictionaryItem) {
  itemEditorMode.value = 'edit'
  editingItemId.value = item.dictDataId
  Object.assign(itemForm, { dictType: item.dictType, dictLabel: item.dictLabel, dictName: item.dictName, dictValue: item.dictValue ?? '', color: item.color ?? '', defaultFlag: item.defaultFlag, sortOrder: item.sortOrder, remark: '' })
  itemEditorOpen.value = true
}

async function submitItem() {
  if (!(await itemFormRef.value?.validate()) || itemSubmitting.value) return
  itemSubmitting.value = true
  try {
    if (itemEditorMode.value === 'create') await createDictItem({ ...itemForm })
    else if (editingItemId.value) {
      const payload = {
        dictLabel: itemForm.dictLabel,
        dictName: itemForm.dictName,
        dictValue: itemForm.dictValue,
        color: itemForm.color,
        defaultFlag: itemForm.defaultFlag,
        sortOrder: itemForm.sortOrder,
        remark: itemForm.remark,
      }
      await updateDictItem(editingItemId.value, payload)
    }
    ElMessage.success(itemEditorMode.value === 'create' ? '字典项已创建' : '字典项已更新')
    itemEditorOpen.value = false
    await loadItems()
  } catch (error) { ElMessage.error(messageOf(error, '字典项保存失败')) }
  finally { itemSubmitting.value = false }
}

async function toggleItemStatus(item: DictionaryItem) {
  try {
    await updateDictItemStatus(item.dictDataId, item.status === 1 ? 0 : 1)
    ElMessage.success(item.status === 1 ? '字典项已停用' : '字典项已启用')
    await loadItems()
  } catch (error) { ElMessage.error(messageOf(error, '状态更新失败')) }
}

async function removeItem(item: DictionaryItem) {
  try {
    await ElMessageBox.confirm(`确认删除字典项“${item.dictName}”？`, '删除字典项', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    await deleteDictItem(item.dictDataId)
    ElMessage.success('字典项已删除')
    await loadItems()
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error, '字典项删除失败')) }
}

function messageOf(error: unknown, fallback: string) { return error instanceof ApiError ? error.message : fallback }
onMounted(() => void loadTypes(false))
</script>

<template>
  <section
    class="dictionary-page"
    aria-label="字典管理"
  >
    <StatePanel
      v-if="typeLoading && types.length === 0"
      status="loading"
      title="正在加载字典"
    />
    <StatePanel
      v-else-if="loadError && types.length === 0"
      status="error"
      title="字典加载失败"
      :description="loadError"
      action-label="重新加载"
      @action="loadTypes(false)"
    />
    <div
      v-else
      class="dictionary-layout"
    >
      <aside class="type-panel">
        <header>
          <div><h2>字典类型</h2><p>共 {{ typeTotal }} 项</p></div><ElButton
            type="primary"
            @click="openTypeCreate"
          >
            新增类型
          </ElButton>
        </header>
        <div class="toolbar">
          <input
            v-model="filters.keyword"
            aria-label="字典关键字"
            placeholder="编码或名称"
          ><select
            v-model="filters.status"
            aria-label="字典类型状态"
          >
            <option :value="null">
              全部状态
            </option><option :value="1">
              启用
            </option><option :value="0">
              停用
            </option>
          </select><button
            type="button"
            @click="typeCurrent = 1; loadTypes(false)"
          >
            查询
          </button>
        </div>
        <div
          v-if="types.length"
          class="type-list"
        >
          <article
            v-for="type in types"
            :key="type.dictType"
            :class="['type-card', { 'is-active': selectedType?.dictType === type.dictType }]"
            @click="selectType(type)"
          >
            <div><strong>{{ type.dictName }}</strong><code>{{ type.dictType }}</code></div><span :class="type.status === 1 ? 'enabled' : 'disabled'">{{ type.status === 1 ? '启用' : '停用' }}</span>
            <footer>
              <span>排序 {{ type.sortOrder }}</span><div>
                <button
                  type="button"
                  @click.stop="openTypeEdit(type)"
                >
                  编辑
                </button><button
                  type="button"
                  @click.stop="toggleTypeStatus(type)"
                >
                  {{ type.status === 1 ? '停用' : '启用' }}
                </button><button
                  type="button"
                  class="danger"
                  :disabled="type.systemBuiltin === 1"
                  @click.stop="removeType(type)"
                >
                  删除
                </button>
              </div>
            </footer>
          </article>
        </div>
        <StatePanel
          v-else
          status="empty"
          title="暂无字典类型"
          description="创建类型后即可维护字典项"
        />
        <footer class="pager">
          <button
            :disabled="typeCurrent <= 1"
            @click="typeCurrent -= 1; loadTypes()"
          >
            上一页
          </button><span>第 {{ typeCurrent }} 页</span><button
            :disabled="typeCurrent * pageSize >= typeTotal"
            @click="typeCurrent += 1; loadTypes()"
          >
            下一页
          </button>
        </footer>
      </aside>

      <main class="item-panel">
        <header>
          <div><h2>{{ selectedType?.dictName ?? '字典项' }}</h2><p>{{ selectedType?.dictType ?? '请先选择字典类型' }}</p></div><ElButton
            type="primary"
            :disabled="!selectedType"
            @click="openItemCreate"
          >
            新增字典项
          </ElButton>
        </header>
        <div class="toolbar">
          <input
            v-model="itemFilters.keyword"
            aria-label="字典项关键字"
            placeholder="标签或值"
          ><select
            v-model="itemFilters.status"
            aria-label="字典项状态"
          >
            <option :value="null">
              全部状态
            </option><option :value="1">
              启用
            </option><option :value="0">
              停用
            </option>
          </select><button
            type="button"
            :disabled="!selectedType"
            @click="itemCurrent = 1; loadItems()"
          >
            查询
          </button>
        </div>
        <div
          class="table-wrap"
          :aria-busy="itemLoading"
        >
          <table>
            <thead><tr><th>名称</th><th>稳定标签</th><th>远端值</th><th>颜色</th><th>默认</th><th>排序</th><th>状态</th><th>操作</th></tr></thead><tbody>
              <tr
                v-for="item in items"
                :key="item.dictDataId"
              >
                <td>{{ item.dictName }}</td><td><code>{{ item.dictLabel }}</code></td><td><code>{{ item.dictValue }}</code></td><td>
                  <span
                    class="color-dot"
                    :style="{ background: item.color || '#94a3b8' }"
                  />{{ item.color || '--' }}
                </td><td>{{ item.defaultFlag === 1 ? '是' : '否' }}</td><td>{{ item.sortOrder }}</td><td :class="item.status === 1 ? 'enabled' : 'disabled'">
                  {{ item.status === 1 ? '启用' : '停用' }}
                </td><td class="actions">
                  <button
                    type="button"
                    @click="openItemEdit(item)"
                  >
                    编辑
                  </button><button
                    type="button"
                    @click="toggleItemStatus(item)"
                  >
                    {{ item.status === 1 ? '停用' : '启用' }}
                  </button><button
                    type="button"
                    class="danger"
                    :disabled="item.systemBuiltin === 1"
                    @click="removeItem(item)"
                  >
                    删除
                  </button>
                </td>
              </tr>
            </tbody>
          </table><StatePanel
            v-if="!itemLoading && selectedType && items.length === 0"
            status="empty"
            title="暂无字典项"
          />
        </div>
        <footer class="pager">
          <span>共 {{ itemTotal }} 条</span><button
            :disabled="itemCurrent <= 1"
            @click="itemCurrent -= 1; loadItems()"
          >
            上一页
          </button><button
            :disabled="itemCurrent * pageSize >= itemTotal"
            @click="itemCurrent += 1; loadItems()"
          >
            下一页
          </button>
        </footer>
      </main>
    </div>

    <ElDrawer
      v-model="typeEditorOpen"
      :title="typeTitle"
      size="min(460px, 100%)"
    >
      <ElForm
        ref="typeFormRef"
        :model="typeForm"
        :rules="typeRules"
        label-position="top"
      >
        <ElFormItem
          label="字典编码"
          prop="dictType"
        >
          <ElInput
            v-model="typeForm.dictType"
            :disabled="Boolean(editingTypeId)"
          />
        </ElFormItem><ElFormItem
          label="字典名称"
          prop="dictName"
        >
          <ElInput v-model="typeForm.dictName" />
        </ElFormItem><ElFormItem label="排序值">
          <ElInputNumber
            v-model="typeForm.sortOrder"
            :min="0"
          />
        </ElFormItem><ElFormItem
          v-if="typeEditorMode === 'create'"
          label="系统内置"
        >
          <ElSwitch
            v-model="typeForm.systemBuiltin"
            :active-value="1"
            :inactive-value="0"
          />
        </ElFormItem><ElFormItem label="备注">
          <ElInput
            v-model="typeForm.remark"
            type="textarea"
          />
        </ElFormItem>
      </ElForm><template #footer>
        <ElButton @click="typeEditorOpen = false">
          取消
        </ElButton><ElButton
          type="primary"
          :loading="typeSubmitting"
          @click="submitType"
        >
          保存
        </ElButton>
      </template>
    </ElDrawer>
    <ElDrawer
      v-model="itemEditorOpen"
      :title="itemTitle"
      size="min(460px, 100%)"
    >
      <ElForm
        ref="itemFormRef"
        :model="itemForm"
        :rules="itemRules"
        label-position="top"
      >
        <ElFormItem
          label="字典标签"
          prop="dictLabel"
        >
          <ElInput v-model="itemForm.dictLabel" />
        </ElFormItem><ElFormItem
          label="字典值"
          prop="dictName"
        >
          <ElInput v-model="itemForm.dictName" />
        </ElFormItem>
        <ElFormItem
          label="远端参数值"
          prop="dictValue"
        >
          <ElInput v-model="itemForm.dictValue" />
        </ElFormItem><ElFormItem label="展示颜色">
          <ElColorPicker
            v-model="itemForm.color"
            show-alpha
          />
        </ElFormItem><ElFormItem label="默认项">
          <ElSwitch
            v-model="itemForm.defaultFlag"
            :active-value="1"
            :inactive-value="0"
          />
        </ElFormItem><ElFormItem label="排序值">
          <ElInputNumber
            v-model="itemForm.sortOrder"
            :min="0"
          />
        </ElFormItem><ElFormItem label="备注">
          <ElInput
            v-model="itemForm.remark"
            type="textarea"
          />
        </ElFormItem>
      </ElForm><template #footer>
        <ElButton @click="itemEditorOpen = false">
          取消
        </ElButton><ElButton
          type="primary"
          :loading="itemSubmitting"
          @click="submitItem"
        >
          保存
        </ElButton>
      </template>
    </ElDrawer>
  </section>
</template>

<style scoped>
.dictionary-page{min-height:calc(100vh - 104px)}.dictionary-layout{display:grid;grid-template-columns:340px minmax(0,1fr);min-height:calc(100vh - 104px);overflow:hidden;background:#fff;border:1px solid var(--color-border);border-radius:10px}.type-panel{display:flex;flex-direction:column;border-right:1px solid var(--color-border)}header{display:flex;align-items:center;justify-content:space-between;gap:16px;padding:16px;border-bottom:1px solid var(--color-border)}h2{margin:0;font-size:16px}p{margin:4px 0 0;color:var(--color-text-secondary);font-size:12px}.toolbar{display:flex;gap:8px;padding:12px 16px;border-bottom:1px solid var(--color-border)}.toolbar input,.toolbar select{min-width:0;height:34px;padding:0 10px;border:1px solid var(--color-border);border-radius:6px;background:#fff}.toolbar input{flex:1}.toolbar button,.pager button,.type-card button,.actions button{padding:0;color:var(--color-primary);background:transparent;border:0;cursor:pointer}.toolbar>button{padding:0 16px;color:#fff;background:var(--color-primary);border-radius:6px}.type-list{display:grid;gap:8px;padding:12px;overflow:auto}.type-card{padding:12px;border:1px solid var(--color-border);border-radius:8px;cursor:pointer}.type-card.is-active{background:#eff6ff;border-color:#93c5fd}.type-card>div:first-child{display:flex;justify-content:space-between;gap:8px}.type-card code{color:var(--color-text-secondary)}.type-card footer{display:flex;justify-content:space-between;margin-top:10px;color:var(--color-text-secondary);font-size:12px}.type-card footer div,.actions{display:flex;gap:10px}.pager{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-top:auto;padding:12px 16px;border-top:1px solid var(--color-border);color:var(--color-text-secondary);font-size:12px}.pager button:disabled,.actions button:disabled,.type-card button:disabled{cursor:not-allowed;opacity:.4}.item-panel{display:flex;min-width:0;flex-direction:column}.table-wrap{min-width:0;overflow:auto}table{width:100%;min-width:760px;border-collapse:collapse}th,td{padding:12px 14px;border-bottom:1px solid var(--color-border);text-align:left;font-size:13px}th{background:#f8fafc}.enabled{color:var(--color-success)}.disabled{color:var(--color-text-secondary)}.danger{color:var(--color-danger)!important}.color-dot{display:inline-block;width:10px;height:10px;margin-right:6px;border-radius:50%}@media(max-width:900px){.dictionary-layout{grid-template-columns:1fr;overflow:visible}.type-panel{border-right:0;border-bottom:1px solid var(--color-border)}.type-list{grid-template-columns:repeat(2,minmax(0,1fr));max-height:310px}.item-panel{min-height:480px}}@media(max-width:640px){.type-list{grid-template-columns:1fr}.toolbar{flex-wrap:wrap}.toolbar input{flex-basis:100%}}
.toolbar > button {
  display: inline-flex;
  min-width: 64px;
  height: 34px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
}
.dictionary-page {
  --color-primary: var(--color-brand-600);
  --color-text-primary: var(--color-text);
}
</style>
