# 📱 Offline Mobile Support - Verkauf ohne Grenzen

**Phase:** 2 - Intelligence Features  
**Tag:** 6 (Zusatz)  
**Status:** 🔄 Future Enhancement  

## 🧭 Navigation

**← Zurück:** [Mobile Contact Actions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/MOBILE_CONTACT_ACTIONS.md)  
**↑ Übergeordnet:** [Step 3 Main Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)  

## 🎯 Vision: CRM überall verfügbar

**Offline Mobile Support** macht Contact Management **unabhängig von der Internetverbindung**:

> "Auf der Messe, im Keller, im Flugzeug - Ihre Kontakte sind immer dabei"

### 💬 Team-Anforderung:
> "Event-basierte Offline-Queue, Konflikt-Resolution Strategien, PWA mit Service Worker für echte Offline-Fähigkeit"

## 🔄 Offline Architecture

### Progressive Web App Setup

```typescript
// service-worker.ts
import { precacheAndRoute } from 'workbox-precaching';
import { registerRoute } from 'workbox-routing';
import { NetworkFirst, StaleWhileRevalidate } from 'workbox-strategies';
import { Queue } from 'workbox-background-sync';

// Precache app shell
precacheAndRoute(self.__WB_MANIFEST);

// Offline Queue für Contact Actions
const contactQueue = new Queue('contact-actions', {
  onSync: async ({ queue }) => {
    let entry;
    while ((entry = await queue.shiftRequest())) {
      try {
        await fetch(entry.request);
        console.log('✅ Synced:', entry.request.url);
      } catch (error) {
        console.error('❌ Sync failed:', error);
        await queue.unshiftRequest(entry);
        throw error;
      }
    }
  }
});

// API Routes with offline fallback
registerRoute(
  ({ url }) => url.pathname.startsWith('/api/contacts'),
  async ({ event, request }) => {
    try {
      const response = await fetch(request.clone());
      return response;
    } catch (error) {
      // Queue for later sync
      await contactQueue.pushRequest({ request });
      
      // Return from cache or optimistic response
      return new Response(
        JSON.stringify({ 
          status: 'queued',
          message: 'Aktion wird synchronisiert sobald online'
        }),
        { headers: { 'Content-Type': 'application/json' } }
      );
    }
  }
);
```

### Offline Queue Implementation

```typescript
// stores/offlineQueueStore.ts
interface OfflineAction {
  id: string;
  type: 'create' | 'update' | 'delete';
  entity: 'contact' | 'interaction';
  payload: any;
  timestamp: number;
  retryCount: number;
  status: 'pending' | 'syncing' | 'failed' | 'completed';
}

interface OfflineQueueState {
  queue: OfflineAction[];
  isOnline: boolean;
  isSyncing: boolean;
  
  // Actions
  enqueue: (action: Omit<OfflineAction, 'id' | 'timestamp' | 'retryCount' | 'status'>) => void;
  syncQueue: () => Promise<void>;
  clearCompleted: () => void;
}

export const useOfflineQueueStore = create<OfflineQueueState>()(
  persist(
    immer((set, get) => ({
      queue: [],
      isOnline: navigator.onLine,
      isSyncing: false,
      
      enqueue: (action) => {
        const offlineAction: OfflineAction = {
          ...action,
          id: `offline-${Date.now()}-${Math.random()}`,
          timestamp: Date.now(),
          retryCount: 0,
          status: 'pending'
        };
        
        set((state) => {
          state.queue.push(offlineAction);
        });
        
        // Try immediate sync if online
        if (get().isOnline) {
          get().syncQueue();
        }
        
        // Show notification
        showNotification({
          title: 'Offline-Modus',
          message: 'Aktion wurde zur Warteschlange hinzugefügt',
          type: 'info'
        });
      },
      
      syncQueue: async () => {
        const { queue, isOnline } = get();
        if (!isOnline || queue.length === 0) return;
        
        set((state) => {
          state.isSyncing = true;
        });
        
        const pendingActions = queue.filter(a => a.status === 'pending');
        
        for (const action of pendingActions) {
          try {
            set((state) => {
              const idx = state.queue.findIndex(a => a.id === action.id);
              if (idx >= 0) {
                state.queue[idx].status = 'syncing';
              }
            });
            
            // Execute action based on type
            await executeOfflineAction(action);
            
            set((state) => {
              const idx = state.queue.findIndex(a => a.id === action.id);
              if (idx >= 0) {
                state.queue[idx].status = 'completed';
              }
            });
            
          } catch (error) {
            set((state) => {
              const idx = state.queue.findIndex(a => a.id === action.id);
              if (idx >= 0) {
                state.queue[idx].status = 'failed';
                state.queue[idx].retryCount++;
              }
            });
          }
        }
        
        set((state) => {
          state.isSyncing = false;
        });
      },
      
      clearCompleted: () => {
        set((state) => {
          state.queue = state.queue.filter(a => a.status !== 'completed');
        });
      }
    })),
    {
      name: 'offline-queue',
      partialize: (state) => ({ queue: state.queue })
    }
  )
);
```

