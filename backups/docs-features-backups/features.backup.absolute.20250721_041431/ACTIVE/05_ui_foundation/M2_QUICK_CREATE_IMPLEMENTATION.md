# ⚡ M2 Quick Create - Vollständige Implementation

**Feature Code:** M2  
**Feature-Typ:** 🎨 FRONTEND  
**Geschätzter Aufwand:** 3-4 Tage  
**Priorität:** HIGH - Nach M4 Opportunity Pipeline  

---

## 🏗️ ARCHITEKTUR

```
MainLayoutV2 (Container)
    ├── QuickCreateFAB (Floating Action Button)
    │   ├── SpeedDial mit 2 Aktionen
    │   └── Context-aware Visibility
    └── QuickCreateModal
        ├── CustomerQuickForm
        └── OpportunityQuickForm
```

---

## 📦 KOMPONENTEN-IMPLEMENTATION

### 1. Quick Create Modal System

```typescript
// components/common/QuickCreateModal.tsx
import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Stepper,
  Step,
  StepLabel,
  Box,
  IconButton
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { CustomerQuickForm } from '../forms/CustomerQuickForm';
import { OpportunityQuickForm } from '../forms/OpportunityQuickForm';
import { useQuickCreateContext } from '../../hooks/useQuickCreateContext';

interface QuickCreateModalProps {
  open: boolean;
  type: 'customer' | 'opportunity' | null;
  onClose: () => void;
  onSuccess: (data: any) => void;
}

export const QuickCreateModal: React.FC<QuickCreateModalProps> = ({
  open,
  type,
  onClose,
  onSuccess
}) => {
  const { getSmartDefaults, currentContext } = useQuickCreateContext();
  const [activeStep, setActiveStep] = useState(0);
  const [formData, setFormData] = useState<any>({});

  // Smart Defaults basierend auf Kontext
  useEffect(() => {
    if (open && type) {
      const defaults = getSmartDefaults(type, currentContext);
      setFormData(defaults);
    }
  }, [open, type, currentContext]);

  const handleSubmit = async (data: any) => {
    try {
      let result;
      if (type === 'customer') {
        result = await createCustomer(data);
      } else {
        result = await createOpportunity(data);
      }
      
      // Success-Handling mit Smart Navigation
      onSuccess(result);
      
      // Toast Notification
      showSuccessToast(`${type === 'customer' ? 'Kunde' : 'Opportunity'} erstellt!`);
      
      // Optional: Direkt zur Detail-Seite navigieren
      if (currentContext.autoNavigate) {
        navigate(`/${type}s/${result.id}`);
      }
    } catch (error) {
      showErrorToast('Fehler beim Erstellen');
    }
  };

  return (
    <Dialog 
      open={open} 
      onClose={onClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: { minHeight: '400px' }
      }}
    >
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          {type === 'customer' ? 'Neuer Kunde' : 'Neue Opportunity'}
          <IconButton onClick={onClose} size="small">
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>
      
      <DialogContent>
        {type === 'customer' ? (
          <CustomerQuickForm 
            initialData={formData}
            onSubmit={handleSubmit}
          />
        ) : (
          <OpportunityQuickForm 
            initialData={formData}
            onSubmit={handleSubmit}
            context={currentContext}
          />
        )}
      </DialogContent>
    </Dialog>
  );
};
```

### 2. Customer Quick Form

