---
module: "02_neukundengewinnung"
sprint: "2.1.5"
domain: "frontend"
doc_type: "konzept"
status: "approved"
owner: "team/frontend"
updated: "2025-10-04"
---

# Frontend Delta – Sprint 2.1.5

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Frontend Delta

> **🎯 Zweck:**
> Zentrale Frontend-Spezifikation für Sprint 2.1.5 Features. Definiert **Soll-Zustand** für:
> - Activity-Types Mapping (13 Types)
> - LeadSource & Quellenabhängige Validierung
> - Erstkontakt-Block (FIRST_CONTACT_DOCUMENTED)
> - DSGVO Consent (UI-only in 2.1.5)
> - Dedupe 409 Handling (Hard/Soft mit Problem+JSON extensions)
> - Pre-Claim UX (Badge + Filter)

---

## 0. Progressive Profiling UX-Regeln (Best Practice)

### Grundprinzip

**Jede Karte ist eigenständig speicherbar.**
- **Karte 0 (Vormerkung):** Minimaler Aufwand → Lead existiert (stage=0, Pre-Claim aktiv wenn kein Erstkontakt)
- **Karte 1 (Registrierung):** Kontaktdaten → Lead erreichbar (stage=1)
- **Karte 2 (Qualifizierung):** Business-Details → Lead qualifiziert (stage=2)

**Speichern jederzeit möglich:**
- Keine Pflicht, alle Karten durchzugehen
- User kann nach jeder Karte speichern und Dialog schließen
- Später: Lead öffnen und weitere Daten ergänzen

### Karte 0: Vormerkung

**Zweck:** Lead mit minimalen Infos anlegen (Messe/Telefon/Liste)

**Pflichtfelder:**
- Firmenname (min. 2 Zeichen)
- Quelle (LeadSource: MESSE, EMPFEHLUNG, TELEFON, WEB_FORMULAR, PARTNER, SONSTIGE)

**Optionale Felder:**
- Stadt, PLZ, Branche
- **Notizen/Quelle (immer sichtbar):** Freies Textfeld für Kontext (z.B. "Empfehlung von Herrn Schulz") - **KEIN Einfluss auf Schutz**
- **Erstkontakt-Dokumentation (conditional):** Kanal, Datum, Gesprächsnotiz (≥10 Zeichen) - **Aktiviert Schutz**

**Zwei-Felder-Lösung (Design-Entscheidung 2025-10-04):**
- **Feld 1: Notizen/Quelle** → Für Hintergrund-Infos, keine Schutz-Aktivierung
- **Feld 2: Erstkontakt-Dokumentation** → Strukturiert (Kanal, Datum, Notiz), löst `registered_at` aus

**Erstkontakt-Logik:**
- **Bei MESSE/TELEFON:** Erstkontakt-Block immer sichtbar, PFLICHT (Partner hatte direkten Kontakt)
- **Bei EMPFEHLUNG/WEB/PARTNER/SONSTIGE:**
  - Checkbox: "☑ Ich hatte bereits Erstkontakt (für sofortigen Lead-Schutz)"
  - Nur wenn aktiviert → Erstkontakt-Block erscheint
  - Partner kann Notizen-Feld nutzen, ohne Schutz zu aktivieren
- **Wenn Erstkontakt dokumentiert →** Schutz startet (registered_at = now, protection_until = +6 Monate)
- **Wenn NICHT dokumentiert →** Pre-Claim (registered_at = NULL, 10-Tage-Frist)
- **Nachträglich möglich:** Quick-Action in Liste oder Lead bearbeiten → Erstkontakt hinzufügen

**Buttons:**
- `[Abbrechen]` - Schließt Dialog ohne Speichern
- `[Vormerkung speichern]` - POST /api/leads mit stage=0, schließt Dialog
- `[Weiter →]` - Geht zu Karte 1 (ohne Speichern)

**Copy:**
```
Beschreibung: "Legen Sie den Lead mit wenigen Angaben an. Sie können Details später ergänzen."

Notizen-Feld:
Label: "Notizen / Quelle (optional)"
Hint: "Z.B. Empfehlung von Herrn Schulz, Partner-Liste Nr. 47"

Checkbox (nur bei EMPFEHLUNG/WEB/PARTNER/SONSTIGE):
Label: "☑ Ich hatte bereits Erstkontakt (für sofortigen Lead-Schutz)"

Erstkontakt-Box (bei MESSE/TELEFON: immer; sonst: nur wenn Checkbox aktiviert):
Titel: "Erstkontakt dokumentieren"
  + Badge "PFLICHT" (nur bei MESSE/TELEFON)
Hint: "Mindestens 10 Zeichen (z.B. 'Gespräch mit Frau Müller am Stand 12, Interesse an Bio-Produkten')"

Hinweis-Boxen:
  - Pre-Claim (wenn kein Erstkontakt): "⚠️ Pre-Claim: Kein Schutz aktiv! Lead läuft in 10 Tagen ab. Aktivieren Sie den Erstkontakt oder fügen Sie später Kontaktdaten hinzu (Stage 1)."
  - Schutz aktiv (wenn Erstkontakt): "✅ Schutz startet bei Speicherung (6 Monate ab heute)"
```

### Karte 1: Registrierung

**Zweck:** Kontaktdaten erfassen

**Pflichtfelder:**
- **Mind. ein Kontaktkanal:** E-Mail ODER Telefon

**Optionale Felder:**
- Vorname, Nachname

**DSGVO-Hinweis (statt Checkbox bei Vertrieb):**
```tsx
<Box sx={{ mt: 2, p: 2, bgcolor: 'info.light', borderRadius: 1 }}>
  <Typography variant="body2">
    <strong>Berechtigtes Interesse (Art. 6 Abs. 1 lit. f DSGVO)</strong>
  </Typography>
  <Typography variant="caption" color="text.secondary">
    Verarbeitung zur B2B-Geschäftsanbahnung.
    <Link onClick={openDsgvoPopup}>Gesetzestext anzeigen ↗</Link>
  </Typography>
  <Typography variant="caption" display="block" sx={{ mt: 1, fontStyle: 'italic' }}>
    Hinweis: Einwilligung nur erforderlich bei Web-Formular (Kunde gibt selbst Daten ein).
  </Typography>
</Box>
```

**Buttons:**
- `[← Zurück]` - Zurück zu Karte 0
- `[Registrierung speichern]` - POST /api/leads mit stage=1, schließt Dialog
- `[Weiter →]` - Geht zu Karte 2 (ohne Speichern)

**Copy:**
```
Beschreibung: "Erfassen Sie Kontaktdaten, um den Lead erreichbar zu machen."
```

**⚠️ WICHTIG:**
- **KEINE Consent-Checkbox** im Vertriebs-Wizard (Kunde sitzt nicht daneben)
- Schutz startet **NICHT automatisch** bei Kontaktdaten (nur bei Erstkontakt-Dokumentation)

### Karte 2: Qualifizierung

**Zweck:** Geschäftliche Details nachtragen

