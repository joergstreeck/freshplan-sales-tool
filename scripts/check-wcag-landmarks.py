#!/usr/bin/env python3
"""
WCAG Landmarks Check - Pre-Commit Hook PRÜFUNG 12

Prüft WCAG 2.1 Level AA Compliance für:
1. landmark-one-main: Jede Page muss <main> oder component="main" haben
2. page-has-heading-one: Jede Page muss <h1> oder component="h1" haben

Ausnahmen:
- Komponenten die MainLayoutV2 verwenden (hat bereits <main>)
- Komponenten die EntityListHeader verwenden (hat bereits component="h1")
- Reine Layout-Wrapper ohne eigene Inhalte

Sprint 2.1.7.7 - WCAG Accessibility
"""

import os
import re
import sys
from pathlib import Path
from typing import List, Tuple, Set

# Colors for terminal output
RED = '\033[0;31m'
YELLOW = '\033[1;33m'
GREEN = '\033[0;32m'
BLUE = '\033[0;34m'
NC = '\033[0m'  # No Color

# Paths to check
PAGES_DIR = Path("frontend/src/pages")
FEATURES_DIR = Path("frontend/src/features")
APP_FILE = Path("frontend/src/App.tsx")  # Root App component

# Components that provide <main> landmark
MAIN_PROVIDERS = [
    "MainLayoutV2",
    "MainLayout",
]

# Components that provide <h1> heading
H1_PROVIDERS = [
    "EntityListHeader",
]

# Files/patterns to skip (not full pages)
SKIP_PATTERNS = [
    r".*\.test\.tsx$",
    r".*\.spec\.tsx$",
    r".*/components/.*",  # Skip component subdirectories
    r".*/hooks/.*",
    r".*/types/.*",
    r".*/utils/.*",
    r".*/api/.*",
    r".*/store/.*",
    r".*/config/.*",
]

# Known exceptions (components that are wrapped by parent with main/h1)
KNOWN_EXCEPTIONS = [
    "PlaceholderPage.tsx",  # Uses MainLayoutV2 internally
    "index.tsx",  # Barrel exports
    # Cockpit pages that delegate to child components with h1
    "CockpitPage.tsx",  # Uses SalesCockpitV2 which has h1
    "CockpitPageV2.tsx",  # Uses CockpitViewV2 -> SalesCockpitMUI which has h1
    "CockpitViewV2.tsx",  # Uses SalesCockpitMUI which has h1
    # Pipeline page delegates to Kanban which has h1
    "OpportunityPipelinePage.tsx",  # Uses KanbanBoardDndKit which has h1
    # Test pages that delegate to test components
    "TestAuditTimeline.tsx",  # Uses SmartContactCardTest which has h1
    # Placeholder re-exports (use PlaceholderPage with h1 internally)
    "integrationen.tsx",
    "kommunikation.tsx",
    # Modal components (not full pages)
    "LeadWizard.tsx",  # Dialog, not a page
    # Dashboard components (used within pages, not standalone)
    "AuditDashboard.tsx",  # Used in AuditAdminPage
]


def should_skip_file(file_path: str) -> bool:
    """Check if file should be skipped."""
    for pattern in SKIP_PATTERNS:
        if re.match(pattern, file_path):
            return True

    filename = os.path.basename(file_path)
    if filename in KNOWN_EXCEPTIONS:
        return True

    return False


def check_file_for_main(content: str, filepath: str) -> Tuple[bool, str]:
    """
    Check if file has <main> landmark or uses a component that provides it.
    Returns (has_main, reason)
    """
    # Check for MainLayoutV2 or other main providers
    for provider in MAIN_PROVIDERS:
        if f"<{provider}" in content or f"import {{ {provider}" in content or f"import {provider}" in content:
            return True, f"Uses {provider}"

    # Check for direct <main> element
    if "<main" in content:
        return True, "Has <main> element"

    # Check for component="main" (MUI Box)
    if 'component="main"' in content or "component='main'" in content:
        return True, 'Has component="main"'

    return False, "Missing <main> landmark"


