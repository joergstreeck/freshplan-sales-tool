# FC-014: Offline Capabilities & Progressive Web App

**Parent:** [/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-014-mobile-tablet-optimization.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/2025-07-24_TECH_CONCEPT_FC-014-mobile-tablet-optimization.md)  
**Fokus:** Offline-First Architecture

## 🌐 Offline Strategy

### Service Worker Setup
```javascript
// service-worker.js
const CACHE_NAME = 'freshplan-v1';
const OFFLINE_URL = '/offline.html';

// Assets to cache immediately
const STATIC_CACHE = [
  '/',
  '/offline.html',
  '/manifest.json',
  '/static/css/main.css',
  '/static/js/main.js',
  '/static/img/logo.png'
];

// Install Event - Cache static assets
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(STATIC_CACHE))
      .then(() => self.skipWaiting())
  );
});

// Fetch Event - Network first, cache fallback
self.addEventListener('fetch', event => {
  if (event.request.method !== 'GET') return;
  
  event.respondWith(
    fetch(event.request)
      .then(response => {
        // Clone response for cache
        const responseToCache = response.clone();
        caches.open(CACHE_NAME)
          .then(cache => cache.put(event.request, responseToCache));
        return response;
      })
      .catch(() => {
        return caches.match(event.request)
          .then(response => response || caches.match(OFFLINE_URL));
      })
  );
});
```

## 📦 IndexedDB Data Storage

### Offline Data Schema
```typescript
// db.ts - Dexie.js Setup
import Dexie from 'dexie';

export class FreshPlanDB extends Dexie {
  opportunities!: Table<Opportunity>;
  customers!: Table<Customer>;
  activities!: Table<Activity>;
  syncQueue!: Table<SyncQueueItem>;
  
  constructor() {
    super('FreshPlanOffline');
    
    this.version(1).stores({
      opportunities: '&id, customerId, stage, assignedTo, lastModified',
      customers: '&id, name, city, branch, lastModified',
      activities: '&id, entityType, entityId, type, createdAt',
      syncQueue: '++id, action, entity, entityId, timestamp, retries'
    });
  }
}

export const db = new FreshPlanDB();
```

### Sync Queue Management
```typescript
interface SyncQueueItem {
  id?: number;
  action: 'CREATE' | 'UPDATE' | 'DELETE';
  entity: 'opportunity' | 'customer' | 'activity';
  entityId: string;
  data: any;
  timestamp: Date;
  retries: number;
  lastError?: string;
}

export class OfflineSyncManager {
  async addToQueue(item: Omit<SyncQueueItem, 'id' | 'retries'>) {
    await db.syncQueue.add({
      ...item,
      retries: 0,
      timestamp: new Date()
    });
    
    // Trigger sync if online
    if (navigator.onLine) {
      this.syncNow();
    }
  }
  
  async syncNow() {
    const queue = await db.syncQueue.toArray();
    
    for (const item of queue) {
      try {
        await this.syncItem(item);
        await db.syncQueue.delete(item.id!);
      } catch (error) {
        await this.handleSyncError(item, error);
      }
    }
  }
  
  private async syncItem(item: SyncQueueItem) {
    const endpoint = `/api/${item.entity}/${item.entityId}`;
    
    const response = await fetch(endpoint, {
      method: item.action === 'DELETE' ? 'DELETE' : 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(item.data)
    });
    
    if (!response.ok) {
      throw new Error(`Sync failed: ${response.statusText}`);
    }
  }
}
```

## 📱 Offline-First Features

### 1. Opportunity Pipeline Offline
```typescript
// Offline-capable Opportunity Service
export class OpportunityOfflineService {
  async getOpportunities(): Promise<Opportunity[]> {
    if (navigator.onLine) {
      try {
        // Try network first
        const response = await api.getOpportunities();
        
        // Update local cache
        await db.opportunities.clear();
        await db.opportunities.bulkAdd(response.data);
        
        return response.data;
      } catch (error) {
        console.warn('Network failed, using offline data');
      }
    }
    
    // Fallback to IndexedDB
    return db.opportunities.toArray();
  }
  
  async updateOpportunityStage(
    id: string, 
    newStage: OpportunityStage
  ): Promise<void> {
    // Update local immediately
    await db.opportunities.update(id, { 
      stage: newStage,
      lastModified: new Date()
    });
    
    // Queue for sync
    await syncManager.addToQueue({
      action: 'UPDATE',
      entity: 'opportunity',
      entityId: id,
      data: { stage: newStage }
    });
  }
}
```

