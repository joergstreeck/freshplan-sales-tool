# 🎯 LeadWizard.tsx Refactoring Guide - Sprint 2.1.5

**📅 Erstellt:** 2025-10-04
**🎯 Zweck:** Code-Implementation Anleitung für Progressive Profiling UX Best Practice

---

## 📋 Zusammenfassung

**Backend Status:** ✅ COMPLETE (V255-V258 deployed, 31 Unit Tests passing)

**Frontend Status:** 🚧 IN PROGRESS
- ❌ **Erstkontakt-Block auf falscher Karte** (aktuell Karte 1, soll Karte 0)
- ❌ **DSGVO Consent-Checkbox** statt Hinweis (Karte 1)
- ❌ **Keine Save-Buttons auf Karte 0 & 1** (nur "Weiter" → "Create")
- ❌ **Pflichtfeld Karte 1** nicht korrekt (Mind. 1 Kontaktkanal fehlt)

---

## 🔧 Erforderliche Änderungen (Priorität)

### 1. ✅ Erstkontakt-Block von Karte 1 → Karte 0 verschieben

**Aktuell (FALSCH):**
```tsx
case 1: // Karte 1: Registrierung
  // ...
  {/* Sprint 2.1.5: Erstkontakt-Block (conditional) */}
  {['MESSE', 'EMPFEHLUNG', 'TELEFON'].includes(formData.source || '') && (
    <Box sx={{ mt: 3, p: 2, bgcolor: 'action.hover', borderRadius: 1 }}>
      {/* Erstkontakt-Felder */}
    </Box>
  )}
```

**Soll (KORREKT):**
```tsx
case 0: // Karte 0: Vormerkung
  // ... existing fields (companyName, city, postalCode, businessType, source)

  {/* Erstkontakt-Block (optional, empfohlen) */}
  <Box sx={{ mt: 3, p: 2, bgcolor: 'info.light', borderRadius: 1 }}>
    <Typography variant="subtitle2" gutterBottom>
      {t('wizard.stage0.firstContactTitle')} (empfohlen)
    </Typography>
    <Typography variant="caption" color="text.secondary" sx={{ mb: 2, display: 'block' }}>
      {t('wizard.stage0.firstContactHint')}
    </Typography>

    {/* Kanal Dropdown */}
    <FormControl fullWidth margin="dense">
      <InputLabel id="firstContact-channel-label">
        {t('wizard.stage0.firstContactChannel')}
      </InputLabel>
      <Select
        labelId="firstContact-channel-label"
        value={formData.firstContact?.channel || ''}
        onChange={e => setFormData({
          ...formData,
          firstContact: {
            channel: e.target.value as FirstContact['channel'],
            performedAt: formData.firstContact?.performedAt || '',
            notes: formData.firstContact?.notes || '',
          },
        })}
        label={t('wizard.stage0.firstContactChannel')}
      >
        <MenuItem value="">
          <em>{t('wizard.stage0.firstContactChannelPlaceholder')}</em>
        </MenuItem>
        <MenuItem value="MESSE">{t('wizard.firstContactChannels.messe')}</MenuItem>
        <MenuItem value="PHONE">{t('wizard.firstContactChannels.phone')}</MenuItem>
        <MenuItem value="EMAIL">{t('wizard.firstContactChannels.email')}</MenuItem>
        <MenuItem value="REFERRAL">{t('wizard.firstContactChannels.referral')}</MenuItem>
        <MenuItem value="OTHER">{t('wizard.firstContactChannels.other')}</MenuItem>
      </Select>
    </FormControl>

    {/* Zeitpunkt datetime-local */}
    <TextField
      label={t('wizard.stage0.firstContactDate')}
      type="datetime-local"
      value={formData.firstContact?.performedAt || ''}
      onChange={e => setFormData({
        ...formData,
        firstContact: {
          channel: formData.firstContact?.channel || 'OTHER',
          performedAt: e.target.value,
          notes: formData.firstContact?.notes || '',
        },
      })}
      fullWidth
      margin="dense"
      InputLabelProps={{ shrink: true }}
    />

    {/* Notizen Textarea */}
    <TextField
      label={t('wizard.stage0.firstContactNotes')}
      value={formData.firstContact?.notes || ''}
      onChange={e => setFormData({
        ...formData,
        firstContact: {
          channel: formData.firstContact?.channel || 'OTHER',
          performedAt: formData.firstContact?.performedAt || '',
          notes: e.target.value,
        },
      })}
      fullWidth
      margin="dense"
      multiline
      rows={3}
      helperText={t('wizard.stage0.firstContactNotesHelper')}
      inputProps={{ minLength: 10 }}
    />
  </Box>
```

