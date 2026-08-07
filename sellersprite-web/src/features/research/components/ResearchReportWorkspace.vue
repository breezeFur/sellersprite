<script setup lang="ts">
import {
  ArrowLeft,
  DataAnalysis,
  Document,
  Finished,
  FolderOpened,
  Operation,
} from '@element-plus/icons-vue'
import { computed, nextTick, onMounted, ref, watch } from 'vue'

type ResearchWorkspaceSection =
  | 'report'
  | 'evidence'
  | 'selection'
  | 'process'
  | 'artifacts'
type WorkspaceStatusType = 'primary' | 'success' | 'warning' | 'info' | 'danger'

interface WorkspaceTab {
  value: ResearchWorkspaceSection
  label: string
  icon: typeof Document
}

const props = withDefaults(defineProps<{
  activeSection: ResearchWorkspaceSection
  title: string
  jobId: string
  statusLabel: string
  statusType: WorkspaceStatusType
  statusDetail: string
  selectionAvailable?: boolean
}>(), {
  selectionAvailable: false,
})

const emit = defineEmits<{
  select: [section: ResearchWorkspaceSection]
  back: []
}>()
const tabsElement = ref<HTMLElement | null>(null)

const tabs = computed<WorkspaceTab[]>(() => [
  { value: 'report', label: '报告结论', icon: Document },
  { value: 'evidence', label: '证据数据', icon: DataAnalysis },
  ...(
    props.selectionAvailable || props.activeSection === 'selection'
      ? [{ value: 'selection' as const, label: '商品选择', icon: Finished }]
      : []
  ),
  { value: 'process', label: '执行过程', icon: Operation },
  { value: 'artifacts', label: '已发布文件', icon: FolderOpened },
])

function tabId(section: ResearchWorkspaceSection) {
  return `research-workspace-tab-${section}`
}

function selectSection(section: ResearchWorkspaceSection) {
  emit('select', section)
}

function scrollActiveTabIntoView() {
  void nextTick(() => {
    const activeTab = tabsElement.value?.querySelector<HTMLElement>(`#${tabId(props.activeSection)}`)
    activeTab?.scrollIntoView?.({ block: 'nearest', inline: 'nearest' })
  })
}

onMounted(scrollActiveTabIntoView)
watch(
  [() => props.activeSection, () => props.selectionAvailable],
  scrollActiveTabIntoView,
)

function handleTabKeydown(event: KeyboardEvent, index: number) {
  let nextIndex: number
  if (event.key === 'ArrowRight') nextIndex = (index + 1) % tabs.value.length
  else if (event.key === 'ArrowLeft') {
    nextIndex = (index - 1 + tabs.value.length) % tabs.value.length
  }
  else if (event.key === 'Home') nextIndex = 0
  else if (event.key === 'End') nextIndex = tabs.value.length - 1
  else return

  event.preventDefault()
  const nextTab = tabs.value[nextIndex]
  selectSection(nextTab.value)
  const tabButtons = (event.currentTarget as HTMLElement).parentElement
    ?.querySelectorAll<HTMLButtonElement>('[role="tab"]')
  tabButtons?.item(nextIndex).focus()
}
</script>

<template>
  <section
    class="research-report-workspace"
    aria-labelledby="research-workspace-title"
    data-testid="research-report-workspace"
  >
    <header class="research-report-workspace__header">
      <ElTooltip content="返回任务详情">
        <ElButton
          class="research-report-workspace__back"
          :icon="ArrowLeft"
          circle
          aria-label="返回任务详情"
          data-testid="research-workspace-back"
          @click="emit('back')"
        />
      </ElTooltip>

      <div class="research-report-workspace__heading">
        <h1 id="research-workspace-title">
          {{ title }}
        </h1>
        <div class="research-report-workspace__meta">
          <ElTag
            :type="statusType"
            effect="light"
            size="small"
          >
            {{ statusLabel }}
          </ElTag>
          <span v-if="statusDetail">{{ statusDetail }}</span>
          <code :title="jobId">{{ jobId }}</code>
        </div>
      </div>

      <div class="research-report-workspace__actions">
        <slot name="actions" />
      </div>
    </header>

    <nav
      ref="tabsElement"
      class="research-report-workspace__tabs"
      aria-label="报告工作台视图"
      data-testid="research-workspace-tabs"
    >
      <div
        class="research-report-workspace__tab-list"
        role="tablist"
        aria-orientation="horizontal"
      >
        <button
          v-for="(tab, index) in tabs"
          :id="tabId(tab.value)"
          :key="tab.value"
          class="research-report-workspace__tab"
          :class="{ 'is-active': activeSection === tab.value }"
          type="button"
          role="tab"
          :aria-selected="activeSection === tab.value"
          aria-controls="research-workspace-panel"
          :tabindex="activeSection === tab.value ? 0 : -1"
          :data-testid="`research-workspace-tab-${tab.value}`"
          @click="selectSection(tab.value)"
          @keydown="handleTabKeydown($event, index)"
        >
          <ElIcon aria-hidden="true">
            <component :is="tab.icon" />
          </ElIcon>
          <span>{{ tab.label }}</span>
        </button>
      </div>
    </nav>

    <main
      id="research-workspace-panel"
      class="research-report-workspace__body"
      role="tabpanel"
      :aria-labelledby="tabId(activeSection)"
      data-testid="research-workspace-content"
    >
      <slot :active-section="activeSection" />
    </main>
  </section>
