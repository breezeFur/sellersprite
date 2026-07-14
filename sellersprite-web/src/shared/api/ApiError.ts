export class ApiError extends Error {
  readonly code: string
  readonly trackId?: string
  readonly status?: number

  constructor(code: string, message: string, options?: { trackId?: string; status?: number; cause?: unknown }) {
    super(message, { cause: options?.cause })
    this.name = 'ApiError'
    this.code = code
    this.trackId = options?.trackId
    this.status = options?.status
  }
}
