import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'

import AppShell from './AppShell.vue'

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

    expect(wrapper.text()).toContain('元宝猫管理台')
    expect(wrapper.text()).toContain('首页概览')
    expect(wrapper.text()).toContain('元宝管理员')
    expect(wrapper.text()).toContain('页面内容')
  })

  it('toggles desktop sidebar and mobile drawer state', async () => {
    const wrapper = mount(AppShell)

    await wrapper.get('[aria-label="收起导航"]').trigger('click')
    expect(wrapper.classes()).toContain('is-collapsed')
    expect(wrapper.find('[aria-label="展开导航"]').exists()).toBe(true)

    await wrapper.get('[aria-label="打开导航"]').trigger('click')
    expect(wrapper.classes()).toContain('is-mobile-open')
    await wrapper.get('[aria-label="关闭导航"]').trigger('click')
    expect(wrapper.classes()).not.toContain('is-mobile-open')
  })
})
