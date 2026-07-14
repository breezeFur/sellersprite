import { describe, expect, it } from 'vitest'

import type { ApiClient, ApiRequestConfig } from '@/shared/api/http'

import { sellerSpriteOperations } from '../model/operations'
import type { SellerSpriteOperation } from '../model/sellersprite'
import {
  buildSellerSpriteRequest,
  executeSellerSpriteOperation,
  parseSellerSpriteRequest,
  SellerSpriteRequestJsonError,
} from './sellerspriteApi'

function operation(operationId: string) {
  const result = sellerSpriteOperations.find((item) => item.id === operationId)
  if (!result) throw new Error(`Missing test operation ${operationId}`)
  return result as SellerSpriteOperation
}

describe('sellerspriteApi', () => {
  it('parses only JSON objects and reports actionable errors', () => {
    expect(parseSellerSpriteRequest('{"marketplace":"US"}')).toEqual({ marketplace: 'US' })
    expect(() => parseSellerSpriteRequest('{"marketplace":}')).toThrow(SellerSpriteRequestJsonError)
    expect(() => parseSellerSpriteRequest('["US"]')).toThrow('请求内容必须是 JSON 对象')
  })

  it('converts GET payloads to repeated query parameters on the relative proxy URL', () => {
    const config = buildSellerSpriteRequest(operation('ASIN_DETAIL'), {
      marketplace: 'US',
      asin: 'B07Z82895W',
      fields: ['title', 'price'],
      optional: null,
    })

    expect(config.url).toBe('/sellersprite/asins/detail')
    expect(config.url).not.toContain('/api/api')
    expect(config.method).toBe('GET')
    expect(config.params).toBeInstanceOf(URLSearchParams)
    const params = config.params as URLSearchParams
    expect(params.getAll('fields')).toEqual(['title', 'price'])
    expect(params.has('optional')).toBe(false)
  })

  it('sends ordinary POST payloads as JSON data without changing the fixed path', () => {
    const payload = { marketplace: 'US', page: 1 }
    const config = buildSellerSpriteRequest(operation('PRODUCT_RESEARCH'), payload)

    expect(config).toMatchObject({
      method: 'POST',
      url: '/sellersprite/products/research',
      data: payload,
    })
  })

  it('builds multipart data with repeated arrays, booleans, files, and omitted nulls', () => {
    const image = new File(['image'], 'brand.png', { type: 'image/png' })
    const config = buildSellerSpriteRequest(
      operation('GLOBAL_BRAND_LIST'),
      {
        office: ['US', 'EU'],
        text: 'CHINESE',
        exact: false,
        ignored: null,
        imageFile: 'C:\\fakepath\\must-not-be-sent.png',
      },
      { imageFile: image },
    )

    expect(config.url).toBe('/sellersprite/trademarks/search')
    expect(config.data).toBeInstanceOf(FormData)
    const formData = config.data as FormData
    expect(formData.getAll('office')).toEqual(['US', 'EU'])
    expect(formData.get('exact')).toBe('false')
    expect(formData.has('ignored')).toBe(false)
    expect(formData.getAll('imageFile')).toHaveLength(1)
    expect(formData.get('imageFile')).toBeInstanceOf(File)
  })

  it('executes through the shared client contract and returns timing metadata', async () => {
    let requestConfig: ApiRequestConfig | undefined
    const client: ApiClient = {
      async request<T>(config: ApiRequestConfig) {
        requestConfig = config
        return { items: [{ asin: 'B07Z82895W' }] } as T
      },
    }

    const result = await executeSellerSpriteOperation(
      operation('TRAFFIC_KEYWORD'),
      { marketplace: 'US', asin: 'B07Z82895W' },
      {},
      client,
    )

    expect(requestConfig?.url).toBe('/sellersprite/traffic/keywords/reverse')
    expect(result.data).toEqual({ items: [{ asin: 'B07Z82895W' }] })
    expect(result.durationMs).toBeGreaterThanOrEqual(0)
    expect(result.completedAt).toBeGreaterThan(0)
  })
})
