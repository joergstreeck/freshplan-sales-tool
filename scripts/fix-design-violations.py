#!/usr/bin/env python3
"""
Automated Design System Violation Fixer
=========================================
Fixes hardcoded brand colors (#94C456, #004F7B, #000) with theme tokens
"""

import re
from pathlib import Path

PROJECT_ROOT = Path(__file__).parent.parent
FRONTEND_SRC = PROJECT_ROOT / "frontend/src"

# Files to fix (from guard output)
TARGET_FILES = [
    # Priority 1 - Core UI
    "components/common/Logo.tsx",
    "components/export/UniversalExportButton.tsx",
    "components/layout/AdminLayout.tsx",
    "components/layout/HeaderV2.tsx",
    "features/audit/admin/UserActivityPanel.tsx",
    "features/cockpit/components/FocusListColumnMUI.tsx",
    "features/customer/components/FilterBar.tsx",
    "features/leads/LeadScoreIndicator.tsx",
    "features/leads/components/LeadContactsCard.tsx",
    "features/opportunity/components/KanbanBoard.tsx",
    "features/opportunity/utils/opportunityHelpers.ts",
    "features/users/UserFormMUI.tsx",
    "features/users/UserTableMUI.tsx",
    "lib/settings/api.ts",
    "lib/settings/hooks.ts",
]

# Replacement patterns
REPLACEMENTS = [
    # In sx={{}} props and style objects
    (r"backgroundColor:\s*'#94C456'", "backgroundColor: 'primary.main'"),
    (r'backgroundColor:\s*"#94C456"', 'backgroundColor: "primary.main"'),

    (r"borderColor:\s*'#94C456'", "borderColor: 'primary.main'"),
    (r'borderColor:\s*"#94C456"', 'borderColor: "primary.main"'),

    (r"bgcolor:\s*'#94C456'", "bgcolor: 'primary.main'"),
    (r'bgcolor:\s*"#94C456"', 'bgcolor: "primary.main"'),

    (r"color:\s*'#94C456'", "color: 'primary.main'"),
    (r'color:\s*"#94C456"', 'color: "primary.main"'),

    # Secondary color (#004F7B)
    (r"color:\s*'#004F7B'", "color: 'secondary.main'"),
    (r'color:\s*"#004F7B"', 'color: "secondary.main"'),

    (r"borderColor:\s*'#004F7B'", "borderColor: 'secondary.main'"),
    (r'borderColor:\s*"#004F7B"', 'borderColor: "secondary.main"'),

    # Black (#000)
    (r"color:\s*'#000'", "color: 'text.primary'"),
    (r'color:\s*"#000"', 'color: "text.primary"'),

    # Object literals (for config objects)
    (r"'#94C456'(\s*,?\s*//.*FreshFoodz.*)", r"theme.palette.primary.main\1"),
    (r"'#004F7B'(\s*,?\s*//.*FreshFoodz.*)", r"theme.palette.secondary.main\1"),

    # Inline conditionals
    (r"(\?\s*)'#94C456'", r"\1'primary.main'"),
    (r"(\?\s*)'#004F7B'", r"\1'secondary.main'"),
    (r"(:\s*)'#94C456'", r"\1'primary.main'"),
    (r"(:\s*)'#004F7B'", r"\1'secondary.main'"),

    # JSX props
    (r'color="#94C456"', 'color="primary.main"'),
    (r'color="#004F7B"', 'color="secondary.main"'),
]

def fix_file(file_path: Path) -> int:
    """Fix violations in file, return number of fixes"""
    try:
        content = file_path.read_text()
        original = content
        fixes = 0

        for pattern, replacement in REPLACEMENTS:
            new_content, count = re.subn(pattern, replacement, content)
            if count > 0:
                content = new_content
                fixes += count

        if content != original:
            file_path.write_text(content)
            return fixes

        return 0

    except Exception as e:
        print(f"ERROR fixing {file_path}: {e}")
        return 0

def main():
    print("🔧 Automated Design System Violation Fixer")
    print("=" * 60)
    print()

    total_fixes = 0
    fixed_files = 0

    for rel_path in TARGET_FILES:
        file_path = FRONTEND_SRC / rel_path

        if not file_path.exists():
            print(f"⚠️  NOT FOUND: {rel_path}")
            continue

        fixes = fix_file(file_path)
        if fixes > 0:
            print(f"✅ {rel_path}: {fixes} fixes")
            total_fixes += fixes
            fixed_files += 1
        else:
            print(f"   {rel_path}: no auto-fixes")

    print()
    print("=" * 60)
    print(f"✅ DONE: {total_fixes} fixes across {fixed_files} files")
    print()
    print("⚠️  MANUAL CHECK REQUIRED:")
    print("   • useTheme() imported where needed")
    print("   • Object literals may need theme parameter")
    print("   • Run: npm run type-check")
    print()

if __name__ == '__main__':
    main()
