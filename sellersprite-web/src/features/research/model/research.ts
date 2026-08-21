export const researchJobStatuses = [
  'QUEUED',
  'RUNNING',
  'RETRY_WAIT',
  'WAITING_INPUT',
  'SUCCEEDED',
  'ABANDONED',
  'FAILED',
  'CANCELLED',
] as const

export type ResearchJobStatus = (typeof researchJobStatuses)[number]

export const researchStageCodes = [
  'SCREENING',
  'PRODUCT_SELECTION',
  'DEEP_DIVE',
  'FINAL_ANALYSIS',
  'ARTIFACT_FINALIZATION',
] as const

export type ResearchStageCode = (typeof researchStageCodes)[number]

export type ResearchWaitingInputType = 'PRODUCT_SELECTION'

export const researchGraphCodes = ['collection', 'evidence', 'report'] as const

export type ResearchGraphCode = (typeof researchGraphCodes)[number]
export type ResearchNodeCode = `${ResearchGraphCode}.${string}`

export const researchGraphLabels: Record<ResearchGraphCode, string> = {
  collection: '数据采集',
  evidence: '证据整理',
  report: 'AI 报告',
}

export const researchWorkflowStepCodes = [
  'SCREENING',
  'PRODUCT_SELECTION',
  'DEEP_DIVE',
  'FINAL_ANALYSIS',
  'ARTIFACT_FINALIZATION',
] as const

export type ResearchWorkflowStepCode = (typeof researchWorkflowStepCodes)[number]

export interface ResearchWorkflowStep {
  code: ResearchWorkflowStepCode | string
  nodeCode: string
  label: string
}

export const defaultResearchWorkflowSteps: ResearchWorkflowStep[] = [
  { code: 'SCREENING', nodeCode: 'screeningGraph', label: '阶段一：市场初筛' },
  { code: 'PRODUCT_SELECTION', nodeCode: 'productSelectionGate', label: '商品选择' },
  { code: 'DEEP_DIVE', nodeCode: 'deepDiveGraph', label: '阶段二：商品深挖' },
  { code: 'FINAL_ANALYSIS', nodeCode: 'finalAnalysisGraph', label: '阶段三：最终分析' },
  { code: 'ARTIFACT_FINALIZATION', nodeCode: 'finalizeArtifacts', label: '生成并发布产物' },
]

const researchWorkflowPhaseCodes: Record<ResearchWorkflowStepCode, Set<string>> = {
  SCREENING: new Set([
    'collection.validate',
    'collection.checkQuota',
    'collection.collectProducts',
    'collection.collectMarketSalesTrend',
    'collection.collectKeywordDemandTrend',
    'collection.collectSegmentOpportunity',
    'evidence.prepareUs',
    'evidence.prepareSalesTrend',
    'evidence.prepareDemandTrend',
    'evidence.prepareSegmentMarket',
    'evidence.prepareSegmentReturn',
    'evidence.prepareBrand',
    'evidence.prepareConcentration',
  ]),
  PRODUCT_SELECTION: new Set(),
  DEEP_DIVE: new Set([
    'collection.collectAsinIntelligence',
    'collection.collectReviews',
    'collection.collectKeywordIntelligence',
    'evidence.prepareReview',
    'evidence.prepareVoc',
    'evidence.prepareKeyword',
    'evidence.prepareAsinSalesTrend',
    'evidence.prepareAsinOperationTrend',
  ]),
  FINAL_ANALYSIS: new Set(['report.runInitialAnalysis']),
  ARTIFACT_FINALIZATION: new Set(),
}

export function resolveResearchWorkflowStepCode(nodeCode?: string | null) {
  const normalized = nodeCode?.trim() || ''
  if (!normalized) return null
  const directStep = defaultResearchWorkflowSteps.find((step) => step.nodeCode === normalized)
  if (directStep) return directStep.code as ResearchWorkflowStepCode
  if (['SCREENING.enter', 'screening.analysis'].includes(normalized)) return 'SCREENING'
  if (['DEEP_DIVE.enter', 'deepDive.analysis'].includes(normalized)) return 'DEEP_DIVE'
  if (['FINAL_ANALYSIS.enter', 'finalAnalysis.analysis'].includes(normalized)) {
    return 'FINAL_ANALYSIS'
  }
  return researchWorkflowStepCodes.find((stepCode) => (
    researchWorkflowPhaseCodes[stepCode].has(normalized)
  )) ?? null
}

