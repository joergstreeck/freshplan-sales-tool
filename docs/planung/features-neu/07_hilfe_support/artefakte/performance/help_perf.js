import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  thresholds: {
    http_req_duration: ['p(95)<150'],
    checks: ['rate>0.99']
  },
  scenarios: {
    ramp: { executor: 'ramping-vus', startVUs: 1, stages: [{duration:'30s', target: 30}, {duration:'1m', target: 30}] }
  }
};

const BASE = __ENV.BASE_URL;
const TOKEN = __ENV.AUTH_TOKEN;
const headers = { Authorization: `Bearer ${TOKEN}` };

export default function () {
  const r = http.get(`${BASE}/api/help/suggest?context=03:sample:delivery&top=3`, { headers });
  check(r, { '200': (res) => res.status === 200 });
  sleep(1);
}
