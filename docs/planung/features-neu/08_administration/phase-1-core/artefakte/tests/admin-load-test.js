import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  thresholds: {
    http_req_duration: ['p(95)<200'],
    checks: ['rate>0.99']
  },
  vus: 20,
  duration: '1m'
};

const BASE = __ENV.BASE_URL;
const TOKEN = __ENV.AUTH_TOKEN;
const headers = { Authorization: `Bearer ${TOKEN}` };

export default function () {
  let r = http.get(`${BASE}/api/admin/audit/events?limit=20`, { headers });
  check(r, { '200': (res) => res.status === 200 });
  sleep(0.5);
}
