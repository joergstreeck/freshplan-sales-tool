# 📱 FC-032 OFFLINE-FIRST ARCHITECTURE - TECHNISCHES KONZEPT

**Feature Code:** FC-032  
**Feature-Typ:** 🏗️ FULLSTACK INFRASTRUCTURE  
**Geschätzter Aufwand:** 6 Tage  
**Priorität:** HIGH - Überall arbeiten können  
**Abhängigkeiten:** FC-018 Mobile PWA  
**Claude-optimiert:** 15-Min Context Chunks ⚡

---

## 📋 EXECUTIVE SUMMARY (2 Min Lesezeit)

### 🎯 Problem
Verkäufer sind oft ohne Internetverbindung unterwegs (Kundentermine, schlechte Mobilfunkabdeckung, Roaming).

### 💡 Lösung
Offline-First PWA mit intelligenter Synchronisation - alle kritischen Funktionen funktionieren ohne Internet.

### 📈 Business Impact
- **100% Verfügbarkeit:** Verkäufer können überall arbeiten
- **Produktivität:** Keine Unterbrechungen durch Verbindungsprobleme
- **Kundenerlebnis:** Professionelle Demos auch ohne Internet

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[📱 FC-018 Mobile PWA](/docs/features/PLANNED/09_mobile_app/FC-018_TECH_CONCEPT.md)** - PWA Foundation

### ⚡ Direkt integriert in:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_TECH_CONCEPT.md)** - Offline Customer Cards
- **[🧮 M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_TECH_CONCEPT.md)** - Offline Calculations
- **[📧 FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_TECH_CONCEPT.md)** - Offline E-Mail Drafts

### 🚀 Ermöglicht folgende Features:
- **[🚀 FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_TECH_CONCEPT.md)** - Offline Quick Actions
- **[🎯 FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md)** - Offline Template Access
- **[📋 FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_TECH_CONCEPT.md)** - Offline Field Service

### 🎨 UI Integration:
- **[🧭 M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_TECH_CONCEPT.md)** - Offline Status Indicator
- **[⚙️ M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_TECH_CONCEPT.md)** - Sync Settings

### 🔧 Technische Details:
- **[IMPLEMENTATION_BACKEND.md](/docs/features/ACTIVE/01_security/IMPLEMENTATION_BACKEND.md)** *(geplant)* - Sync Infrastructure
- **[IMPLEMENTATION_FRONTEND.md](/docs/features/ACTIVE/01_security/IMPLEMENTATION_FRONTEND.md)** *(geplant)* - Service Worker & Storage
- **[DECISION_LOG.md](/docs/features/ACTIVE/01_security/DECISION_LOG.md)** *(geplant)* - Offline-Strategy Entscheidungen

---

## 🏗️ ARCHITEKTUR OVERVIEW (5 Min)

### 🎨 Frontend-Architektur (Progressive Web App)
```typescript
src/
├── offline/
│   ├── ServiceWorker/
│   │   ├── sw.ts                    # Main Service Worker
│   │   ├── strategies/
│   │   │   ├── CacheFirst.ts        # Statische Assets
│   │   │   ├── NetworkFirst.ts      # API Calls
│   │   │   └── StaleWhileRevalidate.ts  # Customer Data
│   │   └── sync/
│   │       ├── BackgroundSync.ts    # Background Tasks
│   │       └── PeriodicSync.ts      # Periodic Updates
│   ├── storage/
│   │   ├── IndexedDBManager.ts      # Client Storage
│   │   ├── CacheManager.ts          # Cache Strategies
│   │   └── SyncQueue.ts             # Offline Action Queue
│   └── hooks/
│       ├── useOfflineStatus.ts      # Online/Offline Detection
│       ├── useOfflineData.ts        # Offline Data Access
│       └── useBackgroundSync.ts     # Sync Management
```

### 🔧 Backend-Services (Sync Infrastructure)
```java
com.freshplan.offline/
├── service/
│   ├── SyncService.java             # Main Sync Orchestration
│   ├── ConflictResolutionService.java   # Data Conflict Handling
│   ├── DeltaSyncService.java        # Incremental Sync
│   └── OfflineQueueService.java     # Offline Action Processing
├── dto/
│   ├── SyncRequest.java             # Sync Protocol
│   ├── SyncResponse.java            # Sync Results
│   └── ConflictResolution.java      # Conflict Data
└── entity/
    ├── SyncMetadata.java            # Sync State Tracking
    ├── OfflineAction.java           # Queued Actions
    └── ConflictLog.java             # Conflict History
```

---

## 🎯 PHASE 1: SERVICE WORKER & CACHING (2 Tage)

