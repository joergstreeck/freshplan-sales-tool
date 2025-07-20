# 🔄 FC-032 OFFLINE-FIRST ARCHITECTURE (KOMPAKT)

**Erstellt:** 19.07.2025  
**Status:** 📋 READY TO START  
**Feature-Typ:** 🔀 FULLSTACK  
**Priorität:** HIGH - Außendienst muss IMMER arbeiten können  
**Geschätzt:** 1 Tag (Basis), 3 Tage (Full)  

---

## 🧠 WAS WIR BAUEN

**Problem:** Kein Netz = Keine Arbeit  
**Lösung:** Alles läuft auch offline  
**Value:** 100% Verfügbarkeit überall  

> **Business Case:** Keller, Landstraße, Funkloch - egal!

### 🎯 Offline-Fähigkeiten:
- **Read:** Alle Kundendaten lokal
- **Write:** Notizen & Updates queuen
- **Sync:** Automatisch bei Netz
- **Conflict:** Smart Resolution

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **Service Worker Setup:**
```typescript
// sw.js - Der Offline-Magier
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open('freshplan-v1').then((cache) => {
      return cache.addAll([
        '/',
        '/static/js/bundle.js',
        '/static/css/main.css',
        '/manifest.json'
      ]);
    })
  );
});

// Network-First für API
self.addEventListener('fetch', (event) => {
  if (event.request.url.includes('/api/')) {
    event.respondWith(
      fetch(event.request)
        .then(response => {
          // Cache successful responses
          if (response.ok) {
            const clone = response.clone();
            caches.open('api-cache').then(cache => {
              cache.put(event.request, clone);
            });
          }
          return response;
        })
        .catch(() => {
          // Fallback to cache
          return caches.match(event.request);
        })
    );
  }
});
```

### 2. **IndexedDB für Daten:**
```typescript
// Dexie.js macht's einfach
import Dexie from 'dexie';

const db = new Dexie('FreshPlanDB');
db.version(1).stores({
  customers: 'id, name, updatedAt',
  contacts: 'id, customerId, name',
  notes: '++id, customerId, synced',
  queue: '++id, type, payload, timestamp'
});

// Offline-First Read
const getCustomer = async (id: string) => {
  // 1. Try IndexedDB first
  const local = await db.customers.get(id);
  if (local) return local;
  
  // 2. Fetch and cache
  const remote = await api.getCustomer(id);
  await db.customers.put(remote);
  return remote;
};
```

### 3. **Sync Queue:**
```typescript
const syncQueue = {
  // Add to queue when offline
  add: async (action: any) => {
    await db.queue.add({
      type: action.type,
      payload: action.data,
      timestamp: Date.now()
    });
    
    // Try immediate sync
    if (navigator.onLine) {
      syncQueue.process();
    }
  },
  
  // Process queue when online
  process: async () => {
    const pending = await db.queue.toArray();
    
    for (const item of pending) {
      try {
        await api[item.type](item.payload);
        await db.queue.delete(item.id);
      } catch (error) {
        console.error('Sync failed:', item);
      }
    }
  }
};

// Auto-sync on reconnect
window.addEventListener('online', () => {
  showToast('Wieder online! Synchronisiere...');
  syncQueue.process();
});
```

---

## 🔄 SYNC STRATEGIES

### Conflict Resolution:
```typescript
const conflictResolver = {
  // Last-Write-Wins (Simple)
  simple: (local, remote) => {
    return local.updatedAt > remote.updatedAt ? local : remote;
  },
  
  // Three-Way-Merge (Smart)
  smart: (base, local, remote) => {
    const merged = { ...base };
    
    // Apply non-conflicting changes
    Object.keys(local).forEach(key => {
      if (local[key] !== base[key] && remote[key] === base[key]) {
        merged[key] = local[key];
      }
    });
    
    // Handle conflicts
    Object.keys(remote).forEach(key => {
      if (remote[key] !== base[key]) {
        if (local[key] === base[key]) {
          merged[key] = remote[key];
        } else {
          // Real conflict - ask user
          merged[key] = { conflict: true, local: local[key], remote: remote[key] };
        }
      }
    });
    
    return merged;
  }
};
```

