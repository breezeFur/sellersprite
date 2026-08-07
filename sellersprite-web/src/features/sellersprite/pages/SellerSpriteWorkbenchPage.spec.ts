import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import { ApiError } from '@/shared/api/ApiError'

import * as sellerSpriteApi from '../api/sellerspriteApi'
import type { SellerSpriteExecutionResult } from '../model/sellersprite'
import * as excelExport from '../utils/sellerSpriteExcelExport'
import SellerSpriteWorkbenchPage from './SellerSpriteWorkbenchPage.vue'

const { routeState } = vi.hoisted(() => ({
  routeState: { path: '/sellersprite/workbench' },
}))

vi.mock('vue-router', () => ({
  useRoute: () => routeState,
}))

vi.mock('../api/sellerspriteApi', async (importOriginal) => {
  const original = await importOriginal<typeof import('../api/sellerspriteApi')>()
  return {
    ...original,
    executeSellerSpriteOperation: vi.fn(),
  }
})

vi.mock('../utils/sellerSpriteExcelExport', async (importOriginal) => {
  const original = await importOriginal<typeof import('../utils/sellerSpriteExcelExport')>()
  return {
    ...original,
    exportSellerSpriteQueryResult: vi.fn(),
  }
})

describe('SellerSpriteWorkbenchPage', () => {
  beforeEach(() => {
    routeState.path = '/sellersprite/workbench'
    vi.mocked(sellerSpriteApi.executeSellerSpriteOperation).mockReset()
    vi.mocked(excelExport.exportSellerSpriteQueryResult).mockReset().mockResolvedValue({
      fileName: 'sellersprite-result.xlsx',
      rowCount: 1,
    })
  })

  it('opens the guided query mode by default and keeps the API debugger available', async () => {
    const wrapper = mount(SellerSpriteWorkbenchPage)

    expect(wrapper.find('[aria-label="SellerSprite 引导查询"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('45 个官方接口')
    await wrapper.get('[aria-label="SellerSprite 工作台模式"] button:nth-child(2)').trigger('click')

    expect(wrapper.find('[aria-label="SellerSprite API 调试台"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('45 个固定代理操作')
  })

  it('opens the business domain selected by a SellerSprite submenu route', () => {
    routeState.path = '/sellersprite/market'
    const wrapper = mount(SellerSpriteWorkbenchPage)

    expect(wrapper.get('[data-guided-group-id="market"]').classes()).toContain('active')
    expect(wrapper.find('[data-guided-operation-id="MARKET_RESEARCH"]').exists()).toBe(true)
  })

  it('switches domains and loads the selected operation example', async () => {
    const wrapper = await mountDebugWorkbench()

    await wrapper.get('[aria-label="SellerSprite 业务域"] button:nth-child(1)').trigger('click')
    await wrapper.get('[data-operation-id="ACCOUNT_VISITS"]').trigger('click')
    await wrapper.get('[aria-label="SellerSprite 业务域"] button:nth-child(3)').trigger('click')
    await wrapper.get('[data-operation-id="ASIN_DETAIL"]').trigger('click')

    expect(wrapper.text()).toContain('/api/sellersprite/asins/detail')
    expect(wrapper.get('[aria-label="SellerSprite 请求 JSON"]').element).toHaveProperty(
      'value',
      expect.stringContaining('B07Z82895W'),
    )
  })

  it('rejects invalid JSON without sending a request', async () => {
    const wrapper = await mountDebugWorkbench()
    await wrapper.get('[aria-label="SellerSprite 请求 JSON"]').setValue('{"marketplace":}')
    await wrapper.get('[aria-label="发送 SellerSprite 请求"]').trigger('click')

    expect(wrapper.text()).toContain('请求内容不是有效的 JSON')
    expect(sellerSpriteApi.executeSellerSpriteOperation).not.toHaveBeenCalled()
  })

  it('prevents duplicate submissions and renders success timing data', async () => {
    let resolveRequest!: (value: SellerSpriteExecutionResult) => void
    vi.mocked(sellerSpriteApi.executeSellerSpriteOperation).mockImplementation(() => (
      new Promise((resolve) => { resolveRequest = resolve })
    ))
    const wrapper = await mountDebugWorkbench()
    const submit = wrapper.get('[aria-label="发送 SellerSprite 请求"]')

    await submit.trigger('click')
    await submit.trigger('click')
    expect(sellerSpriteApi.executeSellerSpriteOperation).toHaveBeenCalledTimes(1)

    resolveRequest({ data: { remaining: 100 }, durationMs: 42, completedAt: Date.now() })
    await flushPromises()

    expect(wrapper.get('[aria-label="SellerSprite 响应"]').text()).toContain('调用成功')
    expect(wrapper.get('[aria-label="SellerSprite 响应"]').text()).toContain('42 ms')
    expect(wrapper.get('[aria-label="SellerSprite 响应"]').text()).toContain('remaining')
  })

  it('keeps stable business error details and trackId', async () => {
    vi.mocked(sellerSpriteApi.executeSellerSpriteOperation).mockRejectedValue(
      new ApiError('S429', 'SellerSprite 接口可用次数已耗尽', { trackId: 'track-seller-1' }),
    )
    const wrapper = await mountDebugWorkbench()

    await wrapper.get('[aria-label="发送 SellerSprite 请求"]').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('S429')
    expect(wrapper.text()).toContain('SellerSprite 接口可用次数已耗尽')
    expect(wrapper.text()).toContain('track-seller-1')
  })

  it('exports the latest successful API debugger response', async () => {
    const response = { remaining: 100, resetAt: 1784000000000 }
    vi.mocked(sellerSpriteApi.executeSellerSpriteOperation).mockResolvedValue({
      data: response,
      durationMs: 7,
      completedAt: Date.now(),
    })
    const wrapper = await mountDebugWorkbench()
    expect(wrapper.get('[aria-label="导出 API 调试结果"]').attributes('disabled')).toBeDefined()

    await wrapper.get('[aria-label="发送 SellerSprite 请求"]').trigger('click')
    await flushPromises()
    await wrapper.get('[aria-label="导出 API 调试结果"]').trigger('click')
    await flushPromises()

    expect(excelExport.exportSellerSpriteQueryResult).toHaveBeenCalledWith({
      operationId: 'PRODUCT_COMPETITOR_LOOKUP',
      operationName: '查竞品',
      data: response,
    })
  })

  it('shows an explicit file selector for multipart operations', async () => {
    const wrapper = await mountDebugWorkbench()
    await wrapper.get('[aria-label="SellerSprite 业务域"] button:nth-child(9)').trigger('click')
    await wrapper.get('[data-operation-id="OCR"]').trigger('click')

    const fileInput = wrapper.get('input[aria-label="待识别图片"]')
    const image = new File(['image'], 'ocr.png', { type: 'image/png' })
    Object.defineProperty(fileInput.element, 'files', { configurable: true, value: [image] })
    await fileInput.trigger('change')

    expect(wrapper.text()).toContain('ocr.png')
  })
})

async function mountDebugWorkbench() {
  const wrapper = mount(SellerSpriteWorkbenchPage)
  await wrapper.get('[aria-label="SellerSprite 工作台模式"] button:nth-child(2)').trigger('click')
  return wrapper
}
