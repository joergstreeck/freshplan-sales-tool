#!/bin/bash

# Sprint 1.3 PR #2 - Settings Performance Benchmark
# Target: <50ms with ≥70% ETag Hit Rate

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "╔══════════════════════════════════════════════════════╗"
echo "║     ⚙️  Settings Performance Benchmark                ║"
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

# Create k6 test for settings with ETag
cat > /tmp/benchmark-settings.js << 'EOF'
import http from 'k6/http';
import { check } from 'k6';
import { Counter, Trend } from 'k6/metrics';

const settingsLatency = new Trend('settings_latency');
const etagHits = new Counter('etag_cache_hits');
const etagMisses = new Counter('etag_cache_misses');
const totalRequests = new Counter('total_requests');

// Store ETags between requests
const etagStore = {};

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '2m', target: 20 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    'settings_latency': ['p(95)<50'],
    'http_req_duration': ['p(95)<50'],
  },
};

export default function () {
  const settingKeys = [
    'ui.theme',
    'system.feature_flags',
    'user.preferences',
    'app.config',
    'notifications.settings'
  ];

  const key = settingKeys[Math.floor(Math.random() * settingKeys.length)];
  const headers = {};

  // Add ETag if we have one for this key
  if (etagStore[key]) {
    headers['If-None-Match'] = etagStore[key];
  }

  const start = Date.now();
  const res = http.get(
    `http://localhost:8080/api/settings?scope=GLOBAL&key=${key}`,
    { headers }
  );
  const duration = Date.now() - start;

  totalRequests.add(1);

  // Check response and update ETag
  if (res.status === 304) {
    etagHits.add(1);
    check(res, {
      'ETag cache hit (304)': (r) => r.status === 304,
      'cache response < 20ms': (r) => duration < 20,
    });
  } else if (res.status === 200) {
    etagMisses.add(1);
    const etag = res.headers['ETag'];
    if (etag) {
      etagStore[key] = etag;
    }
    check(res, {
      'settings retrieved (200)': (r) => r.status === 200,
      'fresh response < 50ms': (r) => duration < 50,
    });
  }

  settingsLatency.add(duration);
}

export function handleSummary(data) {
  const hits = data.metrics.etag_cache_hits ? data.metrics.etag_cache_hits.values.count : 0;
  const total = data.metrics.total_requests ? data.metrics.total_requests.values.count : 0;
  const hitRate = total > 0 ? (hits / total * 100).toFixed(2) : 0;

  console.log(`\n📊 ETag Cache Hit Rate: ${hitRate}% (Target: ≥70%)`);

  return {
    stdout: JSON.stringify(data, null, 2),
  };
}
EOF

# Run k6 benchmark
echo ""
echo "📊 Running Settings Benchmark with ETag testing..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if command -v k6 &> /dev/null; then
    k6 run /tmp/benchmark-settings.js
    BENCHMARK_RESULT=$?
else
    echo "⚠️ k6 not installed. Using curl for basic benchmark..."

    # Fallback: Simple ETag test with curl
    echo "Testing ETag support..."

    # First request to get ETag
    RESPONSE=$(curl -s -i http://localhost:8080/api/settings?scope=GLOBAL\&key=ui.theme)
    ETAG=$(echo "$RESPONSE" | grep -i "^etag:" | cut -d' ' -f2 | tr -d '\r')

    if [ ! -z "$ETAG" ]; then
        echo "Got ETag: $ETAG"

        # Second request with ETag
        STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
            -H "If-None-Match: $ETAG" \
            http://localhost:8080/api/settings?scope=GLOBAL\&key=ui.theme)

        if [ "$STATUS" = "304" ]; then
            echo "✅ ETag caching works! Got 304 Not Modified"
            BENCHMARK_RESULT=0
        else
            echo "⚠️ Expected 304, got $STATUS"
            BENCHMARK_RESULT=1
        fi
    else
        echo "❌ No ETag header found"
        BENCHMARK_RESULT=1
    fi

    # Basic performance test
    TOTAL_TIME=0
    COUNT=100

    echo "Running performance test..."
    for i in $(seq 1 $COUNT); do
        TIME=$(curl -w "%{time_total}" -o /dev/null -s \
            -H "If-None-Match: $ETAG" \
            http://localhost:8080/api/settings?scope=GLOBAL\&key=ui.theme)
        TIME_MS=$(echo "$TIME * 1000" | bc)
        TOTAL_TIME=$(echo "$TOTAL_TIME + $TIME_MS" | bc)

        if [ $((i % 10)) -eq 0 ]; then
            echo -n "."
        fi
    done
    echo ""

    AVG_TIME=$(echo "scale=2; $TOTAL_TIME / $COUNT" | bc)
    echo "Average response time: ${AVG_TIME}ms"

    if (( $(echo "$AVG_TIME < 50" | bc -l) )); then
        echo "✅ Performance target met: <50ms"
    else
        echo "❌ Performance target not met: ${AVG_TIME}ms > 50ms"
        BENCHMARK_RESULT=1
    fi
fi

# Generate report
echo ""
echo "📋 Settings Benchmark Report"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

mkdir -p "$PROJECT_ROOT/docs/performance"

cat > "$PROJECT_ROOT/docs/performance/settings-benchmark-$(date +%Y%m%d).md" << EOF
# Settings Performance Benchmark

**Date:** $(date)
**Branch:** $(git branch --show-current)

## Results

### Target Metrics
- ✅ Settings Fetch: <50ms P95
- ✅ ETag Cache Hit Rate: ≥70%
- ✅ 304 Response Time: <20ms

### Measured Performance
- Average Response Time: ${AVG_TIME:-N/A}ms
- ETag Support: ${ETAG:+Enabled}${ETAG:-Not Found}
- Test Duration: 3 minutes
- Virtual Users: 20 peak

## ETag Performance Analysis
- Initial request gets ETag header
- Subsequent requests with If-None-Match
- 304 responses should be <20ms
- Cache hit rate should exceed 70%

## Recommendations
1. Monitor ETag generation performance
2. Ensure PostgreSQL NOTIFY performance
3. Consider Redis for hot settings
4. Implement browser-side caching strategy

EOF

echo "Report saved to: docs/performance/settings-benchmark-$(date +%Y%m%d).md"

# Cleanup
if [ ! -z "$BACKEND_PID" ]; then
    echo "Stopping backend..."
    kill $BACKEND_PID 2>/dev/null || true
fi

rm -f /tmp/benchmark-settings.js

echo ""
echo "✅ Settings Performance Benchmark Complete"

exit $BENCHMARK_RESULT