export interface AuthCredentials {
  username: string
  password: string
  deviceId?: string
  deviceName?: string
  clientType?: string
}

export interface AuthUser {
  userId: string
  username: string
  nickname: string
  realName: string
  avatarUrl: string
  mobile: string | null
  email: string | null
  primaryDeptId: string | null
  status: number
  roleIds: string[]
}

export interface AuthRole {
  roleId: string
  roleCode: string
  roleName: string
  roleType?: string
  sortOrder?: number
  status?: number
}

export interface AuthMenu {
  functionId: string
  parentId: string
  name: string
  type: 'DIR' | 'MENU' | 'BUTTON'
  routePath: string | null
  componentPath: string | null
  icon: string
  cacheable: number
  permissionCode: string | null
  sortOrder: number
  children: AuthMenu[]
}

export interface AuthSession {
  user: AuthUser
  roles: AuthRole[]
  menuTree: AuthMenu[]
  permissionCodes: string[]
  permissionVersion: number
}

export interface AuthLoginResult extends AuthSession {
  accessToken: string
  tokenType: string
  expiresAt: number
}
