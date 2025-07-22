# ✅ UI FOUNDATION IMPLEMENTATION CHECKLIST

**Erstellt:** 20.07.2025  
**Zweck:** Schritt-für-Schritt Anleitung nach Entscheidungen  
**Status:** Ready to Execute

---

## 🏁 PRE-FLIGHT CHECKS

- [x] D1: OpenAI API entschieden ✅
- [x] D2: Polling-Strategie entschieden ✅  
- [x] D3: Sidebar bereits implementiert ✅
- [ ] OpenAI API Key beschaffen
- [ ] Environment Variables vorbereiten
- [ ] Backend Endpoints identifizieren

---

## 📋 M3 SALES COCKPIT ENHANCEMENT

### Phase 1: OpenAI Setup (Tag 1)
- [ ] OpenAI Account erstellen
- [ ] API Key generieren
- [ ] `.env` File erweitern:
  ```env
  VITE_OPENAI_API_KEY=sk-...
  VITE_OPENAI_MODEL=gpt-3.5-turbo
  VITE_OPENAI_MAX_TOKENS=150
  ```
- [ ] `openai.service.ts` erstellen
- [ ] Anonymisierungs-Helper implementieren

### Phase 2: Polling Setup (Tag 2)
- [ ] React Query Config erweitern
- [ ] `useSalesCockpit` Hook erstellen
- [ ] Polling-Intervalle definieren:
  - MyDay: 30s
  - FocusList: 30s  
  - TeamUpdates: 15s
- [ ] Background Refetch aktivieren
- [ ] Loading States optimieren

### Phase 3: MyDay KI-Integration (Tag 3-4)
- [ ] `fetchMyDayWithAI` implementieren
- [ ] Priorisierungs-Prompt erstellen
- [ ] Response-Parser bauen
- [ ] Fallback für API-Ausfall
- [ ] UI für KI-Vorschläge erweitern
- [ ] "Warum?"-Erklärungen anzeigen

### Phase 4: Backend Integration (Tag 5)
- [ ] `/api/cockpit/my-day` Endpoint
- [ ] `/api/cockpit/focus-list` Endpoint
- [ ] `/api/team/updates` Endpoint
- [ ] Daten-Transformation
- [ ] Error Handling

### Phase 5: Testing & Polish (Tag 6)
- [ ] Unit Tests für Services
- [ ] Integration Tests für Hooks
- [ ] Performance-Messung
- [ ] Kosten-Monitoring (OpenAI)
- [ ] User Feedback sammeln

---

## 📋 M1 NAVIGATION ENHANCEMENT

### Breadcrumb Integration
- [ ] Breadcrumb Component erstellen
- [ ] Route-Hierarchie definieren
- [ ] In MainLayoutV2 integrieren
- [ ] Mobile-Responsive machen

### Global Search
- [ ] SearchBar Component
- [ ] Keyboard Shortcuts (Cmd+K)
- [ ] Search Service
- [ ] Ergebnis-Dropdown

---

## 📋 M2 QUICK CREATE

### FAB Implementation
- [ ] Floating Action Button
- [ ] Multi-Action Menu
- [ ] Keyboard Shortcuts
- [ ] Mobile-optimiert

### Quick Dialogs
- [ ] Customer Quick Create
- [ ] Opportunity Quick Create
- [ ] Task Quick Create
- [ ] Auto-Save Draft

---

## 📋 M7 SETTINGS ENHANCEMENT

### Neue Tabs
- [ ] "Integrationen" Tab
- [ ] "KI-Einstellungen" Tab
- [ ] "Team-Benachrichtigungen" Tab
- [ ] "Theme-Anpassung" Tab

### Integration Settings
- [ ] OpenAI API Config UI
- [ ] Polling-Intervalle UI
- [ ] Xentral Connection
- [ ] E-Mail Integration

---

## 🎯 DEFINITION OF DONE

### Pro Feature:
- [ ] Code implementiert
- [ ] Tests geschrieben (>80%)
- [ ] Dokumentation aktualisiert
- [ ] Review durchgeführt
- [ ] Performance gemessen
- [ ] In Produktion getestet

### Gesamt:
- [ ] Alle Module enhanced
- [ ] Integration getestet
- [ ] Performance-Budget eingehalten
- [ ] OpenAI-Kosten im Budget
- [ ] User-Feedback positiv

---

## 📊 ERFOLGS-METRIKEN

- **KI-Akzeptanz:** >80% der Vorschläge genutzt
- **Performance:** Alle Updates <30s
- **Kosten:** OpenAI <200€/Monat
- **User Satisfaction:** NPS >8
- **Produktivität:** +25% Tasks/Tag

---

## 🚀 NÄCHSTER SPRINT

Nach UI Foundation Enhancement:
1. **M4 Opportunity Pipeline** mit Kanban
2. **FC-011 Bonitätsprüfung** Integration
3. **FC-012 Team Communication** Real-time