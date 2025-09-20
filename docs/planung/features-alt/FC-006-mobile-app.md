# FC-006: Mobile Companion App mit Spracheingabe

**Feature Code:** FC-006  
**Status:** 📋 Planungsphase  
**Geschätzter Aufwand:** 15-20 Tage  
**Priorität:** HOCH - Game-Changer für Außendienst  
**Phase:** 5.1 (Mobile First)  

## 🎯 Zusammenfassung

"Ich komme gerade vom Kunden und will schnell was festhalten" - DER Klassiker! Die FreshPlan Mobile Companion App fokussiert auf schnelle Datenerfassung unterwegs mit revolutionärer Spracheingabe.

## 📱 Kernkonzept

```
Reduzierte App mit Fokus auf:
- Schnelle Sprachnotizen mit KI-Erkennung
- Foto von Visitenkarte → Automatische Kontaktanlage  
- Offline-Fähigkeit mit intelligenter Sync
- Push für wichtige Ereignisse (Zahlungseingang!)
```

## 🏗️ Technische Architektur

### 1. App-Architektur (React Native)

```typescript
// App-Struktur
src/
├── core/
│   ├── api/           # API Client mit Offline-Queue
│   ├── auth/          # Keycloak Integration
│   ├── storage/       # AsyncStorage + SQLite
│   └── sync/          # Sync Engine
├── features/
│   ├── voice/         # Spracheingabe
│   ├── capture/       # Foto & OCR
│   ├── customers/     # Kunden-Ansicht
│   └── activities/    # Schnellerfassung
└── shared/
    ├── components/    # UI-Komponenten
    └── utils/         # Helpers
```

### 2. Sprach-Engine

```typescript
// VoiceCapture Service
export class VoiceService {
  private whisperApi: WhisperAPI;
  private nlpProcessor: NLPProcessor;
  
  async processVoiceNote(audioBlob: Blob): Promise<ProcessedNote> {
    // 1. Speech-to-Text (Whisper API)
    const transcript = await this.whisperApi.transcribe(audioBlob);
    
    // 2. NLP für Entitäten-Erkennung
    const entities = await this.nlpProcessor.extract(transcript);
    
    // 3. Smart-Parsing
    return {
      text: transcript,
      customer: entities.customer,
      quantities: entities.quantities,    // "500 Stück"
      dates: entities.dates,              // "Lieferung April"
      products: entities.products,        // Produktnamen
      nextActions: entities.actions       // "Angebot senden"
    };
  }
}
```

### 3. Offline-First Architektur

```typescript
// Sync-Manager
export class SyncManager {
  private queue: OfflineQueue;
  private conflictResolver: ConflictResolver;
  
  async syncData() {
    // 1. Lokale Änderungen sammeln
    const pendingChanges = await this.queue.getPending();
    
    // 2. Batch-Upload wenn online
    if (isOnline()) {
      const results = await this.uploadBatch(pendingChanges);
      
      // 3. Konflikte auflösen
      const conflicts = results.filter(r => r.hasConflict);
      await this.conflictResolver.resolve(conflicts);
    }
    
    // 4. Neue Daten pullen
    await this.pullLatestData();
  }
}
```

### 4. Context-Awareness

```typescript
// Kontext-Service nutzt GPS & Kalender
export class ContextService {
  async getCurrentContext(): Promise<Context> {
    const location = await this.getLocation();
    const calendar = await this.getCalendarEvents();
    
    // Intelligente Zuordnung
    const nearbyCustomers = await this.findCustomersNearby(location);
    const currentAppointment = this.getCurrentAppointment(calendar);
    
    return {
      probableCustomer: currentAppointment?.customer || nearbyCustomers[0],
      location: location,
      time: new Date()
    };
  }
}
```

### 5. OCR für Visitenkarten

```typescript
// Business Card Scanner
export class BusinessCardScanner {
  private textractApi: TextractAPI;
  private customerMatcher: CustomerMatcher;
  
  async scanCard(imageUri: string): Promise<Contact> {
    // 1. Text-Extraktion
    const extractedText = await this.textractApi.analyze(imageUri);
    
    // 2. Strukturierung
    const contact = this.parseContactInfo(extractedText);
    
    // 3. Kunden-Zuordnung vorschlagen
    const suggestedCustomer = await this.customerMatcher.match(contact);
    
    return {
      ...contact,
      suggestedCustomerId: suggestedCustomer?.id
    };
  }
}
```

## 🔗 Abhängigkeiten

