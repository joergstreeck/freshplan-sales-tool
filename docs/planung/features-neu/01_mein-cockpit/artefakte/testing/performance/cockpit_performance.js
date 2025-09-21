import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = { vus: 25, duration: '2m', thresholds: { http_req_duration: ['p(95)<200'], checks: ['rate>0.99'] } };

export default function () {
  const token = __ENV.TOKEN;
  const headers = { Authorization: `Bearer ${token}` };

  let r = http.get('https://api.example.com/api/cockpit/summary?range=30d', { headers });
  check(r, { 'summary 200': (res) => res.status === 200 });

  r = http.post('https://api.example.com/api/cockpit/roi/calc', JSON.stringify({
    mealsPerDay: 120, daysPerMonth: 26, laborMinutesSavedPerMeal: 2.5,
    laborCostPerHour: 22, foodCostPerMeal: 3.2, wasteReductionPct: 8, channel: 'DIRECT', investment: 15000
  }), { headers: { ...headers, 'Content-Type': 'application/json' } });
  check(r, { 'roi 200': (res) => res.status === 200 });

  sleep(1);
}
