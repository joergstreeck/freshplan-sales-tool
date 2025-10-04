---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "guideline"
status: "approved"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-10-03"
---

# Pre-Claim Logic – Stage 0 Schutz-Mechanik

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Pre-Claim Logic

## Zweck

Dokumentiert die **Pre-Claim Mechanik** für Stage 0 Leads gemäß Handelsvertretervertrag §2(8)(a):

> "Lead-Schutz entsteht mit Registrierung (Firma, Ort **und zentraler Kontakt** **oder dokumentierter Erstkontakt**)"

**Business-Problem:**
- Partner erfassen Lead bei Messe → haben nur Firma + Stadt, noch **keinen namentlichen Kontakt**
- Vertrag verlangt **entweder** Kontakt **oder** dokumentierten Erstkontakt für Schutzbeginn
- **Pre-Claim** = Vormerkung **ohne Schutz** (10 Tage Frist) bis Daten vervollständigt werden

---

## Business Rules

### Rule 1: Schutzbeginn-Kriterien

**⚠️ OPTION A: FINALE REGELUNG (2025-10-04 - User-Feedback integriert)**

**Erstkontakt PFLICHT (kein Pre-Claim):**
- 🎪 **MESSE:** Partner HAT direkten Erstkontakt → Dokumentation PFLICHT
- 📞 **TELEFON:** Partner HAT direkten Erstkontakt → Dokumentation PFLICHT
- → **Schutz startet SOFORT** bei Speicherung

**Pre-Claim MÖGLICH (Erstkontakt optional):**
- 🤝 **EMPFEHLUNG:** Partner hat KEINEN direkten Erstkontakt (nur Empfehlung) → Pre-Claim erlaubt
- 🌐 **WEB_FORMULAR:** Kein direkter Erstkontakt → Pre-Claim erlaubt
- 🔗 **PARTNER:** API-Import → Pre-Claim erlaubt
- ❓ **SONSTIGE:** Fallback → Pre-Claim erlaubt
- → **10 Tage Frist** zur Vervollständigung

**Schutz startet SOFORT wenn:**
- ✅ **Kontakt vorhanden** (`contact.firstName` + `contact.lastName` + (`contact.email` OR `contact.phone`))
- **ODER**
- ✅ **Erstkontakt dokumentiert** (`firstContactActivity.channel` + `firstContactActivity.date` + `firstContactActivity.notes`)

**Pre-Claim (kein Schutz) wenn:**
- ❌ **Weder Kontakt noch dokumentierter Erstkontakt**
- ❌ **Source erlaubt Pre-Claim** (EMPFEHLUNG, WEB_FORMULAR, PARTNER, SONSTIGE)
- Lead hat 10 Tage Zeit zur Vervollständigung
- Erkennbar an: `registered_at IS NULL`

---

### Rule 2: Pre-Claim Ablauf

```
Tag 0:  Lead erstellt mit stage=0, registered_at=NULL
        → Pre-Claim aktiv (kein Schutz!)

Tag 1-10: Lead muss vervollständigt werden:
          - Entweder Kontakt hinzufügen (Stage 1)
          - Oder Erstkontakt dokumentieren (Activity FIRST_CONTACT_DOCUMENTED)

Tag 10: Pre-Claim läuft ab
        → Lead wird archiviert/gelöscht (manuell in 2.1.5, Job in 2.1.6)
```

---

### Rule 3: Migration-Ausnahme (WICHTIG!)

**Bestandsleads bei go-live:**
- ✅ **Sofortiger Schutz** (auch ohne Kontakt/Erstkontakt)
- Begründung: Bestehende Geschäftsbeziehung impliziert dokumentierten Kontakt
- Technisch: Migration setzt `registered_at = created_at` für alle Bestandsleads

---

## Datenmodell

### Existierende Felder (KEINE neue Migration!)

```sql
-- leads Table (V255 bereits deployed)
leads:
  id UUID PRIMARY KEY
  company_name VARCHAR(255) NOT NULL
  city VARCHAR(255) NOT NULL
  registered_at TIMESTAMPTZ NULL  -- ← NULL = Pre-Claim!
  protected_until TIMESTAMPTZ NULL
  progress_deadline TIMESTAMPTZ NULL
  stage SMALLINT NOT NULL DEFAULT 0
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
```

