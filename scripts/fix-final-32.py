#!/usr/bin/env python3
"""
Fix Final 32 Violations - Precision Fixes
==========================================
"""

import re
from pathlib import Path

PROJECT_ROOT = Path(__file__).parent.parent
FRONTEND_SRC = PROJECT_ROOT / "frontend/src"

FIXES = [
    # CustomerTable.tsx - fallback color
    ("features/customers/components/CustomerTable.tsx", [
        (r"bgcolor: stageColors\[stageValue\] \|\| '#9E9E9E'",
         "bgcolor: stageColors[stageValue] || 'grey.500'"),
    ]),

    # SmartContactCard.tsx - array with mixed
    ("features/customers/components/contacts/SmartContactCard.tsx", [
        (r"const colors = \['#94C456', '#004F7B', 'warning\.main', '#2196F3', 'secondary\.l[^']*'\]",
         "const colors = ['primary.main', 'secondary.main', 'warning.main', 'info.main', 'secondary.light']"),
    ]),

    # DetailedLocationsStep.tsx - broken sed
    ("features/customers/components/steps/DetailedLocationsStep.tsx", [
        (r"''\&:hover': \{ bgcolor: '#7BA345' \}:hover': \{ bgcolor: 'primary\.dark' \}",
         "'&:hover': { bgcolor: 'primary.dark' }"),
    ]),

    # LeadActivityTimelineGrouped.tsx
    ("features/leads/components/LeadActivityTimelineGrouped.tsx", [
        (r"return outcomeMap\[outcome\] \|\| \{ label: outcome, color: '#757575', icon: '❓' \}",
         "return outcomeMap[outcome] || { label: outcome, color: 'grey.600', icon: '❓' }"),
    ]),

    # PreClaimBadge.tsx
    ("features/leads/components/PreClaimBadge.tsx", [
        (r"backgroundColor: variant === 'default' \? '#FF9800'",
         "backgroundColor: variant === 'default' ? 'warning.main'"),
    ]),

    # PreClaimDashboardWidget.tsx
    ("features/leads/components/PreClaimDashboardWidget.tsx", [
        (r"borderColor: '#2196F3'",
         "borderColor: 'info.main'"),
    ]),

    # LeadProtectionManager.tsx
    ("features/leads/components/intelligence/LeadProtectionManager.tsx", [
        (r'color="#B71C1C"',
         'color="error.dark"'),
    ]),

    # leads/types.ts
    ("features/leads/types.ts", [
        (r"EMERGENCY: '#F44336'",
         "EMERGENCY: 'error.main'"),
    ]),

    # KanbanBoard.tsx
    ("features/opportunity/components/KanbanBoard.tsx", [
        (r"bgcolor: config\?\.bgColor \|\| '#f5f5f5'",
         "bgcolor: config?.bgColor || 'grey.100'"),
    ]),

    # stage-config.ts - remaining colors
    ("features/opportunity/config/stage-config.ts", [
        (r"color: '#7B1FA2'",
         "color: 'secondary.dark'"),
        (r"bgColor: '#F3E5F5'",
         "bgColor: 'secondary.lighter'"),
        (r"color: '#2E7D32'",
         "color: 'success.dark'"),
        (r"bgColor: '#C8E6C9'",
         "bgColor: 'success.light'"),
        (r"bgColor: '#FFCDD2'",
         "bgColor: 'error.lighter'"),
    ]),

    # opportunityHelpers.ts
    ("features/opportunity/utils/opportunityHelpers.ts", [
        (r"PROPOSAL: '#FFC107'",
         "PROPOSAL: 'warning.light'"),
        (r"CLOSED_LOST: '#F44336'",
         "CLOSED_LOST: 'error.main'"),
        (r"return colors\[stage\] \|\| '#E0E0E0'",
         "return colors[stage] || 'grey.300'"),
    ]),

    # UserFormMUI.tsx
    ("features/users/UserFormMUI.tsx", [
        (r"backgroundColor: '#cccccc'",
         "backgroundColor: 'grey.400'"),
    ]),
]

def fix_file(relative_path: str, patterns: list) -> int:
    file_path = FRONTEND_SRC / relative_path
    if not file_path.exists():
        print(f"⚠️  File not found: {relative_path}")
        return 0

    content = file_path.read_text()
    changes = 0

    for old_pattern, new_text in patterns:
        matches = len(re.findall(old_pattern, content))
        if matches > 0:
            content = re.sub(old_pattern, new_text, content)
            changes += matches

    if changes > 0:
        file_path.write_text(content)
        print(f"✅ {relative_path}: {changes} fixes")

    return changes

def main():
    print("🎯 Final 32 Violations - Precision Fixes")
    print("━" * 50)

    total = 0
    for file_path, patterns in FIXES:
        total += fix_file(file_path, patterns)

    print("\n" + "━" * 50)
    print(f"📊 Total: {total} fixes")

    if total > 0:
        print("\n✅ Running final check...")
        import subprocess
        result = subprocess.run(
            ["python3", str(PROJECT_ROOT / "scripts/check-design-system.py")],
            capture_output=True,
            text=True
        )
        if "FAILURE" in result.stdout:
            violations_match = re.search(r'(\d+) violations found', result.stdout)
            if violations_match:
                remaining = int(violations_match.group(1))
                print(f"\n📊 Remaining: {remaining} violations")
        else:
            print("\n🎉 ZERO VIOLATIONS ACHIEVED!")

if __name__ == '__main__':
    main()
