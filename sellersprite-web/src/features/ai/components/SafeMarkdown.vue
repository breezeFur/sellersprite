<script setup lang="ts">
/* eslint-disable vue/no-v-html -- All rendered HTML is sanitized with DOMPurify. */
import DOMPurify from 'dompurify'
import hljs from 'highlight.js/lib/core'
import bash from 'highlight.js/lib/languages/bash'
import css from 'highlight.js/lib/languages/css'
import java from 'highlight.js/lib/languages/java'
import json from 'highlight.js/lib/languages/json'
import sql from 'highlight.js/lib/languages/sql'
import typescript from 'highlight.js/lib/languages/typescript'
import xml from 'highlight.js/lib/languages/xml'
import { marked } from 'marked'
import { ref, useId, watch } from 'vue'

const props = defineProps<{
  content: string
}>()

hljs.registerLanguage('bash', bash)
hljs.registerLanguage('css', css)
hljs.registerLanguage('html', xml)
hljs.registerLanguage('java', java)
hljs.registerLanguage('json', json)
hljs.registerLanguage('sql', sql)
hljs.registerLanguage('ts', typescript)
hljs.registerLanguage('typescript', typescript)
hljs.registerLanguage('xml', xml)

let mermaidInitialized = false
let mermaidRenderSequence = 0
let renderVersion = 0
const XY_CHART_MIN_WIDTH_PX = 720
const XY_CHART_AXIS_SPACE_PX = 160
const XY_CHART_POINT_WIDTH_PX = 96
const XY_CHART_HEIGHT_PX = 500
const HORIZONTAL_BAR_WIDTH_PX = 960
const HORIZONTAL_BAR_BASE_HEIGHT_PX = 170
const HORIZONTAL_BAR_ROW_HEIGHT_PX = 48
const mermaidIdPrefix = `safe-markdown-mermaid-${useId().replace(/[^A-Za-z0-9_-]/g, '')}`
const rendered = ref('')

interface MermaidChartLayout {
  width: number
  height: number
  horizontalBar: boolean
}

const renderer = new marked.Renderer()
renderer.code = ({ text, lang }) => {
  const requestedLanguage = lang?.trim().toLowerCase() ?? ''
  if (requestedLanguage === 'mermaid') {
    return `<div class="mermaid-diagram" data-mermaid-source="${encodeURIComponent(text)}">正在生成图表…</div>`
  }
  const language = requestedLanguage && hljs.getLanguage(requestedLanguage) ? requestedLanguage : 'plaintext'
  const highlighted = language === 'plaintext'
    ? escapeHtml(text)
    : hljs.highlight(text, { language }).value
  const languageClass = language === 'plaintext' ? '' : ` language-${language}`
  return `<pre><code class="hljs${languageClass}">${highlighted}</code></pre>`
}

watch(() => props.content, () => void renderMarkdown(), { immediate: true })

async function renderMarkdown() {
  const version = ++renderVersion
  const raw = marked.parse(props.content, {
    async: false,
    breaks: true,
    gfm: true,
    renderer,
  })
  const sanitized = DOMPurify.sanitize(raw, {
    USE_PROFILES: { html: true },
    ADD_ATTR: ['data-mermaid-source'],
  })
  const container = document.createElement('div')
  container.innerHTML = sanitized
  container.querySelectorAll('a[href]').forEach((link) => {
    link.setAttribute('target', '_blank')
    link.setAttribute('rel', 'noopener noreferrer')
  })
  const diagrams = [...container.querySelectorAll<HTMLElement>('[data-mermaid-source]')]
  if (diagrams.length > 0) {
    await renderMermaidDiagrams(diagrams, version)
  }
  if (version === renderVersion) rendered.value = container.innerHTML
}

