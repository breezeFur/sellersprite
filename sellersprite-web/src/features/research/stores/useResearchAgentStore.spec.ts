import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'

import type { ResearchJobDetail } from '../model/research'
import type { ResearchStreamEvent } from '../model/researchStream'
import { useResearchAgentStore } from './useResearchAgentStore'

describe('useResearchAgentStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('hydrates ordered history, ignores replayed sequences and tracks the resume cursor', () => {
    const store = useResearchAgentStore()
    store.startJob('job-1')

    store.hydrate([
      event(2, 'research_node_completed', { scope: 'research' }),
      event(1, 'research_started', { scope: 'RESEARCH' }),
    ])
    store.appendEvent(event(2, 'research_node_completed', { scope: 'research' }))

    expect(store.events.map((item) => item.sequenceNo)).toEqual([1, 2])
    expect(store.events[0].scope).toBe('research')
    expect(store.lastSequence).toBe(2)
  })

  it('keeps a workspace dismissal across same-job remounts and clears it for a new job', () => {
    const store = useResearchAgentStore()
    store.startJob('job-1')
    store.dismissWorkspace('job-1', 'analysis-1')

    store.startJob('job-1')
    expect(store.workspaceDismissedJobId).toBe('job-1')
    expect(store.workspaceDismissedRunId).toBe('analysis-1')

    store.startJob('job-2')
    expect(store.workspaceDismissedJobId).toBe('')
    expect(store.workspaceDismissedRunId).toBe('')
  })

  it('applies an authoritative snapshot and later aggregate frames atomically', () => {
    const store = useResearchAgentStore()
    store.startJob('job-1')

    store.applyFrame({
      frameType: 'snapshot',
      jobId: 'job-1',
      afterSequence: 0,
      lastSequence: 2,
      replayComplete: true,
      job: job(),
      nodes: [node('execution-1', 'RUNNING')],
      events: [
        event(2, 'research_node_started'),
        event(1, 'research_started'),
      ],
    })

    expect(store.job?.status).toBe('RUNNING')
    expect(store.nodes).toEqual([expect.objectContaining({ executionId: 'execution-1' })])
    expect(store.events.map((item) => item.sequenceNo).filter(Boolean)).toEqual([1, 2])
    expect(store.lastSequence).toBe(2)
    expect(store.historyLoading).toBe(false)

    store.applyFrame({
      frameType: 'events',
      jobId: 'job-1',
      afterSequence: 2,
      lastSequence: 3,
      replayComplete: true,
      job: job({ status: 'SUCCEEDED', progress: 100 }),
      nodes: [node('execution-1', 'SUCCEEDED')],
      events: [event(3, 'workflow_completed', { scope: 'workflow', terminal: true })],
    })

    expect(store.job?.status).toBe('SUCCEEDED')
    expect(store.nodes[0]?.status).toBe('SUCCEEDED')
    expect(store.lastSequence).toBe(3)
    expect(store.workflowTerminal?.sequenceNo).toBe(3)
  })

  it('aggregates summary deltas per analysis run and replaces them with the final summary', () => {
    const store = useResearchAgentStore()
    store.startJob('job-1')

    store.appendEvent(event(3, 'summary_delta', {
      scope: 'analysis',
      analysisRunId: 'run-1',
      message: '第一段',
      data: '第一段',
    }))
    store.appendEvent(event(4, 'summary_delta', {
      scope: 'analysis',
      analysisRunId: 'run-1',
      message: '第二段',
      data: '第二段',
    }))

    expect(store.events).toHaveLength(1)
    expect(store.events[0]).toMatchObject({
      eventType: 'summary',
      message: '第一段第二段',
      streaming: true,
    })

    store.appendEvent(event(5, 'summary', {
      scope: 'analysis',
      analysisRunId: 'run-1',
      message: '最终结论',
    }))

    expect(store.events).toHaveLength(1)
    expect(store.events[0]).toMatchObject({
      sequenceNo: 5,
      message: '最终结论',
    })
    expect(store.events[0].streaming).toBeUndefined()
  })

  it('aggregates replayable report charts by run and chart code', () => {
    const store = useResearchAgentStore()
    store.startJob('job-1')
    const chart = {
      chartCode: 'market-sales-volume',
      sectionCode: 'market-sales-trend',
      sectionTitle: '行业销售趋势',
      type: 'LINE',
      title: '行业月销量趋势',
      categories: ['2026-01', '2026-02'],
      series: [{ name: '销量', values: [1200, 1500] }],
    }

    store.appendEvent(event(6, 'report_chart', {
      scope: 'analysis',
      analysisRunId: 'run-1',
      payload: { data: chart },
    }))
    store.appendEvent(event(7, 'report_chart', {
      scope: 'analysis',
      analysisRunId: 'run-1',
      payload: { data: { ...chart, series: [{ name: '销量', values: [1200, 1600] }] } },
    }))

    expect(store.reportChartList).toHaveLength(1)
    expect(store.reportChartList[0]?.series[0]?.values).toEqual([1200, 1600])
  })

  it('unwraps persisted Agent delta content from payload data', () => {
    const store = useResearchAgentStore()
    store.startJob('job-1')

    store.appendEvent(event(3, 'summary_delta', {
      scope: 'analysis',
      analysisRunId: 'run-1',
      message: '模型正在输出总结',
      payload: { stepIndex: 3, data: '真实增量' },
    }))

    expect(store.events).toHaveLength(1)
    expect(store.events[0]?.message).toBe('真实增量')
    expect(store.events[0]?.data).toBe('真实增量')
  })

  it('keeps stage and dataset metadata while unwrapping persisted Agent data', () => {
    const store = useResearchAgentStore()
    store.startJob('job-1')

    store.appendEvent(event(4, 'sheet_think_delta', {
      scope: 'analysis',
      analysisRunId: 'run-screening',
      sheetName: 'US',
      payload: {
        stageCode: 'SCREENING',
        datasetCode: 'evidence.products',
        data: '市场容量',
      },
    }))

    expect(store.events[0]).toMatchObject({
      stageCode: 'SCREENING',
      datasetCode: 'evidence.products',
      data: '市场容量',
    })
  })

  it('aggregates sheet think deltas independently for each sheet and finalizes in place', () => {
    const store = useResearchAgentStore()
    store.startJob('job-1')

    store.appendEvent(event(6, 'sheet_think_delta', {
      scope: 'analysis',
      analysisRunId: 'run-1',
      sheetName: 'US',
      data: '容量',
    }))
    store.appendEvent(event(7, 'sheet_think_delta', {
      scope: 'analysis',
      analysisRunId: 'run-1',
      sheetName: 'US',
      data: '增长',
    }))

    expect(store.events).toHaveLength(1)
    expect(store.events[0].data).toBe('容量增长')

    store.appendEvent(event(8, 'sheet_think', {
      scope: 'analysis',
      analysisRunId: 'run-1',
      sheetName: 'US',
      message: '模型完成 US 判断',
      data: '最终判断',
    }))

    expect(store.events).toHaveLength(1)
    expect(store.events[0]).toMatchObject({
      sequenceNo: 8,
      message: '模型完成 US 判断',
      data: '最终判断',
    })
  })

  it('does not treat analysis done as the workflow terminal', () => {
    const store = useResearchAgentStore()
    store.startJob('job-1')
    store.markConnected()

    store.appendEvent(event(9, 'done', {
      scope: 'analysis',
      analysisRunId: 'run-1',
      terminal: true,
    }))

    expect(store.analysisState).toBe('SUCCEEDED')
    expect(store.workflowTerminal).toBeNull()
    expect(store.streaming).toBe(true)

    store.appendEvent(event(10, 'workflow_completed', {
      scope: 'workflow',
      terminal: true,
    }))

    expect(store.workflowTerminal?.eventType).toBe('workflow_completed')
    expect(store.streaming).toBe(true)
  })

  it('keeps analysis succeeded when stage completed follows done', () => {
    const store = useResearchAgentStore()
    store.startJob('job-1')

    store.appendEvent(event(9, 'done', {
      scope: 'analysis',
      analysisRunId: 'run-1',
      data: { analysisStatus: 'SUCCEEDED' },
    }))
    store.appendEvent(event(10, 'stage_completed', {
      scope: 'analysis',
      analysisRunId: 'run-1',
      stageCode: 'SCREENING',
    }))

    expect(store.analysisState).toBe('SUCCEEDED')
    expect(store.analysisRunning).toBe(false)
    expect(store.analysisRetryable).toBe(false)
  })

  it('treats market abandoned as a normal workflow terminal', () => {
    const store = useResearchAgentStore()
    store.startJob('job-1')

    store.appendEvent(event(11, 'market_abandoned', {
      scope: 'workflow',
      terminal: true,
      payload: { stageCode: 'PRODUCT_SELECTION' },
    }))

    expect(store.workflowTerminal).toMatchObject({
      eventType: 'market_abandoned',
      stageCode: 'PRODUCT_SELECTION',
    })
  })

  it('keeps failed and cancelled analysis status from the done payload', () => {
    const store = useResearchAgentStore()
    store.startJob('job-1')

    store.appendEvent(event(9, 'done', {
      scope: 'analysis',
      data: { analysisStatus: 'FAILED' },
    }))
    expect(store.analysisState).toBe('FAILED')
    expect(store.analysisRetryable).toBe(true)

    store.appendEvent(event(10, 'done', {
      scope: 'analysis',
      data: { analysisStatus: 'CANCELLED' },
    }))
    expect(store.analysisState).toBe('CANCELLED')
    expect(store.analysisRetryable).toBe(false)
  })

  it('keeps previous turns when a persisted follow-up analysis starts', () => {
    const store = useResearchAgentStore()
    store.startJob('job-1')
    store.appendEvent(event(11, 'summary', {
      scope: 'analysis',
      analysisRunId: 'run-1',
      message: '第一轮结论',
    }))
    store.prepareForNewAnalysis('run-2')

    store.appendEvent(event(12, 'user_message', {
      scope: 'analysis',
      analysisRunId: 'run-2',
      message: '只看退货风险',
    }))

    expect(store.events.filter((item) => item.eventType === 'user_message')).toHaveLength(1)
    expect(store.events.some((item) => item.message === '第一轮结论')).toBe(true)
    expect(store.activeAnalysisRunId).toBe('run-2')
    expect(store.analysisState).toBe('QUEUED')
  })

  it('reopens replay after an older workflow terminal when a persisted follow-up has started', () => {
    const store = useResearchAgentStore()
    store.startJob('job-1')

    store.hydrate([
      event(20, 'done', { scope: 'analysis', analysisRunId: 'run-1', terminal: true }),
      event(21, 'workflow_completed', { scope: 'workflow', terminal: true }),
      event(22, 'user_message', {
        scope: 'analysis',
        analysisRunId: 'run-2',
        message: '继续看退货风险',
      }),
    ])

    expect(store.workflowTerminal).toBeNull()
    expect(store.activeAnalysisRunId).toBe('run-2')
    expect(store.lastSequence).toBe(22)
  })
})

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
    createdAt: Date.now() + sequenceNo,
    ...overrides,
  }
}