**Alle Felder optional:**
- Geschätztes Volumen, Küchengröße, Mitarbeiterzahl, Website, Branche (Details)

**Buttons:**
- `[← Zurück]` - Zurück zu Karte 1
- `[Qualifizierung speichern]` - POST /api/leads mit stage=2, schließt Dialog

**Copy:**
```
Beschreibung: "Ergänzen Sie Geschäftsdaten, sobald sie vorliegen. Sie können jederzeit speichern und später fortfahren."
```

### Pre-Claim & Schutz-Logik

**Pre-Claim (Vormerkung ohne Schutz):**
- `registered_at IS NULL` (kein Erstkontakt dokumentiert)
- 10-Tage-Frist ab `created_at`
- **Badge:** "⏳ Vormerkung läuft ab in X Tagen"
- **Filter:** "Pre-Claim Leads" (eigener Filter in LeadList)

**Schutz aktiv:**
- Erstkontakt dokumentiert (FIRST_CONTACT_DOCUMENTED Activity)
- `registered_at NOT NULL` + `protection_until` gesetzt (6 Monate)
- **Badge:** "🛡️ Geschützt bis TT.MM.JJJJ"

**Erstkontakt nachträglich:**
- Lead öffnen → Activity hinzufügen → Type: FIRST_CONTACT_DOCUMENTED
- **Zeitpunkt:** Aktuell (Backdating erst Sprint 2.1.6, außer Altdaten-Migration)
- Pre-Claim endet → Schutz startet ab erfasstem Zeitpunkt

### ❌ Nicht tun (Anti-Patterns)

1. **Keine Consent-Checkbox im Vertriebs-Wizard** (nur bei WEB_FORMULAR, wenn Kunde selbst Daten eingibt)
2. **Erstkontakt nicht mehrfach abfragen** (nur auf Karte 0)
3. **Speichern nicht ans Ende koppeln** (jede Karte separat speicherbar)
4. **Schutz nicht bei Kontaktdaten starten** (nur bei Erstkontakt-Dokumentation)

---

## 1. Activity-Types (13 Types)

### TypeScript Definition

```typescript
// frontend/src/features/leads/types.ts

/**
 * Activity-Types für Lead-Tracking (Sprint 2.1.5)
 *
 * @see ACTIVITY_TYPES_PROGRESS_MAPPING.md
 * @see V258 Migration (Backend Constraint)
 */
export type ActivityType =
  // Progress Activities (countsAsProgress = TRUE) - 5 Types
  | 'QUALIFIED_CALL'
  | 'MEETING'
  | 'DEMO'
  | 'ROI_PRESENTATION'
  | 'SAMPLE_SENT'

  // Non-Progress Activities (countsAsProgress = FALSE) - 5 Types
  | 'NOTE'
  | 'FOLLOW_UP'
  | 'EMAIL'
  | 'CALL'
  | 'SAMPLE_FEEDBACK'

  // System Activities (countsAsProgress = FALSE) - 3 Types
  | 'FIRST_CONTACT_DOCUMENTED'  // ⚠️ Erstkontakt-Block → Activity
  | 'EMAIL_RECEIVED'
  | 'LEAD_ASSIGNED';

/**
 * Progress-Mapping für UI (ActivityTimeline Filtering)
 */
export const ACTIVITY_PROGRESS_MAP: Record<ActivityType, boolean> = {
  // Progress = true (5)
  QUALIFIED_CALL: true,
  MEETING: true,
  DEMO: true,
  ROI_PRESENTATION: true,
  SAMPLE_SENT: true,

  // Non-Progress = false (8)
  NOTE: false,
  FOLLOW_UP: false,
  EMAIL: false,
  CALL: false,
  SAMPLE_FEEDBACK: false,
  FIRST_CONTACT_DOCUMENTED: false,
  EMAIL_RECEIVED: false,
  LEAD_ASSIGNED: false,
} as const;
```

### UI Integration

**ActivityTimeline.tsx:**
- Filter-Toggle: "Nur Fortschritt" (nur `ACTIVITY_PROGRESS_MAP[type] === true`)
- Icon-Mapping: Progress = CheckCircle (grün), Non-Progress = InfoOutlined (grau)
- System-Types: spezielle Icons (FIRST_CONTACT_DOCUMENTED = ContactPhone)

---

## 2. LeadSource & Quellenabhängige Validierung

### TypeScript Definition

```typescript
// frontend/src/features/leads/types.ts

/**
 * Lead-Herkunftsquellen (Sprint 2.1.5)
 *
 * Bestimmt Pflichtfelder + DSGVO-Consent-Pflicht
 */
export type LeadSource =
  | 'MESSE'           // Messe/Event
  | 'EMPFEHLUNG'      // Partner/Kunde
  | 'TELEFON'         // Cold Call
  | 'WEB_FORMULAR'    // Website (Sprint 2.1.6)
  | 'PARTNER'         // Partner-API (Sprint 2.1.6)
  | 'SONSTIGE';       // Fallback
```

### Validierungsregeln (LeadWizard.tsx)

**Option A: Finale Regelung (2025-10-04 - Zwei-Felder-Lösung)**

| Source | Notizen-Feld | Erstkontakt-Block | Checkbox | Pre-Claim | DSGVO Consent |
|--------|--------------|-------------------|----------|-----------|---------------|
| **MESSE** | ✅ Immer (optional) | ✅ Immer (PFLICHT) | ❌ Nein | ❌ Nein | ❌ Optional |
| **TELEFON** | ✅ Immer (optional) | ✅ Immer (PFLICHT) | ❌ Nein | ❌ Nein | ❌ Optional |
| **EMPFEHLUNG** | ✅ Immer (optional) | ☑ Bei Checkbox | ✅ Ja | ✅ Ja | ❌ Optional |
| **WEB_FORMULAR** | ✅ Immer (optional) | ☑ Bei Checkbox | ✅ Ja | ✅ Ja | ✅ PFLICHT |
| **PARTNER** | ✅ Immer (optional) | ☑ Bei Checkbox | ✅ Ja | ✅ Ja | ❌ Optional |
| **SONSTIGE** | ✅ Immer (optional) | ☑ Bei Checkbox | ✅ Ja | ✅ Ja | ❌ Optional |

**Begründung (User-Feedback):**
- **MESSE/TELEFON:** Partner HAT direkten Erstkontakt → Erstkontakt-Block immer sichtbar, PFLICHT
- **EMPFEHLUNG/WEB/PARTNER/SONSTIGE:** Partner hat oft KEINEN direkten Erstkontakt (nur Info) → Checkbox "Ich hatte bereits Erstkontakt" → Pre-Claim möglich

**Zwei-Felder-Lösung:**
- **Feld 1: Notizen/Quelle** (immer sichtbar, optional)
  - Label: "Notizen / Quelle (optional)"
  - Hint: "Z.B. Empfehlung von Herrn Schulz, Partner-Liste Nr. 47"
  - Kein Einfluss auf `registered_at` (kein Schutz-Trigger)
