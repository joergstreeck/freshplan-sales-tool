# DSGVO Consent Management Specification - Sprint 2.1.5

**Dokumenttyp:** Technical Specification
**Status:** Definitive (02.10.2025)
**Owner:** team/leads-frontend
**Sprint:** 2.1.5 Frontend Phase 2

---

## 📋 Übersicht

Dieses Dokument definiert die rechtskonforme Implementierung des Consent-Managements für die Lead-Erfassung im FreshFoodz CRM gemäß DSGVO Art. 6 Abs. 1 lit. a (Einwilligung).

---

## 🎯 Business-Kontext

**Rechtliche Anforderung:**
Bei Erfassung personenbezogener Daten (Kontaktdaten) im B2B-Kontext ohne vorherige Geschäftsbeziehung ist eine **explizite Einwilligung** die rechtssicherste Lösung.

**Anwendungsfall:**
Partner erfassen neue Leads auf Trade-Messen, Veranstaltungen oder durch Kaltakquise. Die betroffenen Personen (Küchenchefs, Einkäufer) müssen der Datenverarbeitung aktiv zustimmen.

---

## 🔒 Rechtliche Grundlage

### DSGVO Art. 6 Abs. 1 lit. a - Einwilligung

**Anforderungen an rechtswirksame Einwilligung:**
1. **Freiwilligkeit:** Keine Koppelung an Vertragsabschluss (hier erfüllt: Lead-Vormerkung Stage 0 möglich OHNE Consent)
2. **Informiertheit:** Transparente Information über Verarbeitungszweck
3. **Eindeutigkeit:** Aktive Handlung erforderlich (Checkbox), NICHT vorausgefüllt
4. **Nachweisbarkeit:** Zeitstempel + User-ID in Audit-Log
5. **Widerrufbarkeit:** Jederzeit möglich, einfacher Prozess

---

## 🏗️ Technische Implementierung

### 1. Progressive Profiling Stage-Modell

**Stage 0 (Vormerkung):**
- **KEINE** personenbezogenen Daten
- Firma + Stadt + Branche (Optional)
- **Consent NICHT erforderlich**

**Stage 1 (Lead-Registrierung):**
- **CONSENT PFLICHT** sobald Contact-Daten erfasst werden
- Felder: `contact.firstName`, `contact.lastName`, `contact.email`, `contact.phone`

**Stage 2 (Qualifizierung):**
- Consent bereits in Stage 1 erteilt
- Erweiterte Geschäftsdaten (VAT ID, Expected Volume)

### 2. Backend Schema

```sql
-- Bereits vorhanden in Lead Entity (seit V255):
ALTER TABLE leads ADD COLUMN consent_given_at TIMESTAMPTZ;
```

**Lead.java Entity:**
```java
@Column(name = "consent_given_at")
private Instant consentGivenAt;
```

### 3. API Validierung

**POST /api/leads (Stage 1):**

```json
{
  "stage": 1,
  "companyName": "Restaurant Musterküche",
  "city": "Berlin",
  "contact": {
    "firstName": "Max",
    "lastName": "Mustermann",
    "email": "max.mustermann@example.com",
    "phone": "+49 30 12345678"
  },
  "consentGivenAt": "2025-10-02T14:30:00Z"  // PFLICHT bei Stage 1
}
```

**Validierungsregel:**
```java
// LeadService.java
if (leadRequest.stage() >= 1 && leadRequest.contact() != null) {
    if (leadRequest.consentGivenAt() == null) {
        throw new BadRequestException("Consent required for Stage 1 with contact data");
    }
}
```

### 4. Frontend UI Spezifikation

**LeadWizard.tsx - Stage 1 Step:**

```tsx
<FormControlLabel
  control={
    <Checkbox
      checked={consentGiven}
      onChange={(e) => setConsentGiven(e.target.checked)}
      required
      aria-required="true"
    />
  }
  label={
    <Typography variant="body2">
      Ich stimme zu, dass meine Kontaktdaten gespeichert werden.{' '}
      <Link href="/datenschutz" target="_blank">
        Widerruf jederzeit möglich
      </Link>
    </Typography>
  }
/>

{/* Submit-Button nur aktiv wenn Consent gegeben */}
<Button
  type="submit"
  variant="contained"
  disabled={!consentGiven || isSubmitting}
>
  Lead registrieren
</Button>
```

