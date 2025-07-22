# FC-002-M2: Quick-Create System ("+ Neu" Button)

**Modul:** M2  
**Feature:** FC-002  
**Status:** 📋 In Planung (0%)  
**Geschätzter Aufwand:** 2 Tage  

## 📋 Implementierungs-Checkliste

- [ ] QuickCreateButton.tsx Komponente
- [ ] QuickCreateMenu.tsx Dropdown-Menü
- [ ] QuickCreateModal.tsx für Formulare
- [ ] quickCreateStore.ts erstellen
- [ ] Keyboard Shortcut (Ctrl/Cmd + N)
- [ ] Formular-Templates für jeden Typ
- [ ] API-Integration für Create-Actions
- [ ] Loading & Error States
- [ ] Unit Tests
- [ ] E2E Test für Quick-Create Flow

## 🏗️ Komponenten-Struktur

```
frontend/src/
├── components/
│   └── quickCreate/
│       ├── QuickCreateButton.tsx       # Header-Button
│       ├── QuickCreateMenu.tsx         # Dropdown-Menü
│       ├── QuickCreateModal.tsx        # Modal-Container
│       ├── forms/
│       │   ├── QuickCustomerForm.tsx   # Kunde anlegen
│       │   ├── QuickContactForm.tsx    # Kontakt anlegen
│       │   ├── QuickTaskForm.tsx       # Aufgabe anlegen
│       │   └── QuickNoteForm.tsx       # Notiz anlegen
│       └── __tests__/
├── store/
│   └── quickCreateStore.ts             # Zustand für Quick-Create
├── hooks/
│   └── useQuickCreateShortcut.ts       # Keyboard Shortcut
└── services/
    └── quickCreate.service.ts          # API Calls
```

## 📝 Detaillierte Spezifikation

### 1. Quick-Create Button

```typescript
// frontend/src/components/quickCreate/QuickCreateButton.tsx
import React, { useRef, useState } from 'react';
import { 
  IconButton, 
  Tooltip, 
  Badge,
  Box,
  styled 
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useQuickCreateStore } from '@/store/quickCreateStore';
import { useQuickCreateShortcut } from '@/hooks/useQuickCreateShortcut';
import { QuickCreateMenu } from './QuickCreateMenu';

const StyledIconButton = styled(IconButton)(({ theme }) => ({
  backgroundColor: theme.palette.primary.main,
  color: theme.palette.common.white,
  '&:hover': {
    backgroundColor: theme.palette.primary.dark,
  },
  width: 40,
  height: 40,
}));

export const QuickCreateButton: React.FC = () => {
  const anchorRef = useRef<HTMLButtonElement>(null);
  const [menuOpen, setMenuOpen] = useState(false);
  const { pendingCreations } = useQuickCreateStore();
  
  // Keyboard shortcut
  useQuickCreateShortcut(() => setMenuOpen(true));

  const handleToggle = () => {
    setMenuOpen((prevOpen) => !prevOpen);
  };

  const handleClose = (event: Event | React.SyntheticEvent) => {
    if (
      anchorRef.current &&
      anchorRef.current.contains(event.target as HTMLElement)
    ) {
      return;
    }
    setMenuOpen(false);
  };

  return (
    <Box sx={{ position: 'relative' }}>
      <Tooltip title="Neu erstellen (Ctrl+N)">
        <Badge 
          badgeContent={pendingCreations} 
          color="error"
          overlap="circular"
        >
          <StyledIconButton
            ref={anchorRef}
            onClick={handleToggle}
            size="small"
            aria-label="Neu erstellen"
            aria-controls={menuOpen ? 'quick-create-menu' : undefined}
            aria-haspopup="true"
            aria-expanded={menuOpen ? 'true' : undefined}
          >
            <AddIcon />
          </StyledIconButton>
        </Badge>
      </Tooltip>
      
      <QuickCreateMenu
        open={menuOpen}
        anchorEl={anchorRef.current}
        onClose={handleClose}
      />
    </Box>
  );
};
```

### 2. Quick-Create Menü

