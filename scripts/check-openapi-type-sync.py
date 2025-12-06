#!/usr/bin/env python3
"""
OpenAPI Type Sync Check - Contract-First Development Guard
===========================================================
Purpose: Ensure Frontend types are generated from Backend DTOs (Single Source of Truth)
         NOT manually maintained in types.ts files.

Sprint 2.1.8: Prevents manual type definitions that can drift from backend schema.

Exit Codes:
- 0 = OK (no manual type definitions found, or warning mode)
- 1 = BLOCKED (manual type definitions detected in strict mode)

Modes:
- --strict: Block commits with manual type definitions (default for CI)
- --warn: Only warn about manual types, don't block (default for pre-commit during migration)
- --new-fields-only: Only block NEW manual fields (allows existing technical debt)

Usage:
- Pre-commit hook: Warns about manual types, blocks new ones
- CI: Can be strict or warning depending on configuration
"""

import argparse
import re
import sys
from pathlib import Path
from dataclasses import dataclass
from typing import List, Set, Dict

# ============================================================================
# Configuration
# ============================================================================

SCRIPT_DIR = Path(__file__).parent
PROJECT_ROOT = SCRIPT_DIR.parent

# Backend DTO files to check (Single Source of Truth)
BACKEND_DTO_PATHS = [
    PROJECT_ROOT / "backend/src/main/java/de/freshplan/modules/leads/api/LeadDTO.java",
    PROJECT_ROOT / "backend/src/main/java/de/freshplan/modules/leads/api/LeadContactDTO.java",
]

# Frontend type files that SHOULD be generated (not manual)
FRONTEND_TYPE_FILES = [
    PROJECT_ROOT / "frontend/src/features/leads/types.ts",
]

# Expected generated types directory (Contract-First)
GENERATED_TYPES_DIR = PROJECT_ROOT / "frontend/src/api/generated"

# Fields that are OK to have in frontend (framework/UI-only fields)
FRONTEND_ONLY_FIELDS = {
    # UI state fields
    'consentGiven',  # UI checkbox state, maps to consentGivenAt
    # Legacy compatibility aliases
    'name',  # Legacy alias for companyName
}

# ============================================================================
# BASELINE: Known manual type definitions (technical debt from before Sprint 2.1.8)
# These are allowed in --new-fields-only mode but flagged in --strict mode.
# As fields are migrated to OpenAPI, remove them from this list.
# ============================================================================
BASELINE_MANUAL_FIELDS = {
    # Core Lead fields (existed before Sprint 2.1.8)
    'id', 'stage', 'companyName', 'companyNameNormalized', 'city', 'postalCode',
    'street', 'countryCode', 'territory', 'contact', 'contacts', 'contactPerson',
    'email', 'emailNormalized', 'phone', 'phoneE164', 'consentGivenAt',
    'businessType', 'industry', 'kitchenSize', 'employeeCount', 'estimatedVolume',
    'website', 'websiteDomain', 'status', 'ownerUserId', 'collaboratorUserIds',
    'source', 'registeredAt', 'firstContactDocumentedAt', 'protectionUntil',
    'progressDeadline', 'progressWarningSentAt', 'lastActivityAt',
    'clockStoppedAt', 'stopReason', 'leadScore', 'budgetConfirmed', 'dealSize',
    'painScore', 'revenueScore', 'fitScore', 'engagementScore', 'branchCount',
    'isChain', 'painStaffShortage', 'painHighCosts', 'painFoodWaste',
    'painQualityInconsistency', 'painTimePressure', 'painSupplierQuality',
    'painUnreliableDelivery', 'painPoorService', 'painNotes', 'urgencyLevel',
    'multiPainBonus', 'relationshipStatus', 'decisionMakerAccess',
    'competitorInUse', 'internalChampionName', 'createdAt', 'updatedAt',
    # Sprint 2.1.8 fields (added to baseline as they were added manually first)
    'originalCreatedAt', 'effectiveCreatedAt',
}

# Java type to TypeScript type mapping for validation
JAVA_TO_TS_TYPES = {
    'Long': 'string',  # IDs are often strings in frontend
    'Integer': 'number',
    'String': 'string',
    'Boolean': 'boolean',
    'LocalDateTime': 'string',  # ISO 8601 strings
    'BigDecimal': 'number',
    'Set<String>': 'string[]',
    'List<LeadContactDTO>': 'LeadContactDTO[]',
}


@dataclass
class FieldDefinition:
    name: str
    java_type: str
    line_number: int
    file_path: Path


@dataclass
class ManualTypeViolation:
    field_name: str
    file_path: Path
    line_number: int
    message: str


# ============================================================================
# Backend DTO Field Extraction
# ============================================================================

