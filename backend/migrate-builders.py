#!/usr/bin/env python3
"""
Migrates all tests from Legacy Builder to TestDataFactory Pattern.
Issue #130: CDI-Konflikt zwischen Legacy Buildern (src/main) und neuen Buildern (src/test)
"""

import re
import sys
from pathlib import Path

# Migration mappings
IMPORT_REPLACEMENTS = {
    'import de.freshplan.test.builders.CustomerBuilder;': 'import de.freshplan.test.builders.CustomerTestDataFactory;',
    'import de.freshplan.test.builders.ContactBuilder;': 'import de.freshplan.test.builders.ContactTestDataFactory;',
    'import de.freshplan.test.builders.OpportunityBuilder;': 'import de.freshplan.test.builders.OpportunityTestDataFactory;',
    'import de.freshplan.test.builders.ContactInteractionBuilder;': '// ContactInteractionBuilder removed - create manually',
}

def migrate_imports(content: str) -> tuple[str, list[str]]:
    """Replace old builder imports with new factory imports."""
    changes = []
    for old, new in IMPORT_REPLACEMENTS.items():
        if old in content:
            content = content.replace(old, new)
            changes.append(f"Import: {old} -> {new}")

    # Add EntityManager import if ContactTestDataFactory is used
    if 'ContactTestDataFactory' in content and 'import jakarta.persistence.EntityManager;' not in content:
        # Find the import section and add EntityManager
        import_match = re.search(r'(import jakarta\.inject\.Inject;)', content)
        if import_match:
            content = content.replace(
                import_match.group(1),
                'import jakarta.inject.Inject;\nimport jakarta.persistence.EntityManager;'
            )
            changes.append("Added: import jakarta.persistence.EntityManager;")

    return content, changes

def migrate_inject_declarations(content: str) -> tuple[str, list[str]]:
    """Remove @Inject declarations for old builders, add EntityManager if needed."""
    changes = []

    # Remove old builder injections
    patterns = [
        (r'\s*@Inject\s+CustomerBuilder\s+customerBuilder;', ''),
        (r'\s*@Inject\s+ContactBuilder\s+contactBuilder;', ''),
        (r'\s*@Inject\s+OpportunityBuilder\s+opportunityBuilder;', ''),
        (r'\s*@Inject\s+ContactInteractionBuilder\s+contactInteractionBuilder;', ''),
    ]

    for pattern, replacement in patterns:
        if re.search(pattern, content):
            content = re.sub(pattern, replacement, content)
            changes.append(f"Removed injection: {pattern}")

    # Add EntityManager injection if ContactTestDataFactory is used and EntityManager not present
    if 'ContactTestDataFactory' in content and '@Inject EntityManager entityManager;' not in content:
        # Find where to insert EntityManager (after other @Inject declarations)
        inject_match = re.search(r'(@Inject\s+\w+Repository\s+\w+Repository;)', content)
        if inject_match:
            last_inject = inject_match.group(1)
            content = content.replace(
                last_inject,
                f"{last_inject}\n\n  @Inject EntityManager entityManager;"
            )
            changes.append("Added: @Inject EntityManager entityManager;")

    return content, changes

def migrate_customer_usage(content: str) -> tuple[str, list[str]]:
    """Migrate CustomerBuilder usage to CustomerTestDataFactory."""
    changes = []

    # Pattern 1: Simple builder with persist
    # OLD: customerBuilder.withCompanyName("Test").build(); customerRepository.persist(customer);
    # NEW: CustomerTestDataFactory.builder().withCompanyName("Test").buildAndPersist(customerRepository);

    # This is complex - we'll handle it case-by-case in actual code
    # For now, just document that manual review needed for complex cases

    if 'customerBuilder' in content:
        changes.append("WARNING: Manual review needed for customerBuilder usage patterns")

    return content, changes

def migrate_file(file_path: Path) -> dict:
    """Migrate a single test file."""
    try:
        content = file_path.read_text(encoding='utf-8')
        original_content = content
        all_changes = []

        # Step 1: Migrate imports
        content, changes = migrate_imports(content)
        all_changes.extend(changes)

        # Step 2: Migrate @Inject declarations
        content, changes = migrate_inject_declarations(content)
        all_changes.extend(changes)

        # Step 3: Migrate usage patterns (manual review needed)
        content, changes = migrate_customer_usage(content)
        all_changes.extend(changes)

        # Only write if changes were made
        if content != original_content:
            file_path.write_text(content, encoding='utf-8')
            return {
                'file': str(file_path),
                'status': 'migrated',
                'changes': all_changes
            }
        else:
            return {
                'file': str(file_path),
                'status': 'no_changes',
                'changes': []
            }

    except Exception as e:
        return {
            'file': str(file_path),
            'status': 'error',
            'error': str(e)
        }

def find_test_files() -> list[Path]:
    """Find all test files that use legacy builders."""
    test_dir = Path('src/test')
    files = []

    for java_file in test_dir.rglob('*.java'):
        content = java_file.read_text(encoding='utf-8')
        if any(old in content for old in IMPORT_REPLACEMENTS.keys()):
            files.append(java_file)

    return files

def main():
    print("🔍 Finding test files with legacy builder imports...")
    files = find_test_files()

    print(f"📝 Found {len(files)} files to migrate\n")

    migrated = []
    errors = []

    for file_path in files:
        result = migrate_file(file_path)

        if result['status'] == 'migrated':
            migrated.append(result)
            print(f"✅ {result['file']}")
            for change in result['changes']:
                print(f"   - {change}")
        elif result['status'] == 'error':
            errors.append(result)
            print(f"❌ {result['file']}: {result['error']}")

    print(f"\n📊 Summary:")
    print(f"   ✅ Migrated: {len(migrated)}")
    print(f"   ❌ Errors: {len(errors)}")

    if errors:
        print(f"\n⚠️  Files with errors:")
        for err in errors:
            print(f"   - {err['file']}: {err['error']}")
        return 1

    print(f"\n⚠️  IMPORTANT: Auto-migration only handles imports and injections.")
    print(f"   Manual review needed for:")
    print(f"   - CustomerBuilder usage patterns → CustomerTestDataFactory.builder()")
    print(f"   - ContactBuilder usage patterns → ContactTestDataFactory.builder()")
    print(f"   - OpportunityBuilder usage patterns → OpportunityTestDataFactory.builder()")

    return 0

if __name__ == '__main__':
    sys.exit(main())
