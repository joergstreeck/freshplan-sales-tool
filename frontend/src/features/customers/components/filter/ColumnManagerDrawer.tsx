/**
 * Column Manager Drawer Component
 * 
 * Manages visible columns and their order in the customer list
 * 
 * @module ColumnManagerDrawer
 * @since FC-005 PR4
 */

import React from 'react';
import {
  Drawer,
  Stack,
  Typography,
  IconButton,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  Switch,
  Alert,
} from '@mui/material';
import {
  Close as CloseIcon,
  ArrowUpward as ArrowUpIcon,
  ArrowDownward as ArrowDownIcon,
} from '@mui/icons-material';
import { useTheme } from '@mui/material/styles';

import type { ColumnConfig } from '../../types/filter.types';

interface ColumnManagerDrawerProps {
  open: boolean;
  onClose: () => void;
  columns: ColumnConfig[];
  onColumnToggle: (columnId: string) => void;
  onColumnMove: (columnId: string, direction: 'up' | 'down') => void;
}

export function ColumnManagerDrawer({
  open,
  onClose,
  columns,
  onColumnToggle,
  onColumnMove,
}: ColumnManagerDrawerProps) {
  const theme = useTheme();

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={onClose}
      sx={{
        '& .MuiDrawer-paper': {
          width: 360,
          p: 3,
        },
      }}
    >
      <Stack spacing={3}>
        {/* Header */}
        <Stack direction="row" alignItems="center" justifyContent="space-between">
          <Typography variant="h6">Spalten verwalten</Typography>
          <IconButton onClick={onClose} size="small">
            <CloseIcon />
          </IconButton>
        </Stack>

        <Divider />

        {/* Column List with Arrow Controls */}
        <List>
          {columns.map((column, index) => (
            <ListItem key={column.id}>
              <ListItemText
                primary={column.label}
                secondary={column.locked ? 'Fixiert' : undefined}
              />
              <ListItemSecondaryAction>
                <Stack direction="row" spacing={1} alignItems="center">
                  {/* Move Up/Down Buttons */}
                  {!column.locked && (
                    <>
                      <IconButton
                        size="small"
                        onClick={() => onColumnMove(column.id, 'up')}
                        disabled={index === 0}
                      >
                        <ArrowUpIcon fontSize="small" />
                      </IconButton>
                      <IconButton
                        size="small"
                        onClick={() => onColumnMove(column.id, 'down')}
                        disabled={index === columns.length - 1}
                      >
                        <ArrowDownIcon fontSize="small" />
                      </IconButton>
                    </>
                  )}
                  {/* Visibility Toggle */}
                  <Switch
                    edge="end"
                    checked={column.visible}
                    onChange={() => onColumnToggle(column.id)}
                    disabled={column.locked}
                  />
                </Stack>
              </ListItemSecondaryAction>
            </ListItem>
          ))}
        </List>

        {/* Info */}
        <Alert severity="info">
          Verwenden Sie die Pfeile, um die Reihenfolge zu ändern. Fixierte Spalten können nicht
          verschoben oder ausgeblendet werden.
        </Alert>
      </Stack>
    </Drawer>
  );
}