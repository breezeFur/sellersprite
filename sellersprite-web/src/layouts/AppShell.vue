<script setup lang="ts">
import { Close, Expand, Fold, Menu, UserFilled } from '@element-plus/icons-vue'
import { storeToRefs } from 'pinia'
import { onBeforeUnmount, onMounted } from 'vue'

import { useLayoutStore } from './useLayoutStore'

withDefaults(
  defineProps<{
    pageTitle?: string
    userName?: string
  }>(),
  {
    pageTitle: '',
    userName: '当前用户',
  },
)

const layoutStore = useLayoutStore()
const { mobileSidebarOpen, sidebarCollapsed } = storeToRefs(layoutStore)

const DESKTOP_MIN_WIDTH_PX = 769

function closeMobileSidebarAtDesktopWidth() {
  if (window.innerWidth >= DESKTOP_MIN_WIDTH_PX) {
    layoutStore.closeMobileSidebar()
  }
}

onMounted(() => {
  window.addEventListener('resize', closeMobileSidebarAtDesktopWidth)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', closeMobileSidebarAtDesktopWidth)
})
</script>

<template>
  <div
    class="app-shell"
    :class="{ 'is-collapsed': sidebarCollapsed, 'is-mobile-open': mobileSidebarOpen }"
  >
    <button
      v-if="mobileSidebarOpen"
      class="app-shell__overlay"
      type="button"
      aria-label="关闭导航"
      @click="layoutStore.closeMobileSidebar"
    />

    <aside
      id="app-sidebar-navigation"
      class="app-shell__sidebar"
      aria-label="主导航"
    >
      <div class="app-shell__brand">
        <span
          class="app-shell__brand-mark"
          aria-hidden="true"
        >元</span>
        <span class="app-shell__brand-name">opc管理台</span>
        <button
          class="app-shell__icon-button app-shell__mobile-close"
          type="button"
          title="关闭导航"
          aria-label="关闭导航"
          @click="layoutStore.closeMobileSidebar"
        >
          <Close aria-hidden="true" />
        </button>
      </div>
      <nav
        class="app-shell__navigation"
        aria-label="业务导航"
      >
        <slot name="navigation" />
      </nav>
      <div class="app-shell__sidebar-footer">
        <span class="app-shell__environment">SellerSprite Console</span>
      </div>
    </aside>

    <div class="app-shell__workspace">
      <header class="app-shell__header">
        <button
          class="app-shell__icon-button app-shell__mobile-trigger"
          type="button"
          title="打开导航"
          aria-label="打开导航"
          aria-controls="app-sidebar-navigation"
          :aria-expanded="mobileSidebarOpen"
          @click="layoutStore.openMobileSidebar"
        >
          <Menu aria-hidden="true" />
        </button>
        <button
          class="app-shell__icon-button app-shell__desktop-trigger"
          type="button"
          :title="sidebarCollapsed ? '展开导航' : '收起导航'"
          :aria-label="sidebarCollapsed ? '展开导航' : '收起导航'"
          aria-controls="app-sidebar-navigation"
          :aria-expanded="!sidebarCollapsed"
          @click="layoutStore.toggleSidebar"
        >
          <Expand
            v-if="sidebarCollapsed"
            aria-hidden="true"
          />
          <Fold
            v-else
            aria-hidden="true"
          />
        </button>

        <div class="app-shell__heading">
          <slot name="breadcrumb" />
          <h1
            v-if="pageTitle"
            class="app-shell__title"
          >
            {{ pageTitle }}
          </h1>
        </div>

        <div class="app-shell__actions">
          <slot name="header-actions" />
          <button
            class="app-shell__user"
            type="button"
          >
            <span
              class="app-shell__avatar"
              aria-hidden="true"
            >
              <UserFilled />
            </span>
            <span class="app-shell__user-name">{{ userName }}</span>
          </button>
        </div>
      </header>

      <main class="app-shell__content">
        <slot />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: var(--color-page);
}

.app-shell__sidebar {
  position: fixed;
  inset: 0 auto 0 0;
  z-index: var(--z-sidebar);
  display: grid;
  grid-template-rows: var(--header-height) minmax(0, 1fr) auto;
  width: var(--sidebar-width);
  color: var(--color-sidebar-text);
  background: var(--color-sidebar);
  transition: width var(--motion-normal) ease, transform var(--motion-normal) ease;
}

.app-shell__brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
  padding: 0 var(--space-4);
  border-bottom: 1px solid rgb(255 255 255 / 8%);
  overflow: hidden;
}

.app-shell__brand-mark {
  display: grid;
  flex: 0 0 32px;
  width: 32px;
  height: 32px;
  place-items: center;
  color: #ffffff;
  background: var(--color-brand-600);
  border-radius: var(--radius-md);
  font-size: var(--font-size-lg);
  font-weight: 700;
}

