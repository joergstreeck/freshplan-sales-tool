#!/usr/bin/env python3
"""
Fixes misplaced @AfterEach cleanup methods that were inserted outside the class.
Sprint 2.1.7.7: Test Cleanup - Fix Script Bugs
"""

import sys
import re
from pathlib import Path

# Files that need fixing (same 7 files from add-test-cleanup.py)
test_files_to_fix = [
    "src/test/java/de/freshplan/domain/opportunity/service/OpportunityServiceConvertToCustomerTest.java",
    "src/test/java/de/freshplan/domain/opportunity/service/OpportunityServiceCreateForCustomerTest.java",
    "src/test/java/de/freshplan/domain/opportunity/service/OpportunityServiceCreateFromLeadTest.java",
    "src/test/java/de/freshplan/domain/opportunity/service/OpportunityServiceFindByCustomerIdTest.java",
    "src/test/java/de/freshplan/domain/opportunity/service/OpportunityServiceStageTransitionTest.java",
    "src/test/java/de/freshplan/modules/leads/service/LeadConvertServiceTest.java",
    "src/test/java/de/freshplan/modules/xentral/repository/XentralSettingsRepositoryTest.java",
]


def fix_cleanup_placement(file_path: Path) -> bool:
    """Moves @AfterEach cleanup from outside class to inside class."""

    if not file_path.exists():
        print(f"⚠️  File not found: {file_path}")
        return False

    content = file_path.read_text()

    # Pattern to find the misplaced cleanup method (before class declaration)
    misplaced_pattern = r'(@AfterEach\s+@Transactional\s+void cleanup\(\) \{[^}]+\})\s+'

    # Find if cleanup is misplaced
    match = re.search(misplaced_pattern, content, re.DOTALL)
    if not match:
        print(f"✅ {file_path.name} - cleanup already correctly placed or not found")
        return True

    cleanup_block = match.group(1)

    # Remove the misplaced cleanup
    content = re.sub(misplaced_pattern, '', content, flags=re.DOTALL)

    # Find where to insert it - after the last @Inject field
    inject_pattern = r'(@Inject[^\n]+;)'
    inject_matches = list(re.finditer(inject_pattern, content))

    if not inject_matches:
        print(f"⚠️  {file_path.name} - no @Inject fields found")
        return False

    # Find position after last @Inject
    last_inject = inject_matches[-1]
    insert_pos = last_inject.end()

    # Insert cleanup with proper indentation
    cleanup_with_spacing = f"\n\n  {cleanup_block}\n"
    content = content[:insert_pos] + cleanup_with_spacing + content[insert_pos:]

    file_path.write_text(content)
    print(f"✅ {file_path.name} - cleanup moved inside class")
    return True


def main():
    backend_dir = Path(__file__).parent.parent

    print("\n" + "=" * 80)
    print("🔧 Fixing misplaced @AfterEach cleanup methods")
    print("=" * 80)

    success_count = 0
    total = len(test_files_to_fix)

    for rel_path in test_files_to_fix:
        file_path = backend_dir / rel_path
        if fix_cleanup_placement(file_path):
            success_count += 1

    print("\n" + "=" * 80)
    print(f"✅ Successfully fixed {success_count}/{total} files")
    print("=" * 80)

    return 0 if success_count == total else 1


if __name__ == "__main__":
    sys.exit(main())
