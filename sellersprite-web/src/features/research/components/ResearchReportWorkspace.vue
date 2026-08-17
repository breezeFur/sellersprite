<script setup lang="ts">
import {
  ArrowLeft,
  CaretBottom,
  DataAnalysis,
  Document,
  Finished,
  FolderOpened,
  Menu,
  Operation,
} from '@element-plus/icons-vue'
import { computed, nextTick, onMounted, ref, watch } from 'vue'

import { useLayoutStore } from '@/layouts/useLayoutStore'

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
  streamStatusLabel?: string
  streamStatusType?: Exclude<WorkspaceStatusType, 'primary'>
  streamStatusSequence?: number
  selectionAvailable?: boolean
}>(), {
  selectionAvailable: false,
  streamStatusLabel: '',
  streamStatusType: 'info',
  streamStatusSequence: 0,
})

const emit = defineEmits<{
  select: [section: ResearchWorkspaceSection]
  back: []
}>()
const tabsElement = ref<HTMLElement | null>(null)
const layoutStore = useLayoutStore()
const chromeToggleElement = ref<HTMLButtonElement | null>(null)
const chromeHover = ref(false)
const chromeFocus = ref(false)
const chromePinned = ref(false)
const ignoreNextChromeFocus = ref(false)

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
const activeTab = computed(() => tabs.value.find((tab) => tab.value === props.activeSection) || tabs.value[0])
const chromeOpen = computed(() => chromeHover.value || chromeFocus.value || chromePinned.value)

function tabId(section: ResearchWorkspaceSection) {
  return `research-workspace-tab-${section}`
}

function selectSection(section: ResearchWorkspaceSection) {
  chromePinned.value = false
  emit('select', section)
}

function handleChromeFocusIn() {
  if (ignoreNextChromeFocus.value) {
    ignoreNextChromeFocus.value = false
    return
  }
  chromeFocus.value = true
}

function handleChromePointerEnter(event: PointerEvent) {
  if (event.pointerType === 'touch') return
  chromeHover.value = true
}

function handleChromePointerLeave(event: PointerEvent) {
  if (event.pointerType === 'touch') return
  chromeHover.value = false
}

function handleChromeFocusOut(event: FocusEvent) {
  const nextTarget = event.relatedTarget as Node | null
  if (!event.currentTarget || !(event.currentTarget as HTMLElement).contains(nextTarget)) {
    chromeFocus.value = false
  }
}

function toggleChrome() {
  const shouldClose = chromePinned.value
  chromePinned.value = !shouldClose
  if (shouldClose) chromeFocus.value = false
}

