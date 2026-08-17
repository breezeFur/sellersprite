import { expect, test } from '@playwright/test'

const failedResult = { code: 'A401', message: '会话已过期', data: null }

test.beforeEach(async ({ page }) => {
  await page.route('**/api/auth/refresh', (route) => route.fulfill({ json: failedResult }))
})

test('opens the static login route', async ({ page }) => {
  await page.goto('/login')

  await expect(page.getByRole('heading', { name: '登录' })).toBeVisible()
  await expect(page.getByLabel('用户名')).toBeFocused()
})

test('logs in and opens the first dynamic route without persisting tokens', async ({ page }) => {
  await page.route('**/api/dashboard/overview', (route) =>
    route.fulfill({
      json: {
        code: '00000',
        message: '操作成功',
        data: {
          userCount: 1,
          enabledRoleCount: 1,
          departmentCount: 1,
          dictTypeCount: 0,
          todayAiConversationCount: 0,
          todayFailedOperationCount: 0,
          trends: [],
          recentActivities: [],
        },
      },
    }),
  )
  await page.route('**/api/auth/login', (route) =>
    route.fulfill({
      json: {
        code: '00000',
        message: '操作成功',
        data: {
          accessToken: 'browser-memory-token',
          tokenType: 'Bearer',
          expiresAt: 1_900_000_000_000,
          permissionVersion: 1,
          user: {
            userId: 'user-1',
            username: 'yuanbao',
            nickname: '元宝管理员',
            realName: '',
            avatarUrl: '',
            mobile: null,
            email: null,
            primaryDeptId: null,
            status: 1,
            roleIds: ['role-1'],
          },
          roles: [{ roleId: 'role-1', roleCode: 'admin', roleName: '管理员' }],
          menuTree: [
            {
              functionId: 'menu-dashboard',
              parentId: '0',
              name: '首页概览',
              type: 'MENU',
              routePath: '/dashboard',
              componentPath: 'dashboard/overview',
              icon: 'House',
              cacheable: 0,
              permissionCode: 'dashboard:view',
              sortOrder: 0,
              children: [],
            },
          ],
          permissionCodes: ['dashboard:view'],
        },
      },
    }),
  )
  await page.goto('/login')
  await page.getByLabel('用户名').fill('yuanbao')
  await page.getByLabel('密码').fill('correct-password')
  await page.getByRole('button', { name: '登录' }).click()

  await expect(page).toHaveURL(/\/dashboard$/)
  await expect(page.getByRole('heading', { name: '首页概览' })).toBeVisible()
  await expect(page.getByText('元宝管理员')).toBeVisible()
  await expect
    .poll(() => page.evaluate(() => ({ local: localStorage.length, session: sessionStorage.length })))
    .toEqual({ local: 0, session: 0 })
})

