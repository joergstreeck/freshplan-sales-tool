# 📱 FC-018 MOBILE PWA (KOMPAKT)

**Feature Code:** FC-018  
**Feature-Typ:** 🎨 FRONTEND  
**Geschätzter Aufwand:** 8-10 Tage  
**Priorität:** MEDIUM - Erweiterte Mobile-Unterstützung  
**ROI:** 40% mehr Mobile-Nutzung, keine App-Store Updates  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** Native Apps = 2x Entwicklung, App Store Approval, Updates nerven  
**Lösung:** Progressive Web App - Ein Code, überall, immer aktuell  
**Impact:** Install-Button im Browser, Offline-fähig, Push-Notifications  

---

## 📱 PWA FEATURES

```
MOBILE BROWSER → "Zum Homescreen"
         ↓
PWA INSTALLIERT
         ↓
• Vollbild ohne Browser-UI
• Offline-Funktionalität
• Push-Notifications
• Native App Feeling
• Auto-Updates
```

---

## 📋 FEATURES IM DETAIL

### 1. PWA Manifest & Service Worker

```json
// manifest.json
{
  "name": "FreshPlan Sales Tool",
  "short_name": "FreshPlan",
  "start_url": "/",
  "display": "standalone",
  "theme_color": "#94C456",
  "background_color": "#ffffff",
  "icons": [
    {
      "src": "/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "/icon-512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}
```

### 2. Offline-First Architecture

```typescript
// Service Worker mit Workbox
import { precacheAndRoute } from 'workbox-precaching';
import { registerRoute } from 'workbox-routing';
import { StaleWhileRevalidate, NetworkFirst } from 'workbox-strategies';

// Precache static assets
precacheAndRoute(self.__WB_MANIFEST);

// API calls - Network first, fallback to cache
registerRoute(
  ({ url }) => url.pathname.startsWith('/api/'),
  new NetworkFirst({
    cacheName: 'api-cache',
    networkTimeoutSeconds: 5
  })
);

// Images - Cache first
registerRoute(
  ({ request }) => request.destination === 'image',
  new StaleWhileRevalidate({
    cacheName: 'images'
  })
);
```

### 3. Install Prompt Component

```typescript
const InstallPWA: React.FC = () => {
  const [deferredPrompt, setDeferredPrompt] = useState<any>(null);
  const [showInstallButton, setShowInstallButton] = useState(false);

  useEffect(() => {
    window.addEventListener('beforeinstallprompt', (e) => {
      e.preventDefault();
      setDeferredPrompt(e);
      setShowInstallButton(true);
    });
  }, []);

  const handleInstall = async () => {
    if (!deferredPrompt) return;
    
    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    
    if (outcome === 'accepted') {
      trackEvent('PWA_INSTALLED');
    }
    
    setDeferredPrompt(null);
    setShowInstallButton(false);
  };

  if (!showInstallButton) return null;

  return (
    <Alert 
      severity="info"
      action={
        <Button color="inherit" size="small" onClick={handleInstall}>
          Installieren
        </Button>
      }
    >
      FreshPlan als App installieren für bessere Performance!
    </Alert>
  );
};
```

### 4. Push Notifications

```typescript
// Push Notification Service
const NotificationService = {
  async requestPermission() {
    const permission = await Notification.requestPermission();
    if (permission === 'granted') {
      await this.subscribeUser();
    }
  },

  async subscribeUser() {
    const registration = await navigator.serviceWorker.ready;
    const subscription = await registration.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: urlB64ToUint8Array(PUBLIC_VAPID_KEY)
    });
    
    // Send to backend
    await api.post('/notifications/subscribe', subscription);
  },

  async sendNotification(title: string, options: NotificationOptions) {
    const registration = await navigator.serviceWorker.ready;
    registration.showNotification(title, {
      ...options,
      icon: '/icon-192.png',
      badge: '/badge-72.png',
      vibrate: [200, 100, 200]
    });
  }
};
```

### 5. App Shell Architecture