</template>

<style scoped>
.research-report-workspace {
  display: flex;
  width: 100%;
  max-width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;
  flex-direction: column;
  overflow: hidden;
  background: var(--color-surface);
  border: 0;
  border-radius: 0;
}

.research-report-workspace__header {
  display: grid;
  min-width: 0;
  min-height: 76px;
  grid-template-columns: 32px minmax(0, 1fr) auto;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.research-report-workspace__back {
  margin: 0;
}

.research-report-workspace__heading {
  min-width: 0;
}

.research-report-workspace__heading h1 {
  margin: 0;
  overflow: hidden;
  color: var(--color-text);
  font-size: 18px;
  line-height: var(--line-height-tight);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.research-report-workspace__meta {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: var(--space-2);
  margin-top: 6px;
  color: var(--color-text-secondary);
  font-size: 11px;
}

.research-report-workspace__meta > span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.research-report-workspace__meta code {
  min-width: 0;
  margin-left: auto;
  overflow: hidden;
  color: var(--color-text-muted);
  font: 10px/1.5 var(--font-mono);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.research-report-workspace__actions {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.research-report-workspace__actions:empty {
  display: none;
}

.research-report-workspace__tabs {
  min-width: 0;
  flex: 0 0 auto;
  overflow-x: auto;
  overflow-y: hidden;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  overscroll-behavior-x: contain;
  scrollbar-width: thin;
  touch-action: pan-x;
}

.research-report-workspace__tab-list {
  display: flex;
  width: max-content;
  min-width: 100%;
  padding: 0 var(--space-2);
}

.research-report-workspace__tab {
  position: relative;
  display: inline-flex;
  min-width: 112px;
  height: 46px;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 0 var(--space-4);
  color: var(--color-text-secondary);
  background: transparent;
  border: 0;
  cursor: pointer;
  font: 500 var(--font-size-sm)/1 var(--font-sans);
  white-space: nowrap;
}

.research-report-workspace__tab::after {
  position: absolute;
  right: var(--space-3);
  bottom: 0;
  left: var(--space-3);
  height: 2px;
  background: transparent;
  content: '';
}

.research-report-workspace__tab:hover {
  color: var(--color-brand-700);
  background: var(--color-brand-50);
}

.research-report-workspace__tab:focus-visible {
  z-index: 1;
  outline: 0;
  box-shadow: inset var(--focus-ring);
}

.research-report-workspace__tab.is-active {
  color: var(--color-brand-700);
  font-weight: 650;
}

.research-report-workspace__tab.is-active::after {
  background: var(--color-brand-600);
}

.research-report-workspace__body {
  display: flex;
  width: 100%;
  max-width: 100%;
  min-width: 0;
  min-height: 0;
  flex: 1;
  flex-direction: column;
  overflow: auto;
  background: var(--color-surface-muted);
  overscroll-behavior: contain;
}

@media (max-width: 720px) {
  .research-report-workspace {
    min-height: 0;
  }

  .research-report-workspace__header {
    min-height: 0;
    grid-template-columns: 32px minmax(0, 1fr);
    align-items: start;
    padding: var(--space-3);
  }

  .research-report-workspace__heading h1 {
    display: -webkit-box;
    overflow: hidden;
    white-space: normal;
    overflow-wrap: anywhere;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }

  .research-report-workspace__meta {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .research-report-workspace__meta > span {
    max-width: 100%;
    white-space: normal;
    overflow-wrap: anywhere;
  }

  .research-report-workspace__meta code {
    width: 100%;
    margin-left: 0;
  }

  .research-report-workspace__actions {
    grid-column: 1 / -1;
  }

  .research-report-workspace__tab-list {
    min-width: max-content;
    padding-inline: var(--space-1);
  }

  .research-report-workspace__tab {
    min-width: 104px;
    padding-inline: var(--space-3);
  }
}
</style>
