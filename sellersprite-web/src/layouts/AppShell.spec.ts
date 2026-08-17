import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'

import AppShell from './AppShell.vue'
import { useLayoutStore } from './useLayoutStore'

describe('AppShell', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders branded shell slots and user context', () => {
    const wrapper = mount(AppShell, {
      props: { pageTitle: '首页概览', userName: '元宝管理员' },
      slots: {
        navigation: '<a href="/dashboard">首页</a>',
        default: '<section>页面内容</section>',
      },
    })

    expect(wrapper.text()).toContain('opc管理台')
    expect(wrapper.text()).toContain('首页概览')
    expect(wrapper.text()).toContain('元宝管理员')
    expect(wrapper.text()).toContain('页面内容')
  })

  it('toggles desktop sidebar and mobile drawer state', async () => {
    const wrapper = mount(AppShell)

    await wrapper.get('[aria-label="收起导航"]').trigger('click')
    expect(wrapper.classes()).toContain('is-collapsed')
    expect(wrapper.find('[aria-label="展开导航"]').exists()).toBe(true)
    expect(wrapper.get('[aria-label="展开导航"]').attributes('aria-expanded')).toBe('false')

    await wrapper.get('[aria-label="打开导航"]').trigger('click')
    expect(wrapper.classes()).toContain('is-mobile-open')
    expect(wrapper.get('[aria-label="打开导航"]').attributes('aria-expanded')).toBe('true')
    await wrapper.get('.app-shell__mobile-close').trigger('click')
    expect(wrapper.classes()).not.toContain('is-mobile-open')
  })

  it('marks the shell as focused workspace while keeping a reachable top trigger', () => {
    const pinia = createPinia()
    const layoutStore = useLayoutStore(pinia)
    layoutStore.setWorkspaceFocusMode(true)
    const wrapper = mount(AppShell, { global: { plugins: [pinia] } })

    expect(wrapper.classes()).toContain('is-workspace-focus')
    expect(wrapper.get('.app-shell__header')).toBeTruthy()
  })

  it('closes the mobile drawer when the viewport crosses into desktop width', async () => {
    const originalInnerWidth = window.innerWidth
    Object.defineProperty(window, 'innerWidth', { configurable: true, value: 640 })
    const wrapper = mount(AppShell)

    try {
      await wrapper.get('[aria-label="打开导航"]').trigger('click')
      expect(wrapper.classes()).toContain('is-mobile-open')

      Object.defineProperty(window, 'innerWidth', { configurable: true, value: 1024 })
      window.dispatchEvent(new Event('resize'))
      await wrapper.vm.$nextTick()

      expect(wrapper.classes()).not.toContain('is-mobile-open')
    } finally {
      wrapper.unmount()
      Object.defineProperty(window, 'innerWidth', {
        configurable: true,
        value: originalInnerWidth,
      })
    }
  })
})
