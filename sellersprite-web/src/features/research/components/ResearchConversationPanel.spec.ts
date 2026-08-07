import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

import * as streamApi from '../api/researchStreamApi'
import type {
  ResearchAnalysisRun,
  ResearchJobDetail,
  ResearchJobStatus,
} from '../model/research'
import type { ResearchStreamEvent } from '../model/researchStream'
import { useResearchAgentStore } from '../stores/useResearchAgentStore'
import ResearchConversationPanel from './ResearchConversationPanel.vue'

vi.mock('../api/researchStreamApi', () => ({
  cancelResearchAnalysis: vi.fn(),
  continueResearchAnalysis: vi.fn(),
  downloadResearchArtifact: vi.fn(),
  listResearchAnalysisRuns: vi.fn(),
  retryResearchAnalysis: vi.fn(),
  sendResearchMessage: vi.fn(),
}))

describe('ResearchConversationPanel', () => {
  beforeEach(() => {
    vi.mocked(streamApi.cancelResearchAnalysis).mockReset().mockResolvedValue(undefined)
    vi.mocked(streamApi.continueResearchAnalysis).mockReset()
    vi.mocked(streamApi.downloadResearchArtifact).mockReset()
    vi.mocked(streamApi.listResearchAnalysisRuns).mockReset().mockResolvedValue([])
    vi.mocked(streamApi.retryResearchAnalysis).mockReset()
    vi.mocked(streamApi.sendResearchMessage).mockReset()
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.restoreAllMocks()
  })

  it('restores FOLLOW_UP history below the formal report without local user events', async () => {
    vi.mocked(streamApi.listResearchAnalysisRuns).mockResolvedValue([
      analysisRun({
        analysisRunId: 'run-follow-up',
        analysisGoal: '退货风险是否可控？',
        status: 'SUCCEEDED',
        finalSummary: '退货风险中等，应先验证尺寸描述和包装破损问题。',
        finishedAt: 1_700_000_050_000,
      }),
    ])
    const { wrapper } = mountPanel([
      event(1, 'summary', {
        scope: 'analysis',
        analysisRunId: 'run-initial',
        message: '## 正式报告结论',
      }),
      event(2, 'summary', {
        scope: 'analysis',
        analysisRunId: 'run-follow-up',
        message: '退货风险中等，应先验证尺寸描述和包装破损问题。',
      }),
    ], 'SUCCEEDED', false, 'report', true)

    await flushPromises()

    expect(wrapper.text()).toContain('正式报告结论')
    expect(wrapper.text()).toContain('报告问答')
    expect(wrapper.get('[data-testid="research-follow-up-turn"]').text())
      .toContain('退货风险是否可控？')
    expect(wrapper.get('[data-testid="research-follow-up-turn"]').text())
      .toContain('退货风险中等，应先验证尺寸描述和包装破损问题。')
    expect(wrapper.findAll('[data-testid="research-follow-up-turn"]')).toHaveLength(1)
    expect(wrapper.find('[data-testid="research-user-message"]').exists()).toBe(false)
    expect(wrapper.find('[data-testid="research-follow-up-composer"]').exists()).toBe(true)
  })

  it('merges streaming SSE deltas into the matching persisted follow-up run', async () => {
    const running = analysisRun({
      analysisRunId: 'run-streaming',
      analysisGoal: '只看利润空间',
      status: 'RUNNING',
    })
    const succeeded = analysisRun({
      ...running,
      status: 'SUCCEEDED',
      finalSummary: '利润空间偏紧，建议先验证广告成本。',
      finishedAt: 1_700_000_060_000,
    })
    vi.mocked(streamApi.listResearchAnalysisRuns)
      .mockResolvedValueOnce([running])
      .mockResolvedValue([succeeded])
    const { wrapper, store } = mountPanel([], 'SUCCEEDED', false, 'report', true)
    await flushPromises()

    store.appendEvent(event(10, 'summary_delta', {
      scope: 'analysis',
      analysisRunId: 'run-streaming',
      data: '利润空间偏紧，',
    }))
    store.appendEvent(event(11, 'summary_delta', {
      scope: 'analysis',
      analysisRunId: 'run-streaming',
      data: '建议先验证广告成本。',
    }))
    await nextTick()
    await nextTick()

    expect(wrapper.get('[data-testid="research-follow-up-turn"]').text())
      .toContain('利润空间偏紧，建议先验证广告成本。')
    expect(wrapper.findAll('[data-testid="research-follow-up-turn"]')).toHaveLength(1)

    store.appendEvent(event(12, 'summary', {
      scope: 'analysis',
      analysisRunId: 'run-streaming',
      message: '利润空间偏紧，建议先验证广告成本。',
    }))
    store.appendEvent(event(13, 'done', {
      scope: 'analysis',
      analysisRunId: 'run-streaming',
      data: { analysisStatus: 'SUCCEEDED' },
      terminal: true,
    }))
    await flushPromises()

    expect(wrapper.get('[data-testid="research-follow-up-turn"]').text()).toContain('已回答')
    expect(wrapper.findAll('[data-testid="research-follow-up-turn"]')).toHaveLength(1)
  })

  it('optimistically inserts the submitted run and keeps the answer in the report timeline', async () => {
    const queued = analysisRun({
      analysisRunId: 'run-new',
      analysisGoal: '差评主要来自哪里？',
      status: 'QUEUED',
    })
    const succeeded = analysisRun({
      ...queued,
      status: 'SUCCEEDED',
      finalSummary: '差评主要集中在包装破损和尺寸描述偏差。',
      finishedAt: 1_700_000_080_000,
    })
    vi.mocked(streamApi.listResearchAnalysisRuns)
      .mockResolvedValueOnce([])
      .mockResolvedValue([succeeded])
    vi.mocked(streamApi.sendResearchMessage).mockResolvedValue(queued)
    const { wrapper, store } = mountPanel([], 'SUCCEEDED', false, 'report', true)
    await flushPromises()
    const timeline = wrapper.get('.research-agent__timeline').element as HTMLElement
    Object.defineProperty(timeline, 'scrollHeight', { configurable: true, value: 640 })

    await wrapper.get('textarea[aria-label="继续追问"]').setValue('差评主要来自哪里？')
    await wrapper.get('[data-testid="research-follow-up-composer"]').trigger('submit')
    await flushPromises()

    expect(streamApi.sendResearchMessage).toHaveBeenCalledWith('job-1', '差评主要来自哪里？')
    expect(wrapper.get('[data-follow-up-run-id="run-new"]').text()).toContain('差评主要来自哪里？')
    expect(wrapper.get('[data-follow-up-run-id="run-new"]').text()).toContain('正在回答')
    expect(wrapper.emitted('resume')).toHaveLength(1)
    expect(timeline.scrollTop).toBe(640)

    store.appendEvent(event(30, 'summary', {
      scope: 'analysis',
      analysisRunId: 'run-new',
      message: succeeded.finalSummary!,
    }))
    store.appendEvent(event(31, 'done', {
      scope: 'analysis',
      analysisRunId: 'run-new',
      data: { analysisStatus: 'SUCCEEDED' },
      terminal: true,
    }))
    await flushPromises()

    expect(wrapper.findAll('[data-testid="research-follow-up-turn"]')).toHaveLength(1)
    expect(wrapper.get('[data-follow-up-run-id="run-new"]').text())
      .toContain('差评主要集中在包装破损和尺寸描述偏差。')
  })

  it('renders a failed follow-up in place and retries that exact run', async () => {
    const failed = analysisRun({
      analysisRunId: 'run-failed',
      analysisGoal: '库存风险如何？',
      status: 'FAILED',
      errorCode: 'MODEL_TIMEOUT',
      errorMessage: '模型响应超时',
      retryable: true,
    })
    vi.mocked(streamApi.listResearchAnalysisRuns).mockResolvedValue([failed])
    vi.mocked(streamApi.retryResearchAnalysis).mockResolvedValue(analysisRun({
      analysisRunId: 'run-retry',
      parentRunId: 'run-failed',
      analysisGoal: failed.analysisGoal,
      status: 'QUEUED',
    }))
    const { wrapper } = mountPanel([], 'SUCCEEDED', false, 'report', true)
    await flushPromises()

    expect(wrapper.get('[data-follow-up-run-id="run-failed"]').text()).toContain('模型响应超时')
    await wrapper.get('[data-testid="retry-research-follow-up-run-failed"]').trigger('click')
    await flushPromises()

    expect(streamApi.retryResearchAnalysis).toHaveBeenCalledWith('run-failed')
    expect(wrapper.emitted('resume')).toHaveLength(1)
    expect(wrapper.findAll('[data-testid="research-follow-up-turn"]')).toHaveLength(1)
    expect(wrapper.find('[data-follow-up-run-id="run-failed"]').exists()).toBe(false)
    expect(wrapper.find('[data-follow-up-run-id="run-retry"]').exists()).toBe(true)
  })

  it('shows process events without report questions or the composer', async () => {
    vi.mocked(streamApi.listResearchAnalysisRuns).mockResolvedValue([
      analysisRun({ analysisRunId: 'run-follow-up', analysisGoal: '追问问题' }),
    ])
    const { wrapper } = mountPanel([
      event(20, 'plan', {
        scope: 'analysis',
        analysisRunId: 'run-follow-up',
        message: '追问执行过程',
      }),
      event(21, 'summary_delta', {
        scope: 'analysis',
        analysisRunId: 'run-follow-up',
        data: '流式回答',
      }),
    ], 'SUCCEEDED', false, 'process', true)
    await flushPromises()

    expect(wrapper.text()).toContain('追问执行过程')
    expect(wrapper.text()).toContain('流式回答')
    expect(wrapper.text()).not.toContain('报告问答')
    expect(wrapper.find('[data-testid="research-follow-up-composer"]').exists()).toBe(false)
  })

  it('auto-follows new events until the user scrolls away and resumes at the bottom', async () => {
    const { wrapper, store } = mountPanel()
    await flushPromises()
    const timeline = wrapper.get('.research-agent__timeline').element as HTMLElement
    let scrollHeight = 500
    Object.defineProperty(timeline, 'scrollHeight', {
      configurable: true,
      get: () => scrollHeight,
    })
    Object.defineProperty(timeline, 'clientHeight', { configurable: true, value: 100 })

    store.appendEvent(event(1, 'research_node_completed'))
    await nextTick()
    await nextTick()
    expect(timeline.scrollTop).toBe(500)

    scrollHeight = 700
    await wrapper.get('.research-agent__timeline').trigger('scroll')
    store.appendEvent(event(2, 'research_node_completed'))
    await nextTick()
    await nextTick()
    expect(timeline.scrollTop).toBe(700)

    timeline.scrollTop = 120
    await wrapper.get('.research-agent__timeline').trigger('scroll')
    scrollHeight = 800
    store.appendEvent(event(3, 'research_node_completed'))
    await nextTick()
    await nextTick()
    expect(timeline.scrollTop).toBe(120)

    timeline.scrollTop = 700
    await wrapper.get('.research-agent__timeline').trigger('scroll')
    scrollHeight = 900
    store.appendEvent(event(4, 'research_node_completed'))
    await nextTick()
    await nextTick()
    expect(timeline.scrollTop).toBe(900)
  })

  it('keeps active-event scrolling under the parent timeline follow state', async () => {
    const scrollIntoView = vi.fn(function (this: HTMLElement) {
      const timeline = this.closest('.research-agent__timeline') as HTMLElement | null
      if (timeline) timeline.scrollTop = 450
    })
    Object.defineProperty(HTMLElement.prototype, 'scrollIntoView', {
      configurable: true,
      value: scrollIntoView,
    })
    const { wrapper, store } = mountPanel(
      [event(1, 'research_node_completed')],
      'RUNNING',
      false,
      'process',
      true,
      'event-1',
    )
    await flushPromises()
    scrollIntoView.mockClear()
    const timeline = wrapper.get('.research-agent__timeline').element as HTMLElement
    let scrollHeight = 600
    Object.defineProperty(timeline, 'scrollHeight', {
      configurable: true,
      get: () => scrollHeight,
    })
    Object.defineProperty(timeline, 'clientHeight', { configurable: true, value: 100 })

    timeline.scrollTop = 500
    await wrapper.get('.research-agent__timeline').trigger('scroll')
    timeline.scrollTop = 200
    await wrapper.get('.research-agent__timeline').trigger('scroll')
    scrollHeight = 700
    store.appendEvent(event(2, 'research_node_completed'))
    await wrapper.setProps({ activeEventId: 'event-2' })
    await nextTick()
    await nextTick()

    expect(timeline.scrollTop).toBe(200)
    expect(scrollIntoView).not.toHaveBeenCalled()

    timeline.scrollTop = 600
    await wrapper.get('.research-agent__timeline').trigger('scroll')
    scrollHeight = 800
    store.appendEvent(event(3, 'research_node_completed'))
    await wrapper.setProps({ activeEventId: 'event-3' })
    await nextTick()
    await nextTick()
    expect((scrollIntoView.mock.instances.at(-1) as HTMLElement).dataset.eventId).toBe('event-3')
  })

  it('delegates reconnect and active-run cancellation to the page stream owner', async () => {
    const { wrapper, store } = mountPanel([
      event(7, 'plan', { scope: 'analysis', analysisRunId: 'run-active' }),
    ], 'SUCCEEDED')
    store.markDisconnected('事件流已关闭（从 #7 继续）')
    await nextTick()

    await wrapper.get('[data-testid="reconnect-research-stream"]').trigger('click')
    await wrapper.get('[data-testid="cancel-research-analysis"]').trigger('click')
    await flushPromises()

    expect(wrapper.emitted('resume')).toHaveLength(1)
    expect(streamApi.cancelResearchAnalysis).toHaveBeenCalledWith('run-active')
  })

  it('fills its parent when the page enables focus mode', () => {
    const { wrapper } = mountPanel([], 'RUNNING', true)

    expect(wrapper.get('[data-testid="research-conversation-panel"]').classes())
      .toContain('research-agent--focus')
  })
})

