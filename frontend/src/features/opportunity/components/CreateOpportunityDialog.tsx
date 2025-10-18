/**
 * CreateOpportunityDialog Component
 * Sprint 2.1.7.1 - Lead → Opportunity Conversion
 *
 * @description MUI Dialog für Opportunity-Erstellung aus Leads
 * @since 2025-10-16
 */

import { useState } from 'react';
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
} from '@mui/material';
import { LocalizationProvider, DatePicker } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { de } from 'date-fns/locale';
import { addDays, isAfter, startOfDay } from 'date-fns';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import EuroIcon from '@mui/icons-material/Euro';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import BusinessIcon from '@mui/icons-material/Business';
import { httpClient } from '../../../lib/apiClient';
import { OpportunityType } from '../types/opportunity.types';
import type { Lead } from '../../leads/types';

interface CreateOpportunityDialogProps {
  open: boolean;
  lead: Lead;
  onClose: () => void;
  onSuccess: () => void;
}

/**
 * Request DTO für Lead → Opportunity Conversion
 * Backend: CreateOpportunityFromLeadRequest.java
 */
interface CreateOpportunityFromLeadRequest {
  name?: string;
  description?: string;
  opportunityType?: OpportunityType;
  dealType?: string;
  timeframe?: string;
  expectedValue?: number;
  expectedCloseDate?: string; // ISO-8601 date
  assignedTo?: string; // UUID
}

interface ValidationErrors {
  expectedValue?: string;
  expectedCloseDate?: string;
}

/**
 * OpportunityType Labels (German)
 * @see OpportunityCard.tsx getOpportunityTypeLabel()
 */
const OPPORTUNITY_TYPE_LABELS: Record<OpportunityType, string> = {
  [OpportunityType.NEUGESCHAEFT]: 'Neugeschäft',
  [OpportunityType.SORTIMENTSERWEITERUNG]: 'Sortimentserweiterung',
  [OpportunityType.NEUER_STANDORT]: 'Neuer Standort',
  [OpportunityType.VERLAENGERUNG]: 'Vertragsverlängerung',
};

/**
 * OpportunityType Icons (Emojis)
 * @see OpportunityCard.tsx getOpportunityTypeIcon()
 */
const OPPORTUNITY_TYPE_ICONS: Record<OpportunityType, string> = {
  [OpportunityType.NEUGESCHAEFT]: '🆕',
  [OpportunityType.SORTIMENTSERWEITERUNG]: '📈',
  [OpportunityType.NEUER_STANDORT]: '📍',
  [OpportunityType.VERLAENGERUNG]: '🔁',
};

/**
 * Generate default description for Opportunity
 * (Copilot Code Review - DRY principle)
 */
function generateDefaultDescription(lead: Lead): string {
  return `Neugeschäft für ${lead.companyName}.${lead.leadScore ? ` Lead-Score: ${lead.leadScore}/100` : ''}`;
}

