# FC-017: Frontend Components - Error Handling System

**Parent:** [FC-017 Error Handling System](../2025-07-25_TECH_CONCEPT_FC-017-error-handling-system.md)  
**Datum:** 2025-07-25  
**Status:** Draft  

## 📋 Übersicht

Dieses Dokument beschreibt die Frontend-Komponenten für das Error Handling System mit Fokus auf:
- Sichtbare Statusanzeigen auf UI-Komponenten
- User-freundliche Fehlermeldungen
- Recovery-Aktionen
- Offline-Mode-Handling

## 🎯 Komponenten-Hierarchie

```
ErrorHandlingComponents/
├── Core/
│   ├── ErrorBoundary.tsx
│   ├── GlobalErrorHandler.tsx
│   └── ErrorContext.tsx
├── StatusIndicators/
│   ├── StatusIndicator.tsx
│   ├── SyncStatusBadge.tsx
│   └── ServiceHealthIndicator.tsx
├── Notifications/
│   ├── ErrorNotification.tsx
│   ├── OfflineNotification.tsx
│   └── RecoveryNotification.tsx
└── Cards/
    ├── OpportunityCardWithStatus.tsx
    ├── CustomerCardWithStatus.tsx
    └── ContractCardWithStatus.tsx
```

## 🔧 Core Components

### ErrorBoundary.tsx

```tsx
interface ErrorBoundaryProps {
  fallback?: React.ComponentType<ErrorFallbackProps>;
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
  isolate?: boolean; // Isoliert Fehler auf Komponenten-Ebene
}

interface ErrorFallbackProps {
  error: Error;
  resetError: () => void;
  errorInfo?: ErrorInfo;
}

// Verwendung:
<ErrorBoundary 
  fallback={CustomErrorFallback}
  onError={(error) => errorTracker.log(error)}
  isolate
>
  <RiskyComponent />
</ErrorBoundary>
```

### GlobalErrorHandler.tsx

```tsx
const GlobalErrorHandler: React.FC = () => {
  const { errors, clearError } = useErrorContext();
  const notifications = useNotifications();
  
  useEffect(() => {
    errors.forEach(error => {
      if (error.severity === 'critical') {
        notifications.showBlockingError(error);
      } else {
        notifications.showToast(error);
      }
    });
  }, [errors]);
  
  return null; // Invisible component
};
```

## 📊 Status Indicators

### StatusIndicator.tsx

```tsx
interface StatusIndicatorProps {
  status: 'synced' | 'syncing' | 'error' | 'offline' | 'warning';
  size?: 'small' | 'medium' | 'large';
  showLabel?: boolean;
  lastSync?: Date;
  errorDetails?: ErrorDetails;
  onRetry?: () => void;
  position?: 'inline' | 'absolute';
}

const StatusIndicator: React.FC<StatusIndicatorProps> = ({
  status,
  size = 'medium',
  showLabel = true,
  lastSync,
  errorDetails,
  onRetry,
  position = 'inline'
}) => {
  // Komponente mit:
  // - Animierte Icons (Spinner für syncing)
  // - Tooltip mit Details
  // - Retry-Button bei Fehlern
  // - Relative Zeit-Anzeige
};
```

### SyncStatusBadge.tsx

```tsx
interface SyncStatusBadgeProps {
  entityType: 'opportunity' | 'customer' | 'contract';
  entityId: string;
  compact?: boolean;
}

// Zeigt Sync-Status für einzelne Entitäten
// - Grün: Synchronisiert
// - Gelb: Lokale Änderungen
// - Rot: Sync-Fehler
// - Grau: Offline
```

## 🎨 Card Integration

### OpportunityCardWithStatus.tsx

