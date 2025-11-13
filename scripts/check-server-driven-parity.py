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
FRONTEND_FEATURES_DIR = PROJECT_ROOT / "frontend/src/features"

# Whitelist: Dialoge/Forms die NICHT schema-driven sein müssen
# (einfache Dialoge ohne Entity-Daten: Confirm, Delete, Filter, etc.)
SCHEMA_DRIVEN_WHITELIST = {
    'DeleteLeadDialog.tsx',           # Nur Bestätigung
    'StopTheClockDialog.tsx',         # Nur Bestätigung
    'ConvertToCustomerDialog.tsx',    # Nur Bestätigung + einfache Auswahl
    'AddFirstContactDialog.tsx',      # Einfacher First-Contact-Dialog (4 TextFields, keine Enums)
    'AdvancedFilterDialog.tsx',       # Filter UI, keine Entity-Daten
    'CalculatorForm.tsx',             # Calculator-spezifisch, keine Server-Daten
    'EngagementScoreForm.tsx',        # Score-Formular, kein Entity
    'PainScoreForm.tsx',              # Score-Formular, kein Entity
    'RevenueScoreForm.tsx',           # Score-Formular, kein Entity
}


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

    Handles camelCase → snake_case conversion (e.g., legalForm → legal_form)
    to match database column names in SQL migrations.

    Returns: (has_constraint: bool, constraint_line: str)
    """
    if not MIGRATIONS_DIR.exists():
        return False, ""

    # Convert camelCase to snake_case (e.g., legalForm → legal_form)
    snake_case_name = re.sub(r'(?<!^)(?=[A-Z])', '_', field_name).lower()

    # Try both camelCase and snake_case patterns
    # (Schema uses camelCase, DB uses snake_case)
    patterns = [
        rf'CHECK\s*\(\s*{field_name}\s+IN\s*\(',       # camelCase (rare)
        rf'CHECK\s*\(\s*{snake_case_name}\s+IN\s*\(',  # snake_case (common)
    ]

    for migration in sorted(MIGRATIONS_DIR.glob("V*.sql")):
        content = migration.read_text()
        for pattern in patterns:
            if re.search(pattern, content, re.IGNORECASE):
                # Extract constraint line
                for line in content.split('\n'):
                    if re.search(pattern, line, re.IGNORECASE):
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


def find_all_dialogs_and_forms() -> List[Path]:
    """
    Find all Dialog and Form files in frontend/src/features

    Returns: List of Path objects for *Dialog.tsx and *Form.tsx files
    """
    if not FRONTEND_FEATURES_DIR.exists():
        return []

    dialogs = list(FRONTEND_FEATURES_DIR.glob('**/*Dialog.tsx'))
    forms = list(FRONTEND_FEATURES_DIR.glob('**/*Form.tsx'))

    return sorted(dialogs + forms)


def check_file_is_schema_driven(file_path: Path) -> Tuple[bool, List[str], str]:
    """
    Check if a Dialog/Form file uses schema-driven rendering

    Schema-driven indicators:
    - ✅ Uses use*Schema() hook (useContactSchema, useCustomerSchema, etc.)
    - ✅ Has renderField() or similar dynamic rendering function
    - ✅ Maps over schema.sections or schema.fields

    Hardcoded indicators (violations):
    - ❌ Hardcoded <TextField label="..." /> without dynamic schema
    - ❌ Hardcoded field keys without schema mapping

    Returns: (is_schema_driven, violations, status_emoji)
    """
    if not file_path.exists():
        return True, [], '⚠️'

    content = file_path.read_text()
    violations = []

    # Check 1: Does it use any use*Schema() hook OR useEnumOptions()?
    schema_hooks = [
        'useContactSchema()',
        'useCustomerSchema()',
        'useLeadSchema()',
        'useOpportunitySchema()',
        'useUserSchema()',
    ]
    uses_schema_hook = any(hook in content for hook in schema_hooks)

    # Check 1b: Does it use useEnumOptions() for server-driven enums?
    # (Sprint 2.1.7.7 - Schema-Driven Forms Migration)
    # Dialogs that only need enum options (not full schema) are also compliant
    uses_enum_options = 'useEnumOptions(' in content

    # Schema-driven if EITHER pattern is present
    uses_server_driven = uses_schema_hook or uses_enum_options

    # Check 2: Does it have dynamic rendering?
    has_dynamic_rendering = bool(
        re.search(r'\.map\(\s*\(?(?:section|field)', content) or
        re.search(r'renderField\s*\(', content) or
        re.search(r'(contact|customer|lead)Schema.*\.sections', content)
    )

    # Check 3: Look for hardcoded field labels (strong indicator of hardcoded UI)
    # Skip comments to avoid false positives
    lines_without_comments = [
        line.split('//')[0] for line in content.split('\n')
        if not line.strip().startswith('//')
    ]
    content_without_comments = '\n'.join(lines_without_comments)

    hardcoded_patterns = [
        r'<TextField\s+[^>]*label="[^"]{3,}"[^>]*(?!{)',  # TextField with static label
        r'<FormControl[^>]*>\s*<InputLabel[^>]*>[^<]{3,}</InputLabel>',  # Static InputLabel
    ]

    hardcoded_count = 0
    for pattern in hardcoded_patterns:
        matches = re.findall(pattern, content_without_comments)
        hardcoded_count += len(matches)

    # Determine if schema-driven
    # Two valid patterns:
    # 1. Full Schema-Driven: useSchema() + dynamic rendering (.map over sections/fields)
    # 2. Enum-Only Schema-Driven: useEnumOptions() for server-driven enum selects
    is_schema_driven = (uses_schema_hook and has_dynamic_rendering) or uses_enum_options

    # Build violation messages
    if not uses_server_driven and hardcoded_count > 3:
        # Only complain if there are significant hardcoded fields (3+ fields)
        violations.append(f"Missing use*Schema() or useEnumOptions() hook, found {hardcoded_count} hardcoded fields")

    if uses_schema_hook and not has_dynamic_rendering and not uses_enum_options:
        violations.append("Has schema hook but NO dynamic rendering (.map over sections/fields)")

    if hardcoded_count > 5 and not is_schema_driven:
        violations.append(f"Found {hardcoded_count} hardcoded field labels (should use schema)")

    # Determine status emoji
    if is_schema_driven:
        status_emoji = '✅'
    elif file_path.name in SCHEMA_DRIVEN_WHITELIST:
        status_emoji = '⚪'  # Whitelisted (simple dialog, no schema needed)
    elif hardcoded_count > 5:
        status_emoji = '❌'  # Critical: Many hardcoded fields
    elif hardcoded_count > 0:
        status_emoji = '⚠️'  # Warning: Some hardcoded fields
    else:
        status_emoji = '✅'  # OK: No violations

    return is_schema_driven, violations, status_emoji


def scan_all_frontend_components() -> Tuple[bool, Dict[str, List]]:
    """
    Scan ALL Dialog/Form files in frontend for schema-driven architecture

    Returns: (all_passed, results_by_status)
    Where results_by_status = {
        'schema_driven': [...],
        'whitelisted': [...],
        'warnings': [...],
        'violations': [...]
    }
    """
    all_files = find_all_dialogs_and_forms()

    results = {
        'schema_driven': [],
        'whitelisted': [],
        'warnings': [],
        'violations': []
    }

    for file_path in all_files:
        is_schema_driven, violations, status = check_file_is_schema_driven(file_path)
        relative_path = file_path.relative_to(PROJECT_ROOT)

        file_info = {
            'path': relative_path,
            'name': file_path.name,
            'violations': violations
        }

        # Categorize by status
        if status == '✅':
            results['schema_driven'].append(file_info)
        elif status == '⚪':
            results['whitelisted'].append(file_info)
        elif status == '⚠️':
            results['warnings'].append(file_info)
        elif status == '❌':
            results['violations'].append(file_info)

    # All passed if NO critical violations (❌)
    all_passed = len(results['violations']) == 0

    return all_passed, results


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

    # ========== STUFE 5: Frontend Schema-Driven Architecture Scan (ALL Files) ==========
    print(f"{BLUE}📋 STUFE 5: Frontend Schema-Driven Architecture Scan{NC}")
    print(f"{BLUE}   Scanning ALL Dialog/Form files in frontend/src/features{NC}")
    print()

    all_passed, results = scan_all_frontend_components()

    total_files = (
        len(results['schema_driven']) +
        len(results['whitelisted']) +
        len(results['warnings']) +
        len(results['violations'])
    )

    print(f"  Found {total_files} Dialog/Form files")
    print()

    # Show schema-driven files (✅)
    if results['schema_driven']:
        print(f"{GREEN}✅ Schema-Driven ({len(results['schema_driven'])} files):{NC}")
        for file_info in results['schema_driven']:
            print(f"  {GREEN}✓{NC} {file_info['name']}")
        print()

    # Show whitelisted files (⚪)
    if results['whitelisted']:
        print(f"{BLUE}⚪ Whitelisted - Simple Dialogs ({len(results['whitelisted'])} files):{NC}")
        for file_info in results['whitelisted']:
            print(f"  {BLUE}○{NC} {file_info['name']}")
        print()

    # Show warnings (⚠️)
    if results['warnings']:
        print(f"{YELLOW}⚠️  Warnings - Some Hardcoded Fields ({len(results['warnings'])} files):{NC}")
        for file_info in results['warnings']:
            print(f"  {YELLOW}⚠{NC} {file_info['name']}")
            if file_info['violations']:
                for v in file_info['violations']:
                    print(f"     → {v}")
        print()

    # Show violations (❌) - BLOCKING
    if results['violations']:
        print(f"{RED}❌ VIOLATIONS - Critical: Many Hardcoded Fields ({len(results['violations'])} files):{NC}")
        print()
        for file_info in results['violations']:
            print(f"  {RED}✗{NC} {file_info['path']}")
            for v in file_info['violations']:
                print(f"     → {v}")
        print()
        print(f"{RED}🚫 RULE VIOLATION: Frontend MUST use schema-driven rendering (ZERO TOLERANCE){NC}")
        print()
        print("📖 Required architecture:")
        print("   1. Import and use use*Schema() hook (useContactSchema, useCustomerSchema, etc.)")
        print("   2. Implement renderField() function to map FieldType → MUI component")
        print("   3. Map over schema.sections.fields dynamically")
        print("   4. NO hardcoded <TextField label=\"...\"> for entity fields")
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
        print(f"{GREEN}✅ STUFE 5 PASSED: All scanned files follow schema-driven or whitelisted patterns{NC}")
        print()

    # ========== STUFE 6: API REQUEST BODY → DTO PARITY (UNIVERSAL) ==========
    print(f"{BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━{NC}")
    print(f"{BLUE}PRÜFUNG 6: API Request Body → Backend DTO Parity (Universal){NC}")
    print(f"{BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━{NC}")
    print()

    # ========== CONFIGURATION: Dialog → DTO Mappings ==========
    # Add new dialogs here to automatically validate them
    DIALOG_DTO_MAPPINGS = {
        'CreateBranchDialog.tsx': {
            'dto_name': 'CreateCustomerRequest',
            'payload_pattern': r'branchData:\s*\{([^}]+)\}',
            'allowed_fields': {'companyName', 'status', 'customerType'},
            'path': 'frontend/src/features/customers/components/detail/CreateBranchDialog.tsx'
        },
        # Easy to extend with more dialogs:
        # 'LeadCreateDialog.tsx': {
        #     'dto_name': 'CreateLeadRequest',
        #     'payload_pattern': r'createLead\s*\(\s*\{([^}]+)\}',
        #     'allowed_fields': {'name', 'email', 'phone'},
        #     'path': 'frontend/src/features/leads/LeadCreateDialog.tsx'
        # },
    }

    # Validate all configured dialogs
    has_errors = False
    dialogs_checked = 0

    for dialog_name, config in DIALOG_DTO_MAPPINGS.items():
        dialog_path = PROJECT_ROOT / config['path']

        print(f"Checking {dialog_name} → {config['dto_name']} DTO parity...")

        if not dialog_path.exists():
            print(f"{YELLOW}⚠️  {dialog_name} not found - skipping{NC}")
            print()
            continue

        dialogs_checked += 1
        dialog_content = dialog_path.read_text()

        # Extract payload fields from dialog
        match = re.search(config['payload_pattern'], dialog_content, re.DOTALL)

        if not match:
            print(f"{RED}❌ STUFE 6 FAILED: Could not find payload in {dialog_name}{NC}")
            print(f"   Expected pattern: {config['payload_pattern']}")
            print()
            has_errors = True
            continue

        payload_block = match.group(1)

        # Extract field names (e.g., "companyName:", "status:", etc.)
        field_pattern = r'(\w+):'
        found_fields = set(re.findall(field_pattern, payload_block))

        # Remove common non-field keys (like comments, method calls)
        found_fields = {f for f in found_fields if not f.startswith('//') and f not in {'trim'}}

        # Check for violations
        invalid_fields = found_fields - config['allowed_fields']
        missing_fields = config['allowed_fields'] - found_fields

        if invalid_fields:
            print(f"{RED}❌ STUFE 6 FAILED: {dialog_name} sends fields NOT in {config['dto_name']}{NC}")
            print()
            print(f"  File: {dialog_path}")
            print()
            print(f"  {RED}Invalid Fields (Backend rejects these):{NC}")
            for field in sorted(invalid_fields):
                print(f"    ✗ {field}")
            print()
            print(f"  {GREEN}Allowed Fields ({config['dto_name']}):{NC}")
            for field in sorted(config['allowed_fields']):
                print(f"    ✓ {field}")
            print()
            print(f"{RED}🚫 RULE VIOLATION: Frontend Request Body MUST match Backend DTO (ZERO TOLERANCE){NC}")
            print()
            print("📖 Fix:")
            print(f"   1. Remove invalid fields from {dialog_name} state")
            print("   2. Remove invalid fields from payload object")
            print("   3. Remove corresponding UI components (TextField)")
            print("   4. Update validation logic")
            print()
            print(f"{YELLOW}   Backend DTO: backend/src/main/java/.../dto/{config['dto_name']}.java{NC}")
            print()
            has_errors = True
        elif missing_fields:
            print(f"{YELLOW}⚠️  Warning: Some required fields might be missing{NC}")
            print(f"   Missing: {', '.join(sorted(missing_fields))}")
            print()

        if not invalid_fields:
            print(f"{GREEN}✅ {dialog_name} → {config['dto_name']} parity OK{NC}")
            print(f"   Found fields: {', '.join(sorted(found_fields))}")
            print(f"   All fields are valid for {config['dto_name']} DTO")
            print()

    # Summary
    if dialogs_checked == 0:
        print(f"{YELLOW}⚠️  No dialogs found to validate - STUFE 6 skipped{NC}")
        print()
    elif has_errors:
        print(f"{RED}❌ STUFE 6 FAILED: {dialogs_checked} dialog(s) checked, errors found{NC}")
        print()
        return 1
    else:
        print(f"{GREEN}✅ STUFE 6 PASSED: All {dialogs_checked} dialog(s) validated successfully{NC}")
        print()

    # ========== SUMMARY ==========
    print(f"{BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━{NC}")
    print(f"{GREEN}✅ SUCCESS: Server-Driven Parity Check PASSED{NC}")
    print(f"{BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━{NC}")
    print()
    print(f"  📊 Customer Schema:")
    print(f"     Schema Fields: {len(schema_fields)}")
    print(f"     Entity Fields: {len(entity_fields)}")
    print(f"     Enum Resources: {len(enum_resources)}")
    print()
    if contact_frontend_fields and contact_entity_fields:
        print(f"  📊 Contact Parity:")
        print(f"     Frontend Fields: {len(contact_frontend_fields)}")
        print(f"     Entity Fields: {len(contact_entity_fields)}")
        print()
    print(f"  📊 Frontend Code Scan:")
    print(f"     Total Files Scanned: {total_files}")
    print(f"     ✅ Schema-Driven: {len(results['schema_driven'])}")
    print(f"     ⚪ Whitelisted (Simple): {len(results['whitelisted'])}")
    print(f"     ⚠️  Warnings: {len(results['warnings'])}")
    print(f"     ❌ Critical Violations: {len(results['violations'])}")
    print()

    return 0


if __name__ == '__main__':
    exit_code = main()
    sys.exit(exit_code)
