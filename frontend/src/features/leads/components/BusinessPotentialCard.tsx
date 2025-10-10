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
  Group as GroupIcon,
  Restaurant as RestaurantIcon,
} from '@mui/icons-material';
import type { Lead } from '../types';

interface BusinessPotentialCardProps {
  lead: Lead;
  onEdit: () => void;
}

const BusinessPotentialCard: React.FC<BusinessPotentialCardProps> = ({ lead, onEdit }) => {

  return (
    <Card sx={{ mb: 3 }}>
      <CardHeader
        title={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <RestaurantIcon />
            <Typography variant="h6">Vertriebsintelligenz</Typography>
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
          <Grid size={{ xs: 12, sm: 6 }}>
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
          <Grid size={{ xs: 12, sm: 6 }}>
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
          <Grid size={{ xs: 12, sm: 6 }}>
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
          <Grid size={{ xs: 12, sm: 6 }}>
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


          {/* Empty State */}
          {!lead.businessType &&
            !lead.kitchenSize &&
            !lead.employeeCount &&
            !lead.branchCount && (
              <Grid size={{ xs: 12 }}>
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