### 🔥 Advanced Service Worker Implementation
```typescript
// sw.ts - Main Service Worker
import { precacheAndRoute, cleanupOutdatedCaches } from 'workbox-precaching';
import { registerRoute, NavigationRoute } from 'workbox-routing';
import { CacheFirst, NetworkFirst, StaleWhileRevalidate } from 'workbox-strategies';
import { BackgroundSync } from 'workbox-background-sync';
import { ExpirationPlugin } from 'workbox-expiration';

// Precache static assets
precacheAndRoute(self.__WB_MANIFEST);
cleanupOutdatedCaches();

// Cache Strategies per Content Type
const CACHE_STRATEGIES = {
  // Static Assets - Cache First
  STATIC: new CacheFirst({
    cacheName: 'freshplan-static-v1',
    plugins: [
      new ExpirationPlugin({
        maxEntries: 100,
        maxAgeSeconds: 30 * 24 * 60 * 60 // 30 Tage
      })
    ]
  }),
  
  // API Data - Network First mit Fallback
  API: new NetworkFirst({
    cacheName: 'freshplan-api-v1',
    networkTimeoutSeconds: 3,
    plugins: [
      new ExpirationPlugin({
        maxEntries: 1000,
        maxAgeSeconds: 24 * 60 * 60 // 1 Tag
      })
    ]
  }),
  
  // Customer Data - Stale While Revalidate
  CUSTOMER_DATA: new StaleWhileRevalidate({
    cacheName: 'freshplan-customers-v1',
    plugins: [
      new ExpirationPlugin({
        maxEntries: 5000,
        maxAgeSeconds: 7 * 24 * 60 * 60 // 1 Woche
      })
    ]
  })
};

// Route Registrations
registerRoute(
  ({ request }) => request.destination === 'script' || 
                   request.destination === 'style' ||
                   request.destination === 'image',
  CACHE_STRATEGIES.STATIC
);

registerRoute(
  ({ url }) => url.pathname.startsWith('/api/customers'),
  CACHE_STRATEGIES.CUSTOMER_DATA
);

registerRoute(
  ({ url }) => url.pathname.startsWith('/api/'),
  CACHE_STRATEGIES.API
);

// Background Sync für Offline Actions
const bgSync = new BackgroundSync('offline-actions', {
  maxRetentionTime: 24 * 60 // 24 Stunden
});

// Offline Action Queue
self.addEventListener('fetch', event => {
  if (event.request.method === 'POST' || 
      event.request.method === 'PUT' || 
      event.request.method === 'DELETE') {
    
    if (!navigator.onLine) {
      // Queue für später
      event.respondWith(
        queueOfflineAction(event.request).then(() =>
          new Response(JSON.stringify({ 
            queued: true, 
            message: 'Aktion wird synchronisiert wenn wieder online' 
          }), {
            headers: { 'Content-Type': 'application/json' }
          })
        )
      );
    }
  }
});

// Periodic Background Sync (wenn unterstützt)
self.addEventListener('periodicsync', event => {
  if (event.tag === 'customer-data-sync') {
    event.waitUntil(syncCustomerData());
  }
});

const queueOfflineAction = async (request: Request) => {
  const data = {
    url: request.url,
    method: request.method,
    headers: Object.fromEntries(request.headers.entries()),
    body: await request.text(),
    timestamp: Date.now()
  };
  
  return bgSync.replayRequests();
};

const syncCustomerData = async () => {
  try {
    const response = await fetch('/api/sync/customers/delta');
    const deltaData = await response.json();
    
    // IndexedDB Update
    await updateLocalCustomerData(deltaData);
    
    console.log('Customer data sync completed');
  } catch (error) {
    console.error('Background sync failed:', error);
  }
};
```

### 🗄️ IndexedDB Storage Manager
```typescript
// IndexedDBManager.ts
interface StorageSchema {
  customers: Customer & { _lastModified: number; _syncStatus: 'synced' | 'pending' | 'conflict' };
  opportunities: Opportunity & { _lastModified: number; _syncStatus: 'synced' | 'pending' | 'conflict' };
  templates: SmartTemplate & { _lastModified: number };
  offlineActions: OfflineAction;
  syncMetadata: { key: string; lastSync: number; version: number };
}

class IndexedDBManager {
  private db: IDBDatabase | null = null;
  private readonly DB_NAME = 'FreshPlanOfflineDB';
  private readonly DB_VERSION = 3;

  async init(): Promise<void> {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.DB_NAME, this.DB_VERSION);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        this.db = request.result;
        resolve();
      };
      
      request.onupgradeneeded = (event) => {
        const db = (event.target as IDBOpenDBRequest).result;
        
        // Customers Store
        if (!db.objectStoreNames.contains('customers')) {
          const customerStore = db.createObjectStore('customers', { keyPath: 'id' });
          customerStore.createIndex('_lastModified', '_lastModified');
          customerStore.createIndex('_syncStatus', '_syncStatus');
          customerStore.createIndex('name', 'name');
          customerStore.createIndex('industry', 'industry');
        }
        
        // Opportunities Store
        if (!db.objectStoreNames.contains('opportunities')) {
          const opportunityStore = db.createObjectStore('opportunities', { keyPath: 'id' });
          opportunityStore.createIndex('customerId', 'customerId');
          opportunityStore.createIndex('stage', 'stage');
          opportunityStore.createIndex('_lastModified', '_lastModified');
        }
        
        // Offline Actions Store
        if (!db.objectStoreNames.contains('offlineActions')) {
          const actionStore = db.createObjectStore('offlineActions', { 
            keyPath: 'id', 
            autoIncrement: true 
          });
          actionStore.createIndex('timestamp', 'timestamp');
          actionStore.createIndex('type', 'type');
          actionStore.createIndex('status', 'status');
        }
        
        // Sync Metadata Store
        if (!db.objectStoreNames.contains('syncMetadata')) {
          db.createObjectStore('syncMetadata', { keyPath: 'key' });
        }
      };
    });
  }

  async storeCustomers(customers: Customer[]): Promise<void> {
    const transaction = this.db!.transaction(['customers'], 'readwrite');
    const store = transaction.objectStore('customers');
    
    const promises = customers.map(customer => {
      const customerWithMeta = {
        ...customer,
        _lastModified: Date.now(),
        _syncStatus: 'synced' as const
      };
      return store.put(customerWithMeta);
    });
    
    await Promise.all(promises);
  }

  async getCustomersForOfflineUse(limit: number = 1000): Promise<Customer[]> {
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['customers'], 'readonly');
      const store = transaction.objectStore('customers');
      const request = store.getAll();
      
      request.onsuccess = () => {
        const customers = request.result
          .sort((a, b) => b._lastModified - a._lastModified)
          .slice(0, limit);
        resolve(customers);
      };
      
      request.onerror = () => reject(request.error);
    });
  }

  async queueOfflineAction(action: Omit<OfflineAction, 'id'>): Promise<number> {
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineActions'], 'readwrite');
      const store = transaction.objectStore('offlineActions');
      
      const actionWithTimestamp = {
        ...action,
        timestamp: Date.now(),
        status: 'pending' as const,
        retryCount: 0
      };
      
      const request = store.add(actionWithTimestamp);
      
      request.onsuccess = () => resolve(request.result as number);
      request.onerror = () => reject(request.error);
    });
  }

  async getPendingActions(): Promise<OfflineAction[]> {
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineActions'], 'readonly');
      const store = transaction.objectStore('offlineActions');
      const index = store.index('status');
      const request = index.getAll('pending');
      
      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  async updateSyncMetadata(key: string, lastSync: number, version: number): Promise<void> {
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['syncMetadata'], 'readwrite');
      const store = transaction.objectStore('syncMetadata');
      
      const request = store.put({ key, lastSync, version });
      
      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }
}

export const dbManager = new IndexedDBManager();
```

