import { flushPromises, mount, type VueWrapper } from '@vue/test-utils'
import type { CascaderNode, CascaderOption, CascaderProps } from 'element-plus'
import { createPinia } from 'pinia'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useLayoutStore } from '@/layouts/useLayoutStore'

import * as researchApi from '../api/researchApi'
import * as streamApi from '../api/researchStreamApi'
import type { ResearchStreamOptions } from '../api/researchStreamApi'
import {
  createDefaultCollectionGraphConfig,
  defaultResearchWorkflowSteps,
  researchArtifactTypes,
  type ResearchJobDetail,
  type ResearchNodeExecution,
} from '../model/research'
import type { ResearchStreamEvent } from '../model/researchStream'
import { useResearchAgentStore } from '../stores/useResearchAgentStore'
import MarketResearchPage from './MarketResearchPage.vue'

vi.mock('vue-router', () => ({
  useRoute: vi.fn(),
  useRouter: vi.fn(),
}))

vi.mock('../api/researchApi', () => ({
  createResearchJob: vi.fn(),
  getResearchCategoryNodes: vi.fn(),
  resolveResearchCategoriesByAsins: vi.fn(),
  cancelResearchJob: vi.fn(),
  retryResearchJob: vi.fn(),
  getResearchWorkflowTopology: vi.fn(),
}))

vi.mock('../api/researchStreamApi', () => ({
  downloadResearchArtifact: vi.fn(),
  streamResearchEvents: vi.fn(),
}))

vi.mock('../components/ResearchWorkflowDiagram.vue', () => ({
  default: {
    props: ['source', 'steps'],
    template: '<div data-testid="research-workflow-source" :data-source="source" />',
  },
}))

vi.mock('../components/ResearchConversationPanel.vue', () => ({
  default: {
    name: 'ResearchConversationPanel',
    props: {
      jobId: String,
      jobStatus: String,
      focusMode: Boolean,
      section: String,
      workspace: Boolean,
      activeEventId: String,
    },
    emits: ['resume', 'followUpRunsChange', 'openProductSelection'],
    template: '<div data-testid="research-conversation-panel" :data-job-id="jobId" :data-job-status="jobStatus" :data-focus-mode="String(focusMode)" :data-section="section" :data-workspace="String(workspace)" :data-active-event-id="activeEventId" />',
  },
}))

vi.mock('../components/ResearchEvidencePanel.vue', () => ({
  default: {
    props: ['jobId', 'stageCode'],
    template: '<div data-testid="research-evidence-panel" :data-job-id="jobId" :data-stage-code="stageCode" />',
  },
}))

vi.mock('../components/ResearchProductSelectionPanel.vue', () => ({
  default: {
    name: 'ResearchProductSelectionPanel',
    props: ['jobId', 'jobStatus', 'draftAsins'],
    emits: ['submitted', 'update:draftAsins'],
    template: '<div data-testid="research-product-selection" :data-job-id="jobId" :data-job-status="jobStatus" />',
  },
}))

interface StreamConnection {
  jobId: string
  afterSequence: number
  options: ResearchStreamOptions
}

