export function grantSourceLabel(source: string) {
  const labels: Record<string, string> = {
    FUNCTION: '功能派生',
    EXTRA: '直接附加',
    BOTH: '双重来源',
  }
  return labels[source] ?? source
}
