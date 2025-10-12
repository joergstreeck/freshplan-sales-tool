import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  IconButton,
  Card,
  CardContent,
  Typography,
  Alert,
} from '@mui/material';
import {
  Close as CloseIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  Event as EventIcon,
  LocalShipping as LocalShippingIcon,
  ShoppingCart as ShoppingCartIcon,
  Note as NoteIcon,
} from '@mui/icons-material';

interface ActivityDialogProps {
  open: boolean;
  onClose: () => void;
  leadId: number;
  onSave: () => void;
}

/**
 * ActivityDialog - Aktivitäten protokollieren
 *
 * Sprint 2.1.6 Phase 5+ (Platzhalter)
 *
 * Geplante Aktivitätstypen:
 * - 📧 E-Mail
 * - 📞 Anruf
 * - 📅 Termin
 * - 📦 Muster versendet
 * - 🛒 Bestellung (wichtig: Lead → Kunde Conversion!)
 * - 📝 Notiz
 */
export function ActivityDialog({ open, onClose, leadId: _leadId }: ActivityDialogProps) {
  const [_selectedType, _setSelectedType] = useState<string | null>(null);

  const activityTypes = [
    {
      type: 'email',
      icon: <EmailIcon sx={{ fontSize: 40, color: '#004F7B' }} />,
      label: 'E-Mail',
      description: 'E-Mail-Kontakt mit dem Lead',
    },
    {
      type: 'call',
      icon: <PhoneIcon sx={{ fontSize: 40, color: '#004F7B' }} />,
      label: 'Anruf',
      description: 'Telefonischer Kontakt',
    },
    {
      type: 'meeting',
      icon: <EventIcon sx={{ fontSize: 40, color: '#004F7B' }} />,
      label: 'Termin',
      description: 'Meeting / Besuch beim Kunden',
    },
    {
      type: 'sample',
      icon: <LocalShippingIcon sx={{ fontSize: 40, color: '#94C456' }} />,
      label: 'Muster versendet',
      description: 'Produktproben verschickt',
    },
    {
      type: 'order',
      icon: <ShoppingCartIcon sx={{ fontSize: 40, color: '#94C456' }} />,
      label: 'Bestellung',
      description: 'Lead hat bestellt! (→ Conversion)',
    },
    {
      type: 'note',
      icon: <NoteIcon sx={{ fontSize: 40, color: '#004F7B' }} />,
      label: 'Notiz',
      description: 'Interne Notizen / Kommentare',
    },
  ];

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        Neue Aktivität
        <IconButton
          aria-label="close"
          onClick={onClose}
          sx={{
            position: 'absolute',
            right: 8,
            top: 8,
            color: theme => theme.palette.grey[500],
          }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent dividers>
        <Alert severity="info" sx={{ mb: 3 }}>
          <strong>Aktivitäten-Protokollierung:</strong> Die vollständige Implementierung mit
          spezifischen Formularen für jeden Aktivitätstyp wird in der nächsten Phase entwickelt.
        </Alert>

        <Typography variant="h6" gutterBottom>
          Wähle Aktivitätstyp:
        </Typography>

        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', md: '1fr 1fr 1fr' },
            gap: 2,
          }}
        >
          {activityTypes.map(activity => (
            <Card
              key={activity.type}
              sx={{
                cursor: 'pointer',
                border: '2px solid',
                borderColor: 'divider',
                transition: 'all 0.2s',
                '&:hover': {
                  borderColor: '#94C456',
                  bgcolor: 'rgba(148, 196, 86, 0.05)',
                },
              }}
              onClick={() => {
                // TODO: Öffne spezifisches Formular für Aktivitätstyp
                onClose();
              }}
            >
              <CardContent sx={{ textAlign: 'center' }}>
                <Box sx={{ mb: 1 }}>{activity.icon}</Box>
                <Typography variant="h6" gutterBottom>
                  {activity.label}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {activity.description}
                </Typography>
              </CardContent>
            </Card>
          ))}
        </Box>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
      </DialogActions>
    </Dialog>
  );
}
