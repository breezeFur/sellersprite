import { describe, expect, it } from 'vitest'

import { routeComponentWhitelist } from './componentWhitelist'

describe('routeComponentWhitelist', () => {
  it('registers fixed business component paths only', () => {
    expect(routeComponentWhitelist['sellersprite/workbench']).toBeTypeOf('function')
    expect(routeComponentWhitelist['research/market-report']).toBeTypeOf('function')
    expect(routeComponentWhitelist['sellersprite/api']).toBeUndefined()
    expect(routeComponentWhitelist['research/jobs']).toBeUndefined()
  })
})
