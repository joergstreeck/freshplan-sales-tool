import { useQuery, useQueryClient } from '@tanstack/react-query';
import { EffectiveSettings } from './settings.types';

async function fetchEffective(etag?:string): Promise<EffectiveSettings>{
  const headers: Record<string,string> = {};
  if (etag) headers['If-None-Match'] = etag;
  const res = await fetch('/api/settings/effective', { headers });
  if (res.status === 304 && etag) return { blob: {}, etag, computedAt: new Date().toISOString() };
  if (!res.ok) throw new Error('Failed to load settings');
  const data = await res.json();
  return data as EffectiveSettings;
}

export function useEffectiveSettings(){
  const qc = useQueryClient();
  return useQuery<EffectiveSettings>(['settings','effective'], () => {
    const prev = qc.getQueryData<EffectiveSettings>(['settings','effective']);
    return fetchEffective(prev?.etag);
  }, { staleTime: 60000 });
}