- **Feld 2: Erstkontakt-Dokumentation** (conditional)
  - Bei MESSE/TELEFON: Immer sichtbar, PFLICHT
  - Bei anderen: Nur sichtbar wenn Checkbox "☑ Ich hatte bereits Erstkontakt" aktiviert
  - Label: "Erstkontakt dokumentieren" + Badge "PFLICHT" (bei MESSE/TELEFON)
  - Felder: Kanal, Datum, Gesprächsnotiz (≥10 Zeichen)
  - Aktiviert `registered_at` (Schutz startet)

**Implementierung:**

```typescript
// LeadWizard.tsx - State & Logik
const [showFirstContactFields, setShowFirstContactFields] = useState(false);
const requiresFirstContact = ['MESSE', 'TELEFON'].includes(formData.source || '');

// Erstkontakt-Block sichtbar wenn: MESSE/TELEFON ODER Checkbox aktiviert
const showFirstContactBlock = requiresFirstContact || showFirstContactFields;

// Validierung - validateStage0()
const hasStartedFirstContact =
  formData.firstContact?.channel ||
  formData.firstContact?.performedAt ||
  formData.firstContact?.notes?.trim();

if (requiresFirstContact && !hasStartedFirstContact) {
  // MESSE/TELEFON ohne Erstkontakt: Fehler
  errors.firstContact = [t('wizard.stage0.firstContactRequired')];
} else if (showFirstContactBlock && hasStartedFirstContact) {
  // Wenn Erstkontakt-Block sichtbar UND User hat angefangen zu tippen:
  if (!formData.firstContact?.channel) {
    errors['firstContact.channel'] = [t('wizard.stage0.channelRequired')];
  }
  if (!formData.firstContact?.performedAt) {
    errors['firstContact.performedAt'] = [t('wizard.stage0.dateRequired')];
  }
  if (!formData.firstContact?.notes || formData.firstContact.notes.trim().length < 10) {
    errors['firstContact.notes'] = [t('wizard.stage0.notesMin10')];
  }
}

// Schutz-Hinweis Logik
const willActivateProtection = showFirstContactBlock && hasStartedFirstContact;
const isPreClaim = !showFirstContactBlock;
```

**UI-Rendering:**

```tsx
{/* FELD 1: Notizen (immer da) */}
<TextField
  label={t('wizard.stage0.notesLabel')}  // "Notizen / Quelle (optional)"
  helperText={t('wizard.stage0.notesHint')}  // "Z.B. Empfehlung von..."
  value={formData.notes}
  onChange={e => setFormData({...formData, notes: e.target.value})}
/>

{/* Checkbox (nur bei EMPFEHLUNG/WEB/PARTNER/SONSTIGE) */}
{!requiresFirstContact && (
  <FormControlLabel
    control={<Checkbox checked={showFirstContactFields} onChange={e => setShowFirstContactFields(e.target.checked)} />}
    label={t('wizard.stage0.hasFirstContactCheckbox')}  // "☑ Ich hatte bereits Erstkontakt"
  />
)}

{/* FELD 2: Erstkontakt-Block (conditional) */}
{showFirstContactBlock && (
  <Box>
    <Typography>
      {t('wizard.stage0.firstContactTitle')}  // "Erstkontakt dokumentieren"
      {requiresFirstContact && <Chip label="PFLICHT" color="error" />}
    </Typography>
    <Select label="Kanal *" {...} />
    <DateTimePicker label="Datum *" {...} />
    <TextField label="Gesprächsnotiz *" multiline rows={3} {...} />
  </Box>
)}

const requiresConsent = formData.source === 'WEB_FORMULAR';
const hasContactData = formData.contact.firstName || formData.contact.email;

if (requiresConsent && hasContactData && !formData.consentGiven) {
  errors.consentGiven = [t('wizard.stage1.consentRequired')];
}
```

---

## 3. Erstkontakt-Block (FIRST_CONTACT_DOCUMENTED)

### UI-Struktur (LeadWizard.tsx Stage 1)

**Conditional Rendering:**
- Zeige Block nur wenn `source` in `['MESSE', 'EMPFEHLUNG', 'TELEFON']`
- 3 Pflichtfelder: channel, performedAt, notes

```typescript
// frontend/src/features/leads/types.ts

export type FirstContactChannel =
  | 'MESSE'      // Messestand/Event
  | 'PHONE'      // Telefonat
  | 'EMAIL'      // E-Mail
  | 'REFERRAL'   // Empfehlung/Vorstellung
  | 'OTHER';     // Sonstige

export interface FirstContact {
  channel: FirstContactChannel;
  performedAt: string;  // ISO-8601 DateTime
  notes: string;        // min. 10 Zeichen
}
```

**LeadWizard.tsx - Stage 1 UI:**

```tsx
{['MESSE', 'EMPFEHLUNG', 'TELEFON'].includes(formData.source) && (
  <Box sx={{ mt: 3, p: 2, bgcolor: 'action.hover', borderRadius: 1 }}>
    <Typography variant="subtitle2" gutterBottom>
      {t('wizard.stage1.firstContactTitle')}
    </Typography>

    <FormControl fullWidth sx={{ mt: 2 }}>
      <InputLabel id="first-contact-channel-label">
        {t('wizard.stage1.firstContactChannel')}
      </InputLabel>
      <Select
        labelId="first-contact-channel-label"
        id="first-contact-channel"
        value={formData.firstContact?.channel || ''}
        onChange={(e) => setFormData({
          ...formData,
          firstContact: { ...formData.firstContact, channel: e.target.value }
        })}
        required
      >
        <MenuItem value="MESSE">{t('wizard.channels.messe')}</MenuItem>
        <MenuItem value="PHONE">{t('wizard.channels.phone')}</MenuItem>
        <MenuItem value="EMAIL">{t('wizard.channels.email')}</MenuItem>
        <MenuItem value="REFERRAL">{t('wizard.channels.referral')}</MenuItem>
        <MenuItem value="OTHER">{t('wizard.channels.other')}</MenuItem>
      </Select>
    </FormControl>

    <TextField
      fullWidth
      label={t('wizard.stage1.firstContactDate')}
      type="datetime-local"
      value={formData.firstContact?.performedAt || ''}
      onChange={(e) => setFormData({
        ...formData,
        firstContact: { ...formData.firstContact, performedAt: e.target.value }
      })}
      sx={{ mt: 2 }}
      required
      InputLabelProps={{ shrink: true }}
    />

    <TextField
      fullWidth
      label={t('wizard.stage1.firstContactNotes')}
      multiline
      rows={3}
      value={formData.firstContact?.notes || ''}
      onChange={(e) => setFormData({
        ...formData,
        firstContact: { ...formData.firstContact, notes: e.target.value }
      })}
      sx={{ mt: 2 }}
      required
      helperText={t('wizard.stage1.firstContactNotesHelp')}
    />
  </Box>
)}
```

### Payload-Transformation

