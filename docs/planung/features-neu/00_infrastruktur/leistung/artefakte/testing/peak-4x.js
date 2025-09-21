import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
  vus: 200,
  duration: '20m',
  thresholds: { http_req_duration: ['p(95)<350'] }
};

export default function () {
  const headers = { 'Authorization': `Bearer ${__ENV.TOKEN}`, 'X-Correlation-Id': `${__VU}-${Date.now()}` };
  http.get(`${__ENV.API}/api/credit/precheck?customerId=${__ENV.CUSTOMER}`, { headers });
  sleep(1);
}
