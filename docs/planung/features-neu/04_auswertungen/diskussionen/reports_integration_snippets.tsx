// ROUTING (providers.tsx)
<Route path="/reports" element={<AuswertungenDashboard />} />
<Route path="/berichte" element={<Navigate to="/reports" replace />} />
<Route path="/reports/sales" element={<Reports.Sales />} />
<Route path="/reports/customers" element={<Reports.Customers />} />
<Route path="/reports/activities" element={<Reports.Activities />} />

// DATA HOOKS (useReports.ts)
export async function fetchSalesSummary(range: '7d'|'30d'|'90d'='30d') {
  const res = await fetch(`/api/reports/sales-summary?range=${range}`, { headers: { 'Authorization': `Bearer ${token}` }});
  return await res.json();
}

// DASHBOARD INTEGRATION (AuswertungenDashboard.tsx)
<UniversalExportButton
  entity="sales-summary"
  queryParams={{ range }}
  formats={['csv','xlsx','pdf','json','html','jsonl']}
  buttonLabel="Umsatzbericht exportieren"
/>
