# 📱 FC-018 MOBILE PWA CLAUDE_TECH

**Feature Code:** FC-018  
**Claude Productivity:** ⚡ 30-Sekunden QUICK-LOAD  
**Copy-Paste Ready:** ✅ Alle Code-Recipes sofort verwendbar  
**Optimiert für:** 5x schnellere Claude-Arbeitsweise  

---

## ⚡ QUICK-LOAD (30 Sekunden zum produktiven Start)

**Was:** Progressive Web App - Transformation des React-Frontends zu einer vollwertigen App-Experience  
**Warum:** 40% mehr Mobile-Nutzung, 100% sofortige Updates ohne App Store, 60%+ Install-Rate  
**Aufwand:** 8-10 Tage (PWA Foundation + Offline-Storage + Push-Notifications)  
**ROI:** App-like Experience ohne native Entwicklung, Elimination App-Store-Komplexität  

**Core Features:**
1. **Service Worker** - Offline-fähig + Caching-Strategien
2. **PWA Manifest** - Home-Screen Installation + App-Icons
3. **Push Notifications** - VAPID + Rich Notifications mit Actions
4. **Offline Storage** - IndexedDB + Background Sync für CRUD Operations
5. **App Shell** - Fullscreen + Native Gestures + Install Prompt

**Key Benefits:**
- ⚡ Time-to-Interactive < 3 Sekunden auf 3G
- 📶 80% aller Core-Features funktionieren offline
- 🔔 40%+ Opt-in Rate für Push-Notifications
- 📱 Cross-Platform: iOS, Android, Desktop mit einem Codebase

---

## 🚀 COPY-PASTE RECIPES

### 1. PWA Manifest (Dynamic Backend)

```java
@Path("/manifest.json")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class PWAManifestResource {
    
    @ConfigProperty(name = "app.name", defaultValue = "FreshPlan Sales Tool")
    String appName;
    
    @ConfigProperty(name = "app.short-name", defaultValue = "FreshPlan")
    String shortName;
    
    @ConfigProperty(name = "app.theme-color", defaultValue = "#94C456")
    String themeColor;
    
    @GET
    public Response getManifest() {
        PWAManifest manifest = PWAManifest.builder()
            .name(appName)
            .shortName(shortName)
            .startUrl("/")
            .display("standalone")
            .themeColor(themeColor)
            .backgroundColor("#ffffff")
            .orientation("any")
            .scope("/")
            .icons(List.of(
                new PWAIcon("/icon-72.png", "72x72", "image/png"),
                new PWAIcon("/icon-96.png", "96x96", "image/png"),
                new PWAIcon("/icon-128.png", "128x128", "image/png"),
                new PWAIcon("/icon-144.png", "144x144", "image/png"),
                new PWAIcon("/icon-152.png", "152x152", "image/png"),
                new PWAIcon("/icon-192.png", "192x192", "image/png"),
                new PWAIcon("/icon-384.png", "384x384", "image/png"),
                new PWAIcon("/icon-512.png", "512x512", "image/png")
            ))
            .shortcuts(List.of(
                PWAShortcut.builder()
                    .name("Neuer Kunde")
                    .shortName("Kunde")
                    .description("Neuen Kunden anlegen")
                    .url("/customers/new")
                    .icons(List.of(new PWAIcon("/icon-customer.png", "96x96", "image/png")))
                    .build(),
                PWAShortcut.builder()
                    .name("Neue Opportunity")
                    .shortName("Opportunity")  
                    .description("Neue Opportunity erstellen")
                    .url("/opportunities/new")
                    .icons(List.of(new PWAIcon("/icon-opportunity.png", "96x96", "image/png")))
                    .build()
            ))
            .categories(List.of("business", "productivity"))
            .build();
            
        return Response.ok(manifest)
            .header("Cache-Control", "public, max-age=86400") // 24h cache
            .build();
    }
}

@Data
@Builder
public class PWAManifest {
    private String name;
    private String shortName;
    private String startUrl;
    private String display;
    private String themeColor;
    private String backgroundColor;
    private String orientation;
    private String scope;
    private List<PWAIcon> icons;
    private List<PWAShortcut> shortcuts;
    private List<String> categories;
}

@Data
@AllArgsConstructor
public class PWAIcon {
    private String src;
    private String sizes;
    private String type;
}

@Data
@Builder
public class PWAShortcut {
    private String name;
    private String shortName;
    private String description;
    private String url;
    private List<PWAIcon> icons;
}
```

### 2. Service Worker mit Workbox (Frontend)