**UI-Eigenschaften:**
- Checkbox **NICHT** vorausgefüllt (default: `false`)
- Text in Deutsch, WCAG 2.1 AA konform
- Link zur Datenschutzrichtlinie (öffnet in neuem Tab)
- Submit-Button disabled solange Consent nicht erteilt
- Accessible: `aria-required="true"`, Keyboard-Navigation

### 5. Consent-Zeitstempel

**Frontend (onSubmit):**
```typescript
const handleSubmit = async (values: LeadFormValues) => {
  const payload = {
    ...values,
    consentGivenAt: consentGiven ? new Date().toISOString() : null
  };

  await createLead(payload);
};
```

**Backend (LeadService):**
```java
Lead lead = new Lead();
lead.setConsentGivenAt(Instant.now()); // Server-Zeit als Source of Truth
```

---

## 📝 Audit-Log Format

**Event:** `lead_consent_given`

```json
{
  "event_type": "lead_consent_given",
  "timestamp": "2025-10-02T14:30:00Z",
  "user_id": "partner-abc-123",
  "lead_id": 12345,
  "ip_address": "192.168.1.100",  // Optional: Client-IP für forensische Zwecke
  "user_agent": "Mozilla/5.0...",
  "consent_text_version": "v1.0",  // Falls Consent-Text sich ändert
  "data": {
    "contact_email": "max.mustermann@example.com",
    "contact_phone": "+49301234567" // Normalisiert
  }
}
```

**Speicherung:**
- In zentraler Audit-Tabelle (bereits vorhanden)
- Unveränderlich (Append-Only)
- Retention: Mindestens 3 Jahre nach Löschung der Hauptdaten

---

## 🔄 Widerrufsrecht

### 1. Widerruf-Workflow

**User-Requested Withdrawal:**
```
1. Betroffene Person kontaktiert FreshFoodz (Email/Telefon/Formular)
2. Support-Team validiert Identität
3. Admin führt aus: DELETE /api/leads/{id}/personal-data
4. System pseudonymisiert personenbezogene Daten
5. Confirmation-Email an Betroffene
```

**API Endpoint:**
```java
@DELETE
@Path("/{id}/personal-data")
@RolesAllowed({"admin", "dsgvo-officer"})
public Response deletePersonalData(@PathParam("id") Long leadId) {
    leadService.pseudonymizePersonalData(leadId);
    auditService.log("lead_personal_data_deleted", leadId);
    return Response.noContent().build();
}
```

### 2. Pseudonymisierung

**Betroffene Felder:**
```java
lead.setContact(null);  // Löscht firstName, lastName, email, phone
lead.setCompanyName("REDACTED-" + lead.getId());
lead.setNotes("GDPR_DELETION_" + Instant.now());
lead.setConsentGivenAt(null);
lead.setDeletedReason("GDPR_WITHDRAWAL");
lead.setDeletedAt(Instant.now());
```

**Erhaltene Felder (für Statistik):**
- `lead.id` (für Audit-Trail-Referenzen)
- `lead.territory` (für aggregierte Metriken)
- `lead.businessType` (für Marktanalysen)
- `lead.stage` (für Funnel-Analyse)

### 3. Widerrufsbelehrung (Consent-Text)

**Text im LeadWizard:**
```
Ich stimme zu, dass meine Kontaktdaten (Name, E-Mail, Telefon) von
FreshFoodz GmbH zum Zweck der Vertriebsanbahnung gespeichert und
verarbeitet werden.

Widerruf jederzeit möglich per E-Mail an datenschutz@freshfoodz.de
oder über unser Kontaktformular. Der Widerruf der Einwilligung
berührt nicht die Rechtmäßigkeit der bis zum Widerruf erfolgten
Verarbeitung.

Weitere Informationen: [Datenschutzrichtlinie]
```

**Link-Ziel:** `/datenschutz` (Frontend-Route, öffnet Datenschutzrichtlinie)

---

## ⚠️ Compliance-Checkliste

### DSGVO Art. 7 - Bedingungen für die Einwilligung

