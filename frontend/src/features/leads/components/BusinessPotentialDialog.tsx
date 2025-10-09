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
  FormGroup,
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  Store as StoreIcon,
  Warning as WarningIcon,
} from '@mui/icons-material';
import toast from 'react-hot-toast';
import type { Lead, BusinessType, UrgencyLevel, RelationshipStatus, DecisionMakerAccess } from '../types';
import { urgencyLevelLabels, relationshipStatusLabels, decisionMakerAccessLabels } from '../types';
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
    // Pain Scoring V3 - OPERATIONAL PAINS
    painStaffShortage: lead.painStaffShortage || false,
    painHighCosts: lead.painHighCosts || false,
    painFoodWaste: lead.painFoodWaste || false,
    painQualityInconsistency: lead.painQualityInconsistency || false,
    painTimePressure: lead.painTimePressure || false,
    // Pain Scoring V3 - SWITCHING PAINS
    painSupplierQuality: lead.painSupplierQuality || false,
    painUnreliableDelivery: lead.painUnreliableDelivery || false,
    painPoorService: lead.painPoorService || false,
    painNotes: lead.painNotes || '',
    // Urgency Dimension
    urgencyLevel: lead.urgencyLevel || 'NORMAL',
    // Relationship Dimension (V280)
    relationshipStatus: lead.relationshipStatus || '',
    decisionMakerAccess: lead.decisionMakerAccess || '',
    competitorInUse: lead.competitorInUse || '',
    internalChampionName: lead.internalChampionName || '',
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
      // Pain Scoring V3 - OPERATIONAL PAINS
      painStaffShortage: lead.painStaffShortage || false,
      painHighCosts: lead.painHighCosts || false,
      painFoodWaste: lead.painFoodWaste || false,
      painQualityInconsistency: lead.painQualityInconsistency || false,
      painTimePressure: lead.painTimePressure || false,
      // Pain Scoring V3 - SWITCHING PAINS
      painSupplierQuality: lead.painSupplierQuality || false,
      painUnreliableDelivery: lead.painUnreliableDelivery || false,
      painPoorService: lead.painPoorService || false,
      painNotes: lead.painNotes || '',
      // Urgency Dimension
      urgencyLevel: lead.urgencyLevel || 'NORMAL',
      // Relationship Dimension (V280)
      relationshipStatus: lead.relationshipStatus || '',
      decisionMakerAccess: lead.decisionMakerAccess || '',
      competitorInUse: lead.competitorInUse || '',
      internalChampionName: lead.internalChampionName || '',
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
        // Pain Scoring V3 - OPERATIONAL PAINS
        painStaffShortage: formData.painStaffShortage,
        painHighCosts: formData.painHighCosts,
        painFoodWaste: formData.painFoodWaste,
        painQualityInconsistency: formData.painQualityInconsistency,
        painTimePressure: formData.painTimePressure,
        // Pain Scoring V3 - SWITCHING PAINS
        painSupplierQuality: formData.painSupplierQuality,
        painUnreliableDelivery: formData.painUnreliableDelivery,
        painPoorService: formData.painPoorService,
        painNotes: formData.painNotes.trim() || undefined,
        // Urgency Dimension
        urgencyLevel: formData.urgencyLevel as UrgencyLevel,
        // Relationship Dimension (V280)
        relationshipStatus: formData.relationshipStatus as RelationshipStatus || undefined,
        decisionMakerAccess: formData.decisionMakerAccess as DecisionMakerAccess || undefined,
        competitorInUse: formData.competitorInUse.trim() || undefined,
        internalChampionName: formData.internalChampionName.trim() || undefined,
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

        {/* Pain Factors */}
        <Box>
          <Typography variant="subtitle2" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <WarningIcon fontSize="small" />
            Pain-Faktoren
          </Typography>
          <Divider sx={{ mb: 2 }} />

          <Typography variant="caption" color="text.secondary" sx={{ mb: 1, display: 'block' }}>
            OPERATIONAL PAINS (Strukturelle Betriebsprobleme)
          </Typography>
          <FormGroup>
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.painStaffShortage}
                  onChange={e => setFormData({ ...formData, painStaffShortage: e.target.checked })}
                />
              }
              label="Personalmangel / Fachkräftemangel"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.painHighCosts}
                  onChange={e => setFormData({ ...formData, painHighCosts: e.target.checked })}
                />
              }
              label="Hoher Kostendruck"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.painFoodWaste}
                  onChange={e => setFormData({ ...formData, painFoodWaste: e.target.checked })}
                />
              }
              label="Food Waste / Überproduktion"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.painQualityInconsistency}
                  onChange={e => setFormData({ ...formData, painQualityInconsistency: e.target.checked })}
                />
              }
              label="Interne Qualitätsinkonsistenz"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.painTimePressure}
                  onChange={e => setFormData({ ...formData, painTimePressure: e.target.checked })}
                />
              }
              label="Zeitdruck / Effizienzprobleme"
            />
          </FormGroup>

          <Typography variant="caption" color="text.secondary" sx={{ mt: 2, mb: 1, display: 'block' }}>
            SWITCHING PAINS (Probleme mit aktuellem Lieferanten)
          </Typography>
          <FormGroup>
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.painSupplierQuality}
                  onChange={e => setFormData({ ...formData, painSupplierQuality: e.target.checked })}
                />
              }
              label="Qualitätsprobleme beim Lieferanten"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.painUnreliableDelivery}
                  onChange={e => setFormData({ ...formData, painUnreliableDelivery: e.target.checked })}
                />
              }
              label="Unzuverlässige Lieferzeiten"
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={formData.painPoorService}
                  onChange={e => setFormData({ ...formData, painPoorService: e.target.checked })}
                />
              }
              label="Schlechter Service/Support"
            />
          </FormGroup>

          <TextField
            fullWidth
            label="Weitere Details zu Pain-Faktoren"
            multiline
            rows={3}
            value={formData.painNotes}
            onChange={e => setFormData({ ...formData, painNotes: e.target.value })}
            sx={{ mt: 2 }}
          />
        </Box>

        {/* Urgency Dimension */}
        <Box sx={{ mb: 3 }}>
          <Typography variant="subtitle2" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <WarningIcon fontSize="small" />
            Zeitdruck / Dringlichkeit
          </Typography>
          <Divider sx={{ mb: 2 }} />

          <FormControl fullWidth>
            <InputLabel>Dringlichkeit</InputLabel>
            <Select
              value={formData.urgencyLevel}
              label="Dringlichkeit"
              onChange={e => setFormData({ ...formData, urgencyLevel: e.target.value as UrgencyLevel })}
            >
              <MenuItem value="NORMAL">{urgencyLevelLabels.NORMAL}</MenuItem>
              <MenuItem value="MEDIUM">{urgencyLevelLabels.MEDIUM}</MenuItem>
              <MenuItem value="HIGH">{urgencyLevelLabels.HIGH}</MenuItem>
              <MenuItem value="EMERGENCY">{urgencyLevelLabels.EMERGENCY}</MenuItem>
            </Select>
          </FormControl>
        </Box>

        {/* Relationship Dimension (V280) */}
        <Box sx={{ mb: 3 }}>
          <Typography variant="subtitle2" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <StoreIcon fontSize="small" />
            Beziehungsebene
          </Typography>
          <Divider sx={{ mb: 2 }} />

          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
            <FormControl fullWidth>
              <InputLabel>Beziehungsqualität</InputLabel>
              <Select
                value={formData.relationshipStatus}
                label="Beziehungsqualität"
                onChange={e => setFormData({ ...formData, relationshipStatus: e.target.value as RelationshipStatus })}
              >
                <MenuItem value="">Keine Angabe</MenuItem>
                <MenuItem value="COLD">{relationshipStatusLabels.COLD}</MenuItem>
                <MenuItem value="CONTACTED">{relationshipStatusLabels.CONTACTED}</MenuItem>
                <MenuItem value="ENGAGED_SKEPTICAL">{relationshipStatusLabels.ENGAGED_SKEPTICAL}</MenuItem>
                <MenuItem value="ENGAGED_POSITIVE">{relationshipStatusLabels.ENGAGED_POSITIVE}</MenuItem>
                <MenuItem value="TRUSTED">{relationshipStatusLabels.TRUSTED}</MenuItem>
                <MenuItem value="ADVOCATE">{relationshipStatusLabels.ADVOCATE}</MenuItem>
              </Select>
            </FormControl>

            <FormControl fullWidth>
              <InputLabel>Entscheider-Zugang</InputLabel>
              <Select
                value={formData.decisionMakerAccess}
                label="Entscheider-Zugang"
                onChange={e => setFormData({ ...formData, decisionMakerAccess: e.target.value as DecisionMakerAccess })}
              >
                <MenuItem value="">Keine Angabe</MenuItem>
                <MenuItem value="UNKNOWN">{decisionMakerAccessLabels.UNKNOWN}</MenuItem>
                <MenuItem value="BLOCKED">{decisionMakerAccessLabels.BLOCKED}</MenuItem>
                <MenuItem value="INDIRECT">{decisionMakerAccessLabels.INDIRECT}</MenuItem>
                <MenuItem value="DIRECT">{decisionMakerAccessLabels.DIRECT}</MenuItem>
                <MenuItem value="IS_DECISION_MAKER">{decisionMakerAccessLabels.IS_DECISION_MAKER}</MenuItem>
              </Select>
            </FormControl>

            <TextField
              fullWidth
              label="Aktueller Wettbewerber"
              value={formData.competitorInUse}
              onChange={e => setFormData({ ...formData, competitorInUse: e.target.value })}
              placeholder="z.B. Metro, Transgourmet"
            />

            <TextField
              fullWidth
              label="Interner Champion"
              value={formData.internalChampionName}
              onChange={e => setFormData({ ...formData, internalChampionName: e.target.value })}
              placeholder="z.B. Max Müller (Küchenchef)"
            />
          </Box>
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
