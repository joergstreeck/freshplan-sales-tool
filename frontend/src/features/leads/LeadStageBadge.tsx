import { Chip } from '@mui/material';
import { useTranslation } from 'react-i18next';
import type { LeadStage } from './types';

interface LeadStageBadgeProps {
  stage: LeadStage;
}

export default function LeadStageBadge({ stage }: LeadStageBadgeProps) {
  const { t } = useTranslation('leads');

  const stageConfig: Record<number, { label: string; color: 'info' | 'warning' | 'success' | 'default' }> = {
    0: { label: t('wizard.steps.company'), color: 'info' },
    1: { label: t('wizard.steps.contact'), color: 'warning' },
    2: { label: t('wizard.steps.business'), color: 'success' },
  };

  // Fallback für ungültige Stages
  const config = stageConfig[stage] || { label: `Stage ${stage}`, color: 'default' as const };

  return (
    <Chip
      label={config.label}
      color={config.color}
      size="small"
      sx={{ fontWeight: 'medium' }}
    />
  );
}
