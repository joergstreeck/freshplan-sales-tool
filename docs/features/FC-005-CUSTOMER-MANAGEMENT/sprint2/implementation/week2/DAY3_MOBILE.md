# 📆 Tag 3: Mobile Quick Actions

**Datum:** Mittwoch, 14. August 2025  
**Fokus:** Mobile UI & Touch Optimization  
**Ziel:** Mobile-First Quick Actions für Außendienst  

## 🧭 Navigation

**← Vorheriger Tag:** [Tag 2: Crypto](./DAY2_CRYPTO.md)  
**↑ Woche 2 Übersicht:** [README.md](./README.md)  
**→ Nächster Tag:** [Tag 4: Offline](./DAY4_OFFLINE.md)  
**📘 Spec:** [Mobile Specification](./specs/MOBILE_SPEC.md)  

## 🎯 Tagesziel

- Frontend: Quick Action Menu mit Touch-Gesten
- UI: Speed Dial Component
- UX: Swipe Actions implementieren
- Testing: Mobile Device Testing

## 📱 Mobile Design Principles

1. **Touch Targets:** Minimum 44px (iOS) / 48px (Android)
2. **Swipe Gestures:** Natürliche Interaktionen
3. **One-Handed Use:** Wichtige Actions unten
4. **Haptic Feedback:** Bei allen Aktionen

## 🎨 Frontend Implementation

### 1. Quick Action Menu

```typescript
// components/mobile/QuickActionMenu.tsx
export const QuickActionMenu: React.FC<QuickActionMenuProps> = ({
  contact,
  onAction
}) => {
  const [expanded, setExpanded] = useState(false);
  const isMobile = useMediaQuery('(max-width:600px)');
  
  const primaryAction = useMemo(() => {
    // Intelligente Haupt-Aktion basierend auf letzter Interaktion
    const lastInteraction = contact.lastInteraction;
    if (!lastInteraction) return 'call';
    
    const daysSince = daysBetween(lastInteraction, new Date());
    if (daysSince > 30) return 'email'; // Lange nicht gesprochen
    if (daysSince > 7) return 'call';   // Wöchentlicher Check
    return 'note';                       // Kürzlich gesprochen
  }, [contact]);
  
  return (
    <SpeedDial
      ariaLabel="Quick Actions"
      sx={{ position: 'fixed', bottom: 16, right: 16 }}
      icon={<SpeedDialIcon />}
      onClose={() => setExpanded(false)}
      onOpen={() => setExpanded(true)}
      open={expanded}
    >
      <SpeedDialAction
        key="call"
        icon={<PhoneIcon />}
        tooltipTitle="Anrufen"
        onClick={() => {
          onAction('call', contact);
          setExpanded(false);
        }}
        sx={{
          backgroundColor: primaryAction === 'call' ? 'primary.main' : undefined
        }}
      />
      
      <SpeedDialAction
        key="email"
        icon={<EmailIcon />}
        tooltipTitle="E-Mail senden"
        onClick={() => {
          onAction('email', contact);
          setExpanded(false);
        }}
      />
      
      <SpeedDialAction
        key="whatsapp"
        icon={<WhatsAppIcon />}
        tooltipTitle="WhatsApp"
        onClick={() => {
          onAction('whatsapp', contact);
          setExpanded(false);
        }}
      />
    </SpeedDial>
  );
};
```

**Vollständiger Code:** [frontend/QuickActionMenu.tsx](./code/frontend/mobile/QuickActionMenu.tsx)

### 2. Swipe Actions

```typescript
// hooks/useSwipeActions.ts
export const useSwipeActions = () => {
  const handleSwipe = useCallback((direction: 'left' | 'right', contact: Contact) => {
    const actions = {
      left: () => archiveContact(contact),
      right: () => initiateCall(contact)
    };
    
    actions[direction]();
    
    // Haptic feedback on mobile
    if ('vibrate' in navigator) {
      navigator.vibrate(50);
    }
  }, []);
  
  return { handleSwipe };
};
```

### 3. Mobile Contact Card

