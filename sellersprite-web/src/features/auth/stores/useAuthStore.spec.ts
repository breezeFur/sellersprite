import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import * as authApi from '../api/authApi'
import type { AuthLoginResult } from '../model/auth'
import { useAuthStore } from './useAuthStore'

vi.mock('../api/authApi', () => ({
  login: vi.fn(),
  logout: vi.fn(),
  refresh: vi.fn(),
  session: vi.fn(),
}))

const loginResult: AuthLoginResult = {
  accessToken: 'memory-only-token',
  tokenType: 'Bearer',
  expiresAt: 1_900_000_000_000,
  permissionVersion: 3,
  user: {
    userId: 'user-1',
    username: 'yuanbao',
    nickname: '元宝',
    realName: '',
    avatarUrl: '',
    mobile: null,
    email: null,
    primaryDeptId: null,
    status: 1,
    roleIds: ['role-1'],
  },
  roles: [{ roleId: 'role-1', roleCode: 'admin', roleName: '管理员' }],
  menuTree: [],
  permissionCodes: ['system:user:view'],
}

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    sessionStorage.clear()
    vi.clearAllMocks()
  })

  it('recognizes only the admin role as super admin', () => {
    const store = useAuthStore()

    store.roles = [{ roleId: 'role-user', roleCode: 'user', roleName: '普通用户' }]
    expect(store.isSuperAdmin).toBe(false)

    store.roles = [{ roleId: 'role-admin', roleCode: 'admin', roleName: '系统管理员' }]
    expect(store.isSuperAdmin).toBe(true)
  })

  it('keeps the access token only in Pinia memory after login', async () => {
    vi.mocked(authApi.login).mockResolvedValue(loginResult)
    const store = useAuthStore()

    await store.login({ username: 'yuanbao', password: 'secret' })

    expect(store.accessToken).toBe('memory-only-token')
    expect(store.status).toBe('authenticated')
    expect(localStorage.length).toBe(0)
    expect(sessionStorage.length).toBe(0)
  })

  it('restores the full permission context before protected UI mounts', async () => {
    vi.mocked(authApi.refresh).mockResolvedValue(loginResult)
    const store = useAuthStore()

    await expect(store.restore()).resolves.toBe(true)

    expect(store.status).toBe('authenticated')
    expect(store.user?.userId).toBe('user-1')
    expect(store.permissionCodes.has('system:user:view')).toBe(true)
  })

  it('becomes anonymous when startup restoration fails', async () => {
    vi.mocked(authApi.refresh).mockRejectedValue(new Error('no session'))
    const store = useAuthStore()

    await expect(store.restore()).resolves.toBe(false)

    expect(store.status).toBe('anonymous')
    expect(store.accessToken).toBeNull()
  })

  it('clears memory even when the logout request is unreachable', async () => {
    vi.mocked(authApi.login).mockResolvedValue(loginResult)
    vi.mocked(authApi.logout).mockRejectedValue(new Error('network error'))
    const store = useAuthStore()
    await store.login({ username: 'yuanbao', password: 'secret' })

    await expect(store.logout()).resolves.toBeUndefined()

    expect(store.status).toBe('anonymous')
    expect(store.accessToken).toBeNull()
    expect(store.user).toBeNull()
  })
})
