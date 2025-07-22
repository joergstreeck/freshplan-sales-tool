# 🎛️ M3 SALES COCKPIT - ENHANCEMENT PLAN

**Erstellt:** 20.07.2025  
**Status:** 🟢 READY - Alle Entscheidungen getroffen  
**Feature-Typ:** 🎨 FRONTEND  
**Priorität:** ⭐ KERN-FEATURE Enhancement

---

## 🎯 ENHANCEMENT SCOPE

### Basis (60% fertig):
- ✅ 3-Spalten Layout implementiert
- ✅ ResizablePanels funktionieren
- ✅ Freshfoodz CI integriert
- ✅ Live URL: http://localhost:5173/cockpit

### Neue Features (zu implementieren):
1. **KI-Priorisierung für "Mein Tag"**
2. **Polling-basierte Updates (30s)**
3. **Integration mit echten Daten**

---

## 🤖 D1: OPENAI INTEGRATION

### Entscheidung: OpenAI API ✅
```typescript
// frontend/src/services/ai/openai.service.ts
interface OpenAIConfig {
  apiKey: string; // Aus Environment
  model: 'gpt-3.5-turbo' | 'gpt-4';
  maxTokens: 150;
}

// Priorisierungs-Prompt
const PRIORITY_PROMPT = `
Analysiere folgende Verkaufschancen und priorisiere 
die Top 5 für heute basierend auf:
- Wahrscheinlichkeit des Abschlusses
- Umsatzpotential
- Letzte Aktivität
- Deadline-Nähe
`;
```

### Datenschutz-Konzept:
```typescript
// Keine Kundennamen an OpenAI!
interface AnonymizedOpportunity {
  id: string;
  stage: string;
  value: number;
  probability: number;
  daysSinceLastActivity: number;
  daysUntilDeadline: number;
  // KEIN: customerName, contactPerson
}
```

---

## ⚡ D2: POLLING IMPLEMENTATION

### Entscheidung: Polling mit React Query ✅
```typescript
// frontend/src/hooks/useSalesCockpit.ts
const POLLING_INTERVAL = 30000; // 30 Sekunden

export const useSalesCockpit = () => {
  // Mein Tag - mit KI-Priorisierung
  const myDayQuery = useQuery({
    queryKey: ['sales-cockpit', 'my-day'],
    queryFn: fetchMyDayWithAI,
    staleTime: POLLING_INTERVAL,
    refetchInterval: POLLING_INTERVAL,
    refetchIntervalInBackground: true
  });

  // Focus Liste - normale Polling
  const focusListQuery = useQuery({
    queryKey: ['sales-cockpit', 'focus-list'],
    queryFn: fetchFocusList,
    staleTime: POLLING_INTERVAL,
    refetchInterval: POLLING_INTERVAL
  });

  // Team Updates - kritisch für Collaboration
  const teamUpdatesQuery = useQuery({
    queryKey: ['team-updates'],
    queryFn: fetchTeamUpdates,
    staleTime: 15000, // Häufiger für Team-Awareness
    refetchInterval: 15000
  });

  return {
    myDay: myDayQuery.data,
    focusList: focusListQuery.data,
    teamUpdates: teamUpdatesQuery.data,
    isLoading: myDayQuery.isLoading || focusListQuery.isLoading
  };
};
```

### Migration-Pfad zu WebSocket (V2):
```typescript
// Vorbereitung für spätere Migration
interface RealtimeConfig {
  strategy: 'polling' | 'websocket';
  pollInterval?: number;
  wsEndpoint?: string;
}

// Abstraktion ermöglicht späteren Wechsel
const useRealtimeData = (config: RealtimeConfig) => {
  if (config.strategy === 'websocket') {
    return useWebSocketData(config);
  }
  return usePollingData(config);
};
```

---

## 🏗️ IMPLEMENTATION ROADMAP

### Phase 1: OpenAI Service (2 Tage)
- [ ] OpenAI Service implementieren
- [ ] Anonymisierungs-Layer bauen
- [ ] API Key Management (Environment)
- [ ] Error Handling & Fallbacks

### Phase 2: Polling Infrastructure (1 Tag)
- [ ] React Query Setup erweitern
- [ ] Polling Hooks implementieren
- [ ] Loading States optimieren
- [ ] Background Refetch testen

### Phase 3: MyDay Column Enhancement (2 Tage)
- [ ] KI-Priorisierung integrieren
- [ ] UI für priorisierte Items
- [ ] Begründungen anzeigen
- [ ] Manual Override Option

### Phase 4: Integration & Testing (1 Tag)
- [ ] Mit echten Backend-Daten verbinden
- [ ] Performance optimieren
- [ ] Error States testen
- [ ] Team-Update Notifications

---

## 📊 SUCCESS METRICS

- **KI-Qualität:** Verkäufer bestätigen 80%+ der Vorschläge
- **Performance:** Updates innerhalb 30s sichtbar
- **Kosten:** OpenAI < 200€/Monat
- **User Satisfaction:** Produktivitätssteigerung messbar

---

## 🔗 NÄCHSTE SCHRITTE

1. **Backend API erweitern** für Cockpit-Daten
2. **OpenAI Account** einrichten
3. **Environment Variables** konfigurieren
4. **Implementation** nach Roadmap

**Startbereit:** Alle Entscheidungen getroffen ✅