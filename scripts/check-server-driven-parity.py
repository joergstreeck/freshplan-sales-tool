#!/usr/bin/env python3
"""
Server-Driven Architecture Parity Guard (Sprint 2.1.7.2 D11)
============================================================
Purpose: Enforce Backend = Single Source of Truth for schema + enums
         Prevent frontend-only fields, hardcoded options, missing constraints

STUFE 1: Schema → Entity Parity
  - CustomerSchemaResource.fieldKey MUST exist in Customer.java
  - BLOCKS: Ghost fields (schema without entity)

STUFE 2: Enum Source Validation
  - ENUM fields MUST have enumSource="/api/enums/xyz"
  - EnumResource MUST exist at backend/src/.../api/EnumResource.java
  - BLOCKS: Hardcoded options[], missing EnumResource

STUFE 3: Database Constraint Check
  - VARCHAR fields with ENUM-Source SHOULD have CHECK constraint
  - Pattern: CHECK (field_name IN ('val1', 'val2'))
  - WARNS: Enum without DB constraint (not blocking yet)

Usage: ./scripts/check-server-driven-parity.py
Exit: 0 = OK, 1 = Violations found (blocks commit)
"""

import re
import sys
from pathlib import Path
from typing import Set, Dict, List, Tuple

# Colors
RED = '\033[0;31m'
YELLOW = '\033[1;33m'
GREEN = '\033[0;32m'
BLUE = '\033[0;34m'
NC = '\033[0m'

# Paths
SCRIPT_DIR = Path(__file__).parent
PROJECT_ROOT = SCRIPT_DIR.parent
CUSTOMER_SCHEMA = PROJECT_ROOT / "backend/src/main/java/de/freshplan/domain/customer/api/CustomerSchemaResource.java"
CUSTOMER_ENTITY = PROJECT_ROOT / "backend/src/main/java/de/freshplan/domain/customer/entity/Customer.java"
CONTACT_ENTITY = PROJECT_ROOT / "backend/src/main/java/de/freshplan/domain/customer/entity/CustomerContact.java"
CONTACT_DIALOG = PROJECT_ROOT / "frontend/src/features/customers/components/detail/ContactEditDialog.tsx"
ENUM_RESOURCE_FILE = PROJECT_ROOT / "backend/src/main/java/de/freshplan/api/resources/EnumResource.java"
MIGRATIONS_DIR = PROJECT_ROOT / "backend/src/main/resources/db/migration"


def extract_schema_fields() -> Dict[str, Dict]:
    """
    Extract all TOP-LEVEL fieldKey + metadata from CustomerSchemaResource.java

    Ignores nested fields in .fields() (GROUP type) and .itemSchema() (ARRAY type)

    Strategy: Top-level fields have LESS indentation than nested fields
    - Top-level: ~16-20 spaces
    - Nested: ~24+ spaces

    Returns dict: {
        'fieldKey': {
            'type': 'TEXT|ENUM|...',
            'enumSource': '/api/enums/xyz' or None,
            'line': 123
        }
    }
    """
    if not CUSTOMER_SCHEMA.exists():
        print(f"{RED}❌ ERROR: CustomerSchemaResource.java not found{NC}")
        sys.exit(1)

    content = CUSTOMER_SCHEMA.read_text()
    fields = {}

    # Pattern: .fieldKey("xyz")
    field_key_pattern = r'\.fieldKey\("(\w+)"\)'
    # Pattern: .type(FieldType.ENUM)
    type_pattern = r'\.type\(FieldType\.(\w+)\)'
    # Pattern: .enumSource("/api/enums/xyz")
    enum_source_pattern = r'\.enumSource\("([^"]+)"\)'

    lines = content.split('\n')
    current_field = None
    current_type = None
    current_enum_source = None
    current_indentation = 0

    for i, line in enumerate(lines, 1):
        # Detect new field definition
        field_match = re.search(field_key_pattern, line)
        if field_match:
            # Calculate indentation (number of leading spaces)
            indentation = len(line) - len(line.lstrip(' '))

            # Top-level fields have indentation <= 20 (typically 16-20)
            # Nested fields have indentation >= 24
            if indentation <= 20:
                # Save previous field
                if current_field:
                    fields[current_field] = {
                        'type': current_type or 'UNKNOWN',
                        'enumSource': current_enum_source,
                        'line': fields.get(current_field, {}).get('line', i)
                    }

                current_field = field_match.group(1)
                current_type = None
                current_enum_source = None
                current_indentation = indentation
                fields[current_field] = {'line': i}
            # Else: nested field, ignore

        # Detect type and enumSource - only for current field
        if current_field:
            type_match = re.search(type_pattern, line)
            if type_match:
                # Check indentation - only accept if similar to field indentation
                indentation = len(line) - len(line.lstrip(' '))
                if indentation <= 20:
                    current_type = type_match.group(1)

            enum_match = re.search(enum_source_pattern, line)
            if enum_match:
                # Check indentation - only accept if similar to field indentation
                indentation = len(line) - len(line.lstrip(' '))
                if indentation <= 20:
                    current_enum_source = enum_match.group(1)

    # Save last field
    if current_field:
        fields[current_field] = {
            'type': current_type or 'UNKNOWN',
            'enumSource': current_enum_source,
            'line': fields.get(current_field, {}).get('line', 0)
        }

    return fields


