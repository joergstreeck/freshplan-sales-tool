#!/usr/bin/env python3
"""
Pre-Commit Hook: Enum-Rendering-Parity Check (Context-Aware V2)

ZERO TOLERANCE für RAW Enum-Rendering in Read-Views!
Backend = Single Source of Truth für ALLE Enum-Werte (nicht nur Forms!).

Problem:
  {contact.decisionLevel}  →  ❌ RAW "EXECUTIVE" statt "Geschäftsführer/Inhaber"

Lösung:
  useEnumOptions('/api/enums/decision-levels') → Label-Lookup

Context-Aware V2: Erweiterte Filter für präzise Violation Detection!

FILTER 0: Skip comments/JSDoc + render config lines
FILTER 1: Domain context check (contact/lead/customer objects only)
FILTER 2: Not JSX attributes (<Component title="..." />)
FILTER 3: Not comparisons/assignments (const x = data.field)
FILTER 4: Not state/store assignments (salutation: contact.salutation) ← NEW!
FILTER 5: Not type casts/ternaries ((value as Type) or ? : ) ← NEW!
FILTER 6: Not function parameters (getIcon(activity.type)) ← NEW!

Sprint 2.1.7.7 - Server-Driven Architecture Enforcement (Option B - Enhanced)
"""

import re
import sys
from pathlib import Path
from typing import Dict, List, Set, Tuple, Optional

# ANSI Colors
RED = '\033[0;31m'
YELLOW = '\033[1;33m'
GREEN = '\033[0;32m'
BLUE = '\033[0;34m'
NC = '\033[0m'  # No Color

# Mapping: FieldName → Enum-Endpoint (camelCase → kebab-case)
# ONLY Business-Domain Enums (excludes generic fields like 'title', 'status')
ENUM_FIELD_MAPPING = {
    'leadSource': 'lead-sources',
    'businessType': 'business-types',
    'kitchenSize': 'kitchen-sizes',
    'activityType': 'activity-types',
    'activityOutcome': 'activity-outcomes',
    'financingType': 'financing-types',
    'customerType': 'customer-types',
    'paymentTerms': 'payment-terms',
    'deliveryCondition': 'delivery-conditions',
    'legalForm': 'legal-forms',
    'expansionPlan': 'expansion-plan',
    'countryCode': 'country-codes',
    'customerStatus': 'customer-status',
    'contactRole': 'contact-roles',
    'salutation': 'salutations',
    'decisionLevel': 'decision-levels',
    'relationshipStatus': 'relationship-status',
    'decisionMakerAccess': 'decision-maker-access',
    'urgencyLevel': 'urgency-levels',
    'budgetAvailability': 'budget-availability',
    'dealSize': 'deal-sizes',
}

# Context patterns that identify Business Domain objects
DOMAIN_OBJECT_PATTERNS = [
    'contact', 'lead', 'customer', 'activity', 'opportunity',
    'location', 'branch', 'company', 'person', 'data', 'item',
    'row', 'entity', 'record', 'obj'
]

# Files to ignore (known legacy violations - will be fixed in separate tickets)
LEGACY_WHITELIST = [
    # Add files here that have known violations and will be fixed later
    # Example: 'frontend/src/features/customers/components/OldCustomerCard.tsx',
]


def print_box(title: str, lines: List[str], color: str = RED):
    """Print a colored box with a title and lines."""
    width = max(len(title), max(len(line) for line in lines)) + 4
    print(f"{color}╔{'═' * width}╗")
    print(f"║  {title.ljust(width - 2)}║")
    print(f"╠{'═' * width}╣")
    for line in lines:
        print(f"║  {line.ljust(width - 2)}║")
    print(f"╚{'═' * width}╝{NC}")


def find_tsx_files(root_dir: Path) -> List[Path]:
    """Find all .tsx and .ts files in frontend/src."""
    frontend_src = root_dir / "frontend" / "src"
    if not frontend_src.exists():
        return []

    return list(frontend_src.rglob("*.tsx")) + list(frontend_src.rglob("*.ts"))


def is_whitelisted(file_path: Path, root_dir: Path) -> bool:
    """Check if file is in legacy whitelist."""
    relative_path = str(file_path.relative_to(root_dir))
    return relative_path in LEGACY_WHITELIST