**Validierung anpassen:**
```tsx
// Stage 0 Validation erweitern
const validateStage0 = (): Record<string, string[]> | null => {
  const errors: Record<string, string[]> = {};

  if (!formData.companyName.trim()) {
    errors.companyName = [t('wizard.stage0.companyNameRequired')];
  } else if (formData.companyName.trim().length < 2) {
    errors.companyName = [t('wizard.validation.companyNameMin')];
  }

  if (!formData.source) {
    errors.source = [t('wizard.stage0.sourceRequired')];
  }

  // Erstkontakt-Validierung (wenn Felder vorhanden)
  if (formData.firstContact) {
    if (!formData.firstContact.channel) {
      errors['firstContact.channel'] = [t('wizard.stage0.firstContactChannelRequired')];
    }
    if (!formData.firstContact.performedAt) {
      errors['firstContact.performedAt'] = [t('wizard.stage0.firstContactDateRequired')];
    }
    if (!formData.firstContact.notes || formData.firstContact.notes.trim().length < 10) {
      errors['firstContact.notes'] = [t('wizard.stage0.firstContactNotesMin')];
    }
  }

  return Object.keys(errors).length > 0 ? errors : null;
};
```

---

### 2. ✅ DSGVO Consent-Checkbox → Hinweis-Box (Karte 1)

**Aktuell (FALSCH):**
```tsx
{/* DSGVO Consent Checkbox (PFLICHT bei Contact-Daten) */}
<Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
  <FormControlLabel
    control={
      <Checkbox
        checked={formData.consentGiven}
        onChange={e => setFormData({ ...formData, consentGiven: e.target.checked })}
        required={...}
      />
    }
    label={...}
  />
</Box>
```

**Soll (KORREKT):**
```tsx
{/* DSGVO Hinweis (statt Checkbox bei Vertrieb) */}
<Box sx={{ mt: 2, p: 2, bgcolor: 'info.light', borderRadius: 1 }}>
  <Typography variant="body2">
    <strong>Berechtigtes Interesse (Art. 6 Abs. 1 lit. f DSGVO)</strong>
  </Typography>
  <Typography variant="caption" color="text.secondary">
    Verarbeitung zur B2B-Geschäftsanbahnung.
    <Link onClick={openDsgvoPopup} sx={{ ml: 1, cursor: 'pointer' }}>
      Gesetzestext anzeigen ↗
    </Link>
  </Typography>
  <Typography variant="caption" display="block" sx={{ mt: 1, fontStyle: 'italic' }}>
    Hinweis: Einwilligung nur erforderlich bei Web-Formular (Kunde gibt selbst Daten ein).
  </Typography>
</Box>
```

**Validierung entfernen:**
```tsx
// Stage 1 Validation vereinfachen
const validateStage1 = (): Record<string, string[]> | null => {
  const errors: Record<string, string[]> = {};

  // Mind. 1 Kontaktkanal (Email ODER Phone)
  const hasEmail = formData.contact.email?.trim();
  const hasPhone = formData.contact.phone?.trim();

  if (!hasEmail && !hasPhone) {
    errors.contact = [t('wizard.stage1.contactRequired')]; // NEU: "Mind. E-Mail oder Telefon erforderlich"
  }

  // Email-Validierung (wenn vorhanden)
  if (hasEmail && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(hasEmail)) {
    errors['contact.email'] = [t('wizard.validation.emailInvalid')];
  }

  // ❌ ENTFERNEN: Consent-Validierung
  // ❌ ENTFERNEN: Quellenabhängige Erstkontakt-Pflicht (jetzt auf Karte 0)

  return Object.keys(errors).length > 0 ? errors : null;
};
```

