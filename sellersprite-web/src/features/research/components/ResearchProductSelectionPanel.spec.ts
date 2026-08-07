import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import * as researchApi from '../api/researchApi'
import type { ResearchProductCandidate } from '../model/research'
import ResearchProductSelectionPanel from './ResearchProductSelectionPanel.vue'

vi.mock('../api/researchApi', () => ({
  getResearchProductSelection: vi.fn(),
  submitResearchProductSelection: vi.fn(),
}))

const candidate: ResearchProductCandidate = {
  rank: 1,
  asin: 'B012345678',
  imageUrl: null,
  title: 'Portable Blender',
  brand: 'Demo Brand',
  category: 'Kitchen',
  units: '1200',
  revenue: '35988',
  price: '29.99',
  rating: '4.5',
  ratings: '830',
}

describe('ResearchProductSelectionPanel', () => {
  beforeEach(() => {
    vi.mocked(researchApi.getResearchProductSelection).mockReset().mockResolvedValue({
      stageCode: 'PRODUCT_SELECTION',
      status: 'PENDING',
      candidates: [candidate],
      selectedAsins: [],
      submittedAt: null,
    })
    vi.mocked(researchApi.submitResearchProductSelection).mockReset().mockResolvedValue(undefined)
  })

  it('submits selected Top20 ASINs to enter stage two', async () => {
    const wrapper = mount(ResearchProductSelectionPanel, {
      props: { jobId: 'job/1', jobStatus: 'WAITING_INPUT' },
    })
    await flushPromises()

    wrapper.findComponent({ name: 'ElTable' }).vm.$emit('selection-change', [candidate])
    await wrapper.vm.$nextTick()
    await wrapper.get('[data-testid="submit-research-product-selection"]').trigger('click')
    await flushPromises()

    expect(researchApi.submitResearchProductSelection).toHaveBeenCalledWith('job/1', {
      decision: 'ENTER',
      selectedAsins: ['B012345678'],
    })
    expect(wrapper.emitted('submitted')?.[0]).toEqual(['ENTER'])
  })

  it('submits an empty ASIN list when the user abandons the market', async () => {
    const wrapper = mount(ResearchProductSelectionPanel, {
      props: { jobId: 'job/1', jobStatus: 'WAITING_INPUT' },
    })
    await flushPromises()

    await wrapper.get('[data-testid="abandon-research-market"]').trigger('click')
    await flushPromises()

    expect(researchApi.submitResearchProductSelection).toHaveBeenCalledWith('job/1', {
      decision: 'ABANDON',
      selectedAsins: [],
    })
    expect(wrapper.emitted('submitted')?.[0]).toEqual(['ABANDON'])
  })

  it('restores an unsubmitted parent draft after the panel is mounted again', async () => {
    const wrapper = mount(ResearchProductSelectionPanel, {
      props: {
        jobId: 'job/1',
        jobStatus: 'WAITING_INPUT',
        draftAsins: ['B012345678'],
      },
    })
    await flushPromises()

    expect(wrapper.emitted('update:draftAsins')?.at(-1)).toEqual([['B012345678']])
    await wrapper.get('[data-testid="submit-research-product-selection"]').trigger('click')
    await flushPromises()

    expect(researchApi.submitResearchProductSelection).toHaveBeenCalledWith('job/1', {
      decision: 'ENTER',
      selectedAsins: ['B012345678'],
    })
  })

  it('previews every valid candidate image from the selected image index', async () => {
    vi.mocked(researchApi.getResearchProductSelection).mockResolvedValue({
      stageCode: 'PRODUCT_SELECTION',
      status: 'PENDING',
      candidates: [
        { ...candidate, imageUrl: 'https://images.example.com/one.jpg' },
        { ...candidate, rank: 2, asin: 'B087654321', imageUrl: null },
        { ...candidate, rank: 3, asin: 'B098765432', imageUrl: 'invalid-image-url' },
        { ...candidate, rank: 4, asin: 'B076543210', imageUrl: 'https://images.example.com/four.jpg' },
      ],
      selectedAsins: [],
      submittedAt: null,
    })
    const wrapper = mount(ResearchProductSelectionPanel, {
      props: { jobId: 'job/1', jobStatus: 'WAITING_INPUT' },
    })
    await flushPromises()

    const images = wrapper.findAllComponents({ name: 'ElImage' })
    const previewImages = [
      'https://images.example.com/one.jpg',
      'https://images.example.com/four.jpg',
    ]

    expect(images).toHaveLength(2)
    expect(images[0]?.props('previewSrcList')).toEqual(previewImages)
    expect(images[0]?.props('initialIndex')).toBe(0)
    expect(images[0]?.props('previewTeleported')).toBe(true)
    expect(images[1]?.props('previewSrcList')).toEqual(previewImages)
    expect(images[1]?.props('initialIndex')).toBe(1)
    expect(images[1]?.props('previewTeleported')).toBe(true)
  })
})
