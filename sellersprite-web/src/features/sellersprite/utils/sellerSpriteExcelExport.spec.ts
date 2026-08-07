import type { CellObject } from 'write-excel-file/browser'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import {
  buildSellerSpriteExcelSheet,
  exportSellerSpriteQueryResult,
} from './sellerSpriteExcelExport'

const excelMocks = vi.hoisted(() => ({
  toFile: vi.fn(),
  writeExcelFile: vi.fn(),
}))

vi.mock('write-excel-file/browser', () => ({
  default: excelMocks.writeExcelFile,
}))

describe('sellerSpriteExcelExport', () => {
  beforeEach(() => {
    excelMocks.toFile.mockReset().mockResolvedValue(undefined)
    excelMocks.writeExcelFile.mockReset().mockReturnValue({ toFile: excelMocks.toFile })
  })

  it('converts the current page records to ordered and safe Excel cells', () => {
    const sheet = buildSellerSpriteExcelSheet({
      page: 1,
      total: 2,
      items: [
        { asin: 'B001', title: '=HYPERLINK("https://example.com")', price: 29.9, detail: { rank: 1 } },
        { asin: 'B002', title: 'Keyboard', price: 49.9, detail: ['wireless'] },
      ],
    }, [
      { key: 'price', label: '价格' },
      { key: 'asin', label: 'ASIN' },
    ])

    expect(sheet.rowCount).toBe(2)
    expect((sheet.rows[0]?.[0] as CellObject).value).toBe('价格')
    expect((sheet.rows[0]?.[1] as CellObject).value).toBe('ASIN')
    expect(sheet.rows[1]).toEqual([
      29.9,
      'B001',
      `'${'=HYPERLINK("https://example.com")'}`,
      '{"rank":1}',
    ])
    expect(sheet.rows[2]).toEqual([49.9, 'B002', 'Keyboard', '["wireless"]'])
  })

  it('writes a deterministic xlsx file entirely in the browser', async () => {
    const result = await exportSellerSpriteQueryResult({
      operationId: 'ACCOUNT_VISITS',
      operationName: '账户/次数查询',
      data: { remaining: 100 },
      now: new Date(2026, 6, 27, 12, 34, 56),
    })

    expect(excelMocks.writeExcelFile).toHaveBeenCalledWith(
      expect.any(Array),
      expect.objectContaining({ sheet: '账户 次数查询', stickyRowsCount: 1 }),
    )
    expect(excelMocks.toFile).toHaveBeenCalledWith(
      'sellersprite-account_visits-20260727-123456.xlsx',
    )
    expect(result).toEqual({
      fileName: 'sellersprite-account_visits-20260727-123456.xlsx',
      rowCount: 1,
    })
  })

  it('rejects an empty response before loading the Excel writer', async () => {
    await expect(exportSellerSpriteQueryResult({
      operationId: 'PRODUCT_RESEARCH',
      operationName: '选产品',
      data: [],
    })).rejects.toThrow('当前查询没有可导出的数据')

    expect(excelMocks.writeExcelFile).not.toHaveBeenCalled()
  })
})
