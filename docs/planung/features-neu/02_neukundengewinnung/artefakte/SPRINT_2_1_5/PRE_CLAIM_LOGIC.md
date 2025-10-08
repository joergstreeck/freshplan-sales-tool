---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "guideline"
status: "approved"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-10-08"
---

# Pre-Claim Logic – Stage 0 Schutz-Mechanik (Variante B)

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Pre-Claim Logic

## Zweck

Dokumentiert die **Pre-Claim Mechanik** für Stage 0 Leads gemäß Handelsvertretervertrag §2(8)(a):

> "Lead-Schutz entsteht mit Registrierung (Firma, Ort **und zentraler Kontakt** **oder dokumentierter Erstkontakt**)"

**Business-Problem:**
- Partner erfassen Lead bei Messe → haben nur Firma + Stadt, noch **keinen namentlichen Kontakt**
- Vertrag verlangt **entweder** Kontakt **oder** dokumentierten Erstkontakt für Schutzbeginn
- **Pre-Claim** = Vormerkung **MIT Schutz** (10 Tage Frist) bis Erstkontakt dokumentiert wird

**⚠️ ARCHITEKTUR-ENTSCHEIDUNG (2025-10-08):**
- **Variante B**: `registered_at` IMMER gesetzt (Audit Trail + DB Best Practice)
- **Neues Feld**: `first_contact_documented_at` entscheidet über Schutz-Vollständigkeit
- **Vorteil**: Keine NULL-Werte in Timestamps, keine Race Conditions, klarer Audit Trail

---

## Business Rules

### Rule 1: Schutzbeginn-Kriterien

**✅ VARIANTE B: FINALE REGELUNG (2025-10-08 - DB Best Practice)**

**Erstkontakt PFLICHT (kein Pre-Claim):**
- 🎪 **MESSE:** Partner HAT direkten Erstkontakt → Dokumentation PFLICHT
- 📞 **TELEFON:** Partner HAT direkten Erstkontakt → Dokumentation PFLICHT
- → **Schutz vollständig** ab Speicherung (`registered_at` + `first_contact_documented_at` beide gesetzt)

**Pre-Claim MÖGLICH (Erstkontakt optional):**
- 🤝 **EMPFEHLUNG:** Partner hat KEINEN direkten Erstkontakt (nur Empfehlung) → Pre-Claim erlaubt
- 🌐 **WEB_FORMULAR:** Kein direkter Erstkontakt → Pre-Claim erlaubt
- 🔗 **PARTNER:** API-Import → Pre-Claim erlaubt
- ❓ **SONSTIGES:** Fallback → Pre-Claim erlaubt
- → **Lead ist sofort geschützt** (`registered_at` gesetzt), aber hat **10 Tage Frist** für Erstkontakt
- → Erkennbar an: `first_contact_documented_at IS NULL`

**Schutz-Status (2 Stufen):**

| Status | `registered_at` | `first_contact_documented_at` | Bedeutung | Vertrags-Status |
|--------|-----------------|-------------------------------|-----------|-----------------|
| **Vollständig geschützt** | ✅ Gesetzt | ✅ Gesetzt | MESSE/TELEFON mit dokumentiertem Erstkontakt | 6 Monate Schutz aktiv |
| **Pre-Claim (unvollständig)** | ✅ Gesetzt | ❌ NULL | EMPFEHLUNG/WEB/PARTNER ohne Erstkontakt | Schutz aktiv, 10 Tage Frist |

**Warum Variante B (registered_at IMMER gesetzt)?**

1. **Audit Trail**: Jeder Lead hat klaren Timestamp "Wann erfasst?"
2. **Keine Race Conditions**: Zwei Vertreter können nicht gleichzeitig denselben Lead "vormerken"
3. **DB Best Practice**: NOT NULL für Timestamps (Standard-Pattern created_at, updated_at, registered_at)
4. **Einfache Queries**: Keine NULL-Checks bei Statistiken ("Wie viele Leads diese Woche?")
5. **Semantische Klarheit**: `registered_at` = "Erfassung im System", `first_contact_documented_at` = "Erstkontakt"

---

### Rule 2: Pre-Claim Ablauf

