import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 25,
  duration: '2m',
  thresholds: { http_req_duration: ['p(95)<200'] }
};

export default function () {
  const token = __ENV.TOKEN;
  const headers = { Authorization: `Bearer ${token}` };
  const res = http.get('https://api.example.com/api/leads?limit=50', { headers });
  check(res, { 'status 200': (r) => r.status === 200 });
  sleep(1);
}