### Conflict Resolution

```typescript
// services/conflictResolution.ts
export class ConflictResolutionService {
  
  /**
   * Resolve conflicts between offline and server data
   */
  async resolveConflicts(
    localData: Contact,
    serverData: Contact,
    offlineChanges: OfflineAction[]
  ): Promise<Contact> {
    // Last-Write-Wins for simple fields
    const resolvedData = { ...serverData };
    
    // Apply offline changes if newer
    offlineChanges
      .filter(change => change.timestamp > new Date(serverData.updatedAt).getTime())
      .forEach(change => {
        if (change.type === 'update') {
          Object.assign(resolvedData, change.payload);
        }
      });
    
    // Special handling for arrays (merge strategy)
    if (localData.assignedLocationIds && serverData.assignedLocationIds) {
      resolvedData.assignedLocationIds = [
        ...new Set([
          ...serverData.assignedLocationIds,
          ...localData.assignedLocationIds
        ])
      ];
    }
    
    // User confirmation for critical conflicts
    const hasCriticalConflict = this.detectCriticalConflicts(localData, serverData);
    if (hasCriticalConflict) {
      const resolution = await this.promptUserResolution(localData, serverData);
      return resolution;
    }
    
    return resolvedData;
  }
  
  private detectCriticalConflicts(local: Contact, server: Contact): boolean {
    // Email changes are critical
    if (local.email !== server.email) return true;
    
    // Primary contact changes are critical
    if (local.isPrimary !== server.isPrimary) return true;
    
    // Deletion conflicts
    if (local.isActive !== server.isActive) return true;
    
    return false;
  }
}
```

### Offline UI Components

