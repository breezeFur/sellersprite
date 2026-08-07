import { expect, test } from '@playwright/test'

const success = <T>(data: T) => ({ code: '00000', message: '操作成功', data })

const workflowSteps = [
  { code: 'SCREENING', nodeCode: 'screeningGraph', label: '阶段一：市场初筛' },
  { code: 'PRODUCT_SELECTION', nodeCode: 'productSelectionGate', label: '商品选择' },
  { code: 'DEEP_DIVE', nodeCode: 'deepDiveGraph', label: '阶段二：商品深挖' },
  { code: 'FINAL_ANALYSIS', nodeCode: 'finalAnalysisGraph', label: '阶段三：最终分析' },
  { code: 'ARTIFACT_FINALIZATION', nodeCode: 'finalizeArtifacts', label: '生成并发布产物' },
]

const historyRecord = {
  jobId: 'job-history-001',
  reportName: '美国站美容仪市场调研',
  marketplace: 'US',
  nodeIdPath: '172282:281407',
  month: '2026-07',
  keyword: 'facial cleansing device',
  status: 'SUCCEEDED',
  progress: 100,
  analysisRunId: 'analysis-history-001',
  analysisStatus: 'WAITING_RESEARCH',
  analysisPhase: 'waiting_research',
  analysisProgress: 0,
  createdAt: Date.UTC(2026, 6, 29, 2, 30),
  finishedAt: Date.UTC(2026, 6, 29, 2, 45),
  artifacts: [{
    artifactId: 'artifact-history-001',
    analysisRunId: null,
    artifactType: 'STAGE1_RAW_WORKBOOK',
    fileName: '美国站美容仪阶段一原始数据.xlsx',
    mediaType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    fileSize: 2048,
    createdAt: Date.UTC(2026, 6, 29, 2, 44),
  }, {
    artifactId: 'artifact-history-002',
    analysisRunId: null,
    artifactType: 'STAGE1_EVIDENCE_WORKBOOK',
    fileName: '美国站美容仪阶段一证据数据.xlsx',
    mediaType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    fileSize: 1536,
    createdAt: Date.UTC(2026, 6, 29, 2, 44),
  }, {
    artifactId: 'artifact-history-003',
    analysisRunId: null,
    artifactType: 'STAGE2_RAW_WORKBOOK',
    fileName: '美国站美容仪阶段二原始数据.xlsx',
    mediaType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    fileSize: 1280,
    createdAt: Date.UTC(2026, 6, 29, 2, 44),
  }, {
    artifactId: 'artifact-history-004',
    analysisRunId: null,
    artifactType: 'STAGE2_EVIDENCE_WORKBOOK',
    fileName: '美国站美容仪阶段二证据数据.xlsx',
    mediaType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    fileSize: 1152,
    createdAt: Date.UTC(2026, 6, 29, 2, 44),
  }, {
    artifactId: 'artifact-history-005',
    analysisRunId: 'analysis-history-001',
    artifactType: 'AI_ANALYSIS_REPORT',
    fileName: '美国站美容仪 AI 分析报告.md',
    mediaType: 'text/markdown',
    fileSize: 1024,
    createdAt: Date.UTC(2026, 6, 29, 2, 45),
  }],
}

