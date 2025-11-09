#!/usr/bin/env python3
"""
Fix remaining 19 Repository Inject Tests mit @AfterEach cleanup
Sprint 2.1.7.7: Test Cleanup Fix - Phase 2
"""

import os
import re
from pathlib import Path

# Test files to fix with their cleanup patterns
TEST_FILES = {
    # Customer CQRS Tests (6)
    "de/freshplan/domain/customer/service/ContactEventCaptureCQRSIntegrationTest.java": {
        "pattern": "customerNumber LIKE 'TEST-%'",
        "type": "customer_cqrs"
    },
    "de/freshplan/domain/customer/service/ContactInteractionServiceCQRSIntegrationTest.java": {
        "pattern": "customerNumber LIKE 'TEST-%'",
        "type": "customer_cqrs"
    },
    "de/freshplan/domain/customer/service/CustomerCQRSIntegrationTest.java": {
        "pattern": "customerNumber LIKE 'TEST-%'",
        "type": "customer_cqrs"
    },
    "de/freshplan/domain/customer/service/timeline/TimelineCQRSIntegrationTest.java": {
        "pattern": "customerNumber LIKE 'TEST-%'",
        "type": "customer_cqrs"
    },
    "de/freshplan/domain/customer/service/command/CustomerCommandServiceIntegrationTest.java": {
        "pattern": "customerNumber LIKE 'TEST-%'",
        "type": "customer_cqrs"
    },
    "de/freshplan/domain/customer/service/CustomerServiceIntegrationTest.java": {
        "pattern": "customerNumber LIKE 'TEST-%'",
        "type": "customer_cqrs"
    },

    # Other CQRS Tests (4)
    "de/freshplan/domain/profile/service/ProfileCQRSIntegrationTest.java": {
        "pattern": "testMarker LIKE 'TEST-%'",
        "type": "simple"
    },
    "de/freshplan/domain/help/service/HelpContentCQRSIntegrationTest.java": {
        "pattern": "testMarker LIKE 'TEST-%'",
        "type": "simple"
    },
    "de/freshplan/domain/search/service/SearchCQRSIntegrationTest.java": {
        "pattern": "customerNumber LIKE 'TEST-%'",
        "type": "customer_related"
    },
    "de/freshplan/domain/export/service/HtmlExportCQRSIntegrationTest.java": {
        "pattern": "customerNumber LIKE 'TEST-%'",
        "type": "customer_related"
    },

    # Opportunity Tests (3)
    "de/freshplan/domain/opportunity/repository/OpportunityRepositoryBasicTest.java": {
        "pattern": "testMarker LIKE 'TEST-%'",
        "type": "opportunity"
    },
    "de/freshplan/domain/opportunity/service/OpportunityCalculationServiceTest.java": {
        "pattern": "testMarker LIKE 'TEST-%'",
        "type": "opportunity"
    },
    "de/freshplan/domain/opportunity/service/OpportunityServiceTest.java": {
        "pattern": "testMarker LIKE 'TEST-%'",
        "type": "opportunity"
    },
}

CLEANUP_TEMPLATES = {
    "customer_cqrs": """
  @AfterEach
  @TestTransaction
  void cleanup() {
    // Delete in correct order to respect foreign key constraints
    em.createNativeQuery("DELETE FROM customer_timeline_events WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')").executeUpdate();
    em.createNativeQuery("DELETE FROM customer_contacts WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')").executeUpdate();
    em.createNativeQuery("DELETE FROM opportunities WHERE customer_id IN (SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')").executeUpdate();
    em.createNativeQuery("DELETE FROM customers WHERE customer_number LIKE 'TEST-%'").executeUpdate();
  }
""",
    "customer_related": """
  @AfterEach
  @TestTransaction
  void cleanup() {
    // Delete test customers
    customerRepository.delete("customerNumber LIKE 'TEST-%'");
  }
""",
    "simple": """
  @AfterEach
  @TestTransaction
  void cleanup() {
    // Delete test data
    repository.delete("testMarker LIKE 'TEST-%'");
  }
""",
    "opportunity": """
  @AfterEach
  @TestTransaction
  void cleanup() {
    // Delete in correct order
    em.createNativeQuery("DELETE FROM opportunity_activities WHERE opportunity_id IN (SELECT id FROM opportunities WHERE test_marker LIKE 'TEST-%')").executeUpdate();
    em.createNativeQuery("DELETE FROM opportunities WHERE test_marker LIKE 'TEST-%'").executeUpdate();
  }
"""
}

def add_aftereach_import(content):
    """Add AfterEach import if not present"""
    if "import org.junit.jupiter.api.AfterEach;" in content:
        return content

    # Find the right place to add import (after BeforeEach if present, otherwise after Tag)
    if "import org.junit.jupiter.api.BeforeEach;" in content:
        return content.replace(
            "import org.junit.jupiter.api.BeforeEach;",
            "import org.junit.jupiter.api.AfterEach;\nimport org.junit.jupiter.api.BeforeEach;"
        )
    elif "import org.junit.jupiter.api.Tag;" in content:
        return content.replace(
            "import org.junit.jupiter.api.Tag;",
            "import org.junit.jupiter.api.AfterEach;\nimport org.junit.jupiter.api.Tag;"
        )
    elif "import org.junit.jupiter.api.Test;" in content:
        return content.replace(
            "import org.junit.jupiter.api.Test;",
            "import org.junit.jupiter.api.AfterEach;\nimport org.junit.jupiter.api.Test;"
        )

    return content

def add_cleanup_method(content, cleanup_type):
    """Add @AfterEach cleanup method"""
    if "@AfterEach" in content and "void cleanup()" in content:
        print("    ⏭️  Already has @AfterEach cleanup")
        return content

    cleanup_code = CLEANUP_TEMPLATES[cleanup_type]

    # Find insertion point (after @BeforeEach or before first @Test)
    if "@BeforeEach" in content:
        # Insert after @BeforeEach method ends
        pattern = r'(@BeforeEach.*?void.*?\{.*?\n\s*\})\n'
        match = re.search(pattern, content, re.DOTALL)
        if match:
            return content[:match.end()] + cleanup_code + content[match.end():]

    # Fallback: Insert before first @Test
    pattern = r'(\n\s*)(@Test)'
    match = re.search(pattern, content)
    if match:
        return content[:match.start(1)] + cleanup_code + "\n" + content[match.start(1):]

    return content

def fix_test_file(file_path, config):
    """Fix a single test file"""
    if not os.path.exists(file_path):
        print(f"❌ File not found: {file_path}")
        return False

    print(f"📝 Fixing: {os.path.basename(file_path)}")

    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Step 1: Add import
    content = add_aftereach_import(content)

    # Step 2: Add cleanup method
    content = add_cleanup_method(content, config["type"])

    # Write back
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

    print(f"✅ Fixed: {os.path.basename(file_path)}")
    return True

def main():
    base_path = Path(__file__).parent.parent / "src" / "test" / "java"

    print("🚀 Starting Test Cleanup Fix - Phase 2\n")
    print(f"📂 Base path: {base_path}\n")

    fixed = 0
    failed = 0

    for rel_path, config in TEST_FILES.items():
        file_path = base_path / rel_path
        if fix_test_file(str(file_path), config):
            fixed += 1
        else:
            failed += 1
        print()

    print(f"\n📊 Summary:")
    print(f"✅ Fixed: {fixed}")
    print(f"❌ Failed: {failed}")
    print(f"📋 Total: {len(TEST_FILES)}")

if __name__ == "__main__":
    main()
