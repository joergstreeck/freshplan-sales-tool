# 📱 Mobile Contact Actions - Quick Actions für Vertrieb unterwegs

**Phase:** 1 - Foundation  
**Tag:** 4 der Woche 1  
**Status:** 🎯 Ready for Implementation  

## 🧭 Navigation

**← Zurück:** [Smart Contact Cards](./SMART_CONTACT_CARDS.md)  
**→ Nächster:** [Integration Testing](./TESTING_INTEGRATION.md)  
**↑ Übergeordnet:** [Step 3 Main Guide](./README.md)  

## 🎯 Vision: Vertrieb unterwegs optimieren

**Mobile Contact Actions** verwandelt das Smartphone in ein **mächtiges Vertriebstool**:

> "Ein Swipe nach rechts = sofortiger Anruf beim wichtigsten Kontakt"

### 💬 Team-Feedback:
> "Gamechanger für Adoption und Akzeptanz. Top-UX für Außendienst/Messe. CRM-Light im Alltag, volle Kraft im Detail."

## 📱 Mobile-First Action System

### Quick Actions Architecture

```typescript
// types/mobileActions.types.ts
export interface QuickAction {
  id: string;
  type: 'call' | 'email' | 'whatsapp' | 'sms' | 'calendar' | 'note';
  label: string;
  icon: React.ReactNode;
  color: string;
  primary?: boolean; // User-defined primary action
  contextual?: boolean; // Shown based on contact data
}

export interface SwipeActions {
  left: QuickAction;  // Swipe left action
  right: QuickAction; // Swipe right action
}

export interface ContactActionConfig {
  contactId: string;
  primaryAction: QuickAction;
  secondaryActions: QuickAction[];
  swipeActions: SwipeActions;
  lastUsedAction?: string;
  actionHistory: ActionHistory[];
}
```

### Smart Action Suggestions

```typescript
// services/actionSuggestionService.ts
export class ActionSuggestionService {
  
  getSuggestedActions(contact: Contact, warmth?: RelationshipWarmth): QuickAction[] {
    const actions: QuickAction[] = [];
    
    // Primary contact info based suggestions
    if (contact.phone) {
      actions.push({
        id: 'call',
        type: 'call',
        label: 'Anrufen',
        icon: <PhoneIcon />,
        color: '#4CAF50',
        contextual: true
      });
    }
    
    if (contact.mobile) {
      actions.push({
        id: 'whatsapp',
        type: 'whatsapp',
        label: 'WhatsApp',
        icon: <WhatsAppIcon />,
        color: '#25D366',
        contextual: true
      });
    }
    
    if (contact.email) {
      actions.push({
        id: 'email',
        type: 'email',
        label: 'E-Mail',
        icon: <EmailIcon />,
        color: '#2196F3',
        contextual: true
      });
    }
    
    // Warmth-based suggestions
    if (warmth?.temperature === 'COOLING') {
      actions.unshift({
        id: 'urgent-call',
        type: 'call',
        label: '🔥 Dringend anrufen',
        icon: <PhoneIcon />,
        color: '#FF5722',
        primary: true
      });
    }
    
    // Birthday-based suggestions
    if (this.isBirthdayUpcoming(contact.birthday)) {
      actions.unshift({
        id: 'birthday-call',
        type: 'call',
        label: '🎂 Geburtstag gratulieren',
        icon: <CakeIcon />,
        color: '#E91E63',
        primary: true
      });
    }
    
    return actions;
  }
  
  private isBirthdayUpcoming(birthday?: Date): boolean {
    if (!birthday) return false;
    
    const today = new Date();
    const thisYear = today.getFullYear();
    const birthdayThisYear = new Date(thisYear, birthday.getMonth(), birthday.getDate());
    
    const daysUntil = Math.ceil((birthdayThisYear.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
    return daysUntil >= 0 && daysUntil <= 7;
  }
}
```

## 🎨 Mobile UI Components

### Swipeable Contact Card

