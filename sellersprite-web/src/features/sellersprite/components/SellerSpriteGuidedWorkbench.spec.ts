import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import * as dictionaryApi from '@/features/system/api/dictionaryApi'

import * as sellerSpriteApi from '../api/sellerspriteApi'
import * as excelExport from '../utils/sellerSpriteExcelExport'
import SellerSpriteGuidedWorkbench from './SellerSpriteGuidedWorkbench.vue'

vi.mock('@/features/system/api/dictionaryApi', () => ({
  getEnabledDictionary: vi.fn(),
}))

vi.mock('../api/sellerspriteApi', async (importOriginal) => {
  const original = await importOriginal<typeof import('../api/sellerspriteApi')>()
  return {
    ...original,
    executeSellerSpriteOperation: vi.fn(),
  }
})

vi.mock('../utils/sellerSpriteExcelExport', () => ({
  exportSellerSpriteQueryResult: vi.fn(),
}))

describe('SellerSpriteGuidedWorkbench', () => {
  beforeEach(() => {
    vi.mocked(sellerSpriteApi.executeSellerSpriteOperation).mockReset()
    vi.mocked(excelExport.exportSellerSpriteQueryResult).mockReset().mockResolvedValue({
      fileName: 'sellersprite-result.xlsx',
      rowCount: 1,
    })
    vi.mocked(dictionaryApi.getEnabledDictionary).mockImplementation(async (dictType) => dictionaryFixture(dictType))
  })

  it('submits structured filters and renders a SellerSprite page as a scaffold table', async () => {
    vi.mocked(sellerSpriteApi.executeSellerSpriteOperation).mockResolvedValue({
      data: {
        page: 1,
        size: 50,
        total: 1,
        pages: 1,
        took: 23,
        items: [{ asin: 'B07Z82895W', title: 'Wireless Earbuds', price: 29.9 }],
      },
      durationMs: 31,
      completedAt: Date.now(),
    })
    const wrapper = mount(SellerSpriteGuidedWorkbench)

    await wrapper.get('[aria-label="执行引导查询"]').trigger('click')
    await flushPromises()

    expect(sellerSpriteApi.executeSellerSpriteOperation).toHaveBeenCalledWith(
      expect.objectContaining({ id: 'PRODUCT_COMPETITOR_LOOKUP' }),
      expect.objectContaining({ marketplace: 'MARKET_US', page: 1, size: 50 }),
      {},
    )
    expect(wrapper.get('[aria-label="引导查询结果"]').text()).toContain('共 1 条')
    expect(wrapper.get('[aria-label="引导查询结果"]').text()).toContain('B07Z82895W')
    expect(wrapper.get('[aria-label="引导查询结果"]').text()).toContain('上游 23 ms')
  })

  it('reuses the submitted filters when changing pages', async () => {
    vi.mocked(sellerSpriteApi.executeSellerSpriteOperation).mockImplementation(async (_operation, payload) => ({
      data: {
        page: payload.page,
        size: payload.size,
        total: 120,
        items: [{ asin: `PAGE-${String(payload.page)}` }],
      },
      durationMs: 10,
      completedAt: Date.now(),
    }))
    const wrapper = mount(SellerSpriteGuidedWorkbench)

    await wrapper.get('[aria-label="执行引导查询"]').trigger('click')
    await flushPromises()
    await wrapper.get('.el-pagination .btn-next').trigger('click')
    await flushPromises()

    expect(sellerSpriteApi.executeSellerSpriteOperation).toHaveBeenCalledTimes(2)
    expect(vi.mocked(sellerSpriteApi.executeSellerSpriteOperation).mock.calls[1]?.[1]).toEqual(
      expect.objectContaining({ marketplace: 'MARKET_US', page: 2, size: 50 }),
    )
    expect(wrapper.text()).toContain('PAGE-2')
  })

  it('exports only the records returned by the current guided query', async () => {
    vi.mocked(sellerSpriteApi.executeSellerSpriteOperation).mockResolvedValue({
      data: {
        page: 1,
        size: 50,
        total: 2,
        items: [{ asin: 'B001', title: 'Keyboard', price: 49.9 }],
      },
      durationMs: 12,
      completedAt: Date.now(),
    })
    const wrapper = mount(SellerSpriteGuidedWorkbench)
    expect(wrapper.get('[aria-label="导出引导查询结果"]').attributes('disabled')).toBeDefined()

    await wrapper.get('[aria-label="执行引导查询"]').trigger('click')
    await flushPromises()
    await wrapper.get('[aria-label="导出引导查询结果"]').trigger('click')
    await flushPromises()

    expect(excelExport.exportSellerSpriteQueryResult).toHaveBeenCalledWith({
      operationId: 'PRODUCT_COMPETITOR_LOOKUP',
      operationName: '查竞品',
      data: [{ asin: 'B001', title: 'Keyboard', price: 49.9 }],
      columns: expect.arrayContaining([
        { key: 'asin', label: 'ASIN' },
        { key: 'title', label: '标题' },
        { key: 'price', label: '价格' },
      ]),
    })
  })

  it('renders every returned product field instead of truncating the table to nine columns', async () => {
    vi.mocked(sellerSpriteApi.executeSellerSpriteOperation).mockResolvedValue({
      data: {
        page: 1,
        size: 50,
        total: 1,
        items: [{
          asin: 'B0DB5VT4QJ',
          brand: 'OLANLY',
          title: 'Bathroom Rugs',
          price: 100.89,
          units: 158113,
          revenue: 16059537,
          rating: 4.4,
          ratings: 18821,
          sellerName: 'OLANLY',
          fulfillment: 'FBA',
          pkgWeight: '10.43 pounds',
          subcategories: [{ code: '1063242', rank: 1, label: 'Bath Rugs' }],
        }],
      },
      durationMs: 31,
      completedAt: Date.now(),
    })
    const wrapper = mount(SellerSpriteGuidedWorkbench)

    await wrapper.get('[data-guided-operation-id="PRODUCT_RESEARCH"]').trigger('click')
    await wrapper.get('[aria-label="执行引导查询"]').trigger('click')
    await flushPromises()

    expect(wrapper.get('[aria-label="引导查询结果"]').text()).toContain('包装重量')
    expect(wrapper.get('[aria-label="引导查询结果"]').text()).toContain('10.43 pounds')
    expect(wrapper.get('[aria-label="引导查询结果"]').text()).toContain('Bath Rugs')
  })

  it('executes non-paginated operations without injecting page parameters', async () => {
    vi.mocked(sellerSpriteApi.executeSellerSpriteOperation).mockResolvedValue({
      data: { remaining: 100, resetAt: 1784000000000 },
      durationMs: 5,
      completedAt: Date.now(),
    })
    const wrapper = mount(SellerSpriteGuidedWorkbench)

    await wrapper.get('[data-guided-group-id="account"]').trigger('click')
    await wrapper.get('[aria-label="执行引导查询"]').trigger('click')
    await flushPromises()

    expect(sellerSpriteApi.executeSellerSpriteOperation).toHaveBeenCalledWith(
      expect.objectContaining({ id: 'ACCOUNT_VISITS' }),
      {},
      {},
    )
    expect(wrapper.text()).toContain('remaining')
    expect(wrapper.text()).toContain('100')
    expect(wrapper.find('.guided-pagination').exists()).toBe(false)
  })

  it('shows all 60 documented product research controls', async () => {
    const wrapper = mount(SellerSpriteGuidedWorkbench)

    await wrapper.get('[data-guided-operation-id="PRODUCT_RESEARCH"]').trigger('click')
    await flushPromises()

    expect(wrapper.findAll('[data-guided-field]')).toHaveLength(60)
    expect(wrapper.find('[data-guided-field="includeSellers"]').exists()).toBe(true)
    expect(wrapper.find('[data-guided-field="maxRevenueCr"]').exists()).toBe(true)
    expect(wrapper.find('[data-guided-field="dimensionType"] .el-select').exists()).toBe(true)
    expect(wrapper.find('[data-guided-field="order"] textarea').exists()).toBe(false)
    expect(wrapper.find('[data-guided-field="order"] .guided-sort-control').exists()).toBe(true)
  })

  it('submits product size and sort dictionary labels with comma-separated multi-values', async () => {
    vi.mocked(sellerSpriteApi.executeSellerSpriteOperation).mockResolvedValue({
      data: { page: 1, size: 50, total: 0, items: [] },
      durationMs: 8,
      completedAt: Date.now(),
    })
    const wrapper = mount(SellerSpriteGuidedWorkbench)
    await wrapper.get('[data-guided-operation-id="PRODUCT_RESEARCH"]').trigger('click')
    await flushPromises()

    const dimensionField = wrapper.get('[data-guided-field="dimensionType"]')
    const dimensionSelect = dimensionField.findComponent({ name: 'ElSelect' })
    expect(dictionaryApi.getEnabledDictionary).toHaveBeenCalledWith('PRODUCT_SIZE_US')
    expect(dictionaryApi.getEnabledDictionary).toHaveBeenCalledWith('PRODUCT_SORT_FIELD')
    dimensionSelect.vm.$emit('update:modelValue', [
      'PRODUCT_SIZE_US_ST_SS',
      'PRODUCT_SIZE_US_LS',
    ])
    await flushPromises()

    await wrapper.get('[aria-label="执行引导查询"]').trigger('click')
    await flushPromises()

    expect(sellerSpriteApi.executeSellerSpriteOperation).toHaveBeenCalledWith(
      expect.objectContaining({ id: 'PRODUCT_RESEARCH' }),
      expect.objectContaining({
        dimensionType: 'PRODUCT_SIZE_US_ST_SS,PRODUCT_SIZE_US_LS',
        order: { field: 'PRODUCT_SORT_FIELD_TOTAL_UNITS', desc: true },
      }),
      {},
    )
  })

  it('uses fixed review selectors and blocks a missing required ASIN', async () => {
    const wrapper = mount(SellerSpriteGuidedWorkbench)

    await wrapper.get('[data-guided-group-id="review"]').trigger('click')

    expect(wrapper.find('[data-guided-field="marketplace"]').exists()).toBe(true)
    expect(wrapper.find('[data-guided-field="starList"]').exists()).toBe(true)
    expect(wrapper.find('[data-guided-field="typeList"]').exists()).toBe(true)

    await wrapper.get('[aria-label="执行引导查询"]').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('请选择或填写ASIN')
    expect(sellerSpriteApi.executeSellerSpriteOperation).not.toHaveBeenCalled()
  })

  it('keeps the multipart image selector for guided trademark search', async () => {
    const wrapper = mount(SellerSpriteGuidedWorkbench)

    await wrapper.get('[data-guided-group-id="trademark"]').trigger('click')
    await wrapper.get('[data-guided-operation-id="GLOBAL_BRAND_LIST"]').trigger('click')
    const input = wrapper.get('input[aria-label="商标图片"]')
    const file = new File(['image'], 'brand.png', { type: 'image/png' })
    Object.defineProperty(input.element, 'files', { configurable: true, value: [file] })
    await input.trigger('change')

    expect(wrapper.text()).toContain('brand.png')
  })
})