```typescript
// frontend/src/components/quickCreate/QuickCreateMenu.tsx
import React from 'react';
import {
  Popper,
  Paper,
  ClickAwayListener,
  MenuList,
  MenuItem,
  ListItemIcon,
  ListItemText,
  Divider,
  Typography,
  Grow,
} from '@mui/material';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import ContactPhoneIcon from '@mui/icons-material/ContactPhone';
import TaskIcon from '@mui/icons-material/Task';
import NoteAddIcon from '@mui/icons-material/NoteAdd';
import EmailIcon from '@mui/icons-material/Email';
import EventIcon from '@mui/icons-material/Event';
import { useQuickCreateStore } from '@/store/quickCreateStore';
import { useAuthStore } from '@/store/authStore';

interface QuickCreateMenuProps {
  open: boolean;
  anchorEl: HTMLElement | null;
  onClose: (event: Event | React.SyntheticEvent) => void;
}

interface CreateOption {
  id: string;
  label: string;
  icon: React.ReactElement;
  shortcut?: string;
  permission?: string;
  action: () => void;
}

export const QuickCreateMenu: React.FC<QuickCreateMenuProps> = ({
  open,
  anchorEl,
  onClose,
}) => {
  const { openModal } = useQuickCreateStore();
  const { hasPermission } = useAuthStore();

  const createOptions: CreateOption[] = [
    {
      id: 'customer',
      label: 'Neuer Kunde',
      icon: <PersonAddIcon />,
      shortcut: 'K',
      permission: 'customers.create',
      action: () => {
        openModal('customer');
        onClose(new Event(''));
      },
    },
    {
      id: 'contact',
      label: 'Neuer Kontakt',
      icon: <ContactPhoneIcon />,
      shortcut: 'C',
      permission: 'contacts.create',
      action: () => {
        openModal('contact');
        onClose(new Event(''));
      },
    },
    {
      id: 'task',
      label: 'Neue Aufgabe',
      icon: <TaskIcon />,
      shortcut: 'A',
      permission: 'tasks.create',
      action: () => {
        openModal('task');
        onClose(new Event(''));
      },
    },
    {
      id: 'note',
      label: 'Neue Notiz',
      icon: <NoteAddIcon />,
      shortcut: 'N',
      action: () => {
        openModal('note');
        onClose(new Event(''));
      },
    },
  ];

  const quickActions: CreateOption[] = [
    {
      id: 'email',
      label: 'E-Mail verfassen',
      icon: <EmailIcon />,
      action: () => {
        openModal('email');
        onClose(new Event(''));
      },
    },
    {
      id: 'appointment',
      label: 'Termin erstellen',
      icon: <EventIcon />,
      action: () => {
        openModal('appointment');
        onClose(new Event(''));
      },
    },
  ];

  const visibleOptions = createOptions.filter(
    option => !option.permission || hasPermission(option.permission)
  );

  return (
    <Popper
      open={open}
      anchorEl={anchorEl}
      role={undefined}
      placement="bottom-end"
      transition
      disablePortal
      style={{ zIndex: 1300 }}
    >
      {({ TransitionProps, placement }) => (
        <Grow
          {...TransitionProps}
          style={{
            transformOrigin:
              placement === 'bottom-end' ? 'right top' : 'right bottom',
          }}
        >
          <Paper elevation={8} sx={{ minWidth: 240 }}>
            <ClickAwayListener onClickAway={onClose}>
              <MenuList autoFocusItem={open} id="quick-create-menu">
                <Typography
                  variant="caption"
                  color="text.secondary"
                  sx={{ px: 2, py: 1 }}
                >
                  ERSTELLEN
                </Typography>
                
                {visibleOptions.map((option) => (
                  <MenuItem key={option.id} onClick={option.action}>
                    <ListItemIcon>{option.icon}</ListItemIcon>
                    <ListItemText primary={option.label} />
                    {option.shortcut && (
                      <Typography variant="body2" color="text.secondary">
                        {option.shortcut}
                      </Typography>
                    )}
                  </MenuItem>
                ))}
                
                <Divider />
                
                <Typography
                  variant="caption"
                  color="text.secondary"
                  sx={{ px: 2, py: 1 }}
                >
                  SCHNELLAKTIONEN
                </Typography>
                
                {quickActions.map((action) => (
                  <MenuItem key={action.id} onClick={action.action}>
                    <ListItemIcon>{action.icon}</ListItemIcon>
                    <ListItemText primary={action.label} />
                  </MenuItem>
                ))}
              </MenuList>
            </ClickAwayListener>
          </Paper>
        </Grow>
      )}
    </Popper>
  );
};
```

### 3. Quick-Create Modal