```typescript
// components/SwipeableContactCard.tsx
import { useSwipeable } from 'react-swipeable';

export const SwipeableContactCard: React.FC<{
  contact: Contact;
  warmth?: RelationshipWarmth;
  onAction: (action: QuickAction) => void;
}> = ({ contact, warmth, onAction }) => {
  const actionSuggestionService = useActionSuggestionService();
  const [swipeActions, setSwipeActions] = useState<SwipeActions>();
  
  useEffect(() => {
    const suggestedActions = actionSuggestionService.getSuggestedActions(contact, warmth);
    setSwipeActions({
      left: suggestedActions.find(a => a.type === 'email') || defaultEmailAction,
      right: suggestedActions.find(a => a.type === 'call') || defaultCallAction
    });
  }, [contact, warmth]);
  
  const handlers = useSwipeable({
    onSwipedLeft: () => onAction(swipeActions.left),
    onSwipedRight: () => onAction(swipeActions.right),
    preventDefaultTouchmoveEvent: true,
    trackMouse: true
  });
  
  return (
    <Box {...handlers} sx={{ position: 'relative' }}>
      {/* Swipe Indicator Overlays */}
      <SwipeIndicator direction="left" action={swipeActions?.left} />
      <SwipeIndicator direction="right" action={swipeActions?.right} />
      
      {/* Main Contact Card */}
      <Card sx={{ 
        transition: 'transform 0.2s ease',
        '&:active': { transform: 'scale(0.98)' }
      }}>
        <CardContent>
          <ContactCardContent contact={contact} warmth={warmth} />
          <MobileActionBar 
            contact={contact} 
            onAction={onAction}
            suggestions={actionSuggestionService.getSuggestedActions(contact, warmth)}
          />
        </CardContent>
      </Card>
    </Box>
  );
};
```

### Mobile Action Bar

```typescript
// components/MobileActionBar.tsx
export const MobileActionBar: React.FC<{
  contact: Contact;
  suggestions: QuickAction[];
  onAction: (action: QuickAction) => void;
}> = ({ contact, suggestions, onAction }) => {
  const [showAll, setShowAll] = useState(false);
  const primaryActions = suggestions.slice(0, 4);
  const secondaryActions = suggestions.slice(4);
  
  return (
    <Box sx={{ mt: 2 }}>
      {/* Primary Actions - Always Visible */}
      <Box sx={{ display: 'flex', gap: 1, justifyContent: 'space-around' }}>
        {primaryActions.map(action => (
          <IconButton
            key={action.id}
            onClick={() => onAction(action)}
            sx={{
              backgroundColor: action.color,
              color: 'white',
              minWidth: 48,
              minHeight: 48,
              '&:hover': {
                backgroundColor: action.color,
                opacity: 0.8
              }
            }}
          >
            {action.icon}
          </IconButton>
        ))}
        
        {secondaryActions.length > 0 && (
          <IconButton
            onClick={() => setShowAll(!showAll)}
            sx={{ backgroundColor: '#757575', color: 'white' }}
          >
            <MoreHorizIcon />
          </IconButton>
        )}
      </Box>
      
      {/* Secondary Actions - Expandable */}
      <Collapse in={showAll}>
        <Box sx={{ display: 'flex', gap: 1, mt: 1, justifyContent: 'center' }}>
          {secondaryActions.map(action => (
            <Chip
              key={action.id}
              label={action.label}
              onClick={() => onAction(action)}
              sx={{
                backgroundColor: action.color,
                color: 'white',
                '&:hover': { opacity: 0.8 }
              }}
            />
          ))}
        </Box>
      </Collapse>
      
      {/* Swipe Instructions */}
      <Typography variant="caption" color="text.secondary" sx={{ mt: 1, textAlign: 'center', display: 'block' }}>
        👈 Wischen für {primaryActions.find(a => a.type === 'email')?.label} • 
        {primaryActions.find(a => a.type === 'call')?.label} für Wischen 👉
      </Typography>
    </Box>
  );
};
```

### Action Execution Service

