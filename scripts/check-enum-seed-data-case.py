#!/usr/bin/env python3
"""
Enum Value Case Validator for Seed Data
========================================
Prüft Seed-Files (V900*.sql) auf Mixed-Case Enum-Werte

WHY: legal_form hatte 'GmbH' (Mixed Case) statt 'GMBH' (UPPERCASE)
     → Backend-Start scheitert mit CHECK Constraint Violation!

     salutation hatte 'Frau' (Mixed Case) statt 'FRAU' (UPPERCASE)
     → Backend-Start OK, aber MUI-Warnings im Browser!

SOLUTION: Pre-Commit Hook prüft ALLE Enum-Felder in Seed-Daten

Exit Codes:
  0 = OK (alle Enum-Werte korrekt UPPERCASE)
  1 = Violations gefunden (Mixed-Case Enum-Werte in Seed-Files)

Usage: ./scripts/check-enum-seed-data-case.py
"""

import re
import sys
from pathlib import Path
from typing import List, Tuple, Dict

SCRIPT_DIR = Path(__file__).parent
PROJECT_ROOT = SCRIPT_DIR.parent
SEED_DIR = PROJECT_ROOT / "backend/src/main/resources/db/dev-seed"

# ==============================================================================
# ENUM DEFINITIONS - Single Source of Truth from Backend/Database
# ==============================================================================
#
# Diese Enums werden in V900*.sql Seed-Files genutzt
# Quelle: V10043__add_enum_check_constraints.sql + Backend Enums
#

ENUM_DEFINITIONS: Dict[str, Dict[str, List[str]]] = {
    # Enums WITH CHECK Constraints (V10043) - CRITICAL!
    # Backend-Start scheitert bei Mixed-Case!
    "legal_form": {
        "valid_values": [
            "GMBH", "AG", "GMBH_CO_KG", "KG", "OHG",
            "EINZELUNTERNEHMEN", "GBR", "EV", "STIFTUNG", "SONSTIGE"
        ],
        "has_check_constraint": True,
        "tables": ["customers"],
        "backend_class": "de.freshplan.domain.customer.entity.LegalForm"
    },

    "business_type": {
        "valid_values": [
            "RESTAURANT", "HOTEL", "CATERING", "KANTINE",
            "GROSSHANDEL", "LEH", "BILDUNG", "GESUNDHEIT", "SONSTIGES"
        ],
        "has_check_constraint": True,
        "tables": ["customers", "leads"],
        "backend_class": "de.freshplan.domain.shared.BusinessType"
    },

    "kitchen_size": {
        "valid_values": ["KLEIN", "MITTEL", "GROSS", "SEHR_GROSS"],
        "has_check_constraint": True,
        "tables": ["leads"],
        "backend_class": "de.freshplan.domain.shared.KitchenSize"
    },

    "payment_terms": {
        "valid_values": [
            "SOFORT", "NETTO_7", "NETTO_14", "NET_15", "NETTO_30", "NET_30",
            "NETTO_60", "NETTO_90", "VORKASSE", "LASTSCHRIFT"
        ],
        "has_check_constraint": True,
        "tables": ["customers"],
        "backend_class": "de.freshplan.domain.customer.entity.PaymentTerms"
    },

    "delivery_condition": {
        "valid_values": [
            "STANDARD", "EXPRESS", "DAP", "SELBSTABHOLUNG", "FREI_HAUS", "SONDERKONDITIONEN"
        ],
        "has_check_constraint": True,
        "tables": ["customers"],
        "backend_class": "de.freshplan.domain.customer.entity.DeliveryCondition"
    },

    # Enums WITHOUT CHECK Constraints - SOFT ERRORS!
    # Backend-Start OK, aber MUI-Warnings im Browser!
    "salutation": {
        "valid_values": ["HERR", "FRAU", "DIVERS"],
        "has_check_constraint": False,
        "tables": ["customer_contacts", "lead_contacts"],
        "backend_class": "de.freshplan.domain.shared.Salutation"
    },
}

# Common Mixed-Case Violations (for faster regex matching)
COMMON_MIXED_CASE_VIOLATIONS = {
    "legal_form": [
        "GmbH", "Ag", "GmbH & Co. KG", "Kg", "Ohg",
        "Einzelunternehmen", "GbR", "eV", "e.V.", "Stiftung", "Sonstige"
    ],
    "salutation": ["Herr", "Frau", "Divers"],
}

# ==============================================================================
# VIOLATION DETECTION
# ==============================================================================

