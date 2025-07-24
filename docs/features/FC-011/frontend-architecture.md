# FC-011: Frontend-Architektur für Pipeline-Cockpit Integration

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-011-pipeline-cockpit-integration.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-011-pipeline-cockpit-integration.md)  
**Fokus:** React-Komponenten und State Management

## 🏗️ Komponenten-Struktur

### 1. Enhanced Pipeline Card Component

```typescript
// frontend/src/features/opportunity/components/OpportunityCard.tsx
interface OpportunityCardProps {
  opportunity: Opportunity;
  onSingleClick?: (opportunity: Opportunity) => void;
  onDoubleClick?: (opportunity: Opportunity) => void;
  onContextMenu?: (e: React.MouseEvent, opportunity: Opportunity) => void;
  isActive?: boolean;
  displayMode: 'compact' | 'standard' | 'detailed';
}

export const OpportunityCard: React.FC<OpportunityCardProps> = ({
  opportunity,
  onSingleClick,
  onDoubleClick,
  onContextMenu,
  isActive,
  displayMode
}) => {
  const [clickTimeout, setClickTimeout] = useState<NodeJS.Timeout | null>(null);
  
  const handleClick = (e: React.MouseEvent) => {
    e.preventDefault();
    
    if (clickTimeout) {
      // Double click detected
      clearTimeout(clickTimeout);
      setClickTimeout(null);
      onDoubleClick?.(opportunity);
    } else {
      // Single click - wait to see if double click follows
      const timeout = setTimeout(() => {
        onSingleClick?.(opportunity);
        setClickTimeout(null);
      }, 250);
      setClickTimeout(timeout);
    }
  };

  return (
    <Card
      className={cn(
        'opportunity-card',
        isActive && 'opportunity-card--active',
        `opportunity-card--${displayMode}`
      )}
      onClick={handleClick}
      onContextMenu={(e) => {
        e.preventDefault();
        onContextMenu?.(e, opportunity);
      }}
    >
      {/* Card Content based on displayMode */}
    </Card>
  );
};
```

### 2. Cockpit Context Provider

```typescript
// frontend/src/contexts/CockpitContext.tsx
interface CockpitContextValue {
  activeOpportunity: Opportunity | null;
  activeCustomer: Customer | null;
  loadingState: LoadingState;
  viewMode: ViewMode;
  history: OpportunityHistory[];
  
  // Actions
  loadCustomerFromOpportunity: (opportunityId: string) => Promise<void>;
  setViewMode: (mode: ViewMode) => void;
  navigateBack: () => void;
  navigateForward: () => void;
  clearCockpit: () => void;
}

export const CockpitProvider: React.FC<PropsWithChildren> = ({ children }) => {
  const [state, dispatch] = useReducer(cockpitReducer, initialState);
  const queryClient = useQueryClient();
  
  const loadCustomerFromOpportunity = async (opportunityId: string) => {
    dispatch({ type: 'LOAD_START' });
    
    try {
      // Fetch opportunity details
      const opportunity = await opportunityApi.getById(opportunityId);
      
      // Prefetch related data
      const [customer, activities, documents] = await Promise.all([
        customerApi.getById(opportunity.customerId),
        activityApi.getByCustomerId(opportunity.customerId),
        documentApi.getByCustomerId(opportunity.customerId)
      ]);
      
      // Cache in React Query
      queryClient.setQueryData(['customer', customer.id], customer);
      queryClient.setQueryData(['activities', customer.id], activities);
      queryClient.setQueryData(['documents', customer.id], documents);
      
      dispatch({
        type: 'LOAD_SUCCESS',
        payload: { opportunity, customer }
      });
      
      // Add to history
      dispatch({ type: 'ADD_TO_HISTORY', payload: opportunity });
      
    } catch (error) {
      dispatch({ type: 'LOAD_ERROR', payload: error });
    }
  };
  
  return (
    <CockpitContext.Provider value={{
      ...state,
      loadCustomerFromOpportunity,
      // ... other actions
    }}>
      {children}
    </CockpitContext.Provider>
  );
};
```

