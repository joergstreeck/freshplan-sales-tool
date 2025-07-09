#!/bin/bash

# Simple breadcrumb logging script for crash recovery
# Usage: ./scripts/log-step.sh "Description of what was done"

if [ -z "$1" ]; then
    echo "Usage: $0 \"Description of step\""
    exit 1
fi

# Get current date
DATE=$(date +%Y-%m-%d)
TIME=$(date +%H:%M:%S)

# Create directory if it doesn't exist
LOG_DIR="docs/claude-work/daily-work/$DATE"
mkdir -p "$LOG_DIR"

# Log file
LOG_FILE="$LOG_DIR/session-breadcrumbs.log"

# Append breadcrumb with timestamp
echo "[$TIME] $1" >> "$LOG_FILE"

# Confirm
echo "✓ Logged: $1"