import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { List, ListItemButton, ListItemText, Tooltip, Collapse, Box } from '@mui/material';
import { useTheme, alpha } from '@mui/material/styles';
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
  hasOwnPage?: boolean; // NEU für Dashboards mit Untermenüs
  subItems?: NavigationSubItem[]; // NEU für verschachtelte Menüs
}

interface NavigationSubMenuProps {
  items: NavigationSubItem[];
  onItemClick: (pathOrAction: string, isAction?: boolean) => void;
}

export const NavigationSubMenu: React.FC<NavigationSubMenuProps> = ({ items, onItemClick }) => {
  const location = useLocation();
  const theme = useTheme();

  // Auto-expand nested menus that contain the current path
  const getInitialExpandedItems = (): string[] => {
    const expanded: string[] = [];
    const currentPath = location.pathname;

    const checkItem = (item: NavigationSubItem) => {
      // If the item has a path that matches the current path AND has subitems
      // it should be expanded (for dashboards with submenus)
      if (item.hasOwnPage && item.path && currentPath === item.path && item.subItems) {
        expanded.push(item.label);
      }

      if (item.subItems) {
        // Check if any sub-item matches the current path
        const hasActiveSubItem = item.subItems.some(subItem => {
          if (subItem.path && currentPath.startsWith(subItem.path)) {
            return true;
          }
          if (subItem.subItems) {
            return checkItem(subItem);
          }
          return false;
        });

        if (hasActiveSubItem) {
          expanded.push(item.label);
        }
      }
    };

    items.forEach(checkItem);
    return expanded;
  };

  const [expandedItems, setExpandedItems] = useState<string[]>(getInitialExpandedItems);

  // Update expanded items when location changes
  useEffect(() => {
    const currentPath = location.pathname;
    const newExpanded: string[] = [];

    // Check each item to see if it should be expanded
    items.forEach(item => {
      // If we navigated to this item's dashboard, expand it
      if (item.hasOwnPage && item.path === currentPath && item.subItems) {
        newExpanded.push(item.label);
      }

      // Also check for nested paths
      if (item.subItems) {
        const hasActiveSubItem = item.subItems.some(subItem =>
          subItem.path && currentPath.startsWith(subItem.path)
        );
        if (hasActiveSubItem) {
          newExpanded.push(item.label);
        }
      }
    });

    // Always set the expanded items based on current path
    setExpandedItems(newExpanded);
  }, [location.pathname, items]); // eslint-disable-line react-hooks/exhaustive-deps

  const toggleExpanded = (itemLabel: string) => {
    setExpandedItems(prev =>
      prev.includes(itemLabel)
        ? prev.filter(i => i !== itemLabel)
        : [...prev, itemLabel]
    );
  };

  const ensureExpanded = (itemLabel: string) => {
    setExpandedItems(prev =>
      prev.includes(itemLabel) ? prev : [...prev, itemLabel]
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
            if (hasSubItems && item.hasOwnPage && item.path) {
              // Has both submenu AND own page - navigate and ensure expanded
              onItemClick(item.path, false);
              ensureExpanded(item.label);
            } else if (hasSubItems) {
              // Only has submenu - toggle
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
          pl: 6 + depth * 2.5, // Erhöhte Einrückung für bessere Hierarchie
          py: 0.875, // Etwas mehr Padding für bessere Klickfläche
          borderRadius: 1,
          mb: 0.25,
          position: 'relative',
          opacity: isDisabled ? 0.5 : 1,
          cursor: isDisabled ? 'not-allowed' : 'pointer',
          borderLeft: depth > 1 ? `2px solid ${alpha(theme.palette.success.main, 0.1)}` : 'none',
          '&:hover': {
            backgroundColor: isDisabled ? 'transparent' : alpha(theme.palette.success.main, 0.08),
          },
          '&.Mui-selected': {
            backgroundColor: alpha(theme.palette.success.main, 0.12),
            '&:hover': {
              backgroundColor: alpha(theme.palette.success.main, 0.18),
            },
            '& .MuiListItemText-primary': {
              color: theme.palette.success.main,
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
            backgroundColor: theme.palette.success.main,
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
              color: isActive ? theme.palette.success.main : theme.palette.primary.main,
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
