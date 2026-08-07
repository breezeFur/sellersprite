<script setup lang="ts">
/* eslint-disable vue/no-v-html -- Mermaid SVG is sanitized with DOMPurify before rendering. */
import DOMPurify from 'dompurify'
import { FullScreen, RefreshLeft, ZoomIn, ZoomOut } from '@element-plus/icons-vue'
import { computed, onBeforeUnmount, ref, watch } from 'vue'

import StatePanel from '@/shared/components/StatePanel.vue'
import type { ResearchWorkflowStep } from '../model/research'

const props = withDefaults(defineProps<{
  source: string
  steps?: ResearchWorkflowStep[]
}>(), {
  steps: () => [],
})

const WORKFLOW_DIAGRAM_MIN_WIDTH_PX = 960
const MIN_ZOOM = 0.65
const MAX_ZOOM = 2
const ZOOM_STEP = 0.15

let mermaidInitialized = false
let renderSequence = 0

const renderedSvg = ref('')
const renderError = ref('')
const rendering = ref(false)
const zoom = ref(1)
const previewZoom = ref(1.15)
const previewVisible = ref(false)
let renderVersion = 0

const localizedSource = computed(() => localizeWorkflowSource(props.source, props.steps))

async function renderDiagram() {
  const version = ++renderVersion
  rendering.value = true
  renderError.value = ''
  renderedSvg.value = ''
  try {
    const mermaid = (await import('mermaid')).default
    if (!mermaidInitialized) {
      mermaid.initialize({
        startOnLoad: false,
        securityLevel: 'strict',
        theme: 'base',
        htmlLabels: false,
        flowchart: { curve: 'linear', nodeSpacing: 24, rankSpacing: 54 },
      })
      mermaidInitialized = true
    }
    const renderId = `research-workflow-${++renderSequence}`
    const { svg } = await mermaid.render(renderId, localizedSource.value)
    if (version !== renderVersion) return
    renderedSvg.value = DOMPurify.sanitize(svg, {
      USE_PROFILES: { svg: true, svgFilters: true },
    })
  } catch (error) {
    if (version !== renderVersion) return
    renderError.value = error instanceof Error ? error.message : '工作流拓扑渲染失败'
  } finally {
    if (version === renderVersion) rendering.value = false
  }
}

watch(
  () => localizedSource.value,
  () => void renderDiagram(),
  { immediate: true },
)

function localizeWorkflowSource(source: string, steps: ResearchWorkflowStep[]) {
  const normalizedSource = source.trim().replace(/\r\n?/g, '\n')
  if (!normalizedSource || steps.length === 0) return normalizedSource

  const validSteps = steps.filter((step) => step.nodeCode.trim() && step.label.trim())
  const subgraphCodes = new Set<string>()
  const localizedSubgraphs = validSteps.reduce((result, step) => {
    const nodeCode = step.nodeCode.trim()
    const subgraphPattern = new RegExp(
      `(^[\\t ]*subgraph[\\t ]+)${escapeRegExp(nodeCode)}(?:[\\t ]*\\[[^\\n]*\\])?([\\t ]*)$`,
      'gm',
    )
    return result.replace(subgraphPattern, (_match, prefix: string, suffix: string) => {
      subgraphCodes.add(nodeCode)
      return `${prefix}${nodeCode}["${mermaidLabel(step.label)}"]${suffix}`
    })
  }, normalizedSource)
  const definitions = validSteps
    .filter((step) => !subgraphCodes.has(step.nodeCode.trim()))
    .map((step) => `${step.nodeCode.trim()}["${mermaidLabel(step.label)}"]`)
  if (definitions.length === 0) return localizedSubgraphs
  return `${localizedSubgraphs}\n%% 中文主节点标签\n${definitions.join('\n')}`
}

function mermaidLabel(label: string) {
  return label.replace(/["\r\n]+/g, ' ').trim()
}

function escapeRegExp(value: string) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

function changeZoom(target: 'main' | 'preview', delta: number) {
  const value = target === 'main' ? zoom : previewZoom
  value.value = Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, Number((value.value + delta).toFixed(2))))
}

function resetZoom(target: 'main' | 'preview') {
  if (target === 'main') zoom.value = 1
  else previewZoom.value = 1.15
}

