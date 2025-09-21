import React, { Suspense } from 'react'
import { Route, Routes } from 'react-router-dom'
import Spinner from '../components/Spinner'

const Cockpit = React.lazy(() => import('../modules/01-cockpit/CockpitPage'))
const Leads = React.lazy(() => import('../modules/02-neukundengewinnung/LeadsPage'))
const Customers = React.lazy(() => import('../modules/03-kundenmanagement/CustomersPage'))
// ... weitere Module analog

export default function AppRoutes() {
  return (
    <Suspense fallback={<Spinner />}>
      <Routes>
        <Route path="/" element={<Cockpit/>} />
        <Route path="/leads" element={<Leads/>} />
        <Route path="/kunden" element={<Customers/>} />
      </Routes>
    </Suspense>
  )
}
