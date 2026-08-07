import { flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import * as researchApi from '../api/researchApi'
import * as streamApi from '../api/researchStreamApi'
import type { ResearchJobHistory } from '../model/research'
import ResearchHistoryPage from './ResearchHistoryPage.vue'

const { routerPush } = vi.hoisted(() => ({ routerPush: vi.fn() }))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: routerPush }),
}))

vi.mock('../api/researchApi', () => ({
  pageResearchJobs: vi.fn(),
}))

vi.mock('../api/researchStreamApi', () => ({
  downloadResearchArtifact: vi.fn(),
}))

const historyRow: ResearchJobHistory = {
  jobId: 'job-1',
  reportName: '美国站美容仪市场调研',
  marketplace: 'US',
  nodeIdPath: '172282:281407',
  month: '2026-07',
  keyword: 'facial cleansing device',
  status: 'SUCCEEDED',
  progress: 100,
  analysisRunId: 'analysis-1',
  analysisStatus: 'SUCCEEDED',
  analysisPhase: 'report_generation',
  analysisProgress: 100,
  createdAt: Date.UTC(2026, 6, 29, 2, 30),
  finishedAt: Date.UTC(2026, 6, 29, 2, 45),
  artifacts: [{
    artifactId: 'artifact-1',
    analysisRunId: null,
    artifactType: 'STAGE1_RAW_WORKBOOK',
    fileName: '美国站美容仪阶段一原始数据.xlsx',
    mediaType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    fileSize: 2048,
    createdAt: Date.UTC(2026, 6, 29, 2, 44),
  }, {
    artifactId: 'artifact-2',
    analysisRunId: null,
    artifactType: 'STAGE1_EVIDENCE_WORKBOOK',
    fileName: '美国站美容仪阶段一证据数据.xlsx',
    mediaType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    fileSize: 1536,
    createdAt: Date.UTC(2026, 6, 29, 2, 44),
  }, {
    artifactId: 'artifact-3',
    analysisRunId: null,
    artifactType: 'STAGE2_RAW_WORKBOOK',
    fileName: '美国站美容仪阶段二原始数据.xlsx',
    mediaType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    fileSize: 1024,
    createdAt: Date.UTC(2026, 6, 29, 2, 45),
  }, {
    artifactId: 'artifact-4',
    analysisRunId: null,
    artifactType: 'STAGE2_EVIDENCE_WORKBOOK',
    fileName: '美国站美容仪阶段二证据数据.xlsx',
    mediaType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    fileSize: 768,
    createdAt: Date.UTC(2026, 6, 29, 2, 45),
  }, {
    artifactId: 'artifact-5',
    analysisRunId: 'analysis-1',
    artifactType: 'AI_ANALYSIS_REPORT',
    fileName: '美国站美容仪分析报告.md',
    mediaType: 'text/markdown',
    fileSize: 1024,
    createdAt: Date.UTC(2026, 6, 29, 2, 45),
  }],
}

function mountPage() {
  return mount(ResearchHistoryPage, {
    global: {
      stubs: { Teleport: true },
    },
  })
}

