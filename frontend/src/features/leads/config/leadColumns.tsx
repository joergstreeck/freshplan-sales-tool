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

// Lead Stage Labels (User-friendly display - from Backend LeadStage enum)
const leadStageLabels: Record<string, string> = {
  VORMERKUNG: 'Vormerkung',
  REGISTRIERUNG: 'Registrierung',
  KONTAKTIERT: 'Kontaktiert',
  QUALIFIZIERT: 'Qualifiziert',
  ANGEBOT: 'Angebot',
  VERHANDLUNG: 'Verhandlung',
  GEWONNEN: 'Gewonnen',
  VERLOREN: 'Verloren',
  INAKTIV: 'Inaktiv',
};

// Lead Stage Color Mapping (Progressive Farbskala)
const getLeadStageColor = (stage: string, theme: Theme): string => {
  switch (stage) {
    case 'VORMERKUNG':
      return theme.palette.grey[500];           // Grau - noch nicht begonnen
    case 'REGISTRIERUNG':
      return theme.palette.info.main;           // Blau - registriert
    case 'KONTAKTIERT':
      return theme.palette.primary.main;        // FreshFoodz Grün - aktiv
    case 'QUALIFIZIERT':
      return theme.palette.success.main;        // Grün - qualifiziert
    case 'ANGEBOT':
      return theme.palette.warning.main;        // Orange - Angebot erstellt
    case 'VERHANDLUNG':
      return theme.palette.warning.dark;        // Dunkelorange - Verhandlung
    case 'GEWONNEN':
      return theme.palette.success.dark;        // Dunkelgrün - gewonnen
    case 'VERLOREN':
      return theme.palette.error.main;          // Rot - verloren
    case 'INAKTIV':
      return theme.palette.grey[600];           // Dunkelgrau - inaktiv
    default:
      return theme.palette.grey[400];
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
        fontFamily: 'Antonio, sans-serif', // Design System: Bold Chips
        fontWeight: 700,
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
              <Box sx={{ fontWeight: 'medium' }}>{lead.companyName}</Box>
              {lead.city && (
                <Box sx={{ fontSize: '0.75rem', color: 'text.secondary' }}>{lead.city}</Box>
              )}
            </Box>
            {isNew && (
              <Chip
                label="NEU"
                size="small"
                sx={{ bgcolor: 'primary.main', color: 'white', fontSize: '0.7rem', height: 20 }}
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
      render: (lead: any) => <LeadStageChip stage={lead.stage || 'VORMERKUNG'} />,
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
      id: 'expectedAnnualVolume',
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
      id: 'assignedTo',
      label: 'Zugewiesen an',
      field: 'ownerUserId',
      visible: false,
      order: 6,
      width: 150,
      render: (lead: Lead) => lead.ownerUserId || '-',
    },
  ];
}