```typescript
// components/forms/CustomerQuickForm.tsx
import React from 'react';
import { useForm, Controller } from 'react-hook-form';
import {
  TextField,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Box,
  Button,
  InputAdornment
} from '@mui/material';
import { Business, Email, Phone, Person } from '@mui/icons-material';

interface CustomerQuickFormData {
  name: string;
  email: string;
  phone: string;
  industry?: string;
  contactFirstName?: string;
  contactLastName?: string;
}

export const CustomerQuickForm: React.FC<{
  initialData: Partial<CustomerQuickFormData>;
  onSubmit: (data: CustomerQuickFormData) => void;
}> = ({ initialData, onSubmit }) => {
  const { control, handleSubmit, formState: { errors } } = useForm({
    defaultValues: {
      name: '',
      email: '',
      phone: '',
      industry: '',
      contactFirstName: '',
      contactLastName: '',
      ...initialData
    }
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Grid container spacing={3}>
        {/* Firmenname - Pflichtfeld */}
        <Grid item xs={12}>
          <Controller
            name="name"
            control={control}
            rules={{ required: 'Firmenname ist erforderlich' }}
            render={({ field }) => (
              <TextField
                {...field}
                fullWidth
                label="Firmenname"
                error={!!errors.name}
                helperText={errors.name?.message}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Business />
                    </InputAdornment>
                  ),
                }}
                autoFocus
              />
            )}
          />
        </Grid>

        {/* Kontaktperson */}
        <Grid item xs={6}>
          <Controller
            name="contactFirstName"
            control={control}
            render={({ field }) => (
              <TextField
                {...field}
                fullWidth
                label="Vorname Kontakt"
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Person />
                    </InputAdornment>
                  ),
                }}
              />
            )}
          />
        </Grid>
        
        <Grid item xs={6}>
          <Controller
            name="contactLastName"
            control={control}
            render={({ field }) => (
              <TextField
                {...field}
                fullWidth
                label="Nachname Kontakt"
              />
            )}
          />
        </Grid>

        {/* E-Mail - Pflichtfeld */}
        <Grid item xs={12}>
          <Controller
            name="email"
            control={control}
            rules={{ 
              required: 'E-Mail ist erforderlich',
              pattern: {
                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                message: 'Ungültige E-Mail-Adresse'
              }
            }}
            render={({ field }) => (
              <TextField
                {...field}
                fullWidth
                label="E-Mail"
                type="email"
                error={!!errors.email}
                helperText={errors.email?.message}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Email />
                    </InputAdornment>
                  ),
                }}
              />
            )}
          />
        </Grid>

        {/* Telefon */}
        <Grid item xs={12}>
          <Controller
            name="phone"
            control={control}
            render={({ field }) => (
              <TextField
                {...field}
                fullWidth
                label="Telefon"
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Phone />
                    </InputAdornment>
                  ),
                }}
              />
            )}
          />
        </Grid>

        {/* Branche */}
        <Grid item xs={12}>
          <Controller
            name="industry"
            control={control}
            render={({ field }) => (
              <FormControl fullWidth>
                <InputLabel>Branche</InputLabel>
                <Select {...field} label="Branche">
                  <MenuItem value="">Keine Angabe</MenuItem>
                  <MenuItem value="gastronomie">Gastronomie</MenuItem>
                  <MenuItem value="einzelhandel">Einzelhandel</MenuItem>
                  <MenuItem value="grosshandel">Großhandel</MenuItem>
                  <MenuItem value="produktion">Produktion</MenuItem>
                  <MenuItem value="dienstleistung">Dienstleistung</MenuItem>
                  <MenuItem value="it">IT</MenuItem>
                </Select>
              </FormControl>
            )}
          />
        </Grid>

        {/* Submit Button */}
        <Grid item xs={12}>
          <Box display="flex" justifyContent="flex-end" gap={2} mt={2}>
            <Button variant="outlined" onClick={() => window.history.back()}>
              Abbrechen
            </Button>
            <Button 
              type="submit" 
              variant="contained" 
              color="primary"
            >
              Kunde erstellen
            </Button>
          </Box>
        </Grid>
      </Grid>
    </form>
  );
};
```

### 3. Opportunity Quick Form

