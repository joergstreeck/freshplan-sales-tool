/**
 * Smart Sort Selector - Intelligente Sortierung für Sales Cockpit
 *
 * Bietet vordefinierte, Sales-orientierte Sortierungsoptionen
 * mit klaren Beschreibungen und Kategorisierung.
 */

import {
  Box,
  Button,
  ButtonGroup,
  Menu,
  MenuItem,
  Typography,
  Chip,
  ListItemText,
  ListItemIcon,
  Divider,
  Tooltip,
} from '@mui/material';
import {
  Sort as SortIcon,
  KeyboardArrowDown as ArrowDownIcon,
  CheckCircle as CheckIcon,
} from '@mui/icons-material';
import { useState } from 'react';
import { useFocusListStore } from '../store/focusListStore';
import {
  getSmartSortById,
  getSmartSortsByCategory,
  type SmartSortOption,
} from '../store/focusListStore';

interface SmartSortSelectorProps {
  variant?: 'full' | 'compact';
  showDescription?: boolean;
}

export function SmartSortSelector({
  variant = 'full',
  showDescription = true,
}: SmartSortSelectorProps) {
  const { smartSortId, setSmartSort } = useFocusListStore();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  const currentSort = smartSortId ? getSmartSortById(smartSortId) : null;

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleSortSelect = (sortId: string) => {
    setSmartSort(sortId);
    handleClose();
  };

  // Gruppiere Sortierungsoptionen nach Kategorie
  const prioritySorts = getSmartSortsByCategory('priority');
  const businessSorts = getSmartSortsByCategory('business');
  const activitySorts = getSmartSortsByCategory('activity');
  const customSorts = getSmartSortsByCategory('custom');

  const getCategoryLabel = (category: SmartSortOption['category']) => {
    switch (category) {
      case 'priority':
        return '🚨 DRINGEND - Sofort handeln';
      case 'business':
        return '💰 HARDFACTS - Umsatz & Geschäft';
      case 'activity':
        return '📞 AKTIVITÄT - Verkaufsaktivität';
      case 'custom':
        return '📋 STANDARD - Klassische Sortierung';
      default:
        return category;
    }
  };

  const getCategoryColor = (category: SmartSortOption['category']) => {
    switch (category) {
      case 'priority':
        return '#f44336'; // Rot
      case 'business':
        return '#4caf50'; // Grün
      case 'activity':
        return '#2196f3'; // Blau
      case 'custom':
        return '#9e9e9e'; // Grau
      default:
        return '#9e9e9e';
    }
  };

  const renderSortGroup = (sorts: SmartSortOption[], category: SmartSortOption['category']) => {
    if (sorts.length === 0) return null;

    return (
      <Box key={category}>
        <MenuItem disabled sx={{ opacity: 1, py: 1 }}>
          <Typography
            variant="overline"
            sx={{
              fontWeight: 600,
              color: getCategoryColor(category),
              fontSize: '0.75rem',
            }}
          >
            {getCategoryLabel(category)}
          </Typography>
        </MenuItem>
        {sorts.map(sort => (
          <MenuItem
            key={sort.id}
            onClick={() => handleSortSelect(sort.id)}
            selected={smartSortId === sort.id}
            sx={{
              pl: 2,
              minHeight: 56,
              '&.Mui-selected': {
                backgroundColor: 'action.selected',
              },
            }}
          >
            <ListItemIcon sx={{ minWidth: 32 }}>
              {smartSortId === sort.id ? (
                <CheckIcon color="primary" fontSize="small" />
              ) : (
                <Typography sx={{ fontSize: '1.2rem' }}>{sort.icon}</Typography>
              )}
            </ListItemIcon>
            <ListItemText
              primary={sort.label}
              secondary={showDescription ? sort.description : undefined}
              primaryTypographyProps={{
                fontWeight: smartSortId === sort.id ? 600 : 400,
                color: smartSortId === sort.id ? 'primary.main' : 'text.primary',
              }}
              secondaryTypographyProps={{
                fontSize: '0.75rem',
                color: 'text.secondary',
              }}
            />
          </MenuItem>
        ))}
        <Divider sx={{ my: 0.5 }} />
      </Box>
    );
  };

  if (variant === 'compact') {
    return (
      <>
        <Tooltip title={currentSort?.description || 'Sortierung auswählen'}>
          <Button
            variant="outlined"
            size="small"
            startIcon={<SortIcon />}
            endIcon={<ArrowDownIcon />}
            onClick={handleClick}
            sx={{
              minWidth: 160,
              textTransform: 'none',
              fontSize: '0.75rem',
              height: 32,
            }}
          >
            {currentSort?.icon} {currentSort?.label || 'Sortierung'}
          </Button>
        </Tooltip>

        <Menu
          anchorEl={anchorEl}
          open={open}
          onClose={handleClose}
          PaperProps={{
            sx: {
              minWidth: 280,
              maxWidth: 350,
              maxHeight: 400,
            },
          }}
          transformOrigin={{ horizontal: 'left', vertical: 'top' }}
          anchorOrigin={{ horizontal: 'left', vertical: 'bottom' }}
        >
          <MenuItem disabled sx={{ opacity: 1, pb: 1 }}>
            <Typography variant="subtitle2" color="primary">
              🧠 Intelligente Sortierung
            </Typography>
          </MenuItem>
          <Divider />

          {renderSortGroup(prioritySorts, 'priority')}
          {renderSortGroup(businessSorts, 'business')}
          {renderSortGroup(activitySorts, 'activity')}
          {renderSortGroup(customSorts, 'custom')}
        </Menu>
      </>
    );
  }

  return (
    <Box>
      <ButtonGroup variant="outlined" size="small">
        <Button
          startIcon={<SortIcon />}
          onClick={handleClick}
          sx={{
            justifyContent: 'flex-start',
            minWidth: 200,
            textTransform: 'none',
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flex: 1 }}>
            {currentSort && <Typography sx={{ fontSize: '1rem' }}>{currentSort.icon}</Typography>}
            <Box sx={{ textAlign: 'left', flex: 1 }}>
              <Typography variant="body2" sx={{ fontWeight: 500 }}>
                {currentSort?.label || 'Sortierung wählen'}
              </Typography>
              {showDescription && currentSort && (
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                  {currentSort.description}
                </Typography>
              )}
            </Box>
          </Box>
        </Button>
        <Button size="small" onClick={handleClick}>
          <ArrowDownIcon />
        </Button>
      </ButtonGroup>

      <Menu
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        PaperProps={{
          sx: {
            minWidth: 320,
            maxWidth: 400,
            maxHeight: 500,
          },
        }}
        transformOrigin={{ horizontal: 'left', vertical: 'top' }}
        anchorOrigin={{ horizontal: 'left', vertical: 'bottom' }}
      >
        <MenuItem disabled sx={{ opacity: 1, pb: 1 }}>
          <Typography variant="h6" color="primary">
            🧠 Intelligente Sortierung
          </Typography>
        </MenuItem>
        <Divider />

        {renderSortGroup(prioritySorts, 'priority')}
        {renderSortGroup(businessSorts, 'business')}
        {renderSortGroup(activitySorts, 'activity')}
        {renderSortGroup(customSorts, 'custom')}
      </Menu>
    </Box>
  );
}

// Quick Sort Buttons für häufig genutzte Sortierungen
export function QuickSortButtons() {
  const { smartSortId, setSmartSort } = useFocusListStore();

  const quickSorts = [
    'revenue-high-to-low',
    'risk-critical-first',
    'contracts-expiring',
    'last-contact-oldest',
  ];

  return (
    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
      {quickSorts.map(sortId => {
        const sort = getSmartSortById(sortId);
        if (!sort) return null;

        const isActive = smartSortId === sortId;

        return (
          <Chip
            key={sortId}
            label={`${sort.icon} ${sort.label}`}
            variant={isActive ? 'filled' : 'outlined'}
            color={isActive ? 'primary' : 'default'}
            onClick={() => setSmartSort(sortId)}
            sx={{
              cursor: 'pointer',
              '&:hover': {
                backgroundColor: isActive ? 'primary.dark' : 'action.hover',
              },
            }}
          />
        );
      })}
    </Box>
  );
}