async function renderMermaidDiagrams(diagrams: HTMLElement[], version: number) {
  try {
    const mermaid = (await import('mermaid')).default
    if (!mermaidInitialized) {
      mermaid.initialize({
        startOnLoad: false,
        securityLevel: 'strict',
        theme: 'base',
        htmlLabels: false,
        themeVariables: {
          xyChart: {
            plotColorPalette: '#2563EB,#DC2626,#059669,#7C3AED,#C2410C',
            dataLabelColor: '#FFFFFF',
          },
        },
      })
      mermaidInitialized = true
    }
    for (const diagram of diagrams) {
      if (version !== renderVersion) return
      const source = decodeURIComponent(diagram.dataset.mermaidSource ?? '')
      const layout = xyChartLayout(source)
      const renderSource = layout === null ? source : xyChartSourceWithLayout(source, layout)
      const renderId = `${mermaidIdPrefix}-${++mermaidRenderSequence}`
      const { svg } = await mermaid.render(renderId, renderSource)
      diagram.removeAttribute('data-mermaid-source')
      diagram.innerHTML = DOMPurify.sanitize(svg, {
        USE_PROFILES: { svg: true, svgFilters: true },
      })
      applyMermaidChartLayout(diagram, layout)
      if (layout?.horizontalBar) applyCategoryTooltips(diagram, source)
    }
  } catch {
    diagrams.forEach((diagram) => {
      diagram.removeAttribute('data-mermaid-source')
      diagram.textContent = '图表暂时无法渲染'
      diagram.classList.add('mermaid-diagram--error')
    })
  }
}

function applyMermaidChartLayout(diagram: HTMLElement, layout: MermaidChartLayout | null) {
  const svg = diagram.querySelector<SVGSVGElement>('svg')
  if (layout === null || !svg) return

  svg.style.width = `${layout.width}px`
  svg.style.minWidth = layout.horizontalBar ? `${XY_CHART_MIN_WIDTH_PX}px` : `${layout.width}px`
  svg.style.maxWidth = 'none'
  diagram.classList.add(layout.horizontalBar
    ? 'mermaid-diagram--horizontal-bar'
    : 'mermaid-diagram--scrollable')
}

function xyChartSourceWithLayout(source: string, layout: MermaidChartLayout) {
  const configuration = [
    '---',
    'config:',
    '  xyChart:',
    `    width: ${layout.width}`,
    `    height: ${layout.height}`,
  ]
  if (layout.horizontalBar) configuration.push('    showDataLabel: true')
  return [...configuration, '---', source].join('\n')
}

function xyChartLayout(source: string): MermaidChartLayout | null {
  if (source.trimStart().startsWith('---') || !/^\s*xychart(?:-beta)?\b/im.test(source)) {
    return null
  }

  const seriesPointCounts = [...source.matchAll(/^\s*(?:line|bar)\s*\[([^\]]*)]/gim)]
    .map((match) => match[1]?.split(',').filter((value) => value.trim()).length ?? 0)
  const pointCount = Math.max(0, ...seriesPointCounts)
  const horizontalBar = /^\s*xychart(?:-beta)?\s+horizontal\b/im.test(source)
  if (horizontalBar) {
    return {
      width: HORIZONTAL_BAR_WIDTH_PX,
      height: Math.max(XY_CHART_HEIGHT_PX, HORIZONTAL_BAR_BASE_HEIGHT_PX + pointCount * HORIZONTAL_BAR_ROW_HEIGHT_PX),
      horizontalBar: true,
    }
  }
  return {
    width: Math.max(
      XY_CHART_MIN_WIDTH_PX,
      XY_CHART_AXIS_SPACE_PX + pointCount * XY_CHART_POINT_WIDTH_PX,
    ),
    height: XY_CHART_HEIGHT_PX,
    horizontalBar: false,
  }
}

