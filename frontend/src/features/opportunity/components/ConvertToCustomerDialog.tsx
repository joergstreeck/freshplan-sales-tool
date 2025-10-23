/**
 * ConvertToCustomerDialog Component
 * Sprint 2.1.7.2 - Customer Management + Xentral Integration
 *
 * @description MUI Dialog für Opportunity → Customer Konvertierung mit Xentral-Integration
 * @since 2025-10-23
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
  AlertTitle,
  Autocomplete,
  Typography,
  Box,
  Stack,
  CircularProgress,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import BusinessIcon from '@mui/icons-material/Business';
import { useNavigate } from 'react-router-dom';
import { httpClient } from '../../../lib/apiClient';
import type { IOpportunity } from '../types/opportunity.types';

interface ConvertToCustomerDialogProps {
  open: boolean;
  opportunity: IOpportunity;
  onClose: () => void;
  onSuccess?: () => void;
}

/**
 * Xentral Customer DTO (simplified from backend)
 * Backend: XentralCustomerDTO.java
 */
interface XentralCustomerDTO {
  xentralId: string;
  companyName: string;
  totalRevenue?: number;
}

/**
 * Request DTO für Opportunity → Customer Konvertierung
 * Backend: ConvertToCustomerRequest.java (Sprint 2.1.7.1)
 */
interface ConvertToCustomerRequest {
  companyName: string;
  xentralCustomerId?: string;
  notes?: string;
}

/**
 * Response DTO
 * Backend: CustomerResponse.java
 */
interface CustomerResponse {
  id: string;
  companyName: string;
  status: string;
}

/**
 * Current user context (from auth)
 */
interface CurrentUser {
  id: string;
  name: string;
  xentralSalesRepId?: string;
}

// TODO: Replace with real auth hook
const useCurrentUser = (): CurrentUser => {
  return {
    id: '00000000-0000-0000-0000-000000000001',
    name: 'Test User',
    xentralSalesRepId: 'SR-001',
  };
};