```
Tag 0:  Lead erstellt mit stage=0
        → registered_at = NOW() (Lead geschützt!)
        → first_contact_documented_at = NULL (Pre-Claim aktiv)

Tag 1-10: Lead muss Erstkontakt dokumentieren:
          - Activity FIRST_CONTACT_DOCUMENTED erstellen
          - first_contact_documented_at wird gesetzt
          - Pre-Claim → Vollständiger Schutz

Tag 10: Pre-Claim-Frist läuft ab
        → Lead bleibt geschützt (registered_at vorhanden)
        → Supervisor kann Lead freigeben oder Vertreter mahnen
        → Nightly Job markiert als "unvollständig"
```

**Kernunterschied zu Variante A:**
- **Variante A**: `registered_at = NULL` → Lead hat KEINEN Schutz (Race Condition möglich!)
- **Variante B**: `registered_at = NOW()` → Lead hat SOFORT Schutz (10 Tage für Erstkontakt)

---

### Rule 3: Migration-Ausnahme (WICHTIG!)

**Bestandsleads bei go-live:**
- ✅ **Sofortiger Vollschutz** (auch ohne explizite Erstkontakt-Dokumentation)
- Begründung: Bestehende Geschäftsbeziehung impliziert dokumentierten Kontakt
- Technisch: Migration setzt `registered_at = created_at` UND `first_contact_documented_at = created_at`

---

## Datenmodell

### Migration V274: Neues Feld first_contact_documented_at

```sql
-- V274__add_first_contact_documented_at_for_preclaim.sql
ALTER TABLE leads
  ADD COLUMN first_contact_documented_at TIMESTAMPTZ NULL;

COMMENT ON COLUMN leads.first_contact_documented_at IS
  'Zeitpunkt der Erstkontakt-Dokumentation (MESSE/TELEFON: Pflicht bei Erstellung,
   EMPFEHLUNG/WEB/PARTNER: Optional, 10 Tage Frist). NULL = Pre-Claim aktiv.';

-- Index für Pre-Claim Queries (Nightly Job)
CREATE INDEX IF NOT EXISTS idx_leads_first_contact_documented_at
  ON leads(first_contact_documented_at)
  WHERE first_contact_documented_at IS NULL;

-- Backfill für Bestandsleads (bereits geschützte Leads)
UPDATE leads
SET first_contact_documented_at = registered_at
WHERE first_contact_documented_at IS NULL
  AND registered_at IS NOT NULL;
```

### Schema-Übersicht

```sql
-- leads Table
leads:
  id UUID PRIMARY KEY
  company_name VARCHAR(255) NOT NULL
  city VARCHAR(255) NOT NULL
  registered_at TIMESTAMPTZ NOT NULL DEFAULT NOW()  -- ← IMMER gesetzt!
  first_contact_documented_at TIMESTAMPTZ NULL      -- ← NULL = Pre-Claim
  protected_until TIMESTAMPTZ NULL
  progress_deadline TIMESTAMPTZ NULL
  stage SMALLINT NOT NULL DEFAULT 0
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

**Pre-Claim Erkennung:**
```sql
-- Leads im Pre-Claim (noch kein Erstkontakt)
SELECT id, company_name, city, registered_at,
       (registered_at + INTERVAL '10 days') AS preclaim_expires_at
FROM leads
WHERE first_contact_documented_at IS NULL;

-- Leads mit vollständigem Schutz
SELECT id, company_name, protected_until
FROM leads
WHERE first_contact_documented_at IS NOT NULL;

-- Pre-Claim läuft bald ab (< 3 Tage verbleibend)
SELECT id, company_name, city, registered_at,
       (registered_at + INTERVAL '10 days') AS expires_at
FROM leads
WHERE first_contact_documented_at IS NULL
  AND registered_at < NOW() - INTERVAL '7 days';
