---
module: "02_neukundengewinnung"
sprint: "2.1.5"
doc_type: "technical_concept"
status: "approved"
owner: "team/leads"
created: "2025-10-08"
updated: "2025-10-11"
---

# Variante B Migration Guide – Pre-Claim Logic

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Variante B Migration

## Zweck

Dokumentiert die Migration von **Variante A** (registeredAt = NULL) zu **Variante B** (firstContactDocumentedAt = NULL) für Pre-Claim Logic.

---

## Executive Summary

**Was ändert sich?**
- `registered_at` ist NICHT MEHR nullable → IMMER gesetzt (Audit Trail)
- Neues Feld: `first_contact_documented_at` (nullable) → NULL = Pre-Claim aktiv
- Lead ist SOFORT geschützt (auch bei Pre-Claim), hat aber 10 Tage für Erstkontakt

**Warum?**
- **DB Best Practice**: NOT NULL für Timestamps (created_at, updated_at, registered_at)
- **Keine Race Conditions**: Zwei Vertreter können nicht denselben Lead doppelt "vormerken"
- **Audit Trail**: Jeder Lead hat klaren "Wann erfasst?"-Timestamp
- **Semantische Klarheit**: registered_at = "Erfassung", first_contact_documented_at = "Erstkontakt"

---

## Breaking Changes

### 1. Database Schema

**ALT (Variante A):**
```sql
leads:
  registered_at TIMESTAMPTZ NULL  -- NULL = Pre-Claim
```

**NEU (Variante B):**
```sql
leads:
  registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW()  -- IMMER gesetzt
  first_contact_documented_at TIMESTAMPTZ NULL      -- NULL = Pre-Claim
```

**Migration:** V274__add_first_contact_documented_at_for_preclaim.sql

---

### 2. Backend API

**ALT (Variante A):**
```java
// Pre-Claim erkennen
if (lead.getRegisteredAt() == null) {
    // Pre-Claim aktiv
}

// Pre-Claim erstellen
lead.setRegisteredAt(null);  // Kein Schutz
```

**NEU (Variante B):**
```java
// Pre-Claim erkennen
if (lead.getFirstContactDocumentedAt() == null) {
    // Pre-Claim aktiv
}

// Pre-Claim erstellen
lead.setRegisteredAt(LocalDateTime.now());          // Schutz sofort
lead.setFirstContactDocumentedAt(null);             // 10 Tage Frist
```

---

### 3. Frontend TypeScript

**ALT (Variante A):**
```typescript
interface Lead {
  registeredAt: string | null;  // null = Pre-Claim
}

// Pre-Claim Check
const isPreClaim = lead.registeredAt === null;

// Pre-Claim Filter
const preClaims = leads.filter(l => l.registeredAt === null);
```

**NEU (Variante B):**
```typescript
interface Lead {
  registeredAt: string;                 // IMMER vorhanden
  firstContactDocumentedAt: string | null;  // null = Pre-Claim
}

// Pre-Claim Check
const isPreClaim = lead.firstContactDocumentedAt === null;

// Pre-Claim Filter
const preClaims = leads.filter(l => l.firstContactDocumentedAt === null);
```

---

### 4. SQL Queries

**ALT (Variante A):**
```sql
-- Pre-Claims finden
SELECT * FROM leads WHERE registered_at IS NULL;

-- Geschützte Leads finden
SELECT * FROM leads WHERE registered_at IS NOT NULL;

-- Statistik
SELECT COUNT(*) FROM leads WHERE registered_at >= '2025-10-01';
-- ❌ PROBLEM: Pre-Claims werden nicht gezählt!
```

**NEU (Variante B):**
```sql
-- Pre-Claims finden
SELECT * FROM leads WHERE first_contact_documented_at IS NULL;

-- Vollständig geschützte Leads finden
SELECT * FROM leads WHERE first_contact_documented_at IS NOT NULL;

-- Statistik (ALLE Leads, inkl. Pre-Claims!)
SELECT COUNT(*) FROM leads WHERE registered_at >= '2025-10-01';
-- ✅ FUNKTIONIERT: Alle Leads haben registered_at!
```

---

## Migration Checklist