---

## 🎯 PHASE 2: OFFLINE DATA MANAGEMENT (2 Tage)

### 🔄 Smart Data Synchronization
```typescript
// useOfflineData.ts
interface OfflineDataState {
  customers: Customer[];
  opportunities: Opportunity[];
  templates: SmartTemplate[];
  isLoading: boolean;
  lastSync: Date | null;
  syncProgress: number;
  conflicts: ConflictItem[];
}

export const useOfflineData = () => {
  const [state, setState] = useState<OfflineDataState>({
    customers: [],
    opportunities: [],
    templates: [],
    isLoading: false,
    lastSync: null,
    syncProgress: 0,
    conflicts: []
  });
  
  const { isOnline } = useOfflineStatus();
  
  // Initial Data Load
  useEffect(() => {
    loadOfflineData();
  }, []);
  
  // Auto-Sync when coming online
  useEffect(() => {
    if (isOnline) {
      syncWithServer();
    }
  }, [isOnline]);
  
  const loadOfflineData = async () => {
    setState(prev => ({ ...prev, isLoading: true }));
    
    try {
      const [customers, opportunities, templates] = await Promise.all([
        dbManager.getCustomersForOfflineUse(),
        dbManager.getOpportunitiesForOfflineUse(),
        dbManager.getTemplatesForOfflineUse()
      ]);
      
      const syncMeta = await dbManager.getSyncMetadata('global');
      
      setState(prev => ({
        ...prev,
        customers,
        opportunities, 
        templates,
        lastSync: syncMeta ? new Date(syncMeta.lastSync) : null,
        isLoading: false
      }));
    } catch (error) {
      console.error('Failed to load offline data:', error);
      setState(prev => ({ ...prev, isLoading: false }));
    }
  };
  
  const syncWithServer = async () => {
    if (!isOnline) return;
    
    setState(prev => ({ ...prev, isLoading: true, syncProgress: 0 }));
    
    try {
      // 1. Upload pending actions
      await uploadPendingActions();
      setState(prev => ({ ...prev, syncProgress: 25 }));
      
      // 2. Download delta changes
      const deltaData = await downloadDeltaChanges();
      setState(prev => ({ ...prev, syncProgress: 50 }));
      
      // 3. Resolve conflicts
      const conflicts = await resolveConflicts(deltaData);
      setState(prev => ({ ...prev, syncProgress: 75, conflicts }));
      
      // 4. Update local storage
      if (conflicts.length === 0) {
        await updateLocalStorage(deltaData);
        await dbManager.updateSyncMetadata('global', Date.now(), deltaData.version);
      }
      
      setState(prev => ({ 
        ...prev, 
        syncProgress: 100,
        lastSync: new Date(),
        isLoading: false 
      }));
      
      // Reload data after sync
      await loadOfflineData();
      
    } catch (error) {
      console.error('Sync failed:', error);
      setState(prev => ({ ...prev, isLoading: false, syncProgress: 0 }));
      throw error;
    }
  };
  
  const uploadPendingActions = async () => {
    const pendingActions = await dbManager.getPendingActions();
    
    for (const action of pendingActions) {
      try {
        const response = await fetch('/api/sync/replay-action', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(action)
        });
        
        if (response.ok) {
          await dbManager.markActionCompleted(action.id);
        } else {
          await dbManager.incrementRetryCount(action.id);
        }
      } catch (error) {
        console.error('Failed to replay action:', action, error);
        await dbManager.incrementRetryCount(action.id);
      }
    }
  };
  
  const downloadDeltaChanges = async (): Promise<DeltaResponse> => {
    const lastSync = await dbManager.getSyncMetadata('global');
    const since = lastSync ? lastSync.lastSync : 0;
    
    const response = await fetch(`/api/sync/delta?since=${since}`);
    if (!response.ok) {
      throw new Error('Failed to download delta changes');
    }
    
    return response.json();
  };
  
  const resolveConflicts = async (deltaData: DeltaResponse): Promise<ConflictItem[]> => {
    const conflicts: ConflictItem[] = [];
    
    // Check for conflicts in customers
    for (const serverCustomer of deltaData.customers) {
      const localCustomer = await dbManager.getCustomer(serverCustomer.id);
      
      if (localCustomer && 
          localCustomer._syncStatus === 'pending' &&
          localCustomer._lastModified > serverCustomer.lastModified) {
        
        conflicts.push({
          type: 'customer',
          id: serverCustomer.id,
          localData: localCustomer,
          serverData: serverCustomer,
          field: 'multiple'
        });
      }
    }
    
    return conflicts;
  };
  
  const createCustomerOffline = async (customerData: Omit<Customer, 'id'>) => {
    const tempId = `temp_${Date.now()}_${Math.random()}`;
    const customer: Customer = {
      ...customerData,
      id: tempId
    };
    
    // Store locally
    await dbManager.storeCustomer({
      ...customer,
      _lastModified: Date.now(),
      _syncStatus: 'pending'
    });
    
    // Queue for sync
    await dbManager.queueOfflineAction({
      type: 'CREATE_CUSTOMER',
      entityType: 'customer',
      entityId: tempId,
      data: customer,
      method: 'POST',
      url: '/api/customers'
    });
    
    // Update state
    setState(prev => ({
      ...prev,
      customers: [...prev.customers, customer]
    }));
    
    return customer;
  };
  
  const updateCustomerOffline = async (customerId: string, updates: Partial<Customer>) => {
    const existingCustomer = state.customers.find(c => c.id === customerId);
    if (!existingCustomer) {
      throw new Error('Customer not found');
    }
    
    const updatedCustomer = { ...existingCustomer, ...updates };
    
    // Store locally
    await dbManager.storeCustomer({
      ...updatedCustomer,
      _lastModified: Date.now(),
      _syncStatus: 'pending'
    });
    
    // Queue for sync
    await dbManager.queueOfflineAction({
      type: 'UPDATE_CUSTOMER',
      entityType: 'customer',
      entityId: customerId,
      data: updates,
      method: 'PUT',
      url: `/api/customers/${customerId}`
    });
    
    // Update state
    setState(prev => ({
      ...prev,
      customers: prev.customers.map(c => 
        c.id === customerId ? updatedCustomer : c
      )
    }));
    
    return updatedCustomer;
  };
  
  return {
    ...state,
    syncWithServer,
    createCustomerOffline,
    updateCustomerOffline,
    loadOfflineData
  };
};
```