```typescript
// components/forms/OpportunityQuickForm.tsx
import React, { useState, useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import {
  TextField,
  Grid,
  Autocomplete,
  Box,
  Button,
  InputAdornment,
  Chip
} from '@mui/material';
import { Euro, TrendingUp } from '@mui/icons-material';
import { useCustomerSearch } from '../../hooks/useCustomerSearch';

interface OpportunityQuickFormData {
  title: string;
  customerId: string;
  value: number;
  expectedCloseDate: string;
  notes?: string;
}

export const OpportunityQuickForm: React.FC<{
  initialData: Partial<OpportunityQuickFormData>;
  onSubmit: (data: OpportunityQuickFormData) => void;
  context: any;
}> = ({ initialData, onSubmit, context }) => {
  const { control, handleSubmit, formState: { errors }, watch, setValue } = useForm({
    defaultValues: {
      title: '',
      customerId: context?.customerId || '',
      value: 0,
      expectedCloseDate: getDefaultCloseDate(),
      notes: '',
      ...initialData
    }
  });

  const { searchCustomers, customers, loading } = useCustomerSearch();
  const [selectedCustomer, setSelectedCustomer] = useState(null);

  // Wenn aus Customer-Detail geöffnet, Customer vorauswählen
  useEffect(() => {
    if (context?.customerId && context?.customerName) {
      setSelectedCustomer({
        id: context.customerId,
        name: context.customerName
      });
    }
  }, [context]);

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Grid container spacing={3}>
        {/* Opportunity Titel */}
        <Grid item xs={12}>
          <Controller
            name="title"
            control={control}
            rules={{ required: 'Titel ist erforderlich' }}
            render={({ field }) => (
              <TextField
                {...field}
                fullWidth
                label="Opportunity Titel"
                placeholder="z.B. Q2 2025 Liefervertrag"
                error={!!errors.title}
                helperText={errors.title?.message}
                autoFocus={!context?.customerId}
              />
            )}
          />
        </Grid>

        {/* Kunde - mit Autocomplete */}
        <Grid item xs={12}>
          <Controller
            name="customerId"
            control={control}
            rules={{ required: 'Kunde ist erforderlich' }}
            render={({ field }) => (
              <Autocomplete
                value={selectedCustomer}
                onChange={(event, newValue) => {
                  setSelectedCustomer(newValue);
                  field.onChange(newValue?.id || '');
                }}
                onInputChange={(event, newInputValue) => {
                  if (newInputValue.length > 2) {
                    searchCustomers(newInputValue);
                  }
                }}
                options={customers}
                getOptionLabel={(option) => option.name || ''}
                loading={loading}
                disabled={!!context?.customerId}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Kunde"
                    error={!!errors.customerId}
                    helperText={errors.customerId?.message}
                    required
                  />
                )}
                renderOption={(props, option) => (
                  <Box component="li" {...props}>
                    <Box>
                      <strong>{option.name}</strong>
                      {option.city && (
                        <Typography variant="caption" display="block">
                          {option.city}
                        </Typography>
                      )}
                    </Box>
                  </Box>
                )}
              />
            )}
          />
        </Grid>

        {/* Wert */}
        <Grid item xs={6}>
          <Controller
            name="value"
            control={control}
            rules={{ 
              required: 'Wert ist erforderlich',
              min: { value: 1, message: 'Wert muss größer als 0 sein' }
            }}
            render={({ field }) => (
              <TextField
                {...field}
                fullWidth
                label="Geschätzter Wert"
                type="number"
                error={!!errors.value}
                helperText={errors.value?.message}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <Euro />
                    </InputAdornment>
                  ),
                }}
              />
            )}
          />
        </Grid>

        {/* Voraussichtlicher Abschluss */}
        <Grid item xs={6}>
          <Controller
            name="expectedCloseDate"
            control={control}
            rules={{ required: 'Abschlussdatum ist erforderlich' }}
            render={({ field }) => (
              <TextField
                {...field}
                fullWidth
                label="Voraussichtlicher Abschluss"
                type="date"
                error={!!errors.expectedCloseDate}
                helperText={errors.expectedCloseDate?.message}
                InputLabelProps={{ shrink: true }}
              />
            )}
          />
        </Grid>

        {/* Quick Tags für häufige Opportunity-Typen */}
        <Grid item xs={12}>
          <Box display="flex" gap={1} flexWrap="wrap">
            <Chip 
              label="Neukundengeschäft" 
              onClick={() => setValue('title', 'Neukundengeschäft ' + new Date().getFullYear())}
              variant="outlined"
              size="small"
            />
            <Chip 
              label="Vertragsverlängerung" 
              onClick={() => setValue('title', 'Vertragsverlängerung ' + (selectedCustomer?.name || ''))}
              variant="outlined"
              size="small"
            />
            <Chip 
              label="Zusatzauftrag" 
              onClick={() => setValue('title', 'Zusatzauftrag ' + (selectedCustomer?.name || ''))}
              variant="outlined"
              size="small"
            />
          </Box>
        </Grid>

        {/* Notizen */}
        <Grid item xs={12}>
          <Controller
            name="notes"
            control={control}
            render={({ field }) => (
              <TextField
                {...field}
                fullWidth
                label="Notizen (optional)"
                multiline
                rows={2}
                placeholder="z.B. Entscheider ist Herr Müller, Budget bereits freigegeben"
              />
            )}
          />
        </Grid>

        {/* Submit */}
        <Grid item xs={12}>
          <Box display="flex" justifyContent="flex-end" gap={2} mt={2}>
            <Button variant="outlined" onClick={() => window.history.back()}>
              Abbrechen
            </Button>
            <Button 
              type="submit" 
              variant="contained" 
              color="primary"
              startIcon={<TrendingUp />}
            >
              Opportunity erstellen
            </Button>
          </Box>
        </Grid>
      </Grid>
    </form>
  );
};

// Helper Funktion
function getDefaultCloseDate() {
  const date = new Date();
  date.setMonth(date.getMonth() + 1); // Default: 1 Monat in der Zukunft
  return date.toISOString().split('T')[0];
}
```

### 4. Context Hook für Smart Defaults

