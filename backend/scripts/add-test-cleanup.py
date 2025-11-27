#!/usr/bin/env python3
"""
Adds @AfterEach cleanup methods to test files that are missing them.
Sprint 2.1.7.7: Test Cleanup - Whitelist Removal
"""

import sys
from pathlib import Path

# Test files to fix with their cleanup logic
test_files = {
    "src/test/java/de/freshplan/domain/opportunity/service/OpportunityServiceConvertToCustomerTest.java": {
        "cleanup_sql": [
            "DELETE FROM opportunity_activities WHERE opportunity_id IN (SELECT id FROM opportunities WHERE test_marker LIKE 'TEST-%')",
            "DELETE FROM opportunities WHERE test_marker LIKE 'TEST-%'",
            "DELETE FROM customer_contacts WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')",
            "DELETE FROM customer_timeline_events WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')",
            "DELETE FROM customers WHERE customer_number LIKE 'TEST-%'",
        ]
    },
    "src/test/java/de/freshplan/domain/opportunity/service/OpportunityServiceCreateForCustomerTest.java": {
        "cleanup_sql": [
            "DELETE FROM opportunity_activities WHERE opportunity_id IN (SELECT id FROM opportunities WHERE test_marker LIKE 'TEST-%')",
            "DELETE FROM opportunities WHERE test_marker LIKE 'TEST-%'",
            "DELETE FROM customer_contacts WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')",
            "DELETE FROM customer_timeline_events WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')",
            "DELETE FROM customers WHERE customer_number LIKE 'TEST-%'",
        ]
    },
    "src/test/java/de/freshplan/domain/opportunity/service/OpportunityServiceCreateFromLeadTest.java": {
        "cleanup_sql": [
            "DELETE FROM opportunity_activities WHERE opportunity_id IN (SELECT id FROM opportunities WHERE test_marker LIKE 'TEST-%')",
            "DELETE FROM opportunities WHERE test_marker LIKE 'TEST-%'",
        ]
    },
    "src/test/java/de/freshplan/domain/opportunity/service/OpportunityServiceFindByCustomerIdTest.java": {
        "cleanup_sql": [
            "DELETE FROM opportunity_activities WHERE opportunity_id IN (SELECT id FROM opportunities WHERE test_marker LIKE 'TEST-%')",
            "DELETE FROM opportunities WHERE test_marker LIKE 'TEST-%'",
            "DELETE FROM customer_contacts WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')",
            "DELETE FROM customer_timeline_events WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')",
            "DELETE FROM customers WHERE customer_number LIKE 'TEST-%'",
        ]
    },
    "src/test/java/de/freshplan/domain/opportunity/service/OpportunityServiceStageTransitionTest.java": {
        "cleanup_sql": [
            "DELETE FROM opportunity_activities WHERE opportunity_id IN (SELECT id FROM opportunities WHERE test_marker LIKE 'TEST-%')",
            "DELETE FROM opportunities WHERE test_marker LIKE 'TEST-%'",
            "DELETE FROM customer_contacts WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')",
            "DELETE FROM customer_timeline_events WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')",
            "DELETE FROM customers WHERE customer_number LIKE 'TEST-%'",
            "DELETE FROM users WHERE username LIKE 'testuser_%'",
        ]
    },
    "src/test/java/de/freshplan/modules/leads/service/LeadConvertServiceTest.java": {
        "cleanup_sql": [
            "DELETE FROM opportunity_activities WHERE opportunity_id IN (SELECT id FROM opportunities WHERE test_marker LIKE 'TEST-%')",
            "DELETE FROM opportunities WHERE test_marker LIKE 'TEST-%'",
            "DELETE FROM customer_contacts WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')",
            "DELETE FROM customer_timeline_events WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')",
            "DELETE FROM customers WHERE customer_number LIKE 'TEST-%'",
        ]
    },
    "src/test/java/de/freshplan/modules/xentral/repository/XentralSettingsRepositoryTest.java": {
        "cleanup_sql": [
            "DELETE FROM xentral_settings WHERE test_marker LIKE 'TEST-%'",
        ]
    },
}


def add_cleanup_to_file(file_path: Path, cleanup_config: dict) -> bool:
    """Adds @AfterEach cleanup to a test file if it doesn't exist."""

    if not file_path.exists():
        print(f"⚠️  File not found: {file_path}")
        return False

    content = file_path.read_text()

    # Check if cleanup already exists
    if "@AfterEach" in content and "void cleanup()" in content:
        print(f"✅ {file_path.name} - cleanup already exists")
        return True

    # Add imports if missing
    imports_to_add = []
    if "import jakarta.transaction.Transactional;" not in content:
        imports_to_add.append("import jakarta.transaction.Transactional;")
    if "import org.junit.jupiter.api.AfterEach;" not in content:
        imports_to_add.append("import org.junit.jupiter.api.AfterEach;")

    if imports_to_add:
        # Find the last import statement
        import_lines = [i for i, line in enumerate(content.split("\n")) if line.startswith("import ")]
        if import_lines:
            lines = content.split("\n")
            last_import_line = max(import_lines)
            for imp in imports_to_add:
                lines.insert(last_import_line + 1, imp)
            content = "\n".join(lines)

    # Add EntityManager inject if missing
    if "@Inject jakarta.persistence.EntityManager em;" not in content:
        # Find the class body start (after class declaration)
        class_match = content.find("class ")
        if class_match != -1:
            # Find the opening brace of the class
            brace_pos = content.find("{", class_match)
            if brace_pos != -1:
                # Find the first @Inject line after class declaration
                inject_pos = content.find("@Inject", brace_pos)
                if inject_pos != -1:
                    # Find the end of that line
                    line_end = content.find("\n", inject_pos)
                    if line_end != -1:
                        # Insert EntityManager inject after the first @Inject
                        content = (
                            content[:line_end + 1] +
                            "\n  @Inject jakarta.persistence.EntityManager em;\n" +
                            content[line_end + 1:]
                        )

    # Generate cleanup method
    cleanup_sql_statements = cleanup_config["cleanup_sql"]
    cleanup_lines = []
    cleanup_lines.append("  @AfterEach")
    cleanup_lines.append("  @Transactional")
    cleanup_lines.append("  void cleanup() {")
    cleanup_lines.append("    // Delete test data using pattern matching")
    for sql in cleanup_sql_statements:
        cleanup_lines.append(f'    em.createNativeQuery("{sql}").executeUpdate();')
    cleanup_lines.append("  }")
    cleanup_lines.append("")

    cleanup_method = "\n".join(cleanup_lines)

    # Find where to insert cleanup (after field declarations, before first test method)
    test_method_pos = content.find("@Test")
    if test_method_pos != -1:
        # Insert before @Test
        content = content[:test_method_pos] + cleanup_method + "\n  " + content[test_method_pos:]

    file_path.write_text(content)
    print(f"✅ {file_path.name} - cleanup added")
    return True


def main():
    backend_dir = Path(__file__).parent.parent

    print("\n" + "=" * 80)
    print("🔧 Adding @AfterEach cleanup to test files")
    print("=" * 80)

    success_count = 0
    total = len(test_files)

    for rel_path, config in test_files.items():
        file_path = backend_dir / rel_path
        if add_cleanup_to_file(file_path, config):
            success_count += 1

    print("\n" + "=" * 80)
    print(f"✅ Successfully updated {success_count}/{total} files")
    print("=" * 80)

    return 0 if success_count == total else 1


if __name__ == "__main__":
    sys.exit(main())
