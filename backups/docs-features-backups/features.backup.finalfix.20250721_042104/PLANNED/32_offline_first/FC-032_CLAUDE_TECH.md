# 📱 FC-032 Offline-First Architecture - CLAUDE TECH ⚡

**Feature-Typ:** 🔀 FULLSTACK  
**Komplexität:** HOCH | **Aufwand:** 6 Tage | **Priorität:** HIGH  
**Status:** Ready | **Version:** 2.0 | **Letzte Aktualisierung:** 21.07.2025

## ⚡ QUICK-LOAD (30 Sekunden zur Produktivität)

**Was:** Progressive Web App mit intelligenter Offline-Synchronisation - alle kritischen Funktionen arbeiten ohne Internet
**Warum:** Verkäufer sind oft ohne Internetverbindung unterwegs (Kundentermine, schlechte Mobilfunkabdeckung, Roaming)
**Für wen:** Außendienst-Verkäufer und Mobile-First Teams die überall arbeiten müssen

### 🎯 Sofort loslegen:
```bash
# 1. Service Worker Setup
cp OfflineTemplates/sw.ts frontend/public/
cp OfflineTemplates/workbox-config.js frontend/

# 2. IndexedDB Manager
cp OfflineTemplates/IndexedDBManager.ts frontend/src/offline/storage/

# 3. Offline Hooks
cp OfflineTemplates/useOfflineData.ts frontend/src/offline/hooks/
```

### 📋 Copy-Paste Ready Recipes:

#### Service Worker mit intelligenten Cache-Strategien:
```typescript
// sw.ts - Main Service Worker
import { precacheAndRoute } from 'workbox-precaching';
import { registerRoute } from 'workbox-routing';
import { CacheFirst, NetworkFirst, StaleWhileRevalidate } from 'workbox-strategies';
import { BackgroundSync } from 'workbox-background-sync';
import { ExpirationPlugin } from 'workbox-expiration';

precacheAndRoute(self.__WB_MANIFEST);

const CACHE_STRATEGIES = {
  STATIC: new CacheFirst({
    cacheName: 'freshplan-static-v1',
    plugins: [new ExpirationPlugin({ maxEntries: 100, maxAgeSeconds: 30 * 24 * 60 * 60 })]
  }),
  API: new NetworkFirst({
    cacheName: 'freshplan-api-v1',
    networkTimeoutSeconds: 3,
    plugins: [new ExpirationPlugin({ maxEntries: 1000, maxAgeSeconds: 24 * 60 * 60 })]
  }),
  CUSTOMER_DATA: new StaleWhileRevalidate({
    cacheName: 'freshplan-customers-v1',
    plugins: [new ExpirationPlugin({ maxEntries: 5000, maxAgeSeconds: 7 * 24 * 60 * 60 })]
  })
};

// Route Registrations
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
  maxRetentionTime: 24 * 60
});

self.addEventListener('fetch', event => {
  if (event.request.method === 'POST' && !navigator.onLine) {
    event.respondWith(
      queueOfflineAction(event.request).then(() =>
        new Response(JSON.stringify({ 
          queued: true, 
          message: 'Aktion wird synchronisiert wenn wieder online' 
        }), { headers: { 'Content-Type': 'application/json' } })
      )
    );
  }
});
```

#### IndexedDB Storage Manager:
```typescript
interface StorageSchema {
  customers: Customer & { _lastModified: number; _syncStatus: 'synced' | 'pending' | 'conflict' };
  opportunities: Opportunity & { _lastModified: number; _syncStatus: 'synced' | 'pending' | 'conflict' };
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
      
      request.onsuccess = () => { this.db = request.result; resolve(); };
      request.onerror = () => reject(request.error);
      
      request.onupgradeneeded = (event) => {
        const db = (event.target as IDBOpenDBRequest).result;
        
        if (!db.objectStoreNames.contains('customers')) {
          const customerStore = db.createObjectStore('customers', { keyPath: 'id' });
          customerStore.createIndex('_syncStatus', '_syncStatus');
          customerStore.createIndex('name', 'name');
        }
        
        if (!db.objectStoreNames.contains('offlineActions')) {
          const actionStore = db.createObjectStore('offlineActions', { 
            keyPath: 'id', autoIncrement: true 
          });
          actionStore.createIndex('status', 'status');
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
}

export const dbManager = new IndexedDBManager();
```

