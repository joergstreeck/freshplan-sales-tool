#!/bin/bash

# FreshPlan - Quick Daily Cleanup
# Schnelle tägliche Bereinigung ohne Rückfragen

echo "🧹 Quick Cleanup..."

# Sichere Bereinigung (keine Rückfragen)
find . -name ".DS_Store" -delete 2>/dev/null
find . -name "*~" -delete 2>/dev/null
find . -name "*.swp" -delete 2>/dev/null
find . -name "*.tmp" -delete 2>/dev/null

# Alte Logs (älter als 3 Tage)
find . -name "*.log" -mtime +3 -delete 2>/dev/null

echo "✅ Fertig! Temporäre Dateien bereinigt."