def extract_entity_fields() -> Set[str]:
    """Extract field names from Customer.java entity"""
    if not CUSTOMER_ENTITY.exists():
        print(f"{RED}❌ ERROR: Customer.java not found{NC}")
        sys.exit(1)

    content = CUSTOMER_ENTITY.read_text()
    fields = set()

    # Patterns for private fields
    patterns = [
        r'^\s*private\s+\w+(?:<[^>]+>)?\s+(\w+)\s*;',
        r'^\s*private\s+\w+(?:<[^>]+>)?\s+(\w+)\s*=',
        r'^\s*@Column.*?\n\s*private\s+\w+(?:<[^>]+>)?\s+(\w+)',
    ]

    lines = content.split('\n')
    for i, line in enumerate(lines):
        for pattern in patterns:
            combined = line
            if i + 1 < len(lines):
                combined = line + '\n' + lines[i + 1]

            match = re.search(pattern, combined, re.MULTILINE)
            if match:
                field_name = match.group(1)
                if field_name not in {'serialVersionUID', 'class'}:
                    fields.add(field_name)

    return fields


def extract_enum_resources() -> Dict[str, Path]:
    """
    Extract all enum endpoints from EnumResource.java

    Scans for class-level @Path("/api/enums") and method-level @Path("/xyz"),
    then combines them to build full endpoint paths like "/api/enums/xyz"

    Example:
        @Path("/api/enums")  // class level
        public class EnumResource {
            @GET
            @Path("/legal-forms")  // method level
            public List<EnumValue> getLegalForms() { ... }
        }

        Result: {'/api/enums/legal-forms': Path to EnumResource.java}

    Returns: {
        '/api/enums/legal-forms': Path to EnumResource.java,
        '/api/enums/customer-types': Path to EnumResource.java,
        ...
    }
    """
    enum_resources = {}

    if not ENUM_RESOURCE_FILE.exists():
        return enum_resources

    content = ENUM_RESOURCE_FILE.read_text()

    # Step 1: Extract class-level @Path (e.g., "/api/enums")
    class_path_pattern = r'@Path\("(/api/enums)"\)'
    class_match = re.search(class_path_pattern, content)
    if not class_match:
        # No class-level @Path found, skip
        return enum_resources

    class_path = class_match.group(1)  # "/api/enums"

    # Step 2: Extract all method-level @Path annotations
    # Pattern: Look for @GET followed by @Path("/xyz")
    # This ensures we only match GET endpoint methods
    method_path_pattern = r'@GET\s+@Path\("([^"]+)"\)'

    for match in re.finditer(method_path_pattern, content, re.MULTILINE | re.DOTALL):
        method_path = match.group(1)  # e.g., "/legal-forms"
        full_path = class_path + method_path  # "/api/enums/legal-forms"
        enum_resources[full_path] = ENUM_RESOURCE_FILE

    return enum_resources


