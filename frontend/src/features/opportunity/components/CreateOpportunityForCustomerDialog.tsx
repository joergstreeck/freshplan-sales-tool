/**
 * CreateOpportunityForCustomerDialog Component
 * Sprint 2.1.7.3 - Customer → Opportunity Conversion (Bestandskunden-Workflow)
 * Sprint 2.1.7.7 - Multi-Location: Branch-Dropdown für HEADQUARTER-Kunden
 *
 * @description MUI Dialog für Opportunity-Erstellung aus aktiven Kunden
 * @features
 * - Business-Type-Matrix für intelligente Wertschätzung
 * - 3-Tier Fallback: actualAnnualVolume > expectedAnnualVolume > 0
 * - Automatische Berechnung: baseVolume × multiplier
 * - Default: SORTIMENTSERWEITERUNG (Bestandskunden-Workflow)
 * - Branch-Dropdown für HEADQUARTER-Kunden (Sprint 2.1.7.7)
 *
 * @since 2025-10-19
 */

import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Alert,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
  InputAdornment,
  Stack,
  Typography,
  Chip,
  CircularProgress,
  Box,
} from '@mui/material';
import { LocalizationProvider, DatePicker } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { de } from 'date-fns/locale';
import { addDays, isAfter, startOfDay } from 'date-fns';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import EuroIcon from '@mui/icons-material/Euro';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import BusinessIcon from '@mui/icons-material/Business';
import CalculateIcon from '@mui/icons-material/Calculate';
import { httpClient } from '../../../lib/apiClient';
import { OpportunityType } from '../types/opportunity.types';
import type { CustomerResponse } from '../../customer/types/customer.types';
import { CustomerHierarchyType } from '../../customer/types/customer.types';
import { useEnumOptions } from '../../../hooks/useEnumOptions';
import { useGetBranches } from '../../customer/api/customerQueries';
import { useMemo } from 'react';
import AccountTreeIcon from '@mui/icons-material/AccountTree';

interface CreateOpportunityForCustomerDialogProps {
  open: boolean;
  customer: CustomerResponse;
  onClose: () => void;
  onSuccess: () => void;
}

/**
 * Request DTO für Customer → Opportunity Conversion
 * Backend: CreateOpportunityForCustomerRequest.java
 */
interface CreateOpportunityForCustomerRequest {
  name?: string;
  description?: string;
  opportunityType?: OpportunityType;
  timeframe?: string;
  expectedValue?: number;
  expectedCloseDate?: string; // ISO-8601 date
}

/**
 * Business-Type-Matrix Multiplier (Backend Response)
 * GET /api/settings/opportunity-multipliers
 */
interface OpportunityMultiplierResponse {
  id: string;
  businessType: string; // RESTAURANT, HOTEL, CATERING, etc.
  opportunityType: OpportunityType;
  multiplier: number; // 0.25, 0.65, 1.00, etc.
}

interface ValidationErrors {
  expectedValue?: string;
  expectedCloseDate?: string;
}

/**
 * OpportunityType Icons (Emojis) - UI-Concern, bleibt im Frontend
 * Labels kommen vom Backend via useEnumOptions (Sprint 2.1.7.7 Schema-Driven Forms)
 * @see OpportunityCard.tsx getOpportunityTypeIcon()
 */
const OPPORTUNITY_TYPE_ICONS: Record<OpportunityType, string> = {
  [OpportunityType.NEUGESCHAEFT]: '🆕',
  [OpportunityType.SORTIMENTSERWEITERUNG]: '📈',
  [OpportunityType.NEUER_STANDORT]: '📍',
  [OpportunityType.VERLAENGERUNG]: '🔁',
};

/**
 * 3-Tier Fallback für Base Volume
 * TIER 1: actualAnnualVolume (Xentral - BESTE Quelle)
 * TIER 2: expectedAnnualVolume (Lead-Schätzung)
 * TIER 3: 0 (Manuelle Eingabe erforderlich)
 */
