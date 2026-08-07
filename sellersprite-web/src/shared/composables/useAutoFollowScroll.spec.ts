import { describe, expect, it } from 'vitest'

import { useAutoFollowScroll } from './useAutoFollowScroll'

describe('useAutoFollowScroll', () => {
  it('follows by default, pauses away from the bottom, and resumes at the bottom', async () => {
    const element = document.createElement('div')
    let scrollHeight = 500
    Object.defineProperty(element, 'scrollHeight', {
      configurable: true,
      get: () => scrollHeight,
    })
    Object.defineProperty(element, 'clientHeight', { configurable: true, value: 100 })
    const { handleScroll, setScrollContainer, scrollToBottom } = useAutoFollowScroll()
    setScrollContainer(element)

    await scrollToBottom()
    expect(element.scrollTop).toBe(500)

    scrollHeight = 700
    handleScroll()
    await scrollToBottom()
    expect(element.scrollTop).toBe(700)

    element.scrollTop = 120
    handleScroll()
    scrollHeight = 800
    await scrollToBottom()
    expect(element.scrollTop).toBe(120)

    element.scrollTop = 700
    handleScroll()
    scrollHeight = 900
    await scrollToBottom()
    expect(element.scrollTop).toBe(900)
  })
})
