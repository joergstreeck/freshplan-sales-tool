/**
 * Customer Detail Tab: Verlauf
 *
 * Sprint 2.1.7.2 D11: Server-Driven Customer Cards - Phase 3
 *
 * Tab "Verlauf" mit 2 Sections:
 * 1. Ansprechpartner (Contact Management)
 * 2. Timeline (Platzhalter für Sprint 2.2.x)
 *
 * Features:
 * - Contact List mit Edit/Delete
 * - Button [+ Neuer Kontakt]
 * - ContactEditDialog Integration
 * - Timeline Platzhalter
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */

import React, { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  List,
  ListItem,
  ListItemText,
  IconButton,
  Chip,
  Alert,
  CircularProgress,
  Divider,
  Stack,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Person as PersonIcon,
  Timeline as TimelineIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  Note as NoteIcon,
} from '@mui/icons-material';
import { ContactEditDialog, type Contact } from './ContactEditDialog';
import { useCustomerContacts } from '../../../customer/hooks/useCustomerContacts';

interface CustomerDetailTabVerlaufProps {
  customerId: string;
}

/**
 * Tab "Verlauf"
 *
 * Zeigt Kontakte und Timeline (Kommunikationshistorie).
 */
export const CustomerDetailTabVerlauf: React.FC<CustomerDetailTabVerlaufProps> = ({
  customerId,
}) => {
  // Fetch contacts from API (Sprint 2.1.7.2 D11.1 - Hotfix)
  const { data: contacts = [], isLoading } = useCustomerContacts(customerId);

  // Dialog state
  const [contactDialogOpen, setContactDialogOpen] = useState(false);
  const [selectedContact, setSelectedContact] = useState<Contact | null>(null);

  // Handle create contact
  const handleCreateContact = () => {
    setSelectedContact(null);
    setContactDialogOpen(true);
  };

  // Handle edit contact
  const handleEditContact = (contact: Contact) => {
    setSelectedContact(contact);
    setContactDialogOpen(true);
  };

  // Handle delete contact
  const handleDeleteContact = async (contactId: string) => {
    if (!window.confirm('Möchten Sie diesen Kontakt wirklich löschen?')) {
      return;
    }

    try {
      // TODO: Implement API call in Phase 4
      // await deleteContact(customerId, contactId);
      console.log('Delete contact:', contactId);
      // refetch();
    } catch (error) {
      console.error('Error deleting contact:', error);
      alert('Fehler beim Löschen des Kontakts. Bitte versuchen Sie es erneut.');
    }
  };

  // Handle submit contact
  const handleSubmitContact = async (contactData: Partial<Contact>) => {
    try {
      if (selectedContact) {
        // Update existing
        // TODO: Implement API call in Phase 4
        // await updateContact(customerId, selectedContact.id!, contactData);
        console.log('Update contact:', selectedContact.id, contactData);
      } else {
        // Create new
        // TODO: Implement API call in Phase 4
        // await createContact(customerId, contactData);
        console.log('Create contact:', contactData);
      }
      // refetch();
      setContactDialogOpen(false);
    } catch (error) {
      console.error('Error saving contact:', error);
      throw error;
    }
  };

  // Get role/position label (supports both backend 'position' and frontend 'role')
  const getRoleLabel = (contact: Contact): string => {
    const roleOrPosition = contact.position || contact.role || '';

    // Try to map known role codes to labels
    const labels: Record<string, string> = {
      CHEF: 'Küchenchef',
      BUYER: 'Einkäufer',
      MANAGER: 'Manager',
      OTHER: 'Sonstiges',
    };

    // Return mapped label or original position/role text
    return labels[roleOrPosition.toUpperCase()] || roleOrPosition;
  };

  // Contact Action Handlers
  const handleEmailContact = (contact: Contact) => {
    if (contact.email) {
      window.location.href = `mailto:${contact.email}?subject=Kontaktaufnahme von FreshPlan`;
    } else {
      alert('Keine E-Mail-Adresse für diesen Kontakt hinterlegt.');
    }
  };

  const handleCallContact = (contact: Contact) => {
    const phone = contact.mobile || contact.phone;
    if (phone) {
      window.location.href = `tel:${phone}`;
    } else {
      alert('Keine Telefonnummer für diesen Kontakt hinterlegt.');
    }
  };

  const handleAddNote = (contact: Contact) => {
    // TODO Sprint 2.2: Notiz-Dialog öffnen
    console.log('Notiz hinzufügen für:', contact.firstName, contact.lastName);
    alert('Notiz-Funktion wird in Sprint 2.2 implementiert.');
  };

  return (
    <Box>
      {/* Info Banner */}
      <Alert severity="info" sx={{ mb: 3 }}>
        <Typography variant="body2">
          <strong>Tab "Verlauf":</strong> Kontaktverwaltung und Kommunikationshistorie
          (Timeline).
        </Typography>
      </Alert>

      {/* Section 1: Ansprechpartner */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Stack direction="row" spacing={1} alignItems="center">
            <PersonIcon color="primary" />
            <Typography variant="h6">Ansprechpartner</Typography>
          </Stack>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleCreateContact}
          >
            Neuer Kontakt
          </Button>
        </Box>

        <Divider sx={{ mb: 2 }} />

        {/* Loading State */}
        {isLoading && (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
            <CircularProgress />
          </Box>
        )}

        {/* Empty State */}
        {!isLoading && contacts.length === 0 && (
          <Alert severity="info">
            <Typography variant="body2">
              Noch keine Kontakte erfasst. Klicken Sie auf "Neuer Kontakt" um einen
              Ansprechpartner hinzuzufügen.
            </Typography>
          </Alert>
        )}

        {/* Contact List */}
        {!isLoading && contacts.length > 0 && (
          <List>
            {contacts.map(contact => (
              <ListItem
                key={contact.id}
                sx={{
                  border: 1,
                  borderColor: 'divider',
                  borderRadius: 1,
                  mb: 1,
                  bgcolor: contact.isPrimary ? 'action.hover' : 'background.paper',
                }}
                secondaryAction={
                  <Stack direction="row" spacing={0.5} alignItems="center">
                    {/* Action Buttons */}
                    <IconButton
                      size="small"
                      aria-label="E-Mail senden"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleEmailContact(contact);
                      }}
                      disabled={!contact.email}
                      color="primary"
                    >
                      <EmailIcon fontSize="small" />
                    </IconButton>
                    <IconButton
                      size="small"
                      aria-label="Anrufen"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleCallContact(contact);
                      }}
                      disabled={!contact.phone && !contact.mobile}
                      color="primary"
                    >
                      <PhoneIcon fontSize="small" />
                    </IconButton>
                    <IconButton
                      size="small"
                      aria-label="Notiz hinzufügen"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleAddNote(contact);
                      }}
                      color="primary"
                    >
                      <NoteIcon fontSize="small" />
                    </IconButton>

                    <Divider orientation="vertical" flexItem sx={{ mx: 0.5 }} />

                    {/* Edit/Delete Buttons */}
                    <IconButton
                      size="small"
                      aria-label="Kontakt bearbeiten"
                      onClick={() => handleEditContact(contact)}
                    >
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton
                      size="small"
                      aria-label="Kontakt löschen"
                      onClick={() => handleDeleteContact(contact.id!)}
                      color="error"
                    >
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Stack>
                }
              >
                <ListItemText
                  primaryTypographyProps={{ component: 'div' }}
                  secondaryTypographyProps={{ component: 'div' }}
                  primary={
                    <Stack direction="row" spacing={1} alignItems="center">
                      <Typography variant="subtitle1" fontWeight="medium" component="span">
                        {contact.firstName} {contact.lastName}
                      </Typography>
                      {contact.isPrimary && (
                        <Chip
                          label="Hauptansprechpartner"
                          color="primary"
                          size="small"
                        />
                      )}
                    </Stack>
                  }
                  secondary={
                    <Box component="span" sx={{ mt: 0.5, display: 'block' }}>
                      <Stack direction="row" spacing={1} alignItems="center" flexWrap="wrap">
                        <Chip
                          label={getRoleLabel(contact)}
                          size="small"
                          variant="outlined"
                        />
                        {contact.email && (
                          <Typography variant="body2" color="text.secondary" component="span">
                            {contact.email}
                          </Typography>
                        )}
                        {contact.phone && (
                          <Typography variant="body2" color="text.secondary" component="span">
                            {contact.phone}
                          </Typography>
                        )}
                        {contact.mobile && (
                          <Typography variant="body2" color="text.secondary" component="span">
                            {contact.mobile}
                          </Typography>
                        )}
                      </Stack>
                      {contact.notes && (
                        <Typography variant="caption" color="text.secondary" component="span" sx={{ mt: 1, display: 'block' }}>
                          {contact.notes}
                        </Typography>
                      )}
                    </Box>
                  }
                />
              </ListItem>
            ))}
          </List>
        )}
      </Paper>

      {/* Section 2: Timeline (Platzhalter) */}
      <Paper sx={{ p: 3 }}>
        <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
          <TimelineIcon color="primary" />
          <Typography variant="h6">Timeline</Typography>
        </Stack>

        <Divider sx={{ mb: 2 }} />

        <Alert severity="info">
          <Typography variant="body2">
            <strong>Timeline/Aktivitäten:</strong> Kommunikation und Aktivitäten werden in
            Sprint 2.2.x implementiert.
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Geplant: E-Mails, Telefonate, Meetings, Notizen
          </Typography>
        </Alert>
      </Paper>

      {/* Contact Edit Dialog */}
      <ContactEditDialog
        open={contactDialogOpen}
        onClose={() => setContactDialogOpen(false)}
        customerId={customerId}
        contact={selectedContact}
        onSubmit={handleSubmitContact}
      />
    </Box>
  );
};
