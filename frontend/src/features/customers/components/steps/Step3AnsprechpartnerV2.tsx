/**
 * Step 3: Ansprechpartner V2
 * 
 * Erweiterte Version mit Standort-Zuordnung und mehreren Ansprechpartnern.
 * Dropdown-Felder für Titel und Funktion mit individueller Eingabemöglichkeit.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/wizard/STEP3_ANSPRECHPARTNER_V2.md
 */

import React, { useState, useCallback, useMemo } from 'react';
import { 
  Box, 
  Typography, 
  Button,
  Card,
  CardContent,
  IconButton,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Radio,
  RadioGroup,
  FormControlLabel,
  Checkbox,
  FormGroup,
  Chip,
  Autocomplete
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Person as PersonIcon
} from '@mui/icons-material';
import { useCustomerOnboardingStore } from '../../stores/customerOnboardingStore';
import { AdaptiveFormContainer } from '../adaptive/AdaptiveFormContainer';
import type { Contact } from '../../types/contact.types';

// Vordefinierte Titel-Optionen
const TITLE_OPTIONS = [
  'Dr.',
  'Prof.'
];

// Vordefinierte Positionen/Funktionen
const POSITION_OPTIONS = [
  // Management
  'Geschäftsführer',
  'Direktor',
  'Inhaber',
  'Vorstand',
  
  // Hotel-spezifisch
  'Hoteldirektor',
  'F&B Manager',
  'Küchenchef',
  'Einkaufsleiter',
  'Betriebsleiter',
  
  // Krankenhaus-spezifisch
  'Verwaltungsdirektor',
  'Küchenleitung',
  'Verpflegungsmanager',
  
  // Betriebsrestaurant
  'Kantinenchef',
  'Gastronomiemanager',
  
  // Allgemein
  'Einkäufer',
  'Prokurist',
  'Assistent der Geschäftsführung'
];

interface ContactFormData extends Contact {
  responsibilityScope: 'all' | 'specific';
  assignedLocationIds: string[];
}

