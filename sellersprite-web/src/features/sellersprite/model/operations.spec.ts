import { describe, expect, it } from 'vitest'

import { getSellerSpriteOperation, sellerSpriteDomains, sellerSpriteOperations } from './operations'

describe('sellerSpriteOperations', () => {
  it('locks the fixed 45-operation catalog across nine domains', () => {
    expect(sellerSpriteOperations).toHaveLength(45)
    expect(sellerSpriteDomains).toHaveLength(9)
    expect(new Set(sellerSpriteOperations.map((operation) => operation.id)).size).toBe(45)
    expect(new Set(sellerSpriteOperations.map((operation) => operation.domain))).toEqual(
      new Set(sellerSpriteDomains.map((domain) => domain.id)),
    )
  })

  it('contains only unique fixed proxy method and path pairs', () => {
    const signatures = sellerSpriteOperations.map(
      (operation) => `${operation.method} ${operation.path}`,
    )

    expect(new Set(signatures).size).toBe(45)
    expect(sellerSpriteOperations.every((operation) => operation.path.startsWith('/sellersprite/'))).toBe(true)
    expect(sellerSpriteOperations.every((operation) => !operation.path.includes('://'))).toBe(true)
  })

  it('declares examples, transports, and explicit multipart file fields', () => {
    expect(sellerSpriteOperations.every((operation) => operation.example !== null)).toBe(true)
    expect(new Set(sellerSpriteOperations.map((operation) => operation.transport))).toEqual(
      new Set(['query', 'json', 'multipart']),
    )
    expect(sellerSpriteOperations.filter((operation) => operation.transport === 'multipart')).toHaveLength(3)
    expect(sellerSpriteOperations
      .filter((operation) => operation.transport === 'multipart')
      .every((operation) => operation.fileFields?.length === 1)).toBe(true)
  })

  it('uses the backend date field for weekly and monthly ABA examples', () => {
    expect(getSellerSpriteOperation('ABA_RESEARCH_WEEKLY')?.example).toMatchObject({
      date: '20250705',
    })
    expect(getSellerSpriteOperation('ABA_RESEARCH_MONTHLY')?.example).toMatchObject({
      date: '202507',
    })
  })
})
