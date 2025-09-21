# 🎴 Tag 3: Multi-Contact UI

**Datum:** Tag 3 der Step 3 Implementation  
**Fokus:** Contact Cards & Management UI  
**Ziel:** Intuitive Multi-Contact Verwaltung  

## 🧭 Navigation

**← Vorher:** [Frontend Foundation](./FRONTEND_FOUNDATION.md)  
**↑ Übersicht:** [Step 3 Guide](./README.md)  
**→ Nächster:** [Relationship Fields](./RELATIONSHIP_FIELDS.md)  

## 🎯 Tagesziel

Multi-Contact UI mit:
- Contact Cards
- Add/Edit/Delete Funktionen
- Primary Contact Toggle
- Location Assignment
- Responsive Design

## 💻 Implementation

### 1. Contact Card Component

```typescript
// components/ContactCard.tsx

import React from 'react';
import {
  Card,
  CardContent,
  CardActions,
  Typography,
  Box,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  Avatar
} from '@mui/material';
import {
  MoreVert as MoreVertIcon,
  Star as StarIcon,
  StarBorder as StarBorderIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  LocationOn as LocationIcon,
  Phone as PhoneIcon,
  Email as EmailIcon
} from '@mui/icons-material';
import type { Contact } from '../types/contact.types';

interface ContactCardProps {
  contact: Contact;
  onEdit: (contact: Contact) => void;
  onDelete: (contactId: string) => void;
  onSetPrimary: (contactId: string) => void;
  onAssignLocation: (contactId: string) => void;
}

export const ContactCard: React.FC<ContactCardProps> = ({
  contact,
  onEdit,
  onDelete,
  onSetPrimary,
  onAssignLocation
}) => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  
  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };
  
  const handleMenuClose = () => {
    setAnchorEl(null);
  };
  
  const getInitials = () => {
    return `${contact.firstName[0]}${contact.lastName[0]}`.toUpperCase();
  };
  
  const getDecisionLevelColor = () => {
    const colors = {
      executive: 'error',
      manager: 'warning',
      operational: 'info',
      influencer: 'default'
    };
    return colors[contact.decisionLevel || 'influencer'] as any;
  };
  
  return (
    <Card 
      sx={{ 
        mb: 2,
        border: contact.isPrimary ? '2px solid' : '1px solid',
        borderColor: contact.isPrimary ? 'primary.main' : 'divider',
        position: 'relative',
        transition: 'all 0.3s ease',
        '&:hover': {
          boxShadow: 3,
          transform: 'translateY(-2px)'
        }
      }}
    >
      {contact.isPrimary && (
        <Chip
          icon={<StarIcon />}
          label="Hauptkontakt"
          color="primary"
          size="small"
          sx={{
            position: 'absolute',
            top: 8,
            right: 8
          }}
        />
      )}
      
      <CardContent>
        <Box display="flex" alignItems="flex-start" gap={2}>
          <Avatar sx={{ bgcolor: 'primary.main', width: 56, height: 56 }}>
            {getInitials()}
          </Avatar>
          
          <Box flex={1}>
            <Typography variant="h6" component="div">
              {contact.salutation} {contact.title} {contact.firstName} {contact.lastName}
            </Typography>
            
            <Typography color="text.secondary" gutterBottom>
              {contact.position}
            </Typography>
            
            {contact.decisionLevel && (
              <Chip
                label={contact.decisionLevel}
                size="small"
                color={getDecisionLevelColor()}
                sx={{ mt: 1 }}
              />
            )}
            
            <Box mt={2} display="flex" flexDirection="column" gap={1}>
              {contact.email && (
                <Box display="flex" alignItems="center" gap={1}>
                  <EmailIcon fontSize="small" color="action" />
                  <Typography variant="body2">{contact.email}</Typography>
                </Box>
              )}
              
              {contact.phone && (
                <Box display="flex" alignItems="center" gap={1}>
                  <PhoneIcon fontSize="small" color="action" />
                  <Typography variant="body2">{contact.phone}</Typography>
                </Box>
              )}
              
              {contact.assignedLocationName && (
                <Box display="flex" alignItems="center" gap={1}>
                  <LocationIcon fontSize="small" color="action" />
                  <Typography variant="body2">
                    Zuständig für: {contact.assignedLocationName}
                  </Typography>
                </Box>
              )}
            </Box>
          </Box>
          
          <IconButton onClick={handleMenuOpen}>
            <MoreVertIcon />
          </IconButton>
        </Box>
      </CardContent>
      
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={() => {
          onEdit(contact);
          handleMenuClose();
        }}>
          <EditIcon fontSize="small" sx={{ mr: 1 }} />
          Bearbeiten
        </MenuItem>
        
        {!contact.isPrimary && (
          <MenuItem onClick={() => {
            onSetPrimary(contact.id);
            handleMenuClose();
          }}>
            <StarBorderIcon fontSize="small" sx={{ mr: 1 }} />
            Als Hauptkontakt
          </MenuItem>
        )}
        
        <MenuItem onClick={() => {
          onAssignLocation(contact.id);
          handleMenuClose();
        }}>
          <LocationIcon fontSize="small" sx={{ mr: 1 }} />
          Filiale zuordnen
        </MenuItem>
        
        <MenuItem 
          onClick={() => {
            onDelete(contact.id);
            handleMenuClose();
          }}
          sx={{ color: 'error.main' }}
        >
          <DeleteIcon fontSize="small" sx={{ mr: 1 }} />
          Löschen
        </MenuItem>
      </Menu>
    </Card>
  );
};
```