export const Step3AnsprechpartnerV2: React.FC = () => {
  const {
    customerData,
    locations,
    setCustomerField
  } = useCustomerOnboardingStore();
  
  // Initialisiere Kontakte aus customerData wenn vorhanden
  const [contacts, setContacts] = useState<ContactFormData[]>(() => {
    if (customerData.contacts && Array.isArray(customerData.contacts)) {
      return customerData.contacts.map((contact: any) => ({
        ...contact,
        responsibilityScope: contact.responsibilityScope || 'all',
        assignedLocationIds: contact.assignedLocationIds || []
      }));
    }
    // Standard: Ein leerer Hauptansprechpartner
    return [{
      id: `contact-${Date.now()}`,
      salutation: 'Herr',
      title: '',
      firstName: '',
      lastName: '',
      position: '',
      email: '',
      phone: '',
      mobile: '',
      isPrimary: true,
      decisionLevel: 'decision_maker',
      responsibilityScope: 'all',
      assignedLocationIds: []
    }];
  });
  
  const isSingleLocation = locations.length <= 1;
  
  // Update customerData wenn contacts sich ändern
  const updateCustomerContacts = useCallback((updatedContacts: ContactFormData[]) => {
    setCustomerField('contacts', updatedContacts);
  }, [setCustomerField]);
  
  const handleContactChange = useCallback((index: number, field: keyof ContactFormData, value: any) => {
    const newContacts = [...contacts];
    newContacts[index] = {
      ...newContacts[index],
      [field]: value
    };
    setContacts(newContacts);
    updateCustomerContacts(newContacts);
  }, [contacts, updateCustomerContacts]);
  
  const handleResponsibilityChange = useCallback((index: number, scope: 'all' | 'specific', locationIds?: string[]) => {
    const newContacts = [...contacts];
    newContacts[index] = {
      ...newContacts[index],
      responsibilityScope: scope,
      assignedLocationIds: scope === 'specific' ? (locationIds || []) : []
    };
    setContacts(newContacts);
    updateCustomerContacts(newContacts);
  }, [contacts, updateCustomerContacts]);
  
  const addContact = useCallback(() => {
    const newContact: ContactFormData = {
      id: `contact-${Date.now()}`,
      salutation: 'Herr',
      title: '',
      firstName: '',
      lastName: '',
      position: '',
      email: '',
      phone: '',
      mobile: '',
      isPrimary: contacts.length === 0,
      decisionLevel: 'influencer',
      responsibilityScope: 'all',
      assignedLocationIds: []
    };
    const newContacts = [...contacts, newContact];
    setContacts(newContacts);
    updateCustomerContacts(newContacts);
  }, [contacts, updateCustomerContacts]);
  
  const removeContact = useCallback((index: number) => {
    const newContacts = contacts.filter((_, i) => i !== index);
    // Wenn der gelöschte Kontakt der Primary war, mache den ersten zum Primary
    if (contacts[index].isPrimary && newContacts.length > 0) {
      newContacts[0].isPrimary = true;
    }
    setContacts(newContacts);
    updateCustomerContacts(newContacts);
  }, [contacts, updateCustomerContacts]);
  
  const getContactDisplayName = (contact: ContactFormData) => {
    const parts = [];
    if (contact.salutation) parts.push(contact.salutation);
    if (contact.title) parts.push(contact.title);
    if (contact.firstName) parts.push(contact.firstName);
    if (contact.lastName) parts.push(contact.lastName);
    return parts.join(' ') || 'Neuer Ansprechpartner';
  };
  
  return (
    <Box>
      <Typography variant="h5" component="h2" gutterBottom>
        Schritt 3: Ansprechpartner
      </Typography>
      
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Erfassen Sie die wichtigsten Ansprechpartner für eine erfolgreiche Zusammenarbeit.
      </Typography>
      
      {contacts.map((contact, index) => (
        <Card key={contact.id} sx={{ mb: 3 }}>
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <PersonIcon color="primary" />
                <Typography variant="h6">
                  {contact.isPrimary ? '👤 Hauptansprechpartner' : `Ansprechpartner ${index + 1}`}
                </Typography>
                {contact.isPrimary && (
                  <Chip label="Primär" color="primary" size="small" />
                )}
              </Box>
              {!contact.isPrimary && (
                <IconButton 
                  onClick={() => removeContact(index)}
                  color="error"
                  size="small"
                >
                  <DeleteIcon />
                </IconButton>
              )}
            </Box>
            
            <AdaptiveFormContainer>
              {/* Name Fields */}
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                <FormControl size="small" sx={{ minWidth: 100 }}>
                  <InputLabel>Anrede</InputLabel>
                  <Select
                    value={contact.salutation}
                    onChange={(e) => handleContactChange(index, 'salutation', e.target.value)}
                    label="Anrede"
                  >
                    <MenuItem value="Herr">Herr</MenuItem>
                    <MenuItem value="Frau">Frau</MenuItem>
                    <MenuItem value="Divers">Divers</MenuItem>
                  </Select>
                </FormControl>
                
                <Autocomplete
                  value={contact.title}
                  onChange={(_, newValue) => handleContactChange(index, 'title', newValue || '')}
                  onInputChange={(_, newValue) => handleContactChange(index, 'title', newValue)}
                  options={TITLE_OPTIONS}
                  freeSolo
                  size="small"
                  sx={{ minWidth: 120, maxWidth: 200 }}
                  renderInput={(params) => (
                    <TextField {...params} label="Titel" placeholder="Dr., Prof." />
                  )}
                />
                
                <TextField
                  label="Vorname"
                  value={contact.firstName}
                  onChange={(e) => handleContactChange(index, 'firstName', e.target.value)}
                  required
                  size="small"
                  sx={{ flex: 1, minWidth: 150 }}
                />
                
                <TextField
                  label="Nachname"
                  value={contact.lastName}
                  onChange={(e) => handleContactChange(index, 'lastName', e.target.value)}
                  required
                  size="small"
                  sx={{ flex: 1, minWidth: 150 }}
                />
              </Box>
              
              {/* Position und Entscheider-Ebene */}
              <Box sx={{ display: 'flex', gap: 2, mt: 2, flexWrap: 'wrap' }}>
                <Autocomplete
                  value={contact.position}
                  onChange={(_, newValue) => handleContactChange(index, 'position', newValue || '')}
                  onInputChange={(_, newValue) => handleContactChange(index, 'position', newValue)}
                  options={POSITION_OPTIONS}
                  freeSolo
                  size="small"
                  sx={{ flex: 1, minWidth: 250, maxWidth: 400 }}
                  renderInput={(params) => (
                    <TextField 
                      {...params} 
                      label="Position/Funktion" 
                      placeholder="z.B. Geschäftsführer, Einkaufsleiter"
                      required
                    />
                  )}
                />
                
                <FormControl size="small" sx={{ minWidth: 200 }}>
                  <InputLabel>Entscheider-Ebene</InputLabel>
                  <Select
                    value={contact.decisionLevel}
                    onChange={(e) => handleContactChange(index, 'decisionLevel', e.target.value)}
                    label="Entscheider-Ebene"
                  >
                    <MenuItem value="decision_maker">Entscheider</MenuItem>
                    <MenuItem value="influencer">Beeinflusser</MenuItem>
                    <MenuItem value="gatekeeper">Gatekeeper</MenuItem>
                    <MenuItem value="user">Nutzer</MenuItem>
                  </Select>
                </FormControl>
              </Box>
              
              {/* Kontaktdaten */}
              <Box sx={{ display: 'flex', gap: 2, mt: 2, flexWrap: 'wrap' }}>
                <TextField
                  label="E-Mail"
                  type="email"
                  value={contact.email}
                  onChange={(e) => handleContactChange(index, 'email', e.target.value)}
                  required
                  size="small"
                  sx={{ flex: 1, minWidth: 250 }}
                />
                
                <TextField
                  label="Telefon"
                  value={contact.phone}
                  onChange={(e) => handleContactChange(index, 'phone', e.target.value)}
                  size="small"
                  sx={{ flex: 1, minWidth: 200 }}
                  placeholder="+49 30 123456"
                />
                
                <TextField
                  label="Mobil"
                  value={contact.mobile}
                  onChange={(e) => handleContactChange(index, 'mobile', e.target.value)}
                  size="small"
                  sx={{ flex: 1, minWidth: 200 }}
                  placeholder="+49 170 123456"
                />
              </Box>
              
              {/* Zuständigkeitsbereich - nur bei mehreren Standorten */}
              {!isSingleLocation && locations.length > 0 && (
                <Box sx={{ mt: 3, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
                  <Typography variant="subtitle2" gutterBottom>
                    📍 Zuständigkeitsbereich:
                  </Typography>
                  
                  <RadioGroup
                    value={contact.responsibilityScope}
                    onChange={(e) => handleResponsibilityChange(
                      index, 
                      e.target.value as 'all' | 'specific'
                    )}
                  >
                    <FormControlLabel
                      value="all"
                      control={<Radio />}
                      label="Für alle Standorte"
                    />
                    <FormControlLabel
                      value="specific"
                      control={<Radio />}
                      label="Für bestimmte Standorte:"
                    />
                  </RadioGroup>
                  
                  {contact.responsibilityScope === 'specific' && (
                    <Box sx={{ ml: 4, mt: 1 }}>
                      <FormGroup>
                        {locations.map((location) => (
                          <FormControlLabel
                            key={location.id}
                            control={
                              <Checkbox
                                checked={contact.assignedLocationIds.includes(location.id)}
                                onChange={(e) => {
                                  const newIds = e.target.checked
                                    ? [...contact.assignedLocationIds, location.id]
                                    : contact.assignedLocationIds.filter(id => id !== location.id);
                                  handleResponsibilityChange(index, 'specific', newIds);
                                }}
                              />
                            }
                            label={`${location.name}${location.city ? ` (${location.city})` : ''}`}
                          />
                        ))}
                      </FormGroup>
                      {contact.assignedLocationIds.length === 0 && (
                        <Typography variant="caption" color="error" sx={{ mt: 1, display: 'block' }}>
                          Bitte wählen Sie mindestens einen Standort aus.
                        </Typography>
                      )}
                    </Box>
                  )}
                </Box>
              )}
            </AdaptiveFormContainer>
            
            {/* Contact Display Preview */}
            {(contact.firstName || contact.lastName) && (
              <Card variant="outlined" sx={{ mt: 2, bgcolor: 'primary.light' }}>
                <CardContent>
                  <Typography variant="body2" color="text.secondary">
                    Erfasster Kontakt:
                  </Typography>
                  <Typography variant="subtitle1" fontWeight="bold">
                    {getContactDisplayName(contact)}
                  </Typography>
                  {contact.position && (
                    <Typography variant="body2">
                      {contact.position}
                    </Typography>
                  )}
                  {contact.email && (
                    <Typography variant="body2" color="primary">
                      {contact.email}
                    </Typography>
                  )}
                </CardContent>
              </Card>
            )}
          </CardContent>
        </Card>
      ))}
      
      <Button
        variant="outlined"
        startIcon={<AddIcon />}
        onClick={addContact}
        sx={{ mb: 3 }}
      >
        Weiteren Ansprechpartner hinzufügen
      </Button>
      
      {/* Summary */}
      <Card sx={{ bgcolor: 'info.light' }}>
        <CardContent>
          <Typography variant="body2">
            💡 <strong>Tipp:</strong> Bei mehreren Ansprechpartnern kann die Zuständigkeit 
            nach Standorten aufgeteilt werden. Dies hilft bei der gezielten Kommunikation.
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
};