---

### 3. ✅ Button-Logik umbauen (3 Save-Buttons)

**Aktuell (FALSCH):**
```tsx
<DialogActions>
  <Button onClick={handleClose}>Abbrechen</Button>
  <Box sx={{ flex: '1 1 auto' }} />
  {activeStep > 0 && <Button onClick={handleBack}>Zurück</Button>}
  {activeStep < steps.length - 1 ? (
    <Button variant="contained" onClick={handleNext}>Weiter</Button>
  ) : (
    <Button variant="contained" onClick={handleSubmit}>Erstellen</Button>
  )}
</DialogActions>
```

**Soll (KORREKT):**
```tsx
<DialogActions>
  <Button onClick={handleClose} disabled={saving}>
    {t('wizard.actions.cancel')}
  </Button>
  <Box sx={{ flex: '1 1 auto' }} />

  {activeStep > 0 && (
    <Button onClick={handleBack} disabled={saving}>
      {t('wizard.actions.back')} {/* ← Zurück */}
    </Button>
  )}

  {/* Save-Buttons je Karte */}
  {activeStep === 0 && (
    <>
      <Button
        variant="contained"
        onClick={() => handleSave(0)} // NEU: handleSave(stage)
        disabled={saving || !formData.companyName.trim() || !formData.source}
      >
        {saving ? t('wizard.actions.saving') : t('wizard.actions.saveVormerkung')} {/* Vormerkung speichern */}
      </Button>
      <Button onClick={handleNext} disabled={saving}>
        {t('wizard.actions.next')} {/* Weiter → */}
      </Button>
    </>
  )}

  {activeStep === 1 && (
    <>
      <Button
        variant="contained"
        onClick={() => handleSave(1)} // NEU: handleSave(stage)
        disabled={saving}
      >
        {saving ? t('wizard.actions.saving') : t('wizard.actions.saveRegistrierung')} {/* Registrierung speichern */}
      </Button>
      <Button onClick={handleNext} disabled={saving}>
        {t('wizard.actions.next')} {/* Weiter → */}
      </Button>
    </>
  )}

  {activeStep === 2 && (
    <Button
      variant="contained"
      onClick={() => handleSave(2)} // NEU: handleSave(stage)
      disabled={saving}
    >
      {saving ? t('wizard.actions.saving') : t('wizard.actions.saveQualifizierung')} {/* Qualifizierung speichern */}
    </Button>
  )}
</DialogActions>
```

**Neue handleSave-Funktion:**
```tsx
const handleSave = async (stage: 0 | 1 | 2) => {
  // Validate current stage
  let validationErrors: Record<string, string[]> | null = null;
  if (stage === 0) validationErrors = validateStage0();
  if (stage === 1) validationErrors = validateStage1();
  if (stage === 2) validationErrors = validateStage2();

  if (validationErrors) {
    setError({ errors: validationErrors, status: 400, title: 'Validierungsfehler' });
    return;
  }

  setSaving(true);
  setError(null);

  try {
    const hasContactData = formData.contact.email?.trim() || formData.contact.phone?.trim();

    // Erstkontakt → activities[] Transformation
    const activities = formData.firstContact
      ? [{
          activityType: 'FIRST_CONTACT_DOCUMENTED',
          performedAt: formData.firstContact.performedAt,
          summary: `${formData.firstContact.channel}: ${formData.firstContact.notes}`,
          countsAsProgress: false,
          metadata: {
            channel: formData.firstContact.channel,
            notes: formData.firstContact.notes,
          },
        }]
      : undefined;

    const payload = {
      stage,
      companyName: formData.companyName.trim(),
      city: formData.city?.trim() || undefined,
      postalCode: formData.postalCode?.trim() || undefined,
      businessType: formData.businessType,
      source: formData.source,
      contact: hasContactData
        ? {
            firstName: formData.contact.firstName?.trim() || undefined,
            lastName: formData.contact.lastName?.trim() || undefined,
            email: formData.contact.email?.trim() || undefined,
            phone: formData.contact.phone?.trim() || undefined,
          }
        : undefined,
      activities,
      estimatedVolume: stage >= 2 ? formData.estimatedVolume : undefined,
      kitchenSize: stage >= 2 ? formData.kitchenSize : undefined,
      employeeCount: stage >= 2 ? formData.employeeCount : undefined,
      website: stage >= 2 ? formData.website?.trim() || undefined : undefined,
      industry: stage >= 2 ? formData.industry?.trim() || undefined : undefined,
    };

    await createLead(payload);

    // Reset & Close
    setFormData({
      companyName: '',
      city: '',
      postalCode: '',
      businessType: undefined,
      source: undefined,
      contact: { firstName: '', lastName: '', email: '', phone: '' },
      consentGiven: false,
      firstContact: undefined,
      estimatedVolume: undefined,
      kitchenSize: undefined,
      employeeCount: undefined,
      website: '',
      industry: '',
    });
    setActiveStep(0);

    onCreated();
    onClose();
  } catch (e) {
    setError(e as Problem);
  } finally {
    setSaving(false);
  }
};
```

