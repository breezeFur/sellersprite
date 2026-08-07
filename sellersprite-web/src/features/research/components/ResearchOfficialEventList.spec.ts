import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

import type { ResearchStreamRecord } from '../model/researchStream'
import ResearchOfficialEventList from './ResearchOfficialEventList.vue'

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

  it('emits the controlled artifact instead of opening a raw URL', async () => {
    const report = {
      artifactId: 'artifact-1',
      fileName: 'analysis.md',
      downloadUrl: 'https://untrusted.example/report',
    }
    const wrapper = mount(ResearchOfficialEventList, {
      props: { events: [record('download', '报告已生成', report)] },
    })

    await wrapper.get('button').trigger('click')

    expect(wrapper.emitted('download')?.[0]).toEqual([report])
    expect(wrapper.find('a').exists()).toBe(false)
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
