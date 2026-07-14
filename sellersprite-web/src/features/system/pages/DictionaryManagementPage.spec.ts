import { flushPromises, mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import * as dictionaryApi from '../api/dictionaryApi'
import DictionaryManagementPage from './DictionaryManagementPage.vue'

vi.mock('../api/dictionaryApi', () => ({
  pageDictTypes: vi.fn(),
  pageDictItems: vi.fn(),
  createDictType: vi.fn(),
  updateDictType: vi.fn(),
  updateDictTypeStatus: vi.fn(),
  deleteDictType: vi.fn(),
  createDictItem: vi.fn(),
  updateDictItem: vi.fn(),
  updateDictItemStatus: vi.fn(),
  deleteDictItem: vi.fn(),
}))

describe('DictionaryManagementPage', () => {
  beforeEach(() => {
    vi.mocked(dictionaryApi.pageDictTypes).mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      records: [{ dictType: 'USER_STATUS', dictName: '用户状态', systemBuiltin: 0, sortOrder: 10, status: 1, items: [] }],
    })
    vi.mocked(dictionaryApi.pageDictItems).mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      records: [{ dictDataId: 'item-1', dictType: 'USER_STATUS', dictLabel: 'USER_STATUS_ENABLED', dictName: '启用', dictValue: '1', color: '#16a34a', defaultFlag: 1, sortOrder: 10, systemBuiltin: 0, status: 1 }],
    })
  })

  it('renders dictionary types and the selected type items', async () => {
    const wrapper = mount(DictionaryManagementPage, { global: { stubs: { Teleport: true } } })
    await flushPromises()

    expect(wrapper.text()).toContain('用户状态')
    expect(wrapper.text()).toContain('USER_STATUS')
    expect(wrapper.text()).toContain('启用')
  })
})