```typescript
// frontend/src/components/quickCreate/QuickCreateModal.tsx
import React, { Suspense, lazy } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  IconButton,
  Typography,
  Box,
  LinearProgress,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useQuickCreateStore } from '@/store/quickCreateStore';

// Lazy load forms
const formComponents = {
  customer: lazy(() => import('./forms/QuickCustomerForm')),
  contact: lazy(() => import('./forms/QuickContactForm')),
  task: lazy(() => import('./forms/QuickTaskForm')),
  note: lazy(() => import('./forms/QuickNoteForm')),
  email: lazy(() => import('./forms/QuickEmailForm')),
  appointment: lazy(() => import('./forms/QuickAppointmentForm')),
};

const modalTitles = {
  customer: 'Neuer Kunde',
  contact: 'Neuer Kontakt',
  task: 'Neue Aufgabe',
  note: 'Neue Notiz',
  email: 'E-Mail verfassen',
  appointment: 'Neuer Termin',
};

export const QuickCreateModal: React.FC = () => {
  const { isOpen, modalType, closeModal } = useQuickCreateStore();

  if (!isOpen || !modalType) return null;

  const FormComponent = formComponents[modalType];
  const title = modalTitles[modalType];

  return (
    <Dialog
      open={isOpen}
      onClose={closeModal}
      maxWidth="sm"
      fullWidth
      aria-labelledby="quick-create-dialog-title"
    >
      <DialogTitle id="quick-create-dialog-title">
        <Box display="flex" alignItems="center" justifyContent="space-between">
          <Typography variant="h6">{title}</Typography>
          <IconButton
            aria-label="schließen"
            onClick={closeModal}
            size="small"
          >
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>
      
      <DialogContent>
        <Suspense fallback={<LinearProgress />}>
          <FormComponent onClose={closeModal} />
        </Suspense>
      </DialogContent>
    </Dialog>
  );
};
```

### 4. Quick-Create Store

```typescript
// frontend/src/store/quickCreateStore.ts
import { create } from 'zustand';

export type ModalType = 'customer' | 'contact' | 'task' | 'note' | 'email' | 'appointment';

interface QuickCreateState {
  // State
  isOpen: boolean;
  modalType: ModalType | null;
  pendingCreations: number;
  lastCreated: {
    type: ModalType;
    id: string;
    timestamp: number;
  } | null;
  
  // Actions
  openModal: (type: ModalType) => void;
  closeModal: () => void;
  incrementPending: () => void;
  decrementPending: () => void;
  setLastCreated: (type: ModalType, id: string) => void;
}

export const useQuickCreateStore = create<QuickCreateState>((set) => ({
  isOpen: false,
  modalType: null,
  pendingCreations: 0,
  lastCreated: null,
  
  openModal: (type) => set({ isOpen: true, modalType: type }),
  
  closeModal: () => set({ isOpen: false, modalType: null }),
  
  incrementPending: () => set((state) => ({ 
    pendingCreations: state.pendingCreations + 1 
  })),
  
  decrementPending: () => set((state) => ({ 
    pendingCreations: Math.max(0, state.pendingCreations - 1) 
  })),
  
  setLastCreated: (type, id) => set({ 
    lastCreated: { type, id, timestamp: Date.now() } 
  }),
}));
```

### 5. Beispiel-Formular: Quick Customer Form

