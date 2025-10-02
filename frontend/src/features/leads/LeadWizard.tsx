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
import type { LeadFormStage0, LeadFormStage1, LeadFormStage2, Problem, BusinessType } from './types';
import { createLead } from './api';

interface LeadWizardProps {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
}

const steps = ['Vormerkung', 'Registrierung', 'Qualifizierung'];

export default function LeadWizard({ open, onClose, onCreated }: LeadWizardProps) {
  const { t } = useTranslation('leads');
  const [activeStep, setActiveStep] = useState(0);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<Problem | null>(null);

  // Form State (Progressive Profiling)
  const [formData, setFormData] = useState<LeadFormStage2>({
    companyName: '',
    city: '',
    postalCode: '',
    businessType: undefined,
    contact: {
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
    },
    consentGiven: false, // DSGVO Consent (Stage 1)
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
      errors.companyName = ['Firmenname ist Pflicht'];
    } else if (formData.companyName.trim().length < 2) {
      errors.companyName = ['Firmenname muss mindestens 2 Zeichen lang sein'];
    }

    return Object.keys(errors).length > 0 ? errors : null;
  };

  // Stage 1 Validation (Registrierung - Contact + DSGVO Consent)
  const validateStage1 = (): Record<string, string[]> | null => {
    const errors: Record<string, string[]> = {};

    // Validate contact fields if provided
    if (formData.contact.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.contact.email.trim())) {
      errors['contact.email'] = ['Ungültige E-Mail-Adresse'];
    }

    // DSGVO Consent PFLICHT wenn Contact-Daten vorhanden
    const hasContactData =
      formData.contact.firstName ||
      formData.contact.lastName ||
      formData.contact.email ||
      formData.contact.phone;

    if (hasContactData && !formData.consentGiven) {
      errors.consentGiven = ['Einwilligung erforderlich für Kontaktdaten'];
    }

    return Object.keys(errors).length > 0 ? errors : null;
  };

  // Stage 2 Validation (Qualifizierung - Business Details)
  const validateStage2 = (): Record<string, string[]> | null => {
    const errors: Record<string, string[]> = {};

    if (formData.estimatedVolume && formData.estimatedVolume < 0) {
      errors.estimatedVolume = ['Volumen muss positiv sein'];
    }

    if (formData.employeeCount && formData.employeeCount < 0) {
      errors.employeeCount = ['Mitarbeiterzahl muss positiv sein'];
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

    setActiveStep((prevActiveStep) => prevActiveStep + 1);
  };

  const handleBack = () => {
    setError(null);
    setActiveStep((prevActiveStep) => prevActiveStep - 1);
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

      const payload = {
        stage,
        companyName: formData.companyName.trim(),
        city: formData.city?.trim() || undefined,
        postalCode: formData.postalCode?.trim() || undefined,
        businessType: formData.businessType,
        contact: hasContactData
          ? {
              firstName: formData.contact.firstName?.trim() || undefined,
              lastName: formData.contact.lastName?.trim() || undefined,
              email: formData.contact.email?.trim() || undefined,
              phone: formData.contact.phone?.trim() || undefined,
            }
          : undefined,
        consentGivenAt: formData.consentGiven ? new Date().toISOString() : undefined,
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
        contact: { firstName: '', lastName: '', email: '', phone: '' },
        consentGiven: false,
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
              Erfassen Sie grundlegende Firmendaten ohne personenbezogene Informationen.
            </Typography>

            <TextField
              label="Firmenname *"
              value={formData.companyName}
              onChange={(e) => setFormData({ ...formData, companyName: e.target.value })}
              fullWidth
              required
              margin="dense"
              error={!!fieldErrors.companyName}
              helperText={fieldErrors.companyName?.[0] || ''}
              inputProps={{ minLength: 2 }}
            />

            <Stack direction="row" spacing={2} sx={{ mt: 1 }}>
              <TextField
                label="Stadt"
                value={formData.city}
                onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                fullWidth
                margin="dense"
              />
              <TextField
                label="PLZ"
                value={formData.postalCode}
                onChange={(e) => setFormData({ ...formData, postalCode: e.target.value })}
                fullWidth
                margin="dense"
                inputProps={{ maxLength: 10 }}
              />
            </Stack>

            <FormControl fullWidth margin="dense" sx={{ mt: 2 }}>
              <InputLabel id="businessType-label">Branche</InputLabel>
              <Select
                labelId="businessType-label"
                id="businessType-select"
                value={formData.businessType || ''}
                onChange={(e) => setFormData({ ...formData, businessType: e.target.value as BusinessType })}
                label="Branche"
              >
                <MenuItem value="">
                  <em>Bitte wählen</em>
                </MenuItem>
                <MenuItem value="restaurant">Restaurant</MenuItem>
                <MenuItem value="hotel">Hotel</MenuItem>
                <MenuItem value="catering">Catering</MenuItem>
                <MenuItem value="canteen">Kantine</MenuItem>
                <MenuItem value="other">Sonstige</MenuItem>
              </Select>
            </FormControl>
          </Box>
        );

      case 1:
        // Stage 1: Registrierung (Contact Details + DSGVO Consent)
        return (
          <Box>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Erfassen Sie Kontaktdaten. <strong>Einwilligung erforderlich!</strong>
            </Typography>

            <Stack direction="row" spacing={2}>
              <TextField
                label="Vorname"
                value={formData.contact.firstName}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    contact: { ...formData.contact, firstName: e.target.value },
                  })
                }
                fullWidth
                margin="dense"
              />
              <TextField
                label="Nachname"
                value={formData.contact.lastName}
                onChange={(e) =>
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
              label="E-Mail"
              type="email"
              value={formData.contact.email}
              onChange={(e) =>
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
              label="Telefon"
              type="tel"
              value={formData.contact.phone}
              onChange={(e) =>
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
                    onChange={(e) => setFormData({ ...formData, consentGiven: e.target.checked })}
                    required={
                      !!(
                        formData.contact.firstName ||
                        formData.contact.lastName ||
                        formData.contact.email ||
                        formData.contact.phone
                      )
                    }
                    aria-required="true"
                  />
                }
                label={
                  <Typography variant="body2">
                    Ich stimme zu, dass meine Kontaktdaten gespeichert werden.{' '}
                    <Link href="/datenschutz" target="_blank" rel="noopener">
                      Widerruf jederzeit möglich
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
          </Box>
        );

      case 2:
        // Stage 2: Qualifizierung (Business Details)
        return (
          <Box>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Erweiterte Geschäftsdaten für die Qualifizierung.
            </Typography>

            <Stack direction="row" spacing={2}>
              <TextField
                label="Geschätztes Volumen (€/Monat)"
                type="number"
                value={formData.estimatedVolume || ''}
                onChange={(e) =>
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
                <InputLabel id="kitchenSize-label">Küchengröße</InputLabel>
                <Select
                  labelId="kitchenSize-label"
                  id="kitchenSize-select"
                  value={formData.kitchenSize || ''}
                  onChange={(e) =>
                    setFormData({
                      ...formData,
                      kitchenSize: e.target.value as 'small' | 'medium' | 'large' | undefined,
                    })
                  }
                  label="Küchengröße"
                >
                  <MenuItem value="">
                    <em>Bitte wählen</em>
                  </MenuItem>
                  <MenuItem value="small">Klein (&lt;10 Plätze)</MenuItem>
                  <MenuItem value="medium">Mittel (10-50 Plätze)</MenuItem>
                  <MenuItem value="large">Groß (&gt;50 Plätze)</MenuItem>
                </Select>
              </FormControl>
            </Stack>

            <TextField
              label="Mitarbeiterzahl"
              type="number"
              value={formData.employeeCount || ''}
              onChange={(e) =>
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
              label="Website"
              type="url"
              value={formData.website}
              onChange={(e) => setFormData({ ...formData, website: e.target.value })}
              fullWidth
              margin="dense"
              placeholder="https://example.com"
            />

            <TextField
              label="Branche (Details)"
              value={formData.industry}
              onChange={(e) => setFormData({ ...formData, industry: e.target.value })}
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
      <DialogTitle>Neuer Lead (Progressive Profiling)</DialogTitle>
      <DialogContent>
        {error?.status === 409 && (
          <Box mb={2}>
            <Alert severity="warning">Lead mit dieser E-Mail existiert bereits.</Alert>
          </Box>
        )}

        {error && error.status !== 409 && (
          <Box mb={2}>
            <Alert severity="error">
              {error.title ?? 'Fehler'}
              {error.detail ? ` – ${error.detail}` : ''}
            </Alert>
          </Box>
        )}

        <Stepper activeStep={activeStep} sx={{ pt: 3, pb: 5 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        {renderStepContent(activeStep)}
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} disabled={saving}>
          Abbrechen
        </Button>
        <Box sx={{ flex: '1 1 auto' }} />
        {activeStep > 0 && (
          <Button onClick={handleBack} disabled={saving}>
            Zurück
          </Button>
        )}
        {activeStep < steps.length - 1 ? (
          <Button variant="contained" onClick={handleNext} disabled={saving}>
            Weiter
          </Button>
        ) : (
          <Button variant="contained" onClick={handleSubmit} disabled={saving || !formData.companyName.trim()}>
            {saving ? 'Speichert...' : 'Lead erstellen'}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
}