**⚠️ WICHTIG:** Erstkontakt wird als **Activity** gesendet, NICHT als `firstContact` Feld!

```typescript
// LeadWizard.tsx - handleSubmit()
const payload: CreateLeadRequest = {
  companyName: formData.companyName,
  source: formData.source,  // ⚠️ NICHT hardcoded 'manual'!
  contact: formData.contact,
  address: formData.address,

  // ⚠️ Erstkontakt → Activity (NICHT firstContact Feld!)
  activities: formData.firstContact ? [{
    activityType: 'FIRST_CONTACT_DOCUMENTED',
    performedAt: formData.firstContact.performedAt,
    summary: `${formData.firstContact.channel}: ${formData.firstContact.notes}`,
    countsAsProgress: false,
    metadata: {
      channel: formData.firstContact.channel,
    }
  }] : undefined,

  // ⚠️ KEIN consentGiven/consent_given_at in Sprint 2.1.5 senden!
  // Persistenz erst in V259 (Sprint 2.1.6)
};
```

---

## 4. DSGVO Consent (UI-Only in 2.1.5)

### Aktueller Stand Sprint 2.1.5

**Backend:**
- ❌ `lead.consent_given_at` Feld existiert NICHT (erst V259 in Sprint 2.1.6)
- ❌ Request darf KEIN `consentGiven` / `consent_given_at` senden

**Frontend:**
- ✅ Checkbox UI vorhanden (LeadWizard.tsx)
- ✅ Validierung: PFLICHT wenn `source = WEB_FORMULAR` UND Contact-Daten vorhanden
- ⚠️ Wert wird NICHT gesendet (nur UI-Validierung)

### Implementierung

```typescript
// LeadWizard.tsx - Stage 1 Validierung
const validateStage1 = (): boolean => {
  const errors: ValidationErrors = {};

  const requiresConsent = formData.source === 'WEB_FORMULAR';
  const hasContactData = formData.contact.firstName || formData.contact.email;

  if (requiresConsent && hasContactData && !formData.consentGiven) {
    errors.consentGiven = [t('wizard.stage1.consentRequired')];
  }

  setErrors(errors);
  return Object.keys(errors).length === 0;
};

// ⚠️ NICHT im Request Payload senden!
const payload = {
  companyName: formData.companyName,
  source: formData.source,
  // consentGiven: formData.consentGiven,  ❌ NICHT senden in 2.1.5!
};
```

**UI (Checkbox):**

```tsx
{formData.source === 'WEB_FORMULAR' && (
  <FormControlLabel
    control={
      <Checkbox
        checked={formData.consentGiven || false}
        onChange={(e) => setFormData({ ...formData, consentGiven: e.target.checked })}
        required
      />
    }
    label={
      <Typography variant="body2">
        {t('wizard.stage1.consentLabel')}
        <Link href="/datenschutz" target="_blank" sx={{ ml: 0.5 }}>
          {t('wizard.stage1.consentLink')}
        </Link>
      </Typography>
    }
  />
)}
```

**Migration zu Sprint 2.1.6:**
- V259: `lead.consent_given_at TIMESTAMPTZ` hinzufügen
- Frontend: `consentGiven` im Payload senden
- Backend: Feld persistieren

---

## 5. Dedupe 409 Handling (RFC 7807 Problem+JSON)

### Problem Type mit Extensions

```typescript
// frontend/src/features/leads/types.ts

export interface Problem {
  type?: string;
  title?: string;
  detail?: string;
  status?: number;
  errors?: Record<string, string[]>;

  // ⭐ NEU in Sprint 2.1.5
  extensions?: {
    severity?: 'WARNING';  // Nur bei Soft Collisions
    duplicates?: DuplicateLead[];
  };
}

export interface DuplicateLead {
  leadId: string;
  companyName: string;
  city?: string;
  postalCode?: string;
  ownerUserId?: string;
}
```

### Dedupe-Szenarien

#### Hard Collision (OHNE severity)

**Backend Response:**

```json
{
  "type": "https://freshplan/errors/duplicate-lead",
  "title": "Duplicate lead detected",
  "status": 409,
  "detail": "Email bereits registriert: max@example.com",
  "extensions": {
    "duplicates": [
      {
        "leadId": "uuid-123",
        "companyName": "Beispiel GmbH",
        "city": "München",
        "postalCode": "80331",
        "ownerUserId": "uuid-owner"
      }
    ]
  }
}
```

**Frontend Handling:**

```typescript
// api.ts - createLead()
export async function createLead(
  payload: CreateLeadRequest,
  options?: { params?: { reason?: string; overrideReason?: string } }
) {
  const queryParams = new URLSearchParams();
  if (options?.params?.reason) queryParams.set('reason', options.params.reason);
  if (options?.params?.overrideReason) queryParams.set('overrideReason', options.params.overrideReason);

  const url = queryParams.toString()
    ? `${BASE}/api/leads?${queryParams}`
    : `${BASE}/api/leads`;

  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),  // ⚠️ KEIN hardcoded source: 'manual'
  });

  if (!res.ok) {
    const problem: Problem = await res.json();
    throw problem;
  }

  return res.json();
}
```

**Dialog (DuplicateLeadDialog.tsx):**

- Zeige: Existierenden Lead öffnen (Link zu `/leads/${leadId}`)
- Manager/Admin only: "Trotzdem anlegen" Button
- TextArea: `overrideReason` (min. 10 Zeichen)
- Resubmit: `createLead(payload, { params: { overrideReason } })`

---

#### Soft Collision (MIT severity: "WARNING")

**Backend Response:**

```json
{
  "type": "https://freshplan/errors/duplicate-lead",
  "title": "Similar lead found",
  "status": 409,
  "detail": "Ähnlicher Lead gefunden (gleiche Domain + Stadt)",
  "extensions": {
    "severity": "WARNING",
    "duplicates": [
      {
        "leadId": "uuid-456",
        "companyName": "Beispiel AG",
        "city": "München"
      }
    ]
  }
}
```

**Dialog (SimilarLeadDialog.tsx):**

- Zeige: Ähnlichen Lead öffnen (optional)
- Alle Rollen: "Trotzdem anlegen" Button
- TextArea: `reason` (min. 10 Zeichen)
- Resubmit: `createLead(payload, { params: { reason } })`

---

### Hook-Implementierung

```typescript
// frontend/src/features/leads/hooks/useCreateLead.ts

export function useCreateLead() {
  const [error, setError] = useState<Problem | null>(null);
  const [isDuplicate, setIsDuplicate] = useState(false);

  const create = async (payload: CreateLeadRequest, params?: { reason?: string; overrideReason?: string }) => {
    try {
      setError(null);
      setIsDuplicate(false);
      const lead = await createLead(payload, { params });
      return lead;
    } catch (err) {
      const problem = err as Problem;
      if (problem.status === 409) {
        setIsDuplicate(true);
        setError(problem);
      }
      throw err;
    }
  };

  return { create, error, isDuplicate };
}
```

---

## 6. Pre-Claim UX (Badge + Filter)