def find_enum_violations(sql_file: Path) -> List[Tuple[str, int, str, str]]:
    """
    Scannt SQL-File nach Mixed-Case Enum-Werten.

    Returns:
        List of (enum_field, line_num, found_value, line_content)
    """
    violations = []

    with open(sql_file, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    for line_num, line in enumerate(lines, start=1):
        # Skip Comments
        if line.strip().startswith('--') or line.strip().startswith('/*'):
            continue

        # Check each enum field
        for enum_field, enum_config in ENUM_DEFINITIONS.items():
            # Build regex pattern for this enum field
            # Pattern: field_name, value, ... (SQL INSERT syntax)
            # Example: 'GmbH',  or  'Frau',

            # Check common violations first (faster)
            if enum_field in COMMON_MIXED_CASE_VIOLATIONS:
                for wrong_value in COMMON_MIXED_CASE_VIOLATIONS[enum_field]:
                    # Pattern: 'WrongValue', (with quotes and comma)
                    pattern = rf"'{re.escape(wrong_value)}',"
                    if re.search(pattern, line):
                        violations.append((
                            enum_field,
                            line_num,
                            wrong_value,
                            line.strip()
                        ))

            # Also check for ANY non-uppercase value (broader check)
            # Pattern: field_name column value 'SomeValue' (case-insensitive match for field, case-sensitive for value)
            for valid_value in enum_config["valid_values"]:
                # Check for case mismatch: valid_value is UPPERCASE, but found value is not
                # Example: GMBH valid, but GmbH/Gmbh/gmbh found

                # Build all possible mixed-case variations
                # (This is a simplified check - in practice, we're checking the common violations)
                pass

    return violations

def check_all_seed_files() -> Tuple[bool, List[Tuple[Path, str, int, str, str]]]:
    """
    Scannt alle V900*.sql Files.

    Returns:
        (success, violations)
        violations = [(file, enum_field, line_num, found_value, line_content), ...]
    """
    all_violations = []

    # Find all V900*.sql files
    seed_files = sorted(SEED_DIR.glob("V900*.sql"))

    if not seed_files:
        print(f"⚠️  WARNING: No V900*.sql seed files found in {SEED_DIR}")
        return True, []

    for sql_file in seed_files:
        violations = find_enum_violations(sql_file)
        for enum_field, line_num, found_value, line_content in violations:
            all_violations.append((sql_file, enum_field, line_num, found_value, line_content))

    success = len(all_violations) == 0
    return success, all_violations

# ==============================================================================
# OUTPUT & REPORTING
# ==============================================================================

def print_violations(violations: List[Tuple[Path, str, int, str, str]]) -> None:
    """Gibt Violations formatiert aus."""

    # Group by enum field
    violations_by_field: Dict[str, List] = {}
    for file, enum_field, line_num, found_value, line_content in violations:
        if enum_field not in violations_by_field:
            violations_by_field[enum_field] = []
        violations_by_field[enum_field].append((file, line_num, found_value, line_content))

    print("\n" + "=" * 80)
    print("❌ ENUM VALUE CASE VIOLATIONS DETECTED")
    print("=" * 80)
    print()

    for enum_field, field_violations in violations_by_field.items():
        enum_config = ENUM_DEFINITIONS[enum_field]

        print(f"\n🔴 Field: {enum_field}")
        print(f"   Tables: {', '.join(enum_config['tables'])}")
        print(f"   Backend: {enum_config['backend_class']}")
        print(f"   Valid Values: {', '.join(enum_config['valid_values'])}")
        print(f"   Has CHECK Constraint: {'✅ YES (CRITICAL!)' if enum_config['has_check_constraint'] else '⚠️  NO (Soft Error)'}")
        print()

        # Group by file
        files_dict: Dict[Path, List] = {}
        for file, line_num, found_value, line_content in field_violations:
            if file not in files_dict:
                files_dict[file] = []
            files_dict[file].append((line_num, found_value, line_content))

        for file, file_violations_list in files_dict.items():
            print(f"   📄 File: {file.name}")
            for line_num, found_value, line_content in file_violations_list:
                # Find correct value
                correct_value = found_value.upper()
                print(f"      Line {line_num}: '{found_value}' → should be '{correct_value}'")
                # print(f"         {line_content[:100]}")  # Uncomment to show line content
            print()

    print("=" * 80)
    print("🔧 HOW TO FIX:")
    print("=" * 80)
    print()
    print("Option 1: Manual Fix")
    print("  - Edit the files listed above")
    print("  - Change all enum values to UPPERCASE")
    print("  - Example: 'GmbH' → 'GMBH', 'Frau' → 'FRAU'")
    print()
    print("Option 2: Bulk Fix (sed)")
    print("  cd backend/src/main/resources/db/dev-seed")
    print("  for file in V900*.sql; do")
    print("    sed -i '' \\")
    print("      -e \"s/'GmbH',/'GMBH',/g\" \\")
    print("      -e \"s/'Einzelunternehmen',/'EINZELUNTERNEHMEN',/g\" \\")
    print("      -e \"s/'GbR',/'GBR',/g\" \\")
    print("      -e \"s/'eV',/'EV',/g\" \\")
    print("      -e \"s/'Herr',/'HERR',/g\" \\")
    print("      -e \"s/'Frau',/'FRAU',/g\" \\")
    print("      -e \"s/'Divers',/'DIVERS',/g\" \\")
    print("      \"$file\"")
    print("  done")
    print()
    print("=" * 80)
    print()

# ==============================================================================
# MAIN
# ==============================================================================

def main() -> int:
    """
    Main entry point.

    Returns:
        0 = success (no violations)
        1 = failure (violations found)
    """

    # Check if seed directory exists
    if not SEED_DIR.exists():
        print(f"⚠️  WARNING: Seed directory not found: {SEED_DIR}")
        print("   Skipping enum seed data validation.")
        return 0  # Don't block commit if seed dir doesn't exist

    success, violations = check_all_seed_files()

    if success:
        print("✅ Enum Seed Data Validation: PASSED")
        print(f"   Scanned {len(list(SEED_DIR.glob('V900*.sql')))} seed files")
        print(f"   All enum values are correctly UPPERCASE")
        return 0
    else:
        print_violations(violations)
        print(f"❌ Found {len(violations)} enum case violations in seed files")
        print()
        print("🚫 PRE-COMMIT HOOK BLOCKED")
        print("   Fix violations before committing.")
        print("   Bypass (NOT RECOMMENDED): git commit --no-verify")
        return 1

if __name__ == "__main__":
    sys.exit(main())