**Pre-Claim Erkennung:**
```sql
-- Leads im Pre-Claim (kein Schutz)
SELECT id, company_name, city, created_at
FROM leads
WHERE registered_at IS NULL;

-- Leads mit Schutz aktiv
SELECT id, company_name, protected_until
FROM leads
WHERE registered_at IS NOT NULL;

-- Pre-Claim läuft bald ab (< 3 Tage verbleibend)
SELECT id, company_name, city, created_at,
       (created_at + INTERVAL '10 days') AS expires_at
FROM leads
WHERE registered_at IS NULL
  AND created_at < NOW() - INTERVAL '7 days';
```

---

## Backend Implementation

### LeadService.createLead()

```java
public Lead createLead(LeadDTO dto) {
    Lead lead = new Lead();
    lead.setCompanyName(dto.companyName());
    lead.setCity(dto.city());
    lead.setStage(dto.stage());
    lead.setSource(dto.source());
    lead.setOwnerUserId(dto.ownerUserId());

    // PRE-CLAIM LOGIC
    if (hasContact(dto) || hasDocumentedFirstContact(dto)) {
        // ✅ Schutz startet SOFORT
        lead.setRegisteredAt(Instant.now());
        lead.setProtectedUntil(calculateProtectionUntil(lead.getRegisteredAt()));
        lead.setProgressDeadline(calculateProgressDeadline(Instant.now()));

        // Activity erstellen falls dokumentierter Erstkontakt
        if (hasDocumentedFirstContact(dto)) {
            createFirstContactActivity(lead, dto.firstContactActivity());
        }
    } else {
        // ❌ PRE-CLAIM: Kein Schutz
        lead.setRegisteredAt(null);
        lead.setProtectedUntil(null);
        lead.setProgressDeadline(null);
        // created_at + 10 days = Ablauf Pre-Claim
    }

    return leadRepository.persist(lead);
}

private boolean hasContact(LeadDTO dto) {
    return dto.contact() != null
        && dto.contact().firstName() != null
        && dto.contact().lastName() != null
        && (dto.contact().email() != null || dto.contact().phone() != null);
}

private boolean hasDocumentedFirstContact(LeadDTO dto) {
    FirstContactActivityDTO activity = dto.firstContactActivity();
    return activity != null
        && activity.channel() != null
        && activity.date() != null
        && activity.notes() != null
        && activity.notes().length() >= 10; // Mindestlänge
}

private void createFirstContactActivity(Lead lead, FirstContactActivityDTO dto) {
    LeadActivity activity = new LeadActivity();
    activity.setLeadId(lead.getId());
    activity.setActivityType("FIRST_CONTACT_DOCUMENTED");
    activity.setActivityDate(dto.date());
    activity.setChannel(dto.channel());
    activity.setSummary(dto.notes());
    activity.setCountsAsProgress(false); // ← WICHTIG: kein Progress!
    activityRepository.persist(activity);
}
```

---

## Frontend Implementation

### LeadWizard Stage 0 - Vormerkung (Zwei-Felder-Lösung)

**Design-Entscheidung (2025-10-04):**
- **Feld 1: Notizen/Quelle** (immer sichtbar, optional) → für Kontext, kein Einfluss auf Schutz
- **Feld 2: Erstkontakt-Dokumentation** (conditional) → aktiviert Schutz

**Logik:**
- Bei **MESSE/TELEFON:** Erstkontakt-Block immer sichtbar, PFLICHT
- Bei **EMPFEHLUNG/WEB/PARTNER/SONSTIGE:** Checkbox "Ich hatte bereits Erstkontakt" → nur dann Erstkontakt-Block anzeigen

