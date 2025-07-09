import React from 'react';
import {
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Collapse,
  Tooltip,
} from '@mui/material';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { NavigationItemType } from '@/types/navigation.types';
import { NavigationSubMenu } from './NavigationSubMenu';

interface NavigationItemProps {
  item: NavigationItemType;
  isActive: boolean;
  isExpanded: boolean;
  isCollapsed: boolean;
  onItemClick: () => void;
  onSubItemClick: (path: string) => void;
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
        '&.Mui-selected': {
          backgroundColor: 'primary.lighter',
          '&:hover': {
            backgroundColor: 'primary.light',
          },
        },
      }}
    >
      <ListItemIcon sx={{ minWidth: 40 }}>
        <Icon />
      </ListItemIcon>
      {!isCollapsed && (
        <>
          <ListItemText primary={item.label} />
          {item.subItems && (
            isExpanded ? <ExpandLessIcon /> : <ExpandMoreIcon />
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
          <NavigationSubMenu
            items={item.subItems}
            onItemClick={onSubItemClick}
          />
        </Collapse>
      )}
    </>
  );
};