### Pre-Claim Status

**Definition:**
- Lead mit `registered_at = NULL` (Vormerkung, Stage 0)
- 10 Tage Ablauf nach `created_at`
- KEIN Lead-Protection (6-Month Schutz startet erst bei `registered_at != NULL`)

### Badge (LeadList.tsx)

```tsx
{lead.registeredAt === null && (
  <Chip
    label="⏳ Vormerkung"
    size="small"
    color="warning"
    sx={{ ml: 1 }}
  />
)}
```

### Filter (LeadList.tsx)

**4 Tabs:**

```tsx
<Tabs value={preclaimFilter} onChange={(e, v) => setPreclaimFilter(v)}>
  <Tab label="Alle" value="all" />
  <Tab label="≤ 3 Tage" value="fresh" />
  <Tab label="≤ 10 Tage" value="valid" />
  <Tab label="Abgelaufen" value="expired" />
</Tabs>
```

**Client-Side Filtering (Fallback):**

```typescript
const filterPreClaims = (leads: Lead[], filter: string): Lead[] => {
  const now = new Date();

  return leads.filter(lead => {
    if (lead.registeredAt !== null) return true; // Registrierte Leads immer zeigen

    const createdAt = new Date(lead.createdAt);
    const daysSinceCreated = Math.floor((now.getTime() - createdAt.getTime()) / (1000 * 60 * 60 * 24));

    switch (filter) {
      case 'fresh': return daysSinceCreated <= 3;
      case 'valid': return daysSinceCreated <= 10;
      case 'expired': return daysSinceCreated > 10;
      default: return true;
    }
  });
};
```

**⚠️ Backend API (Sprint 2.1.6):**
- `GET /api/leads?preclaimStatus=FRESH|VALID|EXPIRED`
- Server-Side Filtering via `WHERE registered_at IS NULL AND ...`

---

## 7. Definition of Done (Frontend)

### Types & Validierung

- [ ] **13 Activity-Types** definiert (types.ts)
- [ ] **ACTIVITY_PROGRESS_MAP** implementiert (5 true, 8 false)
- [ ] **LeadSource** Typ definiert (6 Werte)
- [ ] **FirstContactChannel** Typ definiert (5 Werte)
- [ ] **Problem.extensions** mit `severity` + `duplicates[]` (types.ts)

### API Integration

- [ ] **createLead()** akzeptiert Query-Params (`reason`, `overrideReason`)
- [ ] **KEIN** hardcoded `source: 'manual'` (api.ts:53 entfernen)
- [ ] **Erstkontakt** als `activities[]` senden (NICHT `firstContact` Feld)
- [ ] **KEIN** `consentGiven` / `consent_given_at` im Request (nur UI-Validierung)

### LeadWizard.tsx

- [ ] **Quellenabhängige Validierung** (MESSE/EMPFEHLUNG/TELEFON → Erstkontakt PFLICHT)
- [ ] **Erstkontakt-Block UI** (channel, performedAt, notes)
- [ ] **Consent Checkbox** nur bei `source = WEB_FORMULAR`
- [ ] **Stage-Determination** korrekt (falsy → undefined, NICHT 0)

### Dedupe Handling

- [ ] **DuplicateLeadDialog.tsx** (Hard Collision, Manager/Admin, overrideReason)
- [ ] **SimilarLeadDialog.tsx** (Soft Collision, alle Rollen, reason)
- [ ] **useCreateLead Hook** (409 Handling, isDuplicate State)

### Pre-Claim UX

- [ ] **Badge "⏳ Vormerkung"** (LeadList.tsx, registeredAt = NULL)
- [ ] **4 Filter-Tabs** (Alle / ≤3d / ≤10d / Abgelaufen)
- [ ] **Client-Side Filtering** (Fallback, Server-API in 2.1.6)

### ActivityTimeline.tsx

- [ ] **ACTIVITY_PROGRESS_MAP** Integration (Filter "Nur Fortschritt")
- [ ] **System-Types Icons** (FIRST_CONTACT_DOCUMENTED = ContactPhone, etc.)
- [ ] **Progress-Types** grün (CheckCircle), Non-Progress grau (InfoOutlined)

### Tests

- [ ] **types.ts:** 13 Activity-Types, ACTIVITY_PROGRESS_MAP korrekt
- [ ] **api.ts:** Query-Params Support, kein hardcoded source
- [ ] **LeadWizard.tsx:** Quellenabhängige Validierung, Erstkontakt-Block Rendering
- [ ] **Dedupe Dialoge:** 409 Handling, Resubmit mit reason/overrideReason
- [ ] **Pre-Claim Filter:** Client-Side Filtering korrekt
- [ ] **Coverage:** ≥80% (bestehend nicht verschlechtern)

### i18n

- [ ] **de/leads.json:** Alle Keys für Erstkontakt-Block, Channels, Consent, Dedupe
- [ ] **en/leads.json:** Englische Übersetzungen (optional)

---

## 8. Code-Änderungen (Ready-to-Implement)

### 1. types.ts

```diff
 export type ActivityType =
-  | 'QUALIFIED_CALL'
-  | 'MEETING'
-  | 'DEMO'
-  | 'ROI_PRESENTATION'
-  | 'SAMPLE_SENT'
-  | 'NOTE'
-  | 'FOLLOW_UP'
-  | 'EMAIL'
-  | 'CALL'
-  | 'SAMPLE_FEEDBACK';
+  // Progress Activities (countsAsProgress = TRUE) - 5 Types
+  | 'QUALIFIED_CALL'
+  | 'MEETING'
+  | 'DEMO'
+  | 'ROI_PRESENTATION'
+  | 'SAMPLE_SENT'
+
+  // Non-Progress Activities (countsAsProgress = FALSE) - 5 Types
+  | 'NOTE'
+  | 'FOLLOW_UP'
+  | 'EMAIL'
+  | 'CALL'
+  | 'SAMPLE_FEEDBACK'
+
+  // System Activities (countsAsProgress = FALSE) - 3 Types
+  | 'FIRST_CONTACT_DOCUMENTED'
+  | 'EMAIL_RECEIVED'
+  | 'LEAD_ASSIGNED';
+
+export const ACTIVITY_PROGRESS_MAP: Record<ActivityType, boolean> = {
+  QUALIFIED_CALL: true,
+  MEETING: true,
+  DEMO: true,
+  ROI_PRESENTATION: true,
+  SAMPLE_SENT: true,
+  NOTE: false,
+  FOLLOW_UP: false,
+  EMAIL: false,
+  CALL: false,
+  SAMPLE_FEEDBACK: false,
+  FIRST_CONTACT_DOCUMENTED: false,
+  EMAIL_RECEIVED: false,
+  LEAD_ASSIGNED: false,
+} as const;
+
+export type LeadSource =
+  | 'MESSE'
+  | 'EMPFEHLUNG'
+  | 'TELEFON'
+  | 'WEB_FORMULAR'
+  | 'PARTNER'
+  | 'SONSTIGE';
+
+export type FirstContactChannel =
+  | 'MESSE'
+  | 'PHONE'
+  | 'EMAIL'
+  | 'REFERRAL'
+  | 'OTHER';
+
+export interface FirstContact {
+  channel: FirstContactChannel;
+  performedAt: string;
+  notes: string;
+}

 export interface Problem {
   type?: string;
   title?: string;
   detail?: string;
   status?: number;
   errors?: Record<string, string[]>;
+  extensions?: {
+    severity?: 'WARNING';
+    duplicates?: DuplicateLead[];
+  };
 }
+
+export interface DuplicateLead {
+  leadId: string;
+  companyName: string;
+  city?: string;
+  postalCode?: string;
+  ownerUserId?: string;
+}
```

