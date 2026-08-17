import { mount, type VueWrapper } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

import ResearchReportWorkspace from './ResearchReportWorkspace.vue'

type WorkspaceSection = 'report' | 'evidence' | 'selection' | 'process' | 'artifacts'

const tabs: Array<{ section: WorkspaceSection, label: string }> = [
  { section: 'report', label: '报告结论' },
  { section: 'evidence', label: '证据数据' },
  { section: 'process', label: '执行过程' },
  { section: 'artifacts', label: '已发布文件' },
]

describe('ResearchReportWorkspace', () => {
  let wrapper: VueWrapper | undefined

  afterEach(() => {
    wrapper?.unmount()
    wrapper = undefined
  })

  it('renders the four workspace tabs and emits each selected section', async () => {
    wrapper = mountWorkspace()
    const tabButtons = wrapper.findAll('[role="tab"]')

    expect(wrapper.get('[data-testid="research-workspace-chrome-toggle"]')
      .attributes('aria-expanded')).toBe('false')
    expect(wrapper.find('[data-testid="research-workspace-stream-status-host"]').exists()).toBe(true)

    expect(tabButtons.map((tab) => tab.text())).toEqual(tabs.map((tab) => tab.label))
    expect(tabButtons[0].attributes('aria-selected')).toBe('true')
    expect(tabButtons[0].attributes('tabindex')).toBe('0')
    tabButtons.slice(1).forEach((tab) => {
      expect(tab.attributes('aria-selected')).toBe('false')
      expect(tab.attributes('tabindex')).toBe('-1')
    })

    for (const tab of tabs) {
      await wrapper.get(`[data-testid="research-workspace-tab-${tab.section}"]`).trigger('click')
    }

    expect(wrapper.emitted('select')).toEqual(tabs.map((tab) => [tab.section]))
  })

  it('opens the compact workspace chrome by click and closes it with Escape', async () => {
    wrapper = mountWorkspace()
    const toggle = wrapper.get<HTMLButtonElement>('[data-testid="research-workspace-chrome-toggle"]')

    await toggle.trigger('click')
    expect(toggle.attributes('aria-expanded')).toBe('true')

    await wrapper.get('[data-testid="research-workspace-chrome"]').trigger('keydown', { key: 'Escape' })
    expect(toggle.attributes('aria-expanded')).toBe('false')
    expect(document.activeElement).toBe(toggle.element)
  })

  it('adds a dedicated product-selection tab when the workflow exposes a selection review', async () => {
    wrapper = mountWorkspace('selection', true)
    const tabButtons = wrapper.findAll('[role="tab"]')

    expect(tabButtons.map((tab) => tab.text())).toEqual([
      '报告结论',
      '证据数据',
      '商品选择',
      '执行过程',
      '已发布文件',
    ])
    expect(wrapper.get('[data-testid="research-workspace-tab-selection"]')
      .attributes('aria-selected')).toBe('true')

    await wrapper.get('[data-testid="research-workspace-tab-selection"]').trigger('click')
    expect(wrapper.emitted('select')).toEqual([['selection']])
  })

  it('emits back and supports wrapping arrow plus Home and End keyboard navigation', async () => {
    wrapper = mountWorkspace()
    const tabButtons = wrapper.findAll('[role="tab"]')

    await wrapper.get('[data-testid="research-workspace-back"]').trigger('click')
    expect(wrapper.emitted('back')).toEqual([[]])

    const keyboardCases = [
      { from: 0, key: 'ArrowRight', to: 1, section: 'evidence' },
      { from: 0, key: 'ArrowLeft', to: 3, section: 'artifacts' },
      { from: 2, key: 'Home', to: 0, section: 'report' },
      { from: 1, key: 'End', to: 3, section: 'artifacts' },
    ] as const

    for (const keyboardCase of keyboardCases) {
      await tabButtons[keyboardCase.from].trigger('keydown', { key: keyboardCase.key })
      expect(wrapper.emitted('select')?.at(-1)).toEqual([keyboardCase.section])
      expect(document.activeElement).toBe(tabButtons[keyboardCase.to].element)
    }
  })

  it('scrolls the active tab into view when the route section changes', async () => {
    const scrollIntoView = vi.fn()
    Object.defineProperty(HTMLElement.prototype, 'scrollIntoView', {
      configurable: true,
      value: scrollIntoView,
    })
    wrapper = mountWorkspace()
    await nextTick()

    expect(scrollIntoView).toHaveBeenLastCalledWith({ block: 'nearest', inline: 'nearest' })
    expect((scrollIntoView.mock.instances.at(-1) as HTMLElement).dataset.testid)
      .toBe('research-workspace-tab-report')

    await wrapper.setProps({ activeSection: 'process' })
    await nextTick()

    expect((scrollIntoView.mock.instances.at(-1) as HTMLElement).dataset.testid)
      .toBe('research-workspace-tab-process')
  })

  function mountWorkspace(
    activeSection: WorkspaceSection = 'report',
    selectionAvailable = false,
  ) {
    return mount(ResearchReportWorkspace, {
      attachTo: document.body,
      global: { plugins: [createPinia()] },
      props: {
        activeSection,
        title: '美国站美容仪市场调研',
        jobId: 'job-research-1',
        statusLabel: '已完成',
        statusType: 'success',
        statusDetail: '校验并发布',
        selectionAvailable,
      },
      slots: {
        default: '<p data-testid="workspace-slot-content">报告内容</p>',
      },
    })
  }
})