def is_domain_context(object_name: str) -> bool:
    """
    Check if object name suggests a Business Domain object.

    Examples:
      ✅ contact.decisionLevel → contact is domain object
      ✅ lead.businessType → lead is domain object
      ✅ customer.customerType → customer is domain object
      ❌ props.title → props is NOT domain object
      ❌ theme.palette → theme is NOT domain object
    """
    object_lower = object_name.lower()

    # Exact match with known domain objects
    for pattern in DOMAIN_OBJECT_PATTERNS:
        if pattern in object_lower:
            return True

    # Common non-domain patterns to exclude
    non_domain_patterns = [
        'props', 'theme', 'config', 'settings', 'options',
        'state', 'context', 'params', 'query', 'headers',
        'event', 'error', 'response', 'request', 'sx',
        'style', 'className', 'ref', 'key', 'id'
    ]

    for pattern in non_domain_patterns:
        if pattern in object_lower:
            return False

    return False


def is_jsx_attribute(line: str, field_name: str) -> bool:
    """
    Check if field is used as JSX/HTML attribute (not business data).

    Examples:
      ❌ <Component title="..." /> → attribute, not data
      ❌ <Typography title="..." /> → tooltip prop
      ✅ {contact.decisionLevel} → data rendering
    """
    # Pattern: <Component fieldName="..." />
    if re.search(rf'<\w+[^>]*\s{field_name}\s*=', line):
        return True

    # Pattern: title="..." or title={"..."}
    if re.search(rf'{field_name}\s*=\s*["\']', line):
        return True

    return False


def is_comparison_or_assignment(line: str, field_name: str) -> bool:
    """
    Check if field is used in comparison or assignment (not rendering).

    Examples:
      ❌ if (status === 'active') → comparison
      ❌ const x = data.status → assignment
      ✅ {contact.decisionLevel} → rendering
    """
    # Pattern: field === ... or field !== ...
    if re.search(rf'{field_name}\s*[!=]==', line):
        return True

    # Pattern: const/let/var x = ...field
    if re.search(rf'(const|let|var)\s+\w+\s*=.*{field_name}', line):
        return True

    # Pattern: if (...field...)
    if re.search(rf'if\s*\([^)]*{field_name}', line):
        return True

    return False


def is_state_assignment(line: str, field_name: str) -> bool:
    """
    Check if field is in useState/store/object assignment (not rendering).

    Examples:
      ❌ useState({ salutation: contact.salutation })
      ❌ setFormData({ decisionLevel: contact.decisionLevel })
      ❌ const newContact = { salutation: contact.salutation }
      ❌ salutation: contact?.salutation (optional chaining)
      ✅ {contact.decisionLevel} → rendering
    """
    # Pattern 1: Object property assignment: { field: value.field, ... }
    # This catches: salutation: contact.salutation, AND contact?.salutation (optional chaining)
    if re.search(rf'{field_name}:\s*\w+\??\.{field_name}', line):
        return True

    # Pattern 2: useState/setState with object: useState({ field: value })
    if re.search(rf'(useState|set\w+)\s*\(\s*\{{[^}}]*{field_name}:', line):
        return True

    # Pattern 3: Spread with field override: { ...contact, salutation: contact.salutation }
    if re.search(rf'\.\.\.\w+,\s*{field_name}:', line):
        return True

    return False


def is_type_cast_or_ternary(line: str, field_name: str) -> bool:
    """
    Check if field is in type cast or ternary expression (not rendering).

    Examples:
      ❌ (formData.businessType as BusinessType)
      ❌ (formData.kitchenSize as 'small' | 'medium' | 'large')
      ❌ condition ? formData.businessType : ''
      ❌ ? formData.businessType (multi-line ternary)
      ❌ businessTypeOptions?.some(opt => opt.value === formData.businessType)
      ✅ {contact.decisionLevel} → rendering
    """
    # Pattern 1: Type cast: (value.field as Type) or (value.field as 'union' | 'types')
    # Match anything after 'as' until closing paren (supports union types)
    if re.search(rf'\([^)]*{field_name}[^)]*as\s+[^)]+\)', line):
        return True

    # Pattern 2: Ternary expression: condition ? value.field : default
    # OR multi-line ternary (just the ? part without : on same line)
    if re.search(rf'\?[^:]*{field_name}', line):
        return True

    # Pattern 3: .some/.every/.find/.filter with field comparison
    if re.search(rf'\.(some|every|find|filter)\([^)]*{field_name}', line):
        return True

    return False


def is_function_parameter(line: str, field_name: str) -> bool:
    """
    Check if field is passed as function parameter (not rendering).

    Examples:
      ❌ getActivityTypeInfo(activity.activityType)
      ❌ handleClick(lead.businessType)
      ✅ {contact.decisionLevel} → rendering
    """
    # Pattern: functionName(arg1, value.field, arg3)
    # This matches: someFunc(...field...) where field is inside ()
    if re.search(rf'\w+\([^)]*\w+\.{field_name}[^)]*\)', line):
        return True

    return False