def check_database_constraints(field_name: str, enum_source: str) -> Tuple[bool, str]:
    """
    Check if VARCHAR field has CHECK constraint in migrations

    Returns: (has_constraint: bool, constraint_line: str)
    """
    if not MIGRATIONS_DIR.exists():
        return False, ""

    # Pattern: CHECK (field_name IN ('val1', 'val2'))
    constraint_pattern = rf'CHECK\s*\(\s*{field_name}\s+IN\s*\('

    for migration in sorted(MIGRATIONS_DIR.glob("V*.sql")):
        content = migration.read_text()
        if re.search(constraint_pattern, content, re.IGNORECASE):
            # Extract constraint line
            for line in content.split('\n'):
                if re.search(constraint_pattern, line, re.IGNORECASE):
                    return True, line.strip()

    return False, ""


def extract_contact_frontend_fields() -> Set[str]:
    """
    Extract field names from ContactEditDialog.tsx (Contact interface)

    Returns: Set of field names that frontend declares
    """
    if not CONTACT_DIALOG.exists():
        return set()

    content = CONTACT_DIALOG.read_text()
    fields = set()

    # Find Contact interface definition
    # Pattern: export interface Contact { ... }
    interface_pattern = r'export\s+interface\s+Contact\s*\{([^}]+)\}'
    match = re.search(interface_pattern, content, re.DOTALL)

    if match:
        interface_body = match.group(1)

        # Extract field names LINE BY LINE to filter out comments
        for line in interface_body.split('\n'):
            # Skip comment-only lines
            line_stripped = line.strip()
            if line_stripped.startswith('//') or not line_stripped:
                continue

            # Remove inline comments: "firstName: string; // comment" -> "firstName: string;"
            line_clean = line.split('//')[0]

            # Pattern: fieldName?: type; (at start of line, after optional whitespace)
            field_pattern = r'^\s*(\w+)\??:\s*'
            field_match = re.search(field_pattern, line_clean)
            if field_match:
                field_name = field_match.group(1)
                # Keep as camelCase for comparison with Entity (both use camelCase)
                fields.add(field_name)

    return fields


def extract_contact_entity_fields() -> Set[str]:
    """Extract field names from CustomerContact.java entity

    Also handles DTO→Entity mappings:
    - Frontend: assignedLocationId (String) → Backend: assignedLocation (CustomerLocation)
    """
    if not CONTACT_ENTITY.exists():
        return set()

    content = CONTACT_ENTITY.read_text()
    fields = set()

    # Same patterns as Customer entity extraction
    # Note: [\w.]+ matches qualified types like java.time.LocalDate
    patterns = [
        r'^\s*private\s+[\w.]+(?:<[^>]+>)?\s+(\w+)\s*;',
        r'^\s*private\s+[\w.]+(?:<[^>]+>)?\s+(\w+)\s*=',
        r'^\s*@Column.*?\n\s*private\s+[\w.]+(?:<[^>]+>)?\s+(\w+)',
    ]

    lines = content.split('\n')
    for i, line in enumerate(lines):
        for pattern in patterns:
            combined = line
            if i + 1 < len(lines):
                combined = line + '\n' + lines[i + 1]

            match = re.search(pattern, combined, re.MULTILINE)
            if match:
                field_name = match.group(1)
                if field_name not in {'serialVersionUID', 'class', 'roles', 'reportsTo', 'directReports'}:
                    fields.add(field_name)

                    # DTO→Entity mapping: assignedLocation → also accept assignedLocationId
                    if field_name == 'assignedLocation':
                        fields.add('assignedLocationId')

    return fields