```typescript
// LeadWizard.tsx - Stage 0
const LeadWizardStage0 = () => {
  const [showFirstContactFields, setShowFirstContactFields] = useState(false);
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
        <MenuItem value="SONSTIGE">Sonstige</MenuItem>
      </Select>

      {/* FELD 1: Notizen/Quelle (immer sichtbar, optional) */}
      <TextField
        name="notes"
        label="Notizen / Quelle (optional)"
        multiline
        rows={2}
        helperText="Z.B. Empfehlung von Herrn Schulz, Partner-Liste Nr. 47"
      />

      {/* FELD 2: Erstkontakt-Dokumentation (conditional) */}

      {/* Bei EMPFEHLUNG/WEB/PARTNER: Checkbox zeigen */}
      {!requiresFirstContact && (
        <FormControlLabel
          control={
            <Checkbox
              checked={showFirstContactFields}
              onChange={(e) => setShowFirstContactFields(e.target.checked)}
            />
          }
          label="☑ Ich hatte bereits Erstkontakt (für sofortigen Lead-Schutz)"
        />
      )}

      {/* Erstkontakt-Block anzeigen wenn: MESSE/TELEFON ODER Checkbox aktiviert */}
      {(requiresFirstContact || showFirstContactFields) && (
        <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
          <Typography variant="subtitle2" gutterBottom>
            Erstkontakt dokumentieren
            {requiresFirstContact && (
              <Chip label="PFLICHT" color="error" size="small" sx={{ ml: 1 }} />
            )}
          </Typography>

          <Select
            name="firstContact.channel"
            label="Kanal *"
            required={requiresFirstContact}
          >
            <MenuItem value="MESSE">Messe-Gespräch</MenuItem>
            <MenuItem value="PHONE">Telefonat</MenuItem>
            <MenuItem value="EMAIL">E-Mail</MenuItem>
            <MenuItem value="REFERRAL">Empfehlung/Vorstellung</MenuItem>
            <MenuItem value="OTHER">Sonstige</MenuItem>
          </Select>

          <DateTimePicker
            name="firstContact.performedAt"
            label="Datum/Uhrzeit *"
            required={requiresFirstContact}
          />

          <TextField
            name="firstContact.notes"
            label="Gesprächsnotiz *"
            multiline
            rows={3}
            required={requiresFirstContact}
            helperText="Mindestens 10 Zeichen (z.B. 'Gespräch mit Frau Müller am Stand 12, Interesse an Bio-Produkten')"
          />
        </Box>
      )}

      {/* Schutz-Status-Hinweis */}
      {!showFirstContactFields && !requiresFirstContact && (
        <Alert severity="warning" sx={{ mt: 2 }}>
          ⚠️ <strong>Pre-Claim:</strong> Kein Schutz aktiv!<br/>
          Lead läuft in 10 Tagen ab. Aktivieren Sie den Erstkontakt oder fügen Sie später Kontaktdaten hinzu (Stage 1).
        </Alert>
      )}

      {(requiresFirstContact || showFirstContactFields) && (
        <Alert severity="success" sx={{ mt: 2 }}>
          ✅ Schutz startet bei Speicherung (6 Monate ab heute)
        </Alert>
      )}
    </Box>
  );
};
```

**Wichtige Unterscheidung:**
- **Notizen-Feld:** Freier Text, keine Auswirkung auf `registered_at`
- **Erstkontakt-Felder:** Strukturiert (Kanal, Datum, Notiz), löst `registered_at = NOW()` aus

---

## API Contract

### POST /api/leads (erweitert)

**Request Body:**
```json
{
  "companyName": "Test GmbH",
  "city": "Hamburg",
  "source": "MESSE",
  "stage": 0,
  "ownerUserId": "partner-123",

  // OPTION 1: Kontakt vorhanden → Schutz startet
  "contact": {
    "firstName": "Max",
    "lastName": "Mustermann",
    "email": "max@test.de"
  }

  // OPTION 2: Erstkontakt dokumentiert → Schutz startet
  "firstContactActivity": {
    "channel": "MESSE",
    "date": "2025-10-03T14:30:00Z",
    "notes": "Gespräch am Stand 12, Interesse an Bio-Produkten"
  }

  // OPTION 3: Weder Kontakt noch Erstkontakt → Pre-Claim (kein Schutz)
}
```

**Response (Pre-Claim):**
```json
{
  "id": "lead-uuid",
  "companyName": "Test GmbH",
  "city": "Hamburg",
  "stage": 0,
  "registeredAt": null,  // ← NULL = Pre-Claim!
  "protectedUntil": null,
  "progressDeadline": null,
  "createdAt": "2025-10-03T14:30:00Z",
  "preClaimExpiresAt": "2025-10-13T14:30:00Z"  // created_at + 10 days
}
```

**Response (Schutz aktiv):**
```json
{
  "id": "lead-uuid",
  "companyName": "Test GmbH",
  "city": "Hamburg",
  "stage": 0,
  "registeredAt": "2025-10-03T14:30:00Z",  // ← Schutz aktiv!
  "protectedUntil": "2026-04-03T14:30:00Z",  // +6 Monate
  "progressDeadline": "2025-12-02T14:30:00Z",  // +60 Tage
  "createdAt": "2025-10-03T14:30:00Z"
}
```

---

## Tests

### Unit Tests (Mock-first)