```typescript
// /frontend/public/sw.js - Service Worker
import { precacheAndRoute, cleanupOutdatedCaches } from 'workbox-precaching';
import { registerRoute, NavigationRoute } from 'workbox-routing';
import { 
  StaleWhileRevalidate, 
  NetworkFirst, 
  CacheFirst,
  NetworkOnly 
} from 'workbox-strategies';
import { BackgroundSync } from 'workbox-background-sync';

declare const self: ServiceWorkerGlobalScope;

// Precache static assets (automatically handled by Workbox build process)
precacheAndRoute(self.__WB_MANIFEST);
cleanupOutdatedCaches();

// App Shell - Cache First Strategy
registerRoute(
  ({ request, url }) => 
    request.mode === 'navigate' || 
    url.pathname === '/' ||
    url.pathname.startsWith('/app'),
  new CacheFirst({
    cacheName: 'app-shell-v1',
    plugins: [{
      cacheKeyWillBeUsed: async ({ request }) => {
        // Always serve index.html for navigation requests
        return new URL('/', self.location.origin).href;
      }
    }]
  })
);

// API Calls - Network First with Background Sync fallback
const bgSync = new BackgroundSync('api-queue-v1', {
  maxRetentionTime: 24 * 60 // 24 hours in minutes
});

registerRoute(
  ({ url }) => url.pathname.startsWith('/api/'),
  new NetworkFirst({
    cacheName: 'api-cache-v1',
    networkTimeoutSeconds: 5,
    plugins: [
      bgSync,
      {
        cacheKeyWillBeUsed: async ({ request }) => {
          // Remove auth headers from cache key for privacy
          const url = new URL(request.url);
          return url.href;
        },
        cacheWillUpdate: async ({ response }) => {
          // Only cache successful responses
          return response.status === 200;
        }
      }
    ]
  })
);

// Static Assets - Stale While Revalidate
registerRoute(
  ({ request }) => 
    request.destination === 'style' ||
    request.destination === 'script' ||
    request.destination === 'worker',
  new StaleWhileRevalidate({
    cacheName: 'static-resources-v1'
  })
);

// Images - Cache First with 30 day expiration
registerRoute(
  ({ request }) => request.destination === 'image',
  new CacheFirst({
    cacheName: 'images-v1',
    plugins: [
      {
        cacheKeyWillBeUsed: async ({ request }) => request.url,
        cacheExpiration: {
          maxEntries: 100,
          maxAgeSeconds: 30 * 24 * 60 * 60 // 30 days
        }
      }
    ]
  })
);

// Push Notification Handler
self.addEventListener('push', (event) => {
  if (!event.data) return;
  
  const data = event.data.json();
  
  const options: NotificationOptions = {
    body: data.message,
    icon: data.icon || '/icon-192.png',
    badge: data.badge || '/badge-72.png',
    data: data.data,
    actions: data.actions || [],
    vibrate: [200, 100, 200],
    requireInteraction: data.requireInteraction || false,
    tag: data.tag || 'default'
  };
  
  event.waitUntil(
    self.registration.showNotification(data.title, options)
  );
});

// Notification Click Handler - Smart URL routing
self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  
  const data = event.notification.data;
  let url = '/';
  
  if (event.action) {
    // Handle contextual notification actions
    switch (event.action) {
      case 'view':
        url = data.url || '/';
        break;
      case 'assign':
        url = `${data.url}/assign` || '/opportunities';
        break;
      case 'complete':
        // Handle background task completion
        event.waitUntil(completeTaskInBackground(data.taskId));
        return;
      case 'snooze':
        event.waitUntil(snoozeTask(data.taskId, data.snoozeMinutes || 60));
        return;
    }
  } else {
    url = data.url || '/';
  }
  
  event.waitUntil(
    clients.openWindow(url)
  );
});

// Background Sync for offline operations
self.addEventListener('sync', (event) => {
  if (event.tag === 'background-sync') {
    event.waitUntil(syncOfflineData());
  }
});

async function syncOfflineData() {
  try {
    const db = await openDB('freshplan-offline', 1);
    const tx = db.transaction('pending-operations', 'readonly');
    const operations = await tx.objectStore('pending-operations').getAll();
    
    let successCount = 0;
    let errorCount = 0;
    
    for (const operation of operations) {
      try {
        const response = await fetch(operation.url, {
          method: operation.method,
          headers: operation.headers,
          body: operation.body
        });
        
        if (response.ok) {
          // Remove from offline queue
          const deleteTx = db.transaction('pending-operations', 'readwrite');
          await deleteTx.objectStore('pending-operations').delete(operation.id);
          successCount++;
        } else {
          errorCount++;
        }
      } catch (error) {
        console.error('Sync operation failed:', error);
        errorCount++;
      }
    }
    
    // Show sync summary notification if any operations were processed
    if (successCount > 0 || errorCount > 0) {
      await self.registration.showNotification('Synchronisation abgeschlossen', {
        body: `${successCount} erfolgreich, ${errorCount} Fehler`,
        icon: '/icon-192.png',
        tag: 'sync-complete',
        requireInteraction: false
      });
    }
    
  } catch (error) {
    console.error('Background sync failed:', error);
  }
}

async function completeTaskInBackground(taskId: string) {
  try {
    const response = await fetch(`/api/tasks/${taskId}/complete`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' }
    });
    
    if (response.ok) {
      await self.registration.showNotification('Aufgabe erledigt', {
        body: 'Die Aufgabe wurde erfolgreich als erledigt markiert.',
        icon: '/icon-192.png',
        tag: 'task-completed'
      });
    }
  } catch (error) {
    console.error('Failed to complete task:', error);
  }
}

async function snoozeTask(taskId: string, minutes: number) {
  try {
    const response = await fetch(`/api/tasks/${taskId}/snooze`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ snoozeMinutes: minutes })
    });
    
    if (response.ok) {
      await self.registration.showNotification('Aufgabe verschoben', {
        body: `Erinnerung in ${minutes} Minuten.`,
        icon: '/icon-192.png',
        tag: 'task-snoozed'
      });
    }
  } catch (error) {
    console.error('Failed to snooze task:', error);
  }
}
```