describe('ResearchHistoryPage', () => {
  beforeEach(() => {
    vi.mocked(researchApi.pageResearchJobs).mockReset().mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      records: [historyRow],
    })
    vi.mocked(streamApi.downloadResearchArtifact).mockReset().mockResolvedValue(new Blob(['xlsx']))
    routerPush.mockReset()
    Object.defineProperty(URL, 'createObjectURL', {
      configurable: true,
      value: vi.fn(() => 'blob:history-report'),
    })
    Object.defineProperty(URL, 'revokeObjectURL', {
      configurable: true,
      value: vi.fn(),
    })
    vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined)
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('loads owned reports and only applies keyword search after submission', async () => {
    vi.mocked(researchApi.pageResearchJobs).mockResolvedValue({
      current: 1,
      size: 20,
      total: 45,
      records: [historyRow],
    })
    const wrapper = mountPage()
    await flushPromises()

    expect(researchApi.pageResearchJobs).toHaveBeenNthCalledWith(1, {
      current: 1,
      size: 20,
    })
    expect(wrapper.text()).toContain('美国站美容仪市场调研')
    expect(wrapper.text()).toContain('facial cleansing device')
    expect(wrapper.get('[data-testid="history-graph-job-1-collection"]').text()).toContain('已完成')
    expect(wrapper.get('[data-testid="history-graph-job-1-evidence"]').text()).toContain('已完成')
    expect(wrapper.get('[data-testid="history-graph-job-1-report"]').text()).toContain('已完成')
    expect(wrapper.get('[data-testid="history-artifact-group-job-1-STAGE1_RAW_WORKBOOK"]').text())
      .toContain('阶段一原始数据 Excel')
    expect(wrapper.get('[data-testid="history-artifact-group-job-1-STAGE1_EVIDENCE_WORKBOOK"]').text())
      .toContain('阶段一证据数据 Excel')
    expect(wrapper.get('[data-testid="history-artifact-group-job-1-STAGE2_RAW_WORKBOOK"]').text())
      .toContain('阶段二原始数据 Excel')
    expect(wrapper.get('[data-testid="history-artifact-group-job-1-STAGE2_EVIDENCE_WORKBOOK"]').text())
      .toContain('阶段二证据数据 Excel')
    expect(wrapper.get('[data-testid="history-artifact-group-job-1-AI_ANALYSIS_REPORT"]').text())
      .toContain('AI 分析报告')

    await wrapper.get('[data-testid="history-keyword"]').setValue('  cleansing device  ')
    await wrapper.get('[data-testid="history-status"]').setValue('SUCCEEDED')
    await wrapper.get('[data-testid="history-marketplace"]').setValue('US')
    await wrapper.get('[data-testid="history-month"]').setValue('2026-07')
    expect(researchApi.pageResearchJobs).toHaveBeenCalledOnce()

    await wrapper.get('[data-testid="history-filter-form"]').trigger('submit')
    await flushPromises()

    expect(researchApi.pageResearchJobs).toHaveBeenNthCalledWith(2, {
      current: 1,
      size: 20,
      keyword: 'cleansing device',
      status: 'SUCCEEDED',
      marketplace: 'US',
      month: '2026-07',
    })

    await wrapper.get('[data-testid="history-reset"]').trigger('click')
    await flushPromises()
    expect(researchApi.pageResearchJobs).toHaveBeenNthCalledWith(3, {
      current: 1,
      size: 20,
    })

    await wrapper.get('[data-testid="history-next-page"]').trigger('click')
    await flushPromises()
    expect(researchApi.pageResearchJobs).toHaveBeenNthCalledWith(4, {
      current: 2,
      size: 20,
    })
  })

  it('retries a failed filter from page one without retaining stale rows', async () => {
    vi.mocked(researchApi.pageResearchJobs)
      .mockReset()
      .mockResolvedValueOnce({ current: 1, size: 20, total: 45, records: [historyRow] })
      .mockResolvedValueOnce({ current: 2, size: 20, total: 45, records: [historyRow] })
      .mockRejectedValueOnce(new Error('筛选请求失败'))
      .mockResolvedValueOnce({ current: 1, size: 20, total: 0, records: [] })

    const wrapper = mountPage()
    await flushPromises()
    await wrapper.get('[data-testid="history-next-page"]').trigger('click')
    await flushPromises()

    await wrapper.get('[data-testid="history-keyword"]').setValue('  cleansing device  ')
    await wrapper.get('[data-testid="history-filter-form"]').trigger('submit')
    await flushPromises()

    expect(wrapper.text()).toContain('筛选请求失败')
    expect(wrapper.text()).not.toContain(historyRow.reportName)

    await wrapper.get('[data-testid="history-refresh"]').trigger('click')
    await flushPromises()

    expect(researchApi.pageResearchJobs).toHaveBeenNthCalledWith(4, {
      current: 1,
      size: 20,
      keyword: 'cleansing device',
    })
  })

  it('opens the existing report conversation and downloads a published artifact', async () => {
    const wrapper = mountPage()
    await flushPromises()

    await wrapper.get('[data-testid="view-report-job-1"]').trigger('click')
    expect(routerPush).toHaveBeenCalledWith({
      path: '/research/market-report',
      query: { jobId: 'job-1', view: 'conversation', section: 'report' },
    })

    await wrapper.get('[data-testid="download-artifact-artifact-1"]').trigger('click')
    await flushPromises()

    expect(streamApi.downloadResearchArtifact).toHaveBeenCalledWith('job-1', historyRow.artifacts[0])
    expect(URL.createObjectURL).toHaveBeenCalled()
    expect(HTMLAnchorElement.prototype.click).toHaveBeenCalled()
  })

  it('opens a clean market research page from the primary create action', async () => {
    const wrapper = mountPage()
    await flushPromises()

    const createButton = wrapper.get('[data-testid="history-create-job"]')
    expect(createButton.classes()).toContain('el-button--primary')
    expect(wrapper.get('[data-testid="history-refresh"]').classes())
      .not.toContain('el-button--primary')

    await createButton.trigger('click')

    expect(routerPush).toHaveBeenCalledWith({ path: '/research/market-report' })
  })

  it('keeps completed upstream artifacts available when the report graph fails', async () => {
    vi.mocked(researchApi.pageResearchJobs).mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      records: [{
        ...historyRow,
        status: 'FAILED',
        progress: 90,
        artifacts: historyRow.artifacts.filter((artifact) => (
          artifact.artifactType !== 'AI_ANALYSIS_REPORT'
        )),
      }],
    })

    const wrapper = mountPage()
    await flushPromises()

    expect(wrapper.get('[data-testid="history-graph-job-1-collection"]').text()).toContain('已完成')
    expect(wrapper.get('[data-testid="history-graph-job-1-evidence"]').text()).toContain('已完成')
    expect(wrapper.get('[data-testid="history-graph-job-1-report"]').text()).toContain('失败')
    expect(wrapper.get('[data-testid="history-artifact-group-job-1-STAGE1_RAW_WORKBOOK"]').text())
      .toContain('美国站美容仪阶段一原始数据.xlsx')
    expect(wrapper.get('[data-testid="history-artifact-group-job-1-STAGE1_EVIDENCE_WORKBOOK"]').text())
      .toContain('美国站美容仪阶段一证据数据.xlsx')
    expect(wrapper.get('[data-testid="history-artifact-group-job-1-STAGE2_RAW_WORKBOOK"]').text())
      .toContain('美国站美容仪阶段二原始数据.xlsx')
    expect(wrapper.get('[data-testid="history-artifact-group-job-1-STAGE2_EVIDENCE_WORKBOOK"]').text())
      .toContain('美国站美容仪阶段二证据数据.xlsx')
    expect(wrapper.get('[data-testid="history-artifact-group-job-1-AI_ANALYSIS_REPORT"]').text())
      .toContain('未发布')
  })

  it('translates the report phase without exposing internal enums', async () => {
    vi.mocked(researchApi.pageResearchJobs).mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      records: [{
        ...historyRow,
        analysisStatus: 'WAITING_RESEARCH',
        analysisPhase: 'waiting_research',
        analysisProgress: 0,
      }],
    })

    const wrapper = mountPage()
    await flushPromises()

    expect(wrapper.text()).toMatch(/等待市场数据\s+· 0%/)
    expect(wrapper.text()).not.toContain('WAITING_RESEARCH')
  })

  it('translates a failed analysis phase without exposing the persisted value', async () => {
    vi.mocked(researchApi.pageResearchJobs).mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      records: [{
        ...historyRow,
        analysisStatus: 'FAILED',
        analysisPhase: 'failed',
        analysisProgress: 0,
      }],
    })

    const wrapper = mountPage()
    await flushPromises()

    expect(wrapper.text()).toContain('分析失败')
    expect(wrapper.text()).not.toContain('failed')
  })

  it('translates the persisted plan phase', async () => {
    vi.mocked(researchApi.pageResearchJobs).mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      records: [{
        ...historyRow,
        analysisStatus: 'RUNNING',
        analysisPhase: 'plan',
        analysisProgress: 5,
      }],
    })

    const wrapper = mountPage()
    await flushPromises()

    expect(wrapper.text()).toMatch(/制定分析计划\s+· 5%/)
    expect(wrapper.text()).not.toContain('plan')
  })

  it('shows a recoverable full-page error when the initial request fails', async () => {
    vi.mocked(researchApi.pageResearchJobs).mockRejectedValueOnce(new Error('服务暂时不可用'))

    const wrapper = mountPage()
    await flushPromises()

    expect(wrapper.text()).toContain('历史报告加载失败')
    expect(wrapper.text()).toContain('服务暂时不可用')
    expect(wrapper.text()).toContain('重新加载')
  })
})