### 2. Main Component Update

```typescript
// components/steps/Step3AnsprechpartnerV5.tsx

import React, { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Grid,
  Alert,
  Skeleton,
  Fab,
  Dialog
} from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { useContactStore } from '../../stores/contactStore';
import { ContactCard } from '../ContactCard';
import { ContactForm } from '../ContactForm';
import { LocationAssignmentDialog } from '../LocationAssignmentDialog';
import type { Contact } from '../../types/contact.types';

export const Step3AnsprechpartnerV5: React.FC = () => {
  const { customerData } = useCustomerOnboardingStore();
  const { 
    contacts, 
    loading, 
    error,
    loadContacts,
    addContact,
    updateContact,
    deleteContact,
    setPrimaryContact,
    assignToLocation
  } = useContactStore();
  
  const [formOpen, setFormOpen] = useState(false);
  const [editingContact, setEditingContact] = useState<Contact | null>(null);
  const [locationDialogOpen, setLocationDialogOpen] = useState(false);
  const [selectedContactId, setSelectedContactId] = useState<string | null>(null);
  
  useEffect(() => {
    if (customerData.id) {
      loadContacts(customerData.id);
    }
  }, [customerData.id]);
  
  const handleAddContact = () => {
    setEditingContact(null);
    setFormOpen(true);
  };
  
  const handleEditContact = (contact: Contact) => {
    setEditingContact(contact);
    setFormOpen(true);
  };
  
  const handleSaveContact = async (data: any) => {
    try {
      if (editingContact) {
        await updateContact(editingContact.id, data);
      } else {
        await addContact(customerData.id, data);
      }
      setFormOpen(false);
    } catch (error) {
      // Error handling
    }
  };
  
  const handleDeleteContact = async (contactId: string) => {
    if (confirm('Möchten Sie diesen Kontakt wirklich löschen?')) {
      await deleteContact(contactId);
    }
  };
  
  const handleAssignLocation = (contactId: string) => {
    setSelectedContactId(contactId);
    setLocationDialogOpen(true);
  };
  
  if (loading) {
    return (
      <Box>
        <Typography variant="h5" gutterBottom>
          Ansprechpartner
        </Typography>
        <Grid container spacing={2}>
          {[1, 2, 3].map(i => (
            <Grid item xs={12} md={6} lg={4} key={i}>
              <Skeleton variant="rectangular" height={200} />
            </Grid>
          ))}
        </Grid>
      </Box>
    );
  }
  
  return (
    <Box>
      <Box mb={3}>
        <Typography variant="h5" gutterBottom>
          Ansprechpartner
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Verwalten Sie die Kontakte für {customerData.companyName}
        </Typography>
      </Box>
      
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}
      
      {contacts.length === 0 ? (
        <Box 
          sx={{ 
            textAlign: 'center', 
            py: 8,
            bgcolor: 'background.paper',
            borderRadius: 2,
            border: '2px dashed',
            borderColor: 'divider'
          }}
        >
          <Typography variant="h6" gutterBottom>
            Noch keine Ansprechpartner vorhanden
          </Typography>
          <Typography variant="body2" color="text.secondary" paragraph>
            Fügen Sie den ersten Kontakt hinzu
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleAddContact}
          >
            Ansprechpartner hinzufügen
          </Button>
        </Box>
      ) : (
        <>
          <Grid container spacing={2}>
            {contacts.map(contact => (
              <Grid item xs={12} md={6} lg={4} key={contact.id}>
                <ContactCard
                  contact={contact}
                  onEdit={handleEditContact}
                  onDelete={handleDeleteContact}
                  onSetPrimary={setPrimaryContact}
                  onAssignLocation={handleAssignLocation}
                />
              </Grid>
            ))}
          </Grid>
          
          <Fab
            color="primary"
            aria-label="add contact"
            sx={{
              position: 'fixed',
              bottom: 24,
              right: 24
            }}
            onClick={handleAddContact}
          >
            <AddIcon />
          </Fab>
        </>
      )}
      
      <ContactForm
        open={formOpen}
        contact={editingContact}
        onClose={() => setFormOpen(false)}
        onSave={handleSaveContact}
      />
      
      <LocationAssignmentDialog
        open={locationDialogOpen}
        contactId={selectedContactId}
        onClose={() => setLocationDialogOpen(false)}
        onAssign={async (locationId) => {
          if (selectedContactId) {
            await assignToLocation(selectedContactId, locationId);
          }
          setLocationDialogOpen(false);
        }}
      />
    </Box>
  );
};
```