function mountPanel(
  events: ResearchStreamEvent[] = [],
  status: ResearchJobStatus = 'RUNNING',
  focusMode = false,
  section: 'all' | 'report' | 'process' = 'all',
  workspace = false,
  activeEventId = '',
) {
  const pinia = createPinia()
  setActivePinia(pinia)
  const store = useResearchAgentStore()
  store.startJob('job-1')
  store.applyFrame({
    frameType: 'snapshot',
    jobId: 'job-1',
    afterSequence: 0,
    lastSequence: Math.max(0, ...events.map((item) => item.sequenceNo)),
    replayComplete: true,
    job: researchJob(status),
    nodes: [],
    events,
  })
  const wrapper = mount(ResearchConversationPanel, {
    props: { jobId: 'job-1', jobStatus: status, focusMode, section, workspace, activeEventId },
    global: { plugins: [pinia] },
  })
  return { wrapper, store }
}

function analysisRun(overrides: Partial<ResearchAnalysisRun> = {}): ResearchAnalysisRun {
  return {
    analysisRunId: 'run-follow-up',
    jobId: 'job-1',
    conversationId: 'conversation-1',
    parentRunId: 'run-initial',
    runType: 'FOLLOW_UP',
    stageCode: 'FINAL_ANALYSIS',
    analysisGoal: '继续追问',
    status: 'RUNNING',
    currentPhase: 'summary',
    progress: 50,
    attemptCount: 1,
    maxAttempts: 3,
    nextRunAt: null,
    leaseUntil: null,
    heartbeatAt: null,
    cancelRequestedAt: null,
    modelCallCount: 1,
    eventCount: 2,
    finalSummary: null,
    errorCode: null,
    errorMessage: null,
    startedAt: 1_700_000_010_000,
    finishedAt: null,
    createdAt: 1_700_000_000_000,
    cancellable: true,
    retryable: false,
    ...overrides,
  }
}