### 🔄 Conflict Resolution System
```typescript
// ConflictResolution.tsx
interface ConflictResolutionProps {
  conflicts: ConflictItem[];
  onResolve: (resolutions: ConflictResolution[]) => void;
  onCancel: () => void;
}

const ConflictResolution: React.FC<ConflictResolutionProps> = ({
  conflicts,
  onResolve,
  onCancel
}) => {
  const [resolutions, setResolutions] = useState<ConflictResolution[]>([]);
  
  const handleResolveConflict = (conflictId: string, resolution: 'local' | 'server' | 'merge') => {
    setResolutions(prev => {
      const existing = prev.find(r => r.conflictId === conflictId);
      if (existing) {
        return prev.map(r => 
          r.conflictId === conflictId 
            ? { ...r, resolution }
            : r
        );
      } else {
        return [...prev, { conflictId, resolution }];
      }
    });
  };
  
  const generateMergedData = (conflict: ConflictItem) => {
    // Intelligente Merge-Strategie
    const merged = { ...conflict.serverData };
    
    // Bestimmte Felder bevorzugen lokale Änderungen
    if (conflict.localData.notes && conflict.localData.notes !== conflict.serverData.notes) {
      merged.notes = `${conflict.serverData.notes}\n\n[Lokale Ergänzung]: ${conflict.localData.notes}`;
    }
    
    // Kontaktdaten: Neuere Änderung gewinnt
    if (conflict.localData._lastModified > conflict.serverData.lastModified) {
      merged.email = conflict.localData.email || merged.email;
      merged.phone = conflict.localData.phone || merged.phone;
    }
    
    return merged;
  };
  
  return (
    <Dialog open={conflicts.length > 0} maxWidth="md" fullWidth>
      <DialogTitle>
        Daten-Konflikte lösen
        <Typography variant="body2" color="textSecondary">
          {conflicts.length} Konflikte gefunden - diese Daten wurden sowohl lokal als auch auf dem Server geändert
        </Typography>
      </DialogTitle>
      
      <DialogContent>
        {conflicts.map(conflict => (
          <Card key={conflict.id} sx={{ mb: 2 }}>
            <CardHeader
              title={`${conflict.type}: ${conflict.localData.name || conflict.id}`}
              subheader={`Lokale Änderung: ${new Date(conflict.localData._lastModified).toLocaleString()}`}
            />
            
            <CardContent>
              <Grid container spacing={2}>
                {/* Lokale Version */}
                <Grid item xs={6}>
                  <Paper sx={{ p: 2, bgcolor: 'warning.light' }}>
                    <Typography variant="h6" gutterBottom>
                      📱 Lokale Version
                    </Typography>
                    <ConflictDataDisplay data={conflict.localData} />
                    <Button
                      fullWidth
                      variant={resolutions.find(r => r.conflictId === conflict.id)?.resolution === 'local' ? 'contained' : 'outlined'}
                      onClick={() => handleResolveConflict(conflict.id, 'local')}
                      sx={{ mt: 1 }}
                    >
                      Lokale Version behalten
                    </Button>
                  </Paper>
                </Grid>
                
                {/* Server Version */}
                <Grid item xs={6}>
                  <Paper sx={{ p: 2, bgcolor: 'info.light' }}>
                    <Typography variant="h6" gutterBottom>
                      ☁️ Server Version
                    </Typography>
                    <ConflictDataDisplay data={conflict.serverData} />
                    <Button
                      fullWidth
                      variant={resolutions.find(r => r.conflictId === conflict.id)?.resolution === 'server' ? 'contained' : 'outlined'}
                      onClick={() => handleResolveConflict(conflict.id, 'server')}
                      sx={{ mt: 1 }}
                    >
                      Server Version behalten
                    </Button>
                  </Paper>
                </Grid>
                
                {/* Merge Option */}
                <Grid item xs={12}>
                  <Paper sx={{ p: 2, bgcolor: 'success.light' }}>
                    <Typography variant="h6" gutterBottom>
                      🔀 Intelligente Zusammenführung
                    </Typography>
                    <ConflictDataDisplay data={generateMergedData(conflict)} />
                    <Button
                      fullWidth
                      variant={resolutions.find(r => r.conflictId === conflict.id)?.resolution === 'merge' ? 'contained' : 'outlined'}
                      onClick={() => handleResolveConflict(conflict.id, 'merge')}
                      sx={{ mt: 1 }}
                    >
                      Zusammenführen (empfohlen)
                    </Button>
                  </Paper>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        ))}
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onCancel}>
          Abbrechen
        </Button>
        <Button 
          onClick={() => onResolve(resolutions)}
          variant="contained"
          disabled={resolutions.length !== conflicts.length}
        >
          Konflikte lösen ({resolutions.length}/{conflicts.length})
        </Button>
      </DialogActions>
    </Dialog>
  );
};

const ConflictDataDisplay: React.FC<{ data: any }> = ({ data }) => (
  <Box>
    {Object.entries(data).map(([key, value]) => {
      if (key.startsWith('_')) return null; // Skip metadata
      
      return (
        <Box key={key} sx={{ mb: 1 }}>
          <Typography variant="body2" fontWeight="bold">
            {key}:
          </Typography>
          <Typography variant="body2" color="textSecondary">
            {typeof value === 'object' ? JSON.stringify(value) : String(value)}
          </Typography>
        </Box>
      );
    })}
  </Box>
);
```