### 3. Responsive Grid System

```typescript
// hooks/useResponsiveGrid.ts

export const useResponsiveGrid = () => {
  const theme = useTheme();
  const isXs = useMediaQuery(theme.breakpoints.down('sm'));
  const isSm = useMediaQuery(theme.breakpoints.down('md'));
  const isMd = useMediaQuery(theme.breakpoints.down('lg'));
  
  const getGridColumns = () => {
    if (isXs) return 1;
    if (isSm) return 2;
    if (isMd) return 3;
    return 4;
  };
  
  return {
    columns: getGridColumns(),
    spacing: isXs ? 1 : 2
  };
};
```

## 🧪 UI Tests

```typescript
describe('Multi-Contact UI', () => {
  it('should display contact cards correctly', () => {
    render(<Step3AnsprechpartnerV5 />);
    
    expect(screen.getByText('Max Mustermann')).toBeInTheDocument();
    expect(screen.getByText('Hauptkontakt')).toBeInTheDocument();
  });
  
  it('should open form on add button click', async () => {
    render(<Step3AnsprechpartnerV5 />);
    
    const addButton = screen.getByLabelText('add contact');
    await userEvent.click(addButton);
    
    expect(screen.getByText('Neuer Ansprechpartner')).toBeInTheDocument();
  });
});
```

## 📝 Checkliste

- [ ] Contact Card Component erstellt
- [ ] Multi-Contact Layout implementiert
- [ ] Add/Edit/Delete funktioniert
- [ ] Primary Contact Toggle
- [ ] Location Assignment Dialog
- [ ] Responsive Design getestet

## 🔗 Nächste Schritte

**Morgen:** [Relationship Fields](./RELATIONSHIP_FIELDS.md) - Beziehungsebene implementieren

---

**Status:** 📋 Bereit zur Implementierung