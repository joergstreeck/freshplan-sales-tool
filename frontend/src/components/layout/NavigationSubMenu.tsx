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
            pl: 6,
            borderRadius: 1,
            mb: 0.5,
            '&:hover': {
              backgroundColor: 'primary.lighter',
            },
          }}
        >
          <ListItemText
            primary={item.label}
            primaryTypographyProps={{
              variant: 'body2',
              sx: { fontWeight: 400 }
            }}
          />
        </ListItemButton>
      ))}
    </List>
  );
};