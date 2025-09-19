import { useState } from 'react';
import { postJSON } from '../lib/apiClient';
import type { ROICalcRequest, ROICalcResponse } from '../types/cockpit';

export function useROI(){
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<ROICalcResponse | null>(null);
  async function calculate(body: ROICalcRequest){
    setLoading(true); setError(null);
    try { const res = await postJSON<ROICalcRequest, ROICalcResponse>('/api/cockpit/roi/calc', body); setResult(res); }
    catch(e:any){ setError(e.message); }
    finally { setLoading(false); }
  }
  return { loading, error, result, calculate };
}
