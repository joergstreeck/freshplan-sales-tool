# 🛠️ Sprint 2 - Tag 1: Foundation & Quick Wins

**Sprint:** Sprint 2 - Customer UI Integration & Task Preview  
**Tag:** Tag 1 von 3.5  
**Dauer:** 8 Stunden  
**Fokus:** CustomersPage Refactoring, Empty States, Quick Wins  

---

## 📍 Navigation

### Sprint 2 Dokumente:
- **← Zurück:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)
- **→ Weiter:** [Tag 2 Implementation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY2_IMPLEMENTATION.md)
- **↑ Philosophie:** [Philosophy & Approach](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/PHILOSOPHY_AND_APPROACH.md)
- **📚 Quick Ref:** [Quick Reference](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/QUICK_REFERENCE.md)

### FC-005 Struktur:
- **Hauptübersicht:** [FC-005 README](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/README.md)
- **Frontend Docs:** [03-FRONTEND](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/README.md)
- **Implementation:** [08-IMPLEMENTATION](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/08-IMPLEMENTATION/README.md)

### Übergeordnete Docs:
- **Master Plan:** [CRM V5](/Users/joergstreeck/freshplan-sales-tool/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)
- **Feature Roadmap:** [Complete Roadmap](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-12_COMPLETE_FEATURE_ROADMAP.md)
- **UI Diskussion:** [UI Integration Discussion](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/discussions/2025-07-27_UI_INTEGRATION_DISCUSSION.md)

---

## 🎯 Tag 1 Ziele

1. **CustomersPage komplett refactoren** (2h)
2. **Empty State Hero implementieren** (1h)
3. **Keyboard Shortcuts einbauen** (1h)
4. **ActionToast Component** (1h)
5. **Smart Empty States** (1h)
6. **CustomerListHeader** (1h)
7. **Tests & Polish** (1h)

---

## 1.1 CustomersPage Refactoring (2h)

### Aktueller Stand:
```typescript
// frontend/src/pages/CustomersPage.tsx - VORHER
export function CustomersPage() {
  const { data: customers } = useCustomers();
  return (
    <div>
      <h1>Kunden</h1>
      <CustomerTable customers={customers} />
    </div>
  );
}
```

### Ziel-Implementation:
```typescript
// frontend/src/pages/CustomersPage.tsx - NACHHER
import { useState } from 'react';
import { Box, Button, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { CustomerOnboardingWizard } from '../features/customers/components/CustomerOnboardingWizard';
import { EmptyStateHero } from '../components/common/EmptyStateHero';
import { CustomerTable } from '../features/customers/components/CustomerTable';
import { CustomerListHeader } from '../features/customers/components/CustomerListHeader';
import { useAuth } from '../contexts/AuthContext';
import { useCustomers } from '../features/customers/hooks/useCustomers';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-hot-toast';
import { ActionToast } from '../components/notifications/ActionToast';
import { taskEngine } from '../services/taskEngine';
import { featureFlags } from '../config/featureFlags';

export function CustomersPage() {
  const [wizardOpen, setWizardOpen] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();
  
  // Verkäuferschutz: Nur eigene Kunden
  const { data: customers = [], refetch, isLoading } = useCustomers({
    filter: user.role === 'sales' ? { assignedTo: user.id } : undefined
  });

  const handleCustomerCreated = async (customer: Customer) => {
    setWizardOpen(false);
    
    // Task Preview: Automatisch erste Aufgabe generieren
    let taskId: string | undefined;
    if (featureFlags.taskPreview) {
      try {
        const tasks = await taskEngine.processEvent({
          type: 'customer-created',
          context: { customer, user }
        });
        taskId = tasks[0]?.id;
      } catch (error) {
        console.error('Task generation failed:', error);
        // Nicht blockierend - Customer wurde trotzdem angelegt
      }
    }
    
    // Erfolgs-Feedback mit Action
    toast.success(
      <ActionToast
        message={`Kunde "${customer.name}" erfolgreich angelegt!`}
        action={taskId ? {
          label: "Aufgabe anzeigen",
          onClick: () => navigate(`/tasks/${taskId}`)
        } : undefined}
      />
    );
    
    // Liste aktualisieren
    await refetch();
    
    // Zur Detail-Seite navigieren
    navigate(`/customers/${customer.id}?highlight=new`);
  };

  if (isLoading) {
    return <CustomerListSkeleton />;
  }

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header mit Button - immer sichtbar */}
      <CustomerListHeader 
        totalCount={customers.length}
        onAddCustomer={() => setWizardOpen(true)}
      />
      
      {/* Content Area */}
      <Box sx={{ flex: 1, overflow: 'auto', p: 3 }}>
        {customers.length === 0 ? (
          <EmptyStateHero 
            title="Noch keine Kunden"
            description="Legen Sie Ihren ersten Kunden an und starten Sie Ihre Erfolgsgeschichte!"
            illustration="/illustrations/empty-customers.svg"
            action={{
              label: "✨ Ersten Kunden anlegen",
              onClick: () => setWizardOpen(true),
              variant: "contained",
              size: "large"
            }}
            secondaryAction={{
              label: "Demo-Daten importieren",
              onClick: () => navigate('/settings/import')
            }}
          />
        ) : (
          <>
            {/* Filter Bar (später) */}
            <CustomerTable 
              customers={customers}
              onRowClick={(customer) => navigate(`/customers/${customer.id}`)}
              highlightNew
            />
          </>
        )}
      </Box>
      
      {/* Wizard Modal/Drawer */}
      <CustomerOnboardingWizard
        open={wizardOpen}
        onClose={() => setWizardOpen(false)}
        onComplete={handleCustomerCreated}
      />
    </Box>
  );
}
```

