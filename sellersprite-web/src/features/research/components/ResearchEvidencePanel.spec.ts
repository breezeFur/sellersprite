import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import * as researchApi from '../api/researchApi'
import ResearchEvidencePanel from './ResearchEvidencePanel.vue'

vi.mock('../api/researchApi', () => ({
  getResearchEvidencePage: vi.fn(),
  listResearchEvidenceTables: vi.fn(),
}))

describe('ResearchEvidencePanel', () => {
  beforeEach(() => {
    vi.mocked(researchApi.listResearchEvidenceTables).mockReset().mockResolvedValue([{
      datasetCode: 'evidence.products',
      sheetName: 'US',
      stageCode: 'SCREENING',
      rowCount: 51,
      columns: ['排名', 'ASIN', '品牌'],
    }])
    vi.mocked(researchApi.getResearchEvidencePage).mockReset()
      .mockResolvedValueOnce({
        datasetCode: 'evidence.products',
        sheetName: 'US',
        stageCode: 'SCREENING',
        columns: ['排名', 'ASIN', '品牌'],
        records: [{ 排名: 1, ASIN: 'B012345678', 品牌: 'Demo Brand' }],
        current: 1,
        size: 50,
        total: 51,
      })
      .mockResolvedValueOnce({
        datasetCode: 'evidence.products',
        sheetName: 'US',
        stageCode: 'SCREENING',
        columns: ['排名', 'ASIN', '品牌'],
        records: [{ 排名: 51, ASIN: 'B087654321', 品牌: 'Last Brand' }],
        current: 2,
        size: 50,
        total: 51,
      })
  })

  it('loads the screening table catalog and pages persisted evidence rows', async () => {
    const wrapper = mount(ResearchEvidencePanel, {
      props: { jobId: 'job/1', stageCode: 'SCREENING' },
    })
    await flushPromises()

    expect(researchApi.listResearchEvidenceTables).toHaveBeenCalledWith('job/1', 'SCREENING')
    expect(researchApi.getResearchEvidencePage).toHaveBeenCalledWith(
      'job/1',
      'evidence.products',
      1,
      50,
    )
    expect(wrapper.text()).toContain('B012345678')
    expect(wrapper.text()).toContain('Demo Brand')

    await wrapper.get('.btn-next').trigger('click')
    await flushPromises()

    expect(researchApi.getResearchEvidencePage).toHaveBeenLastCalledWith(
      'job/1',
      'evidence.products',
      2,
      50,
    )
    expect(wrapper.text()).toContain('B087654321')
  })
})