---

## 🎯 PHASE 3: OFFLINE UI PATTERNS (1 Tag)

### 🎨 Offline Status Components
```typescript
// OfflineStatusBar.tsx
const OfflineStatusBar: React.FC = () => {
  const { isOnline, syncProgress, lastSync, pendingActionsCount } = useOfflineStatus();
  const { syncWithServer } = useOfflineData();
  
  if (isOnline && pendingActionsCount === 0) {
    return null; // Keine Anzeige wenn alles synchron
  }
  
  return (
    <Paper 
      sx={{ 
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        zIndex: 1300,
        p: 1,
        bgcolor: isOnline ? 'warning.main' : 'error.main',
        color: 'white'
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        {isOnline ? (
          <>
            <CloudSyncIcon />
            <Typography variant="body2">
              Synchronisierung läuft... {Math.round(syncProgress)}%
            </Typography>
            {syncProgress > 0 && (
              <LinearProgress 
                variant="determinate" 
                value={syncProgress}
                sx={{ flexGrow: 1, bgcolor: 'rgba(255,255,255,0.3)' }}
              />
            )}
          </>
        ) : (
          <>
            <CloudOffIcon />
            <Typography variant="body2">
              Offline-Modus aktiv
            </Typography>
            {pendingActionsCount > 0 && (
              <Chip 
                label={`${pendingActionsCount} ausstehende Aktionen`}
                size="small"
                sx={{ bgcolor: 'rgba(255,255,255,0.2)' }}
              />
            )}
            <Button
              size="small"
              onClick={syncWithServer}
              disabled={!isOnline}
              sx={{ color: 'white', borderColor: 'white' }}
            >
              Jetzt synchronisieren
            </Button>
          </>
        )}
        
        {lastSync && (
          <Typography variant="caption">
            Letzte Sync: {formatDistanceToNow(lastSync, { locale: de })}
          </Typography>
        )}
      </Box>
    </Paper>
  );
};
```

### 📱 Offline-Aware Components
```typescript
// OfflineCustomerCard.tsx
const OfflineCustomerCard: React.FC<{
  customer: Customer;
  onClick?: () => void;
}> = ({ customer, onClick }) => {
  const { isOnline } = useOfflineStatus();
  const isLocalOnly = customer.id.startsWith('temp_');
  const isPendingSync = customer._syncStatus === 'pending';
  
  return (
    <Card 
      onClick={onClick}
      sx={{
        cursor: 'pointer',
        position: 'relative',
        opacity: isLocalOnly ? 0.8 : 1,
        '&:hover': { elevation: 4 }
      }}
    >
      {/* Sync Status Indicator */}
      {(isLocalOnly || isPendingSync) && (
        <Chip
          icon={isOnline ? <SyncIcon /> : <CloudOffIcon />}
          label={isLocalOnly ? 'Lokal erstellt' : 'Änderungen ausstehend'}
          size="small"
          color={isOnline ? 'warning' : 'error'}
          sx={{
            position: 'absolute',
            top: 8,
            right: 8,
            zIndex: 1
          }}
        />
      )}
      
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Avatar sx={{ bgcolor: 'primary.main' }}>
            {customer.name.charAt(0)}
          </Avatar>
          
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h6">
              {customer.name}
            </Typography>
            <Typography variant="body2" color="textSecondary">
              {customer.industry} • {customer.location}
            </Typography>
            
            {!isOnline && (
              <Typography variant="caption" color="warning.main">
                📱 Offline verfügbar
              </Typography>
            )}
          </Box>
          
          <Box sx={{ textAlign: 'right' }}>
            <Typography variant="h6" color="primary">
              {formatCurrency(customer.annualRevenue)}
            </Typography>
            <Typography variant="caption" color="textSecondary">
              Jahresumsatz
            </Typography>
          </Box>
        </Box>
        
        {/* Quick Actions - nur wenn online oder cached */}
        <Box sx={{ mt: 2, display: 'flex', gap: 1 }}>
          <IconButton 
            size="small" 
            disabled={!isOnline && !customer.phone}
            title={isOnline ? 'Anrufen' : 'Offline nicht verfügbar'}
          >
            <PhoneIcon />
          </IconButton>
          <IconButton 
            size="small"
            disabled={!isOnline && !customer.email}
            title={isOnline ? 'E-Mail' : 'Als Entwurf speichern'}
          >
            <EmailIcon />
          </IconButton>
          <IconButton 
            size="small"
            title="Notizen (offline verfügbar)"
          >
            <NoteIcon />
          </IconButton>
        </Box>
      </CardContent>
    </Card>
  );
};
```

