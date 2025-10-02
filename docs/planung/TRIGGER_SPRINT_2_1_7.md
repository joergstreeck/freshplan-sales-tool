---
sprint_id: "2.1.7"
title: "Lead Scoring & Mobile Optimization"
doc_type: "konzept"
status: "planned"
owner: "team/leads-backend"
date_start: "2025-10-19"
date_end: "2025-10-25"
modules: ["02_neukundengewinnung"]
entry_points:
  - "features-neu/02_neukundengewinnung/_index.md"
  - "features-neu/02_neukundengewinnung/backend/_index.md"
  - "features-neu/02_neukundengewinnung/SPRINT_MAP.md"
  - "features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_7/SUMMARY.md"
pr_refs: []
updated: "2025-10-02"
---

# Sprint 2.1.7 – Lead Scoring & Mobile Optimization

**📍 Navigation:** Home → Planung → Sprint 2.1.7

> **⚠️ TEST-STRATEGIE BEACHTEN!**
> Tests MÜSSEN Mocks verwenden, NICHT @QuarkusTest mit echter DB!
> Siehe: [`backend/TEST_MIGRATION_PLAN.md`](features-neu/02_neukundengewinnung/backend/TEST_MIGRATION_PLAN.md)
>
> **🎯 Arbeitsanweisung – Reihenfolge**
> 1. **SPRINT_MAP des Moduls öffnen** → `features-neu/02_neukundengewinnung/SPRINT_MAP.md`
> 2. **Backend:** Lead-Scoring-Algorithmus implementieren
> 3. **Backend:** Activity-Templates System
> 4. **Frontend:** Mobile-First UI-Optimierung
> 5. **Frontend:** Offline-Fähigkeit (Service Worker + IndexedDB)
> 6. **Frontend:** QR-Code-Scanner für schnelle Kontakterfassung

## Sprint-Ziel

Erhöhung der Vertriebseffizienz durch intelligentes Lead-Scoring, standardisierte Activity-Templates und mobile-optimierte UI mit Offline-Fähigkeit für Trade-Messe-Szenarien.

## User Stories

### 1. Lead-Scoring Algorithmus
**Akzeptanzkriterien:**
- Lead-Score Berechnung (0-100 Punkte)
- Faktoren:
  - **Stage** (0=20, 1=50, 2=80 Punkte)
  - **Estimated Volume** (>5000€ = +10, >10000€ = +15)
  - **Business Type** (Restaurant = +5, Hotel = +10, Catering = +8)
  - **Activity Frequency** (Call/Week = +2, Meeting/Month = +5)
  - **Response Time** (< 24h Antwort = +5)
  - **Territory** (Prioritäts-Territorien = +3)
- Backend: `lead.score INT` Feld (V259 Migration)
- Backend: LeadScoringService mit konfigurierbaren Gewichtungen
- Scheduled Job: Score-Recalc täglich (1 AM)
- API: GET /api/leads?sort=score,desc (Default-Sorting nach Score)

### 2. Activity-Templates System
**Akzeptanzkriterien:**
- Backend: `activity_templates` Tabelle (V260 Migration)
- CRUD-API: /api/activity-templates
- Standard-Templates (Seeds):
  - "Erstkontakt Küchenchef" (summary vorgefüllt, countsAsProgress=true)
  - "Sample-Box Versand" (outcome="pending", nextAction="Follow-up in 7 Tagen")
  - "ROI-Präsentation" (countsAsProgress=true, type=ROI_PRESENTATION)
  - "Follow-up nach Demo" (nextAction vorgefüllt, nextActionDate=+3 Tage)
- Frontend: ActivityTimeline → "Template verwenden" Dropdown
- User-spezifische Templates erstellen/editieren/löschen
- Template-Nutzung in Activity-Audit protokollieren

### 3. Mobile-First UI Optimierung
**Akzeptanzkriterien:**
- LeadWizard:
  - Touch-optimiert (Button-Größen ≥ 44px)
  - Mobile-Breakpoints (<768px): Single-Column Layout
  - Stepper kompakt (Icons only, Text on Tap)
  - Auto-Save bei Netzwerk-Unterbrechung (localStorage)