```tsx
const OpportunityCardWithStatus: React.FC<OpportunityCardProps> = ({ 
  opportunity 
}) => {
  const { syncStatus, syncError, retry } = useEntitySync(
    'opportunity', 
    opportunity.id
  );
  
  return (
    <Card 
      className={clsx(
        styles.card,
        syncError && styles.errorBorder,
        opportunity.hasLocalChanges && styles.warningBorder
      )}
    >
      <CardHeader>
        <Box display="flex" justifyContent="space-between">
          <Typography variant="h6">{opportunity.title}</Typography>
          <StatusIndicator 
            status={syncStatus}
            size="small"
            lastSync={opportunity.lastSyncedAt}
            onRetry={retry}
          />
        </Box>
      </CardHeader>
      
      <CardContent>
        {/* Opportunity Details */}
        
        {syncError && (
          <Alert severity="error" className={styles.syncAlert}>
            <AlertTitle>Synchronisation fehlgeschlagen</AlertTitle>
            {syncError.userMessage}
            <Button size="small" onClick={retry}>
              Erneut versuchen
            </Button>
          </Alert>
        )}
        
        {opportunity.hasLocalChanges && (
          <Chip
            icon={<CloudOffIcon />}
            label="Lokale Änderungen"
            size="small"
            color="warning"
          />
        )}
      </CardContent>
    </Card>
  );
};
```

## 🔔 Notification Components

### ErrorNotification.tsx

```tsx
interface ErrorNotificationProps {
  error: AppError;
  variant?: 'toast' | 'alert' | 'modal';
  autoHideDuration?: number | null;
  action?: NotificationAction;
}

const ErrorNotification: React.FC<ErrorNotificationProps> = ({
  error,
  variant = 'toast',
  autoHideDuration = 6000,
  action
}) => {
  // Intelligente Fehlermeldungen:
  // - User-freundliche Texte
  // - Kontext-sensitive Aktionen
  // - Wichtigkeit bestimmt Darstellung
  // - Error-ID für Support
};
```

### OfflineNotification.tsx

```tsx
const OfflineNotification: React.FC = () => {
  const { isOnline, pendingChanges, serviceStatus } = useOfflineMode();
  
  if (isOnline && serviceStatus.allHealthy) return null;
  
  return (
    <Snackbar
      open={true}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
    >
      <Alert severity="warning" variant="filled">
        <AlertTitle>Eingeschränkter Betrieb</AlertTitle>
        {!isOnline ? (
          <>Keine Internetverbindung - Änderungen werden lokal gespeichert</>
        ) : (
          <>Einige Services nicht erreichbar: {getUnavailableServices()}</>
        )}
        {pendingChanges > 0 && (
          <Typography variant="caption" display="block">
            {pendingChanges} Änderungen warten auf Synchronisation
          </Typography>
        )}
      </Alert>
    </Snackbar>
  );
};
```

## 🏗️ Service Health Dashboard

### ServiceHealthDashboard.tsx

```tsx
const ServiceHealthDashboard: React.FC = () => {
  const { services, lastCheck, refresh } = useServiceHealth();
  
  return (
    <Paper className={styles.dashboard}>
      <Box display="flex" justifyContent="space-between" p={2}>
        <Typography variant="h6">System Status</Typography>
        <Box>
          <Typography variant="caption" color="textSecondary">
            Letzte Prüfung: {formatRelativeTime(lastCheck)}
          </Typography>
          <IconButton onClick={refresh} size="small">
            <RefreshIcon />
          </IconButton>
        </Box>
      </Box>
      
      <Divider />
      
      <Grid container spacing={2} p={2}>
        <ServiceHealthGrid services={services} />
      </Grid>
      
      {hasAnyServiceDown(services) && (
        <Box p={2} bgcolor="warning.light">
          <Typography variant="body2" color="warning.dark">
            ⚠️ Das System arbeitet im eingeschränkten Modus. 
            Einige Funktionen sind möglicherweise nicht verfügbar.
          </Typography>
        </Box>
      )}
    </Paper>
  );
};
```

## 🎨 Styling & Themes