```typescript
// services/actionExecutionService.ts
export class ActionExecutionService {
  
  async executeAction(action: QuickAction, contact: Contact): Promise<void> {
    // Track action for analytics
    this.trackAction(action, contact);
    
    switch (action.type) {
      case 'call':
        await this.initiateCall(contact);
        break;
      case 'email':
        await this.composeEmail(contact);
        break;
      case 'whatsapp':
        await this.openWhatsApp(contact);
        break;
      case 'sms':
        await this.composeSMS(contact);
        break;
      case 'calendar':
        await this.scheduleAppointment(contact);
        break;
      case 'note':
        await this.addQuickNote(contact);
        break;
    }
  }
  
  private async initiateCall(contact: Contact): Promise<void> {
    const phoneNumber = contact.mobile || contact.phone;
    if (!phoneNumber) {
      throw new Error('Keine Telefonnummer verfügbar');
    }
    
    // For mobile apps, use tel: protocol
    if (this.isMobileApp()) {
      window.location.href = `tel:${phoneNumber}`;
    } else {
      // For web, show call dialog
      this.showCallDialog(contact, phoneNumber);
    }
    
    // Record interaction
    await this.recordInteraction(contact.id, 'CALL_INITIATED');
  }
  
  private async composeEmail(contact: Contact): Promise<void> {
    if (!contact.email) {
      throw new Error('Keine E-Mail-Adresse verfügbar');
    }
    
    const subject = `Kontakt bezüglich ${contact.customer?.companyName}`;
    const body = this.generateEmailTemplate(contact);
    
    const mailtoLink = `mailto:${contact.email}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
    
    if (this.isMobileApp()) {
      window.location.href = mailtoLink;
    } else {
      this.showEmailDialog(contact, subject, body);
    }
    
    await this.recordInteraction(contact.id, 'EMAIL_INITIATED');
  }
  
  private async openWhatsApp(contact: Contact): Promise<void> {
    const phoneNumber = contact.mobile?.replace(/[^\d]/g, ''); // Remove formatting
    if (!phoneNumber) {
      throw new Error('Keine Mobilnummer für WhatsApp verfügbar');
    }
    
    const message = this.generateWhatsAppTemplate(contact);
    const whatsappUrl = `https://wa.me/${phoneNumber}?text=${encodeURIComponent(message)}`;
    
    window.open(whatsappUrl, '_blank');
    await this.recordInteraction(contact.id, 'WHATSAPP_INITIATED');
  }
  
  private generateEmailTemplate(contact: Contact): string {
    return `Hallo ${contact.firstName},

ich hoffe, es geht Ihnen gut! 

Bezüglich ${contact.customer?.companyName} wollte ich mich kurz bei Ihnen melden.

Beste Grüße
Ihr FreshPlan Team`;
  }
  
  private generateWhatsAppTemplate(contact: Contact): string {
    return `Hallo ${contact.firstName}, ich wollte mich kurz bezüglich ${contact.customer?.companyName} bei Ihnen melden. Haben Sie kurz Zeit für ein Gespräch?`;
  }
  
  private async recordInteraction(contactId: string, type: string): Promise<void> {
    // Record in backend for warmth calculation
    await fetch(`/api/contacts/${contactId}/interactions`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        type,
        timestamp: new Date().toISOString(),
        source: 'mobile_action'
      })
    });
  }
}
```

## 📈 Progressive Web App Features

### Service Worker for Offline Actions

```typescript
// public/sw.js - Service Worker
self.addEventListener('sync', event => {
  if (event.tag === 'contact-action') {
    event.waitUntil(syncContactActions());
  }
});

async function syncContactActions() {
  const pendingActions = await getStoredActions();
  
  for (const action of pendingActions) {
    try {
      await fetch('/api/contacts/actions', {
        method: 'POST',
        body: JSON.stringify(action)
      });
      await removeStoredAction(action.id);
    } catch (error) {
      console.log('Sync failed, will retry:', error);
    }
  }
}
```

### Push Notifications for Contact Reminders

```typescript
// services/notificationService.ts
export class NotificationService {
  
  async scheduleContactReminder(contact: Contact, reminderType: string): Promise<void> {
    const permission = await Notification.requestPermission();
    if (permission !== 'granted') return;
    
    const message = this.getReminderMessage(contact, reminderType);
    
    // Schedule via service worker
    await navigator.serviceWorker.ready.then(registration => {
      registration.showNotification(`Kontakt-Erinnerung: ${contact.firstName}`, {
        body: message,
        icon: '/icons/contact-reminder-192.png',
        badge: '/icons/badge-72.png',
        actions: [
          { action: 'call', title: 'Anrufen', icon: '/icons/call-24.png' },
          { action: 'email', title: 'E-Mail', icon: '/icons/email-24.png' }
        ],
        data: { contactId: contact.id, type: reminderType }
      });
    });
  }
  
