/**
 * Table Column Settings Component
 * 
 * Ermöglicht die Konfiguration der sichtbaren Tabellenspalten
 * mit Checkboxen für jede verfügbare Spalte
 */

import React, { useState } from 'react';
import {
  IconButton,
  Menu,
  MenuItem,
  FormControlLabel,
  Checkbox,
  Box,
  Typography,
  Divider,
  Button,
  Tooltip,
} from '@mui/material';
import SettingsIcon from '@mui/icons-material/Settings';
import ResetIcon from '@mui/icons-material/RestartAlt';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';
import { useFocusListStore } from '../store/focusListStore';
import type { TableColumn } from '../store/focusListStore';


export function TableColumnSettings() {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);
  
  const {
    tableColumns,
    toggleColumnVisibility,
    resetTableColumns,
    setColumnOrder,
  } = useFocusListStore();

  const handleClick = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleToggleColumn = (columnId: string) => {
    toggleColumnVisibility(columnId);
  };
  
  const handleMoveUp = (columnId: string) => {
    const currentIndex = sortedColumns.findIndex(col => col.id === columnId);
    if (currentIndex > 0) {
      const newOrder = [...sortedColumns];
      [newOrder[currentIndex - 1], newOrder[currentIndex]] = [newOrder[currentIndex], newOrder[currentIndex - 1]];
      setColumnOrder(newOrder.map(col => col.id));
    }
  };
  
  const handleMoveDown = (columnId: string) => {
    const currentIndex = sortedColumns.findIndex(col => col.id === columnId);
    if (currentIndex < sortedColumns.length - 1) {
      const newOrder = [...sortedColumns];
      [newOrder[currentIndex], newOrder[currentIndex + 1]] = [newOrder[currentIndex + 1], newOrder[currentIndex]];
      setColumnOrder(newOrder.map(col => col.id));
    }
  };

  const handleReset = () => {
    resetTableColumns();
  };
  

  // Sortiere Spalten nach order für Anzeige
  const sortedColumns = [...tableColumns].sort((a, b) => a.order - b.order);
  
  // Zähle sichtbare Spalten (mindestens 2 müssen sichtbar sein)
  const visibleCount = tableColumns.filter(col => col.visible).length;

  return (
    <>
      <Tooltip title="Spalten konfigurieren">
        <IconButton
          size="small"
          onClick={handleClick}
          aria-label="Spalten-Einstellungen"
          aria-controls={open ? 'column-settings-menu' : undefined}
          aria-haspopup="true"
          aria-expanded={open ? 'true' : undefined}
        >
          <SettingsIcon fontSize="small" />
        </IconButton>
      </Tooltip>
      
      <Menu
        id="column-settings-menu"
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
        PaperProps={{
          sx: { 
            minWidth: 250,
            maxHeight: 400,
          }
        }}
      >
        <Box sx={{ px: 2, py: 1 }}>
          <Typography variant="subtitle2" fontWeight="medium">
            Spalten konfigurieren
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Wählen Sie die Spalten und ihre Reihenfolge
          </Typography>
        </Box>
        
        <Divider />
        
        <Box sx={{ maxHeight: 300, overflow: 'auto' }}>
          {sortedColumns.map((column, index) => (
            <Box
              key={column.id}
              sx={{ 
                display: 'flex', 
                alignItems: 'center',
                px: 2,
                py: 0.5,
                '&:hover': {
                  backgroundColor: 'action.hover',
                }
              }}
            >
              <FormControlLabel
                control={
                  <Checkbox
                    size="small"
                    checked={column.visible}
                    onChange={() => handleToggleColumn(column.id)}
                    disabled={column.visible && visibleCount <= 2}
                  />
                }
                label={
                  <Typography variant="body2">
                    {column.label}
                  </Typography>
                }
                sx={{ flex: 1, mr: 0 }}
              />
              
              {/* Verschiebe-Buttons - nicht für Aktionen-Spalte */}
              {column.id !== 'actions' && (
                <Box sx={{ display: 'flex', gap: 0.5 }}>
                  <IconButton
                    size="small"
                    onClick={() => handleMoveUp(column.id)}
                    disabled={index === 0}
                    sx={{ p: 0.5 }}
                  >
                    <ArrowUpwardIcon fontSize="small" />
                  </IconButton>
                  <IconButton
                    size="small"
                    onClick={() => handleMoveDown(column.id)}
                    disabled={index === sortedColumns.length - 1 || sortedColumns[index + 1]?.id === 'actions'}
                    sx={{ p: 0.5 }}
                  >
                    <ArrowDownwardIcon fontSize="small" />
                  </IconButton>
                </Box>
              )}
            </Box>
          ))}
        </Box>
        
        <Divider />
        
        <Box sx={{ p: 1 }}>
          <Button
            size="small"
            startIcon={<ResetIcon />}
            onClick={handleReset}
            fullWidth
            variant="text"
          >
            Zurücksetzen
          </Button>
        </Box>
      </Menu>
    </>
  );
}