def is_render_function_config(line: str) -> bool:
    """
    Check if line is render function config (e.g., leadColumns.tsx pattern).

    Examples:
      ❌ render: (lead: Lead) => lead.businessType || '-'
      ✅ {contact.decisionLevel} → rendering
    """
    # Pattern: render: (item: Type) => item.field
    if re.search(r'render:\s*\(\w+:\s*\w+\)\s*=>', line):
        return True

    return False


def is_in_comment_or_docstring(line: str) -> bool:
    """
    Check if line is comment or JSDoc documentation.

    Examples:
      ❌ // formData.businessType
      ❌ * <Select value={formData.businessType}>
      ✅ {contact.decisionLevel} → rendering
    """
    stripped = line.strip()
    # Single-line comment
    if stripped.startswith('//'):
        return True

    # JSDoc line
    if stripped.startswith('*') or stripped.startswith('/*'):
        return True

    return False


def find_enum_field_accesses(content: str) -> Dict[str, List[Tuple[int, str, str]]]:
    """
    Find all enum field accesses in TSX/TS content (context-aware).

    Returns:
        Dict[field_name, List[(line_num, full_match, object_name)]]

    Examples:
      ✅ {contact.decisionLevel} → ('decisionLevel', [(109, '{contact.decisionLevel}', 'contact')])
      ❌ <Component title="..." /> → FILTERED OUT (JSX attribute)
      ❌ if (status === 'active') → FILTERED OUT (comparison)
      ❌ salutation: contact.salutation → FILTERED OUT (state assignment)
      ❌ (formData.businessType as Type) → FILTERED OUT (type cast)
      ❌ getIcon(activity.activityType) → FILTERED OUT (function parameter)
      ❌ render: (lead) => lead.businessType → FILTERED OUT (config function)
      ❌ * <Select value={formData.businessType}> → FILTERED OUT (JSDoc comment)
    """
    found_fields: Dict[str, List[Tuple[int, str, str]]] = {}

    lines = content.split('\n')

    for line_num, line in enumerate(lines, 1):
        # FILTER 0: Skip comments and JSDoc
        if is_in_comment_or_docstring(line):
            continue

        # FILTER 0.5: Skip render function config lines
        if is_render_function_config(line):
            continue

        # Pattern: {objectName.fieldName} or objectName.fieldName
        pattern = r'\{?\s*(\w+)\??\.(\w+)\s*\}?'

        for match in re.finditer(pattern, line):
            object_name = match.group(1)
            field_name = match.group(2)
            full_match = match.group(0)

            # Check if it's a known enum field
            if field_name not in ENUM_FIELD_MAPPING:
                continue

            # FILTER 1: Must be domain context
            if not is_domain_context(object_name):
                continue

            # FILTER 2: Not JSX attribute
            if is_jsx_attribute(line, field_name):
                continue

            # FILTER 3: Not comparison/assignment
            if is_comparison_or_assignment(line, field_name):
                continue

            # FILTER 4: Not state/store assignment (NEW!)
            if is_state_assignment(line, field_name):
                continue

            # FILTER 5: Not type cast or ternary (NEW!)
            if is_type_cast_or_ternary(line, field_name):
                continue

            # FILTER 6: Not function parameter (NEW!)
            if is_function_parameter(line, field_name):
                continue

            # Valid enum field access found!
            if field_name not in found_fields:
                found_fields[field_name] = []

            found_fields[field_name].append((line_num, full_match, object_name))

    return found_fields


def has_label_lookup(content: str, field_name: str) -> bool:
    """
    Check if useEnumOptions() is used for this field with proper label lookup.

    Valid patterns:
    - useEnumOptions('/api/enums/decision-levels')
    - useEnumOptions(`/api/enums/decision-levels`)
    - decisionLevelLabels[contact.decisionLevel]
    """
    enum_endpoint = ENUM_FIELD_MAPPING.get(field_name)
    if not enum_endpoint:
        return False

    # Check for useEnumOptions with correct endpoint
    use_enum_options_pattern = rf"useEnumOptions\(['\"`]/api/enums/{enum_endpoint}['\"`]\)"
    if re.search(use_enum_options_pattern, content):
        return True

    # Check for label lookup (e.g., decisionLevelLabels[contact.decisionLevel])
    label_var_name = f"{field_name}Labels"
    label_lookup_pattern = rf"{label_var_name}\[.+?\.{field_name}\]"
    if re.search(label_lookup_pattern, content):
        return True

    return False


