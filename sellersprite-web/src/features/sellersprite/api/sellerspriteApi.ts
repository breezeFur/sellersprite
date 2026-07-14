import { apiClient, type ApiClient, type ApiRequestConfig } from '@/shared/api/http'

import type {
  SellerSpriteExecutionResult,
  SellerSpriteOperation,
  SellerSpriteRequestPayload,
} from '../model/sellersprite'

export class SellerSpriteRequestJsonError extends Error {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, options)
    this.name = 'SellerSpriteRequestJsonError'
  }
}

export function parseSellerSpriteRequest(source: string): SellerSpriteRequestPayload {
  let value: unknown
  try {
    value = JSON.parse(source)
  } catch (error) {
    throw new SellerSpriteRequestJsonError('请求内容不是有效的 JSON，请检查逗号、引号和括号。', {
      cause: error,
    })
  }
  if (!isPlainObject(value)) {
    throw new SellerSpriteRequestJsonError('请求内容必须是 JSON 对象，例如 {"marketplace":"US"}。')
  }
  return value
}

export function buildSellerSpriteRequest(
  operation: SellerSpriteOperation,
  payload: SellerSpriteRequestPayload,
  files: Readonly<Record<string, File | undefined>> = {},
): ApiRequestConfig {
  if (operation.transport === 'query') {
    return {
      method: operation.method,
      url: operation.path,
      params: toQueryParams(payload),
    }
  }
  if (operation.transport === 'multipart') {
    const fileFieldNames = new Set(operation.fileFields?.map((field) => field.name) ?? [])
    return {
      method: operation.method,
      url: operation.path,
      data: toFormData(payload, files, fileFieldNames),
    }
  }
  return {
    method: operation.method,
    url: operation.path,
    data: payload,
  }
}

export async function executeSellerSpriteOperation(
  operation: SellerSpriteOperation,
  payload: SellerSpriteRequestPayload,
  files: Readonly<Record<string, File | undefined>> = {},
  client: ApiClient = apiClient,
): Promise<SellerSpriteExecutionResult> {
  const startedAt = performance.now()
  const data = await client.request<unknown>(buildSellerSpriteRequest(operation, payload, files))
  return {
    data,
    durationMs: Math.max(0, Math.round(performance.now() - startedAt)),
    completedAt: Date.now(),
  }
}

export function toQueryParams(payload: SellerSpriteRequestPayload) {
  const params = new URLSearchParams()
  for (const [name, value] of Object.entries(payload)) {
    appendValues(params, name, value)
  }
  return params
}

export function toFormData(
  payload: SellerSpriteRequestPayload,
  files: Readonly<Record<string, File | undefined>> = {},
  fileFieldNames: ReadonlySet<string> = new Set(),
) {
  const formData = new FormData()
  for (const [name, value] of Object.entries(payload)) {
    if (fileFieldNames.has(name)) {
      continue
    }
    appendValues(formData, name, value)
  }
  for (const [name, file] of Object.entries(files)) {
    if (file) {
      formData.append(name, file, file.name)
    }
  }
  return formData
}

function appendValues(target: URLSearchParams | FormData, name: string, value: unknown) {
  if (value === null || value === undefined) {
    return
  }
  if (Array.isArray(value)) {
    for (const item of value) {
      appendValues(target, name, item)
    }
    return
  }
  if (value instanceof Blob) {
    if (target instanceof FormData) {
      target.append(name, value)
    }
    return
  }
  const serialized = typeof value === 'object' ? JSON.stringify(value) : String(value)
  target.append(name, serialized)
}

function isPlainObject(value: unknown): value is SellerSpriteRequestPayload {
  return value !== null && typeof value === 'object' && !Array.isArray(value)
}
