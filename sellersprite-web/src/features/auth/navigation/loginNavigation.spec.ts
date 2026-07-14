import { describe, expect, it } from 'vitest'

import { resolveSafeLoginRedirect } from './loginNavigation'

describe('resolveSafeLoginRedirect', () => {
  it('keeps only local absolute paths', () => {
    expect(resolveSafeLoginRedirect('/system/users?page=2')).toBe('/system/users?page=2')
    expect(resolveSafeLoginRedirect('https://example.com')).toBeNull()
    expect(resolveSafeLoginRedirect('//example.com')).toBeNull()
    expect(resolveSafeLoginRedirect('/\\example.com')).toBeNull()
    expect(resolveSafeLoginRedirect(['/dashboard'])).toBeNull()
  })
})
