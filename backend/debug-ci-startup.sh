#!/bin/bash
# Debug script for CI startup issues

echo "=== CI Debug Script Starting ==="
echo "Date: $(date)"
echo "PWD: $(pwd)"
echo "User: $(whoami)"
echo "Java Version:"
java -version 2>&1

echo ""
echo "=== Environment Variables ==="
env | grep -E "(JAVA|DB_|QUARKUS|MAVEN)" | sort

echo ""
echo "=== Database Connection Test ==="
if command -v psql &> /dev/null; then
    echo "Testing PostgreSQL connection..."
    PGPASSWORD="${DB_PASSWORD:-freshplan}" psql -h localhost -U "${DB_USERNAME:-freshplan}" -d freshplan -c "SELECT version();" 2>&1 || echo "DB connection failed"
else
    echo "psql not available, skipping DB test"
fi

echo ""
echo "=== Memory Info ==="
if [ -f /proc/meminfo ]; then
    grep -E "MemTotal|MemAvailable" /proc/meminfo
fi

echo ""
echo "=== Disk Space ==="
df -h .

echo ""
echo "=== Migration Files ==="
ls -la src/main/resources/db/migration/ 2>/dev/null | tail -5 || echo "Migration dir not found"

echo ""
echo "=== Starting Application with Debug Logging ==="
echo "Running: java -Xms256m -Xmx512m -Dquarkus.profile=ci -jar target/quarkus-app/quarkus-run.jar"

# Start app with timeout and capture output
timeout 30s java -Xms256m -Xmx512m \
    -Dquarkus.profile=ci \
    -Dquarkus.log.level=DEBUG \
    -Dquarkus.log.category."io.quarkus".level=DEBUG \
    -Dquarkus.log.category."de.freshplan".level=DEBUG \
    -jar target/quarkus-app/quarkus-run.jar 2>&1 | tee startup.log &

APP_PID=$!
echo "App started with PID: $APP_PID"

# Wait for startup or timeout
sleep 20

echo ""
echo "=== Health Check ==="
curl -v http://localhost:8080/q/health 2>&1 || echo "Health check failed"

echo ""
echo "=== Last 50 lines of startup log ==="
tail -50 startup.log 2>/dev/null || echo "No startup log found"

# Kill the app
kill $APP_PID 2>/dev/null

echo ""
echo "=== Debug Script Complete ==="
