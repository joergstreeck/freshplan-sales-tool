import { onLCP, onINP, onCLS } from 'web-vitals'

type RumEvent = {
  type: 'LCP'|'INP'|'CLS'|'BUSINESS'
  value: number
  route: string
  device: 'low'|'mid'|'high'
  network?: string
  ts: number
  meta?: Record<string, any>
}

function deviceClass(): 'low'|'mid'|'high' {
  const m = navigator.hardwareConcurrency || 2
  const mem = (navigator as any).deviceMemory || 4
  if (m >= 8 && mem >= 8) return 'high'
  if (m >= 4 && mem >= 4) return 'mid'
  return 'low'
}

function sendRum(e: RumEvent) {
  navigator.sendBeacon?.('/api/rum', JSON.stringify(e))
}

export function setupWebVitalsRUM() {
  const dev = deviceClass()
  const route = location.pathname
  const network = (navigator as any).connection?.effectiveType
  onLCP(v => sendRum({ type: 'LCP', value: v.value, route, device: dev, network, ts: Date.now() }))
  onINP(v => sendRum({ type: 'INP', value: v.value, route, device: dev, network, ts: Date.now() }))
  onCLS(v => sendRum({ type: 'CLS', value: v.value, route, device: dev, network, ts: Date.now() }))
}

export function trackBusiness(event: string, meta?: Record<string, any>) {
  const dev = deviceClass()
  const route = location.pathname
  const network = (navigator as any).connection?.effectiveType
  sendRum({ type: 'BUSINESS', value: 1, route, device: dev, network, ts: Date.now(), meta: { event, ...meta } })
}
