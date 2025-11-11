#!/usr/bin/env python3
"""
Fix Compilation Errors - Sprint 2.1.7.7

Behebt Kompilierungsfehler in Test-Dateien:
- Fügt fehlende @Inject EntityManager em hinzu
- Ersetzt @TestTransaction mit @Transactional
- Korrigiert repository Referenzen

Author: Claude
Since: 2.1.7.7
"""

import re
from pathlib import Path

# Betroffene Dateien laut Kompilierungsfehlern
FILES_TO_FIX = [
    "src/test/java/de/freshplan/domain/opportunity/service/OpportunityServiceTest.java",
    "src/test/java/de/freshplan/domain/customer/service/ContactInteractionServiceCQRSIntegrationTest.java",
    "src/test/java/de/freshplan/domain/customer/service/CustomerCQRSIntegrationTest.java",
    "src/test/java/de/freshplan/domain/profile/service/ProfileCQRSIntegrationTest.java",
    "src/test/java/de/freshplan/domain/customer/service/timeline/TimelineCQRSIntegrationTest.java",
    "src/test/java/de/freshplan/domain/customer/service/CustomerServiceIntegrationTest.java",
    "src/test/java/de/freshplan/domain/customer/service/command/CustomerCommandServiceIntegrationTest.java",
]

def fix_test_file(file_path: Path) -> bool:
    """Korrigiert eine Test-Datei"""

    content = file_path.read_text(encoding='utf-8')
    original_content = content

    # 1. Ersetze @TestTransaction mit @Transactional
    content = content.replace('@TestTransaction', '@Transactional')

    # 2. Prüfe ob cleanup() em verwendet aber em nicht deklariert ist
    if 'em.' in content or 'em;' in content:
        if '@Inject EntityManager em' not in content and '@Inject jakarta.persistence.EntityManager em' not in content:
            # Füge EntityManager em nach den anderen @Inject Annotationen hinzu
            inject_pattern = r'(@Inject\s+\w+\s+\w+;)'
            matches = list(re.finditer(inject_pattern, content))
            if matches:
                last_inject = matches[-1]
                insert_pos = last_inject.end()
                content = content[:insert_pos] + '\n  @Inject jakarta.persistence.EntityManager em;' + content[insert_pos:]
                print(f"  ✅ EntityManager em hinzugefügt")

    # 3. Finde repository Referenzen in cleanup() und korrigiere
    cleanup_match = re.search(r'@AfterEach.*?void cleanup\(\) \{(.*?)\n  \}', content, re.DOTALL)
    if cleanup_match:
        cleanup_body = cleanup_match.group(1)

        # Prüfe ob "repository" verwendet wird
        if 'repository.' in cleanup_body or 'repository;' in cleanup_body:
            # Finde korrekte Repository-Variable
            repo_pattern = r'@Inject\s+(\w*Repository)\s+(\w+);'
            repo_match = re.search(repo_pattern, content)
            if repo_match:
                repo_var_name = repo_match.group(2)
                # Ersetze "repository" mit korrektem Namen
                if repo_var_name != 'repository':
                    content = content.replace('repository.delete', f'{repo_var_name}.delete')
                    print(f"  ✅ repository → {repo_var_name} korrigiert")

    # Schreibe nur wenn Änderungen
    if content != original_content:
        file_path.write_text(content, encoding='utf-8')
        return True

    return False

def main():
    backend_dir = Path(__file__).parent.parent

    print("🔧 Fixing Compilation Errors...\n")

    fixed_count = 0
    for file_rel_path in FILES_TO_FIX:
        file_path = backend_dir / file_rel_path

        if not file_path.exists():
            print(f"⚠️  {file_path.name} - NICHT GEFUNDEN")
            continue

        print(f"📝 {file_path.name}")
        if fix_test_file(file_path):
            fixed_count += 1
            print(f"  ✅ Datei korrigiert")
        else:
            print(f"  ℹ️  Keine Änderungen nötig")

    print(f"\n✅ {fixed_count}/{len(FILES_TO_FIX)} Dateien korrigiert")

if __name__ == '__main__':
    main()