#### Smart Offline Data Hook:
```typescript
export const useOfflineData = () => {
  const [state, setState] = useState<OfflineDataState>({
    customers: [], opportunities: [], isLoading: false,
    lastSync: null, syncProgress: 0, conflicts: []
  });
  
  const { isOnline } = useOfflineStatus();
  
  useEffect(() => { loadOfflineData(); }, []);
  useEffect(() => { if (isOnline) syncWithServer(); }, [isOnline]);
  
  const createCustomerOffline = async (customerData: Omit<Customer, 'id'>) => {
    const tempId = `temp_${Date.now()}_${Math.random()}`;
    const customer: Customer = { ...customerData, id: tempId };
    
    // Store locally
    await dbManager.storeCustomer({
      ...customer, _lastModified: Date.now(), _syncStatus: 'pending'
    });
    
    // Queue for sync
    await dbManager.queueOfflineAction({
      type: 'CREATE_CUSTOMER', entityType: 'customer', entityId: tempId,
      data: customer, method: 'POST', url: '/api/customers'
    });
    
    setState(prev => ({ ...prev, customers: [...prev.customers, customer] }));
    return customer;
  };
  
  const syncWithServer = async () => {
    if (!isOnline) return;
    setState(prev => ({ ...prev, isLoading: true, syncProgress: 0 }));
    
    try {
      await uploadPendingActions();
      setState(prev => ({ ...prev, syncProgress: 25 }));
      
      const deltaData = await downloadDeltaChanges();
      setState(prev => ({ ...prev, syncProgress: 50 }));
      
      const conflicts = await resolveConflicts(deltaData);
      setState(prev => ({ ...prev, syncProgress: 75, conflicts }));
      
      if (conflicts.length === 0) {
        await updateLocalStorage(deltaData);
        await dbManager.updateSyncMetadata('global', Date.now(), deltaData.version);
      }
      
      setState(prev => ({ 
        ...prev, syncProgress: 100, lastSync: new Date(), isLoading: false 
      }));
      
      await loadOfflineData();
    } catch (error) {
      console.error('Sync failed:', error);
      setState(prev => ({ ...prev, isLoading: false, syncProgress: 0 }));
    }
  };
  
  return { ...state, syncWithServer, createCustomerOffline, loadOfflineData };
};
```

#### Offline Status Bar Component:
```typescript
const OfflineStatusBar: React.FC = () => {
  const { isOnline, syncProgress, pendingActionsCount } = useOfflineStatus();
  const { syncWithServer } = useOfflineData();
  
  if (isOnline && pendingActionsCount === 0) return null;
  
  return (
    <Paper sx={{ 
      position: 'fixed', top: 0, left: 0, right: 0, zIndex: 1300, p: 1,
      bgcolor: isOnline ? 'warning.main' : 'error.main', color: 'white'
    }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        {isOnline ? (
          <>
            <CloudSyncIcon />
            <Typography variant="body2">
              Synchronisierung läuft... {Math.round(syncProgress)}%
            </Typography>
            {syncProgress > 0 && (
              <LinearProgress 
                variant="determinate" value={syncProgress}
                sx={{ flexGrow: 1, bgcolor: 'rgba(255,255,255,0.3)' }}
              />
            )}
          </>
        ) : (
          <>
            <CloudOffIcon />
            <Typography variant="body2">Offline-Modus aktiv</Typography>
            {pendingActionsCount > 0 && (
              <Chip 
                label={`${pendingActionsCount} ausstehende Aktionen`}
                size="small" sx={{ bgcolor: 'rgba(255,255,255,0.2)' }}
              />
            )}
            <Button size="small" onClick={syncWithServer} disabled={!isOnline}
              sx={{ color: 'white', borderColor: 'white' }}>
              Jetzt synchronisieren
            </Button>
          </>
        )}
      </Box>
    </Paper>
  );
};
```

