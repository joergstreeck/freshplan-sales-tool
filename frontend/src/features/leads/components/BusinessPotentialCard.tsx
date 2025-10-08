/**
 * BusinessPotentialCard - Displays business metrics and pain factors
 *
 * Sprint 2.1.6 Phase 5+ - V277 Migration
 * Shows:
 * - Business type, kitchen size, employee count
 * - Branch count & chain indicator
 * - Estimated volume with total potential calculation
 * - Lead score with color coding
 * - Pain factors as chips
 */

import React from 'react';
import {
  Card,
  CardHeader,
  CardContent,
  Box,
  Typography,
  Chip,
  Button,
  Grid,
} from '@mui/material';
import {
  Edit as EditIcon,
  Store as StoreIcon,
  AttachMoney as MoneyIcon,
  TrendingUp as TrendingUpIcon,
  Warning as WarningIcon,
  Group as GroupIcon,
  Restaurant as RestaurantIcon,
} from '@mui/icons-material';
import type { Lead } from '../types';
import { urgencyLevelLabels, urgencyLevelColors } from '../types';

interface BusinessPotentialCardProps {
  lead: Lead;
  onEdit: () => void;
}

// Lead Score Color (same as LEAD_SCORING_SPECIFICATION.md)
function getLeadScoreColor(score: number | undefined): string {
  if (score === undefined) return '#9E9E9E'; // Gray
  if (score < 40) return '#f44336'; // Red
  if (score < 70) return '#ff9800'; // Orange
  return '#94C456'; // Green (FreshFoodz CI)
}

// Pain Factor Labels (V278 - ohne Punkte für Vertriebler)
const painFactorLabels: Record<string, string> = {
  painStaffShortage: 'Personalmangel',
  painHighCosts: 'Hohe Kosten',
  painFoodWaste: 'Food Waste',
  painQualityInconsistency: 'Qualitätsinkonsistenz',
  painTimePressure: 'Zeitdruck',
  painSupplierQuality: 'Lieferanten-Qualität',
  painUnreliableDelivery: 'Unzuverlässige Lieferung',
  painPoorService: 'Schlechter Service',
};

