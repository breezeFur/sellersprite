import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean
    requiresAuth?: boolean
    title?: string
    functionId?: string
    permissionCode?: string
    cacheable?: boolean
    dynamic?: boolean
  }
}

export {}
