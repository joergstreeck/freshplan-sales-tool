import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

export type User = { id: string; email: string; name?: string; roles: string[]; orgId?: string|null; territories?: string[]; channels?: string[]; scopes?: string[]; disabled?: boolean };

export function useUsers(params?: { q?: string; orgId?: string; territory?: string; limit?: number }){
  const qs = new URLSearchParams();
  if (params?.q) qs.set('q', params.q);
  if (params?.orgId) qs.set('orgId', params.orgId);
  if (params?.territory) qs.set('territory', params.territory);
  if (params?.limit) qs.set('limit', String(params.limit));
  return useQuery({ queryKey: ['admin','users', Object.fromEntries(qs)], queryFn: async ()=> {
    const r = await fetch(`/api/admin/users?${qs.toString()}`);
    if(!r.ok) throw new Error('load users failed');
    const j = await r.json();
    return j.items as User[];
  }});
}

export function useCreateUser(){
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (body: { email: string; name?: string; roles?: string[] })=>{
      const r = await fetch('/api/admin/users', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body)});
      if(!r.ok) throw new Error('create user failed');
      return r.json() as Promise<User>;
    },
    onSuccess(){ qc.invalidateQueries({ queryKey: ['admin','users'] }); }
  });
}

export function useReplaceClaims(){
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, claims }: { id: string; claims: { orgId?: string|null; territories?: string[]; channels?: string[]; scopes?: string[] } })=>{
      const r = await fetch(`/api/admin/users/${id}/claims`, { method:'PUT', headers:{'Content-Type':'application/json'}, body: JSON.stringify(claims)});
      if(!r.ok) throw new Error('replace claims failed');
    },
    onSuccess(){ qc.invalidateQueries({ queryKey: ['admin','users'] }); }
  });
}