### 3. PWA Install Prompt Component (Frontend)

```typescript
// /frontend/src/components/PWAInstallPrompt.tsx
import React, { useState, useEffect } from 'react';
import {
  Alert,
  Button,
  Box,
  Typography,
  IconButton,
  Slide,
  Chip
} from '@mui/material';
import {
  InstallMobile as InstallMobileIcon,
  Close as CloseIcon,
  Smartphone as SmartphoneIcon,
  Download as DownloadIcon
} from '@mui/icons-material';

interface BeforeInstallPromptEvent extends Event {
  prompt(): Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

export const PWAInstallPrompt: React.FC = () => {
  const [deferredPrompt, setDeferredPrompt] = useState<BeforeInstallPromptEvent | null>(null);
  const [showPrompt, setShowPrompt] = useState(false);
  const [isInstalled, setIsInstalled] = useState(false);
  
  useEffect(() => {
    // Check if PWA is already installed
    const checkInstalled = () => {
      const isInStandaloneMode = window.matchMedia('(display-mode: standalone)').matches ||
                                (window.navigator as any).standalone === true;
      setIsInstalled(isInStandaloneMode);
    };
    
    checkInstalled();
    
    // Listen for beforeinstallprompt event
    const handleBeforeInstallPrompt = (e: Event) => {
      e.preventDefault();
      setDeferredPrompt(e as BeforeInstallPromptEvent);
      
      // Don't show immediately - wait for user interaction
      setTimeout(() => {
        if (!isRecentlyDismissed()) {
          setShowPrompt(true);
        }
      }, 3000); // Show after 3 seconds of app usage
    };
    
    // Listen for app installation
    const handleAppInstalled = () => {
      setIsInstalled(true);
      setShowPrompt(false);
      
      // Track successful installation
      if (typeof gtag !== 'undefined') {
        gtag('event', 'pwa_installed', {
          method: 'browser_prompt'
        });
      }
    };
    
    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    window.addEventListener('appinstalled', handleAppInstalled);
    
    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
      window.removeEventListener('appinstalled', handleAppInstalled);
    };
  }, []);
  
  const handleInstall = async () => {
    if (!deferredPrompt) return;
    
    try {
      // Track install prompt shown
      if (typeof gtag !== 'undefined') {
        gtag('event', 'pwa_install_prompt_shown');
      }
      
      await deferredPrompt.prompt();
      const { outcome } = await deferredPrompt.userChoice;
      
      // Track user choice
      if (typeof gtag !== 'undefined') {
        gtag('event', 'pwa_install_prompt_result', {
          outcome: outcome
        });
      }
      
      if (outcome === 'accepted') {
        setShowPrompt(false);
        // Show success message
        setTimeout(() => {
          if (!isInstalled) { // Only show if installation detection didn't fire
            alert('FreshPlan wurde erfolgreich installiert! Sie finden die App nun auf Ihrem Homescreen.');
          }
        }, 1000);
      }
      
    } catch (error) {
      console.error('Installation failed:', error);
    } finally {
      setDeferredPrompt(null);
    }
  };
  
  const handleDismiss = () => {
    setShowPrompt(false);
    // Remember dismissal for 7 days
    localStorage.setItem('pwa-install-dismissed', Date.now().toString());
    
    // Track dismissal
    if (typeof gtag !== 'undefined') {
      gtag('event', 'pwa_install_prompt_dismissed');
    }
  };
  
  const isRecentlyDismissed = (): boolean => {
    const dismissedTime = localStorage.getItem('pwa-install-dismissed');
    if (!dismissedTime) return false;
    
    const daysSinceDismissal = (Date.now() - Number(dismissedTime)) / (1000 * 60 * 60 * 24);
    return daysSinceDismissal < 7; // Don't show again for 7 days
  };
  
  // Don't show if already installed or recently dismissed
  if (isInstalled || !showPrompt || !deferredPrompt) {
    return null;
  }
  
  return (
    <Slide direction="up" in={showPrompt} mountOnEnter unmountOnExit>
      <Alert 
        severity="info"
        sx={{ 
          position: 'fixed',
          bottom: 16,
          left: 16,
          right: 16,
          zIndex: 1300,
          borderRadius: 2,
          boxShadow: 3,
          '& .MuiAlert-message': { 
            flex: 1,
            display: 'flex',
            alignItems: 'center'
          }
        }}
        action={
          <Box display="flex" gap={1}>
            <Button 
              color="inherit" 
              size="small" 
              variant="contained"
              onClick={handleInstall}
              startIcon={<DownloadIcon />}
              sx={{ 
                bgcolor: 'primary.main',
                color: 'white',
                '&:hover': { bgcolor: 'primary.dark' }
              }}
            >
              Installieren
            </Button>
            <IconButton 
              color="inherit" 
              size="small"
              onClick={handleDismiss}
              aria-label="Schließen"
            >
              <CloseIcon />
            </IconButton>
          </Box>
        }
      >
        <Box display="flex" alignItems="center" gap={2}>
          <SmartphoneIcon color="primary" />
          <Box>
            <Typography variant="subtitle2" fontWeight="bold">
              App installieren
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Bessere Performance und Offline-Nutzung!
            </Typography>
          </Box>
          <Chip 
            label="Kostenlos" 
            size="small" 
            color="success"
            sx={{ ml: 'auto' }}
          />
        </Box>
      </Alert>
    </Slide>
  );
};

export default PWAInstallPrompt;
```