### Phase 1: Dokumentation (✅ COMPLETE)
- [x] PRE_CLAIM_LOGIC.md neu geschrieben
- [x] BUSINESS_LOGIC_LEAD_ERFASSUNG.md aktualisiert
- [x] FRONTEND_DELTA.md Breaking-Change-Header
- [x] VARIANTE_B_MIGRATION_GUIDE.md erstellt (dieses Dokument)
- [ ] CRM_AI_CONTEXT_SCHNELL.md aktualisieren
- [ ] CRM_COMPLETE_MASTER_PLAN_V5.md aktualisieren
- [ ] TRIGGER_SPRINT_2_1_7.md aktualisieren

### Phase 2: Backend Migration
- [ ] V274 Migration: `first_contact_documented_at` Column hinzufügen
- [ ] Lead.java: `firstContactDocumentedAt` Field hinzufügen
- [ ] Lead.java: `registeredAt` Field NOT NULL behalten (Zeile 124: kein `= LocalDateTime.now()`)
- [ ] Lead.java: `@PrePersist` Hook entfernen (Zeilen 341-343)
- [ ] LeadResource.createLead(): Pre-Claim Logic auf Variante B umbauen
- [ ] LeadDTO: `firstContactDocumentedAt` Field hinzufügen
- [ ] LeadPreClaimLogicTest.java: Tests auf Variante B anpassen

### Phase 3: Frontend Migration (Optional - aktuell kein Frontend implementiert)
- [ ] Lead.ts Interface: `firstContactDocumentedAt` Field hinzufügen
- [ ] LeadWizard: Pre-Claim Check auf `firstContactDocumentedAt` ändern
- [ ] LeadList: Pre-Claim Badge auf `firstContactDocumentedAt` ändern
- [ ] LeadList: Pre-Claim Filter auf `firstContactDocumentedAt` ändern

---

## Rollback-Plan

**Falls Variante B Probleme macht:**

```sql
-- Schritt 1: Migration rückgängig machen
DROP INDEX IF EXISTS idx_leads_first_contact_documented_at;
ALTER TABLE leads DROP COLUMN IF EXISTS first_contact_documented_at;

-- Schritt 2: registered_at wieder nullable machen (GEFÄHRLICH!)
-- ❌ NUR wenn ALLE Leads registered_at gesetzt haben!
-- ALTER TABLE leads ALTER COLUMN registered_at DROP NOT NULL;
```

**⚠️ WICHTIG:** Rollback zu Variante A ist NICHT empfohlen!
- Bestehende Leads haben bereits `registered_at` gesetzt
- NULL-Werte würden Audit Trail zerstören

---

## Testing Strategy

### Unit Tests
```java
@Test
void testPreClaimWithVariantB() {
    Lead lead = new Lead();
    lead.setRegisteredAt(LocalDateTime.now());          // ← IMMER gesetzt
    lead.setFirstContactDocumentedAt(null);             // ← Pre-Claim!

    assertTrue(lead.isPreClaim());  // Custom Methode: firstContactDocumentedAt == null
    assertFalse(lead.isFullyProtected());
}

@Test
void testFullProtectionWithVariantB() {
    Lead lead = new Lead();
    lead.setRegisteredAt(LocalDateTime.now());
    lead.setFirstContactDocumentedAt(LocalDateTime.now());  // ← Vollschutz!

    assertFalse(lead.isPreClaim());
    assertTrue(lead.isFullyProtected());
}
```

### Integration Tests
```java
@Test
@TestSecurity(user = "test-user", roles = {"USER"})
void testEmpfehlungLeadCreatesPreClaim() {
    Map<String, Object> request = Map.of(
        "companyName", "Test GmbH",
        "source", "EMPFEHLUNG",
        "city", "Berlin"
    );

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .post("/api/leads")
        .then()
        .statusCode(201)
        .body("registeredAt", notNullValue())            // ← IMMER gesetzt!
        .body("firstContactDocumentedAt", nullValue());  // ← Pre-Claim!
}
```

---

## Referenzen

- **Haupt-Spec:** [PRE_CLAIM_LOGIC.md](./PRE_CLAIM_LOGIC.md)
- **Business-Logic:** [BUSINESS_LOGIC_LEAD_ERFASSUNG.md](./BUSINESS_LOGIC_LEAD_ERFASSUNG.md)
- **Frontend:** [FRONTEND_DELTA.md](./FRONTEND_DELTA.md)
- **Migration:** V274__add_first_contact_documented_at_for_preclaim.sql

---

**Autor:** Claude Code
**Datum:** 2025-10-08
**Status:** ✅ Approved - Ready for Implementation
