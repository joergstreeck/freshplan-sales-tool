import { Tooltip } from '@mui/material';
import { useTranslation } from 'react-i18next';
import type { LeadSource } from './types';

interface LeadSourceIconProps {
  source?: LeadSource;
}

export default function LeadSourceIcon({ source }: LeadSourceIconProps) {
  const { t } = useTranslation('leads');

  if (!source) return null;

  const sourceConfig: Record<LeadSource, { icon: string; label: string }> = {
    MESSE: { icon: '🎪', label: t('wizard.sources.messe') },
    TELEFON: { icon: '📞', label: t('wizard.sources.telefon') },
    EMPFEHLUNG: { icon: '🤝', label: t('wizard.sources.empfehlung') },
    WEB_FORMULAR: { icon: '🌐', label: t('wizard.sources.webFormular') },
    PARTNER: { icon: '🔗', label: t('wizard.sources.partner') },
    SONSTIGE: { icon: '❓', label: t('wizard.sources.sonstige') },
  };

  const config = sourceConfig[source];

  return (
    <Tooltip title={config.label}>
      <span style={{ fontSize: '1.25rem', cursor: 'help' }}>{config.icon}</span>
    </Tooltip>
  );
}
