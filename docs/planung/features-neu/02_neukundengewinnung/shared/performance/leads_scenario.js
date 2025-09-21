import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 30,
  duration: '2m',
  thresholds: {
    http_req_duration: ['p(95)<200'],
    checks: ['rate>0.99']
  },
};

export default function () {
  const token = __ENV.TOKEN;
  const headers = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };

  // Search
  let r = http.get('https://api.example.com/api/leads?limit=50', { headers });
  check(r, { 'GET 200': (res) => res.status === 200 });

  // Create
  const body = JSON.stringify({ name: `Test Lead ${__ITER}`, territory: 'BER', status: 'NEW' });
  r = http.post('https://api.example.com/api/leads', body, { headers });
  check(r, { 'POST 201': (res) => res.status === 201 });

  sleep(1);
}