```java
@Test
void createLead_withContact_shouldStartProtection() {
    // Given
    LeadDTO dto = LeadDTO.builder()
        .companyName("Test GmbH")
        .city("Hamburg")
        .stage(0)
        .contact(new ContactDTO("Max", "Mustermann", "max@test.de", null))
        .build();

    // When
    Lead lead = leadService.createLead(dto);

    // Then
    assertThat(lead.getRegisteredAt()).isNotNull();  // Schutz aktiv
    assertThat(lead.getProtectedUntil()).isNotNull();
    assertThat(lead.getProgressDeadline()).isNotNull();
}

@Test
void createLead_withDocumentedFirstContact_shouldStartProtection() {
    // Given
    FirstContactActivityDTO activity = new FirstContactActivityDTO(
        "MESSE",
        Instant.now(),
        "Gespräch am Stand 12, Interesse an Bio-Produkten"
    );
    LeadDTO dto = LeadDTO.builder()
        .companyName("Test GmbH")
        .city("Hamburg")
        .stage(0)
        .firstContactActivity(activity)
        .build();

    // When
    Lead lead = leadService.createLead(dto);

    // Then
    assertThat(lead.getRegisteredAt()).isNotNull();  // Schutz aktiv
    assertThat(lead.getProtectedUntil()).isNotNull();

    // Activity erstellt
    List<LeadActivity> activities = activityRepository.findByLeadId(lead.getId());
    assertThat(activities).hasSize(1);
    assertThat(activities.get(0).getActivityType()).isEqualTo("FIRST_CONTACT_DOCUMENTED");
    assertThat(activities.get(0).getCountsAsProgress()).isFalse();
}

@Test
void createLead_withoutContactOrFirstContact_shouldBePreClaim() {
    // Given
    LeadDTO dto = LeadDTO.builder()
        .companyName("Test GmbH")
        .city("Hamburg")
        .stage(0)
        .build();

    // When
    Lead lead = leadService.createLead(dto);

    // Then
    assertThat(lead.getRegisteredAt()).isNull();  // ← Pre-Claim!
    assertThat(lead.getProtectedUntil()).isNull();
    assertThat(lead.getProgressDeadline()).isNull();

    // Pre-Claim läuft in 10 Tagen ab
    Instant expiresAt = lead.getCreatedAt().plus(Duration.ofDays(10));
    assertThat(expiresAt).isAfter(Instant.now());
}
```

---

## Cleanup-Strategie

### Sprint 2.1.5 (Manuell)

**Listen-Filter in Frontend:**
```sql
-- Pre-Claim läuft bald ab (<= 3 Tage)
SELECT id, company_name, city, created_at,
       (created_at + INTERVAL '10 days') AS expires_at
FROM leads
WHERE registered_at IS NULL
  AND created_at < NOW() - INTERVAL '7 days'
ORDER BY created_at ASC;
```

**Manuelle Aktion:**
- Partner bekommt "Pre-Claim expiring soon" Badge
- Kann Lead vervollständigen oder archivieren

---

### Sprint 2.1.6 (Nightly Job)

```java
@Scheduled(cron = "0 2 * * *")  // 02:00 Uhr täglich
public void cleanupExpiredPreClaims() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(10);

    List<Lead> expiredPreClaims = leadRepository.find(
        "registeredAt IS NULL AND createdAt < ?1",
        cutoff
    ).list();

    for (Lead lead : expiredPreClaims) {
        // Log für Audit
        auditService.log("pre_claim_expired", lead.getId(),
            Map.of("companyName", lead.getCompanyName(),
                   "createdAt", lead.getCreatedAt()));

        // Archivieren oder löschen
        leadRepository.delete(lead);
    }

    logger.info("Cleaned up {} expired pre-claims", expiredPreClaims.size());
}
```

---

## Metriken

```java
// LeadMetrics.java
@Gauge(name = "leads_preclaim_active", description = "Anzahl aktive Pre-Claims")
public long activePreClaims() {
    return leadRepository.count("registeredAt IS NULL");
}

@Gauge(name = "leads_preclaim_expiring_soon", description = "Pre-Claims < 3 Tage")
public long expiringPreClaims() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(7);
    return leadRepository.count("registeredAt IS NULL AND createdAt < ?1", cutoff);
}

@Gauge(name = "leads_protected", description = "Anzahl geschützte Leads")
public long protectedLeads() {
    return leadRepository.count("registeredAt IS NOT NULL");
}
```

---

## Referenzen

- **Handelsvertretervertrag:** §2(8)(a) - Lead-Schutz mit Registrierung
- **ADR-004:** Inline-First Architecture (kein separate lead_protection table)
- **V255:** `stage`, `progress_deadline`, `progress_warning_sent_at` Felder
- **V257:** `calculate_protection_until()`, `calculate_progress_deadline()` Functions

---

**Letzte Aktualisierung:** 2025-10-03
**Autor:** Claude Code (Sprint 2.1.5 Pre-Claim Implementation)
**Status:** ✅ Production-Ready (Validiert gegen Handelsvertretervertrag)
