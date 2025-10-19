/**
 * Logo Component mit intelligentem Fallback-System
 *
 * @module components/common/Logo
 * @description Enterprise-ready Logo-Komponente mit automatischem Fallback auf SVG-Icon
 *              bei fehlenden Bilddateien. Unterstützt responsive Größenanpassung und
 *              verschiedene Darstellungsvarianten.
 *
 * @example
 * ```tsx
 * // Standard Logo mit voller Breite
 * <Logo variant="full" height={60} />
 *
 * // Icon-only Variante für mobile Ansicht
 * <Logo variant="icon" height={{ xs: 30, sm: 40 }} />
 *
 * // Klickbares Logo mit Navigation
 * <Logo onClick={() => navigate('/')} />
 * ```
 *
 * @since 2.0.0
 * @author FreshPlan Team
 */

import React, { useState } from 'react';
import { Box, Typography } from '@mui/material';
import { LocalFlorist as FloristIcon } from '@mui/icons-material';

/**
 * Props für die Logo-Komponente
 *
 * @interface LogoProps
 * @property {'full' | 'icon'} [variant='full'] - Darstellungsvariante des Logos
 * @property {number | { xs: number; sm: number }} [height=40] - Höhe des Logos (responsive möglich)
 * @property {() => void} [onClick] - Click-Handler für Logo-Navigation
 * @property {boolean} [showFallback=true] - Zeigt Fallback-Logo bei Ladefehler
 */
interface LogoProps {
  variant?: 'full' | 'icon';
  height?: number | { xs: number; sm: number };
  onClick?: () => void;
  showFallback?: boolean;
}

export const Logo: React.FC<LogoProps> = ({
  variant = 'full',
  height = 40,
  onClick,
  showFallback = true,
}) => {
  const [imageError, setImageError] = useState(false);

  const logoHeight = typeof height === 'number' ? height : height.sm;
  const mobileHeight = typeof height === 'object' ? height.xs : height * 0.8;

  const logoSrc = variant === 'full' ? '/freshplan-logo.png' : '/freshplan-logo-icon.png';

  // Fallback Logo Component
  const FallbackLogo = () => (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 1,
        cursor: onClick ? 'pointer' : 'default',
        userSelect: 'none',
      }}
      onClick={onClick}
    >
      <Box
        sx={{
          width: logoHeight,
          height: logoHeight,
          borderRadius: '50%',
          backgroundColor: '#94C456',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0,
        }}
      >
        <FloristIcon
          sx={{
            color: '#FFFFFF',
            fontSize: logoHeight * 0.6,
          }}
        />
      </Box>
      {variant === 'full' && (
        <Typography
          variant="h5"
          sx={{
            fontFamily: theme => theme.typography.h4.fontFamily,
            fontWeight: 700,
            color: 'secondary.main',
            fontSize: { xs: '1.25rem', sm: '1.5rem' },
            letterSpacing: '-0.02em',
          }}
        >
          FreshPlan
        </Typography>
      )}
    </Box>
  );

  if (imageError || !showFallback) {
    return <FallbackLogo />;
  }

  return (
    <Box
      component="img"
      src={logoSrc}
      alt="FreshPlan Logo"
      onError={() => setImageError(true)}
      sx={{
        height: {
          xs: typeof height === 'object' ? height.xs : mobileHeight,
          sm: typeof height === 'object' ? height.sm : logoHeight,
        },
        width: 'auto',
        objectFit: 'contain',
        cursor: onClick ? 'pointer' : 'default',
        display: 'block',
      }}
      onClick={onClick}
    />
  );
};

export default Logo;