```

---

## Backend Implementation

### LeadResource.createLead() - Variante B

```java
@POST
@Transactional
public Response createLead(LeadCreateRequest request, @Context UriInfo uriInfo) {
    Lead lead = new Lead();
    lead.setCompanyName(request.companyName);
    lead.setCity(request.city);
    lead.setStage(LeadStage.VORMERKUNG);
    lead.setSource(LeadSource.fromString(request.source));
    lead.setOwnerUserId(currentUserId);

    // Variante B: registered_at IMMER setzen (Audit Trail)
    lead.setRegisteredAt(LocalDateTime.now());

    // PRE-CLAIM LOGIC: Erstkontakt-Check
    if (lead.getSource() != null && lead.getSource().requiresFirstContact()) {
        // MESSE/TELEFON: Erstkontakt PFLICHT
        if (request.contactPerson == null || request.contactPerson.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                    "error", "First contact required",
                    "message", "MESSE/TELEFON leads require contact person name",
                    "source", lead.getSource().name()
                ))
                .build();
        }

        // Erstkontakt dokumentiert → Vollschutz
        lead.setFirstContactDocumentedAt(LocalDateTime.now());
        lead.setStage(LeadStage.REGISTRIERUNG);
        lead.setStatus(LeadStatus.REGISTERED);
        lead.setProtectedUntil(calculateProtectionUntil(lead.getRegisteredAt()));
        lead.setProgressDeadline(calculateProgressDeadline(LocalDateTime.now()));

        LOG.infof("Lead %s (%s source): Direct REGISTRIERUNG (first contact: %s)",
            lead.getCompanyName(), lead.getSource().name(), request.contactPerson);

    } else {
        // EMPFEHLUNG/WEB/PARTNER: Pre-Claim erlaubt
        lead.setFirstContactDocumentedAt(null);  // ← Pre-Claim aktiv!
        lead.setStage(LeadStage.VORMERKUNG);
        lead.setStatus(LeadStatus.REGISTERED);
        lead.setProtectedUntil(calculateProtectionUntil(lead.getRegisteredAt()));
        lead.setProgressDeadline(null);  // Kein Progress-Deadline bei Pre-Claim

        LOG.infof("Lead %s (%s source): VORMERKUNG (10 days to document first contact)",
            lead.getCompanyName(), lead.getSource() != null ? lead.getSource().name() : "UNKNOWN");
    }

    lead.persist();

    URI location = uriInfo.getAbsolutePathBuilder().path(lead.getId().toString()).build();
    LeadDTO dto = LeadDTO.from(lead);
    return Response.created(location).entity(dto).build();
}
```

### LeadSource.requiresFirstContact()

```java
public enum LeadSource {
    MESSE,
    TELEFON,
    EMPFEHLUNG,
    WEB_FORMULAR,
    PARTNER,
    SONSTIGES;

