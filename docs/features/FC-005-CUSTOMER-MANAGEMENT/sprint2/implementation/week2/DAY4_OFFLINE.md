# 📆 Tag 4: Offline Queue Implementation

**Datum:** Donnerstag, 15. August 2025  
**Fokus:** Offline-Fähigkeit & Sync  
**Ziel:** Robuste Offline Queue für Außendienst  

## 🧭 Navigation

**← Vorheriger Tag:** [Tag 3: Mobile](./DAY3_MOBILE.md)  
**↑ Woche 2 Übersicht:** [README.md](./README.md)  
**→ Nächster Tag:** [Tag 5: Export](./DAY5_EXPORT.md)  
**📘 Spec:** [Offline Specification](./specs/OFFLINE_SPEC.md)  

## 🎯 Tagesziel

- Frontend: Offline Action Queue
- Service Worker: Caching Strategy
- Sync: Conflict Resolution
- Testing: Offline Scenarios

## 🔄 Offline Architecture

```
┌───────────────────┐     ┌───────────────────┐
│   User Action     │     │  Service Worker   │
└────────┬──────────┘     └───────┬─────────┘
         ↓                         ↓
    ┌────┴─────┐            ┌────┴─────┐
    │ Online?  │            │  Cache   │
    └──┬───┬───┘            └──────────┘
       Yes  No
        │    │
    ┌───┴────┴───┐
    │   Queue     │
    │ IndexedDB   │
    └─────┬──────┘
          │
    ┌─────┴──────┐
    │ Background  │
    │    Sync     │
    └────────────┘
```

## 💻 Implementation

### 1. Offline Action Queue

```typescript
// services/offlineQueue.ts
interface QueuedAction {
  id: string;
  type: ActionType;
  payload: any;
  timestamp: Date;
  retryCount: number;
  conflictMarkers?: string[];
}

export class OfflineActionQueue {
  private queue: QueuedAction[] = [];
  private syncInProgress = false;
  
  constructor(
    private storage: Storage = localStorage,
    private api: ApiService
  ) {
    this.loadQueue();
    this.setupEventListeners();
  }
  
  // Queue action when offline
  async queueAction(action: ContactAction): Promise<void> {
    const queuedAction: QueuedAction = {
      id: generateId(),
      type: action.type,
      payload: action.payload,
      timestamp: new Date(),
      retryCount: 0,
      conflictMarkers: this.detectConflictMarkers(action)
    };
    
    this.queue.push(queuedAction);
    this.persistQueue();
    
    // Try immediate sync if online
    if (navigator.onLine) {
      await this.syncQueue();
    }
  }
  
  // Sync when coming online
  private setupEventListeners(): void {
    window.addEventListener('online', () => {
      console.log('Back online - syncing queue');
      this.syncQueue();
    });
    
    // Periodic sync attempt
    setInterval(() => {
      if (navigator.onLine && this.queue.length > 0) {
        this.syncQueue();
      }
    }, 30000); // Every 30 seconds
  }
  
  // Sync queue with conflict resolution
  private async syncQueue(): Promise<void> {
    if (this.syncInProgress || this.queue.length === 0) return;
    
    this.syncInProgress = true;
    const failedActions: QueuedAction[] = [];
    
    for (const action of this.queue) {
      try {
        // Check for conflicts
        if (action.conflictMarkers?.length) {
          const conflicts = await this.checkConflicts(action);
          if (conflicts.length > 0) {
            // Handle conflict
            const resolution = await this.resolveConflict(action, conflicts);
            if (resolution === 'skip') {
              continue;
            }
          }
        }
        
        // Execute action
        await this.executeAction(action);
        
      } catch (error) {
        action.retryCount++;
        if (action.retryCount < 3) {
          failedActions.push(action);
        } else {
          // Log failed action for manual resolution
          console.error('Action failed after 3 retries:', action);
        }
      }
    }
    
    this.queue = failedActions;
    this.persistQueue();
    this.syncInProgress = false;
  }
}
```

**Vollständiger Code:** [frontend/services/offlineQueue.ts](./code/frontend/services/offlineQueue.ts)

### 2. Service Worker