- LeadList:
  - Card-View auf Mobile (statt Table)
  - Infinite Scroll (statt Pagination)
  - Swipe-Aktionen (Swipe-Left: Details, Swipe-Right: Activity)
- Performance:
  - Bundle <200KB (Code-Splitting)
  - First Contentful Paint <1.5s (3G Network)
  - Time to Interactive <3.5s (3G Network)

### 4. Offline-Fähigkeit (Progressive Web App)
**Akzeptanzkriterien:**
- Service Worker für Offline-Support
- IndexedDB für lokale Lead-Speicherung
- Sync-Strategy:
  - Online: Direkt POST /api/leads
  - Offline: Speichern in IndexedDB
  - Bei Reconnect: Background-Sync (max. 10 Leads/Batch)
- UI-Indikator: "Offline-Modus aktiv" Badge
- Conflict-Resolution: Server-Wins bei gleichzeitigen Edits
- Installation-Prompt: "Zur Startseite hinzufügen"

### 5. QR-Code-Scanner Integration
**Akzeptanzkriterien:**
- Frontend: QR-Code-Scanner Component (Camera-API)
- Unterstützte Formate:
  - vCard (Kontakt-Daten)
  - meCard (Japanese Contact Format)
  - Plain Text (Email/Phone)
- Automatisches Parsing in LeadWizard Felder
- Fallback: Manuelle Eingabe bei Scan-Fehler
- Permission-Handling: Camera-Zugriff anfordern
- Desktop-Fallback: File-Upload (QR-Code Bild)

### 6. Lead-Scoring UI & Filter
**Akzeptanzkriterien:**
- LeadList:
  - Score-Spalte mit Color-Coding:
    - 0-30: Rot (Low Priority)
    - 31-60: Gelb (Medium Priority)
    - 61-100: Grün (High Priority)
  - Default-Sorting: score DESC
  - Filter: "Nur Leads > 60 Punkte"
  - Filter: "Nur Leads < 30 Punkte (Cleanup-Kandidaten)"
- LeadDetail:
  - Score-Breakdown Accordion:
    - Stage: +50
    - Volume: +15
    - Business Type: +10
    - Activity Frequency: +10
    - GESAMT: 85 Punkte
  - "Score neu berechnen" Button (Admin/Manager)

## Technische Details

### Lead-Scoring Algorithmus (Backend):
```java
@ApplicationScoped
public class LeadScoringService {

  public int calculateScore(Lead lead) {
    int score = 0;

    // Stage Score (0-80)
    score += switch (lead.stage) {
      case 0 -> 20;
      case 1 -> 50;
      case 2 -> 80;
      default -> 0;
    };

    // Volume Score (0-15)
    if (lead.estimatedVolume != null) {
      if (lead.estimatedVolume.compareTo(BigDecimal.valueOf(10000)) > 0) {
        score += 15;
      } else if (lead.estimatedVolume.compareTo(BigDecimal.valueOf(5000)) > 0) {
        score += 10;
      }
    }

    // Business Type Score (0-10)
    score += switch (lead.businessType) {
      case "hotel" -> 10;
      case "catering" -> 8;
      case "restaurant" -> 5;
      default -> 0;
    };

    // Activity Frequency Score (0-10)
    long activitiesLast30Days = countActivities(lead.id, 30);
    score += Math.min((int) activitiesLast30Days * 2, 10);

    return Math.min(score, 100);  // Cap at 100
  }
}
```

### Activity-Templates Schema:
```sql
-- V260: activity_templates Tabelle
CREATE TABLE activity_templates (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(50) NOT NULL,  -- NULL = System-Template
  name VARCHAR(200) NOT NULL,
  activity_type VARCHAR(30) NOT NULL,
  summary_template VARCHAR(500),
  counts_as_progress BOOLEAN DEFAULT false,
  default_outcome VARCHAR(50),
  next_action_template VARCHAR(200),
  next_action_offset_days INT,  -- +3 Tage relativ
  is_system_template BOOLEAN DEFAULT false,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_activity_templates_user ON activity_templates(user_id);
```

