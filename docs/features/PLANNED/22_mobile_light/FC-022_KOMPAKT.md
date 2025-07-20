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

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[🧭 M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md)** - Mobile Navigation
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - Mobile Auth
- **[👥 M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_KOMPAKT.md)** - Customer Data API

### ⚡ Synergien:
- **[📱 FC-018 Mobile PWA](/docs/features/PLANNED/18_mobile_pwa/FC-018_KOMPAKT.md)** - PWA Foundation
- **[📴 FC-032 Offline-First](/docs/features/PLANNED/32_offline_first/FC-032_KOMPAKT.md)** - Offline Strategy
- **[📱 FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_KOMPAKT.md)** - Full Mobile Version

### 🚀 Ermöglicht folgende Features:
- **[📸 FC-033 Visual Customer Cards](/docs/features/PLANNED/33_visual_cards/FC-033_KOMPAKT.md)** - Mobile Card View
- **[⚡ FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_KOMPAKT.md)** - Quick Mobile Actions
- **[🗣️ FC-029 Voice-First](/docs/features/PLANNED/29_voice_first/FC-029_KOMPAKT.md)** - Voice Notes

### 🎨 UI Integration:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Mobile Dashboard
- **[➕ M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md)** - Mobile Quick Create
- **[⚙️ M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)** - Mobile Settings

### 🔧 Technische Details:
- [FC-022_IMPLEMENTATION_GUIDE.md](./FC-022_IMPLEMENTATION_GUIDE.md) *(geplant)* - PWA Setup Guide
- [FC-022_DECISION_LOG.md](./FC-022_DECISION_LOG.md) *(geplant)* - Light vs. Full Mobile
- [MOBILE_PATTERNS.md](./MOBILE_PATTERNS.md) *(geplant)* - Touch-optimierte UI Patterns