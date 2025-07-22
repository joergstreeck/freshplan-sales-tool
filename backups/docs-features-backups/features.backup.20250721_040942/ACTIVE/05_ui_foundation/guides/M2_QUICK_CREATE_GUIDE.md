# ⚡ M2 QUICK CREATE IMPLEMENTATION GUIDE

**Fokus:** Nur Quick Create FAB + Modals  
**Zeilen:** <200 für optimale Claude-Arbeitsweise  
**Prerequisite:** [M2_QUICK_CREATE_KOMPAKT.md](../M2_QUICK_CREATE_KOMPAKT.md) gelesen  
**Zurück zur Übersicht:** [IMPLEMENTATION_GUIDE.md](../IMPLEMENTATION_GUIDE.md)  

---

## 🚀 PHASE 1: FLOATING ACTION BUTTON (Tag 1)

### 1.1: FAB Integration in MainLayoutV2

```typescript
// components/layout/MainLayoutV2.tsx (BESTEHEND ERWEITERN!)
import { QuickCreateFAB } from '../common/QuickCreateFAB';
import { QuickCreateModal } from '../common/QuickCreateModal';

export const MainLayoutV2: React.FC<Props> = ({ children }) => {
  const [quickCreateOpen, setQuickCreateOpen] = useState(false);
  const [quickCreateType, setQuickCreateType] = useState<'customer' | 'opportunity' | null>(null);
  
  const handleQuickCreate = (type: 'customer' | 'opportunity') => {
    setQuickCreateType(type);
    setQuickCreateOpen(true);
  };
  
  return (
    <Box sx={{ display: 'flex' }}>
      {/* BESTEHENDE Layout-Struktur */}
      <AppBar>...</AppBar>
      <Drawer>...</Drawer>
      
      <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
        <Toolbar />
        {children}
      </Box>
      
      {/* NEU: FAB hinzufügen */}
      <QuickCreateFAB onQuickCreate={handleQuickCreate} />
      
      {/* NEU: Modal hinzufügen */}
      <QuickCreateModal
        open={quickCreateOpen}
        type={quickCreateType}
        onClose={() => setQuickCreateOpen(false)}
        onSuccess={(data) => {
          // Success handling
          setQuickCreateOpen(false);
        }}
      />
    </Box>
  );
};
```

### 1.2: FAB Component

```typescript
// components/common/QuickCreateFAB.tsx (NEU)
import { SpeedDial, SpeedDialAction, SpeedDialIcon } from '@mui/material';
import { Add as AddIcon, Person as PersonIcon, TrendingUp as OpportunityIcon } from '@mui/icons-material';

interface QuickCreateFABProps {
  onQuickCreate: (type: 'customer' | 'opportunity') => void;
}

export const QuickCreateFAB: React.FC<QuickCreateFABProps> = ({ onQuickCreate }) => {
  const actions = [
    { 
      icon: <PersonIcon />, 
      name: 'Neuer Kunde', 
      action: () => onQuickCreate('customer') 
    },
    { 
      icon: <OpportunityIcon />, 
      name: 'Neue Opportunity', 
      action: () => onQuickCreate('opportunity') 
    }
  ];

  return (
    <SpeedDial
      ariaLabel="Quick Create"
      sx={{ position: 'fixed', bottom: 24, right: 24 }}
      icon={<SpeedDialIcon />}
    >
      {actions.map((action) => (
        <SpeedDialAction
          key={action.name}
          icon={action.icon}
          tooltipTitle={action.name}
          onClick={action.action}
        />
      ))}
    </SpeedDial>
  );
};
```

---

## 📝 PHASE 2: QUICK CREATE MODALS (Tag 2-3)

### 2.1: Modal Framework

