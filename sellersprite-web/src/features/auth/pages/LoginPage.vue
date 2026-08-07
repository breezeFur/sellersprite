<script setup lang="ts">
import { Lock, User } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { ApiError } from '@/shared/api/ApiError'

import type { AuthCredentials } from '../model/auth'
import { resolveSafeLoginRedirect } from '../navigation/loginNavigation'
import { useAuthStore } from '../stores/useAuthStore'
import { usePermissionStore } from '@/features/permission/stores/usePermissionStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const permissionStore = usePermissionStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const submitError = ref('')
const form = reactive<AuthCredentials>({
  username: '',
  password: '',
  clientType: 'WEB',
  deviceName: 'SellerSprite Web',
})
const rules: FormRules<AuthCredentials> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { max: 64, message: '用户名不能超过 64 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { max: 128, message: '密码不能超过 128 个字符', trigger: 'blur' },
  ],
}

async function submit() {
  if (!formRef.value || submitting.value) {
    return
  }
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) {
    return
  }

  submitting.value = true
  submitError.value = ''
  try {
    await authStore.login(form)
    permissionStore.registerRoutes(router, authStore.menuTree, undefined, authStore.isSuperAdmin)
    const requested = resolveSafeLoginRedirect(route.query.redirect)
    const requestedRoute = requested ? router.resolve(requested) : null
    const requestedAllowed = requestedRoute?.matched.some((record) => record.meta.dynamic) === true
    const target = requestedAllowed ? requested : permissionStore.firstAccessiblePath
    await router.replace(target ?? { name: 'forbidden', query: { reason: 'no-accessible-route' } })
  } catch (error) {
    submitError.value = error instanceof ApiError ? error.message : '登录失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section
      class="login-page__brand"
      aria-label="opc管理台"
    >
      <div class="login-page__brand-lockup">
        <span
          class="login-page__brand-mark"
          aria-hidden="true"
        >元</span>
        <div>
          <h1>opc</h1>
          <p>管理控制台</p>
        </div>
      </div>
      <span class="login-page__edition">SELLERSPRITE CONSOLE</span>
    </section>

    <section
      class="login-page__form-region"
      aria-labelledby="login-title"
    >
      <div class="login-page__form-wrap">
        <header class="login-page__heading">
          <h2 id="login-title">
            登录
          </h2>
          <p>使用系统账号继续</p>
        </header>

        <ElForm
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          size="large"
          novalidate
          @submit.prevent="submit"
        >
          <ElFormItem
            label="用户名"
            prop="username"
          >
            <ElInput
              v-model="form.username"
              autocomplete="username"
              autofocus
              placeholder="请输入用户名"
            >
              <template #prefix>
                <ElIcon><User /></ElIcon>
              </template>
            </ElInput>
          </ElFormItem>
          <ElFormItem
            label="密码"
            prop="password"
          >
            <ElInput
              v-model="form.password"
              type="password"
              autocomplete="current-password"
              placeholder="请输入密码"
              show-password
            >
              <template #prefix>
                <ElIcon><Lock /></ElIcon>
              </template>
            </ElInput>
          </ElFormItem>

          <p
            v-if="submitError"
            class="login-page__error"
            role="alert"
          >
            {{ submitError }}
          </p>

          <ElButton
            class="login-page__submit"
            type="primary"
            native-type="submit"
            :loading="submitting"
          >
            登录
          </ElButton>
        </ElForm>
      </div>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  display: grid;
  min-height: 100vh;
  grid-template-columns: minmax(300px, 38%) minmax(0, 1fr);
  background: var(--color-surface);
}

.login-page__brand {
  display: flex;
  min-height: 100vh;
  padding: clamp(32px, 5vw, 72px);
  flex-direction: column;
  justify-content: space-between;
  color: #ffffff;
  background: var(--color-sidebar);
}

.login-page__brand-lockup {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.login-page__brand-mark {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  background: var(--color-brand-600);
  border-radius: var(--radius-lg);
  font-size: 22px;
  font-weight: 700;
}

.login-page__brand h1,
.login-page__brand p {
  margin: 0;
  letter-spacing: 0;
}

.login-page__brand h1 {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.3;
}

.login-page__brand p {
  margin-top: var(--space-1);
  color: var(--color-sidebar-text);
  font-size: var(--font-size-sm);
}

.login-page__edition {
  color: var(--color-sidebar-muted);
  font-family: var(--font-mono);
  font-size: 10px;
}

.login-page__form-region {
  display: grid;
  min-width: 0;
  padding: var(--space-8);
  place-items: center;
  background: var(--color-surface);
}

.login-page__form-wrap {
  width: min(100%, 380px);
}

.login-page__heading {
  margin-bottom: var(--space-8);
}

.login-page__heading h2 {
  margin: 0;
  color: var(--color-text);
  font-size: 26px;
  font-weight: 700;
  line-height: var(--line-height-tight);
}

.login-page__heading p {
  margin: var(--space-2) 0 0;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.login-page__error {
  min-height: 22px;
  margin: 0 0 var(--space-3);
  color: var(--color-danger);
  font-size: var(--font-size-sm);
}

.login-page__submit {
  width: 100%;
  height: 40px;
  margin-top: var(--space-2);
  font-size: var(--font-size-md);
}

@media (max-width: 768px) {
  .login-page {
    grid-template-columns: 1fr;
    grid-template-rows: 120px minmax(0, 1fr);
  }

  .login-page__brand {
    min-height: 0;
    padding: var(--space-5) var(--space-6);
    justify-content: center;
  }

  .login-page__brand-mark {
    width: 40px;
    height: 40px;
    font-size: var(--font-size-xl);
  }

  .login-page__brand h1 {
    font-size: var(--font-size-xl);
  }

  .login-page__edition {
    display: none;
  }

  .login-page__form-region {
    padding: var(--space-8) var(--space-5);
    align-items: start;
  }
}
</style>
