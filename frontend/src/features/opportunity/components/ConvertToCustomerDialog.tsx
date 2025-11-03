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
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import BusinessIcon from '@mui/icons-material/Business';
import { useNavigate } from 'react-router-dom';
import { httpClient } from '../../../lib/apiClient';
import { useCurrentUser } from '../../../hooks/useCurrentUser';
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
 * Sprint 2.1.7.7: hierarchyType + parentCustomerId für Multi-Location Management
 */
interface ConvertToCustomerRequest {
  companyName: string;
  xentralCustomerId?: string;
  hierarchyType?: 'STANDALONE' | 'HEADQUARTER' | 'FILIALE';
  parentCustomerId?: string;
  notes?: string;
}

/**
 * Customer DTO for Parent-Selection (Sprint 2.1.7.7 D0)
 */
interface Customer {
  id: string;
  companyName: string;
  city?: string;
  businessType?: string;
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

export default function ConvertToCustomerDialog({
  open,
  opportunity,
  onClose,
  onSuccess,
}: ConvertToCustomerDialogProps) {
  const navigate = useNavigate();
  const { user: currentUser, loading: _userLoading } = useCurrentUser();

  // Form State
  const [companyName, setCompanyName] = useState(
    opportunity.customerName || opportunity.leadCompanyName || opportunity.name || ''
  );
  const [selectedXentralCustomer, setSelectedXentralCustomer] = useState<XentralCustomerDTO | null>(
    null
  );
  const [notes, setNotes] = useState('');
  const [hierarchyType, setHierarchyType] = useState<'STANDALONE' | 'HEADQUARTER' | 'FILIALE'>(
    'STANDALONE'
  );
  const [loading, setLoading] = useState(false);

  // Parent-Selection (Sprint 2.1.7.7 D0)
  const [parentCustomer, setParentCustomer] = useState<Customer | null>(null);
  const [headquarterCustomers, setHeadquarterCustomers] = useState<Customer[]>([]);
  const [loadingHeadquarters, setLoadingHeadquarters] = useState(false);

  // Xentral Customers
  const [xentralCustomers, setXentralCustomers] = useState<XentralCustomerDTO[]>([]);
  const [loadingCustomers, setLoadingCustomers] = useState(false);

  // Error State (statt Toast)
  const [apiError, setApiError] = useState<string | null>(null);

  /**
   * Load Headquarter Customers for Parent-Selection (Sprint 2.1.7.7 D0)
   * Backend: GET /api/customers?hierarchyType=HEADQUARTER
   */
  useEffect(() => {
    if (open) {
      loadHeadquarterCustomers();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  const loadHeadquarterCustomers = async () => {
    setLoadingHeadquarters(true);
    try {
      const response = await httpClient.get<Customer[]>(
        '/api/customers?hierarchyType=HEADQUARTER'
      );
      setHeadquarterCustomers(response.data || []);
    } catch (error) {
      console.error('Failed to load headquarter customers:', error);
      setApiError('Fehler beim Laden der Zentral-Kunden');
      setHeadquarterCustomers([]);
    } finally {
      setLoadingHeadquarters(false);
    }
  };

  /**
   * Load Xentral Customers (verkäufer-gefiltert)
   * Backend: GET /api/xentral/customers?salesRepId={id}
   */
  useEffect(() => {
    if (open && currentUser?.xentralSalesRepId) {
      loadXentralCustomers();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, currentUser]);

  const loadXentralCustomers = async () => {
    if (!currentUser?.xentralSalesRepId) {
      console.warn('No xentralSalesRepId found for current user');
      setXentralCustomers([]);
      return;
    }

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
        hierarchyType: hierarchyType,
        parentCustomerId: hierarchyType === 'FILIALE' ? parentCustomer?.id : undefined,
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
      navigate(`/customer-management/customers/${customer.id}`);
    } catch (error: unknown) {
      console.error('Failed to convert opportunity:', error);
      const errorMessage =
        (error as { response?: { data?: { message?: string } } })?.response?.data?.message ||
        'Fehler beim Anlegen des Customers';
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
      setParentCustomer(null);
      setNotes('');
      setHierarchyType('STANDALONE');
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
            onChange={e => setCompanyName(e.target.value)}
            required
            InputProps={{
              startAdornment: <BusinessIcon sx={{ mr: 1, color: 'action.active' }} />,
            }}
            helperText="Name des Unternehmens (wird als Customer angelegt)"
          />

          {/* hierarchyType Select - Sprint 2.1.7.2 Multi-Location Prep */}
          <FormControl fullWidth>
            <InputLabel>Unternehmenstyp</InputLabel>
            <Select
              value={hierarchyType}
              label="Unternehmenstyp"
              onChange={e =>
                setHierarchyType(e.target.value as 'STANDALONE' | 'HEADQUARTER' | 'FILIALE')
              }
            >
              <MenuItem value="STANDALONE">Einzelbetrieb</MenuItem>
              <MenuItem value="HEADQUARTER">Zentrale/Hauptbetrieb (mit Filialen)</MenuItem>
              <MenuItem value="FILIALE">Filiale (gehört zu Zentrale)</MenuItem>
            </Select>
            <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, ml: 1.75 }}>
              Wählen Sie den Unternehmenstyp basierend auf der Organisationsstruktur.
            </Typography>
          </FormControl>

          {/* Parent-Selection Autocomplete (Sprint 2.1.7.7 D0) */}
          {hierarchyType === 'FILIALE' && (
            <Autocomplete
              options={headquarterCustomers}
              loading={loadingHeadquarters}
              value={parentCustomer}
              onChange={(event, value) => setParentCustomer(value)}
              getOptionLabel={option => option.companyName}
              renderInput={params => (
                <TextField
                  {...params}
                  label="Gehört zu (Hauptbetrieb)"
                  required
                  helperText="Wählen Sie den Hauptbetrieb aus, zu dem diese Filiale gehört"
                  InputProps={{
                    ...params.InputProps,
                    endAdornment: (
                      <>
                        {loadingHeadquarters ? <CircularProgress color="inherit" size={20} /> : null}
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
                      {option.city && `${option.city}`}
                      {option.businessType && ` • ${option.businessType}`}
                    </Typography>
                  </Stack>
                </Box>
              )}
              noOptionsText={
                loadingHeadquarters
                  ? 'Lade Hauptbetriebe...'
                  : 'Keine Hauptbetriebe gefunden'
              }
              isOptionEqualToValue={(option, value) => option.id === value.id}
            />
          )}

          {/* Warning Alert: FILIALE ohne Parent */}
          {hierarchyType === 'FILIALE' && !parentCustomer && (
            <Alert severity="warning">
              <AlertTitle>Hauptbetrieb erforderlich</AlertTitle>
              Bitte wählen Sie den Hauptbetrieb aus, zu dem diese Filiale gehört.
            </Alert>
          )}

          {/* Xentral-Kunden-Dropdown */}
          <Autocomplete
            options={xentralCustomers}
            loading={loadingCustomers}
            value={selectedXentralCustomer}
            onChange={(event, value) => setSelectedXentralCustomer(value)}
            getOptionLabel={option => `${option.companyName} (${option.xentralId})`}
            renderInput={params => (
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
                    {option.totalRevenue != null &&
                      ` • Umsatz: ${option.totalRevenue.toLocaleString('de-DE')} €`}
                  </Typography>
                </Stack>
              </Box>
            )}
            filterOptions={(options, { inputValue }) => {
              // Suche in Company-Name UND Xentral-ID
              const lowerInput = inputValue.toLowerCase();
              return options.filter(
                option =>
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
            onChange={e => setNotes(e.target.value)}
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
          disabled={
            loading ||
            !companyName.trim() ||
            (hierarchyType === 'FILIALE' && !parentCustomer)
          }
          startIcon={loading ? <CircularProgress size={20} /> : <CheckCircleIcon />}
        >
          {loading ? 'Wird angelegt...' : 'Customer anlegen'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
