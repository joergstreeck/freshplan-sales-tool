#!/usr/bin/env python3
"""
Design System Refactoring Helper
==================================
Automatisches Refactoring von häufigen Design System Violations
"""

import re
import sys
from pathlib import Path
from typing import Dict, List

SCRIPT_DIR = Path(__file__).parent
PROJECT_ROOT = SCRIPT_DIR.parent
FRONTEND_SRC = PROJECT_ROOT / "frontend/src"

# Refactoring Patterns
REFACTORINGS = [
    # Fonts: Antonio (nur wenn inline, nicht in Typography components)
    (r"sx=\{\{\s*fontFamily:\s*['\"]Antonio,\s*sans-serif['\"]",
     "sx={{ fontFamily: theme => theme.typography.h4.fontFamily"),
    (r"fontFamily:\s*['\"]Antonio,\s*sans-serif['\"]",
     "fontFamily: theme => theme.typography.h4.fontFamily"),

    # Fonts: Poppins (nur wenn inline)
    (r"sx=\{\{\s*fontFamily:\s*['\"]Poppins,\s*sans-serif['\"]",
     "sx={{ fontFamily: theme => theme.typography.body1.fontFamily"),
    (r"fontFamily:\s*['\"]Poppins,\s*sans-serif['\"]",
     "fontFamily: theme => theme.typography.body1.fontFamily"),

    # Fonts: Roboto (Legacy - sollte zu Poppins werden)
    (r"fontFamily:\s*['\"]Roboto,\s*Helvetica,\s*Arial,\s*sans-serif['\"]",
     "fontFamily: theme => theme.typography.body1.fontFamily"),

    # Fonts: Monospace
    (r"fontFamily:\s*['\"]monospace['\"]",
     "fontFamily: '\"Courier New\", Courier, monospace'"),

    # Primary green variants (#94C456, #7BA646, #7BA545, #7AA348, #7FB03E, #7FB03F, #7AA845, #7aa845)
    (r"color:\s*['\"]#94C456['\"]",
     "color: 'primary.main'"),
    (r"color:\s*['\"]#7BA646['\"]",
     "color: 'primary.dark'"),
    (r"color:\s*['\"]#7BA545['\"]",
     "color: 'primary.dark'"),
    (r"color:\s*['\"]#7AA348['\"]",
     "color: 'primary.dark'"),
    (r"color:\s*['\"]#7FB03E['\"]",
     "color: 'primary.dark'"),
    (r"color:\s*['\"]#7FB03F['\"]",
     "color: 'primary.dark'"),
    (r"color:\s*['\"]#7BA347['\"]",
     "color: 'primary.dark'"),
    (r"color:\s*['\"]#7aa845['\"]",
     "color: 'primary.dark'"),
    (r"bgcolor:\s*['\"]#94C456['\"]",
     "bgcolor: 'primary.main'"),
    (r"bgcolor:\s*['\"]#7BA646['\"]",
     "bgcolor: 'primary.dark'"),
    (r"bgcolor:\s*['\"]#7BA545['\"]",
     "bgcolor: 'primary.dark'"),
    (r"bgcolor:\s*['\"]#7BA347['\"]",
     "bgcolor: 'primary.dark'"),
    (r"bgcolor:\s*['\"]#7aa845['\"]",
     "bgcolor: 'primary.dark'"),
    (r"backgroundColor:\s*['\"]#7BA347['\"]",
     "backgroundColor: 'primary.dark'"),
    (r"'&:hover':\s*\{\s*bgcolor:\s*['\"]#7FB03E['\"]",
     "'&:hover': { bgcolor: 'primary.dark'"),
    (r"'&:hover':\s*\{\s*bgcolor:\s*['\"]#7aa845['\"]",
     "'&:hover': { bgcolor: 'primary.dark'"),
    (r"borderColor:\s*['\"]#7AA348['\"]",
     "borderColor: 'primary.dark'"),

    # Secondary blue variants (#004F7B, #003A5A, #2196F3, #1976d2, #1976D2)
    (r"color:\s*['\"]#004F7B['\"]",
     "color: 'secondary.main'"),
    (r"color:\s*['\"]#003A5A['\"]",
     "color: 'secondary.dark'"),
    (r"borderColor:\s*['\"]#003A5A['\"]",
     "borderColor: 'secondary.dark'"),
    (r"color:\s*['\"]#2196F3['\"]",
     "color: 'info.main'"),
    (r"color:\s*['\"]#2196f3['\"]",
     "color: 'info.main'"),
    (r"color:\s*['\"]#1976d2['\"]",
     "color: 'info.main'"),
    (r"color:\s*['\"]#1976D2['\"]",
     "color: 'info.main'"),
    (r"bgcolor:\s*['\"]#2196F3['\"]",
     "bgcolor: 'info.main'"),
    (r"primaryColor:\s*['\"]#1976d2['\"]",
     "primaryColor: theme.palette.info.main"),

    # Common status colors - success/green shades
    (r"color:\s*['\"]#4CAF50['\"]",
     "color: 'success.main'"),
    (r"color:\s*['\"]#4caf50['\"]",
     "color: 'success.main'"),
    (r"color:\s*['\"]#66BB6A['\"]",
     "color: 'success.main'"),
    (r"color:\s*['\"]#66bb6a['\"]",
     "color: 'success.main'"),

    # Warning/orange shades
    (r"color:\s*['\"]#FF9800['\"]",
     "color: 'warning.main'"),
    (r"color:\s*['\"]#ff9800['\"]",
     "color: 'warning.main'"),
    (r"color:\s*['\"]#FFA726['\"]",
     "color: 'warning.main'"),
    (r"color:\s*['\"]#FFC107['\"]",
     "color: 'warning.main'"),

    # Error/red shades
    (r"color:\s*['\"]#F44336['\"]",
     "color: 'error.main'"),
    (r"color:\s*['\"]#f44336['\"]",
     "color: 'error.main'"),
    (r"color:\s*['\"]#EF5350['\"]",
     "color: 'error.main'"),
    (r"color:\s*['\"]#D32F2F['\"]",
     "color: 'error.dark'"),
    (r"color:\s*['\"]#d32f2f['\"]",
     "color: 'error.dark'"),
    (r"color:\s*['\"]#FF6B6B['\"]",
     "color: 'error.light'"),

    # Background colors - grey shades
    (r"bgcolor:\s*['\"]#F5F5F5['\"]",
     "bgcolor: 'grey.100'"),
    (r"bgcolor:\s*['\"]#f5f5f5['\"]",
     "bgcolor: 'grey.100'"),
    (r"bgcolor:\s*['\"]#FAFAFA['\"]",
     "bgcolor: 'grey.50'"),
    (r"bgcolor:\s*['\"]#fafafa['\"]",
     "bgcolor: 'grey.50'"),
    (r"bgcolor:\s*['\"]#f8f9fa['\"]",
     "bgcolor: 'grey.50'"),
    (r"backgroundColor:\s*['\"]#F5F5F5['\"]",
     "backgroundColor: 'grey.100'"),
    (r"backgroundColor:\s*['\"]#FAFAFA['\"]",
     "backgroundColor: 'grey.50'"),
    (r"contentBackground\s*=\s*['\"]#FAFAFA['\"]",
     "contentBackground = theme.palette.grey[50]"),

    # Grey/neutral colors
    (r"color:\s*['\"]#666666['\"]",
     "color: 'text.secondary'"),
    (r"color:\s*['\"]#9E9E9E['\"]",
     "color: 'grey.500'"),
    (r"color:\s*['\"]#9e9e9e['\"]",
     "color: 'grey.500'"),
    (r"bgcolor:\s*['\"]#9E9E9E['\"]",
     "bgcolor: 'grey.500'"),
    (r"color:\s*['\"]#e0e0e0['\"]",
     "color: 'grey.300'"),
    (r"backgroundColor:\s*['\"]#e0e0e0['\"]",
     "backgroundColor: 'grey.300'"),
    (r"color:\s*['\"]#999['\"]",
     "color: 'grey.600'"),
    (r"color:\s*['\"]#333['\"]",
     "color: 'grey.800'"),
    (r"color:\s*['\"]#666['\"]",
     "color: 'grey.600'"),
    (r"INAKTIV:\s*['\"]#999['\"]",
     "INAKTIV: theme.palette.grey[600]"),
    (r"GESPERRT:\s*['\"]#F44336['\"]",
     "GESPERRT: theme.palette.error.main"),

    # Ternary conditions (simple patterns)
    (r"\?\s*['\"]#f44336['\"]\s*:\s*['\"]#4caf50['\"]",
     "? 'error.main' : 'success.main'"),
    (r"\?\s*['\"]#4caf50['\"]\s*:\s*['\"]#ff9800['\"]",
     "? 'success.main' : 'warning.main'"),
    (r"\?\s*['\"]#94C456['\"]\s*:\s*['\"]#ff9800['\"]",
     "? 'primary.main' : 'warning.main'"),

    # Return statements
    (r"return\s*['\"]#F44336['\"];",
     "return theme.palette.error.main;"),
    (r"return\s*['\"]#FF9800['\"];",
     "return theme.palette.warning.main;"),
    (r"return\s*['\"]#4CAF50['\"];",
     "return theme.palette.success.main;"),

    # Providers/Snackbar colors
    (r"background:\s*['\"]#363636['\"]",
     "background: theme.palette.grey[800]"),
    (r"background:\s*['\"]#ef5350['\"]",
     "background: theme.palette.error.main"),

    # Hover states
    (r"'&:hover':\s*\{\s*bgcolor:\s*['\"]#003d62['\"]",
     "'&:hover': { bgcolor: 'secondary.dark'"),
]