function researchJob(status: ResearchJobStatus): ResearchJobDetail {
  return {
    jobId: 'job-1',
    reportName: '测试报告',
    marketplace: 'US',
    nodeIdPath: '1:2',
    month: '2026-07',
    keyword: null,
    dataSourceMode: 'MOCK',
    workflowVersion: 'market-research-v6-cache-insights',
    status,
    currentStage: status === 'WAITING_INPUT' ? 'PRODUCT_SELECTION' : 'SCREENING',
    waitingInputType: status === 'WAITING_INPUT' ? 'PRODUCT_SELECTION' : null,
    currentNode: status === 'SUCCEEDED' ? 'publishReport' : 'validate',
    currentNodeName: '校验',
    progress: status === 'SUCCEEDED' ? 100 : 10,
    attemptCount: 1,
    maxAttempts: 3,
    remainingAttempts: 2,
    nextRunAt: null,
    leaseUntil: null,
    heartbeatAt: null,
    cancelRequestedAt: null,
    cancellable: status === 'RUNNING',
    retryable: false,
    errorCode: null,
    errorMessage: null,
    startedAt: null,
    finishedAt: status === 'SUCCEEDED' ? Date.now() : null,
    createdAt: Date.now(),
  }
}

function event(
  sequenceNo: number,
  eventType: string,
  overrides: Partial<ResearchStreamEvent> = {},
): ResearchStreamEvent {
  return {
    eventId: `event-${sequenceNo}`,
    sequenceNo,
    jobId: 'job-1',
    scope: 'research',
    eventType,
    message: eventType,
    terminal: false,
    createdAt: 1_700_000_000_000 + sequenceNo,
    ...overrides,
  }
}
