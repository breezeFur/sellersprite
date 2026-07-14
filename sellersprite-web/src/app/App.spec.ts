import { createPinia, setActivePinia } from 'pinia'
import { mount } from '@vue/test-utils'
import { createMemoryHistory, createRouter } from 'vue-router'
import { describe, expect, it } from 'vitest'

import { useAuthStore } from '@/features/auth/stores/useAuthStore'

import App from './App.vue'

describe('App', () => {
  it('renders the active route', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    useAuthStore(pinia).status = 'anonymous'
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/', component: { template: '<p>控制台就绪</p>' } }],
    })
    await router.push('/')
    await router.isReady()

    const wrapper = mount(App, {
      global: { plugins: [pinia, router] },
    })

    expect(wrapper.text()).toContain('控制台就绪')
  })

  it('shows a neutral startup screen while restoring the session', async () => {
    const pinia = createPinia()
    setActivePinia(pinia)
    useAuthStore(pinia).status = 'restoring'
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [{ path: '/', component: { template: '<p>受保护内容</p>' } }],
    })
    await router.push('/')
    await router.isReady()

    const wrapper = mount(App, {
      global: { plugins: [pinia, router] },
    })

    expect(wrapper.get('[aria-label="正在恢复会话"]').text()).toContain('正在载入管理台')
    expect(wrapper.text()).not.toContain('受保护内容')
  })
})