### 🎯 Offline Calculator Component
```typescript
// OfflineCalculator.tsx
const OfflineCalculator: React.FC<{
  customerId: string;
  initialData?: CalculationData;
}> = ({ customerId, initialData }) => {
  const [calculation, setCalculation] = useState<CalculationData>(
    initialData || getDefaultCalculation()
  );
  const { isOnline } = useOfflineStatus();
  const { createCalculationOffline } = useOfflineData();
  
  const handleSave = async () => {
    if (isOnline) {
      // Normal save to server
      await saveCalculation(calculation);
    } else {
      // Save offline and queue for sync
      const savedCalculation = await createCalculationOffline({
        ...calculation,
        customerId,
        createdAt: new Date().toISOString(),
        status: 'draft'
      });
      
      showNotification({
        type: 'info',
        message: 'Kalkulation offline gespeichert - wird synchronisiert wenn wieder online',
        duration: 5000
      });
    }
  };
  
  const handleGeneratePDF = async () => {
    if (!isOnline) {
      // Offline PDF generation
      const pdfBlob = await generatePDFOffline(calculation);
      const url = URL.createObjectURL(pdfBlob);
      
      // Download oder in IndexedDB für später speichern
      const link = document.createElement('a');
      link.href = url;
      link.download = `Angebot_${customer.name}_${Date.now()}.pdf`;
      link.click();
      
      showNotification({
        type: 'success',
        message: 'PDF offline erstellt und heruntergeladen',
        duration: 3000
      });
    } else {
      // Normal server PDF generation
      await generateServerPDF(calculation);
    }
  };
  
  return (
    <Box>
      {!isOnline && (
        <Alert severity="info" sx={{ mb: 2 }}>
          <AlertTitle>Offline-Modus</AlertTitle>
          Kalkulationen werden lokal gespeichert und beim nächsten Online-Zugang synchronisiert.
          PDF-Generierung funktioniert offline mit begrenzten Features.
        </Alert>
      )}
      
      {/* Calculator UI */}
      <CalculatorForm 
        data={calculation}
        onChange={setCalculation}
        offlineMode={!isOnline}
      />
      
      {/* Action Buttons */}
      <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
        <Button
          variant="contained"
          onClick={handleSave}
          startIcon={isOnline ? <SaveIcon /> : <SaveAsIcon />}
        >
          {isOnline ? 'Speichern' : 'Offline speichern'}
        </Button>
        
        <Button
          variant="outlined"
          onClick={handleGeneratePDF}
          startIcon={<PictureAsPdfIcon />}
          disabled={!calculation.items.length}
        >
          PDF erstellen {!isOnline && '(offline)'}
        </Button>
        
        {!isOnline && (
          <Button
            variant="text"
            startIcon={<CloudOffIcon />}
            disabled
          >
            E-Mail-Versand offline nicht verfügbar
          </Button>
        )}
      </Box>
    </Box>
  );
};
```

---

## 🎯 PHASE 4: BACKEND SYNC INFRASTRUCTURE (1 Tag)

### 🔧 Delta Sync Service
```java
// DeltaSyncService.java
@ApplicationScoped
public class DeltaSyncService {
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject
    OpportunityRepository opportunityRepository;
    
    @Inject
    ConflictResolutionService conflictService;
    
    public DeltaSyncResponse getDeltaChanges(String userId, long sinceTimestamp) {
        var deltaResponse = DeltaSyncResponse.builder();
        
        // Customer Changes
        var customerChanges = customerRepository
            .findModifiedSince(sinceTimestamp)
            .stream()
            .filter(customer -> hasUserAccess(userId, customer))
            .map(this::toDeltaCustomer)
            .collect(Collectors.toList());
        
        // Opportunity Changes  
        var opportunityChanges = opportunityRepository
            .findModifiedSince(sinceTimestamp)
            .stream()
            .filter(opp -> hasUserAccess(userId, opp))
            .map(this::toDeltaOpportunity)
            .collect(Collectors.toList());
        
        // Deleted Records
        var deletedRecords = getDeletedRecords(sinceTimestamp, userId);
        
        return deltaResponse
            .customers(customerChanges)
            .opportunities(opportunityChanges)
            .deletedRecords(deletedRecords)
            .serverTimestamp(System.currentTimeMillis())
            .version(getCurrentDataVersion())
            .build();
    }
    
    @Transactional
    public SyncResult processOfflineActions(List<OfflineAction> actions, String userId) {
        var results = new ArrayList<ActionResult>();
        var conflicts = new ArrayList<ConflictItem>();
        
        for (var action : actions) {
            try {
                var result = processAction(action, userId);
                
                if (result.hasConflict()) {
                    conflicts.add(result.getConflict());
                } else {
                    results.add(ActionResult.success(action.getId()));
                }
                
            } catch (Exception e) {
                results.add(ActionResult.error(action.getId(), e.getMessage()));
            }
        }
        
        return SyncResult.builder()
            .processedActions(results)
            .conflicts(conflicts)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    private ActionResult processAction(OfflineAction action, String userId) {
        switch (action.getType()) {
            case CREATE_CUSTOMER:
                return processCreateCustomer(action, userId);
            case UPDATE_CUSTOMER:
                return processUpdateCustomer(action, userId);
            case CREATE_OPPORTUNITY:
                return processCreateOpportunity(action, userId);
            default:
                throw new UnsupportedOperationException("Action type not supported: " + action.getType());
        }
    }
    
    private ActionResult processCreateCustomer(OfflineAction action, String userId) {
        var customerData = objectMapper.convertValue(action.getData(), Customer.class);
        
        // Check for duplicates (by email/phone)
        var existingCustomer = customerRepository.findByEmailOrPhone(
            customerData.getEmail(),
            customerData.getPhone()
        );
        
        if (existingCustomer.isPresent()) {
            // Potential duplicate - create conflict
            return ActionResult.conflict(
                action.getId(),
                ConflictItem.builder()
                    .type("customer_duplicate")
                    .localData(customerData)
                    .serverData(existingCustomer.get())
                    .build()
            );
        }
        
        // Create new customer with server-generated ID
        var newCustomer = Customer.builder()
            .name(customerData.getName())
            .email(customerData.getEmail())
            .phone(customerData.getPhone())
            .industry(customerData.getIndustry())
            .createdBy(userId)
            .createdAt(LocalDateTime.now())
            .lastModified(System.currentTimeMillis())
            .build();
            
        customerRepository.persist(newCustomer);
        
        return ActionResult.success(action.getId())
            .withMapping(action.getEntityId(), newCustomer.getId());
    }
    
    private ActionResult processUpdateCustomer(OfflineAction action, String userId) {
        var customerId = action.getEntityId();
        var updates = objectMapper.convertValue(action.getData(), Map.class);
        
        var customer = customerRepository.findById(customerId);
        if (customer == null) {
            return ActionResult.error(action.getId(), "Customer not found: " + customerId);
        }
        
        // Check for conflicts (server modified since client last sync)
        var clientLastModified = (Long) updates.get("_lastModified");
        if (customer.getLastModified() > clientLastModified) {
            return ActionResult.conflict(
                action.getId(),
                ConflictItem.builder()
                    .type("customer_modified")
                    .id(customerId)
                    .localData(updates)
                    .serverData(customer)
                    .build()
            );
        }
        
        // Apply updates
        updateCustomerFields(customer, updates);
        customer.setLastModified(System.currentTimeMillis());
        customer.setModifiedBy(userId);
        
        return ActionResult.success(action.getId());
    }
    
    private void updateCustomerFields(Customer customer, Map<String, Object> updates) {
        if (updates.containsKey("name")) {
            customer.setName((String) updates.get("name"));
        }
        if (updates.containsKey("email")) {
            customer.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("phone")) {
            customer.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("notes")) {
            customer.setNotes((String) updates.get("notes"));
        }
        // ... weitere Felder
    }
    
    private boolean hasUserAccess(String userId, Object entity) {
        // Implement permission checking
        return true; // Simplified for demo
    }
}
```

