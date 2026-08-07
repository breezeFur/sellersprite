<script setup lang="ts">
import { Document, Download, MagicStick, WarningFilled } from '@element-plus/icons-vue'
import { computed } from 'vue'

import SafeMarkdown from '@/features/ai/components/SafeMarkdown.vue'

import { researchEventMeta, researchEventTitle } from '../model/researchEventPresentation'
import type { ResearchReportDownload, ResearchStreamRecord } from '../model/researchStream'

const props = withDefaults(defineProps<{
  events: ResearchStreamRecord[]
  downloadingArtifactId?: string
  activeEventId?: string
}>(), {
  downloadingArtifactId: '',
  activeEventId: '',
})

const emit = defineEmits<{
  download: [report: ResearchReportDownload]
}>()

const hasEvents = computed(() => props.events.length > 0)

function reportDownload(event: ResearchStreamRecord): ResearchReportDownload | undefined {
  const data = event.data
  if (!isRecord(data) || typeof data.artifactId !== 'string' || typeof data.fileName !== 'string') {
    return undefined
  }
  return {
    artifactId: data.artifactId,
    fileName: data.fileName,
    mediaType: typeof data.mediaType === 'string' ? data.mediaType : undefined,
    analysisRunId: typeof data.analysisRunId === 'string' ? data.analysisRunId : undefined,
    downloadUrl: typeof data.downloadUrl === 'string' ? data.downloadUrl : undefined,
  }
}

function eventIcon(eventType: string) {
  if (['error', 'workflow_failed', 'workflow_cancelled'].includes(eventType)) return WarningFilled
  if (eventType === 'download') return Document
  return MagicStick
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value)
}
</script>

<template>
  <section
    v-if="hasEvents"
    class="official-event-list"
    data-testid="research-official-event-list"
    aria-label="市场调研正式分析结果"
  >
    <article
      v-for="event in events"
      :key="event.id"
      class="official-event"
      :class="[
        `official-event--${event.eventType}`,
        { 'official-event--active': event.id === activeEventId },
      ]"
      :data-event-id="event.id"
    >
      <ElIcon class="official-event__icon">
        <component :is="eventIcon(event.eventType)" />
      </ElIcon>
      <div class="official-event__content">
        <div class="official-event__head">
          <strong>{{ researchEventTitle(event) }}</strong>
          <span v-if="researchEventMeta(event)">{{ researchEventMeta(event) }}</span>
        </div>
        <SafeMarkdown
          class="official-event__body"
          :content="event.message"
        />
        <ElButton
          v-if="reportDownload(event)"
          class="official-event__download"
          type="primary"
          plain
          size="small"
          :icon="Download"
          :loading="downloadingArtifactId === reportDownload(event)?.artifactId"
          @click="emit('download', reportDownload(event)!)"
        >
          下载 {{ reportDownload(event)?.fileName }}
        </ElButton>
      </div>
    </article>
  </section>
</template>

<style scoped>
.official-event-list{display:grid;margin-top:16px;gap:12px}.official-event{display:grid;grid-template-columns:28px minmax(0,1fr);gap:10px;padding:15px;border:1px solid var(--color-border);border-radius:var(--radius-md);background:var(--color-surface)}.official-event--summary,.official-event--stage_completed,.official-event--workflow_completed{border-color:var(--color-brand-200);background:linear-gradient(135deg,var(--color-brand-50),var(--color-surface) 60%)}.official-event--product_selection_required{border-color:var(--el-color-warning-light-5);background:var(--el-color-warning-light-9)}.official-event--error,.official-event--workflow_failed{border-color:var(--el-color-danger-light-7);background:var(--el-color-danger-light-9)}.official-event__icon{display:grid;width:28px;height:28px;place-items:center;color:var(--color-brand-700);background:var(--color-brand-50);border-radius:50%}.official-event__content{min-width:0}.official-event__head{display:flex;align-items:center;justify-content:space-between;gap:10px}.official-event__head strong{color:var(--color-text);font-size:13px}.official-event__head span{color:var(--color-text-muted);font:10px var(--font-mono)}.official-event__body{margin-top:8px;color:var(--color-text);font-size:13px}.official-event__download{margin-top:10px}

.official-event__body {
  min-width: 0;
  max-width: 100%;
  overflow-x: auto;
}

.official-event__download {
  max-width: 100%;
  overflow: hidden;
}

.official-event__download :deep(span) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.official-event--active {
  border-left: 3px solid var(--color-brand-500);
  box-shadow: 0 0 0 2px var(--color-brand-100);
}

@media (max-width: 640px) {
  .official-event {
    grid-template-columns: 22px minmax(0, 1fr);
    gap: 8px;
    padding: 12px 10px;
  }

  .official-event__icon {
    width: 22px;
    height: 22px;
  }

  .official-event__head {
    align-items: flex-start;
    flex-direction: column;
    gap: 3px;
  }
}
</style>
