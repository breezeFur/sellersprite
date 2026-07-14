import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import * as authApi from '../api/authApi'
import type { AuthCredentials, AuthLoginResult, AuthMenu, AuthRole, AuthSession, AuthUser } from '../model/auth'

export type AuthStatus = 'idle' | 'restoring' | 'authenticated' | 'anonymous'

const SUPER_ADMIN_ROLE_CODE = 'admin'

export const useAuthStore = defineStore('auth', () => {
  const status = ref<AuthStatus>('idle')
  const accessToken = ref<string | null>(null)
  const expiresAt = ref<number | null>(null)
  const user = ref<AuthUser | null>(null)
  const roles = ref<AuthRole[]>([])
  const menuTree = ref<AuthMenu[]>([])
  const permissionCodes = ref(new Set<string>())
  const permissionVersion = ref(0)
  let restorePromise: Promise<boolean> | null = null

  const isAuthenticated = computed(() => status.value === 'authenticated' && Boolean(accessToken.value))
  const isSuperAdmin = computed(() => roles.value.some((role) => role.roleCode === SUPER_ADMIN_ROLE_CODE))

  function applySession(session: AuthSession) {
    user.value = session.user
    roles.value = session.roles
    menuTree.value = session.menuTree
    permissionCodes.value = new Set(session.permissionCodes)
    permissionVersion.value = session.permissionVersion
  }

  function applyLogin(result: AuthLoginResult) {
    accessToken.value = result.accessToken
    expiresAt.value = result.expiresAt
    applySession(result)
    status.value = 'authenticated'
  }

  function expireSession() {
    status.value = 'anonymous'
    accessToken.value = null
    expiresAt.value = null
    user.value = null
    roles.value = []
    menuTree.value = []
    permissionCodes.value = new Set()
    permissionVersion.value = 0
  }

  async function login(credentials: AuthCredentials) {
    const result = await authApi.login(credentials)
    applyLogin(result)
  }

  async function refreshAccessToken() {
    const result = await authApi.refresh()
    applyLogin(result)
    return result.accessToken
  }

  function restore() {
    if (restorePromise) {
      return restorePromise
    }
    status.value = 'restoring'
    restorePromise = refreshAccessToken()
      .then(() => true)
      .catch(() => {
        expireSession()
        return false
      })
      .finally(() => {
        restorePromise = null
      })
    return restorePromise
  }

  async function reloadSession() {
    const currentSession = await authApi.session()
    applySession(currentSession)
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // Local session cleanup is required even when the server is unreachable.
    } finally {
      expireSession()
    }
  }

  return {
    status,
    accessToken,
    expiresAt,
    user,
    roles,
    menuTree,
    permissionCodes,
    permissionVersion,
    isAuthenticated,
    isSuperAdmin,
    login,
    restore,
    refreshAccessToken,
    reloadSession,
    logout,
    expireSession,
  }
})
