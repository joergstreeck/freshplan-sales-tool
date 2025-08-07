# Sprint 2 Vollständige Struktur-Überprüfung

**Datum:** 2025-07-30  
**Prüfer:** Claude  
**Scope:** Gesamte Sprint 2 Struktur nach neuer Step 2 Planung

## 1. ✅ Vollständigkeit

### Frontend-Struktur
- ✅ Wizard mit 3 Steps vollständig implementiert
- ✅ DynamicFieldRenderer mit AdaptiveFormContainer
- ✅ FieldCatalogExtensions.json mit allen Feldern
- ✅ TypeScript Types korrekt mit `import type`
- ⚠️ **FEHLT:** Globales Feld "expectedAnnualRevenue" (Umsatzerwartung)
- ⚠️ **FEHLT:** LocationSelector für Standortverwaltung
- ⚠️ **FEHLT:** "Für alle Filialen übernehmen" Option

### Backend-Struktur
- ✅ Customer Entity hat Chain-Felder (totalLocationsEU, etc.)
- ✅ expectedAnnualVolume bereits vorhanden (BigDecimal)
- ✅ Pain Points als JSON Array
- ✅ CustomerLocation Entity für Standorte
- ⚠️ **FEHLT:** Angebotsdaten pro Location (Services wie Frühstück, Roomservice)

## 2. ❌ Inkonsistenzen

### Reihenfolge-Problem
- **IST:** Step 2 zeigt erst Angebotsstruktur, dann Pain Points
- **SOLL:** Pain Points global → Umsatzerwartung → Zusatzgeschäft → Angebotsstruktur pro Filiale
- **AUSWIRKUNG:** Unlogischer Flow für Vertrieb

### Datenmodell-Inkonsistenz
- **Frontend:** Erfasst Hotel-Services global am Customer
- **Backend:** CustomerLocation hat keine Service-Felder
- **LÖSUNG NÖTIG:** Services müssen an Location-Entity

## 3. 🔄 Redundanzen

### Field Definitions
- painPoints werden mehrfach definiert (fieldCatalogExtensions + PAIN_POINT_SOLUTIONS)
- Hotel-Services teilweise doppelt (offersBreakfast vs. breakfastOffered)

### Layout-Komponenten
- FilialstrukturLayout und AngebotsstrukturLayout sehr ähnlich
- Könnte vereinheitlicht werden zu einem FlexibleFieldLayout

## 4. 📊 Logik & Best Practice

### ✅ Gut umgesetzt:
- Verkaufsfokussierte Struktur
- Pain Point → Solution Mapping
- Adaptive Theme System für Feldgrößen
- TypeScript import type korrekt

### ❌ Verbesserungsbedarf:
- **Globale vs. Lokale Daten** nicht sauber getrennt
- **Standort-Auswahl** fehlt in Step 2
- **Branchen-Logik** nur für Hotels implementiert

## 5. 🎯 Übereinstimmung mit Diskussion

### Erfüllt:
- ✅ Pain Points als Verkaufschancen
- ✅ Adaptive Feldgrößen
- ✅ Strukturierte Ansprechpartner (Step 3)

### Nicht erfüllt:
- ❌ Reihenfolge (Pain Points zuerst)
- ❌ Umsatzerwartung prominent platziert
- ❌ Standort-basierte Angebotserfassung

## 6. 💡 Zusätzliche Empfehlungen

### UI/UX Verbesserungen:
1. **Progress Indicator** mit Standort-Anzeige
   ```
   Step 2: Herausforderungen & Angebote
   [Global] → [Standort 1/3: Berlin] → [Standort 2/3: München]
   ```

2. **Smart Defaults** für Ketten
   - "Typische Filiale" als Template
   - Abweichungen nur bei Bedarf

3. **Inline-Potenzialberechnung**
   - Pro Pain Point zeigen: "+15% Potenzial"
   - Umsatzerwartung automatisch vorschlagen

### Technische Verbesserungen:
1. **State Management** für Multi-Location
   ```typescript
   interface CustomerWizardState {
     globalData: GlobalCustomerData;
     locations: Map<string, LocationData>;
     currentLocationId?: string;
   }
   ```

2. **Validierung** über Standorte hinweg
   - Summe Standorte = Einzelstandorte
   - Mindestens 1 Hauptstandort

## 7. 🏭 Andere Branchen

### Aktueller Stand:
- Nur Hotel-Branche vollständig
- Krankenhäuser/Betriebsrestaurants nur Platzhalter

### Empfehlung für andere Branchen:

**Krankenhäuser:**
```typescript
const hospitalFieldGroups = [
  {
    title: 'Verpflegungssystem',
    fields: ['mealSystem', 'mealsPerDay', 'bedCount']
  },
  {
    title: 'Spezialanforderungen',  
    fields: ['dietTypes', 'textureModified', 'allergenFree']
  }
];
```

**Betriebsrestaurants:**
```typescript
const canteenFieldGroups = [
  {
    title: 'Betriebszeiten',
    fields: ['operatingDays', 'shiftCount', 'peakTimes']
  },
  {
    title: 'Angebotsstruktur',
    fields: ['subsidized', 'guestAccess', 'takeAway']
  }
];
```

## 8. 🔧 Backend-Änderungen erforderlich

### Neue Entities/Felder:

1. **CustomerLocation erweitern:**
```java
// Angebotsdaten pro Standort
@Column(name = "offers_breakfast")
private Boolean offersBreakfast;

@Column(name = "breakfast_guests_per_day")
private Integer breakfastGuestsPerDay;

@Column(name = "offers_room_service")
private Boolean offersRoomService;

// etc. für alle standortspezifischen Services
```

2. **Neue Entity: LocationServices**
```java
@Entity
@Table(name = "customer_location_services")
public class LocationServices {
  @ManyToOne
  private CustomerLocation location;
  
  @Enumerated(EnumType.STRING)
  private ServiceType serviceType;
  
  @Column(name = "is_offered")
  private Boolean isOffered;
  
  @Column(name = "capacity")
  private Integer capacity;
  
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Object> details;
}
```

3. **API Endpoints anpassen:**
```
POST /api/customers/{id}/locations/{locationId}/services
GET /api/customers/{id}/locations/{locationId}/services
PUT /api/customers/{id}/global-data (Pain Points, Umsatz)
```

## 🎬 Fazit & Nächste Schritte

### Priorität 1 (Sofort):
1. ✅ Step 2 Reihenfolge umbauen
2. ✅ expectedAnnualRevenue Feld hinzufügen
3. ✅ LocationSelector Component erstellen

### Priorität 2 (Diese Woche):
1. Backend für standortbasierte Services erweitern
2. Andere Branchen implementieren
3. "Für alle übernehmen" Feature

### Priorität 3 (Später):
1. Layout-Komponenten vereinheitlichen
2. Performance-Optimierung für viele Standorte
3. Erweiterte Validierung

**Die neue Struktur ist Best Practice und macht fachlich absolut Sinn! Die Umsetzung erfordert moderate Anpassungen, hauptsächlich im UI-Flow und Backend-Datenmodell.**