const reportSnapshotJob = {
  ...historyRecord,
  dataSourceMode: 'MOCK',
  workflowVersion: 'market-research-v6-cache-insights',
  currentStage: 'ARTIFACT_FINALIZATION',
  waitingInputType: null,
  currentNode: 'report.publishArtifacts',
  currentNodeName: '发布报告产物',
  attemptCount: 1,
  maxAttempts: 3,
  remainingAttempts: 2,
  nextRunAt: null,
  leaseUntil: null,
  heartbeatAt: Date.UTC(2026, 6, 29, 2, 45),
  cancelRequestedAt: null,
  cancellable: false,
  retryable: false,
  errorCode: null,
  errorMessage: null,
  startedAt: Date.UTC(2026, 6, 29, 2, 31),
  analysisGoal: '重点判断差评与退货风险',
  seedAsins: ['B012345678'],
  collectionConfig: {
    collectProducts: {
      productResearch: {},
      pagination: { startPage: 1, pageSize: 100, targetCount: 100 },
      enrichmentAsinLimit: 5,
    },
    collectMarketSalesTrend: { monthCount: 12 },
    collectKeywordDemandTrend: { topN: 100, newProduct: 6 },
    collectSegmentOpportunity: {
      marketResearch: {},
      pagination: { startPage: 1, pageSize: 50, targetCount: 50 },
      distribution: { topN: 100, newProduct: 6, asins: ['B012345678'] },
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
}

const reportSnapshotEvents = [
  {
    eventId: 'history-analysis-plan',
    sequenceNo: 1,
    jobId: historyRecord.jobId,
    analysisRunId: 'analysis-history-001',
    scope: 'analysis',
    eventType: 'plan',
    message: '正在整理市场证据',
    terminal: false,
  },
  {
    eventId: 'history-analysis-summary',
    sequenceNo: 2,
    jobId: historyRecord.jobId,
    analysisRunId: 'analysis-history-001',
    scope: 'analysis',
    eventType: 'summary',
    message: '## 市场结论\n\n该类目需求稳定，进入前需重点控制差评与退货风险。',
    terminal: false,
  },
  {
    eventId: 'history-analysis-done',
    sequenceNo: 3,
    jobId: historyRecord.jobId,
    analysisRunId: 'analysis-history-001',
    scope: 'analysis',
    eventType: 'done',
    message: '初次分析完成',
    terminal: true,
  },
  {
    eventId: 'history-workflow-completed',
    sequenceNo: 4,
    jobId: historyRecord.jobId,
    scope: 'workflow',
    eventType: 'workflow_completed',
    message: '市场调研工作流已完成',
    terminal: true,
  },
]

test('filters, pages, downloads, and opens an owned historical report responsively', async ({ page }, testInfo) => {
  const requestedPages: URL[] = []
  await page.route('**/api/auth/refresh', (route) => route.fulfill({
    json: { code: 'A401', message: '会话已过期', data: null },
  }))
  await page.route('**/api/auth/login', (route) => route.fulfill({
    json: success({
      accessToken: 'history-browser-token',
      tokenType: 'Bearer',
      expiresAt: 1_900_000_000_000,
      permissionVersion: 1,
      user: {
        userId: 'history-user-001',
        username: 'yuanbao',
        nickname: '元宝管理员',
        realName: '',
        avatarUrl: '',
        mobile: null,
        email: null,
        primaryDeptId: null,
        status: 1,
        roleIds: ['role-admin'],
      },
      roles: [{ roleId: 'role-admin', roleCode: 'admin', roleName: '管理员' }],
      menuTree: [
        {
          functionId: 'menu-research-history',
          parentId: '0',
          name: '我的全部历史报告',
          type: 'MENU',
          routePath: '/research/report-history',
          componentPath: 'research/report-history',
          icon: 'Tickets',
          cacheable: 0,
          permissionCode: 'research:report-history:view',
          sortOrder: 28,
          children: [],
        },
        {
          functionId: 'menu-market-report',
          parentId: '0',
          name: '市场调研报告',
          type: 'MENU',
          routePath: '/research/market-report',
          componentPath: 'research/market-report',
          icon: 'DataAnalysis',
          cacheable: 0,
          permissionCode: 'research:market-report:view',
          sortOrder: 27,
          children: [],
        },
      ],
      permissionCodes: ['research:report-history:view', 'research:market-report:view'],
    }),
  }))
  await page.route('**/api/market-research/jobs?*', (route) => {
    requestedPages.push(new URL(route.request().url()))
    return route.fulfill({
      json: success({ current: Number(requestedPages.at(-1)?.searchParams.get('current')), size: 20, total: 41, records: [historyRecord] }),
    })
  })
  await page.route('**/api/market-research/jobs/job-history-001/artifacts/artifact-history-001/download', (route) => route.fulfill({
    status: 200,
    contentType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    headers: { 'Content-Disposition': 'attachment; filename="history.xlsx"' },
    body: Buffer.from('browser-xlsx'),
  }))
  await page.route('**/api/market-research/jobs/job-history-001/stream?*', (route) => route.fulfill({
    status: 200,
    contentType: 'text/event-stream',
    body: [
      'id: 4',
      'event: snapshot',
      `data: ${JSON.stringify({
        frameType: 'snapshot',
        jobId: historyRecord.jobId,
        afterSequence: 0,
        lastSequence: 4,
        replayComplete: true,
        job: reportSnapshotJob,
        nodes: [],
        events: reportSnapshotEvents,
      })}`,
      '',
      '',
    ].join('\n'),
  }))
  await page.route('**/api/market-research/categories?*', (route) => route.fulfill({
    json: success([]),
  }))
  await page.route('**/api/market-research/workflow', (route) => route.fulfill({
    json: success({
      type: 'MERMAID',
      title: '市场调研工作流',
      content: [
        'flowchart TD',
        'screeningGraph --> productSelectionGate',
        'productSelectionGate --> deepDiveGraph',
        'deepDiveGraph --> finalAnalysisGraph --> finalizeArtifacts',
      ].join('\n'),
      steps: workflowSteps,
    }),
  }))
  await page.route('**/api/market-research/jobs/job-history-001/evidence?*', (route) => route.fulfill({
    json: success([]),
  }))

  await page.setViewportSize({ width: 1440, height: 900 })
  await page.goto('/login')
  await page.getByLabel('用户名').fill('yuanbao')
  await page.getByLabel('密码').fill('correct-password')
  await page.getByRole('button', { name: '登录' }).click()

  await expect(page).toHaveURL(/\/research\/report-history$/)
  const historyPage = page.locator('section[aria-label="我的全部历史报告"]')
  await expect(historyPage.getByRole('heading', { name: '我的全部历史报告' })).toBeVisible()
  await expect(page.getByTestId('history-create-job')).toBeVisible()
  await expect(page.getByText('数据采集')).toBeVisible()
  await expect(page.getByText('证据整理')).toBeVisible()
  await expect(page.getByText('AI 报告')).toBeVisible()
  await expect(page.getByText('等待市场数据 · 0%')).toBeVisible()
  await expect(page.getByTestId('history-artifact-group-job-history-001-STAGE1_RAW_WORKBOOK'))
    .toContainText('美国站美容仪阶段一原始数据.xlsx')
  await expect(page.getByTestId('history-artifact-group-job-history-001-STAGE1_EVIDENCE_WORKBOOK'))
    .toContainText('美国站美容仪阶段一证据数据.xlsx')
  await expect(page.getByTestId('history-artifact-group-job-history-001-STAGE2_RAW_WORKBOOK'))
    .toContainText('美国站美容仪阶段二原始数据.xlsx')
  await expect(page.getByTestId('history-artifact-group-job-history-001-STAGE2_EVIDENCE_WORKBOOK'))
    .toContainText('美国站美容仪阶段二证据数据.xlsx')
  await expect(page.getByTestId('history-artifact-group-job-history-001-AI_ANALYSIS_REPORT'))
    .toContainText('美国站美容仪 AI 分析报告.md')

  await page.getByTestId('history-keyword').fill(' cleansing device ')
  await page.getByTestId('history-status').selectOption('SUCCEEDED')
  await page.getByTestId('history-marketplace').selectOption('US')
  await page.getByTestId('history-month').fill('2026-07')
  await page.getByTestId('history-search').click()
  await expect.poll(() => requestedPages.length).toBe(2)
  expect(requestedPages[1].searchParams.get('keyword')).toBe('cleansing device')
  expect(requestedPages[1].searchParams.get('status')).toBe('SUCCEEDED')
  expect(requestedPages[1].searchParams.get('marketplace')).toBe('US')
  expect(requestedPages[1].searchParams.get('month')).toBe('2026-07')

  await page.getByTestId('history-next-page').click()
  await expect.poll(() => requestedPages.length).toBe(3)
  expect(requestedPages[2].searchParams.get('current')).toBe('2')
  expect(requestedPages[2].searchParams.get('keyword')).toBe('cleansing device')

  const download = page.waitForEvent('download')
  await page.getByTestId('download-artifact-artifact-history-001').click()
  expect((await download).suggestedFilename()).toBe('美国站美容仪阶段一原始数据.xlsx')

  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
  await page.screenshot({ path: testInfo.outputPath('history-desktop.png'), fullPage: true })

  await page.setViewportSize({ width: 768, height: 900 })
  await expect(historyPage.getByRole('heading', { name: '我的全部历史报告' })).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
  await page.screenshot({ path: testInfo.outputPath('history-tablet.png'), fullPage: true })

  await page.getByTestId('view-report-job-history-001').click()
  await expect(page).toHaveURL(
    /\/research\/market-report\?jobId=job-history-001&view=conversation&section=report$/,
  )
  await expect(page.getByTestId('research-report-workspace')).toBeVisible()
  await expect(page.getByRole('heading', { name: '美国站美容仪市场调研' })).toBeVisible()
  await expect(page.getByText('市场结论')).toBeVisible()

  await page.setViewportSize({ width: 390, height: 844 })
  await page.getByTestId('research-workspace-tab-conversation').click()
  await expect(page).toHaveURL(/section=conversation/)
  await expect(page.getByTestId('research-follow-up-composer')).toBeVisible()
  expect(await page.evaluate(() => (
    document.documentElement.scrollWidth <= document.documentElement.clientWidth
  ))).toBe(true)

  await Promise.all([
    page.waitForURL(/\/research\/market-report\?jobId=job-history-001$/),
    page.getByTestId('research-workspace-back').click(),
  ])
  await page.waitForTimeout(1_500)
  await expect(page).toHaveURL(/\/research\/market-report\?jobId=job-history-001$/)
  await expect(page.getByTestId('research-job-status')).toBeVisible()
  await expect(page.getByTestId('research-task-input-summary')).toContainText('B012345678')
  await expect(page.getByTestId('research-task-input-summary')).toContainText('重点判断差评与退货风险')

  await page.setViewportSize({ width: 1440, height: 900 })
  await page.getByTestId('toggle-research-workflow').click()
  await expect(page.getByTestId('research-workflow-diagram')).toBeVisible()
  await page.getByTestId('preview-research-workflow').click()
  await expect(page.getByTestId('research-workflow-preview')).toBeVisible()
})
