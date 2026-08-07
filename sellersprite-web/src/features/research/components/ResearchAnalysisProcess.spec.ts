import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import { describe, expect, it, vi } from 'vitest'

import type { ResearchStreamRecord } from '../model/researchStream'
import ResearchAnalysisProcess from './ResearchAnalysisProcess.vue'

describe('ResearchAnalysisProcess', () => {
  it('renders streaming sheet reasoning with sanitized Markdown detail', () => {
    const wrapper = mount(ResearchAnalysisProcess, {
      props: {
        expanded: true,
        events: [sheetThink('event-1', [
          '### Sheet 定位',
          '',
          '- **价值**：判断市场容量',
          '- **风险**：头部品牌集中',
        ].join('\n'))],
      },
    })

    expect(wrapper.get('[data-testid="research-analysis-process"]').attributes('open')).toBeDefined()
    expect(wrapper.find('.process-event__detail h3').text()).toBe('Sheet 定位')
    expect(wrapper.find('.process-event__detail strong').text()).toBe('价值')
  })

  it('stops auto-following a streaming detail after the user scrolls up', async () => {
    const wrapper = mount(ResearchAnalysisProcess, {
      props: { expanded: true, events: [sheetThink('event-2', '第一段')] },
    })
    await nextTick()
    const scroller = wrapper.get('.process-event__detail-scroll').element as HTMLElement
    Object.defineProperty(scroller, 'scrollHeight', { configurable: true, value: 500 })
    Object.defineProperty(scroller, 'clientHeight', { configurable: true, value: 100 })
    scroller.scrollTop = 400
    await wrapper.get('.process-event__detail-scroll').trigger('scroll')
    scroller.scrollTop = 0
    await wrapper.get('.process-event__detail-scroll').trigger('scroll')

    await wrapper.setProps({ events: [sheetThink('event-2', '第一段\n第二段')] })
    await nextTick()
    await nextTick()

    expect(scroller.scrollTop).toBe(0)
  })

  it('keeps following rapid detail growth while the scrollbar remains at the bottom', async () => {
    const wrapper = mount(ResearchAnalysisProcess, {
      props: { expanded: true, events: [sheetThink('event-3', '第一段')] },
    })
    await nextTick()
    const scroller = wrapper.get('.process-event__detail-scroll').element as HTMLElement
    let scrollHeight = 500
    Object.defineProperty(scroller, 'scrollHeight', {
      configurable: true,
      get: () => scrollHeight,
    })
    Object.defineProperty(scroller, 'clientHeight', { configurable: true, value: 100 })
    scroller.scrollTop = 400
    await wrapper.get('.process-event__detail-scroll').trigger('scroll')

    scrollHeight = 650
    await wrapper.get('.process-event__detail-scroll').trigger('scroll')
    await wrapper.setProps({ events: [sheetThink('event-3', '第一段\n第二段')] })
    await nextTick()
    await nextTick()

    expect(scroller.scrollTop).toBe(650)
  })

  it('opens and highlights the process event that is currently receiving SSE updates', async () => {
    const scrollIntoView = vi.fn()
    Object.defineProperty(HTMLElement.prototype, 'scrollIntoView', {
      configurable: true,
      value: scrollIntoView,
    })
    const active = sheetThink('event-active', '正在追加判断')
    const wrapper = mount(ResearchAnalysisProcess, {
      props: {
        events: [sheetThink('event-old', '旧判断'), active],
        activeEventId: active.id,
      },
    })
    await nextTick()

    expect(wrapper.get('[data-testid="research-analysis-process"]').attributes('open')).toBeDefined()
    const activeEvent = wrapper.get('[data-event-id="event-active"]')
    expect(activeEvent.classes()).toContain('process-event--active')
    expect(activeEvent.get('.process-event__detail').attributes('open')).toBeDefined()
    expect(scrollIntoView).not.toHaveBeenCalled()
  })
})

function sheetThink(id: string, data: string): ResearchStreamRecord {
  return {
    id,
    sequenceNo: 12,
    jobId: 'job-1',
    analysisRunId: 'run-1',
    scope: 'analysis',
    eventType: 'sheet_think',
    sheetName: 'US',
    phase: 'think',
    message: '模型正在判断 Sheet「US」的市场价值。',
    data,
    terminal: false,
    receivedAt: '12:00:00',
  }
}
