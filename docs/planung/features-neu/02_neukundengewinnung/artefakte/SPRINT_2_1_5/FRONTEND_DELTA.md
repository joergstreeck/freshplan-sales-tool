---
sprint: "2.1.5"
domain: "frontend"
doc_type: "specification"
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
- **Erstkontakt (empfohlen):** Kanal, Zeitpunkt, Notiz (≥10 Zeichen)

**Erstkontakt-Logik:**
- Wird Erstkontakt dokumentiert → **Schutz startet** (registered_at gesetzt, V257 Trigger feuert)
- Wird NICHT dokumentiert → **Pre-Claim** (10-Tage-Frist, Badge "⏳ Vormerkung läuft ab in X Tagen")
- Erstkontakt **kann jederzeit nachgetragen werden** (Lead öffnen → Activity hinzufügen)

**Buttons:**
- `[Abbrechen]` - Schließt Dialog ohne Speichern
- `[Vormerkung speichern]` - POST /api/leads mit stage=0, schließt Dialog
- `[Weiter →]` - Geht zu Karte 1 (ohne Speichern)

**Copy:**
```
Beschreibung: "Legen Sie den Lead mit wenigen Angaben an. Sie können Details später ergänzen."

Erstkontakt-Box (optional, conditional):
Titel: "Erstkontakt dokumentieren (empfohlen)"
Hint: "Hatten Sie bereits Kontakt? Dokumentieren Sie kurz den Erstkontakt. Dadurch startet der Schutz für diesen Lead."
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

| Source | Erstkontakt-Block | DSGVO Consent | Notizen |
|--------|-------------------|---------------|---------|
| **MESSE** | ✅ PFLICHT | ❌ Optional | channel = MESSE, performedAt = Messedatum |
| **EMPFEHLUNG** | ✅ PFLICHT | ❌ Optional | channel = REFERRAL, summary = Empfehlungsgeber |
| **TELEFON** | ✅ PFLICHT | ❌ Optional | channel = PHONE, summary = Gesprächsnotiz |
| **WEB_FORMULAR** | ❌ Optional | ✅ PFLICHT | Sprint 2.1.6 Web-Intake |
| **PARTNER** | ❌ Optional | ❌ Optional | Sprint 2.1.6 API-Import |
| **SONSTIGE** | ⚠️ Optional | ❌ Optional | Fallback |

**Implementierung:**

```typescript
// LeadWizard.tsx - validateStage1()
const requiresFirstContact = ['MESSE', 'EMPFEHLUNG', 'TELEFON'].includes(formData.source);

if (requiresFirstContact && !formData.firstContact) {
  errors.firstContact = [t('wizard.stage1.firstContactRequired')];
}

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

## 9. Referenzen

- [ACTIVITY_TYPES_PROGRESS_MAPPING.md](./ACTIVITY_TYPES_PROGRESS_MAPPING.md)
- [DEDUPE_POLICY.md](./DEDUPE_POLICY.md)
- [DSGVO_CONSENT_SPECIFICATION.md](./DSGVO_CONSENT_SPECIFICATION.md)
- [PRE_CLAIM_LOGIC.md](./PRE_CLAIM_LOGIC.md)
- [V258 Migration](../../../../../backend/src/main/resources/db/migration/V258__expand_activity_type_constraint.sql)
- [FRONTEND_ACCESSIBILITY.md](../../../../../../frontend/FRONTEND_ACCESSIBILITY.md)

---

🤖 **Generated with [Claude Code](https://claude.com/claude-code)**

Co-Authored-By: Claude <noreply@anthropic.com>
