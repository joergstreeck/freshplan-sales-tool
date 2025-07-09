/**
 * Logo Component mit Fallback
 * 
 * Zeigt das FreshPlan Logo oder einen Fallback,
 * falls die Bilddatei nicht verfügbar ist
 */

import React, { useState } from 'react';
import { Box, Typography } from '@mui/material';
import { LocalFlorist as FloristIcon } from '@mui/icons-material';

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
  showFallback = true
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
            fontFamily: 'Antonio, sans-serif',
            fontWeight: 700,
            color: '#004F7B',
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
          sm: typeof height === 'object' ? height.sm : logoHeight 
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