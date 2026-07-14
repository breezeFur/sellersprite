import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import * as researchApi from '../api/researchApi'
import type { ResearchJobDetail } from '../model/research'
import MarketResearchPage from './MarketResearchPage.vue'

vi.mock('vue-router', () => ({
  useRoute: vi.fn(),
  useRouter: vi.fn(),
}))

vi.mock('../api/researchApi', () => ({
  createResearchJob: vi.fn(),
  getResearchJob: vi.fn(),
  downloadResearchReport: vi.fn(),
}))

function researchJob(overrides: Partial<ResearchJobDetail> = {}): ResearchJobDetail {
  return {
    jobId: 'job-research-1',
    reportName: '美容仪美国站市场调研',
    marketplace: 'US',
    keyword: 'facial cleansing device',
    dataSourceMode: 'MOCK',
    status: 'RUNNING',
    currentPhase: 'COLLECT_MARKET_AND_PRODUCTS',
    currentPhaseName: '采集市场与商品',
    progress: 35,
    batchJobExecutionId: 101,
    errorCode: null,
    errorMessage: null,
    startedAt: 1_700_000_001_000,
    finishedAt: null,
    createdAt: 1_700_000_000_000,
    downloadable: false,
    fileName: null,
    ...overrides,
  }
}

describe('MarketResearchPage', () => {
  const replace = vi.fn()
  let routeState: { query: Record<string, string | undefined> }

  beforeEach(() => {
    vi.useFakeTimers()
    replace.mockReset().mockResolvedValue(undefined)
    routeState = reactive({ query: {} })
    vi.mocked(useRoute).mockReturnValue(routeState as unknown as ReturnType<typeof useRoute>)
    vi.mocked(useRouter).mockReturnValue({ replace } as unknown as ReturnType<typeof useRouter>)
    vi.mocked(researchApi.createResearchJob).mockReset()
    vi.mocked(researchApi.getResearchJob).mockReset()
    vi.mocked(researchApi.downloadResearchReport).mockReset()
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.restoreAllMocks()
  })

  it('creates a job, writes the query and polls until the report succeeds', async () => {
    vi.mocked(researchApi.createResearchJob).mockResolvedValue({
      jobId: 'job-research-1',
      status: 'QUEUED',
      dataSourceMode: 'MOCK',
    })
    vi.mocked(researchApi.getResearchJob)
      .mockResolvedValueOnce(researchJob())
      .mockResolvedValueOnce(researchJob({
        status: 'SUCCEEDED',
        currentPhase: 'VALIDATE_AND_PUBLISH',
        currentPhaseName: '校验并发布',
        progress: 100,
        finishedAt: 1_700_000_100_000,
        downloadable: true,
        fileName: 'market-research.xlsx',
      }))

    const wrapper = mount(MarketResearchPage)
    await wrapper.get('input[aria-label="报告名称"]').setValue('美容仪美国站市场调研')
    await wrapper.get('input[aria-label="核心关键词"]').setValue('facial cleansing device')
    await wrapper.get('textarea[aria-label="种子 ASIN"]').setValue('b0mock0001\nB0MOCK0002')
    await wrapper.get('[data-testid="create-research-job"]').trigger('click')
    await flushPromises()

    expect(researchApi.createResearchJob).toHaveBeenCalledWith({
      reportName: '美容仪美国站市场调研',
      keyword: 'facial cleansing device',
      seedAsins: ['B0MOCK0001', 'B0MOCK0002'],
    })
    expect(replace).toHaveBeenCalledWith({ query: { jobId: 'job-research-1' } })
    expect(researchApi.getResearchJob).toHaveBeenCalledTimes(1)
    expect(wrapper.get('[data-testid="research-job-status"]').text()).toContain('执行中')

    await vi.advanceTimersByTimeAsync(2_000)
    await flushPromises()

    expect(researchApi.getResearchJob).toHaveBeenCalledTimes(2)
    expect(wrapper.get('[data-testid="research-job-status"]').text()).toContain('已完成')
    expect(wrapper.text()).toContain('报告已生成')

    await vi.advanceTimersByTimeAsync(2_000)
    expect(researchApi.getResearchJob).toHaveBeenCalledTimes(2)
  })

  it('locks duplicate creation while the first detail request is pending', async () => {
    let resolveDetail!: (detail: ResearchJobDetail) => void
    vi.mocked(researchApi.createResearchJob).mockResolvedValue({
      jobId: 'job-research-1',
      status: 'QUEUED',
      dataSourceMode: 'MOCK',
    })
    vi.mocked(researchApi.getResearchJob).mockImplementation(() => new Promise((resolve) => {
      resolveDetail = resolve
    }))

    const wrapper = mount(MarketResearchPage)
    await wrapper.get('input[aria-label="报告名称"]').setValue('测试报告')
    await wrapper.get('input[aria-label="核心关键词"]').setValue('test keyword')
    const createButton = wrapper.get('[data-testid="create-research-job"]')

    await createButton.trigger('click')
    await flushPromises()

    expect(researchApi.createResearchJob).toHaveBeenCalledTimes(1)
    expect(researchApi.getResearchJob).toHaveBeenCalledTimes(1)
    expect((createButton.element as HTMLButtonElement).disabled).toBe(true)

    await createButton.trigger('click')
    expect(researchApi.createResearchJob).toHaveBeenCalledTimes(1)

    resolveDetail(researchJob({
      status: 'SUCCEEDED',
      currentPhase: 'VALIDATE_AND_PUBLISH',
      currentPhaseName: '校验并发布',
      progress: 100,
      downloadable: true,
    }))
    await flushPromises()
  })

  it('rejects malformed ASIN text without creating a task', async () => {
    vi.useRealTimers()
    const wrapper = mount(MarketResearchPage)
    await wrapper.get('input[aria-label="报告名称"]').setValue('测试报告')
    await wrapper.get('input[aria-label="核心关键词"]').setValue('test keyword')
    await wrapper.get('textarea[aria-label="种子 ASIN"]').setValue('BAD-ASIN')
    await wrapper.get('[data-testid="create-research-job"]').trigger('click')
    await flushPromises()

    expect(wrapper.get('.el-form-item.is-error').classes()).toContain('is-error')
    expect(researchApi.createResearchJob).not.toHaveBeenCalled()
  })

  it('restores a failed job from the route query and does not poll a terminal state', async () => {
    routeState.query.jobId = 'job-failed-1'
    vi.mocked(researchApi.getResearchJob).mockResolvedValue(researchJob({
      jobId: 'job-failed-1',
      status: 'FAILED',
      currentPhase: 'COLLECT_REVIEWS',
      currentPhaseName: '采集评论',
      progress: 70,
      errorCode: 'RESEARCH_PROVIDER_FAILED',
      errorMessage: '评论数据采集失败',
      finishedAt: 1_700_000_050_000,
    }))

    const wrapper = mount(MarketResearchPage)
    await flushPromises()

    expect(researchApi.getResearchJob).toHaveBeenCalledWith('job-failed-1')
    expect(wrapper.text()).toContain('市场调研任务执行失败')
    expect(wrapper.text()).toContain('RESEARCH_PROVIDER_FAILED：评论数据采集失败')
    expect(vi.getTimerCount()).toBe(0)
  })

  it('downloads a completed report', async () => {
    routeState.query.jobId = 'job-research-1'
    vi.mocked(researchApi.getResearchJob).mockResolvedValue(researchJob({
      status: 'SUCCEEDED',
      currentPhase: 'VALIDATE_AND_PUBLISH',
      currentPhaseName: '校验并发布',
      progress: 100,
      downloadable: true,
      fileName: 'market-research.xlsx',
    }))
    vi.mocked(researchApi.downloadResearchReport).mockResolvedValue(
      new Blob(['xlsx'], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' }),
    )
    const createObjectUrl = vi.fn(() => 'blob:market-research')
    const revokeObjectUrl = vi.fn()
    Object.defineProperty(URL, 'createObjectURL', { configurable: true, value: createObjectUrl })
    Object.defineProperty(URL, 'revokeObjectURL', { configurable: true, value: revokeObjectUrl })
    const linkClick = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})

    const wrapper = mount(MarketResearchPage)
    await flushPromises()
    await wrapper.get('[data-testid="download-research-report"]').trigger('click')
    await flushPromises()

    expect(researchApi.downloadResearchReport).toHaveBeenCalledWith('job-research-1')
    expect(createObjectUrl).toHaveBeenCalledTimes(1)
    expect(linkClick).toHaveBeenCalledTimes(1)
    expect(revokeObjectUrl).not.toHaveBeenCalled()

    await vi.advanceTimersByTimeAsync(1_000)
    expect(revokeObjectUrl).toHaveBeenCalledWith('blob:market-research')
  })

  it('clears a running poll when the page unmounts', async () => {
    routeState.query.jobId = 'job-research-1'
    vi.mocked(researchApi.getResearchJob).mockResolvedValue(researchJob())

    const wrapper = mount(MarketResearchPage)
    await flushPromises()
    expect(researchApi.getResearchJob).toHaveBeenCalledTimes(1)

    wrapper.unmount()
    await vi.advanceTimersByTimeAsync(2_000)

    expect(researchApi.getResearchJob).toHaveBeenCalledTimes(1)
  })

  it('switches jobs when the route query changes and returns to the create state when cleared', async () => {
    routeState.query.jobId = 'job-research-1'
    vi.mocked(researchApi.getResearchJob)
      .mockResolvedValueOnce(researchJob())
      .mockResolvedValueOnce(researchJob({
        jobId: 'job-research-2',
        status: 'FAILED',
        currentPhase: 'COLLECT_KEYWORDS',
        currentPhaseName: '采集关键词',
        progress: 55,
        errorCode: 'KEYWORD_COLLECTION_FAILED',
        errorMessage: '关键词采集失败',
      }))

    const wrapper = mount(MarketResearchPage)
    await flushPromises()
    expect(researchApi.getResearchJob).toHaveBeenLastCalledWith('job-research-1')

    routeState.query.jobId = 'job-research-2'
    await nextTick()
    await flushPromises()

    expect(researchApi.getResearchJob).toHaveBeenCalledTimes(2)
    expect(researchApi.getResearchJob).toHaveBeenLastCalledWith('job-research-2')
    expect(wrapper.text()).toContain('KEYWORD_COLLECTION_FAILED：关键词采集失败')

    await vi.advanceTimersByTimeAsync(2_000)
    expect(researchApi.getResearchJob).toHaveBeenCalledTimes(2)

    routeState.query.jobId = undefined
    await nextTick()

    expect(wrapper.text()).toContain('尚未创建调研任务')
  })
})
