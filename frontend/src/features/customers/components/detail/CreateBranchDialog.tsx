/**
 * Create Branch Dialog Component
 *
 * Sprint 2.1.7.7 D4: CreateBranchDialog - Multi-Location Management
 *
 * Dialog zum Anlegen neuer Filialen (FILIALE) unter einem HEADQUARTER-Kunden.
 *
 * Features:
 * - Formular mit companyName (Pflichtfeld), status, customerType
 * - Validierung: companyName erforderlich
 * - POST /api/customers/{headquarterId}/branches
 * - Automatische Verknüpfung mit Headquarter
 * - Automatische Vererbung der xentralCustomerId
 * - Design System konform (MUI Theme, keine hardcoded colors)
 * - Vollständig auf Deutsch
 *
 * Backend Endpoint:
 * - POST /api/customers/{headquarterId}/branches
 * - Body: { companyName, status?, customerType? }
 * - Returns: CustomerResponse mit hierarchyType=FILIALE
 *
 * @author FreshPlan Team
 * @since 2.1.7.7
 */

import React, { useState, useEffect } from 'react';
import {
  Box,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  MenuItem,
  Grid,
  Alert,
  useTheme,
  useMediaQuery,
  Typography,
  CircularProgress,
} from '@mui/material';
import { useCreateBranch } from '../../../customer/api/customerQueries';
import { useEnumOptions } from '../../../../hooks/useEnumOptions';

interface CreateBranchDialogProps {
  /** Dialog open state */
  open: boolean;
  /** Close dialog callback */
  onClose: () => void;
  /** UUID of the parent HEADQUARTER customer */
  headquarterId: string;
  /** Optional success callback */
  onSuccess?: () => void;
}

/**
 * CreateBranchDialog - Dialog für Filialanlage unter einem Headquarter
 *
 * Validierung:
 * - companyName: Pflichtfeld, min. 2 Zeichen
 * - status: Optional (default: PROSPECT)
 * - customerType: Optional (default: UNTERNEHMEN)
 *
 * Hierarchie:
 * - hierarchyType wird automatisch auf FILIALE gesetzt (Backend)
 * - parentCustomerId wird automatisch gesetzt (Backend)
 * - xentralCustomerId wird vom Parent vererbt (Backend)
 */