  private getReminderMessage(contact: Contact, type: string): string {
    switch (type) {
      case 'birthday':
        return `🎂 ${contact.firstName} hat heute Geburtstag!`;
      case 'cooling':
        return `⚠️ Kontakt zu ${contact.firstName} kühlt ab`;
      case 'follow_up':
        return `📞 Follow-up mit ${contact.firstName} geplant`;
      default:
        return `Kontakt zu ${contact.firstName} benötigt Aufmerksamkeit`;
    }
  }
}
```

## 🧪 Mobile Testing Strategy

### Touch & Swipe Tests

```typescript
// __tests__/SwipeableContactCard.test.tsx
describe('SwipeableContactCard', () => {
  it('should trigger call action on swipe right', async () => {
    const mockOnAction = jest.fn();
    const contact = createMockContact({ phone: '+49123456789' });
    
    render(<SwipeableContactCard contact={contact} onAction={mockOnAction} />);
    
    // Simulate swipe right
    const card = screen.getByTestId('swipeable-card');
    fireEvent.touchStart(card, { touches: [{ clientX: 0, clientY: 0 }] });
    fireEvent.touchMove(card, { touches: [{ clientX: 100, clientY: 0 }] });
    fireEvent.touchEnd(card);
    
    await waitFor(() => {
      expect(mockOnAction).toHaveBeenCalledWith(
        expect.objectContaining({ type: 'call' })
      );
    });
  });
  
  it('should show contextual actions based on available contact data', () => {
    const contactWithWhatsApp = createMockContact({ 
      mobile: '+49123456789',
      phone: null,
      email: null
    });
    
    render(<SwipeableContactCard contact={contactWithWhatsApp} onAction={jest.fn()} />);
    
    expect(screen.getByText('WhatsApp')).toBeInTheDocument();
    expect(screen.queryByText('E-Mail')).not.toBeInTheDocument();
  });
});
```

### Performance Tests

```typescript
describe('Mobile Performance', () => {
  it('should render contact list within performance budget', async () => {
    const contacts = Array.from({ length: 100 }, createMockContact);
    
    const startTime = performance.now();
    render(<MobileContactList contacts={contacts} />);
    const endTime = performance.now();
    
    expect(endTime - startTime).toBeLessThan(100); // 100ms budget
  });
  
  it('should handle rapid swipe gestures without lag', async () => {
    const mockOnAction = jest.fn();
    render(<SwipeableContactCard contact={mockContact} onAction={mockOnAction} />);
    
    // Simulate rapid swipes
    for (let i = 0; i < 10; i++) {
      fireEvent.touchStart(screen.getByTestId('swipeable-card'));
      fireEvent.touchMove(screen.getByTestId('swipeable-card'));
      fireEvent.touchEnd(screen.getByTestId('swipeable-card'));
    }
    
    // Should not freeze or crash
    expect(screen.getByTestId('swipeable-card')).toBeInTheDocument();
  });
});
```

## 📊 Analytics & Insights

### Action Analytics

```typescript
// Track which actions are most effective
interface ActionAnalytics {
  actionType: string;
  contactId: string;
  timestamp: Date;
  outcome?: 'successful' | 'failed' | 'ignored';
  responseTime?: number; // Time until contact responded
  conversionValue?: number; // If led to sale
}

// Generate insights
export const getActionInsights = (analytics: ActionAnalytics[]) => {
  return {
    mostEffectiveActions: analytics
      .filter(a => a.outcome === 'successful')
      .reduce((acc, a) => {
        acc[a.actionType] = (acc[a.actionType] || 0) + 1;
        return acc;
      }, {}),
    
    averageResponseTime: analytics
      .filter(a => a.responseTime)
      .reduce((sum, a) => sum + a.responseTime!, 0) / analytics.length,
    
    conversionRate: analytics.filter(a => a.conversionValue).length / analytics.length
  };
};
```

## 💡 Desktop Fallback & FAB Integration

### Floating Action Button für Mobile

```typescript
// components/MobileContactFAB.tsx
import { SpeedDial, SpeedDialAction, SpeedDialIcon } from '@mui/material';

