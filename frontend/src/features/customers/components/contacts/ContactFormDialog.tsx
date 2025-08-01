/**
 * ContactFormDialog Component
 * 
 * Modal dialog for creating and editing contacts.
 * Uses Theme Architecture components for consistency.
 * 
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/FRONTEND_FOUNDATION.md
 */

import React, { useState, useEffect, useMemo } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Divider,
  Alert,
  FormControlLabel,
  Switch,
  Tabs,
  Tab,
  useTheme,
  useMediaQuery
} from '@mui/material';

import { AdaptiveFormContainer } from '../adaptive/AdaptiveFormContainer';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import { LocationCheckboxList } from '../shared/LocationCheckboxList';

import type { Contact, CreateContactDTO } from '../../types/contact.types';
import type { Location } from '../../types/location.types';
import type { FieldDefinition } from '../../types/field.types';
import { 
  contactFieldExtensions,
  getContactFieldsForGroup 
} from '../../data/fieldCatalogContactExtensions';

interface ContactFormDialogProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (contact: Partial<Contact>) => void;
  contact?: Contact | null;
  locations?: Location[];
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`contact-tabpanel-${index}`}
      aria-labelledby={`contact-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ pt: 2 }}>{children}</Box>}
    </div>
  );
}

/**
 * Contact Form Dialog
 * 
 * Multi-tab form for comprehensive contact data entry.
 */
export const ContactFormDialog: React.FC<ContactFormDialogProps> = ({
  open,
  onClose,
  onSubmit,
  contact,
  locations = []
}) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const isEdit = !!contact;
  
  // Form state
  const [formData, setFormData] = useState<Partial<Contact>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [activeTab, setActiveTab] = useState(0);
  
  // Initialize form data
  useEffect(() => {
    if (contact) {
      setFormData(contact);
    } else {
      setFormData({
        salutation: undefined,
        firstName: '',
        lastName: '',
        isPrimary: false,
        isActive: true,
        responsibilityScope: 'all',
        assignedLocationIds: []
      });
    }
    setErrors({});
    setActiveTab(0);
  }, [contact, open]);
  
  // Field groups for tabs
  const fieldGroups = useMemo(() => [
    {
      label: 'Basis',
      fields: getContactFieldsForGroup('basicInfo').concat(
        getContactFieldsForGroup('professionalInfo')
      )
    },
    {
      label: 'Kontakt',
      fields: getContactFieldsForGroup('contactDetails').concat(
        getContactFieldsForGroup('responsibility')
      )
    },
    {
      label: 'Beziehung',
      fields: getContactFieldsForGroup('relationshipData')
    }
  ], []);
  
  // Handle field change
  const handleFieldChange = (fieldKey: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      [fieldKey.replace('contact', '').charAt(0).toLowerCase() + fieldKey.replace('contact', '').slice(1)]: value
    }));
    
    // Clear error for this field
    if (errors[fieldKey]) {
      setErrors(prev => {
        const next = { ...prev };
        delete next[fieldKey];
        return next;
      });
    }
  };
  
  // Handle responsibility scope change
  const handleResponsibilityScopeChange = (scope: 'all' | 'specific') => {
    setFormData(prev => ({
      ...prev,
      responsibilityScope: scope,
      assignedLocationIds: scope === 'all' ? [] : prev.assignedLocationIds || []
    }));
  };
  
  // Handle location assignment
  const handleLocationChange = (locationIds: string[]) => {
    setFormData(prev => ({
      ...prev,
      assignedLocationIds: locationIds
    }));
  };
  
  // Validate form
  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};
    
    // Required fields
    if (!formData.firstName?.trim()) {
      newErrors.contactFirstName = 'Vorname ist erforderlich';
    }
    if (!formData.lastName?.trim()) {
      newErrors.contactLastName = 'Nachname ist erforderlich';
    }
    if (!formData.salutation) {
      newErrors.contactSalutation = 'Anrede ist erforderlich';
    }
    
    // Email validation
    if (formData.email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.email)) {
        newErrors.contactEmail = 'Ungültige E-Mail-Adresse';
      }
    }
    
    // At least one contact method
    if (!formData.email && !formData.phone && !formData.mobile) {
      newErrors.contactEmail = 'Mindestens eine Kontaktmöglichkeit erforderlich';
    }
    
    // Location assignment validation
    if (formData.responsibilityScope === 'specific' && (!formData.assignedLocationIds || formData.assignedLocationIds.length === 0)) {
      newErrors.assignedLocationIds = 'Bitte wählen Sie mindestens einen Standort';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  
  // Handle submit
  const handleSubmit = () => {
    if (validateForm()) {
      onSubmit(formData);
      onClose();
    } else {
      // Switch to tab with first error
      const firstErrorField = Object.keys(errors)[0];
      const errorTabIndex = fieldGroups.findIndex(group =>
        group.fields.some(field => field.key === firstErrorField)
      );
      if (errorTabIndex >= 0) {
        setActiveTab(errorTabIndex);
      }
    }
  };
  
  // Get field value
  const getFieldValue = (fieldKey: string): any => {
    const key = fieldKey.replace('contact', '').charAt(0).toLowerCase() + fieldKey.replace('contact', '').slice(1);
    return formData[key as keyof Contact];
  };
  
  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      fullScreen={isMobile}
    >
      <DialogTitle>
        {isEdit ? 'Kontakt bearbeiten' : 'Neuen Kontakt anlegen'}
      </DialogTitle>
      
      <DialogContent dividers>
        {/* Tabs */}
        <Tabs
          value={activeTab}
          onChange={(_, newValue) => setActiveTab(newValue)}
          variant={isMobile ? 'fullWidth' : 'standard'}
          sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}
        >
          {fieldGroups.map((group, index) => (
            <Tab 
              key={index} 
              label={group.label}
              id={`contact-tab-${index}`}
              aria-controls={`contact-tabpanel-${index}`}
            />
          ))}
        </Tabs>
        
        {/* Tab Panels */}
        {fieldGroups.map((group, index) => (
          <TabPanel key={index} value={activeTab} index={index}>
            <AdaptiveFormContainer>
              <DynamicFieldRenderer
                fields={group.fields}
                values={Object.fromEntries(
                  group.fields.map(field => [
                    field.key,
                    getFieldValue(field.key)
                  ])
                )}
                errors={errors}
                onChange={handleFieldChange}
                onBlur={() => {}}
              />
            </AdaptiveFormContainer>
            
            {/* Special handling for responsibility on Contact tab */}
            {index === 1 && locations.length > 1 && (
              <Box sx={{ mt: 3 }}>
                <Divider sx={{ mb: 2 }} />
                <Typography variant="subtitle2" gutterBottom>
                  Zuständigkeitsbereich
                </Typography>
                
                <Box sx={{ mb: 2 }}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={formData.responsibilityScope === 'specific'}
                        onChange={(e) => handleResponsibilityScopeChange(
                          e.target.checked ? 'specific' : 'all'
                        )}
                      />
                    }
                    label="Nur für bestimmte Standorte zuständig"
                  />
                </Box>
                
                {formData.responsibilityScope === 'specific' && (
                  <>
                    <LocationCheckboxList
                      locations={locations}
                      selectedLocationIds={formData.assignedLocationIds || []}
                      onChange={handleLocationChange}
                    />
                    {errors.assignedLocationIds && (
                      <Alert severity="error" sx={{ mt: 1 }}>
                        {errors.assignedLocationIds}
                      </Alert>
                    )}
                  </>
                )}
              </Box>
            )}
          </TabPanel>
        ))}
        
        {/* Primary Contact Option */}
        <Box sx={{ mt: 3 }}>
          <FormControlLabel
            control={
              <Switch
                checked={formData.isPrimary || false}
                onChange={(e) => setFormData(prev => ({
                  ...prev,
                  isPrimary: e.target.checked
                }))}
              />
            }
            label="Als Hauptansprechpartner festlegen"
          />
        </Box>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>
          Abbrechen
        </Button>
        <Button 
          onClick={handleSubmit} 
          variant="contained"
          disabled={!formData.firstName || !formData.lastName}
        >
          {isEdit ? 'Speichern' : 'Kontakt anlegen'}
        </Button>
      </DialogActions>
    </Dialog>
  );
};