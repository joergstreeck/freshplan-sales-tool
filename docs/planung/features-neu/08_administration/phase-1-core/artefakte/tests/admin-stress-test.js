import http from 'k6/http';
import { check } from 'k6';

export const options = {
  thresholds: { http_req_failed: ['rate<0.01'] },
  scenarios: {
    spike: { executor: 'ramping-arrival-rate', startRate: 50, timeUnit: '1s', preAllocatedVUs: 50, stages: [
      { target: 200, duration: '30s' },
      { target: 400, duration: '30s' },
      { target: 0, duration: '30s' }
    ]}
  }
};

const BASE = __ENV.BASE_URL;
const TOKEN = __ENV.AUTH_TOKEN;
const headers = { Authorization: `Bearer ${TOKEN}` };

export default function () {
  let payload = JSON.stringify({ subjectId: '00000000-0000-0000-0000-000000000001', justification: 'load test' });
  let r = http.post(`${BASE}/api/admin/ops/dsar/export`, payload, { headers: { ...headers, 'Content-Type': 'application/json' } });
  check(r, { '202': (res) => res.status === 202 });
}