function job(overrides: Partial<ResearchJobDetail> = {}): ResearchJobDetail {
  return {
    jobId: 'job-1',
    reportName: '测试报告',
    marketplace: 'US',
    nodeIdPath: '1:2',
    month: '2026-07',
    keyword: null,
    dataSourceMode: 'MOCK',
    workflowVersion: 'market-research-v6-cache-insights',
    status: 'RUNNING',
    currentStage: 'SCREENING',
    waitingInputType: null,
    currentNode: 'validate',
    currentNodeName: '校验',
    progress: 10,
    attemptCount: 1,
    maxAttempts: 3,
    remainingAttempts: 2,
    nextRunAt: null,
    leaseUntil: null,
    heartbeatAt: null,
    cancelRequestedAt: null,
    cancellable: true,
    retryable: false,
    errorCode: null,
    errorMessage: null,
    startedAt: null,
    finishedAt: null,
    createdAt: 1_700_000_000_000,
    ...overrides,
  }
}

function node(executionId: string, status: 'RUNNING' | 'SUCCEEDED') {
  return {
    executionId,
    graphCode: 'collection',
    nodeCode: 'collection.validate',
    nodeName: '校验',
    jobAttempt: 1,
    nodeAttempt: 1,
    status,
    startedAt: 1_700_000_000_000,
    finishedAt: status === 'SUCCEEDED' ? 1_700_000_001_000 : null,
    durationMs: status === 'SUCCEEDED' ? 1_000 : null,
    errorCode: null,
    errorMessage: null,
  }
}