### 3. Split-View Container

```typescript
// frontend/src/components/layout/SplitViewContainer.tsx
interface SplitViewContainerProps {
  leftPanel: React.ReactNode;
  rightPanel: React.ReactNode;
  defaultRatio?: number; // 0.3 = 30% left, 70% right
  minLeftWidth?: number;
  minRightWidth?: number;
  onRatioChange?: (ratio: number) => void;
}

export const SplitViewContainer: React.FC<SplitViewContainerProps> = ({
  leftPanel,
  rightPanel,
  defaultRatio = 0.3,
  minLeftWidth = 300,
  minRightWidth = 500,
  onRatioChange
}) => {
  const [ratio, setRatio] = useState(defaultRatio);
  const containerRef = useRef<HTMLDivElement>(null);
  const [isDragging, setIsDragging] = useState(false);
  
  const handleMouseMove = (e: MouseEvent) => {
    if (!isDragging || !containerRef.current) return;
    
    const containerWidth = containerRef.current.offsetWidth;
    const newLeftWidth = e.clientX - containerRef.current.offsetLeft;
    const newRatio = newLeftWidth / containerWidth;
    
    // Enforce minimum widths
    const minRatio = minLeftWidth / containerWidth;
    const maxRatio = 1 - (minRightWidth / containerWidth);
    
    const clampedRatio = Math.max(minRatio, Math.min(maxRatio, newRatio));
    setRatio(clampedRatio);
    onRatioChange?.(clampedRatio);
  };
  
  return (
    <div ref={containerRef} className="split-view-container">
      <div 
        className="split-view-left" 
        style={{ width: `${ratio * 100}%` }}
      >
        {leftPanel}
      </div>
      
      <div 
        className="split-view-divider"
        onMouseDown={() => setIsDragging(true)}
      >
        <div className="split-view-divider-handle" />
      </div>
      
      <div 
        className="split-view-right"
        style={{ width: `${(1 - ratio) * 100}%` }}
      >
        {rightPanel}
      </div>
    </div>
  );
};
```

### 4. Preloading Hook

```typescript
// frontend/src/features/opportunity/hooks/useOpportunityPreload.ts
export const useOpportunityPreload = () => {
  const queryClient = useQueryClient();
  const [preloadQueue, setPreloadQueue] = useState<Set<string>>(new Set());
  
  const preloadOpportunity = useCallback((opportunityId: string) => {
    if (preloadQueue.has(opportunityId)) return;
    
    setPreloadQueue(prev => new Set(prev).add(opportunityId));
    
    // Debounced preload
    setTimeout(() => {
      queryClient.prefetchQuery({
        queryKey: ['opportunity', opportunityId],
        queryFn: () => opportunityApi.getById(opportunityId),
        staleTime: 5 * 60 * 1000, // 5 minutes
      });
      
      setPreloadQueue(prev => {
        const next = new Set(prev);
        next.delete(opportunityId);
        return next;
      });
    }, 500);
  }, [queryClient, preloadQueue]);
  
  return { preloadOpportunity };
};
```

### 5. Context Menu Component

