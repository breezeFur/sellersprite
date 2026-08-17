import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it } from 'vitest'

import { useLayoutStore } from './useLayoutStore'

describe('useLayoutStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('keeps the mobile drawer expanded without losing the desktop collapse preference', () => {
    const store = useLayoutStore()

    store.toggleSidebar()
    expect(store.sidebarCollapsed).toBe(true)
    expect(store.navigationCollapsed).toBe(true)

    store.openMobileSidebar()
    expect(store.mobileSidebarOpen).toBe(true)
    expect(store.navigationCollapsed).toBe(false)

    store.closeMobileSidebar()
    expect(store.mobileSidebarOpen).toBe(false)
    expect(store.navigationCollapsed).toBe(true)
  })

  it('sets the desktop collapse state idempotently and closes any mobile drawer', () => {
    const store = useLayoutStore()

    store.openMobileSidebar()
    store.setSidebarCollapsed(true)
    store.setSidebarCollapsed(true)

    expect(store.sidebarCollapsed).toBe(true)
    expect(store.mobileSidebarOpen).toBe(false)
    expect(store.navigationCollapsed).toBe(true)
  })

  it('toggles the workspace focus mode independently from navigation state', () => {
    const store = useLayoutStore()

    expect(store.workspaceFocusMode).toBe(false)
    store.setSidebarCollapsed(true)
    store.setWorkspaceFocusMode(true)
    store.setWorkspaceFocusMode(true)

    expect(store.workspaceFocusMode).toBe(true)
    expect(store.sidebarCollapsed).toBe(true)

    store.setWorkspaceFocusMode(false)
    expect(store.workspaceFocusMode).toBe(false)
    expect(store.sidebarCollapsed).toBe(true)
  })
})