### 🔄 Conflict Resolution Service
```java
// ConflictResolutionService.java
@ApplicationScoped
public class ConflictResolutionService {
    
    public ConflictResolution resolveConflict(
        ConflictItem conflict,
        ConflictResolutionStrategy strategy,
        String userId
    ) {
        switch (strategy) {
            case USE_LOCAL:
                return resolveUseLocal(conflict, userId);
            case USE_SERVER:
                return resolveUseServer(conflict, userId);
            case MERGE:
                return resolveMerge(conflict, userId);
            default:
                throw new IllegalArgumentException("Unknown strategy: " + strategy);
        }
    }
    
    private ConflictResolution resolveUseLocal(ConflictItem conflict, String userId) {
        // Override server data with local changes
        var localData = conflict.getLocalData();
        
        switch (conflict.getType()) {
            case "customer":
                var customer = customerRepository.findById(conflict.getId());
                updateCustomerFromMap(customer, (Map<String, Object>) localData);
                customer.setLastModified(System.currentTimeMillis());
                customer.setModifiedBy(userId);
                break;
                
            // Handle other entity types...
        }
        
        return ConflictResolution.builder()
            .conflictId(conflict.getId())
            .strategy(ConflictResolutionStrategy.USE_LOCAL)
            .resolvedBy(userId)
            .resolvedAt(LocalDateTime.now())
            .build();
    }
    
    private ConflictResolution resolveMerge(ConflictItem conflict, String userId) {
        // Intelligent merge strategy
        var merged = performIntelligentMerge(conflict);
        
        switch (conflict.getType()) {
            case "customer":
                var customer = customerRepository.findById(conflict.getId());
                updateCustomerFromMap(customer, merged);
                customer.setLastModified(System.currentTimeMillis());
                customer.setModifiedBy(userId);
                break;
        }
        
        return ConflictResolution.builder()
            .conflictId(conflict.getId())
            .strategy(ConflictResolutionStrategy.MERGE)
            .mergedData(merged)
            .resolvedBy(userId)
            .resolvedAt(LocalDateTime.now())
            .build();
    }
    
    private Map<String, Object> performIntelligentMerge(ConflictItem conflict) {
        var serverData = (Map<String, Object>) conflict.getServerData();
        var localData = (Map<String, Object>) conflict.getLocalData();
        var merged = new HashMap<>(serverData);
        
        // Merge strategy: Local changes win for specific fields
        var localPriorityFields = Set.of("notes", "phone", "email");
        
        for (var field : localPriorityFields) {
            if (localData.containsKey(field) && 
                !Objects.equals(localData.get(field), serverData.get(field))) {
                
                if (field.equals("notes")) {
                    // Combine notes instead of overwriting
                    var serverNotes = (String) serverData.get("notes");
                    var localNotes = (String) localData.get("notes");
                    merged.put("notes", mergeNotes(serverNotes, localNotes));
                } else {
                    merged.put(field, localData.get(field));
                }
            }
        }
        
        return merged;
    }
    
    private String mergeNotes(String serverNotes, String localNotes) {
        if (serverNotes == null) return localNotes;
        if (localNotes == null) return serverNotes;
        
        return serverNotes + "\n\n--- Lokale Ergänzungen ---\n" + localNotes;
    }
}
```

---

## 📊 TESTING & PERFORMANCE

### 🧪 Offline Functionality Tests
```typescript
// OfflineSync.test.ts
describe('Offline Sync', () => {
  let mockDB: jest.Mocked<IndexedDBManager>;
  
  beforeEach(() => {
    mockDB = createMockDB();
    // Simulate offline
    Object.defineProperty(navigator, 'onLine', {
      writable: true,
      value: false
    });
  });
  
  test('sollte Kunden offline erstellen und für Sync vormerken', async () => {
    const { createCustomerOffline } = renderHook(() => useOfflineData()).result.current;
    
    const customerData = {
      name: 'Test GmbH',
      email: 'test@example.com',
      industry: 'GASTRONOMY'
    };
    
    const customer = await createCustomerOffline(customerData);
    
    expect(customer.id).toMatch(/^temp_/);
    expect(mockDB.storeCustomer).toHaveBeenCalledWith(
      expect.objectContaining({
        ...customerData,
        _syncStatus: 'pending'
      })
    );
    expect(mockDB.queueOfflineAction).toHaveBeenCalledWith(
      expect.objectContaining({
        type: 'CREATE_CUSTOMER',
        entityId: customer.id
      })
    );
  });
  
  test('sollte Konflikte erkennen und Auflösung anbieten', async () => {
    const localCustomer = {
      id: 'cust_123',
      name: 'Updated Name',
      _lastModified: Date.now(),
      _syncStatus: 'pending'
    };
    
    const serverCustomer = {
      id: 'cust_123',
      name: 'Server Name',
      lastModified: Date.now() - 1000
    };
    
    mockDB.getCustomer.mockResolvedValue(localCustomer);
    
    const deltaData = {
      customers: [serverCustomer],
      version: 123
    };
    
    const conflicts = await resolveConflicts(deltaData);
    
    expect(conflicts).toHaveLength(1);
    expect(conflicts[0]).toMatchObject({
      type: 'customer',
      id: 'cust_123',
      localData: localCustomer,
      serverData: serverCustomer
    });
  });
  
  test('sollte Service Worker korrekt registrieren', async () => {
    const mockServiceWorker = {
      register: jest.fn().mockResolvedValue({
        installing: null,
        waiting: null,
        active: { postMessage: jest.fn() }
      })
    };
    
    Object.defineProperty(navigator, 'serviceWorker', {
      value: mockServiceWorker
    });
    
    await registerServiceWorker();
    
    expect(mockServiceWorker.register).toHaveBeenCalledWith('/sw.js');
  });
});
```