```typescript
// frontend/src/components/common/ContextMenu.tsx
interface ContextMenuItem {
  id: string;
  label: string;
  icon?: React.ReactNode;
  shortcut?: string;
  action: () => void;
  divider?: boolean;
  disabled?: boolean;
}

export const OpportunityContextMenu: React.FC<{
  opportunity: Opportunity;
  position: { x: number; y: number };
  onClose: () => void;
}> = ({ opportunity, position, onClose }) => {
  const { loadCustomerIntoCockpit } = useCockpit();
  const { openEmailModal } = useEmailIntegration();
  
  const menuItems: ContextMenuItem[] = [
    {
      id: 'load',
      label: 'Im Cockpit öffnen',
      icon: <IconCockpit />,
      shortcut: 'Space',
      action: () => loadCustomerIntoCockpit(opportunity.id)
    },
    {
      id: 'details',
      label: 'Details anzeigen',
      icon: <IconDetails />,
      shortcut: 'D',
      action: () => openOpportunityModal(opportunity.id)
    },
    { divider: true },
    {
      id: 'email',
      label: 'E-Mail senden',
      icon: <IconEmail />,
      shortcut: 'E',
      action: () => openEmailModal(opportunity.customerId)
    },
    {
      id: 'note',
      label: 'Notiz hinzufügen',
      icon: <IconNote />,
      shortcut: 'N',
      action: () => openNoteModal(opportunity.id)
    },
    { divider: true },
    {
      id: 'won',
      label: 'Als gewonnen markieren',
      icon: <IconCheck />,
      shortcut: 'G',
      action: () => markAsWon(opportunity.id),
      disabled: opportunity.stage === 'CLOSED_WON'
    },
    {
      id: 'lost',
      label: 'Als verloren markieren',
      icon: <IconX />,
      shortcut: 'L',
      action: () => markAsLost(opportunity.id),
      disabled: opportunity.stage === 'CLOSED_LOST'
    }
  ];
  
  return (
    <ContextMenu
      items={menuItems}
      position={position}
      onClose={onClose}
    />
  );
};
```

## 🎨 CSS Architecture

```scss
// frontend/src/styles/features/_pipeline-cockpit.scss

// Active opportunity card
.opportunity-card {
  transition: all 0.2s ease;
  cursor: pointer;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  &--active {
    border: 2px solid var(--primary-color);
    background: var(--active-bg);
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: var(--primary-color);
    }
  }
  
  // Display modes
  &--compact {
    padding: 8px 12px;
    
    .opportunity-details {
      display: none;
    }
  }
  
  &--standard {
    padding: 12px 16px;
  }
  
  &--detailed {
    padding: 16px 20px;
    
    .opportunity-timeline {
      display: block;
    }
  }
}

// Split view
.split-view-container {
  display: flex;
  height: 100%;
  
  .split-view-divider {
    width: 8px;
    background: var(--border-color);
    cursor: col-resize;
    position: relative;
    
    &:hover,
    &.dragging {
      background: var(--primary-color);
      
      .split-view-divider-handle {
        opacity: 1;
      }
    }
    
    &-handle {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 4px;
      height: 40px;
      background: var(--text-secondary);
      border-radius: 2px;
      opacity: 0.5;
      transition: opacity 0.2s;
    }
  }
}

// Loading states
.cockpit-loading {
  @keyframes pulse {
    0% { opacity: 0.6; }
    50% { opacity: 1; }
    100% { opacity: 0.6; }
  }
  
  .skeleton {
    animation: pulse 1.5s infinite;
    background: var(--skeleton-bg);
    border-radius: 4px;
  }
}
```

## 🔗 Integration Points

### Mit M4 Opportunity Pipeline
- Erweitert `OpportunityCard` Component
- Nutzt bestehende `OpportunityApi`
- Teilt Filter-State mit FC-010

### Mit M5 Customer Cockpit
- Lädt Daten in `CustomerCockpit` Component
- Erweitert `CustomerApi` für Context-Loading
- Nutzt bestehende Activity/Document APIs

### Mit FC-003 Email Integration
- Context-basierte Email-Actions
- Quick-Email aus Kontextmenü
- Template-Vorauswahl basierend auf Stage

## 📊 Performance Metriken

```typescript
// Tracking für User Experience
interface PerformanceMetrics {
  clickToLoadTime: number; // Target: < 500ms
  preloadHitRate: number; // Target: > 60%
  contextSwitchTime: number; // Target: < 200ms
  memoryFootprint: number; // Target: < 50MB additional
}
```