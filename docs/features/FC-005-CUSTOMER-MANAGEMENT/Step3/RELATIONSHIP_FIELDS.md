# 💝 Tag 4: Beziehungsebene Implementation

**Datum:** Tag 4 der Step 3 Implementation  
**Fokus:** Persönliche Daten & Relationship Features  
**Ziel:** Beziehungspflege ermöglichen  

## 🧭 Navigation

**← Vorher:** [Multi-Contact UI](./FRONTEND_MULTICONTACT.md)  
**↑ Übersicht:** [Step 3 Guide](./README.md)  
**→ Nächster:** [Testing & Integration](./TESTING_INTEGRATION.md)  

## 🎯 Tagesziel

Beziehungsebene mit:
- Erweiterte Contact Form
- Birthday Reminders
- Conversation Starters
- Personal Notes
- Relationship Warmth (Simplified)

## 💻 Implementation

### 1. Enhanced Contact Form

```typescript
// components/ContactForm.tsx

import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Box,
  Tabs,
  Tab,
  Typography
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { useForm, Controller } from 'react-hook-form';
import { DynamicFieldRenderer } from '../fields/DynamicFieldRenderer';
import { useFieldDefinitions } from '../../hooks/useFieldDefinitions';
import type { Contact } from '../../types/contact.types';

interface ContactFormProps {
  open: boolean;
  contact: Contact | null;
  onClose: () => void;
  onSave: (data: any) => void;
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (
    <div hidden={value !== index}>
      {value === index && <Box sx={{ pt: 2 }}>{children}</Box>}
    </div>
  );
};

export const ContactForm: React.FC<ContactFormProps> = ({
  open,
  contact,
  onClose,
  onSave
}) => {
  const [tabValue, setTabValue] = React.useState(0);
  const { control, handleSubmit, reset, formState: { errors } } = useForm({
    defaultValues: contact || {
      firstName: '',
      lastName: '',
      email: '',
      position: '',
      decisionLevel: 'operational',
      hobbies: [],
      familyStatus: '',
      childrenCount: 0
    }
  });
  
  const { getFieldByKey } = useFieldDefinitions();
  
  React.useEffect(() => {
    if (contact) {
      reset(contact);
    } else {
      reset({
        firstName: '',
        lastName: '',
        email: '',
        position: '',
        decisionLevel: 'operational',
        hobbies: [],
        familyStatus: '',
        childrenCount: 0
      });
    }
  }, [contact, reset]);
  
  const onSubmit = (data: any) => {
    onSave(data);
    reset();
  };
  
  const hobbyOptions = [
    'Golf', 'Tennis', 'Segeln', 'Wein', 'Kochen', 
    'Reisen', 'Kunst', 'Musik', 'Lesen', 'Sport'
  ];
  
  return (
    <Dialog 
      open={open} 
      onClose={onClose}
      maxWidth="md"
      fullWidth
    >
      <DialogTitle>
        {contact ? 'Kontakt bearbeiten' : 'Neuer Ansprechpartner'}
      </DialogTitle>
      
      <form onSubmit={handleSubmit(onSubmit)}>
        <DialogContent>
          <Tabs value={tabValue} onChange={(_, v) => setTabValue(v)}>
            <Tab label="Basisdaten" />
            <Tab label="Beziehungsebene" />
            <Tab label="Notizen" />
          </Tabs>
          
          <TabPanel value={tabValue} index={0}>
            <Grid container spacing={2}>
              <Grid item xs={12} sm={4}>
                <Controller
                  name="salutation"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Anrede</InputLabel>
                      <Select {...field} label="Anrede">
                        <MenuItem value="herr">Herr</MenuItem>
                        <MenuItem value="frau">Frau</MenuItem>
                        <MenuItem value="divers">Divers</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              
              <Grid item xs={12} sm={4}>
                <Controller
                  name="title"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Titel"
                      fullWidth
                      placeholder="Dr., Prof., etc."
                    />
                  )}
                />
              </Grid>
              
              <Grid item xs={12} sm={4}>
                <Controller
                  name="firstName"
                  control={control}
                  rules={{ required: 'Vorname ist erforderlich' }}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Vorname *"
                      fullWidth
                      error={!!errors.firstName}
                      helperText={errors.firstName?.message}
                    />
                  )}
                />
              </Grid>
              
              <Grid item xs={12} sm={4}>
                <Controller
                  name="lastName"
                  control={control}
                  rules={{ required: 'Nachname ist erforderlich' }}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Nachname *"
                      fullWidth
                      error={!!errors.lastName}
                      helperText={errors.lastName?.message}
                    />
                  )}
                />
              </Grid>
              
              <Grid item xs={12} sm={4}>
                <Controller
                  name="position"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Position"
                      fullWidth
                      placeholder="z.B. Geschäftsführer"
                    />
                  )}
                />
              </Grid>
              
              <Grid item xs={12} sm={4}>
                <Controller
                  name="decisionLevel"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Entscheidungsebene</InputLabel>
                      <Select {...field} label="Entscheidungsebene">
                        <MenuItem value="executive">Geschäftsführung</MenuItem>
                        <MenuItem value="manager">Abteilungsleitung</MenuItem>
                        <MenuItem value="operational">Operativ</MenuItem>
                        <MenuItem value="influencer">Beeinflusser</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              
              <Grid item xs={12} sm={6}>
                <Controller
                  name="email"
                  control={control}
                  rules={{ 
                    pattern: {
                      value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                      message: 'Ungültige E-Mail-Adresse'
                    }
                  }}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="E-Mail"
                      fullWidth
                      type="email"
                      error={!!errors.email}
                      helperText={errors.email?.message}
                    />
                  )}
                />
              </Grid>
              
              <Grid item xs={12} sm={3}>
                <Controller
                  name="phone"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Telefon"
                      fullWidth
                      placeholder="+49 30 12345678"
                    />
                  )}
                />
              </Grid>
              
              <Grid item xs={12} sm={3}>
                <Controller
                  name="mobile"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Mobil"
                      fullWidth
                      placeholder="+49 170 1234567"
                    />
                  )}
                />
              </Grid>
            </Grid>
          </TabPanel>
          
          <TabPanel value={tabValue} index={1}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <Typography variant="subtitle2" gutterBottom>
                  Persönliche Informationen für bessere Kundenbeziehungen
                </Typography>
              </Grid>
              
              <Grid item xs={12} sm={6}>
                <Controller
                  name="birthday"
                  control={control}
                  render={({ field }) => (
                    <DatePicker
                      label="Geburtstag"
                      value={field.value ? new Date(field.value) : null}
                      onChange={(date) => field.onChange(date?.toISOString())}
                      slotProps={{
                        textField: {
                          fullWidth: true,
                          helperText: "Für persönliche Glückwünsche"
                        }
                      }}
                    />
                  )}
                />
              </Grid>
              
              <Grid item xs={12} sm={6}>
                <Controller
                  name="familyStatus"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Familienstand</InputLabel>
                      <Select {...field} label="Familienstand">
                        <MenuItem value="">Nicht angegeben</MenuItem>
                        <MenuItem value="single">Ledig</MenuItem>
                        <MenuItem value="married">Verheiratet</MenuItem>
                        <MenuItem value="divorced">Geschieden</MenuItem>
                        <MenuItem value="widowed">Verwitwet</MenuItem>
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
              
              <Grid item xs={12} sm={6}>
                <Controller
                  name="childrenCount"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Anzahl Kinder"
                      type="number"
                      fullWidth
                      inputProps={{ min: 0, max: 10 }}
                    />
                  )}
                />
              </Grid>
              
              <Grid item xs={12}>
                <Controller
                  name="hobbies"
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Hobbys/Interessen</InputLabel>
                      <Box sx={{ mt: 2 }}>
                        {hobbyOptions.map((hobby) => (
                          <Chip
                            key={hobby}
                            label={hobby}
                            onClick={() => {
                              const current = field.value || [];
                              if (current.includes(hobby)) {
                                field.onChange(current.filter(h => h !== hobby));
                              } else {
                                field.onChange([...current, hobby]);
                              }
                            }}
                            color={field.value?.includes(hobby) ? 'primary' : 'default'}
                            sx={{ m: 0.5 }}
                          />
                        ))}
                      </Box>
                    </FormControl>
                  )}
                />
              </Grid>
            </Grid>
          </TabPanel>
          
          <TabPanel value={tabValue} index={2}>
            <Controller
              name="personalNotes"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  label="Persönliche Notizen"
                  multiline
                  rows={8}
                  fullWidth
                  placeholder="z.B. Bevorzugt Telefonate am Vormittag, Vegetarier, Fußballfan von Bayern München..."
                  helperText="Diese Notizen sind nur intern sichtbar"
                />
              )}
            />
          </TabPanel>
        </DialogContent>
        
        <DialogActions>
          <Button onClick={onClose}>Abbrechen</Button>
          <Button type="submit" variant="contained">
            {contact ? 'Speichern' : 'Hinzufügen'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};
```

