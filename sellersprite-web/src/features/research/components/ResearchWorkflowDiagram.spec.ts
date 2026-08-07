import { flushPromises, mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'

import { defaultResearchWorkflowSteps } from '../model/research'
import ResearchWorkflowDiagram from './ResearchWorkflowDiagram.vue'

const mermaidMocks = vi.hoisted(() => ({
  initialize: vi.fn(),
  render: vi.fn().mockResolvedValue({ svg: '<svg><text>workflow</text></svg>' }),
}))

vi.mock('mermaid', () => ({
  default: mermaidMocks,
}))

describe('ResearchWorkflowDiagram', () => {
  it('keeps compiled edges, adds Chinese parent labels, and supports zoomed preview', async () => {
    const source = [
      'flowchart TD',
      '\tsubgraph screeningGraph',
      '\t\tcollection.collectProducts --> evidence.prepareProducts',
      '\tend',
      '\tscreeningGraph --> productSelectionGate',
      '\tproductSelectionGate --> deepDiveGraph',
      '\tsubgraph deepDiveGraph',
      '\t\tcollection.collectReviews --> evidence.prepareReview',
      '\tend',
      '\tdeepDiveGraph --> finalAnalysisGraph --> finalizeArtifacts',
    ].join('\n')
    const wrapper = mount(ResearchWorkflowDiagram, {
      props: { source, steps: defaultResearchWorkflowSteps },
      global: { stubs: { Teleport: true } },
    })
    await flushPromises()

    expect(mermaidMocks.initialize).toHaveBeenCalledWith(expect.objectContaining({
      htmlLabels: false,
      securityLevel: 'strict',
      startOnLoad: false,
    }))
    expect(mermaidMocks.render).toHaveBeenCalledWith(
      expect.stringMatching(/^research-workflow-/),
      expect.any(String),
    )
    const renderedSource = mermaidMocks.render.mock.calls[0]?.[1]
    expect(renderedSource).toContain('screeningGraph --> productSelectionGate')
    expect(renderedSource).toContain('subgraph screeningGraph["阶段一：市场初筛"]')
    expect(renderedSource).toContain('subgraph deepDiveGraph["阶段二：商品深挖"]')
    expect(renderedSource).toContain('productSelectionGate["商品选择"]')
    expect(renderedSource).toContain('finalizeArtifacts["生成并发布产物"]')
    const diagram = wrapper.get('[data-testid="research-workflow-diagram"]')
    expect(diagram.attributes('style')).toContain('--workflow-diagram-width: 960px')
    expect(diagram.html()).toContain('workflow')

    await wrapper.get('button[aria-label="放大工作流拓扑"]').trigger('click')
    expect(diagram.attributes('style')).toContain('--workflow-diagram-width: 1104px')

    await wrapper.get('[data-testid="preview-research-workflow"]').trigger('click')
    expect(wrapper.get('[data-testid="research-workflow-preview"]').html()).toContain('workflow')
  })
})