describe('MarketResearchPage', () => {
  const replace = vi.fn()
  const push = vi.fn()
  let routeState: { query: Record<string, string | undefined> }
  let connections: StreamConnection[]

  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date(2026, 6, 28, 12))
    replace.mockReset().mockResolvedValue(undefined)
    push.mockReset().mockResolvedValue(undefined)
    routeState = reactive({ query: {} })
    connections = []
    vi.mocked(useRoute).mockReturnValue(routeState as unknown as ReturnType<typeof useRoute>)
    vi.mocked(useRouter).mockReturnValue({ replace, push } as unknown as ReturnType<typeof useRouter>)
    vi.mocked(researchApi.createResearchJob).mockReset()
    vi.mocked(researchApi.getResearchCategoryNodes).mockReset().mockResolvedValue([])
    vi.mocked(researchApi.cancelResearchJob).mockReset().mockResolvedValue(undefined)
    vi.mocked(researchApi.retryResearchJob).mockReset().mockResolvedValue(undefined)
    vi.mocked(researchApi.getResearchWorkflowTopology).mockReset().mockResolvedValue({
      type: 'MERMAID',
      title: '市场调研工作流',
      content: 'flowchart TD\nscreeningGraph --> productSelectionGate',
      steps: defaultResearchWorkflowSteps,
    })
    vi.mocked(streamApi.downloadResearchArtifact).mockReset()
    vi.mocked(streamApi.streamResearchEvents).mockReset().mockImplementation(
      (jobId, afterSequence, options) => {
        connections.push({ jobId, afterSequence, options })
        options.onOpen?.()
        return new Promise<void>((resolve) => {
          if (options.signal.aborted) resolve()
          else options.signal.addEventListener('abort', () => resolve(), { once: true })
        })
      },
    )
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.restoreAllMocks()
  })

  it('defaults the month picker to the previous month across a year boundary', () => {
    vi.setSystemTime(new Date(2026, 0, 15, 12))

    const wrapper = mountPage()

    expect((wrapper.get('input[aria-label="月份"]').element as HTMLInputElement).value).toBe('2025-12')
  })

  it('shows the current nested workflow version in the selected context', () => {
    const wrapper = mountPage()

    expect(wrapper.text()).toContain('market-research-v6-cache-insights')
  })

  it('shows the Top20 gate in a dedicated workspace section while waiting for input', async () => {
    routeState.query.jobId = 'job-research-1'
    routeState.query.view = 'conversation'
    routeState.query.section = 'selection'
    const wrapper = mountPage()
    pushSnapshot(connections[0], researchJob({
      status: 'WAITING_INPUT',
      currentStage: 'PRODUCT_SELECTION',
      waitingInputType: 'PRODUCT_SELECTION',
      currentNode: 'productSelectionGate',
      currentNodeName: '选择阶段二商品',
      progress: 45,
      cancellable: false,
    }), [event(7, 'product_selection_required', {
      scope: 'workflow',
      stageCode: 'PRODUCT_SELECTION',
    })])
    await nextTick()

    expect(wrapper.get('[data-testid="research-report-workspace"]').text())
      .toContain('等待商品选择')
    expect(wrapper.find('[data-testid="research-selection-review"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="research-workspace-product-selection"]').exists()).toBe(true)
    expect(wrapper.get('[data-testid="research-workspace-tab-selection"]')
      .attributes('aria-selected')).toBe('true')
    expect(wrapper.get('[data-testid="research-product-selection"]').attributes('data-job-status'))
      .toBe('WAITING_INPUT')
    expect(replace).not.toHaveBeenCalled()
  })

  it('keeps task details focused on deep-dive evidence after stage two completes', async () => {
    routeState.query.jobId = 'job-research-1'
    const wrapper = mountPage()
    pushSnapshot(connections[0], researchJob({
      currentStage: 'FINAL_ANALYSIS',
      currentNode: 'finalAnalysisGraph',
      currentNodeName: '最终综合分析',
      progress: 85,
    }), [
      event(7, 'product_selection_submitted', { stageCode: 'PRODUCT_SELECTION' }),
      event(8, 'stage_completed', { stageCode: 'DEEP_DIVE' }),
    ])
    await nextTick()

    expect(wrapper.find('[data-testid="research-selection-review"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="research-deep-dive-evidence"]').exists()).toBe(true)
    expect(wrapper.findAll('[data-testid="research-evidence-panel"]')
      .map((panel) => panel.attributes('data-stage-code')))
      .toEqual(['DEEP_DIVE'])
  })

  it('treats market abandoned as a normal terminal state', async () => {
    routeState.query.jobId = 'job-research-1'
    const wrapper = mountPage()
    pushSnapshot(connections[0], researchJob({
      status: 'ABANDONED',
      currentStage: 'PRODUCT_SELECTION',
      waitingInputType: null,
      progress: 100,
      cancellable: false,
      finishedAt: 1_700_000_100_000,
    }), [event(8, 'market_abandoned', {
      scope: 'workflow',
      terminal: true,
      stageCode: 'PRODUCT_SELECTION',
    })])
    await nextTick()

    expect(wrapper.get('[data-testid="research-job-status"]').text()).toContain('已放弃')
    expect(wrapper.text()).toContain('这是正常业务终态')
    expect(wrapper.getComponent({ name: 'ElProgress' }).props('percentage')).toBe(40)
    expect(wrapper.findAllComponents({ name: 'ElSteps' })[0].props('active')).toBe(1)
  })

  it('provides structured parameters for all six collection nodes', () => {
    const wrapper = mountPage()

    expect(wrapper.text()).toContain('采集商品池')
    expect(wrapper.text()).toContain('采集市场销售趋势')
    expect(wrapper.text()).toContain('采集关键词需求趋势')
    expect(wrapper.text()).toContain('采集细分市场机会')
    expect(wrapper.text()).toContain('采集评论')
    expect(wrapper.text()).toContain('采集关键词情报')
    expect((wrapper.get('input[aria-label="销售趋势月数"]').element as HTMLInputElement).value)
      .toBe('12')
    expect((wrapper.get('input[aria-label="每个 ASIN 评论数"]').element as HTMLInputElement).value)
      .toBe('20')
    expect(wrapper.find('input[aria-label="自动执行 Curation 分析"]').exists()).toBe(false)
  })

  it('shows the persisted task inputs and keeps deep-dive progress on the third main step', async () => {
    routeState.query.jobId = 'job-research-1'
    const wrapper = mountPage()
    pushSnapshot(connections[0], researchJob({
      currentStage: 'DEEP_DIVE',
      currentNode: 'collection.collectAsinIntelligence',
      currentNodeName: '采集ASIN经营情报',
      progress: 29,
      analysisGoal: '重点判断退货风险',
      seedAsins: ['B012345678', 'B087654321'],
    }), undefined, undefined, [nodeExecution({
      nodeCode: 'collection.collectAsinIntelligence',
      nodeName: '采集ASIN经营情报',
      status: 'RUNNING',
    })])
    await flushPromises()

    const inputSummary = wrapper.get('[data-testid="research-task-input-summary"]')
    expect(inputSummary.text()).toContain('重点判断退货风险')
    expect(inputSummary.text()).toContain('B012345678')
    expect(inputSummary.text()).toContain('商品池')
    expect(wrapper.find('[data-testid="create-research-job"]').exists()).toBe(false)

    expect(wrapper.findAll('.research-steps--horizontal .el-step__title').map((step) => step.text()))
      .toEqual(defaultResearchWorkflowSteps.map((step) => step.label))
    const progress = wrapper.getComponent({ name: 'ElProgress' }).props('percentage') as number
    expect(progress).toBeGreaterThanOrEqual(40)
    expect(progress).toBeLessThan(60)

    await wrapper.get('[data-testid="research-create-new-job"]').trigger('click')
    expect(push).toHaveBeenCalledWith({ path: '/research/market-report' })
  })

  it('lazily loads root categories and the expanded node children', async () => {
    vi.mocked(researchApi.getResearchCategoryNodes)
      .mockResolvedValueOnce([categoryNode({
        nodeIdPath: '172282',
        nodeLabelPath: 'Electronics',
        nodeLabelLocale: '电子产品',
        nodeLabelPathLocale: '电子产品',
        products: 536875,
      })])
      .mockResolvedValueOnce([categoryNode()])
    const wrapper = mountPage()
    await flushPromises()

    expect(researchApi.getResearchCategoryNodes).toHaveBeenNthCalledWith(1, {
      marketplace: 'US',
      month: '2026-06',
    })

    const lazyLoad = categoryLazyLoad(wrapper)
    const resolveChildren = vi.fn<(nodes?: CascaderOption[]) => void>()
    const rejectChildren = vi.fn<() => void>()
    await lazyLoad({ root: false, value: '172282' } as CascaderNode, resolveChildren, rejectChildren)
    await flushPromises()

    expect(researchApi.getResearchCategoryNodes).toHaveBeenNthCalledWith(2, {
      marketplace: 'US',
      month: '2026-06',
      nodeIdPath: '172282',
    })
    expect(resolveChildren).toHaveBeenCalledWith([{
      label: 'Accessories & Supplies (配件)（320）',
      value: '172282:281407',
    }])
    expect(rejectChildren).not.toHaveBeenCalled()
  })

  it('searches categories through the server and shows complete paths in the same cascader', async () => {
    const wrapper = mountPage()
    await flushPromises()
    vi.mocked(researchApi.getResearchCategoryNodes).mockClear().mockResolvedValueOnce([
      categoryNode(),
      categoryNode({
        nodeIdPath: '172282:24046923011',
        nodeLabelPath: 'Electronics:Headphones',
        nodeLabelLocale: '耳机',
        nodeLabelPathLocale: '电子产品:耳机',
        products: 188,
      }),
    ])

    await wrapper.get('input[aria-label="类目搜索关键词"]').setValue('  配件  ')
    await wrapper.get('[data-testid="search-research-categories"]').trigger('click')
    await flushPromises()

    expect(researchApi.getResearchCategoryNodes).toHaveBeenCalledTimes(1)
    expect(researchApi.getResearchCategoryNodes).toHaveBeenCalledWith({
      marketplace: 'US',
      month: '2026-06',
      keyword: '配件',
    })
    expect(wrapper.findAllComponents({ name: 'ElCascader' })).toHaveLength(1)

    const cascader = wrapper.getComponent({ name: 'ElCascader' })
    expect(cascader.props('options')).toEqual([{
      label: 'Electronics (电子产品)',
      value: '172282',
      children: [{
        label: 'Accessories & Supplies (配件)（320）',
        value: '172282:281407',
        leaf: true,
      }, {
        label: 'Headphones (耳机)（188）',
        value: '172282:24046923011',
        leaf: true,
      }],
    }])
    expect((cascader.props('props') as CascaderProps).lazy).not.toBe(true)
    expect((cascader.props('props') as CascaderProps).emitPath).toBe(false)
  })

  it('clears category search and restores the lazy root tree', async () => {
    const wrapper = mountPage()
    await flushPromises()
    vi.mocked(researchApi.getResearchCategoryNodes).mockClear().mockResolvedValueOnce([categoryNode()])

    await wrapper.get('input[aria-label="类目搜索关键词"]').setValue('配件')
    await wrapper.get('[data-testid="search-research-categories"]').trigger('click')
    await flushPromises()
    await selectCategory(wrapper)

    vi.mocked(researchApi.getResearchCategoryNodes).mockClear().mockResolvedValueOnce([
      categoryNode({
        nodeIdPath: '172282',
        nodeLabelPath: 'Electronics',
        nodeLabelLocale: '电子产品',
        nodeLabelPathLocale: '电子产品',
        products: 536875,
      }),
    ])
    await wrapper.get('input[aria-label="类目搜索关键词"]').setValue('')
    await flushPromises()

    expect(researchApi.getResearchCategoryNodes).toHaveBeenCalledTimes(1)
    expect(researchApi.getResearchCategoryNodes).toHaveBeenCalledWith({
      marketplace: 'US',
      month: '2026-06',
    })

    const cascader = wrapper.getComponent({ name: 'ElCascader' })
    expect(cascader.props('options')).toEqual([])
    expect(cascader.props('modelValue')).toBe('')
    expect((cascader.props('props') as CascaderProps).lazy).toBe(true)
    expect((cascader.props('props') as CascaderProps).lazyLoad).toBeTypeOf('function')
  })

  it('ignores a pending search after the keyword changes', async () => {
    let resolveSearch!: (
      value: Awaited<ReturnType<typeof researchApi.getResearchCategoryNodes>>,
    ) => void
    const wrapper = mountPage()
    await flushPromises()
    vi.mocked(researchApi.getResearchCategoryNodes)
      .mockClear()
      .mockImplementationOnce(() => new Promise((resolve) => {
        resolveSearch = resolve
      }))
      .mockResolvedValueOnce([])

    await wrapper.get('input[aria-label="类目搜索关键词"]').setValue('headphones')
    await wrapper.get('[data-testid="search-research-categories"]').trigger('click')
    await flushPromises()
    await wrapper.get('input[aria-label="类目搜索关键词"]').setValue('speakers')
    await flushPromises()

    resolveSearch([categoryNode()])
    await flushPromises()

    expect(researchApi.getResearchCategoryNodes).toHaveBeenNthCalledWith(1, {
      marketplace: 'US',
      month: '2026-06',
      keyword: 'headphones',
    })
    expect(researchApi.getResearchCategoryNodes).toHaveBeenNthCalledWith(2, {
      marketplace: 'US',
      month: '2026-06',
    })

    const cascader = wrapper.getComponent({ name: 'ElCascader' })
    expect(cascader.props('options')).toEqual([])
    expect(cascader.props('modelValue')).toBe('')
    expect((cascader.props('props') as CascaderProps).lazy).toBe(true)
  })

  it('keeps the selected tree category when clearing an unsubmitted search keyword', async () => {
    const wrapper = mountPage()
    await flushPromises()
    vi.mocked(researchApi.getResearchCategoryNodes).mockClear()
    await selectCategory(wrapper)
    await wrapper.get('input[aria-label="类目搜索关键词"]').setValue('draft keyword')

    const searchInput = wrapper.findAllComponents({ name: 'ElInput' }).find(
      (input) => input.find('input[aria-label="类目搜索关键词"]').exists(),
    )
    if (!searchInput) throw new Error('未找到类目搜索输入组件')
    searchInput.vm.$emit('update:modelValue', '')
    searchInput.vm.$emit('clear')
    await flushPromises()

    expect(researchApi.getResearchCategoryNodes).not.toHaveBeenCalled()
    const cascader = wrapper.getComponent({ name: 'ElCascader' })
    expect(cascader.props('modelValue')).toBe('172282:281407')
    expect((cascader.props('props') as CascaderProps).lazy).toBe(true)
  })

  it('resolves category from single ASIN and directly applies it to cascader', async () => {
    const wrapper = mountPage()
    await flushPromises()

    vi.mocked(researchApi.resolveResearchCategoriesByAsins).mockResolvedValueOnce([
      {
        nodeIdPath: '1055398:1063252:1063280',
        nodeId: '1063280',
        displayName: 'Blankets & Throws (毯子、盖毯)',
        nodeLabelPath: 'Home & Kitchen:Bedding:Blankets & Throws',
        nodeLabel: 'Blankets & Throws',
        nodeLabelLocale: '毯子、盖毯',
        matchedCount: 1,
        matchedAsins: ['B08GHW4TBS'],
        matchedRatio: 100.0,
      },
    ])

    await wrapper.get('input[aria-label="ASIN 反查类目"]').setValue('B08GHW4TBS')
    await wrapper.get('[data-testid="resolve-category-by-asin"]').trigger('click')
    await flushPromises()

    expect(researchApi.resolveResearchCategoriesByAsins).toHaveBeenCalledWith({
      marketplace: 'US',
      asins: ['B08GHW4TBS'],
      month: '2026-06',
    })

    const cascader = wrapper.getComponent({ name: 'ElCascader' })
    expect(cascader.props('modelValue')).toBe('1055398:1063252:1063280')
  })

  it('creates a job and lets snapshot plus aggregate events drive it to success', async () => {
    vi.mocked(researchApi.createResearchJob).mockResolvedValue({
      jobId: 'job-research-1',
      status: 'QUEUED',
      dataSourceMode: 'MOCK',
      workflowVersion: 'market-research-v6-cache-insights',
    })

    const wrapper = mountPage()
    await selectCategory(wrapper)
    await wrapper.get('input[aria-label="报告名称"]').setValue('美容仪美国站市场调研')
    await wrapper.get('input[aria-label="核心关键词"]').setValue('facial cleansing device')
    await wrapper.get('textarea[aria-label="Agent 分析目标"]').setValue('重点判断进入机会和退货风险')
    await wrapper.get('[data-testid="create-research-job"]').trigger('click')
    await flushPromises()

    expect(researchApi.createResearchJob).toHaveBeenCalledWith({
      reportName: '美容仪美国站市场调研',
      marketplace: 'US',
      nodeIdPath: '172282:281407',
      month: '2026-06',
      keyword: 'facial cleansing device',
      analysisGoal: '重点判断进入机会和退货风险',
      collectionConfig: {
        collectProducts: {
          productResearch: {},
          pagination: { startPage: 1, pageSize: 100, targetCount: 100 },
          enrichmentAsinLimit: 5,
        },
        collectMarketSalesTrend: { monthCount: 12 },
        collectKeywordDemandTrend: { topN: 100 },
        collectSegmentOpportunity: {
          marketResearch: {},
          pagination: { startPage: 1, pageSize: 50, targetCount: 50 },
          distribution: {
            topN: 100,
            newProduct: 6,
            asins: [],
          },
        },
        collectReviews: {
          starList: [],
          typeList: [],
          pagination: { startPage: 1, pageSize: 10, targetCountPerAsin: 20 },
        },
        collectKeywordIntelligence: {
          keywordResearch: { page: 1, size: 15 },
          keywordMiner: { page: 1, size: 50 },
          trafficKeyword: { page: 1, size: 50 },
          trafficAsinLimit: 5,
        },
      },
    })
    expect(replace).toHaveBeenCalledWith({ query: { jobId: 'job-research-1' } })
    expect(connections).toHaveLength(1)
    expect(connections[0]).toMatchObject({ jobId: 'job-research-1', afterSequence: 0 })

    pushSnapshot(connections[0], researchJob())
    await nextTick()
    expect(wrapper.get('[data-testid="research-job-status"]').text()).toContain('执行中')

    pushEvents(connections[0], researchJob({
      status: 'SUCCEEDED',
      currentNode: 'report.publishReport',
      currentNodeName: '校验并发布',
      progress: 100,
      finishedAt: 1_700_000_100_000,
    }), [event(2, 'workflow_completed')])
    await nextTick()

    expect(wrapper.get('[data-testid="research-job-status"]').text()).toContain('已完成')
    expect(wrapper.text()).toContain('报告已生成')
    expect(wrapper.text()).toContain(defaultResearchWorkflowSteps.at(-1)!.label)
    await vi.advanceTimersByTimeAsync(4_000)
    expect(connections).toHaveLength(1)
  })

  it('locks duplicate creation while the create command is pending', async () => {
    let resolveCreate!: (value: Awaited<ReturnType<typeof researchApi.createResearchJob>>) => void
    vi.mocked(researchApi.createResearchJob).mockImplementation(() => new Promise((resolve) => {
      resolveCreate = resolve
    }))

    const wrapper = mountPage()
    await selectCategory(wrapper)
    await wrapper.get('input[aria-label="报告名称"]').setValue('测试报告')
    const createButton = wrapper.get('[data-testid="create-research-job"]')

    await createButton.trigger('click')
    await flushPromises()
    expect(researchApi.createResearchJob).toHaveBeenCalledTimes(1)
    expect((createButton.element as HTMLButtonElement).disabled).toBe(true)

    await createButton.trigger('click')
    expect(researchApi.createResearchJob).toHaveBeenCalledTimes(1)

    resolveCreate({
      jobId: 'job-research-1',
      status: 'QUEUED',
      dataSourceMode: 'MOCK',
      workflowVersion: 'market-research-v6-cache-insights',
    })
    await flushPromises()
  })

  it('restores a failed route job from the first SSE snapshot without polling APIs', async () => {
    routeState.query.jobId = 'job-failed-1'
    const wrapper = mountPage()

    expect(connections).toHaveLength(1)
    pushSnapshot(connections[0], researchJob({
      jobId: 'job-failed-1',
      status: 'FAILED',
      currentNode: 'collection.collectReviews',
      currentNodeName: '采集评论',
      cancellable: false,
      retryable: true,
      progress: 70,
      errorCode: 'RESEARCH_PROVIDER_FAILED',
      errorMessage: '评论数据采集失败',
      finishedAt: 1_700_000_050_000,
    }))
    await nextTick()

    expect(wrapper.text()).toContain('市场调研任务执行失败')
    expect(wrapper.text()).toContain('RESEARCH_PROVIDER_FAILED：评论数据采集失败')
    await vi.advanceTimersByTimeAsync(4_000)
    expect(connections).toHaveLength(1)
  })

  it('groups node executions by the current five-step workflow stage', async () => {
    routeState.query.jobId = 'job-research-1'
    const wrapper = mountPage()

    pushSnapshot(connections[0], researchJob(), undefined, undefined, [
      nodeExecution({
        graphCode: 'collection',
        nodeCode: 'collection.collectProducts',
        nodeName: '采集商品池',
      }),
      nodeExecution({
        graphCode: null,
        nodeCode: 'evidence.prepareBrand',
        nodeName: '整理竞品品牌证据',
      }),
      nodeExecution({
        graphCode: 'collection',
        nodeCode: 'collection.collectReviews',
        nodeName: '采集评论',
      }),
      nodeExecution({
        graphCode: 'report',
        nodeCode: 'report.generateAnalysis',
        nodeName: '生成 AI 报告',
      }),
    ])
    await flushPromises()

    const executions = wrapper.get('.graph-executions').text()
    expect(executions).toContain('阶段一：市场初筛')
    expect(executions).toContain('阶段二：商品深挖')
    expect(executions).toContain('阶段三：最终分析')
    expect(wrapper.findAllComponents({ name: 'ElTable' }).flatMap((table) => (
      (table.props('data') as ResearchNodeExecution[]).map((node) => node.nodeCode)
    ))).toEqual([
      'collection.collectProducts',
      'evidence.prepareBrand',
      'collection.collectReviews',
      'report.generateAnalysis',
    ])
  })

  it('opens the process workspace for live execution and moves to report after final output', async () => {
    routeState.query.jobId = 'job-research-1'
    const pinia = createPinia()
    mountPage(pinia)
    const layoutStore = useLayoutStore(pinia)
    const runningJob = researchJob()

    pushSnapshot(connections[0], runningJob, [event(1, 'research_started')])
    await nextTick()
    await vi.runOnlyPendingTimersAsync()
    await nextTick()

    expect(replace).toHaveBeenLastCalledWith({
      query: {
        jobId: 'job-research-1',
        view: 'conversation',
        section: 'process',
      },
    })
    expect(layoutStore.sidebarCollapsed).toBe(true)

    routeState.query.view = 'conversation'
    routeState.query.section = 'process'
    replace.mockClear()

    pushEvents(connections[0], runningJob, [event(2, 'plan', {
      scope: 'analysis',
      analysisRunId: 'run-1',
    })])
    await nextTick()
    expect(replace).not.toHaveBeenCalled()

    pushEvents(connections[0], researchJob({
      status: 'SUCCEEDED',
      currentStage: 'FINAL_ANALYSIS',
      currentNode: 'finalizeArtifacts',
      progress: 100,
    }), [event(3, 'report', {
      scope: 'analysis',
      analysisRunId: 'run-1',
      message: '最终报告',
    })])
    await nextTick()

    expect(replace).toHaveBeenLastCalledWith({
      query: {
        jobId: 'job-research-1',
        view: 'conversation',
        section: 'report',
      },
    })
  })

  it('respects a manual tab choice until the live business section changes', async () => {
    routeState.query.jobId = 'job-research-1'
    routeState.query.view = 'conversation'
    routeState.query.section = 'process'
    const wrapper = mountPage()
    const runningJob = researchJob()
    pushSnapshot(connections[0], runningJob)
    await nextTick()
    replace.mockClear()

    pushEvents(connections[0], runningJob, [event(2, 'workbook_ready', {
      stageCode: 'DEEP_DIVE',
    })])
    await nextTick()

    expect(replace).toHaveBeenLastCalledWith({
      query: {
        jobId: 'job-research-1',
        view: 'conversation',
        section: 'evidence',
      },
    })
    routeState.query.section = 'evidence'
    await nextTick()
    replace.mockClear()

    await wrapper.get('[data-testid="research-workspace-tab-report"]').trigger('click')
    routeState.query.section = 'report'
    await nextTick()
    replace.mockClear()

    pushEvents(connections[0], runningJob, [event(3, 'workbook_ready', {
      stageCode: 'DEEP_DIVE',
    })])
    await nextTick()
    expect(replace).not.toHaveBeenCalled()

    pushEvents(connections[0], runningJob, [event(4, 'research_node_started')])
    await nextTick()
    expect(replace).toHaveBeenLastCalledWith({
      query: {
        jobId: 'job-research-1',
        view: 'conversation',
        section: 'process',
      },
    })

    routeState.query.section = 'process'
    await nextTick()
    replace.mockClear()
    await wrapper.get('[data-testid="research-workspace-tab-report"]').trigger('click')
    routeState.query.section = 'report'
    await nextTick()
    replace.mockClear()

    pushEvents(connections[0], runningJob, [event(5, 'plan', {
      scope: 'analysis',
      analysisRunId: 'run-new',
    })])
    await nextTick()
    expect(replace).toHaveBeenLastCalledWith({
      query: {
        jobId: 'job-research-1',
        view: 'conversation',
        section: 'process',
      },
    })
  })

  it('enters the terminal workspace after the backend completes a split initial replay', async () => {
    routeState.query.jobId = 'job-research-1'
    mountPage()
    const completedJob = researchJob({
      status: 'SUCCEEDED',
      currentStage: 'FINAL_ANALYSIS',
      currentNode: 'finalizeArtifacts',
      progress: 100,
      cancellable: false,
      finishedAt: 1_700_000_100_000,
    })
    const initialEvents = [event(1, 'summary', {
      scope: 'analysis',
      analysisRunId: 'run-history',
      message: '历史报告',
    })]

    connections[0].options.onFrame({
      frameType: 'snapshot',
      jobId: 'job-research-1',
      afterSequence: 0,
      lastSequence: 1,
      replayComplete: false,
      job: completedJob,
      nodes: [],
      events: initialEvents,
    })
    await nextTick()
    expect(replace).not.toHaveBeenCalled()

    connections[0].options.onFrame({
      frameType: 'events',
      jobId: 'job-research-1',
      afterSequence: 1,
      lastSequence: 1,
      replayComplete: true,
      job: completedJob,
      nodes: [],
      events: [],
    })
    await nextTick()
    await vi.runOnlyPendingTimersAsync()
    await nextTick()

    expect(replace).toHaveBeenLastCalledWith({
      query: {
        jobId: 'job-research-1',
        view: 'conversation',
        section: 'report',
      },
    })
  })

  it('opens the dedicated selection workspace when the live workflow requires product selection', async () => {
    routeState.query.jobId = 'job-research-1'
    routeState.query.view = 'conversation'
    routeState.query.section = 'process'
    mountPage()
    pushSnapshot(connections[0], researchJob())
    await nextTick()
    replace.mockClear()

    pushEvents(connections[0], researchJob({
      status: 'WAITING_INPUT',
      waitingInputType: 'PRODUCT_SELECTION',
      currentNode: 'productSelectionGate',
    }), [event(2, 'product_selection_required', {
      scope: 'workflow',
      stageCode: 'SCREENING',
    })])
    await nextTick()

    expect(replace).toHaveBeenCalledTimes(1)
    expect(replace).toHaveBeenLastCalledWith({
      query: {
        jobId: 'job-research-1',
        view: 'conversation',
        section: 'selection',
      },
    })
  })

  it('keeps a direct product-selection entry in task details after the user returns', async () => {
    routeState.query.jobId = 'job-research-1'
    const wrapper = mountPage()
    pushSnapshot(connections[0], researchJob({
      status: 'WAITING_INPUT',
      currentStage: 'PRODUCT_SELECTION',
      waitingInputType: 'PRODUCT_SELECTION',
      currentNode: 'productSelectionGate',
      cancellable: false,
    }))
    await flushPromises()
    push.mockClear()

    await wrapper.get('[data-testid="open-research-product-selection"]').trigger('click')

    expect(push).toHaveBeenLastCalledWith({
      query: {
        jobId: 'job-research-1',
        view: 'conversation',
        section: 'selection',
      },
    })
  })

  it('releases the selection navigation lock when a route guard rejects navigation', async () => {
    routeState.query.jobId = 'job-research-1'
    routeState.query.view = 'conversation'
    routeState.query.section = 'process'
    replace.mockRejectedValueOnce(new Error('navigation rejected'))
    mountPage()
    const waitingJob = researchJob({
      status: 'WAITING_INPUT',
      currentStage: 'PRODUCT_SELECTION',
      waitingInputType: 'PRODUCT_SELECTION',
      currentNode: 'productSelectionGate',
      cancellable: false,
    })

    pushSnapshot(connections[0], waitingJob, [event(1, 'product_selection_required')])
    await flushPromises()
    expect(replace).toHaveBeenCalledTimes(1)

    pushEvents(connections[0], waitingJob, [event(2, 'product_selection_required')])
    await flushPromises()
    expect(replace).toHaveBeenCalledTimes(2)
    expect(replace).toHaveBeenLastCalledWith({
      query: {
        jobId: 'job-research-1',
        view: 'conversation',
        section: 'selection',
      },
    })
  })

  it('reveals a persisted product-selection gate once when restoring a workspace deep link', async () => {
    routeState.query.jobId = 'job-research-1'
    routeState.query.view = 'conversation'
    routeState.query.section = 'artifacts'
    routeState.query.source = 'history'
    const wrapper = mountPage()
    const waitingJob = researchJob({
      status: 'WAITING_INPUT',
      currentStage: 'PRODUCT_SELECTION',
      waitingInputType: 'PRODUCT_SELECTION',
      currentNode: 'productSelectionGate',
      currentNodeName: '选择阶段二商品',
      cancellable: false,
    })

    pushSnapshot(connections[0], waitingJob, [], 0)
    await flushPromises()

    expect(replace).toHaveBeenCalledTimes(1)
    expect(replace).toHaveBeenLastCalledWith({
      query: {
        jobId: 'job-research-1',
        source: 'history',
        view: 'conversation',
        section: 'selection',
      },
    })
    expect(wrapper.find('[data-testid="research-workspace-tab-selection"]').exists()).toBe(true)

    replace.mockClear()
    pushEvents(connections[0], waitingJob, [])
    await flushPromises()
    expect(replace).not.toHaveBeenCalled()
  })

  it('reveals the gate on the first authoritative frame when Pinia retained the same waiting job', async () => {
    routeState.query.jobId = 'job-research-1'
    routeState.query.view = 'conversation'
    routeState.query.section = 'artifacts'
    const waitingJob = researchJob({
      status: 'WAITING_INPUT',
      currentStage: 'PRODUCT_SELECTION',
      waitingInputType: 'PRODUCT_SELECTION',
      currentNode: 'productSelectionGate',
      cancellable: false,
    })
    const pinia = createPinia()
    const researchAgentStore = useResearchAgentStore(pinia)
    researchAgentStore.startJob('job-research-1')
    researchAgentStore.applyFrame({
      frameType: 'snapshot',
      jobId: 'job-research-1',
      afterSequence: 0,
      lastSequence: 0,
      replayComplete: true,
      job: waitingJob,
      nodes: [],
      events: [],
    })

    mountPage(pinia)
    pushSnapshot(connections[0], waitingJob, [], 0)
    await flushPromises()

    expect(replace).toHaveBeenCalledTimes(1)
    expect(replace).toHaveBeenLastCalledWith({
      query: {
        jobId: 'job-research-1',
        view: 'conversation',
        section: 'selection',
      },
    })

    replace.mockClear()
    pushEvents(connections[0], waitingJob, [])
    await flushPromises()
    expect(replace).not.toHaveBeenCalled()
  })

  it.each(['ENTER', 'ABANDON'] as const)(
    'moves from product selection to the live process after a %s decision is submitted',
    async (decision) => {
      routeState.query.jobId = 'job-research-1'
      routeState.query.view = 'conversation'
      routeState.query.section = 'selection'
      const wrapper = mountPage()
      const waitingJob = researchJob({
        status: 'WAITING_INPUT',
        currentStage: 'PRODUCT_SELECTION',
        waitingInputType: 'PRODUCT_SELECTION',
        currentNode: 'productSelectionGate',
        cancellable: false,
      })

      pushSnapshot(connections[0], waitingJob, [], 0)
      await flushPromises()
      replace.mockClear()

      wrapper.getComponent({ name: 'ResearchProductSelectionPanel' })
        .vm.$emit('submitted', decision)
      await flushPromises()

      expect(replace).toHaveBeenLastCalledWith({
        query: {
          jobId: 'job-research-1',
          view: 'conversation',
          section: 'process',
        },
      })
    },
  )

  it('restores the unsubmitted product draft after switching workspace sections', async () => {
    routeState.query.jobId = 'job-research-1'
    routeState.query.view = 'conversation'
    routeState.query.section = 'selection'
    const wrapper = mountPage()
    pushSnapshot(connections[0], researchJob({
      status: 'WAITING_INPUT',
      currentStage: 'PRODUCT_SELECTION',
      waitingInputType: 'PRODUCT_SELECTION',
      currentNode: 'productSelectionGate',
      cancellable: false,
    }))
    await flushPromises()

    wrapper.getComponent({ name: 'ResearchProductSelectionPanel' })
      .vm.$emit('update:draftAsins', ['B012345678'])
    await nextTick()
    routeState.query.section = 'evidence'
    await nextTick()
    expect(wrapper.findComponent({ name: 'ResearchProductSelectionPanel' }).exists()).toBe(false)

    routeState.query.section = 'selection'
    await nextTick()
    expect(wrapper.getComponent({ name: 'ResearchProductSelectionPanel' }).props('draftAsins'))
      .toEqual(['B012345678'])
  })

  it('restores an explicit workspace deep link and returns to task details through the URL', async () => {
    routeState.query.jobId = 'job-research-1'
    routeState.query.view = 'conversation'
    routeState.query.section = 'report'
    routeState.query.source = 'history'
    const pinia = createPinia()
    const wrapper = mountPage(pinia)
    const layoutStore = useLayoutStore(pinia)

    pushSnapshot(connections[0], researchJob({
      status: 'SUCCEEDED',
      currentNode: 'report.publishReport',
      progress: 100,
      cancellable: false,
    }), [
      event(1, 'summary', {
        scope: 'analysis',
        analysisRunId: 'run-history',
        message: '历史分析结论',
      }),
      event(2, 'done', {
        scope: 'analysis',
        analysisRunId: 'run-history',
        terminal: true,
      }),
    ])
    await nextTick()

    expect(wrapper.get('.research-page').classes()).toContain('research-page--workspace')
    expect(wrapper.find('.page-header').exists()).toBe(false)
    expect(wrapper.find('.form-panel').exists()).toBe(false)
    expect(wrapper.find('[data-testid="research-report-workspace"]').exists()).toBe(true)
    expect(wrapper.get('[data-testid="research-conversation-panel"]').attributes('data-section'))
      .toBe('report')
    expect(wrapper.get('[data-testid="research-conversation-panel"]').attributes('data-workspace'))
      .toBe('true')
    expect(layoutStore.sidebarCollapsed).toBe(true)
    expect(replace).not.toHaveBeenCalled()

    await wrapper.get('[data-testid="research-workspace-back"]').trigger('click')

    expect(push).toHaveBeenCalledWith({
      query: {
        jobId: 'job-research-1',
        source: 'history',
      },
    })

    routeState.query.view = undefined
    routeState.query.section = undefined
    await nextTick()
    expect(layoutStore.workspaceFocusMode).toBe(false)

    wrapper.unmount()
    expect(layoutStore.workspaceFocusMode).toBe(false)
  })

  it('opens product selection from the stage-one report decision action', async () => {
    routeState.query.jobId = 'job-research-1'
    routeState.query.view = 'conversation'
    routeState.query.section = 'report'
    const wrapper = mountPage()

    pushSnapshot(connections[0], researchJob({
      status: 'WAITING_INPUT',
      currentStage: 'PRODUCT_SELECTION',
      waitingInputType: 'PRODUCT_SELECTION',
      currentNode: 'productSelectionGate',
      currentNodeName: '等待商品选择',
      cancellable: false,
    }))
    await nextTick()
    push.mockClear()

    wrapper.getComponent({ name: 'ResearchConversationPanel' })
      .vm.$emit('openProductSelection')
    await nextTick()

    expect(push).toHaveBeenCalledWith({
      query: {
        jobId: 'job-research-1',
        view: 'conversation',
        section: 'selection',
      },
    })
  })

  it('keeps FOLLOW_UP SSE in the current workspace section without stealing navigation', async () => {
    routeState.query.jobId = 'job-research-1'
    routeState.query.view = 'conversation'
    routeState.query.section = 'report'
    const wrapper = mountPage()
    const completedJob = researchJob({
      status: 'SUCCEEDED',
      currentNode: 'report.publishReport',
      progress: 100,
      cancellable: false,
    })

    pushSnapshot(connections[0], completedJob, [
      event(1, 'summary', {
        scope: 'analysis',
        analysisRunId: 'run-1',
      }),
      event(2, 'done', {
        scope: 'analysis',
        analysisRunId: 'run-1',
        terminal: true,
      }),
    ])
    await flushPromises()
    wrapper.getComponent({ name: 'ResearchConversationPanel' })
      .vm.$emit('followUpRunsChange', ['run-2'])
    await nextTick()
    replace.mockClear()

    pushEvents(connections[0], completedJob, [
      event(3, 'analysis_queued', {
        scope: 'analysis',
        analysisRunId: 'run-2',
        data: { runType: 'FOLLOW_UP', status: 'QUEUED' },
      }),
    ])
    await nextTick()
    expect(replace).not.toHaveBeenCalled()

    routeState.query.section = 'process'
    await nextTick()
    replace.mockClear()

    pushEvents(connections[0], completedJob, [event(4, 'plan', {
      scope: 'analysis',
      analysisRunId: 'run-2',
    })])
    await nextTick()
    expect(replace).not.toHaveBeenCalled()

    pushEvents(connections[0], completedJob, [event(5, 'summary', {
      scope: 'analysis',
      analysisRunId: 'run-2',
      message: '追问回答',
    })])
    await nextTick()
    expect(replace).not.toHaveBeenCalled()
  })

  it('treats the removed conversation section as an invalid value and renders report', async () => {
    routeState.query.jobId = 'job-research-1'
    routeState.query.view = 'conversation'
    routeState.query.section = 'conversation'
    const wrapper = mountPage()
    pushSnapshot(connections[0], researchJob({
      status: 'SUCCEEDED',
      currentNode: 'report.publishReport',
      progress: 100,
      cancellable: false,
    }))
    await nextTick()

    expect(wrapper.get('[data-testid="research-conversation-panel"]').attributes('data-section'))
      .toBe('report')
    expect(wrapper.find('[data-testid="research-workspace-tab-conversation"]').exists()).toBe(false)
    expect(replace).not.toHaveBeenCalled()
  })

  it('shows and downloads all seven artifacts by artifact id', async () => {
    routeState.query.jobId = 'job-research-1'
    vi.mocked(streamApi.downloadResearchArtifact).mockResolvedValue(
      new Blob(['xlsx'], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
    )
    expect(researchArtifactTypes).toEqual([
      'STAGE1_RAW_WORKBOOK',
      'STAGE1_EVIDENCE_WORKBOOK',
      'STAGE1_CONCLUSION_REPORT',
      'STAGE2_RAW_WORKBOOK',
      'STAGE2_EVIDENCE_WORKBOOK',
      'STAGE2_CONCLUSION_REPORT',
      'AI_ANALYSIS_REPORT',
    ])
    const artifacts = researchArtifactTypes.map((artifactType, index) => ({
      artifactId: `artifact-${index + 1}`,
      analysisRunId: artifactType.endsWith('REPORT') ? `analysis-${index + 1}` : null,
      artifactType,
      fileName: artifactType.endsWith('REPORT')
        ? 'market-research.pdf'
        : `${artifactType.toLowerCase()}.xlsx`,
      mediaType: artifactType.endsWith('REPORT')
        ? 'application/pdf'
        : 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      fileSize: 1024 + index,
      createdAt: 1_700_000_001_000 + index,
    }))
    const createObjectUrl = vi.fn(() => 'blob:market-research')
    const revokeObjectUrl = vi.fn()
    Object.defineProperty(URL, 'createObjectURL', { configurable: true, value: createObjectUrl })
    Object.defineProperty(URL, 'revokeObjectURL', { configurable: true, value: revokeObjectUrl })
    const linkClick = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})
    const wrapper = mountPage()
    pushSnapshot(connections[0], researchJob({
      status: 'SUCCEEDED',
      currentNode: 'report.publishReport',
      progress: 100,
      artifacts,
    }))
    await nextTick()

    for (const artifact of artifacts) {
      await wrapper.get(
        `[data-testid="download-research-artifact-${artifact.artifactType}"]`,
      ).trigger('click')
      await flushPromises()
    }

    artifacts.forEach((artifact, index) => {
      expect(streamApi.downloadResearchArtifact).toHaveBeenNthCalledWith(
        index + 1,
        'job-research-1',
        artifact,
      )
    })
    expect(createObjectUrl).toHaveBeenCalledTimes(7)
    expect(linkClick).toHaveBeenCalledTimes(7)
    await vi.advanceTimersByTimeAsync(1_000)
    expect(revokeObjectUrl).toHaveBeenCalledTimes(7)
  })

  it('aborts the owned SSE connection when the page unmounts', async () => {
    routeState.query.jobId = 'job-research-1'
    const wrapper = mountPage()
    const signal = connections[0]?.options.signal

    expect(signal?.aborted).toBe(false)
    wrapper.unmount()
    await flushPromises()

    expect(signal?.aborted).toBe(true)
  })

  it('switches route jobs and aborts the obsolete stream', async () => {
    routeState.query.jobId = 'job-research-1'
    const wrapper = mountPage()
    const firstSignal = connections[0]?.options.signal
    pushSnapshot(connections[0], researchJob())

    routeState.query.jobId = 'job-research-2'
    await nextTick()

    expect(firstSignal?.aborted).toBe(true)
    expect(connections).toHaveLength(2)
    expect(connections[1]).toMatchObject({ jobId: 'job-research-2', afterSequence: 0 })
    pushSnapshot(connections[1], researchJob({
      jobId: 'job-research-2',
      status: 'FAILED',
      currentNode: 'collection.collectKeywordDemandTrend',
      currentNodeName: '采集关键词需求趋势',
      cancellable: false,
      retryable: true,
      progress: 55,
      errorCode: 'KEYWORD_COLLECTION_FAILED',
      errorMessage: '关键词采集失败',
    }))
    await nextTick()
    expect(wrapper.text()).toContain('KEYWORD_COLLECTION_FAILED：关键词采集失败')

    routeState.query.jobId = undefined
    await nextTick()
    expect(wrapper.text()).toContain('任务参数')
  })

  it('manually refreshes by reconnecting from the last acknowledged sequence', async () => {
    routeState.query.jobId = 'job-research-1'
    const wrapper = mountPage()
    pushSnapshot(connections[0], researchJob(), [event(5, 'research_node_completed')], 5)
    await nextTick()
    const firstSignal = connections[0]?.options.signal

    await wrapper.get('button[aria-label="刷新任务状态"]').trigger('click')

    expect(firstSignal?.aborted).toBe(true)
    expect(connections).toHaveLength(2)
    expect(connections[1]).toMatchObject({ jobId: 'job-research-1', afterSequence: 5 })

    pushEvents(connections[0], researchJob({ status: 'FAILED', progress: 99 }), [event(6, 'workflow_failed')])
    await nextTick()
    expect(wrapper.get('[data-testid="research-job-status"]').text()).toContain('执行中')
  })

  it('loads and caches the compiled workflow topology when expanded', async () => {
    vi.mocked(researchApi.getResearchWorkflowTopology).mockResolvedValue({
      type: 'MERMAID',
      title: '市场调研工作流',
      content: 'flowchart TD\ncollection --> evidence --> report',
    })
    const wrapper = mountPage()

    await wrapper.get('[data-testid="toggle-research-workflow"]').trigger('click')
    await flushPromises()
    expect(researchApi.getResearchWorkflowTopology).toHaveBeenCalledTimes(1)
    expect(wrapper.get('[data-testid="research-workflow-source"]').attributes('data-source'))
      .toContain('collection --> evidence --> report')

    await wrapper.get('[data-testid="toggle-research-workflow"]').trigger('click')
    await wrapper.get('[data-testid="toggle-research-workflow"]').trigger('click')
    await flushPromises()
    expect(researchApi.getResearchWorkflowTopology).toHaveBeenCalledTimes(1)
  })

  it('describes the staged human-review flow without a fixed node count', () => {
    const wrapper = mountPage()

    expect(wrapper.text()).toContain('分阶段采集与分析市场数据')
    expect(wrapper.text()).toContain('任务参数')
    expect(wrapper.text()).not.toContain('22 个执行阶段')
  })

  function mountPage(pinia = createPinia()) {
    return mount(MarketResearchPage, { global: { plugins: [pinia] } })
  }
})

