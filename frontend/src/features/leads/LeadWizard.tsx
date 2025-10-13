import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Stepper,
  Step,
  StepLabel,
  TextField,
  FormControl,
  FormControlLabel,
  Checkbox,
  InputLabel,
  Select,
  MenuItem,
  Typography,
  Link,
  Alert,
  Box,
  Stack,
  IconButton,
  CircularProgress,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useTranslation } from 'react-i18next';
import type { LeadFormStage2, Problem, BusinessType, LeadSource, FirstContact } from './types';
import { createLead, createLeadContact } from './api';
import { useBusinessTypes } from '../../hooks/useBusinessTypes';
import { useLeadSources } from './hooks/useLeadSources';
import { useKitchenSizes } from './hooks/useKitchenSizes';

interface LeadWizardProps {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
}

export default function LeadWizard({ open, onClose, onCreated }: LeadWizardProps) {
  const { t } = useTranslation('leads');
  const steps = [t('wizard.steps.company'), t('wizard.steps.contact'), t('wizard.steps.business')];
  const [activeStep, setActiveStep] = useState(0);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<Problem | null>(null);

  // Sprint 2.1.6: Fetch enum values from backend (Single Source of Truth - NO hardcoding)
  const { data: businessTypes, isLoading: isLoadingBusinessTypes } = useBusinessTypes();
  const { data: leadSources, isLoading: isLoadingLeadSources } = useLeadSources();
  const { data: kitchenSizes, isLoading: isLoadingKitchenSizes } = useKitchenSizes();

  // Form State (Progressive Profiling - Sprint 2.1.5)
  const [formData, setFormData] = useState<
    LeadFormStage2 & {
      source?: LeadSource;
      notes?: string; // Sprint 2.1.5: Feld 1 - Notizen/Quelle (immer sichtbar, optional)
      firstContact?: FirstContact;
    }
  >({
    companyName: '',
    city: '',
    postalCode: '',
    businessType: undefined,
    source: undefined, // Sprint 2.1.5: MESSE, EMPFEHLUNG, TELEFON, etc.
    notes: '', // Sprint 2.1.5: Zwei-Felder-Lösung - Feld 1
    contact: {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
    },
    consentGiven: false, // DSGVO Consent (Stage 1)
    firstContact: undefined, // Sprint 2.1.5: Zwei-Felder-Lösung - Feld 2 (conditional)
    estimatedVolume: undefined,
    kitchenSize: undefined,
    employeeCount: undefined,
    website: '',
    industry: '',
  });

  // Sprint 2.1.5: Checkbox für optionalen Erstkontakt (nur bei EMPFEHLUNG/WEB/PARTNER/SONSTIGE)
  const [showFirstContactFields, setShowFirstContactFields] = useState(false);

  const fieldErrors = error?.errors || {};

  // Stage 0 Validation (Vormerkung - Company Basics + Source + Erstkontakt)
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

    // Sprint 2.1.5: Zwei-Felder-Lösung - Erstkontakt-Validierung
    const requiresFirstContact = ['MESSE', 'TELEFON'].includes(formData.source || '');
    const showFirstContactBlock = requiresFirstContact || showFirstContactFields;

    // Erstkontakt-Block nur validieren wenn sichtbar
    if (showFirstContactBlock) {
      const hasStartedFirstContact =
        formData.firstContact?.performedAt || formData.firstContact?.notes?.trim();

      if (requiresFirstContact && !hasStartedFirstContact) {
        errors.firstContact = [t('wizard.stage0.firstContactRequired')];
      } else if (hasStartedFirstContact) {
        // Wenn begonnen, dann vollständig ausfüllen
        if (requiresFirstContact && !formData.firstContact?.performedAt) {
          errors['firstContact.performedAt'] = [t('wizard.stage0.firstContactDateRequired')];
        }
        // Bei PFLICHT: Zeitpunkt + Notizen erforderlich
        // Bei OPTIONAL: Nur Notizen erforderlich (Zeitpunkt optional)
        if (!formData.firstContact?.notes || formData.firstContact.notes.trim().length < 10) {
          errors['firstContact.notes'] = [t('wizard.stage0.firstContactNotesMin')];
        }
      }
    }

    return Object.keys(errors).length > 0 ? errors : null;
  };

  // Stage 1 Validation (Registrierung - Mind. 1 Kontaktkanal)
  const validateStage1 = (): Record<string, string[]> | null => {
    const errors: Record<string, string[]> = {};

    // Mind. 1 Kontaktkanal (Email ODER Phone)
    const hasEmail = formData.contact.email?.trim();
    const hasPhone = formData.contact.phone?.trim();

    if (!hasEmail && !hasPhone) {
      errors.contact = [t('wizard.stage1.contactRequired')];
    }

    // Email-Validierung (wenn vorhanden)
    if (hasEmail && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(hasEmail)) {
      errors['contact.email'] = [t('wizard.validation.emailInvalid')];
    }

    return Object.keys(errors).length > 0 ? errors : null;
  };

  // Stage 2 Validation (Qualifizierung - Business Details)
  const validateStage2 = (): Record<string, string[]> | null => {
    const errors: Record<string, string[]> = {};

    if (formData.estimatedVolume && formData.estimatedVolume < 0) {
      errors.estimatedVolume = [t('wizard.validation.volumePositive')];
    }

    if (formData.employeeCount && formData.employeeCount < 0) {
      errors.employeeCount = [t('wizard.validation.employeePositive')];
    }

    return Object.keys(errors).length > 0 ? errors : null;
  };

  const handleNext = () => {
    setError(null);

    // Validate current stage
    let validationErrors: Record<string, string[]> | null = null;
    if (activeStep === 0) validationErrors = validateStage0();
    if (activeStep === 1) validationErrors = validateStage1();
    if (activeStep === 2) validationErrors = validateStage2();

    if (validationErrors) {
      setError({ errors: validationErrors, status: 400, title: 'Validierungsfehler' });
      return;
    }

    setActiveStep(prevActiveStep => prevActiveStep + 1);
  };

  const handleBack = () => {
    setError(null);
    setActiveStep(prevActiveStep => prevActiveStep - 1);
  };

  // Sprint 2.1.5: Progressive Profiling - Save per card
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
      // Sprint 2.1.6 Phase 5+: Harmonisierung mit lead_contacts (ADR-007 Option C)
      const hasContactData =
        formData.contact.firstName?.trim() &&
        formData.contact.lastName?.trim() &&
        (formData.contact.email?.trim() || formData.contact.phone?.trim());

      // Sprint 2.1.5: Erstkontakt → activities[] Transformation
      const activities = formData.firstContact
        ? [
            {
              activityType: 'FIRST_CONTACT_DOCUMENTED',
              performedAt: formData.firstContact.performedAt,
              summary: `${formData.firstContact.channel}: ${formData.firstContact.notes}`,
              countsAsProgress: false,
              metadata: {
                channel: formData.firstContact.channel,
                notes: formData.firstContact.notes,
              },
            },
          ]
        : undefined;

      // Step 1: Create Lead WITHOUT contact fields (new harmonized approach)
      const payload = {
        stage,
        companyName: formData.companyName.trim(),
        name: formData.companyName.trim(), // Legacy support (Backend compatibility)
        city: formData.city?.trim() || undefined,
        postalCode: formData.postalCode?.trim() || undefined,
        businessType: formData.businessType,
        source: formData.source,
        activities,
        estimatedVolume: stage >= 2 ? formData.estimatedVolume : undefined,
        kitchenSize: stage >= 2 ? formData.kitchenSize : undefined,
        employeeCount: stage >= 2 ? formData.employeeCount : undefined,
        website: stage >= 2 ? formData.website?.trim() || undefined : undefined,
        industry: stage >= 2 ? formData.industry?.trim() || undefined : undefined,
      };

      console.log('🚀 Step 1: Creating lead (without contact):', payload); // DEBUG
      const result = await createLead(payload);
      console.log('✅ Lead created:', result); // DEBUG

      // Step 2: Create separate contact via lead_contacts API (if data provided)
      if (hasContactData) {
        console.log('🚀 Step 2: Creating contact for lead:', result.id); // DEBUG

        const contactPayload = {
          firstName: formData.contact.firstName.trim(),
          lastName: formData.contact.lastName.trim(),
          email: formData.contact.email?.trim() || undefined,
          phone: formData.contact.phone?.trim() || undefined,
          isPrimary: true, // First contact is always primary
        };

        await createLeadContact(result.id, contactPayload);
        console.log('✅ Contact created'); // DEBUG
      }

      // Reset & Close
      setFormData({
        companyName: '',
        city: '',
        postalCode: '',
        businessType: undefined,
        source: undefined,
        notes: '', // Sprint 2.1.5: Zwei-Felder-Lösung
        contact: { firstName: '', lastName: '', email: '', phone: '' },
        consentGiven: false,
        firstContact: undefined,
        estimatedVolume: undefined,
        kitchenSize: undefined,
        employeeCount: undefined,
        website: '',
        industry: '',
      });
      setShowFirstContactFields(false); // Sprint 2.1.5: Reset Checkbox
      setActiveStep(0);

      onCreated(result);
      onClose();
    } catch (e) {
      console.error('❌ Lead creation failed:', e); // DEBUG
      setError(e as Problem);
    } finally {
      setSaving(false);
    }
  };

  const handleClose = () => {
    if (!saving) {
      setError(null);
      setActiveStep(0);
      onClose();
    }
  };

  const renderStepContent = (step: number) => {
    switch (step) {
      case 0:
        // Stage 0: Vormerkung (Company Basics - KEINE personenbezogenen Daten)
        return (
          <Box>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              {t('wizard.stage0.description')}
            </Typography>

            <TextField
              label={`${t('wizard.stage0.companyName')} *`}
              value={formData.companyName}
              onChange={e => setFormData({ ...formData, companyName: e.target.value })}
              fullWidth
              required
              margin="dense"
              error={!!fieldErrors.companyName}
              helperText={fieldErrors.companyName?.[0] || ''}
              inputProps={{ minLength: 2 }}
            />

            <Stack direction="row" spacing={2} sx={{ mt: 1 }}>
              <TextField
                label={t('wizard.stage0.city')}
                value={formData.city}
                onChange={e => setFormData({ ...formData, city: e.target.value })}
                fullWidth
                margin="dense"
              />
              <TextField
                label={t('wizard.stage0.postalCode')}
                value={formData.postalCode}
                onChange={e => setFormData({ ...formData, postalCode: e.target.value })}
                fullWidth
                margin="dense"
                inputProps={{ maxLength: 10 }}
              />
            </Stack>

            <FormControl fullWidth margin="dense" sx={{ mt: 2 }}>
              <InputLabel id="businessType-label">{t('wizard.stage0.businessType')}</InputLabel>
              <Select
                labelId="businessType-label"
                id="businessType-select"
                value={formData.businessType || ''}
                onChange={e =>
                  setFormData({ ...formData, businessType: e.target.value as BusinessType })
                }
                label={t('wizard.stage0.businessType')}
                disabled={isLoadingBusinessTypes}
              >
                <MenuItem value="">
                  <em>{t('wizard.stage0.businessTypePlaceholder')}</em>
                </MenuItem>
                {isLoadingBusinessTypes ? (
                  <MenuItem value="" disabled>
                    <CircularProgress size={20} />
                  </MenuItem>
                ) : (
                  businessTypes?.map(type => (
                    <MenuItem key={type.value} value={type.value}>
                      {type.label}
                    </MenuItem>
                  ))
                )}
              </Select>
            </FormControl>

            {/* Sprint 2.1.5: Lead Source - Dynamically loaded from backend */}
            <FormControl fullWidth margin="dense" sx={{ mt: 2 }}>
              <InputLabel id="source-label">{t('wizard.stage0.source')} *</InputLabel>
              <Select
                labelId="source-label"
                id="source-select"
                value={formData.source || ''}
                onChange={e => {
                  const newSource = e.target.value as LeadSource;
                  setFormData({ ...formData, source: newSource });
                  // Reset Checkbox bei Wechsel zu MESSE/TELEFON
                  if (['MESSE', 'TELEFON'].includes(newSource)) {
                    setShowFirstContactFields(false);
                  }
                }}
                label={`${t('wizard.stage0.source')} *`}
                required
                disabled={isLoadingLeadSources}
              >
                <MenuItem value="">
                  <em>{t('wizard.stage0.sourcePlaceholder')}</em>
                </MenuItem>
                {isLoadingLeadSources ? (
                  <MenuItem value="" disabled>
                    <CircularProgress size={20} />
                  </MenuItem>
                ) : (
                  leadSources?.map(source => (
                    <MenuItem key={source.value} value={source.value}>
                      {source.label}
                    </MenuItem>
                  ))
                )}
              </Select>
            </FormControl>

            {/* Variante B Pre-Claim Hints */}
            {formData.source && (
              <Box
                sx={{
                  mt: 2,
                  p: 1.5,
                  bgcolor: ['MESSE', 'TELEFON'].includes(formData.source) ? '#FFF3E0' : '#E3F2FD',
                  borderRadius: 1,
                  border: '1px solid',
                  borderColor: ['MESSE', 'TELEFON'].includes(formData.source)
                    ? '#FF9800'
                    : '#2196F3',
                }}
              >
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.5 }}>
                  {['MESSE', 'TELEFON'].includes(formData.source)
                    ? 'ℹ️ Vollschutz ab jetzt'
                    : 'ℹ️ Pre-Claim für 10 Tage'}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {['MESSE', 'TELEFON'].includes(formData.source)
                    ? 'Erstkontakt muss jetzt dokumentiert werden. 6-Monate-Schutz startet sofort.'
                    : 'Erstkontakt kann später dokumentiert werden. Sie haben 10 Tage Zeit für Pre-Claim.'}
                </Typography>
              </Box>
            )}

            {/* Sprint 2.1.5: Zwei-Felder-Lösung - Feld 1: Notizen/Quelle (immer sichtbar) */}
            <TextField
              label="Notizen / Quelle (optional)"
              value={formData.notes || ''}
              onChange={e => setFormData({ ...formData, notes: e.target.value })}
              fullWidth
              margin="dense"
              multiline
              rows={2}
              placeholder="Z.B. Empfehlung von Herrn Schulz, Partner-Liste Nr. 47, Stand A-12 auf der INTERNORGA..."
              helperText="Hintergrund-Informationen ohne Einfluss auf Lead-Schutz"
              sx={{ mt: 2 }}
            />

            {/* Sprint 2.1.5: Zwei-Felder-Lösung - Checkbox (nur bei EMPFEHLUNG/WEB/PARTNER/SONSTIGE) */}
            {!['MESSE', 'TELEFON'].includes(formData.source || '') && formData.source && (
              <Box sx={{ mt: 2 }}>
                <FormControlLabel
                  control={
                    <Checkbox
                      checked={showFirstContactFields}
                      onChange={e => {
                        setShowFirstContactFields(e.target.checked);
                        // Reset Erstkontakt-Daten wenn deaktiviert
                        if (!e.target.checked) {
                          setFormData({ ...formData, firstContact: undefined });
                        }
                      }}
                    />
                  }
                  label="☑ Ich hatte bereits Erstkontakt (für sofortigen Lead-Schutz)"
                />
                <Typography variant="caption" color="text.secondary" display="block" sx={{ ml: 4 }}>
                  Aktiviert 6-Monate-Schutz ab jetzt. Nur ankreuzen wenn tatsächlich Erstkontakt
                  stattfand.
                </Typography>
              </Box>
            )}

            {/* Sprint 2.1.5: Zwei-Felder-Lösung - Feld 2: Erstkontakt-Block (conditional) */}
            {(['MESSE', 'TELEFON'].includes(formData.source || '') || showFirstContactFields) && (
              <Box
                sx={{
                  mt: 3,
                  p: 2,
                  bgcolor: 'grey.50',
                  borderRadius: 1,
                  border: '1px solid',
                  borderColor: fieldErrors['firstContact'] ? 'error.main' : 'grey.300',
                }}
              >
                <Typography variant="subtitle2" gutterBottom>
                  {['MESSE', 'TELEFON'].includes(formData.source || '')
                    ? 'Erstkontakt dokumentieren (PFLICHT)'
                    : 'Erstkontakt dokumentieren'}
                  {['MESSE', 'TELEFON'].includes(formData.source || '') && (
                    <Typography component="span" color="error.main">
                      {' '}
                      *
                    </Typography>
                  )}
                </Typography>
                <Typography
                  variant="caption"
                  color="text.secondary"
                  sx={{ mb: 2, display: 'block' }}
                >
                  {['MESSE', 'TELEFON'].includes(formData.source || '')
                    ? 'Wann und wie fand der Erstkontakt statt? (Aktiviert 6-Monate-Schutz)'
                    : 'Wann und wie fand der Erstkontakt statt? (Aktiviert 6-Monate-Schutz ab jetzt)'}
                </Typography>
                {!!fieldErrors['firstContact'] && (
                  <Typography variant="caption" color="error" sx={{ display: 'block', mb: 1 }}>
                    {fieldErrors['firstContact'][0]}
                  </Typography>
                )}

                <TextField
                  label={t('wizard.stage0.firstContactDate')}
                  type="date"
                  value={formData.firstContact?.performedAt || ''}
                  onChange={e => {
                    // Auto-derive channel from source
                    const channelFromSource =
                      formData.source === 'MESSE'
                        ? 'MESSE'
                        : formData.source === 'EMPFEHLUNG'
                          ? 'REFERRAL'
                          : formData.source === 'TELEFON'
                            ? 'PHONE'
                            : 'OTHER';

                    setFormData({
                      ...formData,
                      firstContact: {
                        channel: channelFromSource,
                        performedAt: e.target.value,
                        notes: formData.firstContact?.notes || '',
                      },
                    });
                    // Close native picker by removing focus
                    e.target.blur();
                  }}
                  fullWidth
                  margin="dense"
                  error={!!fieldErrors['firstContact.performedAt']}
                  helperText={
                    fieldErrors['firstContact.performedAt']?.[0] ||
                    'Wann fand der Erstkontakt statt?'
                  }
                  InputLabelProps={{ shrink: true }}
                />

                <TextField
                  label={t('wizard.stage0.firstContactNotes')}
                  value={formData.firstContact?.notes || ''}
                  onChange={e => {
                    // Auto-derive channel from source
                    const channelFromSource =
                      formData.source === 'MESSE'
                        ? 'MESSE'
                        : formData.source === 'EMPFEHLUNG'
                          ? 'REFERRAL'
                          : formData.source === 'TELEFON'
                            ? 'PHONE'
                            : 'OTHER';

                    setFormData({
                      ...formData,
                      firstContact: {
                        channel: channelFromSource,
                        performedAt: formData.firstContact?.performedAt || '',
                        notes: e.target.value,
                      },
                    });
                  }}
                  fullWidth
                  margin="dense"
                  multiline
                  rows={3}
                  error={!!fieldErrors['firstContact.notes']}
                  helperText={
                    fieldErrors['firstContact.notes']?.[0] ||
                    'Was wurde besprochen? Welche nächsten Schritte vereinbart?'
                  }
                  inputProps={{ minLength: 10 }}
                />
              </Box>
            )}
          </Box>
        );

      case 1:
        // Stage 1: Registrierung (Contact Details + DSGVO Consent)
        return (
          <Box>
            <Typography
              variant="body2"
              color="text.secondary"
              sx={{ mb: 2 }}
              dangerouslySetInnerHTML={{ __html: t('wizard.stage1.description') }}
            />

            <Stack direction="row" spacing={2}>
              <TextField
                label={t('wizard.stage1.firstName')}
                value={formData.contact.firstName}
                onChange={e =>
                  setFormData({
                    ...formData,
                    contact: { ...formData.contact, firstName: e.target.value },
                  })
                }
                fullWidth
                margin="dense"
              />
              <TextField
                label={t('wizard.stage1.lastName')}
                value={formData.contact.lastName}
                onChange={e =>
                  setFormData({
                    ...formData,
                    contact: { ...formData.contact, lastName: e.target.value },
                  })
                }
                fullWidth
                margin="dense"
              />
            </Stack>

            <TextField
              label={t('wizard.stage1.email')}
              type="email"
              value={formData.contact.email}
              onChange={e =>
                setFormData({
                  ...formData,
                  contact: { ...formData.contact, email: e.target.value },
                })
              }
              fullWidth
              margin="dense"
              error={!!fieldErrors['contact.email'] || !!fieldErrors['contact']}
              helperText={
                fieldErrors['contact.email']?.[0] ||
                (fieldErrors['contact'] && !formData.contact.phone?.trim()
                  ? fieldErrors['contact'][0]
                  : '')
              }
            />

            <TextField
              label={t('wizard.stage1.phone')}
              type="tel"
              value={formData.contact.phone}
              onChange={e =>
                setFormData({
                  ...formData,
                  contact: { ...formData.contact, phone: e.target.value },
                })
              }
              fullWidth
              margin="dense"
              error={!!fieldErrors['contact'] || !!fieldErrors['contact.phone']}
              helperText={
                fieldErrors['contact.phone']?.[0] ||
                (fieldErrors['contact'] && !formData.contact.email?.trim()
                  ? fieldErrors['contact'][0]
                  : '')
              }
            />

            {/* DSGVO Hinweis (statt Checkbox bei Vertrieb) */}
            <Box
              sx={{
                mt: 2,
                p: 2,
                bgcolor: 'grey.50',
                borderRadius: 1,
                border: '1px solid',
                borderColor: 'grey.300',
              }}
            >
              <Typography variant="body2">
                <strong>Berechtigtes Interesse (Art. 6 Abs. 1 lit. f DSGVO)</strong>
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Verarbeitung zur B2B-Geschäftsanbahnung.
                <Link
                  onClick={() => {
                    window.open(
                      'https://dsgvo-gesetz.de/art-6-dsgvo/',
                      '_blank',
                      'noopener,noreferrer'
                    );
                  }}
                  sx={{ ml: 1, cursor: 'pointer' }}
                >
                  Gesetzestext anzeigen ↗
                </Link>
              </Typography>
              <Typography variant="caption" display="block" sx={{ mt: 1, fontStyle: 'italic' }}>
                Hinweis: Einwilligung nur erforderlich bei Web-Formular (Kunde gibt selbst Daten
                ein).
              </Typography>
            </Box>
          </Box>
        );

      case 2:
        // Stage 2: Qualifizierung (Business Details)
        return (
          <Box>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              {t('wizard.stage2.description')}
            </Typography>

            <Stack direction="row" spacing={2}>
              <TextField
                label={t('wizard.stage2.estimatedVolume')}
                type="number"
                value={formData.estimatedVolume || ''}
                onChange={e =>
                  setFormData({
                    ...formData,
                    estimatedVolume: e.target.value ? Number(e.target.value) : undefined,
                  })
                }
                fullWidth
                margin="dense"
                error={!!fieldErrors.estimatedVolume}
                helperText={fieldErrors.estimatedVolume?.[0] || ''}
                inputProps={{ min: 0 }}
              />
              <FormControl fullWidth margin="dense">
                <InputLabel id="kitchenSize-label">{t('wizard.stage2.kitchenSize')}</InputLabel>
                <Select
                  labelId="kitchenSize-label"
                  id="kitchenSize-select"
                  value={formData.kitchenSize || ''}
                  onChange={e =>
                    setFormData({
                      ...formData,
                      kitchenSize: e.target.value as 'small' | 'medium' | 'large' | undefined,
                    })
                  }
                  label={t('wizard.stage2.kitchenSize')}
                  disabled={isLoadingKitchenSizes}
                >
                  <MenuItem value="">
                    <em>{t('wizard.stage2.kitchenSizePlaceholder')}</em>
                  </MenuItem>
                  {isLoadingKitchenSizes ? (
                    <MenuItem value="" disabled>
                      <CircularProgress size={20} />
                    </MenuItem>
                  ) : (
                    kitchenSizes?.map(size => (
                      <MenuItem key={size.value} value={size.value}>
                        {size.label}
                      </MenuItem>
                    ))
                  )}
                </Select>
              </FormControl>
            </Stack>

            <TextField
              label={t('wizard.stage2.employeeCount')}
              type="number"
              value={formData.employeeCount || ''}
              onChange={e =>
                setFormData({
                  ...formData,
                  employeeCount: e.target.value ? Number(e.target.value) : undefined,
                })
              }
              fullWidth
              margin="dense"
              error={!!fieldErrors.employeeCount}
              helperText={fieldErrors.employeeCount?.[0] || ''}
              inputProps={{ min: 0 }}
            />

            <TextField
              label={t('wizard.stage2.website')}
              type="url"
              value={formData.website}
              onChange={e => setFormData({ ...formData, website: e.target.value })}
              fullWidth
              margin="dense"
              placeholder={t('wizard.stage2.websitePlaceholder')}
            />

            <TextField
              label={t('wizard.stage2.industry')}
              value={formData.industry}
              onChange={e => setFormData({ ...formData, industry: e.target.value })}
              fullWidth
              margin="dense"
              multiline
              rows={2}
            />
          </Box>
        );

      default:
        return null;
    }
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      fullWidth
      maxWidth="md"
      fullScreen={false}
      disableEnforceFocus
    >
      <DialogTitle>
        {t('wizard.title')}
        <IconButton
          aria-label="close"
          onClick={handleClose}
          disabled={saving}
          sx={{
            position: 'absolute',
            right: 8,
            top: 8,
            color: theme => theme.palette.grey[500],
          }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>
      <DialogContent>
        {error?.status === 409 && (
          <Box mb={2}>
            <Alert severity="warning">{t('errors.duplicateEmail')}</Alert>
          </Box>
        )}

        {error && error.status !== 409 && (
          <Box mb={2}>
            <Alert severity="error">
              {error.title ?? t('wizard.validation.validationError')}
              {error.detail ? ` – ${error.detail}` : ''}
            </Alert>
          </Box>
        )}

        <Stepper activeStep={activeStep} sx={{ pt: 3, pb: 5 }}>
          {steps.map(label => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        {renderStepContent(activeStep)}
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} disabled={saving}>
          {t('wizard.actions.cancel')}
        </Button>
        <Box sx={{ flex: '1 1 auto' }} />

        {activeStep > 0 && (
          <Button onClick={handleBack} disabled={saving}>
            {t('wizard.actions.back')}
          </Button>
        )}

        {/* Sprint 2.1.5: Save-Buttons je Karte */}
        {activeStep === 0 && (
          <>
            <Button
              variant="contained"
              onClick={() => handleSave(0)}
              disabled={saving || !formData.companyName.trim() || !formData.source}
            >
              {saving ? t('wizard.actions.saving') : t('wizard.actions.saveVormerkung')}
            </Button>
            <Button onClick={handleNext} disabled={saving}>
              {t('wizard.actions.next')}
            </Button>
          </>
        )}

        {activeStep === 1 && (
          <>
            <Button variant="contained" onClick={() => handleSave(1)} disabled={saving}>
              {saving ? t('wizard.actions.saving') : t('wizard.actions.saveRegistrierung')}
            </Button>
            <Button onClick={handleNext} disabled={saving}>
              {t('wizard.actions.next')}
            </Button>
          </>
        )}

        {activeStep === 2 && (
          <Button variant="contained" onClick={() => handleSave(2)} disabled={saving}>
            {saving ? t('wizard.actions.saving') : t('wizard.actions.saveQualifizierung')}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
}
