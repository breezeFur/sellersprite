import { describe, expect, it } from 'vitest'

import { sellerSpriteOperations } from '@/features/sellersprite/model/operations'

import { menuApiBindings, sellerSpriteWorkbenchApis } from './menuApiBindings'

describe('menuApiBindings', () => {
  it('covers every seeded business menu with normalized API paths', () => {
    expect(menuApiBindings.map((binding) => binding.functionCode)).toEqual([
      'dashboard',
      'ai.chat',
      'sellersprite.workbench',
      'research.market-report',
      'system.user',
      'system.dept',
      'system.role',
      'system.dict',
      'system.function',
      'system.api',
      'ops.cache',
      'ops.logs',
    ])
    expect(menuApiBindings
      .every((binding) => binding.apis.every((api) => api.pathPattern.startsWith('/api/')))).toBe(true)
  })

  it('binds the workbench to exactly 45 unique SellerSprite proxy endpoints', () => {
    expect(sellerSpriteWorkbenchApis).toHaveLength(45)
    expect(new Set(sellerSpriteWorkbenchApis.map(
      (api) => `${api.httpMethod} ${api.pathPattern}`,
    )).size).toBe(45)
    expect(sellerSpriteWorkbenchApis.every(
      (api) => api.pathPattern.startsWith('/api/sellersprite/'),
    )).toBe(true)
    expect(sellerSpriteWorkbenchApis).toEqual(sellerSpriteOperations.map((operation) => ({
      httpMethod: operation.method,
      pathPattern: `/api${operation.path}`,
    })))

    const workbench = menuApiBindings.find((binding) => binding.functionCode === 'sellersprite.workbench')
    expect(workbench?.apis).toHaveLength(46)
    expect(workbench?.apis[0]).toEqual({ httpMethod: 'GET', pathPattern: '/api/auth/session' })
  })

  it('binds the market research page to its complete three-endpoint workflow', () => {
    const research = menuApiBindings.find(
      (binding) => binding.functionCode === 'research.market-report',
    )

    expect(research?.apis).toEqual([
      { httpMethod: 'GET', pathPattern: '/api/auth/session' },
      { httpMethod: 'POST', pathPattern: '/api/market-research/jobs' },
      { httpMethod: 'GET', pathPattern: '/api/market-research/jobs/{jobId}' },
      { httpMethod: 'GET', pathPattern: '/api/market-research/jobs/{jobId}/download' },
    ])
  })

  it('lists shared basic queries under every menu that uses them', () => {
    const users = menuApiBindings.find((binding) => binding.functionCode === 'system.user')
    const departments = menuApiBindings.find((binding) => binding.functionCode === 'system.dept')
    const roles = menuApiBindings.find((binding) => binding.functionCode === 'system.role')
    const functions = menuApiBindings.find((binding) => binding.functionCode === 'system.function')

    expect(users?.apis).toContainEqual({ httpMethod: 'GET', pathPattern: '/api/depts/tree' })
    expect(departments?.apis).toContainEqual({ httpMethod: 'GET', pathPattern: '/api/depts/tree' })
    expect(roles?.apis).toContainEqual({ httpMethod: 'GET', pathPattern: '/api/permissions/functions/tree' })
    expect(functions?.apis).toContainEqual({ httpMethod: 'GET', pathPattern: '/api/permissions/functions/tree' })
  })
})
