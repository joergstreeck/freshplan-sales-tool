/**
 * Lead Wizard Component - Progressive Profiling
 *
 * Sprint 2.1.7.2 D11.2: Server-Driven UI Migration
 *
 * Schema-driven Lead creation wizard with 3 Progressive Profiling stages.
 * Backend controls form structure via LeadSchemaResource (/api/leads/schema).
 *
 * Architecture:
 * - Backend: Single Source of Truth for Lead schema + field definitions
 * - Frontend: Rendering Layer (dynamic field rendering from schema)
 * - No hardcoded field definitions (except Contact fields - separate entity)
 *
 * Progressive Profiling Stages:
 * - Stage 0: Pre-Claim (10 Tage Schutz) - Company basics from schema
 * - Stage 1: Vollschutz (6 Monate) - Business details + Contact person
 * - Stage 2: Nurturing - Pain Points, Relationship Status
 *
 * Benefits:
 * - Backend controls Lead form structure
 * - Automatic label fixes (e.g., "€/Jahr" statt "€/Monat")
 * - No frontend/backend parity issues
 * - Enum sources from backend (/api/enums/...)
 * - Future field changes only in backend
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import { useState, useEffect } from 'react';
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
  Grid,
  Autocomplete,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import type { Problem, FirstContact } from './types';
import { createLead, createLeadContact } from './api';
import { useLeadSchema } from '../../hooks/useLeadSchema';
import { useEnumOptions, type EnumOption } from '../../hooks/useEnumOptions';
import type { FieldDefinition } from '../../hooks/useContactSchema';

interface LeadWizardProps {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
}

export default function LeadWizard({ open, onClose, onCreated }: LeadWizardProps) {
  const steps = ['Basis-Informationen', 'Erweiterte Informationen', 'Nurturing & Qualifikation'];
  const [activeStep, setActiveStep] = useState(0);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<Problem | null>(null);

  // Fetch schema from backend (Server-Driven UI)
  const { data: schemas, isLoading: schemaLoading } = useLeadSchema();
  const progressiveSchema = schemas?.find(s => s.cardId === 'lead_progressive_profiling');

  // Extract sections for each stage
  const stage0Section = progressiveSchema?.sections.find(s => s.sectionId === 'stage_0_pre_claim');
  const stage1Section = progressiveSchema?.sections.find(s => s.sectionId === 'stage_1_vollschutz');
  const stage2Section = progressiveSchema?.sections.find(s => s.sectionId === 'stage_2_nurturing');

  // Load enum options for all ENUM fields across all stages
  const sourceField = stage0Section?.fields.find(f => f.fieldKey === 'source');
  const businessTypeField = stage1Section?.fields.find(f => f.fieldKey === 'businessType');
  const kitchenSizeField = stage1Section?.fields.find(f => f.fieldKey === 'kitchenSize');
  const relationshipStatusField = stage2Section?.fields.find(
    f => f.fieldKey === 'relationshipStatus'
  );
  const decisionMakerAccessField = stage2Section?.fields.find(
    f => f.fieldKey === 'decisionMakerAccess'
  );

  const { data: sourceOptions, isLoading: sourceLoading } = useEnumOptions(
    sourceField?.enumSource || ''
  );
  const { data: businessTypeOptions, isLoading: businessTypeLoading } = useEnumOptions(
    businessTypeField?.enumSource || ''
  );
  const { data: kitchenSizeOptions, isLoading: kitchenSizeLoading } = useEnumOptions(
    kitchenSizeField?.enumSource || ''
  );
  const { data: relationshipStatusOptions, isLoading: relationshipStatusLoading } = useEnumOptions(
    relationshipStatusField?.enumSource || ''
  );
  const { data: decisionMakerAccessOptions, isLoading: decisionMakerAccessLoading } =
    useEnumOptions(decisionMakerAccessField?.enumSource || '');

  // Build enum options map for renderField

  const enumOptionsMap: Record<string, { options: EnumOption[]; loading: boolean }> = {
    source: { options: sourceOptions || [], loading: sourceLoading },
    businessType: { options: businessTypeOptions || [], loading: businessTypeLoading },
    kitchenSize: { options: kitchenSizeOptions || [], loading: kitchenSizeLoading },
    relationshipStatus: {
      options: relationshipStatusOptions || [],
      loading: relationshipStatusLoading,
    },
    decisionMakerAccess: {
      options: decisionMakerAccessOptions || [],
      loading: decisionMakerAccessLoading,
    },
  };

  // Form State (dynamically built from schema)
  const [formData, setFormData] = useState<Record<string, unknown>>({});

  // Contact person state (Stage 1 - not in schema, separate entity)
  const [contactData, setContactData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
  });

  // FirstContact state (Stage 0 custom logic)
  const [firstContact, setFirstContact] = useState<FirstContact | undefined>(undefined);
  const [showFirstContactFields, setShowFirstContactFields] = useState(false);

  const fieldErrors = error?.errors || {};

  // Initialize formData from schema when schema loads
  useEffect(() => {
    if (progressiveSchema) {
      const initialData: Record<string, unknown> = {};
      progressiveSchema.sections.forEach(section => {
        section.fields.forEach(field => {
          if (field.type === 'BOOLEAN') {
            initialData[field.fieldKey] = false;
          } else {
            initialData[field.fieldKey] = '';
          }
        });
      });
      setFormData(initialData);
    }
  }, [progressiveSchema]);

  // Stage 0 Validation
  const validateStage0 = (): Record<string, string[]> | null => {
    const errors: Record<string, string[]> = {};

    if (
      !formData.companyName ||
      (typeof formData.companyName === 'string' && !formData.companyName.trim())
    ) {
      errors.companyName = ['Firmenname ist erforderlich'];
    } else if (typeof formData.companyName === 'string' && formData.companyName.trim().length < 2) {
      errors.companyName = ['Firmenname muss mindestens 2 Zeichen lang sein'];
    }

    if (!formData.source) {
      errors.source = ['Quelle ist erforderlich'];
    }

    // FirstContact validation (MESSE, TELEFON require documentation)
    const requiresFirstContact = ['MESSE', 'TELEFON'].includes((formData.source as string) || '');
    const showFirstContactBlock = requiresFirstContact || showFirstContactFields;

    if (showFirstContactBlock) {
      const hasStartedFirstContact = firstContact?.performedAt || firstContact?.notes?.trim();

      if (requiresFirstContact && !hasStartedFirstContact) {
        errors.firstContact = ['Erstkontakt-Dokumentation ist erforderlich für MESSE/TELEFON'];
      } else if (hasStartedFirstContact) {
        if (requiresFirstContact && !firstContact?.performedAt) {
          errors['firstContact.performedAt'] = ['Datum des Erstkontakts ist erforderlich'];
        }
        if (!firstContact?.notes || firstContact.notes.trim().length < 10) {
          errors['firstContact.notes'] = ['Notizen müssen mindestens 10 Zeichen lang sein'];
        }
      }
    }

    return Object.keys(errors).length > 0 ? errors : null;
  };

  // Stage 1 Validation
  const validateStage1 = (): Record<string, string[]> | null => {
    const errors: Record<string, string[]> = {};

    // Contact person: at least email OR phone
    const hasEmail = contactData.email?.trim();
    const hasPhone = contactData.phone?.trim();

    if (!hasEmail && !hasPhone) {
      errors.contact = ['Mindestens E-Mail oder Telefon erforderlich'];
    }

    if (hasEmail && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(hasEmail)) {
      errors['contact.email'] = ['Ungültige E-Mail-Adresse'];
    }

    return Object.keys(errors).length > 0 ? errors : null;
  };

  // Stage 2 Validation
  const validateStage2 = (): Record<string, string[]> | null => {
    const errors: Record<string, string[]> = {};

    if (typeof formData.estimatedVolume === 'number' && formData.estimatedVolume < 0) {
      errors.estimatedVolume = ['Wert muss positiv sein'];
    }

    if (typeof formData.employeeCount === 'number' && formData.employeeCount < 0) {
      errors.employeeCount = ['Wert muss positiv sein'];
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

  // Save Lead (per stage)
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
      const hasContactData =
        contactData.firstName?.trim() &&
        contactData.lastName?.trim() &&
        (contactData.email?.trim() || contactData.phone?.trim());

      // Build activities array from firstContact
      const activities = firstContact
        ? [
            {
              activityType: 'FIRST_CONTACT_DOCUMENTED',
              performedAt: firstContact.performedAt,
              summary: `${firstContact.channel}: ${firstContact.notes}`,
              countsAsProgress: false,
              metadata: {
                channel: firstContact.channel,
                notes: firstContact.notes,
              },
            },
          ]
        : undefined;

      // Build payload from formData (include fields up to current stage)
      const payload: Record<string, unknown> = {
        stage,
      };

      // Add fields based on stage (progressive disclosure)
      if (stage >= 0 && stage0Section) {
        stage0Section.fields.forEach(field => {
          const value = formData[field.fieldKey];
          if (value !== null && value !== undefined && value !== '') {
            if (typeof value === 'string') {
              const trimmed = value.trim();
              if (trimmed) payload[field.fieldKey] = trimmed;
            } else {
              payload[field.fieldKey] = value;
            }
          }
        });
        // Legacy support: companyName → name
        if (formData.companyName) {
          payload.name = formData.companyName;
        }
      }

      if (stage >= 1 && stage1Section) {
        stage1Section.fields.forEach(field => {
          const value = formData[field.fieldKey];
          if (value !== null && value !== undefined && value !== '') {
            if (typeof value === 'string') {
              const trimmed = value.trim();
              if (trimmed) payload[field.fieldKey] = trimmed;
            } else {
              payload[field.fieldKey] = value;
            }
          }
        });
      }

      if (stage >= 2 && stage2Section) {
        stage2Section.fields.forEach(field => {
          const value = formData[field.fieldKey];
          if (value !== null && value !== undefined && value !== '') {
            if (typeof value === 'string') {
              const trimmed = value.trim();
              if (trimmed) payload[field.fieldKey] = trimmed;
            } else {
              payload[field.fieldKey] = value;
            }
          }
        });
      }

      // Add activities if firstContact was documented
      if (activities) {
        payload.activities = activities;
      }

      console.log('🚀 Step 1: Creating lead (schema-driven):', payload);
      const result = await createLead(payload);
      console.log('✅ Lead created:', result);

      // Step 2: Create separate contact via lead_contacts API (if data provided)
      if (hasContactData) {
        console.log('🚀 Step 2: Creating contact for lead:', result.id);

        const contactPayload = {
          firstName: contactData.firstName.trim(),
          lastName: contactData.lastName.trim(),
          email: contactData.email?.trim() || undefined,
          phone: contactData.phone?.trim() || undefined,
          isPrimary: true,
        };

        await createLeadContact(result.id, contactPayload);
        console.log('✅ Contact created');
      }

      // Reset form and close dialog
      const initialData: Record<string, unknown> = {};
      progressiveSchema?.sections.forEach(section => {
        section.fields.forEach(field => {
          if (field.type === 'BOOLEAN') {
            initialData[field.fieldKey] = false;
          } else {
            initialData[field.fieldKey] = '';
          }
        });
      });
      setFormData(initialData);
      setContactData({ firstName: '', lastName: '', email: '', phone: '' });
      setFirstContact(undefined);
      setShowFirstContactFields(false);
      setActiveStep(0);

      onCreated(result);
      onClose();
    } catch (e) {
      console.error('❌ Lead creation failed:', e);
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

  // Helper: Render field from schema
  const renderField = (field: FieldDefinition) => {
    const value = formData[field.fieldKey];
    const fieldError = fieldErrors[field.fieldKey];

    // ENUM type
    if (field.type === 'ENUM') {
      const enumData = enumOptionsMap[field.fieldKey] || { options: [], loading: false };

      // Special case: source field with Autocomplete for better UX
      if (field.fieldKey === 'source') {
        return (
          <Grid key={field.fieldKey} size={{ xs: 12, sm: field.gridCols || 12 }}>
            <Autocomplete
              options={enumData.options}
              getOptionLabel={option => option.label}
              value={enumData.options.find(opt => opt.value === value) || null}
              onChange={(_, newValue) => {
                setFormData({ ...formData, source: newValue?.value || '' });
                // Reset firstContact checkbox when switching to MESSE/TELEFON
                if (['MESSE', 'TELEFON'].includes(newValue?.value || '')) {
                  setShowFirstContactFields(false);
                }
              }}
              disabled={enumData.loading || saving}
              renderInput={params => (
                <TextField
                  {...params}
                  label={field.label + (field.required ? ' *' : '')}
                  error={!!fieldError}
                  helperText={fieldError?.[0] || field.helpText}
                  required={field.required}
                />
              )}
              fullWidth
            />
          </Grid>
        );
      }

      // Standard Select for other ENUMs
      return (
        <Grid key={field.fieldKey} size={{ xs: 12, sm: field.gridCols || 12 }}>
          <FormControl fullWidth error={!!fieldError}>
            <InputLabel id={`${field.fieldKey}-label`}>
              {field.label}
              {field.required && ' *'}
            </InputLabel>
            <Select
              labelId={`${field.fieldKey}-label`}
              value={value || ''}
              onChange={e => setFormData({ ...formData, [field.fieldKey]: e.target.value })}
              label={field.label + (field.required ? ' *' : '')}
              disabled={enumData.loading || saving}
            >
              <MenuItem value="">
                <em>Nicht angegeben</em>
              </MenuItem>
              {enumData.options.map(option => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
            {(fieldError || field.helpText) && (
              <Typography
                variant="caption"
                sx={{ mt: 0.5, ml: 1.75, color: fieldError ? 'error.main' : 'text.secondary' }}
              >
                {fieldError?.[0] || field.helpText}
              </Typography>
            )}
          </FormControl>
        </Grid>
      );
    }

    // TEXT type
    if (field.type === 'TEXT') {
      return (
        <Grid key={field.fieldKey} size={{ xs: 12, sm: field.gridCols || 12 }}>
          <TextField
            label={field.label}
            value={value || ''}
            onChange={e => setFormData({ ...formData, [field.fieldKey]: e.target.value })}
            required={field.required}
            disabled={saving}
            fullWidth
            placeholder={field.placeholder}
            helperText={fieldError?.[0] || field.helpText}
            error={!!fieldError}
          />
        </Grid>
      );
    }

    // TEXTAREA type
    if (field.type === 'TEXTAREA') {
      return (
        <Grid key={field.fieldKey} size={{ xs: 12, sm: field.gridCols || 12 }}>
          <TextField
            label={field.label}
            value={value || ''}
            onChange={e => setFormData({ ...formData, [field.fieldKey]: e.target.value })}
            required={field.required}
            disabled={saving}
            fullWidth
            multiline
            rows={3}
            placeholder={field.placeholder}
            helperText={fieldError?.[0] || field.helpText}
            error={!!fieldError}
          />
        </Grid>
      );
    }

    // NUMBER type
    if (field.type === 'NUMBER') {
      return (
        <Grid key={field.fieldKey} size={{ xs: 12, sm: field.gridCols || 12 }}>
          <TextField
            label={field.label}
            type="number"
            value={value || ''}
            onChange={e =>
              setFormData({
                ...formData,
                [field.fieldKey]: e.target.value ? Number(e.target.value) : '',
              })
            }
            required={field.required}
            disabled={saving}
            fullWidth
            placeholder={field.placeholder}
            helperText={fieldError?.[0] || field.helpText}
            error={!!fieldError}
            inputProps={{ min: 0 }}
          />
        </Grid>
      );
    }

    // CURRENCY type (same as NUMBER but with currency formatting hint)
    if (field.type === 'CURRENCY') {
      return (
        <Grid key={field.fieldKey} size={{ xs: 12, sm: field.gridCols || 12 }}>
          <TextField
            label={field.label}
            type="number"
            value={value || ''}
            onChange={e =>
              setFormData({
                ...formData,
                [field.fieldKey]: e.target.value ? Number(e.target.value) : '',
              })
            }
            required={field.required}
            disabled={saving}
            fullWidth
            placeholder={field.placeholder}
            helperText={fieldError?.[0] || field.helpText}
            error={!!fieldError}
            inputProps={{ min: 0, step: 1000 }}
          />
        </Grid>
      );
    }

    // BOOLEAN type
    if (field.type === 'BOOLEAN') {
      return (
        <Grid key={field.fieldKey} size={{ xs: 12, sm: field.gridCols || 12 }}>
          <FormControlLabel
            control={
              <Checkbox
                checked={!!value}
                onChange={e => setFormData({ ...formData, [field.fieldKey]: e.target.checked })}
                disabled={saving}
              />
            }
            label={
              <Box>
                <Typography variant="body2">{field.label}</Typography>
                {field.helpText && (
                  <Typography variant="caption" color="text.secondary">
                    {field.helpText}
                  </Typography>
                )}
              </Box>
            }
          />
        </Grid>
      );
    }

    // Fallback
    return (
      <Grid key={field.fieldKey} size={{ xs: 12 }}>
        <Typography color="error">Unbekannter Feldtyp: {field.type}</Typography>
      </Grid>
    );
  };

  const renderStepContent = (step: number) => {
    if (schemaLoading) {
      return (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress />
        </Box>
      );
    }

    if (!progressiveSchema) {
      return (
        <Alert severity="error" sx={{ my: 2 }}>
          Schema konnte nicht geladen werden. Bitte versuchen Sie es später erneut.
        </Alert>
      );
    }

    switch (step) {
      case 0:
        // Stage 0: Pre-Claim (schema-driven + custom firstContact logic)
        return (
          <Box>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              {stage0Section?.subtitle}
            </Typography>

            <Grid container spacing={2}>
              {stage0Section?.fields.map(field => renderField(field))}
            </Grid>

            {/* Source-specific hints */}
            {formData.source && (
              <Box
                sx={{
                  mt: 3,
                  p: 1.5,
                  bgcolor: ['MESSE', 'TELEFON'].includes(formData.source as string)
                    ? 'warning.lighter'
                    : 'info.lighter',
                  borderRadius: 1,
                  border: '1px solid',
                  borderColor: ['MESSE', 'TELEFON'].includes(formData.source as string)
                    ? 'warning.main'
                    : 'info.main',
                }}
              >
                <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.5 }}>
                  {['MESSE', 'TELEFON'].includes(formData.source as string)
                    ? 'ℹ️ Vollschutz ab jetzt'
                    : 'ℹ️ Pre-Claim für 10 Tage'}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {['MESSE', 'TELEFON'].includes(formData.source as string)
                    ? 'Erstkontakt muss jetzt dokumentiert werden. 6-Monate-Schutz startet sofort.'
                    : 'Erstkontakt kann später dokumentiert werden. Sie haben 10 Tage Zeit für Pre-Claim.'}
                </Typography>
              </Box>
            )}

            {/* Optional firstContact checkbox (nur bei EMPFEHLUNG/WEB/PARTNER/SONSTIGE) */}
            {!['MESSE', 'TELEFON'].includes((formData.source as string) || '') &&
              formData.source && (
                <Box sx={{ mt: 2 }}>
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={showFirstContactFields}
                        onChange={e => {
                          setShowFirstContactFields(e.target.checked);
                          if (!e.target.checked) {
                            setFirstContact(undefined);
                          }
                        }}
                      />
                    }
                    label="☑ Ich hatte bereits Erstkontakt (für sofortigen Lead-Schutz)"
                  />
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    display="block"
                    sx={{ ml: 4 }}
                  >
                    Aktiviert 6-Monate-Schutz ab jetzt. Nur ankreuzen wenn tatsächlich Erstkontakt
                    stattfand.
                  </Typography>
                </Box>
              )}

            {/* FirstContact block (conditional) */}
            {(['MESSE', 'TELEFON'].includes((formData.source as string) || '') ||
              showFirstContactFields) && (
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
                  {['MESSE', 'TELEFON'].includes((formData.source as string) || '')
                    ? 'Erstkontakt dokumentieren (PFLICHT)'
                    : 'Erstkontakt dokumentieren'}
                  {['MESSE', 'TELEFON'].includes((formData.source as string) || '') && (
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
                  {['MESSE', 'TELEFON'].includes((formData.source as string) || '')
                    ? 'Wann und wie fand der Erstkontakt statt? (Aktiviert 6-Monate-Schutz)'
                    : 'Wann und wie fand der Erstkontakt statt? (Aktiviert 6-Monate-Schutz ab jetzt)'}
                </Typography>
                {!!fieldErrors['firstContact'] && (
                  <Typography variant="caption" color="error" sx={{ display: 'block', mb: 1 }}>
                    {fieldErrors['firstContact'][0]}
                  </Typography>
                )}

                <TextField
                  label="Datum des Erstkontakts"
                  type="date"
                  value={firstContact?.performedAt || ''}
                  onChange={e => {
                    const channelFromSource =
                      formData.source === 'MESSE'
                        ? 'MESSE'
                        : formData.source === 'EMPFEHLUNG'
                          ? 'REFERRAL'
                          : formData.source === 'TELEFON'
                            ? 'PHONE'
                            : 'OTHER';

                    setFirstContact({
                      channel: channelFromSource,
                      performedAt: e.target.value,
                      notes: firstContact?.notes || '',
                    });
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
                  label="Notizen zum Erstkontakt"
                  value={firstContact?.notes || ''}
                  onChange={e => {
                    const channelFromSource =
                      formData.source === 'MESSE'
                        ? 'MESSE'
                        : formData.source === 'EMPFEHLUNG'
                          ? 'REFERRAL'
                          : formData.source === 'TELEFON'
                            ? 'PHONE'
                            : 'OTHER';

                    setFirstContact({
                      channel: channelFromSource,
                      performedAt: firstContact?.performedAt || '',
                      notes: e.target.value,
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
        // Stage 1: Vollschutz (schema-driven business fields + custom contact person)
        return (
          <Box>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              {stage1Section?.subtitle}
            </Typography>

            {/* Contact Person Fields (custom - not in schema) */}
            <Box sx={{ mb: 3 }}>
              <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
                Kontaktperson
              </Typography>
              <Typography variant="caption" color="text.secondary" sx={{ mb: 2, display: 'block' }}>
                Mindestens E-Mail oder Telefon erforderlich
              </Typography>

              <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
                <TextField
                  label="Vorname"
                  value={contactData.firstName}
                  onChange={e => setContactData({ ...contactData, firstName: e.target.value })}
                  fullWidth
                  disabled={saving}
                />
                <TextField
                  label="Nachname"
                  value={contactData.lastName}
                  onChange={e => setContactData({ ...contactData, lastName: e.target.value })}
                  fullWidth
                  disabled={saving}
                />
              </Stack>

              <TextField
                label="E-Mail"
                type="email"
                value={contactData.email}
                onChange={e => setContactData({ ...contactData, email: e.target.value })}
                fullWidth
                margin="dense"
                error={!!fieldErrors['contact.email'] || !!fieldErrors['contact']}
                helperText={
                  fieldErrors['contact.email']?.[0] ||
                  (fieldErrors['contact'] && !contactData.phone?.trim()
                    ? fieldErrors['contact'][0]
                    : '')
                }
                disabled={saving}
              />

              <TextField
                label="Telefon"
                type="tel"
                value={contactData.phone}
                onChange={e => setContactData({ ...contactData, phone: e.target.value })}
                fullWidth
                margin="dense"
                error={!!fieldErrors['contact'] || !!fieldErrors['contact.phone']}
                helperText={
                  fieldErrors['contact.phone']?.[0] ||
                  (fieldErrors['contact'] && !contactData.email?.trim()
                    ? fieldErrors['contact'][0]
                    : '')
                }
                disabled={saving}
              />

              {/* DSGVO Hinweis */}
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
              </Box>
            </Box>

            {/* Business Fields from Schema (Vollschutz) */}
            <Typography variant="subtitle2" sx={{ mb: 1, mt: 3, fontWeight: 600 }}>
              Business-Informationen
            </Typography>
            <Grid container spacing={2}>
              {stage1Section?.fields.map(field => renderField(field))}
            </Grid>
          </Box>
        );

      case 2:
        // Stage 2: Nurturing (schema-driven)
        return (
          <Box>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              {stage2Section?.subtitle}
            </Typography>

            <Grid container spacing={2}>
              {stage2Section?.fields.map(field => renderField(field))}
            </Grid>
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
        {progressiveSchema?.title || 'Lead erfassen'}
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
            <Alert severity="warning">Ein Lead mit dieser E-Mail existiert bereits</Alert>
          </Box>
        )}

        {error && error.status !== 409 && (
          <Box mb={2}>
            <Alert severity="error">
              {error.title ?? 'Validierungsfehler'}
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
          Abbrechen
        </Button>
        <Box sx={{ flex: '1 1 auto' }} />

        {activeStep > 0 && (
          <Button onClick={handleBack} disabled={saving}>
            Zurück
          </Button>
        )}

        {/* Save buttons per stage */}
        {activeStep === 0 && (
          <>
            <Button
              variant="contained"
              onClick={() => handleSave(0)}
              disabled={
                saving ||
                !formData.companyName ||
                (typeof formData.companyName === 'string' && !formData.companyName.trim()) ||
                !formData.source
              }
            >
              {saving ? 'Wird gespeichert...' : 'Vormerkung speichern'}
            </Button>
            <Button onClick={handleNext} disabled={saving}>
              Weiter
            </Button>
          </>
        )}

        {activeStep === 1 && (
          <>
            <Button variant="contained" onClick={() => handleSave(1)} disabled={saving}>
              {saving ? 'Wird gespeichert...' : 'Registrierung speichern'}
            </Button>
            <Button onClick={handleNext} disabled={saving}>
              Weiter
            </Button>
          </>
        )}

        {activeStep === 2 && (
          <Button variant="contained" onClick={() => handleSave(2)} disabled={saving}>
            {saving ? 'Wird gespeichert...' : 'Qualifizierung abschließen'}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
}