### 2. Birthday Reminder Component

```typescript
// components/BirthdayReminder.tsx

export const BirthdayReminder: React.FC = () => {
  const { contacts } = useContactStore();
  
  const upcomingBirthdays = React.useMemo(() => {
    const today = new Date();
    const nextWeek = addDays(today, 7);
    
    return contacts
      .filter(contact => contact.birthday)
      .map(contact => ({
        ...contact,
        birthdayDate: new Date(contact.birthday),
        daysUntil: differenceInDays(new Date(contact.birthday), today)
      }))
      .filter(contact => 
        contact.daysUntil >= 0 && contact.daysUntil <= 7
      )
      .sort((a, b) => a.daysUntil - b.daysUntil);
  }, [contacts]);
  
  if (upcomingBirthdays.length === 0) return null;
  
  return (
    <Alert severity="info" sx={{ mb: 2 }}>
      <AlertTitle>🎂 Geburtstage diese Woche</AlertTitle>
      {upcomingBirthdays.map(contact => (
        <Box key={contact.id}>
          <strong>{contact.firstName} {contact.lastName}</strong> - 
          {contact.daysUntil === 0 ? ' Heute!' : ` in ${contact.daysUntil} Tagen`}
        </Box>
      ))}
    </Alert>
  );
};
```