export const CreateBranchDialog: React.FC<CreateBranchDialogProps> = ({
  open,
  onClose,
  headquarterId,
  onSuccess,
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  // ========== ENUM OPTIONS (SERVER-DRIVEN) ==========
  const { data: customerTypeOptions, isLoading: customerTypeLoading } = useEnumOptions(
    '/api/enums/customer-types'
  );
  const { data: statusOptions, isLoading: statusLoading } = useEnumOptions(
    '/api/enums/customer-status'
  );

  // ========== FORM STATE ==========
  const [formData, setFormData] = useState({
    companyName: '',
    status: 'PROSPECT',
    customerType: 'UNTERNEHMEN',
  });

  const [errors, setErrors] = useState<Record<string, string>>({});

  // ========== REACT QUERY MUTATION ==========
  const createBranchMutation = useCreateBranch();

  // ========== LOADING STATE ==========
  const isLoadingOptions = customerTypeLoading || statusLoading;

  // ========== EFFECTS ==========

  /**
   * Reset form when dialog opens/closes
   */
  useEffect(() => {
    if (open) {
      setFormData({
        companyName: '',
        status: 'PROSPECT',
        customerType: 'UNTERNEHMEN',
      });
      setErrors({});
    }
  }, [open]);

  // ========== VALIDATION ==========

  /**
   * Validate form fields
   *
   * Rules:
   * - companyName: required, min 2 chars
   */
  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Company name validation
    if (!formData.companyName?.trim()) {
      newErrors.companyName = 'Firmenname ist erforderlich';
    } else if (formData.companyName.trim().length < 2) {
      newErrors.companyName = 'Firmenname muss mindestens 2 Zeichen lang sein';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // ========== EVENT HANDLERS ==========

  /**
   * Handle form submit
   */
  const handleSubmit = async () => {
    if (!validateForm()) {
      return;
    }

    try {
      await createBranchMutation.mutateAsync({
        headquarterId,
        branchData: {
          companyName: formData.companyName.trim(),
          status: formData.status,
          customerType: formData.customerType,
        },
      });

      // Success - trigger callback and close
      onSuccess?.();
      onClose();
    } catch (error) {
      console.error('Error creating branch:', error);

      // Check for specific error messages
      const errorMessage =
        error instanceof Error
          ? error.message
          : 'Fehler beim Anlegen der Filiale. Bitte versuchen Sie es erneut.';

      setErrors({
        submit: errorMessage,
      });
    }
  };

  /**
   * Handle field value change
   */
  const handleFieldChange = (field: string, value: string) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));

    // Clear error for this field when user types
    if (errors[field]) {
      setErrors(prev => {
        const next = { ...prev };
        delete next[field];
        return next;
      });
    }
  };

  /**
   * Handle dialog close
   */
  const handleClose = () => {
    if (!createBranchMutation.isPending) {
      onClose();
    }
  };

  // ========== RENDER ==========

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth fullScreen={isMobile}>
      <DialogTitle>
        <Typography variant="h6" component="div">
          Neue Filiale anlegen
        </Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>
          Filiale wird automatisch mit dem Headquarter verknüpft
        </Typography>
      </DialogTitle>

      <DialogContent dividers>
        {/* Error Alert */}
        {errors.submit && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {errors.submit}
          </Alert>
        )}

        <Box sx={{ mt: 1 }}>
          <Grid container spacing={2}>
            {/* ========== FIRMENNAME (PFLICHTFELD) ========== */}
            <Grid size={{ xs: 12 }}>
              <TextField
                label="Firmenname"
                value={formData.companyName}
                onChange={e => handleFieldChange('companyName', e.target.value)}
                fullWidth
                required
                error={!!errors.companyName}
                helperText={
                  errors.companyName || 'z.B. "NH Hotel München" oder "Filiale Frankfurt"'
                }
                placeholder="Filialname eingeben"
                autoFocus
                disabled={createBranchMutation.isPending}
              />
            </Grid>

            {/* ========== KUNDENTYP ========== */}
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                select
                label="Kundentyp"
                value={formData.customerType}
                onChange={e => handleFieldChange('customerType', e.target.value)}
                fullWidth
                disabled={createBranchMutation.isPending || isLoadingOptions}
                helperText="Rechtsform des Kunden"
              >
                {isLoadingOptions ? (
                  <MenuItem value="">
                    <CircularProgress size={20} />
                  </MenuItem>
                ) : (
                  customerTypeOptions?.map(option => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))
                )}
              </TextField>
            </Grid>

            {/* ========== STATUS ========== */}
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField
                select
                label="Status"
                value={formData.status}
                onChange={e => handleFieldChange('status', e.target.value)}
                fullWidth
                disabled={createBranchMutation.isPending || isLoadingOptions}
                helperText="Aktueller Status der Filiale"
              >
                {isLoadingOptions ? (
                  <MenuItem value="">
                    <CircularProgress size={20} />
                  </MenuItem>
                ) : (
                  statusOptions?.map(option => (
                    <MenuItem key={option.value} value={option.value}>
                      {option.label}
                    </MenuItem>
                  ))
                )}
              </TextField>
            </Grid>
          </Grid>

          {/* ========== INFO BOX ========== */}
          <Alert severity="info" sx={{ mt: 3 }}>
            <Typography variant="body2">
              <strong>Hinweis:</strong> Die Filiale wird automatisch angelegt mit:
            </Typography>
            <Box component="ul" sx={{ mt: 1, mb: 0, pl: 2 }}>
              <li>
                <Typography variant="body2">hierarchyType = FILIALE</Typography>
              </li>
              <li>
                <Typography variant="body2">
                  Verknüpfung zum Headquarter (parentCustomerId)
                </Typography>
              </li>
              <li>
                <Typography variant="body2">
                  Übernahme der xentralCustomerId vom Headquarter
                </Typography>
              </li>
            </Box>
          </Alert>
        </Box>
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} disabled={createBranchMutation.isPending}>
          Abbrechen
        </Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={createBranchMutation.isPending || !formData.companyName.trim()}
        >
          {createBranchMutation.isPending ? 'Speichert...' : 'Filiale anlegen'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// Explicit re-export for Vite HMR
export default CreateBranchDialog;
