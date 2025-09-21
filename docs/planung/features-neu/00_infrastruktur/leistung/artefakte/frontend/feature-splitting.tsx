import React, { Suspense } from 'react'

const RoiCalculator = React.lazy(() => import('./RoiCalculator'))
const Charts = React.lazy(() => import('./Charts'))

export function RoiButton({ show }: { show: boolean }) {
  return show ? (
    <Suspense fallback={<span/>}>
      <RoiCalculator/>
    </Suspense>
  ) : null
}

export function Analytics({ enableCharts }: { enableCharts: boolean }) {
  return enableCharts ? (
    <Suspense fallback={<span/>}>
      <Charts/>
    </Suspense>
  ) : null
}
