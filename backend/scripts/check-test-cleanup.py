#!/usr/bin/env python3
"""
Test Cleanup Validator - Pre-Commit Hook

Sprint 2.1.7.7: Test Cleanup Fix
Prüft, ob alle @QuarkusTest-Tests mit DB-Operationen einen @AfterEach cleanup haben.

Regeln:
1. @QuarkusTest + Repository/EntityManager Inject → MUSS @AfterEach cleanup haben
2. @InjectMock für alle Repositories → OK (kein DB-Cleanup nötig)
3. Plain JUnit ohne @QuarkusTest → OK (kein DB-Cleanup nötig)
4. Tests mit @BeforeEach @Transactional → MUSS @AfterEach cleanup haben!

Exit Codes:
0 = Alle Tests OK
1 = Tests ohne cleanup gefunden
2 = Script-Fehler

Author: FreshPlan Team
Since: 2.1.7.7
"""

import sys
import re
from pathlib import Path
from dataclasses import dataclass
from typing import List, Set
from collections import defaultdict


@dataclass
class TestAnalysis:
    """Analysis result für eine Test-Datei"""
    file_path: Path
    is_quarkus_test: bool
    has_inject_repository: bool
    has_inject_entity_manager: bool
    has_inject_mock_only: bool
    has_after_each_cleanup: bool
    has_before_each_transactional: bool
    injected_types: Set[str]
    cleanup_method: str = ""

    @property
    def needs_cleanup(self) -> bool:
        """Prüft, ob dieser Test cleanup benötigt"""
        # Plain JUnit ohne @QuarkusTest braucht kein cleanup
        if not self.is_quarkus_test:
            return False

        # Nur Mocks → kein cleanup nötig
        if self.has_inject_mock_only:
            return False

        # @QuarkusTest + Repository oder EntityManager → cleanup nötig
        if self.has_inject_repository or self.has_inject_entity_manager:
            return True

        # @BeforeEach @Transactional → cleanup nötig
        if self.has_before_each_transactional:
            return True

        return False

    @property
    def has_violation(self) -> bool:
        """Prüft, ob Test gegen Cleanup-Regeln verstößt"""
        return self.needs_cleanup and not self.has_after_each_cleanup


def analyze_test_file(file_path: Path) -> TestAnalysis:
    """Analysiert eine Test-Datei auf Cleanup-Anforderungen"""

    content = file_path.read_text(encoding='utf-8')

    # Pattern für verschiedene Annotationen und Imports
    is_quarkus_test = bool(re.search(r'@QuarkusTest', content))
    has_after_each = bool(re.search(r'@AfterEach', content))
    has_before_each_transactional = bool(
        re.search(r'@BeforeEach\s+@Transactional', content, re.MULTILINE) or
        re.search(r'@Transactional\s+@BeforeEach', content, re.MULTILINE)
    )

    # Finde alle @Inject Annotationen
    inject_pattern = r'@Inject\s+(\w+)\s+(\w+);'
    inject_mock_pattern = r'@InjectMock\s+(\w+)\s+(\w+);'

    injected_types = set()
    inject_mock_types = set()

    for match in re.finditer(inject_pattern, content):
        type_name = match.group(1)
        injected_types.add(type_name)

    for match in re.finditer(inject_mock_pattern, content):
        type_name = match.group(1)
        inject_mock_types.add(type_name)

    # Prüfe, ob Repository oder EntityManager injiziert werden
    has_inject_repository = any('Repository' in t for t in injected_types)
    has_inject_entity_manager = 'EntityManager' in injected_types

    # Prüfe, ob ALLE Injects @InjectMock sind (keine echten @Inject)
    has_inject_mock_only = len(injected_types) == 0 and len(inject_mock_types) > 0

    # Finde cleanup-Methode
    cleanup_method = ""
    cleanup_match = re.search(
        r'@AfterEach[^}]*?void\s+(\w+)\s*\([^)]*\)\s*\{',
        content,
        re.DOTALL
    )
    if cleanup_match:
        cleanup_method = cleanup_match.group(1)

    return TestAnalysis(
        file_path=file_path,
        is_quarkus_test=is_quarkus_test,
        has_inject_repository=has_inject_repository,
        has_inject_entity_manager=has_inject_entity_manager,
        has_inject_mock_only=has_inject_mock_only,
        has_after_each_cleanup=has_after_each,
        has_before_each_transactional=has_before_each_transactional,
        injected_types=injected_types,
        cleanup_method=cleanup_method
    )