export default function CreateOpportunityDialog({
  open,
  lead,
  onClose,
  onSuccess,
}: CreateOpportunityDialogProps) {
  // Form State
  const [name, setName] = useState(lead.companyName); // Pre-filled (OHNE Type-Präfix!)
  const [opportunityType, setOpportunityType] = useState<OpportunityType>(
    OpportunityType.NEUGESCHAEFT
  ); // Default für Lead-Conversion
  const [expectedValue, setExpectedValue] = useState<number | undefined>(
    lead.estimatedVolume || undefined
  );
  const [expectedCloseDate, setExpectedCloseDate] = useState<Date | null>(addDays(new Date(), 30)); // +30 Tage
  const [description, setDescription] = useState(generateDefaultDescription(lead));

  // UI State
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);
  const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});

  /**
   * Client-Side Validation
   * - Expected Value > 0 (required)
   * - Close Date > Today (must be in future)
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
   * API: POST /api/opportunities/from-lead/{leadId}
   */
  const handleSubmit = async () => {
    if (!validate()) {
      return;
    }

    setIsSubmitting(true);
    setApiError(null);

    try {
      const request: CreateOpportunityFromLeadRequest = {
        name: name.trim() || undefined, // Backend auto-generates if empty
        opportunityType,
        expectedValue,
        expectedCloseDate: expectedCloseDate?.toISOString().split('T')[0], // YYYY-MM-DD
        description: description.trim() || undefined,
      };

      await httpClient.post(`/api/opportunities/from-lead/${lead.id}`, request);

      // Success!
      onSuccess();
      handleClose();
    } catch (err: unknown) {
      console.error('Failed to create opportunity from lead:', err);

      // Handle specific HTTP status codes (Copilot Code Review)
      const error = err as { response?: { status?: number; data?: { detail?: string; title?: string } } };
      const status = error.response?.status;
      let message: string;

      switch (status) {
        case 404:
          message = 'Lead nicht gefunden. Möglicherweise wurde er gelöscht.';
          break;
        case 409:
          message = 'Dieser Lead wurde bereits in eine Opportunity konvertiert.';
          break;
        case 403:
          message = 'Keine Berechtigung. Sie können diesen Lead nicht konvertieren.';
          break;
        default:
          // Extract error message from backend
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
   * Close Handler
   * Reset form state when dialog closes
   */
  const handleClose = () => {
    if (!isSubmitting) {
      // Reset form
      setName(lead.companyName);
      setOpportunityType(OpportunityType.NEUGESCHAEFT);
      setExpectedValue(lead.estimatedVolume || undefined);
      setExpectedCloseDate(addDays(new Date(), 30));
      setDescription(generateDefaultDescription(lead));
      setApiError(null);
      setValidationErrors({});
      onClose();
    }
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={de}>
      <Dialog open={open} onClose={handleClose} fullWidth maxWidth="md">
        {/* Dialog Title */}
        <DialogTitle>
          <Stack direction="row" alignItems="center" spacing={1}>
            <TrendingUpIcon color="primary" />
            <Typography variant="h6">Opportunity erstellen</Typography>
          </Stack>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            Lead: <strong>{lead.companyName}</strong>
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

          <Stack spacing={2} sx={{ mt: 1 }}>
            {/* Name Field (Optional - Auto-Generated from Lead) */}
            <TextField
              label="Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              fullWidth
              disabled={isSubmitting}
              placeholder={`Wird automatisch generiert (${lead.companyName})`}
              helperText="Optional - leer lassen für automatische Generierung aus Lead-Daten"
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <BusinessIcon />
                  </InputAdornment>
                ),
              }}
            />

            {/* Opportunity Type Select (Sprint 2.1.7.1 - Freshfoodz Business Types) */}
            <FormControl fullWidth disabled={isSubmitting}>
              <InputLabel>Opportunity-Typ</InputLabel>
              <Select
                value={opportunityType}
                onChange={(e) => setOpportunityType(e.target.value as OpportunityType)}
                label="Opportunity-Typ"
              >
                {Object.values(OpportunityType).map((type) => (
                  <MenuItem key={type} value={type}>
                    <Stack direction="row" spacing={1} alignItems="center">
                      <Typography>{OPPORTUNITY_TYPE_ICONS[type]}</Typography>
                      <Typography>{OPPORTUNITY_TYPE_LABELS[type]}</Typography>
                    </Stack>
                  </MenuItem>
                ))}
              </Select>
              <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, ml: 1.5 }}>
                Lead-Conversion: Standard ist "Neugeschäft"
              </Typography>
            </FormControl>

            {/* Expected Value (Required, pre-filled from lead.estimatedVolume) */}
            <TextField
              label="Erwarteter Wert"
              type="number"
              value={expectedValue || ''}
              onChange={(e) => setExpectedValue(parseFloat(e.target.value) || undefined)}
              fullWidth
              required
              disabled={isSubmitting}
              error={!!validationErrors.expectedValue}
              helperText={validationErrors.expectedValue || 'Geschätzter Deal-Wert in EUR'}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <EuroIcon />
                  </InputAdornment>
                ),
                inputProps: { min: 0, step: 100 },
              }}
            />

            {/* Expected Close Date (Required, default +30 days) */}
            <DatePicker
              label="Erwartetes Abschlussdatum"
              value={expectedCloseDate}
              onChange={(newValue) => setExpectedCloseDate(newValue)}
              disabled={isSubmitting}
              minDate={addDays(new Date(), 1)} // Minimum: tomorrow
              slotProps={{
                textField: {
                  fullWidth: true,
                  required: true,
                  error: !!validationErrors.expectedCloseDate,
                  helperText:
                    validationErrors.expectedCloseDate || 'Wann soll der Deal abgeschlossen werden?',
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

            {/* Description (Optional) */}
            <TextField
              label="Beschreibung"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
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
                <li>Lead-Status wird auf <Chip label="CONVERTED" size="small" sx={{ fontSize: '0.7rem', height: '18px' }} /> gesetzt</li>
                <li>Opportunity erscheint in der Pipeline (Stage: <strong>NEW_LEAD</strong>)</li>
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
            disabled={isSubmitting}
            startIcon={<TrendingUpIcon />}
          >
            {isSubmitting ? 'Erstelle...' : 'Opportunity erstellen'}
          </Button>
        </DialogActions>
      </Dialog>
    </LocalizationProvider>
  );
}