- [x] **Abs. 1:** Nachweis durch `consent_given_at` Timestamp + Audit-Log
- [x] **Abs. 2:** Verständliche und leicht zugängliche Form (Checkbox + Link)
- [x] **Abs. 3:** Widerruf jederzeit möglich (Widerrufsbelehrung vorhanden)
- [x] **Abs. 4:** Freiwilligkeit gewährleistet (Stage 0 ohne Consent möglich)

### DSGVO Art. 13 - Informationspflichten

- [x] **Abs. 1 a)** Verantwortlicher: FreshFoodz GmbH
- [x] **Abs. 1 b)** Kontakt: datenschutz@freshfoodz.de
- [x] **Abs. 1 c)** Verarbeitungszweck: Vertriebsanbahnung B2B-Food
- [x] **Abs. 1 d)** Rechtsgrundlage: Art. 6 Abs. 1 lit. a (Einwilligung)
- [x] **Abs. 2 b)** Speicherdauer: 6 Monate Lead-Protection, dann Pseudonymisierung
- [x] **Abs. 2 c)** Widerrufsrecht: Belehrung vorhanden

---

## 🧪 Test-Szenarien

### 1. Happy Path - Consent Erteilt
```typescript
test('can submit Stage 1 lead with consent', async () => {
  render(<LeadWizard />);

  // Fill Stage 0
  await fillStage0Fields({ companyName: 'Test GmbH', city: 'Berlin' });
  await clickNext();

  // Fill Stage 1
  await fillContactFields({ firstName: 'Max', lastName: 'Mustermann' });
  await clickConsentCheckbox(); // Consent erteilen
  await clickSubmit();

  expect(mockApi.createLead).toHaveBeenCalledWith(
    expect.objectContaining({
      consentGivenAt: expect.any(String)
    })
  );
});
```

### 2. Negative Case - Consent Verweigert
```typescript
test('cannot submit Stage 1 without consent', async () => {
  render(<LeadWizard />);

  await fillStage0Fields({ companyName: 'Test GmbH', city: 'Berlin' });
  await clickNext();
  await fillContactFields({ firstName: 'Max', lastName: 'Mustermann' });
  // Consent Checkbox NICHT aktiviert

  const submitButton = screen.getByRole('button', { name: /registrieren/i });
  expect(submitButton).toBeDisabled();
});
```

### 3. API Validation - Backend rejects missing consent
```java
@Test
void shouldRejectStage1WithoutConsent() {
    LeadRequest request = new LeadRequest(
        1, // Stage 1
        "Test GmbH",
        "Berlin",
        new ContactData("Max", "Mustermann", "max@test.de", "+49301234567"),
        null // consentGivenAt = null
    );

    assertThrows(BadRequestException.class, () -> {
        leadService.createLead(request, securityContext);
    });
}
```

### 4. Widerruf E2E
```java
@Test
void shouldPseudonymizeOnWithdrawal() {
    Long leadId = createLeadWithConsent();

    leadService.pseudonymizePersonalData(leadId);

    Lead lead = leadRepository.findById(leadId);
    assertNull(lead.getContact());
    assertTrue(lead.getCompanyName().startsWith("REDACTED-"));
    assertNotNull(lead.getDeletedAt());
}
```

---

## 📚 Referenzen

- **DSGVO:** [Art. 6 Abs. 1 lit. a](https://dsgvo-gesetz.de/art-6-dsgvo/), [Art. 7](https://dsgvo-gesetz.de/art-7-dsgvo/), [Art. 13](https://dsgvo-gesetz.de/art-13-dsgvo/)
- **TRIGGER_SPRINT_2_1_5.md:** Zeile 160-168 (DSGVO & Compliance)
- **Lead.java Entity:** `backend/src/main/java/de/freshplan/modules/leads/domain/Lead.java`
- **LeadWizard.tsx:** `frontend/src/features/leads/LeadWizard.tsx`
- **Audit-Log:** Zentrale Audit-Infrastruktur (Modul 00 Security)

---

## 📊 Status & Timeline

- **Definiert:** 02.10.2025
- **Implementation:** Sprint 2.1.5 Frontend Phase 2 (IN PROGRESS)
- **Review:** DSGVO-Beauftragter vor Production-Deployment
- **Go-Live:** Sprint 2.1.5 Completion (ca. 11.10.2025)

---

**Dokument-Owner:** Jörg Streeck + Claude Code
**Letzte Änderung:** 2025-10-02
**Version:** 1.0 (Definitive)
