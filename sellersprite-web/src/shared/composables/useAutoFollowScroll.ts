import { nextTick, readonly, ref } from 'vue'

const DEFAULT_BOTTOM_THRESHOLD = 8
const SCROLL_DIRECTION_TOLERANCE = 1

export function useAutoFollowScroll(bottomThreshold = DEFAULT_BOTTOM_THRESHOLD) {
  const scrollContainer = ref<HTMLElement>()
  const autoFollowEnabled = ref(true)
  let lastScrollTop = 0

  function handleScroll() {
    const element = scrollContainer.value
    if (!element) {
      return
    }
    const currentScrollTop = element.scrollTop
    const isAtBottom = element.scrollHeight - currentScrollTop - element.clientHeight
      <= bottomThreshold
    // SSE content can grow before an earlier scroll event runs; unchanged position is not user intent.
    if (isAtBottom) {
      autoFollowEnabled.value = true
    } else if (currentScrollTop < lastScrollTop - SCROLL_DIRECTION_TOLERANCE) {
      autoFollowEnabled.value = false
    }
    lastScrollTop = currentScrollTop
  }

  function setScrollContainer(element: unknown) {
    const nextElement = element instanceof HTMLElement ? element : undefined
    if (scrollContainer.value === nextElement) return
    scrollContainer.value = nextElement
    if (nextElement) lastScrollTop = nextElement.scrollTop
  }

  async function scrollToBottom(force = false) {
    if (force) {
      autoFollowEnabled.value = true
    }
    const shouldFollow = force || autoFollowEnabled.value
    await nextTick()
    const element = scrollContainer.value
    if (element && shouldFollow) {
      element.scrollTop = element.scrollHeight
      lastScrollTop = element.scrollTop
    }
  }

  function resetAutoFollow() {
    autoFollowEnabled.value = true
    lastScrollTop = scrollContainer.value?.scrollTop ?? 0
  }

  return {
    handleScroll,
    isAutoFollowing: readonly(autoFollowEnabled),
    resetAutoFollow,
    setScrollContainer,
    scrollToBottom,
  }
}
