import React from 'react';
import { ListItemButton, ListItemIcon, ListItemText, Collapse, Tooltip, Box } from '@mui/material';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { NavigationSubMenu } from './NavigationSubMenu';

// Temporär: Direkte Type-Definition um Import-Probleme zu umgehen
interface NavigationSubItem {
  label: string;
  path?: string;
  action?: string;
  permissions?: string[];
  disabled?: boolean;
  tooltip?: string;
  hasOwnPage?: boolean;
  subItems?: NavigationSubItem[];
}

interface NavigationItemType {
  id: string;
  label: string;
  icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
  path: string;
  permissions?: string[];
  subItems?: NavigationSubItem[];
}

interface NavigationItemProps {
  item: NavigationItemType;
  isActive: boolean;
  isExpanded: boolean;
  isCollapsed: boolean;
  onItemClick: () => void;
  onSubItemClick: (pathOrAction: string, isAction?: boolean) => void;
}

export const NavigationItem: React.FC<NavigationItemProps> = ({
  item,
  isActive,
  isExpanded,
  isCollapsed,
  onItemClick,
  onSubItemClick,
}) => {
  const Icon = item.icon;

  const button = (
    <ListItemButton
      onClick={onItemClick}
      selected={isActive}
      sx={{
        borderRadius: 1,
        mb: 0.5,
        position: 'relative',
        display: 'flex',
        alignItems: 'center',
        pr: 1,
        pl: isActive ? 'calc(8px - 3px)' : '8px', // Kompensiert den Border
        '&.Mui-selected': {
          backgroundColor: 'rgba(148, 196, 86, 0.12)', // Freshfoodz Grün transparent
          borderLeft: '3px solid #94C456',
          '&:hover': {
            backgroundColor: 'rgba(148, 196, 86, 0.18)',
          },
          '& .MuiListItemIcon-root': {
            color: '#94C456',
          },
          '& .MuiListItemText-primary': {
            color: '#94C456',
            fontWeight: 600,
          },
        },
        '&:hover': {
          backgroundColor: 'rgba(148, 196, 86, 0.08)',
        },
      }}
    >
      <ListItemIcon
        sx={{
          minWidth: 40,
          color: isActive ? '#94C456' : '#004F7B', // Freshfoodz Farben
        }}
      >
        <Icon />
      </ListItemIcon>
      {!isCollapsed && (
        <>
          <ListItemText
            primary={item.label}
            sx={{
              mr: item.subItems ? 1 : 0,
              '& .MuiListItemText-primary': {
                fontSize: '0.95rem',
                fontFamily: 'Poppins, sans-serif',
                fontWeight: isActive ? 500 : 400,
                color: isActive ? '#94C456' : '#000',
                lineHeight: 1.3,
                // Erlaubt Zeilenumbruch bei sehr langen Texten
                whiteSpace: 'normal',
                wordBreak: 'break-word',
              },
            }}
          />
          {item.subItems && (
            <Box
              sx={{
                ml: 'auto',
                display: 'flex',
                alignItems: 'center',
                flexShrink: 0,
                width: 24,
                height: 24,
              }}
            >
              {isExpanded ? (
                <ExpandLessIcon sx={{ fontSize: 20, color: '#94C456' }} />
              ) : (
                <ExpandMoreIcon sx={{ fontSize: 20, color: '#94C456' }} />
              )}
            </Box>
          )}
        </>
      )}
    </ListItemButton>
  );

  return (
    <>
      {isCollapsed ? (
        <Tooltip title={item.label} placement="right">
          {button}
        </Tooltip>
      ) : (
        button
      )}

      {item.subItems && !isCollapsed && (
        <Collapse in={isExpanded} timeout="auto" unmountOnExit>
          <NavigationSubMenu items={item.subItems} onItemClick={onSubItemClick} />
        </Collapse>
      )}
    </>
  );
};
