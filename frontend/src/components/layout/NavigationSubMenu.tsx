import React from 'react';
import { useLocation } from 'react-router-dom';
import { List, ListItemButton, ListItemText, Tooltip } from '@mui/material';

// Temporär: Direkte Type-Definition um Import-Probleme zu umgehen
interface NavigationSubItem {
  label: string;
  path?: string;
  action?: string;
  permissions?: string[];
  disabled?: boolean;
  tooltip?: string;
}

interface NavigationSubMenuProps {
  items: NavigationSubItem[];
  onItemClick: (pathOrAction: string, isAction?: boolean) => void;
}

export const NavigationSubMenu: React.FC<NavigationSubMenuProps> = ({ items, onItemClick }) => {
  const location = useLocation();

  return (
    <List component="div" disablePadding>
      {items.map(item => {
        const isActive = item.path ? location.pathname.startsWith(item.path) : false;
        const isDisabled = item.disabled || false;
        const key = item.path || item.action || item.label;

        const button = (
          <ListItemButton
            key={key}
            onClick={() => {
              if (!isDisabled) {
                if (item.action) {
                  onItemClick(item.action, true);
                } else if (item.path) {
                  onItemClick(item.path, false);
                }
              }
            }}
            selected={isActive}
            disabled={isDisabled}
            sx={{
              pl: 7,
              py: 0.75,
              borderRadius: 1,
              mb: 0.25,
              position: 'relative',
              opacity: isDisabled ? 0.5 : 1,
              cursor: isDisabled ? 'not-allowed' : 'pointer',
              '&:hover': {
                backgroundColor: isDisabled ? 'transparent' : 'rgba(148, 196, 86, 0.08)',
              },
              '&.Mui-selected': {
                backgroundColor: 'rgba(148, 196, 86, 0.12)',
                '&:hover': {
                  backgroundColor: 'rgba(148, 196, 86, 0.18)',
                },
                '& .MuiListItemText-primary': {
                  color: '#94C456',
                  fontWeight: 600,
                },
              },
              '&:before': {
                content: '""',
                position: 'absolute',
                left: 48,
                top: '50%',
                transform: 'translateY(-50%)',
                width: 4,
                height: 4,
                borderRadius: '50%',
                backgroundColor: isActive ? '#94C456' : '#94C456',
                opacity: isActive ? 1 : 0.4,
              },
            }}
          >
            <ListItemText
              primary={item.label}
              primaryTypographyProps={{
                variant: 'body2',
                sx: {
                  fontWeight: isActive ? 600 : 400,
                  fontSize: '0.875rem',
                  color: isActive ? '#94C456' : '#004F7B',
                },
              }}
            />
          </ListItemButton>
        );

        // Wrap with tooltip if disabled and tooltip text exists
        if (isDisabled && item.tooltip) {
          return (
            <Tooltip key={key} title={item.tooltip} placement="right">
              <span>{button}</span>
            </Tooltip>
          );
        }

        return button;
      })}
    </List>
  );
};