function categoryLazyLoad(wrapper: VueWrapper) {
  const props = wrapper.getComponent({ name: 'ElCascader' }).props('props') as CascaderProps
  if (!props.lazyLoad) throw new Error('类目级联组件未配置懒加载')
  return props.lazyLoad
}

async function selectCategory(
  wrapper: VueWrapper,
  nodeIdPath = '172282:281407',
) {
  wrapper.getComponent({ name: 'ElCascader' }).vm.$emit('update:modelValue', nodeIdPath)
  await nextTick()
}

function categoryNode(overrides: Partial<ReturnType<typeof categoryNodeFixture>> = {}) {
  return { ...categoryNodeFixture(), ...overrides }
}

function categoryNodeFixture() {
  return {
    nodeIdPath: '172282:281407',
    nodeLabelPath: 'Electronics:Accessories & Supplies',
    nodeLabelLocale: '配件',
    nodeLabelPathLocale: '电子产品:配件',
    products: 320,
  }
}

function researchJob(overrides: Partial<ResearchJobDetail> = {}): ResearchJobDetail {
  return {
    jobId: 'job-research-1',
    reportName: '美容仪美国站市场调研',
    marketplace: 'US',
    nodeIdPath: '172282:281407',
    month: '2026-07',
    keyword: 'facial cleansing device',
    dataSourceMode: 'MOCK',
    workflowVersion: 'market-research-v6-cache-insights',
    status: 'RUNNING',
    currentStage: 'SCREENING',
    waitingInputType: null,
    currentNode: 'collection.collectProducts',
    currentNodeName: '采集商品池',
    progress: 35,
    attemptCount: 1,
    maxAttempts: 3,
    remainingAttempts: 2,
    nextRunAt: null,
    leaseUntil: null,
    heartbeatAt: 1_700_000_001_000,
    cancelRequestedAt: null,
    cancellable: true,
    retryable: false,
    errorCode: null,
    errorMessage: null,
    startedAt: 1_700_000_001_000,
    finishedAt: null,
    createdAt: 1_700_000_000_000,
    analysisProgress: 0,
    analysisGoal: '判断进入机会和评价风险',
    seedAsins: ['B012345678'],
    collectionConfig: createDefaultCollectionGraphConfig(),
    ...overrides,
  }
}