---

## 📝 i18n Labels (de/leads.json)

**NEU hinzufügen:**
```json
{
  "wizard": {
    "actions": {
      "saveVormerkung": "Vormerkung speichern",
      "saveRegistrierung": "Registrierung speichern",
      "saveQualifizierung": "Qualifizierung speichern"
    },
    "stage0": {
      "sourceRequired": "Quelle ist erforderlich",
      "firstContactTitle": "Erstkontakt dokumentieren",
      "firstContactHint": "Hatten Sie bereits Kontakt? Dokumentieren Sie kurz den Erstkontakt. Dadurch startet der Schutz für diesen Lead.",
      "firstContactChannel": "Kanal",
      "firstContactChannelPlaceholder": "Kanal wählen...",
      "firstContactChannelRequired": "Kanal ist erforderlich",
      "firstContactDate": "Zeitpunkt",
      "firstContactDateRequired": "Zeitpunkt ist erforderlich",
      "firstContactNotes": "Notizen",
      "firstContactNotesMin": "Notizen müssen mindestens 10 Zeichen lang sein",
      "firstContactNotesHelper": "Mindestens 10 Zeichen"
    },
    "stage1": {
      "contactRequired": "Mind. E-Mail oder Telefon erforderlich"
    }
  }
}
```

**ENTFERNEN aus stage1:**
```json
{
  "wizard": {
    "stage1": {
      // ❌ ENTFERNEN:
      "firstContactTitle": ...,
      "firstContactHint": ...,
      "firstContactChannel": ...,
      // ... alle firstContact-Keys
    }
  }
}
```

---

## ✅ Testing-Checklist

Nach Refactoring:

1. **Unit Tests aktualisieren:**
   - `LeadWizard.integration.test.tsx`
   - Button-Texte anpassen (3 Save-Buttons)
   - Erstkontakt auf Karte 0 testen
   - DSGVO-Hinweis (kein Checkbox) testen

2. **Coverage prüfen:**
   ```bash
   npm run test:coverage
   # Target: ≥80%
   ```

3. **Browser-Test:**
   - Karte 0: Vormerkung speichern → Dialog schließt
   - Karte 1: Registrierung speichern → Dialog schließt
   - Karte 2: Qualifizierung speichern → Dialog schließt
   - Erstkontakt auf Karte 0 → Pre-Claim Badge verschwindet

---

## 🚀 Nächste Schritte (Priorität)

1. ✅ **Erstkontakt-Block verschieben** (Karte 1 → Karte 0)
2. ✅ **DSGVO Consent → Hinweis** (Karte 1)
3. ✅ **Button-Logik umbauen** (3 Save-Buttons + handleSave-Funktion)
4. ✅ **Validierung anpassen** (Stage 0 & 1)
5. ✅ **i18n Labels hinzufügen** (de/leads.json)
6. ✅ **Tests aktualisieren** (Button-Texte, Erstkontakt-Position)
7. ✅ **Coverage prüfen** (≥80%)

---

**📅 Erstellt:** 2025-10-04
**🎯 Status:** Production-Ready Documentation
**📋 Referenz:** TRIGGER_SPRINT_2_1_5.md, FRONTEND_DELTA.md, BUSINESS_LOGIC_LEAD_ERFASSUNG.md
