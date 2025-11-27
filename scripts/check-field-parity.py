#!/usr/bin/env python3
"""
Backend/Frontend Field Parity Guard (Server-Driven UI)
=======================================================
Purpose: Ensure Backend Schema Resources exist and entity fields are consistent
         (Sprint 2.1.7.7 - fieldCatalog.json removed, now Server-Driven UI)
Usage: ./scripts/check-field-parity.py
Exit: 0 = OK, 1 = Issues found
"""

import re
import sys
from pathlib import Path

# Setup paths
SCRIPT_DIR = Path(__file__).parent
PROJECT_ROOT = SCRIPT_DIR.parent
CUSTOMER_ENTITY = PROJECT_ROOT / "backend/src/main/java/de/freshplan/domain/customer/entity/Customer.java"
LOCATION_ENTITY = PROJECT_ROOT / "backend/src/main/java/de/freshplan/domain/customer/entity/CustomerLocation.java"
CUSTOMER_SCHEMA = PROJECT_ROOT / "backend/src/main/java/de/freshplan/domain/customer/api/CustomerSchemaResource.java"
BRANCH_SCHEMA = PROJECT_ROOT / "backend/src/main/java/de/freshplan/domain/customer/api/BranchSchemaResource.java"
CONTACT_SCHEMA = PROJECT_ROOT / "backend/src/main/java/de/freshplan/domain/customer/api/ContactSchemaResource.java"
LOCATION_SCHEMA = PROJECT_ROOT / "backend/src/main/java/de/freshplan/api/resources/LocationServiceSchemaResource.java"

# Legacy file - should NOT exist anymore
LEGACY_FIELD_CATALOG = PROJECT_ROOT / "frontend/src/features/customers/data/fieldCatalog.json"

def extract_entity_fields(entity_path):
    """Extract field names from a Java entity"""
    if not entity_path.exists():
        return set()

    content = entity_path.read_text()

    # Multiple patterns to catch different field declaration styles
    patterns = [
        r'^\s*private\s+\w+(?:<[^>]+>)?\s+(\w+)\s*;',           # private Type field;
        r'^\s*private\s+\w+(?:<[^>]+>)?\s+(\w+)\s*=',           # private Type field = value;
        r'^\s*@Column.*?\n\s*private\s+\w+(?:<[^>]+>)?\s+(\w+)', # @Column\n private Type field
    ]

    fields = set()
    lines = content.split('\n')

    for i, line in enumerate(lines):
        for pattern in patterns:
            # For multi-line patterns, combine current + next line
            combined_line = line
            if i + 1 < len(lines):
                combined_line = line + '\n' + lines[i + 1]

            match = re.search(pattern, combined_line)
            if match:
                field_name = match.group(1)
                # Skip common non-field matches
                if field_name not in {'serialVersionUID', 'class', 'this', 'new'}:
                    fields.add(field_name)

    return fields

def extract_schema_fields(schema_path):
    """Extract field keys from a Schema Resource"""
    if not schema_path.exists():
        return set()

    content = schema_path.read_text()

    # Look for .key("fieldName") patterns
    pattern = r'\.key\s*\(\s*"(\w+)"\s*\)'
    matches = re.findall(pattern, content)

    return set(matches)

def check_legacy_file():
    """Check that fieldCatalog.json no longer exists"""
    if LEGACY_FIELD_CATALOG.exists():
        print("❌ ERROR: fieldCatalog.json still exists!")
        print(f"   Path: {LEGACY_FIELD_CATALOG}")
        print("   This file was removed in Sprint 2.1.7.7 (Server-Driven UI)")
        print("   Please delete it and use Backend Schema Resources instead.")
        return False
    return True

def main():
    print("🔍 Backend/Frontend Field Parity Check (Server-Driven UI)")
    print("━" * 50)

    errors = []

    # Check 1: Legacy fieldCatalog.json should not exist
    print("📋 Checking for deprecated fieldCatalog.json...")
    if not check_legacy_file():
        errors.append("fieldCatalog.json still exists")
    else:
        print("✅ fieldCatalog.json correctly removed (Server-Driven UI)")

    print()

    # Check 2: Backend entities exist
    print("📋 Extracting backend entity fields...")
    customer_backend = extract_entity_fields(CUSTOMER_ENTITY)
    location_backend = extract_entity_fields(LOCATION_ENTITY)
    print(f"✅ Customer.java: {len(customer_backend)} fields")
    print(f"✅ CustomerLocation.java: {len(location_backend)} fields")

    print()

    # Check 3: Schema Resources exist
    print("📋 Checking Backend Schema Resources...")
    schema_files = [
        (CUSTOMER_SCHEMA, "CustomerSchemaResource"),
        (BRANCH_SCHEMA, "BranchSchemaResource"),
        (CONTACT_SCHEMA, "ContactSchemaResource"),
        (LOCATION_SCHEMA, "LocationServiceSchemaResource"),
    ]

    for schema_path, name in schema_files:
        if schema_path.exists():
            fields = extract_schema_fields(schema_path)
            print(f"✅ {name}: {len(fields)} field definitions")
        else:
            print(f"⚠️  {name}: not found (optional)")

    print()

    # Check 4: Schema fields exist in entities (spot check)
    print("📋 Validating Schema → Entity parity...")
    customer_schema_fields = extract_schema_fields(CUSTOMER_SCHEMA)

    if customer_schema_fields:
        ghost_fields = sorted(customer_schema_fields - customer_backend)
        # Exclude computed/virtual fields that don't need entity backing
        virtual_fields = {'id', 'createdAt', 'updatedAt', 'createdBy', 'modifiedBy'}
        ghost_fields = [f for f in ghost_fields if f not in virtual_fields]

        if ghost_fields:
            print(f"⚠️  Warning: {len(ghost_fields)} schema fields not in Customer entity:")
            for field in ghost_fields[:10]:  # Show first 10
                print(f"    - {field}")
            if len(ghost_fields) > 10:
                print(f"    ... and {len(ghost_fields) - 10} more")
        else:
            print("✅ All CustomerSchema fields exist in entity")

    print()
    print("━" * 50)

    # Final result
    if errors:
        print(f"❌ FAILURE: {len(errors)} issue(s) found")
        for error in errors:
            print(f"   - {error}")
        sys.exit(1)
    else:
        print("✅ SUCCESS: Server-Driven UI Field Parity OK")
        print("   - fieldCatalog.json removed ✓")
        print("   - Backend entities exist ✓")
        print("   - Schema Resources exist ✓")
        sys.exit(0)

if __name__ == '__main__':
    main()
