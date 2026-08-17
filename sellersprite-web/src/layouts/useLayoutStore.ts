import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const useLayoutStore = defineStore('layout', () => {
  const sidebarCollapsed = ref(false)
  const mobileSidebarOpen = ref(false)
  const workspaceFocusMode = ref(false)
  const navigationCollapsed = computed(() => (
    sidebarCollapsed.value && !mobileSidebarOpen.value
  ))

  function setSidebarCollapsed(collapsed: boolean) {
    sidebarCollapsed.value = collapsed
    mobileSidebarOpen.value = false
  }

  function toggleSidebar() {
    setSidebarCollapsed(!sidebarCollapsed.value)
  }

  function openMobileSidebar() {
    mobileSidebarOpen.value = true
  }

  function closeMobileSidebar() {
    mobileSidebarOpen.value = false
  }

  function setWorkspaceFocusMode(enabled: boolean) {
    workspaceFocusMode.value = enabled
  }

  return {
    sidebarCollapsed,
    mobileSidebarOpen,
    navigationCollapsed,
    workspaceFocusMode,
    setSidebarCollapsed,
    toggleSidebar,
    openMobileSidebar,
    closeMobileSidebar,
    setWorkspaceFocusMode,
  }
})