def find_all_test_files(test_dir: Path) -> List[Path]:
    """Findet alle *Test.java Dateien"""
    return sorted(test_dir.rglob("*Test.java"))


def main():
    """Main entry point"""

    # Finde Backend test directory
    script_dir = Path(__file__).parent
    backend_dir = script_dir.parent
    test_dir = backend_dir / "src" / "test" / "java"

    if not test_dir.exists():
        print(f"❌ Test directory nicht gefunden: {test_dir}", file=sys.stderr)
        return 2

    # Finde alle Test-Dateien
    test_files = find_all_test_files(test_dir)

    if not test_files:
        print("⚠️  Keine Test-Dateien gefunden!", file=sys.stderr)
        return 2

    # Analysiere alle Tests
    all_analyses: List[TestAnalysis] = []
    violations: List[TestAnalysis] = []

    for test_file in test_files:
        try:
            analysis = analyze_test_file(test_file)
            all_analyses.append(analysis)

            if analysis.has_violation:
                violations.append(analysis)
        except Exception as e:
            print(f"⚠️  Fehler beim Analysieren von {test_file.name}: {e}", file=sys.stderr)

    # Statistiken
    total_tests = len(all_analyses)
    quarkus_tests = sum(1 for a in all_analyses if a.is_quarkus_test)
    tests_needing_cleanup = sum(1 for a in all_analyses if a.needs_cleanup)
    tests_with_cleanup = sum(1 for a in all_analyses if a.needs_cleanup and a.has_after_each_cleanup)

    print("\n" + "=" * 80)
    print("🔍 TEST CLEANUP VALIDATION REPORT")
    print("=" * 80)
    print(f"\n📊 Statistiken:")
    print(f"   Total Tests:                {total_tests}")
    print(f"   @QuarkusTest Tests:         {quarkus_tests}")
    print(f"   Tests mit DB-Operationen:   {tests_needing_cleanup}")
    print(f"   Tests mit @AfterEach:       {tests_with_cleanup}")
    print(f"   ❌ Tests OHNE cleanup:      {len(violations)}")

    # Violations ausgeben
    if violations:
        print("\n" + "=" * 80)
        print("❌ VERLETZUNGEN GEFUNDEN - Tests ohne @AfterEach cleanup:")
        print("=" * 80)

        # Gruppiere nach Kategorie
        by_category = defaultdict(list)
        for v in violations:
            if v.has_inject_repository:
                by_category["Repository Inject"].append(v)
            elif v.has_inject_entity_manager:
                by_category["EntityManager Inject"].append(v)
            elif v.has_before_each_transactional:
                by_category["@BeforeEach @Transactional"].append(v)
            else:
                by_category["Andere"].append(v)

        for category, tests in sorted(by_category.items()):
            print(f"\n📁 {category}: ({len(tests)} Tests)")
            for test in tests:
                rel_path = test.file_path.relative_to(test_dir)
                print(f"   • {rel_path}")

                if test.injected_types:
                    print(f"     Injected: {', '.join(sorted(test.injected_types))}")

                print(f"     Grund: ", end="")
                reasons = []
                if test.has_inject_repository:
                    reasons.append("Repository Inject")
                if test.has_inject_entity_manager:
                    reasons.append("EntityManager Inject")
                if test.has_before_each_transactional:
                    reasons.append("@BeforeEach @Transactional")
                print(", ".join(reasons))

        print("\n" + "=" * 80)
        print("💡 EMPFEHLUNG:")
        print("=" * 80)
        print("""
Füge @AfterEach cleanup hinzu:

    @AfterEach
    @Transactional
    void cleanup() {
        // Step 1: Delete child entities (foreign keys)
        entityManager.createNativeQuery(
            "DELETE FROM child_table WHERE parent_id IN " +
            "(SELECT id FROM parent_table WHERE test_marker = 'TEST-%')")
            .executeUpdate();

        // Step 2: Delete parent entities
        repository.delete("testMarker LIKE 'TEST-%'");
    }

Siehe BranchServiceTest.java für Beispiel mit Foreign Key Constraints.
        """)

        return 1  # Exit code 1 = Violations gefunden

    else:
        print("\n" + "=" * 80)
        print("✅ ALLE TESTS OK - Cleanup korrekt implementiert!")
        print("=" * 80)
        return 0


if __name__ == "__main__":
    sys.exit(main())
