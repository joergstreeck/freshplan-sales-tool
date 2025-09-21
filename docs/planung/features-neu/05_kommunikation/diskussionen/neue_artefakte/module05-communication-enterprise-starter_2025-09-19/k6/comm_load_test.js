import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  thresholds: {
    http_req_duration: ['p(95)<200'],
    checks: ['rate>0.99']
  }
};

const BASE = __ENV.BASE_URL;
const TOKEN = __ENV.AUTH_TOKEN;
const headers = { Authorization: `Bearer ${TOKEN}`, 'Content-Type':'application/json' };

export default function () {
  // 1) Threads list
  let r = http.get(`${BASE}/api/comm/threads?limit=50`, { headers });
  check(r, { 'threads 200': (res) => res.status === 200 });

  // 2) Start conversation
  const payload = JSON.stringify({
    customerId: '00000000-0000-0000-0000-000000000001',
    subject: 'k6 Load Test Conversation',
    to: ['test@example.com'],
    bodyText: 'Hallo aus k6'
  });
  r = http.post(`${BASE}/api/comm/messages`, payload, { headers });
  check(r, { 'start message 201/403': (res) => res.status === 201 || res.status === 403 });

  // 3) Reply with bogus ETag (should 412)
  const threadId = r.json('threadId') || '00000000-0000-0000-0000-000000000002';
  r = http.post(`${BASE}/api/comm/threads/${threadId}/reply`, JSON.stringify({ bodyText: 'Test' }),
    { headers: { ...headers, 'If-Match': '"v9999"' } });
  check(r, { 'reply precondition': (res) => res.status === 412 || res.status === 404 });

  sleep(1);
}
