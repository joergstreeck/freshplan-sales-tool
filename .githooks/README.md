# Git Hooks - FreshPlan Sales Tool

## ✅ Dies ist der AKTIVE Hooks-Ordner

Dieser Ordner enthält die **versionierten Git Hooks** für das Projekt.

```bash
git config core.hooksPath .githooks
```

## Pre-Commit Hook (Stand 2025-11-27 - Sprint 2.1.7.7)

Der `pre-commit` Hook führt **10 Prüfungen** aus (9 blockierend + 1 Info):

| # | Prüfung | Beschreibung | Status |
|---|---------|--------------|--------|
| 1 | **Design System Compliance** | Keine hardcoded colors/fonts | BLOCKIEREND |
| 2 | **Server-Driven Parity** | Backend/Frontend Schema-Parity | BLOCKIEREND |
| 2.3 | **Enum Seed Data Case** | Enum-Werte UPPERCASE | BLOCKIEREND |
| 2.5 | **Enum-Rendering-Parity** | Frontend nutzt Backend-Enums | BLOCKIEREND |
| 2.6 | **Field Type Architecture** | field.type statt field.fieldType | BLOCKIEREND |
| 3 | **Server-Driven Sections** | Wizard-Struktur vom Backend | BLOCKIEREND |
| 4 | **Backend Formatting** | Spotless Auto-Format | AUTO-FIX |
| 5 | **Backend Compilation** | Code muss kompilieren | BLOCKIEREND |
| 6 | **Frontend Formatting** | Lint-Staged Auto-Format | AUTO-FIX |
| 7 | **Dependency Cycles** | Keine zirkulären Abhängigkeiten | BLOCKIEREND |
| 8 | **Test Cleanup Validation** | `@AfterEach` mit echtem Code | BLOCKIEREND |
| 9 | **OpenAPI Type Sync** | TypeScript-Types aktuell | BLOCKIEREND |
| 10 | **fieldCatalog.json Guard** | Migration zu Server-Driven UI | BLOCKIEREND |

### PRÜFUNG 8: Test Cleanup Validation (Verbessert 2025-11-15)

**Was wird geprüft:**
- Alle `@QuarkusTest` mit Repository/EntityManager Inject müssen `@AfterEach` cleanup haben
- Tests mit `@BeforeEach @Transactional` müssen `@AfterEach` cleanup haben
- **NEU:** Cleanup-Methode darf NICHT leer sein (nur Kommentare/Whitespace) ← **Verhindert Test-Daten-Leaks!**
- Script: `backend/scripts/check-test-cleanup.py`

**Bei Fehler:**
- ❌ Commit wird blockiert
- ⚠️  **Neue Fehler-Kategorie:** "Leere Cleanup-Methode (nur Kommentare)"
  - Erkennt `@AfterEach cleanup()` mit nur Kommentaren aber keinem echten Code
  - Verhindert Silent Failures wie in `LeadConvertServiceTest.java` (CUSTOM-* Leak)
- 📚 Zeigt **funktionierendes Beispiel** aus `BranchServiceTest.java`
- ✅ Zeigt Best Practices:
  - Child-Entities ZUERST löschen (Foreign Keys!)
  - Pattern-Matching (`TEST-%`, `KD-%`) für Test-Daten
  - `@Transactional` ist Pflicht
  - EntityManager mit `@Inject` einbinden
  - **Cleanup muss echten Code enthalten, nicht nur Kommentare!**

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

### PRÜFUNG 9: OpenAPI Type Sync Check (Neu)

**Was wird geprüft:**
- Erkennt Änderungen an Backend-DTOs (*Request.java, *Response.java)
- Informiert über notwendige TypeScript-Type-Regenerierung

**Bei Änderungen an DTOs:**
- ℹ️  Zeigt Info-Meldung (NICHT blockierend)
- 📋 Gibt klare Anleitung:
  1. Backend starten: `cd backend && ./mvnw quarkus:dev`
  2. Types generieren: `cd frontend && npm run generate-api`
  3. Generated files committen: `git add frontend/src/api/generated/`

**OpenAPI Contract-First Development:**
- Backend ist Single Source of Truth
- Frontend nutzt generierte TypeScript-Types
- Verhindert manuelle Type-Definitionen und Schema-Drift

---
**Last Update:** 2025-11-27 (Sprint 2.1.7.7 - 10 Prüfungen inkl. Dependency Cycles, OpenAPI Type Sync, fieldCatalog.json Guard)