def check_frontend_schema_driven() -> Tuple[bool, List[str]]:
    """
    Check if ContactEditDialog.tsx uses schema-driven rendering instead of hardcoded fields

    Returns: (is_schema_driven: bool, violations: List[str])

    Schema-driven indicators:
    - ✅ Uses useContactSchema() hook
    - ✅ Has renderField() or similar dynamic rendering function
    - ✅ Maps over schema.sections or schema.fields

    Hardcoded indicators (violations):
    - ❌ Hardcoded <TextField label="Vorname" />
    - ❌ Hardcoded field keys without schema mapping
    """
    if not CONTACT_DIALOG.exists():
        return True, []  # File doesn't exist, skip check

    content = CONTACT_DIALOG.read_text()
    violations = []

    # Check 1: Does it use useContactSchema()?
    uses_schema_hook = 'useContactSchema()' in content

    # Check 2: Does it have dynamic rendering?
    has_dynamic_rendering = bool(
        re.search(r'\.map\(\s*\(?(?:section|field)', content) or
        re.search(r'renderField\s*\(', content) or
        re.search(r'contactSchema.*\.sections', content)
    )

    # Check 3: Look for hardcoded field labels (strong indicator of hardcoded UI)
    # Pattern: <TextField label="Vorname" (but NOT in comments)
    hardcoded_patterns = [
        r'<TextField\s+[^>]*label="(Vorname|Nachname|E-Mail|Telefon|Position|Anrede|Titel)"',
        r'<Grid[^>]*>\s*<TextField[^>]*label="[^"]{3,}"',  # Grid with hardcoded TextField
    ]

    hardcoded_matches = []
    for pattern in hardcoded_patterns:
        matches = re.findall(pattern, content)
        hardcoded_matches.extend(matches)

    # Determine if schema-driven
    is_schema_driven = uses_schema_hook and has_dynamic_rendering

    # Build violation messages
    if not uses_schema_hook:
        violations.append("Missing useContactSchema() hook import/usage")

    if not has_dynamic_rendering:
        violations.append("No dynamic rendering detected (.map over sections/fields)")

    if hardcoded_matches and not is_schema_driven:
        violations.append(f"Found {len(hardcoded_matches)} hardcoded field labels (schema-driven should render dynamically)")

    return is_schema_driven, violations