### Offline-Sync (Frontend):
```typescript
// Service Worker: Background Sync
self.addEventListener('sync', async (event) => {
  if (event.tag === 'sync-leads') {
    event.waitUntil(syncOfflineLeads());
  }
});

async function syncOfflineLeads() {
  const db = await openDB('freshplan-offline', 1);
  const offlineLeads = await db.getAll('leads');

  for (const lead of offlineLeads) {
    try {
      await fetch('/api/leads', {
        method: 'POST',
        body: JSON.stringify(lead),
        headers: { 'Content-Type': 'application/json' }
      });

      // Success → Remove from IndexedDB
      await db.delete('leads', lead.tempId);
    } catch (error) {
      console.error('Sync failed:', error);
      // Retry later (max 3 attempts)
    }
  }
}
```

### QR-Code Scanner:
```tsx
// QRCodeScanner.tsx
import { BrowserQRCodeReader } from '@zxing/library';

export function QRCodeScanner({ onScan }: { onScan: (data: vCard) => void }) {
  const codeReader = new BrowserQRCodeReader();

  const startScan = async () => {
    const videoInputDevices = await codeReader.listVideoInputDevices();
    const selectedDeviceId = videoInputDevices[0].deviceId;

    codeReader.decodeFromVideoDevice(selectedDeviceId, 'video', (result, error) => {
      if (result) {
        const vcard = parseVCard(result.getText());
        onScan(vcard);
      }
    });
  };

  return (
    <Box>
      <video id="video" width="300" height="200" />
      <Button onClick={startScan}>Scan starten</Button>
    </Box>
  );
}
```

## Frontend Components (NEU):
- `QRCodeScanner.tsx` - QR-Code Scan für schnelle Kontakterfassung
- `ActivityTemplateSelector.tsx` - Template-Dropdown in ActivityTimeline
- `LeadScoreBreakdown.tsx` - Score-Detail-Ansicht mit Faktoren
- `OfflineIndicator.tsx` - "Offline-Modus" Badge in Header
- `LeadCard.tsx` - Mobile Card-View für LeadList

## Definition of Done (Sprint)

- [ ] **Lead-Scoring-Algorithmus implementiert & getestet**
- [ ] **Activity-Templates CRUD funktioniert**
- [ ] **Mobile UI-Optimierung (Touch, Breakpoints, Performance)**
- [ ] **Offline-Fähigkeit (Service Worker + IndexedDB + Sync)**
- [ ] **QR-Code-Scanner funktioniert (Camera + vCard)**
- [ ] **Lead-Scoring UI mit Filter & Breakdown**
- [ ] **Lighthouse Score: Performance >90, PWA >90**
- [ ] **Offline-Test: 10 Leads erfassen → Sync erfolgreich**

## Risiken & Mitigation

- **Scoring-Genauigkeit**: Iteratives Tuning mit Sales-Team Feedback
- **Offline-Konflikte**: Server-Wins Strategie + User-Benachrichtigung
- **Camera-Permissions**: Klarer Onboarding-Flow + Fallback File-Upload
- **Performance Mobile**: Code-Splitting + Lazy-Loading Components
- **IndexedDB Storage-Limits**: Max. 50 Offline-Leads, ältere löschen

## Abhängigkeiten

- Sprint 2.1.5 (Progressive Profiling UI) muss abgeschlossen sein
- Modul 08 (Administration) für Template-Management (optional)
- PWA Manifest + Service Worker Setup

## Monitoring & KPIs

- **Lead-Score Distribution**: Histogram (Low/Medium/High)
- **Template-Nutzung**: Top 5 meistgenutzte Templates
- **Offline-Sync Success-Rate**: >95% erfolgreiche Syncs
- **Mobile Performance**: P95 Load-Time <3.5s (3G)
- **QR-Code Scan Success-Rate**: >90%

## Next Sprint Preview

Sprint 2.2: Kundenmanagement - Field-based Customer Architecture mit Contact-Hierarchie