### Background Sync:
```typescript
// Register Background Sync
navigator.serviceWorker.ready.then(sw => {
  return sw.sync.register('sync-queue');
});

// In Service Worker
self.addEventListener('sync', (event) => {
  if (event.tag === 'sync-queue') {
    event.waitUntil(processOfflineQueue());
  }
});
```

---

## 📱 OFFLINE UI/UX

### Status Indicator:
```typescript
const OfflineIndicator = () => {
  const isOnline = useOnlineStatus();
  const queueSize = useQueueSize();
  
  if (isOnline && queueSize === 0) return null;
  
  return (
    <Chip
      icon={isOnline ? <SyncIcon /> : <CloudOffIcon />}
      label={isOnline ? `Synchronisiere ${queueSize}` : 'Offline-Modus'}
      color={isOnline ? 'primary' : 'warning'}
      size="small"
    />
  );
};
```

### Offline-Aware Components:
```typescript
const CustomerCard = ({ customer }) => {
  const { isSynced, lastSync } = useSync(customer.id);
  
  return (
    <Card sx={{ opacity: isSynced ? 1 : 0.8 }}>
      <CardHeader
        title={customer.name}
        subheader={
          <Typography variant="caption" color="text.secondary">
            {isSynced 
              ? `Aktualisiert: ${formatTime(lastSync)}`
              : '⚠️ Lokale Änderungen'
            }
          </Typography>
        }
      />
    </Card>
  );
};
```

---

## 🔧 ADVANCED FEATURES

### Delta Sync:
```typescript
// Nur Änderungen übertragen
const deltaSync = {
  getChanges: async (since: Date) => {
    return await api.get(`/sync/changes?since=${since.toISOString()}`);
  },
  
  applyChanges: async (changes) => {
    for (const change of changes) {
      switch (change.operation) {
        case 'CREATE':
        case 'UPDATE':
          await db[change.table].put(change.data);
          break;
        case 'DELETE':
          await db[change.table].delete(change.id);
          break;
      }
    }
  }
};
```

### Selective Offline:
```typescript
// User wählt was offline verfügbar sein soll
const offlineSettings = {
  customers: {
    enabled: true,
    filter: 'myCustomers', // nur eigene
    maxAge: 7 // Tage
  },
  documents: {
    enabled: true,
    maxSize: 100 // MB
  }
};
```

---

## 🎯 SUCCESS METRICS

- **Verfügbarkeit:** 100% auch ohne Netz
- **Sync-Zeit:** < 30 Sekunden bei Reconnect
- **Conflicts:** < 1% (Smart Resolution)
- **Storage:** < 50MB für 1000 Kunden

**Integration:** Tag 8-11 mit Mobile Light PWA!

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[📱 FC-018 Mobile PWA](/docs/features/PLANNED/18_mobile_pwa/FC-018_KOMPAKT.md)** - PWA Foundation
- **[📱 FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_KOMPAKT.md)** - Mobile UI
- **[🔄 FC-023 Event Sourcing](/docs/features/PLANNED/23_event_sourcing/FC-023_KOMPAKT.md)** - Event-based Sync

### ⚡ Sync & Datenquellen:
- **[👥 M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_KOMPAKT.md)** - Customer Data Model
- **[📈 FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_KOMPAKT.md)** - Activity Sync
- **[📊 M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)** - Pipeline Data

### 🚀 Ermöglicht folgende Features:
- **[🎤 FC-029 Voice-First](/docs/features/PLANNED/29_voice_first/FC-029_KOMPAKT.md)** - Offline Voice Commands
- **[👆 FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_KOMPAKT.md)** - Offline Actions
- **[📱 FC-006 Native Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_KOMPAKT.md)** - Offline-First Mobile

### 🎨 UI Integration:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Offline Indicators
- **[🧭 M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md)** - Sync Status
- **[⚙️ M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)** - Offline Preferences

### 🔧 Technische Details:
- [FC-032_IMPLEMENTATION_GUIDE.md](./FC-032_IMPLEMENTATION_GUIDE.md) *(geplant)* - Service Worker Setup
- [FC-032_DECISION_LOG.md](./FC-032_DECISION_LOG.md) *(geplant)* - IndexedDB vs. LocalStorage
- [SYNC_STRATEGIES.md](./SYNC_STRATEGIES.md) *(geplant)* - Conflict Resolution Details