def main():
    print(f"{BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━{NC}")
    print(f"{BLUE}🔒 Server-Driven Architecture Parity Guard{NC}")
    print(f"{BLUE}   Sprint 2.1.7.2 D11 - Backend = Single Source of Truth{NC}")
    print(f"{BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━{NC}")
    print()

    # Track all violations across all stages
    all_violations = {
        'stufe1': [],
        'stufe2': [],
        'stufe4': [],
        'stufe5': [],
    }

    # ========== STUFE 1: Schema → Entity Parity ==========
    print(f"{BLUE}📋 STUFE 1: Schema → Entity Parity{NC}")
    print()

    schema_fields = extract_schema_fields()
    entity_fields = extract_entity_fields()

    print(f"  CustomerSchemaResource.java: {len(schema_fields)} fields")
    print(f"  Customer.java entity: {len(entity_fields)} fields")
    print()

    ghost_fields = []
    for field_name, metadata in schema_fields.items():
        # Skip GROUP and ARRAY fields - they don't exist in entity, only their nested fields do
        if metadata['type'] in ('GROUP', 'ARRAY'):
            continue

        if field_name not in entity_fields:
            ghost_fields.append((field_name, metadata['line']))

    if ghost_fields:
        print(f"{RED}❌ FAILURE: Found {len(ghost_fields)} ghost fields (schema without entity):{NC}")
        print()
        for field, line in ghost_fields:
            print(f"  {RED}✗{NC} {field} (CustomerSchemaResource.java:{line})")
        print()
        print(f"{RED}🚫 RULE VIOLATION: Backend Entity Parity (ZERO TOLERANCE){NC}")
        print()
        print("📖 Fix by:")
        print("   1. Add missing fields to Customer.java entity")
        print("   2. Create migration to add columns to 'customers' table")
        print("   3. Or remove ghost fields from CustomerSchemaResource.java")
        print()
        return 1
    else:
        print(f"{GREEN}✅ STUFE 1 PASSED: All schema fields exist in entity{NC}")
        print()

    # ========== STUFE 2: Enum Source Validation ==========
    print(f"{BLUE}📋 STUFE 2: Enum Source Validation{NC}")
    print()

    enum_resources = extract_enum_resources()
    print(f"  Found {len(enum_resources)} EnumResource files")

    enum_violations = []

    for field_name, metadata in schema_fields.items():
        if metadata['type'] == 'ENUM':
            enum_source = metadata.get('enumSource')

            # Check 1: enumSource must exist
            if not enum_source:
                enum_violations.append({
                    'field': field_name,
                    'line': metadata['line'],
                    'error': 'Missing enumSource (hardcoded options forbidden!)'
                })
                continue

            # Check 2: EnumResource must exist
            if enum_source not in enum_resources:
                enum_violations.append({
                    'field': field_name,
                    'line': metadata['line'],
                    'error': f'EnumResource not found: {enum_source}'
                })

    if enum_violations:
        print()
        print(f"{RED}❌ FAILURE: Found {len(enum_violations)} enum violations:{NC}")
        print()
        for violation in enum_violations:
            print(f"  {RED}✗{NC} {violation['field']} (line {violation['line']})")
            print(f"     → {violation['error']}")
        print()
        print(f"{RED}🚫 RULE VIOLATION: Enum Source Enforcement (ZERO TOLERANCE){NC}")
        print()
        print("📖 Fix by:")
        print("   1. Add .enumSource(\"/api/enums/xyz\") to ENUM fields")
        print("   2. Create corresponding XyzEnumResource.java in backend")
        print("   3. NEVER use hardcoded options: [{value, label}]")
        print()
        print("   See CLAUDE.md → BACKEND/FRONTEND PARITY for details")
        print()
        return 1
    else:
        print(f"{GREEN}✅ STUFE 2 PASSED: All ENUM fields have valid enumSource{NC}")
        print()

    # ========== STUFE 3: Database Constraint Check (WARNING only) ==========
    print(f"{BLUE}📋 STUFE 3: Database Constraint Check (Advisory){NC}")
    print()

    missing_constraints = []

    for field_name, metadata in schema_fields.items():
        if metadata['type'] == 'ENUM' and metadata.get('enumSource'):
            has_constraint, constraint = check_database_constraints(field_name, metadata['enumSource'])
            if not has_constraint:
                missing_constraints.append(field_name)

    if missing_constraints:
        print(f"{YELLOW}⚠️  WARNING: {len(missing_constraints)} ENUM fields without CHECK constraint:{NC}")
        print()
        for field in missing_constraints:
            print(f"  {YELLOW}⚠{NC}  {field}")
        print()
        print(f"{YELLOW}📌 RECOMMENDATION: Add CHECK constraints for data integrity{NC}")
        print()
        print("   Example migration:")
        print(f"   ALTER TABLE customers ADD CONSTRAINT chk_{missing_constraints[0]}")
        print(f"   CHECK ({missing_constraints[0]} IN ('value1', 'value2'));")
        print()
        print(f"{YELLOW}   (This is a WARNING, not blocking commit){NC}")
        print()
    else:
        print(f"{GREEN}✅ STUFE 3 INFO: Database constraints recommended{NC}")
        print()

    # ========== STUFE 4: Contact Frontend → Entity Parity (Basic Check) ==========
    print(f"{BLUE}📋 STUFE 4: Contact Parity Check (Basic - No Schema Resource Yet){NC}")
    print()

    contact_frontend_fields = extract_contact_frontend_fields()
    contact_entity_fields = extract_contact_entity_fields()

    if contact_frontend_fields and contact_entity_fields:
        print(f"  ContactEditDialog.tsx: {len(contact_frontend_fields)} fields")
        print(f"  CustomerContact.java entity: {len(contact_entity_fields)} fields")
        print()

        # Check for frontend fields missing in backend
        contact_violations = []
        for frontend_field in contact_frontend_fields:
            # Map common mismatches (camelCase vs snake_case)
            # Frontend uses isPrimary, backend might use is_primary or isPrimary
            if frontend_field not in contact_entity_fields:
                # Try alternative names
                alt_names = [
                    frontend_field,
                    frontend_field.replace('is_', ''),
                    frontend_field.replace('_', ''),
                    'is' + frontend_field.replace('is_', '').title().replace('_', ''),
                ]
                found = any(alt in contact_entity_fields for alt in alt_names)
                if not found:
                    contact_violations.append(frontend_field)

        if contact_violations:
            print(f"{RED}❌ FAILURE: Found {len(contact_violations)} Contact fields missing in backend:{NC}")
            print()
            for field in contact_violations:
                print(f"  {RED}✗{NC} {field}")
            print()
            print(f"{RED}🚫 RULE VIOLATION: Contact Frontend/Backend Parity (ZERO TOLERANCE){NC}")
            print()
            print("📖 Fix by:")
            print("   1. Add missing fields to CustomerContact.java entity")
            print("   2. Create migration to add columns to 'customer_contacts' table")
            print("   3. Update ContactMapper.java to map new fields")
            print()
            return 1
        else:
            print(f"{GREEN}✅ STUFE 4 PASSED: All Contact frontend fields exist in backend{NC}")
            print()
    else:
        print(f"{YELLOW}⚠️  INFO: Contact files not found or no fields detected{NC}")
        print()

    # ========== STUFE 5: Frontend Schema-Driven Check ==========
    print(f"{BLUE}📋 STUFE 5: Frontend Schema-Driven Architecture Check{NC}")
    print()

    is_schema_driven, schema_violations = check_frontend_schema_driven()

    if not is_schema_driven:
        print(f"{RED}❌ FAILURE: ContactEditDialog.tsx is NOT schema-driven!{NC}")
        print()
        print(f"  Violations found:")
        for violation in schema_violations:
            print(f"  {RED}✗{NC} {violation}")
        print()
        print(f"{RED}🚫 RULE VIOLATION: Frontend MUST use schema-driven rendering (ZERO TOLERANCE){NC}")
        print()
        print("📖 Required architecture:")
        print("   1. Import and use useContactSchema() hook")
        print("   2. Implement renderField() function to map FieldType → MUI component")
        print("   3. Map over contactSchema.sections.fields dynamically")
        print("   4. NO hardcoded <TextField label=\"...\"> without schema")
        print()
        print("   Example:")
        print("   const { data: schemas } = useContactSchema();")
        print("   const contactSchema = schemas?.[0];")
        print("   contactSchema.sections.map(section => (")
        print("     section.fields.map(field => renderField(field))")
        print("   ))")
        print()
        print(f"{YELLOW}   See ContactEditDialog.tsx for reference implementation{NC}")
        print()
        return 1
    else:
        print(f"{GREEN}✅ STUFE 5 PASSED: ContactEditDialog uses schema-driven rendering{NC}")
        print()
        print(f"  ✓ Uses useContactSchema() hook")
        print(f"  ✓ Has dynamic field rendering")
        print(f"  ✓ Maps over schema.sections/fields")
        print()

    # ========== SUMMARY ==========
    print(f"{BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━{NC}")
    print(f"{GREEN}✅ SUCCESS: Server-Driven Parity Check PASSED{NC}")
    print(f"{BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━{NC}")
    print()
    print(f"  Schema Fields: {len(schema_fields)}")
    print(f"  Entity Fields: {len(entity_fields)}")
    print(f"  Enum Resources: {len(enum_resources)}")
    print(f"  Ghost Fields: {len(ghost_fields)}")
    print(f"  Enum Violations: {len(enum_violations)}")
    if contact_frontend_fields and contact_entity_fields:
        print(f"  Contact Frontend Fields: {len(contact_frontend_fields)}")
        print(f"  Contact Entity Fields: {len(contact_entity_fields)}")
    print()

    return 0


if __name__ == '__main__':
    exit_code = main()
    sys.exit(exit_code)
