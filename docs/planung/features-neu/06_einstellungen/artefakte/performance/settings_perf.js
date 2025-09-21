import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  thresholds: {
    http_req_duration: ['p(95)<50'],
    checks: ['rate>0.99']
  },
  scenarios: {
    steady: { executor: 'constant-arrival-rate', rate: 50, timeUnit: '1s', duration: '2m', preAllocatedVUs: 50 }
  }
};

const BASE = __ENV.BASE_URL;
const TOKEN = __ENV.AUTH_TOKEN;
const headers = { Authorization: `Bearer ${TOKEN}` };

export default function () {
  let r = http.get(`${BASE}/api/settings/effective`, { headers });
  check(r, { '200': (res) => res.status === 200 && res.json('etag') });
  const etag = r.headers['ETag'];
  r = http.get(`${BASE}/api/settings/effective`, { headers: { ...headers, 'If-None-Match': etag } });
  check(r, { '304': (res) => res.status === 304 });
  sleep(1);
}
