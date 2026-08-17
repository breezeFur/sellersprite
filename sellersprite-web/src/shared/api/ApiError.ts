export class ApiError extends Error {
  readonly code: string
  readonly traceId?: string
  readonly status?: number

  constructor(code: string, message: string, options?: { traceId?: string; status?: number; cause?: unknown }) {
    super(message, { cause: options?.cause })
    this.name = 'ApiError'
    this.code = code
    this.traceId = options?.traceId
    this.status = options?.status
  }
}