---

## 1.2 Empty State Hero Component (1h)

```typescript
// frontend/src/components/common/EmptyStateHero.tsx
import { Box, Button, Typography, useTheme, useMediaQuery } from '@mui/material';
import { keyframes } from '@emotion/react';

const fadeIn = keyframes`
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
`;

interface EmptyStateHeroProps {
  title: string;
  description?: string;
  illustration?: string;
  action?: {
    label: string;
    onClick: () => void;
    variant?: 'text' | 'outlined' | 'contained';
    size?: 'small' | 'medium' | 'large';
  };
  secondaryAction?: {
    label: string;
    onClick: () => void;
  };
}

export function EmptyStateHero({
  title,
  description,
  illustration,
  action,
  secondaryAction
}: EmptyStateHeroProps) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: 400,
        p: 4,
        textAlign: 'center',
        animation: `${fadeIn} 0.6s ease-out`
      }}
    >
      {illustration && (
        <Box
          component="img"
          src={illustration}
          alt=""
          sx={{
            width: isMobile ? 200 : 280,
            height: 'auto',
            mb: 4,
            opacity: 0.8
          }}
        />
      )}
      
      <Typography 
        variant={isMobile ? "h5" : "h4"} 
        gutterBottom
        sx={{ fontWeight: 600, color: 'text.primary' }}
      >
        {title}
      </Typography>
      
      {description && (
        <Typography 
          variant="body1" 
          color="text.secondary"
          sx={{ mb: 4, maxWidth: 500 }}
        >
          {description}
        </Typography>
      )}
      
      <Box sx={{ display: 'flex', gap: 2, flexDirection: isMobile ? 'column' : 'row' }}>
        {action && (
          <Button
            variant={action.variant || 'contained'}
            size={action.size || 'medium'}
            onClick={action.onClick}
            sx={{ 
              minWidth: isMobile ? 200 : 'auto',
              bgcolor: theme.palette.primary.main,
              '&:hover': {
                bgcolor: theme.palette.primary.dark,
                transform: 'translateY(-2px)',
                boxShadow: theme.shadows[4]
              },
              transition: 'all 0.2s ease'
            }}
          >
            {action.label}
          </Button>
        )}
        
        {secondaryAction && (
          <Button
            variant="text"
            onClick={secondaryAction.onClick}
            sx={{ minWidth: isMobile ? 200 : 'auto' }}
          >
            {secondaryAction.label}
          </Button>
        )}
      </Box>
    </Box>
  );
}
```

---

## 1.3 Keyboard Shortcuts Hook (1h)

```typescript
// frontend/src/hooks/useKeyboardShortcuts.ts
import { useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useHotkeys } from 'react-hotkeys-hook';

interface ShortcutConfig {
  key: string;
  description: string;
  action: () => void;
  preventDefault?: boolean;
}

export function useKeyboardShortcuts() {
  const navigate = useNavigate();
  
  const shortcuts: ShortcutConfig[] = [
    {
      key: 'ctrl+n, cmd+n',
      description: 'Neuen Kunden anlegen',
      action: () => {
        // Trigger global event
        window.dispatchEvent(new CustomEvent('freshplan:new-customer'));
      }
    },
    {
      key: 'ctrl+t, cmd+t',
      description: 'Neue Aufgabe',
      action: () => {
        window.dispatchEvent(new CustomEvent('freshplan:new-task'));
      }
    },
    {
      key: 'ctrl+k, cmd+k',
      description: 'Command Palette öffnen',
      action: () => {
        window.dispatchEvent(new CustomEvent('freshplan:command-palette'));
      }
    },
    {
      key: 'ctrl+/, cmd+/',
      description: 'Hilfe anzeigen',
      action: () => {
        window.dispatchEvent(new CustomEvent('freshplan:show-help'));
      }
    }
  ];
  
  // Register all shortcuts
  shortcuts.forEach(({ key, action, preventDefault = true }) => {
    useHotkeys(key, (e) => {
      if (preventDefault) e.preventDefault();
      action();
    }, { enableOnFormTags: false });
  });
  
  return { shortcuts };
}

// Global Provider Component
export function KeyboardShortcutsProvider({ children }: { children: React.ReactNode }) {
  useKeyboardShortcuts();
  return <>{children}</>;
}
```

