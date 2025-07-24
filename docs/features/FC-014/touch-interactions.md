# FC-014: Touch Interactions für Mobile & Tablet

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-014-mobile-tablet-optimization.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-014-mobile-tablet-optimization.md)  
**Fokus:** Touch-Gesten und Mobile UX

## 🖐️ Touch-Gesten Katalog

### Pipeline Kanban Board (M4)
```typescript
// Touch-optimiertes Drag & Drop
const TOUCH_DRAG_CONFIG = {
  activationConstraint: {
    delay: 100,        // Kurzer Hold zum Aktivieren
    tolerance: 5       // Pixel-Toleranz
  },
  sensors: [
    useSensor(TouchSensor, {
      activationConstraint: {
        delay: 100,
        tolerance: 5
      }
    }),
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8
      }
    })
  ]
};

// Swipe Actions auf Opportunity Cards
const swipeHandlers = {
  onSwipeLeft: () => showQuickActions(),
  onSwipeRight: () => markAsWon(),
  onLongPress: () => openDetailModal()
};
```

### Quick Actions (FC-013)
```typescript
// Touch-optimierte Action Buttons
const TouchActionButton: React.FC<{action: QuickAction}> = ({action}) => {
  return (
    <Button
      sx={{
        minWidth: 44,      // Apple HIG minimum
        minHeight: 44,     // Touch target size
        padding: '12px',
        fontSize: '16px',  // Prevent zoom on iOS
        touchAction: 'manipulation'
      }}
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >
      {action.icon}
      <Typography variant="caption">{action.label}</Typography>
    </Button>
  );
};
```

## 📱 Mobile-spezifische Patterns

### 1. Bottom Sheet für Aktionen
```typescript
// Statt Dropdown-Menüs
export const MobileActionSheet: React.FC = () => {
  return (
    <SwipeableDrawer
      anchor="bottom"
      open={open}
      onClose={handleClose}
      onOpen={handleOpen}
      disableBackdropTransition={!iOS}
      disableDiscovery={iOS}
    >
      <Box sx={{ 
        pb: 'env(safe-area-inset-bottom)', // iPhone Notch
        borderTopLeftRadius: 12,
        borderTopRightRadius: 12
      }}>
        {actions.map(action => (
          <ListItem button onClick={action.handler}>
            <ListItemIcon>{action.icon}</ListItemIcon>
            <ListItemText primary={action.label} />
          </ListItem>
        ))}
      </Box>
    </SwipeableDrawer>
  );
};
```

### 2. Pull-to-Refresh
```typescript
const usePullToRefresh = (onRefresh: () => Promise<void>) => {
  const [isPulling, setIsPulling] = useState(false);
  const startY = useRef(0);
  
  const handleTouchStart = (e: TouchEvent) => {
    if (window.scrollY === 0) {
      startY.current = e.touches[0].pageY;
    }
  };
  
  const handleTouchMove = (e: TouchEvent) => {
    const pullDistance = e.touches[0].pageY - startY.current;
    if (pullDistance > 50 && window.scrollY === 0) {
      setIsPulling(true);
    }
  };
  
  const handleTouchEnd = async () => {
    if (isPulling) {
      await onRefresh();
      setIsPulling(false);
    }
  };
};
```

### 3. Haptic Feedback
```typescript
// Vibration API für Feedback
const hapticFeedback = {
  light: () => navigator.vibrate?.(10),
  medium: () => navigator.vibrate?.(20),
  heavy: () => navigator.vibrate?.(30),
  success: () => navigator.vibrate?.([10, 10, 10]),
  error: () => navigator.vibrate?.([50, 50, 50])
};

// Usage in Actions
const handleQuickAction = async (action: QuickAction) => {
  hapticFeedback.light();
  try {
    await executeAction(action);
    hapticFeedback.success();
  } catch (error) {
    hapticFeedback.error();
  }
};
```

## 🎯 Touch-Optimierung nach Feature

### M4 Opportunity Pipeline
- **Drag Threshold:** 10px für Touch (vs 5px Desktop)
- **Card Size:** Min-Height 60px auf Mobile
- **Stage Scrolling:** Horizontal scroll mit Snap Points
- **Quick Actions:** Swipe oder Long-Press

### M8 Calculator
- **Number Pad:** Native inputmode="decimal"
- **Stepper Controls:** +/- Buttons statt Input
- **Preset Buttons:** Häufige Werte (10, 50, 100)
- **Summary:** Sticky bottom auf Mobile

### FC-013 Activities
- **Timeline:** Infinite Scroll statt Pagination
- **Quick Checkboxes:** 44x44px Touch Targets
- **Voice Notes:** Audio Recording Option
- **Attachments:** Camera/Gallery Integration

### FC-011 Cockpit Integration
- **Split View:** 
  - Mobile: Stacked (Pipeline → Cockpit)
  - Tablet: Side-by-side (anpassbar)
- **Navigation:** Swipe-Back Gesture
- **Context Switch:** Animated Transitions

## 📏 Touch Guidelines

### Minimum Sizes
```scss
// Touch target minimums
$touch-target-min: 44px;  // iOS HIG
$touch-target-android: 48px; // Material Design

// Spacing
$touch-spacing-min: 8px;
$touch-spacing-comfortable: 16px;

// Font sizes
$font-size-mobile-min: 16px; // Prevents zoom
$font-size-mobile-body: 14px;
$font-size-mobile-caption: 12px;
```

### Gesture Zones
```typescript
// Safe zones für System-Gesten
const SYSTEM_GESTURE_ZONES = {
  top: 44,           // Status bar
  bottom: 34,        // Home indicator (iPhone X+)
  sides: 20          // Back gesture (Android/iOS)
};

// App-Gesten nur in sicheren Bereichen
const isSafeZone = (x: number, y: number): boolean => {
  return x > SYSTEM_GESTURE_ZONES.sides &&
         x < window.innerWidth - SYSTEM_GESTURE_ZONES.sides &&
         y > SYSTEM_GESTURE_ZONES.top &&
         y < window.innerHeight - SYSTEM_GESTURE_ZONES.bottom;
};
```

## 🔧 Implementation Helper

### Touch vs Mouse Detection
```typescript
export const useInputMethod = () => {
  const [inputMethod, setInputMethod] = useState<'touch' | 'mouse'>('mouse');
  
  useEffect(() => {
    let lastTouchTime = 0;
    
    const handleTouch = () => {
      lastTouchTime = Date.now();
      setInputMethod('touch');
    };
    
    const handleMouse = (e: MouseEvent) => {
      // Ignore mouse events shortly after touch
      if (Date.now() - lastTouchTime > 500) {
        setInputMethod('mouse');
      }
    };
    
    document.addEventListener('touchstart', handleTouch);
    document.addEventListener('mousemove', handleMouse);
    
    return () => {
      document.removeEventListener('touchstart', handleTouch);
      document.removeEventListener('mousemove', handleMouse);
    };
  }, []);
  
  return inputMethod;
};
```

## ⚡ Performance Tipps

1. **Passive Event Listeners** für Scroll-Performance
2. **will-change: transform** für Animationen
3. **contain: layout** für Render-Optimierung
4. **requestAnimationFrame** für Touch-Tracking
5. **Debounced Updates** bei Drag-Operationen