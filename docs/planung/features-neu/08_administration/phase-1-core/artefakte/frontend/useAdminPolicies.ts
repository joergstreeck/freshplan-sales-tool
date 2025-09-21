import { useMutation, useQuery } from '@tanstack/react-query';

export function usePolicies(){
  return useQuery({ queryKey:['admin','security','policies'], queryFn: async ()=>{
    const r = await fetch('/api/admin/security/policies'); if(!r.ok) throw new Error('policies failed'); return r.json();
  }});
}

export function usePolicyChange(){
  return useMutation({
    mutationFn: async (body: { key: string; value: any; riskTier?: 'TIER1'|'TIER2'|'TIER3'; justification?: string; emergency?: boolean })=>{
      const r = await fetch('/api/admin/security/policies', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body)});
      if(!r.ok) throw new Error('policy change failed');
    }
  });
}
