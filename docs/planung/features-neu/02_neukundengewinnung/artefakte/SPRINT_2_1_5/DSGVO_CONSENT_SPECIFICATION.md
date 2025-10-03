---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "specification"
status: "draft"
sprint: "2.1.6"
owner: "team/leads-backend"
updated: "2025-10-03"
---

# DSGVO Consent Spezifikation – Web-Intake

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → DSGVO Consent

**⚠️ WICHTIG:** Dieses Dokument ist erst für **Sprint 2.1.6 (Web-Intake)** relevant.
In Sprint 2.1.5 wird nur die Frontend-UI vorbereitet, Backend-Feld `consent_given_at` kommt erst in V259.

## Rechtsgrundlage (Art. 6 DSGVO)

### Source-abhängige Consent-Logik

| Lead-Quelle | Rechtsgrundlage | Consent PFLICHT? | UI-Element |
|------------|----------------|------------------|------------|
| WEB_FORMULAR | Art. 6 Abs. 1 lit. a (Einwilligung) | ✅ JA | Consent-Checkbox |
| MESSE | Art. 6 Abs. 1 lit. f (Berechtigtes Interesse) | ❌ NEIN | Info-Text |
| EMPFEHLUNG | Art. 6 Abs. 1 lit. f (Berechtigtes Interesse) | ❌ NEIN | Info-Text |
| TELEFON | Art. 6 Abs. 1 lit. f (Berechtigtes Interesse) | ❌ NEIN | Info-Text |
| PARTNER_IMPORT | Art. 6 Abs. 1 lit. f (Berechtigtes Interesse) | ❌ NEIN | - |

**Rationale:**
- **Web-Formular**: Lead registriert sich selbst → explizite Einwilligung erforderlich
- **Partner-Erfassung**: B2B-Geschäftsanbahnung → berechtigtes Interesse ausreichend

## Consent-Text (finalisiert)

### ✅ Web-Formular Consent-Checkbox (WEB_FORMULAR)

**Text (Deutsch):**
```
☐ Ich stimme zu, dass meine Kontaktdaten (Name, E-Mail, Telefon, Firma)
  von FreshFoodz zur Bearbeitung meiner Anfrage gespeichert und verarbeitet werden.
  Die Einwilligung kann jederzeit widerrufen werden.
```

**Pflichtfeld:** JA (ohne Consent kein Submit möglich)

**Widerrufshinweis (unter Checkbox):**
```
Weitere Informationen zum Datenschutz: https://freshfoodz.de/datenschutz
Widerruf jederzeit per E-Mail an: datenschutz@freshfoodz.de
```

**Barrierefreiheit (ARIA):**
```tsx
<Checkbox
  id="dsgvo-consent"
  required
  aria-required="true"
  aria-describedby="consent-description consent-revocation"
  onChange={(e) => setConsentGiven(e.target.checked)}
/>
<FormHelperText id="consent-description">
  Pflichtfeld: Ohne Einwilligung kann Ihre Anfrage nicht bearbeitet werden
</FormHelperText>
<FormHelperText id="consent-revocation">
  Weitere Informationen zum Datenschutz: <a href="https://freshfoodz.de/datenschutz">Datenschutzerklärung</a>.
  Widerruf jederzeit per E-Mail an: datenschutz@freshfoodz.de
</FormHelperText>
```

### ℹ️ Partner-Erfassung Info-Text (MESSE/EMPFEHLUNG/TELEFON)

**Text (Deutsch):**
```
ℹ️ Hinweis zum Datenschutz:
Die Daten werden auf Basis berechtigten Interesses (Art. 6 Abs. 1 lit. f DSGVO)
im Rahmen der B2B-Geschäftsanbahnung gespeichert. Widerspruch jederzeit möglich.
```

**Keine Checkbox:** Info-Text wird angezeigt, aber keine Zustimmung erforderlich

**Platzierung:** Unter "Quelle"-Dropdown, nur sichtbar bei MESSE/EMPFEHLUNG/TELEFON

## Technische Implementation (Sprint 2.1.6)

### DB-Migration V259 (consent_given_at Feld)

```sql
-- V259__add_consent_given_at.sql
ALTER TABLE leads ADD COLUMN consent_given_at TIMESTAMPTZ NULL;

COMMENT ON COLUMN leads.consent_given_at IS
  'DSGVO Art. 6 Abs. 1 lit. a: Zeitpunkt der Einwilligung (nur bei source=WEB_FORMULAR)';

-- Index für Consent-Queries (z.B. "Alle Leads ohne Consent")
CREATE INDEX idx_leads_consent_given_at ON leads(consent_given_at)
  WHERE consent_given_at IS NOT NULL;
```

### Backend-Validierung (LeadService)