def refactor_file(file_path: Path, dry_run: bool = False) -> Dict[str, int]:
    """Refactor a single file"""
    stats = {'changes': 0, 'file': str(file_path.relative_to(PROJECT_ROOT))}

    try:
        content = file_path.read_text()
        original_content = content

        for pattern, replacement in REFACTORINGS:
            matches = len(re.findall(pattern, content))
            if matches > 0:
                content = re.sub(pattern, replacement, content)
                stats['changes'] += matches

        if stats['changes'] > 0 and not dry_run:
            file_path.write_text(content)
            print(f"✅ {stats['file']}: {stats['changes']} changes")
        elif stats['changes'] > 0:
            print(f"🔍 {stats['file']}: {stats['changes']} changes (dry-run)")

    except Exception as e:
        print(f"❌ Error in {file_path}: {e}")

    return stats

def main():
    import argparse
    parser = argparse.ArgumentParser(description='Refactor Design System violations')
    parser.add_argument('--dry-run', action='store_true', help='Show what would be changed without modifying files')
    parser.add_argument('--file', type=str, help='Refactor specific file only')
    args = parser.parse_args()

    print("🔧 Design System Refactoring")
    print("━" * 50)

    if args.file:
        file_path = PROJECT_ROOT / args.file
        if file_path.exists():
            stats = refactor_file(file_path, args.dry_run)
            print(f"\n📊 Total: {stats['changes']} changes")
        else:
            print(f"❌ File not found: {args.file}")
            sys.exit(1)
        return

    # Find all TS/TSX files
    tsx_files = list(FRONTEND_SRC.glob("**/*.tsx"))
    ts_files = list(FRONTEND_SRC.glob("**/*.ts"))

    # Exclude theme files
    EXCLUDE = ['theme/freshfoodz.ts', 'theme/ThemeProvider.tsx', 'types/theme-v2.mui.ts']

    all_files = [
        f for f in (tsx_files + ts_files)
        if 'node_modules' not in str(f)
        and 'dist' not in str(f)
        and '.test.' not in str(f)
        and not any(excl in str(f) for excl in EXCLUDE)
    ]

    print(f"📋 Found {len(all_files)} files to refactor")
    if args.dry_run:
        print("🔍 DRY-RUN MODE (no files will be modified)\n")
    else:
        print("⚠️  LIVE MODE (files will be modified)\n")

    total_changes = 0
    files_changed = 0

    for file_path in all_files:
        stats = refactor_file(file_path, args.dry_run)
        if stats['changes'] > 0:
            total_changes += stats['changes']
            files_changed += 1

    print("\n" + "━" * 50)
    print(f"📊 Summary:")
    print(f"   Files changed: {files_changed}/{len(all_files)}")
    print(f"   Total changes: {total_changes}")

    if not args.dry_run and total_changes > 0:
        print("\n✅ Refactoring complete! Run design system check again:")
        print("   python3 scripts/check-design-system.py")

if __name__ == '__main__':
    main()
