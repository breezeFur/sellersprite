export function resolveSafeLoginRedirect(value: unknown) {
  if (typeof value !== 'string') {
    return null
  }
  return value.startsWith('/') && !value.startsWith('//') && !value.includes('\\') ? value : null
}