```typescript
// components/OfflineIndicator.tsx
export const OfflineIndicator: React.FC = () => {
  const { isOnline, queue, isSyncing } = useOfflineQueueStore();
  const pendingCount = queue.filter(a => a.status === 'pending').length;
  
  if (isOnline && pendingCount === 0) return null;
  
  return (
    <Chip
      icon={
        isSyncing ? (
          <CircularProgress size={16} />
        ) : isOnline ? (
          <CloudUploadIcon />
        ) : (
          <CloudOffIcon />
        )
      }
      label={
        isSyncing
          ? 'Synchronisiere...'
          : !isOnline
          ? 'Offline'
          : `${pendingCount} ausstehend`
      }
      color={!isOnline ? 'error' : pendingCount > 0 ? 'warning' : 'default'}
      onClick={() => {
        if (pendingCount > 0) {
          // Show queue dialog
        }
      }}
    />
  );
};

// components/OfflineQueueDialog.tsx
export const OfflineQueueDialog: React.FC<{
  open: boolean;
  onClose: () => void;
}> = ({ open, onClose }) => {
  const { queue, syncQueue, clearCompleted } = useOfflineQueueStore();
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        Offline-Warteschlange
        <IconButton onClick={onClose} sx={{ position: 'absolute', right: 8, top: 8 }}>
          <CloseIcon />
        </IconButton>
      </DialogTitle>
      
      <DialogContent>
        <List>
          {queue.map(action => (
            <ListItem key={action.id}>
              <ListItemIcon>
                {action.status === 'completed' ? (
                  <CheckCircleIcon color="success" />
                ) : action.status === 'failed' ? (
                  <ErrorIcon color="error" />
                ) : action.status === 'syncing' ? (
                  <CircularProgress size={20} />
                ) : (
                  <ScheduleIcon />
                )}
              </ListItemIcon>
              
              <ListItemText
                primary={getActionDescription(action)}
                secondary={
                  <>
                    {new Date(action.timestamp).toLocaleString('de-DE')}
                    {action.retryCount > 0 && ` (${action.retryCount} Versuche)`}
                  </>
                }
              />
              
              {action.status === 'failed' && (
                <ListItemSecondaryAction>
                  <IconButton onClick={() => retryAction(action)}>
                    <RefreshIcon />
                  </IconButton>
                </ListItemSecondaryAction>
              )}
            </ListItem>
          ))}
        </List>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={clearCompleted} disabled={!queue.some(a => a.status === 'completed')}>
          Erledigte löschen
        </Button>
        <Button onClick={syncQueue} variant="contained" disabled={!navigator.onLine}>
          Jetzt synchronisieren
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

### Network Status Hook

```typescript
// hooks/useNetworkStatus.ts
export const useNetworkStatus = () => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [connectionType, setConnectionType] = useState<string>('');
  
  useEffect(() => {
    const handleOnline = () => {
      setIsOnline(true);
      showNotification({
        title: 'Online',
        message: 'Verbindung wiederhergestellt - synchronisiere Daten...',
        type: 'success'
      });
      
      // Trigger sync
      useOfflineQueueStore.getState().syncQueue();
    };
    
    const handleOffline = () => {
      setIsOnline(false);
      showNotification({
        title: 'Offline',
        message: 'Keine Internetverbindung - Änderungen werden lokal gespeichert',
        type: 'warning'
      });
    };
    
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    
    // Network Information API
    if ('connection' in navigator) {
      const connection = (navigator as any).connection;
      setConnectionType(connection.effectiveType);
      
      connection.addEventListener('change', () => {
        setConnectionType(connection.effectiveType);
      });
    }
    
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);
  
  return { isOnline, connectionType };
};
```

## 🧪 Testing Offline Scenarios

```typescript
// __tests__/offline.test.ts
describe('Offline Support', () => {
  beforeEach(() => {
    // Mock offline state
    Object.defineProperty(navigator, 'onLine', {
      writable: true,
      value: false
    });
  });
  
  it('should queue actions when offline', async () => {
    const { enqueue } = useOfflineQueueStore.getState();
    
    enqueue({
      type: 'update',
      entity: 'contact',
      payload: { id: '123', firstName: 'Updated' }
    });
    
    const { queue } = useOfflineQueueStore.getState();
    expect(queue).toHaveLength(1);
    expect(queue[0].status).toBe('pending');
  });
  
  it('should sync queue when coming online', async () => {
    // Add items to queue
    const { enqueue } = useOfflineQueueStore.getState();
    enqueue({ type: 'create', entity: 'contact', payload: mockContact });
    
    // Simulate coming online
    Object.defineProperty(navigator, 'onLine', { value: true });
    window.dispatchEvent(new Event('online'));
    
    // Wait for sync
    await waitFor(() => {
      const { queue } = useOfflineQueueStore.getState();
      expect(queue[0].status).toBe('completed');
    });
  });
});
```

## 🎯 Success Metrics

### Offline Performance:
- **Queue Processing:** < 5s nach Online
- **Conflict Resolution:** < 10% manuelle Eingriffe
- **Data Loss:** 0% bei normaler Nutzung
- **Storage Usage:** < 50MB local

### User Experience:
- **Transparenz:** Klarer Offline-Status
- **Zuverlässigkeit:** Keine verlorenen Änderungen
- **Performance:** Kein Unterschied zu Online

---

**↑ Zurück zu:** [Step 3 Main Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)

**Offline = Keine Grenzen! 📱✨**