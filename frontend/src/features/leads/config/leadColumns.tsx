/**
 * Lead Table Columns Configuration
 *
 * Column configuration for Lead DataTable (similar to customerColumns.tsx)
 * Based on LEADS_TABLE_COLUMNS from contextConfig.ts
 *
 * @module leadColumns
 * @since Sprint 2.1.7.7 (Migration M3)
 */

import { Box, Chip, Typography, useTheme } from '@mui/material';
import type { Theme } from '@mui/material/styles';
import type { DataTableColumn } from '../../shared/components/data-table/DataTableTypes';
import type { Lead } from '../types';
import { formatCurrency, formatDate } from '../../shared/utils/dataFormatters';

// Lead Stage Labels (Progressive Profiling - 3 stages only)
// Backend LeadStage enum: VORMERKUNG (0), REGISTRIERUNG (1), QUALIFIZIERT (2)
// API sends stage as STRING, not number
const leadStageLabels: Record<string, string> = {
  VORMERKUNG: 'Vormerkung',       // Stage 0: Pre-Claim (minimal company data)
  REGISTRIERUNG: 'Registrierung', // Stage 1: Registered (contact added)
  QUALIFIZIERT: 'Qualifiziert',   // Stage 2: Qualified (full business data)
};

// Lead Stage Color Mapping (Progressive Profiling 3 stages)
const getLeadStageColor = (stage: string, theme: Theme): string => {
  switch (stage) {
    case 'VORMERKUNG':
      return theme.palette.grey[500];     // Grau - Pre-Claim (noch nicht geschützt)
    case 'REGISTRIERUNG':
      return theme.palette.info.main;     // Blau - Registriert (Schutz aktiv)
    case 'QUALIFIZIERT':
      return theme.palette.success.main;  // Grün - Qualifiziert (bereit für Conversion)
    default:
      return theme.palette.grey[400];     // Fallback
  }
};

/**
 * Lead Score Display Component (Progress Bar like Customer Risk Score)
 * Prevents React Hook errors by extracting hook usage into component
 */
function LeadScoreDisplay({ score }: { score: number | null | undefined }) {
  if (score === null || score === undefined) {
    return (
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <Typography variant="caption" color="text.secondary">
          -
        </Typography>
      </Box>
    );
  }

  // Score-based coloring (same logic as before)
  const scoreColor =
    score >= 80
      ? 'success.main'
      : score >= 60
        ? 'info.main'
        : score >= 40
          ? 'warning.main'
          : 'error.main';

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <Box
        sx={{
          width: 60,
          height: 8,
          bgcolor: 'grey.200',
          borderRadius: 1,
          overflow: 'hidden',
        }}
      >
        <Box
          sx={{
            width: `${score}%`,
            height: '100%',
            bgcolor: scoreColor,
            transition: 'width 0.3s ease',
          }}
        />
      </Box>
      <Typography variant="caption" color="text.secondary">
        {score}
      </Typography>
    </Box>
  );
}

/**
 * Lead Stage Chip Component (Theme-based colors like CustomerStatusChip)
 */
function LeadStageChip({ stage }: { stage: string }) {
  const theme = useTheme();
  const stageColor = getLeadStageColor(stage, theme);
  const label = leadStageLabels[stage] || stage;

  return (
    <Chip
      label={label}
      size="small"
      sx={{
        bgcolor: stageColor,
        color: 'white',
        fontWeight: 700, // Bold weight for emphasis
      }}
    />
  );
}

/**
 * Get Lead Table Columns Configuration
 *
 * Returns column configuration for DataTable component.
 * Based on LEADS_TABLE_COLUMNS but with custom rendering.
 */
export function getLeadTableColumns(): DataTableColumn<Lead>[] {
  return [
    {
      id: 'companyName',
      label: 'Lead',
      field: 'companyName',
      visible: true,
      order: 0,
      width: 250,
      render: (lead: Lead) => {
        const isNew =
          lead.createdAt && new Date(lead.createdAt) > new Date(Date.now() - 24 * 60 * 60 * 1000);
        return (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box>
              <Typography variant="body2" fontWeight="medium">
                {lead.companyName}
              </Typography>
              {lead.city && (
                <Typography variant="caption" color="text.secondary">
                  {lead.city}
                </Typography>
              )}
            </Box>
            {isNew && (
              <Chip
                label="NEU"
                size="small"
                sx={{ bgcolor: 'primary.main', color: 'white' }}
              />
            )}
          </Box>
        );
      },
    },
    {
      id: 'stage',
      label: 'Status',
      field: 'stage',
      visible: true,
      order: 1,
      width: 140,
      align: 'left',
      render: (lead: Lead) => <LeadStageChip stage={lead.stage || 'VORMERKUNG'} />,
    },
    {
      id: 'businessType',
      label: 'Branche',
      field: 'businessType',
      visible: true,
      order: 2,
      width: 150,
      render: (lead: Lead) => lead.businessType || '-',
    },
    {
      id: 'leadScore',
      label: 'Score',
      field: 'leadScore',
      visible: true,
      order: 3,
      width: 120,
      align: 'left',
      sortable: true,
      render: (lead: Lead) => <LeadScoreDisplay score={lead.leadScore} />,
    },
    {
      id: 'estimatedVolume',
      label: 'Erwarteter Umsatz',
      field: 'estimatedVolume',
      visible: true,
      order: 4,
      width: 150,
      align: 'right',
      sortable: true,
      render: (lead: Lead) => formatCurrency(lead.estimatedVolume),
    },
    {
      id: 'createdAt',
      label: 'Erstellt am',
      field: 'createdAt',
      visible: true,
      order: 5,
      width: 120,
      sortable: true,
      render: (lead: Lead) => formatDate(lead.createdAt),
    },
    {
      id: 'ownerUserId',
      label: 'Zugewiesen an',
      field: 'ownerUserId',
      visible: false,
      order: 6,
      width: 150,
      render: (lead: Lead) => lead.ownerUserId || '-',
    },
  ];
}
