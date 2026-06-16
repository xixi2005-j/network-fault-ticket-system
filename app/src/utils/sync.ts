const channel = new BroadcastChannel('ticket-sync')

export function notifyRefresh(type: string = 'ticket') {
  channel.postMessage({ type, timestamp: Date.now() })
}

export function onRefresh(callback: (type: string) => void) {
  const handler = (e: MessageEvent) => {
    callback(e.data.type)
  }
  channel.addEventListener('message', handler)
  return () => channel.removeEventListener('message', handler)
}