```java
@ApplicationScoped
public class LeadService {

    public Lead createLead(LeadCreateRequest request) {
        // DSGVO Consent-Validierung
        if (request.getSource() == LeadSource.WEB_FORMULAR) {
            if (request.getConsentGivenAt() == null) {
                throw new BadRequestException(
                    "Consent erforderlich für Web-Formular Leads (Art. 6 Abs. 1 lit. a DSGVO)"
                );
            }
            // Consent-Zeitstempel validieren (nicht in Zukunft, max. 24h alt)
            validateConsentTimestamp(request.getConsentGivenAt());
        } else {
            // Berechtigtes Interesse: consent_given_at MUSS NULL sein
            if (request.getConsentGivenAt() != null) {
                throw new BadRequestException(
                    "Consent-Feld nur bei source=WEB_FORMULAR erlaubt"
                );
            }
        }
        // ... Lead erstellen
    }

    private void validateConsentTimestamp(LocalDateTime consentGivenAt) {
        LocalDateTime now = LocalDateTime.now();
        if (consentGivenAt.isAfter(now)) {
            throw new BadRequestException("consent_given_at darf nicht in der Zukunft liegen");
        }
        if (consentGivenAt.isBefore(now.minusHours(24))) {
            throw new BadRequestException("consent_given_at max. 24h alt (Cookie-Ablauf?)");
        }
    }
}
```

### Frontend-Validierung (LeadWizard.tsx)

```tsx
// Sprint 2.1.5: Nur UI-Validierung, kein Backend-Persist
// Sprint 2.1.6: Backend-Feld consent_given_at kommt hinzu

const LeadWizard = () => {
  const [consentGiven, setConsentGiven] = useState(false);
  const [source, setSource] = useState<LeadSource | null>(null);

  const handleSubmit = async (formData: LeadFormData) => {
    // DSGVO Consent-Check (Frontend-seitig)
    if (source === 'WEB_FORMULAR' && !consentGiven) {
      toast.error('Consent erforderlich für Web-Formular Leads');
      return;
    }

    // Sprint 2.1.6: consent_given_at mit senden
    const payload = {
      ...formData,
      consentGivenAt: source === 'WEB_FORMULAR' ? new Date().toISOString() : null
    };

    await createLead(payload);
  };

  return (
    <Box>
      {/* Source Dropdown */}
      <Select value={source} onChange={(e) => setSource(e.target.value)}>
        <MenuItem value="WEB_FORMULAR">Web-Formular</MenuItem>
        <MenuItem value="MESSE">Messe</MenuItem>
        <MenuItem value="EMPFEHLUNG">Empfehlung</MenuItem>
        <MenuItem value="TELEFON">Telefon</MenuItem>
      </Select>

      {/* Consent-Checkbox (nur bei WEB_FORMULAR) */}
      {source === 'WEB_FORMULAR' && (
        <FormControlLabel
          control={
            <Checkbox
              required
              checked={consentGiven}
              onChange={(e) => setConsentGiven(e.target.checked)}
            />
          }
          label="Ich stimme zu, dass meine Kontaktdaten ..."
        />
      )}

      {/* Info-Text (bei MESSE/EMPFEHLUNG/TELEFON) */}
      {source !== 'WEB_FORMULAR' && source !== null && (
        <Alert severity="info">
          Hinweis zum Datenschutz: Die Daten werden auf Basis berechtigten Interesses ...
        </Alert>
      )}

      <Button disabled={source === 'WEB_FORMULAR' && !consentGiven}>
        Lead erstellen
      </Button>
    </Box>
  );
};
```

## Audit-Events

### lead_consent_given

```json
{
  "event": "lead.consent.given",
  "leadId": "uuid",
  "consentGivenAt": "2025-10-03T14:30:00Z",
  "source": "WEB_FORMULAR",
  "userAgent": "Mozilla/5.0 ...",
  "ipAddress": "192.168.1.1"  // Optional, bei Web-Intake
}
```

### lead_consent_revoked

```json
{
  "event": "lead.consent.revoked",
  "leadId": "uuid",
  "revokedAt": "2025-11-01T10:00:00Z",
  "revokedBy": "lead-owner-uuid",
  "reason": "Widerruf per E-Mail (datenschutz@freshfoodz.de)"
}
```

## Widerruf-Prozess (Sprint 2.1.7+)

**Widerruf via E-Mail:**
1. Lead sendet E-Mail an `datenschutz@freshfoodz.de`
2. Admin-Team prüft Lead-ID (via Email-Adresse)
3. Admin setzt `consent_revoked_at = NOW()`
4. Lead wird pseudonymisiert (Name/Email/Telefon → `***WIDERRUFEN***`)
5. Audit-Event `lead_consent_revoked` wird erstellt

**Widerruf via UI (Self-Service):**
- Lead erhält Magic-Link in Bestätigungs-E-Mail
- Lead klickt auf "Widerruf" → Formular mit Grund (optional)
- Automatische Pseudonymisierung

## Datensparsamkeit (Art. 5 DSGVO)

### Progressive Profiling Stages

| Stage | Personenbezogene Daten | Consent nötig? |
|-------|----------------------|----------------|
| 0 (Vormerkung) | ❌ Keine (nur Firma/Stadt) | ❌ Nein |
| 1 (Registrierung) | ✅ Ja (Name/Email/Telefon) | ✅ Ja (bei WEB_FORMULAR) |
| 2 (Qualifiziert) | ✅ Ja (wie Stage 1) | ✅ Ja (bei WEB_FORMULAR) |

