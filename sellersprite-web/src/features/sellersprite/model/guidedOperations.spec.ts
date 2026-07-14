import { describe, expect, it } from 'vitest'

import { getSellerSpriteOperation, sellerSpriteOperations } from './operations'
import { officialSellerSpriteOperationContracts } from './officialOperationContracts.generated'
import {
  getGuidedSellerSpriteOperation,
  guidedSellerSpriteGroups,
  guidedSellerSpriteOperations,
  sellerSpriteMarketplaceOptions,
} from './guidedOperations'

describe('guidedSellerSpriteOperations', () => {
  it('covers all 45 operations and their nine business domains', () => {
    expect(guidedSellerSpriteOperations).toHaveLength(45)
    expect(new Set(guidedSellerSpriteOperations.map((operation) => operation.id)).size).toBe(45)
    expect(guidedSellerSpriteOperations.map((operation) => operation.id)).toEqual(
      sellerSpriteOperations.map((operation) => operation.id),
    )
    expect(guidedSellerSpriteGroups.map((group) => group.id)).toEqual([
      'account',
      'product',
      'asin',
      'keyword',
      'traffic',
      'market',
      'review',
      'trademark',
      'tool',
    ])

    for (const operation of guidedSellerSpriteOperations) {
      expect(getSellerSpriteOperation(operation.id)).toBeDefined()
      expect(getGuidedSellerSpriteOperation(operation.id)).toBe(operation)
    }
  })

  it('declares only legal positive page sizes and includes every default', () => {
    for (const operation of guidedSellerSpriteOperations.filter((item) => item.responseShape === 'page')) {
      const { defaultSize, pageSizes } = operation.pagination
      expect(Number.isInteger(defaultSize)).toBe(true)
      expect(defaultSize).toBeGreaterThan(0)
      expect(pageSizes).toContain(defaultSize)
      expect(new Set(pageSizes).size).toBe(pageSizes.length)
      expect(pageSizes.every((size) => Number.isInteger(size) && size > 0)).toBe(true)
      expect([...pageSizes].sort((left, right) => left - right)).toEqual([...pageSizes])
    }

    expect(getGuidedSellerSpriteOperation('REVIEW_LIST')?.pagination).toEqual({
      defaultSize: 5,
      pageSizes: [5, 10],
    })
    expect(getGuidedSellerSpriteOperation('KEYWORD_ORDER')?.pagination.pageSizes).toEqual([50])
  })

  it('uses explicit controls for marketplaces, dates, modes, and review filters', () => {
    expect(sellerSpriteMarketplaceOptions).toHaveLength(9)
    expect(new Set(sellerSpriteMarketplaceOptions.map((option) => option.value))).toEqual(
      new Set([
        'MARKET_US',
        'MARKET_JP',
        'MARKET_UK',
        'MARKET_DE',
        'MARKET_FR',
        'MARKET_IT',
        'MARKET_ES',
        'MARKET_CA',
        'MARKET_IN',
      ]),
    )

    for (const operation of guidedSellerSpriteOperations.filter(({ fields }) => (
      fields.some((field) => field.key === 'marketplace')
    ))) {
      const marketplace = operation.fields.find((field) => field.key === 'marketplace')
      expect(marketplace?.control).toBe('select')
      expect(marketplace?.options).toHaveLength(9)
    }

    expect(getGuidedSellerSpriteOperation('ABA_RESEARCH_WEEKLY')?.fields
      .find((field) => field.key === 'date')).toMatchObject({ control: 'date', valueFormat: 'YYYYMMDD' })
    expect(getGuidedSellerSpriteOperation('ABA_RESEARCH_MONTHLY')?.fields
      .find((field) => field.key === 'date')).toMatchObject({ control: 'month', valueFormat: 'YYYYMM' })
    expect(getGuidedSellerSpriteOperation('KEYWORD_ORDER')?.fields
      .find((field) => field.key === 'reverseType')?.options?.map((option) => option.value)).toEqual(['W', 'M'])

    const reviewFields = getGuidedSellerSpriteOperation('REVIEW_LIST')?.fields
    expect(reviewFields?.find((field) => field.key === 'starList')).toMatchObject({
      control: 'multi-select',
      options: reviewStarOptionsForTest(),
    })
    expect(reviewFields?.find((field) => field.key === 'typeList')?.options).toHaveLength(4)
  })

  it('exposes every documented product research input and response field', () => {
    const contract = officialSellerSpriteOperationContracts.find((item) => item.operation === 'PRODUCT_RESEARCH')
    const operation = getGuidedSellerSpriteOperation('PRODUCT_RESEARCH')
    const officialInputKeys = contract?.requestFields
      .filter((field) => !field.field.startsWith('└'))
      .filter((field) => !['page', 'size'].includes(field.field))
      .map((field) => field.field)

    expect(operation?.fields.map((field) => field.key)).toEqual(officialInputKeys)
    expect(operation?.fields).toHaveLength(60)
    expect(operation?.responseFields).toEqual(contract?.responseFields)
    expect(operation?.fields.find((field) => field.key === 'availableMonth')).toMatchObject({
      control: 'select',
      dictType: 'LISTING_DATE',
    })
    expect(operation?.fields.find((field) => field.key === 'dimensionType')).toMatchObject({
      control: 'multi-select',
      joinWithComma: true,
      dictTypesByMarketplace: {
        MARKET_US: 'PRODUCT_SIZE_US',
        MARKET_JP: 'PRODUCT_SIZE_JP',
        MARKET_CA: 'PRODUCT_SIZE_CA',
        MARKET_UK: 'PRODUCT_SIZE_EU',
      },
    })
    expect(operation?.fields.find((field) => field.key === 'sellerNation')).toMatchObject({
      control: 'multi-select',
      dictType: 'SELLER_NATIONALITY',
      joinWithComma: true,
    })
    expect(operation?.fields.find((field) => field.key === 'order')).toMatchObject({
      control: 'sort',
      dictType: 'PRODUCT_SORT_FIELD',
      defaultValue: { field: 'PRODUCT_SORT_FIELD_TOTAL_UNITS', desc: true },
    })
  })

  it('uses the documented appendix dictionary for every structured sort field', () => {
    const expectedSortDictionaries: Record<string, string> = {
      PRODUCT_COMPETITOR_LOOKUP: 'PRODUCT_SORT_FIELD',
      PRODUCT_RESEARCH: 'PRODUCT_SORT_FIELD',
      KEYWORD_RESEARCH: 'KEYWORD_RESEARCH_SORT_FIELD',
      KEYWORD_MINER: 'ABA_SORT_FIELD',
      KEYWORD_TRAFFIC_EXTEND: 'REVERSE_MULTIPLE_ASIN_SORT_FIELD',
      ABA_RESEARCH_WEEKLY: 'ABA_SORT_FIELD',
      ABA_RESEARCH_MONTHLY: 'ABA_SORT_FIELD',
      KEYWORD_ORDER: 'KEYWORD_EXPLORER_SORT_FIELD',
      TRAFFIC_KEYWORD: 'REVERSE_ASIN_SORT_FIELD',
      TRAFFIC_LISTING_PAGE: 'RELATED_PRODUCT_ASSOCIATION_TYPE',
      TRAFFIC_SOURCE: 'ABA_SORT_FIELD',
      MARKET_RESEARCH: 'PRODUCT_SORT_FIELD',
    }

    for (const [operationId, dictType] of Object.entries(expectedSortDictionaries)) {
      expect(getGuidedSellerSpriteOperation(operationId)?.fields.find((field) => field.key === 'order'))
        .toMatchObject({ control: 'sort', dictType })
    }
  })
})

function reviewStarOptionsForTest() {
  return [
    { label: '一星', value: '1' },
    { label: '二星', value: '2' },
    { label: '三星', value: '3' },
    { label: '四星', value: '4' },
    { label: '五星', value: '5' },
  ]
}