def check_file(file_path: Path) -> Dict[str, List[Tuple[int, str, str]]]:
    """
    Check a single file for enum-rendering violations (context-aware).

    Returns:
        Dict[field_name, List[(line_num, full_match, object_name)]] for violations
    """
    try:
        content = file_path.read_text(encoding='utf-8')
    except UnicodeDecodeError:
        return {}  # Skip binary files

    violations: Dict[str, List[Tuple[int, str, str]]] = {}

    # Find all enum field accesses (context-aware)
    enum_fields = find_enum_field_accesses(content)

    for field_name, occurrences in enum_fields.items():
        # Check if label lookup exists
        if not has_label_lookup(content, field_name):
            # This is a violation!
            violations[field_name] = occurrences

    return violations


def main():
    """Run Enum-Rendering-Parity check (context-aware)."""
    root_dir = Path(__file__).parent.parent
    tsx_files = find_tsx_files(root_dir)

    if not tsx_files:
        print(f"{YELLOW}⚠️  No TSX/TS files found to check{NC}")
        return 0

    print(f"\n{BLUE}🔍 Checking Enum-Rendering-Parity (Context-Aware V2)...{NC}")
    print(f"   Scanning {len(tsx_files)} files for RAW enum rendering")
    print(f"   Advanced Filters: 7 context checks (state assignments, type casts, function params, etc.)\n")

    all_violations: Dict[str, Dict[str, List[Tuple[int, str, str]]]] = {}

    for file_path in tsx_files:
        if is_whitelisted(file_path, root_dir):
            continue

        violations = check_file(file_path)
        if violations:
            relative_path = str(file_path.relative_to(root_dir))
            all_violations[relative_path] = violations

    if not all_violations:
        print(f"{GREEN}✅ Enum-Rendering-Parity Check V2: PASSED{NC}")
        print(f"   All enum fields use proper label lookup via useEnumOptions()")
        print(f"   Advanced Context-Aware Analysis: 0 violations detected")
        print(f"   Filters applied: 7 context checks (state/store/type-casts/params/etc.)\n")
        return 0

    # Count total violations
    total_violations = sum(len(v) for file_violations in all_violations.values() for v in file_violations.values())

    # Print violations
    print()
    print_box(
        "❌ ENUM-RENDERING-PARITY VIOLATIONS DETECTED",
        [
            "Backend = Single Source of Truth für ALLE Enum-Werte!",
            "RAW Enum-Rendering ist VERBOTEN (ZERO TOLERANCE).",
            "",
            f"Found {len(all_violations)} files with {total_violations} violations:",
        ],
        RED
    )
    print()

    for file_path, field_violations in sorted(all_violations.items()):
        print(f"{RED}📄 {file_path}{NC}")
        for field_name, occurrences in field_violations.items():
            enum_endpoint = ENUM_FIELD_MAPPING[field_name]
            for line_num, full_match, object_name in occurrences:
                print(f"   Line {line_num}: {YELLOW}{object_name}.{field_name}{NC} → {full_match}")
                print(f"             Missing: useEnumOptions('/api/enums/{enum_endpoint}')")
        print()

    print_box(
        "🛠️  HOW TO FIX",
        [
            "1. Add useEnumOptions() Hook:",
            f"   {GREEN}const {{ data: decisionLevelOptions }} = useEnumOptions('/api/enums/decision-levels');{NC}",
            "",
            "2. Create Label Lookup Map:",
            f"   {GREEN}const decisionLevelLabels = useMemo(() => {{{NC}",
            f"   {GREEN}  return decisionLevelOptions?.reduce((acc, item) => {{{NC}",
            f"   {GREEN}    acc[item.value] = item.label; return acc;{NC}",
            f"   {GREEN}  }}, {{}});{NC}",
            f"   {GREEN}}}, [decisionLevelOptions]);{NC}",
            "",
            "3. Replace RAW Enum with Label Lookup:",
            f"   {RED}❌ {{contact.decisionLevel}}{NC}",
            f"   {GREEN}✅ {{decisionLevelLabels[contact.decisionLevel] || contact.decisionLevel}}{NC}",
            "",
            "Reference: docs/planung/SERVER_DRIVEN_ENUM_MAPPING.md",
        ],
        YELLOW
    )
    print()

    return 1


if __name__ == "__main__":
    sys.exit(main())
