/**
 * Step 3: Multi-Contact Management
 *
 * Advanced contact management with CRUD operations, smart cards,
 * and relationship intelligence features.
 *
 * CRITICAL: Uses mandatory Theme Architecture components!
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/THEME_ARCHITECTURE.md
 */

import React, { useState, useMemo, useCallback } from 'react';
import {
  Box,
  Typography,
  Button,
  IconButton,
  Chip,
  Alert,
  Tooltip,
  Divider,
  Badge,
  Paper,
  Stack,
  Collapse,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Edit as EditIcon,
  Star as StarIcon,
  StarBorder as StarBorderIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  LocationOn as LocationIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  Business as BusinessIcon,
} from '@mui/icons-material';

import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { AdaptiveFormContainer } from '../adaptive/AdaptiveFormContainer';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import { LocationCheckboxList } from '../shared/LocationCheckboxList';
import { ContactCard } from '../contacts/ContactCard';
import { ContactFormDialog } from '../contacts/ContactFormDialog';
import { ContactQuickActions } from '../contacts/ContactQuickActions';

import type { Contact } from '../../types/contact.types';
import type { FieldDefinition } from '../../types/field.types';
import {
  contactFieldExtensions,
  getContactFieldsForGroup,
} from '../../data/fieldCatalogContactExtensions';
import { getContactFullName, getContactBadgeInfo } from '../../types/contact.types';

/**
 * Step 3: Multi-Contact Management
 *
 * Implements the vision of an intelligent relationship center
 * with mandatory Theme Architecture components.
 */
