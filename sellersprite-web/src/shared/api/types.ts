export interface ApiResult<T> {
  code: string
  message: string
  data: T
  traceId?: string
}

export interface PageResult<T> {
  current: number
  size: number
  total: number
  records: T[]
}
