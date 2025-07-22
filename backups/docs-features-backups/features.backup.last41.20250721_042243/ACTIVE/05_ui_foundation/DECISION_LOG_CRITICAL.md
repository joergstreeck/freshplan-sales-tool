# 🚨 UI FOUNDATION CRITICAL DECISIONS

**Zweck:** Nur die 3 kritischen Entscheidungen - Claude-optimiert  
**Erstellungsdatum:** 17.07.2025 19:50  
**Status:** 3 KRITISCHE ENTSCHEIDUNGEN BLOCKIEREN START!  
**Deadline:** 19.07.2025

---

## 🔥 KRITISCHE ENTSCHEIDUNGEN (BLOCKIEREN START!)

### **D1: KI-Integration Provider** 🤖 ✅ ENTSCHIEDEN

**Problem:** Sales Cockpit braucht KI-Priorisierung für MyDay Column

**Optionen:**
- **A) OpenAI API** - Extern, kostet pro Request, beste Qualität ✅ GEWÄHLT
- **B) Local Model** - Intern, einmalige Kosten, Datenschutz

**Impact:** Architektur-Entscheidung für alle KI-Features

**Entscheidung:** OpenAI API für MVP
- Budget: 50-200€/Monat akzeptiert
- Datenschutz: Nur aggregierte Daten, keine Kundennamen
- Migration zu Local Model später möglich

---

### **D2: Real-time Strategy** ⚡ ✅ ENTSCHIEDEN

**Problem:** Team-Updates und Live-Daten in Cockpit

**Optionen:**
- **A) Polling** - Alle 30s abfragen, einfach, Server-Last ✅ GEWÄHLT (MVP)
- **B) WebSocket** - Echtes Real-time, komplex, bessere UX (V2)

**Impact:** Performance und Architektur-Komplexität

**Entscheidung:** Polling für MVP, WebSocket für V2
- Polling-Intervall: 30 Sekunden
- React Query mit staleTime: 30000ms
- Migration-Pfad zu WebSocket vorbereitet

---

### **D3: Navigation Style** 🧭 ✅ ENTSCHIEDEN

**Problem:** Haupt-Navigation Layout für MainLayoutV2

**Optionen:**
- **A) Sidebar** - Vertikal, mehr Platz, Desktop-First ✅ GEWÄHLT
- **B) Top-Navigation** - Horizontal, kompakt, Mobile-First

**Impact:** Komplette UI-Struktur und User Experience

**Status:** ✅ IMPLEMENTIERT in `SidebarNavigation.tsx`
- 320px breit (erweitert) / 64px (collapsed)
- Freshfoodz CI integriert (grüner Border)
- Theme-Integration vorhanden

---

## 📋 ENTSCHEIDUNGSMATRIX

| ID | Entscheidung | Optionen | Gewählt | Status |
|----|--------------|----------|---------|--------|
| **D1** | KI-Provider | OpenAI vs. Local | OpenAI API | ✅ Entschieden |
| **D2** | Real-time | Polling vs. WebSocket | Polling (MVP) | ✅ Entschieden |
| **D3** | Navigation | Sidebar vs. Top | Sidebar | ✅ Implementiert |

---

## 🚀 NACH ENTSCHEIDUNGEN

**Sofort umsetzbar:**
- UI Foundation Enhancement kann starten
- KI-Features auf bestehende Basis aufbauen
- Real-time Features implementieren

**Blockiert ohne Entscheidungen:**
- Keine KI-Priorisierung möglich
- Keine Real-time Updates
- Navigation-Struktur unklar

---

## 📞 ESKALATION

**Bei Jörg klären:**
1. **D1:** KI-Provider Budget/Strategie
2. **D2:** Real-time Anforderungen
3. **D3:** Navigation UX-Präferenz

**Nach Entscheidungen:**
→ [M3_TECH_CONCEPT.md](/docs/features/ACTIVE/05_ui_foundation/M3_TECH_CONCEPT.md) ⭐

---

## 🔗 VOLLSTÄNDIGE ENTSCHEIDUNGEN

**Alle 8 Entscheidungen:** [DECISION_LOG.md](/docs/features/ACTIVE/05_ui_foundation/DECISION_LOG.md) (250 Zeilen)

**Diese Datei:** Nur die 3 kritischen für Claude-Kontext optimiert!