### Backend-APIs benötigt:
- **GraphQL API** für effiziente Mobile-Queries
- **Sync API** für Offline-Support
- **Push Service** für Notifications
- **File Upload** für Bilder/Audio

### Zu anderen Features:
- **FC-004 (Verkäuferschutz)**: Mobile Ansicht des Schutz-Status
- **FC-005 (Xentral)**: Push bei Provisions-relevanten Events
- **FC-007 (Dashboard)**: Mobile KPI-Ansicht

### Externe Services:
- **OpenAI Whisper**: Sprach-zu-Text
- **AWS Textract**: OCR für Visitenkarten
- **Firebase**: Push Notifications
- **Mapbox**: Standort-Services

## 🏛️ Architektur-Entscheidungen

### ADR-006-001: React Native
**Entscheidung:** React Native statt nativer Apps
- Code-Sharing mit Web-Frontend
- Schnellere Entwicklung
- Ausreichende Performance

### ADR-006-002: Offline-First
**Entscheidung:** SQLite für lokale Datenhaltung
- Vollständige Offline-Funktionalität
- Konflikt-Resolution via CRDT
- Background-Sync

### ADR-006-003: Reduzierter Funktionsumfang
**Entscheidung:** Fokus auf Datenerfassung
- Keine komplexen Workflows
- Optimiert für One-Handed Use
- Speed over Features

## 🚀 Implementierungsphasen

### Phase 1: Basis-App (5 Tage)
1. React Native Setup
2. Keycloak Auth Integration
3. Basis-Navigation
4. Offline Storage Setup

### Phase 2: Core Features (5 Tage)
1. Customer List (Cached)
2. Quick Activity Creation
3. Basic Sync Engine
4. Push Notifications

### Phase 3: Spracheingabe (5 Tage)
1. Audio Recording UI
2. Whisper API Integration
3. Entity Extraction
4. Smart Suggestions

### Phase 4: Advanced Features (3 Tage)
1. Visitenkarten-Scanner
2. GPS-Context
3. Kalender-Integration
4. Offline-Konflikt-Handling

### Phase 5: Polish & Performance (2 Tage)
1. Performance-Optimierung
2. Offline-Indikatoren
3. Sync-Status UI
4. Error Handling

## 🧪 Test-Strategie

```typescript
describe('VoiceCapture', () => {
  it('should extract entities from speech', async () => {
    // Given
    const audio = loadTestAudio('kunde-will-500-stueck.wav');
    
    // When  
    const result = await voiceService.processVoiceNote(audio);
    
    // Then
    expect(result.quantities).toEqual([{ amount: 500, unit: 'Stück' }]);
    expect(result.customer).toBeTruthy();
  });
  
  it('should work offline and sync later', async () => {
    // Given
    setOfflineMode(true);
    
    // When
    await createActivity({ note: 'Test' });
    setOfflineMode(false);
    await syncManager.sync();
    
    // Then
    expect(await getServerActivities()).toContainEqual(
      expect.objectContaining({ note: 'Test' })
    );
  });
});
```

## 📊 Erfolgsmetriken

- Adoption-Rate: >80% der Außendienst-Mitarbeiter
- Zeitersparnis: 15 Min/Tag durch Spracheingabe
- Datenqualität: 95% korrekte Entity-Erkennung
- Offline-Nutzung: 30% der Interaktionen

## 🎨 UI/UX Prinzipien

### Design-Guidelines:
- **Thumb-Friendly**: Alle Actions im Daumen-Bereich
- **High Contrast**: Auch bei Sonnenlicht lesbar
- **Large Touch Targets**: Minimum 44x44 pt
- **Gesture-Based**: Swipe für häufige Actions

### Kern-Screens:
1. **Home**: Große Action-Buttons (Voice, Photo, Note)
2. **Customer**: Kompakte Übersicht mit Quick Actions
3. **Capture**: Full-Screen für Sprache/Foto
4. **Sync**: Clear Status + Manual Trigger

## 🔍 Offene Fragen

1. **App Stores**: Deployment-Strategie (Public/Enterprise)?
2. **Offline-Maps**: Lizenzkosten für Offline-Karten?
3. **Audio-Speicherung**: Wie lange Sprachnotizen aufbewahren?
4. **Geräteanforderungen**: Minimum iOS/Android Version?

## 💡 Zukunfts-Features

- AR für Lager-Visualisierung
- Barcode-Scanner für Produkte
- Unterschriften-Erfassung
- Video-Notizen