```typescript
// sw.js
const CACHE_NAME = 'contact-cache-v1';
const API_CACHE = 'api-cache-v1';

// Cache strategies
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);
  
  // API calls - Network first, fallback to cache
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(
      fetch(request)
        .then(response => {
          // Cache successful responses
          if (response.ok) {
            const responseClone = response.clone();
            caches.open(API_CACHE).then(cache => {
              cache.put(request, responseClone);
            });
          }
          return response;
        })
        .catch(() => {
          // Offline - return cached version
          return caches.match(request);
        })
    );
  }
  
  // Static assets - Cache first
  else {
    event.respondWith(
      caches.match(request)
        .then(response => response || fetch(request))
    );
  }
});

// Background sync
self.addEventListener('sync', (event) => {
  if (event.tag === 'sync-contacts') {
    event.waitUntil(syncContacts());
  }
});
```

**Vollständiger Code:** [frontend/sw.js](./code/frontend/sw.js)

### 3. Conflict Resolution

```typescript
// services/conflictResolver.ts
export class ConflictResolver {
  async resolveConflict(
    local: QueuedAction,
    remote: ContactData
  ): Promise<ConflictResolution> {
    // Compare timestamps
    if (local.timestamp > remote.lastModified) {
      return { strategy: 'use-local', data: local.payload };
    }
    
    // Check specific fields
    const conflicts = this.detectFieldConflicts(local.payload, remote);
    
    if (conflicts.length === 0) {
      // No real conflict - merge
      return {
        strategy: 'merge',
        data: { ...remote, ...local.payload }
      };
    }
    
    // Real conflict - ask user
    return {
      strategy: 'ask-user',
      conflicts: conflicts,
      local: local.payload,
      remote: remote
    };
  }
}
```

## 📏 IndexedDB Schema

```typescript
// db/schema.ts
const DB_NAME = 'ContactOfflineDB';
const DB_VERSION = 1;

export function initDB(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);
    
    request.onupgradeneeded = (event) => {
      const db = event.target.result;
      
      // Actions queue
      if (!db.objectStoreNames.contains('actions')) {
        const store = db.createObjectStore('actions', { keyPath: 'id' });
        store.createIndex('timestamp', 'timestamp');
        store.createIndex('type', 'type');
      }
      
      // Cached contacts
      if (!db.objectStoreNames.contains('contacts')) {
        const store = db.createObjectStore('contacts', { keyPath: 'id' });
        store.createIndex('customerId', 'customerId');
      }
    };
    
    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}
```

## 🧪 Tests

### Offline Scenario Test

```typescript
// __tests__/offline.test.ts
describe('Offline Queue', () => {
  beforeEach(() => {
    // Mock offline state
    Object.defineProperty(navigator, 'onLine', {
      writable: true,
      value: false
    });
  });
  
  it('should queue actions when offline', async () => {
    const queue = new OfflineActionQueue();
    const action = {
      type: 'updateContact',
      payload: { id: '123', name: 'Updated Name' }
    };
    
    await queue.queueAction(action);
    
    const queued = await queue.getQueuedActions();
    expect(queued).toHaveLength(1);
    expect(queued[0].type).toBe('updateContact');
  });
  
  it('should sync when coming online', async () => {
    const queue = new OfflineActionQueue();
    await queue.queueAction(mockAction);
    
    // Simulate coming online
    Object.defineProperty(navigator, 'onLine', { value: true });
    window.dispatchEvent(new Event('online'));
    
    // Wait for sync
    await new Promise(resolve => setTimeout(resolve, 100));
    
    const remaining = await queue.getQueuedActions();
    expect(remaining).toHaveLength(0);
  });
});
```

## 📝 Checkliste

- [ ] OfflineActionQueue implementiert
- [ ] Service Worker registriert
- [ ] Cache Strategien definiert
- [ ] Conflict Resolution Logic
- [ ] IndexedDB Schema erstellt
- [ ] Background Sync eingerichtet
- [ ] Offline Tests geschrieben

## 🔗 Weiterführende Links

- **PWA Guide:** [Progressive Web App Best Practices](./guides/PWA_GUIDE.md)
- **Sync Strategies:** [Offline Sync Patterns](./guides/OFFLINE_SYNC_PATTERNS.md)
- **Nächster Schritt:** [→ Tag 5: GDPR Export](./DAY5_EXPORT.md)

---

**Status:** 📋 Bereit zur Implementierung