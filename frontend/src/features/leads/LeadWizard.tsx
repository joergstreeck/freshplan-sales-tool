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
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Checkbox,
  Typography,
  Link,
  Alert,
  Box,
  Stack,
} from '@mui/material';
import { useTranslation } from 'react-i18next';
import type { LeadFormStage2, Problem, BusinessType, LeadSource, FirstContact } from './types';
import { createLead } from './api';

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

  // Form State (Progressive Profiling - Sprint 2.1.5)
  const [formData, setFormData] = useState<LeadFormStage2 & {
    source?: LeadSource;
    firstContact?: FirstContact;
  }>({
    companyName: '',
    city: '',
    postalCode: '',
    businessType: undefined,
    source: undefined, // Sprint 2.1.5: MESSE, EMPFEHLUNG, TELEFON, etc.
    contact: {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
    },
    consentGiven: false, // DSGVO Consent (Stage 1)
    firstContact: undefined, // Sprint 2.1.5: Erstkontakt-Block
    estimatedVolume: undefined,
    kitchenSize: undefined,
    employeeCount: undefined,
    website: '',
    industry: '',
  });

  const fieldErrors = error?.errors || {};

  // Stage 0 Validation (Vormerkung - Company Basics)
  const validateStage0 = (): Record<string, string[]> | null => {
    const errors: Record<string, string[]> = {};

    if (!formData.companyName.trim()) {
      errors.companyName = [t('wizard.stage0.companyNameRequired')];
    } else if (formData.companyName.trim().length < 2) {
      errors.companyName = [t('wizard.validation.companyNameMin')];
    }

    return Object.keys(errors).length > 0 ? errors : null;
  };

  // Stage 1 Validation (Registrierung - Contact + DSGVO Consent + Erstkontakt)
  const validateStage1 = (): Record<string, string[]> | null => {
    const errors: Record<string, string[]> = {};

    // Validate contact fields if provided
    if (
      formData.contact.email &&
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.contact.email.trim())
    ) {
      errors['contact.email'] = [t('wizard.validation.emailInvalid')];
    }

    // Sprint 2.1.5: Quellenabhängige Pflichtfelder
    const requiresFirstContact = ['MESSE', 'EMPFEHLUNG', 'TELEFON'].includes(formData.source || '');
    const hasContactData =
      formData.contact.firstName ||
      formData.contact.lastName ||
      formData.contact.email ||
      formData.contact.phone;

    // Erstkontakt-Block PFLICHT wenn kein Kontakt UND Quelle erfordert es
    if (requiresFirstContact && !hasContactData && !formData.firstContact) {
      errors.firstContact = [t('wizard.stage1.firstContactRequired')];
    }

    // Erstkontakt-Block Validierung (wenn vorhanden)
    if (formData.firstContact) {
      if (!formData.firstContact.channel) {
        errors['firstContact.channel'] = [t('wizard.stage1.firstContactChannelRequired')];
      }
      if (!formData.firstContact.performedAt) {
        errors['firstContact.performedAt'] = [t('wizard.stage1.firstContactDateRequired')];
      }
      if (!formData.firstContact.notes || formData.firstContact.notes.trim().length < 10) {
        errors['firstContact.notes'] = [t('wizard.stage1.firstContactNotesMin')];
      }
    }

    // DSGVO Consent: Source-abhängig (WEB_FORMULAR = PFLICHT, andere = optional)
    const requiresConsent = formData.source === 'WEB_FORMULAR';
    if (requiresConsent && hasContactData && !formData.consentGiven) {
      errors.consentGiven = [t('wizard.stage1.consentRequired')];
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

  const handleSubmit = async () => {
    const validationErrors = validateStage2();
    if (validationErrors) {
      setError({ errors: validationErrors, status: 400, title: 'Validierungsfehler' });
      return;
    }

    setSaving(true);
    setError(null);

    try {
      // Determine stage based on form data
      let stage = 0;
      const hasContactData =
        formData.contact.firstName ||
        formData.contact.lastName ||
        formData.contact.email ||
        formData.contact.phone;

      if (hasContactData && formData.consentGiven) {
        stage = 1;
      }

      const hasBusinessData =
        formData.estimatedVolume !== undefined ||
        formData.kitchenSize !== undefined ||
        formData.employeeCount !== undefined ||
        (formData.website && formData.website.trim() !== '');

      if (hasBusinessData) {
        stage = 2;
      }

      // Sprint 2.1.5: Erstkontakt → activities[] Transformation
      const activities = formData.firstContact
        ? [
            {
              activityType: 'FIRST_CONTACT_DOCUMENTED',
              performedAt: formData.firstContact.performedAt,
              summary: `${formData.firstContact.channel}: ${formData.firstContact.notes}`,
              countsAsProgress: false, // System Activity
              metadata: {
                channel: formData.firstContact.channel,
                notes: formData.firstContact.notes,
              },
            },
          ]
        : undefined;

      const payload = {
        stage,
        companyName: formData.companyName.trim(),
        city: formData.city?.trim() || undefined,
        postalCode: formData.postalCode?.trim() || undefined,
        businessType: formData.businessType,
        source: formData.source || undefined, // Sprint 2.1.5: Lead Source
        contact: hasContactData
          ? {
              firstName: formData.contact.firstName?.trim() || undefined,
              lastName: formData.contact.lastName?.trim() || undefined,
              email: formData.contact.email?.trim() || undefined,
              phone: formData.contact.phone?.trim() || undefined,
            }
          : undefined,
        // ⚠️ Sprint 2.1.5: consentGivenAt NICHT senden (UI-only, Backend-Feld erst Sprint 2.1.6)
        // consentGivenAt wird erst in Sprint 2.1.6 (V259) ins Backend-Schema aufgenommen
        activities, // Sprint 2.1.5: Erstkontakt als FIRST_CONTACT_DOCUMENTED
        estimatedVolume: formData.estimatedVolume,
        kitchenSize: formData.kitchenSize,
        employeeCount: formData.employeeCount,
        website: formData.website?.trim() || undefined,
        industry: formData.industry?.trim() || undefined,
      };

      await createLead(payload);

      // Reset form
      setFormData({
        companyName: '',
        city: '',
        postalCode: '',
        businessType: undefined,
        source: undefined, // Sprint 2.1.5
        contact: { firstName: '', lastName: '', email: '', phone: '' },
        consentGiven: false,
        firstContact: undefined, // Sprint 2.1.5
        estimatedVolume: undefined,
        kitchenSize: undefined,
        employeeCount: undefined,
        website: '',
        industry: '',
      });
      setActiveStep(0);

      onCreated();
    } catch (e) {
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
              >
                <MenuItem value="">
                  <em>{t('wizard.stage0.businessTypePlaceholder')}</em>
                </MenuItem>
                <MenuItem value="restaurant">{t('wizard.businessTypes.restaurant')}</MenuItem>
                <MenuItem value="hotel">{t('wizard.businessTypes.hotel')}</MenuItem>
                <MenuItem value="catering">{t('wizard.businessTypes.catering')}</MenuItem>
                <MenuItem value="canteen">{t('wizard.businessTypes.canteen')}</MenuItem>
                <MenuItem value="other">{t('wizard.businessTypes.other')}</MenuItem>
              </Select>
            </FormControl>

            {/* Sprint 2.1.5: Lead Source */}
            <FormControl fullWidth margin="dense" sx={{ mt: 2 }}>
              <InputLabel id="source-label">{t('wizard.stage0.source')}</InputLabel>
              <Select
                labelId="source-label"
                id="source-select"
                value={formData.source || ''}
                onChange={e =>
                  setFormData({ ...formData, source: e.target.value as LeadSource })
                }
                label={t('wizard.stage0.source')}
              >
                <MenuItem value="">
                  <em>{t('wizard.stage0.sourcePlaceholder')}</em>
                </MenuItem>
                <MenuItem value="MESSE">{t('wizard.sources.messe')}</MenuItem>
                <MenuItem value="EMPFEHLUNG">{t('wizard.sources.empfehlung')}</MenuItem>
                <MenuItem value="TELEFON">{t('wizard.sources.telefon')}</MenuItem>
                <MenuItem value="WEB_FORMULAR">{t('wizard.sources.webFormular')}</MenuItem>
                <MenuItem value="PARTNER">{t('wizard.sources.partner')}</MenuItem>
                <MenuItem value="SONSTIGE">{t('wizard.sources.sonstige')}</MenuItem>
              </Select>
            </FormControl>
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
              error={!!fieldErrors['contact.email']}
              helperText={fieldErrors['contact.email']?.[0] || ''}
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
            />

            {/* DSGVO Consent Checkbox (PFLICHT bei Contact-Daten) */}
            <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={formData.consentGiven}
                    onChange={e => setFormData({ ...formData, consentGiven: e.target.checked })}
                    required={
                      !!(
                        formData.contact.firstName ||
                        formData.contact.lastName ||
                        formData.contact.email ||
                        formData.contact.phone
                      )
                    }
                    aria-required={
                      !!(
                        formData.contact.firstName ||
                        formData.contact.lastName ||
                        formData.contact.email ||
                        formData.contact.phone
                      )
                    }
                  />
                }
                label={
                  <Typography variant="body2">
                    {t('wizard.stage1.consentLabel')}{' '}
                    <Link href="/datenschutz" target="_blank" rel="noopener">
                      {t('wizard.stage1.consentLink')}
                    </Link>
                  </Typography>
                }
              />
              {!!fieldErrors.consentGiven && (
                <Typography variant="caption" color="error" sx={{ display: 'block', mt: 1 }}>
                  {fieldErrors.consentGiven[0]}
                </Typography>
              )}
            </Box>

            {/* Sprint 2.1.5: Erstkontakt-Block (conditional) */}
            {['MESSE', 'EMPFEHLUNG', 'TELEFON'].includes(formData.source || '') && (
              <Box sx={{ mt: 3, p: 2, bgcolor: 'action.hover', borderRadius: 1 }}>
                <Typography variant="subtitle2" gutterBottom>
                  {t('wizard.stage1.firstContactTitle')}
                </Typography>
                <Typography variant="caption" color="text.secondary" sx={{ mb: 2, display: 'block' }}>
                  {t('wizard.stage1.firstContactHint')}
                </Typography>

                <FormControl fullWidth margin="dense" error={!!fieldErrors['firstContact.channel']}>
                  <InputLabel id="firstContact-channel-label">
                    {t('wizard.stage1.firstContactChannel')} *
                  </InputLabel>
                  <Select
                    labelId="firstContact-channel-label"
                    id="firstContact-channel-select"
                    value={formData.firstContact?.channel || ''}
                    onChange={e =>
                      setFormData({
                        ...formData,
                        firstContact: {
                          channel: e.target.value as FirstContact['channel'],
                          performedAt: formData.firstContact?.performedAt || '',
                          notes: formData.firstContact?.notes || '',
                        },
                      })
                    }
                    label={`${t('wizard.stage1.firstContactChannel')} *`}
                  >
                    <MenuItem value="">
                      <em>{t('wizard.stage1.firstContactChannelPlaceholder')}</em>
                    </MenuItem>
                    <MenuItem value="MESSE">{t('wizard.firstContactChannels.messe')}</MenuItem>
                    <MenuItem value="PHONE">{t('wizard.firstContactChannels.phone')}</MenuItem>
                    <MenuItem value="EMAIL">{t('wizard.firstContactChannels.email')}</MenuItem>
                    <MenuItem value="REFERRAL">{t('wizard.firstContactChannels.referral')}</MenuItem>
                    <MenuItem value="OTHER">{t('wizard.firstContactChannels.other')}</MenuItem>
                  </Select>
                  {!!fieldErrors['firstContact.channel'] && (
                    <Typography variant="caption" color="error" sx={{ mt: 0.5 }}>
                      {fieldErrors['firstContact.channel'][0]}
                    </Typography>
                  )}
                </FormControl>

                <TextField
                  label={`${t('wizard.stage1.firstContactDate')} *`}
                  type="datetime-local"
                  value={formData.firstContact?.performedAt || ''}
                  onChange={e =>
                    setFormData({
                      ...formData,
                      firstContact: {
                        channel: formData.firstContact?.channel || 'OTHER',
                        performedAt: e.target.value,
                        notes: formData.firstContact?.notes || '',
                      },
                    })
                  }
                  fullWidth
                  margin="dense"
                  error={!!fieldErrors['firstContact.performedAt']}
                  helperText={fieldErrors['firstContact.performedAt']?.[0] || ''}
                  InputLabelProps={{ shrink: true }}
                />

                <TextField
                  label={`${t('wizard.stage1.firstContactNotes')} *`}
                  value={formData.firstContact?.notes || ''}
                  onChange={e =>
                    setFormData({
                      ...formData,
                      firstContact: {
                        channel: formData.firstContact?.channel || 'OTHER',
                        performedAt: formData.firstContact?.performedAt || '',
                        notes: e.target.value,
                      },
                    })
                  }
                  fullWidth
                  margin="dense"
                  multiline
                  rows={3}
                  error={!!fieldErrors['firstContact.notes']}
                  helperText={
                    fieldErrors['firstContact.notes']?.[0] ||
                    t('wizard.stage1.firstContactNotesHelper')
                  }
                  inputProps={{ minLength: 10 }}
                />

                {!!fieldErrors.firstContact && (
                  <Typography variant="caption" color="error" sx={{ display: 'block', mt: 1 }}>
                    {fieldErrors.firstContact[0]}
                  </Typography>
                )}
              </Box>
            )}
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
                >
                  <MenuItem value="">
                    <em>{t('wizard.stage2.kitchenSizePlaceholder')}</em>
                  </MenuItem>
                  <MenuItem value="small">{t('wizard.kitchenSizes.small')}</MenuItem>
                  <MenuItem value="medium">{t('wizard.kitchenSizes.medium')}</MenuItem>
                  <MenuItem value="large">{t('wizard.kitchenSizes.large')}</MenuItem>
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
    <Dialog open={open} onClose={handleClose} fullWidth maxWidth="md" fullScreen={false}>
      <DialogTitle>{t('wizard.title')}</DialogTitle>
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
        {activeStep < steps.length - 1 ? (
          <Button variant="contained" onClick={handleNext} disabled={saving}>
            {t('wizard.actions.next')}
          </Button>
        ) : (
          <Button
            variant="contained"
            onClick={handleSubmit}
            disabled={saving || !formData.companyName.trim()}
          >
            {saving ? t('wizard.actions.saving') : t('wizard.actions.create')}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
}
