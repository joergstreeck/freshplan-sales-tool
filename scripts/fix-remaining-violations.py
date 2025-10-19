#!/usr/bin/env python3
"""
Fix Remaining Design System Violations
========================================
Intelligentes Refactoring für komplexe Cases
"""

import re
from pathlib import Path

PROJECT_ROOT = Path(__file__).parent.parent
FRONTEND_SRC = PROJECT_ROOT / "frontend/src"

# Pattern-based fixes for all remaining violations
COMPLEX_FIXES = {
    # Type definition files - use string literals
    'types.ts': [
        (r"REGISTERED: '#2196F3'", "REGISTERED: 'info.main'"),
        (r"ACTIVE: '#4CAF50'", "ACTIVE: 'success.main'"),
        (r"REMINDER: '#FFC107'", "REMINDER: 'warning.light'"),
        (r"GRACE_PERIOD: '#FF9800'", "GRACE_PERIOD: 'warning.main'"),
        (r"QUALIFIED: '#00BCD4'", "QUALIFIED: 'info.light'"),
        (r"CONVERTED: '#2E7D32'", "CONVERTED: 'success.dark'"),
        (r"LOST: '#F44336'", "LOST: 'error.main'"),
        (r"ARCHIVED: '#9E9E9E'", "ARCHIVED: 'grey.500'"),
    ],

    # Config files - const objects need string literals
    'stage-config.ts': [
        (r"color: '#1976D2'", "color: 'info.main'"),
        (r"bgColor: '#E3F2FD'", "bgColor: 'info.light'"),
        (r"color: '#388E3C'", "color: 'success.dark'"),
        (r"bgColor: '#E8F5E9'", "bgColor: 'success.light'"),
        (r"color: '#689F38'", "color: 'success.main'"),
        (r"color: '#1565C0'", "color: 'info.dark'"),
        (r"color: '#00897B'", "color: 'success.dark'"),
        (r"color: '#FF6F00'", "color: 'warning.dark'"),
        (r"color: '#C62828'", "color: 'error.dark'"),
    ],

    # Helper functions
    'opportunityHelpers.ts': [
        (r"NEUER_STANDORT: '#FF9800'", "NEUER_STANDORT: 'warning.main'"),
        (r"VERLAENGERUNG: '#2196F3'", "VERLAENGERUNG: 'info.main'"),
        (r"NEW_LEAD: '#E0E0E0'", "NEW_LEAD: 'grey.300'"),
        (r"QUALIFICATION: '#FF9800'", "QUALIFICATION: 'warning.main'"),
        (r"NEEDS_ANALYSIS: '#2196F3'", "NEEDS_ANALYSIS: 'info.main'"),
        (r"PROPOSAL: '#1976D2'", "PROPOSAL: 'info.dark'"),
        (r"NEGOTIATION: '#689F38'", "NEGOTIATION: 'success.main'"),
        (r"CLOSED_WON: '#2E7D32'", "CLOSED_WON: 'success.dark'"),
        (r"CLOSED_LOST: '#B71C1C'", "CLOSED_LOST: 'error.dark'"),
    ],

    # Mobile actions
    'mobileActions.types.ts': [
        (r"call: '#4CAF50'", "call: 'success.main'"),
        (r"email: '#2196F3'", "email: 'info.main'"),
        (r"whatsapp: '#25D366'", "whatsapp: 'success.dark'"),
        (r"message: '#9C27B0'", "message: 'secondary.light'"),
    ],
}

def fix_file(file_path: Path) -> int:
    """Fix violations in a single file"""
    changes = 0

    try:
        content = file_path.read_text()
        original = content

        # Check if file matches any pattern
        for pattern_file, fixes in COMPLEX_FIXES.items():
            if pattern_file in str(file_path):
                for old, new in fixes:
                    if re.search(old, content):
                        content = re.sub(old, new, content)
                        changes += 1

        if changes > 0:
            file_path.write_text(content)
            print(f"✅ {file_path.relative_to(PROJECT_ROOT)}: {changes} fixes")

    except Exception as e:
        print(f"❌ Error in {file_path}: {e}")

    return changes

def main():
    print("🔧 Fixing Remaining Design System Violations")
    print("━" * 50)

    total = 0

    # Find and fix all relevant files
    for pattern_file in COMPLEX_FIXES.keys():
        matching_files = list(FRONTEND_SRC.glob(f"**/{pattern_file}"))
        for file_path in matching_files:
            total += fix_file(file_path)

    print("\n" + "━" * 50)
    print(f"📊 Total fixes: {total}")

    if total > 0:
        print("\n✅ Run design system check again:")
        print("   python3 scripts/check-design-system.py")

if __name__ == '__main__':
    main()