```typescript
// App Shell für schnelles Initial Loading
const AppShell: React.FC = () => {
  return (
    <Box sx={{ display: 'flex', height: '100vh' }}>
      {/* Skeleton während Content lädt */}
      <Skeleton variant="rectangular" width={240} height="100%" />
      <Box flex={1}>
        <Skeleton variant="rectangular" height={64} />
        <Box p={2}>
          <Skeleton variant="text" sx={{ fontSize: '2rem' }} />
          <Skeleton variant="rectangular" height={300} sx={{ mt: 2 }} />
        </Box>
      </Box>
    </Box>
  );
};
```

### 6. Background Sync

```typescript
// Background Sync für Offline-Aktionen
self.addEventListener('sync', (event) => {
  if (event.tag === 'sync-opportunities') {
    event.waitUntil(syncOpportunities());
  }
});

async function syncOpportunities() {
  const db = await openDB('freshplan-offline', 1);
  const tx = db.transaction('pending-updates', 'readonly');
  const updates = await tx.objectStore('pending-updates').getAll();
  
  for (const update of updates) {
    try {
      await fetch('/api/opportunities', {
        method: update.method,
        body: JSON.stringify(update.data),
        headers: { 'Content-Type': 'application/json' }
      });
      
      // Remove from pending
      await db.delete('pending-updates', update.id);
    } catch (error) {
      console.error('Sync failed, will retry', error);
    }
  }
}
```

---

## 🎯 BUSINESS VALUE

- **Keine App-Store Abhängigkeit:** Updates sofort live
- **Plattform-unabhängig:** iOS, Android, Desktop
- **Offline-fähig:** Auch ohne Netz produktiv
- **Bessere Performance:** Caching & Service Worker
- **Higher Engagement:** Push Notifications

---

## 🚀 IMPLEMENTIERUNGS-PHASEN

1. **Phase 1:** PWA Manifest & Basic Service Worker
2. **Phase 2:** Offline-First mit IndexedDB
3. **Phase 3:** Push Notifications
4. **Phase 4:** Background Sync & Advanced Features

---

## 🔗 ABHÄNGIGKEITEN

- **Benötigt:** Modern Browser mit Service Worker Support
- **Integration:** Alle bestehenden Features
- **Nice-to-have:** FC-032 Offline-First Architecture

---

## 📊 SUCCESS METRICS

- **Install Rate:** > 60% der Mobile User
- **Offline Usage:** > 30% der Sessions
- **Performance:** < 3s Time to Interactive
- **Push Opt-in:** > 40% der User

---

**Nächster Schritt:** PWA Manifest erstellen und Service Worker Setup

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[🧭 M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md)** - App Shell Structure
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - Auth in Service Worker
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Hauptansicht der PWA

### ⚡ Synergien:
- **[📱 FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_KOMPAKT.md)** - Shared Mobile Components
- **[📱 FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_KOMPAKT.md)** - Lightweight Alternative
- **[📴 FC-032 Offline-First](/docs/features/PLANNED/32_offline_first/FC-032_KOMPAKT.md)** - Offline Architecture

### 🚀 Ermöglicht folgende Features:
- **[🔔 FC-012 Team Communication](/docs/features/PLANNED/14_team_communication/FC-012_KOMPAKT.md)** - Push Notifications
- **[📸 FC-033 Visual Customer Cards](/docs/features/PLANNED/33_visual_cards/FC-033_KOMPAKT.md)** - Offline Image Cache
- **[⚡ FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_KOMPAKT.md)** - PWA Quick Actions

### 🎨 UI Integration:
- **[➕ M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md)** - Offline Queue
- **[⚙️ M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)** - PWA Settings
- **[🧮 M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_KOMPAKT.md)** - Offline Calculations

### 🔧 Technische Details:
- [FC-018_IMPLEMENTATION_GUIDE.md](./FC-018_IMPLEMENTATION_GUIDE.md) *(geplant)* - PWA Setup Details
- [FC-018_DECISION_LOG.md](./FC-018_DECISION_LOG.md) *(geplant)* - Native vs. PWA Decision
- [SERVICE_WORKER_STRATEGY.md](./SERVICE_WORKER_STRATEGY.md) *(geplant)* - Caching Strategies