export function researchWorkflowPhaseCount(stepCode: ResearchWorkflowStepCode) {
  return researchWorkflowPhaseCodes[stepCode].size
}

export function resolveResearchGraphCode(
  graphCode?: string | null,
  nodeCode?: string | null,
): ResearchGraphCode | null {
  const candidate = graphCode?.trim() || nodeCode?.split('.', 1)[0]?.trim() || ''
  return researchGraphCodes.includes(candidate as ResearchGraphCode)
    ? candidate as ResearchGraphCode
    : null
}

export const sellerSpriteMarketplaces = [
  'US',
  'JP',
  'UK',
  'DE',
  'FR',
  'IT',
  'ES',
  'CA',
  'IN',
] as const

export type SellerSpriteMarketplace = (typeof sellerSpriteMarketplaces)[number]

export interface SellerSpriteSortOrder {
  field?: string
  desc?: boolean
}

export interface ProductResearchRequestConfig {
  marketplace?: SellerSpriteMarketplace
  month?: string
  keyword?: string
  minPrice?: number
  maxPrice?: number
  minRating?: number
  maxRating?: number
  minRatings?: number
  maxRatings?: number
  minUnits?: number
  maxUnits?: number
  minRevenue?: number
  maxRevenue?: number
  includeBrands?: string
  excludeBrands?: string
  nodeIdPaths?: string[]
  nodeIdPathEqual?: boolean
  page?: number
  size?: number
  order?: SellerSpriteSortOrder
}

export interface MarketResearchRequestConfig {
  marketplace?: SellerSpriteMarketplace
  month?: string
  nodeIdPath?: string
  departmentKeyword?: string
  topNum?: number
  newProduct?: number
  minAvgUnits?: number
  maxAvgUnits?: number
  minAvgRevenue?: number
  maxAvgRevenue?: number
  minAvgRating?: number
  maxAvgRating?: number
  minAvgRatings?: number
  maxAvgRatings?: number
  page?: number
  size?: number
  order?: SellerSpriteSortOrder
}

export interface KeywordResearchRequestConfig {
  marketplace?: SellerSpriteMarketplace
  month?: string
  keywords?: string
  excludeKeywords?: string
  departments?: string[]
  minSearches?: number
  maxSearches?: number
  minProducts?: number
  maxProducts?: number
  page?: number
  size?: number
  order?: SellerSpriteSortOrder
}

export interface KeywordMinerRequestConfig {
  marketplace?: SellerSpriteMarketplace
  historyDate?: string
  keyword?: string
  keywordList?: string[]
  minSearch?: number
  maxSearch?: number
  minProducts?: number
  maxProducts?: number
  page?: number
  size?: number
  order?: SellerSpriteSortOrder
}

export interface TrafficKeywordRequestConfig {
  marketplace?: SellerSpriteMarketplace
  asin?: string
  keyword?: string
  month?: string
  badges?: string[]
  trafficKeywordTypes?: string[]
  conversionKeywordTypes?: string[]
  page?: number
  size?: number
  order?: SellerSpriteSortOrder
}

export interface CollectionGraphConfig {
  collectProducts: {
    productResearch: ProductResearchRequestConfig
    pagination: {
      startPage: number
      pageSize: number
      targetCount: number
    }
    enrichmentAsinLimit: number
  }
  collectMarketSalesTrend: {
    monthCount: number
  }
  collectKeywordDemandTrend: {
    topN: number
  }
  collectSegmentOpportunity: {
    marketResearch: MarketResearchRequestConfig
    pagination: {
      startPage: number
      pageSize: number
      targetCount: number
    }
    distribution: {
      topN: number
      newProduct: number
      asins: string[]
    }
  }
  collectReviews: {
    starList: string[]
    typeList: string[]
    pagination: {
      startPage: number
      pageSize: number
      targetCountPerAsin: number
    }
  }
  collectKeywordIntelligence: {
    keywordResearch: KeywordResearchRequestConfig
    keywordMiner: KeywordMinerRequestConfig
    trafficKeyword: TrafficKeywordRequestConfig
    trafficAsinLimit: number
  }
  collectAsinIntelligence?: {
    keepaTrend?: Record<string, unknown>
  }
}