**Rationale:**
- Stage 0: Keine personenbezogenen Daten → kein Consent nötig
- Stage 1+: Kontaktdaten → Consent erforderlich (bei Web-Intake)

## Testing

### Unit Tests (LeadServiceTest.java)

```java
@Test
void shouldRequireConsentForWebFormularSource() {
    var request = LeadCreateRequest.builder()
        .source(LeadSource.WEB_FORMULAR)
        .consentGivenAt(null)  // ❌ Fehlt!
        .build();

    assertThatThrownBy(() -> leadService.createLead(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Consent erforderlich");
}

@Test
void shouldRejectConsentForNonWebFormularSource() {
    var request = LeadCreateRequest.builder()
        .source(LeadSource.MESSE)
        .consentGivenAt(LocalDateTime.now())  // ❌ Nicht erlaubt!
        .build();

    assertThatThrownBy(() -> leadService.createLead(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("nur bei source=WEB_FORMULAR erlaubt");
}
```

### Frontend Tests (LeadWizard.test.tsx)

```tsx
it('should disable submit when WEB_FORMULAR selected but no consent given', () => {
  render(<LeadWizard />);

  // Select WEB_FORMULAR
  const sourceSelect = screen.getByLabelText('Quelle');
  fireEvent.change(sourceSelect, { target: { value: 'WEB_FORMULAR' } });

  // Consent-Checkbox nicht angekreuzt
  const submitButton = screen.getByRole('button', { name: /Lead erstellen/i });
  expect(submitButton).toBeDisabled();

  // Consent-Checkbox ankreuzen
  const consentCheckbox = screen.getByRole('checkbox', { name: /Ich stimme zu/i });
  fireEvent.click(consentCheckbox);

  expect(submitButton).toBeEnabled();
});

it('should NOT show consent checkbox for MESSE source', () => {
  render(<LeadWizard />);

  const sourceSelect = screen.getByLabelText('Quelle');
  fireEvent.change(sourceSelect, { target: { value: 'MESSE' } });

  const consentCheckbox = screen.queryByRole('checkbox', { name: /Ich stimme zu/i });
  expect(consentCheckbox).not.toBeInTheDocument();

  // Info-Text sollte sichtbar sein
  expect(screen.getByText(/berechtigten Interesses/i)).toBeInTheDocument();
});
```

## Rechtliche Absicherung (Dokumentation)

### Verarbeitungsverzeichnis (Art. 30 DSGVO)

**Eintrag für Lead-Verwaltung:**
- Zweck: B2B-Geschäftsanbahnung (Art. 6 Abs. 1 lit. f DSGVO)
- Rechtsgrundlage: Berechtigtes Interesse ODER Einwilligung (Art. 6 Abs. 1 lit. a)
- Kategorien: Kontaktdaten (Name, E-Mail, Telefon, Firma)
- Empfänger: FreshFoodz Vertriebsmitarbeiter (RBAC-gesichert)
- Speicherdauer: 6 Monate Lead-Schutz + 60 Tage Retention → Pseudonymisierung

### Datenschutz-Folgenabschätzung (DSFA)

**Risikobewertung:**
- Risiko: Gering (B2B-Daten, keine sensiblen Kategorien nach Art. 9 DSGVO)
- Schutzbedarf: Normal (Vertraulichkeit, Integrität)
- DSFA erforderlich? Nein (Art. 35 Abs. 3 DSGVO nicht erfüllt)

**Schutzmaßnahmen:**
- RBAC: Nur zugewiesene Partner sehen eigene Leads
- Verschlüsselung: TLS 1.3 in Transit, AES-256 at Rest
- Audit-Log: Alle Zugriffe protokolliert
- Pseudonymisierung: Nach 60 Tagen ohne Progress

## Nächste Schritte (Sprint 2.1.7+)

- [ ] Self-Service Widerruf-Portal (Magic-Link)
- [ ] Automatische E-Mail-Benachrichtigung bei Consent-Anfragen
- [ ] Datenschutz-Dashboard für Leads (Einsicht/Widerruf/Löschung)
- [ ] Integration mit Consent-Management-Plattform (z.B. OneTrust)

## Referenzen

- [Art. 6 DSGVO - Rechtmäßigkeit der Verarbeitung](https://dsgvo-gesetz.de/art-6-dsgvo/)
- [Art. 13 DSGVO - Informationspflichten](https://dsgvo-gesetz.de/art-13-dsgvo/)
- [Art. 30 DSGVO - Verzeichnis von Verarbeitungstätigkeiten](https://dsgvo-gesetz.de/art-30-dsgvo/)
- [Bundesdatenschutzbeauftragter - B2B Lead-Verarbeitung](https://www.bfdi.bund.de/)

---

**Letzte Aktualisierung:** 2025-10-03
**Nächste Review:** Sprint 2.1.6 Start (12.10.2025)
**Status:** Draft (für Web-Intake Sprint 2.1.6)