function closeChrome(event: KeyboardEvent) {
  if (event.key !== 'Escape') return
  event.preventDefault()
  chromePinned.value = false
  chromeHover.value = false
  chromeFocus.value = false
  ignoreNextChromeFocus.value = true
  chromeToggleElement.value?.focus()
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
    @keydown="closeChrome"
  >
    <div
      class="research-report-workspace__chrome-layer"
      :class="{ 'is-open': chromeOpen }"
      data-testid="research-workspace-chrome"
      @pointerenter="handleChromePointerEnter"
      @pointerleave="handleChromePointerLeave"
      @focusin="handleChromeFocusIn"
      @focusout="handleChromeFocusOut"
    >
      <button
        class="research-report-workspace__mobile-nav"
        type="button"
        aria-label="打开导航"
        aria-controls="app-sidebar-navigation"
        @click.stop="layoutStore.openMobileSidebar"
      >
        <Menu aria-hidden="true" />
      </button>
      <button
        ref="chromeToggleElement"
        class="research-report-workspace__chrome-toggle"
        type="button"
        :aria-label="chromeOpen ? '收起报告工作台导航' : '展开报告工作台导航'"
        aria-controls="research-workspace-chrome-panel"
        :aria-expanded="chromeOpen"
        data-testid="research-workspace-chrome-toggle"
        @click.stop="toggleChrome"
      >
        <ElIcon aria-hidden="true">
          <component :is="activeTab.icon" />
        </ElIcon>
        <span>{{ activeTab.label }}</span>
        <ElTag
          :type="statusType"
          effect="plain"
          size="small"
        >
          {{ statusLabel }}
        </ElTag>
        <span
          v-if="streamStatusLabel"
          class="research-report-workspace__stream-summary"
        >
          {{ streamStatusLabel }} · #{{ streamStatusSequence }}
        </span>
        <CaretBottom aria-hidden="true" />
      </button>

      <div
        id="research-workspace-chrome-panel"
        class="research-report-workspace__chrome-panel"
        :inert="!chromeOpen"
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

        <div
          id="research-workspace-stream-status-host"
          class="research-report-workspace__stream-status-host"
          data-testid="research-workspace-stream-status-host"
        />

        <div
          v-if="$slots.context"
          class="research-report-workspace__context"
          data-testid="research-workspace-context"
        >
          <slot name="context" />
        </div>

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
      </div>
    </div>

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
  position: relative;
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

.research-report-workspace__chrome-layer {
  position: absolute;
  inset: 0 0 auto;
  z-index: 10;
  height: 32px;
  pointer-events: auto;
}

.research-report-workspace__chrome-toggle {
  display: flex;
  width: 100%;
  height: 32px;
  align-items: center;
  gap: 7px;
  padding: 0 16px;
  color: var(--color-text-secondary);
  background: color-mix(in srgb, var(--color-surface) 94%, var(--color-brand-50));
  border: 0;
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  font: 500 12px/1 var(--font-sans);
  text-align: left;
}

.research-report-workspace__chrome-toggle > span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.research-report-workspace__chrome-toggle .el-tag {
  margin-left: auto;
}

.research-report-workspace__chrome-toggle > svg {
  flex: 0 0 14px;
  width: 14px;
  height: 14px;
  transition: transform var(--motion-fast) ease;
}

.research-report-workspace__chrome-layer.is-open .research-report-workspace__chrome-toggle > svg {
  transform: rotate(180deg);
}

.research-report-workspace__chrome-toggle:focus-visible {
  outline: 0;
  box-shadow: inset var(--focus-ring);
}

.research-report-workspace__chrome-panel {
  position: absolute;
  top: 32px;
  right: 0;
  left: 0;
  overflow: hidden;
  background: var(--color-surface);
  box-shadow: var(--shadow-overlay);
  opacity: 0;
  pointer-events: none;
  transform: translateY(-12px);
  transition: opacity var(--motion-fast) ease, transform var(--motion-fast) ease;
}

.research-report-workspace__chrome-layer.is-open .research-report-workspace__chrome-panel {
  opacity: 1;
  pointer-events: auto;
  transform: translateY(0);
}

.research-report-workspace__mobile-nav {
  display: none;
}

.research-report-workspace__stream-status-host {
  display: block;
  min-height: 0;
}

.research-report-workspace__stream-status-host:empty {
  display: none;
}

.research-report-workspace__stream-status-host :deep(.research-agent__statusbar) {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 16px;
  color: var(--color-text-secondary);
  background: var(--color-surface-muted);
  border-bottom: 1px solid var(--color-border);
}

.research-report-workspace__stream-status-host :deep(.research-agent__statusbar > div) {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
}

.research-report-workspace__stream-status-host :deep(.research-agent__statusbar p) {
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.research-report-workspace__stream-status-host :deep(.research-agent__actions) {
  flex: 0 0 auto;
}

.research-report-workspace__context {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding: 8px 16px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-surface-muted);
}

.research-report-workspace__context :deep(.el-segmented) {
  max-width: 100%;
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
  padding-top: 32px;
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

  .research-report-workspace__chrome-layer {
    height: 44px;
  }

  .research-report-workspace__chrome-toggle {
    height: 44px;
    padding: 0 12px 0 48px;
  }

  .research-report-workspace__context {
    justify-content: flex-start;
    overflow-x: auto;
  }

  .research-report-workspace__mobile-nav {
    position: absolute;
    top: 8px;
    left: 10px;
    z-index: 1;
    display: grid;
    flex: 0 0 28px;
    width: 28px;
    height: 28px;
    padding: 5px;
    place-items: center;
    color: var(--color-text-secondary);
    background: transparent;
    border: 0;
    border-radius: var(--radius-md);
    cursor: pointer;
  }

  .research-report-workspace__chrome-panel {
    top: 44px;
  }

  .research-report-workspace__body {
    padding-top: 44px;
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
