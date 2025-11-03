#!/usr/bin/env python3
"""
Dependency Cycle Checker for Java Backend
Sprint 2.1.7.7 - Pre-Commit Hook Integration

Prüft auf zirkuläre Abhängigkeiten zwischen Java-Packages.
ZERO TOLERANCE für Circular Dependencies!

Usage:
  python3 ./scripts/check-dependency-cycles.py

Exit Codes:
  0 - No cycles found
  1 - Cycles detected
"""

import os
import re
import sys
from collections import defaultdict
from pathlib import Path
from typing import Set, Dict, List, Tuple


class DependencyCycleChecker:
    def __init__(self, root_dir: str = "backend/src/main/java"):
        self.root_dir = Path(root_dir)
        self.package_deps: Dict[str, Set[str]] = defaultdict(set)
        self.java_files: List[Path] = []

    def find_java_files(self) -> None:
        """Find all .java files in src/main/java."""
        if not self.root_dir.exists():
            print(f"❌ Directory not found: {self.root_dir}")
            sys.exit(1)

        self.java_files = list(self.root_dir.rglob("*.java"))
        print(f"📋 Found {len(self.java_files)} Java files")

    def extract_package_name(self, file_path: Path) -> str:
        """Extract package name from Java file."""
        try:
            content = file_path.read_text(encoding='utf-8')
            match = re.search(r'^package\s+([\w.]+)\s*;', content, re.MULTILINE)
            if match:
                return match.group(1)
        except Exception as e:
            print(f"⚠️  Could not read {file_path}: {e}")
        return ""

    def extract_imports(self, file_path: Path) -> Set[str]:
        """Extract imported packages from Java file."""
        imports = set()
        try:
            content = file_path.read_text(encoding='utf-8')
            # Match: import de.freshplan.domain.customer.*;
            # Match: import de.freshplan.domain.customer.entity.Customer;
            import_pattern = r'^import\s+(de\.freshplan\.[\w.]+?)(?:\.\w+)?\s*;'

            for match in re.finditer(import_pattern, content, re.MULTILINE):
                import_stmt = match.group(1)
                # Extract package (without class name)
                # de.freshplan.domain.customer.entity.Customer → de.freshplan.domain.customer.entity
                package_parts = import_stmt.split('.')
                # Keep everything except the last part (which might be a class name)
                # If it starts with uppercase, it's a class → remove it
                if package_parts[-1][0].isupper():
                    package = '.'.join(package_parts[:-1])
                else:
                    package = import_stmt

                imports.add(package)
        except Exception as e:
            print(f"⚠️  Could not read {file_path}: {e}")
        return imports

    def build_dependency_graph(self) -> None:
        """Build package dependency graph."""
        print("\n🔍 Building dependency graph...")

        for java_file in self.java_files:
            package = self.extract_package_name(java_file)
            if not package or not package.startswith("de.freshplan"):
                continue

            imports = self.extract_imports(java_file)

            # Add dependencies (only de.freshplan packages)
            for imported_package in imports:
                if imported_package and imported_package != package:
                    if imported_package.startswith("de.freshplan"):
                        self.package_deps[package].add(imported_package)

        print(f"📊 Found {len(self.package_deps)} packages with dependencies")

    def find_cycles_dfs(self) -> List[List[str]]:
        """Find all cycles using DFS."""
        cycles = []
        visited = set()
        rec_stack = set()
        path = []

        def dfs(node: str) -> None:
            visited.add(node)
            rec_stack.add(node)
            path.append(node)

            for neighbor in self.package_deps.get(node, []):
                if neighbor not in visited:
                    dfs(neighbor)
                elif neighbor in rec_stack:
                    # Cycle found!
                    cycle_start = path.index(neighbor)
                    cycle = path[cycle_start:] + [neighbor]
                    cycles.append(cycle)

            path.pop()
            rec_stack.remove(node)

        for package in self.package_deps:
            if package not in visited:
                dfs(package)

        return cycles

    def print_cycles(self, cycles: List[List[str]]) -> None:
        """Print found cycles in readable format."""
        if not cycles:
            print("\n✅ No circular dependencies found!")
            return

        print(f"\n❌ Found {len(cycles)} circular dependencies:\n")

        for i, cycle in enumerate(cycles, 1):
            print(f"🔄 Cycle {i}:")
            print(f"   {' → '.join(cycle)}")
            print()

    def check(self) -> bool:
        """Run full dependency cycle check."""
        print("=" * 60)
        print("🔍 Dependency Cycle Checker")
        print("=" * 60)

        self.find_java_files()
        self.build_dependency_graph()

        print("\n🔎 Searching for cycles...")
        cycles = self.find_cycles_dfs()

        self.print_cycles(cycles)

        if cycles:
            print("=" * 60)
            print("❌ DEPENDENCY CYCLE CHECK FAILED")
            print("=" * 60)
            print("\n💡 How to fix:")
            print("   1. Identify the cycle above")
            print("   2. Break the cycle by:")
            print("      • Introducing an interface/abstraction")
            print("      • Moving shared code to a common package")
            print("      • Inverting the dependency direction")
            print()
            return False
        else:
            print("=" * 60)
            print("✅ DEPENDENCY CYCLE CHECK PASSED")
            print("=" * 60)
            return True


def main():
    checker = DependencyCycleChecker()
    success = checker.check()
    sys.exit(0 if success else 1)


if __name__ == "__main__":
    main()