### 4. Offline Storage Service (Frontend)

```typescript
// /frontend/src/services/OfflineStorageService.ts
class OfflineStorageService {
  private dbName = 'freshplan-offline';
  private dbVersion = 1;
  private db: IDBDatabase | null = null;
  
  async init(): Promise<void> {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.dbName, this.dbVersion);
      
      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        this.db = request.result;
        resolve();
      };
      
      request.onupgradeneeded = (event) => {
        const db = (event.target as IDBOpenDBRequest).result;
        
        // Create object stores for different entities
        if (!db.objectStoreNames.contains('customers')) {
          const customerStore = db.createObjectStore('customers', { keyPath: 'id' });
          customerStore.createIndex('updatedAt', 'updatedAt');
          customerStore.createIndex('syncStatus', 'syncStatus');
          customerStore.createIndex('companyName', 'companyName');
        }
        
        if (!db.objectStoreNames.contains('opportunities')) {
          const opportunityStore = db.createObjectStore('opportunities', { keyPath: 'id' });
          opportunityStore.createIndex('customerId', 'customerId');
          opportunityStore.createIndex('status', 'status');
          opportunityStore.createIndex('updatedAt', 'updatedAt');
        }
        
        if (!db.objectStoreNames.contains('activities')) {
          const activityStore = db.createObjectStore('activities', { keyPath: 'id' });
          activityStore.createIndex('customerId', 'customerId');
          activityStore.createIndex('opportunityId', 'opportunityId');
          activityStore.createIndex('timestamp', 'timestamp');
        }
        
        if (!db.objectStoreNames.contains('pending-operations')) {
          const pendingStore = db.createObjectStore('pending-operations', { 
            keyPath: 'id', 
            autoIncrement: true 
          });
          pendingStore.createIndex('timestamp', 'timestamp');
          pendingStore.createIndex('entityType', 'entityType');
          pendingStore.createIndex('priority', 'priority');
        }
        
        if (!db.objectStoreNames.contains('sync-metadata')) {
          const metadataStore = db.createObjectStore('sync-metadata', { keyPath: 'key' });
        }
      };
    });
  }
  
  async saveEntity(storeName: string, entity: any, options: { syncStatus?: string } = {}): Promise<void> {
    if (!this.db) await this.init();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction([storeName], 'readwrite');
      const store = transaction.objectStore(storeName);
      
      // Add offline metadata
      const entityWithMetadata = {
        ...entity,
        offlineUpdatedAt: new Date().toISOString(),
        syncStatus: options.syncStatus || 'pending',
        deviceId: this.getDeviceId()
      };
      
      const request = store.put(entityWithMetadata);
      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }
  
  async getEntity(storeName: string, id: string): Promise<any | null> {
    if (!this.db) await this.init();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction([storeName], 'readonly');
      const store = transaction.objectStore(storeName);
      const request = store.get(id);
      
      request.onsuccess = () => resolve(request.result || null);
      request.onerror = () => reject(request.error);
    });
  }
  
  async getAllEntities(storeName: string, filter?: { index?: string, value?: any }): Promise<any[]> {
    if (!this.db) await this.init();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction([storeName], 'readonly');
      const store = transaction.objectStore(storeName);
      
      let request: IDBRequest;
      
      if (filter?.index && filter?.value) {
        const index = store.index(filter.index);
        request = index.getAll(filter.value);
      } else {
        request = store.getAll();
      }
      
      request.onsuccess = () => resolve(request.result || []);
      request.onerror = () => reject(request.error);
    });
  }
  
  async deleteEntity(storeName: string, id: string): Promise<void> {
    if (!this.db) await this.init();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction([storeName], 'readwrite');
      const store = transaction.objectStore(storeName);
      const request = store.delete(id);
      
      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }
  
  async addPendingOperation(operation: OfflineOperation): Promise<void> {
    if (!this.db) await this.init();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['pending-operations'], 'readwrite');
      const store = transaction.objectStore('pending-operations');
      
      const operationWithMetadata = {
        ...operation,
        timestamp: new Date().toISOString(),
        retryCount: 0,
        deviceId: this.getDeviceId(),
        priority: operation.priority || 'normal'
      };
      
      const request = store.add(operationWithMetadata);
      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }
  
  async getPendingOperations(limit?: number): Promise<OfflineOperation[]> {
    if (!this.db) await this.init();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['pending-operations'], 'readonly');
      const store = transaction.objectStore('pending-operations');
      const index = store.index('timestamp');
      
      // Get operations sorted by timestamp (oldest first)
      const request = index.getAll();
      
      request.onsuccess = () => {
        const operations = request.result || [];
        const limitedOps = limit ? operations.slice(0, limit) : operations;
        resolve(limitedOps);
      };
      request.onerror = () => reject(request.error);
    });
  }
  
  async clearPendingOperation(id: number): Promise<void> {
    if (!this.db) await this.init();
    
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['pending-operations'], 'readwrite');
      const store = transaction.objectStore('pending-operations');
      const request = store.delete(id);
      
      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }
  
  async updateSyncStatus(storeName: string, entityId: string, status: 'pending' | 'syncing' | 'synced' | 'error'): Promise<void> {
    const entity = await this.getEntity(storeName, entityId);
    if (entity) {
      entity.syncStatus = status;
      entity.lastSyncAttempt = new Date().toISOString();
      await this.saveEntity(storeName, entity, { syncStatus: status });
    }
  }
  
  async getStorageStats(): Promise<{ used: number, available: number, entities: Record<string, number> }> {
    if (!this.db) await this.init();
    
    const stores = ['customers', 'opportunities', 'activities', 'pending-operations'];
    const entityCounts: Record<string, number> = {};
    
    for (const storeName of stores) {
      const entities = await this.getAllEntities(storeName);
      entityCounts[storeName] = entities.length;
    }
    
    // Estimate storage usage (rough calculation)
    let estimatedSize = 0;
    for (const [storeName, count] of Object.entries(entityCounts)) {
      estimatedSize += count * 1024; // Estimate 1KB per entity
    }
    
    return {
      used: estimatedSize,
      available: 50 * 1024 * 1024, // Assume 50MB available (varies by browser)
      entities: entityCounts
    };
  }
  
  private getDeviceId(): string {
    let deviceId = localStorage.getItem('device-id');
    if (!deviceId) {
      deviceId = 'device_' + Math.random().toString(36).substr(2, 9);
      localStorage.setItem('device-id', deviceId);
    }
    return deviceId;
  }
  
  async clearAllData(): Promise<void> {
    if (!this.db) await this.init();
    
    const stores = ['customers', 'opportunities', 'activities', 'pending-operations', 'sync-metadata'];
    
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(stores, 'readwrite');
      let completed = 0;
      
      stores.forEach(storeName => {
        const store = transaction.objectStore(storeName);
        const request = store.clear();
        
        request.onsuccess = () => {
          completed++;
          if (completed === stores.length) {
            resolve();
          }
        };
        
        request.onerror = () => reject(request.error);
      });
    });
  }
}

export interface OfflineOperation {
  id?: number;
  entityType: string;
  entityId: string;
  operation: 'CREATE' | 'UPDATE' | 'DELETE';
  url: string;
  method: string;
  headers: Record<string, string>;
  body?: string;
  timestamp?: string;
  retryCount?: number;
  deviceId?: string;
  priority?: 'high' | 'normal' | 'low';
}

export const offlineStorage = new OfflineStorageService();
```