def check_file_for_h1(content: str, filepath: str) -> Tuple[bool, str]:
    """
    Check if file has <h1> heading or uses a component that provides it.
    Returns (has_h1, reason)
    """
    # Check for H1 provider components
    for provider in H1_PROVIDERS:
        if f"<{provider}" in content:
            return True, f"Uses {provider}"

    # Check for direct <h1> element
    if "<h1" in content:
        return True, "Has <h1> element"

    # Check for component="h1" (MUI Typography)
    if 'component="h1"' in content or "component='h1'" in content:
        return True, 'Has component="h1"'

    # Check for variant="h1" with component might be implicit
    # But safer to require explicit component="h1"

    return False, "Missing <h1> heading"


def find_page_files() -> List[Path]:
    """Find all page/view TSX files that need checking."""
    files = []

    # Check App.tsx (root component)
    if APP_FILE.exists():
        files.append(APP_FILE)

    # Check pages directory
    if PAGES_DIR.exists():
        for file in PAGES_DIR.rglob("*.tsx"):
            rel_path = str(file.relative_to(Path(".")))
            if not should_skip_file(rel_path):
                files.append(file)

    # Check feature pages (only top-level page components)
    if FEATURES_DIR.exists():
        for file in FEATURES_DIR.rglob("*Page.tsx"):
            rel_path = str(file.relative_to(Path(".")))
            if not should_skip_file(rel_path):
                files.append(file)

        # Also check for *Dashboard.tsx, *View.tsx patterns
        for pattern in ["*Dashboard.tsx", "*View.tsx", "*Wizard.tsx"]:
            for file in FEATURES_DIR.rglob(pattern):
                rel_path = str(file.relative_to(Path(".")))
                if not should_skip_file(rel_path):
                    files.append(file)

    return list(set(files))  # Remove duplicates


def main() -> int:
    """Main check function."""
    print(f"{BLUE}Checking WCAG Landmarks (main + h1)...{NC}")
    print()

    violations: List[Tuple[str, str, str]] = []
    checked_count = 0

    page_files = find_page_files()

    for file_path in page_files:
        try:
            content = file_path.read_text(encoding='utf-8')
        except Exception as e:
            print(f"{YELLOW}Warning: Could not read {file_path}: {e}{NC}")
            continue

        checked_count += 1
        rel_path = str(file_path.relative_to(Path(".")))

        # Check for <main>
        has_main, main_reason = check_file_for_main(content, rel_path)
        if not has_main:
            violations.append((rel_path, "landmark-one-main", main_reason))

        # Check for <h1>
        has_h1, h1_reason = check_file_for_h1(content, rel_path)
        if not has_h1:
            violations.append((rel_path, "page-has-heading-one", h1_reason))

    # Report results
    if violations:
        print(f"{RED}WCAG Landmark Violations Found:{NC}")
        print()

        # Group by file
        files_with_issues: Set[str] = set()
        for filepath, rule, reason in violations:
            files_with_issues.add(filepath)

        for filepath in sorted(files_with_issues):
            print(f"{RED}  {filepath}:{NC}")
            for vpath, rule, reason in violations:
                if vpath == filepath:
                    print(f"{YELLOW}    - [{rule}] {reason}{NC}")
            print()

        print(f"{YELLOW}Lösungen:{NC}")
        print()
        print(f"  {YELLOW}Für landmark-one-main:{NC}")
        print(f"    • Verwende <MainLayoutV2> als Wrapper")
        print(f"    • Oder: <main className=\"...\">...</main>")
        print(f"    • Oder: <Box component=\"main\">...</Box>")
        print()
        print(f"  {YELLOW}Für page-has-heading-one:{NC}")
        print(f"    • Verwende <EntityListHeader title=\"...\" />")
        print(f"    • Oder: <Typography variant=\"h4\" component=\"h1\">...</Typography>")
        print(f"    • Oder: <h1>...</h1>")
        print()

        return 1

    print(f"{GREEN}✓ {checked_count} pages checked - All have proper WCAG landmarks{NC}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