### 2. Activity Tracking Offline
```typescript
// Offline Activity Creation
export const useOfflineActivity = () => {
  const createActivity = async (activity: CreateActivityDTO) => {
    const newActivity = {
      ...activity,
      id: generateOfflineId(),
      createdAt: new Date(),
      syncStatus: 'pending'
    };
    
    // Save locally
    await db.activities.add(newActivity);
    
    // Queue for sync
    await syncManager.addToQueue({
      action: 'CREATE',
      entity: 'activity',
      entityId: newActivity.id,
      data: activity
    });
    
    // Optimistic UI update
    queryClient.invalidateQueries(['activities']);
  };
  
  return { createActivity };
};
```

### 3. Conflict Resolution
```typescript
export class ConflictResolver {
  async resolveConflict(
    local: any,
    remote: any,
    type: string
  ): Promise<any> {
    // Simple last-write-wins strategy
    if (local.lastModified > remote.lastModified) {
      return local;
    }
    
    // For opportunities, preserve higher stage
    if (type === 'opportunity') {
      const stageOrder = ['LEAD', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION', 'CLOSED_WON'];
      const localIndex = stageOrder.indexOf(local.stage);
      const remoteIndex = stageOrder.indexOf(remote.stage);
      
      if (localIndex > remoteIndex) {
        return local;
      }
    }
    
    return remote;
  }
}
```

## 🔄 Background Sync

### Periodic Background Sync
```javascript
// Register periodic sync
navigator.serviceWorker.ready.then(registration => {
  registration.periodicSync.register('sync-data', {
    minInterval: 60 * 60 * 1000 // 1 hour
  });
});

// Handle in Service Worker
self.addEventListener('periodicsync', event => {
  if (event.tag === 'sync-data') {
    event.waitUntil(syncOfflineData());
  }
});
```

### Network Status Handling
```typescript
export const useNetworkStatus = () => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [networkType, setNetworkType] = useState<string>('unknown');
  
  useEffect(() => {
    const updateOnlineStatus = () => {
      setIsOnline(navigator.onLine);
      
      if ('connection' in navigator) {
        const connection = (navigator as any).connection;
        setNetworkType(connection.effectiveType || 'unknown');
      }
    };
    
    window.addEventListener('online', updateOnlineStatus);
    window.addEventListener('offline', updateOnlineStatus);
    
    return () => {
      window.removeEventListener('online', updateOnlineStatus);
      window.removeEventListener('offline', updateOnlineStatus);
    };
  }, []);
  
  return { isOnline, networkType };
};
```

## 📊 Offline UI Indicators

### Offline Banner
```tsx
export const OfflineBanner: React.FC = () => {
  const { isOnline } = useNetworkStatus();
  const { pendingCount } = useSyncQueue();
  
  if (isOnline) return null;
  
  return (
    <Alert 
      severity="info" 
      sx={{ 
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        zIndex: 9999
      }}
    >
      <AlertTitle>Offline-Modus</AlertTitle>
      Sie arbeiten offline. {pendingCount > 0 && 
        `${pendingCount} Änderungen werden synchronisiert, sobald Sie online sind.`
      }
    </Alert>
  );
};
```

### Sync Status Icons
```tsx
const SyncStatusIcon: React.FC<{status: SyncStatus}> = ({status}) => {
  switch(status) {
    case 'synced':
      return <CloudDoneIcon color="success" />;
    case 'pending':
      return <CloudQueueIcon color="warning" />;
    case 'error':
      return <CloudOffIcon color="error" />;
    default:
      return <CloudIcon />;
  }
};
```

## 🚀 PWA Installation

### Install Prompt
```typescript
export const useInstallPrompt = () => {
  const [installPrompt, setInstallPrompt] = useState<any>(null);
  const [isInstalled, setIsInstalled] = useState(false);
  
  useEffect(() => {
    // Check if already installed
    if (window.matchMedia('(display-mode: standalone)').matches) {
      setIsInstalled(true);
    }
    
    // Capture install prompt
    const handleBeforeInstall = (e: Event) => {
      e.preventDefault();
      setInstallPrompt(e);
    };
    
    window.addEventListener('beforeinstallprompt', handleBeforeInstall);
    
    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstall);
    };
  }, []);
  
  const promptInstall = async () => {
    if (!installPrompt) return;
    
    installPrompt.prompt();
    const result = await installPrompt.userChoice;
    
    if (result.outcome === 'accepted') {
      setIsInstalled(true);
    }
    
    setInstallPrompt(null);
  };
  
  return { canInstall: !!installPrompt, promptInstall, isInstalled };
};
```

## ⚡ Performance Optimizations

1. **Selective Caching:** Nur wichtige Daten offline
2. **Compression:** LZ-String für IndexedDB
3. **Lazy Sync:** Niedrige Priorität für Background Sync
4. **Quota Management:** Alte Daten automatisch löschen
5. **Delta Sync:** Nur Änderungen synchronisieren