### 5. Push Notifications Backend

```java
@Path("/api/notifications")
@ApplicationScoped
public class NotificationResource {
    
    @Inject
    NotificationService notificationService;
    
    @Inject
    PushSubscriptionRepository subscriptionRepo;
    
    @Inject
    SecurityContext securityContext;
    
    @POST
    @Path("/subscribe")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response subscribeToPush(PushSubscriptionRequest subscription) {
        UUID userId = getCurrentUserId();
        
        PushSubscription saved = notificationService.savePushSubscription(
            userId, subscription
        );
        
        return Response.status(201).entity(saved).build();
    }
    
    @DELETE
    @Path("/unsubscribe")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response unsubscribeFromPush() {
        UUID userId = getCurrentUserId();
        notificationService.removePushSubscription(userId);
        return Response.noContent().build();
    }
    
    @POST
    @Path("/send")
    @RolesAllowed({"admin", "manager"})
    public Response sendNotification(NotificationRequest request) {
        NotificationResult result = notificationService.sendPushNotification(
            request.getUserIds(),
            request.getTitle(),
            request.getMessage(),
            request.getPayload()
        );
        
        return Response.ok(result).build();
    }
    
    @POST
    @Path("/broadcast")
    @RolesAllowed({"admin"})
    public Response broadcastNotification(BroadcastNotificationRequest request) {
        NotificationResult result = notificationService.broadcastNotification(
            request.getTitle(),
            request.getMessage(),
            request.getPayload(),
            request.getTargetRoles()
        );
        
        return Response.ok(result).build();
    }
    
    @GET
    @Path("/vapid-public-key")
    public Response getVapidPublicKey() {
        String publicKey = notificationService.getVapidPublicKey();
        return Response.ok(Map.of("publicKey", publicKey)).build();
    }
    
    private UUID getCurrentUserId() {
        return SecurityUtils.getCurrentUserId(securityContext);
    }
}

@ApplicationScoped
@Transactional
public class NotificationService {
    
    @Inject
    WebPushService webPushService;
    
    @Inject
    PushSubscriptionRepository subscriptionRepo;
    
    @ConfigProperty(name = "vapid.public.key")
    String vapidPublicKey;
    
    @ConfigProperty(name = "vapid.private.key") 
    String vapidPrivateKey;
    
    public String getVapidPublicKey() {
        return vapidPublicKey;
    }
    
    public PushSubscription savePushSubscription(UUID userId, PushSubscriptionRequest request) {
        // Remove any existing subscription for this user/device
        subscriptionRepo.deleteByUserIdAndEndpoint(userId, request.getEndpoint());
        
        PushSubscription subscription = PushSubscription.builder()
            .userId(userId)
            .endpoint(request.getEndpoint())
            .p256dhKey(request.getKeys().getP256dh())
            .authKey(request.getKeys().getAuth())
            .userAgent(request.getUserAgent())
            .deviceType(detectDeviceType(request.getUserAgent()))
            .active(true)
            .createdAt(LocalDateTime.now())
            .build();
            
        subscriptionRepo.persist(subscription);
        
        Log.infof("Saved push subscription for user %s", userId);
        return subscription;
    }
    
    public void removePushSubscription(UUID userId) {
        subscriptionRepo.deleteByUserId(userId);
        Log.infof("Removed push subscription for user %s", userId);
    }
    
    public NotificationResult sendPushNotification(List<UUID> userIds, 
                                                 String title, 
                                                 String message,
                                                 Map<String, Object> payload) {
        List<PushSubscription> subscriptions = subscriptionRepo
            .findActiveByUserIds(userIds);
            
        List<NotificationDelivery> deliveries = new ArrayList<>();
        
        for (PushSubscription subscription : subscriptions) {
            try {
                PushNotificationPayload notification = PushNotificationPayload.builder()
                    .title(title)
                    .message(message)
                    .icon("/icon-192.png")
                    .badge("/badge-72.png")
                    .data(payload)
                    .actions(createContextualActions(payload))
                    .requireInteraction(isHighPriority(payload))
                    .tag(generateNotificationTag(payload))
                    .build();
                    
                webPushService.sendNotification(
                    subscription.getEndpoint(),
                    subscription.getKeys(),
                    notification.toJson(),
                    vapidPublicKey,
                    vapidPrivateKey
                );
                
                deliveries.add(NotificationDelivery.success(subscription.getUserId()));
                
            } catch (Exception e) {
                Log.errorf("Failed to send push notification to %s: %s", 
                    subscription.getUserId(), e.getMessage());
                    
                // Mark subscription as inactive if it's expired
                if (e.getMessage().contains("expired") || e.getMessage().contains("invalid")) {
                    subscription.setActive(false);
                    subscriptionRepo.persist(subscription);
                }
                
                deliveries.add(NotificationDelivery.failed(
                    subscription.getUserId(), e.getMessage()
                ));
            }
        }
        
        return NotificationResult.builder()
            .totalSent(deliveries.size())
            .successCount((int) deliveries.stream().filter(NotificationDelivery::isSuccess).count())
            .deliveries(deliveries)
            .build();
    }
    
    private List<NotificationAction> createContextualActions(Map<String, Object> payload) {
        List<NotificationAction> actions = new ArrayList<>();
        String type = (String) payload.get("type");
        
        switch (type) {
            case "NEW_OPPORTUNITY":
                actions.add(new NotificationAction("view", "Anzeigen", "/icon-view.png"));
                actions.add(new NotificationAction("assign", "Zuweisen", "/icon-assign.png"));
                break;
            case "TASK_REMINDER":
                actions.add(new NotificationAction("complete", "Erledigen", "/icon-check.png"));
                actions.add(new NotificationAction("snooze", "Später (1h)", "/icon-snooze.png"));
                break;
            case "CUSTOMER_UPDATE":
                actions.add(new NotificationAction("view", "Kunde öffnen", "/icon-customer.png"));
                break;
            case "DEAL_CLOSED":
                actions.add(new NotificationAction("view", "Details", "/icon-money.png"));
                break;
        }
        
        return actions;
    }
    
    private boolean isHighPriority(Map<String, Object> payload) {
        String priority = (String) payload.get("priority");
        return "high".equals(priority) || "urgent".equals(priority);
    }
    
    private String generateNotificationTag(Map<String, Object> payload) {
        String type = (String) payload.get("type");
        String entityId = (String) payload.get("entityId");
        return type + "_" + (entityId != null ? entityId : "general");
    }
    
    private String detectDeviceType(String userAgent) {
        if (userAgent == null) return "unknown";
        
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile") || userAgent.contains("android")) {
            return "mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "tablet";
        } else {
            return "desktop";
        }
    }
}

@Data
@Builder
public class PushNotificationPayload {
    private String title;
    private String message;
    private String icon;
    private String badge;
    private Map<String, Object> data;
    private List<NotificationAction> actions;
    private boolean requireInteraction;
    private String tag;
    
    public String toJson() {
        // Convert to JSON string for Web Push
        return Json.createObjectBuilder()
            .add("title", title)
            .add("body", message)
            .add("icon", icon)
            .add("badge", badge)
            .add("data", Json.createObjectBuilder(data != null ? data : Map.of()).build())
            .add("requireInteraction", requireInteraction)
            .add("tag", tag)
            .build()
            .toString();
    }
}

@Data
@AllArgsConstructor
public class NotificationAction {
    private String action;
    private String title;
    private String icon;
}
```