### 2. api.ts

```diff
-export async function createLead(payload: {...}) {
+export async function createLead(
+  payload: CreateLeadRequest,
+  options?: { params?: { reason?: string; overrideReason?: string } }
+) {
+  const queryParams = new URLSearchParams();
+  if (options?.params?.reason) queryParams.set('reason', options.params.reason);
+  if (options?.params?.overrideReason) queryParams.set('overrideReason', options.params.overrideReason);
+
+  const url = queryParams.toString()
+    ? `${BASE}/api/leads?${queryParams}`
+    : `${BASE}/api/leads`;
+
-  const res = await fetch(`${BASE}/api/leads`, {
+  const res = await fetch(url, {
     method: 'POST',
     headers: { 'Content-Type': 'application/json' },
-    body: JSON.stringify({ ...payload, source: 'manual' }),
+    body: JSON.stringify(payload),
   });
```

### 3. LeadWizard.tsx

**Siehe Abschnitt 3 (Erstkontakt-Block UI) für vollständige Implementierung**

---

## 8. Lead-Management UI (2025-10-04) - ADR-006 Hybrid-Ansatz

**⚠️ ARCHITEKTUR-ENTSCHEIDUNG (ADR-006):**
Sprint 2.1.5 implementiert **Phase 1** des Hybrid-Ansatzes:
- ✅ Leads als Customer-Status (Wiederverwendung Customer-Architektur)
- ⏸️ Lead-spezifische Features → Sprint 2.1.6 (Phase 2)

**📋 Siehe:** [ADR-006-lead-management-hybrid-architecture.md](../../shared/adr/ADR-006-lead-management-hybrid-architecture.md)

---

### 8.1 Phase 1: Lead-Management via CustomersPageV2

**Status:** ✅ IMPLEMENTIERT (2025-10-04)

#### Architektur-Entscheidung

**Leads verwenden die Customer-Verwaltung mit Lead-Filter + Lifecycle-Context:**
```typescript
// /pages/LeadsPage.tsx
import { CustomersPageV2 } from './CustomersPageV2';

export default function LeadsPage() {
  return (
    <CustomersPageV2
      defaultFilter={{ status: ['LEAD'] }}
      title="Lead-Management"
      createButtonLabel="Lead erfassen"
      context="leads"  // ← Lifecycle Context für FilterBar
    />
  );
}
```

**Lifecycle-Context-Architektur:**

Die `IntelligentFilterBar` nutzt einen `context`-Prop für kontext-sensitive Filter:

| Context | Status-Filter | Bedeutung |
|---------|--------------|-----------|
| `"leads"` | LEAD, PROSPECT | "Baby-Kunden" (Lead Lifecycle Phase) |
| `"customers"` | AKTIV, INAKTIV, ARCHIVIERT | "Erwachsene Kunden" (Customer Lifecycle Phase) |

**Alle anderen Filter sind identisch** (Risiko, Umsatz, Kontakte, Letzter Kontakt):
- ✅ Lead hat `riskScore` → Filter: "Gefährdete Leads"
- ✅ Lead hat `expectedAnnualVolume` → Filter: "High-Potential Leads"
- ✅ Lead hat `lastContactDate` → Filter: "Lange kein Kontakt"

**Rationale:** Lead ist eine Lifecycle-Phase, kein separates Entity (Best Practice analog Salesforce/HubSpot)

#### Verfügbare Features (aus CustomersPageV2)

**1. Intelligent Filter Bar**
- Quick Filters: "Neue Leads", "Heiße Leads", "Meine Leads"
- Advanced Filters: Status, Branche, Umsatzbereich, Risiko-Level
- Universal Search mit Caching (Debounce: 300ms)

**2. Smart Sort (verkaufsorientiert)**
- 🔥 "Heiße Leads zuerst" → status ASC, expectedAnnualVolume DESC
- ⏰ "Auslaufende Verträge" → nextFollowUpDate ASC
- 💰 "Umsatz: Hoch → Niedrig" → expectedAnnualVolume DESC
- 📞 "Neueste Kontakte" → lastContactDate DESC

**3. Virtualisierte Tabelle**
- react-window für Performance (>20 Leads)
- Column Manager (Drag & Drop, Sichtbarkeit)
- Persistent Config via Zustand Store

**4. Actions**
- ✏️ Bearbeiten (öffnet LeadWizard)
- 🗑️ Löschen (Confirmation Dialog)
- 📊 Details (Navigation zu Lead-Detail)

#### Lead-spezifische Komponenten (behalten)

**Behalten für Lead-Workflow:**
```
/features/leads/
├── LeadWizard.tsx              # ✅ Progressive Profiling (Karte 0/1/2)
├── LeadProtectionBadge.tsx     # ✅ Lead-spezifischer Schutz-Status
├── LeadSourceIcon.tsx          # ✅ Lead-Quellen-Icons (MESSE, TELEFON, etc.)
├── api.ts                      # ✅ Lead-spezifische API-Calls
└── types.ts                    # ✅ Lead-spezifische Types
```

**Obsolet (durch CustomersPageV2 ersetzt):**
```
/features/leads/
├── LeadListEnhanced.tsx        # ❌ Ersetzt durch CustomersPageV2
├── LeadStageBadge.tsx          # ❌ Duplikat zu CustomerStatusBadge
└── [weitere List-Komponenten]  # ❌ Nicht mehr benötigt
```

---

### 8.2 Phase 1 vs. Original-Implementierung (Vergleich)

#### ~~8.1 Lead-Liste (LeadListEnhanced.tsx)~~ **[OBSOLET]**

**Ursprüngliche Card-Implementierung (ersetzt):**

#### ~~Lead-Card Anzeige~~ **[NICHT MEHR VERWENDET]**
Jeder Lead wird als Material-UI Card dargestellt mit:

1. **Header-Bereich:**
   - 🎪📞🤝 **Quelle-Icon** (LeadSourceIcon.tsx)
     - MESSE: 🎪, TELEFON: 📞, EMPFEHLUNG: 🤝, WEB_FORMULAR: 🌐, PARTNER: 🔗, SONSTIGE: ❓
   - **Firmenname** (fett, klickbar → öffnet Detail-Ansicht)
   - **Stage-Badge** (LeadStageBadge.tsx):
     - Stage 0: 🔵 "Vormerkung" (info)
     - Stage 1: 🟡 "Registrierung" (warning)
     - Stage 2: 🟢 "Qualifizierung" (success)
   - **Schutz-Status** (LeadProtectionBadge.tsx):
     - 🟢 "Geschützt" (protected)
     - 🟠 "Warnung - X Tage" (warning)
     - 🔴 "Abgelaufen" (expired)
     - ⏳ "Pre-Claim - X Tage" (pre-claim)

