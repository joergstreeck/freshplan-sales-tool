#!/usr/bin/env python3

"""
Test Data Coverage Check
=========================

Prüft ob alle @Entity Klassen in TestDataService abgedeckt sind.

Exit Codes:
  0 = Coverage OK (100%)
  1 = Coverage-Lücken gefunden (Entities ohne Test-Daten)

Usage:
  python3 scripts/check-test-data-coverage.py

Integration:
  - CI Pipeline (GitHub Actions, GitLab CI, etc.)
  - Pre-Commit Hook (optional, zusätzlich zur Dokumentations-Prüfung)
  - Manual checks (z.B. vor Release)

Author: FreshPlan Development Team
Since: Sprint 2.1.7.2
"""

import re
import sys
from pathlib import Path
from typing import List, Set

# ============================================================================
# CONFIGURATION
# ============================================================================

# Pfade relativ zum Project Root
ENTITY_SEARCH_PATHS = [
    "backend/src/main/java/de/freshplan/domain",
    "backend/src/main/java/de/freshplan/modules",
]

TEST_DATA_SERVICE_PATH = "backend/src/main/java/de/freshplan/domain/testdata/service/TestDataService.java"

# Entities die NICHT getestet werden müssen (z.B. rein technische Entities)
EXCLUDED_ENTITIES = {
    "AuditEntry",         # Audit-Log (System-generiert)
    "Setting",            # System-Einstellungen
    "ImportJob",          # Job-Tracking
    "OutboxEmail",        # Outbox Pattern
    "CampaignTemplate",   # Templates (konfigurierbar)
    "Territory",          # Territorien (konfigurierbar)
    "Permission",         # Permissions (RBAC)
    "Role",               # Roles (RBAC)
    "RolePermission",     # RolePermissions (RBAC)
    "UserPermission",     # UserPermissions (RBAC)
    "BudgetLimit",        # Budget-Management (optional)
    "CostTransaction",    # Cost-Tracking (optional)
    "HelpContent",        # Help-System Content
    "XentralSettings",    # Xentral-Config (Singleton)
}

# Entities die im SEED-Data erwähnt werden SOLLTEN (Kritisch für Testing)
CRITICAL_ENTITIES = {
    "User",
    "Lead",
    "LeadContact",
    "Customer",
    "Contact",           # CustomerContact
    "CustomerLocation",
    "Activity",
    "Opportunity",
}

# ============================================================================
# FUNCTIONS
# ============================================================================

def find_entities() -> Set[str]:
    """
    Findet alle @Entity Klassen in der Codebase.

    Returns:
        Set of entity class names
    """
    entities = set()
    entity_pattern = re.compile(r'@Entity.*?class\s+(\w+)', re.MULTILINE | re.DOTALL)

    project_root = Path(__file__).parent.parent

    for search_path in ENTITY_SEARCH_PATHS:
        search_dir = project_root / search_path

        if not search_dir.exists():
            print(f"⚠️  Warning: Search path not found: {search_dir}", file=sys.stderr)
            continue

        for java_file in search_dir.rglob('*.java'):
            content = java_file.read_text(encoding='utf-8', errors='ignore')

            if '@Entity' in content:
                match = entity_pattern.search(content)
                if match:
                    entity_name = match.group(1)
                    entities.add(entity_name)

    return entities


def check_test_data_service(entities: Set[str]) -> Set[str]:
    """
    Prüft ob Entities in TestDataService erwähnt werden.

    Args:
        entities: Set of entity class names to check

    Returns:
        Set of entities that are covered in TestDataService
    """
    covered = set()

    project_root = Path(__file__).parent.parent
    test_data_service_file = project_root / TEST_DATA_SERVICE_PATH

    if not test_data_service_file.exists():
        print(f"❌ Error: TestDataService not found: {test_data_service_file}", file=sys.stderr)
        sys.exit(1)

    test_data_service_content = test_data_service_file.read_text(encoding='utf-8')

    for entity in entities:
        # Check if entity is mentioned in TestDataService
        # (either as class name, variable name, or in comments)
        if entity in test_data_service_content:
            covered.add(entity)

    return covered


def print_report(all_entities: Set[str], covered_entities: Set[str], missing_entities: Set[str]):
    """
    Druckt Coverage-Report.

    Args:
        all_entities: All entities found in codebase
        covered_entities: Entities covered in TestDataService
        missing_entities: Entities NOT covered in TestDataService
    """
    # Filter excluded entities
    relevant_entities = all_entities - EXCLUDED_ENTITIES
    relevant_missing = missing_entities - EXCLUDED_ENTITIES

    coverage_percent = (len(covered_entities) / len(relevant_entities) * 100) if relevant_entities else 100

    print("\n" + "="*80)
    print("🧪 TEST-DATEN COVERAGE REPORT")
    print("="*80)
    print()
    print(f"📊 Coverage: {len(covered_entities)}/{len(relevant_entities)} ({coverage_percent:.1f}%)")
    print()

    # Critical Entities Status
    critical_covered = CRITICAL_ENTITIES & covered_entities
    critical_missing = CRITICAL_ENTITIES & relevant_missing

    if critical_missing:
        print("🚨 KRITISCHE ENTITIES FEHLEN:")
        for entity in sorted(critical_missing):
            print(f"   ❌ {entity}")
        print()
    else:
        print(f"✅ Alle kritischen Entities abgedeckt ({len(critical_covered)}/{len(CRITICAL_ENTITIES)})")
        print()

    # All Missing Entities
    if relevant_missing:
        print(f"❌ FEHLENDE ENTITIES ({len(relevant_missing)}):")
        for entity in sorted(relevant_missing):
            critical_marker = "🚨 KRITISCH" if entity in CRITICAL_ENTITIES else ""
            print(f"   - {entity} {critical_marker}")
        print()
        print("📋 ERFORDERLICHE AKTION:")
        print("   1. TestDataService erweitern (neue seed-Methode)")
        print("   2. TEST_DATA_SCENARIOS.md aktualisieren (Szenario hinzufügen)")
        print("   3. TEST_DATA_GUIDE.md aktualisieren (Quick Reference)")
        print()
    else:
        print("✅ Alle relevanten Entities haben Test-Daten")
        print()

    # Excluded Entities Info
    if EXCLUDED_ENTITIES:
        print(f"ℹ️  Ausgeschlossene Entities ({len(EXCLUDED_ENTITIES)}):")
        for entity in sorted(EXCLUDED_ENTITIES):
            print(f"   - {entity}")
        print()


def main():
    """Main entry point."""
    print("\n🔍 Suche @Entity Klassen...")
    all_entities = find_entities()

    if not all_entities:
        print("⚠️  Keine Entities gefunden. Prüfe ENTITY_SEARCH_PATHS.", file=sys.stderr)
        sys.exit(1)

    print(f"   Gefunden: {len(all_entities)} Entities")

    print("\n🔍 Prüfe TestDataService...")
    covered_entities = check_test_data_service(all_entities)

    missing_entities = all_entities - covered_entities
    relevant_missing = missing_entities - EXCLUDED_ENTITIES

    # Print Report
    print_report(all_entities, covered_entities, missing_entities)

    # Exit Code
    if relevant_missing:
        print("="*80)
        print("❌ TEST-DATEN COVERAGE UNVOLLSTÄNDIG")
        print("="*80)
        print()
        sys.exit(1)
    else:
        print("="*80)
        print("✅ TEST-DATEN COVERAGE: 100%")
        print("="*80)
        print()
        sys.exit(0)


if __name__ == "__main__":
    main()
