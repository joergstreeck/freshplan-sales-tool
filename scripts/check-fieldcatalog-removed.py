#!/usr/bin/env python3
"""
Pre-Commit Hook: fieldCatalog.json Removal Guard

Sprint 2.1.7.x: fieldCatalog.json Migration - Server-Driven UI

Prüft, dass fieldCatalog.json nicht existiert
Prüft, dass deprecated Hooks nicht importiert werden

ZWECK: Verhindert Regression nach fieldCatalog.json Migration
- fieldCatalog.json wurde in Sprint 2.1.7.x gelöscht
- Ersetzt durch Server-Driven UI (useCustomerSchema, useLocationServiceSchema)
- Backend ist Single Source of Truth für Field Definitions

EXIT CODES:
- 0: Alle Checks bestanden
- 1: fieldCatalog.json gefunden ODER deprecated Imports gefunden
"""

import os
import sys
import re


def check_fieldcatalog_removed():
    """Prüft dass fieldCatalog.json und deprecated Hooks entfernt wurden."""

    errors = []

    # 1. Prüfe dass fieldCatalog.json nicht existiert
    fieldcatalog_path = "frontend/src/features/customers/data/fieldCatalog.json"
    if os.path.exists(fieldcatalog_path):
        errors.append(f"❌ ERROR: {fieldcatalog_path} exists!")
        errors.append("   This file was removed in Sprint 2.1.7.x (Server-Driven UI Migration)")
        errors.append("   Backend is now Single Source of Truth for field definitions")
        errors.append("   See: /docs/planung/claude-work/MIGRATION_PLAN_fieldCatalog_removal.md")

    # 2. Prüfe dass deprecated Hook-Dateien nicht existieren
    deprecated_files = [
        "frontend/src/features/customers/hooks/useFieldDefinitions.ts",
        "frontend/src/features/customers/hooks/useFieldDefinitionsApi.ts",
    ]

    for filepath in deprecated_files:
        if os.path.exists(filepath):
            errors.append(f"❌ ERROR: {filepath} exists!")
            errors.append("   This hook was removed in Sprint 2.1.7.x")
            errors.append("   Use useCustomerSchema or useLocationServiceSchema instead")

    # 3. Prüfe dass deprecated Hooks nicht importiert werden
    deprecated_imports = [
        "from '../../hooks/useFieldDefinitions'",
        "from '../hooks/useFieldDefinitions'",
        'from "../../hooks/useFieldDefinitions"',
        "from '../../hooks/useFieldDefinitionsApi'",
        "from '../hooks/useFieldDefinitionsApi'",
        'from "../../hooks/useFieldDefinitionsApi"',
    ]

    # Scan alle .ts/.tsx Dateien (nur frontend/src)
    files_with_deprecated_imports = []

    if os.path.exists("frontend/src"):
        for root, dirs, files in os.walk("frontend/src"):
            # Skip node_modules, dist, coverage
            dirs[:] = [d for d in dirs if d not in ['node_modules', 'dist', 'coverage', '.vite']]

            for file in files:
                if file.endswith(('.ts', '.tsx')):
                    filepath = os.path.join(root, file)
                    try:
                        with open(filepath, 'r', encoding='utf-8') as f:
                            content = f.read()
                            for deprecated in deprecated_imports:
                                if deprecated in content:
                                    files_with_deprecated_imports.append((filepath, deprecated))
                    except Exception as e:
                        # Skip files that can't be read (permissions, encoding errors, etc.)
                        pass

    if files_with_deprecated_imports:
        errors.append("❌ ERROR: Found deprecated useFieldDefinitions/useFieldDefinitionsApi imports:")
        for filepath, import_line in files_with_deprecated_imports:
            errors.append(f"   {filepath}")
            errors.append(f"   Import: {import_line}")
        errors.append("")
        errors.append("   MIGRATION GUIDE:")
        errors.append("   - useFieldDefinitions → useCustomerSchema (customer-level fields)")
        errors.append("   - useFieldDefinitions → useLocationServiceSchema (location service fields)")
        errors.append("   See: /docs/planung/claude-work/MIGRATION_PLAN_fieldCatalog_removal.md")

    # 4. Output Ergebnisse
    if errors:
        print("\n".join(errors))
        print("")
        print("🚫 Pre-Commit BLOCKED: fieldCatalog.json Migration Regression detected!")
        print("")
        return False
    else:
        print("✅ fieldCatalog.json Migration Guard: All checks passed")
        print("   - fieldCatalog.json does not exist")
        print("   - No deprecated hook imports found")
        print("   - Server-Driven UI architecture intact")
        return True


if __name__ == "__main__":
    success = check_fieldcatalog_removed()
    sys.exit(0 if success else 1)