---

## 1.4 ActionToast Component (1h)

```typescript
// frontend/src/components/notifications/ActionToast.tsx
import { Box, Button, Typography } from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

interface ActionToastProps {
  message: string;
  action?: {
    label: string;
    onClick: () => void;
  };
  icon?: React.ReactNode;
}

export function ActionToast({ message, action, icon }: ActionToastProps) {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
      {icon || <CheckCircleIcon sx={{ color: 'success.main' }} />}
      <Box sx={{ flex: 1 }}>
        <Typography variant="body2">{message}</Typography>
      </Box>
      {action && (
        <Button
          size="small"
          variant="contained"
          onClick={action.onClick}
          sx={{
            bgcolor: 'white',
            color: 'primary.main',
            '&:hover': {
              bgcolor: 'grey.100'
            }
          }}
        >
          {action.label}
        </Button>
      )}
    </Box>
  );
}
```

---

## 1.5 CustomerListHeader Component (1h)

```typescript
// frontend/src/features/customers/components/CustomerListHeader.tsx
import { Box, Typography, Button, Chip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useKeyboardShortcuts } from '../../../hooks/useKeyboardShortcuts';

interface CustomerListHeaderProps {
  totalCount: number;
  onAddCustomer: () => void;
}

export function CustomerListHeader({ totalCount, onAddCustomer }: CustomerListHeaderProps) {
  // Listen for global shortcut event
  useEffect(() => {
    const handler = () => onAddCustomer();
    window.addEventListener('freshplan:new-customer', handler);
    return () => window.removeEventListener('freshplan:new-customer', handler);
  }, [onAddCustomer]);
  
  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        p: 3,
        borderBottom: 1,
        borderColor: 'divider',
        bgcolor: 'background.paper'
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <Typography variant="h4" component="h1">
          Kunden
        </Typography>
        <Chip 
          label={totalCount} 
          size="small"
          sx={{ bgcolor: 'primary.light', color: 'primary.contrastText' }}
        />
      </Box>
      
      <Button
        variant="contained"
        startIcon={<AddIcon />}
        onClick={onAddCustomer}
        sx={{
          bgcolor: '#94C456', // Freshfoodz Grün
          '&:hover': {
            bgcolor: '#7BA545'
          }
        }}
      >
        Neuer Kunde
        <Typography
          component="span"
          sx={{
            ml: 1,
            fontSize: '0.75rem',
            opacity: 0.7
          }}
        >
          Strg+N
        </Typography>
      </Button>
    </Box>
  );
}
```

---

## 1.6 Tests für Tag 1 (1h)

```typescript
// frontend/src/pages/__tests__/CustomersPage.test.tsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { CustomersPage } from '../CustomersPage';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from '../../contexts/AuthContext';

const wrapper = ({ children }: { children: React.ReactNode }) => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } }
  });
  
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          {children}
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

describe('CustomersPage', () => {
  it('shows empty state when no customers', async () => {
    render(<CustomersPage />, { wrapper });
    
    await waitFor(() => {
      expect(screen.getByText('Noch keine Kunden')).toBeInTheDocument();
    });
    
    expect(screen.getByText('✨ Ersten Kunden anlegen')).toBeInTheDocument();
  });
  
  it('opens wizard on button click', async () => {
    render(<CustomersPage />, { wrapper });
    
    const button = await screen.findByText('✨ Ersten Kunden anlegen');
    fireEvent.click(button);
    
    await waitFor(() => {
      expect(screen.getByText('Neuen Kunden anlegen')).toBeInTheDocument();
    });
  });
  
  it('responds to keyboard shortcut Ctrl+N', async () => {
    render(<CustomersPage />, { wrapper });
    
    // Simulate Ctrl+N
    fireEvent.keyDown(document, { key: 'n', ctrlKey: true });
    
    await waitFor(() => {
      expect(screen.getByText('Neuen Kunden anlegen')).toBeInTheDocument();
    });
  });
});
```

---

## ✅ Tag 1 Checklist

- [ ] CustomersPage komplett refactored
- [ ] EmptyStateHero implementiert und getestet
- [ ] Keyboard Shortcuts (Ctrl+N) funktionieren
- [ ] ActionToast zeigt Success mit Button
- [ ] CustomerListHeader mit Count Badge
- [ ] Tests grün für alle Components
- [ ] Performance: Initial Load < 200ms
- [ ] Mobile: Touch Targets ≥ 44px

---

## 🎯 Nächste Schritte

Nach erfolgreichem Abschluss von Tag 1:
1. Code committen: `git commit -m "feat(sprint2): implement Day 1 - CustomersPage refactoring & quick wins"`
2. Tests laufen lassen: `npm test`
3. Weiter mit [Tag 2: Task Engine & API](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/DAY2_IMPLEMENTATION.md)

---

**Remember:** Jede Zeile Code sollte das System intelligenter wirken lassen! 🎯