export const Step3MultiContactManagement: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  const {
    contacts,
    primaryContactId,
    locations,
    contactValidationErrors,
    addContact,
    updateContact,
    removeContact,
    setPrimaryContact,
    validateContactField,
  } = useCustomerOnboardingStore();

  // Dialog state
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingContact, setEditingContact] = useState<Contact | null>(null);
  const [expandedCards, setExpandedCards] = useState<Set<string>>(new Set());

  // Sort contacts: primary first, then by name
  const sortedContacts = useMemo(() => {
    return [...contacts].sort((a, b) => {
      if (a.isPrimary && !b.isPrimary) return -1;
      if (!a.isPrimary && b.isPrimary) return 1;
      return getContactFullName(a).localeCompare(getContactFullName(b));
    });
  }, [contacts]);

  // Handle card expansion
  const toggleCardExpansion = useCallback((contactId: string) => {
    setExpandedCards(prev => {
      const next = new Set(prev);
      if (next.has(contactId)) {
        next.delete(contactId);
      } else {
        next.add(contactId);
      }
      return next;
    });
  }, []);

  // Handle add contact
  const handleAddContact = useCallback(() => {
    setEditingContact(null);
    setIsFormOpen(true);
  }, []);

  // Handle edit contact
  const handleEditContact = useCallback((contact: Contact) => {
    setEditingContact(contact);
    setIsFormOpen(true);
  }, []);

  // Handle delete contact
  const handleDeleteContact = useCallback(
    (contactId: string) => {
      if (window.confirm('Möchten Sie diesen Kontakt wirklich löschen?')) {
        removeContact(contactId);
      }
    },
    [removeContact]
  );

  // Handle form submit
  const handleFormSubmit = useCallback(
    (contactData: Partial<Contact>) => {
      if (editingContact) {
        updateContact(editingContact.id, contactData);
      } else {
        addContact(contactData);
      }
      setIsFormOpen(false);
      setEditingContact(null);
    },
    [editingContact, updateContact, addContact]
  );

  // Get validation errors for a contact
  const getContactErrors = useCallback(
    (contactId: string) => {
      return contactValidationErrors[contactId]?.fieldErrors || {};
    },
    [contactValidationErrors]
  );

  return (
    <Box data-testid="adaptive-form-container">
      {/* Header Section */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="h5" gutterBottom>
          Ansprechpartner verwalten
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Erfassen Sie alle wichtigen Kontaktpersonen.
          {locations.length > 1 && ' Sie können Kontakte einzelnen Standorten zuordnen.'}
        </Typography>
      </Box>

      {/* Add Contact Button */}
      <Box sx={{ mb: 3 }}>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleAddContact}
          fullWidth={isMobile}
        >
          Neuen Kontakt hinzufügen
        </Button>
      </Box>

      {/* Contact List */}
      {sortedContacts.length === 0 ? (
        <Alert severity="info">
          Noch keine Kontakte erfasst. Fügen Sie mindestens einen Hauptansprechpartner hinzu.
        </Alert>
      ) : (
        <Stack spacing={2}>
          {sortedContacts.map(contact => (
            <Paper
              key={contact.id}
              data-testid="contact-card"
              role="region"
              aria-label={`Kontakt ${getContactFullName(contact)}`}
              elevation={contact.isPrimary ? 3 : 1}
              sx={{
                p: 2,
                border: contact.isPrimary ? `2px solid ${theme.palette.primary.main}` : undefined,
                position: 'relative',
              }}
            >
              {/* Primary Badge */}
              {contact.isPrimary && (
                <Chip
                  label="Hauptansprechpartner"
                  color="primary"
                  size="small"
                  icon={<StarIcon />}
                  sx={{
                    position: 'absolute',
                    top: 8,
                    right: 8,
                  }}
                />
              )}

              {/* Contact Header */}
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <Box sx={{ flex: 1 }}>
                  <Typography variant="h6">{getContactFullName(contact)}</Typography>
                  {contact.position && (
                    <Typography variant="body2" color="text.secondary">
                      {contact.position}
                    </Typography>
                  )}
                </Box>

                {/* Actions */}
                <Stack direction="row" spacing={1}>
                  {!contact.isPrimary && (
                    <Tooltip title="Als Hauptansprechpartner festlegen">
                      <IconButton
                        size="small"
                        onClick={() => setPrimaryContact(contact.id)}
                        data-testid={`set-primary-${contact.id}`}
                      >
                        <StarBorderIcon />
                      </IconButton>
                    </Tooltip>
                  )}
                  <IconButton
                    size="small"
                    onClick={() => handleEditContact(contact)}
                    data-testid={`edit-contact-${contact.id}`}
                  >
                    <EditIcon />
                  </IconButton>
                  <IconButton
                    size="small"
                    onClick={() => handleDeleteContact(contact.id)}
                    disabled={contact.isPrimary && contacts.length > 1}
                    data-testid={`delete-contact-${contact.id}`}
                  >
                    <DeleteIcon />
                  </IconButton>
                  <IconButton size="small" onClick={() => toggleCardExpansion(contact.id)}>
                    {expandedCards.has(contact.id) ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                  </IconButton>
                </Stack>
              </Box>

              {/* Contact Quick Info */}
              <Stack direction="row" spacing={2} flexWrap="wrap" sx={{ mb: 1 }}>
                {contact.email && (
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <EmailIcon fontSize="small" color="action" />
                    <Typography variant="body2">{contact.email}</Typography>
                  </Box>
                )}
                {contact.phone && (
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <PhoneIcon fontSize="small" color="action" />
                    <Typography variant="body2">{contact.phone}</Typography>
                  </Box>
                )}
                {contact.mobile && (
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <PhoneIcon fontSize="small" color="action" />
                    <Typography variant="body2">{contact.mobile} (Mobil)</Typography>
                  </Box>
                )}
              </Stack>

              {/* Responsibility Info */}
              {locations.length > 1 && (
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mb: 1 }}>
                  <LocationIcon fontSize="small" color="action" />
                  <Typography variant="body2" color="text.secondary">
                    {contact.responsibilityScope === 'all'
                      ? 'Alle Standorte'
                      : `${contact.assignedLocationIds?.length || 0} Standorte`}
                  </Typography>
                </Box>
              )}

              {/* Roles */}
              {contact.roles && contact.roles.length > 0 && (
                <Stack direction="row" spacing={0.5} flexWrap="wrap">
                  {contact.roles.map(role => (
                    <Chip key={role} label={role} size="small" variant="outlined" />
                  ))}
                </Stack>
              )}

              {/* Expanded Details */}
              <Collapse in={expandedCards.has(contact.id)}>
                <Divider sx={{ my: 2 }} />
                <AdaptiveFormContainer>
                  {/* Decision Level */}
                  {contact.decisionLevel && (
                    <Box>
                      <Typography variant="caption" color="text.secondary">
                        Entscheidungsebene
                      </Typography>
                      <Typography variant="body2">{contact.decisionLevel}</Typography>
                    </Box>
                  )}

                  {/* Personal Notes */}
                  {contact.personalNotes && (
                    <Box sx={{ gridColumn: 'span 2' }}>
                      <Typography variant="caption" color="text.secondary">
                        Persönliche Notizen
                      </Typography>
                      <Typography variant="body2">{contact.personalNotes}</Typography>
                    </Box>
                  )}

                  {/* Hobbies */}
                  {contact.hobbies && contact.hobbies.length > 0 && (
                    <Box>
                      <Typography variant="caption" color="text.secondary">
                        Hobbys & Interessen
                      </Typography>
                      <Stack direction="row" spacing={0.5} flexWrap="wrap" sx={{ mt: 0.5 }}>
                        {contact.hobbies.map(hobby => (
                          <Chip
                            key={hobby}
                            label={hobby}
                            size="small"
                            color="secondary"
                            variant="outlined"
                          />
                        ))}
                      </Stack>
                    </Box>
                  )}

                  {/* Birthday */}
                  {contact.birthday && (
                    <Box>
                      <Typography variant="caption" color="text.secondary">
                        Geburtstag
                      </Typography>
                      <Typography variant="body2">
                        {new Date(contact.birthday).toLocaleDateString('de-DE')}
                      </Typography>
                    </Box>
                  )}
                </AdaptiveFormContainer>

                {/* Quick Actions (Mobile) */}
                {isMobile && (
                  <Box sx={{ mt: 2 }}>
                    <ContactQuickActions contact={contact} />
                  </Box>
                )}
              </Collapse>
            </Paper>
          ))}
        </Stack>
      )}

      {/* Validation Summary */}
      {Object.keys(contactValidationErrors).length > 0 && (
        <Alert severity="error" sx={{ mt: 2 }}>
          Bitte überprüfen Sie die Kontaktdaten. Es gibt noch Validierungsfehler.
        </Alert>
      )}

      {/* Help Text */}
      {contacts.length > 0 && !primaryContactId && (
        <Alert severity="warning" sx={{ mt: 2 }}>
          Bitte wählen Sie einen Hauptansprechpartner aus.
        </Alert>
      )}

      {/* Contact Form Dialog */}
      <ContactFormDialog
        open={isFormOpen}
        onClose={() => {
          setIsFormOpen(false);
          setEditingContact(null);
        }}
        onSubmit={handleFormSubmit}
        contact={editingContact}
        locations={locations}
      />
    </Box>
  );
};
