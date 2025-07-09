import React from 'react';
import {
  List,
  ListItemButton,
  ListItemText,
} from '@mui/material';

// Temporär: Direkte Type-Definition um Import-Probleme zu umgehen
interface NavigationSubItem {
  label: string;
  path: string;
  permissions?: string[];
}

interface NavigationSubMenuProps {
  items: NavigationSubItem[];
  onItemClick: (path: string) => void;
}

export const NavigationSubMenu: React.FC<NavigationSubMenuProps> = ({
  items,
  onItemClick,
}) => {
  return (
    <List component="div" disablePadding>
      {items.map((item) => (
        <ListItemButton
          key={item.path}
          onClick={() => onItemClick(item.path)}
          sx={{
            pl: 7,
            py: 0.75,
            borderRadius: 1,
            mb: 0.25,
            position: 'relative',
            '&:hover': {
              backgroundColor: 'rgba(148, 196, 86, 0.08)',
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
              backgroundColor: '#94C456',
            },
          }}
        >
          <ListItemText
            primary={item.label}
            primaryTypographyProps={{
              variant: 'body2',
              sx: { 
                fontWeight: 400,
                fontSize: '0.875rem',
                color: '#004F7B',
              }
            }}
          />
        </ListItemButton>
      ))}
    </List>
  );
};