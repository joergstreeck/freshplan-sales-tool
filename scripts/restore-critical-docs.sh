#!/bin/bash

echo "🔄 FreshPlan Critical Documentation Restore"
echo "=========================================="
echo ""

# Check if backup directory exists
if [ ! -d "docs/backups" ]; then
    echo "❌ No backup directory found!"
    exit 1
fi

# List available backups
echo "📋 Available backups:"
echo ""
ls -la docs/backups/*.tar.gz 2>/dev/null | tail -10
echo ""

# Find latest backup
LATEST_BACKUP=$(ls -t docs/backups/*.tar.gz 2>/dev/null | head -1)

if [ -z "$LATEST_BACKUP" ]; then
    echo "❌ No backup archives found!"
    exit 1
fi

echo "📦 Latest backup: $LATEST_BACKUP"
echo ""
echo "Do you want to restore from this backup? (y/n)"
read -r response

if [[ "$response" != "y" && "$response" != "Y" ]]; then
    echo "❌ Restore cancelled"
    exit 0
fi

# Create temporary extraction directory
TEMP_DIR=$(mktemp -d)
echo "📂 Extracting to temporary directory..."
tar -xzf "$LATEST_BACKUP" -C "$TEMP_DIR"

# Show what will be restored
echo ""
echo "📋 Files to be restored:"
ls -la "$TEMP_DIR"/*.md | grep -v README.md

echo ""
echo "⚠️  WARNING: This will overwrite existing files!"
echo "Continue? (y/n)"
read -r confirm

if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
    echo "❌ Restore cancelled"
    rm -rf "$TEMP_DIR"
    exit 0
fi

# Restore files
echo ""
echo "📥 Restoring files..."
for file in "$TEMP_DIR"/*.md; do
    if [ -f "$file" ] && [ "$(basename "$file")" != "README.md" ]; then
        cp "$file" docs/
        echo "✅ Restored: docs/$(basename "$file")"
    fi
done

# Cleanup
rm -rf "$TEMP_DIR"

echo ""
echo "✅ Restore complete!"
echo ""
echo "💡 Verify restored files:"
echo "   ls -la docs/STANDARDUBERGABE*.md"
echo "   ls -la docs/TRIGGER_TEXTS.md"