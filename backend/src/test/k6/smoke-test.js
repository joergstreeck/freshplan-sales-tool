import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

// Custom counter for actual server errors (5xx)
const serverErrors = new Counter('server_errors');

export let options = {
  vus: 1, // 1 user for smoke test
  duration: '10s',
  thresholds: {
    http_req_duration: ['p(95)<200'], // 95% of requests must complete below 200ms
    // Only count 5xx as errors, not 401/403 (which are expected for protected endpoints)
    server_errors: ['count<5'], // Less than 5 server errors total
  },
};

const BASE_URL = 'http://localhost:8080';

// Helper to track actual server errors
function trackServerError(res, endpoint) {
  if (res.status >= 500) {
    serverErrors.add(1);
    console.log(`Server error at ${endpoint}: ${res.status}`);
  }
}

export default function () {
  // Health check - these MUST return 200
  let healthRes = http.get(`${BASE_URL}/q/health`);
  trackServerError(healthRes, '/q/health');
  check(healthRes, {
    'health check status is 200': (r) => r.status === 200,
    'health check response time < 200ms': (r) => r.timings.duration < 200,
  });

  // Core endpoints that should be available without auth
  let coreEndpoints = [
    '/q/health/ready',
    '/q/health/live',
  ];

  coreEndpoints.forEach(endpoint => {
    let res = http.get(`${BASE_URL}${endpoint}`);
    trackServerError(res, endpoint);
    check(res, {
      [`${endpoint} status is 200`]: (r) => r.status === 200,
      [`${endpoint} response time < 200ms`]: (r) => r.timings.duration < 200,
    });
  });

  // Protected API endpoints - 401/403 is expected, only 5xx is a problem
  // Note: These endpoints require authentication, so 401/403 is correct behavior
  let protectedEndpoints = [
    '/api/leads',
    '/api/customers',
  ];

  protectedEndpoints.forEach(endpoint => {
    let res = http.get(`${BASE_URL}${endpoint}`);
    trackServerError(res, endpoint);
    // 200, 401, 403 are all acceptable (depends on auth state)
    // Only 5xx indicates a real problem
    check(res, {
      [`${endpoint} not a server error`]: (r) => r.status < 500,
      [`${endpoint} response time < 200ms`]: (r) => r.timings.duration < 200,
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

  // Check server errors (5xx only)
  if (data.metrics.server_errors) {
    const serverErrorCount = data.metrics.server_errors.values.count || 0;
    console.log(`Server Errors (5xx): ${serverErrorCount} (Target: <5)`);
    if (serverErrorCount >= 5) {
      console.log('❌ Too many server errors');
      passed = false;
    } else {
      console.log('✅ Server errors within threshold');
    }
  } else {
    console.log('✅ No server errors detected');
  }

  // Note: 401/403 responses are NOT counted as errors
  // They are expected for protected endpoints without auth
  console.log('');
  console.log('Note: 401/403 responses from protected endpoints are expected');
  console.log('Only 5xx responses are counted as actual errors');

  // Return summary for CI
  return {
    'stdout': JSON.stringify({
      passed: passed,
      p95: data.metrics.http_req_duration?.values['p(95)'] || 0,
      serverErrors: data.metrics.server_errors?.values.count || 0,
    }, null, 2),
  };
}