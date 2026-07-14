import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import SafeMarkdown from './SafeMarkdown.vue'

describe('SafeMarkdown', () => {
  it('sanitizes scripts and unsafe links', () => {
    const wrapper = mount(SafeMarkdown, {
      props: {
        content: '[危险链接](javascript:alert(1))<script>window.hacked=true</script>',
      },
    })

    expect(wrapper.html()).not.toContain('<script')
    expect(wrapper.html()).not.toContain('javascript:')
    expect(wrapper.text()).toContain('危险链接')
  })

  it('renders highlighted fenced code without executing markup', () => {
    const wrapper = mount(SafeMarkdown, {
      props: {
        content: '```ts\nconst value = "<img src=x onerror=alert(1)>"\n```',
      },
    })

    expect(wrapper.find('pre code.hljs').exists()).toBe(true)
    expect(wrapper.find('img').exists()).toBe(false)
    expect(wrapper.text()).toContain('const value')
  })
})
