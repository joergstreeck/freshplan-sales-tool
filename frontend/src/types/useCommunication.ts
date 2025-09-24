import { useEffect, useState } from 'react';
import { getJSON, postJSON } from '../lib/apiClient';
import type { ThreadPage } from '../types/communication';

export function useThreads(params: {
  q?: string;
  customerId?: string;
  unread?: boolean;
  channel?: 'EMAIL' | 'PHONE' | 'MEETING' | 'NOTE';
}) {
  const [data, setData] = useState<ThreadPage>({ items: [] });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    const q = new URLSearchParams();
    if (params.q) q.set('q', params.q);
    if (params.customerId) q.set('customerId', params.customerId);
    if (params.unread) q.set('unread', 'true');
    if (params.channel) q.set('channel', params.channel);
    setLoading(true);
    getJSON<ThreadPage>('/api/comm/threads?' + q.toString())
      .then(setData)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, [params.q, params.customerId, params.unread, params.channel]);
  return { data, loading, error };
}

export async function sendReply(threadId: string, etag: string, bodyText: string) {
  return postJSON(`/api/comm/threads/${threadId}/reply`, { bodyText }, { 'If-Match': etag });
}
export async function startConversation(payload: {
  customerId: string;
  subject: string;
  to: string[];
  cc?: string[];
  bodyText: string;
  accountId?: string;
}) {
  return postJSON('/api/comm/messages', payload);
}