export function createDefaultCollectionGraphConfig(): CollectionGraphConfig {
  return {
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
      distribution: { topN: 100, newProduct: 6, asins: [] },
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
  }
}

export interface ResearchJobCreateRequest {
  reportName: string
  marketplace: SellerSpriteMarketplace
  nodeIdPath: string
  month: string
  keyword?: string
  seedAsins?: string[]
  analysisGoal?: string
  collectionConfig: CollectionGraphConfig
}

export interface ResearchCategoryNodeQuery {
  marketplace: SellerSpriteMarketplace
  month: string
  nodeIdPath?: string
  keyword?: string
}

export interface ResearchCategoryNode {
  nodeIdPath: string
  nodeLabelPath: string | null
  products: number | null
  nodeLabelLocale: string | null
  nodeLabelPathLocale: string | null
  nodeId?: string | null
  nodeLabel?: string | null
  displayName?: string | null
}

export interface ResearchCategoryCandidate {
  nodeIdPath: string
  nodeId?: string | null
  nodeLabelPath?: string | null
  nodeLabel?: string | null
  nodeLabelLocale?: string | null
  displayName: string
  matchedCount: number
  matchedAsins: string[]
  matchedRatio: number
}

export interface ResearchJobCreated {
  jobId: string
  status: ResearchJobStatus
  dataSourceMode: string
  workflowVersion: string
  conversationId?: string | null
  analysisRunId?: string | null
  analysisStatus?: string | null
}

export interface ResearchJobDetail {
  jobId: string
  reportName: string
  marketplace: SellerSpriteMarketplace
  nodeIdPath: string
  month: string
  keyword: string | null
  dataSourceMode: string
  workflowVersion: string
  status: ResearchJobStatus
  currentStage: ResearchStageCode | string | null
  waitingInputType: ResearchWaitingInputType | string | null
  currentNode: ResearchNodeCode | string | null
  currentNodeName: string | null
  progress: number
  attemptCount: number
  maxAttempts: number
  remainingAttempts: number
  nextRunAt: number | null
  leaseUntil: number | null
  heartbeatAt: number | null
  cancelRequestedAt: number | null
  cancellable: boolean
  retryable: boolean
  errorCode: string | null
  errorMessage: string | null
  startedAt: number | null
  finishedAt: number | null
  createdAt: number
  conversationId?: string | null
  analysisRunId?: string | null
  analysisStatus?: string | null
  analysisPhase?: string | null
  analysisProgress?: number | null
  analysisGoal?: string | null
  seedAsins?: string[]
  collectionConfig?: CollectionGraphConfig
  artifacts?: ResearchArtifactSummary[]
}

export const researchAnalysisRunTypes = [
  'SCREENING',
  'DEEP_DIVE',
  'FINAL_ANALYSIS',
  'INITIAL',
  'RETRY',
  'FOLLOW_UP',
] as const

export type ResearchAnalysisRunType = (typeof researchAnalysisRunTypes)[number]

export const researchAnalysisRunStatuses = [
  'WAITING_RESEARCH',
  'QUEUED',
  'RUNNING',
  'RETRY_WAIT',
  'SUCCEEDED',
  'FAILED',
  'CANCELLED',
] as const

export type ResearchAnalysisRunStatus = (typeof researchAnalysisRunStatuses)[number]

export interface ResearchAnalysisRun {
  analysisRunId: string
  jobId: string
  conversationId: string | null
  parentRunId: string | null
  runType: ResearchAnalysisRunType
  stageCode: ResearchStageCode | string | null
  analysisGoal: string | null
  status: ResearchAnalysisRunStatus
  currentPhase: string | null
  progress: number | null
  attemptCount: number | null
  maxAttempts: number | null
  nextRunAt: number | null
  leaseUntil: number | null
  heartbeatAt: number | null
  cancelRequestedAt: number | null
  modelCallCount: number | null
  eventCount: number | null
  finalSummary: string | null
  errorCode: string | null
  errorMessage: string | null
  startedAt: number | null
  finishedAt: number | null
  createdAt: number | null
  cancellable: boolean | null
  retryable: boolean | null
}

export interface ResearchJobHistoryQuery {
  current: number
  size: number
  keyword?: string
  status?: ResearchJobStatus
  marketplace?: SellerSpriteMarketplace
  month?: string
}

export interface ResearchArtifactSummary {
  artifactId: string
  analysisRunId: string | null
  artifactType: ResearchArtifactType | string
  fileName: string
  mediaType: string
  fileSize: number | null
  createdAt: number
}

