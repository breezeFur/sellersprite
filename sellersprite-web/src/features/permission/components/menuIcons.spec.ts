import { DataBoard, Menu } from '@element-plus/icons-vue'
import { describe, expect, it } from 'vitest'

import { resolveMenuIcon } from './menuIcons'

describe('resolveMenuIcon', () => {
  it('resolves the SellerSprite workbench icon and keeps the fallback', () => {
    expect(resolveMenuIcon('DataBoard')).toBe(DataBoard)
    expect(resolveMenuIcon('UnknownSellerSpriteIcon')).toBe(Menu)
  })
})