#### Backend Delta Sync Service:
```java
@ApplicationScoped
public class DeltaSyncService {
    @Inject CustomerRepository customerRepository;
    @Inject ConflictResolutionService conflictService;
    
    public DeltaSyncResponse getDeltaChanges(String userId, long sinceTimestamp) {
        var customerChanges = customerRepository.findModifiedSince(sinceTimestamp)
            .stream().filter(customer -> hasUserAccess(userId, customer))
            .map(this::toDeltaCustomer).collect(Collectors.toList());
        
        var deletedRecords = getDeletedRecords(sinceTimestamp, userId);
        
        return DeltaSyncResponse.builder()
            .customers(customerChanges).deletedRecords(deletedRecords)
            .serverTimestamp(System.currentTimeMillis())
            .version(getCurrentDataVersion()).build();
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
        
        return SyncResult.builder().processedActions(results)
            .conflicts(conflicts).timestamp(System.currentTimeMillis()).build();
    }
    
    private ActionResult processCreateCustomer(OfflineAction action, String userId) {
        var customerData = objectMapper.convertValue(action.getData(), Customer.class);
        
        // Check for duplicates
        var existingCustomer = customerRepository.findByEmailOrPhone(
            customerData.getEmail(), customerData.getPhone());
        
        if (existingCustomer.isPresent()) {
            return ActionResult.conflict(action.getId(),
                ConflictItem.builder().type("customer_duplicate")
                    .localData(customerData).serverData(existingCustomer.get()).build());
        }
        
        var newCustomer = Customer.builder().name(customerData.getName())
            .email(customerData.getEmail()).createdBy(userId)
            .createdAt(LocalDateTime.now()).lastModified(System.currentTimeMillis()).build();
            
        customerRepository.persist(newCustomer);
        return ActionResult.success(action.getId()).withMapping(action.getEntityId(), newCustomer.getId());
    }
}
```

## 🏗️ ARCHITEKTUR ÜBERBLICK

### Progressive Web App Strategie:
```
Frontend Offline Stack:
├── Service Worker (workbox)  # Cache-Strategien + Background Sync
├── IndexedDB Storage        # Client-side Datenbank
├── Conflict Resolution UI   # Benutzerfreundliche Konfliktlösung
└── Offline-Aware Components # UI adapts to online/offline state

Backend Sync Infrastructure:
├── Delta Sync Service       # Incremental data synchronization
├── Conflict Resolution      # Server-side conflict handling
├── Offline Action Queue     # Replay offline actions
└── Performance Monitoring   # Sync performance tracking
```

### Cache-First Strategy für Assets:
- **Statische Assets:** Cache First (30 Tage TTL)
- **API Daten:** Network First mit 3s Timeout → Cache Fallback
- **Customer Daten:** Stale While Revalidate (1 Woche TTL)
- **User Actions:** Background Sync Queue

## 📊 BUSINESS VALUE

### 100% Verfügbarkeit:
- **Überall arbeiten:** Keine Unterbrechungen durch Verbindungsprobleme
- **Professionelle Demos:** Kundenpräsentationen auch ohne Internet
- **Mobile-First:** Optimiert für Außendienst und Unterwegs-Szenarien

### Performance Benefits:
- **Instant Loading:** Cached Assets laden sofort
- **Predictive Sync:** Häufig genutzte Daten werden vorab synchronisiert
- **Bandwidth Optimization:** Nur Delta-Änderungen werden übertragen

### User Experience:
- **Nahtlose Übergänge:** Automatischer Switch zwischen Online/Offline
- **Intelligente Konflikte:** Merge-Vorschläge für Daten-Konflikte
- **Visual Feedback:** Klare Anzeige des Sync-Status

## 🔄 ABHÄNGIGKEITEN