export interface ResearchJobHistory {
  jobId: string
  reportName: string
  marketplace: SellerSpriteMarketplace
  nodeIdPath: string
  month: string
  keyword: string | null
  status: ResearchJobStatus
  currentStage?: ResearchStageCode | string | null
  waitingInputType?: ResearchWaitingInputType | string | null
  progress: number
  analysisRunId: string | null
  analysisStatus: string | null
  analysisPhase: string | null
  analysisProgress: number | null
  createdAt: number
  finishedAt: number | null
  artifacts: ResearchArtifactSummary[]
}

export interface ResearchNodeExecution {
  executionId: string
  graphCode?: ResearchGraphCode | string | null
  nodeCode: ResearchNodeCode | string
  nodeName: string
  jobAttempt: number
  nodeAttempt: number
  status: 'RUNNING' | 'SUCCEEDED' | 'FAILED' | 'CANCELLED'
  startedAt: number
  finishedAt: number | null
  durationMs: number | null
  errorCode: string | null
  errorMessage: string | null
}

export interface ResearchWorkflowTopology {
  type: 'MERMAID'
  title: string
  content: string
  steps?: ResearchWorkflowStep[]
}

export interface ResearchEvidenceTableSummary {
  datasetCode: string
  sheetName: string
  stageCode: ResearchStageCode | string
  rowCount: number
  columns: string[]
}

export interface ResearchEvidencePage {
  datasetCode: string
  sheetName: string
  stageCode: ResearchStageCode | string
  columns: string[]
  records: Array<Record<string, unknown>>
  current: number
  size: number
  total: number
}

export interface ResearchProductCandidate {
  rank: number
  asin: string
  parentAsin?: string | null
  variations?: number | string | null
  imageUrl: string | null
  title: string | null
  brand: string | null
  category: string | null
  units: number | string | null
  revenue: number | string | null
  price: number | string | null
  rating: number | string | null
  ratings: number | string | null
}

export type ResearchProductSelectionStatus = 'PENDING' | 'SUBMITTED' | 'ABANDONED'

export interface ResearchProductSelection {
  stageCode: ResearchStageCode | string
  status: ResearchProductSelectionStatus
  candidates: ResearchProductCandidate[]
  selectedAsins: string[]
  submittedAt?: number | null
}

export type ResearchProductSelectionDecision = 'ENTER' | 'ABANDON'

export interface ResearchProductSelectionRequest {
  decision: ResearchProductSelectionDecision
  selectedAsins: string[]
}

export const researchArtifactTypes = [
  'STAGE1_RAW_WORKBOOK',
  'STAGE1_EVIDENCE_WORKBOOK',
  'STAGE1_CONCLUSION_REPORT',
  'STAGE2_RAW_WORKBOOK',
  'STAGE2_EVIDENCE_WORKBOOK',
  'STAGE2_CONCLUSION_REPORT',
  'AI_ANALYSIS_REPORT',
] as const

export type ResearchArtifactType = (typeof researchArtifactTypes)[number]

export const researchArtifactLabels: Record<ResearchArtifactType, string> = {
  STAGE1_RAW_WORKBOOK: '阶段一原始数据 Excel',
  STAGE1_EVIDENCE_WORKBOOK: '阶段一证据数据 Excel',
  STAGE1_CONCLUSION_REPORT: '阶段一结论表 PDF',
  STAGE2_RAW_WORKBOOK: '阶段二原始数据 Excel',
  STAGE2_EVIDENCE_WORKBOOK: '阶段二证据数据 Excel',
  STAGE2_CONCLUSION_REPORT: '阶段二结论表 PDF',
  AI_ANALYSIS_REPORT: 'AI 分析报告',
}

export const researchArtifactGraph: Record<ResearchArtifactType, ResearchGraphCode> = {
  STAGE1_RAW_WORKBOOK: 'collection',
  STAGE1_EVIDENCE_WORKBOOK: 'evidence',
  STAGE1_CONCLUSION_REPORT: 'evidence',
  STAGE2_RAW_WORKBOOK: 'collection',
  STAGE2_EVIDENCE_WORKBOOK: 'evidence',
  STAGE2_CONCLUSION_REPORT: 'evidence',
  AI_ANALYSIS_REPORT: 'report',
}
