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