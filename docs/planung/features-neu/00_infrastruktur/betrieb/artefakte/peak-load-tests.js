import http from 'k6/http';
import { sleep } from 'k6';
import { Trend } from 'k6/metrics';

export const options = {
  scenarios: {
    peak5x: {
      executor: 'ramping-arrival-rate',
      startRate: 50,
      timeUnit: '1s',
      preAllocatedVUs: 200,
      stages: [
        { target: 250, duration: '10m' }, // ramp to ~5x baseline
        { target: 250, duration: '20m' }, // hold
        { target: 0, duration: '5m' }     // ramp down
      ]
    }
  },
  thresholds: {
    http_req_duration: ['p(95)<500'], // peak p95 target
    'http_req_failed{scenario:peak5x}': ['rate<0.02']
  }
};

const creditTrend = new Trend('credit_precheck_ms');

export default function () {
  const headers = {
    'Authorization': `Bearer ${__ENV.TOKEN}`,
    'X-Correlation-Id': `${__VU}-${Date.now()}`
  };

  // Credit PreCheck hot path
  let r1 = http.get(`${__ENV.API}/api/credit/precheck?customerId=${__ENV.CUSTOMER}`, { headers });
  creditTrend.add(r1.timings.duration);

  // Activity create (idempotent key omitted in GET example; use for POST in real tests)
  sleep(1);
}
