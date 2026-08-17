import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

import type { ResearchStreamRecord } from '../model/researchStream'
import ResearchOfficialEventList from './ResearchOfficialEventList.vue'

const mermaidMocks = vi.hoisted(() => ({
  initialize: vi.fn(),
  render: vi.fn().mockResolvedValue({ svg: '<svg><text>行业月销量趋势</text></svg>' }),
}))

vi.mock('mermaid', () => ({
  default: mermaidMocks,
}))

describe('ResearchOfficialEventList', () => {
  it('renders final summary Markdown and removes unsafe HTML', () => {
    const wrapper = mount(ResearchOfficialEventList, {
      props: {
        events: [record('summary', [
          '## 核心结论',
          '',
          '建议验证 **20-30 美元** 价格带。',
          '<img src="x" onerror="window.__unsafe = true">',
          '<script>window.__unsafe = true</script>',
        ].join('\n'))],
      },
    })

    expect(wrapper.find('h2').text()).toBe('核心结论')
    expect(wrapper.find('.official-event__body strong').text()).toBe('20-30 美元')
    expect(wrapper.find('script').exists()).toBe(false)
    expect(wrapper.find('img').attributes('onerror')).toBeUndefined()
  })

  it('renders a scorecard table at the start of every analysis stage', () => {
    const stageScorecards = [
      ['阶段一初筛评分速览', '市场需求吸引力', '75'],
      ['阶段二深挖评分速览', '痛点可解决性', '70'],
      ['最终决策评分速览', '盈利安全性', '65'],
    ]
    const wrapper = mount(ResearchOfficialEventList, {
      props: {
        events: stageScorecards.map(([title, dimension, score], index) => record(
          'summary',
          [
            `## ${title}`,
            '',
            `**综合评分：${score}/100｜推进建议：有条件推进｜置信度：中**`,
            '',
            '| 维度 | 评分 | 关键依据 |',
            '| --- | ---: | --- |',
            `| ${dimension} | ${score} | 现有证据支持该判断 |`,
          ].join('\n'),
          undefined,
          `stage-${index + 1}`,
        )),
      },
    })

    expect(wrapper.findAll('h2').map((heading) => heading.text()))
      .toEqual(stageScorecards.map(([title]) => title))
    expect(wrapper.findAll('table')).toHaveLength(3)
    expect(wrapper.findAll('thead th').map((cell) => cell.text()))
      .toEqual(['维度', '评分', '关键依据', '维度', '评分', '关键依据', '维度', '评分', '关键依据'])
    expect(wrapper.findAll('tbody tr')).toHaveLength(3)
  })

  it('preserves final report content while the SSE summary is still streaming', () => {
    const streamingSummary = record('summary', [
      '## 最终决策评分速览',
      '**综合评分：70/100｜推进建议：有条件推进｜置信度：中**',
      '## 1. US',
      '**章节评分：70/100｜判断：可验证｜置信度：中**',
      '- 市场具备进入机会。',
      '- 样本月销量为 1200。',
    ].join('\n'))
    streamingSummary.streaming = true
    streamingSummary.stageCode = 'FINAL_ANALYSIS'

    const wrapper = mount(ResearchOfficialEventList, {
      props: { events: [streamingSummary] },
    })

    expect(wrapper.text()).toContain('综合评分：70/100')
    expect(wrapper.text()).toContain('章节评分：70/100')
    expect(wrapper.text()).toContain('市场具备进入机会')
    expect(wrapper.text()).toContain('1200')
  })

  it('normalizes numbered Sheet headings while the screening SSE summary is streaming', () => {
    const streamingSummary = record('summary', [
      '## 阶段一初筛评分速览',
      '**综合评分：70/100｜推进建议：有条件推进｜置信度：中**',
      '## Sheet 一',
      '- 市场具备进入机会。',
      '## Sheet 二',
      '- 销量趋势保持稳定。',
    ].join('\n'))
    streamingSummary.streaming = true
    streamingSummary.stageCode = 'SCREENING'

    const wrapper = mount(ResearchOfficialEventList, {
      props: { events: [streamingSummary] },
    })

    expect(wrapper.findAll('h2').map((heading) => heading.text()))
      .toEqual(['阶段一初筛评分速览', 'US', '行业销售趋势'])
    expect(wrapper.text().toLowerCase()).not.toContain('sheet')
  })

  it('emits the controlled artifact instead of opening a raw URL', async () => {
    const report = {
      artifactId: 'artifact-1',
      fileName: 'analysis.pdf',
      downloadUrl: 'https://untrusted.example/report',
    }
    const wrapper = mount(ResearchOfficialEventList, {
      props: { events: [record('download', '报告已生成', report)] },
    })

    await wrapper.get('button').trigger('click')

    expect(wrapper.emitted('download')?.[0]).toEqual([report])
    expect(wrapper.find('a').exists()).toBe(false)
  })

  it('renders an inline Mermaid Markdown chart from the final summary', async () => {
    const chartMarkdown = [
      '```mermaid',
      'xychart-beta',
      '    title "行业月销量趋势"',
      '    x-axis ["2026-01", "2026-02"]',
      '    y-axis "件" 0 --> 1500',
      '    line [1200, 1500]',
      '```',
    ].join('\n')
    const wrapper = mount(ResearchOfficialEventList, {
      props: {
        events: [record('summary', chartMarkdown)],
      },
    })

    await flushPromises()

    expect(wrapper.find('svg').exists()).toBe(true)
    expect(wrapper.text()).toContain('行业月销量趋势')
    expect(wrapper.text()).not.toContain('1200')
    expect(wrapper.text()).not.toContain('1500')
    expect(wrapper.text()).not.toContain('```mermaid')
  })

  it('only highlights the active event and leaves main timeline scrolling to its parent', () => {
    const scrollIntoView = vi.fn()
    Object.defineProperty(HTMLElement.prototype, 'scrollIntoView', {
      configurable: true,
      value: scrollIntoView,
    })
    const wrapper = mount(ResearchOfficialEventList, {
      props: {
        events: [record('summary', '正在更新的回答')],
        activeEventId: 'event-summary',
      },
    })

    expect(wrapper.get('[data-event-id="event-summary"]').classes())
      .toContain('official-event--active')
    expect(scrollIntoView).not.toHaveBeenCalled()
  })
})

function record(
  eventType: string,
  message: string,
  data?: unknown,
  idSuffix = eventType,
): ResearchStreamRecord {
  return {
    id: `event-${idSuffix}`,
    sequenceNo: 21,
    jobId: 'job-1',
    analysisRunId: 'run-1',
    scope: eventType === 'download' ? 'artifact' : 'analysis',
    eventType,
    phase: eventType === 'summary' ? 'summary' : 'report',
    message,
    data,
    terminal: false,
    receivedAt: '12:00:00',
  }
}
