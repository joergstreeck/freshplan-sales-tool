import { useQuery } from '@tanstack/react-query';

export type AuditEvent = { id: string; action: string; resourceType: string; resourceId?: string; riskTier: 'TIER1'|'TIER2'|'TIER3'; createdAt: string };

export function useAudit(params?: { q?: string; resourceType?: string; from?: string; to?: string }){
  const qs = new URLSearchParams();
  if (params?.q) qs.set('q', params.q);
  if (params?.resourceType) qs.set('resourceType', params.resourceType);
  if (params?.from) qs.set('from', params.from);
  if (params?.to) qs.set('to', params.to);
  return useQuery({ queryKey: ['admin','audit', Object.fromEntries(qs)], queryFn: async ()=> {
    const r = await fetch(`/api/admin/audit/events?${qs.toString()}`);
    if(!r.ok) throw new Error('load audit failed');
    const j = await r.json();
    return j.items as AuditEvent[];
  }});
}

export async function exportAudit(from?: string, to?: string){
  const qs = new URLSearchParams();
  if (from) qs.set('from', from);
  if (to) qs.set('to', to);
  const r = await fetch(`/api/admin/audit/export?${qs.toString()}`, { headers:{ 'Accept':'application/jsonlines' }});
  if(!r.ok) throw new Error('export audit failed');
  return await r.text(); // JSONL
}
