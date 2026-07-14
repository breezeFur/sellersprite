export type SellerSpriteDomainId =
  | 'account'
  | 'product'
  | 'asin'
  | 'keyword'
  | 'traffic'
  | 'market'
  | 'review'
  | 'trademark'
  | 'tool'

export type SellerSpriteHttpMethod = 'GET' | 'POST'
export type SellerSpriteTransport = 'query' | 'json' | 'multipart'
export type SellerSpriteRequestPayload = Record<string, unknown>

export interface SellerSpriteDomain {
  id: SellerSpriteDomainId
  label: string
}

export interface SellerSpriteFileField {
  name: string
  label: string
  accept: string
}

export interface SellerSpriteOperation {
  id: string
  domain: SellerSpriteDomainId
  name: string
  description: string
  method: SellerSpriteHttpMethod
  path: `/sellersprite/${string}`
  transport: SellerSpriteTransport
  example: SellerSpriteRequestPayload
  fileFields?: readonly SellerSpriteFileField[]
}

export interface SellerSpriteExecutionResult {
  data: unknown
  durationMs: number
  completedAt: number
}