### 📊 Performance Benchmarks
```typescript
// Performance Targets
const PERFORMANCE_TARGETS = {
  // Offline-to-Online Sync
  initialSyncTime: 5000, // ms für 1000 Kunden
  deltaDownloadTime: 1000, // ms für 100 Änderungen
  conflictResolutionTime: 500, // ms pro Konflikt
  
  // Storage Performance
  customerStoreTime: 100, // ms pro Kunde
  customerRetrievalTime: 50, // ms für Query
  indexedDBQueryTime: 200, // ms für komplexe Query
  
  // Service Worker
  cacheFirstTime: 50, // ms für cached Assets
  networkFirstFallback: 3000, // ms Timeout
  
  // Memory Usage
  maxMemoryUsage: 50 * 1024 * 1024, // 50MB für Offline Data
  maxIndexedDBSize: 100 * 1024 * 1024 // 100MB
};

// Performance Monitoring
const monitorOfflinePerformance = () => {
  performance.mark('sync-start');
  
  return {
    endSync: () => {
      performance.mark('sync-end');
      performance.measure('offline-sync', 'sync-start', 'sync-end');
      
      const syncTime = performance.getEntriesByName('offline-sync')[0].duration;
      
      if (syncTime > PERFORMANCE_TARGETS.initialSyncTime) {
        console.warn(`Sync time exceeded target: ${syncTime}ms`);
      }
      
      // Send metrics to analytics
      analytics.track('offline_sync_performance', {
        duration: syncTime,
        timestamp: Date.now()
      });
    }
  };
};
```

---

## 🚀 DEPLOYMENT & SUCCESS METRICS

### 📦 Progressive Rollout Strategy
```typescript
// Feature Flag Configuration
export const OFFLINE_FEATURE_FLAGS = {
  SERVICE_WORKER: 'offline.service_worker',
  BACKGROUND_SYNC: 'offline.background_sync',
  CONFLICT_RESOLUTION: 'offline.conflict_resolution',
  PERIODIC_SYNC: 'offline.periodic_sync'
} as const;

// Rollout Plan:
// Phase 1: Service Worker + Basic Caching (20% Users)
// Phase 2: + Background Sync (50% Users)
// Phase 3: + Conflict Resolution (80% Users)
// Phase 4: Full Rollout (100% Users)
```

### 📊 Success Metrics & KPIs
```typescript
interface OfflineMetrics {
  // Verfügbarkeit
  offlineUsageTime: number; // Minuten offline gearbeitet
  offlineActionsCount: number; // Aktionen offline ausgeführt
  syncSuccessRate: number; // % erfolgreiche Syncs
  
  // Performance
  avgSyncTime: number; // ms Sync-Dauer
  conflictRate: number; // % Sync-Konflikte
  dataFreshnessScore: number; // Wie aktuell sind Offline-Daten
  
  // Business Impact
  mobileSalesIncrease: number; // % mehr Mobile Sales
  customerSatisfactionScore: number; // NPS bei Offline-Demos
  productivityGain: number; // % Produktivitätssteigerung
}

// Ziel-KPIs:
const TARGET_METRICS = {
  offlineUsageTime: 120, // 2h pro Tag offline arbeiten
  syncSuccessRate: 99, // 99% erfolgreiche Syncs
  avgSyncTime: 3000, // < 3s Sync-Zeit
  conflictRate: 2, // < 2% Konflikte
  mobileSalesIncrease: 25, // 25% mehr Mobile Sales
  productivityGain: 30 // 30% Produktivitätssteigerung
};
```

---

## 💡 FUTURE ENHANCEMENTS

### 🤖 Intelligent Sync Optimization
```typescript
// ML-basierte Sync-Optimierung
interface IntelligentSync {
  // Predictive Pre-loading
  predictUserNeeds: (userBehavior: UserBehavior) => Customer[];
  
  // Smart Conflict Resolution
  autoResolveConflicts: (conflicts: ConflictItem[]) => ConflictResolution[];
  
  // Adaptive Sync Intervals
  optimizeSyncTiming: (usage: UsagePattern) => SyncSchedule;
  
  // Bandwidth-aware Sync
  adaptToBandwidth: (bandwidth: number) => SyncStrategy;
}
```

### 📱 Enhanced Mobile Experience
```typescript
// Progressive Web App Enhancements
interface PWAEnhancements {
  // Native-like Features
  pushNotifications: boolean;
  backgroundAppRefresh: boolean;
  nativeSharing: boolean;
  
  // Offline Capabilities
  offlineVoiceRecording: boolean;
  offlinePhotoCapture: boolean;
  offlineGPSTracking: boolean;
  
  // Advanced Sync
  peerToPeerSync: boolean; // Team-Mitglieder sync direkt
  edgeComputingSync: boolean; // Sync über Edge-Server
}
```

---

**📱 FC-032 Offline-First Architecture Tech Concept - KOMPLETT**

**Nächster Schritt:** Service Worker Implementation + IndexedDB Setup  
**Geschätzter Aufwand:** 6 Tage  
**ROI:** 100% Verfügbarkeit + 30% Produktivitätssteigerung durch überall arbeiten können