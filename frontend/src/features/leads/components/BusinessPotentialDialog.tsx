/**
 * BusinessPotentialDialog - Edit business metrics and pain factors
 *
 * Sprint 2.1.6 Phase 5+ - V277 Migration
 * Allows editing:
 * - Business metrics (businessType, kitchenSize, employeeCount, estimatedVolume)
 * - Branch information (branchCount, isChain checkbox)
 * - Pain factors (5 checkboxes + painNotes textarea)
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
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  Store as StoreIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import type { Lead, BusinessType } from '../types';
import { updateLead } from '../api';

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
      // Validate branchCount
      if (formData.isChain && (!formData.branchCount || formData.branchCount < 2)) {
        toast.error('Kettenbetriebe müssen mindestens 2 Standorte haben');
        return;
      }

      const payload: Partial<Lead> = {
        businessType: formData.businessType as BusinessType || undefined,
        kitchenSize: formData.kitchenSize as 'small' | 'medium' | 'large' || undefined,
        employeeCount: formData.employeeCount ? Number(formData.employeeCount) : undefined,
        estimatedVolume: formData.estimatedVolume ? Number(formData.estimatedVolume) : undefined,
        branchCount: Number(formData.branchCount),
        isChain: formData.isChain,
      };

      await updateLead(lead.id, payload);
      toast.success('Vertriebsintelligenz erfolgreich aktualisiert');
      onSave();
      onClose();
    } catch (error) {
      console.error('Failed to update business potential:', error);
      toast.error('Fehler beim Speichern der Vertriebsdaten');
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <TrendingUpIcon />
          <Typography variant="h6">Vertriebsintelligenz bearbeiten</Typography>
        </Box>
      </DialogTitle>

      <DialogContent>
        {/* Business Metrics */}
        <Box sx={{ mb: 3 }}>
          <Typography variant="subtitle2" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <StoreIcon fontSize="small" />
            Geschäftsdaten
          </Typography>
          <Divider sx={{ mb: 2 }} />

          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>Geschäftstyp</InputLabel>
            <Select
              value={formData.businessType}
              label="Geschäftstyp"
              onChange={e => setFormData({ ...formData, businessType: e.target.value })}
            >
              <MenuItem value="">—</MenuItem>
              <MenuItem value="RESTAURANT">Restaurant</MenuItem>
              <MenuItem value="HOTEL">Hotel</MenuItem>
              <MenuItem value="CATERING">Catering</MenuItem>
              <MenuItem value="KANTINE">Kantine</MenuItem>
              <MenuItem value="GROSSHANDEL">Großhandel</MenuItem>
              <MenuItem value="LEH">LEH</MenuItem>
              <MenuItem value="BILDUNG">Bildung</MenuItem>
              <MenuItem value="GESUNDHEIT">Gesundheit</MenuItem>
              <MenuItem value="SONSTIGES">Sonstiges</MenuItem>
            </Select>
          </FormControl>

          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>Küchengröße</InputLabel>
            <Select
              value={formData.kitchenSize}
              label="Küchengröße"
              onChange={e => setFormData({ ...formData, kitchenSize: e.target.value })}
            >
              <MenuItem value="">—</MenuItem>
              <MenuItem value="small">Klein</MenuItem>
              <MenuItem value="medium">Mittel</MenuItem>
              <MenuItem value="large">Groß</MenuItem>
            </Select>
          </FormControl>

          <TextField
            fullWidth
            label="Mitarbeiteranzahl"
            type="number"
            value={formData.employeeCount}
            onChange={e => setFormData({ ...formData, employeeCount: e.target.value })}
            sx={{ mb: 2 }}
          />

          <TextField
            fullWidth
            label="Umsatzpotenzial"
            type="number"
            value={formData.estimatedVolume}
            onChange={e => setFormData({ ...formData, estimatedVolume: e.target.value })}
            InputProps={{
              endAdornment: <InputAdornment position="end">€/Monat</InputAdornment>,
            }}
          />
        </Box>

        {/* Branch/Chain Information */}
        <Box sx={{ mb: 3 }}>
          <Typography variant="subtitle2" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <StoreIcon fontSize="small" />
            Filialen & Standorte
          </Typography>
          <Divider sx={{ mb: 2 }} />

          <TextField
            fullWidth
            label="Anzahl Standorte"
            type="number"
            value={formData.branchCount}
            onChange={e => setFormData({ ...formData, branchCount: Number(e.target.value) })}
            sx={{ mb: 2 }}
            inputProps={{ min: 1 }}
          />

          <FormControlLabel
            control={
              <Checkbox
                checked={formData.isChain}
                onChange={e => setFormData({ ...formData, isChain: e.target.checked })}
              />
            }
            label="Kettenbetrieb (mehrere Standorte)"
          />

          {formData.isChain && formData.branchCount && formData.estimatedVolume && (
            <Box sx={{ mt: 1, p: 2, bgcolor: 'primary.lighter', borderRadius: 1 }}>
              <Typography variant="body2" color="primary">
                Gesamtpotenzial:{' '}
                <strong>
                  {(Number(formData.estimatedVolume) * formData.branchCount).toLocaleString('de-DE')} €/Monat
                </strong>
              </Typography>
              <Typography variant="caption" color="text.secondary">
                ({Number(formData.estimatedVolume).toLocaleString('de-DE')} € × {formData.branchCount} Standorte)
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
