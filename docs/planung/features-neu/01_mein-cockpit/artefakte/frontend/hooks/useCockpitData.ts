import { useEffect, useState } from 'react';
import { getJSON } from '../lib/apiClient';
import type { SummaryResponse, FiltersResponse } from '../types/cockpit';

export function useCockpitSummary(params: { range?: '7d'|'30d'|'90d'; territory?: string; channels?: string }){
  const [data, setData] = useState<SummaryResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(()=>{
    setLoading(true);
    const q = new URLSearchParams({ range: params.range || '30d', ...(params.territory?{territory:params.territory}:{}) , ...(params.channels?{channels:params.channels}:{}) });
    getJSON<SummaryResponse>(`/api/cockpit/summary?${q.toString()}`)
      .then(setData).catch(e=>setError(e.message)).finally(()=>setLoading(false));
  }, [params.range, params.territory, params.channels]);

  return { data, loading, error };
}

export function useCockpitFilters(){
  const [data, setData] = useState<FiltersResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  useEffect(()=>{
    getJSON<FiltersResponse>('/api/cockpit/filters')
      .then(setData).catch(e=>setError(e.message)).finally(()=>setLoading(false));
  }, []);
  return { data, loading, error };
}
