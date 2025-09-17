import React, { useState } from 'react';
import { useLocation } from 'react-router-dom';
import { List, ListItemButton, ListItemText, Tooltip, Collapse, Box } from '@mui/material';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';

// Temporär: Direkte Type-Definition um Import-Probleme zu umgehen
interface NavigationSubItem {
  label: string;
  path?: string;
  action?: string;
  permissions?: string[];
  disabled?: boolean;
  tooltip?: string;
  subItems?: NavigationSubItem[]; // NEU für verschachtelte Menüs
}

interface NavigationSubMenuProps {
  items: NavigationSubItem[];
  onItemClick: (pathOrAction: string, isAction?: boolean) => void;
}

export const NavigationSubMenu: React.FC<NavigationSubMenuProps> = ({ items, onItemClick }) => {
  const location = useLocation();
  const [expandedItems, setExpandedItems] = useState<string[]>([]);

  const toggleExpanded = (itemLabel: string) => {
    setExpandedItems(prev =>
      prev.includes(itemLabel)
        ? prev.filter(i => i !== itemLabel)
        : [...prev, itemLabel]
    );
  };

  const renderSubItem = (item: NavigationSubItem, depth = 1): React.ReactNode => {
    const isActive = item.path ? location.pathname.startsWith(item.path) : false;
    const isDisabled = item.disabled || false;
    const hasSubItems = item.subItems && item.subItems.length > 0;
    const isExpanded = expandedItems.includes(item.label);
    const key = item.path || item.action || item.label;

    const button = (
      <ListItemButton
        key={key}
        onClick={() => {
          if (!isDisabled) {
            if (hasSubItems) {
              toggleExpanded(item.label);
            } else if (item.action) {
              onItemClick(item.action, true);
            } else if (item.path) {
              onItemClick(item.path, false);
            }
          }
        }}
        selected={isActive}
        disabled={isDisabled}
        sx={{
          pl: 5 + depth * 2,
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
            left: 40 + depth * 16,
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
              fontSize: depth === 1 ? '0.875rem' : '0.813rem',
              color: isActive ? '#94C456' : '#004F7B',
            },
          }}
        />
        {hasSubItems && (
          <Box sx={{ ml: 1, display: 'flex', alignItems: 'center' }}>
            {isExpanded ? (
              <ExpandLessIcon sx={{ fontSize: 16, color: '#94C456' }} />
            ) : (
              <ExpandMoreIcon sx={{ fontSize: 16, color: '#004F7B' }} />
            )}
          </Box>
        )}
      </ListItemButton>
    );

    const wrappedButton = isDisabled && item.tooltip ? (
      <Tooltip key={key} title={item.tooltip} placement="right">
        <span>{button}</span>
      </Tooltip>
    ) : (
      button
    );

    return (
      <React.Fragment key={key}>
        {wrappedButton}
        {hasSubItems && (
          <Collapse in={isExpanded} timeout="auto" unmountOnExit>
            <List component="div" disablePadding>
              {item.subItems!.map(subItem => renderSubItem(subItem, depth + 1))}
            </List>
          </Collapse>
        )}
      </React.Fragment>
    );
  };

  return (
    <List component="div" disablePadding>
      {items.map(item => renderSubItem(item))}
    </List>
  );
};
