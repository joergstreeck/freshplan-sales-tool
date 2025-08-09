/**
 * MobileActionBar Component
 * 
 * Touch-optimized action bar for mobile contact management.
 * Part of FC-005 Contact Management UI - Mobile Actions.
 * 
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/MOBILE_CONTACT_ACTIONS.md
 */

import React, { useState } from 'react';
import {
  Box,
  IconButton,
  SpeedDial,
  SpeedDialAction,
  SpeedDialIcon,
  Tooltip,
  Badge,
  useTheme,
  useMediaQuery,
  Stack,
  Chip,
} from '@mui/material';
import {
  MoreVert as MoreVertIcon,
  Close as CloseIcon,
} from '@mui/icons-material';

import type { Contact } from '../../types/contact.types';
import type { QuickAction } from '../../types/mobileActions.types';

interface MobileActionBarProps {
  contact: Contact;
  suggestions: QuickAction[];
  onAction: (action: QuickAction) => void;
  variant?: 'compact' | 'expanded' | 'fab';
  position?: 'bottom' | 'top' | 'inline';
}

/**
 * Mobile-optimized action bar with quick access buttons
 */
export const MobileActionBar: React.FC<MobileActionBarProps> = ({
  contact,
  suggestions,
  onAction,
  variant = 'compact',
  position = 'inline',
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const [speedDialOpen, setSpeedDialOpen] = useState(false);

  // Filter enabled actions
  const enabledActions = suggestions.filter((action) => action.enabled !== false);
  
  // Get primary actions (max 3 for compact view)
  const primaryActions = enabledActions
    .filter((action) => action.primary || action.urgency === 'high')
    .slice(0, 3);
  
  // Get remaining actions for speed dial
  const secondaryActions = enabledActions.filter(
    (action) => !primaryActions.includes(action)
  );

  const handleAction = (action: QuickAction) => {
    // Haptic feedback on mobile
    if ('vibrate' in navigator && isMobile) {
      navigator.vibrate(30);
    }
    onAction(action);
    setSpeedDialOpen(false);
  };

  if (variant === 'fab' && isMobile) {
    return (
      <SpeedDial
        ariaLabel="Contact Actions"
        sx={{
          position: 'fixed',
          bottom: 16,
          right: 16,
        }}
        icon={<SpeedDialIcon openIcon={<CloseIcon />} />}
        open={speedDialOpen}
        onOpen={() => setSpeedDialOpen(true)}
        onClose={() => setSpeedDialOpen(false)}
      >
        {enabledActions.map((action) => (
          <SpeedDialAction
            key={action.id}
            icon={
              <Badge
                color="error"
                variant="dot"
                invisible={action.urgency !== 'high'}
              >
                {action.icon}
              </Badge>
            }
            tooltipTitle={action.label}
            tooltipOpen
            onClick={() => handleAction(action)}
            sx={{
              backgroundColor: action.color,
              color: 'white',
              '&:hover': {
                backgroundColor: action.color,
                filter: 'brightness(0.9)',
              },
            }}
          />
        ))}
      </SpeedDial>
    );
  }

  if (variant === 'expanded') {
    return (
      <Box
        sx={{
          p: 2,
          backgroundColor: 'background.paper',
          borderTop: 1,
          borderColor: 'divider',
          position: position === 'bottom' ? 'fixed' : 'relative',
          bottom: position === 'bottom' ? 0 : 'auto',
          left: 0,
          right: 0,
          zIndex: 100,
        }}
      >
        <Stack direction="row" spacing={1} flexWrap="wrap">
          {enabledActions.map((action) => (
            <Chip
              key={action.id}
              icon={action.icon as React.ReactElement}
              label={action.label}
              onClick={() => handleAction(action)}
              color={action.urgency === 'high' ? 'error' : 'default'}
              variant={action.primary ? 'filled' : 'outlined'}
              sx={{
                '& .MuiChip-icon': {
                  color: action.color,
                },
              }}
            />
          ))}
        </Stack>
      </Box>
    );
  }

  // Compact variant (default)
  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        p: 1,
        backgroundColor: 'background.paper',
        borderTop: 1,
        borderColor: 'divider',
        position: position === 'bottom' ? 'sticky' : 'relative',
        bottom: position === 'bottom' ? 0 : 'auto',
      }}
    >
      {/* Primary Actions */}
      <Stack direction="row" spacing={0.5}>
        {primaryActions.map((action) => (
          <Tooltip key={action.id} title={action.label}>
            <IconButton
              onClick={() => handleAction(action)}
              sx={{
                backgroundColor: action.urgency === 'high' ? action.color : 'transparent',
                color: action.urgency === 'high' ? 'white' : action.color,
                '&:hover': {
                  backgroundColor: action.color,
                  color: 'white',
                },
                transition: 'all 0.2s ease',
              }}
              size={isMobile ? 'large' : 'medium'}
            >
              <Badge
                color="error"
                variant="dot"
                invisible={action.urgency !== 'high'}
              >
                {action.icon}
              </Badge>
            </IconButton>
          </Tooltip>
        ))}
      </Stack>

      {/* More Actions */}
      {secondaryActions.length > 0 && (
        <Box sx={{ position: 'relative' }}>
          <SpeedDial
            ariaLabel="More Actions"
            sx={{ position: 'absolute', bottom: 0, right: 0 }}
            icon={<MoreVertIcon />}
            direction="up"
            open={speedDialOpen}
            onOpen={() => setSpeedDialOpen(true)}
            onClose={() => setSpeedDialOpen(false)}
            FabProps={{
              size: 'small',
              sx: {
                boxShadow: 'none',
                backgroundColor: 'transparent',
                color: 'text.secondary',
                '&:hover': {
                  backgroundColor: 'action.hover',
                },
              },
            }}
          >
            {secondaryActions.map((action) => (
              <SpeedDialAction
                key={action.id}
                icon={action.icon}
                tooltipTitle={action.label}
                onClick={() => handleAction(action)}
                sx={{
                  backgroundColor: 'background.paper',
                  color: action.color,
                  '&:hover': {
                    backgroundColor: action.color,
                    color: 'white',
                  },
                }}
              />
            ))}
          </SpeedDial>
        </Box>
      )}
    </Box>
  );
};