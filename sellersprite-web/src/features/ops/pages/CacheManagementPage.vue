<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref, watch } from 'vue'

import { ApiError } from '@/shared/api/ApiError'
import StatePanel from '@/shared/components/StatePanel.vue'

import {
  checkCacheKey,
  clearCachePrefix,
  deleteCacheKey,
  getCacheKeys,
  getCacheValue,
  type CacheValueResult,
} from '../api/cacheApi'

const keys = ref<string[]>([])
const loading = ref(true)
const loadError = ref('')
const keyword = ref('')
const current = ref(1)
const pageSize = 20
const existence = ref<Record<string, boolean>>({})
const checkingKey = ref('')
const detailOpen = ref(false)
const detailLoading = ref(false)
const detail = ref<CacheValueResult | null>(null)

const filteredKeys = computed(() => {
  const normalized = keyword.value.trim().toLowerCase()
  return normalized ? keys.value.filter((key) => key.toLowerCase().includes(normalized)) : keys.value
})
const pageCount = computed(() => Math.max(1, Math.ceil(filteredKeys.value.length / pageSize)))
const pageKeys = computed(() => filteredKeys.value.slice((current.value - 1) * pageSize, current.value * pageSize))
const formattedValue = computed(() => formatValue(detail.value?.value))

watch(keyword, () => { current.value = 1 })
watch(pageCount, (count) => { if (current.value > count) current.value = count })

async function loadKeys() {
  loading.value = true
  loadError.value = ''
  try {
    keys.value = [...await getCacheKeys()].sort((left, right) => left.localeCompare(right))
    existence.value = {}
  } catch (error) { loadError.value = messageOf(error, '缓存键加载失败') }
  finally { loading.value = false }
}

async function openDetail(key: string) {
  detailOpen.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await getCacheValue(key)
    existence.value[key] = detail.value.exists
  } catch (error) { ElMessage.error(messageOf(error, '缓存值查询失败')) }
  finally { detailLoading.value = false }
}

async function verifyKey(key: string) {
  checkingKey.value = key
  try {
    const exists = await checkCacheKey(key)
    existence.value[key] = exists
    ElMessage.success(exists ? '缓存键存在' : '缓存键已不存在')
  } catch (error) { ElMessage.error(messageOf(error, '存在性校验失败')) }
  finally { checkingKey.value = '' }
}

async function removeKey(key: string) {
  try {
    await ElMessageBox.confirm(`确认删除缓存键“${key}”？此操作不可撤销。`, '删除缓存键', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
    await deleteCacheKey(key)
    ElMessage.success('缓存键已删除')
    await loadKeys()
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error, '缓存键删除失败')) }
}

async function clearCurrentPrefix() {
  try {
    await ElMessageBox.prompt('此操作会清空当前应用缓存前缀下的全部键。请输入“清空”继续。', '清空当前缓存前缀', {
      type: 'warning',
      confirmButtonText: '确认清空',
      cancelButtonText: '取消',
      inputPlaceholder: '清空',
      inputValidator: (value) => value === '清空' || '请输入“清空”',
    })
    await clearCachePrefix()
    ElMessage.success('当前缓存前缀已清空')
    await loadKeys()
  } catch (error) { if (error !== 'cancel' && error !== 'close') ElMessage.error(messageOf(error, '缓存清空失败')) }
}

function formatValue(value: unknown) {
  if (value === null || value === undefined) return ''
  if (typeof value === 'string') return value
  try { return JSON.stringify(value, null, 2) }
  catch { return String(value) }
}

function messageOf(error: unknown, fallback: string) { return error instanceof ApiError ? error.message : fallback }
onMounted(() => void loadKeys())
</script>

