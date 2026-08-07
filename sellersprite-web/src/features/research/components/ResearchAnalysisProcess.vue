<script setup lang="ts">
import { ArrowDown } from '@element-plus/icons-vue'
import { computed, nextTick, reactive, ref, watch } from 'vue'

import SafeMarkdown from '@/features/ai/components/SafeMarkdown.vue'

import {
  researchEventDetail,
  researchEventDetailLabel,
  researchEventMeta,
  researchEventTitle,
} from '../model/researchEventPresentation'
import type { ResearchStreamRecord } from '../model/researchStream'

const DETAIL_BOTTOM_THRESHOLD = 8
const DETAIL_SCROLL_DIRECTION_TOLERANCE = 1

const props = withDefaults(defineProps<{
  events: ResearchStreamRecord[]
  expanded?: boolean
  activeEventId?: string
}>(), {
  expanded: false,
  activeEventId: '',
})

const eventCountText = computed(() => `${props.events.length} 条过程事件`)
const processEvents = computed(() => props.events.map((event) => ({
  event,
  detail: researchEventDetail(event),
  detailLabel: researchEventDetailLabel(event),
})))
const detailAutoFollow = reactive<Record<string, boolean>>({})
const detailLastScrollTop = reactive<Record<string, number>>({})
const detailScrollElements = new Map<string, HTMLElement>()
const detailElements = new Map<string, HTMLDetailsElement>()
const processDetails = ref<HTMLDetailsElement>()
const hasActiveEvent = computed(() => props.events.some((event) => event.id === props.activeEventId))
const sheetThinkDetailSignal = computed(() => processEvents.value
  .filter(({ event }) => event.eventType === 'sheet_think')
  .map(({ event, detail }) => `${event.id}:${detail.length}:${props.expanded}`)
  .join('|'))

watch(sheetThinkDetailSignal, async () => {
  await nextTick()
  detailScrollElements.forEach((element, eventId) => {
    if (detailAutoFollow[eventId] !== false) {
      element.scrollTop = element.scrollHeight
      detailLastScrollTop[eventId] = element.scrollTop
    }
  })
}, { flush: 'post' })

const activeEventSignal = computed(() => {
  const activeEvent = props.events.find((event) => event.id === props.activeEventId)
  if (!activeEvent) return ''
  const detail = researchEventDetail(activeEvent)
  return `${activeEvent.id}:${activeEvent.sequenceNo ?? ''}:${detail.length}`
})

watch(activeEventSignal, async (signal) => {
  if (!signal) return
  await nextTick()
  if (processDetails.value) processDetails.value.open = true
  const detail = detailElements.get(props.activeEventId)
  if (detail) detail.open = true
}, { flush: 'post', immediate: true })

function setDetailScrollRef(eventId: string, element: unknown) {
  if (element instanceof HTMLElement) {
    detailScrollElements.set(eventId, element)
    detailAutoFollow[eventId] ??= true
    detailLastScrollTop[eventId] ??= element.scrollTop
    void scrollDetailToBottomIfFollowing(eventId)
    return
  }
  detailScrollElements.delete(eventId)
}

function setDetailRef(eventId: string, element: unknown) {
  if (element instanceof HTMLDetailsElement) detailElements.set(eventId, element)
  else detailElements.delete(eventId)
}

function handleDetailScroll(eventId: string) {
  const element = detailScrollElements.get(eventId)
  if (!element) return
  const currentScrollTop = element.scrollTop
  const isAtBottom = element.scrollHeight - currentScrollTop - element.clientHeight
    <= DETAIL_BOTTOM_THRESHOLD
  if (isAtBottom) {
    detailAutoFollow[eventId] = true
  } else if (
    currentScrollTop
      < (detailLastScrollTop[eventId] ?? currentScrollTop) - DETAIL_SCROLL_DIRECTION_TOLERANCE
  ) {
    detailAutoFollow[eventId] = false
  }
  detailLastScrollTop[eventId] = currentScrollTop
}

async function scrollDetailToBottomIfFollowing(eventId: string) {
  await nextTick()
  const element = detailScrollElements.get(eventId)
  if (element && detailAutoFollow[eventId] !== false) {
    element.scrollTop = element.scrollHeight
    detailLastScrollTop[eventId] = element.scrollTop
  }
}
</script>