```typescript
// components/common/QuickCreateModal.tsx (NEU)
import { Dialog, DialogTitle, DialogContent, DialogActions, Button } from '@mui/material';
import { QuickCustomerForm } from './QuickCustomerForm';
import { QuickOpportunityForm } from './QuickOpportunityForm';

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
  const handleSubmit = async (formData: any) => {
    try {
      let result;
      if (type === 'customer') {
        result = await customerService.createCustomer(formData);
      } else if (type === 'opportunity') {
        result = await opportunityService.createOpportunity(formData);
      }
      
      onSuccess(result);
      showSuccessNotification(`${type === 'customer' ? 'Kunde' : 'Opportunity'} erfolgreich erstellt!`);
    } catch (error) {
      showErrorNotification('Fehler beim Erstellen');
    }
  };

  if (!type) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        {type === 'customer' ? 'Neuen Kunden erstellen' : 'Neue Opportunity erstellen'}
      </DialogTitle>
      
      <DialogContent>
        {type === 'customer' ? (
          <QuickCustomerForm onSubmit={handleSubmit} />
        ) : (
          <QuickOpportunityForm onSubmit={handleSubmit} />
        )}
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button type="submit" variant="contained">Erstellen</Button>
      </DialogActions>
    </Dialog>
  );
};
```

### 2.2: Quick Customer Form

```typescript
// components/common/QuickCustomerForm.tsx (NEU)
import { useForm } from 'react-hook-form';
import { TextField, Box, Grid } from '@mui/material';

interface QuickCustomerFormProps {
  onSubmit: (data: any) => void;
}

export const QuickCustomerForm: React.FC<QuickCustomerFormProps> = ({ onSubmit }) => {
  const { register, handleSubmit, formState: { errors } } = useForm();
  
  return (
    <Box component="form" onSubmit={handleSubmit(onSubmit)} sx={{ mt: 2 }}>
      <Grid container spacing={2}>
        <Grid item xs={12} sm={6}>
          <TextField
            {...register('companyName', { required: 'Firmenname ist erforderlich' })}
            label="Firmenname"
            fullWidth
            error={!!errors.companyName}
            helperText={errors.companyName?.message}
          />
        </Grid>
        
        <Grid item xs={12} sm={6}>
          <TextField
            {...register('contactPerson')}
            label="Ansprechpartner"
            fullWidth
          />
        </Grid>
        
        <Grid item xs={12} sm={6}>
          <TextField
            {...register('email')}
            label="E-Mail"
            type="email"
            fullWidth
          />
        </Grid>
        
        <Grid item xs={12} sm={6}>
          <TextField
            {...register('phone')}
            label="Telefon"
            fullWidth
          />
        </Grid>
        
        <Grid item xs={12}>
          <TextField
            {...register('notes')}
            label="Notizen"
            multiline
            rows={3}
            fullWidth
          />
        </Grid>
      </Grid>
    </Box>
  );
};
```

---

## 🎯 PHASE 3: SMART DEFAULTS (Tag 4)

### 3.1: Context-aware Defaults

```typescript
// hooks/useSmartDefaults.ts (NEU)
import { useLocation } from 'react-router-dom';
import { useUser } from '../features/users/userQueries';

export const useSmartDefaults = () => {
  const location = useLocation();
  const { data: currentUser } = useUser();
  
  const getCustomerDefaults = () => {
    const defaults: any = {
      assignedTo: currentUser?.id,
      source: 'manual',
      status: 'new'
    };
    
    // Context-basierte Defaults
    if (location.pathname === '/cockpit') {
      defaults.priority = 'high';
      defaults.followUpDate = new Date(Date.now() + 24 * 60 * 60 * 1000); // Morgen
    }
    
    return defaults;
  };
  
  const getOpportunityDefaults = () => {
    return {
      assignedTo: currentUser?.id,
      stage: 'qualification',
      probability: 25,
      source: 'manual'
    };
  };
  
  return { getCustomerDefaults, getOpportunityDefaults };
};
```

---

## 🎯 SUCCESS CRITERIA

**Nach M2 Implementation:**
1. ✅ FAB ist immer sichtbar und funktioniert
2. ✅ Quick Create Modals für Customer/Opportunity
3. ✅ Smart Defaults basierend auf Context
4. ✅ Form Validation funktioniert
5. ✅ Success/Error Notifications
6. ✅ <30 Sekunden für Customer-Erstellung

**Geschätzter Aufwand:** 4 Tage
**Voraussetzung:** Entscheidung D4 (FAB-Position) geklärt

---

## 📞 NÄCHSTE SCHRITTE

1. **Entscheidung D4 klären** - FAB-Position festlegen
2. **QuickCreateFAB implementieren** - SpeedDial Component
3. **Modal Framework erstellen** - Dialog + Forms
4. **Smart Defaults implementieren** - Context-aware Defaults
5. **Integration testen** - End-to-End Workflow

**WICHTIG:** Komplett neue Entwicklung - keine bestehende Basis!