test('executes SellerSprite success and failure flows across responsive layouts', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 900 })
  let requestedUrl: URL | null = null
  let guidedPayload: Record<string, unknown> | null = null
  let productResearchPayload: Record<string, unknown> | null = null
  let returnBusinessError = false
  await page.route('**/api/system/dicts/*', (route) => {
    const dictType = decodeURIComponent(new URL(route.request().url()).pathname.split('/').pop() ?? '')
    const dictionaryItems: Record<string, Array<Record<string, unknown>>> = {
      MARKET: [{
          dictDataId: 'dict-market-us',
          dictType: 'MARKET',
          dictValue: 'US',
          dictLabel: 'MARKET_US',
          dictName: '美国站',
          defaultFlag: 1,
          sortOrder: 10,
          status: 1,
          systemBuiltin: 1,
      }],
      PRODUCT_SIZE_US: [
        {
          dictDataId: 'dict-size-st-ss',
          dictType: 'PRODUCT_SIZE_US',
          dictValue: 'ST/SS',
          dictLabel: 'PRODUCT_SIZE_US_ST_SS',
          dictName: '小号标准尺寸',
          defaultFlag: 0,
          sortOrder: 10,
          status: 1,
          systemBuiltin: 1,
        },
        {
          dictDataId: 'dict-size-ls',
          dictType: 'PRODUCT_SIZE_US',
          dictValue: 'LS',
          dictLabel: 'PRODUCT_SIZE_US_LS',
          dictName: '大号标准尺寸',
          defaultFlag: 0,
          sortOrder: 20,
          status: 1,
          systemBuiltin: 1,
        },
      ],
      PRODUCT_SORT_FIELD: [{
        dictDataId: 'dict-sort-total-units',
        dictType: 'PRODUCT_SORT_FIELD',
        dictValue: 'total_units',
        dictLabel: 'PRODUCT_SORT_FIELD_TOTAL_UNITS',
        dictName: '月销量',
        defaultFlag: 1,
        sortOrder: 10,
        status: 1,
        systemBuiltin: 1,
      }],
    }
    const items = dictionaryItems[dictType] ?? []
    return route.fulfill({
      json: {
        code: '00000',
        message: '操作成功',
        data: {
          dictType,
          dictName: dictType,
          sortOrder: 0,
          status: 1,
          systemBuiltin: 1,
          items,
        },
      },
    })
  })
  await page.route('**/api/sellersprite/products/competitors', async (route) => {
    guidedPayload = route.request().postDataJSON() as Record<string, unknown>
    return route.fulfill({
      json: {
        code: '00000',
        message: '操作成功',
        data: {
          page: 1,
          size: 50,
          total: 1,
          items: [{ asin: 'B07Z82895W', title: 'Guided SellerSprite product' }],
        },
      },
    })
  })
  await page.route('**/api/sellersprite/products/research', (route) => {
    productResearchPayload = route.request().postDataJSON() as Record<string, unknown>
    return route.fulfill({ json: {
      code: '00000',
      message: '操作成功',
      data: {
        guestId: null,
        pages: 64754,
        page: 1,
        size: 1,
        total: 64754,
        took: 17,
        url: null,
        order: { field: '', desc: true },
        items: [{
          asin: 'B0DB5VT4QJ',
          brand: 'OLANLY',
          title: 'OLANLY Bathroom Rugs 70x46',
          units: 158113,
          revenue: 16059537,
          price: 100.89,
          ratings: 18821,
          rating: 4.4,
          sellerName: 'OLANLY',
          pkgWeight: '10.43 pounds',
          badge: { bestSeller: '#1 Best Seller in Bath Rugs', amazonChoice: 'N' },
          subcategories: [{ code: '1063242', rank: 1, label: 'Bath Rugs' }],
        }],
        terminal: null,
        hasNextPage: null,
        guestVisited: false,
      },
    } })
  })
  await page.route('**/api/sellersprite/asins/detail?*', (route) => {
    requestedUrl = new URL(route.request().url())
    if (returnBusinessError) {
      return route.fulfill({
        json: {
          code: 'S429',
          message: 'SellerSprite 接口可用次数已耗尽',
          traceId: 'browser-trace-id',
          data: null,
        },
      })
    }
    return route.fulfill({
      json: {
        code: '00000',
        message: '操作成功',
        data: { asin: 'B07Z82895W', title: 'SellerSprite browser test product' },
      },
    })
  })
  await page.route('**/api/auth/login', (route) =>
    route.fulfill({
      json: {
        code: '00000',
        message: '操作成功',
        data: {
          accessToken: 'browser-memory-token',
          tokenType: 'Bearer',
          expiresAt: 1_900_000_000_000,
          permissionVersion: 1,
          user: {
            userId: 'user-1',
            username: 'yuanbao',
            nickname: '元宝管理员',
            realName: '',
            avatarUrl: '',
            mobile: null,
            email: null,
            primaryDeptId: null,
            status: 1,
            roleIds: ['role-1'],
          },
          roles: [{ roleId: 'role-1', roleCode: 'admin', roleName: '管理员' }],
          menuTree: [
            {
              functionId: 'menu-sellersprite-workbench',
              parentId: '0',
              name: 'SellerSprite 调试台',
              type: 'MENU',
              routePath: '/sellersprite/workbench',
              componentPath: 'sellersprite/workbench',
              icon: 'DataBoard',
              cacheable: 0,
              permissionCode: 'sellersprite:workbench:view',
              sortOrder: 0,
              children: [],
            },
          ],
          permissionCodes: ['sellersprite:workbench:view'],
        },
      },
    }),
  )

  await page.goto('/login')
  await page.getByLabel('用户名').fill('yuanbao')
  await page.getByLabel('密码').fill('correct-password')
  await page.getByRole('button', { name: '登录' }).click()

  await expect(page).toHaveURL(/\/sellersprite\/workbench$/)
  await expect(page.getByRole('heading', { name: 'SellerSprite 工作台' })).toBeVisible({ timeout: 15_000 })
  await expect(page.getByText('45 个官方接口 · 完整字段表单与结果表格')).toBeVisible()
  await expect(page.locator('[data-guided-field="marketplace"]')).toContainText('美国站')
  await page.getByLabel('执行引导查询').click()
  await expect(page.getByRole('cell', { name: 'Guided SellerSprite product' })).toBeVisible()
  expect(guidedPayload?.marketplace).toBe('MARKET_US')

  await page.locator('[data-guided-operation-id="PRODUCT_RESEARCH"]').click()
  await expect(page.locator('[data-guided-field]')).toHaveCount(60)
  await expect(page.locator('[data-guided-field="order"]')).toContainText('月销量')
  await page.locator('[data-guided-field="dimensionType"] .el-select__wrapper').click()
  await page.getByRole('option', { name: '小号标准尺寸' }).click()
  await page.getByRole('option', { name: '大号标准尺寸' }).click()
  await page.getByLabel('执行引导查询').click()
  expect(productResearchPayload?.dimensionType).toBe('PRODUCT_SIZE_US_ST_SS,PRODUCT_SIZE_US_LS')
  expect(productResearchPayload?.order).toEqual({ field: 'PRODUCT_SORT_FIELD_TOTAL_UNITS', desc: true })
  await expect(page.getByRole('cell', { name: '10.43 pounds' })).toBeVisible()
  await expect(
    page.getByRole('cell', { name: '[{"code":"1063242","rank":1,"label":"Bath Rugs"}]' }),
  ).toBeVisible()

  await page.locator('[data-guided-group-id="keyword"]').click()
  await page.locator('[data-guided-operation-id="KEYWORD_ORDER"]').click()
  await expect(page.locator('[data-guided-field="date"] .el-date-editor--date')).toBeVisible()
  await page.locator('[data-guided-field="reverseType"] .el-select__wrapper').click()
  await page.getByRole('option', { name: '按月' }).click()
  await expect(page.locator('[data-guided-field="date"] .el-date-editor--month')).toBeVisible()

  await page.getByRole('tab', { name: 'API 调试' }).click()
  await expect(page.locator('.workbench__body[aria-label="SellerSprite API 调试台"]')).toBeVisible()
  await page.locator('.domain-list button').filter({ hasText: 'ASIN 分析' }).click()
  await page.locator('[data-operation-id="ASIN_DETAIL"]').click()
  await page.getByLabel('发送 SellerSprite 请求').click()

  await expect(page.getByLabel('SellerSprite 响应')).toContainText('SellerSprite browser test product')
  expect(requestedUrl?.searchParams.get('marketplace')).toBe('US')
  expect(requestedUrl?.searchParams.get('asin')).toBe('B07Z82895W')

  await page.setViewportSize({ width: 1024, height: 900 })
  await expect(page.getByLabel('SellerSprite 请求 JSON')).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)

  returnBusinessError = true
  await page.getByLabel('发送 SellerSprite 请求').click()
  await expect(page.getByRole('alert')).toContainText('S429')
  await expect(page.getByRole('alert')).toContainText('browser-trace-id')

  await page.setViewportSize({ width: 768, height: 900 })
  await expect(page.getByLabel('SellerSprite 请求 JSON')).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
})