function dictionaryFixture(dictType: string) {
  const entries: Record<string, Array<[string, string]>> = {
    PRODUCT_SIZE_US: [
      ['PRODUCT_SIZE_US_ST_SS', '小号标准尺寸'],
      ['PRODUCT_SIZE_US_LS', '大号标准尺寸'],
      ['PRODUCT_SIZE_US_SO', '小号大件'],
      ['PRODUCT_SIZE_US_MO', '中号大件'],
      ['PRODUCT_SIZE_US_LO_LB', '大号大件'],
      ['PRODUCT_SIZE_US_SP', '特殊大件'],
      ['PRODUCT_SIZE_US_O', '其他尺寸'],
      ['PRODUCT_SIZE_US_ELO', '超大尺寸：0 至 50 磅'],
      ['PRODUCT_SIZE_US_EL5O', '超大尺寸：50 到 70 磅'],
      ['PRODUCT_SIZE_US_EL7O', '超大尺寸：70 至 150 磅'],
      ['PRODUCT_SIZE_US_EL15O', '超大尺寸：150 磅以上'],
    ],
    PRODUCT_SIZE_JP: [
      ['PRODUCT_SIZE_JP_SM', '小号'],
      ['PRODUCT_SIZE_JP_ST', '标准'],
      ['PRODUCT_SIZE_JP_OV', '大件'],
      ['PRODUCT_SIZE_JP_SS', '超大尺寸'],
      ['PRODUCT_SIZE_JP_O', '其他尺寸'],
    ],
    PRODUCT_SORT_FIELD: [
      ['PRODUCT_SORT_FIELD_TOTAL_UNITS', '月销量'],
      ['PRODUCT_SORT_FIELD_TOTAL_AMOUNT', '月销售额'],
      ['PRODUCT_SORT_FIELD_BSR_RANK', 'BSR 排名'],
      ['PRODUCT_SORT_FIELD_PRICE', '价格'],
      ['PRODUCT_SORT_FIELD_RATING', '评分'],
      ['PRODUCT_SORT_FIELD_REVIEWS', '评分数'],
      ['PRODUCT_SORT_FIELD_PROFIT', '毛利率'],
      ['PRODUCT_SORT_FIELD_REVIEWS_RATE', '留评率'],
      ['PRODUCT_SORT_FIELD_AVAILABLE_DATE', '上架时间'],
      ['PRODUCT_SORT_FIELD_QUESTIONS', 'Q & A'],
      ['PRODUCT_SORT_FIELD_TOTAL_UNITS_GROWTH', '月销量增长率'],
      ['PRODUCT_SORT_FIELD_TOTAL_AMOUNT_GROWTH', '月销售额增长率'],
      ['PRODUCT_SORT_FIELD_REVIEWS_INCREASEMENT', '月新增评分数'],
      ['PRODUCT_SORT_FIELD_BSR_RANK_CV', '近7天BSR增长数'],
      ['PRODUCT_SORT_FIELD_BSR_RANK_CR', '近7天BSR增长率'],
      ['PRODUCT_SORT_FIELD_AMZ_UNIT', '子体销量'],
    ],
  }
  const items = entries[dictType] ?? []
  return {
    dictType,
    dictName: dictType,
    systemBuiltin: 1,
    sortOrder: 1,
    status: 1,
    items: items.map(([dictLabel, dictName], index) => ({
      dictDataId: `${dictType}-${String(index)}`,
      dictType,
      dictValue: dictLabel,
      dictLabel,
      dictName,
      color: '',
      defaultFlag: 0,
      sortOrder: index + 1,
      systemBuiltin: 1,
      status: 1,
    })),
  }
}
