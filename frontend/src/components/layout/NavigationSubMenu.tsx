import React from 'react';
import { useLocation } from 'react-router-dom';
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
  const location = useLocation();
  
  return (
    <List component="div" disablePadding>
      {items.map((item) => {
        const isActive = location.pathname.startsWith(item.path);
        
        return (
          <ListItemButton
            key={item.path}
            onClick={() => onItemClick(item.path)}
            selected={isActive}
            sx={{
              pl: 7,
              py: 0.75,
              borderRadius: 1,
              mb: 0.25,
              position: 'relative',
              '&:hover': {
                backgroundColor: 'rgba(148, 196, 86, 0.08)',
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
                }
              }}
            />
          </ListItemButton>
        );
      })}
    </List>
  );
};