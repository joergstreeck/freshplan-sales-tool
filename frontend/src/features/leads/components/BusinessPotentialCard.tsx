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

import React, { useMemo } from 'react';
import {
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Box,
  Typography,
  Chip,
  IconButton,
  Grid,
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Edit as EditIcon,
  Store as StoreIcon,
  Group as GroupIcon,
  Restaurant as RestaurantIcon,
} from '@mui/icons-material';
import { useEnumOptions } from '../../../hooks/useEnumOptions';
import type { Lead } from '../types';

interface BusinessPotentialCardProps {
  lead: Lead;
  expanded: boolean;
  onChange: (event: React.SyntheticEvent, isExpanded: boolean) => void;
  onEdit: () => void;
}

const BusinessPotentialCard: React.FC<BusinessPotentialCardProps> = ({
  lead,
  expanded,
  onChange,
  onEdit,
}) => {
  // Server-Driven Enums (Sprint 2.1.7.7 - Enum-Rendering-Parity)
  const { data: businessTypeOptions } = useEnumOptions('/api/enums/business-types');
  const { data: kitchenSizeOptions } = useEnumOptions('/api/enums/kitchen-sizes');

  // Create fast lookup maps (O(1) statt O(n) mit .find())
  const businessTypeLabels = useMemo(() => {
    if (!businessTypeOptions) return {};
    return businessTypeOptions.reduce(
      (acc, item) => {
        acc[item.value] = item.label;
        return acc;
      },
      {} as Record<string, string>,
    );
  }, [businessTypeOptions]);

  const kitchenSizeLabels = useMemo(() => {
    if (!kitchenSizeOptions) return {};
    return kitchenSizeOptions.reduce(
      (acc, item) => {
        acc[item.value] = item.label;
        return acc;
      },
      {} as Record<string, string>,
    );
  }, [kitchenSizeOptions]);

  // Preview Info für Header
  const previewParts = [
    lead.businessType ? businessTypeLabels[lead.businessType] || lead.businessType : '—',
    lead.city || '—',
    `${lead.branchCount || 1} Standort${(lead.branchCount || 1) > 1 ? 'e' : ''}`,
  ].filter(part => part !== '—');

  const previewText = previewParts.join(' • ');

  return (
    <Accordion
      expanded={expanded}
      onChange={onChange}
      sx={{ mb: 2, border: '1px solid', borderColor: 'divider' }}
    >
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <Box sx={{ display: 'flex', alignItems: 'center', width: '100%', gap: 1 }}>
          <RestaurantIcon color="primary" />
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h6">Vertriebsintelligenz</Typography>
            {!expanded && (
              <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                {previewText || 'Noch keine Daten erfasst'}
              </Typography>
            )}
          </Box>
          <IconButton
            size="small"
            component="div"
            onClick={e => {
              e.stopPropagation();
              onEdit();
            }}
            sx={{ cursor: 'pointer' }}
          >
            <EditIcon fontSize="small" />
          </IconButton>
        </Box>
      </AccordionSummary>

      <AccordionDetails>
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
              {lead.businessType ? businessTypeLabels[lead.businessType] || lead.businessType : '—'}
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
              {lead.kitchenSize ? kitchenSizeLabels[lead.kitchenSize] || lead.kitchenSize : '—'}
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
              {lead.isChain && <Chip label="Kettenbetrieb" size="small" color="primary" />}
            </Box>
          </Grid>

          {/* Empty State */}
          {!lead.businessType && !lead.kitchenSize && !lead.employeeCount && !lead.branchCount && (
            <Grid size={{ xs: 12 }}>
              <Typography variant="body2" color="text.secondary" textAlign="center">
                Noch keine Geschäftsdaten erfasst
              </Typography>
            </Grid>
          )}
        </Grid>
      </AccordionDetails>
    </Accordion>
  );
};

export default BusinessPotentialCard;
