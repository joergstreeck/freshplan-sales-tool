#!/bin/bash

# Quick status overview
echo "📊 FreshPlan Quick Status - $(date '+%H:%M')"
echo "=================================="

# Git
echo -n "🌿 Git: "
echo -n "$(git branch --show-current) | "
echo "$(git status --porcelain | wc -l) changes"

# Services
echo -n "🚀 Services: "
BACKEND_STATUS=$(lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null && echo "✅" || echo "❌")
FRONTEND_STATUS=$(lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null && echo "✅" || echo "❌")
DB_STATUS=$(lsof -Pi :5432 -sTCP:LISTEN -t >/dev/null && echo "✅" || echo "❌")
echo "Backend $BACKEND_STATUS | Frontend $FRONTEND_STATUS | DB $DB_STATUS"

# TODOs
echo -n "📋 TODOs: "
echo "[Run TodoRead for details]"

# Recent files
echo "📝 Recently modified:"
find . -name "*.java" -o -name "*.ts" -o -name "*.tsx" | grep -v node_modules | grep -v target | xargs ls -lt 2>/dev/null | head -3 | awk '{print "   " $9}'

echo "=================================="