def extract_java_dto_fields(dto_path: Path) -> List[FieldDefinition]:
    """
    Extract public field definitions from a Java DTO file.

    Matches patterns like:
    - public String companyName;
    - public LocalDateTime createdAt;
    - public List<LeadContactDTO> contacts = new ArrayList<>();
    """
    if not dto_path.exists():
        print(f"  [WARN] DTO file not found: {dto_path}")
        return []

    content = dto_path.read_text()
    fields = []

    # Pattern: public Type fieldName; or public Type fieldName = value;
    # Also handles generic types like List<T>, Set<T>
    pattern = r'^\s*public\s+(\w+(?:<[\w<>,\s]+>)?)\s+(\w+)\s*[;=]'

    for i, line in enumerate(content.split('\n'), 1):
        match = re.match(pattern, line)
        if match:
            java_type = match.group(1)
            field_name = match.group(2)

            # Skip class-level constants and non-field declarations
            if field_name in {'class', 'static', 'final', 'serialVersionUID'}:
                continue

            fields.append(FieldDefinition(
                name=field_name,
                java_type=java_type,
                line_number=i,
                file_path=dto_path
            ))

    return fields


def extract_all_backend_fields() -> Dict[str, FieldDefinition]:
    """Extract all field definitions from all backend DTOs."""
    all_fields = {}

    for dto_path in BACKEND_DTO_PATHS:
        fields = extract_java_dto_fields(dto_path)
        for field in fields:
            all_fields[field.name] = field

    return all_fields


# ============================================================================
# Frontend Type Field Extraction
# ============================================================================

def extract_typescript_type_fields(ts_path: Path) -> List[FieldDefinition]:
    """
    Extract field definitions from TypeScript type files.

    Matches patterns like:
    - companyName: string;
    - createdAt?: string;
    - contacts?: LeadContactDTO[];
    """
    if not ts_path.exists():
        print(f"  [WARN] TypeScript file not found: {ts_path}")
        return []

    content = ts_path.read_text()
    fields = []

    # Pattern: fieldName?: Type; or fieldName: Type;
    pattern = r'^\s*(\w+)\??\s*:\s*([^;]+);'

    # Track if we're inside a type/interface definition for Lead
    in_lead_type = False
    brace_depth = 0

    for i, line in enumerate(content.split('\n'), 1):
        # Detect start of Lead type
        if re.search(r'^\s*export\s+type\s+Lead\s*=\s*\{', line):
            in_lead_type = True
            brace_depth = 1
            continue

        # Track brace depth
        if in_lead_type:
            brace_depth += line.count('{') - line.count('}')
            if brace_depth <= 0:
                in_lead_type = False
                continue

            # Extract field
            match = re.match(pattern, line)
            if match:
                field_name = match.group(1)
                ts_type = match.group(2).strip()

                fields.append(FieldDefinition(
                    name=field_name,
                    java_type=ts_type,  # Repurpose for TS type
                    line_number=i,
                    file_path=ts_path
                ))

    return fields


# ============================================================================
# Validation Logic
# ============================================================================

def check_for_manual_type_definitions() -> List[ManualTypeViolation]:
    """
    Check for manual type definitions that should be auto-generated.

    Returns list of violations (fields defined manually instead of generated).
    """
    violations = []

    # Get all backend fields (Single Source of Truth)
    backend_fields = extract_all_backend_fields()
    backend_field_names = set(backend_fields.keys())

    print(f"  Backend DTOs: {len(backend_field_names)} fields found")

    # Check each frontend type file
    for ts_path in FRONTEND_TYPE_FILES:
        if not ts_path.exists():
            continue

        frontend_fields = extract_typescript_type_fields(ts_path)
        print(f"  Frontend types.ts: {len(frontend_fields)} Lead fields found")

        for field in frontend_fields:
            # Skip frontend-only fields (UI state)
            if field.name in FRONTEND_ONLY_FIELDS:
                continue

            # Check if field exists in backend
            if field.name in backend_field_names:
                # Field exists in both - this is a MANUAL DUPLICATION
                # Should be generated from OpenAPI, not manually defined
                violations.append(ManualTypeViolation(
                    field_name=field.name,
                    file_path=ts_path,
                    line_number=field.line_number,
                    message=f"Manual type definition duplicates Backend DTO field"
                ))

    return violations


def check_generated_types_exist() -> bool:
    """Check if generated types directory exists and has content."""
    if not GENERATED_TYPES_DIR.exists():
        return False

    # Check for any .ts files in the directory
    ts_files = list(GENERATED_TYPES_DIR.glob("*.ts"))
    return len(ts_files) > 0


# ============================================================================
# Main
# ============================================================================

def parse_args():
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(
        description='OpenAPI Type Sync Check - Contract-First Development Guard'
    )
    parser.add_argument(
        '--strict',
        action='store_true',
        help='Block all manual type definitions (fails on any manual types)'
    )
    parser.add_argument(
        '--warn',
        action='store_true',
        help='Only warn about manual types, never block (exit 0)'
    )
    parser.add_argument(
        '--new-fields-only',
        action='store_true',
        default=True,
        help='Only block NEW manual fields not in baseline (default)'
    )
    return parser.parse_args()


