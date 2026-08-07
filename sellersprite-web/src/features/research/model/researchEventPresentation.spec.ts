import { describe, expect, it } from 'vitest'

import {
  isResearchOfficialEvent,
  isResearchProcessEvent,
  isResearchWorkspaceReportEvent,
  isWorkflowTerminalEvent,
  researchEventDetail,
  researchEventMeta,
  researchEventTitle,
  researchWorkspaceIntent,
} from './researchEventPresentation'
import type { ResearchStreamRecord } from './researchStream'

describe('researchEventPresentation', () => {
  it('labels graph and Curation process events with durable sequence metadata', () => {
    const graphEvent = record('research_node_started', {
      scope: 'research',
      nodeCode: 'collection.collectProducts',
      phase: 'collect',
      sequenceNo: 7,
    })
    const sheetEvent = record('sheet_focus', {
      scope: 'analysis',
      sheetName: '竞品品牌',
    })

    expect(researchEventTitle(graphEvent)).toBe('节点开始 · collection.collectProducts')
    expect(researchEventMeta(graphEvent)).toBe('采集 · #7')
    expect(researchEventTitle(sheetEvent)).toBe('Sheet 聚焦 · 竞品品牌')
    expect(isResearchProcessEvent(sheetEvent)).toBe(true)
  })

  it('keeps full sheet detail and hides raw delta records', () => {
    const observation = `Sheet「Keywords」\n${'搜索词 lash serum '.repeat(80)}`
    const sheetEvent = record('sheet', { data: observation })

    expect(researchEventDetail(sheetEvent)).toBe(observation.trim())
    expect(isResearchProcessEvent(record('summary_delta'))).toBe(false)
    expect(isResearchOfficialEvent(record('summary_delta'))).toBe(false)
  })

  it('only closes the unified stream for workflow scoped terminal event names', () => {
    expect(isWorkflowTerminalEvent(record('done', {
      scope: 'analysis',
      terminal: true,
    }))).toBe(false)
    expect(isWorkflowTerminalEvent(record('workflow_completed', {
      scope: 'workflow',
      terminal: true,
    }))).toBe(true)
    expect(isWorkflowTerminalEvent(record('market_abandoned', {
      scope: 'workflow',
      terminal: true,
    }))).toBe(true)
  })

  it('presents human review and stage lifecycle events as official events', () => {
    expect(isResearchOfficialEvent(record('product_selection_required'))).toBe(true)
    expect(isResearchOfficialEvent(record('product_selection_submitted'))).toBe(true)
    expect(isResearchOfficialEvent(record('stage_completed'))).toBe(true)
    expect(isResearchOfficialEvent(record('market_abandoned'))).toBe(true)
    expect(researchEventTitle(record('product_selection_required'))).toBe('等待商品选择')
  })

  it('keeps streaming summaries in process and treats the persisted report as formal output', () => {
    const streamingSummary = record('summary', { streaming: true })
    const persistedReport = record('report')

    expect(isResearchOfficialEvent(streamingSummary)).toBe(false)
    expect(isResearchProcessEvent(streamingSummary)).toBe(true)
    expect(isResearchWorkspaceReportEvent(streamingSummary)).toBe(false)
    expect(isResearchOfficialEvent(persistedReport)).toBe(true)
    expect(isResearchWorkspaceReportEvent(persistedReport)).toBe(true)
  })

  it('maps live events to the workspace that renders them', () => {
    expect(researchWorkspaceIntent(record('plan'))).toEqual({
      mode: 'workspace',
      section: 'process',
    })
    expect(researchWorkspaceIntent(record('workbook_ready', {
      stageCode: 'DEEP_DIVE',
    }))).toEqual({
      mode: 'workspace',
      section: 'evidence',
      evidenceStage: 'DEEP_DIVE',
    })
    expect(researchWorkspaceIntent(record('summary'))).toEqual({
      mode: 'workspace',
      section: 'report',
    })
    expect(researchWorkspaceIntent(record('summary'), true)).toEqual({
      mode: 'workspace',
    })
    expect(researchWorkspaceIntent(record('plan'), true)).toEqual({ mode: 'workspace' })
    expect(researchWorkspaceIntent(record('done'), true)).toEqual({ mode: 'workspace' })
    expect(researchWorkspaceIntent(record('workflow_completed'))).toEqual({
      mode: 'workspace',
      section: 'report',
      force: true,
    })
    expect(researchWorkspaceIntent(record('market_abandoned'))).toEqual({
      mode: 'workspace',
      section: 'process',
      force: true,
    })
    expect(researchWorkspaceIntent(record('product_selection_required'))).toEqual({
      mode: 'task',
    })
  })
})

function record(
  eventType: string,
  overrides: Partial<ResearchStreamRecord> = {},
): ResearchStreamRecord {
  return {
    id: `event-${eventType}`,
    jobId: 'job-1',
    scope: 'analysis',
    eventType,
    message: eventType,
    terminal: false,
    receivedAt: '12:00:00',
    ...overrides,
  }
}
