import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

export type SmtpConfig = { host: string; port: number; username?: string; passwordRef?: string; tlsMode: 'STARTTLS'|'TLS'|'NONE'; truststoreRef?: string };
export type OutboxStatus = { paused: boolean; ratePerMin: number; backlog: number; lastError?: string };

export function useSmtp(){
  return useQuery({ queryKey:['admin','ops','smtp'], queryFn: async ()=>{
    const r = await fetch('/api/admin/ops/smtp');
    if(!r.ok) throw new Error('smtp failed');
    return r.json() as Promise<SmtpConfig>;
  }});
}

export function useUpdateSmtp(){
  const qc = useQueryClient();
  return useMutation({
    mutationFn: async (cfg: SmtpConfig)=>{
      const r = await fetch('/api/admin/ops/smtp', { method:'PUT', headers:{'Content-Type':'application/json'}, body: JSON.stringify(cfg)});
      if(!r.ok) throw new Error('update smtp failed');
    },
    onSuccess(){ qc.invalidateQueries({ queryKey:['admin','ops','smtp'] }); }
  });
}

export function useOutbox(){
  const qc = useQueryClient();
  const q = useQuery({ queryKey:['admin','ops','outbox'], queryFn: async ()=>{
    const r = await fetch('/api/admin/ops/outbox'); if(!r.ok) throw new Error('outbox failed'); return r.json() as Promise<OutboxStatus>;
  }});
  const pause = async ()=>{ const r = await fetch('/api/admin/ops/outbox/pause', {method:'POST'}); if(!r.ok) throw new Error('pause failed'); await qc.invalidateQueries({queryKey:['admin','ops','outbox']}); };
  const resume = async ()=>{ const r = await fetch('/api/admin/ops/outbox/resume', {method:'POST'}); if(!r.ok) throw new Error('resume failed'); await qc.invalidateQueries({queryKey:['admin','ops','outbox']}); };
  const rate = async (n: number)=>{ const r = await fetch('/api/admin/ops/outbox/rate', {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ratePerMin:n})}); if(!r.ok) throw new Error('rate failed'); await qc.invalidateQueries({queryKey:['admin','ops','outbox']}); };
  return { query: q, pause, resume, rate };
}