export default function ConvertToCustomerDialog({
  open,
  opportunity,
  onClose,
  onSuccess,
}: ConvertToCustomerDialogProps) {
  const navigate = useNavigate();
  const currentUser = useCurrentUser();

  // Form State
  const [companyName, setCompanyName] = useState(
    opportunity.customerName || opportunity.leadCompanyName || opportunity.name || ''
  );
  const [selectedXentralCustomer, setSelectedXentralCustomer] =
    useState<XentralCustomerDTO | null>(null);
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(false);

  // Xentral Customers
  const [xentralCustomers, setXentralCustomers] = useState<XentralCustomerDTO[]>([]);
  const [loadingCustomers, setLoadingCustomers] = useState(false);

  // Error State (statt Toast)
  const [apiError, setApiError] = useState<string | null>(null);

  /**
   * Load Xentral Customers (verkäufer-gefiltert)
   * Backend: GET /api/xentral/customers?salesRepId={id}
   */
  useEffect(() => {
    if (open && currentUser.xentralSalesRepId) {
      loadXentralCustomers();
    }
  }, [open, currentUser.xentralSalesRepId]);

  const loadXentralCustomers = async () => {
    setLoadingCustomers(true);
    try {
      const response = await httpClient.get<XentralCustomerDTO[]>(
        `/api/xentral/customers?salesRepId=${currentUser.xentralSalesRepId}`
      );
      setXentralCustomers(response.data || []);
    } catch (error) {
      console.error('Failed to load Xentral customers:', error);
      setApiError('Fehler beim Laden der Xentral-Kunden');
      setXentralCustomers([]);
    } finally {
      setLoadingCustomers(false);
    }
  };

  /**
   * Handle Convert Button Click
   * Backend: POST /api/opportunities/{id}/convert-to-customer
   */
  const handleConvert = async () => {
    if (!companyName.trim()) {
      setApiError('Bitte Firmenname eingeben');
      return;
    }

    setLoading(true);
    setApiError(null);

    try {
      const request: ConvertToCustomerRequest = {
        companyName: companyName.trim(),
        xentralCustomerId: selectedXentralCustomer?.xentralId,
        notes: notes.trim() || undefined,
      };

      const response = await httpClient.post<CustomerResponse>(
        `/api/opportunities/${opportunity.id}/convert-to-customer`,
        request
      );

      const customer = response.data;

      // Success! Call callbacks and navigate
      if (onSuccess) {
        onSuccess();
      }

      onClose();
      navigate(`/customers/${customer.id}`);
    } catch (error: any) {
      console.error('Failed to convert opportunity:', error);
      const errorMessage =
        error?.response?.data?.message || 'Fehler beim Anlegen des Customers';
      setApiError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Reset form when dialog closes
   */
  useEffect(() => {
    if (!open) {
      setCompanyName(
        opportunity.customerName || opportunity.leadCompanyName || opportunity.name || ''
      );
      setSelectedXentralCustomer(null);
      setNotes('');
      setApiError(null);
    }
  }, [open, opportunity]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <CheckCircleIcon color="success" />
          <Typography variant="h6" component="span">
            Opportunity zu Customer konvertieren
          </Typography>
        </Box>
      </DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {/* API Error Alert */}
          {apiError && (
            <Alert severity="error" onClose={() => setApiError(null)}>
              {apiError}
            </Alert>
          )}

          {/* Company Name */}
          <TextField
            fullWidth
            label="Firmenname"
            value={companyName}
            onChange={(e) => setCompanyName(e.target.value)}
            required
            InputProps={{
              startAdornment: <BusinessIcon sx={{ mr: 1, color: 'action.active' }} />,
            }}
            helperText="Name des Unternehmens (wird als Customer angelegt)"
          />

          {/* Xentral-Kunden-Dropdown */}
          <Autocomplete
            options={xentralCustomers}
            loading={loadingCustomers}
            value={selectedXentralCustomer}
            onChange={(event, value) => setSelectedXentralCustomer(value)}
            getOptionLabel={(option) => `${option.companyName} (${option.xentralId})`}
            renderInput={(params) => (
              <TextField
                {...params}
                label="Xentral-Kunde verknüpfen (optional)"
                helperText="Wähle einen bestehenden Xentral-Kunden oder lasse leer für manuelles Verknüpfen später"
                InputProps={{
                  ...params.InputProps,
                  endAdornment: (
                    <>
                      {loadingCustomers ? <CircularProgress color="inherit" size={20} /> : null}
                      {params.InputProps.endAdornment}
                    </>
                  ),
                }}
              />
            )}
            renderOption={(props, option) => (
              <Box component="li" {...props}>
                <Stack>
                  <Typography variant="body1">{option.companyName}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    Xentral-ID: {option.xentralId}
                    {option.totalRevenue != null && ` • Umsatz: ${option.totalRevenue.toLocaleString('de-DE')} €`}
                  </Typography>
                </Stack>
              </Box>
            )}
            filterOptions={(options, { inputValue }) => {
              // Suche in Company-Name UND Xentral-ID
              const lowerInput = inputValue.toLowerCase();
              return options.filter(
                (option) =>
                  option.companyName.toLowerCase().includes(lowerInput) ||
                  option.xentralId.toLowerCase().includes(lowerInput)
              );
            }}
            noOptionsText={
              loadingCustomers ? 'Lade Xentral-Kunden...' : 'Keine Xentral-Kunden gefunden'
            }
            isOptionEqualToValue={(option, value) => option.xentralId === value.xentralId}
          />

          {/* Info-Box: Xentral-Kunde gewählt */}
          {selectedXentralCustomer && (
            <Alert severity="info">
              <AlertTitle>Xentral-Verknüpfung</AlertTitle>
              Nach Anlage werden Umsatzdaten von Xentral-Kunde "
              {selectedXentralCustomer.companyName}" angezeigt.
            </Alert>
          )}

          {/* Info-Box: KEIN Xentral-Kunde gewählt */}
          {!selectedXentralCustomer && (
            <Alert severity="warning">
              <AlertTitle>Keine Xentral-Verknüpfung</AlertTitle>
              Ohne Xentral-Verknüpfung können keine Umsatzdaten angezeigt werden. Du kannst die
              Verknüpfung später im Customer-Detail-Bereich vornehmen.
            </Alert>
          )}

          {/* Info-Box: PROSPECT Status (Sprint 2.1.7.4 Integration) */}
          <Alert severity="info" icon={<HourglassEmptyIcon />}>
            <AlertTitle>Customer Status: PROSPECT</AlertTitle>
            <Typography variant="body2">
              Der neue Customer wird mit Status <strong>PROSPECT</strong> angelegt.
              <br />
              Status wechselt automatisch zu <strong>AKTIV</strong> bei erster Xentral-Bestellung.
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
              ℹ️ Siehe Sprint 2.1.7.4: Customer Status Architecture
            </Typography>
          </Alert>

          {/* Notizen */}
          <TextField
            fullWidth
            label="Notizen (optional)"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            multiline
            rows={3}
            placeholder="Zusätzliche Informationen zur Konvertierung..."
          />
        </Stack>
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={loading}>
          Abbrechen
        </Button>
        <Button
          variant="contained"
          color="primary"
          onClick={handleConvert}
          disabled={loading || !companyName.trim()}
          startIcon={loading ? <CircularProgress size={20} /> : <CheckCircleIcon />}
        >
          {loading ? 'Wird angelegt...' : 'Customer anlegen'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
