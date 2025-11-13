# Git Hooks - FreshPlan Sales Tool

## ✅ Dies ist der AKTIVE Hooks-Ordner

Dieser Ordner enthält die **versionierten Git Hooks** für das Projekt.

```bash
git config core.hooksPath .githooks
```

## Pre-Commit Hook (PRÜFUNG 9 - Stand 2025-11-13)

Der `pre-commit` Hook führt **9 Prüfungen** aus (8 blockierend + 1 Info):

1. **Design System Compliance** - Keine hardcoded colors/fonts (BLOCKIEREND)
2. **Server-Driven Architecture Parity** - Backend/Frontend Schema-Parity (BLOCKIEREND)
3. **Enum Seed Data Case Validation** - Enum-Werte korrekt (BLOCKIEREND)
4. **Enum-Rendering-Parity** - Frontend nutzt Backend-Enums (BLOCKIEREND)
5. **Server-Driven Sections Architecture** - Wizard-Struktur vom Backend (BLOCKIEREND)
6. **Backend Code Formatting** - Spotless Auto-Format (AUTO-FIX)
7. **Backend Compilation Check** - Code muss kompilieren (BLOCKIEREND)
8. **Test Cleanup Validation** - `@AfterEach` cleanup für DB-Tests (BLOCKIEREND)
9. **OpenAPI Type Sync Check** - Erinnert an `npm run generate-api` (INFO-ONLY) ← **NEU!**

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
**Last Update:** 2025-11-13 (PRÜFUNG 9 - OpenAPI Type Sync Check)