### 3. Warmth Indicator (Simplified)

```typescript
// components/RelationshipWarmth.tsx

export const RelationshipWarmth: React.FC<{contact: Contact}> = ({ contact }) => {
  const getWarmthScore = (): number => {
    let score = 50; // Base score
    
    // Personal data completeness
    if (contact.birthday) score += 10;
    if (contact.hobbies?.length > 0) score += 10;
    if (contact.personalNotes) score += 10;
    
    // Interaction frequency (mock)
    // In real app: calculate from interaction history
    
    return Math.min(score, 100);
  };
  
  const score = getWarmthScore();
  const color = score > 70 ? 'success' : score > 40 ? 'warning' : 'error';
  
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <LinearProgress
        variant="determinate"
        value={score}
        color={color}
        sx={{ width: 100, height: 8, borderRadius: 4 }}
      />
      <Typography variant="caption" color="text.secondary">
        {score}% Beziehungsstärke
      </Typography>
    </Box>
  );
};
```

## 🧪 Tests

```typescript
describe('Relationship Fields', () => {
  it('should save birthday correctly', async () => {
    const { getByLabelText, getByText } = render(<ContactForm />);
    
    const birthdayInput = getByLabelText('Geburtstag');
    await userEvent.type(birthdayInput, '15.03.1980');
    
    await userEvent.click(getByText('Speichern'));
    
    expect(mockOnSave).toHaveBeenCalledWith(
      expect.objectContaining({
        birthday: expect.stringContaining('1980-03-15')
      })
    );
  });
});
```

## 📝 Checkliste

- [ ] Enhanced Contact Form mit Tabs
- [ ] Beziehungsfelder implementiert
- [ ] Birthday Reminder Widget
- [ ] Hobbys Multi-Select
- [ ] Personal Notes Feld
- [ ] Warmth Indicator (Basic)

## 🔗 Nächste Schritte

**Morgen:** [Testing & Integration](./TESTING_INTEGRATION.md) - Alles zusammenführen

---

**Status:** 📋 Bereit zur Implementierung