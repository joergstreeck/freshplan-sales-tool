#!/bin/bash
set -euo pipefail

# CI Test Runner mit Retry-Logic und Timeout-Protection
# Verhindert Hangs und macht Tests robuster

echo "🚀 Starting CI Test Runner..."

# Environment setup
export CI=true
export QUARKUS_PROFILE=ci
export MAVEN_OPTS="-Xmx1024m -XX:MaxMetaspaceSize=512m"

# DevServices müssen zur Build-Zeit deaktiviert sein
export QUARKUS_DEVSERVICES_ENABLED=false
export QUARKUS_DATASOURCE_DEVSERVICES_ENABLED=false

# Test configuration
MAX_RETRIES=2
CURRENT_RETRY=0
TEST_TIMEOUT=900  # 15 minutes total timeout

# Function to run tests with timeout
run_tests() {
    local attempt=$1
    echo "📋 Test attempt $attempt of $MAX_RETRIES..."

    # Use timeout to prevent infinite hangs
    timeout $TEST_TIMEOUT ./mvnw -B test \
        -Dtest="A00_EnvDiagTest,*Test" \
        -DexcludedGroups="integration,slow" \
        -DfailIfNoTests=false \
        -Dquarkus.profile=ci \
        -Dquarkus.devservices.enabled=false \
        -Dquarkus.datasource.devservices.enabled=false \
        -Dquarkus.test.hang-detection-timeout=60s \
        -Dquarkus.test.continuous-testing=disabled \
        -Dsurefire.rerunFailingTestsCount=1 \
        -Dtest.run.id=${GITHUB_RUN_ID:-local}
}

# Main test execution with retry
while [ $CURRENT_RETRY -lt $MAX_RETRIES ]; do
    CURRENT_RETRY=$((CURRENT_RETRY + 1))

    if run_tests $CURRENT_RETRY; then
        echo "✅ Tests passed on attempt $CURRENT_RETRY"
        exit 0
    else
        EXIT_CODE=$?

        # Check if it was a timeout
        if [ $EXIT_CODE -eq 124 ]; then
            echo "⏱️ Tests timed out after ${TEST_TIMEOUT}s on attempt $CURRENT_RETRY"
        else
            echo "❌ Tests failed with exit code $EXIT_CODE on attempt $CURRENT_RETRY"
        fi

        if [ $CURRENT_RETRY -lt $MAX_RETRIES ]; then
            echo "🔄 Retrying in 5 seconds..."
            sleep 5

            # Clean up any hanging processes
            pkill -f "maven-surefire" || true
            pkill -f "QuarkusTest" || true
        fi
    fi
done

echo "❌ Tests failed after $MAX_RETRIES attempts"
exit 1