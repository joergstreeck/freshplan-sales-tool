import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 1, // 1 user for smoke test
  duration: '10s',
  thresholds: {
    http_req_duration: ['p(95)<200'], // 95% of requests must complete below 200ms
    http_req_failed: ['rate<0.1'], // http errors should be less than 10%
  },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
  // Health check
  let healthRes = http.get(`${BASE_URL}/q/health`);
  check(healthRes, {
    'health check status is 200': (r) => r.status === 200,
    'health check response time < 200ms': (r) => r.timings.duration < 200,
  });

  // API endpoints that should be available
  let endpoints = [
    '/q/health/ready',
    '/q/health/live',
    '/q/metrics',
  ];

  endpoints.forEach(endpoint => {
    let res = http.get(`${BASE_URL}${endpoint}`);
    check(res, {
      [`${endpoint} status is 200`]: (r) => r.status === 200,
      [`${endpoint} response time < 200ms`]: (r) => r.timings.duration < 200,
    });
  });

  // Test Lead API endpoints (if available)
  let leadEndpoints = [
    { method: 'GET', url: '/api/leads', expectedStatus: [200, 401, 403] },
    { method: 'GET', url: '/api/leads/statistics', expectedStatus: [200, 401, 403] },
  ];

  leadEndpoints.forEach(endpoint => {
    let res;
    if (endpoint.method === 'GET') {
      res = http.get(`${BASE_URL}${endpoint.url}`);
    }

    check(res, {
      [`${endpoint.url} status is acceptable`]: (r) => endpoint.expectedStatus.includes(r.status),
      [`${endpoint.url} response time < 200ms`]: (r) => r.timings.duration < 200,
    });
  });

  sleep(0.5);
}

export function handleSummary(data) {
  console.log('Performance Test Summary:');
  console.log('========================');

  // Check if we met our thresholds
  let passed = true;

  if (data.metrics.http_req_duration) {
    const p95 = data.metrics.http_req_duration.values['p(95)'];
    console.log(`P95 Response Time: ${p95.toFixed(2)}ms (Target: <200ms)`);
    if (p95 > 200) {
      console.log('❌ P95 response time exceeded threshold');
      passed = false;
    } else {
      console.log('✅ P95 response time within threshold');
    }
  }

  if (data.metrics.http_req_failed) {
    const failRate = data.metrics.http_req_failed.values.rate;
    console.log(`Error Rate: ${(failRate * 100).toFixed(2)}% (Target: <10%)`);
    if (failRate > 0.1) {
      console.log('❌ Error rate exceeded threshold');
      passed = false;
    } else {
      console.log('✅ Error rate within threshold');
    }
  }

  // Return summary for CI
  return {
    'stdout': JSON.stringify({
      passed: passed,
      p95: data.metrics.http_req_duration?.values['p(95)'] || 0,
      errorRate: data.metrics.http_req_failed?.values.rate || 0,
    }, null, 2),
  };
}