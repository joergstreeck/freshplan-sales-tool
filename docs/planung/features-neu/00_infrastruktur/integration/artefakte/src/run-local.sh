#!/usr/bin/env bash
set -euo pipefail
export DB_JDBC_URL=${DB_JDBC_URL:-jdbc:postgresql://localhost:5432/freshplan}
export DB_USERNAME=${DB_USERNAME:-app_admin}
export DB_PASSWORD=${DB_PASSWORD:-secret}
export DECK_BIN=${DECK_BIN:-deck}
export KONG_ADDR=${KONG_ADDR:-http://localhost:8001}
export SYNC_DRY_RUN=${SYNC_DRY_RUN:-true}
export SYNC_INTERVAL_SEC=${SYNC_INTERVAL_SEC:-300}
export AUDIT_LOG=${AUDIT_LOG:-./audit/settings-gateway-sync.jsonl}

java -jar target/settings-gateway-sync-runner.jar