```typescript
// components/mobile/MobileContactCard.tsx
export const MobileContactCard: React.FC<MobileContactCardProps> = ({ contact }) => {
  const [{ x }, api] = useSpring(() => ({ x: 0 }));
  const { handleSwipe } = useSwipeActions();
  
  const bind = useDrag(({ down, movement: [mx], velocity }) => {
    const trigger = velocity > 0.2;
    const dir = mx > 0 ? 'right' : 'left';
    
    if (!down && trigger && Math.abs(mx) > 50) {
      handleSwipe(dir, contact);
    }
    
    api.start({
      x: down ? mx : 0,
      immediate: down
    });
  });
  
  return (
    <animated.div
      {...bind()}
      style={{
        transform: x.to(x => `translateX(${x}px)`),
        touchAction: 'pan-y'
      }}
    >
      <Card sx={{ mb: 2, minHeight: 100 }}>
        {/* Card content... */}
      </Card>
    </animated.div>
  );
};
```

## 📱 Mobile-Specific Features

### 1. Bottom Sheet Actions

```typescript
// components/mobile/ContactActionSheet.tsx
export const ContactActionSheet: React.FC = ({ contact, open, onClose }) => {
  return (
    <SwipeableDrawer
      anchor="bottom"
      open={open}
      onClose={onClose}
      onOpen={() => {}}
      disableSwipeToOpen={false}
      ModalProps={{ keepMounted: true }}
    >
      <Box sx={{ pb: 2 }}>
        <ActionList>
          <ActionItem icon={<PhoneIcon />} label="Anrufen" />
          <ActionItem icon={<EmailIcon />} label="E-Mail" />
          <ActionItem icon={<CalendarIcon />} label="Termin" />
          <ActionItem icon={<NoteIcon />} label="Notiz" />
        </ActionList>
      </Box>
    </SwipeableDrawer>
  );
};
```

### 2. Touch-Optimized Forms

```typescript
// Minimum touch target sizes
const TouchButton = styled(Button)(({ theme }) => ({
  minHeight: 48,
  minWidth: 48,
  padding: theme.spacing(1.5, 2),
  '& .MuiButton-startIcon': {
    marginRight: theme.spacing(1.5)
  }
}));
```

## 🧪 Mobile Testing

### Device Testing Checklist

```typescript
// __tests__/mobile.test.tsx
describe('Mobile Quick Actions', () => {
  it('should show correct primary action', () => {
    const contact = {
      lastInteraction: subDays(new Date(), 35)
    };
    
    const { getByLabelText } = render(
      <QuickActionMenu contact={contact} onAction={jest.fn()} />
    );
    
    // Should suggest email for old contacts
    expect(getByLabelText('E-Mail senden')).toHaveStyle({
      backgroundColor: 'primary.main'
    });
  });
  
  it('should handle swipe gestures', async () => {
    const onSwipe = jest.fn();
    const { container } = render(
      <MobileContactCard contact={mockContact} onSwipe={onSwipe} />
    );
    
    // Simulate swipe right
    fireEvent.touchStart(container.firstChild, {
      touches: [{ clientX: 0 }]
    });
    fireEvent.touchMove(container.firstChild, {
      touches: [{ clientX: 100 }]
    });
    fireEvent.touchEnd(container.firstChild);
    
    expect(onSwipe).toHaveBeenCalledWith('right', mockContact);
  });
});
```

## 📱 Performance Optimierungen

### 1. Touch Response
```css
/* Prevent 300ms delay */
touch-action: manipulation;

/* Smooth scrolling */
-webkit-overflow-scrolling: touch;
```

### 2. Viewport Settings
```html
<meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
```

## 📝 Checkliste

- [ ] Quick Action Menu implementiert
- [ ] Swipe Gestures funktionieren
- [ ] Touch Targets ≥ 44px
- [ ] Haptic Feedback eingebaut
- [ ] Bottom Sheet Actions
- [ ] Mobile Forms optimiert
- [ ] Device Testing durchgeführt

## 🔗 Weiterführende Links

- **Mobile Patterns:** [Mobile UI Best Practices](./guides/MOBILE_PATTERNS.md)
- **Touch Guidelines:** [Touch Interaction Guide](./guides/TOUCH_GUIDELINES.md)
- **Nächster Schritt:** [→ Tag 4: Offline Queue](./DAY4_OFFLINE.md)

---

**Status:** 📋 Bereit zur Implementierung