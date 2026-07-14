export interface SseMessage {
  event: string
  data: string
}

export interface SseParser {
  push(chunk: string): void
  finish(): void
}

export function createSseParser(onEvent: (message: SseMessage) => void): SseParser {
  let buffer = ''

  function dispatch(block: string) {
    const data: string[] = []
    let event = 'message'

    for (const line of block.split(/\r?\n/)) {
      if (!line || line.startsWith(':')) {
        continue
      }
      const separator = line.indexOf(':')
      const field = separator >= 0 ? line.slice(0, separator) : line
      let value = separator >= 0 ? line.slice(separator + 1) : ''
      if (value.startsWith(' ')) {
        value = value.slice(1)
      }
      if (field === 'event') {
        event = value || 'message'
      } else if (field === 'data') {
        data.push(value)
      }
    }

    if (data.length > 0) {
      onEvent({ event, data: data.join('\n') })
    }
  }

  function drain() {
    let separator = buffer.match(/\r?\n\r?\n/)
    while (separator?.index !== undefined) {
      const block = buffer.slice(0, separator.index)
      buffer = buffer.slice(separator.index + separator[0].length)
      dispatch(block)
      separator = buffer.match(/\r?\n\r?\n/)
    }
  }

  return {
    push(chunk: string) {
      buffer += chunk
      drain()
    },
    finish() {
      if (buffer.trim()) {
        dispatch(buffer)
      }
      buffer = ''
    },
  }
}

export async function readSseStream(
  stream: ReadableStream<Uint8Array>,
  onEvent: (message: SseMessage) => void,
) {
  const reader = stream.getReader()
  const decoder = new TextDecoder()
  const parser = createSseParser(onEvent)

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) {
        parser.push(decoder.decode())
        parser.finish()
        return
      }
      parser.push(decoder.decode(value, { stream: true }))
    }
  } finally {
    reader.releaseLock()
  }
}