<template>
  <details
    v-if="events.length > 0"
    ref="processDetails"
    class="analysis-process"
    data-testid="research-analysis-process"
    :open="expanded || hasActiveEvent"
  >
    <summary>
      <span>
        <ElIcon><ArrowDown /></ElIcon>
        数据与分析过程
      </span>
      <b>{{ eventCountText }}</b>
    </summary>

    <div class="process-event-list">
      <article
        v-for="{ event, detail, detailLabel } in processEvents"
        :key="event.id"
        class="process-event"
        :class="{ 'process-event--active': event.id === activeEventId }"
        :data-event-id="event.id"
      >
        <span
          class="process-dot"
          aria-hidden="true"
        />
        <div class="process-event__content">
          <div class="process-event__head">
            <strong>{{ researchEventTitle(event) }}</strong>
            <span v-if="researchEventMeta(event)">{{ researchEventMeta(event) }}</span>
          </div>
          <SafeMarkdown
            class="process-event__body"
            :content="event.message"
          />
          <details
            v-if="detail"
            :ref="(element) => setDetailRef(event.id, element)"
            class="process-event__detail"
            :open="expanded || event.id === activeEventId"
          >
            <summary>{{ detailLabel }}</summary>
            <div
              v-if="event.eventType === 'sheet_think'"
              :ref="(element) => setDetailScrollRef(event.id, element)"
              class="process-event__detail-scroll"
              @scroll.passive="handleDetailScroll(event.id)"
            >
              <SafeMarkdown :content="detail" />
            </div>
            <pre v-else>{{ detail }}</pre>
          </details>
        </div>
        <time>{{ event.receivedAt }}</time>
      </article>
    </div>
  </details>
</template>

<style scoped>
.analysis-process{margin-top:16px;border:1px solid var(--color-border);border-radius:var(--radius-md);background:var(--color-surface)}.analysis-process>summary{display:flex;align-items:center;justify-content:space-between;gap:12px;padding:12px 14px;color:var(--color-text-secondary);cursor:pointer;list-style:none}.analysis-process>summary::-webkit-details-marker{display:none}.analysis-process>summary>span{display:flex;align-items:center;gap:7px;font-size:13px;font-weight:650}.analysis-process>summary b{font-size:11px;font-weight:500}.analysis-process[open]>summary .el-icon{transform:rotate(180deg)}.analysis-process .el-icon{transition:transform .18s ease}.process-event-list{display:grid;padding:2px 14px 14px;gap:0}.process-event{display:grid;grid-template-columns:10px minmax(0,1fr) auto;gap:10px;padding:12px 0;border-top:1px solid var(--color-border)}.process-dot{width:8px;height:8px;margin-top:6px;background:var(--color-brand-500);border:2px solid var(--color-surface);border-radius:50%;box-shadow:0 0 0 1px var(--color-brand-200)}.process-event__content{min-width:0}.process-event__head{display:flex;align-items:center;flex-wrap:wrap;gap:8px}.process-event__head strong{color:var(--color-text);font-size:13px}.process-event__head span,time{color:var(--color-text-muted);font:10px/1.5 var(--font-mono)}.process-event__body{margin-top:5px;color:var(--color-text-secondary);font-size:12px}.process-event__detail{margin-top:8px}.process-event__detail>summary{color:var(--color-brand-700);font-size:11px;cursor:pointer}.process-event__detail pre,.process-event__detail-scroll{max-height:260px;margin:8px 0 0;padding:10px;color:var(--color-text-secondary);background:var(--color-surface-muted);border:1px solid var(--color-border);border-radius:var(--radius-sm);font:11px/1.65 var(--font-mono);white-space:pre-wrap;overflow:auto}.process-event__detail-scroll{font-family:inherit}.process-event time{padding-top:2px;white-space:nowrap}@media(max-width:640px){.process-event{grid-template-columns:10px minmax(0,1fr)}.process-event time{display:none}}

.process-event__body {
  min-width: 0;
  max-width: 100%;
  overflow-x: auto;
}

.process-event--active {
  margin: 0 -8px;
  padding-right: 8px;
  padding-left: 8px;
  border-left: 3px solid var(--color-brand-500);
  background: var(--color-brand-50);
}
</style>
