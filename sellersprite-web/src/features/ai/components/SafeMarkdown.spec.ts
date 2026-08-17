import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

import SafeMarkdown from './SafeMarkdown.vue'

const mermaidMocks = vi.hoisted(() => ({
  initialize: vi.fn(),
  render: vi.fn().mockResolvedValue({ svg: '<svg><text>趋势图</text></svg>' }),
}))

vi.mock('mermaid', () => ({
  default: mermaidMocks,
}))

describe('SafeMarkdown', () => {
  it('sanitizes scripts and unsafe links', () => {
    const wrapper = mount(SafeMarkdown, {
      props: {
        content: '[危险链接](javascript:alert(1))<script>window.hacked=true</script>',
      },
    })

    expect(wrapper.html()).not.toContain('<script')
    expect(wrapper.html()).not.toContain('javascript:')
    expect(wrapper.text()).toContain('危险链接')
  })

  it('renders highlighted fenced code without executing markup', () => {
    const wrapper = mount(SafeMarkdown, {
      props: {
        content: '```ts\nconst value = "<img src=x onerror=alert(1)>"\n```',
      },
    })

    expect(wrapper.find('pre code.hljs').exists()).toBe(true)
    expect(wrapper.find('img').exists()).toBe(false)
    expect(wrapper.text()).toContain('const value')
  })

  it('renders Mermaid fenced Markdown as a sanitized SVG', async () => {
    const source = [
      '```mermaid',
      'xychart-beta',
      '    x-axis ["2026-01", "2026-02"]',
      '    line [1200, 1500]',
      '```',
    ].join('\n')
    const wrapper = mount(SafeMarkdown, {
      props: { content: source },
    })

    await flushPromises()

    expect(mermaidMocks.render).toHaveBeenCalledWith(
      expect.stringMatching(/^safe-markdown-mermaid-/),
      expect.stringContaining('xychart-beta'),
    )
    expect(wrapper.find('.mermaid-diagram svg').exists()).toBe(true)
    expect(wrapper.text()).toContain('趋势图')
    expect(wrapper.text()).not.toContain('```mermaid')
  })

  it('uses a high-contrast palette and widens dense XY charts for horizontal scrolling', async () => {
    const categories = Array.from({ length: 15 }, (_, index) => `"关键词 ${index + 1}"`)
    const values = Array.from({ length: 15 }, (_, index) => `${index + 1}`)
    const source = [
      '```mermaid',
      'xychart-beta',
      `    x-axis [${categories.join(', ')}]`,
      `    line [${values.join(', ')}]`,
      '```',
    ].join('\n')
    const wrapper = mount(SafeMarkdown, {
      props: { content: source },
    })

    await flushPromises()

    expect(mermaidMocks.initialize).toHaveBeenCalledWith(expect.objectContaining({
      themeVariables: {
        xyChart: expect.objectContaining({
          plotColorPalette: expect.stringMatching(/^#2563EB/),
        }),
      },
    }))
    expect(mermaidMocks.render).toHaveBeenLastCalledWith(
      expect.stringMatching(/^safe-markdown-mermaid-/),
      expect.stringContaining('width: 1600\n    height: 500'),
    )
    const svg = wrapper.get('.mermaid-diagram svg').element as SVGElement
    expect(Number.parseInt(svg.style.width)).toBeGreaterThan(1_500)
    expect(svg.style.maxWidth).toBe('none')
  })

  it('uses a tall horizontal layout and preserves full keyword labels', async () => {
    mermaidMocks.render.mockResolvedValueOnce({
      svg: '<svg><text>long countertop nugget ice maker keyword</text></svg>',
    })
    const categories = Array.from(
      { length: 10 },
      (_, index) => index === 0 ? '"long countertop nugget ice maker keyword"' : `"关键词 ${index + 1}"`,
    )
    const source = [
      '```mermaid',
      'xychart-beta horizontal',
      `    x-axis [${categories.join(', ')}]`,
      '    y-axis "个 ASIN" 0 --> 2',
      '    bar [2, 1, 1, 1, 1, 1, 1, 1, 1, 1]',
      '```',
    ].join('\n')
    const wrapper = mount(SafeMarkdown, {
      props: { content: source },
    })

    await flushPromises()

    expect(mermaidMocks.render).toHaveBeenLastCalledWith(
      expect.stringMatching(/^safe-markdown-mermaid-/),
      expect.stringContaining('width: 960\n    height: 650\n    showDataLabel: true'),
    )
    const diagram = wrapper.get('.mermaid-diagram')
    expect(diagram.classes()).toContain('mermaid-diagram--horizontal-bar')
    expect(diagram.find('text title').text()).toBe('long countertop nugget ice maker keyword')
    expect(diagram.get('text').attributes('style')).toContain('cursor: help')
  })
})