def main():
    args = parse_args()

    # Determine mode
    if args.strict:
        mode = 'strict'
    elif args.warn:
        mode = 'warn'
    else:
        mode = 'new-fields-only'

    print("=" * 60)
    print("OpenAPI Type Sync Check - Contract-First Development Guard")
    print(f"Mode: {mode.upper()}")
    print("=" * 60)
    print()

    # Step 1: Check if generated types exist
    print("[1/3] Checking for generated types directory...")
    if check_generated_types_exist():
        print(f"  {GENERATED_TYPES_DIR.relative_to(PROJECT_ROOT)}")
        print("  [OK] Generated types directory exists")
    else:
        print(f"  [WARN] Generated types directory not found: {GENERATED_TYPES_DIR}")
        print("  This may indicate npm run generate-api has not been run.")
    print()

    # Step 2: Extract backend and frontend fields
    print("[2/3] Extracting type definitions...")
    backend_fields = extract_all_backend_fields()
    print(f"  Backend: {len(backend_fields)} DTO fields extracted")

    frontend_count = 0
    for ts_path in FRONTEND_TYPE_FILES:
        if ts_path.exists():
            fields = extract_typescript_type_fields(ts_path)
            frontend_count += len(fields)
    print(f"  Frontend: {frontend_count} manual type fields found")
    print()

    # Step 3: Check for violations
    print("[3/3] Checking for manual type duplication...")
    all_violations = check_for_manual_type_definitions()

    # Filter violations based on mode
    if mode == 'new-fields-only':
        # Only consider violations for fields NOT in the baseline
        violations = [v for v in all_violations if v.field_name not in BASELINE_MANUAL_FIELDS]
        baseline_violations = [v for v in all_violations if v.field_name in BASELINE_MANUAL_FIELDS]
    else:
        violations = all_violations
        baseline_violations = []
    print()

    # Report baseline (technical debt)
    if baseline_violations:
        print(f"  [INFO] {len(baseline_violations)} baseline manual fields (technical debt, allowed)")

    # Report results
    if not violations:
        print()
        print("=" * 60)
        if mode == 'new-fields-only' and baseline_violations:
            print(f"[OK] No NEW manual type duplications detected!")
            print(f"     ({len(baseline_violations)} baseline fields allowed as technical debt)")
        else:
            print("[OK] No manual type duplications detected!")
        print("=" * 60)
        print()
        print("Note: This check verifies that frontend types are not")
        print("      manually duplicating backend DTO definitions.")
        print()
        print("Best Practice: Use 'npm run generate-api' to auto-generate")
        print("               TypeScript types from OpenAPI spec.")
        return 0

    # Report violations
    status = "[BLOCKED]" if mode != 'warn' else "[WARNING]"
    print()
    print("=" * 60)
    print(f"{status} {len(violations)} NEW Manual Type Duplications Found!")
    print("=" * 60)
    print()
    print("The following frontend fields are MANUALLY DEFINED but should")
    print("be AUTO-GENERATED from the Backend OpenAPI spec:")
    print()

    # Group by file
    by_file: Dict[Path, List[ManualTypeViolation]] = {}
    for v in violations:
        if v.file_path not in by_file:
            by_file[v.file_path] = []
        by_file[v.file_path].append(v)

    for file_path, file_violations in by_file.items():
        rel_path = file_path.relative_to(PROJECT_ROOT)
        print(f"File: {rel_path}")
        print("-" * 50)

        # Show first 20 violations per file
        for v in file_violations[:20]:
            print(f"  Line {v.line_number}: {v.field_name}")

        if len(file_violations) > 20:
            print(f"  ... and {len(file_violations) - 20} more")
        print()

    print("=" * 60)
    print("HOW TO FIX:")
    print("=" * 60)
    print()
    print("1. Remove manual type definitions from types.ts")
    print("2. Add @Schema annotations to backend DTOs for OpenAPI")
    print("3. Generate types: cd frontend && npm run generate-api")
    print("4. Import generated types instead of manual definitions")
    print()
    print("Example migration:")
    print("  // Before (manual - BAD)")
    print("  export type Lead = { companyName: string; ... };")
    print()
    print("  // After (generated - GOOD)")
    print("  export { Lead } from '../api/generated';")
    print()
    print("Reference: Backend DTOs in:")
    for dto_path in BACKEND_DTO_PATHS:
        if dto_path.exists():
            print(f"  - {dto_path.relative_to(PROJECT_ROOT)}")
    print()

    # In warn mode, always return 0
    if mode == 'warn':
        print("[INFO] Running in WARN mode - not blocking commit")
        return 0

    return 1


if __name__ == '__main__':
    sys.exit(main())
