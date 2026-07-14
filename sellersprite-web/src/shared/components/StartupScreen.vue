<template>
  <main
    class="startup-screen"
    aria-label="正在恢复会话"
  >
    <div
      class="startup-screen__brand"
      aria-hidden="true"
    >
      元
    </div>
    <div
      class="startup-screen__progress"
      aria-hidden="true"
    />
    <p class="startup-screen__status">
      正在载入管理台
    </p>
  </main>
</template>

<style scoped>
.startup-screen {
  display: grid;
  min-height: 100vh;
  place-content: center;
  justify-items: center;
  color: var(--color-text-secondary);
  background: var(--color-page);
}

.startup-screen__brand {
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  color: #ffffff;
  background: var(--color-brand-600);
  border-radius: var(--radius-lg);
  font-size: var(--font-size-xl);
  font-weight: 700;
}

.startup-screen__progress {
  width: 88px;
  height: 2px;
  margin-top: var(--space-5);
  overflow: hidden;
  background: var(--color-border);
}

.startup-screen__progress::after {
  display: block;
  width: 36px;
  height: 100%;
  background: var(--color-brand-600);
  content: '';
  animation: startup-progress 900ms ease-in-out infinite;
}

.startup-screen__status {
  margin: var(--space-3) 0 0;
  font-size: var(--font-size-sm);
}

@keyframes startup-progress {
  from {
    transform: translateX(-40px);
  }
  to {
    transform: translateX(92px);
  }
}
</style>
