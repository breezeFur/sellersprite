import type { Pinia } from 'pinia'
import type { DirectiveBinding, ObjectDirective } from 'vue'

import { useAuthStore } from '@/features/auth/stores/useAuthStore'

export type PermissionValue = string | string[]
export type PermissionMode = 'all' | 'any'

const initialHidden = new WeakMap<HTMLElement, boolean>()

export function hasPermission(granted: Set<string>, required: PermissionValue, mode: PermissionMode = 'all') {
  const requiredCodes = (Array.isArray(required) ? required : [required]).filter(Boolean)
  if (requiredCodes.length === 0) {
    return true
  }
  return mode === 'any'
    ? requiredCodes.some((code) => granted.has(code))
    : requiredCodes.every((code) => granted.has(code))
}

export function createPermissionDirective(pinia: Pinia): ObjectDirective<HTMLElement, PermissionValue> {
  function applyPermission(el: HTMLElement, binding: DirectiveBinding<PermissionValue>) {
    if (!initialHidden.has(el)) {
      initialHidden.set(el, el.hidden !== false)
    }
    const mode: PermissionMode = binding.modifiers.any === true ? 'any' : 'all'
    const authStore = useAuthStore(pinia)
    el.hidden = initialHidden.get(el) === true
      || (!authStore.isSuperAdmin && !hasPermission(authStore.permissionCodes, binding.value, mode))
  }

  return {
    mounted: applyPermission,
    updated: applyPermission,
  }
}