```typescript
// frontend/src/components/quickCreate/forms/QuickCustomerForm.tsx
import React from 'react';
import { useForm, Controller } from 'react-hook-form';
import {
  TextField,
  Button,
  Box,
  Stack,
  Alert,
} from '@mui/material';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { customerService } from '@/services/customer.service';
import { notificationService } from '@/services/notification.service';
import { useQuickCreateStore } from '@/store/quickCreateStore';

interface QuickCustomerFormData {
  companyName: string;
  contactPerson: string;
  email: string;
  phone?: string;
}

interface QuickCustomerFormProps {
  onClose: () => void;
}

export const QuickCustomerForm: React.FC<QuickCustomerFormProps> = ({ onClose }) => {
  const queryClient = useQueryClient();
  const { setLastCreated, incrementPending, decrementPending } = useQuickCreateStore();
  
  const { control, handleSubmit, formState: { errors } } = useForm<QuickCustomerFormData>({
    defaultValues: {
      companyName: '',
      contactPerson: '',
      email: '',
      phone: '',
    },
  });

  const createMutation = useMutation({
    mutationFn: (data: QuickCustomerFormData) => {
      incrementPending();
      return customerService.createQuick(data);
    },
    onSuccess: (newCustomer) => {
      queryClient.invalidateQueries({ queryKey: ['customers'] });
      setLastCreated('customer', newCustomer.id);
      decrementPending();
      
      notificationService.show({
        type: 'success',
        message: `Kunde "${newCustomer.companyName}" wurde angelegt`,
        action: {
          label: 'Anzeigen',
          onClick: () => window.location.href = `/kundenmanagement/${newCustomer.id}`,
        },
      });
      
      onClose();
    },
    onError: (error) => {
      decrementPending();
      notificationService.show({
        type: 'error',
        message: `Fehler: ${error.message}`,
      });
    },
  });

  const onSubmit = (data: QuickCustomerFormData) => {
    createMutation.mutate(data);
  };

  return (
    <Box component="form" onSubmit={handleSubmit(onSubmit)}>
      <Stack spacing={3}>
        {createMutation.isError && (
          <Alert severity="error">
            Kunde konnte nicht angelegt werden. Bitte versuchen Sie es erneut.
          </Alert>
        )}
        
        <Controller
          name="companyName"
          control={control}
          rules={{ required: 'Firmenname ist erforderlich' }}
          render={({ field }) => (
            <TextField
              {...field}
              label="Firmenname"
              fullWidth
              autoFocus
              error={!!errors.companyName}
              helperText={errors.companyName?.message}
            />
          )}
        />
        
        <Controller
          name="contactPerson"
          control={control}
          rules={{ required: 'Ansprechpartner ist erforderlich' }}
          render={({ field }) => (
            <TextField
              {...field}
              label="Ansprechpartner"
              fullWidth
              error={!!errors.contactPerson}
              helperText={errors.contactPerson?.message}
            />
          )}
        />
        
        <Controller
          name="email"
          control={control}
          rules={{
            required: 'E-Mail ist erforderlich',
            pattern: {
              value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
              message: 'Ungültige E-Mail-Adresse',
            },
          }}
          render={({ field }) => (
            <TextField
              {...field}
              label="E-Mail"
              type="email"
              fullWidth
              error={!!errors.email}
              helperText={errors.email?.message}
            />
          )}
        />
        
        <Controller
          name="phone"
          control={control}
          render={({ field }) => (
            <TextField
              {...field}
              label="Telefon (optional)"
              fullWidth
            />
          )}
        />
        
        <Box display="flex" gap={2} justifyContent="flex-end">
          <Button onClick={onClose} disabled={createMutation.isPending}>
            Abbrechen
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={createMutation.isPending}
          >
            {createMutation.isPending ? 'Wird angelegt...' : 'Kunde anlegen'}
          </Button>
        </Box>
      </Stack>
    </Box>
  );
};

export default QuickCustomerForm;
```

### 6. Keyboard Shortcut Hook

```typescript
// frontend/src/hooks/useQuickCreateShortcut.ts
import { useEffect } from 'react';

export const useQuickCreateShortcut = (onTrigger: () => void) => {
  useEffect(() => {
    const handleKeyPress = (event: KeyboardEvent) => {
      // Ctrl/Cmd + N
      if ((event.ctrlKey || event.metaKey) && event.key === 'n') {
        event.preventDefault();
        onTrigger();
      }
    };

    window.addEventListener('keydown', handleKeyPress);
    return () => window.removeEventListener('keydown', handleKeyPress);
  }, [onTrigger]);
};
```

## 🎨 UI/UX Guidelines

- Modal öffnet sich zentriert mit Fade-Animation
- Auto-Focus auf erstes Eingabefeld
- Escape-Taste schließt Modal
- Enter submittet Formular (wenn möglich)
- Loading-States während API-Calls
- Success-Notification mit Action-Link
- Fehler inline im Formular anzeigen

## 🧪 Test-Szenarien

```typescript
describe('QuickCreateSystem', () => {
  it('sollte Menü auf Button-Klick öffnen');
  it('sollte Menü auf Ctrl+N öffnen');
  it('sollte nur erlaubte Optionen anzeigen');
  it('sollte Modal mit korrektem Formular öffnen');
  it('sollte Formular validieren');
  it('sollte API-Call ausführen und Feedback geben');
  it('sollte nach Erfolg Modal schließen');
  it('sollte Pending-Counter korrekt verwalten');
});
```

## ⚡ Performance

- Lazy Loading für alle Formulare
- Optimistic Updates wo möglich
- Debounced Form Validation
- Memoized Menu Items

---

**Nächste Schritte:**
1. QuickCreateButton in Header einbinden
2. Store und Service implementieren
3. Erste Formulare erstellen
4. Tests schreiben