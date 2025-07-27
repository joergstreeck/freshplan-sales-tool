import { Box, Button, Typography, useTheme, useMediaQuery } from '@mui/material';
import { keyframes } from '@emotion/react';

const fadeIn = keyframes`
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
`;

interface EmptyStateHeroProps {
  title: string;
  description?: string;
  illustration?: string;
  action?: {
    label: string;
    onClick: () => void;
    variant?: 'text' | 'outlined' | 'contained';
    size?: 'small' | 'medium' | 'large';
  };
  secondaryAction?: {
    label: string;
    onClick: () => void;
  };
}

export function EmptyStateHero({
  title,
  description,
  illustration,
  action,
  secondaryAction
}: EmptyStateHeroProps) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: 400,
        p: 4,
        textAlign: 'center',
        animation: `${fadeIn} 0.6s ease-out`
      }}
    >
      {illustration && (
        <Box
          component="img"
          src={illustration}
          alt=""
          sx={{
            width: isMobile ? 200 : 280,
            height: 'auto',
            mb: 4,
            opacity: 0.8
          }}
        />
      )}
      
      <Typography 
        variant={isMobile ? "h5" : "h4"} 
        gutterBottom
        sx={{ fontWeight: 600, color: 'text.primary' }}
      >
        {title}
      </Typography>
      
      {description && (
        <Typography 
          variant="body1" 
          color="text.secondary"
          sx={{ mb: 4, maxWidth: 500 }}
        >
          {description}
        </Typography>
      )}
      
      <Box sx={{ display: 'flex', gap: 2, flexDirection: isMobile ? 'column' : 'row' }}>
        {action && (
          <Button
            variant={action.variant || 'contained'}
            size={action.size || 'medium'}
            onClick={action.onClick}
            sx={{ 
              minWidth: isMobile ? 200 : 'auto',
              bgcolor: '#94C456', // Freshfoodz Grün
              color: 'white',
              '&:hover': {
                bgcolor: '#7BA545',
                transform: 'translateY(-2px)',
                boxShadow: theme.shadows[4]
              },
              transition: 'all 0.2s ease'
            }}
          >
            {action.label}
          </Button>
        )}
        
        {secondaryAction && (
          <Button
            variant="text"
            onClick={secondaryAction.onClick}
            sx={{ minWidth: isMobile ? 200 : 'auto' }}
          >
            {secondaryAction.label}
          </Button>
        )}
      </Box>
    </Box>
  );
}