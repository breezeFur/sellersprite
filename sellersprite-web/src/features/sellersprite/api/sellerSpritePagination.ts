import type { PageResult } from '@/shared/api/types'

import type { SellerSpriteRequestPayload } from '../model/sellersprite'

export type SellerSpritePaginationSource = 'scaffold' | 'seller-sprite' | 'request'

export interface SellerSpritePageAdaptation {
  page: PageResult<Record<string, unknown>>
  metadata: Record<string, unknown>
}

const SCAFFOLD_KEYS = new Set(['current', 'size', 'total', 'records'])
const SELLER_SPRITE_KEYS = new Set(['page', 'size', 'total', 'items'])
const REQUEST_PAGE_KEYS = new Set(['total', 'items'])

export function adaptSellerSpritePage(
  data: unknown,
  requestPayload: SellerSpriteRequestPayload,
): SellerSpritePageAdaptation | null {
  if (!isRecord(data)) {
    return null
  }

  if ('records' in data || 'current' in data) {
    const page = createPage(data.current, data.size, data.total, data.records)
    return page ? createAdaptation(page, data, 'scaffold', SCAFFOLD_KEYS) : null
  }

  if ('page' in data || 'size' in data) {
    const page = createPage(data.page, data.size, data.total, data.items)
    return page ? createAdaptation(page, data, 'seller-sprite', SELLER_SPRITE_KEYS) : null
  }

  if ('total' in data || 'items' in data) {
    const page = createPage(requestPayload.page, requestPayload.size, data.total, data.items)
    return page ? createAdaptation(page, data, 'request', REQUEST_PAGE_KEYS) : null
  }

  return null
}

function createPage(
  current: unknown,
  size: unknown,
  total: unknown,
  records: unknown,
): PageResult<Record<string, unknown>> | null {
  if (!isPositiveInteger(current) || !isPositiveInteger(size) || !isNonNegativeInteger(total)) {
    return null
  }
  if (!Array.isArray(records) || !records.every(isRecord)) {
    return null
  }
  return { current, size, total, records }
}

function createAdaptation(
  page: PageResult<Record<string, unknown>>,
  raw: Record<string, unknown>,
  paginationSource: SellerSpritePaginationSource,
  paginationKeys: ReadonlySet<string>,
): SellerSpritePageAdaptation {
  const metadata: Record<string, unknown> = {}
  for (const [key, value] of Object.entries(raw)) {
    if (!paginationKeys.has(key)) {
      metadata[key] = value
    }
  }
  metadata.paginationSource = paginationSource
  metadata.raw = raw
  return { page, metadata }
}

function isPositiveInteger(value: unknown): value is number {
  return typeof value === 'number' && Number.isInteger(value) && value > 0
}

function isNonNegativeInteger(value: unknown): value is number {
  return typeof value === 'number' && Number.isInteger(value) && value >= 0
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return value !== null && typeof value === 'object' && !Array.isArray(value)
}
