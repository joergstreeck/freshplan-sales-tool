# FC-006: Mobile Companion App - Spracheingabe ⚡

**Feature Code:** FC-006  
**Feature-Typ:** 🎨 FRONTEND  
**Geschätzter Aufwand:** 15-20 Tage  
**Priorität:** HOCH - Game-Changer für Außendienst  
**ROI:** 15 Min/Tag Zeitersparnis pro Verkäufer  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** "Komme vom Kunden, will schnell was festhalten" = umständlich  
**Lösung:** Speak & Snap - Spracheingabe + Foto = fertig  
**Impact:** 80%+ Adoption, vollständige Offline-Funktion  

---

## 📱 KERN-FEATURES

```
1. SPRACHE → TEXT → ENTITIES
   "Kunde will 500 Stück im April" → Automatisch strukturiert

2. FOTO → KONTAKT
   Visitenkarte abfotografieren → Kontakt angelegt

3. OFFLINE → ONLINE → SYNC
   Alles funktioniert offline → Automatische Synchronisation

4. CONTEXT → SMART
   GPS + Kalender = Weiß wo du bist und was ansteht
```

---

## 🏃 QUICK-START IMPLEMENTIERUNG

### Voice Processing
```typescript
async processVoiceNote(audioBlob: Blob) {
  // 1. Whisper API
  const transcript = await whisperApi.transcribe(audioBlob);
  
  // 2. Entity Extraction
  const entities = await nlpProcessor.extract(transcript);
  
  // 3. Return strukturiert
  return {
    text: transcript,
    customer: entities.customer,
    quantities: entities.quantities,    // "500 Stück"
    dates: entities.dates,              // "April"
    nextActions: entities.actions       // "Angebot senden"
  };
}
```

### Offline Sync
```typescript
class SyncManager {
  async syncData() {
    if (!isOnline()) {
      await queue.add(changes);
      return;
    }
    
    // Upload pending
    const pending = await queue.getPending();
    await uploadBatch(pending);
    
    // Pull latest
    await pullLatestData();
  }
}
```

### Context Awareness
```typescript
async getCurrentContext() {
  const location = await getLocation();
  const calendar = await getCalendarEvents();
  
  return {
    probableCustomer: findNearbyCustomer(location),
    currentAppointment: getCurrentAppointment(calendar)
  };
}
```

---

## 🔗 DEPENDENCIES & INTEGRATION

**Backend benötigt:**
- GraphQL API (effiziente Mobile Queries)
- Sync API (Conflict Resolution)
- Push Service (Firebase)
- File Upload (Audio/Images)

**Externe Services:**
- OpenAI Whisper (Speech-to-Text)
- AWS Textract (OCR)
- Mapbox (Location Services)

**Integration mit:**
- FC-004 (Schutz-Status mobil)
- FC-005 (Push bei Zahlung)

---

## ⚡ KRITISCHE ENTSCHEIDUNGEN

1. **React Native vs. Flutter vs. Native?**
2. **Offline-Storage: SQLite vs. Realm?**
3. **Speech API: Whisper vs. Google vs. On-Device?**

---

## 📊 SUCCESS METRICS

- **Adoption:** >80% Außendienst nutzt täglich
- **Speed:** <3s von Sprache zu strukturierten Daten
- **Accuracy:** 95% Entity-Erkennung
- **Offline:** 30% aller Aktionen offline

---

## 🚀 NÄCHSTER SCHRITT

1. React Native Projekt Setup
2. Keycloak Mobile Auth
3. Basis-UI mit 3 Hauptaktionen

**Detaillierte Implementierung:** [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)  
**UI/UX Mockups:** [MOBILE_DESIGN.md](./MOBILE_DESIGN.md)

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - Keycloak Mobile Auth
- **[🧭 M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md)** - Mobile Navigation Pattern
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Desktop-Pendant

### ⚡ Mobile-First Features:
- **[🛡️ FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_KOMPAKT.md)** - Schutz-Status mobil
- **[📥 FC-005 Xentral Integration](/docs/features/PLANNED/08_xentral_integration/FC-005_KOMPAKT.md)** - Push bei Zahlung
- **[➕ M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md)** - Mobile Quick Create Pattern

### 🚀 Ermöglicht folgende Features:
- **[📱 FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_KOMPAKT.md)** - Vereinfachte Version
- **[🎙️ FC-029 Voice-First Interface](/docs/features/PLANNED/29_voice_first/FC-029_KOMPAKT.md)** - Voice UI Patterns
- **[🌐 FC-032 Offline-First Architecture](/docs/features/PLANNED/32_offline_first/FC-032_KOMPAKT.md)** - Sync-Strategien

### 🎨 UI Integration:
- **[🔍 FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_KOMPAKT.md)** - Mobile One-Tap UI
- **[📸 FC-033 Visual Customer Cards](/docs/features/PLANNED/33_visual_cards/FC-033_KOMPAKT.md)** - Foto-Integration
- **[⚙️ M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)** - Mobile Settings Sync

### 🔧 Technische Details:
- [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) *(geplant)* - React Native Setup
- [MOBILE_DESIGN.md](./MOBILE_DESIGN.md) *(geplant)* - UI/UX Mockups
- [SYNC_ARCHITECTURE.md](./SYNC_ARCHITECTURE.md) *(geplant)* - Offline-Sync Details