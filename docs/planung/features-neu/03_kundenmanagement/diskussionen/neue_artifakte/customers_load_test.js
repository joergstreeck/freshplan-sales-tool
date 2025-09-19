import http from 'k6/http'; import { check, sleep } from 'k6';
export const options = { vus: 25, duration: '2m', thresholds: { http_req_duration: ['p(95)<200'] } };
export default function () {
  const token = __ENV.TOKEN;
  const headers = { Authorization: `Bearer ${token}` };
  let r = http.get('https://api.example.com/api/customers?limit=50', { headers });
  check(r, { '200': (res) => res.status === 200 });
  sleep(1);
}