### 6. Offline-First API Wrapper (Frontend)

```typescript
// /frontend/src/services/OfflineFirstAPI.ts
class OfflineFirstAPI {
  
  async request<T>(url: string, options: RequestInit = {}): Promise<T> {
    const isOnline = navigator.onLine;
    
    if (isOnline) {
      try {
        const response = await this.makeRequest(url, options);
        
        if (response.ok) {
          const data = await response.json();
          await this.cacheResponse(url, data);
          return data;
        } else {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
      } catch (error) {
        console.warn('Network request failed, falling back to cache:', error);
        return await this.getCachedResponse<T>(url);
      }
    } else {
      return await this.getCachedResponse<T>(url);
    }
  }
  
  async create<T>(url: string, data: any): Promise<T> {
    const isOnline = navigator.onLine;
    
    if (isOnline) {
      try {
        const response = await this.makeRequest(url, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(data)
        });
        
        if (response.ok) {
          const result = await response.json();
          await this.updateLocalStorage(data.entityType, result);
          return result;
        } else {
          throw new Error(`HTTP ${response.status}`);
        }
      } catch (error) {
        console.warn('Create request failed, queuing for later sync:', error);
        await this.queueOperation('CREATE', url, data);
        return this.createOptimisticResponse(data);
      }
    } else {
      await this.queueOperation('CREATE', url, data);
      return this.createOptimisticResponse(data);
    }
  }
  
  async update<T>(url: string, data: any): Promise<T> {
    const isOnline = navigator.onLine;
    
    if (isOnline) {
      try {
        const response = await this.makeRequest(url, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(data)
        });
        
        if (response.ok) {
          const result = await response.json();
          await this.updateLocalStorage(data.entityType, result);
          return result;
        } else {
          throw new Error(`HTTP ${response.status}`);
        }
      } catch (error) {
        await this.queueOperation('UPDATE', url, data);
        await this.updateLocalStorage(data.entityType, data, { syncStatus: 'pending' });
        return data;
      }
    } else {
      await this.queueOperation('UPDATE', url, data);
      await this.updateLocalStorage(data.entityType, data, { syncStatus: 'pending' });
      return data;
    }
  }
  
  async delete<T>(url: string, entityId: string, entityType: string): Promise<void> {
    const isOnline = navigator.onLine;
    
    if (isOnline) {
      try {
        const response = await this.makeRequest(url, { method: 'DELETE' });
        
        if (response.ok) {
          await offlineStorage.deleteEntity(entityType, entityId);
          return;
        } else {
          throw new Error(`HTTP ${response.status}`);
        }
      } catch (error) {
        await this.queueOperation('DELETE', url, { id: entityId, entityType });
        // Mark as deleted locally but keep for sync
        await offlineStorage.updateSyncStatus(entityType, entityId, 'pending');
      }
    } else {
      await this.queueOperation('DELETE', url, { id: entityId, entityType });
      await offlineStorage.updateSyncStatus(entityType, entityId, 'pending');
    }
  }
  
  private async makeRequest(url: string, options: RequestInit): Promise<Response> {
    // Add auth headers if available
    const authToken = localStorage.getItem('auth-token');
    const headers = new Headers(options.headers);
    
    if (authToken) {
      headers.set('Authorization', `Bearer ${authToken}`);
    }
    
    return fetch(url, {
      ...options,
      headers
    });
  }
  
  private async cacheResponse(url: string, data: any): Promise<void> {
    try {
      let cacheDuration = 5 * 60 * 1000; // 5 minutes default
      
      // Adjust cache duration based on data type
      if (url.includes('/customers')) {
        cacheDuration = 30 * 60 * 1000; // 30 minutes for customers
      } else if (url.includes('/opportunities')) {
        cacheDuration = 10 * 60 * 1000; // 10 minutes for opportunities
      } else if (url.includes('/activities')) {
        cacheDuration = 60 * 60 * 1000; // 1 hour for activities
      }
      
      const cacheEntry = {
        data,
        timestamp: Date.now(),
        expiry: Date.now() + cacheDuration,
        url
      };
      
      localStorage.setItem(`api_cache_${btoa(url)}`, JSON.stringify(cacheEntry));
    } catch (error) {
      console.warn('Failed to cache response:', error);
    }
  }
  
  private async getCachedResponse<T>(url: string): Promise<T> {
    try {
      const cached = localStorage.getItem(`api_cache_${btoa(url)}`);
      
      if (cached) {
        const entry = JSON.parse(cached);
        
        if (Date.now() < entry.expiry) {
          return entry.data;
        } else {
          localStorage.removeItem(`api_cache_${btoa(url)}`);
        }
      }
      
      throw new Error('No cached data available');
    } catch (error) {
      throw new Error(`Offline and no cached data for ${url}`);
    }
  }
  
  private async queueOperation(operation: string, url: string, data: any): Promise<void> {
    const pendingOp: OfflineOperation = {
      entityType: this.getEntityTypeFromUrl(url),
      entityId: data.id || 'temp_' + Date.now(),
      operation: operation as any,
      url,
      method: operation === 'CREATE' ? 'POST' : operation === 'UPDATE' ? 'PUT' : 'DELETE',
      headers: { 'Content-Type': 'application/json' },
      body: operation !== 'DELETE' ? JSON.stringify(data) : undefined,
      priority: this.determinePriority(operation, data)
    };
    
    await offlineStorage.addPendingOperation(pendingOp);
    
    // Trigger background sync if available
    if ('serviceWorker' in navigator && 'sync' in window.ServiceWorkerRegistration.prototype) {
      try {
        const registration = await navigator.serviceWorker.ready;
        await registration.sync.register('background-sync');
      } catch (error) {
        console.warn('Background sync registration failed:', error);
      }
    }
  }
  
  private async updateLocalStorage(entityType: string, data: any, options?: { syncStatus?: string }): Promise<void> {
    const storeName = this.getStoreNameFromEntityType(entityType);
    await offlineStorage.saveEntity(storeName, data, options);
  }
  
  private createOptimisticResponse(data: any): any {
    return {
      ...data,
      id: data.id || `temp_${Date.now()}`,
      _offline: true,
      _pending: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
  }
  
  private getEntityTypeFromUrl(url: string): string {
    if (url.includes('/customers')) return 'customer';
    if (url.includes('/opportunities')) return 'opportunity';
    if (url.includes('/activities')) return 'activity';
    return 'unknown';
  }
  
  private getStoreNameFromEntityType(entityType: string): string {
    return entityType + 's'; // customer -> customers, opportunity -> opportunities
  }
  
  private determinePriority(operation: string, data: any): 'high' | 'normal' | 'low' {
    // High priority for creates and updates of opportunities
    if (operation === 'CREATE' && data.entityType === 'opportunity') return 'high';
    if (operation === 'UPDATE' && data.status === 'CLOSED_WON') return 'high';
    
    // Normal priority for most operations
    if (operation === 'CREATE' || operation === 'UPDATE') return 'normal';
    
    // Low priority for deletes and other operations
    return 'low';
  }
}

export const offlineAPI = new OfflineFirstAPI();

// Hook for easier usage in React components
export const useOfflineAPI = () => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [pendingCount, setPendingCount] = useState(0);
  
  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);
    
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    
    // Check pending operations count
    const checkPendingCount = async () => {
      const operations = await offlineStorage.getPendingOperations();
      setPendingCount(operations.length);
    };
    
    checkPendingCount();
    const interval = setInterval(checkPendingCount, 10000); // Check every 10 seconds
    
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
      clearInterval(interval);
    };
  }, []);
  
  return {
    api: offlineAPI,
    isOnline,
    pendingCount,
    storage: offlineStorage
  };
};
```

