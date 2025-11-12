# Git Hooks - FreshPlan Sales Tool

## ✅ Dies ist der AKTIVE Hooks-Ordner

Dieser Ordner enthält die **versionierten Git Hooks** für das Projekt.

```bash
git config core.hooksPath .githooks
```

## Pre-Commit Hook (PRÜFUNG 8 - Stand 2025-11-12)

Der `pre-commit` Hook führt **8 blockierende Prüfungen** aus:

1. **Design System Compliance** - Keine hardcoded colors/fonts
2. **Server-Driven Architecture Parity** - Backend/Frontend Schema-Parity
3. **Enum Seed Data Case Validation** - Enum-Werte korrekt
4. **Enum-Rendering-Parity** - Frontend nutzt Backend-Enums
5. **Server-Driven Sections Architecture** - Wizard-Struktur vom Backend
6. **Backend Code Formatting** - Spotless Auto-Format
7. **Backend Compilation Check** - Code muss kompilieren
8. **Test Cleanup Validation** - `@AfterEach` cleanup für DB-Tests ← **NEU!**

### PRÜFUNG 8: Test Cleanup Validation (Neu)

**Was wird geprüft:**
- Alle `@QuarkusTest` mit Repository/EntityManager Inject müssen `@AfterEach` cleanup haben
- Tests mit `@BeforeEach @Transactional` müssen `@AfterEach` cleanup haben
- Script: `backend/scripts/check-test-cleanup.py`

**Bei Fehler:**
- ❌ Commit wird blockiert
- 📚 Zeigt **funktionierendes Beispiel** aus `BranchServiceTest.java`
- ✅ Zeigt Best Practices:
  - Child-Entities ZUERST löschen (Foreign Keys!)
  - Pattern-Matching (`TEST-%`, `KD-%`) für Test-Daten
  - `@Transactional` ist Pflicht
  - EntityManager mit `@Inject` einbinden

**Beispiel:**
```java
@AfterEach
@Transactional
void cleanup() {
  // Step 1: Delete child entities (foreign key constraints!)
  entityManager.createNativeQuery(
    "DELETE FROM customer_timeline_events WHERE customer_id IN " +
    "(SELECT id FROM customers WHERE customer_number LIKE 'TEST-%')")
    .executeUpdate();

  // Step 2: Delete parent entities
  customerRepository.delete("customerNumber LIKE 'TEST-%'");
}
```

## Hook bearbeiten

**NUR HIER bearbeiten:**
- ✅ `.githooks/pre-commit`

**NIEMALS hier:**
- ❌ `.git/hooks/pre-commit` (wird ignoriert!)

## Hook temporär überspringen

```bash
git commit --no-verify
```

⚠️ **Nur in Ausnahmefällen!** Der Hook verhindert Code-Qualitätsprobleme.

## Für neue Entwickler / Claude-Instanzen

1. **Kein Setup nötig** - Nach `git clone` funktioniert alles automatisch
2. **Hook-Location prüfen:**
   ```bash
   git config --get core.hooksPath
   # Output: .githooks
   ```
3. **Hook testen:**
   ```bash
   .githooks/pre-commit
   ```

---
**Last Update:** 2025-11-12 (PRÜFUNG 8 - Test Cleanup Validation)
