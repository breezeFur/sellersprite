import { describe, expect, it } from 'vitest'

import { adaptSellerSpritePage } from './sellerSpritePagination'

describe('adaptSellerSpritePage', () => {
  it('keeps a scaffold page and preserves non-page metadata', () => {
    const raw = {
      current: 2,
      size: 20,
      total: 61,
      records: [{ id: 21 }],
      traceId: 'trace-1',
    }

    expect(adaptSellerSpritePage(raw, {})).toEqual({
      page: { current: 2, size: 20, total: 61, records: [{ id: 21 }] },
      metadata: {
        traceId: 'trace-1',
        paginationSource: 'scaffold',
        raw,
      },
    })
  })

  it('maps a SellerSprite page and items to the shared page contract', () => {
    const raw = {
      page: 3,
      size: 15,
      total: 31,
      pages: 3,
      items: [{ keyword: 'wireless earbuds' }],
    }

    expect(adaptSellerSpritePage(raw, { page: 1, size: 50 })).toEqual({
      page: {
        current: 3,
        size: 15,
        total: 31,
        records: [{ keyword: 'wireless earbuds' }],
      },
      metadata: {
        pages: 3,
        paginationSource: 'seller-sprite',
        raw,
      },
    })
  })

  it('fills a total/items response from the submitted request page', () => {
    const raw = {
      total: 101,
      items: [{ asin: 'B07Z82895W' }],
      stats: { organic: 88 },
    }

    expect(adaptSellerSpritePage(raw, { page: 4, size: 50 })).toEqual({
      page: {
        current: 4,
        size: 50,
        total: 101,
        records: [{ asin: 'B07Z82895W' }],
      },
      metadata: {
        stats: { organic: 88 },
        paginationSource: 'request',
        raw,
      },
    })
  })

  it('returns null for non-pages and malformed page values', () => {
    expect(adaptSellerSpritePage([{ id: 1 }], { page: 1, size: 20 })).toBeNull()
    expect(adaptSellerSpritePage({ data: [{ id: 1 }] }, { page: 1, size: 20 })).toBeNull()
    expect(adaptSellerSpritePage(
      { page: '1', size: 20, total: 1, items: [{ id: 1 }] },
      { page: 1, size: 20 },
    )).toBeNull()
    expect(adaptSellerSpritePage(
      { total: 1, items: [{ id: 1 }] },
      { page: 0, size: 20 },
    )).toBeNull()
    expect(adaptSellerSpritePage(
      { current: 1, size: 20, total: 1, records: ['not-an-object'] },
      {},
    )).toBeNull()
  })
})