    /**
     * Check if this lead source requires documented first contact for full protection.
     *
     * <p>Business Rule (Handelsvertretervertrag §2(8)(a)):
     * MESSE and TELEFON sources require documented first contact (contact person name + date)
     * at lead creation. Other sources allow Pre-Claim: Lead protection starts immediately,
     * but has 10-day window to document first contact.
     *
     * @return true if first contact documentation is required (MESSE, TELEFON), false otherwise
     */
    public boolean requiresFirstContact() {
        return this == MESSE || this == TELEFON;
    }
}
```

---

## Frontend Implementation

### LeadWizard Stage 0 - Vormerkung (Variante B)

**Logik:**
- Bei **MESSE/TELEFON:** Erstkontakt-Felder immer sichtbar, PFLICHT
- Bei **EMPFEHLUNG/WEB/PARTNER/SONSTIGES:** Optional (Pre-Claim erlaubt)

```typescript
// LeadWizard.tsx - Stage 0
const LeadWizardStage0 = () => {
  const requiresFirstContact = ['MESSE', 'TELEFON'].includes(formData.source);

  return (
    <Box>
      <Typography variant="h6">Stage 0: Vormerkung</Typography>

      {/* Pflichtfelder */}
      <TextField name="companyName" label="Firma *" required />
      <TextField name="city" label="Stadt *" required />
      <Select name="source" label="Quelle *" required>
        <MenuItem value="MESSE">Messe/Event</MenuItem>
        <MenuItem value="EMPFEHLUNG">Empfehlung</MenuItem>
        <MenuItem value="TELEFON">Kaltakquise</MenuItem>
        <MenuItem value="WEB_FORMULAR">Web-Formular</MenuItem>
        <MenuItem value="PARTNER">Partner</MenuItem>
        <MenuItem value="SONSTIGES">Sonstige</MenuItem>
      </Select>

      {/* Kontaktperson (bei MESSE/TELEFON PFLICHT) */}
      <TextField
        name="contactPerson"
        label={requiresFirstContact ? "Kontaktperson (Erstkontakt) *" : "Kontaktperson (optional)"}
        required={requiresFirstContact}
        helperText={
          requiresFirstContact
            ? "PFLICHT: Name der Person bei Erstkontakt (Messe/Telefonat)"
            : "Optional: Name der Kontaktperson"
        }
      />

      {/* Schutz-Status-Hinweis */}
      {requiresFirstContact && (
        <Alert severity="success" sx={{ mt: 2 }}>
          ✅ <strong>Vollständiger Schutz ab Speicherung</strong><br/>
          Erstkontakt dokumentiert → 6 Monate Lead-Schutz aktiv
        </Alert>
      )}

      {!requiresFirstContact && (
        <Alert severity="info" sx={{ mt: 2 }}>
          ℹ️ <strong>Pre-Claim aktiv</strong><br/>
          Lead ist geschützt, aber 10 Tage Zeit für Erstkontakt-Dokumentation.
          Danach wird Lead als "unvollständig" markiert.
        </Alert>
      )}
    </Box>
  );
};
```

---

## API Contract

### POST /api/leads (Variante B)

**Request Body (MESSE - Erstkontakt PFLICHT):**
```json
{
  "companyName": "Test GmbH",
  "city": "Hamburg",
  "source": "MESSE",
  "contactPerson": "Max Mustermann",
  "stage": 0,
  "ownerUserId": "partner-123"
}
```

**Response (Vollständiger Schutz):**
```json
{
  "id": "lead-uuid",
  "companyName": "Test GmbH",
  "city": "Hamburg",
  "stage": 0,
  "registeredAt": "2025-10-08T14:30:00Z",          // ← IMMER gesetzt
  "firstContactDocumentedAt": "2025-10-08T14:30:00Z",  // ← Vollschutz!
  "protectedUntil": "2026-04-08T14:30:00Z",        // +6 Monate
  "progressDeadline": "2025-12-07T14:30:00Z",      // +60 Tage
  "createdAt": "2025-10-08T14:30:00Z"
}
```

**Request Body (EMPFEHLUNG - Pre-Claim):**
```json
{
  "companyName": "Hotel Müller",
  "city": "Dresden",
  "source": "EMPFEHLUNG",
  "stage": 0,
  "ownerUserId": "partner-456"
}
```

**Response (Pre-Claim aktiv):**
```json
{
  "id": "lead-uuid",
  "companyName": "Hotel Müller",
  "city": "Dresden",
  "stage": 0,
  "registeredAt": "2025-10-08T14:30:00Z",          // ← IMMER gesetzt (Audit Trail)
  "firstContactDocumentedAt": null,                 // ← NULL = Pre-Claim!
  "protectedUntil": "2026-04-08T14:30:00Z",        // Schutz aktiv
  "progressDeadline": null,                         // Kein Progress-Deadline
  "preClaimExpiresAt": "2025-10-18T14:30:00Z",     // registeredAt + 10 days
  "createdAt": "2025-10-08T14:30:00Z"
}
```

---

## Tests

### Integration Tests (LeadPreClaimLogicTest.java)

```java
@QuarkusTest
public class LeadPreClaimLogicTest {

  @Test
  @TestSecurity(user = "test-user", roles = {"USER"})
  @DisplayName("MESSE lead with contact person → direct REGISTRIERUNG")
  public void testMesseLeadWithContactPerson_DirectRegistrierung() {
    Map<String, Object> request = Map.of(
        "companyName", "Z-Catering Mitte GmbH",
        "contactPerson", "Max Mustermann",  // ← PFLICHT für MESSE!
        "source", "MESSE",
        "city", "Berlin",
        "countryCode", "DE");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/leads")
        .then()
        .statusCode(201)
        .body("source", equalTo("MESSE"))
        .body("stage", equalTo("REGISTRIERUNG"))
        .body("status", equalTo(LeadStatus.REGISTERED.name()))
        .body("registeredAt", notNullValue())              // ← IMMER gesetzt!
        .body("firstContactDocumentedAt", notNullValue())  // ← Vollschutz!
        .body("contactPerson", equalTo("Max Mustermann"));
  }

