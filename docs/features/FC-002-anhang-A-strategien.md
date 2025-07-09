# FC-002 - Anhang A: Technische Strategien & Offene Fragen

**Feature:** FC-002  
**Typ:** Strategische Entscheidungen  
**Status:** ✅ Entscheidungen getroffen (09.07.2025)
**Aktualisiert:** 09.07.2025 durch Claude nach Besprechung mit Jörg

## A.1 Migrations-Strategie

**Status:** ❌ **VERWORFEN / NICHT BENÖTIGT**

**Begründung:** Entfällt, da sich das System noch in der Entwicklung befindet und keine Altdaten/Nutzer migriert werden müssen.

~~**Problem**: Wie migrieren wir bestehende Nutzer von der alten zur neuen Oberfläche?~~

~~### Notwendige Entscheidungen:~~

~~#### 1. Route-Redirects~~
~~- Alte Routes müssen auf neue Struktur gemappt werden~~
~~- Beispiel: `/calculator` → `/cockpit?view=calculator`~~
~~- Implementierung via React Router Redirect-Komponenten~~

```typescript
// VERWORFEN - Kein Code benötigt
// Da keine Migration erforderlich ist, entfällt die gesamte
// Legacy-Route-Mapping-Implementierung
```

---

## A.2 Feature-Flag-Strategie

**Status:** ❌ **VERWORFEN / NICHT BENÖTIGT**

**Begründung:** Entfällt, da keine schrittweise Einführung für bestehende Nutzer erforderlich ist. Neue UI-Module werden nach Fertigstellung für alle Entwickler/Tester sofort sichtbar.

~~### Optionen zur Evaluierung:~~

```typescript
// VERWORFEN - Keine Feature Flags benötigt
// Alle neuen UI-Module werden direkt nach Fertigstellung
// in den main Branch integriert und sind sofort verfügbar
```

---

## A.3 Performance-Optimierung

**Status:** ✅ **ENTSCHIEDEN & FREIGEGEBEN**

**Entscheidung:** Der aggregierte Backend-Endpunkt wird von Anfang an implementiert, um die Performance zu gewährleisten und spätere Umbauten zu vermeiden.

### Lösung: Aggregierter Backend-Endpunkt

```typescript
// Neuer aggregierter Endpunkt - WIRD IMPLEMENTIERT
GET /api/cockpit/overview?date=2025-07-08

Response:
{
  "alerts": [...],
  "appointments": [...],
  "tasks": [...],
  "unassignedEmails": [...],
  "stats": {
    "totalCustomers": 150,
    "activeOpportunities": 23,
    "todayRevenue": 12500
  }
}
```

**Vorteile:**
- Ein Request statt 4-5
- Backend kann Daten optimiert laden
- Reduzierte Latenz
- Einfacheres Error Handling

**Implementation:**
```typescript
// Frontend
export const useCockpitOverview = (date: string) => {
  return useQuery({
    queryKey: ['cockpit-overview', date],
    queryFn: () => cockpitService.getOverview(date),
    staleTime: 60 * 1000, // 1 Minute
  });
};
```

### 📋 Implementierungsplan:
1. **Sofort**: Aggregierten Endpunkt `/api/cockpit/overview` implementieren
2. **Optional später**: GraphQL evaluieren für flexible Queries
3. **Bei Bedarf**: Caching-Strategy mit Redis

---

## A.4 Echtzeit-Anforderungen

**Status:** ⏸️ **ZURÜCKGESTELLT**

**Entscheidung:** Echte Echtzeit-Funktionen (WebSockets etc.) werden auf eine spätere Phase verschoben. Für den Start ist eine einfache Lösung mit manuellem Refresh oder automatischem Polling (Aktualisierung alle paar Minuten) ausreichend.

### Implementierung für den Start:

```typescript
// Einfaches Auto-Refresh mit React Query
export const useCockpitOverview = (date: string) => {
  return useQuery({
    queryKey: ['cockpit-overview', date],
    queryFn: () => cockpitService.getOverview(date),
    staleTime: 60 * 1000, // 1 Minute
    refetchInterval: 5 * 60 * 1000, // Auto-Refresh alle 5 Minuten
    refetchIntervalInBackground: false // Nur wenn Tab aktiv
  });
};
```

### Manueller Refresh:
- Refresh-Button in der UI
- Keyboard Shortcut: `Ctrl/Cmd + R`
- Pull-to-Refresh auf Mobile (später)

### Zukünftige Evaluierung (Phase 5+):
- WebSockets für echte Collaboration Features
- Server-Sent Events für Notifications
- Evaluierung basierend auf Nutzer-Feedback

---

## 📋 Entscheidungsmatrix

| Thema | Priorität | Empfehlung | Entscheidung |
|-------|-----------|------------|--------------|
| Migration | ~~Hoch~~ | ~~Soft Migration + Auto-Convert~~ | ❌ VERWORFEN |
| Feature Flags | ~~Hoch~~ | ~~User-basiert + Opt-out~~ | ❌ VERWORFEN |
| Performance | Hoch | Aggregierter Endpunkt | ✅ FREIGEGEBEN |
| Echtzeit | Niedrig | Erstmal Polling | ⏸️ ZURÜCKGESTELLT |

---

**Status-Update vom 09.07.2025:**
- Alle strategischen Entscheidungen wurden mit Jörg besprochen
- Migration und Feature Flags entfallen, da kein Live-Betrieb
- Performance-Endpunkt wird direkt implementiert
- Echtzeit-Features kommen später

**Nächste Schritte:**
1. ✅ Entscheidungen dokumentiert
2. FC-002 Implementierung kann beginnen
3. Fokus auf aggregierten Cockpit-Endpunkt