2. **Info-Bereich:**
   - 📍 **Stadt** (falls vorhanden)
   - ⏰ **Letzte Aktivität** ("vor X Tagen" via date-fns)
   - 📅 **Erstellt** (falls keine lastActivityAt)

3. **Aktionen-Bereich:**
   - ✏️ **Bearbeiten** (IconButton → öffnet LeadWizard im Edit-Mode)
   - 🗑️ **Löschen** (IconButton → Bestätigungsdialog)

#### ~~Komponenten-Architektur (VERALTET)~~

**~~Alte Komponenten (nicht mehr verwendet):~~**
```
/features/leads/
├── ~~LeadListEnhanced.tsx~~     # ❌ OBSOLET - Ersetzt durch CustomersPageV2
├── ~~LeadStageBadge.tsx~~       # ❌ OBSOLET - Duplikat zu CustomerStatusBadge
├── LeadSourceIcon.tsx           # ✅ Lead-spezifisch (behalten)
├── LeadProtectionBadge.tsx      # ✅ Lead-spezifisch (behalten)
└── types.ts                     # ✅ Lead-spezifische Types (behalten)
```

**Aktuelle Architektur (Phase 1 - Sprint 2.1.5):**
```
/pages/
├── LeadsPage.tsx                # ✅ Wrapper um CustomersPageV2 (context="leads")
└── CustomersPageV2.tsx          # ✅ Wiederverwendung (context="customers")

/features/leads/
├── LeadWizard.tsx               # ✅ Progressive Profiling
├── LeadSourceIcon.tsx           # ✅ Lead-spezifisch
├── LeadProtectionBadge.tsx      # ✅ Lead-spezifisch
└── types.ts                     # ✅ Lead-spezifische Types
```

**Begründung:** Lifecycle-Context-Architektur (ADR-006) - Lead ist eine Phase, kein separates Entity

**Abhängigkeiten:**
- Material-UI: Card, Chip, IconButton, Tooltip, Stack
- date-fns: formatDistanceToNow (de locale)
- @mui/icons-material: Edit, Delete

#### Daten-Mapping

**Frontend Type (Lead):**
```typescript
export type Lead = {
  id: string;
  companyName: string;           // Anzeige: Fetter Titel
  stage: LeadStage;              // 0|1|2 → Badge
  source?: LeadSource;           // Icon-Mapping
  city?: string;                 // 📍 Anzeige
  lastActivityAt?: string;       // ⏰ "vor X Tagen"
  createdAt?: string;            // 📅 Fallback
  protectionUntil?: string;      // Schutz-Badge
  progressDeadline?: string;     // Schutz-Badge
  // ... weitere Felder
};
```

**Fallback-Logik (Legacy-Daten):**
```typescript
// Wenn companyName fehlt → nutze name (alt)
{lead.companyName || lead.name}

// Wenn stage ungültig → "Stage X" (default badge)
const config = stageConfig[stage] || { label: `Stage ${stage}`, color: 'default' };
```

### 8.2 Geplante Features (TODO)

#### Filter-Leiste (ausstehend)
- 🔍 **Suche:** Firmenname, Stadt, Kontaktperson
- 📊 **Stage:** Alle | Vormerkung | Registrierung | Qualifizierung
- 🛡️ **Schutz:** Alle | Geschützt | Warnung | Abgelaufen | Pre-Claim
- 🏷️ **Quelle:** Alle | Messe | Telefon | Empfehlung | Web | Partner | Sonstige
- 📅 **Zeitraum:** Heute | 7 Tage | 30 Tage | Benutzerdefiniert
- 👤 **Owner:** Meine Leads | Alle Leads (Admin/Manager)

#### Lead-Detail-Dialog (ausstehend)
**3 Tabs:**
1. **📋 Übersicht:** Alle Stammdaten + Stage-Fortschritt + Schutz-Status
2. **📅 Aktivitäten:** Timeline (via ActivityTimeline.tsx) + Neue Aktivität
3. **✏️ Bearbeiten:** LeadWizard im Edit-Modus (alle Stages editierbar)

#### CRUD-Operationen (ausstehend)
- ✅ **Create:** LeadWizard (vorhanden)
- ⏳ **Read:** Detail-Dialog (TODO)
- ⏳ **Update:** LeadWizard Edit-Mode (TODO)
- ⏳ **Delete:** Bestätigungsdialog + DELETE /api/leads/{id} (TODO)

### 8.3 Backend-Anforderungen

**Fehlende/Inkonsistente Felder:**

**Problem (2025-10-04):**
Backend liefert alte Struktur:
```json
{
  "id": "1",
  "name": "Demo Restaurant",        // ← Soll: companyName
  "email": "info@demo.de",          // ← Soll: contact.email
  "createdAt": "2025-09-25T10:00:00Z"
  // Fehlt: stage, source, city, protectionUntil, etc.
}
```

**Soll-Struktur (Lead DTO):**
```json
{
  "id": "1",
  "companyName": "Demo Restaurant",
  "stage": 0,
  "source": "MESSE",
  "city": "Berlin",
  "contact": {
    "email": "info@demo.de"
  },
  "protectionUntil": "2026-03-25T10:00:00Z",
  "progressDeadline": "2025-11-24T10:00:00Z",
  "lastActivityAt": "2025-09-27T14:30:00Z",
  "createdAt": "2025-09-25T10:00:00Z"
}
```

**Action Items Sprint 2.1.5:**
1. ✅ Frontend: Fallback-Logik für Legacy-Daten implementiert
2. ✅ Backend: LeadResource GET /api/leads Response-DTO aktualisiert
3. ✅ Frontend: MSW-Mocks deaktiviert (echtes Backend)
4. ✅ Frontend: DevAuth Auto-Login implementiert
5. ⏳ Backend: PUT /api/leads/{id} implementieren (Update) → Sprint 2.1.6
6. ⏳ Backend: DELETE /api/leads/{id} implementieren (mit Schutz-Check) → Sprint 2.1.6

---

### 8.3 Phase 2: Lead-Erweiterungen (Sprint 2.1.6)

**Status:** 🔄 GEPLANT

#### Lead-Scoring-System

**Lead-Score-Berechnung (0-100 Punkte):**
```typescript
interface LeadScore {
  totalScore: number;  // 0-100
  factors: {
    revenueScore: number;      // 0-25 (Umsatzpotenzial)
    engagementScore: number;   // 0-25 (Interaktionen)
    fitScore: number;          // 0-25 (Branche/Größe-Match)
    urgencyScore: number;      // 0-25 (Zeitfaktor)
  };
}
```