```typescript
// hooks/useQuickCreateContext.tsx
import { useLocation, useParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export const useQuickCreateContext = () => {
  const location = useLocation();
  const params = useParams();
  const { user } = useAuth();
  
  const getSmartDefaults = (type: 'customer' | 'opportunity', context: any) => {
    const defaults: any = {};
    
    // Basis-Defaults
    if (type === 'customer') {
      defaults.industry = 'gastronomie'; // Häufigste Branche
      defaults.assignedTo = user?.id;
    } else if (type === 'opportunity') {
      defaults.stage = 'LEAD';
      defaults.probability = 10;
      defaults.owner = user?.id;
      
      // Context-basierte Defaults
      if (context.currentPage === 'customer-detail') {
        defaults.customerId = context.customerId;
        defaults.title = `Opportunity ${new Date().getFullYear()}`;
      }
      
      // Wenn von Calculator geöffnet
      if (context.fromCalculator) {
        defaults.value = context.calculatedValue;
        defaults.title = 'Angebot ' + context.calculationNumber;
      }
    }
    
    return defaults;
  };
  
  const currentContext = {
    currentPage: location.pathname,
    customerId: params.customerId,
    userId: user?.id,
    timestamp: new Date(),
    autoNavigate: localStorage.getItem('quickCreate.autoNavigate') !== 'false'
  };
  
  return {
    getSmartDefaults,
    currentContext
  };
};
```

### 5. API Integration

```typescript
// services/quickCreateService.ts
import { apiClient } from '../lib/apiClient';

export const quickCreateService = {
  async createCustomer(data: CustomerQuickFormData) {
    // Mapping auf vollständiges Customer-Objekt
    const customer = {
      name: data.name,
      email: data.email,
      phone: data.phone,
      industry: data.industry,
      status: 'ACTIVE',
      createdAt: new Date().toISOString(),
      contacts: data.contactFirstName ? [{
        firstName: data.contactFirstName,
        lastName: data.contactLastName,
        email: data.email,
        phone: data.phone,
        isPrimary: true
      }] : []
    };
    
    const response = await apiClient.post('/api/customers', customer);
    
    // Track Event
    trackEvent('customer_created', {
      source: 'quick_create',
      industry: data.industry
    });
    
    return response.data;
  },
  
  async createOpportunity(data: OpportunityQuickFormData) {
    const opportunity = {
      title: data.title,
      customerId: data.customerId,
      value: data.value,
      stage: 'LEAD',
      probability: 10,
      expectedCloseDate: data.expectedCloseDate,
      notes: data.notes,
      createdAt: new Date().toISOString()
    };
    
    const response = await apiClient.post('/api/opportunities', opportunity);
    
    // Track Event
    trackEvent('opportunity_created', {
      source: 'quick_create',
      value: data.value,
      stage: 'LEAD'
    });
    
    return response.data;
  }
};
```

---

## 🎨 UI/UX FEATURES

### FAB Behavior
- **Position:** Bottom-right, 24px margin
- **Hide on scroll:** Versteckt sich beim Scrollen nach unten
- **Context-aware:** Zeigt relevante Optionen basierend auf Seite
- **Keyboard Shortcut:** `Ctrl+N` öffnet Quick Create

### Modal Features
- **Auto-focus:** Erstes Feld automatisch fokussiert
- **Enter to submit:** Formular mit Enter absenden
- **ESC to close:** Modal mit ESC schließen
- **Loading states:** Während API-Calls
- **Success feedback:** Toast + Optional Navigation

### Smart Features
- **Duplicate Detection:** Warnt vor ähnlichen Kunden
- **Auto-complete:** Kunde-Suche mit Debouncing
- **Quick Tags:** Vordefinierte Titel-Templates
- **Context Memory:** Merkt sich letzte Einstellungen

---

## 🧪 TESTING

```typescript
// __tests__/QuickCreate.test.tsx
describe('Quick Create', () => {
  it('should show FAB on all pages', () => {
    render(<App />);
    expect(screen.getByLabelText('Quick Create')).toBeInTheDocument();
  });
  
  it('should open modal on FAB click', async () => {
    render(<App />);
    fireEvent.click(screen.getByLabelText('Quick Create'));
    fireEvent.click(screen.getByText('Neuer Kunde'));
    
    expect(screen.getByText('Neuer Kunde')).toBeInTheDocument();
    expect(screen.getByLabelText('Firmenname')).toBeInTheDocument();
  });
  
  it('should validate required fields', async () => {
    // Test validation
  });
  
  it('should create customer successfully', async () => {
    // Test API integration
  });
});
```

---

## 📊 SUCCESS METRICS

- **Time to create:** < 30 Sekunden
- **Click reduction:** 80% weniger Klicks
- **Usage rate:** 50%+ aller neuen Einträge
- **Error rate:** < 5% fehlgeschlagene Erstellungen

---

**Nächster Schritt:** Integration in MainLayoutV2 und Testing!