<template>
  <section
    class="cache-page"
    aria-label="缓存管理"
  >
    <header>
      <div>
        <h2>缓存键</h2>
        <p>只读查看 Starter 当前命名空间；共 {{ filteredKeys.length }} 个匹配键</p>
      </div>
      <div class="header-actions">
        <ElButton
          :loading="loading"
          @click="loadKeys"
        >
          刷新
        </ElButton>
        <ElButton
          type="danger"
          plain
          @click="clearCurrentPrefix"
        >
          清空当前前缀
        </ElButton>
      </div>
    </header>
    <div class="filter-bar">
      <label for="cache-keyword">客户端筛选</label>
      <input
        id="cache-keyword"
        v-model="keyword"
        aria-label="缓存键筛选"
        placeholder="输入完整或部分 key"
      >
      <span>第 {{ current }} / {{ pageCount }} 页</span>
    </div>

    <StatePanel
      v-if="loading && keys.length === 0"
      status="loading"
      title="正在读取缓存键"
    />
    <StatePanel
      v-else-if="loadError && keys.length === 0"
      status="error"
      title="缓存键加载失败"
      :description="loadError"
      action-label="重新加载"
      @action="loadKeys"
    />
    <div
      v-else
      class="table-wrap"
    >
      <table>
        <thead><tr><th>#</th><th>缓存键</th><th>存在性</th><th>操作</th></tr></thead>
        <tbody>
          <tr
            v-for="(key, index) in pageKeys"
            :key="key"
          >
            <td>{{ (current - 1) * pageSize + index + 1 }}</td>
            <td><code>{{ key }}</code></td>
            <td>
              <span
                v-if="existence[key] === true"
                class="exists"
              >存在</span><span
                v-else-if="existence[key] === false"
                class="missing"
              >不存在</span><span v-else>未校验</span>
            </td>
            <td class="actions">
              <button
                type="button"
                @click="openDetail(key)"
              >
                查看值
              </button><button
                type="button"
                :disabled="checkingKey === key"
                @click="verifyKey(key)"
              >
                {{ checkingKey === key ? '校验中' : '存在校验' }}
              </button><button
                type="button"
                class="danger"
                @click="removeKey(key)"
              >
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <StatePanel
        v-if="!loading && pageKeys.length === 0"
        status="empty"
        title="暂无匹配缓存键"
        description="调整筛选词或刷新列表"
      />
    </div>
    <footer>
      <span>列表由 Starter 一次返回，筛选与分页均在浏览器本地完成</span><div>
        <button
          :disabled="current <= 1"
          @click="current -= 1"
        >
          上一页
        </button><button
          :disabled="current >= pageCount"
          @click="current += 1"
        >
          下一页
        </button>
      </div>
    </footer>

    <ElDrawer
      v-model="detailOpen"
      :title="`缓存详情 · ${detail?.key ?? ''}`"
      size="min(680px, 100%)"
    >
      <StatePanel
        v-if="detailLoading"
        status="loading"
        title="正在读取缓存值"
      />
      <div
        v-else-if="detail"
        class="cache-detail"
      >
        <dl><div><dt>缓存键</dt><dd><code>{{ detail.key }}</code></dd></div><div><dt>存在</dt><dd>{{ detail.exists ? '是' : '否' }}</dd></div></dl><section><h3>原始值</h3><pre>{{ formattedValue || '--' }}</pre></section>
      </div>
      <StatePanel
        v-else
        status="error"
        title="缓存详情不可用"
      />
      <template #footer>
        <ElButton @click="detailOpen = false">
          关闭
        </ElButton>
      </template>
    </ElDrawer>
  </section>
</template>

<style scoped>
.cache-page{--color-primary:var(--color-brand-600);display:flex;min-height:calc(100vh - 104px);flex-direction:column;overflow:hidden;background:#fff;border:1px solid var(--color-border);border-radius:10px}.cache-page>header{display:flex;align-items:center;justify-content:space-between;gap:16px;padding:16px;border-bottom:1px solid var(--color-border)}h2,h3{margin:0;font-size:16px}p{margin:5px 0 0;color:var(--color-text-secondary);font-size:12px}.header-actions{display:flex;gap:8px}.filter-bar{display:flex;align-items:center;gap:10px;padding:12px 16px;border-bottom:1px solid var(--color-border);color:var(--color-text-secondary);font-size:12px}.filter-bar input{width:min(520px,70%);height:34px;padding:0 10px;border:1px solid var(--color-border);border-radius:6px}.filter-bar span{margin-left:auto}.table-wrap{overflow:auto}table{width:100%;min-width:760px;border-collapse:collapse}th,td{padding:12px 14px;border-bottom:1px solid var(--color-border);text-align:left;font-size:13px}th{background:#f8fafc}td:first-child{width:64px;color:var(--color-text-secondary)}td code{font-family:var(--font-mono);overflow-wrap:anywhere}.exists{color:var(--color-success)}.missing,.danger{color:var(--color-danger)!important}.actions{display:flex;min-width:210px;gap:14px}.actions button,.cache-page>footer button{padding:0;color:var(--color-primary);background:transparent;border:0;cursor:pointer}.actions button:disabled,.cache-page>footer button:disabled{cursor:not-allowed;opacity:.4}.cache-page>footer{display:flex;align-items:center;justify-content:space-between;margin-top:auto;padding:12px 16px;border-top:1px solid var(--color-border);color:var(--color-text-secondary);font-size:12px}.cache-page>footer div{display:flex;gap:18px}.cache-detail dl{display:grid;gap:10px;margin:0 0 20px}.cache-detail dl>div{display:grid;grid-template-columns:80px 1fr;gap:12px}.cache-detail dt{color:var(--color-text-secondary)}.cache-detail dd{min-width:0;margin:0}.cache-detail section{display:grid;gap:10px}.cache-detail pre{max-height:58vh;margin:0;padding:14px;overflow:auto;color:#dbeafe;background:#0f172a;border-radius:8px;font:12px/1.6 var(--font-mono);white-space:pre-wrap;overflow-wrap:anywhere}@media(max-width:640px){.cache-page>header{align-items:flex-start}.header-actions{flex-direction:column}.filter-bar{flex-wrap:wrap}.filter-bar input{width:100%}.filter-bar span{margin-left:0}.cache-page>footer{align-items:flex-start;gap:12px}}
</style>