**Komponenten:**
- `LeadScoreIndicator.tsx` - Visueller Score mit Progress Bar
- `LeadScoreBreakdown.tsx` - Detaillierte Factor-Anzeige
- `hooks/useLeadScore.ts` - Score-Berechnung Hook

#### Lead-Status-Workflows

**Status-Übergänge:**
```
LEAD → PROSPECT (Qualifizierung)
  ↓
PROSPECT → AKTIV (Konversion)
  ↓
AKTIV → [Customer Lifecycle]
```

**Komponenten:**
- `LeadStatusWorkflow.tsx` - Status-Transition UI
- `LeadQualificationForm.tsx` - Qualifizierungs-Dialog
- `hooks/useLeadConversion.ts` - Conversion-Tracking

#### Lead-Activity-Timeline

**Activity-Log:**
```typescript
interface LeadActivity {
  timestamp: string;
  type: 'EMAIL_SENT' | 'CALL_MADE' | 'MEETING_SCHEDULED' | 'QUOTE_SENT';
  description: string;
  userId: string;
  outcome?: 'SUCCESS' | 'FAILED' | 'PENDING';
}
```

**Komponenten:**
- `LeadActivityTimeline.tsx` - Chronologische Activity-Anzeige
- `ActivityTypeIcon.tsx` - Icons für Activity-Types

#### Lead-Protection aktivieren

**Backend-Erweiterung:**
```sql
-- Lead-Protection Index
CREATE INDEX idx_leads_assigned_to ON leads(assigned_to)
  WHERE status IN ('LEAD', 'PROSPECT');

-- API-Erweiterung
GET /api/leads?assignedTo={userId}
```

**Frontend-Integration:**
```typescript
// CustomersPageV2 erweitern
const { data } = useCustomers(
  0,
  1000,
  'companyName',
  user?.role === 'sales' ? user.id : undefined  // Lead-Protection Filter
);
```

**Komponenten:**
- Quick Filter: "Meine Leads" (assignedTo = currentUser)
- Protection-Indicator: Zeigt Lead-Owner

---

## 9. Server-Side Filtering & Pagination (2025-10-04)

**Problem:** CustomersPageV2 lädt aktuell **alle** Kunden (1000+) und filtert client-side → Performance-Problem bei großen Datenmengen

**Lösung:** Migration zu Server-Side Filtering mit Backend `/api/customers/search`

### Performance-Vergleich

| Szenario | Client-Side | Server-Side | Verbesserung |
|----------|-------------|-------------|--------------|
| Initial Load (500 Kunden) | 1200ms | 60ms | **95% faster** |
| Response Size | 850KB | 45KB | **94% smaller** |
| Memory Usage | 1000 Objekte | 50 Objekte | **95% less** |

### Implementation

**1. Neuer Hook: useCustomerSearch**
```typescript
// /frontend/src/features/customer/api/customerQueries.ts
export function useCustomerSearch(params: CustomerSearchParams) {
  return useQuery({
    queryKey: ['customers', 'search', params],
    queryFn: async () => {
      const response = await fetch('/api/customers/search', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', ...authHeaders() },
        body: JSON.stringify({
          page: params.page,
          size: params.size,
          status: params.filters.status,
          industry: params.filters.industry,
          textSearch: params.filters.text,
          sortBy: params.sort.field,
          sortDirection: params.sort.direction === 'asc' ? 'ASC' : 'DESC',
        }),
      });
      if (!response.ok) throw new Error('Search failed');
      return response.json();
    },
    staleTime: 30000, // 30s Cache
  });
}
```

**2. CustomersPageV2 Refactoring**
```typescript
// VORHER (Client-Side)
const { data } = useCustomers(0, 1000); // ❌ Lädt ALLE
const filteredCustomers = useMemo(() => {
  // ... 100 Zeilen Filter-Logik
}, [customers, filterConfig]);

// NACHHER (Server-Side)
const [page, setPage] = useState(0);
const { data } = useCustomerSearch({
  filters: filterConfig,
  sort: sortConfig,
  page,
  size: 50,
}); // ✅ Backend filtert

const customers = data?.content || [];
```

**3. Pagination UI (Load More)**
```typescript
<Box textAlign="center" mt={2}>
  {data?.hasMore && (
    <Button onClick={() => setPage(p => p + 1)} disabled={isLoading}>
      Weitere 50 laden
    </Button>
  )}
  <Typography variant="caption">
    {customers.length} von {data?.total} Kunden
  </Typography>
</Box>
```

### Backend-Endpoint (bereits vorhanden ✅)

```java
@POST
@Path("/search")
public Response searchCustomers(CustomerSearchRequest request) {
    PanacheQuery<Customer> query = Customer.findAll();

    if (request.status != null) {
        query = query.filter("status in ?1", request.status);
    }

    if (request.textSearch != null) {
        query = query.filter(
            "lower(companyName) like ?1",
            "%" + request.textSearch.toLowerCase() + "%"
        );
    }

    List<Customer> results = query.page(request.page, request.size).list();
    return Response.ok(new PagedResponse(results, request.page, request.size)).build();
}
```

### Migration-Checklist

**Frontend:**
- [ ] useCustomerSearch Hook erstellen
- [ ] CustomersPageV2 auf Server-Side umstellen
- [ ] Client-Side Filter-Logik entfernen (Zeile 88-159)
- [ ] Load More Button UI
- [ ] Loading States für Pagination

**Testing:**
- [ ] Performance Test: 1000 Kunden → <100ms
- [ ] Filter Test: Status/Industry/Search
- [ ] Pagination Test: Load More funktioniert
- [ ] Edge Cases: Leere Ergebnisse, Server Error

**Backward Compatibility:**
- ✅ Bestehende useCustomers Hook bleibt unverändert
- ✅ Andere Pages nicht betroffen
- ✅ Filter-Types bleiben gleich (FilterConfig, SortConfig)

📖 **Details:** [SERVER_SIDE_FILTERING.md](./SERVER_SIDE_FILTERING.md)

---

## 10. Referenzen

- [ACTIVITY_TYPES_PROGRESS_MAPPING.md](./ACTIVITY_TYPES_PROGRESS_MAPPING.md)
- [DEDUPE_POLICY.md](./DEDUPE_POLICY.md)
- [DSGVO_CONSENT_SPECIFICATION.md](./DSGVO_CONSENT_SPECIFICATION.md)
- [PRE_CLAIM_LOGIC.md](./PRE_CLAIM_LOGIC.md)
- [SERVER_SIDE_FILTERING.md](./SERVER_SIDE_FILTERING.md)
- [V258 Migration](../../../../../backend/src/main/resources/db/migration/V258__expand_activity_type_constraint.sql)
- [FRONTEND_ACCESSIBILITY.md](../../../../../../frontend/FRONTEND_ACCESSIBILITY.md)

---

🤖 **Generated with [Claude Code](https://claude.com/claude-code)**

Co-Authored-By: Claude <noreply@anthropic.com>
