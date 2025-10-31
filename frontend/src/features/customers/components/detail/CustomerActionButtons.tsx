/**
 * Customer Action Buttons Component
 *
 * Sprint 2.1.7.2 D11 - Cockpit Pattern
 *
 * 6 Schnellaktionen für Customer Detail Page (analog zu FreshPlan Cockpit):
 * - E-Mail schreiben
 * - Anrufen
 * - Angebot erstellen
 * - Notiz hinzufügen
 * - Meeting planen
 * - Kunde bearbeiten
 *
 * Design System:
 * - MUI Theme V2 (FreshFoodz Colors)
 * - Responsive: flex-wrap für kleinere Screens
 * - Action Hover Background für visuelle Gruppierung
 *
 * @author FreshPlan Team
 * @since Sprint 2.1.7.2 D11
 */

import { Box, Button } from '@mui/material';
import {
  Email as EmailIcon,
  Phone as PhoneIcon,
  Receipt as ReceiptIcon,
  Note as NoteIcon,
  Event as EventIcon,
  Edit as EditIcon,
} from '@mui/icons-material';
import type { Customer } from '../../../../types/customer.types';

interface CustomerActionButtonsProps {
  customer: Customer;
  onEdit?: () => void;
}

export const CustomerActionButtons = ({ customer, onEdit }: CustomerActionButtonsProps) => {
  // Handler Functions (Platzhalter für Sprint 2.2+)
  const handleEmail = () => {
    // TODO Sprint 2.2: E-Mail Dialog öffnen
    console.log('E-Mail an Kunde:', customer.companyName);
    window.location.href = `mailto:${customer.email || ''}?subject=Anfrage von FreshPlan`;
  };

  const handleCall = () => {
    // TODO Sprint 2.2: Telefon-Integration
    console.log('Anruf bei Kunde:', customer.companyName);
    if (customer.phone) {
      window.location.href = `tel:${customer.phone}`;
    }
  };

  const handleCreateQuote = () => {
    // TODO Sprint 2.2: Angebot-Wizard öffnen
    console.log('Angebot erstellen für:', customer.companyName);
  };

  const handleAddNote = () => {
    // TODO Sprint 2.2: Notiz-Dialog öffnen
    console.log('Notiz hinzufügen für:', customer.companyName);
  };

  const handleScheduleMeeting = () => {
    // TODO Sprint 2.2: Meeting-Planer öffnen
    console.log('Meeting planen mit:', customer.companyName);
  };

  const handleEdit = () => {
    if (onEdit) {
      onEdit();
    }
  };

  return (
    <Box
      sx={{
        display: 'flex',
        gap: 2,
        mb: 3,
        flexWrap: 'wrap',
        p: 2,
        bgcolor: 'action.hover',
        borderRadius: 1,
      }}
    >
      {/* E-Mail */}
      <Button variant="outlined" startIcon={<EmailIcon />} onClick={handleEmail}>
        E-Mail
      </Button>

      {/* Anrufen */}
      <Button variant="outlined" startIcon={<PhoneIcon />} onClick={handleCall}>
        Anrufen
      </Button>

      {/* Angebot */}
      <Button
        variant="outlined"
        startIcon={<ReceiptIcon />}
        color="success"
        onClick={handleCreateQuote}
      >
        Angebot
      </Button>

      {/* Notiz */}
      <Button variant="outlined" startIcon={<NoteIcon />} onClick={handleAddNote}>
        Notiz
      </Button>

      {/* Meeting */}
      <Button variant="outlined" startIcon={<EventIcon />} onClick={handleScheduleMeeting}>
        Meeting
      </Button>

      {/* Bearbeiten */}
      <Button variant="contained" startIcon={<EditIcon />} onClick={handleEdit} sx={{ ml: 'auto' }}>
        Bearbeiten
      </Button>
    </Box>
  );
};
