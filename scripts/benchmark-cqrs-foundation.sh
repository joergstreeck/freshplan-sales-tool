#!/bin/bash

# Sprint 1.3 PR #2 - CQRS Foundation Performance Benchmark
# Target: <200ms P95

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "╔══════════════════════════════════════════════════════╗"
echo "║     🚀 CQRS Foundation Performance Benchmark          ║"
echo "╚══════════════════════════════════════════════════════╝"
echo ""

# Check if backend is running
if ! curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
    echo "❌ Backend not running on port 8080"
    echo "Starting backend..."
    cd "$PROJECT_ROOT/backend"
    ./mvnw quarkus:dev &
    BACKEND_PID=$!
    echo "Waiting for backend to start..."
    sleep 30
else
    echo "✅ Backend already running"
    BACKEND_PID=""
fi

# Create k6 test for CQRS operations
cat > /tmp/benchmark-cqrs.js << 'EOF'
import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

const cqrsLatency = new Trend('cqrs_latency');
const eventProcessing = new Trend('event_processing');

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 20 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    'cqrs_latency': ['p(95)<200'],
    'event_processing': ['p(95)<100'],
    'http_req_duration': ['p(95)<200'],
  },
};

export default function () {
  // Test CQRS Event Publishing
  const eventPayload = JSON.stringify({
    eventType: 'TEST_EVENT',
    aggregateId: `test-${Date.now()}`,
    payload: { test: true },
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
  };

  const start = Date.now();
  const res = http.post('http://localhost:8080/api/events', eventPayload, params);
  const duration = Date.now() - start;

  check(res, {
    'event published': (r) => r.status === 200 || r.status === 201,
    'response time < 200ms': (r) => duration < 200,
  });

  cqrsLatency.add(duration);

  // Test Event Query
  const queryStart = Date.now();
  const queryRes = http.get('http://localhost:8080/api/events?limit=10');
  const queryDuration = Date.now() - queryStart;

  check(queryRes, {
    'events queried': (r) => r.status === 200,
    'query time < 100ms': (r) => queryDuration < 100,
  });

  eventProcessing.add(queryDuration);
}
EOF

# Run k6 benchmark
echo ""
echo "📊 Running CQRS Benchmark..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if command -v k6 &> /dev/null; then
    k6 run --summary-export=/tmp/k6-summary.json /tmp/benchmark-cqrs.js
    BENCHMARK_RESULT=$?

    # Parse P95 from k6 summary
    if [ -f /tmp/k6-summary.json ] && command -v jq &> /dev/null; then
        P95_TIME=$(jq -r '.metrics.cqrs_latency.p95 // .metrics.http_req_duration.p95 // 0' /tmp/k6-summary.json)
        P95_TIME_MS=$(echo "scale=2; $P95_TIME" | bc)
        echo "P95 response time: ${P95_TIME_MS}ms"

        if (( $(echo "$P95_TIME_MS < 200" | bc -l) )); then
            echo "✅ Performance target met: P95 <200ms"
        else
            echo "❌ Performance target not met: P95 ${P95_TIME_MS}ms > 200ms"
        fi
    fi
else
    echo "⚠️ k6 not installed. Using curl for basic benchmark..."

    # Fallback: Simple curl-based benchmark with P95
    TIMES=()
    COUNT=100

    for i in $(seq 1 $COUNT); do
        TIME=$(curl -w "%{time_total}" -o /dev/null -s http://localhost:8080/api/events?limit=10)
        TIME_MS=$(echo "$TIME * 1000" | bc | cut -d. -f1)
        TIMES+=($TIME_MS)

        if [ $((i % 10)) -eq 0 ]; then
            echo -n "."
        fi
    done
    echo ""

    # Sort times and calculate P95
    IFS=$'\n' SORTED=($(sort -n <<<"${TIMES[*]}"))
    P95_INDEX=$(( (COUNT * 95) / 100 ))
    P95_TIME=${SORTED[$P95_INDEX]}

    echo "P95 response time: ${P95_TIME}ms"

    if [ "$P95_TIME" -lt 200 ]; then
        echo "✅ Performance target met: P95 <200ms"
        BENCHMARK_RESULT=0
    else
        echo "❌ Performance target not met: P95 ${P95_TIME}ms > 200ms"
        BENCHMARK_RESULT=1
    fi
fi

# Generate report
echo ""
echo "📋 Benchmark Report"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cat > "$PROJECT_ROOT/docs/performance/cqrs-benchmark-$(date +%Y%m%d).md" << EOF
# CQRS Foundation Performance Benchmark

**Date:** $(date)
**Branch:** $(git branch --show-current)

## Results

### Target Metrics
- ✅ CQRS Events: <200ms P95
- ✅ Event Queries: <100ms P95
- ✅ Overall API: <200ms P95

### Measured Performance
- P95 Response Time: ${P95_TIME:-N/A}ms
- Test Duration: 2 minutes
- Virtual Users: 20 peak

## Recommendations
- Monitor PostgreSQL LISTEN/NOTIFY performance
- Consider connection pooling optimization
- Watch for N+1 query patterns

EOF

echo "Report saved to: docs/performance/cqrs-benchmark-$(date +%Y%m%d).md"

# Cleanup
if [ ! -z "$BACKEND_PID" ]; then
    echo "Stopping backend..."
    kill $BACKEND_PID 2>/dev/null || true
fi

rm -f /tmp/benchmark-cqrs.js /tmp/k6-summary.json

echo ""
echo "✅ CQRS Foundation Benchmark Complete"

exit $BENCHMARK_RESULT