function event(
  sequenceNo: number,
  eventType: string,
  overrides: Partial<ResearchStreamEvent> = {},
): ResearchStreamEvent {
  const workflowEvent = [
    'product_selection_required',
    'product_selection_submitted',
    'stage_completed',
    'market_abandoned',
    'workflow_completed',
  ].includes(eventType)
  const terminalEvent = ['market_abandoned', 'workflow_completed'].includes(eventType)
  return {
    eventId: `event-${sequenceNo}`,
    sequenceNo,
    jobId: 'job-research-1',
    scope: workflowEvent ? 'workflow' : 'research',
    eventType,
    message: eventType,
    terminal: terminalEvent,
    createdAt: 1_700_000_000_000 + sequenceNo,
    ...overrides,
  }
}

function nodeExecution(overrides: Partial<ResearchNodeExecution> = {}): ResearchNodeExecution {
  return {
    executionId: 'execution-1',
    graphCode: 'collection',
    nodeCode: 'collection.collectProducts',
    nodeName: '采集商品池',
    jobAttempt: 1,
    nodeAttempt: 1,
    status: 'SUCCEEDED',
    startedAt: 1_700_000_000_000,
    finishedAt: 1_700_000_001_000,
    durationMs: 1_000,
    errorCode: null,
    errorMessage: null,
    ...overrides,
  }
}

function pushSnapshot(
  connection: StreamConnection | undefined,
  job: ResearchJobDetail,
  events: ResearchStreamEvent[] = [event(1, 'research_started')],
  lastSequence = Math.max(0, ...events.map((item) => item.sequenceNo)),
  nodes: ResearchNodeExecution[] = [],
) {
  if (!connection) throw new Error('expected an open research stream')
  connection.options.onFrame({
    frameType: 'snapshot',
    jobId: connection.jobId,
    afterSequence: connection.afterSequence,
    lastSequence,
    replayComplete: true,
    job,
    nodes,
    events: events.map((item) => ({ ...item, jobId: connection.jobId })),
  })
}

function pushEvents(
  connection: StreamConnection | undefined,
  job: ResearchJobDetail,
  events: ResearchStreamEvent[],
) {
  if (!connection) throw new Error('expected an open research stream')
  connection.options.onFrame({
    frameType: 'events',
    jobId: connection.jobId,
    afterSequence: connection.afterSequence,
    lastSequence: Math.max(connection.afterSequence, ...events.map((item) => item.sequenceNo)),
    replayComplete: true,
    job,
    nodes: [],
    events: events.map((item) => ({ ...item, jobId: connection.jobId })),
  })
}