### Benötigt:
- **FC-018 Mobile PWA** - PWA Foundation ✅
- **Basic Customer Management** - Sync-Basis ✅

### Ermöglicht:
- **FC-030 One-Tap Actions** - Offline Quick Actions
- **FC-031 Smart Templates** - Offline Template Access
- **FC-022 Mobile Light** - Offline Field Service

### Integriert mit:
- **M3 Sales Cockpit** - Offline Customer Cards
- **M8 Calculator Modal** - Offline Calculations
- **FC-003 E-Mail Integration** - Offline E-Mail Drafts

## 🧪 TESTING STRATEGY

### Offline Functionality Tests:
```typescript
describe('Offline Sync', () => {
  beforeEach(() => {
    // Simulate offline
    Object.defineProperty(navigator, 'onLine', { writable: true, value: false });
  });
  
  test('sollte Kunden offline erstellen und für Sync vormerken', async () => {
    const { createCustomerOffline } = renderHook(() => useOfflineData()).result.current;
    
    const customer = await createCustomerOffline({
      name: 'Test GmbH', email: 'test@example.com', industry: 'GASTRONOMY'
    });
    
    expect(customer.id).toMatch(/^temp_/);
    expect(mockDB.storeCustomer).toHaveBeenCalledWith(
      expect.objectContaining({ _syncStatus: 'pending' })
    );
  });
  
  test('sollte Konflikte erkennen und Auflösung anbieten', async () => {
    const conflicts = await resolveConflicts({
      customers: [{ id: 'cust_123', name: 'Server Name', lastModified: Date.now() - 1000 }]
    });
    
    expect(conflicts).toHaveLength(1);
    expect(conflicts[0]).toMatchObject({
      type: 'customer', id: 'cust_123'
    });
  });
});
```

### Performance Benchmarks:
- **Sync Time:** < 5s für 1000 Kunden
- **Storage Query:** < 200ms für komplexe Queries  
- **Cache Hit Rate:** > 90% für häufig genutzte Daten
- **Memory Usage:** < 50MB für Offline Data

## 🚀 IMPLEMENTATION PLAN

### Phase 1: Service Worker & Caching (2 Tage)
- Advanced Service Worker mit Workbox
- Cache-Strategien für verschiedene Content-Types
- Background Sync für Offline Actions
- Performance Monitoring Setup

### Phase 2: Offline Data Management (2 Tage)
- IndexedDB Manager mit Schema-Versionierung
- Smart Data Synchronization Hook
- Conflict Detection & Resolution UI
- Offline CRUD Operations

### Phase 3: Offline UI Patterns (1 Tag)
- Offline Status Indicators
- Offline-Aware Components
- Sync Progress Feedback
- Error Handling & Recovery

### Phase 4: Backend Sync Infrastructure (1 Tag)
- Delta Sync Service Implementation
- Conflict Resolution Service
- Offline Action Replay Logic
- Performance & Monitoring APIs

## 📈 SUCCESS CRITERIA

### Funktionale Anforderungen:
- ✅ Vollständige Offline-Funktionalität für kritische Features
- ✅ Intelligente Konfliktlösung mit Merge-Optionen
- ✅ Automatische Synchronisation bei Verbindungswiederherstellung
- ✅ Performance: < 3s Sync-Zeit, < 2% Konfliktrate

### Business Impact:
- ✅ 25% Steigerung der Mobile Sales
- ✅ 30% Produktivitätssteigerung durch Überall-Arbeiten
- ✅ 99% Sync-Erfolgsrate
- ✅ 120min täglich offline arbeiten können

### User Experience:
- ✅ Nahtlose Online/Offline Übergänge
- ✅ Intuitive Konfliktlösung
- ✅ Visual Sync-Status Feedback
- ✅ Native App-ähnliche Performance

---
**📱 FC-032 Offline-First Architecture - Ready for 100% Availability!**  
**Optimiert für:** 6 Tage | **ROI:** Überall arbeiten können + 30% Produktivitätssteigerung | **Performance:** < 3s Sync