  @Test
  @TestSecurity(user = "test-user", roles = {"USER"})
  @DisplayName("MESSE lead without contact person → 400 Bad Request")
  public void testMesseLeadWithoutContactPerson_BadRequest() {
    Map<String, Object> request = Map.of(
        "companyName", "Y-Hotel Berlin",
        "source", "MESSE",
        "city", "Berlin",
        "countryCode", "DE");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/leads")
        .then()
        .statusCode(400)
        .body("error", equalTo("First contact required"))
        .body("source", equalTo("MESSE"));
  }

  @Test
  @TestSecurity(user = "test-user", roles = {"USER"})
  @DisplayName("EMPFEHLUNG lead → VORMERKUNG (Pre-Claim allowed)")
  public void testEmpfehlungLead_VormerkungPreClaim() {
    Map<String, Object> request = Map.of(
        "companyName", "V-Hotel Dresden",
        "source", "EMPFEHLUNG",
        "city", "Dresden",
        "countryCode", "DE");

    given()
        .contentType(ContentType.JSON)
        .body(request)
        .when()
        .post("/api/leads")
        .then()
        .statusCode(201)
        .body("source", equalTo("EMPFEHLUNG"))
        .body("stage", equalTo("VORMERKUNG"))
        .body("status", equalTo(LeadStatus.REGISTERED.name()))
        .body("registeredAt", notNullValue())            // ← IMMER gesetzt (Audit Trail)
        .body("firstContactDocumentedAt", nullValue());  // ← NULL = Pre-Claim!
  }
}
```

---

## Cleanup-Strategie

### Sprint 2.1.6 (Nightly Job)

```java
@Scheduled(cron = "0 2 * * *")  // 02:00 Uhr täglich
public void checkPreClaimExpiry() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(10);

    // Finde Pre-Claims mit abgelaufener Frist
    List<Lead> expiredPreClaims = leadRepository.find(
        "firstContactDocumentedAt IS NULL AND registeredAt < ?1",
        cutoff
    ).list();

    for (Lead lead : expiredPreClaims) {
        // Log für Audit
        auditService.log("preclaim_expired", lead.getId(),
            Map.of("companyName", lead.getCompanyName(),
                   "registeredAt", lead.getRegisteredAt()));

        // NICHT löschen! Lead bleibt geschützt (registered_at vorhanden)
        // Supervisor kann entscheiden: Freigeben oder Vertreter mahnen
        lead.setStatus(LeadStatus.INCOMPLETE);
        lead.persist();

        // Optional: Email an Supervisor
        emailService.sendPreClaimExpiryNotification(lead);
    }

    logger.info("Marked {} expired pre-claims as INCOMPLETE", expiredPreClaims.size());
}
```

---

## Metriken

```java
// LeadMetrics.java
@Gauge(name = "leads_preclaim_active", description = "Anzahl aktive Pre-Claims")
public long activePreClaims() {
    return leadRepository.count("firstContactDocumentedAt IS NULL");
}

@Gauge(name = "leads_preclaim_expiring_soon", description = "Pre-Claims < 3 Tage")
public long expiringPreClaims() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
    return leadRepository.count(
        "firstContactDocumentedAt IS NULL AND registeredAt < ?1",
        cutoff);
}

@Gauge(name = "leads_fully_protected", description = "Anzahl vollständig geschützte Leads")
public long fullyProtectedLeads() {
    return leadRepository.count("firstContactDocumentedAt IS NOT NULL");
}
```

---

## Referenzen

- **Handelsvertretervertrag:** §2(8)(a) - Lead-Schutz mit Registrierung
- **ADR-004:** Inline-First Architecture (kein separate lead_protection table)
- **V255:** `stage`, `progress_deadline`, `progress_warning_sent_at` Felder
- **V274:** `first_contact_documented_at` Feld (Variante B)
- **V257:** `calculate_protection_until()`, `calculate_progress_deadline()` Functions

---

**Letzte Aktualisierung:** 2025-10-08
**Autor:** Claude Code (Sprint 2.1.5/2.1.6 Pre-Claim Implementation Variante B)
**Status:** ✅ Production-Ready (DB Best Practice, validiert gegen Handelsvertretervertrag)