---

## 🎯 IMPLEMENTATION NOTES

**PWA Checklist:**
- ✅ HTTPS (required for PWA)
- ✅ Web App Manifest
- ✅ Service Worker
- ✅ Responsive Design
- ✅ Fast Load Time (< 3s)

**Service Worker Strategies:**
- **App Shell**: Cache First (instant loading)
- **API Calls**: Network First with 5s timeout
- **Images**: Cache First with 30-day expiration
- **Static Assets**: Stale While Revalidate

**Offline Capabilities:**
- 80% der Core-Features offline verfügbar
- Background Sync für CRUD Operations
- IndexedDB für strukturierte Daten
- Conflict Resolution: Last-Write-Wins (MVP)

**Push Notifications:**
- VAPID Keys für Server-Authentication
- Rich Notifications mit Action Buttons
- Contextual Actions je nach Notification-Type
- Graceful Fallback ohne Push-Support

---

## ✅ READY TO IMPLEMENT

**Implementierungs-Reihenfolge:**
1. **PWA Manifest** + Icons + Service Worker Basic Setup
2. **Offline Storage** + IndexedDB + Pending Operations Queue
3. **Push Notifications** Backend + Frontend Integration
4. **App Shell** + Install Prompt + PWA Status Components
5. **Integration** bestehender Features für Offline-Fähigkeit

**Estimated Effort:** 8-10 Tage  
**Dependencies:** M1 Navigation, M3 Sales Cockpit, FC-008 Security  
**Priority:** HIGH (Mobile-First Strategy)