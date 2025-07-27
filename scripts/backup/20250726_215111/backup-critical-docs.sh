#!/bin/bash

echo "🔒 Backing up critical documentation"
echo "==================================="
echo ""

# Create backup directory with timestamp
BACKUP_DIR="docs/backups/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

# List of critical files to backup
CRITICAL_FILES=(
    "docs/CLAUDE.md"
    "docs/STANDARDUBERGABE_NEU.md"
    "docs/STANDARDUBERGABE_KOMPAKT.md"
    "docs/STANDARDUBERGABE.md"
    "docs/TRIGGER_TEXTS.md"
    "docs/CRM_COMPLETE_MASTER_PLAN.md"
)

# Copy files
echo "📋 Backing up critical files..."
for file in "${CRITICAL_FILES[@]}"; do
    if [ -f "$file" ]; then
        cp "$file" "$BACKUP_DIR/"
        echo "✅ Backed up: $file"
    else
        echo "⚠️  Not found: $file"
    fi
done

# Create a README in backup directory
cat > "$BACKUP_DIR/README.md" << EOF
# Critical Documentation Backup

**Created:** $(date)
**Purpose:** Backup of critical FreshPlan documentation

## Included Files:
- CLAUDE.md - Working guidelines
- STANDARDUBERGABE_NEU.md - Main handover process
- STANDARDUBERGABE_KOMPAKT.md - Quick reference
- STANDARDUBERGABE.md - Extended troubleshooting
- TRIGGER_TEXTS.md - Official trigger texts
- CRM_COMPLETE_MASTER_PLAN.md - Current master plan

## Restore Instructions:
To restore these files, copy them back to the docs/ directory:
\`\`\`bash
cp $BACKUP_DIR/*.md ../../
\`\`\`
EOF

# Create a compressed archive
echo ""
echo "📦 Creating compressed archive..."
tar -czf "docs/backups/critical_docs_$(date +%Y%m%d_%H%M%S).tar.gz" -C "$BACKUP_DIR" .

# Keep only last 10 backups
echo ""
echo "🧹 Cleaning old backups (keeping last 10)..."
ls -t docs/backups/*.tar.gz 2>/dev/null | tail -n +11 | xargs rm -f 2>/dev/null

echo ""
echo "✅ Backup complete!"
echo "📁 Location: $BACKUP_DIR"
echo "📦 Archive: docs/backups/critical_docs_$(date +%Y%m%d_%H%M%S).tar.gz"