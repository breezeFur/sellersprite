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
import { computed } from 'vue'

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

const renderer = new marked.Renderer()
renderer.code = ({ text, lang }) => {
  const requestedLanguage = lang?.trim().toLowerCase() ?? ''
  const language = requestedLanguage && hljs.getLanguage(requestedLanguage) ? requestedLanguage : 'plaintext'
  const highlighted = language === 'plaintext'
    ? escapeHtml(text)
    : hljs.highlight(text, { language }).value
  const languageClass = language === 'plaintext' ? '' : ` language-${language}`
  return `<pre><code class="hljs${languageClass}">${highlighted}</code></pre>`
}

const rendered = computed(() => {
  const raw = marked.parse(props.content, {
    async: false,
    breaks: true,
    gfm: true,
    renderer,
  })
  const sanitized = DOMPurify.sanitize(raw, {
    USE_PROFILES: { html: true },
  })
  const container = document.createElement('div')
  container.innerHTML = sanitized
  container.querySelectorAll('a[href]').forEach((link) => {
    link.setAttribute('target', '_blank')
    link.setAttribute('rel', 'noopener noreferrer')
  })
  return container.innerHTML
})

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
</style>
