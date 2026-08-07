import type { Cell, CellObject, SheetData } from 'write-excel-file/browser'

const COLLECTION_KEYS = ['records', 'items', 'list', 'rows', 'data'] as const
const MAX_CELL_LENGTH = 32_767
const MAX_COLUMN_WIDTH = 48
const MIN_COLUMN_WIDTH = 12
const FORMULA_PREFIX_PATTERN = /^\s*[=+\-@]/

type ExportRecord = Record<string, unknown>
export interface SellerSpriteExcelColumn {
  key: string
  label: string
}

export interface SellerSpriteExcelExportOptions {
  operationId: string
  operationName: string
  data: unknown
  columns?: SellerSpriteExcelColumn[]
  now?: Date
}

export interface SellerSpriteExcelSheet {
  rows: SheetData
  columns: Array<{ width: number }>
  rowCount: number
}

export interface SellerSpriteExcelExportResult {
  fileName: string
  rowCount: number
}

export async function exportSellerSpriteQueryResult(
  options: SellerSpriteExcelExportOptions,
): Promise<SellerSpriteExcelExportResult> {
  const sheet = buildSellerSpriteExcelSheet(options.data, options.columns)
  if (sheet.rowCount === 0) {
    throw new Error('当前查询没有可导出的数据')
  }

  const fileName = createExcelFileName(options.operationId, options.now ?? new Date())
  const { default: writeExcelFile } = await import('write-excel-file/browser')
  await writeExcelFile(sheet.rows, {
    sheet: normalizeSheetName(options.operationName),
    columns: sheet.columns,
    stickyRowsCount: 1,
  }).toFile(fileName)

  return { fileName, rowCount: sheet.rowCount }
}

export function buildSellerSpriteExcelSheet(
  data: unknown,
  preferredColumns: SellerSpriteExcelColumn[] = [],
): SellerSpriteExcelSheet {
  const records = normalizeExportRecords(data)
  const keys = collectColumnKeys(records, preferredColumns)
  if (records.length === 0 || keys.length === 0) {
    return { rows: [], columns: [], rowCount: 0 }
  }

  const labels = new Map(preferredColumns.map((column) => [column.key, column.label]))
  const header = keys.map<CellObject>((key) => ({
    value: labels.get(key) ?? key,
    fontWeight: 'bold',
    backgroundColor: '#e8f0fc',
    align: 'center',
  }))
  const body = records.map((record) => keys.map((key) => toExcelCellValue(record[key])))
  const rows: SheetData = [header, ...body]

  return {
    rows,
    columns: keys.map((_key, index) => ({ width: calculateColumnWidth(rows, index) })),
    rowCount: records.length,
  }
}

export function normalizeExportRecords(data: unknown): ExportRecord[] {
  if (Array.isArray(data)) {
    return data.map(toExportRecord)
  }
  if (isRecord(data)) {
    const collection = COLLECTION_KEYS
      .map((key) => data[key])
      .find((value): value is unknown[] => Array.isArray(value))
    return collection ? collection.map(toExportRecord) : [data]
  }
  if (data === undefined || data === null || data === '') {
    return []
  }
  return [{ value: data }]
}

function collectColumnKeys(
  records: ExportRecord[],
  preferredColumns: SellerSpriteExcelColumn[],
) {
  const actualKeys = new Set(records.flatMap((record) => Object.keys(record)))
  const orderedKeys = preferredColumns
    .map((column) => column.key)
    .filter((key, index, keys) => actualKeys.delete(key) && keys.indexOf(key) === index)
  return [...orderedKeys, ...actualKeys]
}

function toExportRecord(value: unknown): ExportRecord {
  return isRecord(value) ? value : { value }
}

function toExcelCellValue(value: unknown): Cell {
  if (value === undefined || value === null) return null
  if (value instanceof Date) return value
  if (typeof value === 'boolean') return value
  if (typeof value === 'number') return Number.isFinite(value) ? value : String(value)
  if (typeof value === 'string') return sanitizeText(value)
  if (typeof value === 'object') {
    try {
      return sanitizeText(JSON.stringify(value))
    } catch {
      return sanitizeText(String(value))
    }
  }
  return sanitizeText(String(value))
}

function sanitizeText(value: string) {
  const safeValue = FORMULA_PREFIX_PATTERN.test(value) ? `'${value}` : value
  return safeValue.length > MAX_CELL_LENGTH
    ? `${safeValue.slice(0, MAX_CELL_LENGTH - 3)}...`
    : safeValue
}

function calculateColumnWidth(rows: SheetData, columnIndex: number) {
  const contentWidth = rows.reduce((width, row) => {
    const cell = row[columnIndex]
    const value = isExcelCell(cell) ? cell.value : cell
    return Math.max(width, String(value ?? '').length + 2)
  }, MIN_COLUMN_WIDTH)
  return Math.min(contentWidth, MAX_COLUMN_WIDTH)
}

function createExcelFileName(operationId: string, now: Date) {
  const safeOperationId = operationId.toLowerCase().replace(/[^a-z0-9._-]+/g, '-') || 'query'
  const timestamp = [
    now.getFullYear(),
    padDatePart(now.getMonth() + 1),
    padDatePart(now.getDate()),
    '-',
    padDatePart(now.getHours()),
    padDatePart(now.getMinutes()),
    padDatePart(now.getSeconds()),
  ].join('')
  return `sellersprite-${safeOperationId}-${timestamp}.xlsx`
}

function normalizeSheetName(operationName: string) {
  return operationName.replace(/[\\/:*?[\]]/g, ' ').trim().slice(0, 31) || '查询结果'
}

function padDatePart(value: number) {
  return String(value).padStart(2, '0')
}

function isRecord(value: unknown): value is ExportRecord {
  return value !== null && typeof value === 'object' && !Array.isArray(value)
}

function isExcelCell(value: Cell): value is CellObject {
  return isRecord(value) && 'value' in value
}
