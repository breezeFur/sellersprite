export interface SystemUser {
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

export interface UserCreatePayload {
  username: string
  password: string
  nickname: string
  realName: string
  mobile: string
  email: string
  primaryDeptId: string | null
  roleIds: string[]
}

export type UserUpdatePayload = Omit<UserCreatePayload, 'password' | 'roleIds'>

export interface SystemRole {
  roleId: string
  roleCode: string
  roleName: string
  roleType: string
  sortOrder: number
  status: number
}

export interface DepartmentNode {
  deptId: string
  parentId: string
  deptCode: string
  deptName: string
  deptPath: string
  sortOrder: number
  status: number
  children: DepartmentNode[]
}

export interface DepartmentPayload {
  parentId: string
  deptCode: string
  deptName: string
  leaderUserId: string | null
  sortOrder: number
  remark: string
}

export interface RolePayload {
  roleCode: string
  roleName: string
  roleType: string
  sortOrder: number
  remark: string
}

export interface RoleEffectiveApi {
  sysApiId: string
  apiCode: string
  apiName: string
  httpMethod: string
  pathPattern: string
  permissionCode: string | null
  grantSource: 'FUNCTION' | 'EXTRA' | 'BOTH' | string
}

export interface RolePermission {
  roleId: string
  functionIds: string[]
  extraApiIds: string[]
  effectiveApis: RoleEffectiveApi[]
}

export interface SystemFunction {
  sysFunctionId: string
  parentId: string
  functionCode: string
  functionName: string
  functionType: 'DIR' | 'MENU' | 'BUTTON' | string
  routePath: string | null
  componentPath: string | null
  icon: string
  visible: number
  cacheable: number
  externalLink: string | null
  permissionCode: string | null
  sortOrder: number
  status: number
  children: SystemFunction[]
}

export interface SystemApiResource {
  sysApiId: string
  apiCode: string
  apiName: string
  apiType: string
  httpMethod: string
  pathPattern: string
  permissionCode: string | null
  moduleName: string
  operationName: string
  status: number
}

export interface DictionaryType {
  dictType: string
  dictName: string
  systemBuiltin: number
  sortOrder: number
  status: number
  items: DictionaryItem[]
}

export interface DictionaryItem {
  dictDataId: string
  dictType: string
  dictValue: string | null
  dictLabel: string
  dictName: string
  color: string
  defaultFlag: number
  sortOrder: number
  systemBuiltin: number
  status: number
}

export interface DictionaryTypePayload {
  dictType: string
  dictName: string
  systemBuiltin: number
  sortOrder: number
  remark: string
}

export interface DictionaryItemPayload {
  dictType: string
  dictValue: string
  dictLabel: string
  dictName: string
  color: string
  defaultFlag: number
  sortOrder: number
  remark: string
}

export interface FunctionPayload {
  parentId: string
  functionCode: string
  functionName: string
  functionType: 'DIR' | 'MENU' | 'BUTTON'
  routePath: string
  componentPath: string
  permissionCode: string
  sortOrder: number
  icon: string
  visible: number
  cacheable: number
  externalLink: string
  remark: string
}

export interface ApiResourcePayload {
  apiCode: string
  apiName: string
  apiType: 'PUBLIC' | 'PERMISSION'
  httpMethod: string
  pathPattern: string
  permissionCode: string
  moduleName: string
  operationName: string
  remark: string
}

export interface ApiCatalogSyncResult {
  scanned: number
  created: number
  updated: number
  unchanged: number
}

export interface MenuApiBindingSyncResult {
  functionCount: number
  bindingCount: number
  publicApiCount: number
  permissionApiCount: number
}
