import { flushPromises, mount } from '@vue/test-utils'
import { defineComponent, onMounted, ref } from 'vue'
import { createMemoryHistory, createRouter } from 'vue-router'
import { describe, expect, it } from 'vitest'

import CachedRouterView from './CachedRouterView.vue'

function statefulPage(label: string, onMount: () => void) {
  return defineComponent({
    setup() {
      const inputValue = ref('')
      onMounted(onMount)
      return { inputValue }
    },
    template: `<label>${label}<input v-model="inputValue" :aria-label="'${label}输入框'" /></label>`,
  })
}

describe('CachedRouterView', () => {
  it('keeps a cacheable page instance and local state across menu and query navigation', async () => {
    let cachedPageMounts = 0
    const CachedPage = statefulPage('缓存页面', () => { cachedPageMounts += 1 })
    const OtherPage = statefulPage('其他页面', () => undefined)
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/cached', name: 'business-cached', component: CachedPage, meta: { cacheable: true } },
        { path: '/other', name: 'business-other', component: OtherPage, meta: { cacheable: true } },
      ],
    })
    await router.push('/cached?page=1')
    await router.isReady()
    const wrapper = mount(CachedRouterView, { global: { plugins: [router] } })

    await wrapper.get('[aria-label="缓存页面输入框"]').setValue('keep-alive-proof')
    await router.push('/other')
    await flushPromises()
    await router.push('/cached?page=2')
    await flushPromises()

    expect(cachedPageMounts).toBe(1)
    expect(wrapper.get<HTMLInputElement>('[aria-label="缓存页面输入框"]').element.value)
      .toBe('keep-alive-proof')
  })

  it('remounts a page after leaving when cacheable is disabled', async () => {
    let uncachedPageMounts = 0
    const UncachedPage = statefulPage('非缓存页面', () => { uncachedPageMounts += 1 })
    const OtherPage = statefulPage('其他页面', () => undefined)
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/uncached', name: 'business-uncached', component: UncachedPage, meta: { cacheable: false } },
        { path: '/other', name: 'business-other', component: OtherPage, meta: { cacheable: true } },
      ],
    })
    await router.push('/uncached')
    await router.isReady()
    const wrapper = mount(CachedRouterView, { global: { plugins: [router] } })

    await wrapper.get('[aria-label="非缓存页面输入框"]').setValue('temporary-value')
    await router.push('/other')
    await flushPromises()
    await router.push('/uncached')
    await flushPromises()

    expect(uncachedPageMounts).toBe(2)
    expect(wrapper.get<HTMLInputElement>('[aria-label="非缓存页面输入框"]').element.value).toBe('')
  })
})
