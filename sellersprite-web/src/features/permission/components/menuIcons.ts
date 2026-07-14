import {
  ChatLineRound,
  Collection,
  Connection,
  DataBoard,
  DataAnalysis,
  Document,
  House,
  Key,
  Lock,
  Menu,
  OfficeBuilding,
  SetUp,
  Tickets,
  User,
  UserFilled,
} from '@element-plus/icons-vue'
import type { Component } from 'vue'

const menuIcons: Record<string, Component> = {
  ChatLineRound,
  Collection,
  Connection,
  DataBoard,
  DataAnalysis,
  Document,
  House,
  Key,
  Lock,
  Menu,
  OfficeBuilding,
  SetUp,
  Tickets,
  User,
  UserFilled,
}

export function resolveMenuIcon(icon: string) {
  return menuIcons[icon] ?? Menu
}
