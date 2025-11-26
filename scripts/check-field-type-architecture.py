#!/usr/bin/env python3
"""
Server-Driven Field Type Architecture Guard

Prüft korrekte Verwendung der Server-Driven Field Type Architecture:
1. field.type statt field.fieldType
2. UPPERCASE cases ('TEXT', 'ENUM') statt lowercase ('text', 'enum')

Sprint 2.1.7.7 - Fix für Dashboard/Wizard UI Architecture
"""

import re
import sys
from pathlib import Path
from typing import List, Tuple

# ANSI Colors
RED = '\033[0;31m'
YELLOW = '\033[1;33m'
GREEN = '\033[0;32m'
BLUE = '\033[0;34m'
NC = '\033[0m'  # No Color

class FieldTypeArchitectureViolation:
    def __init__(self, file_path: str, line_num: int, line_content: str, violation_type: str, message: str):
        self.file_path = file_path
        self.line_num = line_num
        self.line_content = line_content
        self.violation_type = violation_type
        self.message = message

def check_file(file_path: Path) -> List[FieldTypeArchitectureViolation]:
    """Prüft eine Datei auf Field Type Architecture Violations."""
    violations = []

    try:
        content = file_path.read_text(encoding='utf-8')
        lines = content.splitlines()

        for i, line in enumerate(lines, start=1):
            # VIOLATION 1: Verwendung von field.fieldType (deprecated)
            if re.search(r'\b(\w+)\.fieldType\b', line):
                # Ausnahme: TypeScript Type Definitions sind erlaubt
                if 'FieldDefinition' not in line and 'interface' not in line and 'type' not in line:
                    violations.append(FieldTypeArchitectureViolation(
                        file_path=str(file_path),
                        line_num=i,
                        line_content=line.strip(),
                        violation_type='FIELD_TYPE_PROPERTY',
                        message=f"❌ Verwende field.type statt field.fieldType (Server-Driven Architecture)"
                    ))

            # VIOLATION 2: Lowercase field type cases in switch/case statements
            # Matches: case 'text': | case 'enum': | case "text": | case "enum":
            lowercase_case_match = re.search(r"case\s+['\"](\w+)['\"]:\s*$", line)
            if lowercase_case_match:
                case_value = lowercase_case_match.group(1)
                # Check if it's a field type (common ones)
                if case_value in ['text', 'email', 'number', 'select', 'multiselect', 'textarea', 'enum', 'group', 'array', 'decimal', 'currency']:
                    violations.append(FieldTypeArchitectureViolation(
                        file_path=str(file_path),
                        line_num=i,
                        line_content=line.strip(),
                        violation_type='LOWERCASE_FIELD_TYPE',
                        message=f"❌ Field Type muss UPPERCASE sein: '{case_value}' → '{case_value.upper()}'"
                    ))

            # VIOLATION 3: Lowercase field types in includes() array checks
            # Matches: ['text', 'email'].includes(field.type)
            includes_match = re.search(r"\[['\"](\w+)['\"](?:,\s*['\"](\w+)['\"])*\]\.includes\(", line)
            if includes_match:
                # Extract all quoted strings
                quoted_strings = re.findall(r"['\"](\w+)['\"]", line)
                for quoted_str in quoted_strings:
                    if quoted_str in ['text', 'email', 'number', 'select', 'multiselect', 'textarea', 'enum', 'group', 'array', 'decimal', 'currency']:
                        violations.append(FieldTypeArchitectureViolation(
                            file_path=str(file_path),
                            line_num=i,
                            line_content=line.strip(),
                            violation_type='LOWERCASE_FIELD_TYPE_ARRAY',
                            message=f"❌ Field Type in Array muss UPPERCASE sein: '{quoted_str}' → '{quoted_str.upper()}'"
                        ))
                        break  # Only report once per line

    except Exception as e:
        print(f"{YELLOW}⚠️  Warnung: Konnte Datei {file_path} nicht lesen: {e}{NC}")

    return violations

def main() -> int:
    """Hauptfunktion des Checks."""

    # Dateien zum Prüfen
    files_to_check = [
        'frontend/src/features/customers/components/fields/DynamicFieldRenderer.tsx',
        'frontend/src/features/customers/components/fields/fieldTypes/GroupField.tsx',
        'frontend/src/features/customers/components/fields/fieldTypes/ArrayField.tsx',
    ]

    # Additional pattern-based search for field rendering files
    frontend_path = Path('frontend/src')
    if frontend_path.exists():
        # Search for all field-related files
        field_files = list(frontend_path.rglob('*Field*.tsx'))
        field_files += list(frontend_path.rglob('*Field*.ts'))

        for file_path in field_files:
            rel_path = str(file_path)
            if rel_path not in files_to_check:
                files_to_check.append(rel_path)

    all_violations = []

    for file_path_str in files_to_check:
        file_path = Path(file_path_str)
        if not file_path.exists():
            continue

        violations = check_file(file_path)
        all_violations.extend(violations)

    if not all_violations:
        print(f"{GREEN}✅ Keine Field Type Architecture Violations gefunden{NC}")
        return 0

    # Report violations
    print(f"\n{RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━{NC}")
    print(f"{RED}🚫 Field Type Architecture Violations gefunden!{NC}")
    print(f"{RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━{NC}\n")

    # Group by file
    violations_by_file = {}
    for violation in all_violations:
        if violation.file_path not in violations_by_file:
            violations_by_file[violation.file_path] = []
        violations_by_file[violation.file_path].append(violation)

    for file_path, violations in violations_by_file.items():
        print(f"\n{BLUE}Datei: {file_path}{NC}")
        print(f"{BLUE}{'─' * 60}{NC}")

        for violation in violations:
            print(f"  Zeile {violation.line_num}: {violation.message}")
            print(f"  Code: {YELLOW}{violation.line_content}{NC}\n")

    print(f"\n{RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━{NC}")
    print(f"{RED}Insgesamt: {len(all_violations)} Violations{NC}")
    print(f"{RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━{NC}\n")

    print(f"{YELLOW}Korrekte Verwendung:{NC}")
    print(f"  {GREEN}✅ field.type{NC}               (nicht field.fieldType)")
    print(f"  {GREEN}✅ case 'TEXT':{NC}             (nicht case 'text':)")
    print(f"  {GREEN}✅ case 'ENUM':{NC}             (nicht case 'enum':)")
    print(f"  {GREEN}✅ ['TEXT', 'EMAIL']{NC}        (nicht ['text', 'email'])")
    print()
    print(f"{YELLOW}Referenz:{NC}")
    print(f"  • frontend/src/types/customer-schema.ts → FieldType (UPPERCASE)")
    print(f"  • Frontend nutzt Server-Driven Schema: field.type ist die Property")
    print(f"  • Backend sendet UPPERCASE values: 'TEXT', 'ENUM', 'NUMBER', etc.")
    print()

    return 1

if __name__ == '__main__':
    sys.exit(main())