export const MobileContactFAB: React.FC<{
  onAddContact: () => void;
  onQuickSearch: () => void;
  onScanCard: () => void;
}> = ({ onAddContact, onQuickSearch, onScanCard }) => {
  const [open, setOpen] = useState(false);
  
  const actions = [
    { 
      icon: <PersonAddIcon />, 
      name: 'Neuer Kontakt',
      onClick: onAddContact 
    },
    { 
      icon: <SearchIcon />, 
      name: 'Schnellsuche',
      onClick: onQuickSearch 
    },
    { 
      icon: <CameraAltIcon />, 
      name: 'Visitenkarte scannen',
      onClick: onScanCard 
    }
  ];
  
  return (
    <SpeedDial
      ariaLabel="Contact Actions"
      sx={{ 
        position: 'fixed', 
        bottom: 24, 
        right: 24,
        '& .MuiSpeedDial-fab': {
          bgcolor: 'primary.main',
          '&:hover': {
            bgcolor: 'primary.dark',
          }
        }
      }}
      icon={<SpeedDialIcon />}
      onClose={() => setOpen(false)}
      onOpen={() => setOpen(true)}
      open={open}
    >
      {actions.map((action) => (
        <SpeedDialAction
          key={action.name}
          icon={action.icon}
          tooltipTitle={action.name}
          onClick={() => {
            action.onClick();
            setOpen(false);
          }}
        />
      ))}
    </SpeedDial>
  );
};
```

### Desktop Context Menu Alternative

```typescript
// components/DesktopContactActions.tsx
export const DesktopContactActions: React.FC<{
  contact: Contact;
  anchorEl: HTMLElement | null;
  onClose: () => void;
  onAction: (action: QuickAction) => void;
}> = ({ contact, anchorEl, onClose, onAction }) => {
  const actionSuggestionService = useActionSuggestionService();
  const suggestedActions = actionSuggestionService.getSuggestedActions(contact);
  
  return (
    <Menu
      anchorEl={anchorEl}
      open={Boolean(anchorEl)}
      onClose={onClose}
      PaperProps={{
        sx: { 
          minWidth: 280,
          maxWidth: 320 
        }
      }}
    >
      {/* Primary Actions */}
      <Box px={2} py={1}>
        <Typography variant="caption" color="text.secondary">
          Schnellaktionen
        </Typography>
      </Box>
      
      {suggestedActions.slice(0, 4).map(action => (
        <MenuItem
          key={action.id}
          onClick={() => {
            onAction(action);
            onClose();
          }}
        >
          <ListItemIcon sx={{ color: action.color }}>
            {action.icon}
          </ListItemIcon>
          <ListItemText>{action.label}</ListItemText>
          {action.primary && (
            <Chip label="Empfohlen" size="small" color="primary" />
          )}
        </MenuItem>
      ))}
      
      <Divider />
      
      {/* Secondary Actions */}
      <Box px={2} py={1}>
        <Typography variant="caption" color="text.secondary">
          Weitere Aktionen
        </Typography>
      </Box>
      
      <MenuItem onClick={() => onAction({ type: 'calendar' })}>
        <ListItemIcon>
          <CalendarIcon />
        </ListItemIcon>
        <ListItemText>Termin vereinbaren</ListItemText>
      </MenuItem>
      
      <MenuItem onClick={() => onAction({ type: 'note' })}>
        <ListItemIcon>
          <NoteIcon />
        </ListItemIcon>
        <ListItemText>Notiz hinzufügen</ListItemText>
      </MenuItem>
    </Menu>
  );
};
```

### Unified Action Handler

```typescript
// hooks/useUnifiedActions.ts
export const useUnifiedActions = () => {
  const isMobile = useMediaQuery('(max-width:768px)');
  const { enqueueSnackbar } = useSnackbar();
  
  const executeAction = async (action: QuickAction, contact: Contact) => {
    try {
      // Mobile-specific handling
      if (isMobile) {
        // Haptic feedback on supported devices
        if ('vibrate' in navigator) {
          navigator.vibrate(50);
        }
      }
      
      // Execute action
      await actionExecutionService.executeAction(action, contact);
      
      // Success feedback
      enqueueSnackbar(`${action.label} erfolgreich`, { 
        variant: 'success',
        autoHideDuration: 3000 
      });
      
    } catch (error) {
      // Error handling with retry option
      enqueueSnackbar(`Fehler bei ${action.label}`, { 
        variant: 'error',
        action: (
          <Button color="inherit" onClick={() => executeAction(action, contact)}>
            Wiederholen
          </Button>
        )
      });
    }
  };
  
  return { executeAction, isMobile };
};
```

## 🎯 Success Metrics

### User Experience:
- **Action Speed:** < 2 seconds from swipe to action execution
- **Touch Response:** < 16ms touch response time
- **Error Rate:** < 1% failed action executions
- **Desktop Feature Parity:** 100% actions available

### Business Impact:
- **Contact Frequency:** +25% increase in customer touchpoints
- **Response Time:** 50% faster initial contact response
- **Mobile Usage:** 60% of contact actions via mobile
- **Cross-Device Consistency:** Seamless experience

### Developer Experience:
- **Code Reuse:** 80% shared between mobile/desktop
- **Test Coverage:** > 90% for action handlers
- **Accessibility:** WCAG 2.1 AA compliant

---

**Nächster Schritt:** [→ Integration Testing](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/TESTING_INTEGRATION.md)

**Mobile Actions = Vertriebspower überall! 📱💼🚀**