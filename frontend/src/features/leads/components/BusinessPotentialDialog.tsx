/**
 * BusinessPotentialDialog - Schema-Driven Business Potential Assessment
 *
 * Sprint 2.1.7.2 D11: Server-Driven UI Migration
 *
 * This dialog fetches its field definitions from the backend via useBusinessPotentialSchema.
 * Backend: GET /api/business-potentials/schema (BusinessPotentialSchemaResource.java)
 *
 * Fields (6, dynamically loaded):
 * - businessType (ENUM, required) - Geschäftsart
 * - kitchenSize (ENUM) - Küchengröße
 * - employeeCount (NUMBER) - Mitarbeiteranzahl
 * - estimatedVolume (CURRENCY) - Geschätztes Jahresvolumen
 * - branchCount (NUMBER) - Anzahl Filialen/Standorte
 * - isChain (BOOLEAN) - Kettenbetrieb
 *
 * Architecture:
 * - Backend = Single Source of Truth for field definitions
 * - Frontend = Rendering Layer (no hardcoded field definitions)
 * - Enum options fetched from backend (/api/enums/...)
 */

import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Checkbox,
  Box,
  Typography,
  Divider,
  InputAdornment,
  CircularProgress,
  Alert,
} from '@mui/material';
import { TrendingUp as TrendingUpIcon, Store as StoreIcon } from '@mui/icons-material';
import toast from 'react-hot-toast';
import type { Lead, BusinessType } from '../types';
import { updateLead } from '../api';
import { useBusinessPotentialSchema } from '../../../hooks/useBusinessPotentialSchema';
import { useEnumOptions } from '../../../hooks/useEnumOptions';

interface BusinessPotentialDialogProps {
  open: boolean;
  onClose: () => void;
  lead: Lead;
  onSave: () => void;
}

