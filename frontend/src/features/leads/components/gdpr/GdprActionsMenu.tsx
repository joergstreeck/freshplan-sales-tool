/**
 * GdprActionsMenu - Sprint 2.1.8
 *
 * Dropdown-Menü für DSGVO-Aktionen:
 * - Art. 15: Datenauskunft (PDF Export)
 * - Art. 17: Löschung (Anonymisierung)
 * - Art. 7.3: Einwilligung widerrufen
 */

import React, { useState } from 'react';
import {
  IconButton,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
  Divider,
  Typography,
  CircularProgress,
} from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import DownloadIcon from '@mui/icons-material/Download';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import BlockIcon from '@mui/icons-material/Block';
import { useGdprDataExport, useGdprRevokeConsent } from './useGdprApi';
import { GdprDeleteDialog } from './GdprDeleteDialog';

interface GdprActionsMenuProps {
  leadId: number;
  companyName: string;
  contactBlocked?: boolean;
  gdprDeleted?: boolean;
  canDelete?: boolean;
  canRevokeConsent?: boolean;
  onActionComplete?: () => void;
}

export function GdprActionsMenu({
  leadId,
  companyName,
  contactBlocked = false,
  gdprDeleted = false,
  canDelete = true,
  canRevokeConsent = true,
  onActionComplete,
}: GdprActionsMenuProps) {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

  const dataExport = useGdprDataExport();
  const revokeConsent = useGdprRevokeConsent();

  const open = Boolean(anchorEl);

  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleDataExport = async () => {
    handleClose();
    try {
      await dataExport.mutateAsync(leadId);
      onActionComplete?.();
    } catch (error) {
      console.error('Datenexport fehlgeschlagen:', error);
    }
  };

  const handleRevokeConsent = async () => {
    handleClose();
    try {
      await revokeConsent.mutateAsync(leadId);
      onActionComplete?.();
    } catch (error) {
      console.error('Einwilligungswiderruf fehlgeschlagen:', error);
    }
  };

  const handleOpenDeleteDialog = () => {
    handleClose();
    setDeleteDialogOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false);
    onActionComplete?.();
  };

  // Lead bereits DSGVO-gelöscht
  if (gdprDeleted) {
    return (
      <Typography variant="caption" color="text.secondary" sx={{ fontStyle: 'italic' }}>
        DSGVO-anonymisiert
      </Typography>
    );
  }

  const isLoading = dataExport.isPending || revokeConsent.isPending;

  return (
    <>
      <IconButton
        aria-label="DSGVO-Aktionen"
        aria-controls={open ? 'gdpr-menu' : undefined}
        aria-haspopup="true"
        aria-expanded={open ? 'true' : undefined}
        onClick={handleClick}
        disabled={isLoading}
        size="small"
      >
        {isLoading ? <CircularProgress size={20} /> : <MoreVertIcon />}
      </IconButton>

      <Menu
        id="gdpr-menu"
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        MenuListProps={{
          'aria-labelledby': 'gdpr-actions-button',
        }}
      >
        <Typography
          variant="caption"
          sx={{ px: 2, py: 1, display: 'block', color: 'text.secondary' }}
        >
          DSGVO-Aktionen
        </Typography>

        <MenuItem onClick={handleDataExport}>
          <ListItemIcon>
            <DownloadIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText primary="Datenauskunft (Art. 15)" secondary="PDF mit allen Daten" />
        </MenuItem>

        <Divider />

        <MenuItem onClick={handleRevokeConsent} disabled={contactBlocked || !canRevokeConsent}>
          <ListItemIcon>
            <BlockIcon fontSize="small" color={contactBlocked ? 'disabled' : 'warning'} />
          </ListItemIcon>
          <ListItemText
            primary="Einwilligung widerrufen (Art. 7.3)"
            secondary={contactBlocked ? 'Bereits gesperrt' : 'Kontakt sperren'}
          />
        </MenuItem>

        <MenuItem
          onClick={handleOpenDeleteDialog}
          disabled={!canDelete}
          sx={{ color: 'error.main' }}
        >
          <ListItemIcon>
            <DeleteForeverIcon fontSize="small" color="error" />
          </ListItemIcon>
          <ListItemText
            primary="Daten löschen (Art. 17)"
            secondary="Unwiderrufliche Anonymisierung"
          />
        </MenuItem>
      </Menu>

      <GdprDeleteDialog
        open={deleteDialogOpen}
        onClose={handleCloseDeleteDialog}
        leadId={leadId}
        companyName={companyName}
      />
    </>
  );
}
