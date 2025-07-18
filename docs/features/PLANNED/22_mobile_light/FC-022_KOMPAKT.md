# 📱 FC-022 MOBILE LIGHT (KOMPAKT)

**Erstellt:** 18.07.2025  
**Status:** 📋 READY TO START  
**Feature-Typ:** 🎨 FRONTEND  
**Priorität:** HIGH - Außendienst Support  
**Geschätzt:** 3 Tage  

---

## 🧠 WAS WIR BAUEN

**Problem:** Außendienst wartet nicht bis Tag 24 auf Mobile  
**Lösung:** Read-Only PWA mit Offline-Notizen  
**Value:** Sofortiger Zugriff unterwegs  

> **Business Case:** 5 Außendienstler × 10 Kundenbesuche/Tag = 50 mobile Zugriffe täglich

### 🎯 Kern-Features:
- **Read-Only Views:** Kunden & Opportunities
- **Offline Notizen:** Sync wenn wieder online
- **Visitenkarten Scanner:** Quick Contact Creation
- **PWA Installation:** "Add to Home Screen"

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **PWA Manifest erstellen:**
```bash
cd frontend/public
touch manifest.json
# → Template unten
```

### 2. **Service Worker Setup:**
```bash
cd frontend/src
touch service-worker.ts
# → Offline-First Strategy
```

### 3. **Mobile Views:**
```bash
cd frontend/src/features/mobile
mkdir -p components views
touch views/MobileCustomerView.tsx
```

**Geschätzt: 3 Tage**

---

## 📱 MOBILE-FIRST UI

```
┌─────────────────────────┐
│ 🔍 Suche               │
├─────────────────────────┤
│ 📋 Meine Kunden        │
│ ┌─────────────────────┐│
│ │ Müller GmbH        ││
│ │ Berlin • A-Kunde   ││
│ │ ☎️ 📧 📝           ││
│ └─────────────────────┘│
│ ┌─────────────────────┐│
│ │ Schmidt AG         ││
│ │ Hamburg • 250k €   ││
│ │ ☎️ 📧 📝           ││
│ └─────────────────────┘│
├─────────────────────────┤
│ [📷 Visitenkarte]      │
└─────────────────────────┘
```

---

## 🔧 TECHNISCHE DETAILS

### PWA Manifest:
```json
{
  "name": "FreshPlan Mobile",
  "short_name": "FreshPlan",
  "start_url": "/mobile",
  "display": "standalone",
  "theme_color": "#94C456",
  "background_color": "#ffffff",
  "icons": [{
    "src": "/icon-192.png",
    "sizes": "192x192",
    "type": "image/png"
  }]
}
```

### Offline Storage:
```typescript
// IndexedDB für Offline-Daten
const db = await openDB('freshplan-mobile', 1, {
  upgrade(db) {
    db.createObjectStore('customers');
    db.createObjectStore('notes');
  }
});

// Background Sync
self.addEventListener('sync', event => {
  if (event.tag === 'sync-notes') {
    event.waitUntil(syncNotes());
  }
});
```

### Mobile-Optimierte Komponenten:
```typescript
// Swipeable Cards
<SwipeableList>
  {customers.map(customer => (
    <SwipeableListItem
      swipeRight={{
        content: <CallButton />,
        action: () => call(customer.phone)
      }}
      swipeLeft={{
        content: <NoteButton />,
        action: () => addNote(customer.id)
      }}
    >
      <CustomerCard {...customer} />
    </SwipeableListItem>
  ))}
</SwipeableList>
```

---

## 📞 NÄCHSTE SCHRITTE

1. **PWA Setup** - manifest.json + Service Worker
2. **Mobile Router** - /mobile/* Routes  
3. **Customer List** - Optimiert für Touch
4. **Offline Notes** - IndexedDB + Sync
5. **Scanner Integration** - react-qr-reader
6. **Install Prompt** - beforeinstallprompt
7. **Performance** - Virtual Scrolling

**WICHTIG:** Mobile-First CSS von Anfang an!