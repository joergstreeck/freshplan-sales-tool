/**
 * WarmthIndicator Component
 *
 * Visualizes relationship warmth with a contact using color-coded indicators.
 * Part of FC-005 Contact Management UI - Smart Contact Cards.
 *
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md
 */

import React from 'react';
import { Box, LinearProgress, Typography, Tooltip, Chip, Stack } from '@mui/material';
import {
  Whatshot as WhatshotIcon,
  AcUnit as AcUnitIcon,
  ThermostatAuto as ThermostatAutoIcon,
} from '@mui/icons-material';

export interface RelationshipWarmth {
  temperature: 'HOT' | 'WARM' | 'COOLING' | 'COLD';
  score: number; // 0-100
  lastInteraction?: Date;
  suggestions?: string[];
}

interface WarmthIndicatorProps {
  warmth: RelationshipWarmth;
  size?: 'small' | 'medium' | 'large';
  showDetails?: boolean;
  variant?: 'bar' | 'chip' | 'icon';
}

/**
 * Displays relationship warmth with visual indicators
 */
export const WarmthIndicator: React.FC<WarmthIndicatorProps> = ({
  warmth,
  size = 'medium',
  showDetails = false,
  variant = 'bar',
}) => {
  const getWarmthConfig = () => {
    const configs = {
      HOT: {
        color: '#FF4444',
        icon: <WhatshotIcon />,
        label: 'Heiß',
        description: 'Sehr aktive Beziehung',
      },
      WARM: {
        color: '#FF8800',
        icon: <ThermostatAutoIcon />,
        label: 'Warm',
        description: 'Gute Beziehung',
      },
      COOLING: {
        color: '#FFBB00',
        icon: <ThermostatAutoIcon />,
        label: 'Abkühlend',
        description: 'Aufmerksamkeit erforderlich',
      },
      COLD: {
        color: '#666666',
        icon: <AcUnitIcon />,
        label: 'Kalt',
        description: 'Dringend reaktivieren',
      },
    };
    return configs[warmth.temperature];
  };

  const config = getWarmthConfig();

  if (variant === 'chip') {
    return (
      <Tooltip title={config.description}>
        <Chip
          icon={config.icon}
          label={config.label}
          size={size === 'small' ? 'small' : 'medium'}
          sx={{
            backgroundColor: config.color,
            color: 'white',
            fontWeight: 'bold',
          }}
        />
      </Tooltip>
    );
  }

  if (variant === 'icon') {
    return (
      <Tooltip title={`${config.label}: ${config.description}`}>
        <Box
          sx={{
            color: config.color,
            display: 'inline-flex',
            alignItems: 'center',
          }}
        >
          {config.icon}
        </Box>
      </Tooltip>
    );
  }

  // Default: bar variant
  return (
    <Box>
      <Stack direction="row" alignItems="center" spacing={1} sx={{ mb: 0.5 }}>
        <Box sx={{ color: config.color, display: 'flex' }}>{config.icon}</Box>
        <Typography variant="caption" sx={{ fontWeight: 'medium' }}>
          Beziehungsstatus: {config.label}
        </Typography>
      </Stack>

      <LinearProgress
        variant="determinate"
        value={warmth.score}
        sx={{
          height: size === 'small' ? 4 : size === 'large' ? 8 : 6,
          borderRadius: 1,
          backgroundColor: 'grey.300',
          '& .MuiLinearProgress-bar': {
            backgroundColor: config.color,
          },
        }}
      />

      {showDetails && (
        <Box sx={{ mt: 1 }}>
          <Typography variant="caption" color="text.secondary">
            {config.description}
          </Typography>
          {warmth.lastInteraction && (
            <Typography variant="caption" display="block" color="text.secondary">
              Letzter Kontakt: {new Date(warmth.lastInteraction).toLocaleDateString('de-DE')}
            </Typography>
          )}
          {warmth.suggestions && warmth.suggestions.length > 0 && (
            <Stack direction="row" spacing={0.5} sx={{ mt: 0.5 }}>
              {warmth.suggestions.map((suggestion, index) => (
                <Chip
                  key={index}
                  label={suggestion}
                  size="small"
                  variant="outlined"
                  sx={{ fontSize: '0.7rem' }}
                />
              ))}
            </Stack>
          )}
        </Box>
      )}
    </Box>
  );
};