const BusinessPotentialDialog: React.FC<BusinessPotentialDialogProps> = ({
  open,
  onClose,
  lead,
  onSave,
}) => {
  const [formData, setFormData] = useState({
    businessType: lead.businessType || '',
    kitchenSize: lead.kitchenSize || '',
    employeeCount: lead.employeeCount || '',
    estimatedVolume: lead.estimatedVolume || '',
    branchCount: lead.branchCount || 1,
    isChain: lead.isChain || false,
  });

  // Fetch schema from backend (Server-Driven UI)
  const {
    data: schemas,
    isLoading: schemaLoading,
    error: schemaError,
  } = useBusinessPotentialSchema();

  // Extract business potential schema
  const businessPotentialSchema = schemas?.find(s => s.cardId === 'business_potential');
  const assessmentSection = businessPotentialSchema?.sections?.find(
    s => s.sectionId === 'potential_assessment'
  );
  const fields = assessmentSection?.fields || [];

  // Fetch enum options for ENUM fields
  const businessTypeField = fields.find(f => f.fieldKey === 'businessType');
  const kitchenSizeField = fields.find(f => f.fieldKey === 'kitchenSize');

  const { data: businessTypeOptions } = useEnumOptions(businessTypeField?.enumSource || '');
  const { data: kitchenSizeOptions } = useEnumOptions(kitchenSizeField?.enumSource || '');

  // Reset form when lead changes
  useEffect(() => {
    setFormData({
      businessType: lead.businessType || '',
      kitchenSize: lead.kitchenSize || '',
      employeeCount: lead.employeeCount || '',
      estimatedVolume: lead.estimatedVolume || '',
      branchCount: lead.branchCount || 1,
      isChain: lead.isChain || false,
    });
  }, [lead]);

  const handleSave = async () => {
    try {
      // Validate branchCount (custom business logic)
      if (formData.isChain && (!formData.branchCount || formData.branchCount < 2)) {
        toast.error('Kettenbetriebe müssen mindestens 2 Standorte haben');
        return;
      }

      const payload: Partial<Lead> = {
        businessType: (formData.businessType as BusinessType) || undefined,
        kitchenSize: (formData.kitchenSize as 'small' | 'medium' | 'large') || undefined,
        employeeCount: formData.employeeCount ? Number(formData.employeeCount) : undefined,
        estimatedVolume: formData.estimatedVolume ? Number(formData.estimatedVolume) : undefined,
        branchCount: Number(formData.branchCount),
        isChain: formData.isChain,
      };

      await updateLead(lead.id, payload);
      toast.success('Geschäftspotenzial erfolgreich aktualisiert');
      onSave();
      onClose();
    } catch (error) {
      console.error('Failed to update business potential:', error);
      toast.error('Fehler beim Speichern der Geschäftsdaten');
    }
  };

  // Loading state
  if (schemaLoading) {
    return (
      <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
        <DialogContent
          sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 4 }}
        >
          <CircularProgress />
        </DialogContent>
      </Dialog>
    );
  }

  // Error state
  if (schemaError || !businessPotentialSchema) {
    return (
      <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
        <DialogContent>
          <Alert severity="error">
            Fehler beim Laden des Schemas. Bitte versuchen Sie es später erneut.
          </Alert>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Schließen</Button>
        </DialogActions>
      </Dialog>
    );
  }

  // Extract field definitions from schema
  const businessTypeFieldDef = fields.find(f => f.fieldKey === 'businessType');
  const kitchenSizeFieldDef = fields.find(f => f.fieldKey === 'kitchenSize');
  const employeeCountFieldDef = fields.find(f => f.fieldKey === 'employeeCount');
  const estimatedVolumeFieldDef = fields.find(f => f.fieldKey === 'estimatedVolume');
  const branchCountFieldDef = fields.find(f => f.fieldKey === 'branchCount');
  const isChainFieldDef = fields.find(f => f.fieldKey === 'isChain');

  // DESIGN_SYSTEM.md: Prevent MUI warnings for out-of-range values
  // Only use value if it exists in loaded options
  const safeBusinessType =
    businessTypeOptions?.some(opt => opt.value === formData.businessType)
      ? formData.businessType
      : '';
  const safeKitchenSize =
    kitchenSizeOptions?.some(opt => opt.value === formData.kitchenSize)
      ? formData.kitchenSize
      : '';

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <TrendingUpIcon />
          <Typography variant="h6">{businessPotentialSchema.title}</Typography>
        </Box>
        {businessPotentialSchema.subtitle && (
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            {businessPotentialSchema.subtitle}
          </Typography>
        )}
      </DialogTitle>

      <DialogContent>
        {/* Schema-driven section rendering */}
        <Box sx={{ mb: 3 }}>
          <Typography
            variant="subtitle2"
            gutterBottom
            sx={{ display: 'flex', alignItems: 'center', gap: 1 }}
          >
            <StoreIcon fontSize="small" />
            {assessmentSection?.title || 'Geschäftsdaten'}
          </Typography>
          {assessmentSection?.subtitle && (
            <Typography variant="caption" color="text.secondary" gutterBottom>
              {assessmentSection.subtitle}
            </Typography>
          )}
          <Divider sx={{ mb: 2 }} />

          {/* businessType (ENUM) */}
          {businessTypeFieldDef && (
            <FormControl fullWidth sx={{ mb: 2 }} required={businessTypeFieldDef.required}>
              <InputLabel>{businessTypeFieldDef.label}</InputLabel>
              <Select
                value={safeBusinessType}
                label={businessTypeFieldDef.label}
                onChange={e => setFormData({ ...formData, businessType: e.target.value })}
              >
                <MenuItem value="">—</MenuItem>
                {businessTypeOptions?.map(option => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </Select>
              {businessTypeFieldDef.helpText && (
                <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5 }}>
                  {businessTypeFieldDef.helpText}
                </Typography>
              )}
            </FormControl>
          )}

          {/* kitchenSize (ENUM) */}
          {kitchenSizeFieldDef && (
            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>{kitchenSizeFieldDef.label}</InputLabel>
              <Select
                value={safeKitchenSize}
                label={kitchenSizeFieldDef.label}
                onChange={e => setFormData({ ...formData, kitchenSize: e.target.value })}
              >
                <MenuItem value="">—</MenuItem>
                {kitchenSizeOptions?.map(option => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </Select>
              {kitchenSizeFieldDef.helpText && (
                <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5 }}>
                  {kitchenSizeFieldDef.helpText}
                </Typography>
              )}
            </FormControl>
          )}

          {/* employeeCount (NUMBER) */}
          {employeeCountFieldDef && (
            <TextField
              fullWidth
              label={employeeCountFieldDef.label}
              type="number"
              value={formData.employeeCount}
              onChange={e => setFormData({ ...formData, employeeCount: e.target.value })}
              placeholder={employeeCountFieldDef.placeholder}
              helperText={employeeCountFieldDef.helpText}
              sx={{ mb: 2 }}
            />
          )}

          {/* estimatedVolume (CURRENCY) */}
          {estimatedVolumeFieldDef && (
            <TextField
              fullWidth
              label={estimatedVolumeFieldDef.label}
              type="number"
              value={formData.estimatedVolume}
              onChange={e => setFormData({ ...formData, estimatedVolume: e.target.value })}
              placeholder={estimatedVolumeFieldDef.placeholder}
              helperText={estimatedVolumeFieldDef.helpText}
              InputProps={{
                endAdornment: <InputAdornment position="end">€</InputAdornment>,
              }}
            />
          )}
        </Box>

        {/* Branch/Chain Information */}
        <Box sx={{ mb: 3 }}>
          <Typography
            variant="subtitle2"
            gutterBottom
            sx={{ display: 'flex', alignItems: 'center', gap: 1 }}
          >
            <StoreIcon fontSize="small" />
            Filialen & Standorte
          </Typography>
          <Divider sx={{ mb: 2 }} />

          {/* branchCount (NUMBER) */}
          {branchCountFieldDef && (
            <TextField
              fullWidth
              label={branchCountFieldDef.label}
              type="number"
              value={formData.branchCount}
              onChange={e => setFormData({ ...formData, branchCount: Number(e.target.value) })}
              placeholder={branchCountFieldDef.placeholder}
              helperText={branchCountFieldDef.helpText}
              sx={{ mb: 2 }}
              inputProps={{ min: 1 }}
            />
          )}

          {/* isChain (BOOLEAN) */}
          {isChainFieldDef && (
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.isChain}
                  onChange={e => setFormData({ ...formData, isChain: e.target.checked })}
                />
              }
              label={isChainFieldDef.label}
            />
          )}
          {isChainFieldDef?.helpText && (
            <Typography variant="caption" color="text.secondary" display="block" sx={{ ml: 4 }}>
              {isChainFieldDef.helpText}
            </Typography>
          )}

          {/* Gesamtpotenzial Calculation (Custom Business Logic) */}
          {formData.isChain && formData.branchCount && formData.estimatedVolume && (
            <Box sx={{ mt: 1, p: 2, bgcolor: 'primary.lighter', borderRadius: 1 }}>
              <Typography variant="body2" color="primary">
                Gesamtpotenzial:{' '}
                <strong>
                  {(Number(formData.estimatedVolume) * formData.branchCount).toLocaleString(
                    'de-DE'
                  )}{' '}
                  €/Jahr
                </strong>
              </Typography>
              <Typography variant="caption" color="text.secondary">
                ({Number(formData.estimatedVolume).toLocaleString('de-DE')} € ×{' '}
                {formData.branchCount} Standorte)
              </Typography>
            </Box>
          )}
        </Box>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button onClick={handleSave} variant="contained">
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default BusinessPotentialDialog;