function getBaseVolume(customer: CustomerResponse): number {
  if (customer.actualAnnualVolume && customer.actualAnnualVolume > 0) {
    return customer.actualAnnualVolume; // Echte Umsatzdaten (Xentral)
  }
  if (customer.expectedAnnualVolume && customer.expectedAnnualVolume > 0) {
    return customer.expectedAnnualVolume; // Lead-Schätzung
  }
  return 0; // Manuelle Eingabe
}

/**
 * Generate default description for Opportunity
 * (Server-Driven: typeLabel kommt vom Backend)
 */
function generateDefaultDescription(customer: CustomerResponse, typeLabel: string): string {
  const baseVolume = getBaseVolume(customer);
  return `${typeLabel}-Opportunity für Bestandskunde ${customer.companyName}.${
    baseVolume > 0 ? ` Aktuelles Jahresvolumen: ${baseVolume.toLocaleString('de-DE')}€` : ''
  }`;
}

export default function CreateOpportunityForCustomerDialog({
  open,
  customer,
  onClose,
  onSuccess,
}: CreateOpportunityForCustomerDialogProps) {
  // Server-Driven Enums (Sprint 2.1.7.7 - Schema-Driven Forms Migration)
  const { data: opportunityTypeOptions } = useEnumOptions('/api/enums/opportunity-types');

  // Sprint 2.1.7.7: Check if customer is HEADQUARTER (can have branches)
  const isHeadquarter = customer.hierarchyType === CustomerHierarchyType.HEADQUARTER;

  // Sprint 2.1.7.7: Fetch branches for HEADQUARTER customers
  const { data: branches, isLoading: branchesLoading } = useGetBranches(
    isHeadquarter ? customer.id : null,
    isHeadquarter && open
  );

  // Create label lookup map for O(1) lookups
  const opportunityTypeLabels = useMemo(() => {
    if (!opportunityTypeOptions) return {};
    return opportunityTypeOptions.reduce(
      (acc, item) => {
        acc[item.value] = item.label;
        return acc;
      },
      {} as Record<string, string>
    );
  }, [opportunityTypeOptions]);

  // Form State
  const [name, setName] = useState(customer.companyName); // Pre-filled (OHNE Type-Präfix!)
  const [opportunityType, setOpportunityType] = useState<OpportunityType>(
    OpportunityType.SORTIMENTSERWEITERUNG // Default für Bestandskunden!
  );
  const [expectedValue, setExpectedValue] = useState<number | undefined>(undefined);
  const [expectedCloseDate, setExpectedCloseDate] = useState<Date | null>(addDays(new Date(), 60)); // +60 Tage (Bestandskunden-Deals)
  const [description, setDescription] = useState('');

  // Sprint 2.1.7.7: Selected branch for HEADQUARTER customers
  // "" = Zentrale (Hauptbetrieb), UUID = Specific branch
  const [selectedBranchId, setSelectedBranchId] = useState<string>('');

  // Business-Type-Matrix State
  const [multipliers, setMultipliers] = useState<OpportunityMultiplierResponse[]>([]);
  const [isLoadingMultipliers, setIsLoadingMultipliers] = useState(false);
  const [multipliersError, setMultipliersError] = useState<string | null>(null);

  // Load Business-Type-Matrix Multipliers on Dialog Open
  useEffect(() => {
    if (!open) return;

    const loadMultipliers = async () => {
      setIsLoadingMultipliers(true);
      setMultipliersError(null);
      try {
        const response = await httpClient.get<OpportunityMultiplierResponse[]>(
          '/api/settings/opportunity-multipliers'
        );
        setMultipliers(response.data);
      } catch (error) {
        console.error('Failed to load opportunity multipliers:', error);
        setMultipliersError('Multipliers konnten nicht geladen werden');
        setMultipliers([]); // Fallback: Empty array
      } finally {
        setIsLoadingMultipliers(false);
      }
    };

    loadMultipliers();
  }, [open]);

  // UI State
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});
  const [manualValueOverride, setManualValueOverride] = useState(false);

  // Auto-Calculate Expected Value (baseVolume × multiplier)
  useEffect(() => {
    // Skip if user has manually overridden the value
    if (manualValueOverride) return;

    // Skip if multipliers haven't loaded yet
    if (multipliers.length === 0) return;

    const baseVolume = getBaseVolume(customer);
    if (baseVolume === 0) return; // No volume available

    // Find multiplier for current opportunityType and customer.industry
    let multiplierValue = 1.0; // Default fallback: 1.0 (wenn keine Industry vorhanden)

    if (customer.industry) {
      const multiplier = multipliers.find(
        m => m.businessType === customer.industry && m.opportunityType === opportunityType
      );

      if (multiplier) {
        multiplierValue = multiplier.multiplier;
      }
    }

    // Calculate with found multiplier or fallback (1.0)
    const calculatedValue = Math.round(baseVolume * multiplierValue);
    setExpectedValue(calculatedValue);
  }, [multipliers, opportunityType, customer, manualValueOverride]);

  /**
   * Get current multiplier for display
   */
  const getCurrentMultiplier = (): number | null => {
    if (!customer.industry) return null;

    const multiplier = multipliers.find(
      m => m.businessType === customer.industry && m.opportunityType === opportunityType
    );
    return multiplier?.multiplier || null;
  };

  /**
   * Client-Side Validation
   */
  const validate = (): boolean => {
    const errors: ValidationErrors = {};

    if (!expectedValue || expectedValue <= 0) {
      errors.expectedValue = 'Wert muss größer als 0 sein';
    }

    if (!expectedCloseDate) {
      errors.expectedCloseDate = 'Abschlussdatum ist erforderlich';
    } else if (!isAfter(startOfDay(expectedCloseDate), startOfDay(new Date()))) {
      errors.expectedCloseDate = 'Datum muss in der Zukunft liegen';
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  /**
   * Submit Handler
   * API: POST /api/opportunities/for-customer/{customerId}
   */
  const handleSubmit = async () => {
    if (!validate()) {
      return;
    }

    setIsSubmitting(true);
    setApiError(null);

    try {
      const request: CreateOpportunityForCustomerRequest = {
        name: name.trim() || undefined,
        opportunityType,
        expectedValue,
        expectedCloseDate: expectedCloseDate?.toISOString().split('T')[0], // YYYY-MM-DD
        description: description.trim() || undefined,
      };

      // Sprint 2.1.7.7: Use selected branch ID or customer ID (Zentrale)
      const targetCustomerId = selectedBranchId || customer.id;
      await httpClient.post(`/api/opportunities/for-customer/${targetCustomerId}`, request);

      // Success!
      onSuccess();
      handleClose();
    } catch (err: unknown) {
      console.error('Failed to create opportunity for customer:', err);

      const error = err as {
        response?: { status?: number; data?: { detail?: string; title?: string } };
      };
      const status = error.response?.status;
      let message: string;

      switch (status) {
        case 404:
          message = 'Kunde nicht gefunden. Möglicherweise wurde er gelöscht.';
          break;
        case 400:
          message = 'Kunde muss AKTIV sein, um Opportunity zu erstellen.';
          break;
        case 403:
          message = 'Keine Berechtigung. Sie können für diesen Kunden keine Opportunity erstellen.';
          break;
        default:
          message =
            error.response?.data?.detail ||
            error.response?.data?.title ||
            'Fehler beim Erstellen der Opportunity';
      }

      setApiError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  /**
   * Close Handler - Reset form state
   */
  const handleClose = () => {
    if (!isSubmitting) {
      setName(customer.companyName);
      setOpportunityType(OpportunityType.SORTIMENTSERWEITERUNG);
      setExpectedValue(undefined);
      setExpectedCloseDate(addDays(new Date(), 60));
      const label =
        opportunityTypeLabels[OpportunityType.SORTIMENTSERWEITERUNG] || 'Sortimentserweiterung';
      setDescription(generateDefaultDescription(customer, label));
      setApiError(null);
      setValidationErrors({});
      setMultipliers([]);
      setMultipliersError(null);
      setSelectedBranchId(''); // Sprint 2.1.7.7: Reset branch selection
      onClose();
    }
  };

  const baseVolume = getBaseVolume(customer);
  const currentMultiplier = getCurrentMultiplier();

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={de}>
      <Dialog open={open} onClose={handleClose} fullWidth maxWidth="md">
        {/* Dialog Title */}
        <DialogTitle>
          <Stack direction="row" alignItems="center" spacing={1}>
            <TrendingUpIcon color="primary" />
            <Typography variant="h6">Neue Opportunity erstellen</Typography>
          </Stack>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            Bestandskunde: <strong>{customer.companyName}</strong>
          </Typography>
        </DialogTitle>

        {/* Dialog Content */}
        <DialogContent>
          {/* API Error Alert */}
          {apiError && (
            <Alert severity="error" sx={{ mb: 2 }} onClose={() => setApiError(null)}>
              {apiError}
            </Alert>
          )}

          {/* Multipliers Loading Error */}
          {multipliersError && (
            <Alert severity="warning" sx={{ mb: 2 }}>
              {multipliersError}
            </Alert>
          )}

          <Stack spacing={2} sx={{ mt: 1 }}>
            {/* Business-Type-Matrix Info Box */}
            {isLoadingMultipliers ? (
              <Box
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1,
                  p: 2,
                  bgcolor: 'grey.50',
                  borderRadius: 1,
                }}
              >
                <CircularProgress size={20} />
                <Typography variant="body2">Lade Multiplier-Matrix...</Typography>
              </Box>
            ) : baseVolume > 0 && currentMultiplier !== null ? (
              <Alert severity="info" icon={<CalculateIcon />}>
                <Typography variant="body2" fontWeight="bold" gutterBottom>
                  Intelligente Wertschätzung:
                </Typography>
                <Typography
                  variant="caption"
                  component="div"
                  sx={{ fontFamily: '"Courier New", Courier, monospace' }}
                >
                  Basisvolumen: {baseVolume.toLocaleString('de-DE')}€ (
                  {customer.actualAnnualVolume ? 'Xentral' : 'Lead-Schätzung'})
                  <br />
                  Multiplier: {currentMultiplier} ({customer.industry || 'UNKNOWN'} ×{' '}
                  {opportunityTypeLabels[opportunityType] || opportunityType})
                  <br />
                  <strong>Erwarteter Wert: {expectedValue?.toLocaleString('de-DE')}€</strong>
                </Typography>
              </Alert>
            ) : (
              <Alert severity="warning">Kein Basisvolumen verfügbar. Bitte manuell schätzen.</Alert>
            )}

            {/* Name Field */}
            <TextField
              label="Name"
              value={name}
              onChange={e => setName(e.target.value)}
              fullWidth
              disabled={isSubmitting}
              placeholder={`${customer.companyName}`}
              helperText="Optional - wird aus Kundendaten generiert"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <BusinessIcon />
                  </InputAdornment>
                ),
              }}
            />

            {/* Sprint 2.1.7.7: Branch Dropdown für HEADQUARTER-Kunden */}
            {isHeadquarter && (
              <FormControl fullWidth disabled={isSubmitting || branchesLoading}>
                <InputLabel>Standort</InputLabel>
                <Select
                  value={selectedBranchId}
                  onChange={e => setSelectedBranchId(e.target.value)}
                  label="Standort"
                  startAdornment={
                    <InputAdornment position="start">
                      <AccountTreeIcon />
                    </InputAdornment>
                  }
                >
                  <MenuItem value="">
                    <Stack direction="row" spacing={1} alignItems="center">
                      <BusinessIcon fontSize="small" color="primary" />
                      <Typography fontWeight="bold">{customer.companyName} (Zentrale)</Typography>
                    </Stack>
                  </MenuItem>
                  {branchesLoading && (
                    <MenuItem disabled>
                      <CircularProgress size={16} sx={{ mr: 1 }} />
                      <Typography>Lade Filialen...</Typography>
                    </MenuItem>
                  )}
                  {branches?.map(branch => (
                    <MenuItem key={branch.id} value={branch.id}>
                      <Stack>
                        <Typography>{branch.companyName}</Typography>
                        {branch.city && (
                          <Typography variant="caption" color="text.secondary">
                            {branch.city}
                          </Typography>
                        )}
                      </Stack>
                    </MenuItem>
                  ))}
                  {!branchesLoading && branches?.length === 0 && (
                    <MenuItem disabled>
                      <Typography variant="body2" color="text.secondary">
                        Keine Filialen vorhanden
                      </Typography>
                    </MenuItem>
                  )}
                </Select>
                <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, ml: 1.5 }}>
                  Wählen Sie den Standort für diese Opportunity
                </Typography>
              </FormControl>
            )}

            {/* Opportunity Type Select (Sprint 2.1.7.7 - Server-Driven Enums) */}
            <FormControl fullWidth disabled={isSubmitting}>
              <InputLabel>Opportunity-Typ</InputLabel>
              <Select
                value={opportunityType}
                onChange={e => setOpportunityType(e.target.value as OpportunityType)}
                label="Opportunity-Typ"
              >
                {opportunityTypeOptions?.map(option => (
                  <MenuItem key={option.value} value={option.value}>
                    <Stack direction="row" spacing={1} alignItems="center">
                      <Typography>
                        {OPPORTUNITY_TYPE_ICONS[option.value as OpportunityType]}
                      </Typography>
                      <Typography>{option.label}</Typography>
                    </Stack>
                  </MenuItem>
                ))}
              </Select>
              <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, ml: 1.5 }}>
                Bestandskunden: Standard ist "Sortimentserweiterung"
              </Typography>
            </FormControl>

            {/* Expected Value (Auto-calculated or Manual) */}
            <TextField
              label="Erwarteter Wert"
              type="number"
              value={expectedValue || ''}
              onChange={e => {
                setExpectedValue(parseFloat(e.target.value) || undefined);
                setManualValueOverride(true); // User manually changed value
              }}
              fullWidth
              required
              disabled={isSubmitting}
              error={!!validationErrors.expectedValue}
              helperText={
                validationErrors.expectedValue ||
                (baseVolume > 0 && currentMultiplier
                  ? 'Auto-berechnet via Business-Type-Matrix (anpassbar)'
                  : 'Geschätzter Deal-Wert in EUR')
              }
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <EuroIcon />
                  </InputAdornment>
                ),
                inputProps: { min: 0, step: 100 },
              }}
            />

            {/* Expected Close Date */}
            <DatePicker
              label="Erwartetes Abschlussdatum"
              value={expectedCloseDate}
              onChange={newValue => setExpectedCloseDate(newValue)}
              disabled={isSubmitting}
              minDate={addDays(new Date(), 1)}
              slotProps={{
                textField: {
                  fullWidth: true,
                  required: true,
                  error: !!validationErrors.expectedCloseDate,
                  helperText:
                    validationErrors.expectedCloseDate || 'Bestandskunden-Deals: Default +60 Tage',
                  InputProps: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <CalendarTodayIcon />
                      </InputAdornment>
                    ),
                  },
                },
              }}
            />

            {/* Description */}
            <TextField
              label="Beschreibung"
              value={description}
              onChange={e => setDescription(e.target.value)}
              fullWidth
              multiline
              rows={3}
              disabled={isSubmitting}
              placeholder="Interne Notizen, Hintergrund, nächste Schritte..."
              helperText="Optional - Details zur Verkaufschance"
            />

            {/* Info Box: What happens after submit */}
            <Alert severity="info" icon={<TrendingUpIcon />}>
              <Typography variant="body2" fontWeight="bold" gutterBottom>
                Nach dem Erstellen:
              </Typography>
              <ul style={{ margin: '4px 0', paddingLeft: '20px' }}>
                <li>
                  Opportunity erscheint in der Pipeline (Stage: <strong>NEEDS_ANALYSIS</strong>)
                </li>
                <li>
                  Kunde bleibt{' '}
                  <Chip label="AKTIV" size="small" sx={{ fontSize: '0.7rem', height: '18px' }} />
                </li>
                <li>Sie werden zur Opportunity-Pipeline weitergeleitet</li>
              </ul>
            </Alert>
          </Stack>
        </DialogContent>

        {/* Dialog Actions */}
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={handleClose} disabled={isSubmitting}>
            Abbrechen
          </Button>
          <Button
            onClick={handleSubmit}
            variant="contained"
            disabled={isSubmitting || isLoadingMultipliers}
            startIcon={<TrendingUpIcon />}
          >
            {isSubmitting ? 'Erstelle...' : 'Opportunity erstellen'}
          </Button>
        </DialogActions>
      </Dialog>
    </LocalizationProvider>
  );
}