const BusinessPotentialCard: React.FC<BusinessPotentialCardProps> = ({ lead, onEdit }) => {
  // Calculate total potential if chain
  const totalPotential = lead.isChain && lead.branchCount && lead.estimatedVolume
    ? lead.estimatedVolume * lead.branchCount
    : lead.estimatedVolume;

  // Collect active pain factors (V278)
  const activePainFactors: string[] = [];
  if (lead.painStaffShortage) activePainFactors.push('painStaffShortage');
  if (lead.painHighCosts) activePainFactors.push('painHighCosts');
  if (lead.painFoodWaste) activePainFactors.push('painFoodWaste');
  if (lead.painQualityInconsistency) activePainFactors.push('painQualityInconsistency');
  if (lead.painTimePressure) activePainFactors.push('painTimePressure');
  if (lead.painSupplierQuality) activePainFactors.push('painSupplierQuality');
  if (lead.painUnreliableDelivery) activePainFactors.push('painUnreliableDelivery');
  if (lead.painPoorService) activePainFactors.push('painPoorService');

  return (
    <Card sx={{ mb: 3 }}>
      <CardHeader
        title={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <TrendingUpIcon />
            <Typography variant="h6">Geschäftspotenzial</Typography>
          </Box>
        }
        action={
          <Button startIcon={<EditIcon />} onClick={onEdit} size="small" variant="outlined">
            Bearbeiten
          </Button>
        }
      />
      <CardContent>
        <Grid container spacing={3}>
          {/* Business Type */}
          <Grid item xs={12} sm={6}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
              <RestaurantIcon fontSize="small" color="action" />
              <Typography variant="body2" color="text.secondary">
                Geschäftstyp
              </Typography>
            </Box>
            <Typography variant="body1">
              {lead.businessType || '—'}
            </Typography>
          </Grid>

          {/* Kitchen Size */}
          <Grid item xs={12} sm={6}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
              <StoreIcon fontSize="small" color="action" />
              <Typography variant="body2" color="text.secondary">
                Küchengröße
              </Typography>
            </Box>
            <Typography variant="body1">
              {lead.kitchenSize
                ? lead.kitchenSize === 'small'
                  ? 'Klein'
                  : lead.kitchenSize === 'medium'
                  ? 'Mittel'
                  : 'Groß'
                : '—'}
            </Typography>
          </Grid>

          {/* Employee Count */}
          <Grid item xs={12} sm={6}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
              <GroupIcon fontSize="small" color="action" />
              <Typography variant="body2" color="text.secondary">
                Mitarbeiteranzahl
              </Typography>
            </Box>
            <Typography variant="body1">
              {lead.employeeCount ? `${lead.employeeCount} Mitarbeiter` : '—'}
            </Typography>
          </Grid>

          {/* Branch Count / Chain Indicator */}
          <Grid item xs={12} sm={6}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
              <StoreIcon fontSize="small" color="action" />
              <Typography variant="body2" color="text.secondary">
                Filialen
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Typography variant="body1">
                {lead.branchCount || 1} {lead.branchCount === 1 ? 'Standort' : 'Standorte'}
              </Typography>
              {lead.isChain && (
                <Chip label="Kettenbetrieb" size="small" color="primary" />
              )}
            </Box>
          </Grid>

          {/* Estimated Volume */}
          <Grid item xs={12} sm={6}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
              <MoneyIcon fontSize="small" color="action" />
              <Typography variant="body2" color="text.secondary">
                Umsatzpotenzial
              </Typography>
            </Box>
            <Typography variant="body1">
              {lead.estimatedVolume
                ? `${lead.estimatedVolume.toLocaleString('de-DE')} €/Monat`
                : '—'}
            </Typography>
          </Grid>

          {/* Total Potential (if chain) */}
          {lead.isChain && totalPotential && totalPotential !== lead.estimatedVolume && (
            <Grid item xs={12} sm={6}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <TrendingUpIcon fontSize="small" color="action" />
                <Typography variant="body2" color="text.secondary">
                  Gesamtpotenzial
                </Typography>
              </Box>
              <Typography variant="body1" fontWeight={600} color="primary">
                {totalPotential.toLocaleString('de-DE')} €/Monat
              </Typography>
              <Typography variant="caption" color="text.secondary">
                ({lead.estimatedVolume?.toLocaleString('de-DE')} € × {lead.branchCount} Standorte)
              </Typography>
            </Grid>
          )}

          {/* Lead Score + Urgency (V278 - neue Anzeige) */}
          {lead.leadScore !== undefined && (
            <Grid item xs={12}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <TrendingUpIcon fontSize="small" color="action" />
                <Typography variant="body2" color="text.secondary">
                  Lead-Score
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', flexWrap: 'wrap' }}>
                <Chip
                  label={`${lead.leadScore}/100`}
                  sx={{
                    backgroundColor: getLeadScoreColor(lead.leadScore),
                    color: '#fff',
                    fontWeight: 600,
                  }}
                />
                {activePainFactors.length > 0 && (
                  <Chip
                    label={`${activePainFactors.length} Pain${activePainFactors.length !== 1 ? 's' : ''} aktiv`}
                    size="small"
                    color="warning"
                    icon={<WarningIcon />}
                  />
                )}
                {lead.urgencyLevel && lead.urgencyLevel !== 'NORMAL' && (
                  <Chip
                    label={urgencyLevelLabels[lead.urgencyLevel]}
                    size="small"
                    sx={{
                      backgroundColor: urgencyLevelColors[lead.urgencyLevel],
                      color: '#fff',
                    }}
                  />
                )}
              </Box>
            </Grid>
          )}

          {/* Pain Factors (Details) */}
          {activePainFactors.length > 0 && (
            <Grid item xs={12}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                <WarningIcon fontSize="small" color="action" />
                <Typography variant="body2" color="text.secondary">
                  Pain-Faktoren (Details)
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {activePainFactors.map(factor => (
                  <Chip
                    key={factor}
                    label={painFactorLabels[factor]}
                    size="small"
                    variant="outlined"
                    color="warning"
                  />
                ))}
              </Box>
              {lead.painNotes && (
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  {lead.painNotes}
                </Typography>
              )}
            </Grid>
          )}

          {/* Empty State */}
          {!lead.businessType &&
            !lead.kitchenSize &&
            !lead.employeeCount &&
            !lead.estimatedVolume &&
            activePainFactors.length === 0 && (
              <Grid item xs={12}>
                <Typography variant="body2" color="text.secondary" textAlign="center">
                  Noch keine Geschäftsdaten erfasst
                </Typography>
              </Grid>
            )}
        </Grid>
      </CardContent>
    </Card>
  );
};

export default BusinessPotentialCard;