function canvasStyle(value: number) {
  return {
    '--workflow-diagram-width': `${Math.round(WORKFLOW_DIAGRAM_MIN_WIDTH_PX * value)}px`,
  }
}

onBeforeUnmount(() => {
  renderVersion += 1
})
</script>

<template>
  <div class="workflow-diagram">
    <StatePanel
      v-if="rendering"
      status="loading"
      title="正在渲染工作流拓扑"
    />
    <StatePanel
      v-else-if="renderError"
      status="error"
      title="工作流拓扑渲染失败"
      :description="renderError"
    />
    <template v-else>
      <div class="workflow-toolbar">
        <span>缩放 {{ Math.round(zoom * 100) }}%</span>
        <ElButtonGroup>
          <ElTooltip content="缩小拓扑">
            <ElButton
              :icon="ZoomOut"
              aria-label="缩小工作流拓扑"
              :disabled="zoom <= MIN_ZOOM"
              @click="changeZoom('main', -ZOOM_STEP)"
            />
          </ElTooltip>
          <ElTooltip content="放大拓扑">
            <ElButton
              :icon="ZoomIn"
              aria-label="放大工作流拓扑"
              :disabled="zoom >= MAX_ZOOM"
              @click="changeZoom('main', ZOOM_STEP)"
            />
          </ElTooltip>
          <ElTooltip content="恢复原始比例">
            <ElButton
              :icon="RefreshLeft"
              aria-label="重置工作流拓扑缩放"
              @click="resetZoom('main')"
            />
          </ElTooltip>
          <ElTooltip content="全屏查看拓扑">
            <ElButton
              :icon="FullScreen"
              aria-label="全屏查看工作流拓扑"
              data-testid="preview-research-workflow"
              @click="previewVisible = true"
            />
          </ElTooltip>
        </ElButtonGroup>
      </div>
      <div
        class="workflow-canvas"
        :style="canvasStyle(zoom)"
        data-testid="research-workflow-diagram"
        v-html="renderedSvg"
      />
    </template>

    <ElDialog
      v-model="previewVisible"
      class="workflow-preview"
      title="市场调研工作流拓扑"
      fullscreen
      append-to-body
      destroy-on-close
    >
      <div class="workflow-toolbar workflow-toolbar--preview">
        <span>缩放 {{ Math.round(previewZoom * 100) }}%</span>
        <ElButtonGroup>
          <ElButton
            :icon="ZoomOut"
            aria-label="缩小全屏工作流拓扑"
            :disabled="previewZoom <= MIN_ZOOM"
            @click="changeZoom('preview', -ZOOM_STEP)"
          />
          <ElButton
            :icon="ZoomIn"
            aria-label="放大全屏工作流拓扑"
            :disabled="previewZoom >= MAX_ZOOM"
            @click="changeZoom('preview', ZOOM_STEP)"
          />
          <ElButton
            :icon="RefreshLeft"
            aria-label="重置全屏工作流拓扑缩放"
            @click="resetZoom('preview')"
          />
        </ElButtonGroup>
      </div>
      <div
        class="workflow-canvas workflow-canvas--preview"
        :style="canvasStyle(previewZoom)"
        data-testid="research-workflow-preview"
        v-html="renderedSvg"
      />
    </ElDialog>
  </div>
</template>

<style scoped>
.workflow-diagram {
  display: grid;
  min-width: 0;
  gap: 14px;
}

.workflow-canvas {
  min-height: 140px;
  max-height: 68dvh;
  padding: 12px;
  border: 1px solid var(--color-border);
  background: var(--color-surface-muted);
  overflow: auto;
}

.workflow-toolbar {
  display: flex;
  min-width: 0;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

.workflow-toolbar > span {
  color: var(--color-text-secondary);
  font: 12px var(--font-mono);
}

.workflow-toolbar--preview {
  margin-bottom: 12px;
}

.workflow-canvas--preview {
  min-height: calc(100dvh - 142px);
  max-height: calc(100dvh - 142px);
}

.workflow-canvas :deep(svg) {
  display: block;
  width: var(--workflow-diagram-width) !important;
  min-width: var(--workflow-diagram-width);
  max-width: none !important;
  height: auto;
  margin: 0 auto;
}

@media (max-width: 640px) {
  .workflow-toolbar {
    justify-content: space-between;
  }

  .workflow-canvas {
    min-height: 132px;
    padding: 8px;
  }
}
</style>