.app-shell__brand-name {
  overflow: hidden;
  color: #ffffff;
  font-size: var(--font-size-lg);
  font-weight: 650;
  line-height: 1;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-shell__navigation {
  min-height: 0;
  padding: var(--space-3) var(--space-2);
  overflow: hidden auto;
}

.app-shell__sidebar-footer {
  min-height: 44px;
  padding: var(--space-3) var(--space-4);
  border-top: 1px solid rgb(255 255 255 / 8%);
  overflow: hidden;
}

.app-shell__environment {
  color: var(--color-sidebar-muted);
  font-family: var(--font-mono);
  font-size: 10px;
  text-transform: uppercase;
  white-space: nowrap;
}

.app-shell__workspace {
  min-width: 0;
  min-height: 100vh;
  margin-left: var(--sidebar-width);
  transition: margin-left var(--motion-normal) ease;
}

.app-shell__header {
  position: sticky;
  top: 0;
  z-index: var(--z-header);
  display: flex;
  align-items: center;
  gap: var(--space-3);
  height: var(--header-height);
  padding: 0 var(--content-gutter);
  background: var(--color-surface);
  box-shadow: var(--shadow-header);
}

.app-shell__icon-button {
  display: grid;
  flex: 0 0 32px;
  width: 32px;
  height: 32px;
  padding: 7px;
  place-items: center;
  color: var(--color-text-secondary);
  background: transparent;
  border: 0;
  border-radius: var(--radius-md);
  cursor: pointer;
}

.app-shell__icon-button:hover {
  color: var(--color-brand-600);
  background: var(--color-brand-50);
}

.app-shell__icon-button svg {
  width: 18px;
  height: 18px;
}

.app-shell__mobile-trigger {
  display: none;
}

.app-shell__mobile-close {
  display: none;
  margin-left: auto;
  color: var(--color-sidebar-text);
}

.app-shell__mobile-close:hover {
  color: #ffffff;
  background: rgb(255 255 255 / 10%);
}

.app-shell__heading {
  min-width: 0;
}

.app-shell__title {
  margin: 0;
  overflow: hidden;
  color: var(--color-text);
  font-size: var(--font-size-lg);
  font-weight: 650;
  line-height: var(--line-height-tight);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-shell__actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  min-width: 0;
  margin-left: auto;
}

.app-shell__user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  min-width: 0;
  height: 36px;
  padding: 0 var(--space-2);
  color: var(--color-text-secondary);
  background: transparent;
  border: 0;
  border-radius: var(--radius-md);
  cursor: pointer;
}

.app-shell__user:hover {
  background: var(--color-surface-muted);
}

.app-shell__avatar {
  display: grid;
  flex: 0 0 28px;
  width: 28px;
  height: 28px;
  padding: 6px;
  place-items: center;
  color: var(--color-brand-700);
  background: var(--color-brand-100);
  border-radius: 50%;
}

.app-shell__avatar svg {
  width: 16px;
  height: 16px;
}

.app-shell__user-name {
  max-width: 132px;
  overflow: hidden;
  font-size: var(--font-size-sm);
  font-weight: 500;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-shell__content {
  min-width: 0;
  padding: var(--content-gutter);
}

.app-shell__overlay {
  display: none;
}

.app-shell.is-collapsed .app-shell__sidebar {
  width: var(--sidebar-width-collapsed);
}

.app-shell.is-collapsed .app-shell__workspace {
  margin-left: var(--sidebar-width-collapsed);
}

.app-shell.is-collapsed .app-shell__brand {
  justify-content: center;
  padding-inline: 0;
}

.app-shell.is-collapsed .app-shell__brand-name,
.app-shell.is-collapsed .app-shell__environment {
  display: none;
}

@media (max-width: 768px) {
  .app-shell__sidebar {
    width: min(82vw, 280px);
    box-shadow: var(--shadow-overlay);
    transform: translateX(-102%);
  }

  .app-shell__workspace,
  .app-shell.is-collapsed .app-shell__workspace {
    margin-left: 0;
  }

  .app-shell.is-collapsed .app-shell__sidebar {
    width: min(82vw, 280px);
  }

  .app-shell.is-collapsed .app-shell__brand {
    justify-content: flex-start;
    padding-inline: var(--space-4);
  }

  .app-shell.is-collapsed .app-shell__brand-name,
  .app-shell.is-collapsed .app-shell__environment {
    display: inline;
  }

  .app-shell__mobile-trigger {
    display: grid;
  }

  .app-shell__mobile-close {
    display: grid;
  }

  .app-shell__desktop-trigger {
    display: none;
  }

  .app-shell__user-name {
    display: none;
  }

  .app-shell.is-mobile-open .app-shell__sidebar {
    transform: translateX(0);
  }

  .app-shell.is-mobile-open .app-shell__overlay {
    position: fixed;
    inset: 0;
    z-index: var(--z-overlay);
    display: block;
    padding: 0;
    background: rgb(15 23 42 / 44%);
    border: 0;
  }
}
</style>