function applyCategoryTooltips(diagram: HTMLElement, source: string) {
  const axis = source.match(/^\s*x-axis(?:\s+"[^"]*")?\s+\[([^\]]*)]/im)?.[1] ?? ''
  const categories = [...axis.matchAll(/"([^"]*)"/g)].map((match) => match[1] ?? '')
  const labels = [...diagram.querySelectorAll<SVGTextElement>('text')]
  categories.forEach((category) => {
    const label = labels.find((candidate) => candidate.textContent?.trim() === category)
    if (!label || label.querySelector('title')) return
    const title = document.createElementNS('http://www.w3.org/2000/svg', 'title')
    title.textContent = category
    label.prepend(title)
    label.style.cursor = 'help'
  })
}

function escapeHtml(value: string) {
  return value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;')
}
</script>

<template>
  <div
    class="safe-markdown"
    v-html="rendered"
  />
</template>

<style scoped>
.safe-markdown {
  min-width: 0;
  color: inherit;
  line-height: 1.72;
  overflow-wrap: anywhere;
}

.safe-markdown :deep(*) {
  max-width: 100%;
}

.safe-markdown :deep(p) {
  margin: 0 0 var(--space-3);
}

.safe-markdown :deep(p:last-child),
.safe-markdown :deep(ul:last-child),
.safe-markdown :deep(ol:last-child),
.safe-markdown :deep(pre:last-child) {
  margin-bottom: 0;
}

.safe-markdown :deep(ul),
.safe-markdown :deep(ol) {
  margin: var(--space-2) 0 var(--space-3);
  padding-left: var(--space-6);
}

.safe-markdown :deep(a) {
  color: var(--color-brand-700);
  text-decoration: underline;
  text-underline-offset: 2px;
}

.safe-markdown :deep(code) {
  padding: 2px 5px;
  background: rgb(15 23 42 / 7%);
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 0.9em;
}

.safe-markdown :deep(pre) {
  max-width: 100%;
  margin: var(--space-3) 0;
  padding: var(--space-4);
  color: #dbeafe;
  background: #111827;
  border: 1px solid #263246;
  border-radius: var(--radius-md);
  overflow: auto;
}

.safe-markdown :deep(pre code) {
  display: block;
  min-width: max-content;
  padding: 0;
  color: inherit;
  background: transparent;
  border-radius: 0;
  line-height: 1.65;
}

.safe-markdown :deep(blockquote) {
  margin: var(--space-3) 0;
  padding-left: var(--space-3);
  color: var(--color-text-secondary);
  border-left: 3px solid var(--color-brand-500);
}

.safe-markdown :deep(table) {
  width: max-content;
  min-width: 100%;
  border-collapse: collapse;
}

.safe-markdown :deep(th),
.safe-markdown :deep(td) {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border-strong);
  text-align: left;
}

.safe-markdown :deep(.hljs-keyword),
.safe-markdown :deep(.hljs-selector-tag),
.safe-markdown :deep(.hljs-literal) {
  color: #c4b5fd;
}

.safe-markdown :deep(.hljs-string),
.safe-markdown :deep(.hljs-attr) {
  color: #86efac;
}

.safe-markdown :deep(.hljs-number),
.safe-markdown :deep(.hljs-symbol) {
  color: #fcd34d;
}

.safe-markdown :deep(.hljs-comment) {
  color: #94a3b8;
}

.safe-markdown :deep(.mermaid-diagram) {
  width: 100%;
  margin: var(--space-3) 0;
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  overflow-x: auto;
  overscroll-behavior-inline: contain;
  scrollbar-gutter: stable;
}

.safe-markdown :deep(.mermaid-diagram svg) {
  display: block;
  min-width: 560px;
  height: auto;
  margin: 0 auto;
}

.safe-markdown :deep(.mermaid-diagram svg .plot [class*='line-plot-'] path) {
  stroke-width: 3.5;
}

.safe-markdown :deep(.mermaid-diagram--horizontal-bar svg) {
  margin-inline: auto;
}

.safe-markdown :deep(.mermaid-diagram--error) {
  color: var(--el-color-danger);
}
</style>