### Error States Styling

```scss
// styles/error-states.module.scss
.errorBorder {
  border: 2px solid $error-color;
  animation: pulse-error 2s infinite;
}

.warningBorder {
  border: 2px solid $warning-color;
}

.syncAlert {
  margin-top: 16px;
  animation: slide-in 0.3s ease-out;
}

@keyframes pulse-error {
  0%, 100% { border-color: $error-color; }
  50% { border-color: lighten($error-color, 20%); }
}

@keyframes slide-in {
  from { 
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

## 🔌 Hooks

### useEntitySync

```tsx
interface EntitySyncState {
  syncStatus: SyncStatus;
  syncError: SyncError | null;
  lastSync: Date | null;
  retry: () => void;
  clearError: () => void;
}

function useEntitySync(
  entityType: EntityType,
  entityId: string
): EntitySyncState {
  // Verwaltet Sync-Status für einzelne Entitäten
  // Integriert mit globalem Error Handler
  // Automatische Retry-Logik
}
```

### useOfflineMode

```tsx
interface OfflineModeState {
  isOnline: boolean;
  serviceStatus: ServiceHealthStatus;
  pendingChanges: number;
  syncQueue: SyncQueueItem[];
  forceSyncNow: () => Promise<void>;
}

function useOfflineMode(): OfflineModeState {
  // Überwacht Online/Offline Status
  // Verwaltet lokale Änderungs-Queue
  // Trigger für automatische Synchronisation
}
```

## 📊 Performance Considerations

### Optimierungen

1. **Status Updates throttlen**
   - Max 1 Update pro Sekunde
   - Batch Updates für mehrere Entitäten

2. **Lazy Loading für Error Details**
   - Details nur bei Bedarf laden
   - Caching von Error Messages

3. **Memoization**
   - Status Icons memoizen
   - Error Messages cachen

## 🧪 Testing Strategy

### Unit Tests

```tsx
describe('StatusIndicator', () => {
  it('should show correct icon for each status', () => {
    const { rerender } = render(<StatusIndicator status="synced" />);
    expect(screen.getByTestId('check-icon')).toBeInTheDocument();
    
    rerender(<StatusIndicator status="error" />);
    expect(screen.getByTestId('error-icon')).toBeInTheDocument();
  });
  
  it('should show retry button on error', () => {
    const onRetry = jest.fn();
    render(<StatusIndicator status="error" onRetry={onRetry} />);
    
    fireEvent.click(screen.getByRole('button', { name: /retry/i }));
    expect(onRetry).toHaveBeenCalled();
  });
});
```

### Integration Tests

```tsx
describe('OpportunityCard with Error Handling', () => {
  it('should show sync error when API fails', async () => {
    server.use(
      rest.put('/api/opportunities/:id', (req, res, ctx) => {
        return res(ctx.status(500), ctx.json({ error: 'Server Error' }));
      })
    );
    
    render(<OpportunityCardWithStatus opportunity={mockOpportunity} />);
    
    // Trigger update
    fireEvent.click(screen.getByRole('button', { name: /save/i }));
    
    await waitFor(() => {
      expect(screen.getByText(/Synchronisation fehlgeschlagen/i))
        .toBeInTheDocument();
    });
  });
});
```

## 🔄 Migration Strategy

### Schrittweise Integration

1. **Phase 1**: Error Boundary um App
2. **Phase 2**: Status Indicators in Cards
3. **Phase 3**: Offline Mode Provider
4. **Phase 4**: Service Health Dashboard

### Backward Compatibility

- Alte Komponenten funktionieren weiter
- Opt-in für neue Features
- Graduelle Migration möglich

---

**Nächste Schritte:**
- [ ] Component Library in Storybook dokumentieren
- [ ] Accessibility (a11y) Tests hinzufügen
- [ ] Performance-Monitoring einrichten
- [ ] User Feedback einsammeln