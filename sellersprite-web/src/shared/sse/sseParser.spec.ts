import { describe, expect, it, vi } from 'vitest'

import { createSseParser } from './sseParser'

describe('createSseParser', () => {
  it('parses events split across arbitrary chunks', () => {
    const onEvent = vi.fn()
    const parser = createSseParser(onEvent)

    parser.push('event: convers')
    parser.push('ation\r\ndata: {"conversationId":"c1"}\r')
    parser.push('\n\r\nevent: delta\ndata: {"content":"你')
    parser.push('好"}\n\n')
    parser.finish()

    expect(onEvent).toHaveBeenNthCalledWith(1, {
      event: 'conversation',
      data: '{"conversationId":"c1"}',
    })
    expect(onEvent).toHaveBeenNthCalledWith(2, {
      event: 'delta',
      data: '{"content":"你好"}',
    })
  })

  it('joins multi-line data and ignores comments', () => {
    const onEvent = vi.fn()
    const parser = createSseParser(onEvent)

    parser.push(': keepalive\nevent: error\ndata: first\ndata: second\n\n')
    parser.finish()

    expect(onEvent).toHaveBeenCalledWith({ event: 'error', data: 'first\nsecond' })
  })
})
